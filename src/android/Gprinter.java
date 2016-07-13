package cordova.plugin.gprint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Vector;

/**
 * This class echoes a string called from JavaScript.
 */
public class Gprinter extends CordovaPlugin {
    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
        }
    }

    public static final String ACTION_CONNECT_STATUS = "action.connect.status";
    private Activity _this = cordova.getActivity();
    private Context applicationContext = cordova.getActivity().getApplicationContext();
    private GpService mGpService = null;
    private PrinterServiceConnection printerServiceConnection = null;
    private EscCommand escCommand = new EscCommand();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initService")) {
            String message = args.getString(0);
            this.initService(callbackContext);
            return true;
        }
        if (action.equals("openPort")){
            int PrinterId=args.getInt(0);
            int PortType=args.getInt(1);
            String DeviceName =args.getString(2);
            int PortNumber= args.getInt(3);
            this.openPort(PrinterId,PortType,DeviceName,PortNumber,callbackContext);
            return true;
        }
        if (action.equals("closePort")){
            int PrinterId=args.getInt(0);
            closePort(PrinterId,callbackContext);
            return true;
        }
        if (action.equals("sendEscCommand")){
            int PrinterId=args.getInt(0);
            String base64=args.getString(1);
            this.sendEscCommand(PrinterId,base64,callbackContext);
            return true;
        }
        if (action.equals("printTestPage")) {
            int PrinterId=args.getInt(0);
            printTestPage(PrinterId,callbackContext);
            return true;
        }
        if (action.equals("resetEscCommand")) {
            resetEscCommand(callbackContext);
            return true;
        }
        return false;
    }

    private void initService(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(_this, GpPrintService.class);
                try {
                    applicationContext.startService(intent);
                    printerServiceConnection = new PrinterServiceConnection();
                    Thread.sleep(1000);
                    applicationContext.bindService(intent, printerServiceConnection, Context.BIND_AUTO_CREATE);
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }
    
    private void openPort(int PrinterId, int PortType, String DeviceName, int PortNumber, final CallbackContext callbackContext) {
        if (mGpService == null) {
            callbackContext.error("No Init Service");
            return;
        }
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_CONNECT_STATUS);
            _this.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                        int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
                        int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
                        if (type == GpDevice.STATE_CONNECTING) {
                            callbackContext.success();
                        } else if (type == GpDevice.STATE_NONE) {

                        } else if (type == GpDevice.STATE_VALID_PRINTER) {

                        } else if (type == GpDevice.STATE_INVALID_PRINTER) {

                        }
                    }

                }
            }, intentFilter);
            mGpService.openPort(PrinterId, PortType, DeviceName, PortNumber);

        } catch (RemoteException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void closePort(int PrinterId, final CallbackContext callbackContext) {
        if (mGpService == null) {
            callbackContext.error("No Init Service");
            return;
        }
        try {
            mGpService.closePort(PrinterId);
        } catch (RemoteException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void printTestPage(int PrinterId, CallbackContext callbackContext) {
        try {
            mGpService.printeTestPage(PrinterId);
            callbackContext.success();
        } catch (RemoteException e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void sendEscCommand(int PrinterId, String base64, CallbackContext callbackContext) {
        String sendString = null;
        if (base64 == null) {
            Vector<Byte> commands = escCommand.getCommand();
            byte[] formatbytes = new byte[commands.size()];
            for (int i = 0; i < commands.size(); i++) {
                formatbytes[i] = commands.get(i);
            }
            sendString = Base64.encodeToString(formatbytes, Base64.DEFAULT);
        } else {
            sendString = base64;
        }
        try {
            int ret = mGpService.sendEscCommand(PrinterId, sendString);
            if (GpCom.ERROR_CODE.SUCCESS == GpCom.ERROR_CODE.values()[ret]) {
                callbackContext.success();
            } else {
                callbackContext.error("Send EscCommand Fail");
            }
        } catch (RemoteException e) {
            callbackContext.error(e.getMessage());
        }

    }

    private void resetEscCommand(CallbackContext callbackContext){
        escCommand = new EscCommand();
        callbackContext.success();
    }

}

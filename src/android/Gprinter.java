package cordova.plugin.gprint;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gprinter.aidl.GpService;
import com.gprinter.service.GpPrintService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class Gprinter extends CordovaPlugin {
    private GpService mGpService = null;
    private PrinterServiceConnection printerServiceConnection = null;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceConnection", "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
            if (mGpService != null)
                try {
                    mGpService.isUserExperience(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initService")) {
            String message = args.getString(0);
            this.initService(callbackContext);
            return true;
        }
        if (action.equals("printTestPage")) {
            this.openPort();
            this.printTestPage(callbackContext);
            return true;
        }
        return false;
    }

    private void initService(CallbackContext callbackContext) {
        try {
            Context context = this.cordova.getActivity().getApplicationContext();
            printerServiceConnection = new PrinterServiceConnection();
            Intent intent = new Intent(cordova.getActivity(), GpPrintService.class);
            context.bindService(intent, printerServiceConnection, Context.BIND_AUTO_CREATE);
            callbackContext.success("InitSuccess");
        } catch (Exception e) {
            callbackContext.error("InitError");
        }
    }

    private void openPort() {
        try {
            mGpService.openPort(0, 4, "8C:DE:52:C7:5A:C8", 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Open port result");
        }
    }

    private void printTestPage(CallbackContext callbackContext) {

    }
}

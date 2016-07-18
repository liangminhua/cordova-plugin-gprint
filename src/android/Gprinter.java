package cordova.plugin.gprint;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class Gprinter extends CordovaPlugin {

    public static final String ACTION_CONNECT_STATUS = "action.connect.status";
    private static final String PRINTER_ID = "printId";
    private static final String PORT_TYPE = "portType";
    private static final String DEVICE_NAME = "deviceName";
    private static final String PORT_NUMBER = "portNumber";
    private static final String TIME_OUT = "timeOut";
    private static final String BASE64 = "base64";
    private GpService gpService = null;
    private PrinterServiceConnection printerServiceConnection = null;
    private EscCommand escCommand= null;
    private CallbackContext callbackContext;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            gpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            gpService = GpService.Stub.asInterface(service);
        }
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("initService")) {
            initService();
            return true;
        }
        if (action.equals("openPort")) {
            openPort(args);
            return true;
        }
        if (action.equals("closePort")) {
            closePort(args);
            return true;
        }
        if (action.equals("getPrinterConnectStatus")) {
            getPrinterConnectStatus(args);
            return true;
        }
        if (action.equals("printTestPage")) {
            printTestPage(args);
            return true;
        }
        if (action.equals("queryPrinterStatus")) {
            queryPrinterStatus(args);
            return true;
        }
        if (action.equals("getPrinterCommandType")) {
            getPrinterCommandType(args);
            return true;
        }
        if (action.equals("sendEscCommand")) {
            sendEscCommand(args);
            return true;
        }
        if (action.equals("sendTscComma")) {
            sendTscCommand(args);
            return true;
        }
        if (action.equals("print")) {
            test(args);
            return true;
        }
        return false;
    }

    private void initService() {
        Intent intent = new Intent(cordova.getActivity(), GpPrintService.class);
        Context context = cordova.getActivity().getApplicationContext();
        try {
            context.startService(intent);
            printerServiceConnection = new PrinterServiceConnection();
            Thread.sleep(1000);
            context.bindService(intent, printerServiceConnection, Context.BIND_AUTO_CREATE);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void openPort(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECT_STATUS);
        cordova.getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                    int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
                    int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
                    if (type == GpDevice.STATE_CONNECTING) {
                        callbackContext.success();
                        cordova.getActivity().unregisterReceiver(this);
                    } else if (type == GpDevice.STATE_NONE) {

                    } else if (type == GpDevice.STATE_VALID_PRINTER) {

                    } else if (type == GpDevice.STATE_INVALID_PRINTER) {

                    }
                }
            }
        }, intentFilter);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            int portType = jsonObject.getInt(PORT_TYPE);
            String deviceName = jsonObject.getString(DEVICE_NAME);
            int portNumber = jsonObject.getInt(PORT_NUMBER);
            gpService.openPort(printId, portType, deviceName, portNumber);
        } catch (JSONException e) {
            callbackContext.error("Invaild Parameters");
        } catch (RemoteException e) {
            callbackContext.error("Fail open port");
        }

    }

    private void closePort(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECT_STATUS);
        cordova.getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                    int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
                    int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
                    if (type == GpDevice.STATE_CONNECTING) {
                        // callbackContext.success();
                        cordova.getActivity().unregisterReceiver(this);
                    } else if (type == GpDevice.STATE_NONE) {
                        callbackContext.success();
                        cordova.getActivity().unregisterReceiver(this);

                    } else if (type == GpDevice.STATE_VALID_PRINTER) {

                    } else if (type == GpDevice.STATE_INVALID_PRINTER) {

                    }
                }
            }
        }, intentFilter);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            gpService.closePort(printId);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getPrinterConnectStatus(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            //int timeOut = jsonObject.getInt(TIME_OUT);
            int result = gpService.getPrinterConnectStatus(printId);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void printTestPage(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        try {
            jsonObject.getInt(PRINTER_ID);
            gpService.printeTestPage(0);
        } catch (JSONException e) {
            callbackContext.error("Invaild Parameters");
        } catch (RemoteException e) {
            callbackContext.error("Fail Print Test Page");
        }
    }

    private void queryPrinterStatus(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            int timeOut = jsonObject.getInt(TIME_OUT);
            int result = gpService.queryPrinterStatus(printId,timeOut);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getPrinterCommandType(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            int result = gpService.getPrinterCommandType(printId);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendEscCommand(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            String base64 =jsonObject.getString(BASE64);
            int result = gpService.sendEscCommand(printId,base64);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendTscCommand(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID);
            String base64 =jsonObject.getString(BASE64);
            int result = gpService.sendLabelCommand(printId,base64);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void test(final JSONArray args){

    }

    private void checkInitService() {
        if (gpService == null)
            callbackContext.error("please initService first!");
    }

    private JSONObject getArgsObject(JSONArray args) {
        if (args.length() == 1) {
            try {
                return args.getJSONObject(0);
            } catch (JSONException ex) {
            }
        }

        return null;
    }

    private void addProperty(JSONObject obj, String key, Object value) {
        //Believe exception only occurs when adding duplicate keys, so just ignore it
        try {
            if (value == null) {
                obj.put(key, JSONObject.NULL);
            } else {
                obj.put(key, value);
            }
        } catch (JSONException e) {
        }
    }
}

package cordova.plugin.gprint;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.service.GpPrintService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * This class echoes a string called from JavaScript.
 */
public class Gprinter extends CordovaPlugin {

    private static final String ACTION_CONNECT_STATUS = "action.connect.status";
    private static final String PRINTER_ID_PARAM = "printId";
    private static final String PORT_TYPE_PARAM = "portType";
    private static final String DEVICE_NAME_PARAM = "deviceName";
    private static final String PORT_NUMBER_PARAM = "portNumber";
    private static final String TIME_OUT_PARAM = "timeOut";
    private static final String BASE64_PARAM = "base64";
    private static final String FUCTION_NAME_PARAM = "functionName";
    private static final String TEXT_PARAM = "text";
    private static final String CHARSETNAME_PARAM = "charsetName";
    private static final String CONTENT_PARAM = "content";
    private static final String ENABLE_PARAM = "enable";
    private GpService gpService = null;
    private PrinterServiceConnection printerServiceConnection = null;
    private CallbackContext callbackContext;
    private CallbackContext connectionCallbackContext;
    private static final String LOG_TAG = "Gprinter";
    BroadcastReceiver receiver;

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
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        initService();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECT_STATUS);
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Gprinter.this.webView != null) {
                        updatePrinterInfo(intent);
                    }
                }
            };
            webView.getContext().registerReceiver(this.receiver, intentFilter);
        }
    }

    @Override
    public void onDestroy() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
            } catch (Exception e) {
                Log.e(LOG_TAG, "onDestroy: Error unregistering printer receiver. " + e.getMessage(), e);
            } finally {
                receiver = null;
            }
        }

    }

    private void updatePrinterInfo(Intent intent) {
        if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
            int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
            int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
            JSONObject info = new JSONObject();
            addProperty(info, "id", id);
            addProperty(info, "connectStatus", type);
            sendUpdate(info);
        }
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param obj the printer info to set as navigator.connection
     */
    private void sendUpdate(JSONObject obj) {
        if (connectionCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            connectionCallbackContext.sendPluginResult(result);
        }
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("getConnectionInfo")) {
            this.connectionCallbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        this.callbackContext = callbackContext;
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
        if (action.equals("getCommand")) {
            getCommand(args);
            return true;
        }
        if (action.equals("sendEscCommand")) {
            sendEscCommand(args);
            return true;
        }
        if (action.equals("sendTscCommand")) {
            sendTscCommand(args);
            return true;
        }
        return false;
    }

    private void initService() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(cordova.getActivity(), GpPrintService.class);
                Context context = cordova.getActivity().getApplicationContext();
                try {
                    context.startService(intent);
                    printerServiceConnection = new PrinterServiceConnection();
                    context.bindService(intent, printerServiceConnection, Context.BIND_AUTO_CREATE);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error(InitService): ",e );
                }
            }
        });
    }

    private void openPort(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            int portType = jsonObject.getInt(PORT_TYPE_PARAM);
            String deviceName = jsonObject.getString(DEVICE_NAME_PARAM);
            int portNumber = jsonObject.getInt(PORT_NUMBER_PARAM);
            int errorCode = gpService.openPort(printId, portType, deviceName, portNumber);
            callbackContext.success(errorCode);
        } catch (JSONException e) {
            callbackContext.error("Invaild Parameters");
        } catch (RemoteException e) {
            if (callbackContext != null)
                callbackContext.error("Error(OpenPort):" + e.getMessage());
        }

    }

    private void closePort(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            gpService.closePort(printId);
            callbackContext.success();
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(ClosePort):" + e.getMessage());
        }
    }

    private void getPrinterConnectStatus(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            int connectStatus = gpService.getPrinterConnectStatus(printId);
            callbackContext.success(connectStatus);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(GetPrinterConnectStatus):" + e.getMessage());
        }
    }

    private void printTestPage(final JSONArray args) {
        checkInitService();
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printerId = jsonObject.getInt(PRINTER_ID_PARAM);
            int errorCode = gpService.printeTestPage(printerId);
            callbackContext.success(errorCode);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(PrintTestPage):" + e.getMessage());
        }
    }

    private void queryPrinterStatus(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            int timeOut = jsonObject.getInt(TIME_OUT_PARAM);
            int printerStatus = gpService.queryPrinterStatus(printId, timeOut);
            callbackContext.success(printerStatus);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(QueryPrinterStatus):" + e.getMessage());
        }
    }

    private void getPrinterCommandType(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            int command = gpService.getPrinterCommandType(printId);
            callbackContext.success(command);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(QueryPrinterStatus):" + e.getMessage());
        }
    }

    private void sendEscCommand(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            String base64 = jsonObject.getString(BASE64_PARAM);
            int errorCode = gpService.sendEscCommand(printId, base64);
            callbackContext.success(errorCode);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(SendEscCommand):" + e.getMessage());
        }
    }

    private void sendTscCommand(final JSONArray args) {
        JSONObject jsonObject = getArgsObject(args);
        try {
            int printId = jsonObject.getInt(PRINTER_ID_PARAM);
            String base64 = jsonObject.getString(BASE64_PARAM);
            int errorCode = gpService.sendLabelCommand(printId, base64);
            callbackContext.success(errorCode);
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        } catch (RemoteException e) {
            callbackContext.error("Error(SendTscCommand):" + e.getMessage());
        }
    }

    private void checkInitService() {
        if (gpService == null) {
            initService();
        }
    }

    private void getCommand(final JSONArray args) {
        EscCommand escCommand = new EscCommand();
        try {
            for (int i = 0; i < args.length(); ++i) {
                JSONObject jsonObject = args.getJSONObject(i);
                String functionName = jsonObject.getString(FUCTION_NAME_PARAM);
                if ("addHorTab".equals(functionName)) {
                    escCommand.addHorTab();
                } else if ("addText".equals(functionName)) {
                    String text = jsonObject.getString(TEXT_PARAM);
                    String charsetName = jsonObject.optString(CHARSETNAME_PARAM, null);
                    escCommand.addText(text, charsetName);
                } else if ("addSound".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    int t = jsonObject.getInt("t");
                    escCommand.addSound((byte) n, (byte) t);
                } else if ("addCancelKanjiMode".equals(functionName)) {
                    escCommand.addCancelKanjiMode();
                } else if ("addSetKanjiLefttandRightSpace".equals(functionName)) {
                    int left = jsonObject.getInt("left");
                    int right = jsonObject.getInt("right");
                    escCommand.addSetKanjiLefttandRightSpace((byte) left, (byte) right);
                } else if ("addSetQuadrupleModeForKanji".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addSetQuadrupleModeForKanji(enable);
                } else if ("addRastBitImage".equals(functionName)) {
                    String bitmapBase64 = jsonObject.getString("bitmap");
                    int nWidth = jsonObject.getInt("nWidth");
                    int nMode = jsonObject.getInt("nMode");
                    byte[] bytes = Base64.decode(bitmapBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        escCommand.addRastBitImage(bitmap, nWidth, nMode);
                    }
                } else if ("addDownloadNvBitImage".equals(functionName)) {
                    JSONArray bitmapArray = jsonObject.getJSONArray("bitmap");
                    int length = bitmapArray.length();
                    Bitmap[] bitmaps = new Bitmap[length];
                    for (int index = 0; index < length; ++index) {
                        String bitmapBase64 = bitmapArray.getString(index);
                        byte[] bytes = Base64.decode(bitmapBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        bitmaps[index] = bitmap;
                    }
                    escCommand.addDownloadNvBitImage(bitmaps);
                } else if ("addPrintNvBitmap".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    int mode = jsonObject.getInt("mode");
                    escCommand.addPrintNvBitmap((byte) n, (byte) mode);
                } else if ("addUPCA".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addUPCA(content);
                } else if ("addUPCE".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addUPCE(content);
                } else if ("addEAN13".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addEAN13(content);
                } else if ("addEAN8".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addEAN8(content);
                } else if ("addCODE39".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addCODE39(content);
                } else if ("addCODE93".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addCODE93(content);
                } else if ("addCODE128".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addCODE128(content);
                } else if ("addEAN13".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addEAN13(content);
                } else if ("addITF".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addITF(content);
                } else if ("addPrintAndLineFeed".equals(functionName)) {
                    escCommand.addPrintAndLineFeed();
                } else if ("addPrintAndFeedLines".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addPrintAndFeedLines((byte) n);
                } else if ("addSetRightSideCharacterSpacing".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetRightSideCharacterSpacing((byte) n);
                } else if ("addSelectPrintModes".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    EscCommand.ENABLE emphasized = EscCommand.ENABLE.valueOf(jsonObject.getString("emphasized"));
                    EscCommand.ENABLE doubleheight = EscCommand.ENABLE.valueOf(jsonObject.getString("doubleheight"));
                    EscCommand.ENABLE doublewidth = EscCommand.ENABLE.valueOf(jsonObject.getString("doublewidth"));
                    EscCommand.ENABLE underline = EscCommand.ENABLE.valueOf(jsonObject.getString("underline"));
                    escCommand.addSelectPrintModes(font, emphasized, doubleheight, doublewidth, underline);
                } else if ("addSetAbsolutePrintPosition".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetAbsolutePrintPosition((short) n);
                } else if ("addTurnUnderlineModeOnOrOff".equals(functionName)) {
                    String underline = jsonObject.getString("underline");
                    EscCommand.UNDERLINE_MODE underlineMode = EscCommand.UNDERLINE_MODE.valueOf(underline);
                    escCommand.addTurnUnderlineModeOnOrOff(underlineMode);
                } else if ("addSelectDefualtLineSpacing ".equals(functionName)) {
                    escCommand.addSelectDefualtLineSpacing();
                } else if ("addSetLineSpacing".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetLineSpacing((byte) n);
                } else if ("addInitializePrinter".equals(functionName)) {
                    escCommand.addInitializePrinter();
                } else if ("addTurnEmphasizedModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addTurnEmphasizedModeOnOrOff(enable);
                } else if ("addTurnDoubleStrikeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addTurnDoubleStrikeOnOrOff(enable);
                } else if ("addPrintAndFeedPaper".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addPrintAndFeedPaper((byte) n);
                } else if ("addSelectCharacterFont".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    escCommand.addSelectCharacterFont(font);
                } else if ("addSelectInternationalCharacterSet".equals(functionName)) {
                    EscCommand.CHARACTER_SET character_set = EscCommand.CHARACTER_SET.valueOf(jsonObject.getString("charset"));
                    escCommand.addSelectInternationalCharacterSet(character_set);
                } else if ("addTurn90ClockWiseRotatin".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addTurn90ClockWiseRotatin(enable);
                } else if ("addSetRelativePrintPositon".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetRelativePrintPositon((short) n);
                } else if ("addSelectJustification".equals(functionName)) {
                    EscCommand.JUSTIFICATION justification = EscCommand.JUSTIFICATION.valueOf(jsonObject.getString("justification"));
                    escCommand.addSelectJustification(justification);
                } else if ("addSelectCodePage".equals(functionName)) {
                    EscCommand.CODEPAGE codepage = EscCommand.CODEPAGE.valueOf(jsonObject.getString("codepage"));
                    escCommand.addSelectCodePage(codepage);
                } else if ("addTurnUpsideDownModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addTurnUpsideDownModeOnOrOff(enable);
                } else if ("addSetCharcterSize".equals(functionName)) {
                    String width = jsonObject.getString("width");
                    String height = jsonObject.getString("height");
                    EscCommand.WIDTH_ZOOM width_zoom = EscCommand.WIDTH_ZOOM.valueOf(width);
                    EscCommand.HEIGHT_ZOOM height_zoom = EscCommand.HEIGHT_ZOOM.valueOf(height);
                    escCommand.addSetCharcterSize(width_zoom, height_zoom);
                } else if ("addTurnReverseModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addTurnReverseModeOnOrOff(enable);
                } else if ("addSelectPrintingPositionForHRICharacters".equals(functionName)) {
                    String _hri_position = jsonObject.getString("HRI_POSITION");
                    EscCommand.HRI_POSITION hri_position = EscCommand.HRI_POSITION.valueOf(_hri_position);
                    escCommand.addSelectPrintingPositionForHRICharacters(hri_position);
                } else if ("addSetLeftMargin".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetLeftMargin((short) n);
                } else if ("addSetHorAndVerMotionUnits".equals(functionName)) {
                    int x = jsonObject.getInt("x");
                    int y = jsonObject.getInt("y");
                    escCommand.addSetHorAndVerMotionUnits((byte) 5, (byte) y);
                } else if ("addCutAndFeedPaper".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addCutAndFeedPaper((byte) n);
                } else if ("addCutPaper".equals(functionName)) {
                    escCommand.addCutPaper();
                } else if ("addSetPrintingAreaWidth".equals(functionName)) {
                    int length = jsonObject.getInt("length");
                    escCommand.addSetPrintingAreaWidth((short) length);
                } else if ("addSelectKanjiMode".equals(functionName)) {
                    escCommand.addSelectKanjiMode();
                } else if ("addSetKanjiUnderLine".equals(functionName)) {
                    EscCommand.UNDERLINE_MODE underline_mode = EscCommand.UNDERLINE_MODE.valueOf(jsonObject.getString("UNDERLINE_MODE"));
                    escCommand.addSetKanjiUnderLine(underline_mode);
                } else if ("addCancelKanjiMode".equals(functionName)) {
                    escCommand.addCancelKanjiMode();
                } else if ("addSetKanjiLeftAndRightSpacing".equals(functionName)) {
                    int left = jsonObject.getInt("left");
                    int right = jsonObject.getInt("right");
                    escCommand.addSetKanjiLefttandRightSpace((byte) left, (byte) right);
                } else if ("addSetQuadrupleModeForKanji".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString(ENABLE_PARAM));
                    escCommand.addSetQuadrupleModeForKanji(enable);
                } else if ("addSetFontForHRICharacter".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    escCommand.addSetFontForHRICharacter(font);
                } else if ("addSetBarcodeHeight".equals(functionName)) {
                    int width = jsonObject.getInt("width");
                    escCommand.addSetLeftMargin((byte) width);
                } else if ("addSetBarcodeWidth".equals(functionName)) {
                    int height = jsonObject.getInt("height");
                    escCommand.addSetLeftMargin((byte) height);
                } else if ("addSelectSizeOfModuleForQRCode".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSelectSizeOfModuleForQRCode((byte) n);
                } else if ("addSelectErrorCorrectionLevelForQRCode".equals(functionName)) {
                    int n = jsonObject.getInt("height");
                    escCommand.addSelectErrorCorrectionLevelForQRCode((byte) n);
                } else if ("addStoreQRCodeData".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT_PARAM);
                    escCommand.addStoreQRCodeData(content);
                } else if ("addPrintQRCode ".equals(functionName)) {
                    escCommand.addPrintQRCode();
                } else if ("addUserCommand".equals(functionName)) {
                    JSONArray arrays = jsonObject.getJSONArray("command");
                    byte[] bytes = new byte[arrays.length()];
                    for (int index = 0; index < arrays.length(); ++index) {
                        bytes[index] = (byte) arrays.getInt(index);
                    }
                    escCommand.addUserCommand(bytes);
                } else {
                    callbackContext.error("Error(FunctionName):no match " + functionName + ".");
                    return;
                }
            }
        } catch (JSONException e) {
            callbackContext.error("Error(Parameters):" + e.getMessage());
        }
        Vector<Byte> Bytes = escCommand.getCommand();
        byte[] bytes = new byte[Bytes.size()];
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = Bytes.get(i);
        }
        String ret = Base64.encodeToString(bytes, Base64.DEFAULT);
        callbackContext.success(ret);
    }

    private JSONObject getArgsObject(JSONArray args) {
        if (args.length() == 1) {
            try {
                return args.getJSONObject(0);
            } catch (JSONException ex) {
                return null;
            }
        } else {
            return null;

        }

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


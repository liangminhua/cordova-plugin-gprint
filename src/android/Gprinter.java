package cordova.plugin.gprint;

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
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * This class echoes a string called from JavaScript.
 */
public class Gprinter extends CordovaPlugin {

    private static final String ACTION_CONNECT_STATUS = "action.connect.status";
    private static final String PRINTER_ID = "printId";
    private static final String PORT_TYPE = "portType";
    private static final String DEVICE_NAME = "deviceName";
    private static final String PORT_NUMBER = "portNumber";
    private static final String TIME_OUT = "timeOut";
    private static final String BASE64 = "base64";
    private static final String FUCTION_NAME = "functionName";
    private static final String TEXT = "text";
    private static final String CHARSETNAME = "charsetName";
    private static final String CONTENT ="content" ;
    private GpService gpService = null;
    private PrinterServiceConnection printerServiceConnection = null;
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
        if (action.equals("getCommand"))
        {
            getCommand(args);
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
                    Thread.sleep(1000);
                    context.bindService(intent, printerServiceConnection, Context.BIND_AUTO_CREATE);
                    callbackContext.success();


                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
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
            int result = gpService.printeTestPage(0);
            callbackContext.success(result);
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
            int result = gpService.queryPrinterStatus(printId, timeOut);
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
            String base64 = jsonObject.getString(BASE64);
            int result = gpService.sendEscCommand(printId, base64);
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
            String base64 = jsonObject.getString(BASE64);
            int result = gpService.sendLabelCommand(printId, base64);
            callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkInitService() {
        if (gpService == null)
            callbackContext.error("please initService first!");
    }

    private void getCommand(final JSONArray args) {
        EscCommand escCommand = new EscCommand();
        try {
            for (int i = 0; i < args.length(); ++i) {
                JSONObject jsonObject = args.getJSONObject(i);
                String functionName = jsonObject.getString(FUCTION_NAME);
                if ("addHorTab".equals(functionName)) {
                    escCommand.addHorTab();
                }
                if ("addText".equals(functionName)) {
                    String text = jsonObject.getString(TEXT);
                    String charsetName = jsonObject.optString(CHARSETNAME, null);
                    escCommand.addText(text, charsetName);
                }
                if ("addSound".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    int t = jsonObject.getInt("t");
                    escCommand.addSound((byte) n, (byte) t);
                }
                if ("addCancelKanjiMode".equals(functionName)) {
                    escCommand.addCancelKanjiMode();
                }
                if ("addSetKanjiLefttandRightSpace".equals(functionName)) {
                    int left = jsonObject.getInt("left");
                    int right = jsonObject.getInt("right");
                    escCommand.addSetKanjiLefttandRightSpace((byte) left, (byte) right);
                }
                if ("addSetQuadrupleModeForKanji".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addSetQuadrupleModeForKanji(enable);
                }
                if ("addRastBitImage".equals(functionName)) {

                }
                if ("addDownloadNvBitImage".equals(functionName)) {

                }
                if ("addPrintNvBitmap".equals(functionName)) {

                }
                if ("addUPCA".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addUPCA(content);
                }
                if ("addUPCE".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addUPCE(content);
                }
                if ("addEAN13".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addEAN13(content);
                }
                if ("addEAN8".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addEAN8(content);
                }
                if ("addCODE39".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addCODE39(content);
                }
                if ("addCODE93".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addCODE93(content);
                }
                if ("addCODE128".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addCODE128(content);
                }
                if ("addEAN13".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addEAN13(content);
                }
                if ("addITF".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addITF(content);
                }
                if ("addPrintAndLineFeed".equals(functionName)) {
                    escCommand.addPrintAndLineFeed();
                }
                if ("addPrintAndFeedLines".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addPrintAndFeedLines((byte) n);
                }
                if ("addSetRightSideCharacterSpacing".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetRightSideCharacterSpacing((byte) n);
                }
                if ("addSelectPrintModes".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    EscCommand.ENABLE emphasized = EscCommand.ENABLE.valueOf(jsonObject.getString("emphasized"));
                    EscCommand.ENABLE doubleheight = EscCommand.ENABLE.valueOf(jsonObject.getString("doubleheight"));
                    EscCommand.ENABLE doublewidth = EscCommand.ENABLE.valueOf(jsonObject.getString("doublewidth"));
                    EscCommand.ENABLE underline = EscCommand.ENABLE.valueOf(jsonObject.getString("underline"));
                    escCommand.addSelectPrintModes(font,emphasized,doubleheight,doublewidth,underline);
                }
                if ("addSetAbsolutePrintPosition".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetAbsolutePrintPosition((short) n);
                }
                if ("addTurnUnderlineModeOnOrOff".equals(functionName)) {
                    String underline = jsonObject.getString("underline");
                    EscCommand.UNDERLINE_MODE underlineMode = EscCommand.UNDERLINE_MODE.valueOf(underline);
                    escCommand.addTurnUnderlineModeOnOrOff(underlineMode);
                }
                if ("addSelectDefualtLineSpacing ".equals(functionName)) {
                    escCommand.addSelectDefualtLineSpacing();
                }
                if ("addSetLineSpacing".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetLineSpacing((byte) n);
                }
                if ("addInitializePrinter".equals(functionName)) {
                    escCommand.addInitializePrinter();
                }
                if ("addTurnEmphasizedModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addTurnEmphasizedModeOnOrOff(enable);
                }
                if ("addTurnDoubleStrikeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addTurnDoubleStrikeOnOrOff(enable);
                }
                if ("addPrintAndFeedPaper".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addPrintAndFeedPaper((byte) n);
                }
                if ("addSelectCharacterFont".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    escCommand.addSelectCharacterFont(font);
                }
                if ("addSelectInternationalCharacterSet".equals(functionName)) {
                    EscCommand.CHARACTER_SET character_set = EscCommand.CHARACTER_SET.valueOf(jsonObject.getString("charset"));
                    escCommand.addSelectInternationalCharacterSet(character_set);
                }
                if ("addTurn90ClockWiseRotatin".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addTurn90ClockWiseRotatin(enable);
                }
                if ("addSetRelativePrintPositon".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetRelativePrintPositon((short) n);
                }
                if ("addSelectJustification".equals(functionName)) {
                    EscCommand.JUSTIFICATION justification = EscCommand.JUSTIFICATION.valueOf(jsonObject.getString("justification"));
                    escCommand.addSelectJustification(justification);

                }
                if ("addSelectCodePage".equals(functionName)) {
                    EscCommand.CODEPAGE codepage = EscCommand.CODEPAGE.valueOf(jsonObject.getString("codepage"));
                    escCommand.addSelectCodePage(codepage);
                }
                if ("addTurnUpsideDownModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addTurnUpsideDownModeOnOrOff(enable);
                }
                if ("addSetCharcterSize".equals(functionName)) {
                    String width = jsonObject.getString("width");
                    String height = jsonObject.getString("height");
                    EscCommand.WIDTH_ZOOM width_zoom = EscCommand.WIDTH_ZOOM.valueOf(width);
                    EscCommand.HEIGHT_ZOOM height_zoom = EscCommand.HEIGHT_ZOOM.valueOf(height);
                    escCommand.addSetCharcterSize(width_zoom, height_zoom);
                }
                if ("addTurnReverseModeOnOrOff".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addTurnReverseModeOnOrOff(enable);
                }
                if ("addSelectPrintingPositionForHRICharacters".equals(functionName)) {
                    String _hri_position = jsonObject.getString("HRI_POSITION");
                    EscCommand.HRI_POSITION hri_position = EscCommand.HRI_POSITION.valueOf(_hri_position);
                    escCommand.addSelectPrintingPositionForHRICharacters(hri_position);
                }
                if ("addSetLeftMargin".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSetLeftMargin((short) n);
                }
                if ("addSetHorAndVerMotionUnits".equals(functionName)) {
                    int x = jsonObject.getInt("x");
                    int y = jsonObject.getInt("y");
                    escCommand.addSetHorAndVerMotionUnits((byte) x, (byte) y);
                }
                if ("addCutAndFeedPaper".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addCutAndFeedPaper((byte) n);
                }
                if ("addCutPaper".equals(functionName)) {
                    escCommand.addCutPaper();
                }
                if ("addSetPrintingAreaWidth".equals(functionName)) {
                    int length = jsonObject.getInt("length");
                    escCommand.addSetPrintingAreaWidth((short) length);
                }
                if ("addSelectKanjiMode".equals(functionName)) {
                    escCommand.addSelectKanjiMode();
                }
                if ("addSetKanjiUnderLine".equals(functionName)) {
                    EscCommand.UNDERLINE_MODE underline_mode = EscCommand.UNDERLINE_MODE.valueOf(jsonObject.getString("UNDERLINE_MODE"));
                    escCommand.addSetKanjiUnderLine(underline_mode);
                }
                if ("addCancelKanjiMode".equals(functionName)) {
                    escCommand.addCancelKanjiMode();
                }
                if ("addSetKanjiLeftAndRightSpacing".equals(functionName)) {
                    int left = jsonObject.getInt("left");
                    int right = jsonObject.getInt("right");
                    escCommand.addSetKanjiLefttandRightSpace((byte) left, (byte) right);
                }
                if ("addSetQuadrupleModeForKanji".equals(functionName)) {
                    EscCommand.ENABLE enable = EscCommand.ENABLE.valueOf(jsonObject.getString("enable"));
                    escCommand.addSetQuadrupleModeForKanji(enable);
                }
                if ("addSetFontForHRICharacter".equals(functionName)) {
                    EscCommand.FONT font = EscCommand.FONT.valueOf(jsonObject.getString("font"));
                    escCommand.addSetFontForHRICharacter(font);
                }
                if ("addSetBarcodeHeight".equals(functionName)) {
                    int width = jsonObject.getInt("width");
                    escCommand.addSetLeftMargin((byte) width);
                }
                if ("addSetBarcodeWidth".equals(functionName)) {
                    int height = jsonObject.getInt("height");
                    escCommand.addSetLeftMargin((byte) height);
                }
//QRCODE
                if ("addSelectSizeOfModuleForQRCode".equals(functionName)) {
                    int n = jsonObject.getInt("n");
                    escCommand.addSelectSizeOfModuleForQRCode((byte) n);
                }
                if ("addSelectErrorCorrectionLevelForQRCode".equals(functionName)) {
                    int n = jsonObject.getInt("height");
                    escCommand.addSelectErrorCorrectionLevelForQRCode((byte) n);
                }
                if ("addStoreQRCodeData".equals(functionName)) {
                    String content = jsonObject.getString(CONTENT);
                    escCommand.addStoreQRCodeData(content);
                }
                if ("addPrintQRCode ".equals(functionName)) {
                    escCommand.addPrintQRCode ();
                }
                if ("addUserCommand".equals(functionName)){
//                    escCommand.addUserCommand();
                }
                //end QRCODE
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
        }else {
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


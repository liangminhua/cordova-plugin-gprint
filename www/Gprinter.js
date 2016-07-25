    var exec = require('cordova/exec');
    var gprinterName = "Gprinter";
    var bluetoothleName = "BluetoothLePlugin";
    var gprinter = {
        initService: function (success, error) {
            alert("initService");
            cordova.exec(success, success, gprinterName, "initService", []);
        },
        stopService: function (success, error) {
            alert("stopService");
            cordova.exec(success, success, gprinterName, "stopService", []);
        },
        openPort: function (success, error, params) {
            alert("openPort");
            cordova.exec(success, success, gprinterName, "openPort", [params]);
        },
        closePort: function (success, error, params) {
            alert("closePort");
            cordova.exec(success, success, gprinterName, "closePort", [params]);
        },
        getPrinterConnectStatus: function (success, error, params) {
            alert("getPrinterConnectStatus");
            cordova.exec(success, error, gprinterName, "getPrinterConnectStatus", [params]);
        },
        printTestPage: function (success, error, params) {
            alert("printTestPage");
            cordova.exec(success, error, gprinterName, "printTestPage", [params]);
        },
        queryPrinterStatus: function (success, error, params) {
            alert("queryPrinterStatus");
            cordova.exec(success, error, gprinterName, "queryPrinterStatus", [params]);
        },
        getPrinterCommandType: function (success, error, params) {
            alert("getPrinterCommandType");
            cordova.exec(success, error, gprinterName, "getPrinterCommandType", [params]);
        },
        sendEscCommand: function (success, error, params) {
            alert("sendEscCommand");
            cordova.exec(success, error, gprinterName, "sendEscCommand", [params]);
        },
        sendTscCommand: function (success, error, params) {
            alert("sendTscCommand");
            cordova.exec(success, error, gprinterName, "sendTscCommand", [params]);
        },
        getCommand: function (success, error, params) {
            alert('getCommand');
            cordova.exec(success, error, gprinterName, "getCommand", params);
        },
        print: function (success, error, params) {
            alert("test");
            cordova.exec(success, error, gprinterName, "test", [params]);
        }
    }
    module.exports = gprinter;



    var exec = require('cordova/exec');
    var channel = require('cordova/channel');
    var utils = require('cordova/utils');
    var gprinterName = "Gprinter";
    var bluetoothleName = "BluetoothLePlugin";
    var gprinter = {
        initService: function (success, error) {
            cordova.exec(success, success, gprinterName, "initService", []);
        },
        stopService: function (success, error) {
            cordova.exec(success, success, gprinterName, "stopService", []);
        },
        openPort: function (success, error, params) {
            cordova.exec(success, success, gprinterName, "openPort", [params]);
        },
        closePort: function (success, error, params) {
            cordova.exec(success, success, gprinterName, "closePort", [params]);
        },
        getPrinterConnectStatus: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "getPrinterConnectStatus", [params]);
        },
        printTestPage: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "printTestPage", [params]);
        },
        queryPrinterStatus: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "queryPrinterStatus", [params]);
        },
        getPrinterCommandType: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "getPrinterCommandType", [params]);
        },
        sendEscCommand: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "sendEscCommand", [params]);
        },
        sendTscCommand: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "sendTscCommand", [params]);
        },
        getCommand: function (success, error, params) {
            cordova.exec(success, error, gprinterName, "getCommand", params);
        }
    }
    channel.createSticky('onCordovaConnectionReady');
    channel.waitForInitialization('onCordovaConnectionReady');
    channel.onCordovaReady.subscribe(function () {
        exec(function (params) {
            if (params != "OK") {
                cordova.fireDocumentEvent("printerConnect", params, false);
            }
            if (channel.onCordovaConnectionReady.state !== 2) {
                channel.onCordovaConnectionReady.fire();
            }
        }, function (params) {
            // If we can't get the network info we should still tell Cordova
            // to fire the deviceready event.
            if (channel.onCordovaConnectionReady.state !== 2) {
                channel.onCordovaConnectionReady.fire();
            }
            console.log("Error initializing gprinter Connection: " + e);
        }, gprinterName, "getConnectionInfo", []);
    });

    module.exports = gprinter;


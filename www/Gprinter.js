var exec = require('cordova/exec');

exports.initService = function (arg0, success, error) {
    exec(success, error, "Gprinter", "initService", [arg0]);
};
exports.openPort = function (arg0, success, error) {
    exec(success, error, "Gprinter", "openPort", arg0);
};
exports.closePort = function (arg0, success, error) {
    exec(success, error, "Gprinter", "closePort", arg0);
};
exports.sendEscCommand = function (arg0, success, error) {
    exec(success, error, "Gprinter", "sendEscCommand", arg0);
};
exports.printTestPage = function (arg0, success, error) {
    exec(success, error, "Gprinter", "printTestPage", arg0);
};
exports.resetEscCommand = function (arg0, success, error) {
    exec(success, error, "Gprinter", "resetEscCommand", arg0);
};
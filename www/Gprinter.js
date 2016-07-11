var exec = require('cordova/exec');

exports.initService = function(arg0, success, error) {
    exec(success, error, "Gprinter", "initService", [arg0]);
};
exports.printTestPage = function(arg0, success, error) {
    exec(success, error, "Gprinter", "printTestPage", [arg0]);
};
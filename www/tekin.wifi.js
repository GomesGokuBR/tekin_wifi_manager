var exec = require('cordova/exec');

exports.toggleWifi = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'toggleWifi', [arg0]);
};

exports.getListeWifi = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'getListeWifi', []);
};

exports.connect = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'connect', [arg0]);
};

exports.gpsAndWifiState = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'gpsAndWifiState', []);
};

exports.hasPermissions = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'hasPermissions', []);
};

exports.requestPermission = function (arg0, success, error) {
    exec(success, error, 'Wifi', 'requestPermission', []);
};

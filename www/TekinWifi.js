var exec = require('cordova/exec');

exports.toggleWifi = function (arg0, success, error) {
    exec(success, error, 'TekinWifi', 'toggleWifi', [arg0]);
};

exports.getListeWifi = function (arg0, success, error) {
    exec(success, error, 'TekinWifi', 'getListeWifi', [arg0]);
};

var exec = require('cordova/exec');

/**
 * Initialize the Facebook SDK
 */
exports.initializeSdk = function(successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'FacebookEventsPlugin', 'initializeSdk');
};

/**
 * Log event in the Facebook SDK
 */
exports.logEvent = function(params, successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'FacebookEventsPlugin', 'logEvent', [params]);
};

/**
 * Log purchase event in the Facebook SDK
 */
exports.logPurchase = function(value, currency, successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'FacebookEventsPlugin', 'logPurchase', [value, currency]);
};

/**
 * Deactivate App in the Facebook SDK
 */
exports.deactivateApp = function(activateApp, successCallback, errorCallback) {
    exec(successCallback, errorCallback,  'FacebookEventsPlugin', 'deactivateApp', activateApp);
};
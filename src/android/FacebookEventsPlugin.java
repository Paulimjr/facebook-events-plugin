package com.facebook.events.plugin;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;


/**
 * Innovagency Mobile Team
 * <p>
 * Author: Paulo Cesar
 * Date: 20-04-2010
 */
public class FacebookEventsPlugin extends CordovaPlugin {

    private static final String TAG = FacebookEventsPlugin.class.getSimpleName();
    // Handler to execute in Second Thread
    // Create a background thread
    private CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action != null) {

            switch (action) {
                case "initializeSdk":
                    initializeSdk();
                    break;
                case "logEvent":
                    logEvent(args);
                    break;
                case "logPurchase":
                    logPurchase(args);
                    break;
                default:
                    this.callbackContext.error("Invalid or not found action!");
            }

        } else {
            this.callbackContext.error("Invalid or not found action!");
        }

        return true;

    }

    private void initializeSdk() {
        try {
            FacebookSdk.sdkInitialize(this.cordova.getActivity().getApplication());
            AppEventsLogger.activateApp(this.cordova.getActivity().getApplication());

            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.setAdvertiserIDCollectionEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
            FacebookSdk.fullyInitialize();

            if (FacebookSdk.isInitialized()) {
                Log.v(TAG, "FacebookSdk initialized");
            }

        } catch (Exception e) {
            this.callbackContext.error("Error: " + e.getMessage());
        }
    }

    /**
     * Possible ways to log events to Fb: (see: https://developers.facebook.com/docs/reference/android/current/class/AppEventsLogger/)
     *
     * <p>
     * logEvent(string name)
     * logEvent(string name, double valueToSum)
     * logEvent(string name, Bundle properties)
     * logEvent(string name, double valueToSum, Bundle properties)
     */
    private void logEvent(JSONArray args) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this.cordova.getActivity().getApplication());
        this.cordova.getThreadPool().execute(() -> {
            try {

                if (args != null) {

                    JSONObject object = (JSONObject) args.get(0);

                    if (object.length() == 1) { // logEvent(string name)
                        logger.logEvent((String) object.get("event"));
                    }

                    if (object.length() == 2) {

                        if (object.has("value")) { // logEvent(string name, double valueToSum)
                            logger.logEvent((String) object.get("event"), object.getDouble("value"));
                        }

                        if (object.has("extras")) { // logEvent(string name, Bundle properties)
                            logger.logEvent((String) object.get("event"), this.fromJson((JSONObject) object.get("extras")));
                        }

                    }

                    if (object.length() == 3) {

                        Bundle extras = null;

                        if (object.has("extras")) {
                            extras = this.fromJson((JSONObject) object.get("extras"));
                        }

                        if (extras != null ) { // logEvent(string name, double valueToSum, Bundle properties)

                            if (object.has("value")) {
                                logger.logEvent((String) object.get("event"), object.getDouble("value"), extras);
                            } else {
                                logger.logEvent((String) object.get("event"), extras);
                            }
                        }
                    }

                    callbackContext.success();

                } else {
                    callbackContext.error("Event invalid or not found!");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
        });
    }

    private Bundle fromJson(JSONObject s) {
        Bundle bundle = new Bundle();

        for (Iterator<String> it = s.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONArray arr = s.optJSONArray(key);
            Double num = s.optDouble(key);
            String str = s.optString(key);

            if (arr != null && arr.length() <= 0)
                bundle.putStringArray(key, new String[]{});

            else if (arr != null && !Double.isNaN(arr.optDouble(0))) {
                double[] newarr = new double[arr.length()];
                for (int i=0; i<arr.length(); i++)
                    newarr[i] = arr.optDouble(i);
                bundle.putDoubleArray(key, newarr);
            }

            else if (arr != null && arr.optString(0) != null) {
                String[] newarr = new String[arr.length()];
                for (int i=0; i<arr.length(); i++)
                    newarr[i] = arr.optString(i);
                bundle.putStringArray(key, newarr);
            }

            else if (!num.isNaN())
                bundle.putDouble(key, num);

            else if (str != null)
                bundle.putString(key, str);

            else
                System.err.println("unable to transform json to bundle " + key);
        }

        return bundle;
    }

    private void logPurchase(JSONArray args) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this.cordova.getActivity().getApplication());
        this.cordova.getThreadPool().execute(() -> {
            try {

                if (args.length() != 2) {
                    callbackContext.error("Argumentos inválidos, você deve informar o value e currency.");
                }

                BigDecimal value = new BigDecimal(args.getString(0));
                String currency = args.getString(1);
                logger.logPurchase(value, Currency.getInstance(currency));
                callbackContext.success();
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
        });
    }

}
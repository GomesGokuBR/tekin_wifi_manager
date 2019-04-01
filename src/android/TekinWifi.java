package cordova.plugin.tekin.wifi;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class TekinWifi extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("toggleWifi")) {
            this.toggleWifi(callbackContext);
            return true;
        }
        return false;
    }

    private void toggleWifi(CallbackContext callbackContext) {
        callbackContext.success("salut plugin");
    }
}

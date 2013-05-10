
package org.brandroid.openmanager.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrivatePreferences {
    private static final Map<String, String> privateKeys = new HashMap<String, String>();

    public static final String getKey(String name) {
        return getKey(name, "");
    }
    
    public static final String getKey(String name, String defValue) {
        if(privateKeys.containsKey(name.toLowerCase(Locale.US)))
            return privateKeys.get(name.toLowerCase(Locale.US));
        else return defValue;
    }

    public static final String getBoxAPIKey() {
        return getKey("box_key");
    }
    
    public static final void putKey(String name, String value) {
        privateKeys.put(name, value);
    }

    static {
        // All Private Keys should be entered here, if enabled by default:
        privateKeys.put("box_key", "");
        privateKeys.put("box_secret", "");
        privateKeys.put("dropbox_key", "");
        privateKeys.put("dropbox_secret", "");
        privateKeys.put("drive_key", "");
        privateKeys.put("drive_secret", "");
    }
}

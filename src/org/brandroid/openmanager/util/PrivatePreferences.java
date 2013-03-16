
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
        return getKey("box");
    }

    static {
        // All Private Keys should go here like this:
        // privateKeys.put("box", "superSecretKeyGoesHere");
    }
}

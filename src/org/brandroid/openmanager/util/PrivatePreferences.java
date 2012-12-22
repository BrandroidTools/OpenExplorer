
package org.brandroid.openmanager.util;

import java.util.Hashtable;

public class PrivatePreferences {
    private static final Hashtable<String, String> privateKeys = new Hashtable<String, String>();

    public static final String getKey(String name) {
        return privateKeys.get(name);
    }

    public static final String getBoxAPIKey() {
        return getKey("box");
    }

    static {
        // All Private Keys should go here like this:
        // privateKeys.put("box", "superSecretKeyGoesHere");
    }
}

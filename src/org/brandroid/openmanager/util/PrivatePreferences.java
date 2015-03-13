
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
        // All Private Keys should go here like this:
        privateKeys.put("box_key", "zqjxn1m3i4eg4iud158e0nz7u9oi2cpu");
        privateKeys.put("box_secret", "BcTh1GpJpma1cJc58sqcfZSjDZeuiYZ2");
        privateKeys.put("dropbox_key", "vajaedmhzkkp3sw");
        privateKeys.put("dropbox_secret", "plkrfrygu17glgn");
        privateKeys.put("drive_key", "645291897772.apps.googleusercontent.com");
        privateKeys.put("drive_secret", "xo9-oPP7P7Rj5er3J1qmzhoG");
//        privateKeys.put("skydrive_key", "00000000400F4500");
//        privateKeys.put("skydrive_secret", "0uUmcI0Bjdxux9KdSWVxmgRCZcpzacyz");
    }
}

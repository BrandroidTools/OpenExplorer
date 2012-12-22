
package org.brandroid.openmanager.util;

import java.util.Map;
import java.util.prefs.Preferences;

import android.content.SharedPreferences;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHosts;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.Util;

public class SimpleHostKeyRepo extends KnownHosts {
    private final SharedPreferences mPrefs;
    private final UserInfo mUserInfo;

    public SimpleHostKeyRepo(JSch jsch, UserInfo userinfo, SharedPreferences prefs)
            throws JSchException {
        super(jsch);
        mPrefs = prefs;
        mUserInfo = userinfo;
        Map<String, ?> allPrefs = mPrefs.getAll();
        for (String host : allPrefs.keySet()) {
            if (!(allPrefs.get(host) instanceof String))
                continue;
            String key = (String)allPrefs.get(host);
            HostKey hostkey = new HostKey(host, unserialize(key));
            super.add(hostkey, userinfo);
        }
    }

    public static byte[] unserialize(String key) {
        byte[] buf = Util.str2byte(key);
        return Util.fromBase64(buf, 0, buf.length);
    }

    public static String serialize(byte[] key) {
        return Util.byte2str(Util.toBase64(key, 0, key.length));
    }

    @Override
    public int check(String host, byte[] key) {
        int kcheck = super.check(host, key);
        if (kcheck == OK)
            return OK;
        if (kcheck == CHANGED)
            return CHANGED;
        String scheck = mPrefs.getString(host, null);
        if (scheck == null)
            return NOT_INCLUDED;
        else if (key.equals(unserialize(scheck)))
            return OK;
        else
            return CHANGED;
    }

    @Override
    public void add(HostKey hostkey, UserInfo ui) {
        // mPrefs.putByteArray(hostkey.getHost(), hostkey.getKeyBytes());
        mPrefs.edit().putString(hostkey.getHost(), hostkey.getKey()).commit();
        super.add(hostkey, ui);
    }

    @Override
    public void remove(String host, String type) {
        mPrefs.edit().remove(host).commit();
        super.remove(host, type);
    }

    @Override
    public void remove(String host, String type, byte[] key) {
        mPrefs.edit().remove(host).commit();
        super.remove(host, type, key);
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return super.getKnownHostsRepositoryID();
    }

    @Override
    public HostKey[] getHostKey() {
        return super.getHostKey();
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        return super.getHostKey(host, type);
    }

}

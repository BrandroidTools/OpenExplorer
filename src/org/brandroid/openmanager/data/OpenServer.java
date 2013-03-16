
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import org.apache.commons.net.ftp.FTPFile;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.SimpleUserInfo;
import org.brandroid.utils.Logger;
import org.brandroid.utils.SimpleCrypto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.box.androidlib.DAO;
import com.box.androidlib.User;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import android.content.Context;
import android.net.Uri;

public class OpenServer {
    private final JSONObject mData;
    private final static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;
    private OpenNetworkPath mPath;
    private int mServerIndex = -1;
    private boolean mPasswordDecrypted = false;

    public OpenServer() {
        mData = new JSONObject();
        // mData = new Hashtable<String, String>();
    }

    public OpenServer(JSONObject obj) {
        mData = obj;
        decryptPW(true);
        /*
         * mData = new Hashtable<String, String>(); if(obj != null) { Iterator
         * keys = obj.keys(); while(keys.hasNext()) { String key =
         * (String)keys.next(); setSetting(key, obj.optString(key,
         * obj.opt(key).toString())); } }
         */
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OpenServer))
            return false;
        OpenServer os = (OpenServer)o;
        try {
            if (os.getHost().equals(getHost()) &&
                    os.getPassword().equals(getPassword()) &&
                    os.getUser().equals(getUser()) &&
                    os.getPath().equals(getPath()) &&
                    os.getPort() == getPort())
                return true;
        } catch (NullPointerException npe) {
            return false;
        }
        return super.equals(o);
    }

    public int getServerIndex() {
        if (mServerIndex > -1)
            return mServerIndex;

        if(OpenServers.hasDefaultServers())
        {
            OpenServers servers = OpenServers.getDefaultServers();
            for (int i = 0; i < servers.size(); i++)
            {
                if (servers.get(i).equals(this))
                {
                    mServerIndex = i;
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean isPasswordDecrypted()
    {
        return mPasswordDecrypted;
    }

    public void setServerIndex(int i) {
        mServerIndex = i;
    }

    private static String getDecryptKey()
    {
        return OpenServers.getDecryptKey();
    }

    public void decryptPW(boolean threaded)
    {
        if (mPasswordDecrypted)
            return;
        final String mPassword = get("password");
        Runnable decryptor = new Runnable() {
            public void run() {
                try {
                    final String pw = SimpleCrypto.decrypt(getDecryptKey(), mPassword);
                    mData.put("password", pw);
                    mPasswordDecrypted = true;
                } catch (Exception e) {
                    // Logger.LogError("Error decrypting password.", e);
                }
            }
        };
        if (threaded)
            new Thread(decryptor).start();
        else
            decryptor.run();
    }

    public void encryptPW(boolean threaded)
    {
        if (!mPasswordDecrypted)
            return;
        final String mPassword = get("password");
        Runnable encryptor = new Runnable() {
            public void run() {
                try {
                    final String pw = SimpleCrypto.encrypt(getDecryptKey(), mPassword);
                    mData.put("password", pw);
                    mPasswordDecrypted = false;
                } catch (Exception e) {
                    // Logger.LogError("Error decrypting password.", e);
                }
            }
        };
        if (threaded)
            new Thread(encryptor).start();
        else
            encryptor.run();
    }

    public boolean isValid() {
        if (getType().equalsIgnoreCase("box"))
            return true;
        if (getType().equalsIgnoreCase("db"))
            return true;
        return mData != null && mData.has("host");
    }

    private static String[] getNames(JSONObject o) {
        JSONArray a = o.names();
        String[] ret = new String[a.length()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = a.optString(i, a.opt(i).toString());
        return ret;
    }

    public JSONObject getJSONObject(Boolean encryptPW, Context context) {
        JSONObject ret = null;
        try {
            ret = new JSONObject(mData, getNames(mData));
        } catch (JSONException e1) {
            return null;
        }
        try {
            if (encryptPW && isPasswordDecrypted())
                ret.put("password", SimpleCrypto.encrypt(getDecryptKey(), getPassword()));
            ret.put("dir", getPath());
        } catch (Exception e) {
        }
        /*
         * for(String s : mData.keySet()) try { ret.put(s, mData.get(s)); }
         * catch (JSONException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        return ret;
    }

    public String getType() {
        return get("type");
    }

    public String get(String key) {
        Object o = mData.opt(key);
        String ret = "";
        if (o instanceof String)
            ret = (String)o;
        else if (o != null)
            ret = o.toString();
        if (DEBUG)
            Logger.LogDebug("OpenServer.getSetting(" + key + ") = " + ret);
        return ret;
    }

    public OpenServer setSetting(String key, String value) {
        try {
            mData.put(key, value);
            if (DEBUG)
                Logger.LogDebug("OpenServer.setSetting(" + key + ", " + value + ")");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * if(key.equalsIgnoreCase("name")) mName = value; else
         * if(key.equalsIgnoreCase("host")) mHost = value; else
         * if(key.equalsIgnoreCase("user")) mUser = value; else
         * if(key.equalsIgnoreCase("password")) mPassword = value; else
         * if(key.equalsIgnoreCase("dir")) mPath = value; else
         * if(key.equalsIgnoreCase("type")) mType = value; else
         * if(key.equalsIgnoreCase("port")) mPort = Integer.parseInt(value);
         */
        /*
         * else if(key != null && value != null) { if(mData == null) mData = new
         * Hashtable<String, String>(); mData.put(key, value); }
         */
        return this;
    }

    public OpenServer setSetting(String key, long value) {
        try {
            mData.put(key, value);
        } catch (JSONException e) {
        }
        return this;
    }

    public String getHost() {
        return get("host", "");
    }

    public String getPath() {
        String mPath = get("dir", "");
        if(!mPath.startsWith("/"))
            mPath = "/" + mPath;
        return mPath + (mPath.equals("") || mPath.endsWith("/") ? "" : "/");
    }

    public OpenNetworkPath getOpenPath()
    {
        if (mPath != null)
            return mPath;
        SimpleUserInfo info = new SimpleUserInfo();
        info.setPassword(getPassword());
        String t2 = getType().toLowerCase(Locale.US);
        if (t2.startsWith("ftp")) {
            mPath = new OpenFTP(null, new FTPFile(), new FTPManager(getHost(),
                    getUser(), getPassword(), getPath()));
        } else if (t2.startsWith("scp")) {
            mPath = new OpenSCP(getHost(), getUser(), getPath(), info);
        } else if (t2.startsWith("sftp")) {
            mPath = new OpenSFTP(getHost(), getUser(), getPath());
            //mPath = new OpenVFS("sftp://" + getUser() + ":" + getPassword() + "@" + getHost() + getPath());
        } else if (t2.startsWith("smb")) {
            try {
                mPath = new OpenSMB(new SmbFile("smb://" + getHost() + "/" + getPath(),
                        new NtlmPasswordAuthentication(getUser().indexOf("/") > -1 ? getUser()
                                .substring(0, getUser().indexOf("/")) : "", getUser(),
                                getPassword())));
            } catch (MalformedURLException e) {
                Logger.LogError("Couldn't add Samba share to bookmarks.", e);
            }
        } else if (t2.startsWith("box"))
        {
            User user = new User();
            if (has("dao"))
            {
                try {
                    user = (User)DAO.fromJSON(get("dao", "{}"), User.class);
                } catch (Exception e) {
                }
            }
            user.setAuthToken(getPassword());
            user.setLogin(getName());
            mPath = new OpenBox(user);
        } else if (t2.startsWith("db"))
        {
            DropboxAPI<AndroidAuthSession> mApi = new DropboxAPI<AndroidAuthSession>(
                    OpenDropBox.buildSession(this));
            mPath = new OpenDropBox(mApi);
        } else
            return null;
        mPath.setServer(this);
        return mPath;
    }

    public String getUser() {
        return get("user");
    }

    public String getPassword() {
        return get("password");
    }

    public String getName() {
        String mName = get("name");
        if(mName != null && !mName.equals(""))
            return mName;
        return getOpenPath().getName();
    }

    public OpenServer setHost(String host) {
        setSetting("host", host);
        return this;
    }

    public OpenServer setPath(String path) {
        setSetting("path", path);
        return this;
    }

    public OpenServer setUser(String user) {
        setSetting("user", user);
        return this;
    }

    public OpenServer setPassword(final String password) {
        setSetting("password", password);
        return this;
    }

    public OpenServer setName(String name) {
        setSetting("name", name);
        return this;
    }

    public OpenServer setType(String type) {
        setSetting("type", type);
        return this;
    }

    public OpenServer setPort(int port) {
        setSetting("port", "" + port);
        return this;
    }

    public boolean has(String name) {
        return mData.has(name);
    }

    public String get(String key, String defValue) {
        String ret = mData.optString(key, defValue);
        if (DEBUG)
            Logger.LogDebug("OpenServer.getSetting(" + key + ") = " + ret);
        return ret;
    }

    public String getString(String key) {
        if (key.equals("name"))
            return getName();
        if (key.equals("host"))
            return getHost();
        if (key.equals("dir"))
            return getPath();
        if (key.equals("path"))
            return getPath();
        if (key.equals("user"))
            return getUser();
        if (key.equals("password"))
            return getPassword();
        if (key.equals("type"))
            return getType();
        if (key.equals("port"))
            return "" + getPort();
        return get(key);
    }

    public int getPort() {
        try {
            String p = get("port", "-1");
            if (p == null || p.equals("null"))
                return -1;
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            Logger.LogError("Invalid number for port? ", e);
            return -1;
        }
    }

    @Override
    public String toString() {
        String ret = getType();
        ret += "://";
        if (!getUser().equals("")) {
            ret += getUser();
            // if (!getPassword().equals(""))
            // ret += ":" + getPassword().replaceAll(".", "*");
            ret += "@";
        }
        ret += getHost();
        if (getPort() > 0)
            ret += ":" + getPort();
        ret += getPath();
        return ret;
    }

    public long get(String key, long defValue) {
        return mData.optLong(key, defValue);
    }
/*
    @Override
    public String getAbsolutePath() {
        return getOpenPath().getAbsolutePath();
    }

    @Override
    public long length() {
        return getOpenPath().length();
    }

    @Override
    public OpenPath getParent() {
        return null;
    }

    @Override
    public OpenPath getChild(String name) {
        return getOpenPath().getChild(name);
    }

    @Override
    public OpenPath[] list() throws IOException {
        return getOpenPath().list();
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
         return getOpenPath().listFiles();
    }

    @Override
    public Boolean isDirectory() {
        return getOpenPath().isDirectory();
    }

    @Override
    public Boolean isFile() {
        return getOpenPath().isFile();
    }

    @Override
    public Boolean isHidden() {
        return getOpenPath().isHidden();
    }

    @Override
    public Uri getUri() {
        return getOpenPath().getUri();
    }

    @Override
    public Long lastModified() {
        return getOpenPath().lastModified();
    }

    @Override
    public Boolean canRead() {
        return getOpenPath().canRead();
    }

    @Override
    public Boolean canWrite() {
        return getOpenPath().canWrite();
    }

    @Override
    public Boolean canExecute() {
        return getOpenPath().canExecute();
    }

    @Override
    public Boolean exists() {
        return getOpenPath().exists();
    }

    @Override
    public Boolean requiresThread() {
        return true;
    }

    @Override
    public Boolean delete() {
        OpenServers.getDefaultServers().remove(getServerIndex());
        return true;
    }

    @Override
    public Boolean mkdir() {
        return getOpenPath().mkdir();
    }
*/
}

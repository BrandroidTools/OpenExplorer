
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.SimpleUserInfo;
import org.brandroid.utils.Logger;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;

public class FTPManager {
    public final static Hashtable<String, FTPManager> instances = new Hashtable<String, FTPManager>();
    private final static Hashtable<String, OpenFTP> fileCache = new Hashtable<String, OpenFTP>();
    private final static Hashtable<String, FTPClient> ftpClients = new Hashtable<String, FTPClient>();

    private long lastConnect = 0;
    private int mPort = 21;
    private String mHost = "", mUser = "", mPassword = "", mBasePath = "";

    public FTPManager() {
    }

    public FTPManager(String sFTPPath) throws MalformedURLException {
        URL url = new URL(sFTPPath);
        mUser = url.getUserInfo();
        if (mUser == null)
            mUser = "";
        else if (mUser.indexOf(":") > -1) {
            mPassword = mUser.substring(mUser.indexOf(":") + 1);
            mUser = mUser.substring(0, mUser.indexOf(":"));
        }
        mHost = url.getHost();
        mBasePath = url.getPath();
        instances
                .put("ftp://" + (url.getUserInfo() != "" ? url.getUserInfo() + "@" : "")
                        + url.getHost(), this);
    }

    public FTPManager(URL url) {
        mUser = url.getUserInfo();
        if (mUser.indexOf(":") > -1) {
            mPassword = mUser.substring(mUser.indexOf(":") + 1);
            mUser = mUser.substring(0, mUser.indexOf(":"));
        }
        mHost = url.getHost();
        mBasePath = url.getPath();
        instances.put("ftp://" + url.getUserInfo() + ":" + url.getHost(), this);
    }

    public FTPManager(String host, String user, String password, String basePath) {
        mHost = host;
        mUser = user;
        mPassword = password;
        mBasePath = basePath;
    }

    public FTPManager(FTPManager base, String newPath) {
        mHost = base.getHost();
        mUser = base.getUser();
        mPassword = base.getPassword();
        mBasePath = newPath;
    }

    public String getHost() {
        return mHost;
    }

    public String getUser() {
        return mUser;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getBasePath() {
        return mBasePath;
    }

    public FTPClient getClient() throws IOException {
        return getClient(false);
    }

    public FTPClient getClient(boolean ensureConnect) throws IOException {
        return getClient(mHost, mPort, mUser, mPassword, mBasePath, ensureConnect);
    }

    @SuppressLint("NewApi")
    public static FTPClient getClient(String mHost, int mPort, String mUser, String mPassword,
            String mBasePath, boolean ensureConnect) throws IOException {
        if (!ftpClients.containsKey(mHost)) {
            FTPClient client = new FTPClient();
            if (ensureConnect) {
                try {
                    client.connect(mHost, mPort);
                    if (!client.login(mUser, mPassword))
                        throw new IOException("Unable to log in to FTP. Invalid credentials?");
                    if (mBasePath.endsWith("/"))
                        mBasePath = mBasePath.substring(0, mBasePath.length() - 1);
                    client.cwd(mBasePath);
                } catch (Throwable e) {
                    if(Build.VERSION.SDK_INT > 8)
                        throw new IOException("Error connecting to FTP.", e);
                    else throw new IOException("Error connecting to FTP.");
                }
            }
            ftpClients.put(mHost, client);
            return client;
        }
        if (ensureConnect) {
            FTPClient client = ftpClients.get(mHost);
            if (client == null)
                client = new FTPClient();
            if (!client.isConnected()) {
                Logger.LogDebug("FTP Client " + mHost + " found (disconnected).");
                client.connect(mHost, mPort);
                if (!client.login(mUser, mPassword))
                    throw new IOException("Unable to log in to FTP. Invalid credentials?");
            } else
                Logger.LogDebug("Client found " + mHost);
            if (mBasePath.endsWith("/"))
                mBasePath = mBasePath.substring(0, mBasePath.length() - 1);
            client.cwd(mBasePath);
            return client;
        }
        return ftpClients.get(mHost);
    }

    public Boolean connect() throws IOException {
        long now = new Date().getTime();
        if (now - lastConnect < 500) {
            // Prevent connect loop if something unexpected happens
            lastConnect = now;
            return false;
        }
        lastConnect = new Date().getTime();
        if (Thread.currentThread().equals(OpenExplorer.UiThread))
            return false;

        FTPClient client = getClient(true);
        if (client.isConnected())
            return true;
        return false;
    }

    public static void closeAll() {
        for (FTPClient client : ftpClients.values()) {
            try {
                client.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public void disconnect() {
        try {
            Logger.LogDebug("Disconnecting FTP?");
            getClient().disconnect();
        } catch (Exception e) {
        }
    }

    public static FTPManager getInstance(String instanceName) {
        if (instances.containsKey(instanceName))
            return instances.get(instanceName);
        else
            return null;
    }

    public static FTPManager getInstance(URL url) throws MalformedURLException {
        FTPManager ret = getInstance("ftp://" + url.getUserInfo() + ":" + url.getHost());
        if (ret == null)
            ret = new FTPManager(url.toString());
        return ret;
    }

    public static void setInstance(String instanceName, FTPManager manager) {
        instances.put(instanceName, manager);
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setBasePath(String path) {
        mBasePath = path;
    }

    @SuppressLint("NewApi")
    public FTPFile[] listFiles() throws IOException {
        if (connect()) {
            FTPFile[] ret = getClient(false).listFiles();
            return ret;
        } else if(Build.VERSION.SDK_INT > 8)
            throw new IOException("Unable to list files due to invalid connection.",
                    new Throwable());
        else
            throw new IOException("Unable to list files due to invalid connection.");
    }

    public InputStream getInputStream() throws IOException {
        return getInputStream(getBasePath());
    }

    public InputStream getInputStream(String path) throws IOException {
        Logger.LogDebug("Getting InputStream for " + path);
        if (connect())
            return getClient().retrieveFileStream(path);
        else
            throw new IOException("Couldn't connect");
    }

    public OutputStream getOutputStream() throws IOException {
        return getOutputStream(getBasePath());
    }

    public OutputStream getOutputStream(String path) throws IOException {
        if (connect())
            return getClient().storeFileStream(path);
        else
            throw new IOException("Couldn't connect");
    }

    public String getPath() {
        return getPath(true);
    }

    public String getPath(boolean bIncludeUser) {
        String ret = "ftp://";
        if (bIncludeUser && mUser != "") {
            ret += mUser;
            if (mPassword != "")
                ret += ":" + mPassword;
            ret += "@";
        }
        ret += mHost;
        if (mPort != 21)
            ret += ":" + mPort;
        ret += "/";
        if (mBasePath.startsWith("/"))
            ret += mBasePath.substring(1);
        else
            ret += mBasePath;
        return ret;
    }

    public Boolean delete() throws IOException {
        if (connect())
            return getClient().deleteFile(getPath());
        else
            return false;
    }

    public void get(String name, OutputStream stream) throws IOException {
        if (connect())
            getClient().retrieveFile(name, stream);
    }

    public boolean isConnected() throws IOException {
        return getClient().isConnected();
    }
}


package org.brandroid.openmanager.activities;

import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import jcifs.smb.ServerMessageBlock;
import jcifs.smb.SmbComReadAndX;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFile.OnSMBCommunicationListener;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTP.OnFTPCommunicationListener;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenContextProvider;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LoggerDbAdapter;
import org.brandroid.utils.Preferences;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.jcraft.jsch.JSch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public abstract class OpenFragmentActivity extends SherlockFragmentActivity implements
        View.OnClickListener, View.OnLongClickListener, OpenContextProvider {
    // public static boolean CONTENT_FRAGMENT_FREE = true;
    // public boolean isFragmentValid = true;
    public static Thread UiThread = Thread.currentThread();
    private Preferences mPreferences = null;
    private final static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public ActionBar getSupportActionBar() {
        return getSherlock().getActionBar();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Logger.LogDebug(getClassName() + ".onAttachToWindow()");
    }

    public void onClick(View v) {
        if (DEBUG)
            Logger.LogDebug(getClassName() + ".onClick(0x" + Integer.toHexString(v.getId())
                    + ") - " + v.toString());
    }

    public void onClick(int id) {
        if (DEBUG)
            Logger.LogDebug("View onClick(0x" + Integer.toHexString(id) + ") / " + getClassName());
    }

    public boolean onClick(int id, MenuItem item, View from) {
        return false;
    }

    public boolean onLongClick(View v) {
        if (DEBUG)
            Logger.LogDebug("View onLongClick(0x" + Integer.toHexString(v.getId()) + ") - "
                    + v.toString());
        return false;
    }

    public boolean onOptionsItemSelected2(MenuItem item) {
        if (DEBUG)
            Logger.LogDebug("Menu selected(0x" + Integer.toHexString(item.getItemId()) + ") - "
                    + item.toString());
        return super.onOptionsItemSelected(item);
    }

    public MenuInflater getSupportMenuInflater() {
        return getSherlock().getMenuInflater();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            Logger.LogError("Error while creating.", e);
        }
        Logger.LogDebug("<-onCreate - " + getClassName());
        // CONTENT_FRAGMENT_FREE = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Logger.LogDebug("->onDestroy - " + getClassName());
    }

    /**
     * Set Application specific language. This has no effect on the system
     * language.
     * 
     * @param context
     * @param language 2 Letter Language Code
     */
    public static void setLanguage(Context context, String language) {
        Locale locale;
        if (language == null || language.equals("")) {
            locale = Locale.getDefault();
        } else if (language.length() == 5 && language.charAt(2) == '_') {
            // language is in the form: en_US
            locale = new Locale(language.substring(0, 2), language.substring(3));
            language = language.substring(0, 2);
        } else {
            locale = new Locale(language);
        }
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
        new Preferences(context).setSetting("global", "pref_language", language);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public int getWindowWidth() {
        if (Build.VERSION.SDK_INT > 13) {
            Point p = new Point();
            getWindowManager().getDefaultDisplay().getSize(p);
            return p.x;
        } else
            return getWindowManager().getDefaultDisplay().getWidth();
    }

    public Preferences getPreferences() {
        if (mPreferences == null)
            mPreferences = new Preferences(getApplicationContext());
        return mPreferences;
    }

    public String getSetting(OpenPath file, String key, String defValue) {
        return getPreferences().getSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), defValue);
    }

    public Boolean getSetting(OpenPath file, String key, Boolean defValue) {
        return getPreferences().getSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), defValue);
    }

    public Integer getSetting(OpenPath file, String key, Integer defValue) {
        return getPreferences().getSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), defValue);
    }

    public Float getSetting(OpenPath file, String key, Float defValue) {
        return getPreferences().getSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), defValue);
    }

    public void setSetting(OpenPath file, String key, String value) {
        getPreferences().setSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), value);
    }

    public void setSetting(OpenPath file, String key, Boolean value) {
        getPreferences().setSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), value);
    }

    public void setSetting(OpenPath file, String key, Integer value) {
        getPreferences().setSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), value);
    }

    public void setSetting(OpenPath file, String key, Float value) {
        getPreferences().setSetting(file == null ? "global" : "views",
                key + (file != null ? "_" + file.getPath() : ""), value);
    }

    public void setSetting(String globalKey, Boolean value) {
        setSetting("global", globalKey, value);
    }

    public void setSetting(String globalKey, Integer value) {
        setSetting("global", globalKey, value);
    }

    public void setSetting(String globalKey, String value) {
        setSetting("global", globalKey, value);
    }

    public void setSetting(String file, String key, Boolean value) {
        getPreferences().setSetting(file, key, value);
    }

    public void setSetting(String file, String key, Integer value) {
        getPreferences().setSetting(file, key, value);
    }

    public void setSetting(String file, String key, String value) {
        getPreferences().setSetting(file, key, value);
    }

    public static boolean isNook() {
        if (Preferences.Is_Nook != null)
            return Preferences.Is_Nook;
        if (Build.DISPLAY.toLowerCase().contains("acclaim")
                || Build.BRAND.toLowerCase().contains("nook")
                || Build.PRODUCT.toLowerCase().contains("nook"))
            return Preferences.Is_Nook = true;
        else
            return Preferences.Is_Nook = false;
    }

    public static boolean isBlackBerry() {
        return Build.MANUFACTURER.trim().equalsIgnoreCase("rim")
                || Build.MODEL.toLowerCase().indexOf("blackberry") > -1;
    }

    public boolean isGTV() {
        return isGTV(this);
    }

    public static boolean isGTV(Context context) {
        return context.getPackageManager().hasSystemFeature("com.google.android.tv");
    }

    public void showToast(final CharSequence message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    public void showToast(final int iStringResource) {
        showToast(getResources().getString(iStringResource));
    }

    public void showToast(final CharSequence message, final int toastLength) {
        Logger.LogVerbose("Made toast: " + message);
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), message, toastLength).show();
            }
        });
    }

    public void showToast(final int resId, final int length) {
        showToast(getString(resId), length);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public abstract void onChangeLocation(OpenPath path);

    protected abstract void sendToLogView(String str, int color);

    protected void setupLoggingDb() {
        FTP.setCommunicationListener(new OnFTPCommunicationListener() {

            @Override
            public void onDisconnect(FTP file) {
                sendToLogView("FTP Disconnect " + getFTPString(file), Color.GRAY);
            }

            @Override
            public void onConnectFailure(FTP file) {
                sendToLogView("FTP Failure " + getFTPString(file), Color.RED);
            }

            @Override
            public void onConnect(FTP file) {
                sendToLogView("FTP Connect " + getFTPString(file), Color.GREEN);
            }

            @Override
            public void onBeforeConnect(FTP file) {
                // sendToLogView("FTP Before Connect " + getFTPString(file));
            }

            @Override
            public void onSendCommand(FTP file, String message) {
                if (message.startsWith("PASS "))
                    message = "PASS " + message.substring(6).replaceAll(".", "*");
                sendToLogView("Command: " + message.replace("\n", ""), Color.BLACK); // +
                                                                                     // getFTPString(file),
                                                                                     // Color.BLACK);
            }

            private String getFTPString(FTP file) {
                if (file != null && file.getSocket() != null && file.getRemoteAddress() != null)
                    return " @ " + file.getRemoteAddress().getHostName();
                return "";
            }

            @Override
            public void onReply(String line) {
                sendToLogView("Reply: " + line, Color.BLUE);
            }
        });
        JSch.setLogger(new com.jcraft.jsch.Logger() {
            @Override
            public void log(int level, String message) {
                switch (level) {
                    case com.jcraft.jsch.Logger.DEBUG:
                        sendToLogView("SFTP - " + message, Color.GREEN);
                        break;
                    case com.jcraft.jsch.Logger.INFO:
                        sendToLogView("SFTP - " + message, Color.BLUE);
                        break;
                    case com.jcraft.jsch.Logger.WARN:
                        sendToLogView("SFTP - " + message, Color.YELLOW);
                        break;
                    case com.jcraft.jsch.Logger.ERROR:
                        sendToLogView("SFTP - " + message, Color.RED);
                        break;
                    case com.jcraft.jsch.Logger.FATAL:
                        sendToLogView("SFTP - " + message, Color.MAGENTA);
                        break;
                    default:
                        sendToLogView("SFTP (" + level + ") - " + message, Color.BLACK);
                        break;
                }
            }

            @Override
            public boolean isEnabled(int level) {
                return true;
            }
        });
        if (Logger.isLoggingEnabled()) {
            if (getPreferences().getBoolean("global", "pref_stats", true)) {
                if (!Logger.hasDb())
                    Logger.setDb(new LoggerDbAdapter(getApplicationContext()));
            } else if (!DEBUG)
                Logger.setLoggingEnabled(false);
        }
        SmbFile.setSMBCommunicationListener(new OnSMBCommunicationListener() {

            @Override
            public void onBeforeConnect(SmbFile file) {
                sendToLogView("SMB Connecting: " + file.getPath(), Color.GREEN);
            }

            @Override
            public void onConnect(SmbFile file) {

                sendToLogView("SMB Connected: " + file.getPath(), Color.GREEN);
            }

            @Override
            public void onConnectFailure(SmbFile file) {
                sendToLogView("SMB Connect Failure: " + file.getPath(), Color.RED);
            }

            @Override
            public void onDisconnect(SmbFile file) {
                sendToLogView("SMB Disconnect: " + file.getPath(), Color.DKGRAY);
            }

            @Override
            public void onSendCommand(SmbFile file, Object... commands) {
                URL url = file.getURL();
                String s = "SMB Command: smb://" + url.getHost() + url.getPath();
                for (Object o : commands) {
                    if (o instanceof ServerMessageBlock) {
                        ServerMessageBlock blk = (ServerMessageBlock)o;
                        if (DEBUG && blk instanceof SmbComReadAndX)
                            continue;
                        String tmp = blk.toShortString();
                        if (tmp == null || tmp == "")
                            continue;
                        if (tmp.indexOf("[") > -1)
                            s += " -> " + tmp.substring(0, tmp.indexOf("["));
                        else
                            s += " -> " + tmp;
                    } else
                        s += " -> " + o.toString();
                }
                sendToLogView(s, Color.BLACK);
            }

        });

        jcifs.Config.registerSmbURLHandler();
        jcifs.Config.setProperty("jcifs.resolveOrder", "WINS,LMHOSTS,BCAST,DNS");
        jcifs.Config.setProperty("jcifs.smb.client.username", "Guest");
        jcifs.Config.setProperty("jcifs.smb.client.password", "");
        // jcifs.Config.setProperty("jcifs.smb.client.responseTimeout", "5000");
        // jcifs.Config.setProperty("jcifs.smb.client.soTimeout", "5000");

    }

    /*
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) {
     * Logger.LogDebug("<-onCreateView - " + getClassName());
     * //CONTENT_FRAGMENT_FREE = false; return super.onCreateView(inflater,
     * container, savedInstanceState); }
     * @Override public void onViewCreated(View view, Bundle savedInstanceState)
     * { super.onViewCreated(view, savedInstanceState);
     * Logger.LogDebug("<-onViewCreated - " + getClassName()); }
     * @Override public void onPause() { super.onPause();
     * Logger.LogDebug("->onPause - " + getClassName()); }
     * @Override public void onResume() { super.onResume();
     * Logger.LogDebug("<-onResume - " + getClassName()); }
     * @Override public void onStart() { super.onStart();
     * Logger.LogDebug("<-onStart - " + getClassName()); }
     * @Override public void onStop() { super.onStop();
     * Logger.LogDebug("->onStop - " + getClassName()); //CONTENT_FRAGMENT_FREE
     * = true; }
     * @Override public void onSaveInstanceState(Bundle outState) {
     * super.onSaveInstanceState(outState);
     * Logger.LogDebug("->onSaveInstanceState - " + getClassName()); }
     */
}

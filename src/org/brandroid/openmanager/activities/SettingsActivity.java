/*
    Open Explorer, an open source file explorer & text editor
    Copyright (C) 2011 Brandon Bowles <brandroid64@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.brandroid.openmanager.activities;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.InputDialog;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LruCache;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.SimpleCrypto;
import org.brandroid.utils.Utils;
import org.json.JSONArray;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;
import com.box.androidlib.BoxAuthentication;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.stericson.RootTools.RootTools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class SettingsActivity extends SherlockPreferenceActivity implements
        OnPreferenceChangeListener, OpenApp {
    // keys used for preference file
    /*
     * public static final String PREF_LIST_KEY = "pref_dirlist"; public static
     * final String PREF_BOOKNAME_KEY = "pref_bookmarks"; public static final
     * String PREF_HIDDEN_KEY = "pref_hiddenFiles"; public static final String
     * PREF_THUMB_KEY = "pref_thumbnail"; public static final String
     * PREF_VIEW_KEY = "pref_view"; public static final String PREF_SORT_KEY =
     * "pref_sorting";
     */
    public static final int MODE_PREFERENCES = 0;
    public static final int MODE_SERVER = 1;

    private Preferences prefs;
    // private BillingService mBillingService;
    // private DonationObserver mDonationObserver;
    private Handler mHandler;

    final static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;

    public static String getDisplayLanguage(Context context, String langCode) {
        if (!langCode.equals("")) {
            int pos = Utils.getArrayIndex(
                    context.getResources().getStringArray(R.array.languages_values),
                    langCode);
            if (pos > -1) {
                String[] langs = context.getResources().getStringArray(R.array.languages);
                langCode = langs[pos];
            }
            return langCode;
        } else
            return langCode;

    }

    @SuppressLint("NewApi")
    public void onBuildHeaders(List<Header> target) {
        if(Build.VERSION.SDK_INT < 12) return; // This shouldn't matter, but just in case
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @SuppressLint("NewApi")
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        super.startPreferenceFragment(fragment, push);
        if(Build.VERSION.SDK_INT < 12) return; // This shouldn't matter, but just in case
        Logger.LogDebug("startPreferenceFragment(" + fragment.toString() + ", " + push + ")");
        setOnChange(((PreferenceFragment)fragment).getPreferenceScreen(), false);
    }

    @SuppressLint("NewApi")
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        if(Build.VERSION.SDK_INT < 12) return false; // This shouldn't matter, but just in case
        Logger.LogDebug("onPreferenceStartFragment(" + caller + ", " + pref + ")");
        return super.onPreferenceStartFragment(caller, pref);
    }

    public boolean onIsMultiPane() {
        // non-multipane doesn't work very well,
        // it does not return intent to OpenExplorer for some reason,
        // so we'll make sure to use multipane if there is enough room
        return getResources().getBoolean(R.bool.large) || getResources().getBoolean(R.bool.land);
    }

    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
        // return null;
    }

    protected void onResume() {
        Intent intent = getIntent();
        Bundle data = intent != null ? intent.getExtras() : null;
        if (data == null || (data.size() == 1 && data.containsKey("path"))) {

        }
        if (DEBUG)
            Logger.LogDebug("SettingsActivity.onResume(" + data + ")");
        super.onResume();
    }

    protected void onRestoreInstanceState(Bundle state) {
        if (DEBUG)
            Logger.LogDebug("SettingsActivity.onRestoreInstanceState(" + state + ")");
        super.onRestoreInstanceState(state);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // int theme = getThemeId();
        // getApplicationContext().setTheme(theme);
        // setTheme(theme);

        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent == null)
            intent = new Intent();
        Bundle config = intent.getExtras();
        if (savedInstanceState != null)
            config = savedInstanceState;
        if (config == null)
            config = new Bundle();

        if (DEBUG)
            Logger.LogDebug("SettingsActivity.onCreate(" + savedInstanceState + ")");

        String path = "global";
        if (config.containsKey("path"))
            path = config.getString("path");

        int mode = 0;
        if (config.containsKey("mode"))
            mode = config.getInt("mode");

        String pathSafe = path.replaceAll("[^A-Za-z0-9]", "_");
        // if(mode == MODE_SERVER)
        // path = pathSafe = "servers";

        if (!config.containsKey("path"))
            config.putString("path", path);

        intent.putExtras(config);
        setIntent(intent);

        PreferenceManager.setDefaultValues(this, pathSafe, PreferenceActivity.MODE_PRIVATE,
                R.xml.preferences, false);

        CheckBoxPreference pSystem = (CheckBoxPreference)findPreference("pref_system_mount");
        if (pSystem != null)
            pSystem.setChecked(RootManager.isSystemMounted());

        // getPreferences(MODE_PRIVATE);
        prefs = new Preferences(getApplicationContext());

        if (mode == MODE_PREFERENCES && Build.VERSION.SDK_INT < 11) {
            PreferenceManager pm = getPreferenceManager();
            pm.setSharedPreferencesName(pathSafe);
            if (!path.equals("global")) // folder preferences
            {
                PreferenceManager.setDefaultValues(this, pathSafe, PreferenceActivity.MODE_PRIVATE,
                        R.xml.preferences_folders, false);
                addPreferencesFromResource(R.xml.preferences_folders);

                Preference pTitle = findPreference("folder_title");
                if (pTitle != null)
                    pTitle.setTitle(pTitle.getTitle() + " - " + path);

            } else { // global preferences
                addPreferencesFromResource(R.xml.preferences);

                final PreferenceActivity pa = this;

                Preference pLanguage = pm.findPreference("pref_language");
                if (pLanguage == null)
                    pLanguage = findPreference("pref_language");
                if (pLanguage != null) {
                    String lang = pm.getSharedPreferences().getString("pref_language", "");
                    if (!lang.equals(""))
                        pLanguage.setSummary(SettingsActivity.getDisplayLanguage(this, lang));
                    else
                        pLanguage.setSummary(pLanguage.getSummary() + " ("
                                + Locale.getDefault().getDisplayLanguage() + ")");
                }

                final Preference pSize = pm.findPreference("text_size") != null ? pm
                        .findPreference("text_size") : findPreference("text_size");
                if (pSize != null) {
                    float sz = new Preferences(getApplication())
                            .getSetting("global", "text_size", 10f);
                    pSize.setSummary(sz + "");
                    pSize.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(final Preference preference) {
                            float mSize = 10f;
                            try {
                                mSize = Float.parseFloat(pSize.getSummary().toString());
                            } catch (Exception e) {
                            }

                            DialogHandler.showSeekBarDialog(SettingsActivity.this,
                                    getString(R.string.s_view_font_size), (int)mSize * 2, 60,
                                    new OnSeekBarChangeListener() {
                                        public void onProgressChanged(SeekBar seekBar,
                                                int progress, boolean fromUser) {
                                            float fsz = (float)(progress + 1) / 2;
                                            pSize.setSummary(fsz + "");
                                        }

                                        public void onStartTrackingTouch(SeekBar seekBar) {
                                            // TODO Auto-generated method stub

                                        }

                                        public void onStopTrackingTouch(SeekBar seekBar) {
                                            new Preferences(getApplication()).setSetting("global",
                                                    "text_size",
                                                    (float)(seekBar.getProgress() + 1) / 2);
                                        }

                                    });
                            return false;
                        }
                    });
                }

                refreshServerList();
            }
        } else if (mode == MODE_SERVER) {
            addPreferencesFromResource(R.xml.server_prefs);
            // SharedPreferences sp =
            // Preferences.getPreferences(getApplicationContext(), "servers");
            // String servers = sp.getString("servers", "");
            OpenServers servers = ServerSetupActivity.LoadDefaultServers(this);
            // new OpenServers(prefs.getJSON("global", "servers", new
            // JSONObject()));
            if (path.equals("server_add")) {
                setTitle(getTitle() + " - Add New");
                getPreferenceScreen().findPreference("server_delete").setEnabled(false);
            } else {
                path = path.replace("server_modify_", "");
                OpenServer server = new OpenServer();
                int index = 0;
                try {
                    index = Integer.parseInt(path);
                    server = servers.get(index);
                    path = server.getName();
                } catch (NumberFormatException e) {
                    Logger.LogWarning("Couldn't parseInt " + path);
                }
                setTitle(getTitle() + " - " + path);

                Preference p = getPreferenceManager().findPreference("server_type");
                String val = server.getString("type");
                if (val == null)
                    val = "ftp";
                String[] types = getApplicationContext().getResources().getStringArray(
                        R.array.server_types_values);
                int pos = 0;
                for (int i = 0; i < types.length; i++)
                    if (types[i].toLowerCase().equals(val.toLowerCase()))
                        pos = i;
                getIntent().putExtra("type", val);
                ((ListPreference)p).setValueIndex(pos);
                p.setSummary(val);

                if (server != null)
                    for (String s : new String[] {
                            "name", "host", "user", "password", "dir", "port"
                    })
                        if ((p = getPreferenceManager().findPreference("server_" + s)) != null) {
                            val = server.getString(s);
                            if (val != null) {
                                getIntent().putExtra(s, val);
                                ((EditTextPreference)p).setText(val);
                                p.setSummary(val);
                                p.setDefaultValue(val);
                            }
                        }
            }
            // PreferenceManager.setDefaultValues(this, "servers",
            // PreferenceActivity.MODE_PRIVATE, R.xml.server_prefs, true);
            // setIntent(intent);
        }
        setOnChange(getPreferenceScreen(), false);

        /*
         * mHandler = new Handler(); mDonationObserver = new
         * DonationObserver(mHandler); mBillingService = new BillingService();
         * mBillingService.setContext(this);
         * ResponseHandler.register(mDonationObserver); if
         * (!mBillingService.checkBillingSupported()) {
         * Logger.LogWarning("Billing not supported.");
         * //showDialog(DIALOG_CANNOT_CONNECT_ID); }
         */
    }

    protected void onDestroy() {
        super.onDestroy();
        // if(mBillingService != null) mBillingService.unbind();
    }

    private void refreshServerList() {
        PreferenceGroup mPrefServers = (PreferenceGroup)findPreference("servers");
        if (mPrefServers != null) {
            // for(int i = mPrefServers.getPreferenceCount() - 1; i > 0; i--)
            // mPrefServers.removePreference(mPrefServers.getPreference(i));
            OpenServers servers = ServerSetupActivity.LoadDefaultServers(this); // new
            // OpenServers(prefs.getSetting("global",
            // "servers", new
            // JSONObject()));
            for (int i = 0; i < servers.size(); i++) {
                OpenServer server = servers.get(i);
                // Logger.LogDebug("Checking server [" + sName + "]");
                // if(sName.equals("")) continue;
                Preference p = mPrefServers.findPreference("server_modify_" + i);
                if (p == null)
                    p = new Preference(this);
                // PreferenceScreen ps =
                // inflatePreferenceScreenFromResource(R.xml.server_prefs);
                p.setKey("server_modify_" + i);
                p.setTitle(server.getName());
                mPrefServers.addPreference(p);
            }
            for (int i = mPrefServers.getPreferenceCount() - 1; i > servers.size(); i--)
                mPrefServers.removePreference(mPrefServers.getPreference(i)); // remove
                                                                              // the
                                                                              // rest
        }
    }

    protected void onStart() {
        super.onStart();
        // ResponseHandler.register(mDonationObserver);
    }

    protected void onStop() {
        super.onStop();
        // ResponseHandler.unregister(mDonationObserver);
    }

    public static void setOnPreferenceChangeListenerToChildren(PreferenceScreen parent,
            OnPreferenceChangeListener listener) {
        for (int i = 0; i < parent.getPreferenceCount(); i++)
            parent.getPreference(i).setOnPreferenceChangeListener(listener);
    }

    public void setOnChange(Preference p, Boolean forceSummaries) {
        if (p == null)
            return;
        if (p instanceof PreferenceGroup) {
            PreferenceGroup ps = (PreferenceGroup)p;
            for (int i = 0; i < ps.getPreferenceCount(); i++)
                setOnChange(ps.getPreference(i), forceSummaries);
            return;
        }

        p.setOnPreferenceChangeListener(this);

        if (forceSummaries || p.getSummary() == null || p.getSummary().equals(""))
            if (p instanceof EditTextPreference) {
                if (((EditTextPreference)p).getText() != null
                        && !"".equals(((EditTextPreference)p).getText())) {
                    String txt = ((EditTextPreference)p).getText();
                    p.setSummary(txt);
                    p.setDefaultValue(txt);
                }
            } else if (p instanceof ListPreference) {
                ListPreference lp = (ListPreference)p;
                lp.setSummary(lp.getEntry());
            }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MODE_SERVER && data != null) {
            String sPath = data.getStringExtra("path");
            Preferences prefs = new Preferences(getApplicationContext());
            OpenServers servers = ServerSetupActivity.LoadDefaultServers(this); // new
            // OpenServers(prefs.getJSON("global",
            // "servers", new
            // JSONObject()));
            OpenServer server = null;
            int index = 0;
            if (sPath.equals("server_add")) {
                server = new OpenServer();
                index = servers.size();
                sPath = index + "";
                servers.add(server);
            } else {
                try {
                    index = Integer.parseInt(sPath.replaceAll("[^0-9]", ""));
                    server = servers.get(index);
                    if (resultCode == RESULT_FIRST_USER) // delete
                    {
                        servers.remove(index);
                        ServerSetupActivity.SaveToDefaultServers(servers, getApplicationContext());
                        refreshServerList();
                        return;
                    } else
                        sPath = index + "";
                } catch (NumberFormatException e) {
                    Logger.LogWarning("Couldn't parseInt " + sPath);
                }
            }

            if (server == null)
                server = new OpenServer();

            Bundle b = data.getExtras();
            for (String s : b.keySet())
                if (s != "mode") {
                    if (b.get(s) == null)
                        continue;
                    server.setSetting(s, b.get(s).toString());
                }

            if (server.getPath() == null
                    || (b.containsKey("dir") && !b.getString("dir").equalsIgnoreCase(
                            server.getPath())))
                server.setPath(b.getString("dir"));

            servers.set(index, server);
            ServerSetupActivity.SaveToDefaultServers(servers, getApplicationContext());
            refreshServerList();
            // prefs.setSetting("global", "servers", servers.getJSONObject());
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    public static void showMasterPassDialog(final Context context)
    {
        showMasterPassDialog(context, false);
    }

    public static void showMasterPassDialog(final Context context, boolean hilight)
    {
        final Preferences prefs = new Preferences(context);
        String msg = context.getResources().getString(R.string.s_master_pass_warning);
        String pw = prefs.getSetting("global", "pref_master_pass", (String)null);
        final String sigKey = GetMasterPassword(context, true, false);
        if (pw != null && !pw.equals(""))
            try {
                pw = SimpleCrypto.decrypt(sigKey, pw);
            } catch (Exception e) {
                try {
                    pw = SimpleCrypto.decrypt(GetMasterPassword(context, false, false), pw);
                } catch (Exception e2) {
                    pw = ""; // corrupted? allow reset
                }
            }
        else
            pw = "";
        final String old = pw;
        final boolean isUpdate = old != null && !old.equals("");
        if (isUpdate)
            msg += "\n\n" + context.getResources().getString(R.string.s_pass_old);
        final InputDialog inp = new InputDialog(context)
                .setTitle(R.string.s_master_pass)
                .setMessageTop(msg)
                .setDefaultTop(isUpdate ? old.replaceAll(".", "*") : null)
                .setMessage(R.string.s_pass_new)
                .setNeutralButton(android.R.string.cancel, null)
                .setCancelable(true);
        inp.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isUpdate && !inp.getInputTopText().equals(old))
                {
                    showMasterPassDialog(context, true);
                    return;
                }
                if (!isUpdate || old.equals(inp.getInputTopText()))
                {
                    final String newPw = inp.getInputText();
                    new Thread(new Runnable() {
                        public void run() {
                            String encPw = newPw;
                            try {
                                if (!newPw.equals(""))
                                    encPw = SimpleCrypto.encrypt(sigKey, encPw);
                            } catch (Exception e) {
                            }
                            prefs.setSetting("global", "pref_master_pass", encPw);
                            OpenServers.setDecryptKey(GetMasterPassword(context));
                            JSONArray jarr = OpenServers.getDefaultServers().getJSONArray();
                            ServerSetupActivity.encryptPasswords(jarr, newPw);
                            ServerSetupActivity.SaveToDefaultServers(jarr, context);
                            OpenServers.setDefaultServers(new OpenServers(jarr));
                        }
                    }).start();
                }
            }
        });
        inp.create().show();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return onPreferenceTreeClick(preferenceScreen, preference, this);
    }

    public static boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference, Activity activity) {
        final String key = preference.getKey();
        if (key.equals("pref_global")) {
            Intent intentGlobal = new Intent(activity, SettingsActivity.class);
            activity.startActivity(intentGlobal);
            return true;
        } else if (key.equals("pref_translate")) {
            OpenExplorer.launchTranslator(activity);
        } else if (key.equals("pref_privacy")) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://brandroid.org/privacy.php"));
            activity.startActivity(intent);
        } else if (key.equals("pref_master_pass")) {
            showMasterPassDialog(activity);
        } else if (key.equals("pref_language")) {

        } else if (key.equals("pref_thumbs_cache_clear")) {
            Toast.makeText(activity, "Cache cleared!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (key.equals("pref_system_mount")) {
            final CheckBoxPreference pSystem = (CheckBoxPreference)preference;
            final boolean checked = pSystem.isChecked();
            new Thread(new Runnable() {
                public void run() {
                    String mode;
                    try {
                        mode = !checked ? "ro" : "rw";
                        RootTools.remount("/system", mode);
                        if (DEBUG)
                            Logger.LogDebug("New /system: " + RootTools.getMountedAs("/system"));
                        // pSystem.setChecked(RootTools.getMountedAs("/system").equalsIgnoreCase("rw"));
                    } catch (Exception e) {
                        Logger.LogError("Unable to remount system", e);
                    }
                }
            }).start();

            return true;
        } else if (key.equals("server_add")) {
            Intent intentServer = new Intent(activity, ServerSetupActivity.class);
            activity.startActivityForResult(intentServer, MODE_SERVER);
            return true;
        } else if (key.equals("pref_start")) {
            OpenExplorer.showSplashIntent(activity,
                    new Preferences(activity).getSetting("global", "pref_start", "External"));
            return true;
        } else if (key.startsWith("server_modify") && activity instanceof OpenApp) {
            int snum = -1;
            OpenNetworkPath onp = null;
            try {
                snum = Integer.parseInt(key.replace("server_modify_", ""));
                onp = ServerSetupActivity.LoadDefaultServers(activity).get(snum).getOpenPath();
            } catch (NumberFormatException e) {
            }
            ServerSetupActivity.showServerDialog((OpenApp)activity, onp);
            return true;
        } else if (key.equals("server_update")) {
            Intent iNew = activity.getIntent();
            // OpenServer server = new OpenServer();
            Preference p = preferenceScreen.findPreference("server_type");
            iNew.putExtra("type", ((ListPreference)p).getValue());
            for (String s : new String[] {
                    "name", "host", "url", "user", "password", "dir"
            })
                if ((p = preferenceScreen.findPreference("server_" + s)) != null) {
                    if (iNew.hasExtra(s))
                        continue;
                    // server.setSetting(s,
                    // ((EditTextPreference)getPreferenceManager().findPreference("server_"
                    // + s)).getText());
                    if (DEBUG)
                        Logger.LogDebug("Found " + s + " = "
                                + ((EditTextPreference)p).getEditText().getText().toString());
                    iNew.putExtra(s, ((EditTextPreference)p).getEditText().getText().toString());
                }
            // server.setSetting("name", server.getString("name",
            // server.getHost()));
            // Logger.LogDebug("Saving " + server.getName() + " (" +
            // server.getHost() + ") - " + server.getJSONObject().toString());
            // OpenServers servers = LoadDefaultServers(); //new
            // OpenServers(prefs.getJSON("global", "servers", new
            // JSONObject()));
            // servers.addServer(server.getString("name", "Server"), server);
            // SaveToDefaultServers(servers, getApplicationContext());
            // prefs.setSetting("global", "servers", servers.getJSONObject());
            activity.setResult(RESULT_OK, iNew);
            activity.finish();
        } else if (key.equals("server_delete")) {
            activity.setResult(RESULT_FIRST_USER, activity.getIntent());
            activity.finish();
        }
        return false;
    }

    public PreferenceScreen inflatePreferenceScreenFromResource(int resId) {
        try {
            Class<PreferenceManager> cls = PreferenceManager.class;
            Method method = cls.getDeclaredMethod("inflateFromResource", Context.class, int.class,
                    PreferenceScreen.class);
            return (PreferenceScreen)method.invoke(getPreferenceManager(), this, resId, null);
        } catch (Exception e) {
            Logger.LogWarning("Could not inflate preference screen from XML", e);
        }

        return null;
    }

    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        return onPreferenceChange(preference, newValue, this);
    }

    public static boolean onPreferenceChange(final Preference preference, final Object newValue,
                final Activity activity)
    {
        final Preferences prefs = new Preferences(activity);
        final Intent intent = activity.getIntent();
        final String key = preference.getKey();

        if (DEBUG)
            Logger.LogDebug("SettingsActivity.onPreferenceChange(" + key + ", " + newValue + ")");

        if (Utils.inArray(key, "pref_fullscreen", "pref_fancy_menus", "pref_basebar",
                "pref_themes", "pref_stats", "pref_root", "pref_language"))
            prefs.setSetting("global", "restart", true);
        if (key.equals("servers_private"))
        {
            OpenFile f = OpenFile.getExternalMemoryDrive(true).getChild("Android").getChild("data")
                    .getChild("org.brandroid.openmanager").getChild("files")
                    .getChild("servers.json");
            OpenFile f2 = new OpenFile(activity.getFilesDir().getPath(), "servers.json");
            Boolean doPrivate = (Boolean)newValue;
            if (doPrivate)
            {
                if (f.exists() && f.length() > f2.length())
                    f2.copyFrom(f);
                f.delete();
            } else {
                if (f2.exists() && f2.length() > f.length())
                    f.copyFrom(f2);
                f2.delete();
            }
        }
        if (key.equals("pref_language"))
            preference.setSummary(SettingsActivity.getDisplayLanguage(activity, (String)newValue));
        else if (preference instanceof ListPreference && newValue instanceof String)
        {
            preference.setSummary((String)newValue);
            CharSequence[] opts = ((ListPreference)preference).getEntries();
            CharSequence[] vals = ((ListPreference)preference).getEntryValues();
            for (int i = 0; i < opts.length; i++)
            {
                if (vals[i].equals(newValue))
                {
                    preference.setSummary(opts[i]);
                    break;
                }
            }
        }
        else if (EditTextPreference.class.equals(preference.getClass())
                && ((EditTextPreference)preference).getEditText() != null
                && ((EditTextPreference)preference).getEditText().getTransformationMethod() != null)
            preference.setSummary(((EditTextPreference)preference)
                    .getEditText()
                    .getTransformationMethod()
                    .getTransformation(newValue.toString(),
                            ((EditTextPreference)preference).getEditText()));

        if (key.equals("pref_show"))
            SettingsActivity.askApplyToAll(activity, preference, "show_");
        else if (key.equals("pref_sorting"))
            SettingsActivity.askApplyToAll(activity, preference, "sort_");
        else if (key.equals("pref_sorting_folders"))
            SettingsActivity.askApplyToAll(activity, preference, "ff_");

        // preference.getExtras().putString("value", newValue.toString());
        Logger.LogInfo("Preference: " + key + ": " + newValue.toString());
        intent.putExtra(key.replace("server_", ""), newValue.toString());
        final OpenApp app = ((OpenApplication)activity.getApplication());
        app.queueToTracker(new Runnable() {
            public void run() {
                app.getAnalyticsTracker().trackEvent("Preferences", "Change", key,
                        newValue instanceof Integer ? (Integer)newValue : 0);
            }
        });
        activity.setIntent(intent);
        if (Arrays.binarySearch(new String[] {
                "pref_fullscreen", "pref_fancy_menus", "pref_basebar", "pref_themes", "pref_stats",
                "pref_root", "pref_language"
        }, key) > -1) {
            prefs.setSetting("global", "restart", true);
            activity.setResult(OpenExplorer.RESULT_RESTART_NEEDED, intent);
        } else
            activity.setResult(OpenExplorer.RESULT_OK, intent);
        return true;
    }

    private static void askApplyToAll(final Context context, final Preference preference,
            final String spKeyPrefix) {
        Preferences prefs = new Preferences(context);
        final SharedPreferences sp = Preferences.getPreferences("views");
        final SharedPreferences spGlobal = prefs.getPreferences();
        final Runnable clearAll = new Runnable() {
            public void run() {
                SharedPreferences.Editor editor = sp.edit();
                Map<String, ?> map = sp.getAll();
                for (String pkey : map.keySet())
                    if (pkey.startsWith(spKeyPrefix))
                        editor.remove(pkey);
                editor.commit();
            }
        };
        /*
         * if(spGlobal.getBoolean("pref_always_" + spKeyPrefix, false)) {
         * clearAll.run(); }
         */
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.string.activity_resolver_use_always:
                        spGlobal.edit().putBoolean("pref_always_" + spKeyPrefix, true).commit();
                    case R.string.activity_resolver_use_once:
                        clearAll.run();
                        break;
                }
            }
        };

        DialogHandler.showConfirmationDialog(context,
                context.getResources().getString(R.string.apply_to_all),
                preference.getTitle().toString(), listener);
    }

    public static String GetMasterPassword(Context context)
    {
        return GetMasterPassword(context, true, true);
    }

    public static String GetMasterPassword(Context context, boolean useMasterKey,
            boolean useMasterPass) {
        String sigKey = GetSignatureKey(context, useMasterKey);
        if (!useMasterKey)
            return sigKey;
        String pass = null;
        if (useMasterPass)
            pass = new Preferences(context)
                    .getSetting("global", "pref_master_pass", (String)null);
        if (pass == null || pass.equals(""))
            return sigKey;
        else {
            try {
                pass = SimpleCrypto.decrypt(sigKey, pass);
            } catch (Exception e) {
            }
            return pass;
        }
    }

    private static String GetSignatureKey(Context context, boolean useMasterKey) {
        String ret = "";
        if (useMasterKey)
            ret = PrivatePreferences.getKey("master_key", "");
        if (!ret.equals(""))
            return ret;
        try {
            Signature[] sigs = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES).signatures;
            for (Signature sig : sigs)
                ret += sig.toCharsString();
        } catch (NameNotFoundException e) {
            Logger.LogError("No Package for Signature?", e);
        }
        return ret;
    }

    public DataManager getDataManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImageCacheService getImageCacheService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DownloadCache getDownloadCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThreadPool getThreadPool() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LruCache<String, Bitmap> getMemoryCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DiskLruCache getDiskCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionMode getActionMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionMode(ActionMode mode) {
        // TODO Auto-generated method stub

    }

    @Override
    public OpenClipboard getClipboard() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ShellSession getShellSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Preferences getPreferences() {
        return ((OpenApplication)getApplication()).getPreferences();
    }

    @Override
    public void refreshBookmarks() {
        // TODO Auto-generated method stub

    }

    @Override
    public GoogleAnalyticsTracker getAnalyticsTracker() {
        return ((OpenApplication)getApplication()).getAnalyticsTracker();
    }

    @Override
    public void queueToTracker(Runnable run) {
        ((OpenApplication)getApplication()).queueToTracker(run);
    }

    @Override
    public int getThemedResourceId(int styleableId, int defaultResourceId) {
        return ((OpenApplication)getApplication()).getThemedResourceId(styleableId,
                defaultResourceId);
    }

    public int getThemeId() {
        String themeName = getPreferences().getString("global", "pref_themes", "dark");
        if (themeName.equals("dark"))
            return R.style.AppTheme_Dark;
        else if (themeName.equals("light"))
            return R.style.AppTheme_Light;
        else if (themeName.equals("lightdark"))
            return R.style.AppTheme_LightAndDark;
        else if (themeName.equals("custom"))
            return R.style.AppTheme_Custom;
        return 0;
    }

}


package org.brandroid.utils;

import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.R.xml;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.JsonReader;

public class Preferences {
    private static Preferences preferences;
    private static Context mContext;
    private static Hashtable<String, SharedPreferences> mStorageHash = new Hashtable<String, SharedPreferences>();
    public static boolean Pref_Intents_Internal = true;
    public static boolean Pref_Text_Internal = true;
    /**
     * Handle Zip Files using OpenExplorer
     */
    public static boolean Pref_Zip_Internal = true;
    public static boolean Pref_ShowUp = false;
    public static boolean Pref_ShowThumbs = true;
    public static boolean Warn_TextEditor = false;
    public static boolean Warn_Networking = false;
    public static int Pref_Text_Max_Size = 500000;
    public static boolean Pref_Analytics = true;
    public static String Pref_Language = ""; // Default
    public static boolean Pref_Root = false;
    public static Boolean Is_Nook = null;
    public static int Run_Count = 0;
    public static String UID = null;

    public interface OnPreferenceInteraction {
        public void setSetting(String key, String value);

        public String getSetting(String key, String sDefault);
    }

    public Preferences(Context context) {
        mContext = context;
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        // mStorageHash.put(file, context.getSharedPreferences(file,
        // PreferenceActivity.MODE_PRIVATE));
        // context.getSharedPreferences(PREFS_NAME, 0);
    }

    public static synchronized SharedPreferences getPreferences(Context context, String file) {
        if (mStorageHash != null && file != null && mStorageHash.containsKey(file))
            return mStorageHash.get(file);
        if (preferences == null)
            preferences = new Preferences(context);
        Logger.LogVerbose("Getting instance of SharedPreferences for "
                + getPreferenceFilename(file));
        SharedPreferences prefs = null;
        if (context != null)
            prefs = context.getSharedPreferences(getPreferenceFilename(file),
                    PreferenceActivity.MODE_PRIVATE);
        if (prefs != null)
            mStorageHash.put(file, prefs);
        // else throw new NullPointerException("Couldn't create Preferences @ "
        // + file);
        return prefs;
    }

    public static void setContext(Context c) {
        mContext = c;
    }

    private static String getPreferenceFilename(String file) {
        return file.replaceAll("[^A-Za-z0-9\\-]", "_");
    }

    public static SharedPreferences getPreferences(String file) {
        if (file == null)
            file = "global";
        return getPreferences(mContext, file);
    }

    public JSONObject getSettings(String file) {
        JSONObject ret = new JSONObject();
        try {
            Map<String, ?> all = getPreferences(file).getAll();
            for (String key : all.keySet())
                ret.put(key, all.get(key));
        } catch (JSONException e) {
            Logger.LogError("Couldn't get all settings for " + file, e);
            ret = null;
        }
        return ret;
    }

    public JSONObject getSetting(String file, String key, JSONObject defValue) {
        try {
            String s = getPreferences(file).getString(key, defValue.toString());
            return new JSONObject(s);
        } catch (JSONException j) {
            return defValue;
        }
    }

    public JSONArray getSetting(String file, String key, JSONArray defValue) {
        try {
            String s = getPreferences(file).getString(key, defValue.toString());
            return new JSONArray(s);
        } catch (JSONException j) {
            return defValue;
        }
    }

    public String getSetting(String file, String key, String defValue) {
        try {

            String ret = getPreferences(file).getString(key, defValue);
            // Logger.LogInfo("Pref GET [" + file + ":" + key + "] = " + ret);
            return ret;
        } catch (ClassCastException cce) {
            Logger.LogWarning("Couldn't get string \"" + key + "\" from Prefs.", cce);
            return defValue;
        } catch (NullPointerException npe) {
            return defValue;
        }
    }

    public int getSetting(String file, String key, Integer defValue) {
        try {
            int ret = defValue;
            try {
                ret = getPreferences(file).getInt(key, defValue);
            } catch (ClassCastException e) {
                try {
                    ret = Integer.parseInt(getSetting(file, key, defValue.toString()));
                } catch (Exception e2) {
                    ret = defValue;
                }
            }
            // Logger.LogDebug("--GetSetting(" + file + "," + key + "," +
            // defValue + ")=" + ret);
            return ret;
        } catch (Exception e) {
            return defValue;
        }
    }

    public float getSetting(String file, String key, Float defValue) {
        try {
            float ret = defValue;
            try {
                ret = getPreferences(file).getFloat(key, defValue);
            } catch (ClassCastException e) {
                ret = Float.parseFloat(getSetting(file, key, defValue.toString()));
            }
            return ret;
        } catch (Exception e) {
            return defValue;
        }
    }

    public Double getSetting(String file, String key, Double defValue) {
        try {
            return Double.parseDouble(getSetting(file, key, defValue.toString()));
        } catch (Exception e) {
            return defValue;
        }
    }

    public Boolean getSetting(String file, String key, Boolean defValue) {
        try {
            Boolean b = getPreferences(file).getBoolean(key, defValue);
            return b;
        } catch (Exception e) {
            try {
                String def = "";
                if (defValue != null)
                    def = defValue.toString();
                String s = getSetting(file, key, def);
                return Boolean.parseBoolean(s);
            } catch (Exception e2) {
                Logger.LogError("Error getting setting [" + key + "] from " + file, e2);
                return defValue;
            }
        }
    }

    public Long getSetting(String file, String key, Long defValue) {
        // try {
        // return mStorage.getLong(key, defValue);
        // } catch(Throwable t) { return defValue; }
        try {
            String s = getPreferences(file).getString(key, defValue.toString());
            return Long.parseLong(s);
        } catch (Exception e) {
            return defValue;
        }
    }

    public String getString(String file, String key, String defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public int getInt(String file, String key, int defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public float getFloat(String file, String key, float defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public Boolean getBoolean(String file, String key, Boolean defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public Long getLong(String file, String key, Long defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public JSONObject getJSON(String file, String key, JSONObject defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public JSONArray getJSON(String file, String key, JSONArray defValue) {
        if (!hasSetting(file, key) && !file.equals("global"))
            return getSetting("global", key, defValue);
        else
            return getSetting(file, key, defValue);
    }

    public void setSettings(String file, JSONObject json) {
        SharedPreferences.Editor editor = getPreferences(file).edit();
        Iterator keys = json.keys();
        Object okey;
        while ((okey = keys.next()) != null) {
            String key = (String)okey;
            String val = null;
            try {
                val = json.getString(key);
            } catch (JSONException e) {
                Logger.LogError("Error storing " + key, e);
            }
            editor.putString(key, val);
        }
        editor.commit();
    }

    public void setSetting(String file, String key, String value) {
        try {
            // Logger.LogDebug("Setting " + key + " to " + value);
            // Logger.LogInfo("Pref set [" + file + ":" + key + "] = " + value);
            SharedPreferences.Editor editor = getPreferences(file).edit();
            editor.putString(key, value);
            // editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set " + key + " in " + file + " preferences.", e);
        }
    }

    public void setSetting(String file, String key, Boolean value) {
        try {
            if (file == null)
                file = "global";
            getPreferences(file).edit().putBoolean(key, value).commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set " + key + " in " + file + " preferences.", e);
        }
    }

    public void setSetting(String file, String key, Integer value) {
        try {
            getPreferences(file).edit().putInt(key, value).commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set " + key + " in " + file + " preferences.", e);
        }
    }

    public void setSetting(String file, String key, Float value) {
        try {
            getPreferences(file).edit().putFloat(key, value).commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set " + key + " in " + file + " preferences.", e);
        }
    }

    public void setSetting(String file, String key, Long value) {
        try {
            getPreferences(file).edit().putLong(key, value).commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set " + key + " in " + file + " preferences.", e);
        }
    }

    public void setSettings(String file, Object... vals) {
        try {
            SharedPreferences.Editor editor = getPreferences(file).edit();
            for (int i = 0; i < vals.length - 1; i += 2) {
                String key = vals[i].toString();
                Object val = vals[i + 1];
                if (val == null)
                    return;
                if (Integer.class.equals(val.getClass()))
                    editor.putInt(key, (Integer)val);
                else if (Float.class.equals(val.getClass()))
                    editor.putFloat(key, (Float)val);
                else if (Long.class.equals(val.getClass()))
                    editor.putLong(key, (Long)val);
                else if (Boolean.class.equals(val.getClass()))
                    editor.putBoolean(key, (Boolean)val);
                else
                    editor.putString(key, val.toString());
            }
            editor.commit();
        } catch (Exception e) {
            Logger.LogError("Couldn't set values in " + file + " preferences.", e);
        }
        // pairs.
    }

    public void setSetting(String file, String key, JSONObject value) {
        setSetting(file, key, value.toString());
    }

    public void setSettings(String file, String key, JSONArray value) {
        setSetting(file, key, value.toString());
    }

    public Boolean hasSetting(String file, String key) {
        try {
            return getPreferences(file).contains(key);
        } catch (Exception e) {
            return false;
        }
    }

    public SharedPreferences getPreferences() {
        return getPreferences("global");
    }

    public void upgradeViewSettings() {
        Logger.LogVerbose("Upgrading view preferences");
        Map<String, ?> globalPrefs = getPreferences("global").getAll();
        SharedPreferences.Editor globalOut = getPreferences("global").edit();
        SharedPreferences.Editor newPrefs = getPreferences("views").edit();
        int changes = 0;
        for (String key : globalPrefs.keySet()) {
            Object val = globalPrefs.get(key);
            if (key.startsWith("hide_") && val instanceof Boolean)
                newPrefs.putBoolean(key.replace("hide_", "show_"), !((Boolean)val));
            else if (key.startsWith("sort_") && val instanceof String)
                newPrefs.putString(key, (String)val);
            else if (key.startsWith("thumbs_") && val instanceof Boolean)
                newPrefs.putBoolean(key, (Boolean)val);
            else if (key.startsWith("view_") && val instanceof Integer)
                newPrefs.putInt(key, (Integer)val);
            else if (key.startsWith("view_") && val instanceof String) {
                String s = (String)val;
                try {
                    newPrefs.putInt(key, Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    newPrefs.putString(key, s);
                }
            } else
                continue;
            changes++;
            globalOut.remove(key);
        }
        globalOut.commit();
        newPrefs.commit();
        Logger.LogVerbose("Upgraded " + changes + " preferences");
    }
}

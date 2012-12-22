
package org.brandroid.openmanager.interfaces;

import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.utils.Preferences;

import android.content.Context;

public interface OpenContextProvider {
    public Context getContext();

    public String getSetting(OpenPath path, String key, String def);

    public Integer getSetting(OpenPath path, String key, Integer def);

    public Boolean getSetting(OpenPath path, String key, Boolean def);

    public Float getSetting(OpenPath path, String key, Float def);

    public void setSetting(OpenPath path, String key, String value);

    public void setSetting(OpenPath path, String key, Integer value);

    public void setSetting(OpenPath path, String key, Boolean value);

    public void setSetting(OpenPath path, String key, Float value);

    public void setSetting(String file, String key, Boolean value);

    public String getString(int stringId);

    public Preferences getPreferences();

    public void onChangeLocation(OpenPath path);

    public void refreshBookmarks();

    public void showToast(int resId);
}

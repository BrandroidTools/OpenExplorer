package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.SettingsActivity;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

@TargetApi(11)
public class PreferenceFragmentV11 extends PreferenceFragment implements OnPreferenceChangeListener {
    public PreferenceFragmentV11()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName("global");
        PreferenceManager.setDefaultValues(getActivity(), "global", R.xml.preferences,
                PreferenceActivity.MODE_PRIVATE, false);

        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen ps = getPreferenceScreen();
        if (getArguments().containsKey("key")) {
            Preference p = ps.findPreference(getArguments().getCharSequence("key"));
            ps.removeAll();
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pc = (PreferenceGroup)p;
                for (int i = 0; i < pc.getPreferenceCount(); i++)
                    ps.addPreference(pc.getPreference(i));
            } else
                ps.addPreference(p);
            setPreferenceScreen(ps);
        }

        setOnChange(getPreferenceScreen(), false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        if (((SettingsActivity)getActivity()).onPreferenceTreeClick(preferenceScreen,
                preference))
            return true;

        return false;
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return SettingsActivity.onPreferenceChange(preference, newValue, getActivity());
    }
}

package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.utils.Logger;

import com.actionbarsherlock.app.SherlockListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class OpenListFragment extends SherlockListFragment implements OnItemClickListener,
        OnItemSelectedListener, OnItemLongClickListener {
    // public static boolean CONTENT_FRAGMENT_FREE = true;
    // public boolean isFragmentValid = true;

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public OpenExplorer getExplorer() {
        return (OpenExplorer)getActivity();
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            Logger.LogInfo("onItemClick (" + arg0 + ", pos " + arg2 + ")");
        } catch (Exception e) {
        }
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            int id = -1;
            if (arg1 != null)
                id = arg1.getId();
            Logger.LogInfo("onItemSelected (" + id + ", pos " + arg2 + ")");
        } catch (Exception e) {
        }
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            int id = -1;
            if (arg1 != null)
                id = arg1.getId();
            Logger.LogInfo("onItemLongClick (" + id + ", pos " + arg2 + ")");
        } catch (Exception e) {
        }
        return false;
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        try {
            int id = -1;
            if (arg0 != null)
                id = arg0.getId();
            Logger.LogInfo("onNothingSelected(" + id + ") - " + getClassName());
        } catch (Exception e) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.LogInfo("<-onCreate - " + getClassName());
        // CONTENT_FRAGMENT_FREE = false;
    }

    @Override
    public void onDestroy() {
        // Logger.LogInfo("->onDestroy - " + getClassName());
        super.onDestroy();
    }

    /*
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) {
     * Logger.LogInfo("<-onCreateView - " + getClassName());
     * //CONTENT_FRAGMENT_FREE = false; return super.onCreateView(inflater,
     * container, savedInstanceState); }
     * @Override public void onViewCreated(View view, Bundle savedInstanceState)
     * { super.onViewCreated(view, savedInstanceState);
     * Logger.LogInfo("<-onViewCreated - " + getClassName()); }
     * @Override public void onPause() { super.onPause();
     * Logger.LogInfo("->onPause - " + getClassName()); }
     * @Override public void onResume() { super.onResume();
     * Logger.LogInfo("<-onResume - " + getClassName()); }
     * @Override public void onStart() { super.onStart();
     * Logger.LogInfo("<-onStart - " + getClassName()); }
     * @Override public void onStop() { super.onStop();
     * Logger.LogInfo("->onStop - " + getClassName()); //CONTENT_FRAGMENT_FREE =
     * true; }
     * @Override public void onSaveInstanceState(Bundle outState) {
     * super.onSaveInstanceState(outState);
     * Logger.LogInfo("->onSaveInstanceState - " + getClassName()); }
     */
}

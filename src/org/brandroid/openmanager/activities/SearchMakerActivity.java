
package org.brandroid.openmanager.activities;

import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;

import android.app.Activity;
import android.os.Bundle;

public class SearchMakerActivity extends Activity {

    @Override
    public boolean onSearchRequested() {
        EventHandler handler = new EventHandler(new FileManager());
        handler.startSearch(new OpenFile("/"), this);
        Bundle appData = new Bundle();
        appData.putBoolean("JARGON", true);
        startSearch(null, false, appData, false);
        return true;
    }
}

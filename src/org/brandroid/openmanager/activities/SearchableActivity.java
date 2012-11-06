
package org.brandroid.openmanager.activities;

import java.util.ArrayList;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.EventHandler.EventType;
import org.brandroid.openmanager.util.EventHandler.OnWorkerUpdateListener;
import org.brandroid.openmanager.util.FileManager;

import android.R.anim;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class SearchableActivity extends ListActivity {
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragments);
        mProgressBar = (ProgressBar)findViewById(android.R.id.progress);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            FileManager fm = new FileManager();
            EventHandler eh = new EventHandler(fm);
            eh.setUpdateListener(new OnWorkerUpdateListener() {
                @Override
                public void onWorkerThreadComplete(EventType type, String... results) {
                    setListAdapter(new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_single_choice, results));
                }

                @Override
                public void onWorkerProgressUpdate(int pos, int total) {
                    if (mProgressBar != null) {
                        mProgressBar.setMax(total);
                        mProgressBar.setProgress(pos);
                    }
                }

                @Override
                public void onWorkerThreadFailure(EventType type, OpenPath... files) {
                }
            });
            eh.searchFile(new OpenFile("/"), intent.getStringExtra(SearchManager.QUERY), this);
        }
    }
}

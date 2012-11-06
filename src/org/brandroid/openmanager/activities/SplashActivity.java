
package org.brandroid.openmanager.activities;

import java.util.Timer;
import java.util.TimerTask;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.utils.Preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class SplashActivity extends Activity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Dark);

        setContentView(R.layout.splash);

        findViewById(R.id.pref_start_videos).setOnClickListener(this);
        findViewById(R.id.pref_start_photos).setOnClickListener(this);
        findViewById(R.id.pref_start_internal).setOnClickListener(this);
        findViewById(R.id.pref_start_external).setOnClickListener(this);
        findViewById(R.id.pref_start_root).setOnClickListener(this);

        if (OpenFile.getExternalMemoryDrive(false) == null)
            findViewById(R.id.pref_start_external).setEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("start")) {
                String start = intent.getStringExtra("start");
                if ("Videos".equals(start))
                    ((RadioButton)findViewById(R.id.pref_start_videos)).setChecked(true);
                else if ("Photos".equals(start))
                    ((RadioButton)findViewById(R.id.pref_start_photos)).setChecked(true);
                else if ("/".equals(start))
                    ((RadioButton)findViewById(R.id.pref_start_root)).setChecked(true);
                else if ("Internal".equals(start))
                    ((RadioButton)findViewById(R.id.pref_start_internal)).setChecked(true);
                else
                    ((RadioButton)findViewById(R.id.pref_start_external)).setChecked(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 15000);
    }

    public void onClick(View v) {
        Intent intent = getIntent();
        if (intent == null)
            intent = new Intent();
        Preferences prefs = new Preferences(this);
        String start = prefs.getSetting("global", "pref_start", "");
        switch (v.getId()) {
            case R.id.pref_start_videos:
                // intent.putExtra("start", "Videos");
                start = "Videos";
                break;
            case R.id.pref_start_photos:
                // intent.putExtra("start", "Photos");
                start = "Photos";
                break;
            case R.id.pref_start_internal:
                // intent.putExtra("start", "Internal");
                start = "Internal";
                break;
            case R.id.pref_start_external:
                // intent.putExtra("start", "External");
                start = "External";
                break;
            case R.id.pref_start_root:
                // intent.putExtra("start", "Root");
                start = "Root";
                break;
        }
        new Preferences(this).setSetting("global", "pref_start", start);
        setResult(RESULT_OK, intent);
        finish();
    }
}

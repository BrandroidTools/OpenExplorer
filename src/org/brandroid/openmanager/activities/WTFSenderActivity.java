
package org.brandroid.openmanager.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.SubmitStatsTask;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class WTFSenderActivity extends Activity implements OnClickListener {
    private Preferences prefs;
    private final static int REQ_CODE_WTF_SEND = R.layout.wtf_sender;
    private static OpenFile crashFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Dark);
        setContentView(R.layout.wtf_sender);
        prefs = new Preferences(this);
        findViewById(R.id.wtf_yes).setOnClickListener(this);
        findViewById(R.id.wtf_no).setOnClickListener(this);
        findViewById(R.id.wtf_preview).setOnClickListener(this);
        findViewById(R.id.wtf_remember).setOnClickListener(this);
        findViewById(R.id.wtf_report).setVisibility(View.GONE);
        prefs.setSetting("global", "pref_autowtf", true);
        crashFile = Logger.getCrashFile();
        /*
         * if(crashFile != null && crashFile.exists()) { List<OpenPath> arr =
         * new ArrayList<OpenPath>(); arr.add(crashFile); OpenFile myPath =
         * OpenFile.getExternalMemoryDrive(true); new EventHandler(new
         * FileManager()).cutFile(arr,
         * myPath.getChild(crashFile.getName().substring(1)), this); crashFile =
         * myPath.getChild(crashFile.getName().substring(1)); }
         */
        if (crashFile != null && crashFile.exists()) {
            String path = crashFile.getParent().getPath();
            crashFile.rename("openexplorer_crash_data.txt");
            crashFile = new OpenFile(path).getChild("openexplorer_crash_data.txt");
        }

    }

    public static void sendEmail(Activity activity, String subj, String msg, OpenFile attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {
            "brandroid64@gmail.com"
        });
        intent.putExtra(Intent.EXTRA_SUBJECT, subj);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setType("text/plain");
        if (attachment != null && attachment.exists()) {
            Logger.LogDebug("Trying to attach: " + attachment.getUri().toString());
            ArrayList<Uri> uris = new ArrayList<Uri>();
            uris.add(attachment.getUri());
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        try {
            activity.startActivityForResult(intent, REQ_CODE_WTF_SEND);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.noApplications, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.LogVerbose("Activity Result for " + requestCode + " = " + resultCode);
        if (requestCode == REQ_CODE_WTF_SEND) {
            Toast.makeText(this, getString(R.string.thanks), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wtf_yes:
                if (crashFile != null && crashFile.exists())
                    EventHandler.execute(new SubmitStatsTask(this), crashFile.readAscii());
                String version = "?";
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (Exception e) {
                }
                Resources res = getResources();
                sendEmail(this, "Crash Report " + res.getString(R.string.app_title) + " v"
                        + version + " " + crashFile.readHead(1), crashFile.readHead(1) + "\n\n"
                        + res.getString(R.string.s_wtf_context) + "\n", crashFile);
                break;
            case R.id.wtf_no:
                finish();
                break;
            case R.id.wtf_preview:
                EditText mPreview = ((EditText)findViewById(R.id.wtf_report));
                mPreview.setText(crashFile.readAscii());
                mPreview.setVisibility(View.VISIBLE);
                v.setEnabled(false);
                break;
            case R.id.wtf_remember:
                prefs.setSetting("global", "prefs_autowtf", ((CheckBox)v).isChecked());
                break;
        }
    }
}

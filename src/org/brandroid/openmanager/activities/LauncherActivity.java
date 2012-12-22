
package org.brandroid.openmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    public static Boolean ENABLE_RESTART = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ENABLE_RESTART = true;
        restartOE();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartOE();
    }

    public void restartOE() {
        if (ENABLE_RESTART) {
            Intent i = new Intent(this, OpenExplorer.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtras(getIntent());
            startActivity(i);
            finish();
        } else
            finish();
        ENABLE_RESTART = false;
    }
}

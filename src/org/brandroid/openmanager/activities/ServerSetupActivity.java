
package org.brandroid.openmanager.activities;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class ServerSetupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layout = R.layout.server;

        int iServersIndex = -1;
        Bundle b = savedInstanceState;
        if (b == null)
            b = new Bundle();
        if (b.containsKey("server_index"))
            iServersIndex = b.getInt("server_index");

        final Context context = this;
        final OpenServers servers = SettingsActivity.LoadDefaultServers(context);
        final OpenServer server = iServersIndex > -1 ? servers.get(iServersIndex)
                : new OpenServer().setName("New Server");

        setContentView(layout);
    }
}

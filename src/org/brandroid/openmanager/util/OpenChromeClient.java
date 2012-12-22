
package org.brandroid.openmanager.util;

import org.brandroid.openmanager.R;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

public class OpenChromeClient extends WebChromeClient {
    public TextView mStatus;

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (mStatus != null) {
            if (newProgress == 100)
                mStatus.setVisibility(View.INVISIBLE);
            else
                mStatus.setText(view.getContext().getString(R.string.s_status_loading) + " ("
                        + newProgress + ")");
        }
        super.onProgressChanged(view, newProgress);
    }
}

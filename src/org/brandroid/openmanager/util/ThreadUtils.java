package org.brandroid.openmanager.util;

import android.os.Handler;

public class ThreadUtils {
    private final static Handler mHandler = new Handler();

    public static void run(final Runnable work, final Runnable onUI)
    {
        new Thread(new Runnable() {
            public void run() {
                work.run();
                mHandler.post(onUI);
            }
        });
    }
}

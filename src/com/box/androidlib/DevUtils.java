/*******************************************************************************
 * Copyright 2011 Box.net.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import android.util.Log;

/**
 * Utility methods for development.
 * 
 * @author developers@box.net
 */
public final class DevUtils {

    /**
     * No instantiation allowed.
     */
    private DevUtils() {
    }

    /**
     * Send string to logcat. If the string contains multiple lines, split them up and send them to logcat.
     * 
     * @param str
     *            the string to log
     */
    public static void logcat(final String str) {
        if (!BoxConfig.getInstance().getHttpLoggingEnabled()) {
            return;
        }
        final String[] lines = str.split("\n");
        for (int i = 0; i < lines.length; i++) {
            Log.d("BOXBOX", lines[i]);
        }
    }

    /**
     * Send num to logcat.
     * 
     * @param num
     *            the num to log.
     */
    public static void logcat(final int num) {
        DevUtils.logcat("int: " + String.valueOf(num));
    }
}

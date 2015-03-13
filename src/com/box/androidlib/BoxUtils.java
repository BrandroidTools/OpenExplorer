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

/**
 * Utility methods.
 * 
 * @author developers@box.net
 */
public final class BoxUtils {

    /** number of bytes in a kilobyte (1024). */
    private static final long BYTES_IN_KILOBYTE = 1024;
    /** number of bytes in a megabyte (1048576). */
    private static final long BYTES_IN_MEGABYTE = 1048576;
    /** number of bytes in a gigabyte (1073741824). */
    private static final long BYTES_IN_GIGABYTE = 1073741824;

    /**
     * No instantiation allowed.
     */
    private BoxUtils() {
    }

    /**
     * Convert a String to an int if possible. Instead of throwing an exception like Integer.parseInt() does, return a specified default int value if an error
     * is encountered.
     * 
     * @param string
     *            String to be parsed.
     * @param defaultValue
     *            default value to be returned if there is a parse error
     * @return parsed int value
     */
    public static int parseInt(final String string, final int defaultValue) {
        try {
            return Integer.parseInt(string);
        }
        catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Converts a String to an int if possible. Instead of throwing an exception like Integer.parseInt() does, return 0 if an error is encountered.
     * 
     * @param string
     *            String to be parsed.
     * @return parsed int valoue
     */
    public static int parseInt(final String string) {
        return parseInt(string, 0);
    }

    /**
     * Convert a String to a long if possible. Instead of throwing an exception like Long.parseLong() does, return a specified default long value if an error is
     * encountered.
     * 
     * @param string
     *            String to be parsed
     * @param defaultValue
     *            default value to be returned if there is a parse error
     * @return parsed long value
     */
    public static long parseLong(final String string, final long defaultValue) {
        try {
            return Long.parseLong(string);
        }
        catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Convert a String to a long if possible. Instead of throwing an exception like Long.parseLong() does, return 0 if an error is encountered.
     * 
     * @param string
     *            String to be parsed
     * @return parsed long value
     */
    public static long parseLong(final String string) {
        return parseLong(string, 0L);
    }

    /**
     * Convert a String to a float if possible. Instead of throwing an exception like Float.parseFloat() does, return a specified default value if an error is
     * encountered
     * 
     * @param string
     *            String to be parsed
     * @param defaultValue
     *            default value to be returned if a parse error
     * @return parsed float value
     */
    public static float parseFloat(final String string, final float defaultValue) {
        try {
            return Float.parseFloat(string);
        }
        catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Convert a String to a float if possible. Instead of throwing an exception like Float.parseFloat() does, return 0 if an error is encountered
     * 
     * @param string
     *            String to be parsed
     * @return parsed float value
     */
    public static float parseFloat(final String string) {
        return parseFloat(string, 0f);
    }

    /**
     * Box API sometimes returns sizes as just the number of bytes, and sometimes returns sizes as strings like "7.4KB". This normalizes to a number of bytes.
     * 
     * @param string
     *            The size string from Box API
     * @return the equivalent number of bytes.
     */
    public static long parseSizeString(final String string) {
        long factor = 1;
        final String string2 = string.toLowerCase().trim();
        if (string2.endsWith("kb")) {
            factor = BYTES_IN_KILOBYTE;
        }
        else if (string2.endsWith("mb")) {
            factor = BYTES_IN_MEGABYTE;
        }
        else if (string2.endsWith("gb")) {
            factor = BYTES_IN_GIGABYTE;
        }
        else {
            return parseLong(string);
        }
        return (long) (BoxUtils.parseFloat(string2.replaceAll("kb", "").replaceAll("mb", "").replaceAll("gb", "").replaceAll("bytes", "").trim()) * factor);
    }
}

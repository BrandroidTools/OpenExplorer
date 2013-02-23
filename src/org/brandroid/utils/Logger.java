
package org.brandroid.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.FileManager;
import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class Logger {
    private static String[] sLastMessage = new String[] {
            "", "", "", "", ""
    };
    private static Integer[] iLastCount = new Integer[] {
            0, 0, 0, 0, 0
    };
    private final static Boolean DO_LOG = true; // global static
    private static Boolean bLoggingEnabled = true; // this can be set view
                                                   // preferences
    public final static Integer MIN_DB_LEVEL = Log.INFO;
    public final static String LOG_KEY = "OpenExplorer";
    private static LoggerDbAdapter dbLog;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (dbLog != null)
            dbLog.close();
    }

    public static Boolean isLoggingEnabled() {
        return bLoggingEnabled && DO_LOG;
    }

    public static void setLoggingEnabled(Boolean enable) {
        bLoggingEnabled = enable;
    }

    /**
     * Checks the last call to logger for specified level. This is to prevent
     * duplicate log calls.
     * 
     * @param msg
     * @param level
     * @return True to indicate the last request was the same and should not
     *         repeat, False otherwise
     */
    private static boolean CheckLastLog(String msg, int level) {
        if (!isLoggingEnabled())
            return true;
        level -= 2;
        if (level < 0 || level > 4)
            return false;
        if (sLastMessage[level] != null && msg != null && msg.equalsIgnoreCase(sLastMessage[level])) {
            iLastCount[level]++;
            return true;
        } else if (iLastCount[level] > 0) {
            Log.println(level, LOG_KEY, "The last message repeated " + iLastCount[level] + " times");
            iLastCount[level] = 0;
        }
        sLastMessage[level] = msg;
        return false;
    }

    private static int getMyStackTraceCount(StackTraceElement[] els) {
        int ret = 0;
        for (int i = 0; i < els.length; i++)
            if (isMyClass(els[i].getClassName(), i))
                ret++;
        return ret;
    }

    private static StackTraceElement[] getMyStackTrace(Throwable e) {
        StackTraceElement[] elArray = e.getStackTrace();
        StackTraceElement[] ret = new StackTraceElement[getMyStackTraceCount(elArray)];
        int j = 0;
        for (int i = 0; i < elArray.length; i++)
            if (isMyClass(elArray[i].getClassName(), i))
                ret[j++] = elArray[i];
        return ret;
    }

    private static StackTraceElement[] getMyStackTrace(Exception e) {
        StackTraceElement[] elArray = e.getStackTrace();
        StackTraceElement[] ret = new StackTraceElement[getMyStackTraceCount(elArray)];
        int j = 0;
        for (int i = 0; i < elArray.length; i++)
            if (isMyClass(elArray[i].getClassName(), i))
                ret[j++] = elArray[i];
        return ret;
    }

    private static boolean isMyClass(String className, int index) {
        if (className.startsWith("java"))
            return false;
        if (className.contains("android") && !className.contains("support"))
            return false;
        if (className.contains("brandroid"))
            return true;
        return index < 2;
    }

    private static void LogToDB(int level, String msg, String stack) {
        if (level < MIN_DB_LEVEL)
            return;
        if (dbLog == null)
            return;
        dbLog.createItem(msg, level, stack);
    }

    public static void openDb(Context c) {
        dbLog = new LoggerDbAdapter(c);
    }

    public static Boolean hasDb() {
        return dbLog != null;
    }

    public static void setDb(LoggerDbAdapter newDb) {
        dbLog = newDb;
    }

    public static JSONArray getDbLogArray() {
        return dbLog != null ? dbLog.getAllItemsJSON() : new JSONArray();
    }

    public static String getDbLogs(Boolean clear) {
        if (dbLog == null)
            return "";
        String ret = dbLog.getAllItems();
        if (clear)
            dbLog.clear();
        return ret;
    }

    public static String getLogText() {
        if (dbLog == null)
            return "";
        return dbLog.getLogText();
    }

    public static int countLevel(int level) {
        if (dbLog == null)
            return 0;
        return dbLog.countLevel(level);
    }

    public static void clearDb() {
        if (dbLog != null)
            dbLog.clear();
    }

    public static int LogError(String msg) {
        if (CheckLastLog(msg, Log.ERROR))
            return 0;
        LogToDB(Log.ERROR, msg, "");
        return Log.e(LOG_KEY, msg);
    }

    public static int LogError(String msg, Error ex) {
        if (CheckLastLog(ex.getMessage(), Log.ERROR))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(ex);
        ex.setStackTrace(trace);
        LogToDB(Log.ERROR, msg, Log.getStackTraceString(ex));
        return Log.e(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), ex);
    }

    public static int LogError(String msg, Exception ex) {
        if (CheckLastLog(ex.getMessage(), Log.ERROR))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(ex);
        if (trace.length == 0)
            trace = ex.getStackTrace();
        ex.setStackTrace(trace);
        LogToDB(Log.ERROR, msg, Log.getStackTraceString(ex));
        return Log.e(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), ex);
    }

    public static boolean checkWTF() {
        OpenFile crp = getCrashFile();
        if (crp != null && crp.exists() && crp.length() > 0)
            return true;
        return countLevel(Log.ASSERT) > 0;
    }

    @SuppressLint("NewApi")
    public static OpenFile getCrashFile() {
        OpenFile ext = OpenFile.getExternalMemoryDrive(true);
        if (ext != null) {
            ext = (OpenFile)ext.getChild(".oe_crash.txt");
            if (ext.exists() && !ext.canWrite() && Build.VERSION.SDK_INT > 8)
                ext.getFile().setWritable(true);
            return ext;
        }
        ext = new OpenFile("/mnt/sdcard/.oe_crash.txt");
        if (ext.exists() && !ext.canWrite() && Build.VERSION.SDK_INT > 8)
            ext.getFile().setWritable(true);
        if (ext.canWrite())
            return ext;
        return null;
    }

    public static String getCrashReport(boolean full) {
        OpenFile crp = getCrashFile();
        if (crp == null || !crp.exists())
            return null;
        return full ? crp.readAscii() : crp.readHead(1);
    }

    public static int LogWTF(String msg, Throwable ex) {
        if (hasDb())
            LogToDB(Log.ASSERT, msg, Log.getStackTraceString(ex));
        OpenFile crp = getCrashFile();
        Throwable cause = ex.getCause();
        if (cause == null) // get root cause
            cause = ex;
        StackTraceElement[] trace = getMyStackTrace(cause);
        if (crp != null) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(crp.getFile(), true);
                if (fw != null) {
                    if (trace.length > 0) {
                        int index = Math.min(trace.length - 1, 1);
                        String badGuy = ex.getMessage();
                        if (badGuy.indexOf(":") > -1)
                            badGuy = badGuy
                                    .substring(badGuy.lastIndexOf(".", badGuy.indexOf(":")) + 1);
                        // badGuy = badGuy.substring(badGuy.lastIndexOf(".") +
                        // 1);
                        fw.write(badGuy + ": " + trace[index].getFileName() + ":"
                                + trace[index].getLineNumber() + " ("
                                + trace[index].getMethodName() + ")\n");
                    } else
                        fw.write("\n");
                    fw.write(Log.getStackTraceString(cause));
                    fw.write("\nVersion " + OpenExplorer.VERSION + "\n");
                    fw.write(getDbLogs(true));
                    fw.write("\n");
                    fw.write(DialogHandler.getDeviceInfo());
                }
                fw.flush();
                fw.close();
            } catch (Exception e) {
                Logger.LogDebug("Couldn't write to crash file!", e);
            }
            if (crp.exists())
                Logger.LogVerbose("Crash file created!");
            else
                Logger.LogWarning("Crash file written, but we can't find it!");
        } else
            Logger.LogWarning("Crash file is null!");
        try {
            return Log.wtf(
                    LOG_KEY,
                    msg
                            + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                    + trace[0].getLineNumber() + ")" : ""), ex);
        } catch (Exception e) {
            return -1;
        }
    }

    public static int LogWarning(String msg) {
        if (CheckLastLog(msg, Log.WARN))
            return 0;
        LogToDB(Log.WARN, msg, "");
        return Log.w(LOG_KEY, msg);
    }

    public static int LogWarning(String msg, Throwable w) {
        if (CheckLastLog(w.getMessage(), Log.WARN))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(w);
        w.setStackTrace(trace);
        LogToDB(Log.WARN, msg, Log.getStackTraceString(w));
        return Log.w(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), w);
    }

    public static int LogWarning(String msg, Exception w) {
        if (CheckLastLog(w.getMessage(), Log.WARN))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(w);
        w.setStackTrace(trace);
        LogToDB(Log.WARN, msg, Log.getStackTraceString(w));
        return Log.w(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), w);
    }

    /*
     * Used for usage tracking
     */
    public static int LogInfo(String msg) {
        if (CheckLastLog(msg, Log.INFO))
            return 0;
        LogToDB(Log.INFO, msg, "");
        return Log.i(LOG_KEY, msg);
    }

    public static int LogInfo(String msg, String stack) {
        if (CheckLastLog(msg, Log.INFO))
            return 0;
        LogToDB(Log.DEBUG, msg, stack);
        return Log.d(LOG_KEY, msg);
    }

    public static int LogInfo(String msg, Throwable t) {
        if (CheckLastLog(msg, Log.INFO))
            return 0;
        LogToDB(Log.DEBUG, msg, Log.getStackTraceString(t));
        return Log.d(LOG_KEY, msg, t);
    }

    public static int LogInfo(String msg, Error e) {
        if (CheckLastLog(e.getMessage(), Log.INFO))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(e);
        e.setStackTrace(trace);
        LogToDB(Log.INFO, msg, Log.getStackTraceString(e));
        return Log.i(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), e);
    }

    public static int LogInfo(String msg, Exception e) {
        if (CheckLastLog(e.getMessage(), Log.INFO))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(e);
        e.setStackTrace(trace);
        LogToDB(Log.INFO, msg, Log.getStackTraceString(e));
        return Log.i(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), e);
    }

    public static int LogDebug(String msg) {
        if (CheckLastLog(msg, Log.DEBUG))
            return 0;
        LogToDB(Log.DEBUG, msg, "");
        return Log.d(LOG_KEY, msg);
    }

    public static int LogDebug(String msg, String stack) {
        if (CheckLastLog(msg, Log.DEBUG))
            return 0;
        LogToDB(Log.DEBUG, msg, stack);
        return Log.d(LOG_KEY, msg);
    }

    public static int LogDebug(String msg, Throwable e) {
        if (CheckLastLog(e.getMessage(), Log.DEBUG))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(e);
        e.setStackTrace(trace);
        LogToDB(Log.DEBUG, msg, Log.getStackTraceString(e));
        return Log.d(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), e);
    }

    public static int LogDebug(String msg, Exception e) {
        if (CheckLastLog(e.getMessage(), Log.DEBUG))
            return 0;
        StackTraceElement[] trace = getMyStackTrace(e);
        e.setStackTrace(trace);
        LogToDB(Log.DEBUG, msg, Log.getStackTraceString(e));
        return Log.d(
                LOG_KEY,
                msg
                        + (trace.length > 0 ? " (" + trace[0].getFileName() + ":"
                                + trace[0].getLineNumber() + ")" : ""), e);
    }

    public static int LogVerbose(String msg) {
        if (CheckLastLog(msg, Log.VERBOSE))
            return 0;
        LogToDB(Log.VERBOSE, msg, "");
        return Log.v(LOG_KEY, msg);
    }

    public static int LogVerbose(String msg, Throwable t) {
        if (CheckLastLog(msg, Log.VERBOSE))
            return 0;
        LogToDB(Log.VERBOSE, msg, Log.getStackTraceString(t));
        return Log.v(LOG_KEY, msg, t);
    }

    public static void closeDb() {
        if (hasDb())
            dbLog.close();
    }
}

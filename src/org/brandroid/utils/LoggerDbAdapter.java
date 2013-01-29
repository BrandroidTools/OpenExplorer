
package org.brandroid.utils;

import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LoggerDbAdapter {
    public static final String KEY_ID = "_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_STACK = "stack";
    public static final String KEY_STAMP = "stamp";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "log.db";
    private static final String DATABASE_TABLE = "error_log";
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_ID
            + " integer primary key autoincrement, " + KEY_MESSAGE + " text null, " + KEY_LEVEL
            + " int not null default 0, " + KEY_STACK + " text not null, " + KEY_STAMP
            + " int not null, " + "sent int not null default 0);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Logger.LogVerbose("Creating table [" + DATABASE_NAME + "]");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.LogVerbose("Upgrading table [" + DATABASE_TABLE + "] from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public LoggerDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public LoggerDbAdapter open() throws SQLException {
        if (mDb != null && mDb.isOpen())
            return this;
        if (mDbHelper == null)
            mDbHelper = new DatabaseHelper(mCtx);
        try {
            if (mDb == null)
                mDb = mDbHelper.getWritableDatabase();
        } catch (IllegalStateException ise) {
            Logger.LogError("Couldn't open logger", ise);
        }
        return this;
    }

    public void close() {
        if (mDb != null && mDb.isOpen())
            mDb.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mDb != null && mDb.isOpen())
            close();
    }

    public long createItem(String message, int level, String stack) {
        if (mDb == null || !mDb.isOpen())
            open();
        if (mDb == null)
            return -1;
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MESSAGE, message);
        initialValues.put(KEY_LEVEL, level);
        initialValues.put(KEY_STACK, stack);
        initialValues.put(KEY_STAMP,
                Math.abs(new java.util.Date().getTime() - new Date(4, 9, 2011).getTime()) / 1000);

        try {
            if (mDb.replace(DATABASE_TABLE, null, initialValues) > -1)
                return 1;
            else
                return 0;
        } catch (Exception e) {
            // we shouldn't log to this one in case it creates an infinite loop
            // Logger.LogError("Couldn't write to Log DB.", e);
            return 0;
        }
    }

    private String arrayToString(int[] list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(list[i]);
        }
        return sb.toString();
    }

    public void updateItemsSent(int[] ids) {
        ContentValues sentValues = new ContentValues();
        sentValues.put("sent", 1);
        mDb.update(DATABASE_TABLE, sentValues, "ids in (" + arrayToString(ids) + ")", null);
    }

    public Cursor fetchAllItems() {
        return fetchAllItems(true);
    }

    public Cursor fetchAllItems(boolean unsentOnly) {
        open();
        try {
            return mDb.query(DATABASE_TABLE, new String[] {
                    KEY_ID, KEY_MESSAGE, KEY_LEVEL, KEY_STACK, KEY_STAMP
            }, unsentOnly ? "sent = 0" : "", null, null, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    public int countLevel(int level) {
        open();
        try {
            Cursor c = mDb.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE, new String[] {
                KEY_LEVEL + " = " + level
            });
            if (c.getCount() == 0)
                return 0;
            int ret = 0;
            if (c.moveToFirst())
                ret = c.getInt(0);
            c.close();
            return ret;
        } catch (Exception e) {
            return 0;
        }
    }

    public int clear() {
        if (mDb != null && mDb.isOpen())
            return mDb.delete(DATABASE_TABLE, null, null);
        else
            return -1;
    }

    public JSONArray getAllItemsJSON() {
        // JSONObject ret = new JSONObject();
        JSONArray items = new JSONArray();
        Cursor c = fetchAllItems();
        if (c == null)
            return items;
        c.moveToFirst();
        // sb.append("{errors:[");
        while (!c.isAfterLast()) {
            int lvl = c.getInt(2);
            if (lvl == Log.INFO || lvl == Log.ERROR || lvl == Log.ASSERT) {
                int id = c.getInt(0);
                String msg = c.getString(1);
                String stack = c.getString(3);
                int stamp = c.getInt(4);
                // if(!c.isFirst()) sb.append(",");
                JSONArray row = new JSONArray();
                row.put(id);
                row.put(stamp);
                row.put(lvl);
                row.put(msg);
                row.put(stack);
                items.put(row);
            }
            // sb.append("[" + id + "," + stamp + "," + lvl + ",\"" +
            // msg.replace("\"", "\\\"") + "\"," + ",\"" + stack.replace("\"",
            // "\\\""));
            c.moveToNext();
        }
        // sb.append("]}");
        c.close();
        // clear();
        // Logger.LogVerbose("Sending Error report: " + retStr);
        return items;
    }

    public String getAllItems() {
        return getAllItemsJSON().toString();
    }

    public String getLogText() {
        Cursor c = fetchAllItems(false);
        if (c == null)
            return "";
        StringBuilder sb = new StringBuilder();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            int id = c.getInt(0);
            String msg = c.getString(1).replace("\n", "\\n").replace("\t", "    ");
            int lvl = c.getInt(2);
            String stack = c.getString(3).replace("\n", "\\n").replace("\t", "    ");
            int stamp = c.getInt(4);
            sb.append(id + "\t" + lvl + "\t" + msg + "\t" + stamp + "\t" + stack + "\n");
            c.moveToNext();
        }
        c.close();
        return sb.toString();
    }
}


package org.brandroid.openmanager.adapters;

import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.utils.Logger;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenPathDbAdapter {
    public static final String KEY_ID = "_id";
    public static final String KEY_FOLDER = "folder";
    public static final String KEY_NAME = "name";
    public static final String KEY_SIZE = "size";
    public static final String KEY_MTIME = "mtime";
    // public static final String KEY_STAMP = "stamp";
    public static final String KEY_ATTRIBUTES = "atts";
    public static final String[] KEYS = {
            KEY_FOLDER, KEY_NAME, KEY_SIZE, KEY_MTIME, KEY_ATTRIBUTES
    };

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "files.db";
    private static final String DATABASE_TABLE = "files";
    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_ID
            + " integer primary key autoincrement, " + KEY_FOLDER + " text null, " + KEY_NAME
            + " text null, " + KEY_SIZE + " text not null, " + KEY_MTIME + " int not null, "
            // + KEY_STAMP + " int not null, "
            + KEY_ATTRIBUTES + " int null);";

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
            if (oldVersion < 4 && newVersion >= 5) {
                Logger.LogVerbose("We can do this upgrade [" + DATABASE_TABLE + "] from "
                        + oldVersion + " to " + newVersion + " and retain old data");
                db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN [" + KEY_ATTRIBUTES
                        + "] int null");
                if (newVersion >= 7)
                    db.execSQL("ALTER TABLE " + DATABASE_TABLE + " DROP COLUMN [stamp]");
                return;
            }
            Logger.LogVerbose("Upgrading table [" + DATABASE_TABLE + "] from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public OpenPathDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public OpenPathDbAdapter open() throws SQLException {
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
        try {
            if (mDb != null)
                close();
        } catch (Exception e) {
        }
    }

    public static int getKeyIndex(String keyName) {
        int ret = 0;
        for (String key : KEYS) {
            if (key.equals(keyName))
                return ret;
            ret++;
        }
        return -1;
    }

    private OpenPath[] slice(OpenPath[] input, int index, int size) {
        OpenPath[] ret = new OpenPath[size];
        for (int i = index; i < index + size; i++)
            ret[i - index] = input[i];
        return ret;
    }

    public long createItem(OpenPath[] files) {
        if (mDb == null || !mDb.isOpen())
            open();
        if (mDb == null)
            return -1;
        if (files.length > 50) {
            Logger.LogDebug("Splitting createItem(OpenPath[]) into groups");
            long ret = 0;
            for (int start = 0; start < files.length; start += 50)
                ret += createItem(slice(files, start, Math.min(50, files.length - start)));
            return ret;
        }
        try {
            String query = generateInsertStatement(files);
            if (query == null || query.equals(""))
                return 0;
            if (mDb == null || !mDb.isOpen())
                open();
            mDb.execSQL(query);
            return files.length;
        } catch (Exception e) {
            Logger.LogError("Couldn't do mass insert.");
            long ret = 0;
            for (OpenPath file : files)
                if (file != null)
                    ret += createItem(file, false);
            return ret;
        }
    }

    private String generateInsertStatement(OpenPath[] files) {
        if (files == null || files.length == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO [" + DATABASE_TABLE + "] ('" + KEY_FOLDER + "','" + KEY_NAME + "','"
                + KEY_SIZE + "','" + KEY_MTIME + "','" + KEY_ATTRIBUTES + "')");
        int i = 0;
        for (OpenPath file : files) {
            if (file == null)
                continue;
            generateInsertStatement(file, sb, i++ == 0);
        }
        // sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private void generateInsertStatement(OpenPath file, StringBuilder sb, boolean first) {
        if (!first)
            sb.append(" UNION ALL");
        sb.append(" SELECT '");
        String sParent = "";
        if (file.getParent() != null)
            sParent = file.getParent().getPath();
        else
            sParent = file.getPath().replace("/" + file.getName(), "");
        sb.append(sParent.replaceAll("'", "''"));
        sb.append("','");
        sb.append(file.getName().replaceAll("'", "''"));
        sb.append("','");
        sb.append(file.length());
        sb.append("','");
        sb.append(file.lastModified());
        sb.append("','");
        sb.append(file.getAttributes());
        sb.append("'");
    }

    public long createItem(OpenPath path, boolean removeOld) {
        open();
        if (mDb == null)
            return -1;
        ContentValues initialValues = new ContentValues();
        String sParent = "";
        if (path != null && path.getParent() != null)
            sParent = path.getParent().getPath();
        if (sParent != "" && !sParent.endsWith("/"))
            sParent += "/";
        initialValues.put(KEY_FOLDER, sParent);
        initialValues.put(KEY_NAME, path.getName());
        initialValues.put(KEY_SIZE, path.length());
        initialValues.put(KEY_MTIME, path.lastModified());
        // initialValues.put(KEY_STAMP, new java.util.Date().getTime());
        initialValues.put(KEY_ATTRIBUTES, path.getAttributes());
        // initialValues.put(KEY_STAMP, (new java.util.Date().getTime() - new
        // Date(4,9,2011).getTime()) / 1000);
        // Logger.LogVerbose("Adding " + path.getPath() + " to files.db");

        try {
            if (removeOld)
                mDb.delete(DATABASE_TABLE, KEY_FOLDER + " = '" + sParent + "' AND " + KEY_NAME
                        + " = '" + path.getName() + "'", null);
            if (mDb.replace(DATABASE_TABLE, null, initialValues) > -1)
                return 1;
            else
                return 0;
        } catch (Exception e) {
            Logger.LogError("Couldn't write to Files DB.", e);
            return 0;
        }
    }

    public int deleteFolder(OpenPath parent) {
        try {
            if (mDb != null && mDb.isOpen() && parent != null) {
                String sParent = parent.getPath();
                int ret = mDb.delete(DATABASE_TABLE,
                        KEY_FOLDER + " = '" + sParent.replaceAll("'", "''") + "'", null);
                Logger.LogDebug("deleteFolder(" + parent.getPath() + ") removed " + ret + " rows");
                return ret;
            } else
                return 0;
        } catch (Exception e) {
            Logger.LogError("Coudln't delete folder from Files DB.", e);
            return -1;
        }
    }

    private String getSortString(SortType sort) {
        boolean asc = true;
        String col = "";
        switch (sort.getType()) {
            case ALPHA_DESC:
                asc = false;
            case ALPHA:
                col = KEY_NAME;
                break;
            case DATE_DESC:
                asc = false;
            case DATE:
                col = KEY_MTIME;
                break;
            case SIZE_DESC:
                asc = false;
            case SIZE:
                col = KEY_SIZE;
                break;
            default:
                return "";
        }
        return col + " " + (asc ? "asc" : "desc");
    }

    public Cursor fetchItemsFromFolder(String folder, SortType sort) {
        open();
        try {
            if (!folder.endsWith("/"))
                folder += "/";
            if (folder != null && folder.endsWith("//"))
                folder = folder.substring(0, folder.length() - 1);
            Logger.LogDebug("Fetching from folder: " + folder + " (" + sort.toString() + ")");
            return mDb.query(true, DATABASE_TABLE, KEYS,
                    KEY_FOLDER + " = '" + folder.replaceAll("'", "''") + "'", null, null, null,
                    getSortString(sort), null);
        } catch (Exception e) {
            Logger.LogError(
                    "Couldn't fetch from folder " + folder + ". " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    public Cursor fetchSearch(String query, String folder) {
        open();
        if (mDb == null)
            return null;
        try {
            String where = KEY_NAME + " LIKE '%" + query.replaceAll("'", "''") + "%'";
            if (folder != null && folder.length() > 0)
                where += " AND " + KEY_FOLDER + " LIKE '" + folder.replace("'", "''") + "%'";
            return mDb.query(true, DATABASE_TABLE, KEYS, where, null, null, null,
                    getSortString(OpenPath.Sorting), null);
        } catch (Exception e) {
            Logger.LogError("Couldn't search for \"" + query + "\"", e);
            return null;
        }
    }

    public void cleanParents() {
        // mDb.rawQuery("UPDATE , selectionArgs)
    }

    public Cursor fetchAllItems() {
        open();
        try {
            return mDb.query(DATABASE_TABLE, KEYS, null, null, null, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    public int clear() {
        if (mDb != null && mDb.isOpen())
            return mDb.delete(DATABASE_TABLE, null, null);
        else
            return -1;
    }

}

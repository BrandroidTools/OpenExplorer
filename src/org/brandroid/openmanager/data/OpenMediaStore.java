
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.brandroid.openmanager.fragments.DialogHandler;
import android.database.Cursor;
import android.net.Uri;

public class OpenMediaStore extends OpenPath {
    private OpenCursor curs;
    private String id;
    private String name;
    private OpenFile mFile = null;
    private long size = -1;
    private long modified = -1;
    private int width = 0, height = 0;
    private long duration = -1;
    private String resolution = "";

    public OpenMediaStore(OpenCursor parent, Cursor cursor) {
        curs = parent;
        // Cursor cursor = parent.getCursor();
        id = cursor.getString(0);
        if (cursor.getColumnCount() > 1)
            name = cursor.getString(1);
        if (cursor.getColumnCount() > 2)
            mFile = new OpenFile(cursor.getString(2));
        try {
            size = cursor.getLong(cursor.getColumnIndexOrThrow("_size"));
        } catch (Exception e) {
            if (mFile != null)
                size = mFile.length();
        }
        try {
            modified = cursor.getLong(cursor.getColumnIndexOrThrow("date_modified"));
        } catch (Exception e) {
            if (mFile != null)
                modified = mFile.lastModified();
        }
        try {
            duration = cursor.getLong(cursor.getColumnIndexOrThrow("duration"));
        } catch (Exception e) {
            duration = -1;
        }
        try {
            width = cursor.getInt(cursor.getColumnIndexOrThrow("width"));
        } catch (Exception e) {
            width = 0;
        }
        try {
            height = cursor.getInt(cursor.getColumnIndexOrThrow("height"));
        } catch (Exception e) {
            height = 0;
        }
        try {
            resolution = cursor.getString(cursor.getColumnIndexOrThrow("resolution"));
            if (resolution.indexOf("x") > -1) {
                try {
                    if (height == 0)
                        height = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
                } catch (Exception he) {
                }
                try {
                    if (width == 0)
                        width = Integer.parseInt(resolution.substring(resolution.indexOf("x") + 1));
                } catch (Exception we) {
                }
            }
        } catch (Exception e) {
            resolution = "";
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String getName() {
        return name;
    }

    public OpenFile getFile() {
        return mFile;
    }

    @Override
    public String getPath() {
        return mFile == null ? getParent().getName() + id : mFile.getPath();
    }

    @Override
    public String getAbsolutePath() {
        return mFile == null ? null : mFile.getAbsolutePath();
    }

    @Override
    public long length() {
        return size;
    }

    @Override
    public OpenCursor getParent() {
        return curs;
    }

    @Override
    public OpenPath getChild(String name) {
        return null;
    }

    @Override
    public OpenPath[] list() {
        return null;
    }

    @Override
    public OpenPath[] listFiles() {
        return null;
    }

    @Override
    public Boolean isDirectory() {
        return false;
    }

    @Override
    public Boolean isFile() {
        return true;
    }

    @Override
    public Boolean isHidden() {
        return mFile == null ? false : mFile.isHidden();
    }

    @Override
    public Uri getUri() {
        return mFile == null ? null : mFile.getUri();
    }

    @Override
    public Long lastModified() {
        return mFile == null ? modified : mFile.lastModified();
    }

    @Override
    public Boolean canRead() {
        return mFile == null ? false : mFile.canRead();
    }

    @Override
    public Boolean canWrite() {
        return mFile == null ? false : mFile.canWrite();
    }

    @Override
    public Boolean canExecute() {
        return mFile == null ? false : mFile.canExecute();
    }

    @Override
    public Boolean exists() {
        return getFile().exists();
    }

    @Override
    public Boolean requiresThread() {
        return false;
    }

    @Override
    public Boolean delete() {
        return getFile().delete();
    }

    @Override
    public Boolean mkdir() {
        return getFile().mkdir();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return getFile().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return getFile().getOutputStream();
    }

    public long getMediaID() {
        return Long.parseLong(id);
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public String getDetails(boolean countHidden) {

        String deets = "";

        if (getWidth() > 0 || getHeight() > 0)
            deets += getWidth() + "x" + getHeight() + " | ";
        if (getDuration() > 0)
            deets += DialogHandler.formatDuration(getDuration()) + " | ";

        deets += DialogHandler.formatSize(length());

        return deets;
    }
}

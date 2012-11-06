
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Observer;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.interfaces.OpenContextProvider;
import org.brandroid.utils.Logger;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class OpenCursor extends OpenPath {
    private static final long serialVersionUID = -8828123354531942575L;
    private Cursor mCursor;
    private OpenMediaStore[] mChildren = new OpenMediaStore[0];
    private final String mName;
    private final Uri mUri;
    private String mTitle;
    private Long mTotalSize = 0l;
    private boolean loaded = false;
    private Long mModified = Long.MIN_VALUE;
    public static int LoadedCursors = 0;
    private UpdateBookmarkTextListener mListener = null;
    private static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;
    private final DataSetObserver mObserver;
    private final ContentObserver mContentObserver;

    private static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (DEBUG)
                Logger.LogDebug("OpenCursor.handleMessage(" + msg + ")");
        }
    };

    public OpenCursor(String name, Uri uri) {
        mName = mTitle = name;
        mUri = uri;
        loaded = false;
        mContentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (DEBUG)
                    Logger.LogDebug("OpenCursor.ContentObserver.onChange(" + selfChange + ")");
            }
        };
        mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mListener != null)
                    mListener.updateBookmarkCount(getListLength());
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                if (mListener != null)
                    mListener.updateBookmarkCount(getListLength());
            }
        };
    }

    @Override
    public int getListLength() {
        return mChildren.length;
    }

    @Override
    public int getChildCount(boolean countHidden) throws IOException {
        if (countHidden)
            return getListLength();
        else {
            int cnt = 0;
            for (OpenPath p : mChildren)
                if (!p.isHidden())
                    cnt++;
            return cnt;
        }
    }

    @Override
    public boolean showChildPath() {
        return true;
    }

    public void setUpdateBookmarkTextListener(UpdateBookmarkTextListener listener) {
        mListener = listener;
        mContentObserver.onChange(false);
        mObserver.onChanged();
    }

    public boolean hasListener() {
        return mListener != null;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public interface UpdateBookmarkTextListener {
        void updateBookmarkCount(int count);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mListener = null;
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.unregisterDataSetObserver(mObserver);
            if (!mCursor.isClosed())
                mCursor.close();
        }
    }

    public void setCursor(Cursor c) {
        // mCursor = c;
        if (c == null)
            return;
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.unregisterDataSetObserver(mObserver);
        }
        mCursor = c;
        c.registerContentObserver(mContentObserver);
        c.registerDataSetObserver(mObserver);
        ArrayList<OpenMediaStore> kids = new ArrayList<OpenMediaStore>(c.getCount());
        // mChildren = new OpenMediaStore[(int)c.getCount()];
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            OpenMediaStore tmp = new OpenMediaStore(this, c);
            mModified = Math.max(tmp.lastModified(), mModified);
            if (!tmp.exists())
                continue;
            if (!tmp.getFile().exists())
                continue;
            kids.add(tmp);
            mTotalSize += tmp.getFile().length();
        }
        mChildren = new OpenMediaStore[kids.size()];
        mChildren = kids.toArray(mChildren);
        if (mListener != null)
            mListener.updateBookmarkCount(mChildren.length);
        if (DEBUG)
            Logger.LogDebug(getName() + " found " + mChildren.length);
        loaded = true;
        c.close();
    }

    public void refresh() {
        if (DEBUG)
            Logger.LogDebug("Refreshing OpenCursor (" + mUri.toString() + ")");
        // if(mCursor != null) mCursor.requery();
        if (mListener != null)
            mListener.updateBookmarkCount(getListLength());
        if (DEBUG)
            Logger.LogDebug("Refreshing OpenCursor...DONE");
    }

    @Override
    public String getName() {
        return mTitle;
    }

    @Override
    public String getPath() {
        return mName;
    }

    @Override
    public String getAbsolutePath() {
        return mName;
    }

    @Override
    public long length() {
        return mChildren.length; // mCursor.getCount();
    }

    @Override
    public OpenPath getParent() {
        return null;
    }

    @Override
    public OpenPath getChild(String name) {
        return null;
    }

    @Override
    public OpenMediaStore[] list() {
        return mChildren;
        /*
         * OpenMediaStore[] ret = new OpenMediaStore[(int)length()];
         * mCursor.moveToFirst(); int i = 0; while(!mCursor.isAfterLast()) {
         * if(!mCursor.isBeforeFirst()) ret[i++] = new OpenMediaStore(this);
         * mCursor.moveToNext(); } return ret;
         */
    }

    @Override
    public OpenMediaStore[] listFiles() {
        refresh();
        return list();
    }

    @Override
    public Boolean isDirectory() {
        return true;
    }

    @Override
    public Boolean isFile() {
        return false;
    }

    @Override
    public Boolean isHidden() {
        return false;
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public Long lastModified() {
        return mModified;
    }

    @Override
    public Boolean canRead() {
        return true;
    }

    @Override
    public Boolean canWrite() {
        return false;
    }

    @Override
    public Boolean canExecute() {
        return false;
    }

    @Override
    public Boolean exists() {
        return true;
    }

    @Override
    public Boolean requiresThread() {
        return true;
    }

    @Override
    public Boolean delete() {
        return false;
    }

    @Override
    public Boolean mkdir() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public void setName(String name) {
        mTitle = name;
    }

}

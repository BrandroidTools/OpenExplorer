
package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class OpenArrayAdapter extends ArrayAdapter<OpenPath> {
    private final int KB = 1024;
    private final int MG = KB * KB;
    private final int GB = MG * KB;

    private BookmarkHolder mHolder;
    private String mName;
    private final OpenApp mApp;

    private int mViewMode = OpenExplorer.VIEW_LIST;

    public void setViewMode(int mode) {
        mViewMode = mode;
    }

    public OpenArrayAdapter(OpenApp app, int layout, List<OpenPath> data) {
        super(app.getContext(), layout, data);
        mApp = app;
    }

    public OpenArrayAdapter(OpenApp app, int layout, OpenPath[] data) {
        super(app.getContext(), layout, data);
        mApp = app;
    }

    @Override
    public void notifyDataSetChanged() {
        // Logger.LogDebug("Data set changed.");
        try {
            // if(mFileManager != null)
            // getExplorer().updateTitle(mFileManager.peekStack().getPath());
            super.notifyDataSetChanged();
        } catch (NullPointerException npe) {
            Logger.LogError("Null found while notifying data change.", npe);
        }
    }

    private final Handler handler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            notifyDataSetChanged();
            return true;
        }
    });

    public OpenPath getItem(int position) {
        return super.getItem(position);
    }

    // //@Override
    public View getView(int position, View view, ViewGroup parent) {
        final OpenPath file = super.getItem(position);
        return getView(file, mApp.getContext(), view, parent);
    }

    public View getView(OpenPath file, Context mContext, View view, ViewGroup parent) {
        if (file == null)
            return null;
        final String mName = file.getName();

        int mWidth = 36, mHeight = 36;
        if (mViewMode == OpenExplorer.VIEW_GRID)
            mWidth = mHeight = 128;

        BookmarkHolder mHolder = null;

        int mode = mViewMode == OpenExplorer.VIEW_GRID ? R.layout.grid_content_layout
                : R.layout.list_content_layout;

        if (view == null || view.getTag() == null || !BookmarkHolder.class.equals(view.getTag())
                || ((BookmarkHolder)view.getTag()).getMode() != mode) {
            LayoutInflater in = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = in.inflate(mode, parent, false);

            mHolder = new BookmarkHolder(file, mName, view, mode);

            view.setTag(mHolder);
            file.setTag(mHolder);

        } else {
            mHolder = (BookmarkHolder)view.getTag();
            // mHolder.cancelTask();
        }

        mHolder.setInfo(getFileDetails(file, false));

        if (file.getClass().equals(OpenMediaStore.class)) {
            mHolder.setPath(file.getPath());
            mHolder.showPath(true);
        } else
            mHolder.showPath(false);

        if (!mHolder.getTitle().equals(mName))
            mHolder.setTitle(mName);

        SoftReference<Bitmap> sr = file.getThumbnail(mApp, mWidth, mHeight, true, true); // ThumbnailCreator.generateThumb(file,
                                                                                         // mWidth,
                                                                                         // mHeight,
                                                                                         // false,
                                                                                         // false,
                                                                                         // getContext());
        // Bitmap b = ThumbnailCreator.getThumbnailCache(file.getPath(), mWidth,
        // mHeight);
        if (sr != null && sr.get() != null)
            mHolder.getIconView().setImageBitmap(sr.get());
        else
            ThumbnailCreator.setThumbnail(mApp, mHolder.getIconView(), file, mWidth, mHeight);

        return view;
    }

    private static String getFileDetails(OpenPath file, Boolean longDate) {
        // OpenPath file = mFileManager.peekStack().getChild(name);
        String deets = ""; // file.getPath() + "\t\t";

        if (file.isDirectory() && !file.requiresThread()) {
            try {
                deets = file.list().length + " items";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            deets = DialogHandler.formatSize(file.length());
        }

        deets += " | ";

        DateFormat df = new SimpleDateFormat(longDate ? "MM-dd-yyyy HH:mm" : "MM-dd");
        deets += df.format(file.lastModified());

        if (OpenExplorer.SHOW_FILE_DETAILS) {

            deets += " | ";

            deets += (file.isDirectory() ? "d" : "-");
            deets += (file.canRead() ? "r" : "-");
            deets += (file.canWrite() ? "w" : "-");
            deets += (file.canExecute() ? "x" : "-");

        }

        return deets;
    }

}


package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.Logger;
import org.brandroid.utils.ViewUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OpenPathAdapter extends BaseAdapter {
    public final OpenPath mPath;
    public int mViewMode = OpenExplorer.VIEW_LIST;
    public final OpenApp mApp;
    public boolean mShowThumbnails = true;
    public boolean mShowHidden = false;

    public OpenPathAdapter(OpenPath path, int view, OpenApp app) {
        mPath = path;
        mApp = app;
    }

    @Override
    public int getCount() {
        try {
            return mPath.getChildCount(mShowHidden);
        } catch (IOException e) {
            Logger.LogError("Error getting OpenPathAdapter.getCount()", e);
            return 0;
        }
    }

    @Override
    public OpenPath getItem(int position) {
        return mPath.getChild(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getViewMode() {
        return mViewMode;
    }

    public void setViewMode(int value) {
        mViewMode = value;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final OpenPath file = getItem(position);
        final String mName = file.getName();

        int mWidth = 36;
        int mHeight = mWidth;
        if (mViewMode == OpenExplorer.VIEW_GRID)
            mWidth = mHeight = 128;

        int mode = getViewMode() == OpenExplorer.VIEW_GRID ? R.layout.grid_content_layout
                : R.layout.list_content_layout;

        if (view == null
        // || view.getTag() == null
        // || !BookmarkHolder.class.equals(view.getTag())
        // || ((BookmarkHolder)view.getTag()).getMode() != mode
        ) {
            LayoutInflater in = (LayoutInflater)mApp.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            view = in.inflate(mode, parent, false);
            // mHolder = new BookmarkHolder(file, mName, view, mode);
            // view.setTag(mHolder);
            // file.setTag(mHolder);
        } // else mHolder = (BookmarkHolder)view.getTag();

        // mHolder.getIconView().measure(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        // Logger.LogVerbose("Content Icon Size: " +
        // mHolder.getIconView().getMeasuredWidth() + "x" +
        // mHolder.getIconView().getMeasuredHeight());

        // view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // mHolder.setInfo(getFileDetails(file, false));
        TextView mInfo = (TextView)view.findViewById(R.id.content_info);
        if (mInfo != null) {
            mInfo.setText(getFileDetails(file, false));

            if (file.showChildPath())
                mInfo.setText(mInfo.getText() + " : " + file.getPath());
            // mHolder.showPath(false);
        }

        TextView mNameView = (TextView)view.findViewById(R.id.content_text);
        if (mNameView != null)
            mNameView.setText(mName);

        final Context mContext = mApp.getContext();

        if (mApp.getClipboard().contains(file))
            mNameView.setTextAppearance(mContext, R.style.Text_Large_Highlight);
        else
            mNameView.setTextAppearance(mContext, R.style.Text_Large);

        if (file.isHidden())
            ViewUtils.setAlpha(0.5f, mNameView, mInfo);
        else
            ViewUtils.setAlpha(1.0f, mNameView, mInfo);

        // if(!mHolder.getTitle().equals(mName))
        // mHolder.setTitle(mName);
        RemoteImageView mIcon = (RemoteImageView)view.findViewById(R.id.content_icon);

        if (mIcon != null) {
            if (file.isHidden())
                mIcon.setAlpha(100);
            else
                mIcon.setAlpha(255);
            if (file.isTextFile())
                mIcon.setImageBitmap(ThumbnailCreator.getFileExtIcon(file.getExtension(), mContext,
                        mWidth > 72));
            else if (!mShowThumbnails || !file.hasThumbnail())
                mIcon.setImageDrawable(mContext.getResources().getDrawable(
                        ThumbnailCreator.getDefaultResourceId(file, mWidth, mHeight)));
            else {
                ThumbnailCreator.setThumbnail(mApp, mIcon, file, mWidth, mHeight);
            }
        }

        return view;
    }

    private String getFileDetails(OpenPath file, Boolean longDate) {
        // OpenPath file = getManager().peekStack().getChild(name);
        String deets = ""; // file.getPath() + "\t\t";

        if (file.isDirectory() && !file.requiresThread()) {
            try {
                deets = file.getChildCount(mShowHidden) + " "
                        + mApp.getResources().getString(R.string.s_files) + " | ";
                // deets = file.list().length + " items";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (file.isFile()) {
            deets = DialogHandler.formatSize(file.length()) + " | ";
        }

        DateFormat df = new SimpleDateFormat(longDate ? "MM-dd-yyyy HH:mm" : "MM-dd-yy");
        deets += df.format(file.lastModified());

        /*
         * deets += " | "; deets += (file.isDirectory()?"d":"-"); deets +=
         * (file.canRead()?"r":"-"); deets += (file.canWrite()?"w":"-"); deets
         * += (file.canExecute()?"x":"-");
         */

        return deets;
    }
}

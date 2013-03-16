/*
    Open Explorer, an open source file explorer & text editor
    Copyright (C) 2011 Brandon Bowles <brandroid64@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.brandroid.openmanager.data;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.ViewUtils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BookmarkHolder {
    private ImageView mEject;
    private View mParentView;
    private String sTitle;
    private String sPath;
    private OpenPath mFile;
    // private ThumbnailTask mTask;
    private int mode;

    public BookmarkHolder(String path, View view, int mode) {
        this(new OpenFile(path), getTitleFromPath(path), view, mode);
    }

    public BookmarkHolder(OpenPath path, String title, final View view, int mode) {
        mParentView = view;
        sPath = path.getPath();
        mFile = path;
        this.mode = mode;
        // mIndicate = (ImageView)view.findViewById(R.id.)
        setTitle(title);
    }

    public int getMode() {
        return mode;
    }

    public void hideViews(View... views) {
        for (View v : views)
            if (v != null)
                v.setVisibility(View.GONE);
    }

    public OpenPath getOpenPath() {
        return mFile;
    }

    public RemoteImageView getIconView() {
        return (RemoteImageView)mParentView.findViewById(R.id.content_icon);
    }

    public void setIconResource(int res) {
        ViewUtils.setImageResource(mParentView, res, R.id.content_icon);
    }

    public void setEjectClickListener(View.OnClickListener listener) {
        if (mEject != null)
            mEject.setOnClickListener(listener);
    }

    public void setEjectable(Boolean eject) {
        if (mEject != null)
            mEject.setVisibility(eject ? View.VISIBLE : View.GONE);
    }

    public Boolean isEjectable() {
        return isEjectable(getOpenPath());
    }
    public static Boolean isEjectable(OpenPath mFile) {
        String path = mFile.getPath().toLowerCase();
        if (path.startsWith("/storage/") && (path.endsWith("sdcard1") || path.contains("usb")))
            return true;
        if (path.indexOf("ext") == -1 || !path.startsWith("/removable/"))
            return false;
        if (mFile.getDepth() > 2 + (path.startsWith("/mnt/") ? 1 : 0))
            return false;
        return true;
    }

    public void setSelected(Boolean sel) {
    }

    public String getPath() {
        return sPath;
    }

    public void setPath(String path) {
        sPath = path;
        ViewUtils.setText(mParentView, path, R.id.text_path);
    }

    public String getTitle() {
        return sTitle;
    }

    public void setTitle(String title) {
        setTitle(title, true);
    }

    public void setTitle(String title, boolean permanent) {
        ViewUtils.setText(mParentView, title, R.id.content_text);
        if (permanent)
            sTitle = title;
    }

    public void hideTitle() {
        ViewUtils.setViewsVisible(mParentView, false, R.id.content_text);
    }

    public void showTitle() {
        ViewUtils.setViewsVisible(mParentView, true, R.id.content_text);
    }

    public View getView() {
        return mParentView;
    }

    public String getInfo() {
        TextView mInfo = (TextView)mParentView.findViewById(R.id.content_info);
        return mInfo != null ? mInfo.getText().toString() : null;
    }

    public void setInfo(final String info) {
        final TextView mInfo = (TextView)mParentView.findViewById(R.id.content_info);
        if (mInfo != null)
            mInfo.post(new Runnable() {
                public void run() {
                    mInfo.setText(info);
                }
            });
    }

    public void setSizeText(final String txt) {
        final TextView mSizeText = (TextView)mParentView.findViewById(R.id.size_text);
        if (mSizeText != null)
            mSizeText.post(new Runnable() {
                public void run() {
                    mSizeText.setText(txt);
                }
            });
    }

    private static String getTitleFromPath(String path) {
        if (path != "/") {
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }

    public void showPath(Boolean visible) {
        ViewUtils.setViewsVisible(mParentView, visible, R.id.text_path);
    }

    /*
     * public void setTask(ThumbnailTask task) { mTask = task; }
     */
    // public void cancelTask() { if(mTask!=null) mTask.cancel(true); }

    public Drawable getIcon(OpenApp app) {
        if (getIconView() != null && getIconView().getDrawable() != null)
            return getIconView().getDrawable();
        if (getOpenPath() != null) {
            Bitmap bmp = ThumbnailCreator.getThumbnailCache(app, getOpenPath(),
                    ContentFragment.mListImageSize, ContentFragment.mListImageSize);
            if (bmp != null)
                return new BitmapDrawable(bmp);
        }
        return null;
    }
}

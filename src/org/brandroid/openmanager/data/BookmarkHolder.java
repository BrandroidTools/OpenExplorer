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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BookmarkHolder {
    private RemoteImageView mIcon;
    private ImageView mEject;
    private TextView mMainText, mInfo, mPath, mSizeText;
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
        ensureViews();
        this.mode = mode;
        // mIndicate = (ImageView)view.findViewById(R.id.)
        setTitle(title);
    }

    public int getMode() {
        return mode;
    }

    private void ensureViews() {
        if (mIcon == null)
            mIcon = (RemoteImageView)mParentView.findViewById(R.id.content_icon);
        if (mMainText == null && mParentView.findViewById(R.id.content_text) != null && mParentView.findViewById(R.id.content_text) instanceof TextView)
            mMainText = (TextView)mParentView.findViewById(R.id.content_text);
        if (mEject == null)
            mEject = (ImageView)mParentView.findViewById(R.id.eject);
        if (mInfo == null)
            mInfo = (TextView)mParentView.findViewById(R.id.content_info);
        if (mSizeText == null)
            mSizeText = (TextView)mParentView.findViewById(R.id.size_text);
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
        ensureViews();
        return mIcon;
    }

    public void setIconResource(int res) {
        ensureViews();
        if (mIcon != null)
            mIcon.setImageDrawable(mParentView.getResources().getDrawable(res));
    }

    public void setIconDrawable(Drawable d) {
        mIcon.setImageDrawable(d);
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
        if (mPath != null)
            mPath.setText(path.substring(0, path.lastIndexOf("/")));
    }

    public String getTitle() {
        return sTitle;
    }

    public void setTitle(String title) {
        setTitle(title, true);
    }

    public void setTitle(String title, boolean permanent) {
        if (mMainText != null)
            mMainText.setText(title);
        if (permanent)
            sTitle = title;
    }

    public void hideTitle() {
        if (mMainText != null)
            mMainText.setVisibility(View.GONE);
    }

    public void showTitle() {
        if (mMainText != null)
            mMainText.setVisibility(View.VISIBLE);
    }

    public View getView() {
        return mParentView;
    }

    public String getInfo() {
        return mInfo != null ? mInfo.getText().toString() : null;
    }

    public void setInfo(String info) {
        if (mInfo != null)
            mInfo.setText(info);
    }

    public void setSizeText(String txt) {
        if (mSizeText != null)
            mSizeText.setText(txt);
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
        if (mPath != null)
            mPath.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /*
     * public void setTask(ThumbnailTask task) { mTask = task; }
     */
    // public void cancelTask() { if(mTask!=null) mTask.cancel(true); }

    public Drawable getIcon(OpenApp app) {
        if (getIconView() != null && getIconView().getDrawable() != null)
            return getIconView().getDrawable();
        if (mIcon != null && getOpenPath() != null) {
            Bitmap bmp = ThumbnailCreator.getThumbnailCache(app, getOpenPath(),
                    ContentFragment.mListImageSize, ContentFragment.mListImageSize);
            if (bmp != null)
                return new BitmapDrawable(bmp);
        }
        return null;
    }
}

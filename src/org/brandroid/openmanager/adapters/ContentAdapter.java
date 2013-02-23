
package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenFileRoot;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.OpenPathUpdateListener;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.SortType.Type;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.ThumbnailCreator.OnUpdateImageListener;
import org.brandroid.openmanager.views.OpenPathView;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Main Adapter used in OpenExplorer. Adapts Grid & List View.
 */
public class ContentAdapter extends BaseAdapter {
    private final int KB = 1024;
    private final int MG = KB * KB;
    private final int GB = MG * KB;

    private final OpenPath mParent;
    private final ArrayList<OpenPath> mData2 = new ArrayList<OpenPath>();
    public int mViewMode = OpenExplorer.VIEW_LIST;
    public boolean mShowThumbnails = true;
    private boolean mShowHiddenFiles = false;
    private SortType mSorting = SortType.ALPHA;
    private OpenApp mApp;
    private boolean mPlusParent = false;
    private boolean mShowDetails = true;
    private boolean mShowFiles = true;
    private boolean mShowChecks = false;

    private int checkboxOnId = -1;
    private int checkboxOffId = -1;
    private int clipboardId = -1;

    /**
     * Set of seleced message IDs.
     */
    private final CopyOnWriteArrayList<OpenPath> mSelectedSet = new CopyOnWriteArrayList<OpenPath>();

    /**
     * Callback from MessageListAdapter. All methods are called on the UI
     * thread.
     */
    public interface Callback {
        /** Called when the user selects/unselects a message */
        void onAdapterSelectedChanged(OpenPath path, boolean newSelected, int mSelectedCount);
    }

    private final Callback mCallback;

    public ContentAdapter(OpenApp app, Callback callback, int mode, OpenPath parent) {
        // super(context, layout, data);
        mApp = app;
        mCallback = callback;
        mParent = parent;

        if (Preferences.Pref_ShowUp && mParent.getParent() != null)
            mPlusParent = true;
        mViewMode = mode;
        fetchThemedAttributes();
    }

    public void fetchThemedAttributes() {
        checkboxOnId = mApp.getThemedResourceId(R.styleable.AppTheme_checkboxButtonOn,
                R.drawable.btn_check_on_holo_light);
        checkboxOffId = mApp.getThemedResourceId(R.styleable.AppTheme_checkboxButtonOff,
                R.drawable.btn_check_off_holo_light);
        clipboardId = mApp.getThemedResourceId(R.styleable.AppTheme_actionIconClipboard,
                R.drawable.ic_menu_clipboard_light);
    }

    public interface CheckClipboardListener {
        public boolean checkClipboard(OpenPath file);

        public void removeFromClipboard(OpenPath file);
    }

    public void setShowPlusParent(boolean showUp) {
        mPlusParent = showUp;
    }

    public void setShowDetails(boolean showDeets) {
        mShowDetails = showDeets;
    }

    public void setShowFiles(boolean showFiles) {
        mShowFiles = showFiles;
    }

    public Context getContext() {
        return mApp.getContext();
    }

    public Resources getResources() {
        return mApp.getResources();
    }

    public int getViewMode() {
        return mViewMode;
    }

    public void setViewMode(int mode) {
        mViewMode = mode;
        notifyDataSetChanged();
    }

    public void setSorting(SortType sort) {
        mSorting = sort;
        notifyDataSetChanged();
    }

    public SortType getSorting() {
        return mSorting;
    }

    public void updateData() {
        OpenPath[] list = getList();
        if (list == null)
            list = new OpenPath[0];
        updateData(list);
    }

    public void updateData(final OpenPath[] items) {
        updateData(items, true);
    }

    private void updateData(final OpenPath[] items, final boolean doSort) {
        long time = new Date().getTime();
        if (items == null) {
            // Logger.LogWarning("ContentAdapter.updateData warning: Items are null!");
            super.notifyDataSetChanged();
            return;
        }

        // new Thread(new Runnable(){public void run() {
        boolean showHidden = getShowHiddenFiles();
        boolean foldersFirst = getSorting().foldersFirst();
        Logger.LogVerbose("updateData on " + items.length + " items (for " + mParent + ") : "
                + (showHidden ? "show" : "hide") + " + " + (foldersFirst ? "folders" : "files")
                + " + " + (doSort ? mSorting.toString() : "no sort"));

        mData2.clear();

        if (items != null)
            for (OpenPath f : items) {
                if (f == null)
                    continue;
                if (!showHidden && f.isHidden())
                    continue;
                if (!f.requiresThread()) {
                    if (!f.exists())
                        continue;
                    if (f.isFile() && !(f.length() >= 0))
                        continue;
                }
                if (!mShowFiles && !f.isDirectory())
                    continue;
                mData2.add(f);
            }

        if (doSort)
            sort();

        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        // super.notifyDataSetChanged();
        /*
         * Please note, this is on purpose. We want to hook into
         * notifyDataSetChanged to ensure filters & sorting are enabled.
         */
        super.notifyDataSetChanged();
    }

    public void sort() {
        sort(mSorting);
    }

    public void sort(SortType sort) {
        OpenPath.Sorting = sort;
        if (mData2 != null && mData2.size() > 1)
            Collections.sort(mData2);
    }

    private OpenPath[] getList() {
        try {
            if (mParent == null
                    || (!mParent.isLoaded() && mParent instanceof OpenPathUpdateListener))
                return new OpenPath[0];
            if (mParent.requiresThread() && Thread.currentThread().equals(OpenExplorer.UiThread))
                return mParent.list();
            else if(!mParent.canRead())
                return new OpenFileRoot(mParent).listFiles();
            else
                return mParent.listFiles();
        } catch (Exception e) {
            Logger.LogError("Couldn't getList in ContentAdapter", e);
            return new OpenPath[0];
        }
    }

    public View getView(OpenPath path, View view, ViewGroup parent) {
        if (!mData2.contains(path))
            return null;
        return getView(mData2.indexOf(path), view, parent);
    }

    // //@Override
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        int mode = getViewMode();
        final int layout = getViewMode() == OpenExplorer.VIEW_GRID ? (!OpenExplorer.DEBUG_TOGGLE ? R.layout.file_grid_item
                : R.layout.grid_content_layout)
                : (!OpenExplorer.DEBUG_TOGGLE ? R.layout.file_list_item
                        : R.layout.list_content_layout);
        final boolean useLarge = getViewMode() == OpenExplorer.VIEW_GRID;
        final OpenPath file = getItem(position); // super.getItem(position);

        View row;

        if (view == null || view.getTag() == null || !(view.getTag() instanceof BookmarkHolder)
                || ((BookmarkHolder)view.getTag()).getMode() != mode) {
            LayoutInflater in = (LayoutInflater)getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            row = in.inflate(layout, parent, false);
            BookmarkHolder mHolder = new BookmarkHolder(file, file.getName(), row, mode);
            row.setTag(mHolder);
            // file.setTag(mHolder);
        } else {
            row = view;
        }

        if (file == null) {
            return row;
        } else if (row instanceof OpenPathView) {
            ((OpenPathView)row).associateFile(file, this);
        }

        Object o = file.getTag();
        if (o != null && o instanceof OpenPath && ((OpenPath)o).equals(file))
            return row;

        TextView mInfo = (TextView)row.findViewById(R.id.content_info);
        TextView mDate = (TextView)row.findViewById(R.id.content_date);
        // TextView mPathView =
        // (TextView)row.findViewById(R.id.content_fullpath);
        TextView mNameView = (TextView)row.findViewById(R.id.content_text);
        final ImageView mIcon = (ImageView)row.findViewById(R.id.content_icon);
        ImageView mCheck = (ImageView)row.findViewById(R.id.content_check);

        if (mPlusParent && position == 0) {
            mNameView.setText(R.string.s_menu_up);
            mIcon.setImageResource(useLarge ? R.drawable.lg_folder_up : R.drawable.sm_folder_up);
            if (mInfo != null)
                mInfo.setText("");
            if (mDate != null)
                mDate.setText("");
            return row;
        }
        final String mName = file.getName();

        int mWidth = getViewMode() == OpenExplorer.VIEW_GRID ? OpenExplorer.IMAGE_SIZE_GRID
                : OpenExplorer.IMAGE_SIZE_LIST;
        int mHeight = mWidth;

        boolean showLongDate = false;
        if (getResources().getBoolean(R.bool.show_long_date))
            showLongDate = true;

        // mHolder.getIconView().measure(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        // Logger.LogVerbose("Content Icon Size: " +
        // mHolder.getIconView().getMeasuredWidth() + "x" +
        // mHolder.getIconView().getMeasuredHeight());

        // view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // mHolder.setInfo(getFileDetails(file, false));

        if (mInfo != null) {
            String sInfo = String.format(file.getDetails(getShowHiddenFiles()), getResources()
                    .getString(R.string.s_files));
            if (OpenPath.Sorting.getType() == Type.SIZE
                    || OpenPath.Sorting.getType() == Type.SIZE_DESC)
                sInfo = "<b>" + sInfo + "</b>";
            if (mShowDetails && mParent.showChildPath()) {
                sInfo += " :: " + file.getPath().replace(file.getName(), "");
                showLongDate = false;
            } else if (!mShowDetails)
                sInfo = "";

            if (mDate != null)
                mDate.setText(file.getFormattedDate(showLongDate));
            else
                sInfo += (sInfo.equals("") ? "" : " | ") + file.getFormattedDate(showLongDate);

            mInfo.setText(Html.fromHtml(sInfo));
        }

        if (mNameView != null)
            mNameView.setText(mName);

        /*
         * if(file.isHidden()) ViewUtils.setAlpha(0.5f, mNameView, mPathView,
         * mInfo); else ViewUtils.setAlpha(1.0f, mNameView, mPathView, mInfo);
         */

        // if(!mHolder.getTitle().equals(mName))
        // mHolder.setTitle(mName);
        // RemoteImageView mIcon =
        // (RemoteImageView)view.findViewById(R.id.content_icon);

        if (mIcon != null) {
            // mIcon.invalidate();
            if (file.isHidden())
                mIcon.setAlpha(100);
            else
                mIcon.setAlpha(255);
            if (file.isTextFile())
                mIcon.setImageBitmap(ThumbnailCreator.getFileExtIcon(file.getExtension(),
                        getContext(), mWidth > 72));
            else if (!mShowThumbnails || !file.hasThumbnail()) {
                mIcon.setImageResource(ThumbnailCreator.getDefaultResourceId(file, mWidth, mHeight));
            } else { // if(!ThumbnailCreator.getImagePath(mIcon).equals(file.getPath()))
                // {
                // Logger.LogDebug("Bitmapping " + file.getPath());
                // if(OpenExplorer.BEFORE_HONEYCOMB) mIcon.setAlpha(0);
                ThumbnailCreator.setThumbnail(mApp, mIcon, file, mWidth, mHeight,
                        new OnUpdateImageListener() {
                            @Override
                            public void updateImage(final Bitmap b) {
                                if (mIcon.getTag() == null
                                        || (mIcon.getTag() instanceof OpenPath && ((OpenPath)mIcon
                                                .getTag()).equals(file)))
                                // if(!ThumbnailCreator.getImagePath(mIcon).equals(file.getPath()))
                                {
                                    Runnable doit = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!OpenExplorer.BEFORE_HONEYCOMB) {
                                                BitmapDrawable d = new BitmapDrawable(
                                                        getResources(), b);
                                                d.setGravity(Gravity.CENTER);
                                                ImageUtils.fadeToDrawable(mIcon, d);
                                            } else {
                                                mIcon.setImageBitmap(b);
                                                // mIcon.setAlpha(255);
                                            }
                                            mIcon.setTag(file);
                                        }
                                    };
                                    if (!Thread.currentThread().equals(OpenExplorer.UiThread))
                                        mIcon.post(doit);
                                    else
                                        doit.run();
                                }
                            }
                        });
            }
        }

        // row.setTag(file);
        boolean mChecked = (mSelectedSet != null && mSelectedSet.contains(file));
        boolean mShowCheck = true; // mChecked || (mSelectedSet != null &&
        // mSelectedSet.size() > 0);
        boolean mShowClip = mApp.getClipboard().contains(file);

        if (mShowClip) {
            ViewUtils.setViewsVisible(row, true, R.id.content_clipboard);
            ViewUtils.setOnClicks(row, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mApp.getClipboard().remove(file);
                    v.setVisibility(View.GONE);
                }
            }, R.id.content_clipboard);
        } else {
            ViewUtils.setViewsVisible(row, false, R.id.content_clipboard);
            ViewUtils.setOnClicks(row, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelected(file);
                }
            }, R.id.checkmark_area);
        }

        if (mCheck != null)
            mCheck.setImageResource(mChecked ? checkboxOnId : checkboxOffId);
        ViewUtils.setViewsVisible(row, mShowCheck, R.id.content_check);

        switch (OpenPath.Sorting.getType()) {
            case DATE:
            case DATE_DESC:
                mDate.setTextAppearance(getContext(), R.style.Text_Small_Highlight);
                //mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                break;
            case SIZE:
            case SIZE_DESC:
                //mInfo.setTextAppearance(getContext(), R.style.Text_Small_Highlight);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                break;
            case ALPHA:
            case ALPHA_DESC:
                mNameView.setTextAppearance(getContext(), R.style.Text_Large);
                //mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                break;
            default:
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                //mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                break;
        }

        return row;
    }

    @Override
    public int getCount() {
        return mData2.size() + (mPlusParent ? 1 : 0);
    }

    @Override
    public OpenPath getItem(int position) {
        if (mPlusParent && position == 0)
            return mParent.getParent();
        int pos = position - (mPlusParent ? 1 : 0);
        if (pos < 0 || pos >= mData2.size())
            return null;
        return mData2.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearData() {
        mData2.clear();
    }

    public void add(OpenPath p) {
        mData2.add(p);
    }

    public boolean contains(OpenPath f) {
        return mData2.contains(f);
    }

    public ArrayList<OpenPath> getAll() {
        return mData2;
    }

    public CopyOnWriteArrayList<OpenPath> getSelectedSet() {
        return mSelectedSet;
    }

    public void setSelectedSet(ArrayList<OpenPath> set) {
        for (OpenPath rememberedPath : set) {
            mSelectedSet.add(rememberedPath);
        }
    }

    public void addSelection(OpenPath path) {
        if (!mSelectedSet.contains(path))
            mSelectedSet.add(path);
    }

    /**
     * Clear the selection. It's preferable to calling {@link Set#clear()} on
     * {@link #getSelectedSet()}, because it also notifies observers.
     */
    public void clearSelection() {
        if (mSelectedSet.size() > 0) {
            mSelectedSet.clear();
            notifyDataSetChanged();
        }
    }

    public boolean isSelected(OpenPath path) {
        return getSelectedSet().contains(path);
    }

    public void toggleSelected(OpenPath path) {
        updateSelected(path, !isSelected(path));
    }

    /**
     * This is used as a callback from the list items, to set the selected state
     * <p>
     * Must be called on the UI thread.
     * 
     * @param itemView the item being changed
     * @param newSelected the new value of the selected flag (checkbox state)
     */
    private void updateSelected(OpenPath path, boolean newSelected) {
        if (newSelected) {
            mSelectedSet.add(path);
        } else {
            mSelectedSet.remove(path);
        }
        if (mCallback != null) {
            mCallback.onAdapterSelectedChanged(path, newSelected, mSelectedSet.size());
        }
    }

    public void setShowHiddenFiles(boolean show) {
        mShowHiddenFiles = show;
    }

    public boolean getShowHiddenFiles() {
        return mShowHiddenFiles;
    }
}


package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenApplication;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenFileRoot;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.OpenPathSizable;
import org.brandroid.openmanager.data.OpenPath.OpenPathUpdateHandler;
import org.brandroid.openmanager.data.OpenPath.SpaceHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.SortType.Type;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.ThumbnailCreator.OnUpdateImageListener;
import org.brandroid.openmanager.views.OpenPathView;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.stericson.RootTools.RootTools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.SparseArray;
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
    private SortedSet<OpenPath> mData2 = new TreeSet<OpenPath>();
    private OpenPath[] mFinalItems;
    private boolean isFinal = false;
    // private final List<OpenPath> mData2 = new ArrayList<OpenPath>();
    public int mViewMode = OpenExplorer.VIEW_LIST;
    public boolean mShowThumbnails = true;
    private boolean mShowHiddenFiles = false;
    private SortType mSorting = SortType.ALPHA;
    private OpenApp mApp;
    private boolean mPlusParent = false;
    private boolean mShowDetails = true;
    private boolean mShowFiles = true;

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
    public interface SelectionCallback {
        /** Called when the user selects/unselects a message */
        void onAdapterSelectedChanged(OpenPath path, boolean newSelected, int mSelectedCount);
    }

    private final SelectionCallback mCallback;

    public ContentAdapter(OpenApp app, SelectionCallback callback, int mode, OpenPath parent) {
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

        OpenPath.Sorting = mSorting;
        mData2.clear();
        isFinal = false;
        mFinalItems = null;

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

        finalize();
    }

    private void prefinalize() {
        if (isFinal)
            return;
        mFinalItems = mData2.toArray(new OpenPath[mData2.size()]);
        mData2.clear();
        isFinal = true;
    }

    public void finalize() {
        prefinalize();
        super.notifyDataSetChanged();
    }

    private void unfinalize() {
        if (!isFinal)
            return;
        isFinal = false;
        mData2.clear();
        mData2.addAll(Arrays.asList(mFinalItems));
    }

    public boolean isFinalized() {
        return isFinal;
    }

    @Override
    public void notifyDataSetChanged() {
        finalize();
        // super.notifyDataSetChanged();
        /*
         * Please note, this is on purpose. We want to hook into
         * notifyDataSetChanged to ensure filters & sorting are enabled.
         */
        super.notifyDataSetChanged();
    }

    private OpenPath[] getList() {
        try {
            if (mParent == null
                    || (!mParent.isLoaded() && mParent instanceof OpenPathUpdateHandler))
                return new OpenPath[0];
            if (mParent.requiresThread() && Thread.currentThread().equals(OpenExplorer.UiThread))
                return mParent.list();
            else if (!mParent.canRead())
            {
                if ((mParent instanceof OpenFile)
                        && OpenApplication.hasRootAccess(true))
                    return new OpenFileRoot(mParent).listFiles();
                else
                    return new OpenPath[0];
            } else
                return mParent.listFiles();
        } catch (Exception e) {
            Logger.LogError("Couldn't getList in ContentAdapter", e);
            return new OpenPath[0];
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        int mode = getViewMode();
        final int layout = getViewMode() == OpenExplorer.VIEW_GRID ? (!OpenExplorer.DEBUG_TOGGLE ? R.layout.file_grid_item
                : R.layout.grid_content_layout)
                : (!OpenExplorer.DEBUG_TOGGLE ? R.layout.file_list_item
                        : R.layout.list_content_layout);
        final boolean useLarge = getViewMode() == OpenExplorer.VIEW_GRID;
        final OpenPath file = getItem(position); // super.getItem(position);

        if (view == null || view.getTag() == null || !(view.getTag() instanceof BookmarkHolder)
                || ((BookmarkHolder)view.getTag()).getMode() != mode) {
            LayoutInflater in = (LayoutInflater)getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            view = in.inflate(layout, parent, false);
            BookmarkHolder mHolder = new BookmarkHolder(file, Utils.ifNull(file.getName(), "/"),
                    view, mode);
            view.setTag(mHolder);
            // file.setTag(mHolder);
        }

        TextView mInfo = (TextView)view.findViewById(R.id.content_info);
        TextView mDate = (TextView)view.findViewById(R.id.content_date);
        // TextView mPathView =
        // (TextView)row.findViewById(R.id.content_fullpath);
        TextView mNameView = (TextView)view.findViewById(R.id.content_text);
        final ImageView mIcon = (ImageView)view.findViewById(R.id.content_icon);
        ImageView mCheck = (ImageView)view.findViewById(R.id.content_check);

        if (mPlusParent && position == 0) {
            mNameView.setText(R.string.s_menu_up);
            mIcon.setImageResource(useLarge ? R.drawable.lg_folder_up : R.drawable.sm_folder_up);
            if (mInfo != null)
                mInfo.setText("");
            if (mDate != null)
                mDate.setText("");
            mCheck.setVisibility(View.GONE);
            return view;
        }

        if (file == null) {
            return view;
        } else if (view instanceof OpenPathView) {
            ((OpenPathView)view).associateFile(file, this);
        }

        // Object o = file.getTag();
        // if (o != null && o instanceof OpenPath && ((OpenPath)o).equals(file))
        // return view;

        final String mName = file.getName();

        final int mWidth = getViewMode() == OpenExplorer.VIEW_GRID ? OpenExplorer.IMAGE_SIZE_GRID
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
            SpannableStringBuilder sInfo = new SpannableStringBuilder(String.format(
                    file.getDetails(getShowHiddenFiles()), getResources()
                            .getString(R.string.s_files)));
            if (OpenPath.Sorting.getType() == Type.SIZE
                    || OpenPath.Sorting.getType() == Type.SIZE_DESC)
                sInfo.setSpan(new StyleSpan(Typeface.BOLD), 0, sInfo.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            if (mShowDetails && mParent.showChildPath()) {
                sInfo.append(" :: " + file.getPath().replace(file.getName(), ""));
                showLongDate = false;
            } else if (!mShowDetails)
                sInfo.clear();

            if (mDate != null)
                mDate.setText(file.getFormattedDate(showLongDate));
            else
                sInfo.append((sInfo.length() == 0 ? "" : " | ")
                        + file.getFormattedDate(showLongDate));

            mInfo.setText(sInfo);
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

        boolean hasOverlay = file instanceof OpenPath.ThumbnailOverlayInterface;
        if (hasOverlay)
            ViewUtils.setImageDrawable(view,
                    ((OpenPath.ThumbnailOverlayInterface)file).getOverlayDrawable(getContext(),
                            mViewMode == OpenExplorer.VIEW_GRID),
                    R.id.content_icon_overlay);
        ViewUtils.setViewsVisible(view, hasOverlay, R.id.content_icon_overlay);

        if (mIcon != null) {
            // mIcon.invalidate();
            ViewUtils.setAlpha(file.isHidden() ? 0.4f : 1.0f, view, R.id.content_icon);
            if (!mShowThumbnails || !file.hasThumbnail()) {
                mIcon.setImageDrawable(ThumbnailCreator.getDefaultDrawable(file, mWidth, mHeight,
                        getContext()));
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
                                            Drawable d = new BitmapDrawable(getResources(), b);
                                            ((BitmapDrawable)d).setGravity(Gravity.CENTER);
                                            ImageUtils.fadeToDrawable(mIcon, d);
                                            mIcon.setTag(file);
                                        }
                                    };
                                    OpenExplorer.post(doit);
                                }
                            }
                        });
            }
        }

        if (mCheck != null)
            mCheck.setImageResource((mSelectedSet != null && mSelectedSet.contains(file))
                    ? checkboxOnId : checkboxOffId);

        boolean mShowClip = false;
        if(mApp.getClipboard() != null)
        {
            mShowClip = mApp.getClipboard().contains(file);
        }

        if (mShowClip) {
            ViewUtils.setViewsVisible(view, true, R.id.content_clipboard);
            ViewUtils.setOnClicks(view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mApp.getClipboard().remove(file);
                    v.setVisibility(View.GONE);
                }
            }, R.id.content_clipboard);
        } else {
            ViewUtils.setViewsVisible(view, false, R.id.content_clipboard);
            final View p = view;
            ViewUtils.setOnClicks(view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelected(file, p);
                    if (mApp != null && mApp.getActionMode() != null)
                        mApp.getActionMode().invalidate();
                }
            }, R.id.checkmark_area);
        }

        switch (OpenPath.Sorting.getType()) {
            case DATE:
            case DATE_DESC:
                mDate.setTextAppearance(getContext(), R.style.Text_Small_Highlight);
                // mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                break;
            case SIZE:
            case SIZE_DESC:
                // mInfo.setTextAppearance(getContext(),
                // R.style.Text_Small_Highlight);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                break;
            case ALPHA:
            case ALPHA_DESC:
                mNameView.setTextAppearance(getContext(), R.style.Text_Large);
                // mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                break;
            default:
                mNameView.setTextAppearance(getContext(), R.style.Text_Large_Dim);
                // mInfo.setTextAppearance(getContext(), R.style.Text_Small);
                mDate.setTextAppearance(getContext(), R.style.Text_Small);
                break;
        }

        return view;
    }

    @Override
    public int getCount() {
        if (!isFinal)
            prefinalize();
        return mFinalItems.length + (mPlusParent ? 1 : 0);
    }

    @Override
    public OpenPath getItem(int position) {
        if (!isFinal)
            prefinalize();
        if (mPlusParent)
        {
            if (position == 0)
                return mParent.getParent();
            else
                position--;
        }
        if (position < 0 || position >= mFinalItems.length)
            return null;
        return mFinalItems[position];
    }

    @Override
    public long getItemId(int position) {
        if (!isFinal)
            prefinalize();
        return position;
    }

    public void clearData() {
        mData2.clear();
        isFinal = false;
        mFinalItems = null;
    }

    public void addAll(Collection<? extends OpenPath> collection) {
        if (isFinal)
            unfinalize();
        mData2.addAll(collection);
        notifyDataSetChanged();
    }

    public void selectAll()
    {
        mSelectedSet.addAll(isFinal ? Arrays.asList(mFinalItems) : mData2);
        notifyDataSetChanged();
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

    public void toggleSelected(OpenPath path, View row) {
        boolean sel = !isSelected(path);
        updateSelected(path, sel);
        row.setSelected(sel);
        ViewUtils.setImageResource(row, sel ? checkboxOnId : checkboxOffId, R.id.content_check);
        row.invalidate();
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
    
    public void getStatus2(final OpenPath.SpaceListener callback) {
        if (mParent instanceof OpenPath.OpenPathSizable
                && ((OpenPathSizable)mParent).getTotalSpace() > 0)
        {
            OpenPathSizable sz = (OpenPathSizable)mParent;
            callback.onSpaceReturned(sz.getTotalSpace(), sz.getUsedSpace(), sz.getThirdSpace());
        } else if (mParent instanceof OpenPath.SpaceHandler)
            new Thread(new Runnable() {
                public void run() {
                    {
                        ((SpaceHandler)mParent).getSpace(callback);
                    }
                }
            }).start();
        else callback.onException(null);
    }

    public CharSequence getStatus() {
        final SpannableStringBuilder ret = new SpannableStringBuilder();
        int[] stats = new int[] {
                0, 0, 0, 0
        }; // total, folders, files, hidden
        long bytes = 0;
        try {
            if (mData2 != null)
                for (OpenPath p : mData2)
                {
                    stats[0]++;
                    if (!mShowHiddenFiles && p.isHidden())
                        stats[3]++;
                    else if (p.isDirectory())
                        stats[1]++;
                    else {
                        stats[2]++;
                        bytes += p.length();
                    }
                }
        } catch (Exception e) {
        }
        try {
            if (mFinalItems != null)
                for (OpenPath p : mFinalItems)
                {
                    stats[0]++;
                    if (!mShowHiddenFiles && p.isHidden())
                        stats[3]++;
                    else if (p.isDirectory())
                        stats[1]++;
                    else {
                        stats[2]++;
                        bytes += p.length();
                    }
                }
        } catch (Exception e) {
        }
        if (stats[0] == 0)
            return "";
        else {
            if (stats[1] > 0)
                ret.append(stats[1] + " " + getResources().getString(R.string.s_folders));
            if (stats[2] > 0)
                ret.append((ret.length() > 0 ? ", " : "") + stats[2] + " "
                        + getResources().getString(R.string.s_files));
            if (stats[3] > 0)
                ret.append((ret.length() > 0 ? ", " : "") + stats[3] + " "
                        + getResources().getString(R.string.s_hidden));
            if (bytes > 0)
                ret.append((ret.length() > 0 ? ", " : "") + OpenPath.formatSize(bytes, 2, true));

        }
        return ret;
    }
}

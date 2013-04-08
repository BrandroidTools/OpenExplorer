
package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.ServerSetupActivity;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.interfaces.OpenApp.OnBookMarkChangeListener;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenCommand;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenCursor.UpdateBookmarkTextListener;
import org.brandroid.openmanager.data.OpenPath.OpenPathSizable;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.SpaceHandler;
import org.brandroid.openmanager.data.OpenPathMerged;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.util.DFInfo;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.InputDialog;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.SimpleUserInfo;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import com.stericson.RootTools.Mount;
import com.stericson.RootTools.RootTools;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OpenBookmarks implements OnGroupClickListener,
        OnBookMarkChangeListener {
    private final List<OpenPath> mBMDrives = new Vector<OpenPath>();
    private final List<OpenPath> mBMSmarts = new Vector<OpenPath>();
    private final List<OpenPath> mBMFavs = new Vector<OpenPath>();
    private final List<OpenPath> mBMServers = new Vector<OpenPath>();
    private final List<OpenPath> mBMHistory = new Vector<OpenPath>();
    // private static List<OpenPath>[] mBookmarksArray;
    // private ImageView mLastIndicater = null;
    private View mBaseView;
    private BookmarkAdapter mBookmarkAdapter;
    private OnBookmarkSelectListener mChangePathListener;
    private final Resources mResources;
    private final Preferences mPreferences;
    private String mBookmarkString;
    private Boolean mHasExternal = false, mHasInternal = false;
    private Boolean mShowTitles = true;
    private Long mAllDataSize = 0l;
    private Long mLargestDataSize = 0l;
    private SharedPreferences mPrefs;
    private static List<String> mBlkids = null;
    private static List<String> mProcMounts = null;
    private static List<String> mDFs = null;
    public static final int BOOKMARK_DRIVE = 0;
    public static final int BOOKMARK_SMART_FOLDER = 1;
    public static final int BOOKMARK_FAVORITE = 2;
    public static final int BOOKMARK_SERVER = 3;
    public static final int BOOKMARK_EDITING = 4;
    private Boolean mFirstRun = true;

    public interface NotifyAdapterCallback {
        public void notifyAdapter();
    }

    public interface OnBookmarkSelectListener {
        public void onBookmarkSelect(OpenPath path);
    }

    public void setOnBookmarkSelectListener(OnBookmarkSelectListener listener) {
        mChangePathListener = listener;
    }

    public OpenBookmarks(OpenApp app, View view) {
        mBookmarkAdapter = new BookmarkAdapter();
        mResources = app.getResources();
        mPreferences = app.getPreferences();
        OpenExplorer.setOnBookMarkAddListener(this);
        mBaseView = view;
        // mApp = app;
        // for(BookmarkType type : BookmarkType.values())
        // mBookmarksArray.put(getTypeInteger(type), new ArrayList<OpenPath>());
        mPrefs = new Preferences(app.getContext()).getPreferences("bookmarks");
        if (mBookmarkString == null)
            mBookmarkString = mPrefs.getString("bookmarks", "");
        if (view != null && view instanceof ExpandableListView)
            setupListView((ExpandableListView)view);
        if (app != null)
            scanBookmarks(app);
    }

    public void scanRoot() {
        // Logger.LogDebug("Trying to get roots");
        if (mBlkids == null && mProcMounts == null) {
            mProcMounts = new ArrayList<String>();
            mBlkids = new ArrayList<String>();
            mDFs = new ArrayList<String>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Preferences.Pref_Root && RootTools.isAccessRequested()
                                && RootTools.isAccessGiven()) {
                            mBlkids = RootTools.sendShell("blkid", 1000);
                            mProcMounts = RootTools.sendShell("cat /proc/mounts", 1000);
                            mDFs = RootTools.sendShell("df", 1000);
                        } else
                            for (Mount m : RootTools.getMounts())
                                mProcMounts.add(m.toString());
                        Logger.LogVerbose("Successfully got " + mProcMounts.size()
                                + " procmounts and " + mBlkids.size() + " blkids!");
                    } catch (Exception e) {
                        Logger.LogError("Unable to get roots from shell", e);
                    }
                }
            }).start();
        } else
            Logger.LogWarning("No root, can't get roots");
    }

    public enum BookmarkType {
        BOOKMARK_DRIVE, BOOKMARK_SMART_FOLDER, BOOKMARK_FAVORITE, BOOKMARK_SERVER
        // ,BOOKMARK_OFFLINE
    }

    private int getTypeInteger(BookmarkType type) {
        int ret = -1;
        for (BookmarkType item : BookmarkType.values()) {
            ret++;
            if (type.equals(item))
                break;
        }
        return ret;
    }

    @Override
    public void scanBookmarks(OpenApp app) {
        scanRoot();
        scanBookmarksInner(app);
        /*
         * new Thread(new Runnable() {
         * @Override public void run() { scanBookmarksInner(); } }).start();
         */
    }

    /**
     * 
     */
    private void scanBookmarksInner(final OpenApp mApp) {
        final Context context = mApp.getContext();
        Logger.LogDebug("Scanning bookmarks...");
        final OpenFile storage = new OpenFile(Environment.getExternalStorageDirectory());
        // mBookmarksArray.clear();
        clearBookmarks();

        if (checkAndAdd(BookmarkType.BOOKMARK_DRIVE, OpenFile.getExternalMemoryDrive(false)))
            mHasInternal = true;
        if (checkAndAdd(BookmarkType.BOOKMARK_DRIVE, OpenFile.getInternalMemoryDrive()))
            mHasExternal = true;

        checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getVideoParent());
        checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getPhotoParent());
        checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getMusicParent());

        if (mApp.getPreferences().getSetting(null, "pref_show_downloads", true))
            checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getDownloadParent());

        checkAndAdd(BookmarkType.BOOKMARK_DRIVE, new OpenFile("/").setRoot());
        checkAndAdd(BookmarkType.BOOKMARK_DRIVE, storage.setRoot());

        Runnable drives = new Runnable() {
            @Override
            public void run() {

                checkAndAdd(BookmarkType.BOOKMARK_DRIVE, OpenFile.getUsbDrive());

                Hashtable<String, DFInfo> df = DFInfo.LoadDF(mFirstRun);
                mAllDataSize = 0l;
                for (String sItem : df.keySet()) {
                    if (sItem.toLowerCase().startsWith("/dev"))
                        continue;
                    if (sItem.toLowerCase().indexOf("/system") > -1)
                        continue;
                    if (sItem.toLowerCase().indexOf("vendor") > -1)
                        continue;
                    OpenFile file = new OpenFile(sItem);
                    if (file.isHidden())
                        continue;
                    // Logger.LogInfo("DF: " + )
                    if (file.getTotalSpace() > 0) {
                        mAllDataSize += file.getTotalSpace();
                        mLargestDataSize = Math.max(mLargestDataSize, file.getTotalSpace());
                    }
                    // if(!file.getFile().canWrite()) continue;
                    // if(sItem.toLowerCase().indexOf("asec") > -1)
                    // continue;
                    checkAndAdd(BookmarkType.BOOKMARK_DRIVE, file.setRoot());
                }
                // }

                if (mBookmarkString.length() > 0) {
                    String[] l = mBookmarkString.split(";");

                    for (String s : l)
                        checkAndAdd(BookmarkType.BOOKMARK_FAVORITE, new OpenFile(s));
                }
            }
        };

        if (mFirstRun)
        {
            notifyDataSetChanged(mApp);
            new Thread(drives).start();
        }
        else
            drives.run();

        final OpenServers servers = ServerSetupActivity.LoadDefaultServers(context);
        Runnable cloud = new Runnable() {
            public void run() {
                for (int i = 0; i < servers.size(); i++) {
                    final int ind = i;
                    final OpenServer server = servers.get(i);
                    // Logger.LogDebug("Checking server #" + ind + ": " +
                    // server.toString());
                    OpenNetworkPath onp = server.getOpenPath();
                    onp.setServer(server);
                    SimpleUserInfo info = new SimpleUserInfo();
                    info.setPassword(server.getPassword());
                    onp.setUserInfo(info);
                    onp.setName(server.getName());
                    if (server.getPort() > 0)
                        onp.setPort(server.getPort());
                    addBookmark(BookmarkType.BOOKMARK_SERVER, onp);
                }
                addBookmark(BookmarkType.BOOKMARK_SERVER,
                        new OpenCommand(
                                mApp.getResources().getString(R.string.s_pref_server_add),
                                OpenCommand.COMMAND_ADD_SERVER),
                        null);
            }
        };
        if (mFirstRun)
        {
            new Thread(cloud).start();
            mFirstRun = false;
        } else
            cloud.run();

        notifyDataSetChanged(mApp);
    }

    public void notifyDataSetChanged(final OpenApp app)
    {
        if (mBaseView instanceof ExpandableListView)
        {
            mBookmarkAdapter.notifyDataSetChanged();
            return;
        }
        final Context context = app.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout ret = (LinearLayout)mBaseView;
        ret.setOrientation(LinearLayout.VERTICAL);
        for (int type = 0; type < mBookmarkAdapter.getGroupCount(); type++)
        {
            final int typeid = type;
            View cat = null;
            if (ret.getChildCount() > type * 2)
                cat = ret.getChildAt(type * 2);
            boolean toAdd = cat == null;
            cat = mBookmarkAdapter.getGroupView(type, true, cat, ret);
            if (toAdd)
                ret.addView(cat);

            View kidView = null;
            if (ret.getChildCount() > (type * 2) + 1)
                kidView = (LinearLayout)ret.getChildAt((type * 2) + 1);
            else {
                kidView = new LinearLayout(context);
                ret.addView(kidView);
            }
            final LinearLayout kidContainer = (LinearLayout)kidView;
            final int dp = getResources().getDimensionPixelSize(R.dimen.one_dp);
            kidContainer.setOrientation(LinearLayout.VERTICAL);
            List<OpenPath> list = getListOfType(typeid);
            for (int i = 0; i < list.size(); i++)
            {
                final int kidid = i;
                final OpenPath path = list.get(i);
                View kid = null;
                toAdd = false;
                if (kidContainer.getChildCount() > i * 2)
                    kid = kidContainer.getChildAt(i * 2);
                else {
                    kid = inflater.inflate(R.layout.bookmark_layout, null);
                    kidContainer.addView(kid);
                    kidContainer.addView(makeDivider(context));
                }
                if (kid == null)
                    continue;
                makeBookmarkView(app, kid, path);
            }

            while (list.size() * 2 < kidContainer.getChildCount())
                kidContainer.removeViewAt(kidContainer.getChildCount() - 1);

            cat.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final boolean toShow = !kidContainer.isShown();
                    ViewUtils.setViewsVisible(kidContainer, toShow);
                    mBookmarkAdapter.getGroupView(typeid, toShow, v, (ViewGroup)mBaseView);
                    onGroupClick(null, v, typeid, typeid);
                }
            });
            cat.setBackgroundResource(android.R.drawable.list_selector_background);
        }
    }

    private View makeDivider(Context context)
    {
        View ret = new View(context);
        ret.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2 * getResources()
                .getDimensionPixelSize(R.dimen.one_dp)));
        ret.setBackgroundResource(android.R.drawable.divider_horizontal_dark);
        return ret;
    }

    public void makeBookmarkView(final OpenApp app, final View parent, final OpenPath path)
    {
        ViewUtils.setText(parent, getPathTitle(path), R.id.content_text);
        if (path instanceof OpenCommand)
            ViewUtils.setViewsVisible(parent, false, R.id.content_icon,
                    R.id.bookmark_icon);
        else
            ViewUtils.setImageResource(parent,
                    ThumbnailCreator.getDrawerResourceId(path),
                    R.id.content_icon, R.id.bookmark_icon, android.R.id.icon);
        if (path instanceof OpenSmartFolder || path instanceof OpenPathMerged) {
            ViewUtils.setText(parent, "(" + path.getListLength() + ")", R.id.content_count);
        } else if (path instanceof OpenCursor) {
            final OpenCursor oc = (OpenCursor)path;
            int cnt = oc.getListLength();
            if (cnt > 0)
                ViewUtils.setText(parent, "(" + cnt + ")", R.id.content_count);
            oc.setUpdateBookmarkTextListener(new UpdateBookmarkTextListener() {
                @Override
                public void updateBookmarkCount(final int count) {
                    ViewUtils.setText(parent, count > 0 ? "(" + count + ")" : null,
                            R.id.content_count);
                }
            });
        } else
            ViewUtils.setViewsVisible(parent, false, R.id.content_count);

        if (path instanceof OpenPath.OpenPathSizable && ((OpenPathSizable)path).getTotalSpace() > 0) {
            OpenPathSizable f = (OpenPathSizable)path;
            long size = f.getTotalSpace();
            long used = f.getUsedSpace();
            long last = f.getThirdSpace();
            updateSizeIndicator(path, parent, size, used, last);
        } else if (path instanceof OpenPath.SpaceHandler) {
            // ViewUtils.setViewsVisible(parent, true, R.id.size_layout,
            // R.id.size_bar);
            ((OpenPath.SpaceHandler)path).getSpace(new OpenPath.SpaceListener() {
                public void onException(Exception e) {
                    ViewUtils.setViewsVisible(parent, false, R.id.size_layout, R.id.size_bar);
                }

                public void onSpaceReturned(long space, long used, long third) {
                    if(path instanceof OpenNetworkPath)
                    {
                        OpenServer server = ((OpenNetworkPath)path).getServer();
                        if(server.isDirty())
                            ServerSetupActivity.SaveToDefaultServers(OpenServers.getDefaultServers(), app.getContext());
                    }
                    updateSizeIndicator(path, parent, space, used, third);
                }
            });
        } else {
            ViewUtils.setViewsVisibleNow(parent, false, R.id.size_layout, R.id.size_bar);
        }

        parent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (path != null) {
                    if (path instanceof OpenCommand)
                        handleCommand(app, ((OpenCommand)path).getCommand());
                    else if (mChangePathListener != null)
                        mChangePathListener.onBookmarkSelect(path);
                    else
                        OpenExplorer.changePath(v.getContext(), path);
                }
            }
        });
        parent.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return OpenBookmarks.this.onLongClick(app, v, path);
            }
        });
    }

    public void saveBookmarks() {
        setSetting("bookmarks", mBookmarkString);
    }

    public String getSetting(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }

    public Boolean getSetting(String key, Boolean defValue) {
        return mPrefs.getBoolean(key, defValue);
    }

    public void setSetting(String key, String value) {
        mPrefs.edit().putString(key, value).commit();
    }

    public void setSetting(String key, Boolean value) {
        mPrefs.edit().putBoolean(key, value).commit();
    }

    public static int getBookmarkType(OpenPath path)
    {
        if (path instanceof OpenNetworkPath)
            return BOOKMARK_SERVER;
        if (path instanceof OpenSmartFolder || path instanceof OpenCursor)
            return BOOKMARK_SMART_FOLDER;
        if (path instanceof OpenFile)
            return BOOKMARK_DRIVE;
        return BOOKMARK_FAVORITE;
    }

    public boolean hasBookmark(OpenPath path) {
        if (path == null)
            return true;
        if (path instanceof OpenNetworkPath)
            return false;
        if (path.getPath() == null)
            return false;
        for (int i = 0; i < 5; i++)
            if (getListOfType(i).contains(path))
                return true;
        return false;
    }

    public void addBookmark(BookmarkType type, OpenPath path) {
        int iType = getTypeInteger(type);
        if (!getListOfType(iType).contains(path))
            getListOfType(iType).add(path);
    }

    public List<OpenPath> getListOfType(int type)
    {
        switch (type)
        {
            case 0:
                return mBMDrives;
            case 1:
                return mBMSmarts;
            case 2:
                return mBMFavs;
            case 3:
                return mBMServers;
            case 4:
                return mBMHistory;
        }
        return null;
    }

    public void addBookmark(BookmarkType type, OpenPath path, NotifyAdapterCallback callback) {
        addBookmark(type, path);
        notifyAdapter(callback);
    }

    public void notifyAdapter(NotifyAdapterCallback callback)
    {
        if (callback == null)
            return;
        if (Thread.currentThread().equals(OpenExplorer.UiThread))
            callback.notifyAdapter();
        // else OpenExplorer.getHandler().post(new Runnable() {
        // public void run() {
        // // do we want to post if it's on a separate thread?
        // }
        // });
    }

    public void addBookmark(BookmarkType type, OpenPath path, int index,
            NotifyAdapterCallback callback) {
        int iType = getTypeInteger(type);
        if (!getListOfType(iType).contains(path)) {
            getListOfType(iType).add(
                    Math.max(getListOfType(iType).size() - 1, index),
                    path);
            notifyAdapter(callback);
        }
    }

    public void refresh(OpenApp app) {
        notifyDataSetChanged(app);
    }

    private void clearBookmarks() {
        for (int i = 0; i < BookmarkType.values().length; i++)
            getListOfType(i).clear();
    }

    public String getPathTitle(OpenPath path) {
        if (path instanceof OpenNetworkPath)
            return path.getName();
        String ret = getPathTitleDefault(path);
        if (mPrefs.contains("title_" + path.getPath()))
            ret = getSetting("title_" + path.getPath(), ret);
        else if (path.getPath().startsWith("/") && mBlkids != null && mProcMounts != null) {
            Logger.LogDebug("Looking for " + path + " in procmounts");
            for (String m : mProcMounts) {
                String[] parts = m.split("  *");
                if (path.getPath().startsWith(parts[1].toString())) {
                    String dev = parts[0];
                    for (String blk : mBlkids)
                        if (blk.indexOf(dev) > -1 && blk.toLowerCase().indexOf("label=") > -1) {
                            String lbl = blk.substring(blk.toLowerCase().indexOf("label=") + 6);
                            if (lbl.startsWith("\""))
                                lbl = lbl.substring(1, lbl.indexOf("\"", 2));
                            else
                                lbl = lbl.substring(0, lbl.indexOf(" "));
                            lbl = lbl.trim();
                            if (lbl.equals(""))
                                return ret;
                            Logger.LogVerbose("Found Device Label for " + path + " = " + lbl);
                            setPathTitle(path, lbl);
                            return lbl;
                        }
                }
                setPathTitle(path, ret);
            }
        }
        if (path.toString().toLowerCase().indexOf("/usb") > -1) {
            ret = getString(R.string.storage_usb);
            setPathTitle(path, ret);
            return ret;
        }
        return ret;
    }

    public void setPathTitle(OpenPath path, String title) {
        setSetting("title_" + path.getPath(), title);
    }

    public Resources getResources() {
        return mResources;
    }

    public String getString(int resId) {
        return mResources.getString(resId);
    }

    public CharSequence getText(int resId) {
        return mResources.getText(resId);
    }

    public String getPathTitleDefault(OpenPath file) {
        if (file.getDepth() > 4)
            return file.getName();
        if (file instanceof OpenCursor || file instanceof OpenMediaStore
                || file instanceof OpenNetworkPath
                || file instanceof OpenPathMerged)
            return file.getName();
        String path = file.getPath().toLowerCase();
        String name = file.getName().toLowerCase();
        if (OpenExplorer.isNook()) {
            if (path.equals("/mnt/media"))
                return getString(R.string.s_internal);
            else if (name.indexOf("sdcard") > -1)
                return getString(R.string.s_external);
        }
        if (path.equals("/"))
            return "/";
        else if (name.indexOf("ext") > -1 || name.equals("sdcard1"))
            return getString(R.string.s_external);
        else if (Build.VERSION.SDK_INT > 16 && path.equals("/storage/emulated/0")) // 4.2
            return getString(R.string.s_internal);
        else if (name.indexOf("download") > -1)
            return getString(R.string.s_downloads);
        else if (name.indexOf("sdcard") > -1)
            return getString(mHasExternal ? R.string.s_internal : R.string.s_external);
        else if (name.indexOf("usb") > -1 || name.indexOf("/media") > -1
                || name.indexOf("removeable") > -1 || name.indexOf("storage") > -1) {
            try {
                return OpenExplorer.getVolumeName(file.getPath());
            } catch (Exception e) {
                Logger.LogWarning("Unable to get actual volume name.", e);
            }
        }

        return file.getName();
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    private boolean checkPrefs(BookmarkType type, OpenPath path) {
        if (path.getPath().equals("/"))
            return getPreferences().getSetting(null, "pref_show_root", false);
        else if (OpenFile.getInternalMemoryDrive().equals(path))
            return getPreferences().getSetting(null, "pref_show_internal", true);
        else if (OpenFile.getExternalMemoryDrive(true).equals(path))
            return getPreferences().getSetting(null, "pref_show_external", true);
        else if (type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Videos"))
            return getPreferences().getSetting(null, "pref_show_videos", true);
        else if (type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Photos"))
            return getPreferences().getSetting(null, "pref_show_photos", true);
        else if (type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Music"))
            return getPreferences().getSetting(null, "pref_show_music", true);
        else if (type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Downloads"))
            return getPreferences().getSetting(null, "pref_show_downloads", true);
        else
            return !getPreferences().getSetting("bookmarks", "hide_" + path.getPath(), false);
    }

    private boolean checkAndAdd(BookmarkType type, OpenPath path) {
        if (path == null)
            return false;
        boolean bypassHide = false; // mExplorer.getPreferences().getSetting("global",
        // "pref_hide", false);
        try {
            if (path instanceof OpenSmartFolder)
                bypassHide = true;
            if (!bypassHide && !checkPrefs(type, path))
                return false;
        } catch (NullPointerException e) {
        }
        if (hasBookmark(path))
            return false;
        if (path instanceof OpenFile) {
            if (((OpenFile)path).isRemoveable())
                if (((OpenFile)path).getUsableSpace() <= 0)
                    return false;
        }
        if (path instanceof OpenCursor || path instanceof OpenNetworkPath
                || path instanceof OpenSmartFolder || path instanceof OpenPathMerged
                || path.exists()) {
            addBookmark(type, path);
            return true;
        }
        return false;
    }

    public void setupListView(ExpandableListView lv) {
        Logger.LogDebug("Setting up ListView in OpenBookmarks");
        // lv.setDrawSelectorOnTop(true);
        // lv.setSelector(R.drawable.selector_blue);
        // lv.setOnChildClickListener(this);
        // lv.setOnGroupClickListener(this);
        lv.setGroupIndicator(null);
        // lv.setOnItemLongClickListener(this);
        lv.setLongClickable(true);
        // lv.setOnItemClickListener(this);
        // lv.setBackgroundResource(R.drawable.listgradback);

        // Logger.LogDebug(mBookmarks.size() + " bookmarks");

        // registerForContextMenu(lv);

        // mBookmarkAdapter = new BookmarkAdapter(mExplorer,
        // R.layout.bookmark_layout, mBookmarksArray);
        lv.setAdapter(mBookmarkAdapter);

    }

    private void handleCommand(OpenApp app, int command) {
        switch (command) {
            case OpenCommand.COMMAND_ADD_SERVER:
                ServerSetupActivity.showServerDialog(app, null);
                // ServerSetupActivity.showServerDialog(mApp, new
                // OpenFTP((OpenFTP)null, null, null), null, true);
                break;
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        // if(mBookmarksArray.get(groupPosition).size() > 0)
        // return false;
        // else return true; // don't allow expand of empty groups
        return false;
    }

    /*
     * public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long
     * arg3) { mExplorer.onChangeLocation(mBookmarksArray.get(pos)); } public
     * void onListItemClick(ListView list, View view, int pos, long id) {
     * //super.onItemClick(list, view, pos, id);
     * mExplorer.onChangeLocation(mBookmarksArray.get(pos)); }
     */

    @Override
    public void onBookMarkAdd(OpenApp app, OpenPath path) {
        int type = getTypeInteger(BookmarkType.BOOKMARK_FAVORITE);
        List<OpenPath> list = getListOfType(type);
        list.add(path);
        refresh(app);
    }

    public boolean showStandardDialog(final OpenApp app,
            final OpenPath mPath, final BookmarkHolder mHolder) {

        final View v = mHolder != null ? mHolder.getView() : new View(app.getContext());

        return showStandardDialog(app, mPath, v);

    }

    public boolean showStandardDialog(final OpenApp app, final OpenPath mPath, final View v)
    {
        final Context context = app.getContext();
        int removeId = R.string.s_remove;
        if (BookmarkHolder.isEjectable(mPath))
            removeId = R.string.s_eject;
        else if (mPath.getPath().equals("/")
                || mPath.equals(OpenFile.getExternalMemoryDrive(false))
                || mPath.equals(OpenFile.getInternalMemoryDrive()))
            removeId = R.string.s_hide;
        else if (mPath instanceof OpenMediaStore || mPath instanceof OpenCursor
                || mPath instanceof OpenPathMerged)
            removeId = R.string.s_hide;
        final int idRemove = removeId;

        final String oldPath = mPath.getPath();

        final InputDialog builder = new InputDialog(context)
                .setTitle(R.string.s_title_bookmark_prefix)
                .setIcon(ThumbnailCreator.getDefaultDrawable(mPath, 64, 64, context))
                .setDefaultText(getPathTitle(mPath))
                .setDefaultTop(removeId == R.string.s_hide ? null : oldPath)
                .setMessage(R.string.s_alert_bookmark_rename)
                .setNeutralButton(removeId, new DialogInterface.OnClickListener() {
                    @Override
                    @SuppressLint("NewApi")
                    public void onClick(DialogInterface dialog, int which) {
                        if (mPath.getPath().equals("/"))
                            getPreferences().setSetting("global", "pref_show_root", false);
                        else if (mPath.equals(OpenFile.getInternalMemoryDrive()))
                            getPreferences().setSetting("global", "pref_show_internal", false);
                        else if (mPath.equals(OpenFile.getExternalMemoryDrive(true)))
                            getPreferences().setSetting("global", "pref_show_external", false);
                        else if (mPath instanceof OpenMediaStore)
                            getPreferences().setSetting("global",
                                    "pref_show_" + mPath.getPath().toLowerCase(), false);
                        else if (idRemove == R.string.s_eject)
                            tryEject(app, v, mPath.getPath());
                        else {
                            setSetting("hide_" + mPath.getPath(), true);
                            if (mBookmarkString != null
                                    && (";" + mBookmarkString + ";").indexOf(mPath.getPath()) > -1)
                                mBookmarkString = (";" + mBookmarkString + ";").replace(
                                        ";" + mPath.getPath() + ";", ";").replaceAll("^;|;$", "");
                            v.post(new Runnable() {
                                public void run() {
                                    v.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }).setNegativeButton(R.string.s_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setPositiveButton(R.string.s_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String path = builder.getInputTopText();
                if (!path.equals(oldPath)) {
                    SharedPreferences sp = context.getSharedPreferences("bookmarks",
                            Context.MODE_PRIVATE);
                    // sp.edit().putStringSet("bookmarks", mBMFavs);
                    String full = sp.getString("bookmarks", "") + ";";
                    full = full.replace(oldPath + ";", path + ";");
                    sp.edit().putString("bookmarks", full).commit();
                    setPathTitle(FileManager.getOpenCache(path), builder.getInputText());
                } else
                    setPathTitle(mPath, builder.getInputText().toString());
                notifyDataSetChanged(app);
            }
        }).create();
        try {
            builder.show();
        } catch (BadTokenException e) {
            Logger.LogWarning("Couldn't show AlertDialog. Bad token?", e);
        }
        return true;
    }

    @SuppressLint("NewApi")
    protected void tryEject(final OpenApp app, final View viewf, String sPath) {
        final Context context = viewf.getContext();
        if (RootManager.tryExecute("umount " + sPath)) {
            Toast.makeText(context, R.string.s_alert_remove_safe, Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= 12)
                viewf.animate().setDuration(500).y(viewf.getY() - viewf.getHeight()).alpha(0)
                        .setListener(new org.brandroid.openmanager.adapters.AnimatorEndListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                refresh(app);
                            }
                        });
        } else {
            Toast.makeText(context, R.string.s_alert_remove_error, Toast.LENGTH_LONG).show();
        }
    }

    public String getBookMarkNameString() {
        return mBookmarkString;
    }

    /**
     * Update Size ProgressBar and TextViews
     * 
     * @param mParentView
     * @param total Total Size to display
     * @param used Used Size to display
     * @param third Optional 3rd Size to display
     */
    public void updateSizeIndicator(OpenPath path, View mParentView, long total, long used,
            long third)
    {
        boolean onlySize = used < 0;

        if (OpenExplorer.IS_DEBUG_BUILD)
            Logger.LogDebug("Update Size indicator for " + path + ": " + total + ", " + used + ", "
                    + third);

        if (total > 0) {
            String txt = "";
            if (used > 0)
            {
                boolean same = OpenPath.getSizeSuffix(used).equals(OpenPath.getSizeSuffix(total));
                txt = OpenPath.formatSize(used, !same) + "/";
            }
            txt += OpenPath.formatSize(total);
            if (third > 0)
                txt += " (" + OpenPath.formatSize(third) + ")";
            ViewUtils.setText(mParentView, txt, R.id.size_text);

            while (total > 100000) {
                total /= 10;
                used /= 10;
                third /= 10;
            }
            float total_percent = ((float)total / (float)Math.max(total, mLargestDataSize));
            total_percent = Math.min(20, total_percent);

            ProgressBar bar = (ProgressBar)mParentView.findViewById(R.id.size_bar);
            ViewUtils.setViewsVisible(bar, !onlySize, R.id.size_bar);

            if (bar != null && !onlySize) {
                bar.setMax((int)total);
                bar.setProgress((int)used);
                if (third > 0)
                    bar.setSecondaryProgress((int)third);
            }

            ViewUtils.setViewsVisible(mParentView, true, R.id.size_bar, R.id.size_layout,
                    R.id.size_text);
        } else
            ViewUtils.setViewsVisible(mParentView, false, R.id.size_bar, R.id.size_layout,
                    R.id.size_text);
    }

    private class BookmarkAdapter extends BaseExpandableListAdapter implements Runnable {
        @Override
        public OpenPath getChild(int group, int pos) {
            return getListOfType(group).get(pos);
        }

        @Override
        public long getChildId(int group, int pos) {
            return pos;
        }

        @Override
        public void run() {
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            if (Thread.currentThread().equals(OpenExplorer.UiThread))
                super.notifyDataSetChanged();
            else
                OpenExplorer.getHandler().post(this);
        }

        @Override
        public View getChildView(int group, int pos, boolean isLastChild, View convertView,
                ViewGroup parent) {
            final View row = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.bookmark_layout,
                    null);

            makeBookmarkView((OpenApp)this, row, getChild(group, pos));

            return row;
        }

        @Override
        public int getChildrenCount(int group) {
            List<OpenPath> list = getGroup(group);
            if (list == null)
                return 0;
            return list.size();
        }

        @Override
        public List<OpenPath> getGroup(int group) {
            return getListOfType(group);
        }

        @Override
        public int getGroupCount() {
            return getResources().getStringArray(R.array.bookmark_groups).length;
        }

        @Override
        public long getGroupId(int group) {
            return group;
        }

        @Override
        public View getGroupView(int group, boolean isExpanded, final View convertView,
                ViewGroup parent) {
            View ret = convertView;
            if (ret == null) {
                ret = ((LayoutInflater)parent.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bookmark_group, null);
            }

            TextView mText = (TextView)ret.findViewById(android.R.id.title);
            if (isExpanded) {
                mText.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mText.setTypeface(Typeface.DEFAULT);
            }

            String[] groups = getResources().getStringArray(R.array.bookmark_groups);
            if (group >= groups.length)
                return null;
            if (mText != null)
            {
                int kc = getChildrenCount(group);
                if (group == BOOKMARK_SERVER)
                    kc--;
                mText.setText(groups[group] + (kc > 0 ? " (" + kc + ")" : ""));
            }
            return ret;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int group, int pos) {
            return true;
        }

    }

    public boolean onLongClick(OpenApp app, View v, OpenPath path)
    {
        final Context c = app.getContext();
        Logger.LogInfo("BookMark.onLongClick(" + path + ")");
        if (path instanceof OpenCommand)
            handleCommand(app, ((OpenCommand)path).getCommand());
        else if (path instanceof OpenNetworkPath)
            ServerSetupActivity.showServerDialog(app, (OpenNetworkPath)path);
        else
            showStandardDialog(app, path, v);
        return true;
    }

}

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

package org.brandroid.openmanager.fragments;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenApplication;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.ServerSetupActivity;
import org.brandroid.openmanager.adapters.ContentAdapter;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.adapters.OpenPathAdapter;
import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenDrive;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenFileRoot;
import org.brandroid.openmanager.data.OpenLZMA;
import org.brandroid.openmanager.data.OpenLZMA.OpenLZMAEntry;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenNetworkPath.Cancellable;
import org.brandroid.openmanager.data.OpenNetworkPath.CloudOpsHandler;
import org.brandroid.openmanager.data.OpenNetworkPath.CloudProgressListener;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.ListHandler;
import org.brandroid.openmanager.data.OpenPath.OpenContentUpdateListener;
import org.brandroid.openmanager.data.OpenPath.OpenPathUpdateHandler;
import org.brandroid.openmanager.data.OpenPath.SpaceListener;
import org.brandroid.openmanager.data.OpenPath.ThumbnailOverlayInterface;
import org.brandroid.openmanager.data.OpenPathArray;
import org.brandroid.openmanager.data.OpenPathMerged;
import org.brandroid.openmanager.data.OpenRAR;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.data.OpenTar;
import org.brandroid.openmanager.data.OpenTar.OpenTarEntry;
import org.brandroid.openmanager.data.OpenZip;
import org.brandroid.openmanager.interfaces.OnAuthTokenListener;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.EventHandler.CompressionType;
import org.brandroid.openmanager.util.EventHandler.EventType;
import org.brandroid.openmanager.util.EventHandler.OnWorkerUpdateListener;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.InputDialog;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.NetworkIOTask;
import org.brandroid.openmanager.util.NetworkIOTask.OnTaskUpdateListener;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;
import org.kamranzafar.jtar.TarUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.MessageQueue;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

@SuppressLint("NewApi")
public class ContentFragment extends OpenFragment implements OnItemLongClickListener,
        OnItemClickListener, OnWorkerUpdateListener, OpenPathFragmentInterface,
        OnTaskUpdateListener, ContentAdapter.SelectionCallback {

    // private static MultiSelectHandler mMultiSelect;
    // private LinearLayout mPathView;
    // private SlidingDrawer mMultiSelectDrawer;
    // private GridView mMultiSelectView;
    protected GridView mGrid = null;
    private TextView mStatus = null;
    private View mStatusBar = null;
    // private View mProgressBarLoading = null;

    // private ArrayList<OpenPath> mData2 = null; //the data that is bound to
    // our array adapter.
    private int mListScrollingState = 0;
    private int mListVisibleStartIndex = 0;
    private int mListVisibleLength = 0;
    private int mListScrollY = 0;
    public static int mGridImageSize = 128;
    public static int mListImageSize = 36;
    public Boolean mShowLongDate = false;
    private boolean isCancelled = false;
    private int mTopIndex = 0;
    private OpenPath mTopPath = null;
    protected OpenPath mPath = null;
    protected int mMenuContextItemIndex = -1;
    private boolean mRefreshReady = true;
    private static String mLoadingMessage = null;
    public static final SortType.Type[] sortTypes = new SortType.Type[] {
            SortType.Type.ALPHA, SortType.Type.ALPHA_DESC, SortType.Type.SIZE,
            SortType.Type.SIZE_DESC, SortType.Type.DATE, SortType.Type.DATE_DESC,
            SortType.Type.TYPE
    };
    public static final int[] sortMenuOpts = new int[] {
            R.id.menu_sort_name_asc, R.id.menu_sort_name_desc, R.id.menu_sort_size_asc,
            R.id.menu_sort_size_desc, R.id.menu_sort_date_asc, R.id.menu_sort_date_desc,
            R.id.menu_sort_type
    };

    private Bundle mBundle;
    private NetworkIOTask mTask;

    protected boolean mIsViewCreated;

    protected Integer mViewMode = null;
    protected ContentAdapter mContentAdapter;
    private OnCreateContextMenuListener mConvListOnCreateContextMenuListener;

    /**
     * If true, we disable the CAB even if there are selected messages. It's
     * used in portrait on the tablet when the message view becomes visible and
     * the message list gets pushed out of the screen, in which case we want to
     * keep the selection but the CAB should be gone.
     */
    private boolean mDisableCab;

    /**
     * {@link ActionMode} shown when 1 or more message is selected.
     */
    private SelectionModeCallback mLastSelectionModeCallback;

    // private static Hashtable<OpenPath, ContentFragment> instances = new
    // Hashtable<OpenPath, ContentFragment>();

    public ContentFragment() {
        if (getArguments() != null && getArguments().containsKey("path")) {
            mPath = (OpenPath)getArguments().getParcelable("path");
            // Logger.LogDebug("ContentFragment Restoring to " + mPath);
        }
    }

    // private ContentFragment(OpenPath path) {
    // mPath = path;
    // }
    //
    // private ContentFragment(OpenPath path, int view) {
    // mPath = path;
    // mViewMode = view;
    // }

    public static ContentFragment getInstance(OpenPath path, int mode) {
        return getInstance(path, mode, null);
    }

    public static ContentFragment getInstance(OpenPath path, int mode, FragmentManager fm) {
        ContentFragment ret = null;
        if (fm != null)
            try {
                ret = (ContentFragment)fm.findFragmentByTag(path.getPath());
            } catch (NullPointerException e) {
            }
        if (ret == null)
            ret = new ContentFragment();
        // if(path instanceof OpenFile) return ret;
        Bundle args = ret.getArguments();
        if (args == null)
            args = new Bundle();
        if (path != null) {
            // Logger.LogDebug("ContentFragment.getInstance(" + path +
            // ", mode, " + fm + ")");
            args.putParcelable("path", path);
            ret.setArguments(args);
        } else
            return null;
        // Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() +
        // ", " + mode + ")");
        return ret;
    }

    public static ContentFragment getInstance(OpenPath path) {
        return getInstance(path, new Bundle());
    }

    public static ContentFragment getInstance(OpenPath path, Bundle args) {
        ContentFragment ret = new ContentFragment();
        if (args == null)
            args = new Bundle();
        args.putParcelable("path", path);
        ret.setArguments(args);
        // Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() +
        // ")");
        return ret;
    }

    public static ContentFragment getInstance(Bundle args) {
        ContentFragment ret = new ContentFragment();
        ret.setArguments(args);
        return ret;
    }

    public static void cancelAllTasks() {
        NetworkIOTask.cancelAllTasks();
    }

    protected ContentAdapter getContentAdapter() {
        if (mContentAdapter == null) {
            mContentAdapter = new ContentAdapter(getExplorer(), this, getViewMode(), mPath);
            mContentAdapter.setShowHiddenFiles(getViewSetting(getPath(), "show",
                    getViewSetting(null, "pref_show", false)));
            SortType sort = new SortType(getViewSetting(getPath(), "sort",
                    getViewSetting(null, "pref_sorting", SortType.ALPHA.toString())));
            if (getViewSetting(getPath(), "ff", (Boolean)null) != null)
                sort.setFoldersFirst(getViewSetting(getPath(), "ff", true));
            else
                sort.setFoldersFirst(getSetting(null, "pref_sorting_folders", true));
            try {
                mContentAdapter.setSorting(sort);
            } catch(Exception e) {
                Logger.LogWarning("Unable to set sorting!", e);
            }
        }
        return mContentAdapter;
    }

    public int getViewMode() {
        if (mViewMode == null)
            mViewMode = getViewSetting(mPath, "view", getGlobalViewMode());
        return mViewMode;
    }

    public int getGlobalViewMode() {
        if (mPath instanceof OpenPathMerged)
            return OpenExplorer.VIEW_GRID;
        String pref = getSetting(null, "pref_view", "list");
        if (pref.equals("list"))
            return OpenExplorer.VIEW_LIST;
        if (pref.equals("grid"))
            return OpenExplorer.VIEW_GRID;
        return OpenExplorer.VIEW_LIST;
    }

    // @Override
    public void setListAdapter(ListAdapter adapter) {
        // super.setListAdapter(adapter);
        /*
         * ListView lv = getListView(); if(getViewMode() ==
         * OpenExplorer.VIEW_GRID) { if(lv != null && lv.isShown())
         * lv.setVisibility(View.GONE); if(mGrid == null) mGrid =
         * (GridView)getView().findViewById(R.id.content_grid);
         * mGrid.setVisibility(View.VISIBLE); mGrid.setAdapter(adapter);
         * updateGridView(); } else { if(mGrid != null && mGrid.isShown())
         * mGrid.setVisibility(View.GONE); lv.setVisibility(View.VISIBLE);
         * lv.setAdapter(adapter); }
         */
        if (mGrid.getAdapter() == null || !mGrid.getAdapter().equals(adapter))
            mGrid.setAdapter(adapter);

        mGrid.setColumnWidth(getResources().getDimensionPixelSize(
                    getViewMode() == OpenExplorer.VIEW_GRID ? R.dimen.grid_width : R.dimen.list_width
                ));
        if (adapter != null && adapter.equals(mContentAdapter)) {
            // Logger.LogDebug("ContentFragment.setListAdapter updateData()");
            mContentAdapter.updateData();
            if (mPath != null && mContentAdapter.getCount() == 0 && !isDetached()) {
                ViewUtils.setText(getView(), getResources().getString(
                        !mPath.isLoaded() ? R.string.s_status_loading : R.string.no_items),
                        android.R.id.empty);
                ViewUtils.setViewsVisible(getView(), true, android.R.id.empty);
            } else
                ViewUtils.setViewsVisible(getView(), false, android.R.id.empty);
            updateStatus();
        }
    }

    // @Override
    public ListAdapter getListAdapter() {
        return getContentAdapter();
    }

    private void setViewMode(int mode) {
        mViewMode = mode;
        setViewSetting(mPath, "view", mode);
        Logger.LogVerbose("Content View Mode: " + mode);
        setListAdapter(null);
        mContentAdapter = null;
        setListAdapter(getContentAdapter());
        mContentAdapter.setViewMode(mode);
        setListAdapter(mContentAdapter);
        refreshData(null, false);
    }

    // @Override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGridImageSize = (int)(OpenExplorer.DP_RATIO * OpenExplorer.IMAGE_SIZE_GRID);
        mListImageSize = (int)(OpenExplorer.DP_RATIO * OpenExplorer.IMAGE_SIZE_LIST);
        if (savedInstanceState != null)
            mBundle = savedInstanceState;
        if (getArguments() != null && getArguments().containsKey("path"))
            mBundle = getArguments();
        if (mBundle != null && mBundle.containsKey("path"))
            mPath = (OpenPath)mBundle.getParcelable("path");

        if (mBundle != null && mBundle.containsKey("view"))
            mViewMode = mBundle.getInt("view");
        else
            mViewMode = getSetting(mPath, "view", getGlobalViewMode());
        
        if(mLoadingMessage == null)
            mLoadingMessage = getString(R.string.s_status_loading);

        // if (mPath == null)
        // Logger.LogDebug("Creating empty ContentFragment", new Exception(
        // "Creating empty ContentFragment"));
        // else
        // Logger.LogDebug("Creating ContentFragment @ " + mPath);

        // OpenExplorer.getEventHandler().setOnWorkerThreadFinishedListener(this);

    }
    /**
     * The Fragment's UI is just a list fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_layout, container, false);
        mGrid = (GridView)v.findViewById(R.id.content_grid);
        mStatus = (TextView)v.findViewById(R.id.content_status);
        mStatusBar = v.findViewById(R.id.content_status_bar);
        mIsViewCreated = true;
        return v;
    }

    /**
     * Called when the activity's onCreate() method has returned.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getExplorer().setViewPagerLocked(false);

        // final ListView lv = getListView();
        // lv.setOnItemLongClickListener(this);
        // lv.setOnItemClickListener(this);
        // lv.setItemsCanFocus(false);
        // lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mGrid.setOnItemClickListener(this);
        mGrid.setOnItemLongClickListener(this);
        // mGrid.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        initListAdapter();

        // if (savedInstanceState != null) {
        // // Fragment doesn't have this method. Call it manually.
        // restoreInstanceState(savedInstanceState);
        // }
    }

    private void initListAdapter() {
        setListAdapter(getContentAdapter());
    }

    /*
     * public synchronized void notifyDataSetChanged() { if(mListAdapter ==
     * null) { mContentAdapter = new ContentAdapter(getActivity(), this,
     * mViewMode, mPath); if(mGrid != null) mGrid.setAdapter(mContentAdapter); }
     * //if(!Thread.currentThread().equals(OpenExplorer.UiThread)) //
     * getActivity().runOnUiThread(new Runnable(){public void
     * run(){mContentAdapter.updateData();}}); //else
     * mContentAdapter.updateData(); }
     */
    public synchronized void refreshData() {
        refreshData(getArguments(), false);
    }

    public synchronized void refreshData(Bundle savedInstanceState, boolean allowSkips) {
        if (!mRefreshReady) {
            Logger.LogWarning("ContentFragment.refreshData warning: Not ready!");
            return;
        }
        if (!isVisible()) {
            // Logger.LogDebug("I'm invisible! " + mPath);
            // return;
        }
        if (isDetached()) {
            // Logger.LogDebug("I'm detached! " + mPath);
            return;
        }

        if (getContext() == null) {
            Logger.LogError("RefreshData out of context");
            return;
        }

        if (savedInstanceState == null && getArguments() != null)
            savedInstanceState = getArguments();

        OpenPath path = mPath;
        if (path == null)
            if (savedInstanceState != null && savedInstanceState.containsKey("path"))
                path = (OpenFile)savedInstanceState.getParcelable("path");

        if (path == null) {
            Logger.LogWarning("ContentFragment.refreshData warning: path is null!");
            return;
        }

        try {
            if (path instanceof OpenFile && !path.canRead()
                    && OpenApplication.hasRootAccess(true))
                path = new OpenFileRoot(path);
        } catch (Exception e) {
            Logger.LogWarning("Unable to convert File to Root. " + getPath(), e);
        }

        // if (DEBUG)
        // Logger.LogDebug("refreshData running...");

        mRefreshReady = false;

        mContentAdapter = null;

        final String sPath = path.getPath();

        if (path instanceof OpenFile && !sPath.startsWith("/")) {
            if (sPath.equals("Photos"))
                path = OpenExplorer.getPhotoParent();
            if (sPath.equals("Videos"))
                path = OpenExplorer.getVideoParent();
            if (sPath.equals("Music"))
                path = OpenExplorer.getMusicParent();
            if (sPath.equals("Downloads"))
                path = OpenExplorer.getDownloadParent();
        }

        /*
         * if (path instanceof OpenFile &&
         * (((path.getName().equalsIgnoreCase("data") || sPath.indexOf("/data")
         * > -1) && !sPath
         * .startsWith(OpenFile.getExternalMemoryDrive(true).getParent
         * ().getPath())) || sPath.startsWith("/mnt/shell") ||
         * (sPath.indexOf("/emulated/") > -1 && sPath.indexOf("/emulated/0") ==
         * -1) || sPath .startsWith("/system"))) path = new OpenFileRoot(path);
         */

        mPath = path;

        getContentAdapter();

        // if (DEBUG)
        // Logger.LogDebug("Refreshing Data for " + mPath);

        SortType sort = SortType.ALPHA;
        if (getExplorer() != null) {
            String ds = getExplorer().getSetting(null, "pref_sorting", SortType.ALPHA.toString());
            // Logger.LogVerbose("Default Sort String: " + ds);

            SortType defSort = new SortType(ds);
            defSort.setFoldersFirst(getExplorer().getSetting(null, "pref_sorting_folders", true));

            // Logger.LogVerbose("Default Sort: " + defSort.toString());

            sort = new SortType(getViewSetting(path, "sort", defSort.toString()));

            // Logger.LogVerbose("Path Sort: " + sort.toString());
        }
        try {
            mContentAdapter.mShowThumbnails = getViewSetting(path, "thumbs",
                    getExplorer() != null ? getExplorer().getSetting(null, "pref_thumbs", true)
                            : true);
        } catch (NullPointerException npe) {
            Logger.LogWarning("Null while getting prefs", npe);
        }

        mContentAdapter.setSorting(sort);

        // Logger.LogVerbose("View options for " + sPath + " : " +
        // (mShowHiddenFiles ? "show" : "hide") + " + " + (mShowThumbnails ?
        // "thumbs" : "icons") + " + " + mSorting.toString());

        // if(path.getClass().equals(OpenCursor.class) &&
        // !OpenExplorer.BEFORE_HONEYCOMB)
        // mShowThumbnails = true;

        if (getActivity() != null && getActivity().getWindow() != null)
            mShowLongDate = getResources().getBoolean(R.bool.show_long_date)
                    && mPath != null;

        if (path.listFromDb(mContentAdapter.getSorting())) {
            int loaded = mContentAdapter.getCount();
            if (path instanceof OpenNetworkPath) {
                OpenNetworkPath[] kids = ((OpenNetworkPath)path).getChildren();
                mContentAdapter.updateData(kids);
                loaded = kids.length;
            }
            Logger.LogDebug("Loaded " + loaded + " entries from cache");
        }
        
        runUpdateTask(!allowSkips);

        mRefreshReady = true;

        // OpenExplorer.setOnSettingsChangeListener(this);

        // new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL,
        // path));

        // if(mGrid != null && savedInstanceState.containsKey("first"))

    }

    public void runUpdateTask() {
        runUpdateTask(false);
    }

    public void runUpdateTask(boolean reconnect) {
        if (mPath == null) {
            Logger.LogWarning("ContentFragment.runUpdateTask warning: mPath is null!");
            return;
        }
        if (mPath instanceof OpenPathUpdateHandler) {
            mContentAdapter.clearData();
            setProgressVisibility(true);

            final OpenContentUpdateListener updateCallback = new OpenContentUpdateListener() {
                @Override
                public void addContentPath(final OpenPath... files) {
                    if(OpenExplorer.IS_DEBUG_BUILD)
                    	Logger.LogVerbose("ContentFragment.OpenContentUpdateListener.addContentPath");
                    mContentAdapter.addAll(Arrays.asList(files));
                    if(OpenPath.AllowDBCache)
                    {
                        new Thread(new Runnable() {
                            public void run() {
                                OpenPathDbAdapter db = OpenPath.getDb();
                                if (db != null)
                                    db.createItem(files);
                                else
                                    for (OpenPath kid : files)
                                        if (kid != null)
                                            kid.addToDb();
                                OpenPath.closeDb();
                            }
                        }).start();
                    }
                }

                @Override
                public void doneUpdating() {
                    if(OpenExplorer.IS_DEBUG_BUILD)
                    	Logger.LogVerbose("ContentFragment.OpenContentUpdateListener.doneUpdating");
                    getHandler().post(new Runnable() {
                        public void run() {
                            setProgressVisibility(false);
                            notifyDataSetChanged();
                            ViewUtils.setViewsVisible(getView(), false, android.R.id.empty);
                        }
                    });
                }

                @Override
                public void onException(final Exception e) {
                    setProgressVisibility(false);
                    if (e instanceof UserRecoverableAuthIOException)
                    {
                        UserRecoverableAuthIOException ue = (UserRecoverableAuthIOException)e;
                        Intent intent = ue.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return;
                    } else if (interceptOldToken(e))
                        return;
                    Logger.LogError("Unable to run Task!", e);
                    getHandler().post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            final Cancellable mUpdateTask = ((OpenPathUpdateHandler)mPath).list(updateCallback);

            final Cancellable cancellor = new Cancellable() {
                public boolean cancel() {
                    updateCallback.doneUpdating();
                    return mUpdateTask.cancel();
                }
            };

            setProgressClickHandler(new View.OnClickListener() {
                public void onClick(View v) {
                    cancellor.cancel();
                }
            });
            
            return;
        } else if (mPath instanceof OpenPath.ListHandler) {
            setProgressVisibility(true);                
            ListHandler lh = (ListHandler)mPath;
            lh.list(new OpenPath.ListListener() {
                public void onException(final Exception e) {
                    if (interceptOldToken(e))
                        return;
                    Logger.LogWarning("Unable to list.", e);
                    try {
                        Toast.makeText(getContext(), "Unable to list. " + e,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e2) {
                    }
                }

                public void onListReceived(final OpenPath[] list) {
                    setProgressVisibility(false);
                    mContentAdapter.updateData(list);
                    notifyDataSetChanged();
                    ViewUtils.setViewsVisible(getView(), false, android.R.id.empty);
                    if(OpenPath.AllowDBCache)
                    {
                        new Thread(new Runnable() {
                            public void run() {
                                OpenPathDbAdapter db = OpenPath.getDb();
                                if (db != null)
                                    db.createItem(list);
                                else
                                    for (OpenPath kid : list)
                                        if (kid != null)
                                            kid.addToDb();
                                OpenPath.closeDb();
                            }
                        }).start();
                    }
                }
            });
            return;
        } else if (mPath instanceof OpenFile) {
            mContentAdapter.updateData(((OpenFile)mPath).listFiles());
            notifyDataSetChanged();
            return;
        }
        final String sPath = mPath.getPath();
        // NetworkIOTask.cancelTask(sPath);
        if (mTask != null)
            mTask.cancel(true);
        mTask = new NetworkIOTask(this);
        setProgressVisibility(true);
        /*
         * if(reconnect && (mPath instanceof OpenNetworkPath))
         * ((OpenNetworkPath)mPath).disconnect();
         */
        Logger.LogDebug("Running Task for " + sPath);
        NetworkIOTask.addTask(sPath, mTask);
        EventHandler.executeNetwork(mTask, mPath);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                }
                if (mTask.getStatus() == Status.RUNNING)
                    mTask.doCancel(false);
            }
        }).start();
    }
    
    private boolean has401occurred = false;

    private boolean interceptOldToken(Exception e)
    {
        if (!(mPath instanceof OpenDrive))
            return false;
        if (!(e instanceof GoogleJsonResponseException))
            return false;
        GoogleJsonResponseException re = (GoogleJsonResponseException)e;
        if (re.getStatusCode() != 401)
            return false;
        if(has401occurred) return false;
        if(((OpenDrive)mPath).getServer() == null) return false;
        has401occurred = true;
        final OpenDrive drive = (OpenDrive)mPath;
        final GoogleCredential cred = drive.getCredential();
        final String refresh = drive.getServer().get("refresh"); 
        if(DEBUG)
            Logger.LogDebug("Refreshing drive token with [" + refresh + "]");
        return ServerSetupActivity.interceptOldToken(e, cred.getAccessToken(), refresh,
                drive.getServer().getUser(), getExplorer(), new OnAuthTokenListener() {

            @Override
            public void onException(Exception e) {
                Logger.LogWarning("Unable to intercept bad token.", e);
            }

            @Override
            public void onDriveAuthTokenReceived(String account, String token) {
                if(token.equals("")) {
                    Logger.LogWarning("Refresh token empty! :(");
                    return;
                }
                Toast.makeText(getContext(), "Token for " + account + " refreshed! " + token,
                        Toast.LENGTH_SHORT).show();
                cred.setAccessToken(token);
                drive.setCredential(cred);
                drive.getServer().setPassword(token);
                ServerSetupActivity.SaveToDefaultServers(OpenServers.getDefaultServers(),
                        getContext());
                runUpdateTask(true);
            }
        });
    }

    /**
     * @return true if the content view is created and not destroyed yet. (i.e.
     *         between {@link #onCreateView} and {@link #onDestroyView}.
     */
    private boolean isViewCreated() {
        // Note that we don't use "getView() != null". This method is used in
        // updateSelectionMode()
        // to determine if CAB shold be shown. But because it's called from
        // onDestroyView(), at
        // this point the fragment still has views but we want to hide CAB, we
        // can't use getView() here.
        return mIsViewCreated;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    // @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        // super.onListItemClick(list, view, position, id);
        onItemClick(list, view, position, id);
    }

    private void untarAll(final OpenPath tar, final OpenPath dest, final String... includes)
    {
        if (TarUtils.checkUntar(tar.getPath(), dest.getPath(), includes))
            DialogHandler.showConfirmationDialog(getContext(),
                    getResources().getString(R.string.s_msg_file_exists),
                    getResources().getString(R.string.s_title_file_exists),
                    new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null)
                                dialog.dismiss();
                            getEventHandler().extractSet(tar, dest, getContext(), includes);
                        }
                    });
        else
            getEventHandler().extractSet(tar, dest, getContext(), includes);
    }

    private void extractSet(final OpenPath archive, final OpenPath dest, final String... includes)
    {
        getEventHandler().extractSet(archive, dest, getContext(), includes);
    }

    private void browseArchive(OpenPath archive)
    {
        String mime = archive.getMimeType();
        String ext = archive.getExtension();
        if (mime.equals("application/zip"))
            getExplorer().changePath(new OpenZip((OpenFile)archive));
        else if (mime.endsWith("rar"))
            getExplorer().changePath(new OpenRAR((OpenFile)archive));
        else if (mime.contains("7z") || mime.contains("lzma"))
            getExplorer().changePath(new OpenLZMA((OpenFile)archive));
        else
            getExplorer().changePath(new OpenTar((OpenFile)archive));
    }

    private void extractArchive(OpenPath archive)
    {
        if (archive.getMimeType().contains("tar"))
            untarAll(archive, archive.getParent());
        else
        {
            OpenPath into = archive.getParent();
            if (archive.getMimeType().contains("tar"))
                into = into.getChild(archive.getName().replace("." + archive.getExtension(), ""));
            getEventHandler().extractSet(archive, into, getContext());
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> list, final View view, final int position,
            final long id) {
        OpenPath file = (OpenPath)list.getItemAtPosition(position);
        Logger.LogInfo("ContentFragment.onItemClick");

        if (getActionMode() == null)
        {
            if (file instanceof OpenFile
                    && (file.getMimeType().contains("tar")
                            || file.getMimeType().endsWith("rar")
                            || file.getMimeType().endsWith("compressed")
                            || file.getMimeType().contains("bz")
                            || file.getMimeType().contains("gz")
                            || file.getMimeType().endsWith("zip")))
            {
                final OpenPath archive = file;
                final String prefType = file.getMimeType()
                        .replace("application/", "")
                        .replace("x-", "")
                        .replace("-compressed", "");
                DialogHandler.showExtractDialog(
                        getExplorer(),
                        getString(R.string.s_extract) + " " + prefType,
                        archive,
                        "pref_archives_" + prefType,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which)
                                {
                                    case R.string.s_browse:
                                        browseArchive(archive);
                                        break;
                                    case R.string.s_extract:
                                        extractArchive(archive);
                                        break;
                                }
                                if (dialog != null)
                                    dialog.dismiss();
                            }
                        });
                return;
            }
        }

        if (getActionMode() != null) {
            if (mLastSelectionModeCallback != null) {
                toggleSelection(view, file);
            } else {
                // Animation anim = Animation.
                /*
                 * Drawable dIcon =
                 * ((ImageView)view.findViewById(R.id.content_icon
                 * )).getDrawable(); if(dIcon instanceof BitmapDrawable) {
                 * IconAnimationPanel panel = new
                 * IconAnimationPanel(getExplorer())
                 * .setIcon(((BitmapDrawable)dIcon).getBitmap()) .setStart(new
                 * Point(view.getLeft(), view.getRight())) .setEnd(new
                 * Point(getActivity
                 * ().getWindow().getWindowManager().getDefaultDisplay
                 * ().getWidth() / 2,
                 * getActivity().getWindowManager().getDefaultDisplay
                 * ().getHeight())) .setDuration(500);
                 * ((ViewGroup)getView()).addView(panel); }
                 */

                addToMultiSelect(file);
                ((TextView)view.findViewById(R.id.content_text)).setTextAppearance(
                        list.getContext(), R.style.Text_Large_Highlight);
            }
            return;
        }

        if (file instanceof OpenNetworkPath && getActionMode() == null) {
            if (file.isTextFile()) {
                if (file.length() < Preferences.Pref_Text_Max_Size && getExplorer() != null)
                    getExplorer().editFile(file);
                else
                    downloadFile((OpenNetworkPath)file);
                return;
            }
        }

        if ((file.isDirectory()
                || (file.isArchive() && Preferences.Pref_Zip_Internal)
                || file.getMimeType().contains("tar"))
                && getActionMode() == null) {
            /*
             * if (mThumbnail != null) { mThumbnail.setCancelThumbnails(true);
             * mThumbnail = null; }
             */

            // setContentPath(file, true);
            getExplorer().changePath(file);

        } else {

            if (file.requiresThread() && FileManager.hasOpenCache(file.getAbsolutePath())) {
                // getExplorer().showToast("Still need to handle this.");
                if (file.isTextFile() && getExplorer().editFile(file))
                    return;
                else {
                    showCopyFromNetworkDialog(file);
                    // getEventHandler().copyFile(file, mPath, mContext);
                }
                return;
            } else if (file.isTextFile() && Preferences.Pref_Text_Internal
                    && getExplorer().editFile(file))
                return;
            else if (!IntentManager.startIntent(file, getExplorer(),
                    Preferences.Pref_Intents_Internal)) {
                getExplorer().showToast(R.string.noApplications);
                getExplorer().editFile(file);
            }
        }
    }

    private void addToMultiSelect(final OpenPath file) {
        getContentAdapter().getSelectedSet().add(file);
    }

    private void showCopyFromNetworkDialog(OpenPath source) {
        // / TODO Implement Copy From Network
        getExplorer().showToast("Not yet implemented (" + source.getMimeType() + ")");
        return;
        /*
         * final View view = FolderPickerActivity.createPickerView(mContext);
         * new DialogBuilder(mContext) .setTitle("Choose a folder to copy " +
         * source.getName() + " into:") .setView(view)
         * .setPositiveButton(android.R.string.ok, new OnClickListener() {
         * public void onClick(DialogInterface dialog, int which) { } });
         */
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_file, menu);
        onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.equals(mGrid)) {
            if (v.getTag() != null && v.getTag() instanceof MotionEvent) {
                MotionEvent lastEvent = (MotionEvent)v.getTag();
                int x = (int)Math.floor(lastEvent.getX());
                int y = (int)Math.floor(lastEvent.getY());
                return createContextMenu(mPath, mGrid, mGrid, 0, x, y);
            } else
                return v.showContextMenu();
        }
        return false;
    }

    // public boolean onItemLongClick(AdapterView<?> list, final View view, int
    // pos, long id) {
    // mMenuContextItemIndex = pos;
    // //view.setBackgroundResource(R.drawable.selector_blue);
    // //list.setSelection(pos);
    // //if(list.showContextMenu()) return true;
    //
    // final OpenPath file =
    // (OpenPath)((BaseAdapter)list.getAdapter()).getItem(pos);
    //
    // return createContextMenu(file, list, view, pos);
    // }
    public boolean createContextMenu(final OpenPath file, final AdapterView<?> list,
            final View view, final int pos) {
        return createContextMenu(file, list, view, pos, 0, 0);
    }

    public boolean createContextMenu(final OpenPath file, final AdapterView<?> list,
            final View view, final int pos, final int xOffset, final int yOffset) {
        Logger.LogInfo(getClassName() + ".onItemLongClick: " + file);

        final OpenContextMenuInfo info = new OpenContextMenuInfo(file);

        if (!OpenExplorer.USE_PRETTY_CONTEXT_MENUS) {
            if (Build.VERSION.SDK_INT > 10) {
                final PopupMenu pop = new PopupMenu(view.getContext(), view);
                pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        if (onOptionsItemSelected(item)) {
                            pop.dismiss();
                            return true;
                        } else if (getExplorer() != null)
                            return getExplorer().onIconContextItemSelected(pop,
                                    MenuUtils.getMenuItem(item, new MenuBuilder(getActivity())),
                                    item.getMenuInfo(), view);
                        return false;
                    }
                });
                pop.getMenuInflater().inflate(R.menu.context_file, pop.getMenu());
                onPrepareOptionsMenu(pop.getMenu());
                if (DEBUG)
                    Logger.LogDebug("PopupMenu.show()");
                pop.show();
                return true;
            } else
                return list.showContextMenu();
        }

        if (!file.isDirectory() && getActionMode() == null) {
            getSherlockActivity().startActionMode(new Callback() {
                // @Override
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                // @Override
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    setActionMode(null);
                }

                // @Override
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    setActionMode(mode);
                    mode.getMenuInflater().inflate(R.menu.context_file, menu);
                    return true;
                }

                // @Override
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    // ArrayList<OpenPath> files = new ArrayList<OpenPath>();

                    // OpenPath file =
                    // mLastPath.getChild(mode.getTitle().toString());
                    // files.add(file);

                    if (item.getItemId() != R.id.menu_context_cut
                            && item.getItemId() != R.id.menu_multi
                            && item.getItemId() != R.id.menu_context_copy) {
                        mode.finish();
                    }
                    return executeMenu(item.getItemId(), mode, file);
                }
            });
            getActionMode().setTitle(file.getName());
            return true;
        }

        if (file.isDirectory() && getActionMode() == null) {
            getSherlockActivity().startActionMode(new Callback() {
                // @Override
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                // @Override
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    setActionMode(null);
                }

                // @Override
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.context_file, menu);
                    menu.findItem(R.id.menu_context_paste).setEnabled(getClipboard().size() > 0);
                    // menu.findItem(R.id.menu_context_unzip).setEnabled(mHoldingZip);

                    setActionMode(mode);

                    return true;
                }

                // @Override
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return executeMenu(item.getItemId(), mode, file);
                }
            });

            getActionMode().setTitle(file.getName());
            return true;

        }

        return false;
    }

    public static void prepareContextMenu(ContextMenu menu, OpenPath path) {
        MenuUtils.setMenuEnabled(menu, !path.isDirectory(), R.id.menu_context_edit);
        MenuUtils.setMenuEnabled(menu, path.canWrite(), R.id.menu_context_delete,
                R.id.menu_context_cut, R.id.menu_context_rename);
        MenuUtils.setMenuEnabled(menu, path.getParent().canWrite(), R.id.menu_context_paste);
    }

    @Override
    public boolean onClick(int id, View view) {
        super.onClick(id, view);
        if (getActivity() == null)
            return false;
        if (view == null)
            view = getActivity().findViewById(id);
        if (view != null && view.getTag() != null && view.getTag() instanceof Menu) {
            Logger.LogDebug("Showing Tagged Menu! " + view.getTag());
            if (showMenu((Menu)view.getTag(), view, ViewUtils.getText(view)))
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null)
            return false;
        if (super.onOptionsItemSelected(item))
            return true;
        if (DEBUG)
            Logger.LogDebug("ContentFragment.onOptionsItemSelected(0x"
                    + Integer.toHexString(item.getItemId()) + ":" + item.getTitle() + ")");
        OpenPath path = null;
        if (getSelectedCount() > 0)
            path = mContentAdapter.getSelectedSet().get(getSelectedCount() - 1);
        else if (mMenuContextItemIndex > -1
                && mMenuContextItemIndex < getContentAdapter().getCount())
            path = getContentAdapter().getItem(mMenuContextItemIndex);
        if (path != null && executeMenu(item.getItemId(), getActionMode(), path))
            return true;
        switch (item.getItemId()) {
            case R.id.menu_sort:
            case R.id.menu_view:
                return true;
            case R.id.menu_context_heatmap:
                DialogHandler.showFileHeatmap(getExplorer(), getPath());
                return true;
            case R.id.menu_new_file:
                DialogHandler.showNewFileDialog(getPath(), getActivity(), this, 1);
                return true;
            case R.id.menu_sort_name_asc:
                onSortingChanged(SortType.Type.ALPHA);
                return true;
            case R.id.menu_sort_name_desc:
                onSortingChanged(SortType.Type.ALPHA_DESC);
                return true;
            case R.id.menu_sort_date_asc:
                onSortingChanged(SortType.Type.DATE);
                return true;
            case R.id.menu_sort_date_desc:
                onSortingChanged(SortType.Type.DATE_DESC);
                return true;
            case R.id.menu_sort_size_asc:
                onSortingChanged(SortType.Type.SIZE);
                return true;
            case R.id.menu_sort_size_desc:
                onSortingChanged(SortType.Type.SIZE_DESC);
                return true;
            case R.id.menu_sort_type:
                onSortingChanged(SortType.Type.TYPE);
                return true;
            case R.id.menu_view_hidden:
                onHiddenFilesChanged(!getShowHiddenFiles());
                return true;
            case R.id.menu_view_thumbs:
                onThumbnailChanged(!getShowThumbnails());
                return true;
            case R.id.menu_sort_folders_first:
                onFoldersFirstChanged(!getFoldersFirst());
                return true;
            default:
                if (executeMenu(item.getItemId(), null, mPath))
                    return true;
        }
        return false;
    }

    public void downloadFile(OpenPath... paths) {
        if(DEBUG)
            Logger.LogDebug("ContentFragment.downloadFile: " + Utils.joinArray(paths, ", "));
        OpenPath dl = OpenExplorer.getDownloadParent().getFirstDir();
        if (dl == null)
            dl = OpenFile.getExternalMemoryDrive(true);
        if (dl == null) {
            getExplorer().showToast(R.string.s_error_ftp);
            return;
        }
        for(OpenPath p : paths)
        {
            getEventHandler().copyFile(p, dl, getContext());
        }
        refreshOperations();
    }
    
    public boolean executeMenu(final int id, final ActionMode mode, final OpenPath file) {
        Logger.LogInfo("ContentFragment.executeMenu(0x" + Integer.toHexString(id) + ") on " + file);
        final String path = file != null ? file.getPath() : null;
        OpenPath parent = file != null ? file.getParent() : mPath;
        if (parent == null || parent instanceof OpenCursor)
            parent = OpenFile.getExternalMemoryDrive(true);
        final OpenPath folder = parent;
        String name = file != null ? file.getName() : null;
        List<OpenPath> selection = mContentAdapter.getSelectedSet();

        final boolean fromPasteMenu = file.equals(mPath);

        switch (id) {
            case R.id.menu_refresh2:
            case R.id.menu_refresh:
                if (DEBUG)
                    Logger.LogDebug("Refreshing " + getPath().getPath());
                //getPath().clearChildren();
                FileManager.removeOpenCache(getPath().getPath());
                getPath().deleteFolderFromDb();
                refreshData(new Bundle(), false);
                return true;

            case R.id.menu_context_download:
                if (selection != null && selection.size() > 0)
                {
                    downloadFile(selection.toArray(new OpenPath[selection.size()]));
                    finishMode(mode);
                    return true;
                }
                break;
            case R.id.menu_context_selectall:
                if (getContentAdapter() == null)
                    return false;
                getContentAdapter().selectAll();
                return true;

            case R.id.menu_context_view:
                Intent vintent = IntentManager.getIntent(file, getExplorer(), Intent.ACTION_VIEW);
                if (vintent != null)
                    getActivity().startActivity(vintent);
                else {
                    if (getExplorer() != null)
                        getExplorer().showToast(R.string.noApplications);
                    if (file.length() < OpenExplorer.TEXT_EDITOR_MAX_SIZE)
                        getExplorer().editFile(file);
                }
                break;

            case R.id.menu_context_edit:
                Intent intent = IntentManager.getIntent(file, getExplorer(), Intent.ACTION_EDIT);
                if (intent != null) {
                    if (intent.getPackage() != null
                            && intent.getPackage().equals(getActivity().getPackageName()))
                        getExplorer().editFile(file);
                    else
                        try {
                            intent.setAction(Intent.ACTION_EDIT);
                            Logger.LogVerbose("Starting Intent: " + intent.toString());
                            getExplorer().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            getExplorer().showToast(R.string.noApplications);
                            getExplorer().editFile(file);
                        }
                } else if (file.length() < OpenExplorer.TEXT_EDITOR_MAX_SIZE) {
                    getExplorer().editFile(file);
                } else {
                    getExplorer().showToast(R.string.noApplications);
                }
                break;

            case R.id.menu_multi:
                // changeMultiSelectState(getActionMode() != null);
                if (!fromPasteMenu)
                    getClipboard().add(file);
                return true;

            case R.id.menu_context_bookmark:
                if (getSelectedCount() > 0)
                    for (OpenPath p : mContentAdapter.getSelectedSet())
                        getExplorer().addBookmark(p);
                else
                    getExplorer().addBookmark(file);
                finishMode(mode);
                return true;

            case R.id.menu_context_delete:
                // fileList.add(file);
                getEventHandler().deleteFile(file, this, true);
                finishMode(mode);
                return true;

            case R.id.menu_context_rename:
                getEventHandler().renameFile(file, true, getActivity());
                finishMode(mode);
                return true;

            case R.id.menu_context_copy:
            case R.id.menu_context_cut:
                getClipboard().DeleteSource = id == R.id.menu_context_cut;
                file.setTag(id);
                getClipboard().add(file);
                return true;

            case R.id.menu_context_paste:
            case R.id.content_paste:
                OpenPath into = file;
                if (fromPasteMenu)
                    into = mPath;
                if (!file.isDirectory()) {
                    Logger.LogWarning("Can't paste into file (" + file.getPath()
                            + "). Using parent directory (" + folder.getPath() + ")");
                    into = folder;
                }
                OpenClipboard cb = getClipboard();
                if (cb != null) {
                    cb.setCurrentPath(into);
                    checkClipboardForTar(cb, into);
                    checkClipboardForLZMA(cb, into);
                    if (cb.size() > 0) {
                        if (cb.DeleteSource)
                            getEventHandler().cutFile(cb, into, getActivity());
                        else
                            getEventHandler().copyFile(cb, into, getActivity());
                        refreshOperations();
                    }

                    cb.DeleteSource = false;
                    if (cb.ClearAfter)
                        cb.clear();
                }
                if (getExplorer() != null)
                    getExplorer().updateTitle(path);
                finishMode(mode);
                return true;

            case R.id.menu_context_zip:
                if (getClipboard() == null || getClipboard().size() == 0)
                    return false;
                OpenPath intoPath = mPath;
                if (!(intoPath instanceof OpenFile))
                    intoPath = OpenFile.getExternalMemoryDrive(true);
                if (!fromPasteMenu)
                    getClipboard().add(file);
                else
                    getClipboard().setCurrentPath(intoPath);

                getClipboard().ClearAfter = true;
                String zname = getClipboard().get(0).getName()
                        + "." + EventHandler.DefaultCompressionType
                                .toString().toLowerCase(Locale.US);
                if (getClipboard().size() > 1) {
                    OpenPath last = getClipboard().get(getClipboard().getCount() - 1);
                    if (last != null && last.getParent() != null) {
                        if (last.getParent() instanceof OpenCursor)
                            zname = folder.getPath();
                        zname = last.getParent().getName() + ".";
                        if (EventHandler.DefaultCompressionType == CompressionType.BZ2
                                || EventHandler.DefaultCompressionType == CompressionType.GZ)
                            zname += "t";
                        zname += EventHandler.DefaultCompressionType.toString().toLowerCase();
                    }
                }
                final String def = zname;
                showZipDialog(intoPath, def, folder, getClipboard(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                        switch (which)
                        {
                            case DialogInterface.BUTTON_NEGATIVE:
                                if (!fromPasteMenu && getClipboard().size() <= 1)
                                    getClipboard().clear();
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
                                if (fromPasteMenu && getClipboard().size() <= 1)
                                    getClipboard().clear();
                                break;
                            case DialogInterface.BUTTON_POSITIVE:
                                finishMode(mode);
                                break;
                        }
                    }
                });

                return true;

                // case R.id.menu_context_unzip:
                // getEventHandler().unzipFile(file, getExplorer());
                // return true;

            case R.id.menu_context_info:
                DialogHandler.showFileInfo(getExplorer(), file);
                finishMode(mode);
                return true;

            case R.id.menu_context_heatmap:
                if (getSelectedCount() > 0) {
                    OpenPathArray sels = new OpenPathArray(mContentAdapter.getSelectedSet()
                            .toArray(new OpenPath[getSelectedCount()]));
                    DialogHandler.showFileHeatmap(getExplorer(), sels);
                } else
                    DialogHandler.showFileHeatmap(getExplorer(), file);
                finishMode(mode);
                return true;

            case R.id.menu_multi_all_clear:
                getClipboard().clear();
                return true;

            case R.id.menu_context_share:

                // TODO: WTF is this?
                Intent mail = new Intent();
                mail.setType("application/mail");

                mail.setAction(android.content.Intent.ACTION_SEND);
                mail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                startActivity(mail);

                // mode.finish();
                return true;

                // this is for bluetooth
                // files.add(path);
                // getEventHandler().sendFile(files);
                // mode.finish();
                // return true;
        }
        return false;
    }

    private void showZipDialog(OpenPath intoPath, final String defaultName, final OpenPath folder,
            final List<OpenPath> files, final DialogInterface.OnClickListener onClick)
    {
        final InputDialog dZip = new InputDialog(getExplorer()).setIcon(R.drawable.sm_zip)
                .setTitle(R.string.s_menu_zip).setMessageTop(R.string.s_prompt_path)
                .setDefaultTop(intoPath.getPath()).setMessage(R.string.s_prompt_zip)
                .setCancelable(true)
                .setNegativeButton(android.R.string.no, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClick.onClick(dialog, which);
                    }
                });
        final Context context = getContext();
        ViewGroup mCompressLayout = (ViewGroup)LayoutInflater.from(getContext())
                .inflate(R.layout.compression_spinner, null);
        Spinner mCompressType = (Spinner)mCompressLayout
                .findViewById(R.id.compression_type);
        ViewGroup view = (ViewGroup)dZip.getView();

        View v = new View(context);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
        v.setLayoutParams(lp);
        v.setBackgroundColor(context.getResources().getColor(R.color.blue));
        view.addView(v);

        mCompressType.setSelection(EventHandler.DefaultCompressionType.ordinal());
        mCompressType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                    long id) {
                String name = dZip.getInputText()
                        .replace(".zip", "").replace(".tar", "")
                        .replace(".tgz", "").replace(".tbz2", "")
                        .replace(".gz", "").replace(".bz2", "");
                switch (position)
                {
                    case 0:
                        name += ".zip";
                        dZip.setDefaultText(name);
                        EventHandler.DefaultCompressionType = CompressionType.ZIP;
                        break;
                    case 1:
                        name += ".tar";
                        dZip.setDefaultText(name);
                        EventHandler.DefaultCompressionType = CompressionType.TAR;
                        break;
                    case 2:
                        name += "." + (files.size() > 1 ? "t" : "") + "gz";
                        dZip.setDefaultText(name);
                        EventHandler.DefaultCompressionType = CompressionType.GZ;
                        break;
                    case 3:
                        name += "." + (files.size() > 1 ? "t" : "") + "bz2";
                        dZip.setDefaultText(name);
                        EventHandler.DefaultCompressionType = CompressionType.BZ2;
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        view.addView(mCompressLayout);
        CheckBox mDeleteAfter = new CheckBox(getContext());
        mDeleteAfter.setText(R.string.s_delete_after);
        if (getPreferences().getBoolean("global", "pref_archive_postdelete", false))
            mDeleteAfter.setChecked(true);
        mDeleteAfter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPreferences().setSetting("global", "pref_archive_postdelete", isChecked);
            }
        });
        view.addView(mDeleteAfter);
        dZip.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onClick.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
            }
        }).setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OpenPath zFolder = new OpenFile(dZip.getInputTopText());
                if (zFolder == null || !zFolder.exists())
                    zFolder = folder;
                OpenPath zipFile = zFolder.getChild(dZip.getInputText());
                Logger.LogVerbose("Zipping " + files.size() + " items to "
                        + zipFile.getPath());
                getEventHandler().zipFile(zipFile, files, getExplorer());
                refreshOperations();
                onClick.onClick(dialog, which);
            }
        }).setDefaultText(defaultName);
        dZip.create().show();
    }

    private boolean checkClipboardForTar(final OpenClipboard cb, final OpenPath into) {
        final Hashtable<OpenTar, Vector<OpenTarEntry>> tarKids = new Hashtable<OpenTar, Vector<OpenTar.OpenTarEntry>>();
        for (OpenPath p : cb)
            if (p instanceof OpenTarEntry)
            {
                OpenTarEntry kid = (OpenTarEntry)p;
                OpenTar tar = kid.getTar();
                if (!tarKids.containsKey(tar))
                    tarKids.put(tar, new Vector<OpenTar.OpenTarEntry>());
                Vector<OpenTarEntry> kids = tarKids.get(tar);
                kids.add(kid);
                tarKids.put(tar, kids);
            }
        if (tarKids.size() == 0)
            return false;
        for (Vector<OpenTarEntry> kids : tarKids.values())
            for (OpenTarEntry kid : kids)
                cb.remove(kid);
        new Thread(new Runnable() {
            public void run() {
                for (OpenTar tar : tarKids.keySet())
                {
                    Vector<OpenTarEntry> kids = tarKids.get(tar);
                    String[] includes = new String[kids.size()];
                    for (int i = 0; i < kids.size(); i++)
                    {
                        OpenTarEntry kid = kids.get(i);
                        includes[i] = kid.getRelativePath();
                    }
                    untarAll(tar, into, includes);
                }
            }
        }).start();
        return true;
    }

    private boolean checkClipboardForLZMA(final OpenClipboard cb, final OpenPath into) {
        final Hashtable<OpenLZMA, Vector<OpenLZMAEntry>> lzKids = new Hashtable<OpenLZMA, Vector<OpenLZMA.OpenLZMAEntry>>();
        for (OpenPath p : cb)
            if (p instanceof OpenLZMAEntry)
            {
                OpenLZMAEntry kid = (OpenLZMAEntry)p;
                OpenLZMA lz = kid.getLZMA();
                if (!lzKids.containsKey(lz))
                    lzKids.put(lz, new Vector<OpenLZMA.OpenLZMAEntry>());
                Vector<OpenLZMAEntry> kids = lzKids.get(lz);
                kids.add(kid);
                lzKids.put(lz, kids);
            }
        if (lzKids.size() == 0)
            return false;
        for (Vector<OpenLZMAEntry> kids : lzKids.values())
            for (OpenLZMAEntry kid : kids)
                cb.remove(kid);
        new Thread(new Runnable() {
            public void run() {
                for (OpenLZMA tar : lzKids.keySet())
                {
                    Vector<OpenLZMAEntry> kids = lzKids.get(tar);
                    String[] includes = new String[kids.size()];
                    for (int i = 0; i < kids.size(); i++)
                    {
                        OpenLZMAEntry kid = kids.get(i);
                        includes[i] = kid.getRelativePath();
                    }
                    extractSet(tar, into, includes);
                }
            }
        }).start();
        return true;
    }

    @Override
    protected void finishMode(ActionMode mode) {
        super.finishMode(mode);
        if (getSelectedCount() > 0)
            mContentAdapter.clearSelection();
    }

    /*
     * @Override public boolean onContextItemSelected(android.view.MenuItem
     * item) { if(item == null) return false; OpenPath path = null;
     * if(item.getMenuInfo() != null && item.getMenuInfo() instanceof
     * OpenContextMenuInfo) path = ((OpenContextMenuInfo)
     * item.getMenuInfo()).getPath(); else if(mMenuContextItemIndex >= 0 &&
     * mMenuContextItemIndex < getContentAdapter().getCount()) path =
     * getContentAdapter().getItem(mMenuContextItemIndex); if(path == null) {
     * Logger.LogWarning("Couldn't find path for context menu"); return false; }
     * return executeMenu(item.getItemId(), null, path); }
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // if (DEBUG)
        // Logger.LogDebug(getClassName() + ".onCreateOptionsMenu (" + getPath()
        // + ")");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.content_full, menu);
        MenuUtils.setMenuEnabled(menu, true, R.id.menu_view);
        // MenuInflater inflater = new MenuInflater(mContext);
        // if(!OpenExplorer.USE_PRETTY_MENUS||!OpenExplorer.BEFORE_HONEYCOMB)
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Logger.LogVerbose("ContentFragment.onPrepareOptionsMenu");
        if (getActivity() == null)
            return;
        if (menu == null)
            return;
        if (isDetached() || !isVisible())
            return;
        super.onPrepareOptionsMenu(menu);
		
		MenuUtils.setMenuEnabled(menu, getPath().canWrite(), R.id.menu_new_file);
		MenuUtils.setMenuVisible(menu, mPath instanceof OpenNetworkPath, R.id.menu_context_download);
		MenuUtils.setMenuVisible(menu, !(mPath instanceof OpenNetworkPath), R.id.menu_context_edit, R.id.menu_context_view);
		
        MenuItem mMenuFF = menu.findItem(R.id.menu_sort_folders_first);
        if (mMenuFF != null) {
            if (mMenuFF.isCheckable()) {
                mMenuFF.setChecked(getSorting().foldersFirst());
            } else {
                if (getSorting().foldersFirst())
                    mMenuFF.setIcon(getThemedResourceId(R.styleable.AppTheme_checkboxButtonOn,
                            R.drawable.btn_check_on_holo_blue));
                else
                    mMenuFF.setIcon(getThemedResourceId(R.styleable.AppTheme_checkboxButtonOff,
                            R.drawable.btn_check_off));
            }
        }
        // MenuUtils.setMenuChecked(menu, getSorting().foldersFirst(),
        // R.id.menu_sort_folders_first);

        if (mPath != null)
            MenuUtils.setMenuEnabled(menu,
                    !mPath.requiresThread() && mPath.canWrite() && !mPath.isArchive(),
                    R.id.menu_multi_all_copy, R.id.menu_multi_all_move, R.id.menu_new_file);

        SortType.Type st = getSorting().getType();
        int sti = Utils.getArrayIndex(sortTypes, st);
        if (sti > -1)
            MenuUtils.setMenuChecked(menu, true, sortMenuOpts[sti], sortMenuOpts);

        if (getClipboard() == null || getClipboard().size() == 0) {
            MenuUtils.setMenuVisible(menu, false, R.id.content_paste);
        } else {
            MenuItem mPaste = menu.findItem(R.id.content_paste);
            if (mPaste != null && getClipboard() != null && !isDetached())
                mPaste.setTitle(getString(R.string.s_menu_paste) + " (" + getClipboard().size()
                        + ")");
            if (mPaste != null)
                mPaste.setVisible(true);
        }

        MenuUtils.setMenuEnabled(menu, true, R.id.menu_view, R.id.menu_sort, R.id.menu_content_ops);

        int mViewMode = getViewMode();
        if (mViewMode == OpenExplorer.VIEW_GRID)
            MenuUtils.setMenuChecked(menu, true, R.id.menu_view_grid, R.id.menu_view_list);
        else
            MenuUtils.setMenuChecked(menu, true, R.id.menu_view_list, R.id.menu_view_grid);

        MenuUtils.setMenuChecked(menu, getShowHiddenFiles(), R.id.menu_view_hidden);
        MenuUtils.setMenuChecked(menu, getShowThumbnails(), R.id.menu_view_thumbs);
    }

    /*
     * @Override public void setInitialSavedState(SavedState state) {
     * super.setInitialSavedState(state); if(state == null) return; Bundle b =
     * state.getBundle(); if(b != null && b.containsKey("last") && mPath ==
     * null) setPath(b.getString("last"));
     * Logger.LogVerbose("setInitialSavedState :: " + state.toString()); }
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            outState.putInt("view", mViewMode);
            if (mPath != null)
                outState.putParcelable("path", mPath);
            if (mListVisibleStartIndex > 0)
                outState.putInt("first", mListVisibleStartIndex);
            if (mListScrollY > 0)
                outState.putInt("scroll", mListScrollY);
            if (mGrid != null)
                outState.putParcelable("grid", mGrid.onSaveInstanceState());
        } catch (NullPointerException e) {
            Logger.LogError("Not sure why this is causing NPE crashes", e);
        }

        /*
         * if(mPath != null && mPath.getPath() != null) {
         * Logger.LogDebug("Content location saving to " + mPath.getPath());
         * outState.putString("location", mPath.getPath()); }
         */
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        if (mBundle == null && savedInstanceState != null)
            mBundle = savedInstanceState;

        if (mBundle == null)
            mBundle = new Bundle();

        // mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
        // if(mGrid == null)
        mGrid = (GridView)v.findViewById(R.id.content_grid);

        // if(mProgressBarLoading == null) mProgressBarLoading =
        // v.findViewById(R.id.content_progress);
        setProgressVisibility(false);

        if (mGrid == null)
            Logger.LogError("WTF, where are they?");
        else
            updateGridView();

        // refreshData(mBundle, true);

        if (mGrid != null) {
            if (mBundle.containsKey("scroll") && mBundle.getInt("scroll") > 0) {
                // Logger.LogDebug("Returning Scroll to " +
                // mBundle.getInt("scroll"));
                mGrid.scrollTo(0, mBundle.getInt("scroll"));
            } else if (mBundle.containsKey("grid"))
                mGrid.onRestoreInstanceState(mBundle.getParcelable("grid"));
            if (mBundle.containsKey("first")) {
                // Logger.LogDebug("Returning first item #" +
                // mBundle.getInt("first"));
                mGrid.setSelection(mBundle.getInt("first"));
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mTask != null)
            mTask.cancel(true);
        super.onDestroy();
    }

    @TargetApi(11)
    public void updateGridView() {
        // Logger.LogDebug("updateGridView() @ " + mPath);
        if (mGrid == null)
            mGrid = (GridView)getView().findViewById(R.id.content_grid);
        if (mGrid == null) {
            Logger.LogWarning("This shouldn't happen");
            mGrid = (GridView)((LayoutInflater)getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.content_grid, null);
            ((ViewGroup)getView()).addView(mGrid);
            setupGridView();
        }
        if (mGrid != null)
            mGrid.invalidateViews();
        if (getExplorer() == null) {
            Logger.LogWarning("ContentFragment.updateGridView warning: getExplorer() is null");
            return;
        }

        invalidateOptionsMenu();

        setupGridView();
        refreshData(getArguments(), false);
    }

    public void setupGridView() {
        mGrid.setVisibility(View.VISIBLE);
        mGrid.setOnItemClickListener(this);
        mGrid.setOnItemLongClickListener(this);
        mGrid.setColumnWidth(getResources().getDimensionPixelSize(
                    getViewMode() == OpenExplorer.VIEW_GRID ? R.dimen.grid_width : R.dimen.list_width
                ));
        mGrid.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mListScrollingState = scrollState;
                if (view != null)
                    mListScrollY = view.getScrollY();
                // if(scrollState == 0)
                // onScrollStopped(view);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                if (firstVisibleItem != mListVisibleStartIndex)
                    mListVisibleStartIndex = firstVisibleItem;
                if (visibleItemCount != mListVisibleLength)
                    mListVisibleLength = visibleItemCount;
            }
        });
        if (!OpenExplorer.USE_PRETTY_CONTEXT_MENUS) // || !USE_ACTIONMODE)
            registerForContextMenu(mGrid);
    }

    /*
     * protected void onScrollStopped(AbsListView view) { boolean skipThis =
     * true; if(skipThis) return; int start = Math.max(0,
     * mListVisibleStartIndex); int end = Math.min(mData2.size() - 1,
     * mListVisibleStartIndex + mListVisibleLength); int mWidth = 128, mHeight =
     * 128; ThumbnailStruct[] thumbs = new ThumbnailStruct[end - start]; for(int
     * i = start; i < end; i++) { Object o = view.getItemAtPosition(i); if(o !=
     * null) { OpenPath file = (OpenPath)o; if(file.getTag() != null &&
     * file.getTag().getClass().equals(BookmarkHolder.class)) { BookmarkHolder
     * mHolder = (BookmarkHolder)file.getTag(); ImageView v =
     * mHolder.getIconView(); thumbs[i - start] = new ThumbnailStruct(file,
     * mHolder, mWidth, mHeight); //new ThumbnailTask().execute(new
     * ThumbnailStruct(file, mHolder, mWidth, mHeight)); } }
     * //view.getItemAtPosition(i); } new ThumbnailTask().execute(thumbs);
     * //Logger.LogDebug("Visible items " +
     * mData2.get(mListVisibleStartIndex).getName() + " - " +
     * mData2.get().getName()); }
     */

    @Override
    public int getPagerPriority() {
        return 1;
    }

    @Override
    public void onWorkerThreadFailure(EventType type, OpenPath... files) {
        for (OpenPath path : files)
            sendToLogView(type.name() + " error on " + path, Color.RED);
        setProgressVisibility(false);
    }

    @Override
    public void onWorkerThreadComplete(EventType type, String... results) {
        Logger.LogVerbose("Need to refresh!");
        switch (type)
        {
            case SEARCH:
                if (results == null || results.length < 1) {
                    Toast.makeText(getApplicationContext(), "Sorry, zero items found",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<OpenPath> files = new ArrayList<OpenPath>();
                for (String s : results)
                    files.add(new OpenFile(s));

                Toast.makeText(getActivity(), "Unimplemented", Toast.LENGTH_LONG).show();
                break;
            default:
                if (results.length == 1)
                    Toast.makeText(getContext(), results[0], Toast.LENGTH_LONG).show();
                // Logger.LogDebug("Worker thread complete (" + type + ")?");
                if (!mPath.requiresThread() || FileManager.hasOpenCache(mPath.getAbsolutePath()))
                    try {
                        if (mPath.requiresThread())
                            mPath = FileManager.getOpenCache(mPath.getPath());
                        if (mPath != null)
                            updateData(mPath.list());
                    } catch (IOException e) {
                        Logger.LogWarning("Couldn't update data after thread completion", e);
                    }
                else {
                    // if(mProgressBarLoading == null) mProgressBarLoading =
                    // getView().findViewById(R.id.content_progress);
                    EventHandler.executeNetwork(new NetworkIOTask(this), mPath);
                }

                // changePath(mPath, false);
                notifyDataSetChanged();

                refreshData(new Bundle(), false);
                // changePath(getManager().peekStack(), false);
        }
        setProgressVisibility(false);
    }

    @Override
    public void onWorkerProgressUpdate(int pos, int total) {
        setProgressVisibility(pos < total);
    }

    private void saveTopPath() {
        if (mGrid == null)
            return;
        mTopIndex = mGrid.getFirstVisiblePosition();
        if (mContentAdapter != null && mTopIndex > -1 && mTopIndex < mContentAdapter.getCount()) {
            mTopPath = mContentAdapter.getItem(mTopIndex);
            Logger.LogVerbose("Top Path saved to " + mTopIndex
                    + (mTopPath != null ? " :: " + mTopPath.getName() : ""));
        }
    }

    private Boolean restoreTopPath() {
        if (mTopPath != null) {
            // Logger.LogDebug("Looking for top path (" + mTopPath.getName() +
            // ")");
            for (int i = 0; i < mContentAdapter.getCount(); i++)
                if (mContentAdapter.getItem(i).getName().equals(mTopPath.getName()))
                    return restoreTopPath(i);
        }
        if (mTopIndex > 0) {
            // Logger.LogDebug("Falling back to top index (" + mTopIndex + ")");
            return restoreTopPath(mTopIndex);
        } else
            return true;
    }

    private Boolean restoreTopPath(int index) {
        Logger.LogVerbose("Top Path restored to " + index);
        mGrid.setSelection(index);
        mTopIndex = 0;
        mTopPath = null;
        return true;
    }

    public void onFoldersFirstChanged(boolean first) {
        setSorting(null, first);
        refreshData(null, false);
    }

    public void onHiddenFilesChanged() {
        onHiddenChanged(!getShowHiddenFiles());
    }

    // @Override
    public void onHiddenFilesChanged(boolean toShow) {
        Logger.LogInfo("onHiddenFilesChanged(" + toShow + ")");
        saveTopPath();
        setViewSetting(getPath(), "show", toShow);
        if (mContentAdapter != null)
            mContentAdapter.setShowHiddenFiles(toShow);
        // getManager().setShowHiddenFiles(state);
        refreshData(new Bundle(), false);
    }

    public void onThumbnailChanged() {
        onThumbnailChanged(!getShowThumbnails());
    }

    // @Override
    public void onThumbnailChanged(boolean state) {
        saveTopPath();
        setShowThumbnails(state);
        refreshData(new Bundle(), false);
    }

    // @Override
    public void onSortingChanged(SortType.Type type) {
        setSorting(type, null);
        // getManager().setSorting(type);
        refreshData(new Bundle(), false);
    }

    public void setSorting(SortType.Type newType, Boolean foldersFirst) {
        SortType newSort = getContentAdapter() != null ? getContentAdapter().getSorting()
                : SortType.ALPHA;
        newSort.setType(newType).setFoldersFirst(foldersFirst);

        setViewSetting(mPath, "sort", newSort.toString());

        getContentAdapter().setSorting(newSort);
    }

    public void setShowThumbnails(boolean thumbs) {
        mContentAdapter.mShowThumbnails = thumbs;
        setViewSetting(mPath, "thumbs", thumbs);
    }

    public void setSettings(SortType sort, boolean thumbs, boolean hidden) {
        setSorting(sort.getType(), thumbs);

        refreshData(new Bundle(), false);
    }

    // @Override
    public void onViewChanged(int state) {
        setViewMode(state);
    }

    @Override
    public void setProgressVisibility(boolean visible) {
        // if(mProgressBarLoading == null && mGrid != null && mGrid.getParent()
        // != null) mProgressBarLoading =
        // ((View)mGrid.getParent()).findViewById(R.id.content_progress);
        // if(mProgressBarLoading != null && mData.length == 0)
        // mProgressBarLoading.setVisibility(visible ? View.VISIBLE :
        // View.GONE);
        ViewUtils.setViewsVisible(getView(), visible, R.id.content_cancel);
        if(visible)
            setStatus(mLoadingMessage);
        else {
            updateStatus();
            ViewUtils.setViewsVisible(getView(), false, android.R.id.empty);
        }
        if (getExplorer() != null)
            getExplorer().setProgressVisibility(visible);
    }

    private void setProgressClickHandler(android.view.View.OnClickListener listener)
    {
        ViewUtils.setOnClicks(getView(), listener, R.id.content_cancel);
        if (getExplorer() != null)
            getExplorer().setProgressClickHandler(listener);
    }

    public SortType getSorting() {
        return mContentAdapter != null ? mContentAdapter.getSorting() : SortType.ALPHA;
    }

    public boolean getFoldersFirst() {
        return getSorting().foldersFirst();
    }

    public boolean getShowHiddenFiles() {
        if (mContentAdapter != null)
            return mContentAdapter.getShowHiddenFiles();
        else
            return getViewSetting(getPath(), "show", false);
    }

    public boolean getShowThumbnails() {
        return mContentAdapter != null ? mContentAdapter.mShowThumbnails : true;
    }

    public void updateStatus()
    {
        ViewUtils.setViewsVisible(mStatusBar, false);
        new Thread(new Runnable() {
            public void run() {
                final CharSequence cs = getContentAdapter().getStatus();
                getHandler().post(new Runnable() {
                    public void run() {
                        setStatus(cs);
                    }
                });
            }}).start();
        getContentAdapter().getStatus2(new SpaceListener() {
            public void onException(Exception e) {
                ViewUtils.setViewsVisible(getView(), false, R.id.content_status_right);
            }
            
            @Override
            public void onSpaceReturned(long total, long used, long third) {
                String txt = "";
                if(used > 0)
                {
                    txt = OpenPath.formatSize(used);
                    if(total > 0)
                        txt += "/";
                }
                if(total > 0)
                    txt += OpenPath.formatSize(total);
                if(third > 0)
                    txt += "(" + OpenPath.formatSize(third) + ")";
                if(txt.equals(""))
                {
                    ViewUtils.setViewsVisible(getView(), false, R.id.content_status_right);
                    return;
                }
                txt += " " + getContext().getString(R.string.s_total);
                ViewUtils.setText(getView(), txt, R.id.content_status_right);
                ViewUtils.setViewsVisible(getView(), true, R.id.content_status_bar, R.id.content_status_right);
            }
        });        
    }

    public void setStatus(CharSequence status)
    {
        boolean empty = status == null || status.length() == 0 || status.toString().startsWith("Loading");
        ViewUtils.setViewsVisible(mStatusBar, !empty);
        if(!empty)
            ViewUtils.setText(mStatus, status);
    }

    @Override
    public CharSequence getTitle() {
        if (mPath == null)
            return "???";
        CharSequence tit = mPath.getTitle(getContext());
        if(!Utils.isNullOrEmpty(tit))
            return tit;
        String ret = mPath.getName();
        if (Utils.isNullOrEmpty(ret) && mPath != null && mPath.getUri() != null)
            ret = mPath.getUri().getLastPathSegment();
        if ((ret == null || ret.equals("")) && mPath != null)
            ret = mPath.toString();
        if ((mPath instanceof OpenFile || mPath instanceof OpenNetworkPath)
                && mPath.isDirectory() && !ret.endsWith("/"))
            ret += "/";
        return ret;
    }

    @Override
    public OpenPath getPath() {
        if (mPath == null && getArguments() != null && getArguments().containsKey("path"))
            return mPath = (OpenPath)getArguments().getParcelable("path");
        return mPath;
    }

    @Override
    public Drawable getIcon() {
        if (isDetached())
            return null;
        if (getActivity() == null || getResources() == null) return null;
        OpenPath path = getPath();
        if (path == null) return null;
        Drawable ret = getResources().getDrawable(
                ThumbnailCreator.getDefaultResourceId(getPath(), 96, 96));
        if (path instanceof OpenPath.ThumbnailOverlayInterface)
        {
            Drawable overlay = ((ThumbnailOverlayInterface)path).getOverlayDrawable(getActivity(), true);
            ret = new LayerDrawable(new Drawable[]{ret,overlay});
        }
        return ret;
    }

    @Override
    public void updateData(final OpenPath[] result) {
        if (mContentAdapter == null) {
            Logger.LogWarning("ContentFragment.updateData warning: mContentAdapter is null");
            return;
        }
        if(OpenExplorer.IS_DEBUG_BUILD)
        	Logger.LogVerbose("ContentFragment.updateData");
        if (Thread.currentThread().equals(OpenExplorer.UiThread)) {
            mContentAdapter.updateData(result);
            notifyDataSetChanged();
        } else {
            OpenExplorer.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mContentAdapter.updateData(result);
                    notifyDataSetChanged();
                }
            });
        }
        updateStatus();
        // notifyDataSetChanged();
    }

    @Override
    public void addFiles(OpenPath[] files) {
        mContentAdapter.addAll(Arrays.asList(files));
    }

    /**
     * Show/hide the "selection" action mode, according to the number of
     * selected messages and the visibility of the fragment. Also update the
     * content (title and menus) if necessary.
     */
    public void updateSelectionMode() {
        final int numSelected = getSelectedCount();
        if ((numSelected == 0) || mDisableCab || !isViewCreated()) {
            finishSelectionMode();
            return;
        }
        if (isInSelectionMode()) {
            updateSelectionModeView();
        } else {
            mLastSelectionModeCallback = new SelectionModeCallback();
            /*
             * for(OpenPath clip : getClipboard().getAll())
             * addToMultiSelect(clip);
             */
            if (getExplorer() != null)
                getExplorer().startActionMode(mLastSelectionModeCallback);
            // mGrid.invalidateViews();

        }
    }

    /**
     * Finish the "selection" action mode. Note this method finishes the
     * contextual mode, but does *not* clear the selection. If you want to do so
     * use {@link #onDeselectAll()} instead.
     */
    private void finishSelectionMode() {
        if (isInSelectionMode()) {
            mLastSelectionModeCallback.mClosedByUser = false;
            getActionMode().finish();
        }
    }

    /** Update the "selection" action mode bar */
    private void updateSelectionModeView() {
        if(isInSelectionMode())
            getActionMode().invalidate();
        // notifyDataSetChanged();
    }

    /**
     * @return the number of messages that are currently selected.
     */
    private int getSelectedCount() {
        if (mContentAdapter != null && mContentAdapter.getSelectedSet() != null)
            return mContentAdapter.getSelectedSet().size();
        else
            return 0;
    }

    /**
     * @return true if the list is in the "selection" mode.
     */
    public boolean isInSelectionMode() {
        return getActionMode() != null;
    }

    public void deselectAll() {
        mContentAdapter.clearSelection();
        if (isInSelectionMode()) {
            finishSelectionMode();
        }
    }

    @Override
    public void onAdapterSelectedChanged(OpenPath path, boolean newSelected, int mSelectedCount) {
        updateSelectionMode();
    }

    private class SelectionModeCallback implements ActionMode.Callback {
        private MenuItem mShare;
        private int viewPageNum;
        private ShareActionProvider mShareActionProvider;

        private boolean pasteReady = false;

        /* package */boolean mClosedByUser = true;
        private MenuItem mRename;
        private MenuItem mInfo;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            setActionMode(mode);

            MenuInflater inflater = getExplorer().getSupportMenuInflater();
            inflater.inflate(R.menu.content_action, menu);

            mRename = menu.findItem(R.id.menu_context_rename);
            mInfo = menu.findItem(R.id.menu_context_info);

            // Set file with share history to the provider and set the share
            // intent.
            mShare = menu.findItem(R.id.menu_context_share);
            if (mShare != null && mShare.getActionProvider() != null) {
                mShareActionProvider = (ShareActionProvider)mShare.getActionProvider();
                if (mShareActionProvider != null) {
                    int cnt = getSelectedCount();
                    OpenPath first = mContentAdapter.getSelectedSet().get(0);
                    Intent shareIntent = null;
                    shareIntent = new Intent(cnt > 1 ? Intent.ACTION_SEND_MULTIPLE
                            : Intent.ACTION_VIEW);
                    if (first != null) {
                        shareIntent.setType(first.getMimeType());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, first.getUri());
                    }
                    mShareActionProvider.setShareIntent(shareIntent);
                    mShareActionProvider
                            .setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
                }
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int num = getSelectedCount();
            // Set title -- "# selected"
            if(num > 1)
                mode.setTitle(getExplorer().getResources().getQuantityString(R.plurals.num_selected,
                    num, num));
            else
                mode.setTitle(mContentAdapter.getSelectedSet().get(0).getName());

            if (mShareActionProvider != null) {
                Intent shareIntent = null;
                int cnt = getSelectedCount();
                menu.removeGroup(10);
                OpenPath first = mContentAdapter.getSelectedSet().get(0);
                if (cnt > 1) {
                    shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    String type = Utils.ifNull(first.getMimeType(), "*/*");
                    ArrayList<Uri> uris = new ArrayList<Uri>();
                    for (OpenPath sel : mContentAdapter.getSelectedSet()) {
                        if (!type.equals(sel.getMimeType()))
                            type = "*/*";
                        uris.add(sel.getUri());
                    }
                    shareIntent.setType(type);
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                } else {
                    shareIntent = new Intent(Intent.ACTION_VIEW);
                    shareIntent.setDataAndType(first.getUri(), Utils.ifNull(first.getMimeType(), "*/*"));
                }
                List<ResolveInfo> resolves = IntentManager.getResolvesAvailable(shareIntent,
                        getExplorer());
                int numIntents = resolves.size();
                boolean hasIntents = numIntents > 1;
                MenuUtils.setMenuVisible(menu, hasIntents, R.id.menu_context_share);
                if (hasIntents)
                    mShareActionProvider.setShareIntent(shareIntent);
                else if (numIntents == 1) {
                    ResolveInfo app = resolves.get(0);
                    shareIntent.setPackage(app.activityInfo.packageName);
                    final Intent theIntent = shareIntent;
                    menu.add(10, Menu.NONE, Menu.FIRST,
                            app.loadLabel(getContext().getPackageManager()))
                            .setIcon(app.loadIcon(getContext().getPackageManager()))
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    getActivity().startActivity(theIntent);
                                    return true;
                                }
                            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
            }

            boolean writable = true, readable = true;
            for (OpenPath p : mContentAdapter.getSelectedSet()) {
                if (writable && !p.canWrite())
                    writable = false;
                if (readable && !p.canRead())
                    readable = false;
                if (!readable)
                    break;
            }
            OpenPath last = mContentAdapter.getSelectedSet().get(getSelectedCount() - 1);

            MenuUtils.setMenuEnabled(menu, writable, R.id.menu_context_delete,
                    R.id.menu_context_cut, R.id.menu_context_rename);
            MenuUtils.setMenuEnabled(menu, readable, R.id.menu_context_copy, R.id.menu_context_cut,
                    R.id.menu_context_download, R.id.menu_context_zip, R.id.menu_context_share);
            
            if(last instanceof CloudOpsHandler)
                MenuUtils.setMenuShowAsAction(menu, MenuItem.SHOW_AS_ACTION_ALWAYS, R.id.menu_context_download);

            if (num == 1) {
                MenuUtils.setMenuVisible(menu, true, R.id.menu_context_bookmark);
                if (!last.isDirectory())
                    MenuUtils.setMenuShowAsAction(menu, MenuItem.SHOW_AS_ACTION_NEVER,
                            R.id.menu_context_bookmark);
                if (last.isFile())
                    mRename.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                mRename.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                MenuUtils.setMenuVisible(menu, false, R.id.menu_context_heatmap);
            }

            mRename.setVisible(num == 1);
            mInfo.setVisible(num == 1);

            viewPageNum = num;
            return true;
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            final CopyOnWriteArrayList<OpenPath> selections = mContentAdapter.getSelectedSet();
            final OpenPath last = selections.get(selections.size() - 1);
            switch (item.getItemId()) {
                case R.id.menu_context_selectall:
                    mContentAdapter.selectAll();
                    mode.invalidate();
                    break;
                case R.id.menu_context_copy:
                    getClipboard().addAll(selections);
                    deselectAll();
                    break;
                case R.id.menu_context_delete:
                    getEventHandler().deleteFile(selections, ContentFragment.this, true);
                    deselectAll();
                    break;
                case R.id.menu_context_zip:
                    OpenPath intoPath = mPath;
                    if (!(intoPath instanceof OpenFile))
                        intoPath = OpenFile.getExternalMemoryDrive(true);
                    final OpenPath folder = intoPath;

                    final OpenPath[] toZip = selections.toArray(new OpenPath[selections.size()]);
                    if (toZip.length == 0)
                        return false;
                    String zname = toZip[0].getName();
                    String type = EventHandler.DefaultCompressionType.toString().toLowerCase();
                    if (toZip.length > 1) {
                        if (last != null && last.getParent() != null)
                            zname = last.getParent().getName() + "-"
                                    + new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());
                        if (type.equals("gz") || type.equals("bz2"))
                            type = "t" + type;
                    }
                    zname += "." + type;
                    final String def = zname;

                    showZipDialog(intoPath, def, folder, selections, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which != DialogInterface.BUTTON_NEUTRAL)
                                deselectAll();
                        }
                    });
                    break;
                case R.id.menu_context_rename:
                    getEventHandler().renameFile(last, last.isDirectory(), getActivity());
                    finishMode(mode);
                    break;
                case R.id.menu_context_info:
                    DialogHandler.showFileInfo(getExplorer(), last);
                    break;
                default:
                    if (onOptionsItemSelected(item))
                        return true;
                    break;
            }
            // }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear this before onDeselectAll() to prevent onDeselectAll() from
            // trying to close the
            // contextual mode again.
            setActionMode(null);
            if (mClosedByUser) {
                // Clear selection, only when the contextual mode is explicitly
                // closed by the user.
                //
                // We close the contextual mode when the fragment becomes
                // temporary invisible
                // (i.e. mIsVisible == false) too, in which case we want to keep
                // the selection.
                deselectAll();
            }
            if (mGrid != null)
                mGrid.invalidateViews();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0 && Preferences.Pref_ShowUp) return false;
        Logger.LogInfo(getClassName() + ".onItemLongClick");
        OpenPath path = getContentAdapter().getItem(position);
        toggleSelection(view, path);
        return true;
    }

    private void toggleSelection(View view, OpenPath path) {
        // view.invalidate();
        mContentAdapter.toggleSelected(path, view);
    }

    public void notifyDataSetChanged() {
        if (getExplorer() == null)
            return;
        if(OpenExplorer.IS_DEBUG_BUILD)
        	Logger.LogVerbose("ContentFragment.notifyDataSetChanged");
        if (!Thread.currentThread().equals(OpenExplorer.UiThread)) {
            OpenExplorer.getHandler().post(new Runnable() {
                public void run() {
                    notifyDataSetChanged();
                }
            });
            return;
        }
        if (mContentAdapter == null) {
            mContentAdapter = getContentAdapter();
        }

        //mContentAdapter.finalize();

        if (mGrid != null
                && (mGrid.getAdapter() == null || !mGrid.getAdapter().equals(mContentAdapter)))
            mGrid.setAdapter(mContentAdapter);

        // if(mContentAdapter != null)
        // mContentAdapter.updateData();
        mContentAdapter.notifyDataSetChanged();

        boolean empty = mContentAdapter == null || mContentAdapter.getCount() == 0;
        if (empty)
            ViewUtils.setText(getView(),
                    getString(!mPath.isLoaded() ? R.string.s_status_loading
                            : R.string.no_items, ""), android.R.id.empty);
        ViewUtils.setViewsVisibleNow(getView(), empty, android.R.id.empty);

        updateStatus();

        // TODO check to see if this is the source of inefficiency
        // if(mGrid != null)
        // mGrid.invalidateViews();
    }
}

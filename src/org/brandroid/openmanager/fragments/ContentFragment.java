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

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.ContentAdapter;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFileRoot;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenZip;
import org.brandroid.openmanager.data.OpenPath.OpenContentUpdater;
import org.brandroid.openmanager.data.OpenPath.OpenPathUpdateListener;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.NetworkIOTask;
import org.brandroid.openmanager.util.NetworkIOTask.OnTaskUpdateListener;
import org.brandroid.openmanager.util.ActionModeHelper;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.InputDialog;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.EventHandler.EventType;
import org.brandroid.openmanager.util.EventHandler.OnWorkerUpdateListener;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuBuilder;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class ContentFragment extends OpenFragment
		implements OnItemClickListener, OnItemLongClickListener,
					OnWorkerUpdateListener, OpenPathFragmentInterface,
					OnTaskUpdateListener
{
	
	//private static MultiSelectHandler mMultiSelect;
	//private LinearLayout mPathView;
	//private SlidingDrawer mMultiSelectDrawer;
	//private GridView mMultiSelectView;
	protected GridView mGrid = null;
	protected Object mActionMode = null;
	//private View mProgressBarLoading = null;
	
	//private ArrayList<OpenPath> mData2 = null; //the data that is bound to our array adapter.
	private int mListScrollingState = 0;
	private int mListVisibleStartIndex = 0;
	private int mListVisibleLength = 0; 
	private int mListScrollY = 0;
	public static float DP_RATIO = 1;
	public static int mGridImageSize = 128;
	public static int mListImageSize = 36;
	public Boolean mShowLongDate = false;
	private int mTopIndex = 0;
	private OpenPath mTopPath = null;
	protected OpenPath mPath = null;
	private OnPathChangeListener mPathListener = null;
	protected int mMenuContextItemIndex = -1;
	private boolean mRefreshReady = true;
	public static final SortType.Type[] sortTypes = new SortType.Type[]{SortType.Type.ALPHA,SortType.Type.ALPHA_DESC,SortType.Type.SIZE,SortType.Type.SIZE_DESC,SortType.Type.DATE,SortType.Type.DATE_DESC,SortType.Type.TYPE};
	public static final int[] sortMenuOpts = new int[]{R.id.menu_sort_name_asc,R.id.menu_sort_name_desc,R.id.menu_sort_size_asc,R.id.menu_sort_size_desc,R.id.menu_sort_date_asc,R.id.menu_sort_date_desc,R.id.menu_sort_type};
	
	private Bundle mBundle;
	
	protected Integer mViewMode = null;
	protected ContentAdapter mContentAdapter;
	
	//private static Hashtable<OpenPath, ContentFragment> instances = new Hashtable<OpenPath, ContentFragment>();
	
	public interface OnPathChangeListener
	{
		public void changePath(OpenPath newPath);
	}

	public ContentFragment() {
		if(getArguments() != null && getArguments().containsKey("last"))
		{
			Logger.LogDebug("ContentFragment Restoring to " + getArguments().getString("last"));
			setPath(getArguments().getString("last"));
		}
	}
	private ContentFragment(OpenPath path)
	{
		mPath = path;
	}
	private ContentFragment(OpenPath path, int view)
	{
		mPath = path;
		mViewMode = view;
	}
	private void setPath(String path)
	{
		mPath = FileManager.getOpenCache(path, getAndroidContext());
	}
	public static ContentFragment getInstance(OpenPath path, int mode)
	{
		return getInstance(path, mode, null);
	}
	public static ContentFragment getInstance(OpenPath path, int mode, FragmentManager fm)
	{
		ContentFragment ret = null;
		if(fm != null)
			ret = (ContentFragment) fm.findFragmentByTag(path.getPath());
		if(ret == null)
			ret = new ContentFragment(path, mode);
		//if(path instanceof OpenFile) return ret;
		Bundle args = ret.getArguments();
		if(args == null)
			args = new Bundle();
		if(path != null)
		{
			args.putString("last", path.getPath());
			ret.setArguments(args);
		} else return null;
		//Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() + ", " + mode + ")");
		return ret;
	}
	public static ContentFragment getInstance(OpenPath path)
	{
		return getInstance(path, new Bundle());
	}
	public static ContentFragment getInstance(OpenPath path, Bundle args)
	{
		ContentFragment ret = new ContentFragment(path);
		if(path != null && !args.containsKey("last"))
		{
			args.putString("last", path.getPath());
			ret.setArguments(args);
		} else return null;
		//Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() + ")");
		return ret;
	}
	public static ContentFragment getInstance(Bundle args)
	{
		ContentFragment ret = new ContentFragment();
		ret.setArguments(args);
		return ret;
	}

	public static void cancelAllTasks()
	{
		NetworkIOTask.cancelAllTasks();
	}
	
	protected ContentAdapter getContentAdapter() {
		if(mContentAdapter == null)
			mContentAdapter = new ContentAdapter(getActivity(), mViewMode, mPath);
		return mContentAdapter;
	}
	
	public int getViewMode() {
		if(mViewMode == null)
			mViewMode = getViewSetting(mPath, "view", getGlobalViewMode());
		return mViewMode;
	}
	public int getGlobalViewMode() {
		String pref = getSetting(null, "pref_view", "list");
		if(pref.equals("list"))
			return OpenExplorer.VIEW_LIST;
		if(pref.equals("grid"))
			return OpenExplorer.VIEW_GRID;
		if(pref.equals("carousel"))
			return OpenExplorer.VIEW_CAROUSEL;
		return OpenExplorer.VIEW_LIST;
	}

	
	private void setViewMode(int mode) {
		mViewMode = mode;
		setViewSetting(mPath, "view", mode);
		Logger.LogVerbose("Content View Mode: " + mode);
		if(mContentAdapter != null)
		{
			mGrid.setAdapter(null);
			mContentAdapter = new ContentAdapter(getAndroidContext(), mViewMode, mPath);
			mContentAdapter.setCheckClipboardListener(this);
			//mContentAdapter = new OpenPathAdapter(mPath, mode, getExplorer());
			mGrid.setAdapter(mContentAdapter);
		} else {
			mContentAdapter.setViewMode(mode);
			refreshData(null, false);
		}
	}
	
	//@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		DP_RATIO = getResources().getDimension(R.dimen.one_dp);
		mGridImageSize = (int) (DP_RATIO * getResources().getInteger(R.integer.content_grid_image_size));
		mListImageSize = (int) (DP_RATIO * getResources().getInteger(R.integer.content_list_image_size));
		if(savedInstanceState != null)
			mBundle = savedInstanceState;
		if(getArguments() != null && getArguments().containsKey("last"))
			mBundle = getArguments();
		if(mBundle != null && mBundle.containsKey("last") && (mPath == null || !mPath.getPath().equals(mBundle.getString("last"))))
			mPath = FileManager.getOpenCache(mBundle.getString("last"), getAndroidContext());
		if(mBundle != null && mBundle.containsKey("view"))
			mViewMode = mBundle.getInt("view");
		
		if(mPath == null)
			Logger.LogDebug("Creating empty ContentFragment", new Exception("Creating empty ContentFragment"));
		else Logger.LogDebug("Creating ContentFragment @ " + mPath);
		
		//OpenExplorer.getEventHandler().setOnWorkerThreadFinishedListener(this);
		
	}
	
	public synchronized void notifyDataSetChanged() {
		if(mContentAdapter == null) {
			mContentAdapter = new ContentAdapter(getActivity(), mViewMode, mPath);
			if(mGrid != null)
				mGrid.setAdapter(mContentAdapter);
		}
		//if(!Thread.currentThread().equals(OpenExplorer.UiThread))
		//	getActivity().runOnUiThread(new Runnable(){public void run(){mContentAdapter.updateData();}});
		//else
		
			mContentAdapter.updateData();
	}
	public synchronized void refreshData()
	{
		refreshData(getArguments(), false);
	}
	public synchronized void refreshData(Bundle savedInstanceState, boolean allowSkips)
	{
		if(!mRefreshReady) return;
		if(!isVisible()) {
			Logger.LogDebug("I'm invisible! " + mPath);
			//return;
		}
		
		if(getAndroidContext() == null) {
			Logger.LogError("RefreshData out of context");
			return;
		}
		
		if(savedInstanceState == null && getArguments() != null)
			savedInstanceState = getArguments();
		
		OpenPath path = mPath;
		if(path == null)
			if (savedInstanceState != null && savedInstanceState.containsKey("last"))
				path = new OpenFile(savedInstanceState.getString("last"));
		
		if(path == null) return;
		
		mRefreshReady = false;
		
		if(mContentAdapter == null)
			mContentAdapter = new ContentAdapter(getAndroidContext(), mViewMode, path);

		if(path instanceof OpenFile && !path.getPath().startsWith("/"))
		{
			if(path.getPath().equals("Photos"))
				path = OpenExplorer.getPhotoParent();
			if(path.getPath().equals("Videos"))
				path = OpenExplorer.getVideoParent();
			if(path.getPath().equals("Music"))
				path = OpenExplorer.getMusicParent();
			if(path.getPath().equals("Downloads"))
				path = OpenExplorer.getDownloadParent();
		}
		
		if(path instanceof OpenFile &&
				(path.getName().equalsIgnoreCase("data") ||
				path.getPath().indexOf("/data") > -1 ||
				path.getPath().indexOf("/system") > -1))
			path = new OpenFileRoot(path);
		
		mPath = path;
		
		Logger.LogDebug("Refreshing Data for " + mPath);
		
		mActionModeSelected = false;
		SortType sort = SortType.ALPHA;
		if(getExplorer() != null)
			sort = new SortType(getViewSetting(path, "sort", 
					getExplorer().getSetting(null, "pref_sorting", SortType.ALPHA.toString())));
		try {
			mContentAdapter.mShowThumbnails = getViewSetting(path, "thumbs", 
						getExplorer() != null ?
								getExplorer().getSetting(null, "pref_thumbs", true)
								: true);
		} catch(NullPointerException npe) {
			Logger.LogWarning("Null while getting prefs", npe);
		}
		
		mContentAdapter.setSorting(sort);
		
		//Logger.LogVerbose("View options for " + path.getPath() + " : " + (mShowHiddenFiles ? "show" : "hide") + " + " + (mShowThumbnails ? "thumbs" : "icons") + " + " + mSorting.toString());

		//if(path.getClass().equals(OpenCursor.class) && !OpenExplorer.BEFORE_HONEYCOMB)
		//	mShowThumbnails = true;
		
		if(getActivity() != null && getActivity().getWindow() != null)
			mShowLongDate = getResources().getBoolean(R.bool.show_long_date) //getActivity().getWindow().getWindowManager().getDefaultDisplay().getRotation() % 180 != 0
					&& mPath != null;

		if(path instanceof OpenFileRoot)
		{
			runUpdateTask();
		} else if(!path.requiresThread() && (!allowSkips || path.getListLength() < 300))
			try {
				path.listFiles();
			} catch (IOException e) {
				Logger.LogError("Error getting children from FileManager for " + path, e);
			}
		else {
			if(path.listFromDb(mContentAdapter.getSorting()))
			{
				int loaded = mContentAdapter.getCount();
				if(path instanceof OpenNetworkPath)
				{
					OpenNetworkPath[] kids = ((OpenNetworkPath)path).getChildren();
					mContentAdapter.updateData(kids);
					loaded = kids.length;
				}
				Logger.LogDebug("Loaded " + loaded + " entries from cache");
				runUpdateTask();
			} else if(path instanceof OpenFile)
				((OpenFile)path).listFiles();
			else runUpdateTask();
			//updateData(mData, allowSkips);
			//cancelAllTasks();
			
		}
		
		notifyDataSetChanged();
		
		mRefreshReady = true;
		
		//OpenExplorer.setOnSettingsChangeListener(this);
		
			//new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, path));
			
			//if(mGrid != null && savedInstanceState.containsKey("first"))
			
	}
	
	public void runUpdateTask() { runUpdateTask(false); }
	public void runUpdateTask(boolean reconnect)
	{
		if(mPath == null) return;
		if(mPath instanceof OpenPathUpdateListener)
		{
			try {
				((OpenPathUpdateListener)mPath).list(new OpenContentUpdater() {
					public void addContentPath(OpenPath file) {
						if(!mContentAdapter.contains(file))
						{
							mContentAdapter.add(file);
						}
					}

					@Override
					public void doneUpdating() {
						mContentAdapter.sort();
						mContentAdapter.notifyDataSetChanged();
					}
				});
				return;
			} catch (IOException e) {
				Logger.LogError("Couldn't list with ContentUpdater");
			}
		}
		final String sPath = mPath.getPath();
		//NetworkIOTask.cancelTask(sPath);
		final NetworkIOTask task = new NetworkIOTask(this);
		if(NetworkIOTask.isTaskRunning(sPath)) return;
		setProgressVisibility(true);
		if(reconnect && (mPath instanceof OpenNetworkPath))
			((OpenNetworkPath)mPath).disconnect();
		Logger.LogDebug("Running Task for " + sPath);
		NetworkIOTask.addTask(sPath, task);
		if(OpenExplorer.BEFORE_HONEYCOMB)
			task.execute(mPath);
		else
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPath);
		new Thread(new Runnable(){
			@Override
			public void run() {
				try { Thread.sleep(30000); } catch (InterruptedException e) { }
				if(task.getStatus() == Status.RUNNING)
					task.doCancel(false);
			}
		}).start();
	}
	

	//@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.content_layout, container, false);
		mGrid = (GridView)v.findViewById(R.id.content_grid);
		final OpenFragment me = this;
		mGrid.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() != KeyEvent.ACTION_DOWN) return false;
				int col = 0;
				int cols = 1;
				try {
					if(!OpenExplorer.BEFORE_HONEYCOMB)
					{
						Method m = GridView.class.getMethod("getNumColumns", new Class[0]);
						Object tmp = m.invoke(mGrid, new Object[0]);
						if(tmp instanceof Integer)
						{
							cols = (Integer)tmp;
							col = mGrid.getSelectedItemPosition() % cols;
						}
					}
				} catch(Exception e) { }
				Logger.LogDebug("ContentFragment.mGrid.onKey(" + keyCode + "," + event + ")@" + col);
				if(!OpenExplorer.BEFORE_HONEYCOMB)
					cols = (Integer)mGrid.getNumColumns();
				if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && col == 0)
					return onFragmentDPAD(me, false);
				else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && col == cols - 1)
					return onFragmentDPAD(me, true);
				else if(MenuUtils.getMenuShortcut(event) != null)
				{
					MenuItem item = MenuUtils.getMenuShortcut(event);
					if(onOptionsItemSelected(item))
					{
						Toast.makeText(v.getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
						return true;
					}
				} 
				return false;
			}
		});
		v.setOnLongClickListener(this);
		mGrid.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				getMenuInflater().inflate(R.menu.context_file, menu);
				onPrepareOptionsMenu(menu);
			}
		});
		//if(mProgressBarLoading == null)
		//	mProgressBarLoading = v.findViewById(R.id.content_progress);
		setProgressVisibility(false);
		super.onCreateView(inflater, container, savedInstanceState);
		//v.setBackgroundResource(R.color.lightgray);
		
		/*
		if (savedInstanceState != null && savedInstanceState.containsKey("location")) {
			String location = savedInstanceState.getString("location");
			if(location != null && !location.equals("") && location.startsWith("/"))
			{
				Logger.LogDebug("Content location restoring to " + location);
				mPath = new OpenFile(location);
				mData = getManager().getChildren(mPath);
				updateData(mData);
			}
			//setContentPath(path, false);
		}
		*/

		return v;
	}
	
	@Override
	public boolean onBackPressed() {
		return false;
	}
	

	//@Override
	public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
		OpenPath file = (OpenPath)list.getItemAtPosition(pos);
		
		Logger.LogInfo("File clicked: " + file.getPath());
		
		if(file.isArchive() && file instanceof OpenFile && Preferences.Pref_Zip_Internal)
			file = new OpenZip((OpenFile)file);
		
		if(getClipboard().isMultiselect()) {
			if(getClipboard().contains(file))
			{
				getClipboard().remove(file);
				if(getClipboard().size() == 0)
					getClipboard().stopMultiselect();
				((BaseAdapter)list.getAdapter()).notifyDataSetChanged();
			} else {
				//Animation anim = Animation.
				/*
				Drawable dIcon = ((ImageView)view.findViewById(R.id.content_icon)).getDrawable();
				if(dIcon instanceof BitmapDrawable)
				{
					IconAnimationPanel panel = new IconAnimationPanel(getExplorer())
						.setIcon(((BitmapDrawable)dIcon).getBitmap())
						.setStart(new Point(view.getLeft(), view.getRight()))
						.setEnd(new Point(getActivity().getWindow().getWindowManager().getDefaultDisplay().getWidth() / 2, getActivity().getWindowManager().getDefaultDisplay().getHeight()))
						.setDuration(500);
					((ViewGroup)getView()).addView(panel);
				}
				*/
				
				addToMultiSelect(file);
				((TextView)view.findViewById(R.id.content_text)).setTextAppearance(list.getContext(), R.style.Highlight);
			}
			return;
		}
		
		if(file.isDirectory() && !mActionModeSelected ) {
			/* if (mThumbnail != null) {
				mThumbnail.setCancelThumbnails(true);
				mThumbnail = null;
			} */
			
			
			//setContentPath(file, true);
			getExplorer().onChangeLocation(file);

		} else if (!file.isDirectory() && !mActionModeSelected) {
			
			if(file.requiresThread() && FileManager.hasOpenCache(file.getAbsolutePath()))
			{
				//getExplorer().showToast("Still need to handle this.");
				if(file.isTextFile())
					getExplorer().editFile(file);
				else {
					showCopyFromNetworkDialog(file);
					//getEventHandler().copyFile(file, mPath, mContext);
				}
				return;
			} else if(file.isTextFile() && Preferences.Pref_Text_Internal)
				getExplorer().editFile(file);
			else if(!IntentManager.startIntent(file, getExplorer(), Preferences.Pref_Intents_Internal))
				getExplorer().editFile(file);
		}
	}

	private void addToMultiSelect(final OpenPath file)
	{
		getClipboard().add(file);
	}
	

	private void showCopyFromNetworkDialog(OpenPath source)
	{
		/// TODO Implement Copy From Network
		getExplorer().showToast("Not yet implemented (" + source.getMimeType() + ")");
		return;
		/*
		final View view = FolderPickerActivity.createPickerView(mContext);
		new DialogBuilder(mContext)
			.setTitle("Choose a folder to copy " + source.getName() + " into:")
			.setView(view)
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			*/
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.context_file, menu);
		onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(v.equals(mGrid))
		{
			if(v.getTag() != null && v.getTag() instanceof MotionEvent)
			{
				MotionEvent lastEvent = (MotionEvent)v.getTag();
				int x = (int)Math.floor(lastEvent.getX());
				int y = (int)Math.floor(lastEvent.getY());
				return createContextMenu(mPath, mGrid, mGrid, 0, x, y);
			} else return v.showContextMenu();
		}
		return false;
	}
	
	public boolean onItemLongClick(AdapterView<?> list, final View view, int pos, long id) {
		mMenuContextItemIndex = pos;
		//view.setBackgroundResource(R.drawable.selector_blue);
		//list.setSelection(pos);
		//if(list.showContextMenu()) return true;
		
		final OpenPath file = (OpenPath)((BaseAdapter)list.getAdapter()).getItem(pos);
		
		return createContextMenu(file, list, view, pos);
	}
	public boolean createContextMenu(final OpenPath file, final AdapterView<?> list,
			final View view, final int pos)
	{
		return createContextMenu(file, list, view, pos, 0, 0);
	}
	public boolean createContextMenu(final OpenPath file, final AdapterView<?> list,
			final View view, final int pos, final int xOffset, final int yOffset)
	{
		Logger.LogInfo(getClassName() + ".onItemLongClick: " + file);
		
		final OpenContextMenuInfo info = new OpenContextMenuInfo(file);
		
		if(!OpenExplorer.USE_PRETTY_CONTEXT_MENUS)
		{
			if(Build.VERSION.SDK_INT > 10)
			{
				final PopupMenu pop = new PopupMenu(view.getContext(), view);
				pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						if(onOptionsItemSelected(item))
						{
							pop.dismiss();
							return true;
						}
						else if(getExplorer() != null)
							return getExplorer().onIconContextItemSelected(pop, item, item.getMenuInfo(), view);
						return false;
					}
				});
				pop.getMenuInflater().inflate(R.menu.context_file, pop.getMenu());
				onPrepareOptionsMenu(pop.getMenu());
				if(DEBUG)
					Logger.LogDebug("PopupMenu.show()");
				pop.show();
				return true;
			} else
				return list.showContextMenu();
		} else if(OpenExplorer.BEFORE_HONEYCOMB || !OpenExplorer.USE_ACTIONMODE) {
			
			try {
				//View anchor = view; //view.findViewById(R.id.content_context_helper);
				//if(anchor == null) anchor = view;
				//view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				//Rect r = new Rect(view.getLeft(),view.getTop(),view.getMeasuredWidth(),view.getMeasuredHeight());
				MenuBuilder cmm = IconContextMenu.newMenu(list.getContext(), R.menu.context_file);
				if(!file.canRead())
				{
					MenuUtils.setMenuEnabled(cmm, false);
					MenuUtils.setMenuEnabled(cmm, true, R.id.menu_context_info);
				}
				MenuUtils.setMenuEnabled(cmm, file.canWrite(), R.id.menu_context_paste, R.id.menu_context_cut, R.id.menu_context_delete, R.id.menu_context_rename);
				onPrepareOptionsMenu(cmm);
				
				//if(!file.isArchive()) hideItem(cmm, R.id.menu_context_unzip);
				if(getClipboard().size() > 0)
					MenuUtils.setMenuVisible(cmm, false, R.id.menu_multi);
				else
					MenuUtils.setMenuVisible(cmm, false, R.id.menu_context_paste);
				MenuUtils.setMenuEnabled(cmm, !file.isDirectory(), R.id.menu_context_edit, R.id.menu_context_view);
				final IconContextMenu cm = new IconContextMenu(
						list.getContext(), cmm, view);
				//cm.setAnchor(anchor);
				cm.setNumColumns(2);
				cm.setOnIconContextItemSelectedListener(getExplorer());
				cm.setInfo(info);
				cm.setTextLayout(R.layout.context_item);
				cm.setTitle(file.getName());
				if(!cm.show()) //r.left, r.top);
					return list.showContextMenu();
				else return true;
			} catch(Exception e) {
				Logger.LogWarning("Couldn't show Iconified menu.", e);
				return list.showContextMenu();
			}
		}
		
		if(!OpenExplorer.BEFORE_HONEYCOMB && OpenExplorer.USE_ACTIONMODE)
		{
			if(!file.isDirectory() && mActionMode == null && !getClipboard().isMultiselect()) {
				try {
					Method mStarter = getActivity().getClass().getMethod("startActionMode");
					mActionMode = mStarter.invoke(getActivity(),
							new ActionModeHelper.Callback() {
						//@Override
						public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
							return false;
						}
						
						//@Override
						public void onDestroyActionMode(android.view.ActionMode mode) {
							mActionMode = null;
							mActionModeSelected = false;
						}
						
						//@Override
						public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
							mode.getMenuInflater().inflate(R.menu.context_file, menu);
				    		
				    		mActionModeSelected = true;
							return true;
						}

						//@Override
						public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
							//ArrayList<OpenPath> files = new ArrayList<OpenPath>();
							
							//OpenPath file = mLastPath.getChild(mode.getTitle().toString());
							//files.add(file);
							
							if(item.getItemId() != R.id.menu_context_cut && item.getItemId() != R.id.menu_multi && item.getItemId() != R.id.menu_context_copy)
							{
								mode.finish();
								mActionModeSelected = false;
							}
							return executeMenu(item.getItemId(), mode, file);
						}
					});
					Class cAM = Class.forName("android.view.ActionMode");
					Method mST = cAM.getMethod("setTitle", CharSequence.class);
					mST.invoke(mActionMode, file.getName());
				} catch (Exception e) {
					Logger.LogError("Error using ActionMode", e);
				}
			}
			return true;

		}
		
		if(file.isDirectory() && mActionMode == null && !getClipboard().isMultiselect()) {
			if(!OpenExplorer.BEFORE_HONEYCOMB && OpenExplorer.USE_ACTIONMODE)
			{
				try {
					Method mStarter = getActivity().getClass().getMethod("startActionMode");
					mActionMode = mStarter.invoke(getActivity(),
							new ActionModeHelper.Callback() {
					
					//@Override
					public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
						return false;
					}
					
					//@Override
					public void onDestroyActionMode(android.view.ActionMode mode) {
						mActionMode = null;
						mActionModeSelected = false;
					}
					
					//@Override
					public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
						mode.getMenuInflater().inflate(R.menu.context_file, menu);
						menu.findItem(R.id.menu_context_paste).setEnabled(getClipboard().size() > 0);
						//menu.findItem(R.id.menu_context_unzip).setEnabled(mHoldingZip);
			        	
			        	mActionModeSelected = true;
						
			        	return true;
					}
					
					//@Override
					public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
						return executeMenu(item.getItemId(), mode, file);
					}
				});
					Class cAM = Class.forName("android.view.ActionMode");
					Method mST = cAM.getMethod("setTitle", CharSequence.class);
					mST.invoke(mActionMode, file.getName());
				} catch (Exception e) {
					Logger.LogError("Error using ActionMode", e);
				}
			}
			
			return true;
		}
		
		return false;
	}

	public static void prepareContextMenu(ContextMenu menu, OpenPath path)
	{
		MenuUtils.setMenuEnabled(menu, !path.isDirectory(), R.id.menu_context_edit);
		MenuUtils.setMenuEnabled(menu, path.canWrite(), R.id.menu_context_delete, R.id.menu_context_cut, R.id.menu_context_rename);
		MenuUtils.setMenuEnabled(menu, path.getParent().canWrite(), R.id.menu_context_paste);
	}
	
	@Override
	public boolean onClick(int id, View view) {
		super.onClick(id, view);
		if(getActivity() == null) return false;
		if(view == null)
			view = getActivity().findViewById(id);
		if(view != null && view.getTag() != null && view.getTag() instanceof Menu)
		{
			Logger.LogDebug("Showing Tagged Menu! " + (Menu)view.getTag());
			if(showMenu((Menu)view.getTag(), view, ViewUtils.getText(view)))
				return true;
		}
		switch(id)
		{
		case R.id.menu_content_ops:
			if(OpenExplorer.USE_PRETTY_MENUS &&
					showMenu(R.menu.content_ops, view, getString(R.string.s_title_operations)))
				return true;
			break;
		case R.id.menu_sort:
			if(OpenExplorer.USE_PRETTY_MENUS &&
					showMenu(R.menu.content_sort, view, getString(R.string.s_menu_sort)))
				return true;
			break;
		case R.id.menu_view:
			if(OpenExplorer.USE_PRETTY_MENUS &&
					showMenu(R.menu.content_view, view, getString(R.string.s_view)))
				return true;
			break;
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item == null) return false;
		if(DEBUG)
			Logger.LogDebug("ContentFragment.onOptionsItemSelected(0x" + Integer.toHexString(item.getItemId()) + ")");
		OpenPath path = null;
		if(mMenuContextItemIndex > -1 && mMenuContextItemIndex < getContentAdapter().getCount())
			path = getContentAdapter().getItem(mMenuContextItemIndex);
		if(path != null && executeMenu(item.getItemId(), mActionMode, path))
			return true;
		switch(item.getItemId())
		{
		case R.id.menu_sort:
		case R.id.menu_view:
		case R.id.menu_content_ops:
			if(OpenExplorer.USE_PRETTY_MENUS)
				onPrepareOptionsMenu(item.getSubMenu());
			return !OpenExplorer.USE_PRETTY_MENUS; // for system menus, return false
		case R.id.menu_new_file:
			EventHandler.createNewFile(getPath(), getActivity());
			return true;
		case R.id.menu_new_folder:
			EventHandler.createNewFolder(getPath(), getActivity());
			return true;
		case R.id.menu_sort_name_asc:	onSortingChanged(SortType.ALPHA); return true; 
		case R.id.menu_sort_name_desc:	onSortingChanged(SortType.ALPHA_DESC); return true; 
		case R.id.menu_sort_date_asc: 	onSortingChanged(SortType.DATE); return true;
		case R.id.menu_sort_date_desc: 	onSortingChanged(SortType.DATE_DESC); return true; 
		case R.id.menu_sort_size_asc: 	onSortingChanged(SortType.SIZE); return true; 
		case R.id.menu_sort_size_desc: 	onSortingChanged(SortType.SIZE_DESC); return true; 
		case R.id.menu_sort_type: 		onSortingChanged(SortType.TYPE); return true;
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
			if(executeMenu(item.getItemId(), null, mPath))
				return true;
		}
		return false;
	}

	
	public boolean executeMenu(final int id, final Object mode, final OpenPath file)
	{
		Logger.LogInfo("ContentFragment.executeMenu(0x" + Integer.toHexString(id) + ") on " + file);
		final String path = file != null ? file.getPath() : null;
		OpenPath parent = file != null ? file.getParent() : mPath;
		if(parent == null || parent instanceof OpenCursor)
			parent = OpenFile.getExternalMemoryDrive(true);
		final OpenPath folder = parent;
		String name = file != null ? file.getName() : null;
		
		final boolean fromPasteMenu = file.equals(mPath);
		
		switch(id)
		{
			case R.id.menu_refresh:
				if(DEBUG)
					Logger.LogDebug("Refreshing " + getPath().getPath());
				getPath().clearChildren();
				FileManager.removeOpenCache(getPath().getPath());
				getPath().deleteFolderFromDb();
				runUpdateTask(true);
				refreshData(new Bundle(), false);
				return true;
				
			case R.id.menu_context_download:
				OpenPath dl = OpenExplorer.getDownloadParent().getFirstDir();
				if(dl == null)
					dl = OpenFile.getExternalMemoryDrive(true);
				if(dl != null)
				{
					List<OpenPath> files = new ArrayList<OpenPath>();
					files.add(file);
					getEventHandler().copyFile(files, dl, getActivity());
				} else
					getExplorer().showToast(R.string.s_error_ftp);
				return true;
			
			case R.id.menu_context_selectall:
				if(getContentAdapter() == null) return false;
				boolean hasAll = true;
				for(OpenPath p : getContentAdapter().getAll())
					if(!getClipboard().contains(p))
					{
						hasAll = false;
						break;
					}
				if(!hasAll)
					getClipboard().addAll(getContentAdapter().getAll());
				else
					getClipboard().removeAll(getContentAdapter().getAll());
				return true;
				
			case R.id.menu_context_view:
				Intent vintent = IntentManager.getIntent(file, getExplorer(), Intent.ACTION_VIEW);
				if(vintent != null)
					getActivity().startActivity(vintent);
				else {
					if(getExplorer() != null)
						getExplorer().showToast(R.string.s_error_no_intents);
					if(file.length() < getResources().getInteger(R.integer.max_text_editor_size))
						getExplorer().editFile(file);
				}
				break;
				
			case R.id.menu_context_edit:
				Intent intent = IntentManager.getIntent(file, getExplorer(), Intent.ACTION_EDIT);
				if(intent != null)
				{
					if(intent.getPackage() != null && intent.getPackage().equals(getActivity().getPackageName()))
						getExplorer().editFile(file);
					else
						try {
							intent.setAction(Intent.ACTION_EDIT);
							Logger.LogVerbose("Starting Intent: " + intent.toString());
							getExplorer().startActivity(intent);
						} catch(ActivityNotFoundException e) {
							getExplorer().showToast(R.string.s_error_no_intents);
							getExplorer().editFile(file);
						}
				} else if(file.length() < getResources().getInteger(R.integer.max_text_editor_size)) {
					getExplorer().editFile(file);
				} else {
					getExplorer().showToast(R.string.s_error_no_intents);
				}
				break;

			case R.id.menu_multi:
				changeMultiSelectState(!getClipboard().isMultiselect());
				if(!fromPasteMenu)
					getClipboard().add(file);
				return true;
			case R.id.menu_context_bookmark:
				getExplorer().addBookmark(file);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_delete:
				//fileList.add(file);
				getHandler().deleteFile(file, getActivity(), true);
				finishMode(mode);
				if(getContentAdapter() != null)
					getContentAdapter().notifyDataSetChanged();
				return true;
				
			case R.id.menu_context_rename:
				getHandler().renameFile(file, true, getActivity());
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
				if(fromPasteMenu) into = mPath;
				if(!file.isDirectory())
				{
					Logger.LogWarning("Can't paste into file (" + file.getPath() + "). Using parent directory (" + folder.getPath() + ")");
					into = folder;
				}
				OpenClipboard cb = getClipboard();
				cb.setCurrentPath(into);
				if(cb.size() > 0)
				{
					if(cb.DeleteSource)
						getHandler().cutFile(cb, into, getActivity());
					else
						getHandler().copyFile(cb, into, getActivity());
					refreshOperations();
				}
				
				cb.DeleteSource = false;
				if(cb.ClearAfter)
					getClipboard().clear();
				getExplorer().updateTitle(path);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_zip:
				if(!fromPasteMenu)
					getClipboard().add(file);
				else getClipboard().setCurrentPath(mPath);
				
				getClipboard().ClearAfter = true;
				String zname = getClipboard().get(0).getName()
						.replace("." + file.getExtension(), "") + ".zip";
				if(getClipboard().size() > 1)
				{
					OpenPath last = getClipboard().get(getClipboard().getCount() - 1);
					if(last != null && last.getParent() != null)
					{
						if(last.getParent() instanceof OpenCursor)
							zname = folder.getPath();
						zname = last.getParent().getName() + ".zip";
					}
				}
				final String def = zname;
				
				final InputDialog dZip = new InputDialog(getExplorer())
					.setIcon(R.drawable.sm_zip)
					.setTitle(R.string.s_menu_zip)
					.setMessageTop(R.string.s_prompt_path)
					.setDefaultTop(mPath.getPath())
					.setMessage(R.string.s_prompt_zip)
					.setCancelable(true)
					.setNegativeButton(android.R.string.no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(!fromPasteMenu && getClipboard().size() <= 1)
								getClipboard().clear();
						}
					});
				dZip
					.setOnCancelListener(new OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							if(fromPasteMenu && getClipboard().size() <= 1)
								getClipboard().clear();
						}
					})
					.setPositiveButton(android.R.string.ok,
						new OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								OpenPath zFolder = new OpenFile(dZip.getInputTopText());
								if(zFolder == null || !zFolder.exists())
									zFolder = folder;
								OpenPath zipFile = zFolder.getChild(dZip.getInputText());
								Logger.LogVerbose("Zipping " + getClipboard().size() + " items to " + zipFile.getPath());
								getHandler().zipFile(zipFile, getClipboard(), getExplorer());
								refreshOperations();
								finishMode(mode);
							}
						})
					.setDefaultText(def);
				dZip.create().show();
				return true;
				
			//case R.id.menu_context_unzip:
			//	getHandler().unzipFile(file, getExplorer());
			//	return true;
			
			case R.id.menu_context_info:
				DialogHandler.showFileInfo(getExplorer(), file);
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
				
				//mode.finish();
				return true;
	
	//			this is for bluetooth
	//			files.add(path);
	//			getHandler().sendFile(files);
	//			mode.finish();
	//			return true;
			}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item == null) return false;
		OpenPath path = null;
		if(item.getMenuInfo() != null &&
				item.getMenuInfo() instanceof OpenContextMenuInfo)
			path = ((OpenContextMenuInfo) item.getMenuInfo()).getPath();
		else if(mMenuContextItemIndex >= 0 &&
				mMenuContextItemIndex < getContentAdapter().getCount())
			path = getContentAdapter().getItem(mMenuContextItemIndex);
		if(path == null) {
			Logger.LogWarning("Couldn't find path for context menu");
			return false;
		}
		return executeMenu(item.getItemId(), null, path);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(DEBUG)
			Logger.LogDebug(getClassName() + ".onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu, inflater);
		if(!OpenExplorer.USE_PRETTY_MENUS || Build.VERSION.SDK_INT > 10)
			inflater.inflate(R.menu.content_full, menu);
		else inflater.inflate(R.menu.content, menu);
		MenuUtils.setMenuEnabled(menu, true, R.id.menu_view);
		//MenuInflater inflater = new MenuInflater(mContext);
		//if(!OpenExplorer.USE_PRETTY_MENUS||!OpenExplorer.BEFORE_HONEYCOMB)
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Logger.LogVerbose("ContentFragment.onPrepareOptionsMenu");
		if(getActivity() == null) return;
		if(menu == null) return;
		if(isDetached() || !isVisible()) return;
		super.onPrepareOptionsMenu(menu);
		if(OpenExplorer.BEFORE_HONEYCOMB)
			MenuUtils.setMenuVisible(menu, false, R.id.menu_view_carousel);
		
		MenuUtils.setMenuVisible(menu, mPath instanceof OpenNetworkPath, R.id.menu_context_download);
		MenuUtils.setMenuVisible(menu, !(mPath instanceof OpenNetworkPath), R.id.menu_context_edit, R.id.menu_context_view);
		
		MenuUtils.setMenuChecked(menu, getSorting().foldersFirst(),
				R.id.menu_sort_folders_first);
		
		if(mPath != null)
			MenuUtils.setMenuEnabled(menu, !mPath.requiresThread() && mPath.canWrite(),
					R.id.menu_multi_all_copy, R.id.menu_multi_all_move);		
		
		SortType.Type st = getSorting().getType();
		int sti = Utils.getArrayIndex(sortTypes, st);
		if(sti > -1)
			MenuUtils.setMenuChecked(menu, true, sortMenuOpts[sti], sortMenuOpts);
		
		if(getClipboard() == null || getClipboard().size() == 0)
		{
			MenuUtils.setMenuVisible(menu, false, R.id.content_paste);
		} else {
			MenuItem mPaste = menu.findItem(R.id.content_paste);
			if(mPaste != null && getClipboard() != null && !isDetached())
				mPaste.setTitle(getString(R.string.s_menu_paste) + " (" + getClipboard().size() + ")");
			if(getClipboard().isMultiselect())
			{
				LayerDrawable d = (LayerDrawable) getResources().getDrawable(R.drawable.ic_menu_paste_multi);
				d.getDrawable(1).setAlpha(127);
				if(menu.findItem(R.id.content_paste) != null)
					menu.findItem(R.id.content_paste).setIcon(d);
			}
			if(mPaste != null)
				mPaste.setVisible(true);
		}
		
		MenuUtils.setMenuEnabled(menu, true, R.id.menu_view, R.id.menu_sort, R.id.menu_content_ops);
		
		int mViewMode = getViewMode();
		MenuUtils.setMenuChecked(menu, true, 0, R.id.menu_view_grid, R.id.menu_view_list, R.id.menu_view_carousel);
		if(mViewMode == OpenExplorer.VIEW_GRID)
			MenuUtils.setMenuChecked(menu, true, R.id.menu_view_grid, R.id.menu_view_list, R.id.menu_view_carousel);
		else if(mViewMode == OpenExplorer.VIEW_LIST)
			MenuUtils.setMenuChecked(menu, true, R.id.menu_view_list, R.id.menu_view_grid, R.id.menu_view_carousel);
		else if(mViewMode == OpenExplorer.VIEW_CAROUSEL)
			MenuUtils.setMenuChecked(menu, true, R.id.menu_view_carousel, R.id.menu_view_grid, R.id.menu_view_list);
		
		MenuUtils.setMenuChecked(menu, getShowHiddenFiles(), R.id.menu_view_hidden);
		MenuUtils.setMenuChecked(menu, getShowThumbnails(), R.id.menu_view_thumbs);
		MenuUtils.setMenuVisible(menu, OpenExplorer.CAN_DO_CAROUSEL, R.id.menu_view_carousel);
		
	}
	
	@Override
	public boolean inflateMenu(Menu menu, int itemId, MenuInflater inflater) {
		if(!OpenExplorer.USE_PRETTY_MENUS) return false;
		switch(itemId)
		{
		case R.id.menu_view:
			inflater.inflate(R.menu.content_view, menu);
			return true;
		case R.id.menu_sort:
			inflater.inflate(R.menu.content_sort, menu);
			return true;
		case R.id.menu_content_ops:
			inflater.inflate(R.menu.content_ops, menu);
			return true;
		}
		return super.inflateMenu(menu, itemId, inflater);
	}
	
	/*
	@Override
	public void setInitialSavedState(SavedState state) {
		super.setInitialSavedState(state);
		if(state == null) return;
		Bundle b = state.getBundle();
		if(b != null && b.containsKey("last") && mPath == null)
			setPath(b.getString("last"));
		
		Logger.LogVerbose("setInitialSavedState :: " + state.toString());
	}
	*/
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		try {
			super.onSaveInstanceState(outState);
			outState.putInt("view", mViewMode);
			if(mPath != null)
				outState.putString("last", mPath.getPath());
			if(mListVisibleStartIndex > 0)
				outState.putInt("first", mListVisibleStartIndex);
			if(mListScrollY > 0)
				outState.putInt("scroll", mListScrollY);
			if(mGrid != null)
				outState.putParcelable("grid", mGrid.onSaveInstanceState());
		} catch(NullPointerException e) {
			Logger.LogError("Not sure why this is causing NPE crashes", e);
		}

		/*
		if(mPath != null && mPath.getPath() != null)
		{
			Logger.LogDebug("Content location saving to " + mPath.getPath());
			outState.putString("location", mPath.getPath());
		}
		*/
	}
	
	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		
		if(mBundle == null && savedInstanceState != null)
			mBundle = savedInstanceState;
		
		if(mBundle == null)
			mBundle = new Bundle();
		
		//mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
		//if(mGrid == null)
		mGrid = (GridView)v.findViewById(R.id.content_grid);
		
		//if(mProgressBarLoading == null) mProgressBarLoading = v.findViewById(R.id.content_progress);
		setProgressVisibility(false);

		if(mGrid == null)
			Logger.LogError("WTF, where are they?");
		else
			updateGridView();
		
		//refreshData(mBundle, true);
		
		if(mGrid != null)
		{
			if(mBundle.containsKey("scroll") && mBundle.getInt("scroll") > 0)
			{
				Logger.LogDebug("Returning Scroll to " + mBundle.getInt("scroll"));
				mGrid.scrollTo(0, mBundle.getInt("scroll"));
			} else if(mBundle.containsKey("grid"))
				mGrid.onRestoreInstanceState(mBundle.getParcelable("grid"));
			if(mBundle.containsKey("first"))
			{
				Logger.LogDebug("Returning first item #" + mBundle.getInt("first"));
				mGrid.setSelection(mBundle.getInt("first"));
			}
		}
	}

	@TargetApi(11)
	public void updateGridView()
	{
		Logger.LogDebug("updateGridView() @ " + mPath);
		int mLayoutID;
		if(mGrid == null)
			mGrid = (GridView)getView().findViewById(R.id.content_grid);
		if(mGrid == null)
		{
			Logger.LogWarning("This shouldn't happen");
			mGrid = (GridView)((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.content_grid, null);
			((ViewGroup)getView()).addView(mGrid);
			setupGridView();
		}
		mGrid.invalidateViews();
		mViewMode = getViewMode();
		if(getExplorer() == null) return;
		//mSorting = FileManager.parseSortType(getExplorer().getSetting(mPath, "sort", getExplorer().getPreferences().getSetting("global", "pref_sorting", mSorting.toString())));
		//mShowHiddenFiles = !getExplorer().getSetting(mPath, "hide", getExplorer().getPreferences().getSetting("global", "pref_hide", true));
		//mShowThumbnails = getExplorer().getSetting(mPath, "thumbs", getExplorer().getPreferences().getSetting("global", "pref_thumbs", true));
		
		invalidateOptionsMenu();
		
		if(getViewMode() == OpenExplorer.VIEW_GRID)
			mGrid.setColumnWidth(getResources().getDimensionPixelSize(R.dimen.grid_width));
		else
			mGrid.setColumnWidth(getResources().getDimensionPixelSize(R.dimen.list_width));
		
		if(mGrid == null) return;

		mContentAdapter = new ContentAdapter(getExplorer(), mViewMode, mPath);
		mContentAdapter.setCheckClipboardListener(this);

		mGrid.setAdapter(mContentAdapter);
		refreshData(getArguments(), false);
		setupGridView();
	}
	public void setupGridView()
	{
		mGrid.setVisibility(View.VISIBLE);
		mGrid.setOnItemClickListener(this);
		mGrid.setOnItemLongClickListener(this);
		mGrid.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mListScrollingState = scrollState;
				if(view != null)
					mListScrollY = view.getScrollY();
				//if(scrollState == 0)
				//	onScrollStopped(view);
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				if(firstVisibleItem != mListVisibleStartIndex)
					mListVisibleStartIndex = firstVisibleItem;
				if(visibleItemCount != mListVisibleLength)
					mListVisibleLength = visibleItemCount;
			}
		});
		if(!OpenExplorer.USE_PRETTY_CONTEXT_MENUS) //|| !USE_ACTIONMODE)
			registerForContextMenu(mGrid);
	}
	
	/*
	protected void onScrollStopped(AbsListView view)
	{
		boolean skipThis = true;
		if(skipThis) return;
		int start = Math.max(0, mListVisibleStartIndex);
		int end = Math.min(mData2.size() - 1, mListVisibleStartIndex + mListVisibleLength);
		int mWidth = 128, mHeight = 128;
		ThumbnailStruct[] thumbs = new ThumbnailStruct[end - start];
		for(int i = start; i < end; i++)
		{
			Object o = view.getItemAtPosition(i);
			if(o != null)
			{
				OpenPath file = (OpenPath)o;
				if(file.getTag() != null && file.getTag().getClass().equals(BookmarkHolder.class))
				{
					BookmarkHolder mHolder = (BookmarkHolder)file.getTag();
					ImageView v = mHolder.getIconView();
					thumbs[i - start] = new ThumbnailStruct(file, mHolder, mWidth, mHeight);
					//new ThumbnailTask().execute(new ThumbnailStruct(file, mHolder, mWidth, mHeight));
				}
			}
			//view.getItemAtPosition(i);
		}
		
		new ThumbnailTask().execute(thumbs);
		//Logger.LogDebug("Visible items " + mData2.get(mListVisibleStartIndex).getName() + " - " + mData2.get().getName());
	}
	*/
	
	@Override
	public int getPagerPriority() {
		return 1;
	}

	@Override
	public void onWorkerThreadFailure(EventType type, OpenPath... files)
	{
		for(OpenPath path : files)
			sendToLogView(type.name() + " error on " + path, Color.RED);
		setProgressVisibility(false);
	}
	
	@Override
	public void onWorkerThreadComplete(EventType type, String... results)
	{	
		Logger.LogVerbose("Need to refresh!");
		if(type == EventType.SEARCH) {
			if(results == null || results.length < 1) {
				Toast.makeText(getApplicationContext(), "Sorry, zero items found", Toast.LENGTH_LONG).show();
				return;
			}
			
			ArrayList<OpenPath> files = new ArrayList<OpenPath>();
			for(String s : results)
				files.add(new OpenFile(s));
			
			Toast.makeText(getActivity(), "Unimplemented", Toast.LENGTH_LONG).show();
			
		} else if(type == EventHandler.UNZIPTO_TYPE && results != null) {
			String name = new OpenFile(results[0]).getName();
			
			getClipboard().add(new OpenFile(results[0]));
			getExplorer().updateTitle("Holding " + name);
			
		} else {
			Logger.LogDebug("Worker thread complete (" + type + ")?");
			if(!mPath.requiresThread() || FileManager.hasOpenCache(mPath.getAbsolutePath()))
				try {
					if(mPath.requiresThread())
						mPath = FileManager.getOpenCache(mPath.getPath());
					updateData(mPath.list());
				} catch (IOException e) {
					Logger.LogWarning("Couldn't update data after thread completion", e);
				}
			else {
				//if(mProgressBarLoading == null) mProgressBarLoading = getView().findViewById(R.id.content_progress);
				new NetworkIOTask(this).execute(mPath);
			}
			
			//changePath(mPath, false);
			if(mContentAdapter != null)
				mContentAdapter.notifyDataSetChanged();
			
			refreshData(null, false);
			//changePath(getManager().peekStack(), false);
		}
		setProgressVisibility(false);
	}
	
	@Override
	public void onWorkerProgressUpdate(int pos, int total) {
		setProgressVisibility(pos < total);
	}
	
	private void saveTopPath()
	{
		if(mGrid == null) return;
		mTopIndex = mGrid.getFirstVisiblePosition();
		if(mContentAdapter != null && mTopIndex > -1 && mTopIndex < mContentAdapter.getCount())
		{
			mTopPath = (OpenPath)mContentAdapter.getItem(mTopIndex);
			Logger.LogInfo("Top Path saved to " + mTopIndex + (mTopPath != null ? " :: " + mTopPath.getName() : ""));
		}
	}
	
	private Boolean restoreTopPath()
	{
		if(mTopPath != null) 
		{
			Logger.LogDebug("Looking for top path (" + mTopPath.getName() + ")");
			for(int i = 0; i < mContentAdapter.getCount(); i++)
				if(((OpenPath)mContentAdapter.getItem(i)).getName().equals(mTopPath.getName()))
					return restoreTopPath(i);
		}
		if(mTopIndex > 0)
		{
			Logger.LogDebug("Falling back to top index (" + mTopIndex + ")");
			return restoreTopPath(mTopIndex);
		} else return true;
	}
	private Boolean restoreTopPath(int index)
	{
		Logger.LogInfo("Top Path restored to " + index);
		mGrid.setSelection(index);
		mTopIndex = 0;
		mTopPath = null;
		return true;
	}
	
	public void onFoldersFirstChanged(boolean first)
	{
		setSorting(getSorting().setFoldersFirst(first));
		refreshData(null, false);
	}
	public void onHiddenFilesChanged()
	{
		onHiddenChanged(!getShowHiddenFiles());
	}
	//@Override
	public void onHiddenFilesChanged(boolean toShow)
	{
		Logger.LogInfo("onHiddenFilesChanged(" + toShow + ")");
		saveTopPath();
		setSorting(getSorting().setShowHiddenFiles(toShow));
		//getManager().setShowHiddenFiles(state);
		refreshData(null, false);
	}

	public void onThumbnailChanged() { 
		onThumbnailChanged(!getShowThumbnails());
	}
	//@Override
	public void onThumbnailChanged(boolean state) {
		saveTopPath();
		setShowThumbnails(state);
		refreshData(null, false);
	}
	
	//@Override
	public void onSortingChanged(SortType type) {
		setSorting(type);
		//getManager().setSorting(type);
		refreshData(null, false);
	}
	
	public void setSorting(SortType type)
	{
		SortType old = type;
		if(getContentAdapter() != null)
			old = getContentAdapter().getSorting();
		old.setType(type.getType());
		getContentAdapter().setSorting(old);
		setViewSetting(mPath, "sort", type.toString());
	}
	
	public void setShowThumbnails(boolean thumbs)
	{
		mContentAdapter.mShowThumbnails = thumbs;
		setViewSetting(mPath, "thumbs", thumbs);
	}
	
	public void setSettings(SortType sort, boolean thumbs, boolean hidden)
	{
		setSorting(sort);
		setShowThumbnails(thumbs);
		setSorting(getSorting().setShowHiddenFiles(hidden));
		
		refreshData(null, false);
	}

	//@Override
	public void onViewChanged(int state) {
		setViewMode(state);
		//getExplorer().setViewMode(state);
		
		View v = getView();
		if(v != null)
		{
			//if(mPathView == null)
			//	mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
			if(mGrid == null)
				mGrid = (GridView)v.findViewById(R.id.content_grid);
			/*if(mMultiSelectView == null)
				mMultiSelectView = (GridView)v.findViewById(R.id.multiselect_path);
			if(mMultiSelectView != null)
				setupMultiSelectView();*/
		}

		if(mGrid == null)
			Logger.LogError("WTF, where is it?");
		else updateGridView();
			//refreshData(null);
	}
	
	
	public void setProgressVisibility(boolean visible)
	{
		//if(mProgressBarLoading == null && mGrid != null && mGrid.getParent() != null) mProgressBarLoading = ((View)mGrid.getParent()).findViewById(R.id.content_progress);
		//if(mProgressBarLoading != null && mData.length == 0) mProgressBarLoading.setVisibility(visible ? View.VISIBLE : View.GONE);
		if(getExplorer() != null)
			getExplorer().setProgressVisibility(visible);
	}
	
	public SortType getSorting() {
		return mContentAdapter != null ? mContentAdapter.getSorting() : SortType.ALPHA;
	}
	public boolean getFoldersFirst() { return getSorting().foldersFirst(); }
	public boolean getShowHiddenFiles() { return getSorting().showHidden(); }
	public boolean getShowThumbnails() {
		return mContentAdapter != null ? mContentAdapter.mShowThumbnails : true;
	}
	@Override
	public CharSequence getTitle() {
		if(mPath == null)
			return "???";
		return mPath.getName() + ((mPath instanceof OpenFile || mPath instanceof OpenNetworkPath) && mPath.isDirectory() && !mPath.getName().endsWith("/") ? "/" : "");
	}
	
	@Override
	public OpenPath getPath() {
		return mPath;
	}
	
	@Override
	public Drawable getIcon() {
		if(isDetached()) return null;
		if(getActivity() != null && getResources() != null)
			return getResources().getDrawable(ThumbnailCreator.getDefaultResourceId(getPath(), 96, 96));
		return null;
	}
	@Override
	public void updateData(final OpenPath[] result) {
		if(mContentAdapter == null) return;
		if(Thread.currentThread().equals(OpenExplorer.UiThread))
			mContentAdapter.updateData(result);
		else mGrid.post(new Runnable(){public void run(){mContentAdapter.updateData(result);}});
		//notifyDataSetChanged();
	}
	@Override
	public void addFiles(OpenPath[] files) {
		for(OpenPath f : files)
			mContentAdapter.add(f);
	}
	
}



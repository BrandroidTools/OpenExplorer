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
import org.brandroid.openmanager.activities.FolderPickerActivity;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.IconContextMenu.IconContextItemSelectedListener;
import org.brandroid.openmanager.adapters.OpenPathAdapter;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenClipboard;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.fragments.DialogHandler.OnSearchFileSelected;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.EventHandler.OnWorkerThreadFinishedListener;
import org.brandroid.openmanager.util.FileManager.SortType;
import org.brandroid.openmanager.util.ThumbnailStruct;
import org.brandroid.openmanager.util.ThumbnailTask;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.net.Uri;

public class ContentFragment extends OpenFragment implements OnItemClickListener,
															OnWorkerThreadFinishedListener{
	
	public static final boolean USE_ACTIONMODE = false;
	
	//private static MultiSelectHandler mMultiSelect;
	//private LinearLayout mPathView;
	//private SlidingDrawer mMultiSelectDrawer;
	//private GridView mMultiSelectView;
	private GridView mGrid = null;
	private View mProgressBarLoading = null;
	
	private OpenPath[] mData; 
	private ArrayList<OpenPath> mData2 = null; //the data that is bound to our array adapter.
	private Context mContext;
	private BaseAdapter mContentAdapter;
	private ActionMode mActionMode = null;
	private boolean mActionModeSelected;
	private boolean mShowThumbnails = true;
	private boolean mReadyToUpdate = true;
	private boolean mShowHiddenFiles = false;
	private SortType mSorting = SortType.ALPHA;
	private int mMenuContextItemIndex = -1;
	private int mListScrollingState = 0;
	private int mListVisibleStartIndex = 0;
	private int mListVisibleLength = 0; 
	private int mListScrollY = 0;
	public static float DP_RATIO = 1;
	public static int mGridImageSize = 128;
	public static int mListImageSize = 36;
	public Boolean mShowLongDate = false;
	
	private Bundle mBundle;
	
	private int mViewMode = OpenExplorer.VIEW_GRID;
	
	private static Hashtable<OpenPath, ContentFragment> instances = new Hashtable<OpenPath, ContentFragment>();

	public ContentFragment() {
		
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
	public static ContentFragment getInstance(OpenPath path, int mode)
	{
		ContentFragment ret = new ContentFragment(path, mode);
		Bundle args = new Bundle();
		args.putString("last", path.getPath());
		args.putInt("view", mode);
		ret.setArguments(args);
		//Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() + ", " + mode + ")");
		return ret;
	}
	public static ContentFragment getInstance(OpenPath path)
	{
		if(instances == null)
			instances = new Hashtable<OpenPath, ContentFragment>();
		if(path == null)
		{
			Logger.LogWarning("Why is path null?");
			return new ContentFragment();
		}
		if(!instances.containsKey(path))
		{
			ContentFragment ret = new ContentFragment(path);
			Bundle args = new Bundle();
			args.putString("last", path.getPath());
			ret.setArguments(args);
			//return ret;
			instances.put(path, ret);
		}
		//Logger.LogVerbose("ContentFragment.getInstance(" + path.getPath() + ")");
		return instances.get(path);
	}
	
	private void setViewMode(int mode) {
		mViewMode = mode;
		Logger.LogVerbose("Content View Mode: " + mode);
		if(mContentAdapter != null)
		{
			if(FileSystemAdapter.class.equals(mContentAdapter.getClass()))
			{
				mGrid.setAdapter(null);
				mContentAdapter = new FileSystemAdapter(mContext, mViewMode, mData2);
				//mContentAdapter = new OpenPathAdapter(mPath, mode, getExplorer());
				mGrid.setAdapter(mContentAdapter);
			}
		}
	}
	public int getViewMode() {
		if(getExplorer() != null && mPath != null)
			return getExplorer().getSetting(mPath, "view", getGlobalViewMode());
		else return getGlobalViewMode();
	}
	public int getGlobalViewMode() {
		if(getExplorer() != null)
		{
			String pref = getExplorer().getSetting(mPath, "pref_view", "list");
			if(pref == "list")
				return OpenExplorer.VIEW_LIST;
			if(pref == "grid")
				return OpenExplorer.VIEW_GRID;
			if(pref == "carousel")
				return OpenExplorer.VIEW_CAROUSEL;
		}
		return OpenExplorer.VIEW_LIST;
	}
	
	//@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DP_RATIO = getResources().getDimension(R.dimen.one_dp);
		mGridImageSize = (int) (DP_RATIO * getResources().getInteger(R.integer.content_grid_image_size));
		mListImageSize = (int) (DP_RATIO * getResources().getInteger(R.integer.content_list_image_size));
		if(savedInstanceState != null)
			mBundle = savedInstanceState;
		else if(getArguments() != null)
			mBundle = getArguments();
		if(mBundle != null && mBundle.containsKey("last") && (mPath == null || mPath.getPath().equals(mBundle.getString("last"))))
		{
			String last = mBundle.getString("last");
			if(last.startsWith("/"))
				mPath = new OpenFile(last).setRoot();
			else if(last.equalsIgnoreCase("Photos"))
				mPath = OpenExplorer.getPhotoParent();
			else if(last.equalsIgnoreCase("Videos"))
				mPath = OpenExplorer.getVideoParent();
			else if(last.equalsIgnoreCase("Music"))
				mPath = OpenExplorer.getMusicParent();
			else
				mPath = new OpenFile(last);
		}
		if(mBundle != null && mBundle.containsKey("view"))
			mViewMode = mBundle.getInt("view");
		
		if(mPath == null)
			Logger.LogDebug("Creating empty ContentFragment", new Exception("Creating empty ContentFragment"));
		else Logger.LogDebug("Creating ContentFragment @ " + mPath.getPath());
		
		mContext = getActivity().getApplicationContext();
		
		//OpenExplorer.getEventHandler().setOnWorkerThreadFinishedListener(this);
		refreshData(mBundle);
		

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
	@Override
	public void onResume() {
		super.onResume();
	}
	
	public void notifyDataSetChanged() {
		if(mContentAdapter != null)
			mContentAdapter.notifyDataSetChanged();
	}
	public void refreshData(Bundle savedInstanceState) { refreshData(savedInstanceState, true); }
	public void refreshData(Bundle savedInstanceState, boolean allowSkips)
	{
		if(mData2 == null)
			mData2 = new ArrayList<OpenPath>();
		else
			mData2.clear();
		
		OpenPath path = mPath;
		if(path == null)
		{
			if (savedInstanceState != null && savedInstanceState.containsKey("last"))
				path = new OpenFile(savedInstanceState.getString("last"));
			else
				path = new OpenFile(Environment.getExternalStorageDirectory());
		}
		
		if(path instanceof OpenFile && !path.getPath().startsWith("/"))
		{
			if(path.getPath().equals("Photos"))
				path = OpenExplorer.getPhotoParent();
			if(path.getPath().equals("Videos"))
				path = OpenExplorer.getVideoParent();
		}
		mPath = path;
		
		mActionModeSelected = false;
		try {
			mShowThumbnails = getExplorer()
								.getSetting(path, "thumbs", true);
		} catch(NullPointerException npe) {
			mShowThumbnails = true;
		}

		//if(path.getClass().equals(OpenCursor.class) && !OpenExplorer.BEFORE_HONEYCOMB)
		//	mShowThumbnails = true;
		
		if(getActivity() != null && getActivity().getWindow() != null)
			mShowLongDate = getResources().getBoolean(R.bool.show_long_date) //getActivity().getWindow().getWindowManager().getDefaultDisplay().getRotation() % 180 != 0
					&& mPath != null;

		
		if(!path.requiresThread() && (!allowSkips || path.getListLength() < 300))
			try {
				mData = path.list();
			} catch (IOException e) {
				Logger.LogError("Error getting children from FileManager for " + path, e);
			}
		else {
			mData2.clear();
			mData = new OpenPath[0];
			if(mContentAdapter != null)
				mContentAdapter.notifyDataSetChanged();
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
			Logger.LogVerbose("Running FileIOTask");
			new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, path));
		}
		
		//OpenExplorer.setOnSettingsChangeListener(this);
		
			//new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, path));
			updateData(mData, allowSkips);
			
			//if(mGrid != null && savedInstanceState.containsKey("first"))
			
	}

	//@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.content_layout, container, false);
		mGrid = (GridView)v.findViewById(R.id.content_grid);
		if(mProgressBarLoading == null)
			mProgressBarLoading = v.findViewById(R.id.content_progress);
		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);
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
	public void onSaveInstanceState(Bundle outState) {
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
		
		//mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
		//if(mGrid == null)
		mGrid = (GridView)v.findViewById(R.id.content_grid);
		
		if(mProgressBarLoading == null)
			mProgressBarLoading = v.findViewById(R.id.content_progress);
		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);

		if(mGrid == null)
			Logger.LogError("WTF, where are they?");
		else
			updateGridView();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		OpenPath path = mData2.get(mMenuContextItemIndex);
		Logger.LogDebug("Showing context for " + path.getName() + "?");
		return executeMenu(item.getItemId(), path);
		//return super.onContextItemSelected(item);
	}
	
	public void updateGridView()
	{
		int mLayoutID;
		if(mGrid == null)
			mGrid = (GridView)getView().findViewById(R.id.content_grid);
		if(mGrid == null)
		{
			Logger.LogWarning("This shouldn't happen");
			mGrid = (GridView)((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.content_grid, null);
			((ViewGroup)getView()).addView(mGrid);
			setupGridView();
		}
		mViewMode = getViewMode();
		mSorting = FileManager.parseSortType(getExplorer().getSetting(mPath, "sort", getExplorer().getPreferences().getSetting("global", "pref_sorting", mSorting.toString())));
		mShowHiddenFiles = !getExplorer().getSetting(mPath, "hide", getExplorer().getPreferences().getSetting("global", "pref_hiddenFiles", true));
		mShowThumbnails = getExplorer().getSetting(mPath, "thumbs", getExplorer().getPreferences().getSetting("global", "pref_thumbnail", true));
		
		if(!OpenExplorer.BEFORE_HONEYCOMB)
			getExplorer().invalidateOptionsMenu();
		
		//Logger.LogVerbose("Check View Mode: " + mViewMode);
		if(getViewMode() == OpenExplorer.VIEW_GRID) {
			mLayoutID = R.layout.grid_content_layout;
			int iColWidth = getResources().getDimensionPixelSize(R.dimen.grid_width);
			//Logger.LogVerbose("Grid Widths: " + iColWidth + " :: " + getActivity().getWindowManager().getDefaultDisplay().getWidth());
			mGrid.setColumnWidth(iColWidth);
			//mGrid.setNumColumns(getActivity().getWindowManager().getDefaultDisplay().getWidth() / iColWidth);
		} else {
			mLayoutID = R.layout.list_content_layout;
			int iColWidth = getResources().getDimensionPixelSize(R.dimen.list_width);
			//Logger.LogVerbose("List Widths: " + iColWidth + " :: " + getActivity().getWindowManager().getDefaultDisplay().getWidth());
			mGrid.setColumnWidth(iColWidth);
			//mGrid.setNumColumns(GridView.AUTO_FIT);
		}
		if(mGrid == null) return;
		if(mData2 == null)
			mData2 = new ArrayList<OpenPath>();
		//mContentAdapter = new OpenPathAdapter(mPath, getViewMode(), getExplorer());
		//Logger.LogDebug("Setting up grid w/ " + mData2.size() + " items");
		mContentAdapter = new FileSystemAdapter(mContext, mLayoutID, mData2);
		/*
		if(OpenCursor.class.equals(mPath.getClass())) {
			if(mContentAdapter != null && OpenCursorAdapter.class.equals(mContentAdapter.getClass()))
			{
				((OpenCursorAdapter)mContentAdapter)
					.setLayout(mLayoutID)
					.setParent((OpenCursor)mPath);
				//getLoaderManager().initLoader(((OpenCursor)mPath).getCursorType(), null, this);
			}
			else
			{
				mContentAdapter = new OpenCursorAdapter(mContext, (OpenCursor)mPath, 0, (OpenCursor)mPath, mLayoutID);
				mGrid.setAdapter(mContentAdapter);
			}
		} else {
			if(mContentAdapter != null && OpenCursorAdapter.class.equals(mContentAdapter.getClass()))
				((OpenCursorAdapter)mContentAdapter).swapCursor(null);
			mContentAdapter = new OpenArrayAdapter(mContext, mLayoutID, mData2);
		}*/
		mGrid.setAdapter(mContentAdapter);
		mContentAdapter.notifyDataSetChanged();
		setupGridView();
	}
	public void setupGridView()
	{
		//if(mGrid.getTag() == null)
		//	mGrid.setTag(true);
		//else return; // only do the following the first time
		
		mGrid.setSelector(R.drawable.selector_blue);
		mGrid.setDrawSelectorOnTop(true);
		mGrid.setVisibility(View.VISIBLE);
		mGrid.setOnItemClickListener(this);
		mGrid.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mListScrollingState = scrollState;
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
		//mGrid.setOnCreateContextMenuListener(this);
		//if(cm == null)
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			//@Override
			@SuppressWarnings("unused")
			public boolean onItemLongClick(AdapterView<?> list, final View view ,int pos, long id) {
				mMenuContextItemIndex = pos;
				//if(list.showContextMenu()) return true;
				
				final OpenPath file = (OpenPath)((BaseAdapter)list.getAdapter()).getItem(pos);
				final String name = file.getPath().substring(file.getPath().lastIndexOf("/")+1);
				if(OpenExplorer.BEFORE_HONEYCOMB || !USE_ACTIONMODE) {
					
					try {
						View anchor = view; //view.findViewById(R.id.content_context_helper);
						//if(anchor == null) anchor = view;
						//view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
						//Rect r = new Rect(view.getLeft(),view.getTop(),view.getMeasuredWidth(),view.getMeasuredHeight());
						final IconContextMenu cm = new IconContextMenu(
								mContext, R.menu.context_file, view, null, null);
						Menu cmm = cm.getMenu();
						if(getClipboard().size() > 0)
							hideItem(cmm, R.id.menu_context_multi);
						else
							hideItem(cmm, R.id.menu_context_paste);
						if(!name.toLowerCase().endsWith(".zip"))
							hideItem(cmm, R.id.menu_context_unzip);
						cm.setTitle(name);
						cm.setOnDismissListener(new android.widget.PopupWindow.OnDismissListener() {
							public void onDismiss() {
								//view.refreshDrawableState();
							}
						});
						cm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {	
							public void onIconContextItemSelected(MenuItem item, Object info, View view) {
								executeMenu(item.getItemId(), mData2.get((Integer)info));
								cm.dismiss();
							}
						});
						cm.setInfo(pos);
						cm.show(); //r.left, r.top);
					} catch(Exception e) {
						Logger.LogWarning("Couldn't show Iconified menu.", e);
						return list.showContextMenu();
					}
					
					return true;
				}
				
				if(!OpenExplorer.BEFORE_HONEYCOMB)
				{
					if(!file.isDirectory() && mActionMode == null && !getClipboard().isMultiselect()) {
						mActionMode = getActivity().startActionMode(new ActionMode.Callback() {
							//@Override
							public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
								return false;
							}
							
							//@Override
							public void onDestroyActionMode(ActionMode mode) {
								mActionMode = null;
								mActionModeSelected = false;
							}
							
							//@Override
							public boolean onCreateActionMode(ActionMode mode, Menu menu) {
								mode.getMenuInflater().inflate(R.menu.context_file, menu);
					    		
					    		mActionModeSelected = true;
								return true;
							}
	
							//@Override
							public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
								//ArrayList<OpenPath> files = new ArrayList<OpenPath>();
								
								//OpenPath file = mLastPath.getChild(mode.getTitle().toString());
								//files.add(file);
								
								if(item.getItemId() != R.id.menu_context_cut && item.getItemId() != R.id.menu_context_multi && item.getItemId() != R.id.menu_context_copy)
								{
									mode.finish();
									mActionModeSelected = false;
								}
								return executeMenu(item.getItemId(), mode, file);
							}
						});
						mActionMode.setTitle(file.getName());
					}
					
					return true;
				}
				
				if(file.isDirectory() && mActionMode == null && !getClipboard().isMultiselect()) {
					if(!OpenExplorer.BEFORE_HONEYCOMB && USE_ACTIONMODE)
					mActionMode = getActivity().startActionMode(new ActionMode.Callback() {
						
						//@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}
						
						//@Override
						public void onDestroyActionMode(ActionMode mode) {
							mActionMode = null;
							mActionModeSelected = false;
						}
						
						//@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							mode.getMenuInflater().inflate(R.menu.context_file, menu);
							menu.findItem(R.id.menu_context_paste).setEnabled(getClipboard().size() > 0);
							//menu.findItem(R.id.menu_context_unzip).setEnabled(mHoldingZip);
				        	
				        	mActionModeSelected = true;
							
				        	return true;
						}
						
						//@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							return executeMenu(item.getItemId(), mode, file);
						}
					});
					mActionMode.setTitle(file.getName());
					
					return true;
				}
				
				return false;
			}

			private void hideItem(Menu menu, int itemId) {
				if(menu != null && menu.findItem(itemId) != null)
					menu.findItem(itemId).setVisible(false);
			}
		});
		if(OpenExplorer.BEFORE_HONEYCOMB || !USE_ACTIONMODE)
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
	
	private EventHandler getHandler()
	{
		return OpenExplorer.getEventHandler();
	}
	
	private FileManager getManager()
	{
		return OpenExplorer.getFileManager();
	}
	
	private void finishMode(Object mode)
	{
		if(!OpenExplorer.BEFORE_HONEYCOMB && mode != null && mode instanceof ActionMode)
			((ActionMode)mode).finish();
	}
	
	public boolean executeMenu(final int id, OpenPath file)
	{
		return executeMenu(id, null, file);
	}
	public boolean executeMenu(final int id, Object mode, OpenPath file)
	{
		ArrayList<OpenPath> files = new ArrayList<OpenPath>();
		files.add(file);
		return executeMenu(id, mode, file, null);
	}
	public boolean executeMenu(final int id, final Object mode, final OpenPath file, List<OpenPath> fileList)
	{
		final String path = file != null ? file.getPath() : null;
		final OpenPath folder = file != null ? file.getParent() : null;
		String name = file != null ? file.getName() : null;
		if(fileList == null)
			fileList = new ArrayList<OpenPath>();
		final OpenPath[] fileArray = new OpenPath[fileList.size()];
		fileList.toArray(fileArray);
		
		super.onClick(id);
		
		switch(id)
		{
			case R.id.menu_context_view:
				Intent vintent = IntentManager.getIntent(file, getExplorer(), Intent.ACTION_VIEW);
				if(vintent != null)
					getActivity().startActivity(vintent);
				else {
					getExplorer().showToast(R.string.s_error_no_intents);
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
				} else {
					getExplorer().editFile(file);
				}
				break;
			case R.id.menu_context_multi:
				changeMultiSelectState(!getClipboard().isMultiselect());
				getClipboard().add(file);
				return true;
				
			case R.id.menu_multi:
				changeMultiSelectState(!getClipboard().isMultiselect());
				getClipboard().add(file);
				return true;
			case R.id.menu_context_bookmark:
				getExplorer().addBookmark(file);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_delete:
				fileList.add(file);
				getHandler().deleteFile(fileList, getActivity());
				finishMode(mode);
				mContentAdapter.notifyDataSetChanged();
				return true;
				
			case R.id.menu_context_rename:
				getHandler().renameFile(file.getPath(), true, getActivity());
				finishMode(mode);
				return true;
				
			case R.id.menu_context_copy:
			case R.id.menu_context_cut:
				if(id == R.id.menu_context_cut)
					getClipboard().DeleteSource = true;
				else
					getClipboard().DeleteSource = false;

				getClipboard().add(file);
				return false;

			case R.id.menu_context_paste:
			case R.id.menu_paste:
				OpenPath into = file;
				if(!file.isDirectory())
				{
					Logger.LogWarning("Can't paste into file (" + file.getPath() + "). Using parent directory (" + folder.getPath() + ")");
					into = folder;
				}
				OpenClipboard cb = getClipboard();
				if(cb.size() > 0)
					if(cb.DeleteSource)
						getHandler().cutFile(cb, into, getActivity());
					else
						getHandler().copyFile(cb, into, getActivity());
				
				cb.DeleteSource = false;
				if(cb.ClearAfter)
					getClipboard().clear();
				getExplorer().updateTitle(path);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_zip:
				getClipboard().add(file);
				final String def = getClipboard().size() == 1 ?
						file.getName() + ".zip" :
						file.getParent().getName() + ".zip";
				
				final DialogBuilder dZip = new DialogBuilder(mContext);
				dZip
					.setDefaultText(def)
					.setIcon(getResources().getDrawable(R.drawable.lg_zip))
					.setTitle(R.string.s_menu_zip)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok,
						new OnClickListener() {
							public void onClick(DialogInterface di, int which) {
								if(which != DialogInterface.BUTTON_POSITIVE) return;
								OpenPath zipFile = folder.getChild(dZip.getInputText());
								Logger.LogInfo("Zipping " + fileArray.length + " items to " + zipFile.getPath());
								getHandler().zipFile(zipFile, fileArray, getActivity());
								finishMode(mode);
							}
						})
					.setMessage(R.string.s_prompt_zip)
					.create().show();
				return true;
				
			case R.id.menu_context_unzip:
				getHandler().unZipFileTo(getClipboard().get(0), file, getActivity());
				
				getClipboard().clear();
				getExplorer().updateTitle("");
				return true;
			
			case R.id.menu_context_info:
				getExplorer().showFileInfo(file);
				finishMode(mode);
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
		return true;
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(!OpenExplorer.BEFORE_HONEYCOMB && USE_ACTIONMODE) return;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		OpenPath file = mData2.get(info != null ? info.position : mMenuContextItemIndex);
		new MenuInflater(mContext).inflate(R.menu.context_file, menu);
		menu.findItem(R.id.menu_context_paste).setEnabled(getClipboard().size() > 0);
		if(!mPath.isFile() || !IntentManager.isIntentAvailable(mPath, getExplorer()))
		{
			menu.findItem(R.id.menu_context_edit).setVisible(false);
			menu.findItem(R.id.menu_context_view).setVisible(false);
		}
	}
	
	//@Override
	public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
		final OpenPath file = (OpenPath)list.getItemAtPosition(pos);
		
		Logger.LogDebug("File clicked: " + file.getPath());
		
		if(getClipboard().isMultiselect()) {
			if(getClipboard().contains(file))
			{
				getClipboard().remove(file);
				if(getClipboard().size() == 0)
					getClipboard().stopMultiselect();
				mContentAdapter.notifyDataSetChanged();
			} else {
				addToMultiSelect(file);
				((TextView)view.findViewById(R.id.content_text)).setTextAppearance(mContext, R.style.Highlight);
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
			
			if(file.requiresThread())
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
			else
				IntentManager.startIntent(file, getExplorer(), Preferences.Pref_Intents_Internal);
		}
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
	
	private void addToMultiSelect(final OpenPath file)
	{
		getClipboard().add(file);
	}
	private void updateData(final OpenPath[] items) { updateData(items, true); }
	private void updateData(final OpenPath[] items, boolean allowSkips)
	{
		if(items == null) return;
		updateData(items,
				!allowSkips || (items.length < 500),
				!allowSkips || (items.length < 500),
				!allowSkips || (items.length < 500 && mShowHiddenFiles) //getManager().getShowHiddenFiles())
				);
	}
	private void updateData(final OpenPath[] items, final boolean doSort, final boolean foldersFirst, final boolean showHidden) {
		if(!mReadyToUpdate) return;
		if(items == null) return;
		mReadyToUpdate = false;
		
		//new Thread(new Runnable(){public void run() {
				
		if(doSort)
		{
			//Logger.LogVerbose("~Sorting by " + mSorting.toString());
			OpenPath.Sorting = mSorting; //getManager().getSorting();
			Arrays.sort(items);
		}
		
		mData2.clear();
		
		int folder_index = 0;
		if(items != null)
		for(OpenPath f : items)
		{
			if(!showHidden && f.isHidden()) continue;
			if(!f.exists()) continue;
			if(f.isFile() && !(f.length() >= 0)) continue;
			if(foldersFirst && f.isDirectory())
				mData2.add(folder_index++, f);
			else
				mData2.add(f);
		}
		

		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);
		
		//Logger.LogDebug("mData has " + mData2.size());
		if(mContentAdapter != null)
			mContentAdapter.notifyDataSetChanged();
		
		mReadyToUpdate = true;
		
		if(mGrid != null)
			updateGridView();
		/*
		mPathView.removeAllViews();
		mBackPathIndex = 0;	
		 */
		//if(getView() != null) getView().invalidate();
		
		//}}).run();
	}
	
	/*
	 * (non-Javadoc)
	 * this will update the data shown to the user after a change to
	 * the file system has been made from our background thread or EventHandler.
	 */
	//@Override
	public void onWorkerThreadComplete(int type, ArrayList<String> results) {
		
		Logger.LogVerbose("Need to refresh!");
		if(type == EventHandler.SEARCH_TYPE) {
			if(results == null || results.size() < 1) {
				Toast.makeText(mContext, "Sorry, zero items found", Toast.LENGTH_LONG).show();
				return;
			}
			
			DialogHandler dialog = DialogHandler.newDialog(DialogHandler.DialogType.SEARCHRESULT_DIALOG, mContext);
			ArrayList<OpenPath> files = new ArrayList<OpenPath>();
			for(String s : results)
				files.add(new OpenFile(s));
			dialog.setHoldingFileList(files);
			dialog.setOnSearchFileSelected(new OnSearchFileSelected() {
				
				//@Override
				public void onFileSelected(String fileName) {
					OpenPath file = null;
					try {
						file = FileManager.getOpenCache(fileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(file == null)
						file = new OpenFile(fileName);
					
					if (file.isDirectory()) {
						getExplorer().changePath(file, true);
					} else {
						getExplorer().changePath(file.getParent(), true);
					}
				}
			});
			
			dialog.show(getFragmentManager(), "dialog");
			
		} else if(type == EventHandler.UNZIPTO_TYPE && results != null) {
			String name = new OpenFile(results.get(0)).getName();
			
			getClipboard().add(new OpenFile(results.get(0)));
			getExplorer().updateTitle("Holding " + name);
			
		} else {
			Logger.LogDebug("Worker thread complete?");
			if(!mPath.requiresThread())
				try {
					updateData(mPath.list());
				} catch (IOException e) {
					Logger.LogWarning("Couldn't update data after thread completion", e);
				}
			else {
				if(mProgressBarLoading == null)
					mProgressBarLoading = getView().findViewById(R.id.content_progress);
				new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, mPath));
			}
			
			//changePath(mPath, false);
			if(mContentAdapter != null)
				mContentAdapter.notifyDataSetChanged();
			
			refreshData(null, false);
			//changePath(getManager().peekStack(), false);
		}
	}
	
	/**
	 * Only to be used to change the path after creation
	 * @param path
	 */
	public void changePath(OpenPath path) {
		mPath = path;
		refreshData(null, false);
	}
	
	//@Override
	public void onHiddenFilesChanged(boolean toShow)
	{
		setShowHiddenFiles(!toShow);
		//getManager().setShowHiddenFiles(state);
		refreshData(null, false);
	}

	//@Override
	public void onThumbnailChanged(boolean state) {
		setShowThumbnails(state);
		refreshData(null);
	}
	
	//@Override
	public void onSortingChanged(SortType type) {
		setSorting(type);
		//getManager().setSorting(type);
		refreshData(null, false);
	}
	public void setShowHiddenFiles(boolean hide)
	{
		mShowHiddenFiles = !hide;
		getExplorer().setSetting(mPath, "hide", hide);
	}
	
	public void setSorting(SortType type)
	{
		mSorting = type;
		getExplorer().setSetting(mPath, "sort", type.toString());
	}
	
	public void setShowThumbnails(boolean thumbs)
	{
		mShowThumbnails = thumbs;
		getExplorer().setSetting(mPath, "thumbs", thumbs);
	}
	
	public void setSettings(SortType sort, boolean thumbs, boolean hidden)
	{
		setSorting(sort);
		setShowThumbnails(thumbs);
		setShowHiddenFiles(hidden);
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
	}
			
	public void changeMultiSelectState(boolean multiSelectOn) {
		if(multiSelectOn)
			getClipboard().startMultiselect();
		else
			getClipboard().stopMultiselect();
		//mMultiSelectDrawer.setVisibility(multiSelectOn ? View.VISIBLE : View.GONE);
	}
	
	
	
	/**
	 * 
	 */
	public class FileSystemAdapter extends ArrayAdapter<OpenPath> {
		private final int KB = 1024;
    	private final int MG = KB * KB;
    	private final int GB = MG * KB;
    	
		public FileSystemAdapter(Context context, int layout, ArrayList<OpenPath> data) {
			super(context, layout, data);
		}
		
		@Override
		public void notifyDataSetChanged() {
			//Logger.LogDebug("Data set changed for FileSystemAdapter - Size = " + getCount() + ". (" + mPath.getPath() + ")");
			try {
				//if(getManager() != null)
				//	getExplorer().updateTitle(getManager().peekStack().getPath());
				super.notifyDataSetChanged();
			} catch(NullPointerException npe) {
				Logger.LogError("Null found while notifying data change.", npe);
			}
		}
		
		private final Handler handler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message msg) {
				notifyDataSetChanged();
				return true;
			}
		});
		
		////@Override
		public View getView(int position, View view, ViewGroup parent)
		{
			final OpenPath file = super.getItem(position);
			final String mName = file.getName();
			
			int mWidth = mListImageSize;
			int mHeight = mWidth;
			if(getViewMode() == OpenExplorer.VIEW_GRID)
				mWidth = mHeight = mGridImageSize;
			
			int mode = getViewMode() == OpenExplorer.VIEW_GRID ?
					R.layout.grid_content_layout : R.layout.list_content_layout;
			
			if(view == null
						//|| view.getTag() == null
						//|| !BookmarkHolder.class.equals(view.getTag())
						//|| ((BookmarkHolder)view.getTag()).getMode() != mode
						) {
				LayoutInflater in = (LayoutInflater)mContext
										.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				view = in.inflate(mode, parent, false);
				//mHolder = new BookmarkHolder(file, mName, view, mode);
				//view.setTag(mHolder);
				//file.setTag(mHolder);
			} //else mHolder = (BookmarkHolder)view.getTag();

			//mHolder.getIconView().measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			//Logger.LogVerbose("Content Icon Size: " + mHolder.getIconView().getMeasuredWidth() + "x" + mHolder.getIconView().getMeasuredHeight());

			//view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//mHolder.setInfo(getFileDetails(file, false));
			TextView mInfo = (TextView)view.findViewById(R.id.content_info);
			if(mInfo != null)
				mInfo.setText(getFileDetails(file, false));
			
			TextView mPathView = (TextView)view.findViewById(R.id.content_fullpath); 
			if(mPathView != null)
			{
				if(file.getClass().equals(OpenMediaStore.class))
				{
					mPathView.setText(file.getPath());
					mPathView.setVisibility(View.VISIBLE);
					//mHolder.setPath(file.getPath());
					//mHolder.showPath(true);
				}
				else
					mPathView.setVisibility(View.GONE);
					//mHolder.showPath(false);
			}
			
			TextView mNameView = (TextView)view.findViewById(R.id.content_text);
			if(mNameView != null)
				mNameView.setText(mName);

			if(getClipboard().contains(file))
				mNameView.setTextAppearance(mContext, R.style.Highlight);
			else
				mNameView.setTextAppearance(mContext,  R.style.Large);
			
			if(file.isHidden())
				setAlpha(0.5f, mNameView, mPathView, mInfo);
			else
				setAlpha(1.0f, mNameView, mPathView, mInfo);
							
			//if(!mHolder.getTitle().equals(mName))
			//	mHolder.setTitle(mName);
			ImageView mIcon = (ImageView)view.findViewById(R.id.content_icon);
			//RemoteImageView mIcon = (RemoteImageView)view.findViewById(R.id.content_icon);
			
			if(mIcon != null)
			{
				if(file.isHidden())
					mIcon.setAlpha(100);
				else
					mIcon.setAlpha(255);
				if(file.isTextFile())
					mIcon.setImageBitmap(ThumbnailCreator.getFileExtIcon(file.getExtension(), mContext, mWidth > 72));
				else if(!mShowThumbnails||!file.hasThumbnail())
				{
					if(file.isDirectory())
					{
						boolean bCountHidden = !getExplorer().getSetting(file, "hide", true);
						try {
							if(file.getChildCount(bCountHidden) > 0)
								mIcon.setImageDrawable(getResources().getDrawable(mWidth > 72 ? R.drawable.lg_folder_full : R.drawable.sm_folder_full));
							else
								mIcon.setImageDrawable(getResources().getDrawable(mWidth > 72 ? R.drawable.lg_folder : R.drawable.sm_folder));
						} catch (Exception e) {
							mIcon.setImageDrawable(getResources().getDrawable(ThumbnailCreator.getDefaultResourceId(file, mWidth, mHeight)));
						}
					} else mIcon.setImageDrawable(getResources().getDrawable(ThumbnailCreator.getDefaultResourceId(file, mWidth, mHeight)));					
				} else {
					ThumbnailCreator.setThumbnail(mIcon, file, mWidth, mHeight);
				}
			}
			
			return view;
		}
		
		private String getFilePath(String name) {
			return getManager().peekStack().getChild(name).getPath();
		}
		private String getFileDetails(OpenPath file, Boolean longDate) {
			//OpenPath file = getManager().peekStack().getChild(name); 
			String deets = ""; //file.getPath() + "\t\t";
			
			if(file.isDirectory() && !file.requiresThread()) {
				try {
					boolean bCountHidden = !getExplorer().getSetting(file, "hide", true);
					deets = file.getChildCount(bCountHidden) + " " + getString(R.string.s_files) + " | ";
					//deets = file.list().length + " items";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(file.isFile()) {
				deets = DialogHandler.formatSize(file.length()) + " | ";
			}
			
			DateFormat df = new SimpleDateFormat(longDate ? "MM-dd-yyyy HH:mm" : "MM-dd-yy");
			deets += df.format(file.lastModified());
			
			/*
			
			deets += " | ";
			
			deets += (file.isDirectory()?"d":"-");
			deets += (file.canRead()?"r":"-");
			deets += (file.canWrite()?"w":"-");
			deets += (file.canExecute()?"x":"-");
			
			*/
			
			return deets;
		}
		
	}
	
	public enum FileIOCommandType
	{
		ALL
	}
	public class FileIOCommand
	{
		public FileIOCommandType Type;
		public OpenPath Path;
		
		public FileIOCommand(FileIOCommandType type, OpenPath path)
		{
			Type = type;
			Path = path;
		}
	}
	
	public class FileIOTask extends AsyncTask<FileIOCommand, Integer, OpenPath[]>
	{
		@Override
		protected OpenPath[] doInBackground(FileIOCommand... params) {
			publishProgress(0);
			ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
			for(FileIOCommand cmd : params)
			{
				if(cmd.Path.requiresThread())
				{
					OpenFTP file = null;
					try {
						file = (OpenFTP)FileManager.getOpenCache(cmd.Path.getAbsolutePath(), true);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					OpenPath[] list = null;
					try {
						if(file != null)
							list = file.list();
					} catch (IOException e) {
						list = null;
					}
					if(list != null) {
						for(OpenPath f : list)
							ret.add(f);
					} else {
						getExplorer().showToast(R.string.s_error_ftp);
					}
				} else {
					try {
						for(OpenPath f : cmd.Path.list())
							ret.add(f);
					} catch (IOException e) {
						Logger.LogError("IOException listing children inside FileIOTask", e);
					}
				}
				if(OpenFTP.class.equals(cmd.Path.getClass()))
					((OpenFTP)cmd.Path).getManager().disconnect();
				getManager().pushStack(cmd.Path);
			}
			Logger.LogDebug("Found " + ret.size() + " items.");
			OpenPath[] ret2 = new OpenPath[ret.size()];
			ret.toArray(ret2);
			return ret2;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//mData.clear();
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
			else Logger.LogDebug("Starting FileIOTask");
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(OpenPath[] result)
		{
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.GONE);
			else Logger.LogDebug("Ending FileIOTask");
			//mData2.clear();
			updateData(result);
		}
		
	}
	public class DialogBuilder extends Builder
	{
		private View view;
		private EditText mEdit, mEdit2;
		
		public DialogBuilder(Context mContext) {
			super(mContext);
			
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.input_dialog_layout, null);
			super.setView(view);
			mEdit = (EditText)view.findViewById(R.id.dialog_input);
			mEdit2 = (EditText)view.findViewById(R.id.dialog_input_top);
			if(mEdit == null)
				mEdit = mEdit2;
		}
		
		public String getInputText() { return mEdit.getText().toString(); }
		public DialogBuilder setPrompt(String s) {
			((EditText)view.findViewById(R.id.dialog_message_top)).setText(s);
			return this;
		}
		
		public DialogBuilder setDefaultText(String s) 
		{
			mEdit.setText(s);
			return this;
		}
		
		@Override
		public DialogBuilder setMessage(CharSequence message) {
			super.setMessage(message);
			return this;
		}
		
		@Override
		public DialogBuilder setTitle(CharSequence title) {
			super.setTitle(title);
			return this;
		}
		
		@Override
		public DialogBuilder setIcon(Drawable icon) {
			super.setIcon(icon);
			return this;
		}
	}
	public SortType getSorting() {
		// TODO Auto-generated method stub
		return mSorting;
	}
	public boolean getShowHiddenFiles() {
		return mShowHiddenFiles;
	}
	public boolean getShowThumbnails() {
		return mShowThumbnails;
	}
}



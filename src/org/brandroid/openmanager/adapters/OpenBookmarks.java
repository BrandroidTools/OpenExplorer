package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.commons.net.ftp.FTPFile;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.OpenFragmentActivity;
import org.brandroid.openmanager.interfaces.OpenContextProvider;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenCommand;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenCursor.UpdateBookmarkTextListener;
import org.brandroid.openmanager.data.FTPManager;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPathArray;
import org.brandroid.openmanager.data.OpenSCP;
import org.brandroid.openmanager.data.OpenSFTP;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.data.OpenSmartFolder.SmartSearch;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.DFInfo;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.InputDialog;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.SimpleUserInfo;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.OpenInterfaces.OnBookMarkChangeListener;
import org.brandroid.openmanager.util.ThumbnailCreator.OnUpdateImageListener;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class OpenBookmarks implements OnBookMarkChangeListener,
								OnGroupClickListener,
								OnChildClickListener,
								OnItemLongClickListener
{
	private Map<Integer, ArrayList<OpenPath>> mBookmarksArray;
	private Hashtable<String, String> mTitles = new Hashtable<String, String>();
	//private ImageView mLastIndicater = null;
	private BookmarkAdapter mBookmarkAdapter;
	private String mBookmarkString;
	private Boolean mHasExternal = false, mHasInternal = false;
	private Boolean mShowTitles = true;
	private Long mAllDataSize = 0l;
	private Long mLargestDataSize = 0l;
	private SharedPreferences mPrefs;
	private final OpenContextProvider mContextHelper;
	public static final int BOOKMARK_DRIVE = 0;
	public static final int BOOKMARK_SMART_FOLDER = 1;
	public static final int BOOKMARK_FAVORITE = 2;
	public static final int BOOKMARK_SERVER = 3;
	public static final int BOOKMARK_OFFLINE = 4;
	
	public OpenBookmarks(OpenContextProvider explorer, ExpandableListView newList)
	{
		mContextHelper = explorer;
		mBookmarksArray = new Hashtable<Integer, ArrayList<OpenPath>>();
		//for(BookmarkType type : BookmarkType.values())
		//	mBookmarksArray.put(getTypeInteger(type), new ArrayList<OpenPath>());
		mPrefs = new Preferences(getContext()).getPreferences("bookmarks"); 
		if(mBookmarkString == null)
			mBookmarkString = mPrefs.getString("bookmarks", "");
		if(newList != null)
			setupListView(newList);
		if(mContextHelper != null)
			scanBookmarks();
	}
	
	public enum BookmarkType
	{
		BOOKMARK_DRIVE,
		BOOKMARK_SMART_FOLDER,
		BOOKMARK_FAVORITE,
		BOOKMARK_SERVER
		//,BOOKMARK_OFFLINE
	}
	
	private int getTypeInteger(BookmarkType type)
	{
		int ret = -1;
		for(BookmarkType item : BookmarkType.values())
		{
			ret++;
			if(type.equals(item))
				break;
		}
		return ret;
	}
	
	public int size()
	{
		return mBookmarksArray.size();
	}
	private Context getContext() { return mContextHelper.getContext(); }
	
	public void scanBookmarks()
	{
		scanBookmarksInner();
		/*
		new Thread(new Runnable() {
			@Override
			public void run() {
				scanBookmarksInner();
			}
		}).start();
		*/
	}
	/**
	 * 
	 */
	private void scanBookmarksInner()
	{
		Logger.LogDebug("Scanning bookmarks...");
		OpenFile storage = new OpenFile(Environment.getExternalStorageDirectory());
		//mBookmarksArray.clear();
		clearBookmarks();
		
		if(checkAndAdd(BookmarkType.BOOKMARK_DRIVE, OpenFile.getExternalMemoryDrive(false)))
			mHasInternal = true;
		if(checkAndAdd(BookmarkType.BOOKMARK_DRIVE, OpenFile.getInternalMemoryDrive()))
			mHasExternal = true;
		
		checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getVideoParent());
		checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getPhotoParent());
		checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getMusicParent());
		if(mContextHelper.getSetting(null, "pref_show_downloads", true))
			checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, OpenExplorer.getDownloadParent());
		
		checkAndAdd(BookmarkType.BOOKMARK_DRIVE, new OpenFile("/").setRoot());
		
		checkAndAdd(BookmarkType.BOOKMARK_DRIVE, storage.setRoot());
		
		//checkAndAdd(BookmarkType.BOOKMARK_SMART_FOLDER, storage.getChild("Download"));
		Hashtable<String, DFInfo> df = DFInfo.LoadDF(true);
		mAllDataSize = 0l;
		for(String sItem : df.keySet())
		{
			if(sItem.toLowerCase().startsWith("/dev")) continue;
			if(sItem.toLowerCase().indexOf("/system") > -1) continue;
			if(sItem.toLowerCase().indexOf("vendor") > -1) continue;
			OpenFile file = new OpenFile(sItem);
			if(file.isHidden()) continue;
			//Logger.LogInfo("DF: " + )
			if(file.getTotalSpace() > 0)
			{
				mAllDataSize += file.getTotalSpace();
				mLargestDataSize = Math.max(mLargestDataSize, file.getTotalSpace());
			}
			//if(!file.getFile().canWrite()) continue;
			//if(sItem.toLowerCase().indexOf("asec") > -1) continue;
			checkAndAdd(BookmarkType.BOOKMARK_DRIVE, file.setRoot());
		}
		if (mBookmarkString.length() > 0) {
			String[] l = mBookmarkString.split(";");
			
			for(String s : l)
				checkAndAdd(BookmarkType.BOOKMARK_FAVORITE, new OpenFile(s));
		}
		
		OpenServers servers = SettingsActivity.LoadDefaultServers(getContext());
		for(int i = 0; i < servers.size(); i++)
		{
			OpenServer server = servers.get(i);
			SimpleUserInfo info = new SimpleUserInfo();
			info.setPassword(server.getPassword());
			OpenNetworkPath onp = null;
			if(server.getType().equalsIgnoreCase("ftp"))
			{
				onp = new OpenFTP(null, new FTPFile(),
						new FTPManager(server.getHost(),
								server.getUser(),
								server.getPassword(),
								server.getPath()));
			} else if(server.getType().equalsIgnoreCase("scp")) {
				onp = new OpenSCP(server.getHost(), server.getUser(), server.getPath(), info);
			} else if(server.getType().equalsIgnoreCase("sftp"))
			{
				onp = new OpenSFTP(
						server.getHost(), server.getUser(), server.getPath());
			} else if(server.getType().equalsIgnoreCase("smb"))
			{
				try {
					onp = new OpenSMB(
							new SmbFile("smb://" + server.getHost() + "/" + server.getPath(),
								new NtlmPasswordAuthentication(
									server.getUser().indexOf("/") > -1 ? server.getUser().substring(0, server.getUser().indexOf("/")) : "",
									server.getUser(),
									server.getPassword())
							));
				} catch (MalformedURLException e) {
					Logger.LogError("Couldn't add Samba share to bookmarks.", e);
					continue;
				}
			} else continue;
			if(onp == null) continue;
			onp.setServersIndex(i);
			onp.setName(server.getName());
			onp.setUserInfo(info);
			if(server.getPort() > 0)
				onp.setPort(server.getPort());
			checkAndAdd(BookmarkType.BOOKMARK_SERVER, onp);
		}
		addBookmark(BookmarkType.BOOKMARK_SERVER, new OpenCommand(mContextHelper.getString(R.string.s_pref_server_add), OpenCommand.COMMAND_ADD_SERVER, android.R.drawable.ic_menu_add));
		if(mBookmarkAdapter != null)
			mBookmarkAdapter.notifyDataSetChanged();
	}
	
	public void saveBookmarks()
	{
		setSetting("bookmarks", mBookmarkString);
	}
	

	public String getSetting(String key, String defValue)
	{
		return mPrefs.getString(key, defValue);
	}
	public Boolean getSetting(String key, Boolean defValue)
	{
		return mPrefs.getBoolean(key, defValue);
	}
	public void setSetting(String key, String value)
	{
		mPrefs.edit().putString(key, value).commit();
	}
	public void setSetting(String key, Boolean value)
	{
		mPrefs.edit().putBoolean(key, value).commit();
	}
	

	public boolean hasBookmark(OpenPath path)
	{
		if(path == null) return true;
		if(path.getPath() == null) return false;
		for(ArrayList<OpenPath> arr : mBookmarksArray.values())
			for(OpenPath p : arr)
				if(p.getPath() != null && p.getPath().replaceAll("/", "").equals(path.getPath().replaceAll("/", "")))
					return true;
		return false;
	}
	
	public void addBookmark(BookmarkType type, OpenPath path)
	{
		int iType = getTypeInteger(type);
		ArrayList<OpenPath> paths = new ArrayList<OpenPath>();
		if(mBookmarksArray.containsKey(iType))
			paths = mBookmarksArray.get(iType);
		if(!paths.contains(paths))
		{
			paths.add(path);
			mBookmarksArray.put(iType, paths);
			if(mBookmarkAdapter != null)
				mBookmarkAdapter.notifyDataSetChanged();
		}
	}
	public void addBookmark(BookmarkType type, OpenPath path, int index)
	{
		int iType = getTypeInteger(type);
		ArrayList<OpenPath> paths = new ArrayList<OpenPath>();
		if(mBookmarksArray.containsKey(iType))
			paths = mBookmarksArray.get(iType);
		if(!paths.contains(path))
		{
			paths.add(Math.max(paths.size() - 1, index), path);
			mBookmarksArray.put(iType, paths);
			if(mBookmarkAdapter != null)
				mBookmarkAdapter.notifyDataSetChanged();
		}
	}
	
	public void refresh()
	{
		if(mBookmarkAdapter != null)
			mBookmarkAdapter.notifyDataSetChanged();
	}
	
	private void clearBookmarks()
	{
		for(int i=0; i < BookmarkType.values().length; i++)
			mBookmarksArray.put(i, new ArrayList<OpenPath>());
	}

	public String getPathTitle(OpenPath path)
	{
		return getSetting("title_" + path.getPath(), getPathTitleDefault(path));
	}
	
	public void setPathTitle(OpenPath path, String title)
	{
		setSetting("title_" + path.getAbsolutePath(), title);
	}
	public String getPathTitleDefault(OpenPath file)
	{
		if(file.getDepth() > 4) return file.getName();
		if(file instanceof OpenCursor || file instanceof OpenMediaStore) return file.getName();
		String path = file.getPath().toLowerCase();
		if(path.equals("/"))
			return "/";
		else if(path.indexOf("ext") > -1)
			return mContextHelper.getString(R.string.s_external);
		else if(path.indexOf("download") > -1)
			return mContextHelper.getString(R.string.s_downloads);
		else if(path.indexOf("sdcard") > -1)
			return mContextHelper.getString(mHasExternal ? R.string.s_internal : R.string.s_external);
		else if(path.indexOf("usb") > -1 || path.indexOf("/media") > -1 || path.indexOf("removeable") > -1)
		{
			try {
				return OpenExplorer.getVolumeName(file.getPath());
			} catch(Exception e) {
				Logger.LogWarning("Unable to get actual volume name.", e);
			}
		}
		
		return file.getName();
	}
	
	private boolean checkPrefs(BookmarkType type, OpenPath path)
	{
		if(path.getPath().equals("/"))
			return mContextHelper.getSetting(null, "pref_show_root", false);
		else if(OpenFile.getInternalMemoryDrive().equals(path))
			return mContextHelper.getSetting(null, "pref_show_internal", true);
		else if(OpenFile.getExternalMemoryDrive(true).equals(path))
			return mContextHelper.getSetting(null, "pref_show_external", true);
		else if(type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Videos"))
			return mContextHelper.getSetting(null, "pref_show_videos", true);
		else if(type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Photos"))
			return mContextHelper.getSetting(null, "pref_show_photos", true);
		else if(type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Music"))
			return mContextHelper.getSetting(null, "pref_show_music", true);
		else if(type == BookmarkType.BOOKMARK_SMART_FOLDER && path.getPath().equals("Downloads"))
			return mContextHelper.getSetting(null, "pref_show_downloads", true);
		else
			return !mContextHelper.getPreferences().getSetting("bookmarks", "hide_" + path.getPath(), false);
	}
	private boolean checkAndAdd(BookmarkType type, OpenPath path)
	{
		if(path == null) return false;
		boolean bypassHide = false; //mExplorer.getPreferences().getSetting("global", "pref_hide", false);
		try {
			if(path instanceof OpenSmartFolder)
				bypassHide = true;
			if(!bypassHide && !checkPrefs(type, path))
				return false;
		} catch(NullPointerException e) { }
		if(hasBookmark(path)) return false;
		if(path instanceof OpenFile)
		{
			if(((OpenFile)path).isRemoveable())
				if(((OpenFile) path).getUsableSpace() <= 0)
					return false;
		}
		if(path instanceof OpenCursor ||
				path instanceof OpenNetworkPath ||
				path instanceof OpenSmartFolder ||
				path.exists())
		{
			addBookmark(type, path);
			return true;
		}
		return false;
	}
	
	public void hideTitles()
	{
		mShowTitles = false;
		refresh();
	}
	
	public void showTitles()
	{
		mShowTitles = true;
		refresh();
	}
	

	public void setupListView(ExpandableListView lv)
	{
		Logger.LogDebug("Setting up ListView in OpenBookmarks");
		lv.setDrawSelectorOnTop(true);
		lv.setSelector(R.drawable.selector_blue);
		lv.setOnChildClickListener(this);
		lv.setOnGroupClickListener(this);
		lv.setGroupIndicator(null);
		lv.setOnItemLongClickListener(this);
		lv.setLongClickable(true);
		//lv.setOnItemClickListener(this);
		//lv.setBackgroundResource(R.drawable.listgradback);
		
		//Logger.LogDebug(mBookmarks.size() + " bookmarks");
		
		//registerForContextMenu(lv);
		
		if(mBookmarkAdapter == null)
			mBookmarkAdapter = new BookmarkAdapter();
			//mBookmarkAdapter = new BookmarkAdapter(mExplorer, R.layout.bookmark_layout, mBookmarksArray);
		lv.setAdapter(mBookmarkAdapter);
		
		OpenExplorer.setOnBookMarkAddListener(this);
		
	}
	
	private void handleCommand(int command)
	{
		switch(command)
		{
			case OpenCommand.COMMAND_ADD_SERVER:
				DialogHandler.showServerDialog(mContextHelper, new OpenFTP((OpenFTP)null, null, null), null, true);
				break;
		}
	}

	public boolean onItemLongClick(AdapterView<?> list, View v, int pos, long id) {
		Logger.LogDebug("Long Click pos: " + pos + " (" + id + "," + v.getTag() + "!)");
		return onLongClick(v);
	}
	
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		OpenPath path = mBookmarkAdapter.getChild(groupPosition, childPosition);
		if(path != null)
		{
			if(path instanceof OpenCommand)
				handleCommand(((OpenCommand)path).getCommand());
			else
				mContextHelper.onChangeLocation(path);
			return true;
		}
		return false;
	}

	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		//if(mBookmarksArray.get(groupPosition).size() > 0)
		//	return false;
		//else return true; // don't allow expand of empty groups
		return false;
	}
	
	
	/*
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3)
	{
		mExplorer.onChangeLocation(mBookmarksArray.get(pos));
	}
	public void onListItemClick(ListView list, View view, int pos, long id) {
		//super.onItemClick(list, view, pos, id);
		mExplorer.onChangeLocation(mBookmarksArray.get(pos));
	}
	*/

	public void onBookMarkAdd(OpenPath path) {
		int type = getTypeInteger(BookmarkType.BOOKMARK_FAVORITE);
		if(mBookmarksArray == null)
			mBookmarksArray = new Hashtable<Integer, ArrayList<OpenPath>>();
		if(mBookmarksArray.get(type) == null)
			mBookmarksArray.put(type, new ArrayList<OpenPath>());
		mBookmarksArray.get(type).add(path);
		mBookmarkString = (mBookmarkString != null && mBookmarkString != "" ? mBookmarkString + ";" : "") + path.getPath();
		mBookmarkAdapter.notifyDataSetChanged();
	}

	public boolean showStandardDialog(final OpenPath mPath, final BookmarkHolder mHolder)
	{
		int removeId = R.string.s_remove;
		if(mHolder != null && mHolder.isEjectable())
			removeId = R.string.s_eject;
		else if(mPath.getPath().equals("/")
			|| mPath.equals(OpenFile.getExternalMemoryDrive(false))
			|| mPath.equals(OpenFile.getInternalMemoryDrive()))
			removeId = R.string.s_hide;
		else if(mPath instanceof OpenMediaStore || mPath instanceof OpenCursor)
			removeId = R.string.s_hide;
		final int idRemove = removeId;
		
		final View v = mHolder != null ? mHolder.getView() : new View(getContext());
		
		final InputDialog builder = new InputDialog(getContext())
			.setTitle(R.string.s_title_bookmark_prefix)
			.setIcon(mHolder != null ? mHolder.getIcon(getContext()) : null)
			.setDefaultText(getPathTitle(mPath))
			.setMessage(R.string.s_alert_bookmark_rename)
			.setNeutralButton(removeId, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(mPath.getPath().equals("/"))
						mContextHelper.getPreferences().setSetting("global", "pref_show_root", false);
					else if(mPath.equals(OpenFile.getInternalMemoryDrive()))
						mContextHelper.getPreferences().setSetting("global", "pref_show_internal", false);
					else if(mPath.equals(OpenFile.getExternalMemoryDrive(true)))
						mContextHelper.getPreferences().setSetting("global", "pref_show_external", false);
					else if(mPath instanceof OpenMediaStore)
						mContextHelper.getPreferences().setSetting("global", "pref_show_" + mPath.getPath().toLowerCase(), false);
					else if(idRemove == R.string.s_eject)
						tryEject(mPath.getPath(), mHolder);
					else {
						setSetting("hide_" + mPath.getAbsolutePath(), true);
						if(mBookmarkString != null && (";"+mBookmarkString+";").indexOf(mPath.getPath()) > -1)
							mBookmarkString = (";" + mBookmarkString + ";").replace(";" + mPath.getPath() + ";", ";").replaceAll("^;|;$", "");
						if(Build.VERSION.SDK_INT >= 12)
							v.animate().alpha(0).setDuration(200).setListener(new org.brandroid.openmanager.adapters.AnimatorEndListener(){
								public void onAnimationEnd(Animator animation) {
									scanBookmarks();
								}});
						else
							v.setVisibility(View.GONE);
					}
					scanBookmarks();
				}
			})
			.setNegativeButton(R.string.s_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}});
		builder
			.setPositiveButton(R.string.s_update, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setPathTitle(mPath, builder.getInputText().toString());					
					mBookmarkAdapter.notifyDataSetChanged();
				}
			})
			.create();
		try {
			builder.show();
		} catch(BadTokenException e) {
			Logger.LogWarning("Couldn't show AlertDialog. Bad token?", e);
		}
		return true;
	}

	
	protected void tryEject(String sPath, BookmarkHolder mHolder) {
		final View viewf = mHolder.getView();
		if(RootManager.tryExecute("umount " + sPath))
		{
			mContextHelper.showToast(R.string.s_alert_remove_safe);
			if(Build.VERSION.SDK_INT >= 12)
				viewf.animate().setDuration(500).y(viewf.getY() - viewf.getHeight()).alpha(0)
					.setListener(new org.brandroid.openmanager.adapters.AnimatorEndListener(){
						public void onAnimationEnd(Animator animation) {
							scanBookmarks();
						}});
		} else
			mContextHelper.showToast(R.string.s_alert_remove_error);
	}

	public String getBookMarkNameString() {
		return mBookmarkString;
	}

	public void updateSizeIndicator(OpenPath mFile, View mParentView)
	{
		View mSizeView = (View)mParentView.findViewById(R.id.size_layout);
		View size_bar = mParentView.findViewById(R.id.size_bar);
		TextView mSizeText = (TextView)mParentView.findViewById(R.id.size_text);
		if(size_bar == null) return;
		long size = 0l;
		long free = 0l;
		long total = 0l;
		try {
			if(mFile instanceof OpenSMB)
			{
				total = size = ((OpenSMB)mFile).getDiskSpace();
				free = ((OpenSMB)mFile).getDiskFreeSpace();
			}
		} catch (Exception e) {
			Logger.LogError("Couldn't get SMB size.", e);
			return;
		}
		int total_width = size_bar.getWidth();
		if(total_width == 0)
			total_width = mParentView.getWidth();
		if(total_width == 0 && mParentView.getRootView().findViewById(R.id.list_frag) != null)
			total_width = mParentView.getRootView().findViewById(R.id.list_frag).getWidth();
		if(total_width == 0)
			total_width = getContext().getResources().getDimensionPixelSize(R.dimen.bookmarks_width);
		if(mFile != null && mFile.getClass().equals(OpenFile.class) && mFile.getPath().indexOf("usic") == -1 && mFile.getPath().indexOf("ownload") ==-1)
		{
			OpenFile f = (OpenFile)mFile;
			size = total = f.getTotalSpace();
			free = f.getFreeSpace();
		}
			
		if(size > 0 && free < size)
		{
			String sFree = DialogHandler.formatSize(free, false);
			String sTotal = DialogHandler.formatSize(size);
			//if(sFree.endsWith(sTotal.substring(sTotal.lastIndexOf(" ") + 1)))
			//	sFree = DFInfo.getFriendlySize(free, false);
			mSizeText.setText(sFree + "/" + sTotal);
			mSizeText.setVisibility(View.VISIBLE);
			
			while(size > 100000)
			{
				size /= 10;
				free /= 10;
			}
			float total_percent = ((float)total / (float)Math.max(total,mLargestDataSize));
			total_percent = Math.min(20, total_percent);
			int percent_width = (int) (total_percent * total_width);
			//Logger.LogInfo("Size Total: " + mLargestDataSize + " This: " + total + " Percent: " + total_percent + " Width: " + percent_width + " / " + total_width);
			if(size_bar instanceof ProgressBar)
			{
				ProgressBar bar = (ProgressBar)size_bar;
				bar.setMax((int)size);
				bar.setProgress((int)(size - free));
				if(bar.getProgress() == 0)
					bar.setVisibility(View.GONE);
				else if(percent_width > 0) {
					bar.setVisibility(View.VISIBLE);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)bar.getLayoutParams();
					//lp.rightMargin = total_width - percent_width;
					lp.width = percent_width;
					//bar.setLayoutParams(lp);
					bar.requestLayout();
				}
				size_bar.setTag(true);
			} else {
				long taken = Math.min(0, size - free);
				float percent = (float)taken / (float)size;
				//mParentView.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				int size_width = total_width; //mParentView.getMeasuredWidth();
				Logger.LogVerbose("Parent Width: " + size_width);
				size_width = Math.min(0, (int) (percent * size_width));
				size_bar.getBackground().setBounds(0,0,size_width,0);
				size_bar.setTag(true);
			}
				
			if(size_bar.getTag() == null) size_bar.setVisibility(View.GONE);
		} else if(mFile != null && OpenCursor.class.equals(mFile.getClass())) {
			//bar.setVisibility(View.INVISIBLE);
			if(size_bar.getTag() == null) size_bar.setVisibility(View.GONE);
			mSizeText.setText(DialogHandler.formatSize(((OpenCursor)mFile).getTotalSize()));
		} else mSizeView.setVisibility(View.GONE);
	}
	
	
	private class BookmarkAdapter extends BaseExpandableListAdapter
	{
		public OpenPath getChild(int group, int pos) {
			return mBookmarksArray.get(group).get(pos);
		}

		public long getChildId(int group, int pos) {
			return pos;
		}

		public View getChildView(int group, int pos,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View ret = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.bookmark_layout, null); //convertView;;
			OpenPath path = getChild(group, pos);
			if(path instanceof OpenCursor && path.getListLength() == 0)
			{
				ret = new View(getContext());
				ret.setLayoutParams(new AbsListView.LayoutParams(0, 0));
				ret.setFocusable(false);
				ret.setVisibility(View.GONE);
				return ret;
			}
			BookmarkHolder mHolder = null;
			mHolder = new BookmarkHolder(path, getPathTitle(path), ret, 0);
			ret.setTag(mHolder);
		
			final TextView mCountText = (TextView)ret.findViewById(R.id.content_count);
			final ImageView mIcon = (ImageView)ret.findViewById(R.id.bookmark_icon);
			
			if(mCountText != null)
			{
				if(path instanceof OpenSmartFolder)
				{
					mCountText.setVisibility(View.VISIBLE);
					mCountText.setText("(" + path.length() + ")");
				}
				if(path instanceof OpenCursor)
				{
					final OpenCursor oc = (OpenCursor)path;
					oc.setUpdateBookmarkTextListener(new UpdateBookmarkTextListener() {
						public void updateBookmarkCount(final int count) {
							mCountText.post(new Runnable() {
								public void run() {
									mIcon.setImageResource(ThumbnailCreator.getDefaultResourceId(oc, 36, 36));
									if(count > 0)
									{
										String txt = " (" + count + ")";
										if(!mCountText.getText().toString().equals(txt))
										{
											mCountText.setText(txt);
											mCountText.setVisibility(View.VISIBLE);
										}
									} else {
										mCountText.setVisibility(View.GONE);
									}
								}
							});
						}
					});
					new Thread(new Runnable(){public void run(){
						oc.refresh();
					}}).start();
				} else mCountText.setVisibility(View.GONE);
			}
				
			if(group == BOOKMARK_DRIVE || path instanceof OpenSMB)
				updateSizeIndicator(path, ret);
			else 
				ViewUtils.setViewsVisible(ret, false, R.id.size_layout);
			
			ViewUtils.setText(ret, getPathTitle(path), R.id.content_text);
			
			if(group == BOOKMARK_FAVORITE)
			{
				Drawable d = mIcon.getResources().getDrawable(
						ThumbnailCreator.getDefaultResourceId(path, 36, 36));
				LayerDrawable ld = new LayerDrawable(new Drawable[]{d,
						mIcon.getResources().getDrawable(R.drawable.ic_favorites)
					});
				mIcon.setImageDrawable(ld);
			} else //if(!(path instanceof OpenCursor))
				ThumbnailCreator.setThumbnail(mIcon, path, 36, 36,
					new OnUpdateImageListener() {
						public void updateImage(Bitmap b) {
							mIcon.setImageBitmap(b);
						}
					});
			
            return ret;
		}

		public int getChildrenCount(int group) {
			if(mBookmarksArray.containsKey(group))
				return mBookmarksArray.get(group).size();
			else return 0;
		}

		public ArrayList<OpenPath> getGroup(int group) {
			return mBookmarksArray.get(group);
		}

		public int getGroupCount() {
			return mBookmarksArray.size();
		}

		public long getGroupId(int group) {
			return group;
		}

		public View getGroupView(int group, boolean isExpanded,
				final View convertView, ViewGroup parent) {
			View ret = convertView;
			if(ret == null)
			{
				ret = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.preference_category_holo, null);
						//R.layout.bookmark_group, null);
			}
			Button button1 = (Button)ret.findViewById(android.R.id.button1);
			if((group == BOOKMARK_FAVORITE || group == BOOKMARK_SERVER || group == BOOKMARK_SMART_FOLDER)
					&& button1 != null)
			{
				button1.setVisibility(View.VISIBLE);
			} else if(button1 != null) {
				button1.setVisibility(View.GONE);
			}
			TextView mText = (TextView)ret.findViewById(android.R.id.title);
			if(isExpanded)
				mText.setTypeface(Typeface.DEFAULT_BOLD);
			else
				mText.setTypeface(Typeface.DEFAULT);
			mText.setBackgroundDrawable(null);
			
			//ret.setBackgroundColor(android.R.color.background_dark);
			//mText.setTextColor(android.R.color.secondary_text_light);
			
			String[] groups = getContext().getResources().getStringArray(R.array.bookmark_groups);
			if(mText != null)
				mText.setText(groups[group] + (getChildrenCount(group) > 0 ? " (" + (getChildrenCount(group)  - (group == BOOKMARK_SERVER ? 1 : 0)) + ")" : ""));
			return ret;
		}

		public boolean hasStableIds() {
			return false;
		}

		public boolean isChildSelectable(int group, int pos) {
			return true;
		}

		
	}

	public BookmarkAdapter getListAdapter() {
		return mBookmarkAdapter;
	}

	public boolean onLongClick(View v) {
		if(v.getTag() == null) {
			//((ExpandableListAdapter)list.getAdapter()).get
			Logger.LogWarning("No tag set on long click in OpenBookmarks.");
			return false;
		}
		Logger.LogInfo("Long click detected in OpenBookmarks");
		return onLongClick((BookmarkHolder)v.getTag());
	}
	public boolean onLongClick(BookmarkHolder h)
	{
		OpenPath path = h.getOpenPath();
		if(path instanceof OpenCommand)
			handleCommand(((OpenCommand)path).getCommand());
		else if(path instanceof OpenNetworkPath)
			DialogHandler.showServerDialog(mContextHelper, (OpenNetworkPath)path, h, false);
		else
			showStandardDialog(path, h);
		return true;
	}

}

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

package org.brandroid.openmanager.activities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTP.OnFTPCommunicationListener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SearchViewCompat;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.ServerMessageBlock;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFile.OnSMBCommunicationListener;
import jcifs.smb.SmbNamedPipe;
import jcifs.smb.SmbSession;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.ArrayPagerAdapter;
import org.brandroid.openmanager.adapters.OpenBookmarks;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.adapters.ArrayPagerAdapter.OnPageTitleClickListener;
import org.brandroid.openmanager.adapters.IconContextMenu.IconContextItemSelectedListener;
import org.brandroid.openmanager.adapters.OpenClipboard.OnClipboardUpdateListener;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.IconContextMenuAdapter;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPathArray;
import org.brandroid.openmanager.data.OpenSFTP;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.data.OpenSmartFolder.SmartSearch;
import org.brandroid.openmanager.fragments.CarouselFragment;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.fragments.LogViewerFragment;
import org.brandroid.openmanager.fragments.OpenFragment;
import org.brandroid.openmanager.fragments.OperationsFragment;
import org.brandroid.openmanager.fragments.OpenFragment.OnFragmentDPADListener;
import org.brandroid.openmanager.fragments.OpenFragment.OnFragmentTitleLongClickListener;
import org.brandroid.openmanager.fragments.OpenFragment.Poppable;
import org.brandroid.openmanager.fragments.OpenPathFragmentInterface;
import org.brandroid.openmanager.fragments.PreferenceFragmentV11;
import org.brandroid.openmanager.fragments.SearchResultsFragment;
import org.brandroid.openmanager.fragments.TextEditorFragment;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.EventHandler.EventType;
import org.brandroid.openmanager.util.EventHandler.OnWorkerUpdateListener;
import org.brandroid.openmanager.util.MimeTypes;
import org.brandroid.openmanager.util.OpenInterfaces.OnBookMarkChangeListener;
import org.brandroid.openmanager.util.MimeTypeParser;
import org.brandroid.openmanager.util.OpenInterfaces;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.openmanager.util.SimpleHostKeyRepo;
import org.brandroid.openmanager.util.SimpleUserInfo;
import org.brandroid.openmanager.util.SimpleUserInfo.UserInfoInteractionCallback;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.ThumbnailCreator.OnUpdateImageListener;
import org.brandroid.openmanager.views.OpenPathList;
import org.brandroid.openmanager.views.OpenViewPager;
import org.brandroid.utils.CustomExceptionHandler;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LoggerDbAdapter;
import org.brandroid.utils.MenuBuilder;
import org.brandroid.utils.MenuItemImpl;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.SubmitStatsTask;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TabPageIndicator.TabView;
import org.xmlpull.v1.XmlPullParserException;

public class OpenExplorer
		extends OpenFragmentActivity
		implements OnBackStackChangedListener, OnClipboardUpdateListener,
			OnWorkerUpdateListener,
			OnPageTitleClickListener, LoaderCallbacks<Cursor>, OnPageChangeListener,
			OpenApp, IconContextItemSelectedListener, OnKeyListener,
			OnFragmentDPADListener, OnFocusChangeListener
	{

	public static final int REQ_PREFERENCES = 6;
	public static final int REQ_SPLASH = 7;
	public static final int REQ_INTENT = 8;
	public static final int REQ_SAVE_FILE = 9;
	public static final int REQ_PICK_FOLDER = 10;
	public static final int REQUEST_VIEW = 11;
	public static final int RESULT_RESTART_NEEDED = 12;
	public static final int VIEW_LIST = 0;
	public static final int VIEW_GRID = 1;
	public static final int VIEW_CAROUSEL = 2;
	
	public static final boolean BEFORE_HONEYCOMB = Build.VERSION.SDK_INT < 11;
	public static boolean CAN_DO_CAROUSEL = false;
	public static boolean USE_ACTION_BAR = false;
	public static boolean USE_ACTIONMODE = false;
	public static boolean USE_SPLIT_ACTION_BAR = true;
	public static boolean IS_DEBUG_BUILD = true;
	public static boolean LOW_MEMORY = false;
	public static final boolean SHOW_FILE_DETAILS = false;
	public static boolean USE_PRETTY_MENUS = true;
	public static boolean USE_PRETTY_CONTEXT_MENUS = true;
	public static boolean IS_FULL_SCREEN = false;
	public static boolean IS_KEYBOARD_AVAILABLE = false;
	
	private final static boolean DEBUG = IS_DEBUG_BUILD && false;
	
	public static int SCREEN_WIDTH = -1;
	public static int SCREEN_HEIGHT = -1;
	public static int SCREEN_DPI = -1;
	public static int VERSION = 160;

	public static SparseArray<MenuItem> mMenuShortcuts;
	
	private static MimeTypes mMimeTypes;
	private Object mActionMode;
	private int mLastBackIndex = -1;
	private static long lastSubmit = 0l;
	private OpenPath mLastPath = null;
	private BroadcastReceiver storageReceiver = null;
	private Handler mHandler = new Handler();  // handler for the main thread
	//private int mViewMode = VIEW_LIST;
	//private static long mLastCursorEnsure = 0;
	private static boolean mRunningCursorEnsure = false;
	private Boolean mSinglePane = false;
	private Boolean mStateReady = true;
	private String mLastMenuClass = "";
	private long lastInvalidate = 0l;
	private int mLastClipSize = -1;
	private boolean mLastClipState = false;
	//private ActionBarHelper mActionBarHelper = null;
	
	private static LogViewerFragment mLogFragment = null;
	private static OperationsFragment mOpsFragment = null;
	private static boolean mLogViewEnabled = true;
	private OpenViewPager mViewPager;
	private static ArrayPagerAdapter mViewPagerAdapter;
	private static final boolean mViewPagerEnabled = true; 
	private ExpandableListView mBookmarksList;
	private OpenBookmarks mBookmarks;
	private BetterPopupWindow mBookmarksPopup;
	private static OnBookMarkChangeListener mBookmarkListener;
	protected MenuBuilder mMainMenu = null, mOptsMenu = null;
	private ViewGroup mToolbarButtons = null;
	private ViewGroup mStaticButtons = null;
	private View mSearchView = null;
	private int mTitleButtons = 0;
	private static ActionBar mBar = null;
	
	private static boolean bRetrieveDimensionsForPhotos = Build.VERSION.SDK_INT >= 10;
	private static boolean bRetrieveExtraVideoDetails = Build.VERSION.SDK_INT > 8;
	private static boolean bRetrieveCursorFiles = Build.VERSION.SDK_INT > 10;
	
	private static final FileManager mFileManager = new FileManager();
	private static final EventHandler mEvHandler = new EventHandler(mFileManager);
	
	public FragmentManager fragmentManager;
	
	private final static OpenCursor
			mPhotoParent = new OpenCursor("Photos", MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
			mVideoParent = new OpenCursor("Videos", MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
			mMusicParent = new OpenCursor("Music", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI),
			mApkParent = new OpenCursor("Apps", BEFORE_HONEYCOMB ? Uri.fromFile(OpenFile.getExternalMemoryDrive(true).getFile()) : MediaStore.Files.getContentUri("/mnt"));
	private final static OpenSmartFolder
			mDownloadParent = new OpenSmartFolder("Downloads");

	
	public boolean isViewPagerEnabled() { return mViewPagerEnabled; }
	
	private void loadPreferences()
	{
		Preferences prefs = getPreferences();

		//mViewPagerEnabled = prefs.getBoolean("global", "pref_pagers", true);
		//USE_ACTIONMODE = getPreferences().getBoolean("global", "pref_actionmode", false);
		
		Preferences.Pref_Intents_Internal = prefs.getBoolean("global", "pref_intent_internal", true);
		Preferences.Pref_Text_Internal = prefs.getBoolean("global", "pref_text_internal", true);
		Preferences.Pref_Zip_Internal = prefs.getBoolean("global", "pref_zip_internal", true);
		Preferences.Pref_ShowUp = prefs.getBoolean("global", "pref_showup", false);
		Preferences.Pref_Language = prefs.getString("global", "pref_language", "");
		
		if(!Preferences.Pref_Language.equals(""))
			setLanguage(getContext(), Preferences.Pref_Language);
		
		USE_PRETTY_MENUS = prefs.getBoolean("global", "pref_fancy_menus", Build.VERSION.SDK_INT < 14);
		USE_PRETTY_CONTEXT_MENUS = prefs.getBoolean("global", "pref_fancy_context", true);
		
		String s = prefs.getString("global", "pref_location_ext", null);
		if(s != null && new OpenFile(s).exists())
			OpenFile.setExternalMemoryDrive(new OpenFile(s));
		else prefs.setSetting("global", "pref_location_ext", OpenFile.getExternalMemoryDrive(true).getPath());
		
		s = prefs.getString("global", "pref_location_int", null);
		if(s != null && new OpenFile(s).exists())
			OpenFile.setInternalMemoryDrive(new OpenFile(s));
		else prefs.setSetting("global", "pref_location_int", OpenFile.getInternalMemoryDrive().getPath());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		onClipboardUpdate();
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		
		if(getPreferences().getBoolean("global", "pref_fullscreen", false))
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								 WindowManager.LayoutParams.FLAG_FULLSCREEN);
			IS_FULL_SCREEN = true;
		} //else getWindow().addFlags(WindowManager.LayoutParams.FLAG
		else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			IS_FULL_SCREEN = false;
		}
		
		IS_KEYBOARD_AVAILABLE = getContext().getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY;
		
		loadPreferences();
		
		if(getPreferences().getBoolean("global", "pref_hardware_accel", true) && !BEFORE_HONEYCOMB)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		//mActionBarHelper = ActionBarHelper.createInstance(this);
		//mActionBarHelper.onCreate(savedInstanceState);
		if(BEFORE_HONEYCOMB)
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			USE_ACTION_BAR = false;
		//} else if(isGTV()) {
		//	USE_ACTION_BAR = false;
		//	mBar = (LeftNavBarService.instance()).getLeftNavBar(this);
		} else if(!BEFORE_HONEYCOMB) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR);
			USE_ACTION_BAR = true;
			mBar = getActionBar();
		}
		if(mBar != null)
		{
			if(Build.VERSION.SDK_INT >= 14)
				mBar.setHomeButtonEnabled(true);
			mBar.setDisplayUseLogoEnabled(true);
			try {
				mBar.setCustomView(R.layout.title_bar);
				mBar.setDisplayShowCustomEnabled(true);
				mBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
				//ViewGroup cv = (ViewGroup)ab.getCustomView();
				//if(cv.findViewById(R.id.title_paste) != null)
				//	cv.removeView(cv.findViewById(R.id.title_paste));
				//ab.getCustomView().findViewById(R.id.title_icon).setVisibility(View.GONE);
			} catch(InflateException e) {
				Logger.LogWarning("Couldn't set up ActionBar custom view", e);
			}
		} else USE_ACTION_BAR = false;
		
		OpenFile.setTempFileRoot(new OpenFile(getFilesDir()).getChild("temp"));
		setupLoggingDb();
		handleExceptionHandler();
		getMimeTypes();
		setupFilesDb();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_fragments);
		getWindow().setBackgroundDrawableResource(R.drawable.background_holo_dark);
		
		try {
			upgradeViewSettings();
		} catch(Exception e) { }
		//try {
			showWarnings();
		//} catch(Exception e) { }
		
		mEvHandler.setUpdateListener(this);
		
		getClipboard().setClipboardUpdateListener(this);
		
		try {
			/*
			Signature[] sigs = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES).signatures;
			for(Signature sig : sigs)
				if(sig.toCharsString().indexOf("4465627567") > -1) // check for "Debug" in signature
					IS_DEBUG_BUILD = true;
			*/
			if(IS_DEBUG_BUILD)
				IS_DEBUG_BUILD = (getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA)
					.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) ==
						ApplicationInfo.FLAG_DEBUGGABLE;
			if(isBlackBerry())
				IS_DEBUG_BUILD = false;
		} catch (NameNotFoundException e1) { }
		
		handleNetworking();
		refreshCursors();

		checkWelcome();
		
		checkRoot();
		
		if(!BEFORE_HONEYCOMB)
		{
			boolean show_underline = true;
			if(Build.VERSION.SDK_INT < 14)
				show_underline = isGTV();
			else if(getResources().getBoolean(R.bool.large)) // ICS+ tablets
				show_underline = false;
			if(Build.VERSION.SDK_INT > 13 && !getResources().getBoolean(R.bool.large))
				show_underline = true;
			
			View tu = findViewById(R.id.title_underline);
			if(tu != null && !show_underline)
			{
				getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_shadow));
				tu.setVisibility(View.GONE);
			}
			
			//if(USE_ACTION_BAR)
			//	setViewVisibility(false, false, R.id.title_bar, R.id.title_underline, R.id.title_underline_2);
		}
		if(BEFORE_HONEYCOMB || !USE_ACTION_BAR)
			ViewUtils.inflateView(this, R.id.title_stub);
		if(BEFORE_HONEYCOMB)
			ViewUtils.inflateView(this, R.id.base_stub);
		setViewVisibility(false, false, R.id.title_paste, R.id.title_ops, R.id.title_log);
		setOnClicks(
				R.id.title_ops, //R.id.menu_global_ops_icon, R.id.menu_global_ops_text,
				R.id.title_log, R.id.title_icon, R.id.menu_more,
				R.id.title_paste_icon
				//,R.id.title_sort, R.id.title_view, R.id.title_up
				);
		checkTitleSeparator();
		IconContextMenu.clearInstances();
		
		if(findViewById(R.id.list_frag) == null)
			mSinglePane = true;
		else if(findViewById(R.id.list_frag).getVisibility() == View.GONE)
			mSinglePane = true;

		Logger.LogDebug("Looking for path");
		OpenPath path = mLastPath;
		if(savedInstanceState == null || path == null)
		{	
			String start = getPreferences().getString("global", "pref_start", "External");
	
			if(savedInstanceState != null && savedInstanceState.containsKey("last") && !savedInstanceState.getString("last").equals(""))
				start = savedInstanceState.getString("last");

			path = FileManager.getOpenCache(start, this);
		}
		
		if(path == null)
			path = OpenFile.getExternalMemoryDrive(true);
		
		if(FileManager.checkForNoMedia(path))
			showToast(R.string.s_error_no_media, Toast.LENGTH_LONG);
		
		mLastPath = path;
		
		boolean bAddToStack = true;

		if(findViewById(R.id.content_pager_frame_stub) != null)
			((ViewStub)findViewById(R.id.content_pager_frame_stub)).inflate();
		
		Logger.LogDebug("Pager inflated");
		
		if(fragmentManager == null)
		{
			fragmentManager = getSupportFragmentManager();
			fragmentManager.addOnBackStackChangedListener(this);
		}
		
		mLogFragment = new LogViewerFragment();

		FragmentTransaction ft = fragmentManager.beginTransaction();

		Logger.LogDebug("Creating with " + path.getPath());
		if(path instanceof OpenFile)
			new PeekAtGrandKidsTask().execute((OpenFile)path);

		initPager();
		if(handleIntent(getIntent()))
		{
			path = mLastPath = null;
			bAddToStack = false;
		}
		
		if(mViewPager != null && mViewPagerAdapter != null && path != null)
		{
			//mViewPagerAdapter.add(mContentFragment);
			mLastPath = null;
			changePath(path, bAddToStack, true);
			setCurrentItem(mViewPagerAdapter.getCount() - 1, false);
			restoreOpenedEditors();
		} else Logger.LogWarning("Nothing to show?!");

		ft.commit();

		invalidateOptionsMenu();
		initBookmarkDropdown();
		
		handleMediaReceiver();

		if(!getPreferences().getBoolean("global", "pref_splash", false))
			showSplashIntent(this, getPreferences().getString("global", "pref_start", "Internal"));
	}
	
	private void checkWelcome()
	{
		try {
			if(isBlackBerry() && !getPreferences().getBoolean("global", "welcome_bb", false))
			{
				showToast("Welcome, PlayBook user!");
				getPreferences().setSetting("global", "welcome_bb", true);
			}
		} catch(Exception e) { }
		try {
			if(isGTV())
			{
				//IS_DEBUG_BUILD = false;
				//CAN_DO_CAROUSEL = true;
				if(!getPreferences().getBoolean("global", "welcome", false))
				{
					showToast("Welcome, GoogleTV user!");
					getPreferences().setSetting("global", "welcome", true);
				}
			}
		} catch(Exception e) { Logger.LogWarning("Couldn't check for GTV", e); }
	}
	
	private void checkRoot()
	{
		try {
			if(getPreferences().getSetting("global", "pref_root", false)
					&& (RootManager.Default == null || !RootManager.Default.isRoot()))
				requestRoot();
			else if(RootManager.Default != null)
				exitRoot();
		} catch(Exception e) { Logger.LogWarning("Couldn't get root.", e); }
	}
	
	private void requestRoot()
	{
		//new Thread(new Runnable(){public void run(){
			RootManager.Default.requestRoot();
		//}}).start();
	}
	
	private void exitRoot()
	{
		if(RootManager.Default == null) return;
		new Thread(new Runnable(){public void run(){
			RootManager.Default.exitRoot();
		}}).start();
	}
	
	private void handleNetworking()
	{
		FileManager.DefaultUserInfo = new SimpleUserInfo();
		final Context c = this;
		Preferences.Warn_Networking = getPreferences().getSetting("warn", "networking", false);
		SimpleUserInfo.setInteractionCallback(new UserInfoInteractionCallback() {
			
			public boolean promptPassword(String message) {
				String mPassword = null;
				View view = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.prompt_password, null);
				TextView tv = (TextView)view.findViewById(android.R.id.message);
				tv.setText(message);
				final EditText text1 = ((EditText)view.findViewById(android.R.id.text1));
				final CheckBox checkpw = (CheckBox)view.findViewById(android.R.id.checkbox);
				checkpw.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
					{
						text1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
						text1.setTransformationMethod(new SingleLineTransformationMethod());
					} else {
						text1.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
						text1.setTransformationMethod(new PasswordTransformationMethod());
					}
				}});
				
				AlertDialog dlg = new AlertDialog.Builder(c)
					.setTitle(R.string.s_prompt_password)
					.setView(view)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String mPassword = text1.getText().toString();
							onPasswordEntered(mPassword);
							onYesNoAnswered(true);
						}
					})
					.setNegativeButton(android.R.string.no, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							onYesNoAnswered(false);
						}
					})
					.create();
				dlg.show();
				return true;
			}
			
			@Override
			public void onYesNoAnswered(boolean yes) {
			}
			
			@Override
			public void onPasswordEntered(String password) {
			}

			@Override
			public boolean promptYesNo(final String message) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AlertDialog dlg = new AlertDialog.Builder(c)
							.setMessage(message)
							.setPositiveButton(android.R.string.yes, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									onYesNoAnswered(true);
								}
							})
							.setNegativeButton(android.R.string.no, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									onYesNoAnswered(false);
								}
							})
							.create();
						dlg.show();
					}
				});
				return true;
			}
		});
		try {
			OpenSFTP.DefaultJSch.setHostKeyRepository(
					new SimpleHostKeyRepo(
						OpenSFTP.DefaultJSch,
						FileManager.DefaultUserInfo,
						Preferences.getPreferences(getApplicationContext(), "hosts")));
			OpenNetworkPath.Timeout = getPreferences().getSetting("global", "server_timeout", 20) * 1000;
		} catch (JSchException e) {
			Logger.LogWarning("Couldn't set Preference-backed Host Key Repository", e);
		}
	}

	private MimeTypes getMimeTypes()
	{
		return getMimeTypes(this);
	}
	
	public static MimeTypes getMimeTypes(Context c)
	{
		if(mMimeTypes != null) return mMimeTypes;
		if(MimeTypes.Default != null) return MimeTypes.Default;
		MimeTypeParser mtp = null;
		try {
			mtp = new MimeTypeParser(c, c.getPackageName());
		} catch (NameNotFoundException e) {
			//Should never happen
		}

		XmlResourceParser in = c.getResources().getXml(R.xml.mimetypes);
		
		try {
			 mMimeTypes = mtp.fromXmlResource(in);
		} catch (XmlPullParserException e) {
			Logger.LogError("Couldn't parse mimeTypes.", e);
			throw new RuntimeException("PreselectedChannelsActivity: XmlPullParserException");
		} catch (IOException e) {
			Logger.LogError("PreselectedChannelsActivity: IOException", e);
			throw new RuntimeException("PreselectedChannelsActivity: IOException");
		}
		MimeTypes.Default = mMimeTypes;
		return mMimeTypes;
	}
	
	private void setCurrentItem(final int page, final boolean smooth)
	{
		try {
			//if(!Thread.currentThread().equals(UiThread))
				mViewPager.post(new Runnable() {
					public void run() {
						if(mViewPager.getCurrentItem() != page)
							mViewPager.setCurrentItem(page, smooth);
					}
				});
			//else if(mViewPager.getCurrentItem() != page)
			//	mViewPager.setCurrentItem(page, smooth);
		} catch(Exception e) {
			Logger.LogError("Couldn't set ViewPager page to " + page, e);
		}
	}

	private void handleExceptionHandler()
	{
		if(Logger.checkWTF())
		{
			if(!getSetting(null, "pref_autowtf", false))
				showWTFIntent();
			//else if(isNetworkConnected())
			//	new SubmitStatsTask(this).execute(
			//			Logger.getCrashReport(true));
		}
	}

	private void showWTFIntent() {
		Intent intent = new Intent(this, WTFSenderActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Logger.LogDebug("New Intent! " + intent.toString());
		setIntent(intent);
		handleIntent(intent);
	}

	public boolean showMenu(int menuId, final View from, final boolean fromTouch)
	{
		if(!BEFORE_HONEYCOMB && MenuUtils.getMenuLookupSub(menuId) > -1) return false;
		Logger.LogInfo("showMenu(0x" + Integer.toHexString(menuId) + "," + (from != null ? from.toString() : "NULL") + ")");
		//if(mMenuPopup == null)
		switch(menuId)
		{
		//case R.menu.content:
		case R.id.menu_more:
			if(showIContextMenu(mMainMenu, from instanceof CheckedTextView ? null : from, fromTouch) != null) return true;
			return true;
		}
		
		if(from != null && !(from instanceof CheckedTextView) && 
				showIContextMenu(menuId, from instanceof CheckedTextView ? null : from, fromTouch) != null)
			return true;
		if(from.showContextMenu()) return true;
		//if(IS_DEBUG_BUILD && BEFORE_HONEYCOMB)
		//	showToast("Invalid option (0x" + Integer.toHexString(menuId) + ")" + (from != null ? " under " + from.toString() + " (" + from.getLeft() + "," + from.getTop() + ")" : ""));
		return false;
	}

	/*
	 * This should only be used with the "main" menu
	 */
	public boolean showMenu(final Menu menu, final View from, final boolean fromTouch)
	{
		if(from != null){
			if(showIContextMenu(menu, from, fromTouch) == null)
			{
				from.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
					public void onCreateContextMenu(ContextMenu cmenu, View v,
							ContextMenuInfo menuInfo) {
						MenuUtils.transferMenu(menu, cmenu, false);
					}
				});
				return from.showContextMenu();
			} else return true;
		} else openOptionsMenu();
		return false;
	}
	
	public IconContextMenu showIContextMenu(int menuId, final View from, final boolean fromTouch)
	{
		if(menuId != R.menu.context_file && !OpenExplorer.USE_PRETTY_MENUS) return null;
		if(menuId == R.menu.context_file && !OpenExplorer.USE_PRETTY_CONTEXT_MENUS) return null;
		try {
			if(menuId == R.id.menu_sort || menuId == R.menu.content_sort)
				menuId = R.menu.content_sort;
			else if(menuId == R.id.menu_view || menuId == R.menu.content_view)
				menuId = R.menu.content_view;
			else if(menuId == R.menu.content)
				menuId = R.menu.content;
			else if(menuId == R.menu.content_sort)
				menuId = R.menu.content_sort;
			else if(menuId == R.menu.content_view)
				menuId = R.menu.content_view;
			else if(menuId == R.menu.text_view)
				menuId = R.menu.text_view;
			else if(menuId == R.menu.content_ops)
				menuId = R.menu.content_ops;
			else if(Utils.getArrayIndex(MenuUtils.MENU_LOOKUP_IDS, menuId) > -1)
				menuId = MenuUtils.getMenuLookupSub(menuId);
			else {
				Logger.LogWarning("Unknown menuId (0x" + Integer.toHexString(menuId) + ")!");
				return null;
			}
			Logger.LogDebug("Trying to show context menu 0x" + Integer.toHexString(menuId) + (from != null ? " under " + from.toString() + " (" + ViewUtils.getAbsoluteLeft(from) + "," + ViewUtils.getAbsoluteTop(from) + ")" : "") + ".");
			if(Utils.getArrayIndex(MenuUtils.MENU_LOOKUP_SUBS, menuId) > -1)
			{
				//IconContextMenu icm1 = new IconContextMenu(getApplicationContext(), menu, from, null, null);
				//MenuBuilder menu = IconContextMenu.newMenu(this, menuId);
				IconContextMenu mOpenMenu = IconContextMenu.getInstance(this, menuId, from);
				onPrepareOptionsMenu(mOpenMenu.getMenu());
				mOpenMenu.setAnchor(from);
				if(menuId == R.menu.context_file)
				{
					mOpenMenu.setNumColumns(2);
					//icm.setPopupWidth(getResources().getDimensionPixelSize(R.dimen.popup_width) / 2);
					mOpenMenu.setTextLayout(R.layout.context_item);
				} else mOpenMenu.setNumColumns(1);
				mOpenMenu.setOnIconContextItemSelectedListener(this);
				/*
				if(menuId == R.menu.menu_sort || menuId == R.menu.content_sort)
					mOpenMenu.setTitle(getString(R.string.s_menu_sort) + " (" + getDirContentFragment(false).getPath().getPath() + ")");
				else if(menuId == R.menu.menu_view || menuId == R.menu.content_view)
					mOpenMenu.setTitle(getString(R.string.s_view) + " (" + getDirContentFragment(false).getPath().getPath() + ")");
				*/
				mOpenMenu.show();
				return mOpenMenu;
			}
		} catch(Exception e) {
			Logger.LogWarning("Couldn't show icon context menu" + (from != null ? " under " + from.toString() + " (" + from.getLeft() + "," + from.getTop() + ")" : "") + ".", e);
			if(from != null)
				return showIContextMenu(menuId, null, fromTouch);
		}
		Logger.LogWarning("Not sure what happend with " + menuId + (from != null ? " under " + from.toString() + " (" + from.getLeft() + "," + from.getTop() + ")" : "") + ".");
		return null;
	}
	public IconContextMenu showIContextMenu(Menu menu, final View from, final boolean fromTouch)
	{
		boolean isContext = menu.findItem(R.id.menu_context_zip) != null && menu.findItem(R.id.menu_context_zip).isVisible();
		return showIContextMenu(menu, from, fromTouch, isContext ? 2 : 1);
	}
	public IconContextMenu showIContextMenu(Menu menu, final View from, final boolean fromTouch, int cols)
	{
		if(menu.findItem(R.id.menu_context_paste) != null)
		{
			if(!OpenExplorer.USE_PRETTY_CONTEXT_MENUS) return null;
		} else if(!OpenExplorer.USE_PRETTY_MENUS) return null;
		
		Logger.LogDebug("Trying to show context menu " + menu.toString() + (from != null ? " under " + from.toString() + " (" + from.getLeft() + "," + from.getTop() + ")" : "") + ".");
		try {
			/*
			if(mToolbarButtons != null)
				for(int i = menu.size() - 1; i >= 0; i--)
				{
					MenuItem item = menu.getItem(i);
					if(mToolbarButtons.findViewById(item.getItemId()) != null)
						menu.removeItemAt(i);
				}*/
			onPrepareOptionsMenu(menu);
			IconContextMenu mOpenMenu = new IconContextMenu(this, menu, from);
			if(cols > 1)
			{
				mOpenMenu.setTextLayout(R.layout.context_item);
				mOpenMenu.setNumColumns(cols);
			}
			mOpenMenu.setOnIconContextItemSelectedListener(this);
			mOpenMenu.show();
			return mOpenMenu;
		} catch(Exception e) {
			Logger.LogWarning("Couldn't show icon context menu.", e);
		}
		return null;
	}
	
	
	/**
	 * Returns true if the Intent was "Handled"
	 * @param intent Input Intent
	 */
	public boolean handleIntent(Intent intent)
	{
		if(Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			OpenPath searchIn = new OpenFile("/");
			Bundle bundle = intent.getBundleExtra(SearchManager.APP_DATA);
			if(bundle != null && bundle.containsKey("path"))
				try {
					searchIn = FileManager.getOpenCache(bundle.getString("path"), false, null);
				} catch (IOException e) {
					searchIn = new OpenFile(bundle.getString("path"));
				}
			String query = intent.getStringExtra(SearchManager.QUERY);
			Logger.LogDebug("ACTION_SEARCH for \"" + query + "\" in " + searchIn);
			SearchResultsFragment srf = SearchResultsFragment.getInstance(searchIn, query);
			if(mViewPagerEnabled && mViewPagerAdapter != null)
			{
				mViewPagerAdapter.add(srf);
				setViewPageAdapter(mViewPagerAdapter, true);
				setCurrentItem(mViewPagerAdapter.getCount() - 1, true);
			} else {
				getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frag, srf)
					.commit();
			}
		}
		else if ((Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_EDIT.equals(intent.getAction()))
				&& intent.getData() != null)
		{
			OpenPath path = FileManager.getOpenCache(intent.getDataString(), this);
			if(editFile(path))
				return true;
		}else if(intent.hasExtra("state"))
		{
			Bundle state = intent.getBundleExtra("state");
			onRestoreInstanceState(state);
		}
		return false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(outState == null) return;
		super.onSaveInstanceState(outState);
		/*
		mStateReady = false;
		if(mLogFragment != null)
			try {
				//fragmentManager.beginTransaction().remove(mLogFragment).disallowAddToBackStack().commitAllowingStateLoss();
			} catch(Exception e) { }
		try {
			Fragment f = fragmentManager.findFragmentByTag("ops");
			//if(f != null)
			//	fragmentManager.beginTransaction().remove(f).disallowAddToBackStack().commitAllowingStateLoss();
		} catch(Exception e) { }
		super.onSaveInstanceState(outState);
		if(mViewPagerAdapter != null)
		{
			Parcelable p = mViewPagerAdapter.saveState();
			if(p != null)
			{
				Logger.LogDebug("<-- Saving Fragments: " + p.toString());
				outState.putParcelable("oe_fragments", p);
				outState.putInt("oe_frag_index", mViewPager.getCurrentItem());
			}
		}
		*/
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		if(state != null)
		{
			Logger.LogDebug("Restoring State: " + state);
			super.onRestoreInstanceState(state);
		}
		/*
		mStateReady = true;
		if(state != null && state.containsKey("oe_fragments"))
		{
			mViewPagerAdapter.restoreState(state, getClassLoader());
			setViewPageAdapter(mViewPagerAdapter);
			setCurrentItem(state.getInt("oe_frag_index"), false);
		}
		*/
	}
	
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		ContentFragment cf = getDirContentFragment(false);
		if(cf == null) return false;
		OpenPath path = cf.getPath();
		appData.putString("path", path.getPath());
		startSearch(null, false, appData, false);
		return true;
	}

	private void upgradeViewSettings() {
		if(getSetting(null, "pref_up_views", false)) return;
		setSetting("pref_up_views", true);
		getPreferences().upgradeViewSettings();
	}

	private void initBookmarkDropdown()
	{
		if(mBookmarksList == null)
			mBookmarksList = new ExpandableListView(this);
		if(findViewById(R.id.list_frag) != null) {
			ViewGroup leftNav = ((ViewGroup)findViewById(R.id.list_frag));
			leftNav.removeAllViews();
			leftNav.addView(mBookmarksList);
		} else {
			View anchor = null;
			if(anchor == null)
				anchor = findViewById(R.id.title_icon);
			if(anchor == null)
				anchor = findViewById(android.R.id.home);
			if(anchor == null && USE_ACTION_BAR && !BEFORE_HONEYCOMB && getActionBar() != null && getActionBar().getCustomView() != null)
				anchor = getActionBar().getCustomView();
			if(anchor == null)
				anchor = findViewById(R.id.title_bar);
			if(anchor == null)
				anchor = findViewById(R.id.base_bar);
			if(anchor == null)
				anchor = findViewById(R.id.base_row);
			mBookmarksPopup = new BetterPopupWindow(this, anchor);
			mBookmarksPopup.setContentView(mBookmarksList);
		}
		mBookmarks = new OpenBookmarks(this, mBookmarksList);
		for(int i = 0; i < mBookmarksList.getCount(); i++)
			mBookmarksList.expandGroup(i);
	}
	
	private void initLogPopup()
	{
		if(mLogFragment == null)
			mLogFragment = new LogViewerFragment();
		if(findViewById(R.id.frag_log) != null)
			return;
		View anchor = ViewUtils.getFirstView(this, R.id.title_log, R.id.title_bar,
				R.id.base_bar, R.id.base_row);
		mLogFragment.setupPopup(this, anchor);
	}
	
	private void initOpsPopup()
	{
		if(mOpsFragment == null)
			mOpsFragment = new OperationsFragment();
		if(findViewById(R.id.frag_log) != null)
			return;
		View anchor = ViewUtils.getFirstView(this, R.id.title_ops, R.id.title_bar,
						R.id.base_bar, R.id.base_row);
		mOpsFragment.setupPopup(this, anchor);
	}
	
	private void initPager()
	{
		mViewPager = ((OpenViewPager)findViewById(R.id.content_pager));
		TabPageIndicator indicator = null;
		if(mViewPagerEnabled && mViewPager != null)
		{
			setViewVisibility(false, false, R.id.content_frag, R.id.title_text, R.id.title_path, R.id.title_bar_inner, R.id.title_underline_2);
			setViewVisibility(true, false, R.id.content_pager, R.id.content_pager_indicator);
			mViewPager.setOnPageChangeListener(this);
			//mViewPager.setOnPageIndicatorChangeListener(this);
			View indicator_frame = findViewById(R.id.content_pager_indicator);
			try {
				//LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.makeInAnimation(getApplicationContext(), false));
				if(indicator_frame != null)
					indicator_frame.setAnimation(AnimationUtils.makeInAnimation(getApplicationContext(), false));
			} catch(Resources.NotFoundException e)
			{
				Logger.LogError("Couldn't load pager animation.", e);
			}
			indicator = (TabPageIndicator)findViewById(R.id.content_pager_indicator);
			if(indicator != null)
				mViewPager.setIndicator(indicator);
			else
				Logger.LogError("Couldn't find indicator!");
			//mViewPager = new ViewPager(getApplicationContext());
			//((ViewGroup)findViewById(R.id.content_frag)).addView(mViewPager);
			//findViewById(R.id.content_frag).setId(R.id.fake_content_id);
		} else {
			//mViewPagerEnabled = false;
			mViewPager = null; //(ViewPager)findViewById(R.id.content_pager);
			setViewVisibility(false, false, R.id.content_pager, R.id.content_pager_indicator);
			setViewVisibility(true, false, R.id.content_frag, R.id.title_text, R.id.title_path, R.id.title_bar_inner, R.id.title_underline_2);
		}

		if(mViewPager != null && mViewPagerEnabled)
		{
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogDebug("Setting up ViewPager");
			mViewPagerAdapter = //new PagerTabsAdapter(this, mViewPager, indicator);
					new ArrayPagerAdapter(this, mViewPager);
			mViewPagerAdapter.setOnPageTitleClickListener(this);
			setViewPageAdapter(mViewPagerAdapter);
		}

	}

	public void setViewVisibility(final boolean visible, final boolean allowAnimation, int... ids) {
		for(int id : ids)
		{
			final View v = findViewById(id);
			if(v != null && visible != (v.getVisibility() == View.VISIBLE))
			{
				if(allowAnimation)
				{
					Animation anim;
					if(visible)
						anim = AnimationUtils.makeInAnimation(getApplicationContext(), true);
					else
						anim = AnimationUtils.makeOutAnimation(getApplicationContext(), false);
					anim.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							if(visible)
								v.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							if(!visible)
								v.setVisibility(View.GONE);
							else
								v.setVisibility(View.VISIBLE);
						}
					});
					v.startAnimation(anim);
				} else
					v.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}
	}

	private boolean setViewPageAdapter(PagerAdapter adapter) { return setViewPageAdapter(adapter, true); }
	private boolean setViewPageAdapter(PagerAdapter adapter, boolean reload)
	{
		if(adapter == null) adapter = mViewPager.getAdapter();
		if(mViewPager != null)
		{
			try {
				if(!adapter.equals(mViewPager.getAdapter()) || reload)
					mViewPager.setAdapter(adapter);
				else {
					mViewPager.notifyDataSetChanged();
					getDirContentFragment(false).notifyDataSetChanged();
				}
				return true;
			} catch(IndexOutOfBoundsException e) {
				Logger.LogError("Why is this happening?", e);
				return false;
			} catch(IllegalStateException e) {
				Logger.LogError("Error trying to set ViewPageAdapter", e);
				return false;
			} catch(Exception e) {
				Logger.LogError("Please stop!", e);
				return false;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static void launchTranslator(Activity a)
	{
		new Preferences(a).setSetting("warn", "translate", true);
		String lang = DialogHandler.getLangCode();
		Uri uri = Uri.parse("http://brandroid.org/translation_helper.php?lang=" + lang + "&full=" + Locale.getDefault().getDisplayLanguage() + "&wid=" + a.getWindowManager().getDefaultDisplay().getWidth());
		//Intent intent = new Intent(a, WebViewActivity.class);
		//intent.setData();
		//a.startActivity(intent);
		WebViewFragment web = new WebViewFragment().setUri(uri);
		if(Build.VERSION.SDK_INT < 100)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			a.startActivity(intent);
		} else if(a.findViewById(R.id.frag_log) != null)
		{
			((FragmentActivity)a).getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frag_log, web)
				.addToBackStack("trans")
				.commit();
			a.findViewById(R.id.frag_log).setVisibility(View.VISIBLE);
			LayoutParams lp = a.findViewById(R.id.frag_log).getLayoutParams();
			lp.width = a.getResources().getDimensionPixelSize(R.dimen.bookmarks_width) * 2;
			a.findViewById(R.id.frag_log).setLayoutParams(lp);
		} else {
			web.setShowsDialog(true);
			web.show(((FragmentActivity)a).getSupportFragmentManager(), "trans");
			if(web.getDialog() != null)
				web.getDialog().setTitle(R.string.button_translate);
			web.setUri(uri);
		}
	}
	private void showWarnings()
	{
		// Non-viewpager disabled
		
		if(checkLanguage() > 0 && !getPreferences().getBoolean("warn", "translate", false))
		{
			//runOnUiThread(new Runnable(){public void run() {
				int msg = checkLanguage() == 1 ? R.string.alert_translate_google : R.string.alert_translate_none;
				new AlertDialog.Builder(OpenExplorer.this)
					.setCancelable(true)
					.setMessage(getString(msg, Locale.getDefault().getDisplayLanguage()))
					.setNeutralButton("Use English",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								setLanguage(getContext(), "en");
								goHome();
							}
						})
					.setPositiveButton(R.string.button_translate,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									launchTranslator(OpenExplorer.this);
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									getPreferences().setSetting("warn", "translate", true);
								}
							})
					.create()
					.show();
			//}});
		}
	}
	
	private int checkLanguage()
	{
		String lang = DialogHandler.getLangCode();
		if(lang.equals("EN")) return 0;
		return ",ES,FR,KO,HE,DE,RU,".indexOf(","+DialogHandler.getLangCode()+",") == -1 ? 2 : 1;
	}
	
	public static void showSplashIntent(Context context, String start)
	{
		Intent intent = new Intent(context, SplashActivity.class);
		intent.putExtra("start", start);
		if(context instanceof OpenExplorer)
			((OpenExplorer)context).startActivityForResult(intent, REQ_SPLASH);
		else if(context instanceof Activity)
			((Activity)context).startActivityForResult(intent, REQ_SPLASH);
		else if(context instanceof FragmentActivity)
			((FragmentActivity)context).startActivityForResult(intent, REQ_SPLASH);
		else
			context.startActivity(intent);
	}
	
	@SuppressWarnings("unused")
	public void updatePagerTitle(int page)
	{
		TextView tvLeft = null; // (TextView)findViewById(R.id.title_left);
		TextView tvRight = null; //(TextView)findViewById(R.id.title_right);
		String left = "";
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		for(int i = 0; i < page; i++)
		{
			OpenFragment f = mViewPagerAdapter.getItem(i);
			if(f instanceof ContentFragment)
			{
				OpenPath p = ((ContentFragment)f).getPath();
				left += p.getName();
				if(p.isDirectory() && !left.endsWith("/"))
					left += "/";
			}
		}
		SpannableString srLeft = new SpannableString(left);
		srLeft.setSpan(new ForegroundColorSpan(Color.GRAY), 0, left.length(), Spanned.SPAN_COMPOSING);
		ssb.append(srLeft);
		//ssb.setSpan(new ForegroundColorSpan(Color.GRAY), 0, left.length(), Spanned.SPAN_COMPOSING);
		OpenFragment curr = mViewPagerAdapter.getItem(page);
		if(curr instanceof ContentFragment)
		{
			OpenPath pCurr = ((ContentFragment)curr).getPath();
			ssb.append(pCurr.getName());
			if(pCurr.isDirectory())
				ssb.append("/");
		}
		String right = "";
		for(int i = page + 1; i < mViewPagerAdapter.getCount(); i++)
		{
			OpenFragment f = mViewPagerAdapter.getItem(i);
			if(f instanceof ContentFragment)
			{
				OpenPath p = ((ContentFragment)f).getPath();
				right += p.getName();
				if(p.isDirectory() && !right.endsWith("/"))
					right += "/";
			}
		}
		SpannableString srRight = new SpannableString(right);
		srRight.setSpan(new ForegroundColorSpan(Color.GRAY), 0, right.length(), Spanned.SPAN_COMPOSING);
		ssb.append(srRight);
		updateTitle(ssb);
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		LOW_MEMORY = true;
		showToast(R.string.s_msg_low_memory);
		ThumbnailCreator.flushCache(getApplicationContext(), false);
		FileManager.clearOpenCache();
		EventHandler.cancelRunningTasks();
	}
	
	public void setBookmarksPopupListAdapter(ListAdapter adapter)
	{
		mBookmarksList.setAdapter(adapter);
	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		handleNetworking();
		try {
			VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) { }
		if(getWindowManager() != null)
		{
			Display d = getWindowManager().getDefaultDisplay();
			if(d != null)
			{
				DisplayMetrics dm = new DisplayMetrics();
				d.getMetrics(dm);
				if(dm != null)
				{
					SCREEN_DPI = dm.densityDpi;
					SCREEN_WIDTH = dm.widthPixels;
					SCREEN_HEIGHT = dm.heightPixels;
				} else {
					SCREEN_WIDTH = d.getWidth();
					SCREEN_HEIGHT = d.getHeight();
				}
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(DEBUG && IS_DEBUG_BUILD)
			Logger.LogVerbose("OpenExplorer.onStart");
		if(findViewById(R.id.frag_log) != null)
		{
			fragmentManager.beginTransaction().add(R.id.frag_log, mLogFragment, "log").commit();
			findViewById(R.id.frag_log).setVisibility(View.GONE);
		} else {
			initLogPopup();
		}
		//submitStats();
		//new Thread(new Runnable(){public void run() {refreshCursors();}}).start();;
		//refreshCursors();
		//mBookmarks.scanBookmarks();
		mStateReady = true;
	}
	
	/*
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			super.onSaveInstanceState(outState);
			List<Fragment> frags = mViewPagerAdapter.getFragments();
			String[] paths = new String[frags.size()];
			for(int i = 0; i < frags.size(); i++)
			{
				Fragment f = frags.get(i);
				if(f instanceof OpenPathFragmentInterface)
					paths[i] = ((OpenPathFragmentInterface)f).getPath().toString();
			}
			outState.putStringArray("paths", paths);
			Logger.LogDebug("-->Saving fragments: " + paths.length);
		} catch(Exception e) {
			Logger.LogError("Couldn't save main state", e);
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.containsKey("paths"))
		{
			String[] paths = savedInstanceState.getStringArray("paths");
			Logger.LogDebug("<--Restoring fragments: " + paths.length);
			for(int i = 0; i < paths.length; i++)
			{
				String path = paths[i];
				if(path == null) continue;
				try {
					OpenPath file = FileManager.getOpenCache(path, false, null);
					if(file == null) file = new OpenFile(path);
					if(file.isTextFile())
						mViewPagerAdapter.add(new TextEditorFragment(file));
				} catch (IOException e) {
					Logger.LogError("Couldn't get Path while restoring state", e);
				}
				
			}
			mViewPagerAdapter.notifyDataSetChanged();
		}
	}
	*/
	
	public boolean usingSplitActionBar()
	{
		if(!USE_SPLIT_ACTION_BAR) return false;
		if(findViewById(R.id.base_bar) != null && findViewById(R.id.base_bar).isShown())
			return true;
		int pos = ViewUtils.getAbsoluteTop(this, R.id.menu_more, R.id.menu_sort, R.id.menu_text_view, R.id.base_row);
		//Logger.LogInfo("SPLIT AB TOP = " + pos);
		return pos > 10;
	}
	private void checkTitleSeparator()
	{
		if(mStaticButtons == null)
			mStaticButtons = (ViewGroup)findViewById(R.id.title_static_buttons);
		if(mStaticButtons == null && !BEFORE_HONEYCOMB && USE_ACTION_BAR)
			mStaticButtons = (ViewGroup)getActionBar().getCustomView().findViewById(R.id.title_static_buttons);
		if(mStaticButtons == null)
		{
			Logger.LogWarning("Unable to find Title Separator");
			return;
		}
		
		boolean visible = false;
		if(!usingSplitActionBar())
		{
			for(int id : new int[]{R.id.title_paste, R.id.title_log, R.id.title_ops})
				if(mStaticButtons.findViewById(id) != null &&
						mStaticButtons.findViewById(id).getVisibility() == View.VISIBLE)
					visible = true;
		} //else Logger.LogDebug("Title Separator hidden since Split Action bar is used.");
		
		ViewUtils.setViewsVisible(mStaticButtons, visible, R.id.title_divider);
	}
	
	public void sendToLogView(final String txt, final int color)
	{
		try {
		
		if(txt == null) return;
		Logger.LogDebug("Log: " + txt);
		if(mLogFragment == null)
			mLogFragment = new LogViewerFragment();
		mLogFragment.print(txt, color);
		if(mLogFragment.getAdded()) return;
		ViewUtils.setViewsVisible(this, true, R.id.title_log);
		checkTitleSeparator();
		if(!mLogFragment.getAdded() && !mLogFragment.isVisible())
		{
			final View logview = findViewById(R.id.frag_log);
			if(logview != null && !mLogFragment.getAdded())
			{
				Fragment fl = fragmentManager.findFragmentById(R.id.frag_log);
				if(!(fl instanceof LogViewerFragment))
					fragmentManager.beginTransaction()
						.replace(R.id.frag_log, mLogFragment)
						.disallowAddToBackStack()
						.commitAllowingStateLoss();
				logview.post(new Runnable(){public void run(){
					logview.setVisibility(View.VISIBLE);
					mLogFragment.setAdded(true);
				}});
			} //else mLogFragment.show(fragmentManager, "log");
		}
		} catch(Exception e) {
			Logger.LogError("Couldn't send to Log Viewer", e);
		}
	}

	
	private void setupFilesDb()
	{
		OpenPath.setDb(new OpenPathDbAdapter(getApplicationContext()));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		saveOpenedEditors();
		if(Logger.isLoggingEnabled() && Logger.hasDb())
			Logger.closeDb();
	}
	
	public boolean isNetworkConnected()
	{
		ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if(!conman.getBackgroundDataSetting()) return false;
		NetworkInfo ni = conman.getActiveNetworkInfo();
		if(ni == null) return false;
		if(!ni.isAvailable() || !ni.isConnected()) return false;
		if(ni.getState() == State.CONNECTING) return false;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void submitStats()
	{
		if(!Logger.isLoggingEnabled()) return;
		setupLoggingDb();
		if(IS_DEBUG_BUILD) return;
		if(new Date().getTime() - lastSubmit < 6000)
			return;
		lastSubmit = new Date().getTime();
		if(!isNetworkConnected()) return;
		
		String logs = Logger.getDbLogs(false);
		if(logs == null || logs == "") logs = "[]";
		//if(logs != null && logs != "") {
			Logger.LogDebug("Found " + logs.length() + " bytes of logs.");
			new SubmitStatsTask(this).execute(logs);
		//} else Logger.LogWarning("Logs not found.");
	}
	
	
	public void handleRefreshMedia(final String path, boolean keepChecking, final int retries)
	{
		if(!keepChecking || retries <= 0)
		{
			refreshBookmarks();
			return;
		}
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				Logger.LogDebug("Check " + retries + " for " + path);
				try {
					StatFs sf = new StatFs(path);
					if(sf.getBlockCount() == 0)
						throw new Exception("No blocks");
					showToast(getResources().getString(R.string.s_alert_new_media) + " " + getVolumeName(path) + " @ " + DialogHandler.formatSize((long)sf.getBlockSize() * (long)sf.getAvailableBlocks()));
					refreshBookmarks();
					if(mLastPath.getPath().equals(path))
						goHome();
				} catch(Throwable e)
				{
					Logger.LogWarning("Couldn't read " + path);
					handleRefreshMedia(path, true, retries - 1); // retry again in 1/2 second
				}
			}
		}, 1000);
	}
	
	public void handleMediaReceiver()
	{
		storageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				String data = intent.getDataString();
				final String path = data.replace("file://", "");
				if(action.equals(Intent.ACTION_MEDIA_MOUNTED))
					handleRefreshMedia(path, true, 10);
				else
					refreshBookmarks();
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		registerReceiver(storageReceiver, filter);
		
		ContentObserver mDbObserver = new ContentObserver(mHandler) {
			@Override
			public void onChange(boolean selfChange) {
				//rebake(false, ImageManager.isMediaScannerScanning(
				 //	   getContentResolver()));
			}
		};
		
		getContentResolver().registerContentObserver(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				true, mDbObserver);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//mActionBarHelper.onPostCreate(savedInstanceState);
		ensureCursorCache();
		/*
		if(mSettingsListener != null)
		{
			mSettingsListener.onHiddenFilesChanged(mPreferences.getBoolean(SettingsActivity.PREF_HIDDEN_KEY, false));
			mSettingsListener.onThumbnailChanged(mPreferences.getBoolean(SettingsActivity.PREF_THUMB_KEY, true));
			mSettingsListener.onViewChanged(mPreferences.getString(SettingsActivity.PREF_VIEW_KEY, "list"));
		} */
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(storageReceiver != null)
			unregisterReceiver(storageReceiver);
	}
	
	@Override
	public OpenClipboard getClipboard() {
		return getOpenApplication().getClipboard();
	}
	
	public void addHoldingFile(OpenPath path) { 
		getClipboard().add(path);
		invalidateOptionsMenu();
	}
	public void clearHoldingFiles() {
		getClipboard().clear();
		invalidateOptionsMenu();
	}
	
	public static final OpenCursor getPhotoParent() {
		//if(mPhotoParent == null) refreshCursors();
		return mPhotoParent;
	}
	public static final OpenCursor getVideoParent() {
		//if(mVideoParent == null) refreshCursors();
		return mVideoParent;
	}
	public static final OpenCursor getMusicParent() {
		//if(mMusicParent == null) refreshCursors();
		return mMusicParent;
	}

	public static final OpenSmartFolder getDownloadParent() {
		return mDownloadParent;
	}
	
	private boolean findCursors()
	{
		mVideoParent.setName(getString(R.string.s_videos));
		mPhotoParent.setName(getString(R.string.s_photos));
		mMusicParent.setName(getString(R.string.s_music));
		mDownloadParent.setName(getString(R.string.s_downloads));
		
		if(mVideoParent.isLoaded())
		{
			//Logger.LogDebug("Videos should be found");
		}else
		{
			if(bRetrieveExtraVideoDetails)
				bRetrieveExtraVideoDetails = !getSetting(null, "tag_novidinfo", false);
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogVerbose("Finding videos");
			//if(!IS_DEBUG_BUILD)
			try {
				getSupportLoaderManager().initLoader(0, null, this);
			} catch(Exception e) { Logger.LogError("Couldn't query videos.", e); }
			Logger.LogDebug("Done looking for videos");
		}
		if(!mPhotoParent.isLoaded())
		{
			if(bRetrieveDimensionsForPhotos)
				bRetrieveDimensionsForPhotos = !getSetting(null, "tag_nodims", false);
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogVerbose("Finding Photos");
			try {
				getSupportLoaderManager().initLoader(1, null, this);
				Logger.LogDebug("Done looking for photos");
				
			} catch(IllegalStateException e) { Logger.LogError("Couldn't query photos.", e); }
		}
		if(!mMusicParent.isLoaded())
		{
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogVerbose("Finding Music");
			try {
				getSupportLoaderManager().initLoader(2, null, this);
				Logger.LogDebug("Done looking for music");
			} catch(IllegalStateException e) { Logger.LogError("Couldn't query music.", e); }
		}
		if(!mApkParent.isLoaded())
		{
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogVerbose("Finding APKs");
			try {
				getSupportLoaderManager().initLoader(3, null, this);
			} catch(IllegalStateException e) { Logger.LogError("Couldn't get Apks.", e); }
		}
		if(!mDownloadParent.isLoaded())
		{
			new Thread(new Runnable(){public void run(){
				OpenFile extDrive = OpenFile.getExternalMemoryDrive(false);
				OpenFile intDrive = OpenFile.getInternalMemoryDrive();
				boolean mHasExternal = false;
				boolean mHasInternal = false;
				if(extDrive != null && extDrive.exists())
					mHasExternal = true;
				if(intDrive != null && intDrive.exists())
					mHasInternal = true;
					//OpenSmartFolder dlSmart = new OpenSmartFolder("Downloads");
				
				if(mHasExternal)
					for(OpenPath kid : extDrive.list())
						if(kid.getName().toLowerCase().indexOf("download")>-1)
							mDownloadParent.addSearch(new SmartSearch(kid));
				if(mHasInternal)
					for(OpenPath kid : intDrive.list())
						if(kid.getName().toLowerCase().indexOf("download")>-1)
							mDownloadParent.addSearch(new SmartSearch(kid));
			}}).start();
		}
		if(DEBUG && IS_DEBUG_BUILD)
			Logger.LogVerbose("Done finding cursors");
		return true;
	}
	private void refreshCursors()
	{
		if(findCursors())
			return;
		//new Thread(new Runnable(){public void run() {
			//ensureCursorCache();
		//}}).start();
	}
	public void ensureCursorCache()
	{
		//findCursors();
		if(mRunningCursorEnsure
				//|| mLastCursorEnsure == 0
				//|| new Date().getTime() - mLastCursorEnsure < 10000 // at least 10 seconds
				)
		{
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogVerbose("Skipping ensureCursorCache");
			return;
		} else if(DEBUG && IS_DEBUG_BUILD)
			Logger.LogVerbose("Running ensureCursorCache");
		mRunningCursorEnsure = true;
		
		// group into blocks
		int iTotalSize = 0;
		for(OpenCursor cur : new OpenCursor[]{mVideoParent, mPhotoParent, mApkParent})
			if(cur != null)
				iTotalSize += cur.length();
		int enSize = Math.max(20, iTotalSize / 10);
		Logger.LogDebug("EnsureCursorCache size: " + enSize + " / " + iTotalSize);
		ArrayList<OpenPath> buffer = new ArrayList<OpenPath>(enSize);
		for(OpenCursor curs : new OpenCursor[]{mVideoParent, mPhotoParent, mApkParent})
		{
			if(curs == null) continue;
			for(OpenMediaStore ms : curs.list())
			{
				buffer.add(ms);
				if(buffer.size() == enSize)
				{
					OpenMediaStore[] buff = new OpenMediaStore[buffer.size()];
					buffer.toArray(buff);
					buffer.clear();
					try {
						//Logger.LogDebug("Executing Task of " + buff.length + " items");
						/*if(!BEFORE_HONEYCOMB)
							new EnsureCursorCacheTask().executeOnExecutor(new Executor() {
								public void execute(Runnable command) {
									command.run();
								}
							}, buff);
						else*/
							new EnsureCursorCacheTask().execute(buff);
					} catch(RejectedExecutionException e) {
						Logger.LogWarning("Couldn't ensure cache.", e);
						return;
					}
				}
			}
		}
		if(buffer.size() > 0)
		{
			OpenMediaStore[] buff = new OpenMediaStore[buffer.size()];
			buffer.toArray(buff);
			buffer.clear();
			try {
				Logger.LogDebug("Executing Task of " + buff.length + " items");
				/*if(!BEFORE_HONEYCOMB)
					new EnsureCursorCacheTask().executeOnExecutor(new Executor() {
						public void execute(Runnable command) {
							command.run();
						}
					}, buff);
				else*/
					new EnsureCursorCacheTask().execute(buff);
			} catch(RejectedExecutionException e) {
				Logger.LogWarning("Couldn't ensure cache.", e);
				return;
			}
		}

		if(DEBUG && IS_DEBUG_BUILD)
			Logger.LogVerbose("Done with ensureCursorCache");
		
		//mLastCursorEnsure = new Date().getTime();
		mRunningCursorEnsure = false;
	}
	

	public void toggleBookmarks(Boolean visible)
	{
		if(!mSinglePane) return;
		if(mBookmarksPopup != null)
		{
			if(visible)
				mBookmarksPopup.showLikePopDownMenu();
			else
				mBookmarksPopup.dismiss();
		}
	}
	
	public void toggleBookmarks()
	{
		final View mBookmarks = findViewById(R.id.list_frag);
		final boolean in = mBookmarks == null || mBookmarks.getVisibility() == View.GONE;
		if(isSinglePane())
			toggleBookmarks(in);
		else
			setViewVisibility(in, true, R.id.list_frag);
	}

	public void refreshOperations() {
		if(mOpsFragment == null)
			mOpsFragment = (OperationsFragment)fragmentManager.findFragmentByTag("ops");
		if(mOpsFragment == null)
			mOpsFragment = new OperationsFragment();
		int tasks = EventHandler.getRunningTasks().length;
		if(tasks <= 0)
		{
			ViewUtils.setViewsVisible(this, false, R.id.frag_log, R.id.title_ops);
			ViewUtils.setViewsVisible(this, true, R.id.title_ops);
			checkTitleSeparator();
			return;
		}
		if(findViewById(R.id.frag_log) != null)
		{
			if(fragmentManager.findFragmentByTag("ops") == null)
				fragmentManager.beginTransaction().add(mOpsFragment, "ops");
			if(fragmentManager.findFragmentById(R.id.frag_log) != mOpsFragment)
				fragmentManager
					.beginTransaction()
					.replace(R.id.frag_log, mOpsFragment)
					.disallowAddToBackStack()
					.commitAllowingStateLoss();
		} else {
			initOpsPopup();
		}
		ViewUtils.setViewsVisible(this, tasks > 0, R.id.title_ops);
		checkTitleSeparator();
	}
	
	public void refreshBookmarks()
	{
		if(DEBUG && IS_DEBUG_BUILD)
			Logger.LogVerbose("refreshBookmarks()");
		refreshCursors();
		if(mBookmarks != null)
		{
			mBookmarks.scanBookmarks();
			mBookmarks.refresh();
		}
		if(mBookmarksList != null)
			mBookmarksList.invalidate();
	}
	public ContentFragment getDirContentFragment(Boolean activate) { return getDirContentFragment(activate, mLastPath); }
	public ContentFragment getDirContentFragment(Boolean activate, OpenPath path)
	{
		//Logger.LogDebug("getDirContentFragment");
		OpenFragment ret = null;
		//if(mViewPager != null && mViewPagerAdapter != null && mViewPagerAdapter instanceof OpenPathPagerAdapter && ((OpenPathPagerAdapter)mViewPagerAdapter).getLastItem() instanceof ContentFragment)
		//	ret = ((ContentFragment)((OpenPathPagerAdapter)mViewPagerAdapter).getLastItem());
		if(mViewPagerAdapter != null && mViewPager != null)
		{
			if(mViewPager.getCurrentItem() > -1)
			{
				ret = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
				//Logger.LogVerbose("Current Page: " + (mViewPager.getCurrentItem() + 1) + " of " + mViewPagerAdapter.getCount() + (ret instanceof ContentFragment ? " : " + ((ContentFragment)ret).getPath().getPath() : ""));
				if(!(ret instanceof ContentFragment))
					ret = mViewPagerAdapter.getItem(mViewPagerAdapter.getLastPositionOfType(ContentFragment.class));
			} else {
				Logger.LogWarning("Couldn't find current Page. Using last.");
				ret = mViewPagerAdapter.getItem(mViewPagerAdapter.getLastPositionOfType(ContentFragment.class));
			}
		}
		if(ret == null)
			ret = (OpenFragment)fragmentManager.findFragmentById(R.id.content_frag);
		if(ret == null || !ret.getClass().equals(ContentFragment.class))
		{
			Logger.LogWarning("Do I need to create a new ContentFragment?");
   			//ret = ContentFragment.newInstance(mLastPath, getSetting(mLastPath, "view", mViewMode));
   			//mViewPagerAdapter.add(ret);
			/*if(mViewPagerAdapter instanceof OpenPathPagerAdapter)
			{
				((OpenPathPagerAdapter)mViewPagerAdapter).setPath(mLastPath);
				ret = ((OpenPathPagerAdapter)mViewPagerAdapter).getLastItem();
			} else if(mViewPagerAdapter instanceof ArrayPagerAdapter)*/
			if(mViewPagerAdapter != null && mViewPager != null)
				ret = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
		}
		if(ret == null && path != null)
		{
			ret = ContentFragment.getInstance(path, getSetting(path, "view", 0), getSupportFragmentManager());
			if(mViewPager != null && ret != null)
			{
				//if(mViewPagerAdapter instanceof ArrayPagerAdapter)
					mViewPagerAdapter.set(mViewPager.getCurrentItem(), ret);
			}
		}
		if(activate && !ret.isVisible())
		{
			setCurrentItem(mViewPagerAdapter.getItemPosition(ret), false);
		}
		
		if(ret instanceof ContentFragment)
			return (ContentFragment)ret;
		else return null;
	}
	public OpenFragment getSelectedFragment()
	{
		OpenFragment ret = null;
		//if(mViewPager != null && mViewPagerAdapter != null && mViewPagerAdapter instanceof OpenPathPagerAdapter && ((OpenPathPagerAdapter)mViewPagerAdapter).getLastItem() instanceof ContentFragment)
		//	ret = ((ContentFragment)((OpenPathPagerAdapter)mViewPagerAdapter).getLastItem());
		if(mViewPagerAdapter != null && mViewPager != null)
		{
			if(mViewPager.getCurrentItem() > -1)
			{
				//Logger.LogVerbose("Current Page: " + (mViewPager.getCurrentItem() + 1) + " of " + mViewPagerAdapter.getCount());
				ret = mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
			} else {
				Logger.LogWarning("Couldn't find current Page. Using last.");
				ret = mViewPagerAdapter.getItem(mViewPagerAdapter.getLastPositionOfType(ContentFragment.class));
			}
		}
		if(ret == null && fragmentManager != null)
			ret = (OpenFragment)fragmentManager.findFragmentById(R.id.content_frag);
		
   		return ret;
	}
	
	public void updateTitle(CharSequence cs)
	{
		TextView title = (TextView)findViewById(R.id.title_path);
		if((title == null || !title.isShown()) && !BEFORE_HONEYCOMB && getActionBar() != null && getActionBar().getCustomView() != null)
			title = (TextView)getActionBar().getCustomView().findViewById(R.id.title_path);
		//if(BEFORE_HONEYCOMB || !USE_ACTION_BAR || getActionBar() == null)
		if(title != null && title.getVisibility() != View.GONE)
			title.setText(cs, BufferType.SPANNABLE);
		if(!BEFORE_HONEYCOMB && USE_ACTION_BAR && getActionBar() != null && (title == null || !title.isShown()))
			getActionBar().setSubtitle(cs);
		//else
		{
			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getString(R.string.app_title));
			sb.append(cs.equals("") ? "" : " - ");
			sb.append(cs);
			setTitle(cs);
		}
	}
	
	private void saveOpenedEditors()
	{
		StringBuilder editing = new StringBuilder(",");
		for(int i = 0; i < mViewPagerAdapter.getCount(); i++)
		{
			OpenFragment f = mViewPagerAdapter.getItem(i);
			if(f instanceof TextEditorFragment)
			{
				TextEditorFragment tf = (TextEditorFragment)f;
				if(!tf.isSalvagable()) continue;
				OpenPath path = tf.getPath();
				if(editing.indexOf(","+path.getPath()+",") == -1)
					editing.append(path + ",");
			}
		}
		if(DEBUG && !editing.equals(","))
			Logger.LogDebug("Saving [" + editing.toString() + "] as TextEditorFragments");
		setSetting("editing", editing.toString());
	}
	private void restoreOpenedEditors()
	{
		String editing = getSetting(null, "editing", (String)null);
		Logger.LogDebug("Restoring [" + editing + "] to TextEditorFragments");
		if(editing == null) return;
		for(String s : editing.split(","))
		{
			if(s == null || s == "") continue;
			OpenPath path = FileManager.getOpenCache(s, this);
			if(path == null) continue;
			editFile(path, true);
		}
		setViewPageAdapter(mViewPagerAdapter, true);
	}
	
	public void closeFragment(final OpenFragment frag)
	{
		final int pos = mViewPagerAdapter.getItemPosition(frag);
		if(pos >= 0)
		{
			if(frag instanceof TextEditorFragment)
				((TextEditorFragment)frag).setSalvagable(false);
			mViewPager.post(new Runnable() {public void run() {
				int cp = mViewPager.getCurrentItem();
				mViewPagerAdapter.remove(frag);
				setViewPageAdapter(mViewPagerAdapter, true);
				if(frag instanceof TextEditorFragment)
					saveOpenedEditors();
				if(mViewPagerAdapter.getCount() == 0)
					finish();
				if(cp == pos && pos > 0)
					mViewPager.setCurrentItem(pos - 1, true);
				else if (cp == pos && mViewPagerAdapter.getCount() > 1)
					mViewPager.setCurrentItem(pos + 1, true);
			}});
		}
	}
	public boolean editFile(OpenPath path) { return editFile(path, false); }
	public boolean editFile(OpenPath path, boolean batch)
	{
		if(path == null) return false;
		if(!path.exists()) return false;
		if(path.length() > getResources().getInteger(R.integer.max_text_editor_size)) return false;
		TextEditorFragment editor = new TextEditorFragment(path);
		if(mViewPagerAdapter != null)
		{
			int pos = mViewPagerAdapter.getItemPosition(editor);
			if(pos == -1)
			{
				mViewPagerAdapter.add(editor);
				setViewPageAdapter(mViewPagerAdapter, !batch);
				if(!batch)
				{
					saveOpenedEditors();
					pos = mViewPagerAdapter.getItemPosition(editor);
					if(pos > -1)
						setCurrentItem(pos, true);
				}
			} else if(!batch) setCurrentItem(pos, true);
		} else
			fragmentManager.beginTransaction()
				.replace(R.id.content_frag, editor)
				//.addToBackStack(null)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
		//addTab(editor, path.getName(), true);
		return true;
	}
	
	@Override
	public void startActivity(Intent intent) {
		//if(handleIntent(intent)) return;
		super.startActivity(intent);
	}
	
	public void goHome()
	{
		Bundle b = new Bundle();
		b.putString("last", mLastPath.getPath());
		onSaveInstanceState(b);
		Intent intent = new Intent(this, OpenExplorer.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(mLastPath != null)
			intent.putExtra("last", mLastPath.getPath());
		else if(getDirContentFragment(false) != null)
			intent.putExtra("last", getDirContentFragment(false).getPath().getPath());
		intent.putExtra("state", b);
		if(OpenExplorer.BEFORE_HONEYCOMB)
		{
			finish();
			startActivity(intent);
		} else {
			setIntent(intent);
			recreate();
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(DEBUG)
			Logger.LogDebug("<-- OpenExplorer.onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		
		int contextMenuId = MenuUtils.getMenuLookupSub(v.getId());
		if(contextMenuId > -1)
			getMenuInflater().inflate(contextMenuId, menu);
		else if(v.getId() == R.id.menu_more)
			onCreateOptionsMenu(menu, false);
		else
			Logger.LogWarning("Submenu not found for " + Integer.toHexString(v.getId()));

		if(DEBUG)
			Logger.LogDebug("--> OpenExplorer.onCreateContextMenu");
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return onClick(item.getItemId(), item, null);
	}
	
	@Override
	public void invalidateOptionsMenu() {
		mLastMenuClass = "";
		if(BEFORE_HONEYCOMB && !USE_PRETTY_MENUS) return;
		if(USE_PRETTY_MENUS)
			setupBaseBarButtons();
		if(!BEFORE_HONEYCOMB)
		try {
			super.invalidateOptionsMenu();
		} catch(Exception e) {
			Logger.LogError("Unable to invalidateOptionsMenu", e);
			
		}
	}
	
	public void setupBaseBarButtons() {
		
		//if(!shouldFlushMenu(mMainMenu)) return;
		
		if(Build.VERSION.SDK_INT > 13 && usingSplitActionBar()) {
			if(DEBUG)
				Logger.LogDebug("No need to setupBaseBarButtons, split bar is being used!");
			return;
		}
		
		if(mMainMenu == null)
			mMainMenu = new MenuBuilder(this);
		else
			mMainMenu.clearAll();

		try {
			onCreateOptionsMenu(mMainMenu, false);
			onPrepareOptionsMenu(mMainMenu);
			setupBaseBarButtons(mMainMenu, false);
		} catch(Exception e) {
			Logger.LogError("Couldn't set up base bar.", e);
		}
	}
	
	public static int getVisibleChildCount(ViewGroup parent)
	{
		int ret = 0;
		for(int i=0; i<parent.getChildCount(); i++)
			if(parent.getChildAt(i).getVisibility() != View.GONE)
				ret++;
		return ret;
	}
	private boolean shouldFlushMenu(Menu menu)
	{
		if(menu == null) return true;
		if(!menu.hasVisibleItems()) return true;
		OpenFragment f = getSelectedFragment();
		if(f == null) return false;
		return !f.getClassName().equals(mLastMenuClass);
	}
	public void setupBaseBarButtons(Menu menu, boolean flush)
	{
		if(flush) mLastMenuClass = "";
		TableLayout mBaseBar = (TableLayout)findViewById(R.id.base_bar);
		mToolbarButtons = (ViewGroup)findViewById(R.id.base_row);
		mStaticButtons = (ViewGroup)findViewById(R.id.title_static_buttons);
		OpenFragment f = getSelectedFragment();
		boolean topButtons = false;
		if(!getResources().getBoolean(R.bool.allow_split_actionbar)
				|| !(getSetting(null, "pref_basebar", true) || mBaseBar == null || mToolbarButtons == null) && findViewById(R.id.title_buttons) != null)
		{
			mToolbarButtons = (ViewGroup)findViewById(R.id.title_buttons);
			if(mToolbarButtons == null && !BEFORE_HONEYCOMB)
				mToolbarButtons = (ViewGroup)getActionBar().getCustomView().findViewById(R.id.title_buttons);
			if(mBaseBar != null)
				mBaseBar.setVisibility(View.GONE);
			topButtons = true;
		}
		if(!shouldFlushMenu(menu)) return;
		USE_SPLIT_ACTION_BAR = !topButtons;
		if(mToolbarButtons != null)
		{
			mToolbarButtons.removeAllViews();
			//if(!topButtons) mToolbarButtons.measure(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height));
			
			int i = -1;
			int btnWidth = getResources().getDimensionPixelSize(R.dimen.actionbar_compat_button_width) + (getResources().getDimensionPixelSize(R.dimen.vpi_padding_horizontal) * 2); // (int)(16 * getResources().getDimension(R.dimen.one_dp));
			int tblWidth = mToolbarButtons.getWidth();
			if(tblWidth <= 0 && !topButtons)
				tblWidth = getWindowWidth();
			if(topButtons || tblWidth <= 0 || tblWidth > getWindowWidth() || !getResources().getBoolean(R.bool.ignore_max_base_buttons))
				tblWidth = btnWidth * getResources().getInteger(R.integer.max_base_buttons);
			ArrayList<View> buttons = new ArrayList<View>();
			buttons.addAll(ViewUtils.findChildByClass(mToolbarButtons, ImageButton.class));
			boolean maxedOut = false;
			while(++i < menu.size())
			{
				if(buttons.size() * btnWidth >= tblWidth)
				{
					maxedOut = true;
					Logger.LogDebug("Base bar full after #" + i + " ~ " + buttons.size() + " (" + (buttons.size() * btnWidth) + ">" + tblWidth + ")!");
					break;
				} else if(menu.getItem(i) instanceof MenuItemImpl)
				{
					final MenuItemImpl item = (MenuItemImpl) menu.getItem(i);
					//if(item.getItemId() == R.id.title_menu) break;
					if(!item.isCheckable() && item.isVisible())
					{
						View btn = makeMenuButton(item, mToolbarButtons);
						if(item.hasSubMenu())
							btn.setTag(item.getSubMenu());
						else if(!BEFORE_HONEYCOMB && item.getActionView() != null)
						{
							if(DEBUG)
								Logger.LogDebug("ACTION VIEW!!!");
							btn = item.getActionView();
							//ActionBarHelper h = ActionBarHelper.createInstance(this);
						}
						buttons.add(btn);
						if(i > 0)
							btn.setNextFocusLeftId(menu.getItem(i - 1).getItemId());
						if(i < menu.size() - 1)
							btn.setNextFocusRightId(menu.getItem(i + 1).getItemId());
						if(!USE_PRETTY_MENUS || topButtons)
							btn.setOnCreateContextMenuListener(this);
						menu.getItem(i).setVisible(false);
						btn.setOnClickListener(this);
						btn.setOnFocusChangeListener(this);
						btn.setOnKeyListener(this);
						if(mToolbarButtons.findViewById(menu.getItem(i).getItemId()) == null)
							mToolbarButtons.addView(btn);
						//menu.removeItem(item.getItemId());
						if(DEBUG)
							Logger.LogDebug("Added " + item.getTitle() + " to base bar.");
					} 
					//else Logger.LogWarning(item.getTitle() + " should not show. " + item.getShowAsAction() + " :: " + item.getFlags());
				}
			}
			mToolbarButtons.setVisibility(View.VISIBLE);
			mLastMenuClass = f.getClassName();
			if(MenuUtils.countVisibleMenus(mMainMenu) > 0)
			{
				if(maxedOut && buttons.size() > 0)
				{
					View old = buttons.remove(buttons.size() - 1);
					MenuUtils.setMenuVisible(mMainMenu, true, old.getId());
					mToolbarButtons.removeView(old);
				}
				final ImageButton btn = (ImageButton)getLayoutInflater().inflate(R.layout.toolbar_button, mToolbarButtons);
				btn.setImageResource(R.drawable.ic_menu_more);
				//btn.measure(getResources().getDimensionPixelSize(R.dimen.actionbar_compat_button_home_width), getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height));
				btn.setId(R.id.menu_more);
				if(buttons.size() > 0)
				{
					buttons.get(buttons.size() - 1).setNextFocusRightId(R.id.menu_more);
					btn.setNextFocusLeftId(buttons.get(buttons.size() - 1).getId());
				}
				btn.setOnKeyListener(this);
				btn.setOnClickListener(this);
				btn.setOnLongClickListener(this);
				btn.setFocusable(true);
				btn.setOnFocusChangeListener(this);
				buttons.add(btn);
				mToolbarButtons.addView(btn);
			}
			if(buttons.size() > 0)
			{
				View last = buttons.get(buttons.size() - 1);
				last.setNextFocusRightId(android.R.id.home);
				if(findViewById(android.R.id.home) != null)
					findViewById(android.R.id.home).setNextFocusLeftId(last.getId());
			}
			
			Logger.LogDebug("Added " + buttons.size() + " children to Base Bar.");
			if(mBaseBar != null)
			{
				if(buttons.size() < 1)
					mBaseBar.setVisibility(View.GONE);
				else mBaseBar.setStretchAllColumns(true);
			}
		} else if(BEFORE_HONEYCOMB) Logger.LogWarning("No Base Row!?");
	}

	private ImageButton makeMenuButton(final MenuItem item, ViewGroup parent)
	{
		ImageButton btn = (ImageButton)getLayoutInflater()
				.inflate(R.layout.toolbar_button, parent, false);
		if(!item.isVisible())
			btn.setVisibility(View.GONE);
		Drawable d = item.getIcon();
		if(d instanceof BitmapDrawable)
			((BitmapDrawable)d).setGravity(Gravity.CENTER);
		btn.setImageDrawable(d);
		btn.setId(item.getItemId());
		btn.setOnClickListener(this);
		btn.setLongClickable(true);
		btn.setFocusable(true);
		btn.setOnFocusChangeListener(this);
		btn.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showToast(item.getTitle());
				return true;
			}
		});
		return btn;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return onCreateOptionsMenu(menu, true);
	}
	public boolean onCreateOptionsMenu(Menu menu, boolean fromSystem)
	{
		MenuUtils.scanMenuShortcuts(menu, getMenuInflater());
		//mActionBarHelper.onCreateOptionsMenu(menu);
		OpenFragment frag = getSelectedFragment();
		
		//if(menu == null) menu = new MenuBuilder(this);
		menu.clear();
		
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onCreateOptionsMenu(" + menu + "," + fromSystem + ")");
		//getMenuInflater().inflate(R.menu.global_top, menu);
		if(frag != null) // && frag.hasOptionsMenu())
			frag.onCreateOptionsMenu(menu, getMenuInflater());
		getMenuInflater().inflate(R.menu.global, menu);
		
		if(!USE_PRETTY_MENUS) {
			MenuUtils.setMenuVisible(menu, false, R.id.menu_more);
			return true;
		}
		
		/*
		if(!BEFORE_HONEYCOMB)
			for(int i = 0; i < menu.size(); i++)
			{
				MenuItem item = menu.getItem(i);
				if(item.getActionView() == null)
					item.setActionView(makeMenuButton(item, null));
			}
		*/
	
		if(mOptsMenu == null)
			mOptsMenu = new MenuBuilder(this);
		mOptsMenu.clear();
		mOptsMenu.setQwertyMode(true);
		MenuUtils.transferMenu(menu, mOptsMenu, false);
		MenuUtils.setMenuVisible(mOptsMenu, false, R.id.menu_more);
		MenuUtils.hideMenuGrandChildren(mOptsMenu);
		
		if(!USE_PRETTY_MENUS) {
			handleMoreMenu(menu, false);
			MenuUtils.fillSubMenus(menu, getMenuInflater());
		} else { // if(isGTV()) {
			if(isGTV())
			{
				handleMoreMenu(mMainMenu, true, 6); //*/
				if(!menu.equals(mMainMenu))
					menu.clear();
				else MenuUtils.fillSubMenus(menu, getMenuInflater());
			}
			else {
				handleMoreMenu(menu, true);
				if(!menu.equals(mMainMenu) && !getResources().getBoolean(R.bool.allow_split_actionbar))
					MenuUtils.setMenuVisible(menu, false);
				//else fillSubMenus(menu, getMenuInflater());
			}
		} //else MenuUtils.setMenuVisible(menu, false, R.id.menu_more);
		/*else {
			fillSubMenus(mMainMenu, getMenuInflater());
			handleMoreMenu(menu, false);
		}*/
		
		return true;
	}
	
	private void handleMoreMenu(Menu menu, boolean forceFromToolbar)
	{
		int max = getResources().getInteger(R.integer.max_base_buttons);
		if(getResources().getBoolean(R.bool.ignore_max_base_buttons))
		{
			if(DEBUG) Logger.LogDebug("Ignoring max base buttons config (" + max + ")");
			max = (int)Math.floor(Math.min(6, getWindowWidth() / getResources().getDimension(R.dimen.actionbar_compat_button_width)));
		} else if(DEBUG) Logger.LogDebug("Max Base Buttons: " + max);
		handleMoreMenu(menu, forceFromToolbar, max);
	}
	private void handleMoreMenu(Menu menu, boolean forceFromToolbar, int max)
	{
		if(forceFromToolbar || (menu.size() > max && Build.VERSION.SDK_INT > 13 && getWindowWidth() < 700))
		{
			MenuItem more = menu.findItem(R.id.menu_more);
			if(more != null)
			{
				SubMenu moreSub = more.getSubMenu();
				if(moreSub != null)
				{
					for(int i = Math.max(0, max - 1); i < menu.size(); i++)
					{
						MenuItem item = menu.getItem(i);
						if(item.getItemId() == R.id.menu_more) continue;
						MenuItem ni = MenuUtils.transferMenu(item, moreSub);
						if(ni == null) continue;
						ni.setAlphabeticShortcut((char)('a' + i));
						item.setVisible(false);
					}
					MenuUtils.scanMenuShortcuts(moreSub);
				}
			}
			
		} else MenuUtils.setMenuVisible(menu, false, R.id.menu_more);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		//Logger.LogVerbose("OpenExplorer.onPrepareOptionsMenu");
		
		if(getClipboard() != null)
		{
			MenuUtils.setMenuChecked(menu, getClipboard().isMultiselect(), R.id.menu_multi);
			MenuUtils.setMenuVisible(menu, getClipboard().size() > 0, R.id.content_paste);
		} else
			MenuUtils.setMenuVisible(menu,  false, R.id.content_paste);
		
		MenuUtils.setMenuVisible(menu, IS_DEBUG_BUILD && !isBlackBerry(), R.id.menu_debug);
		
		if(!BEFORE_HONEYCOMB && USE_ACTION_BAR)
		{
			//MenuUtils.setMenuVisible(menu, false, R.id.title_menu);
			if(menu.findItem(R.id.menu_search) != null)
			{
				if(mSearchView == null)
					mSearchView = SearchViewCompat.newSearchView(this);
				MenuItem item = menu.findItem(R.id.menu_search);
				MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
				MenuItemCompat.setActionView(item, mSearchView);
				if(mSearchView != null)
					SearchViewCompat.setOnQueryTextListener(mSearchView,
						new SearchViewCompat.OnQueryTextListenerCompat() {
						public boolean onQueryTextSubmit(String query) {
							mSearchView.clearFocus();
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_SEARCH);
							Bundle appData = new Bundle();
							appData.putString("path", getDirContentFragment(false).getPath().getPath());
							intent.putExtra(SearchManager.APP_DATA, appData);
							intent.putExtra(SearchManager.QUERY, query);
							handleIntent(intent);
							return true;
						}
						public boolean onQueryTextChange(String newText) {
							return false;
						}
					});
			}
		}
		
		MenuUtils.setMenuChecked(menu, USE_SPLIT_ACTION_BAR, R.id.menu_view_split);
		//MenuUtils.setMenuChecked(menu, mLogFragment != null && mLogFragment.isVisible(), R.id.menu_view_logview);
		MenuUtils.setMenuChecked(menu, getPreferences().getBoolean("global", "pref_fullscreen", false), R.id.menu_view_fullscreen);
		if(!getResources().getBoolean(R.bool.allow_fullscreen))
			MenuUtils.setMenuVisible(menu, false, R.id.menu_view_fullscreen);
		else MenuUtils.setMenuChecked(menu, IS_FULL_SCREEN, R.id.menu_view_fullscreen);
		if(getWindowWidth() < 500 && Build.VERSION.SDK_INT < 14) // ICS can split the actionbar
		{
			MenuUtils.setMenuShowAsAction(menu, 0 // Never
					, R.id.menu_sort, R.id.menu_view, R.id.menu_new_folder);
			MenuUtils.setMenuVisible(menu, true, R.id.menu_more);
		}
		
		//if(BEFORE_HONEYCOMB)
		{
			OpenFragment f = getSelectedFragment();
			if(f != null && f.hasOptionsMenu() && !f.isDetached() && f.isVisible())
				f.onPrepareOptionsMenu(menu);
		}
		
		if(menu != null && menu.findItem(R.id.content_paste) != null && getClipboard() != null && getClipboard().size() > 0)
		{
			SubMenu sub = menu.findItem(R.id.content_paste).getSubMenu();
			if(sub != null)
			{
				int i = 0;
				for(final OpenPath item : getClipboard().getAll())
				{
					sub.add(Menu.CATEGORY_CONTAINER, i++, i, item.getName())
						.setCheckable(true)
						.setChecked(true)
						.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem menuitem) {
								getClipboard().remove(item);
								return true;
							}
						})
						.setIcon(ThumbnailCreator.getDefaultResourceId(item, 32, 32));
				}
			}
		}
		
		if(!CAN_DO_CAROUSEL)
			MenuUtils.setMenuVisible(menu, false, R.id.menu_view_carousel);
		
		//if(BEFORE_HONEYCOMB)
		//	setupBaseBarButtons(menu, false);
		
		return true;
	}
	
	

	
	
	/*
	private void hideMenuIfVisible(Menu menu, int menuId, int viewId) {
		View v = findViewById(viewId);
		if(v != null && v.isShown())
			MenuUtils.setMenuVisible(menu, false, menuId);
		else
			MenuUtils.setMenuVisible(menu, true, menuId);
	}
	*/
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		Logger.LogDebug("OpenExplorer.onMenuOpened(0x" + Integer.toHexString(featureId) + "," + menu + ")");
		if(USE_PRETTY_MENUS)
		{
			if(menu != null && !menu.equals(mMainMenu) && !menu.equals(mOptsMenu))
				menu.close();
			return false;
		}
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onOptionsItemSelected(" + item + ")");
		if(item.getItemId() == R.id.menu_more && isGTV())
		{
			showMenu(mOptsMenu, findViewById(item.getItemId()), false);
			return true;
		}
		
		if(item.getSubMenu() != null)
		{
			onPrepareOptionsMenu(item.getSubMenu());
			if(!USE_PRETTY_MENUS)
				return false;
			else
			{
				View anchor = findViewById(item.getItemId());
				if(anchor == null && !BEFORE_HONEYCOMB && item.getActionView() != null)
					anchor = item.getActionView();
				if(anchor == null && !BEFORE_HONEYCOMB)
				{
					anchor = getActionBar().getCustomView();
					if(anchor.findViewById(item.getItemId()) != null)
						anchor = anchor.findViewById(item.getItemId());
				}
				if(anchor == null)
					anchor = mToolbarButtons;
				if(anchor == null)
					anchor = findViewById(android.R.id.home);
				if(anchor == null && !BEFORE_HONEYCOMB && USE_ACTION_BAR)
					anchor = getActionBar().getCustomView().findViewById(android.R.id.home);
				if(anchor == null)
					anchor = getCurrentFocus().getRootView();
				OpenFragment f = getSelectedFragment();
				if(f != null)
					if(f.onClick(item.getItemId(), anchor))
						return true;
			}
		}
		
		if(item.isCheckable())
			item.setChecked(item.getGroupId() > 0 ? true : !item.isChecked());
		
		OpenFragment f = getSelectedFragment();
		if(f != null && f.onOptionsItemSelected(item))
			return true;
		
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onOptionsItemSelected(0x" + Integer.toHexString(item.getItemId()) + ")");
		
		return onClick(item.getItemId(), item, null);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v == null) return;
		int id = v.getId();
		if(id == R.id.menu_more) {
			if(USE_PRETTY_MENUS && showIContextMenu(mOptsMenu, v, true) != null)
				return;
		}
		if(v.getTag() instanceof Menu && ((Menu)v.getTag()).size() > 0)
		{
			if(USE_PRETTY_MENUS && showIContextMenu((Menu)v.getTag(), v, true) != null)
				return;
		}
		OpenFragment f = getSelectedFragment();
		if(f != null && f.onClick(id, v)) return;
		if(v.getTag() != null && v.getTag() instanceof MenuItem && id != ((MenuItem)v.getTag()).getItemId())
		{
			id = ((MenuItem)v.getTag()).getItemId();
			if(f.onClick(id, v)) return;
			if(MenuUtils.getMenuLookupID(id) > -1 && showMenu(MenuUtils.getMenuLookupSub(id), v, true)) return;
		}
		if(USE_PRETTY_MENUS || !v.showContextMenu())
			onClick(id, null, v);
	}
	
	public boolean onClick(int id, MenuItem item, View from)
	{
		super.onClick(id);
		if(from == null || !from.isShown())
			from = findViewById(id);
		if(id != R.id.title_icon && id != android.R.id.home);
			toggleBookmarks(false);
		OpenFragment f = getSelectedFragment();
		if(f != null && f.onClick(id, from))
			return true;
		if(item != null && f != null && f.onOptionsItemSelected(item))
			return true;
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onClick(0x" + Integer.toHexString(id) + "," + item + "," + from + ")");
		switch(id)
		{
			case R.id.menu_debug:
				debugTest();
				break;
			case R.id.title_icon:
			case android.R.id.home:
				toggleBookmarks();
				return true;
				
			case R.id.menu_multi:
				if(getClipboard().isMultiselect())
				{
					getClipboard().stopMultiselect();
					//getClipboard().clear();
					if(!BEFORE_HONEYCOMB && mActionMode != null)
						((ActionMode)mActionMode).finish();
					return true;
				}
				
				if(BEFORE_HONEYCOMB || !USE_ACTIONMODE)
				{
					getClipboard().startMultiselect();
				} else {
					mActionMode = startActionMode(new ActionMode.Callback() {
						
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}
						public void onDestroyActionMode(ActionMode mode) {
							getClipboard().clear();
							mActionMode = null;
						}
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							mode.setTitle(getString(R.string.s_menu_multi) + ": " + getClipboard().size() + " " + getString(R.string.s_files));
							mode.getMenuInflater().inflate(R.menu.multiselect, menu);
							//MenuUtils.setMenuVisible(menu, false, R.id.menu_context_paste, R.id.menu_context_unzip);
							getDirContentFragment(true).changeMultiSelectState(true);
							return true;
						}
						public boolean onActionItemClicked(ActionMode mode, MenuItem item)
						{
							if(getClipboard().size() < 1)
							{
								mode.finish();
								return true;
							}
							OpenPath file = getClipboard().get(0); //getMultiSelectHandler().getSelectedFiles();
							
							getClipboard().clear();
						
							return getDirContentFragment(false)
									.executeMenu(item.getItemId(), mode, file);
						}
					});
				}
				return true;
								
				
			case R.id.menu_view_carousel:
				changeViewMode(OpenExplorer.VIEW_CAROUSEL, true);
				return true;
				
			case R.id.menu_view_grid:
				changeViewMode(OpenExplorer.VIEW_GRID, true);
				return true;
				
			case R.id.menu_view_list:
				changeViewMode(OpenExplorer.VIEW_LIST, true);
				return true;

			case R.id.menu_view_fullscreen:
				getPreferences().setSetting("global", "pref_fullscreen", 
						!getPreferences().getSetting("global", "pref_fullscreen", false));
				goHome();
				return true;

			case R.id.menu_view_split:
				setSetting("pref_basebar", !USE_SPLIT_ACTION_BAR);
				goHome();
				return true;
				
			//case R.id.menu_global_ops_text:
			//case R.id.menu_global_ops_icon:
			case R.id.title_ops:
				refreshOperations();
				showLogFrag(mOpsFragment, true);
				checkTitleSeparator();
				return true;
				
			case R.id.title_log:
				if(mLogFragment == null)
					mLogFragment = new LogViewerFragment();
				showLogFrag(mLogFragment, true);
				sendToLogView(null, 0);
				return true;
					
			/*case R.id.menu_root:
				if(RootManager.Default.isRoot())
				{
					getPreferences().setSetting("global", "pref_root", false);
					showToast(getString(R.string.s_menu_root_disabled));
					RootManager.Default.exitRoot();
					item.setChecked(false);
				} else
				{
					if(RootManager.Default.isRoot() || RootManager.Default.requestRoot())
					{
						getPreferences().setSetting("global", "pref_root", true);
						showToast(getString(R.string.s_menu_root) + "!");
						item.setTitle(getString(R.string.s_menu_root) + "!");
					} else {
						item.setChecked(false);
						showToast("Unable to achieve root.");
					}
				}
				return true;
				
			case R.id.menu_flush:
				ThumbnailCreator.flushCache(getApplicationContext(), true);
				OpenPath.flushDbCache();
				goHome();
				return true;*/

			case R.id.menu_refresh:
				ContentFragment content = getDirContentFragment(true);
				if(content != null)
				{
					if(DEBUG && IS_DEBUG_BUILD)
						Logger.LogDebug("Refreshing " + content.getPath().getPath());
					FileManager.removeOpenCache(content.getPath().getPath());
					content.getPath().deleteFolderFromDb();
					content.runUpdateTask(true);
					changePath(content.getPath(), false, true);
				}
				mBookmarks.refresh();
				return true;
				
			case R.id.menu_settings:
				showPreferences(null);
				return true;
			
			case R.id.menu_search:
				onSearchRequested();
				return true;

			/*case R.id.menu_favorites:
				toggleBookmarks();
				return true;*/
			
			case R.id.menu_multi_all_delete:
				DialogHandler.showConfirmationDialog(this,
						getResources().getString(R.string.s_confirm_delete, getClipboard().getCount() + " " + getResources().getString(R.string.s_files)),
						getResources().getString(R.string.s_menu_delete_all),
						new DialogInterface.OnClickListener() { // yes
							public void onClick(DialogInterface dialog, int which) {
								getEventHandler().deleteFile(getClipboard(), OpenExplorer.this, false);
							}
						});
				break;
				
			case R.id.menu_multi_all_clear:
				getClipboard().clear();
				return true;

			case R.id.menu_multi_all_copy:
				getClipboard().DeleteSource = false;
				getDirContentFragment(false).executeMenu(R.id.content_paste, null, getDirContentFragment(false).getPath());
				break;
				
			case R.id.menu_multi_all_move:
				getClipboard().DeleteSource = true;
				getDirContentFragment(false).executeMenu(R.id.content_paste, null, getDirContentFragment(false).getPath());
				break;
				
			case R.id.title_paste:
			case R.id.title_paste_icon:
			case R.id.title_paste_text:
			case R.id.content_paste:
				//if(BEFORE_HONEYCOMB)
				getClipboard().setCurrentPath(getCurrentPath());
				onClipboardDropdown(from);
				return true;
				
				//getDirContentFragment(false).executeMenu(R.id.menu_paste, null, mLastPath, mClipboard);
				//return true;
				
			case R.id.menu_about:
				DialogHandler.showAboutDialog(this);
				return true;
				
			case R.id.menu_exit:
				DialogHandler.showConfirmationDialog(this,
						getString(R.string.s_alert_exit),
						getString(R.string.s_menu_exit),
						getPreferences(), "exit",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				return true;
			case R.id.menu_more:
				showMenu(mOptsMenu, ViewUtils.getFirstView(this, R.id.menu_more, R.id.base_bar, R.id.title_buttons, android.R.id.home), true);
				return true;
				
			default:
				if(f instanceof ContentFragment)
				{
					ContentFragment cf = (ContentFragment)f;
					if(item.getMenuInfo() != null && cf.onContextItemSelected(item))
						return true;
					else if(cf.onClick(id, from)) return true;
					else if(cf.onOptionsItemSelected(item)) return true;
					return cf.executeMenu(id, null, getDirContentFragment(false).getPath());
				}
				else if(f instanceof TextEditorFragment)
					((TextEditorFragment)f).onClick(id, from);
				else if(f.onOptionsItemSelected(item)) return true;
		}
		
		//showToast("oops");
		return false;
		//return super.onOptionsItemSelected(item);
	}
	
	private void showLogFrag(OpenFragment frag, boolean toggle)
	{
		View frag_log = findViewById(R.id.frag_log);
		ViewUtils.setViewsVisible(this, true, R.id.title_log);
		if(frag_log == null)
		{
			BetterPopupWindow pw = ((Poppable)frag).getPopup();
			if(!pw.hasShown() || toggle)
				pw.showLikePopDownMenu();
		} else {
			boolean isVis = frag_log.getVisibility() == View.VISIBLE;
			boolean isFragged = false;
			Fragment fl = fragmentManager.findFragmentById(R.id.frag_log);
			if(fl != null && fl.equals(frag))
				isFragged = true;
			if(isFragged)
			{
				if(toggle)
				{
					Logger.LogDebug("OpenExplorer.showLogFrag : Toggling " + frag.getTitle());
					ViewUtils.setViewsVisible(frag_log, !isVis);
				}
			} else if(isVis)
			{
				Logger.LogDebug("OpenExplorer.showLogFrag : Adding " + frag.getTitle());
				fragmentManager.beginTransaction()
					.replace(R.id.frag_log, frag)
					.disallowAddToBackStack()
					.commitAllowingStateLoss();
				ViewUtils.setViewsVisible(frag_log, true);
			} else {
				Logger.LogDebug("OpenExplorer.showLogFrag : Showing " + frag.getTitle());
				ViewUtils.setViewsVisible(frag_log, true);
			}
		}
	}
	
	private void debugTest() {
		//startActivity(new Intent(this, Authenticator.class));
	}
	
	public boolean isSinglePane() { return mSinglePane; }

	private void onClipboardDropdown(View anchor)
	{
		ViewUtils.setViewsVisible(mStaticButtons, true, R.id.title_paste);
		if(anchor == null)
			anchor = ViewUtils.getFirstView(this,
				R.id.title_paste, R.id.title_icon, R.id.frag_holder);
		
		final BetterPopupWindow clipdrop = new BetterPopupWindow(this, anchor);
		View root = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.multiselect, null);
		GridView mGridCommands = (GridView)root.findViewById(R.id.multiselect_command_grid);
		final ListView mListClipboard = (ListView)root.findViewById(R.id.multiselect_item_list);
		mListClipboard.setAdapter(getClipboard());
		mListClipboard.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> list, View view, final int pos, long id) {
				//OpenPath file = mClipboard.get(pos);
				//if(file.getParent().equals(mLastPath))
				Animation anim = AnimationUtils.loadAnimation(OpenExplorer.this,
						R.anim.slide_out_left);
				//anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
				list.getChildAt(pos).startAnimation(anim);
				new Handler().postDelayed(new Runnable(){public void run() {
					getClipboard().remove(pos);
					mListClipboard.invalidate();
					if(getClipboard().getCount() == 0)
						clipdrop.dismiss();
				}}, anim.getDuration());
				//else
					//getEventHandler().copyFile(file, mLastPath, OpenExplorer.this);
			}
		});
		final Menu menu = IconContextMenu.newMenu(this, R.menu.multiselect);
		MenuUtils.setMenuChecked(menu, getClipboard().isMultiselect(), R.id.menu_multi);
		MenuUtils.setMenuEnabled(menu, getClipboard().hasPastable(), R.id.menu_multi_all_copy, R.id.menu_multi_all_copy, R.id.menu_multi_all_move);
		final IconContextMenuAdapter cmdAdapter = new IconContextMenuAdapter(this, menu);
		cmdAdapter.setTextLayout(R.layout.context_item);
		mGridCommands.setAdapter(cmdAdapter);
		mGridCommands.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
				MenuItem item = menu.getItem(pos);
				onClick(item.getItemId(), item, view);
				clipdrop.dismiss();
			}
		});
		
		float w = getResources().getDimension(R.dimen.popup_width) * (3 / 2);
		if(w > getWindowWidth())
			w = getWindowWidth() - 20;
		clipdrop.setPopupWidth((int)w);
		clipdrop.setContentView(root);
		
		clipdrop.showLikePopDownMenu();
		//dropdown.setAdapter(this, new IconContextMenuAdapter(context, menu))
		//BetterPopupWindow win = new BetterPopupWindow(this, anchor);
		//ListView list = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.multiselect, null);
		//win.setContentView(root)
	}
	
	public OpenPath getCurrentPath() {
		OpenFragment f = getSelectedFragment();
		if(f instanceof OpenPathFragmentInterface)
			return ((OpenPathFragmentInterface)f).getPath();
		return null;
	}
	
	public void changeViewMode(int newView, boolean doSet) {
		int mViewMode = getSetting(getCurrentPath(), "view", 0);
		if(mViewMode == newView) {
			Logger.LogWarning("changeViewMode called unnecessarily! " + newView + " = " + mViewMode);
			//return;
		}
		//Logger.LogVerbose("Changing view mode to " + newView);
		int oldView = mViewMode;
		if(newView == VIEW_CAROUSEL && !CAN_DO_CAROUSEL)
			newView = oldView;
		//setViewMode(newView);
		if(doSet)
			setSetting(getCurrentPath(), "view", newView);
		if(!mSinglePane)
		{
			if(oldView == VIEW_CAROUSEL && mViewPagerEnabled)
			{
				setViewVisibility(false, false, R.id.content_frag);
				setViewVisibility(true, false, R.id.content_pager_frame);
				changePath(getCurrentPath(), false);
			} else if(newView == VIEW_CAROUSEL && mViewPagerEnabled)
			{
				setViewVisibility(false, false, R.id.content_pager_frame);
				setViewVisibility(true, false, R.id.content_frag);
				changePath(getCurrentPath(), false);
			}
			ContentFragment cf = getDirContentFragment(true);
			if(cf != null)
				cf.onViewChanged(newView);
			if(!BEFORE_HONEYCOMB)
				invalidateOptionsMenu();
		} else if(newView == VIEW_CAROUSEL && oldView != VIEW_CAROUSEL && CAN_DO_CAROUSEL)
		{
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogDebug("Switching to carousel!");
			if(mViewPagerEnabled)
			{
				setViewVisibility(false, false, R.id.content_pager_indicator,
						R.id.content_pager_frame_stub, R.id.content_pager);
				setViewVisibility(true, false, R.id.content_frag);
			}
			OpenPath path = getDirContentFragment(false).getPath();
			fragmentManager.beginTransaction()
				.replace(R.id.content_frag, new CarouselFragment(path))
				.setBreadCrumbTitle(path.getPath())
				.commit();
			updateTitle(path.getPath());
		} else if (oldView == VIEW_CAROUSEL && newView != VIEW_CAROUSEL && CAN_DO_CAROUSEL) { // if we need to transition from carousel
			if(DEBUG && IS_DEBUG_BUILD)
				Logger.LogDebug("Switching from carousel!");
			if(mViewPagerEnabled)
			{
				setViewVisibility(true, false, R.id.content_frag);
				setViewVisibility(false, false, R.id.content_pager_frame_stub,
						R.id.content_pager, R.id.content_pager_indicator);
				changePath(getCurrentPath(), false, true);
			} else {
				fragmentManager.beginTransaction()
					.replace(R.id.content_frag, ContentFragment.getInstance(getCurrentPath(), mViewMode, getSupportFragmentManager()))
					.setBreadCrumbTitle(getCurrentPath().getAbsolutePath())
					//.addToBackStack(null)
					.commit();
				updateTitle(getCurrentPath().getPath());
			}
			
			invalidateOptionsMenu();
		} else {
			getDirContentFragment(true).onViewChanged(newView);
			if(!BEFORE_HONEYCOMB)
				invalidateOptionsMenu();
		}
	}
	
	public void showPreferences(OpenPath path)
	{
		if(Build.VERSION.SDK_INT > 100)
		{
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.hide(fragmentManager.findFragmentById(R.id.content_frag));
			//ft.replace(R.id.content_frag, new PreferenceFragment(this, path));
			ft.setBreadCrumbTitle("prefs://" + (path != null ? path.getPath() : ""));
			ft.addToBackStack("prefs");
			ft.commit();
			final PreferenceFragmentV11 pf2 = new PreferenceFragmentV11(path);
			getFragmentManager().addOnBackStackChangedListener(new android.app.FragmentManager.OnBackStackChangedListener() {
				
				public void onBackStackChanged() {
					//android.app.FragmentTransaction ft3 = getFragmentManager().beginTransaction();
					Logger.LogDebug("hide me!");
					//getFragmentManager().removeOnBackStackChangedListener(this);
					if(pf2 != null && pf2.getView() != null && getFragmentManager().getBackStackEntryCount() == 0)
						pf2.getView().setVisibility(View.GONE);
				}
			});
			android.app.FragmentTransaction ft2 = getFragmentManager().beginTransaction();
			ft2.replace(R.id.content_frag, pf2);
			ft2.addToBackStack("prefs");
			ft2.commit();
		} else {
			Intent intent = new Intent(this, SettingsActivity.class);
			if(path != null)
				intent.putExtra("path", path.getPath());
			startActivityForResult(intent, REQ_PREFERENCES);
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onKeyUp(" + keyCode + "," + event + ")");
		if(event.getAction() != KeyEvent.ACTION_UP)
			return super.onKeyUp(keyCode, event);
		if(MenuUtils.getMenuShortcut(event) != null)
		{
			if(getCurrentFocus() != null)
			{
				View cf = getCurrentFocus();
				if(cf instanceof TextView)
					return false;
			}
			MenuItem item = MenuUtils.getMenuShortcut(event);
			if(item != null)
				if(onOptionsItemSelected(item))
				{
					showToast(item.getTitle(), Toast.LENGTH_SHORT);
					return true;
				}
		}
		if(keyCode == KeyEvent.KEYCODE_MENU && USE_PRETTY_MENUS)
		{
			View more = findViewById(R.id.menu_more);
			if(more != null && more.isShown() &&
					more.isClickable() && more.performClick())
			{
				Logger.LogDebug("!SHOULD BE HERE!");
				return true;
			} else {
				Logger.LogWarning("Couldn't find \"More Menu\"!");
				MenuBuilder menu = new MenuBuilder(this);
				getSelectedFragment().onCreateOptionsMenu(menu, getMenuInflater());
				getMenuInflater().inflate(R.menu.global, menu);
				onPrepareOptionsMenu(menu);
				showMenu(menu, getCurrentFocus(), true);
			}
		} else if(keyCode == KeyEvent.KEYCODE_BOOKMARK)
		{
			OpenPath path = getDirContentFragment(false).getPath();
			if(mBookmarks.hasBookmark(path))
				addBookmark(path);
			else
				removeBookmark(path);
		} else if(keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9)
		{
			int pos = keyCode - KeyEvent.KEYCODE_1;
			if(mToolbarButtons != null)
			{
				if(pos < mToolbarButtons.getChildCount()
						&& mToolbarButtons.getChildAt(pos).performClick())
					return true;
				return false;
			}
			if(mOptsMenu != null && pos < mOptsMenu.size())
				return onOptionsItemSelected(mOptsMenu.getItem(pos));
			if(mMainMenu != null && pos < mMainMenu.size())
				return onOptionsItemSelected(mMainMenu.getItem(pos));
		}
		/*
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mBackQuit) {
				return super.onKeyUp(keyCode, event);
			} else {
				Toast.makeText(this, "Press back again to quit", Toast.LENGTH_SHORT).show();
				mBackQuit = true;
				return true;
			}		
		}*/
		return super.onKeyUp(keyCode, event);
	}
		
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_PREFERENCES)
		{
			if(resultCode == RESULT_RESTART_NEEDED) {
				showToast(R.string.s_alert_restart);
				goHome(); // just restart
			} else {
				loadPreferences();
				refreshBookmarks();
				notifyPager();
				getDirContentFragment(false).refreshData();
				invalidateOptionsMenu();
			}
		} else if (requestCode == REQ_SPLASH) {
			if(resultCode == RESULT_OK && data != null && data.hasExtra("start"))
			{
				String start = data.getStringExtra("start");
				getPreferences().setSetting("global", "pref_splash", true);
				getPreferences().setSetting("global", "pref_start", start);
				if(!start.equals(getCurrentPath().getPath()))
				{
					if("Videos".equals(start))
						changePath(mVideoParent, true);
					else if("Photos".equals(start))
						changePath(mPhotoParent, true);
					else if("External".equals(start))
						changePath(OpenFile.getExternalMemoryDrive(true).setRoot(), true);
					else if("Internal".equals(start))
						changePath(OpenFile.getInternalMemoryDrive().setRoot(), true);
					else
						changePath(new OpenFile("/").setRoot(), true);
				}
			}
		} else if (requestCode == REQ_INTENT) {
			
		} else {
			if(getSelectedFragment() != null)
				getSelectedFragment().onActivityResult(requestCode, resultCode, data);
		}
	}

	public void goBack()
	{
		//new FileIOTask().execute(new FileIOCommand(FileIOCommandType.PREV, mFileManager.getLastPath()));
		if(fragmentManager.getBackStackEntryCount() == 0) return;
		BackStackEntry entry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
		OpenPath last = null;
		if(entry != null && entry.getBreadCrumbTitle() != null)
			try {
				last = FileManager.getOpenCache(entry.getBreadCrumbTitle().toString(), false, (SortType)null);
			} catch (IOException e) { }
		if(last == null) return;
		Logger.LogDebug("Going back to " + last.getPath());
		changePath(last, false, true);
		//updateData(last.list());
	}
	
	@Override
	public void onBackPressed() {
		if(mViewPagerAdapter.getCount() > 0 && getSelectedFragment().onBackPressed())
			return;
		super.onBackPressed();
	}
	
	public void onBackStackChanged() {
		int i = fragmentManager.getBackStackEntryCount();
		final Boolean isBack = i < mLastBackIndex;
		BackStackEntry entry = null;
		if(i > 0) entry = fragmentManager.getBackStackEntryAt(i - 1);
		Logger.LogDebug("Back Stack " + i + (entry != null ? ": " + entry.getBreadCrumbTitle() : ""));
		if(isBack)
		{
			if(i > 0)
			{
				if(entry != null && entry.getBreadCrumbTitle() != null)
				{
					try {
						mLastPath = FileManager.getOpenCache(entry.getBreadCrumbTitle().toString());
					} catch (Exception e) {
						Logger.LogError("Couldn't get back cache.", e);
					}
					if(mLastPath != null)
					{
						Logger.LogDebug("last path set to " + mLastPath.getPath());
						changePath(mLastPath, false);
						//updateTitle(mLastPath.getPath());
					} else finish();
					
				} 
				else {
					fragmentManager.popBackStack();
				}
			}
			else {
				//updateTitle("");
				showToast(R.string.s_alert_back_to_exit);
			}
		}
		mLastBackIndex = i;
	}

	private void changePath(OpenPath path, Boolean addToStack) { changePath(path, addToStack, false); }
	private void changePath(OpenPath path, Boolean addToStack, Boolean force)
	{
		try {
			//if(mLastPath != null && !mLastPath.equals(path) && mLastPath instanceof OpenNetworkPath)
			//	((OpenNetworkPath)mLastPath).disconnect();
		} catch(Exception e) {
			Logger.LogError("Couldn't disconnect while changing paths.", e);
		}
		toggleBookmarks(false);
		if(path == null) path = mLastPath;
		if(path == null) return;
		if(mLastPath == null && getDirContentFragment(false) != null)
			mLastPath = getDirContentFragment(false).getPath();
		if(!(mLastPath instanceof OpenFile) || !(path instanceof OpenFile))
			force = true;
		
		onClipboardUpdate();
		
		//if(!BEFORE_HONEYCOMB) force = true;
		//if(!force)
			//if(!addToStack && path.getPath().equals("/")) return;
			//if(mLastPath.getPath().equalsIgnoreCase(path.getPath())) return;
		int newView = getSetting(path, "view", 0);
		if(!CAN_DO_CAROUSEL && newView == VIEW_CAROUSEL)
		{
			setSetting(path, "view", VIEW_LIST);
			newView = VIEW_LIST;
		}
		//boolean isNew = !mLastPath.equals(path);
		int oldView = getSetting(mLastPath, "view", 0);
		
		if(path instanceof OpenNetworkPath)
		{
			if(mLogViewEnabled)
				showLogFrag(mLogFragment, false);
		} else
			setViewVisibility(false, false, R.id.frag_log);
		
		final ImageView icon = (ImageView)findViewById(R.id.title_icon);
		if(icon != null)
			ThumbnailCreator.setThumbnail(icon, path, 96, 96,
				new OnUpdateImageListener() {
					public void updateImage(Bitmap b) {
						BitmapDrawable d = new BitmapDrawable(getResources(), b);
						d.setGravity(Gravity.CENTER);
						icon.setImageDrawable(d);
					}
				});
		
		//mFileManager.setShowHiddenFiles(getSetting(path, "hide", false));
		//setViewMode(newView);
		//if(!BEFORE_HONEYCOMB && Build.VERSION.SDK_INT < 14 && newView == VIEW_CAROUSEL) {
			
			//setViewVisibility(true, false, R.id.content_pager_frame);
			//setViewVisibility(false, false, R.id.content_frag);
		
		List<OpenPath> familyTree = path.getAncestors(false);
		
		if(addToStack)
		{
			int bsCount = fragmentManager.getBackStackEntryCount();
			String last = null;
			if(bsCount > 0)
			{
				BackStackEntry entry = fragmentManager.getBackStackEntryAt(bsCount - 1);
				last = entry.getBreadCrumbTitle() != null ? entry.getBreadCrumbTitle().toString() : "";
				Logger.LogVerbose("Changing " + last + " to " + path.getPath() + "? " + (last.equalsIgnoreCase(path.getPath()) ? "No" : "Yes"));
			} else
				Logger.LogVerbose("First changePath to " + path.getPath());
			if(mStateReady && (last == null || !last.equalsIgnoreCase(path.getPath())))
			{
				fragmentManager
					.beginTransaction()
					.setBreadCrumbTitle(path.getPath())
					.addToBackStack("path")
					.commitAllowingStateLoss();
			}
		}
		final OpenFragment cf = (CAN_DO_CAROUSEL && newView == VIEW_CAROUSEL) ?
			new CarouselFragment(path) :
			ContentFragment.getInstance(path, newView, getSupportFragmentManager());
				
			if(force || addToStack || path.requiresThread())
			{
				int common = 0;

				boolean removed = false;
				for(int i = mViewPagerAdapter.getCount() - 1; i >= 0; i--)
				{
					OpenFragment f = mViewPagerAdapter.getItem(i);
					if(f == null || !(f instanceof ContentFragment)) continue;
					if(!familyTree.contains(((ContentFragment)f).getPath()))
					{
						mViewPagerAdapter.remove(i);
						removed = true;
					}
					else common++;
				}
				
				if(force)
				{
					mViewPagerAdapter.remove(cf);
					mViewPagerAdapter.add(cf);
					removed = true;
					//notifyPager();
				}
				
				//mViewPagerAdapter.notifyDataSetChanged();
				//mViewPagerAdapter.removeOfType(ContentFragment.class);
				//mViewPagerAdapter = new ArrayPagerAdapter(fragmentManager);
				int iNonContentPages = mViewPagerAdapter.getCount() - common;
				if(common < 0)
					mViewPagerAdapter.add(cf);
				else
					mViewPagerAdapter.add(common, cf);
				OpenPath tmp = path.getParent();
				while(tmp != null) 
				{
					Logger.LogDebug("Adding Parent: " + tmp.getPath());
					try {
						if(common > 0)
							if(tmp.getPath().equals(((ContentFragment)mViewPagerAdapter.getItem(common - 1)).getPath()))
								break;
					} catch(Exception e) { Logger.LogError("I don't trust this!", e); }
					try {
						mViewPagerAdapter.add(common, ContentFragment.getInstance(tmp, getSetting(tmp, "view", newView), getSupportFragmentManager()));
					} catch(Exception e) { Logger.LogError("Downloads?", e); }
					tmp = tmp.getParent();
				}
				//Logger.LogVerbose("All Titles: [" + getPagerTitles() + "] Paths: [" + getFragmentPaths(mViewPagerAdapter.getFragments()) + "]");
				//mViewPagerAdapter = newAdapter;
				//mViewPagerAdapter.getCount() - iNonContentPages - 1;
				setViewPageAdapter(mViewPagerAdapter, true); // TODO: I really want to set this to false, as it will speed up the app considerably
				//mViewPagerAdapter.notifyDataSetChanged();
				//index -= iNonContentPages;
				//int index = mViewPagerAdapter.getLastPositionOfType(ContentFragment.class);
				int index = mViewPagerAdapter.getItemPosition(cf);
				setCurrentItem(index, addToStack);
				//if(cf instanceof ContentFragment) ((ContentFragment)cf).refreshData(null, false);
				//updatePagerTitle(index);
			} else {
				OpenPath commonBase = null;
				for(int i = mViewPagerAdapter.getCount() - 1; i >= 0; i--)
				{
					if(!(mViewPagerAdapter.getItem(i) instanceof ContentFragment))
						continue;
					ContentFragment c = (ContentFragment)mViewPagerAdapter.getItem(i);
					if(path.getPath().startsWith(c.getPath().getPath()))
						continue;
					commonBase = ((ContentFragment)mViewPagerAdapter.remove(i)).getPath();
				}
				int depth = 0;
				if(commonBase != null)
					depth = commonBase.getDepth() - 1;
				OpenPath tmp = path;
				while(tmp != null && (commonBase == null || !tmp.equals(commonBase)))
				{
					mViewPagerAdapter.add(depth,
						ContentFragment.getInstance(path, getSetting(path, "view", newView), getSupportFragmentManager()));
					tmp = tmp.getParent();
					if(tmp == null) break;
				}
				setViewPageAdapter(mViewPagerAdapter, true);
				//mViewPager.setAdapter(mViewPagerAdapter);
				setCurrentItem(path.getDepth() - 1, false);
				//getDirContentFragment(false).refreshData(null, false);
			}
		//}
		//refreshContent();
		if(!BEFORE_HONEYCOMB)
			invalidateOptionsMenu();
		/*if(content instanceof ContentFragment)
		((ContentFragment)content).setSettings(
			SortType.DATE_DESC,
			getSetting(path, "thumbs", true),
			getSetting(path, "hide", true)
			);*/
		if(path instanceof OpenFile && !path.requiresThread())
			new PeekAtGrandKidsTask().execute((OpenFile)path);
		//ft.replace(R.id.content_frag, content);
		//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		Logger.LogDebug("Setting path to " + path.getPath());
		mLastPath = path;
	}
	private String getFragmentPaths(List<OpenFragment> frags)
	{
		String ret = "";
		for(int i = 0; i < frags.size(); i++)
		{
			OpenFragment f = frags.get(i);
			if(f instanceof OpenPathFragmentInterface)
				ret += ((OpenPathFragmentInterface)f).getPath().getPath();
			else
				ret += f.getClass().toString();
			if(i < frags.size() - 1)
				ret += ",";
		}
		return ret;
	}
	private String getPagerTitles()
	{
		String ret = "";
		for(int i = 0; i < mViewPagerAdapter.getCount(); i++)
		{
			ret += mViewPagerAdapter.getPageTitle(i);
			if(i < mViewPagerAdapter.getCount() - 1)
				ret += ",";
		}
		return ret;
	}
	@SuppressWarnings("deprecation")
	public void setLights(Boolean on)
	{
		try {
			View root = getCurrentFocus().getRootView();
			int vis = on ? View.STATUS_BAR_VISIBLE : View.STATUS_BAR_HIDDEN;
			if(root.getSystemUiVisibility() != vis)
				root.setSystemUiVisibility(vis);
		} catch(Exception e) { }
	}
	public void onChangeLocation(OpenPath path) {
		changePath(path, true, false);
	}

	public static final FileManager getFileManager() {
		return mFileManager;
	}

	public static final EventHandler getEventHandler() {
		return mEvHandler;
	}
	
	
	

	public class EnsureCursorCacheTask extends AsyncTask<OpenPath, Void, Void>
	{
		@Override
		protected Void doInBackground(OpenPath... params) {
			//int done = 0;
			final Context c = getApplicationContext();
			for(OpenPath path : params)
			{
				if(path.isDirectory())
				{
					try {
						for(OpenPath kid : path.list())
						{
							ThumbnailCreator.generateThumb(kid, 36, 36, c);
							ThumbnailCreator.generateThumb(kid, 128, 128, c);
							//done++;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ThumbnailCreator.generateThumb(path, 36, 36, c);
					ThumbnailCreator.generateThumb(path, 128, 128, c);
					//done++;
				}
			}
			//Logger.LogDebug("cursor cache of " + done + " generated.");
			return null;
		}
		
	}


	public void setOnClicks(int... ids)
	{
		for(int id : ids)
			if(findViewById(id) != null)
			{
				View v = findViewById(id);
				if(v.isLongClickable())
					v.setOnLongClickListener(this);
				v.setOnClickListener(this);
			}
	}
	
	public static String getVolumeName(String sPath2) {
		Process mLog = null;
		BufferedReader reader = null;
		try {
			mLog = Runtime.getRuntime().exec(new String[] {"logcat", "-d", "MediaVolume:D *:S"});
			reader = new BufferedReader(new InputStreamReader(mLog.getInputStream()));
			String check = sPath2.substring(sPath2.lastIndexOf("/") + 1);
			if(check.indexOf(".") > -1)
				check = check.substring(check.indexOf(".") + 1);
			String s = null;
			String last = null;
			do {
				s = reader.readLine();
				if(s == null) break;
				if(s.indexOf("New volume - Label:[") > -1 && s.indexOf(check) > -1)
				{
					last = s.substring(s.indexOf("[") + 1);
					if(last.indexOf("]") > -1)
						last = last.substring(0, last.indexOf("]"));
				}
			} while(s != null);
			if(last == null) {
				sPath2 = sPath2.substring(sPath2.lastIndexOf("/") + 1);
				if(sPath2.indexOf("_") > -1 && sPath2.indexOf("usb") < sPath2.indexOf("_"))
					sPath2 = sPath2.substring(0, sPath2.indexOf("_"));
				else if (sPath2.indexOf("_") > -1 && sPath2.indexOf("USB") > sPath2.indexOf("_"))
					sPath2 = sPath2.substring(sPath2.indexOf("_") + 1);
				sPath2 = sPath2.toUpperCase();
				return sPath2;
			}
			if(DEBUG)
				Logger.LogDebug("OpenExplorer.getVolumeName(" + sPath2 + ") = " + last);
			sPath2 = last;
		} catch (IOException e) {
			Logger.LogError("Couldn't read LogCat :(", e);
			sPath2 = sPath2.substring(sPath2.lastIndexOf("/") + 1);
			if(sPath2.indexOf("_") > -1 && sPath2.indexOf("usb") < sPath2.indexOf("_"))
				sPath2 = sPath2.substring(0, sPath2.indexOf("_"));
			else if (sPath2.indexOf("_") > -1 && sPath2.indexOf("USB") > sPath2.indexOf("_"))
				sPath2 = sPath2.substring(sPath2.indexOf("_") + 1);
			sPath2 = sPath2.toUpperCase();
		} finally {
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return sPath2;
	}
	public boolean onLongClick(View v) {
		switch(v.getId())
		{
			case R.id.title_icon:
				goHome();
				return true;
			case R.id.title_ops:
				showToast(R.string.s_title_operations);
				ViewUtils.setViewsVisible(this, false, R.id.title_ops);
				checkTitleSeparator();
				return true;
			case R.id.title_log:
				showToast(R.string.s_pref_logview);
				ViewUtils.setViewsVisible(this, false, R.id.title_log);
				checkTitleSeparator();
				return true;
				
			/*case R.id.menu_favorites:
				MenuUtils.setMenuShowAsAction(mMainMenu, MenuItem.SHOW_AS_ACTION_NEVER, R.id.menu_favorites);
				if(BEFORE_HONEYCOMB)
				{
					mToolbarButtons.removeView(mToolbarButtons.findViewById(R.id.menu_favorites));
					mMainMenu.add(0, R.id.menu_favorites, 0, R.string.s_menu_favorites)
						.setIcon(R.drawable.ic_favorites);
				}
				return true;*/
		}
		OpenFragment f = getSelectedFragment();
		if(f != null)
		{
			if(f.onLongClick(v)) return true;
			Logger.LogDebug("No onLongClick?");
		}
		return false;
	}
	public void addBookmark(OpenPath file) {
		Logger.LogDebug("Adding Bookmark: " + file.getPath());
		String sBookmarks = getPreferences().getSetting("bookmarks", "bookmarks", "");
		sBookmarks += (sBookmarks != "" ? ";" : "") + file.getPath();
		Logger.LogInfo("Bookmarks: " + sBookmarks);
		getPreferences().setSetting("bookmarks", "bookmarks", sBookmarks);
		if(mBookmarkListener != null)
			mBookmarkListener.onBookMarkAdd(file);
	}
	public void removeBookmark(OpenPath file)
	{
		Logger.LogDebug("Removing Bookmark: " + file.getPath());
		String sBookmarks = ";" + getPreferences().getSetting("bookmarks", "bookmarks", "") + ";";
		sBookmarks = sBookmarks.replace(";" + file.getPath() + ";", "");
		if(sBookmarks.startsWith(";"))
			sBookmarks = sBookmarks.substring(1);
		if(sBookmarks.endsWith(";"))
			sBookmarks = sBookmarks.substring(0, sBookmarks.length() - 1);
		getPreferences().setSetting("bookmarks", "bookmarks", sBookmarks);
		refreshBookmarks();
	}
	
	public static void setOnBookMarkAddListener(OpenInterfaces.OnBookMarkChangeListener bookmarkListener) {
		mBookmarkListener = bookmarkListener;
	}
	
	public class PeekAtGrandKidsTask extends AsyncTask<OpenFile, Void, Void>
	{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(OpenFile... params) {
			for(OpenFile file : params)
				file.listFiles(true);
			return null;
		}
		
	}

	public OpenPath getLastPath() {
		return mLastPath;
	}
	
	public View getRootView()
	{
		if(getCurrentFocus() != null)
			return getCurrentFocus().getRootView();
		else if(findViewById(android.R.id.home) != null)
			return findViewById(android.R.id.home).getRootView();
		else return null;
	}
	
	public void onClipboardUpdate() {
		if(getClipboard().size() == mLastClipSize &&
				getClipboard().isMultiselect() == mLastClipState) return;
		if(DEBUG)
			Logger.LogDebug("onClipboardUpdate(" + getClipboard().size() + ")");
		View pb = mStaticButtons.findViewById(R.id.title_paste);
		mLastClipSize = getClipboard().size();
		mLastClipState = getClipboard().isMultiselect();
		ViewUtils.setViewsVisible(pb, mLastClipSize > 0 || mLastClipState);
		ViewUtils.setText(pb, "" + mLastClipSize, R.id.title_paste_text);
		ViewUtils.setImageResource(pb, mLastClipState ?
					R.drawable.ic_menu_paste_multi : R.drawable.ic_menu_clipboard,
					R.id.title_paste_icon);
		checkTitleSeparator();
		//invalidateOptionsMenu();
		if(!BEFORE_HONEYCOMB && USE_ACTIONMODE && mActionMode != null)
			((ActionMode)mActionMode).setTitle(getString(R.string.s_menu_multi) + ": " + mLastClipSize + " " + getString(R.string.s_files));
		ContentFragment cf = getDirContentFragment(false);
		if(cf != null && cf.isAdded() && cf.isVisible())
			cf.notifyDataSetChanged();
	}

	@Override
	public boolean onPageTitleLongClick(int position, View titleView) {
		try {
			OpenFragment f = mViewPagerAdapter.getItem(position);
			if(f instanceof OnFragmentTitleLongClickListener)
				return ((OnFragmentTitleLongClickListener)f).onTitleLongClick(titleView);
			if(f instanceof TextEditorFragment) return false;
			if(!(f instanceof ContentFragment)) return false;
			OpenPath path = ((ContentFragment)mViewPagerAdapter.getItem(position)).getPath();
			if(path.requiresThread()) return false;
			OpenPath parent = path.getParent();
			if(path instanceof OpenCursor)
				parent = new OpenPathArray(new OpenPath[]{mVideoParent,mPhotoParent,mMusicParent,mDownloadParent});
			if(parent == null) parent = new OpenPathArray(new OpenPath[]{path});
			ArrayList<OpenPath> arr = new ArrayList<OpenPath>();
			for(OpenPath kid : parent.list())
				if((path.equals(kid) || kid.isDirectory()) && !kid.isHidden())
					arr.add(kid);
			Collections.sort(arr, new Comparator<OpenPath>() {
				public int compare(OpenPath a, OpenPath b) {
					return a.getName().compareTo(b.getName());
				}
			});
			OpenPath[] siblings = arr.toArray(new OpenPath[arr.size()]);
			ArrayList<OpenPath> siblingArray = new ArrayList<OpenPath>();
			siblingArray.addAll(arr);
			OpenPath foster = new OpenPathArray(siblings);
			//Logger.LogVerbose("Siblings of " + path.getPath() + ": " + siblings.length);
			
			Context mContext = this;
			View anchor = titleView; //findViewById(R.id.title_bar);
			int[] offset = new int[2];
			titleView.getLocationInWindow(offset);
			int offsetX = 0;
			int arrowLeft = 0;
			if(anchor == null && findViewById(R.id.content_pager_indicator) != null)
			{
				offsetX = titleView.getLeft();
				arrowLeft += titleView.getWidth() / 2;
				Logger.LogDebug("Using Pager Indicator as Sibling anchor (" + offsetX + ")");
				anchor = findViewById(R.id.content_pager_indicator);
				//if(anchor != null)
				//	offsetX -= anchor.getLeft();
			}
			final BetterPopupWindow mSiblingPopup = new BetterPopupWindow(mContext, anchor);
			//mSiblingPopup.USE_INDICATOR = false;
			OpenPathList mSiblingList = new OpenPathList(foster, mContext);
			mSiblingList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
					final OpenPath path = (OpenPath)((BaseAdapter)arg0.getAdapter()).getItem(pos);
					mSiblingPopup.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss() {
							changePath(path, true);
						}
					});
					mSiblingPopup.dismiss();
				}
			});
			mSiblingPopup.setContentView(mSiblingList);
			mSiblingPopup.setAnchorOffset(arrowLeft);
			mSiblingPopup.showLikePopDownMenu(offsetX,0);
			return true;
		} catch (Exception e) {
			Logger.LogError("Couldn't show sibling dropdown", e);
		}
		return false;
	}

	@Override
	public void onWorkerThreadComplete(EventType type, String... results) {
		try {
			Thread.sleep(50);
			Logger.LogVerbose("Time to wake up!");
		} catch (InterruptedException e) {
			Logger.LogWarning("Woken up too early!");
		}
		ContentFragment frag = getDirContentFragment(false);
		if(frag != null)
		{
			frag.onWorkerThreadComplete(type, results);
			changePath(frag.getPath(), false, true);
		}
		if(getClipboard().ClearAfter)
			getClipboard().clear();
	}
	
	@Override
	public void onWorkerProgressUpdate(int pos, int total)
	{
		ContentFragment frag = getDirContentFragment(false);
		if(frag != null)
			frag.onWorkerProgressUpdate(pos, total);
	}

	@Override
	public void onWorkerThreadFailure(EventType type, OpenPath... files) {
		for(OpenPath path : files)
			sendToLogView(type.name() + " error on " + path, Color.RED);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;
		int flag = (int) Math.pow(2, id + 1);
		if((OpenCursor.LoadedCursors & flag) == flag) return null;
		switch(id)
		{
			case 0: // videos
				loader = new CursorLoader(
					getApplicationContext(),
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					//Uri.parse("content://media/external/video/media"),
					bRetrieveExtraVideoDetails ?
						new String[]{"_id", "_display_name", "_data", "_size", "date_modified",
								MediaStore.Video.VideoColumns.RESOLUTION,
								MediaStore.Video.VideoColumns.DURATION} :
						new String[]{"_id", "_display_name", "_data", "_size", "date_modified"},
					MediaStore.Video.Media.SIZE + " > 10000", null,
					MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC, " +
					MediaStore.Video.Media.DATE_MODIFIED + " DESC"
						);
				if(bRetrieveExtraVideoDetails)
					try {
						loader.loadInBackground();
					} catch(SQLiteException e) {
						bRetrieveExtraVideoDetails = false;
						setSetting("tag_novidinfo", true);
						return onCreateLoader(id, arg1);
					}
				break;
			case 1: // images
				loader = new CursorLoader(
					getApplicationContext(),
					Uri.parse("content://media/external/images/media"),
					bRetrieveDimensionsForPhotos ? // It seems that < 2.3.3 don't have width & height
						new String[]{"_id", "_display_name", "_data", "_size", "date_modified",
								"width", "height"
							} :
						new String[]{"_id", "_display_name", "_data", "_size", "date_modified"},
					MediaStore.Images.Media.SIZE + " > 10000", null,
					MediaStore.Images.Media.DATE_ADDED + " DESC"
					);
				if(bRetrieveDimensionsForPhotos)
				{
					try {
						loader.loadInBackground();
					} catch(SQLiteException e) {
						bRetrieveDimensionsForPhotos = false;
						setSetting("tag_nodims", true);
						return onCreateLoader(id, arg1);
					}
				}
				break;
			case 2: // music
				loader = new CursorLoader(
					getApplicationContext(),
					Uri.parse("content://media/external/audio/media"),
					new String[]{"_id", "_display_name", "_data", "_size", "date_modified",
							MediaStore.Audio.AudioColumns.DURATION
						},
					MediaStore.Audio.Media.SIZE + " > 10000", null,
					MediaStore.Audio.Media.DATE_ADDED + " DESC"
					);
				break;
			case 3: // apks
				if(bRetrieveCursorFiles)
				loader = new CursorLoader(
					getApplicationContext(),
					MediaStore.Files.getContentUri("/mnt"),
					new String[]{"_id", "_display_name", "_data", "_size", "date_modified"},
					"_size > 10000 AND _data LIKE '%apk'", null,
					"date modified DESC"
					);
				break;
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> l, Cursor c) {
		if(c == null) return;
		int flag = (int)Math.pow(2, l.getId() + 1);
		if((OpenCursor.LoadedCursors & flag) == flag) {
			c.close();
			return;
		}
		OpenCursor.LoadedCursors |= flag;
		Logger.LogVerbose("LoadedCursors: " + OpenCursor.LoadedCursors);
		OpenCursor mParent = mVideoParent;
		if(l.getId() == 1)
			mParent = mPhotoParent;
		else if(l.getId() == 2)
			mParent = mMusicParent;
		else if(l.getId() == 3)
			mParent = mApkParent;
		mParent.setCursor(c);
		mBookmarks.refresh();
		OpenFragment f = getSelectedFragment();
		if(f instanceof ContentFragment && ((ContentFragment)f).getPath().equals(mParent))
			((ContentFragment)f).refreshData(null, false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> l) {
		onLoadFinished(l, null);
	}

	public void setProgressVisibility(boolean visible) {
		setProgressBarIndeterminateVisibility(visible);
		ViewUtils.setViewsVisible(this, visible, android.R.id.progress, R.id.title_progress);
	}

	public void removeFragment(OpenFragment frag) {
		setCurrentItem(mViewPagerAdapter.getCount() - 1, false);
		if(!mViewPagerAdapter.remove(frag))
			Logger.LogWarning("Unable to remove fragment");
		setViewPageAdapter(mViewPagerAdapter);
		//refreshContent();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

	@Override
	public void onPageScrollStateChanged(int state) { }

	@Override
	public void onPageSelected(int position) {
		Logger.LogDebug("onPageSelected(" + position + ")");
		final OpenFragment f = getSelectedFragment();
		if(f == null) return;
		if(!f.isDetached())
		{
			invalidateOptionsMenu();
			if(Build.VERSION.SDK_INT < 14)
				((ImageView)findViewById(R.id.title_icon)).setImageDrawable(f.getIcon());
			else
				ImageUtils.fadeToDrawable((ImageView)findViewById(R.id.title_icon), f.getIcon());
		}
		//if((f instanceof ContentFragment) && (((ContentFragment)f).getPath() instanceof OpenNetworkPath)) ((ContentFragment)f).refreshData(null, false);
	}

	public void notifyPager() {
		mViewPager.post(new Runnable(){public void run(){
			mViewPager.notifyDataSetChanged();
		}});
		if(USE_PRETTY_MENUS && mToolbarButtons != null && mToolbarButtons.getChildCount() > 0
				&& mViewPager != null && mViewPager.getIndicator() != null
				&& mViewPager.getIndicator() instanceof TabPageIndicator)
		{
			TabPageIndicator tpi = (TabPageIndicator)mViewPager.getIndicator();
			if(tpi.getChildCount() > 0)
			{
				TabView tv = tpi.getTab(tpi.getChildCount() - 1);
				View m1 = mToolbarButtons.getChildAt(0);
				tv.setNextFocusRightId(m1.getId());
				m1.setNextFocusLeftId(tv.getId());
			} else {
				View m1 = mToolbarButtons.getChildAt(0);
				tpi.setNextFocusRightId(m1.getId());
				m1.setNextFocusLeftId(tpi.getId());
			}
		}
	}
	
	public OpenApplication getOpenApplication()
	{
		return (OpenApplication)getApplication();
	}

	@Override
	public DataManager getDataManager() {
		return getOpenApplication().getDataManager();
	}

	@Override
	public ImageCacheService getImageCacheService() {
		return getOpenApplication().getImageCacheService();
	}

	@Override
	public DownloadCache getDownloadCache() {
		return getOpenApplication().getDownloadCache();
	}

	@Override
	public ThreadPool getThreadPool() {
		return getOpenApplication().getThreadPool();
	}

	@Override
	public LruCache<String, Bitmap> getMemoryCache() {
		return getOpenApplication().getMemoryCache();
	}

	@Override
	public DiskLruCache getDiskCache() {
		return getOpenApplication().getDiskCache();
	}

	@Override
	public Context getAndroidContext() {
		return getOpenApplication().getAndroidContext();
	}
	
	public ShellSession getShellSession() {
		return getOpenApplication().getShellSession();
	}

	@Override
	public void onIconContextItemSelected(IconContextMenu menu,
			MenuItem item, Object info, View view)
	{
		if(menu != null)
			menu.dismiss();
		if(onClick(item.getItemId(), item, view)) return;
		if(onOptionsItemSelected(item)) return;
		int index = Utils.getArrayIndex(MenuUtils.MENU_LOOKUP_IDS, item.getItemId());
		if(index > -1)
			showMenu(MenuUtils.getMenuLookupSub(index), view, true);
		else
			onClick(item.getItemId());
	}

	public boolean onIconContextItemSelected(PopupMenu menu, MenuItem item,
			ContextMenuInfo menuInfo, View view) {
		if(menu != null)
			menu.dismiss();
		if(onClick(item.getItemId(), item, view)) return true;
		if(onOptionsItemSelected(item)) return true;
		int index = Utils.getArrayIndex(MenuUtils.MENU_LOOKUP_IDS, item.getItemId());
		if(index > -1)
			return showMenu(MenuUtils.getMenuLookupSub(index), view, true);
		else {
			View v = findViewById(item.getItemId());
			if(v != null && v.isClickable())
			{
				onClick(v);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(v == null) return false;
		if(DEBUG)
			Logger.LogDebug("OpenExplorer.onKey(" + v + "," + keyCode + "," + event + ")");
		if(event.getAction() != KeyEvent.ACTION_UP) return false;
		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
		{
			if(MenuUtils.getMenuLookupID(v.getId()) > 0)
				if(showMenu(MenuUtils.getMenuLookupSub(v.getId()), v, false))
					return true;
		}
		return false;
	}
	
	public boolean onFragmentDPAD(OpenFragment frag, boolean toRight) {
		int pos = mViewPagerAdapter.getItemPosition(frag);
		Logger.LogDebug("onFragmentDPAD(" + pos + "," + (toRight?"RIGHT":"LEFT") + ")");
		if(toRight)
		{
			if(frag.getTitleView() != null)
				if(frag.getTitleView().requestFocus())
					return true;
			if(ViewUtils.requestFocus(this,
					findViewById(R.id.content_frag).getNextFocusRightId(),
					R.id.log_clear, android.R.id.home, R.id.menu_search))
				return true;
		} else if (!toRight)
		{
			if(frag.getTitleView() != null)
				if(frag.getTitleView().requestFocus())
					return true;
			if(findViewById(R.id.menu_search) != null)
				if(findViewById(findViewById(R.id.menu_search).getNextFocusLeftId()).requestFocus())
					return true;
			if(ViewUtils.requestFocus(this,
					findViewById(R.id.content_frag).getNextFocusLeftId(),
					R.id.bookmarks_list, R.id.list_frag, android.R.id.home, R.id.frag_log, R.id.menu_search))
				return true;
		}
		pos += toRight ? 1 : -1;
		pos = pos % mViewPagerAdapter.getCount();
		mViewPager.setCurrentItem(pos);
		return true;
	}

	public View getPagerTitleView(OpenFragment frag) {
		int pos = mViewPagerAdapter.getItemPosition(frag);
		if(pos < 0) return null;
		return ((TabPageIndicator)mViewPager.getIndicator()).getView(pos);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		/*
		if(hasFocus && mMainMenu.findItem(v.getId()) != null
				&& mMainMenu.findItem(v.getId()).hasSubMenu())
			v.requestFocus();
			*/
	}

}


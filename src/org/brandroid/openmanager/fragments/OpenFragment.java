package org.brandroid.openmanager.fragments;

import java.lang.reflect.Method;
import java.util.Comparator;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.OpenFragmentActivity;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.adapters.ContentAdapter.CheckClipboardListener;
import org.brandroid.openmanager.adapters.IconContextMenu.IconContextItemSelectedListener;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuBuilder;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.ViewUtils;

import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

/*
 * Base class for all OpenExplorer fragments. Provides convenient methods to access
 * other sections of the application.
 */
public abstract class OpenFragment
			extends Fragment
			implements View.OnClickListener, View.OnLongClickListener
				, Comparator<OpenFragment>
				, Comparable<OpenFragment>
				, OpenApp, CheckClipboardListener
{
	//public static boolean CONTENT_FRAGMENT_FREE = true;
	//public boolean isFragmentValid = true;
	protected boolean mActionModeSelected = false;
	private boolean mHasOptions = false;
	protected boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;
	private OnFragmentDPADListener mDPAD = null;
	public final void setOnFragmentDPADListener(OnFragmentDPADListener listener) { mDPAD = listener; }
		
	public interface OnFragmentDPADListener
	{
		public boolean onFragmentDPAD(OpenFragment fragment, boolean toRight);
	}
	
	public final boolean onFragmentDPAD(OpenFragment fragment, boolean toRight)
	{
		if(mDPAD != null)
			return mDPAD.onFragmentDPAD(fragment, toRight);
		return false;
	}
	
	public interface OnFragmentTitleLongClickListener
	{
		public boolean onTitleLongClick(View titleView);
	}
	
	public interface Poppable
	{
		public void setupPopup(Context c, View anchor);
		public BetterPopupWindow getPopup();
	}
	
	public class OpenContextMenuInfo implements ContextMenuInfo
	{
		private final OpenPath file;
		public OpenContextMenuInfo(OpenPath path) { file = path; }
		public OpenPath getPath() { return file; }
	}
	
	public static OpenFragment instantiate(Context context, String fname, Bundle args) {
        String sPath = null;
    	if(args.containsKey("last"))
    		sPath = args.getString("last");
    	if(args.containsKey("edit_path"))
    		sPath = args.getString("edit_path");
    	OpenPath path = FileManager.getOpenCache(sPath, context);
    	if(sPath != null)
    	{
        	if(fname.endsWith("ContentFragment"))
        		return ContentFragment.getInstance(path, args);
        	else if(fname.endsWith("TextEditorFragment"))
        		return TextEditorFragment.getInstance(path, args);
        	else if(fname.endsWith("CarouselFragment"))
        		return CarouselFragment.getInstance(args);
    	}
        return null;
    }
	
	public int compareTo(OpenFragment b) {
		return compare(this, b);
	}
	
	@Override
	public int compare(OpenFragment a, OpenFragment b) {
		if(a == null && b != null) return 1;
		else if(b == null) return -1;
		
		int priA = a.getPagerPriority();
		int priB = b.getPagerPriority();
		//if(DEBUG) Logger.LogDebug("Comparing " + a.getTitle() + "(" + priA + ") to " + b.getTitle() + "(" + priB + ")");
		if(priA != priB)
		{
			if(priA > priB)
			{
				//Logger.LogDebug("Switch!");
				return 1;
			} else {
				//Logger.LogDebug("Stay!");
				return -1;
			}
		}
		if(a instanceof ContentFragment && b instanceof ContentFragment)
		{
			OpenPath pa = ((ContentFragment)a).getPath();
			OpenPath pb = ((ContentFragment)b).getPath();
			if(pa == null && pb != null)
				return 1;
			else if(pb == null && pa != null)
				return -1;
			else if(pa == null || pb == null)
				return 0;
			priA = pa.getPath().length();
			priB = pb.getPath().length();
			if(priA > priB)
			{
				//Logger.LogDebug("Switch 2!");
				return 1;
			} else {
				//Logger.LogDebug("Stay 2!");
				return -1;
			}
		} else {
			//Logger.LogDebug("0!");
			return 0;
		}
	}
	
	/*
	 * Return priority for ordering in ViewPager (Low to High)
	 */
	public int getPagerPriority() { return 5; }
	
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		if(DEBUG)
			Logger.LogDebug(getClassName() + ".startActivityForResult(" + requestCode + "," + (intent != null ? intent.toString() : "null") + ")");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(DEBUG)
			Logger.LogDebug(getClassName() + ".onActivityResult(" + requestCode + "," + resultCode + "," + (data != null ? data.toString() : "null") + ")");
	}
	
	public boolean showMenu(final Menu menu, View anchor, CharSequence title)
	{
		if(menu == null || menu.size() == 0) return false;
		onPrepareOptionsMenu(menu);
		if(showIContextMenu(menu, anchor, title, 0, 0))
			return true;
		anchor.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu cmenu, View v,
					ContextMenuInfo menuInfo) {
				MenuUtils.transferMenu(menu, cmenu, false);
			}
		});
		return anchor.showContextMenu();
	}

	public boolean showMenu(final int menuId, View from, CharSequence title)
	{
		return showMenu(menuId, from, title, 0, 0);
	}
	public boolean showMenu(final int menuId, View from1, CharSequence title, int xOffset, int yOffset)
	{
		if(from1 == null)
			from1 = ViewUtils.getFirstView(getActivity(), R.id.menu_more, android.R.id.home);
		if(from1 == null)
			from1 = getActivity().getCurrentFocus().getRootView();
		final View from = from1;
		if(showIContextMenu(menuId, from, title, xOffset, yOffset)) return true;
		if(Build.VERSION.SDK_INT > 10)
		{
			final PopupMenu pop = new PopupMenu(getActivity(), from);
			pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					if(onOptionsItemSelected(item))
					{
						pop.dismiss();
						return true;
					}
					else if(getExplorer() != null)
						return getExplorer().onIconContextItemSelected(pop, item, item.getMenuInfo(), from);
					return false;
				}
			});
			pop.getMenuInflater().inflate(menuId, pop.getMenu());
			Logger.LogDebug("PopupMenu.show()");
			pop.show();
			return true;
		}
		if(from == null) return false;
		from.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				getMenuInflater().inflate(menuId, menu);
				onPrepareOptionsMenu(menu);
			}
		});
		boolean ret = from.showContextMenu();
		from.setOnCreateContextMenuListener(null);
		return ret;
	}
	
	public boolean inflateMenu(Menu menu, int menuItemId, MenuInflater inflater)
	{
		return false;
	}
	
	public boolean showIContextMenu(Menu menu, final View from, CharSequence title, int xOffset, int yOffset)
	{
		if(getActivity() == null) return false;
		final IconContextMenu mOpenMenu =
				new IconContextMenu(getActivity(), menu, from);
		if(mOpenMenu == null) return false;
		if(title != null && title.length() > 0)
			mOpenMenu.setTitle(title);
		if(DEBUG)
			Logger.LogDebug("Showing menu " + menu + (from != null ? " near 0x" + Integer.toHexString(from.getId()) : " by itself"));
		if(getActivity() != null)
			getActivity().onPrepareOptionsMenu(menu);
		mOpenMenu.setMenu(menu);
		mOpenMenu.setAnchor(from);
		mOpenMenu.setNumColumns(1);
		mOpenMenu.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
			public void onIconContextItemSelected(final IconContextMenu menu, MenuItem item,
					Object info, View view) {
				if(onOptionsItemSelected(item))
					menu.dismiss();
				else if(getExplorer() != null)
					getExplorer().onIconContextItemSelected(menu, item, info, view);
			}});
		return mOpenMenu.show(xOffset, yOffset);
	}
	
	public boolean showIContextMenu(int menuId, final View from, CharSequence title, int xOffset, int yOffset)
	{
		if(getActivity() == null) return false;
		if(menuId != R.menu.context_file && !OpenExplorer.USE_PRETTY_MENUS) return false;
		if(menuId == R.menu.context_file && !OpenExplorer.USE_PRETTY_CONTEXT_MENUS) return false;
		final IconContextMenu mOpenMenu =
				IconContextMenu.getInstance(getActivity(), menuId, from);
		if(mOpenMenu == null) return false;
		if(title != null && title.length() > 0)
			mOpenMenu.setTitle(title);
		if(DEBUG)
			Logger.LogDebug("Showing menu 0x" + Integer.toHexString(menuId) + (from != null ? " near 0x" + Integer.toHexString(from.getId()) : " by itself"));
		Menu menu = mOpenMenu.getMenu();
		if(getActivity() != null)
			getActivity().onPrepareOptionsMenu(menu);
		mOpenMenu.setMenu(menu);
		mOpenMenu.setAnchor(from);
		mOpenMenu.setNumColumns(1);
		mOpenMenu.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
			public void onIconContextItemSelected(final IconContextMenu menu, MenuItem item,
					Object info, View view) {
				if(onOptionsItemSelected(item))
					menu.dismiss();
				else if(getExplorer() != null)
					getExplorer().onIconContextItemSelected(menu, item, info, view);
			}});
		return mOpenMenu.show(xOffset, yOffset);
	}
	
	@Override
	public void setHasOptionsMenu(boolean hasMenu) {
		//if(!OpenExplorer.BEFORE_HONEYCOMB) super.setHasOptionsMenu(hasMenu);
		mHasOptions = hasMenu;
	}
	public boolean hasOptionsMenu()
	{
		return mHasOptions;
	}
	
	public boolean onBackPressed() {
		if(getExplorer() != null)
		{
			try {
				getExplorer().removeFragment(this);
				return true;
			} catch(Exception e) { }
		}
		return false;
	}
	
	public MenuInflater getMenuInflater()
	{
		if(getActivity() != null)
			return (MenuInflater)getActivity().getMenuInflater();
		else return null;
	}

	protected String getViewSetting(OpenPath path, String key, String def)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			return def;
		else
			return getFragmentActivity().getSetting(path, key, def);
	}
	protected Boolean getViewSetting(OpenPath path, String key, Boolean def)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			return def;
		else
			return getFragmentActivity().getSetting(path, key, def);
	}
	protected Integer getViewSetting(OpenPath path, String key, Integer def)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			return def;
		else
			return getFragmentActivity().getSetting(path, key, def);
	}
	protected Float getViewSetting(OpenPath path, String key, Float def)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			return def;
		else
			return getFragmentActivity().getSetting(path, key, def);
	}
	protected void setViewSetting(OpenPath path, String key, String value)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			Logger.LogWarning("Unable to setViewSetting");
		else
			getFragmentActivity().setSetting(path, key, value);
	}
	protected void setViewSetting(OpenPath path, String key, Boolean value)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			Logger.LogWarning("Unable to setViewSetting");
		else
			getFragmentActivity().setSetting(path, key, value);
	}
	protected void setViewSetting(OpenPath path, String key, Integer value)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			Logger.LogWarning("Unable to setViewSetting");
		else
			getFragmentActivity().setSetting(path, key, value);
	}
	protected void setViewSetting(OpenPath path, String key, Float value)
	{
		if(getExplorer() == null || getExplorer().getPreferences() == null)
			Logger.LogWarning("Unable to setViewSetting");
		else
			getFragmentActivity().setSetting(path, key, value);
	}
	protected Integer getSetting(OpenPath file, String key, Integer defValue)
	{
		if(getActivity() == null) return defValue; 
		return getFragmentActivity().getSetting(file, key, defValue);
	}
	protected String getSetting(OpenPath file, String key, String defValue)
	{
		if(getActivity() == null) return defValue;
		return getFragmentActivity().getSetting(file, key, defValue);
	}
	protected Boolean getSetting(String file, String key, Boolean defValue)
	{
		if(getActivity() == null) return defValue;
		if(getFragmentActivity().getPreferences() == null) return defValue;
		return getFragmentActivity().getPreferences().getSetting(file, key, defValue);
	}
	protected final void setSetting(String file, String key, Boolean value)
	{
		if(getActivity() == null) return;
		getFragmentActivity().getPreferences().setSetting(file, key, value);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OpenExplorer)
			setOnFragmentDPADListener((OpenExplorer)activity);
		if(DEBUG)
			Logger.LogDebug("}-- onAttach :: " + getClassName() + (this instanceof OpenPathFragmentInterface && ((OpenPathFragmentInterface)this).getPath() != null ? " @ " + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if(DEBUG)
			Logger.LogDebug("{-- onDetach :: " + getClassName() + (this instanceof OpenPathFragmentInterface && ((OpenPathFragmentInterface)this).getPath() != null ? " @ " + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	protected final void refreshOperations()
	{
		if(getExplorer() != null)
			getExplorer().refreshOperations();
	}
	
	public void changeMultiSelectState(boolean multiSelectOn) {
		if(multiSelectOn)
			getClipboard().startMultiselect();
		else
			getClipboard().stopMultiselect();
		//mMultiSelectDrawer.setVisibility(multiSelectOn ? View.VISIBLE : View.GONE);
	}


	
	protected final void finishMode(Object mode)
	{
		getClipboard().clear();
		if(!OpenExplorer.BEFORE_HONEYCOMB && mode != null && mode.getClass().getName().equals("ActionMode"))
		{
			try {
				Method mFinish = mode.getClass().getMethod("finish", new Class[0]);
				if(mFinish != null)
					mFinish.invoke(mode, new Object[0]);		
			} catch(Exception e) { }
		}
	}
	
	
	public String getClassName()
	{
		return this.getClass().getSimpleName();
	}
	
	protected EventHandler getHandler()
	{
		return OpenExplorer.getEventHandler();
	}
	
	protected FileManager getManager()
	{
		return OpenExplorer.getFileManager();
	}
	
	public Drawable getDrawable(int resId)
	{
		if(getActivity() == null) return null;
		if(getResources() == null) return null;
		return getResources().getDrawable(resId);
	}
	public OpenFragmentActivity getFragmentActivity() { return (OpenFragmentActivity)getActivity(); }
	public OpenExplorer getExplorer() { return (OpenExplorer)getActivity(); }
	public Context getApplicationContext() { if(getActivity() != null) return getActivity().getApplicationContext(); else return null; }
	public static EventHandler getEventHandler() { return OpenExplorer.getEventHandler(); }
	public static FileManager getFileManager() { return OpenExplorer.getFileManager(); }
	
	@Override
	public OpenClipboard getClipboard() {
		if(getExplorer() != null)
			return getExplorer().getClipboard();
		else return null;
	}
	
	@Override
	public boolean isMultiselect() {
		if(getClipboard() != null)
			return getClipboard().isMultiselect();
		return false;
	}
	
	@Override
	public void removeFromClipboard(OpenPath file) {
		if(getClipboard() != null)
			getClipboard().remove(file);
	}
	
	@Override
	public boolean checkClipboard(OpenPath file) {
		if(getClipboard() != null)
			return getClipboard().contains(file);
		else return false;
	}
	
	public void onClick(View v) {
		Logger.LogInfo(getClassName() + ".onClick(0x" + Integer.toHexString(v.getId()) + ")");
	}
	
	public boolean onClick(int id, View from) {
		Logger.LogInfo(getClassName() + ".onClick(0x" + Integer.toHexString(id) + ")");
		return false;
	}
	
	public boolean onLongClick(View v) {
		Logger.LogInfo(getClassName() + ".onLongClick(" + Integer.toHexString(v.getId()) + ") - " + v.toString());
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Logger.LogDebug("<-- onCreate - " + getClassName() + (this instanceof OpenPathFragmentInterface && ((OpenPathFragmentInterface)this).getPath() != null ? " @ " + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
		//CONTENT_FRAGMENT_FREE = false;
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}
	
	public void invalidateOptionsMenu()
	{
		if(getExplorer() == null) return;
		getExplorer().invalidateOptionsMenu();
	}

	public abstract Drawable getIcon();
	public abstract CharSequence getTitle();
	
	public void notifyPager()
	{
		if(getExplorer() != null)
			getExplorer().notifyPager();
	}
	
	public void sendToLogView(String txt, int color)
	{
		if(getExplorer() != null)
			getExplorer().sendToLogView(txt, color);
	}
	
	public void doClose()
	{
		if(getExplorer() != null && getExplorer().isViewPagerEnabled())
			getExplorer().closeFragment(this);				
		else if(getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0)
			getFragmentManager().popBackStack();
		else if(getActivity() != null)
			getActivity().finish();
	}
	
	public View getActionView(MenuItem item)
	{
		try {
			if(Build.VERSION.SDK_INT < 11)
				return getActivity().findViewById(item.getItemId());
			Method m = MenuItem.class.getMethod("getActionView", new Class[0]);
			Object o = m.invoke(item, new Object[0]);
			if(o != null && o instanceof View)
				return (View)o;
			else if (getActivity().getActionBar() != null && getActivity().getActionBar().getCustomView().findViewById(item.getItemId()) != null)
				return getActivity().getActionBar().getCustomView().findViewById(item.getItemId());
			else return getActivity().findViewById(item.getItemId());
		} catch(Exception e) {
			return getActivity().findViewById(item.getItemId());
		}
	}
	
	@Override
	public Context getAndroidContext() {
		if(getExplorer() != null)
			return getExplorer().getAndroidContext();
		else return getApplicationContext();
	}
	
	@Override
	public DataManager getDataManager() {
		return getExplorer().getDataManager();
	}
	
	@Override
	public DiskLruCache getDiskCache() {
		return getExplorer().getDiskCache();
	}
	
	@Override
	public DownloadCache getDownloadCache() {
		return getExplorer().getDownloadCache();
	}
	
	@Override
	public ImageCacheService getImageCacheService() {
		return getExplorer().getImageCacheService();
	}
	
	@Override
	public ContentResolver getContentResolver() {
		return getExplorer().getContentResolver();
	}
	
	@Override
	public Looper getMainLooper() {
		return getExplorer().getMainLooper();
	}
	
	@Override
	public LruCache<String, Bitmap> getMemoryCache() {
		return getExplorer().getMemoryCache();
	}
	
	@Override
	public ThreadPool getThreadPool() {
		return getExplorer().getThreadPool();
	}
	
	@Override
	public ShellSession getShellSession() {
		return getExplorer().getShellSession();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if(DEBUG)
			Logger.LogDebug("<-- onViewCreated - " + getClassName());
		super.onViewCreated(view, savedInstanceState);
	}

	public View getTitleView() {
		if(getExplorer() != null)
			return getExplorer().getPagerTitleView(this);
		return null;
	}
	
	/*
	 * 
	@Override
	public void onDestroy() {
		Logger.LogDebug("--> onDestroy - " + getClassName());
		super.onDestroy();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Logger.LogDebug("<-- onActivityCreated - " + getClassName());
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.LogDebug("<-- onCreateView - " + getClassName());
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.LogDebug("<-onCreateView - " + getClassName());
		//CONTENT_FRAGMENT_FREE = false;
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Logger.LogDebug("<-onViewCreated - " + getClassName());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Logger.LogDebug("->onPause - " + getClassName());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Logger.LogDebug("<-onResume - " + getClassName());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Logger.LogDebug("<-onStart - " + getClassName());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Logger.LogDebug("->onStop - " + getClassName());
		//CONTENT_FRAGMENT_FREE = true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.LogDebug("->onSaveInstanceState - " + getClassName());
	}
	*/
}

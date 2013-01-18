
package org.brandroid.openmanager.fragments;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

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
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LruCache;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.analytics.Item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
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
@SuppressLint("NewApi")
public abstract class OpenFragment extends SherlockFragment implements View.OnClickListener,
        View.OnLongClickListener, Comparator<OpenFragment>, Comparable<OpenFragment>, OpenApp,
        CheckClipboardListener {
    // public static boolean CONTENT_FRAGMENT_FREE = true;
    // public boolean isFragmentValid = true;
    private boolean mHasOptions = false;
    protected boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
    private OnFragmentDPADListener mDPAD = null;

    public final void setOnFragmentDPADListener(OnFragmentDPADListener listener) {
        mDPAD = listener;
    }

    public interface OnFragmentDPADListener {
        public boolean onFragmentDPAD(OpenFragment fragment, boolean toRight);
    }

    public final boolean onFragmentDPAD(OpenFragment fragment, boolean toRight) {
        if (mDPAD != null)
            return mDPAD.onFragmentDPAD(fragment, toRight);
        return false;
    }

    public interface OnFragmentTitleLongClickListener {
        public boolean onTitleLongClick(View titleView);
    }

    public interface Poppable {
        public void setupPopup(Context c, View anchor);

        public BetterPopupWindow getPopup();
    }

    public class OpenContextMenuInfo implements ContextMenuInfo {
        private final OpenPath file;

        public OpenContextMenuInfo(OpenPath path) {
            file = path;
        }

        public OpenPath getPath() {
            return file;
        }
    }

    private void modifyMenuShare(Menu menu, OpenPath mPath) {
        MenuItem mShare = menu.findItem(R.id.menu_context_share);
        if (mShare == null)
            mShare = menu
                    .add(Menu.NONE, R.id.menu_context_share, Menu.FIRST, R.string.s_menu_share)
                    .setIcon(
                            getThemedResourceId(R.styleable.AppTheme_actionIconShare,
                                    R.drawable.ic_menu_share))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        Intent intent = IntentManager.getIntent(mPath, getExplorer());
        menu.removeGroup(10);
        if (intent == null) {
            mShare.setVisible(false);
            return;
        }

        if (mPath != null) {
            if (mPath.getMimeType() != null)
                intent.setDataAndType(mPath.getUri(), mPath.getMimeType());
            else
                intent.setData(mPath.getUri());
        }

        List<ResolveInfo> resolves = IntentManager.getResolvesAvailable(intent, getExplorer());
        if (resolves.size() == 0) {
            intent.setAction(Intent.ACTION_SEND);
            resolves = IntentManager.getResolvesAvailable(intent, getExplorer());
        }

        if (resolves.size() == 1) {
            mShare.setVisible(false);
            ResolveInfo app = resolves.get(0);
            CharSequence ttl = app.loadLabel(getContext().getPackageManager());
            intent.setPackage(app.activityInfo.packageName);
            menu.add(10, Menu.NONE, Menu.FIRST, ttl)
                    .setIcon(getResolveIcon(getContext().getPackageManager(), resolves.get(0)))
                    .setIntent(intent).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return;
        } else if (resolves.size() == 0)
            return;

        mShare.setVisible(true);

        ShareActionProvider mShareProvider = (ShareActionProvider)mShare.getActionProvider();

        if (mShareProvider == null)
            mShareProvider = new ShareActionProvider(getContext());

        mShareProvider.setShareIntent(intent);
        mShare.setActionProvider(mShareProvider);

        mShare.setVisible(true);
    }

    public String getString(int resId, String mDefault) {
        if (getExplorer() != null)
            return getExplorer().getString(resId);
        else
            return mDefault;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (this instanceof OpenPathFragmentInterface) {
            OpenPath mPath = ((OpenPathFragmentInterface)this).getPath();
            modifyMenuShare(menu, mPath);
        } else
            MenuUtils.setMenuVisible(menu, false, R.id.menu_context_share);
    }

    private Drawable getResolveIcon(PackageManager pm, ResolveInfo info) {
        Drawable ret = pm.getApplicationIcon(info.activityInfo.applicationInfo);
        if (ret instanceof BitmapDrawable) {
            Bitmap bmp = ((BitmapDrawable)ret).getBitmap();
            ret = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bmp, 48, 48, false));
        } else
            Logger.LogWarning("Unknown drawable: " + ret.getClass().toString());
        return ret;

    }

    public static OpenFragment instantiate(Context context, String fname, Bundle args) {
        String sPath = null;
        OpenPath path = null;
        if (args.containsKey("last"))
            sPath = args.getString("last");
        if (args.containsKey("path"))
            path = (OpenPath)args.getParcelable("path");
        if (args.containsKey("edit_path"))
            return TextEditorFragment
                    .getInstance(((OpenPath)args.getParcelable("edit_path")), args);
        if (sPath != null)
            path = FileManager.getOpenCache(sPath, context);
        if (path != null) {
            if (fname.endsWith("ContentFragment"))
                return ContentFragment.getInstance(path, args);
            else if (fname.endsWith("TextEditorFragment"))
                return TextEditorFragment.getInstance(path, args);
        }
        return null;
    }

    public int compareTo(OpenFragment b) {
        return compare(this, b);
    }

    @Override
    public int compare(OpenFragment a, OpenFragment b) {
        if (a == null && b != null)
            return 1;
        else if (b == null)
            return -1;

        int priA = a.getPagerPriority();
        int priB = b.getPagerPriority();
        // if(DEBUG) Logger.LogDebug("Comparing " + a.getTitle() + "(" + priA +
        // ") to " + b.getTitle() + "(" + priB + ")");
        if (priA != priB) {
            if (priA > priB) {
                // Logger.LogDebug("Switch!");
                return 1;
            } else {
                // Logger.LogDebug("Stay!");
                return -1;
            }
        }
        if (a instanceof ContentFragment && b instanceof ContentFragment) {
            OpenPath pa = ((ContentFragment)a).getPath();
            OpenPath pb = ((ContentFragment)b).getPath();
            if (pa == null && pb != null)
                return 1;
            else if (pb == null && pa != null)
                return -1;
            else if (pa == null || pb == null)
                return 0;
            priA = pa.getPath().length();
            priB = pb.getPath().length();
            if (priA > priB) {
                // Logger.LogDebug("Switch 2!");
                return 1;
            } else {
                // Logger.LogDebug("Stay 2!");
                return -1;
            }
        } else {
            // Logger.LogDebug("0!");
            return 0;
        }
    }

    /**
     * Return priority for ordering in ViewPager (Low to High)
     */
    public int getPagerPriority() {
        return 5;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (DEBUG)
            Logger.LogDebug(getClassName() + ".startActivityForResult(" + requestCode + ","
                    + (intent != null ? intent.toString() : "null") + ")");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG)
            Logger.LogDebug(getClassName() + ".onActivityResult(" + requestCode + "," + resultCode
                    + "," + (data != null ? data.toString() : "null") + ")");
    }

    public boolean showMenu(final Menu menu, View anchor, CharSequence title) {
        if (menu == null || menu.size() == 0)
            return false;
        onPrepareOptionsMenu(menu);
        if (showIContextMenu(menu, anchor, title, 0, 0))
            return true;
        return false;
    }

    public boolean showMenu(final int menuId, View from, CharSequence title) {
        return showMenu(menuId, from, title, 0, 0);
    }

    public boolean showMenu(final int menuId, View from1, CharSequence title, int xOffset,
            int yOffset) {
        if (from1 == null)
            from1 = getSherlockActivity().findViewById(android.R.id.home);
        if (from1 == null)
            from1 = getSherlockActivity().getCurrentFocus().getRootView();
        final View from = from1;
        if (showIContextMenu(menuId, from, title, xOffset, yOffset))
            return true;
        if (Build.VERSION.SDK_INT > 10) {
            final PopupMenu pop = new PopupMenu(getSherlockActivity(), from);
            pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (onOptionsItemSelected(item)) {
                        pop.dismiss();
                        return true;
                    } else if (getExplorer() != null)
                        return getExplorer().onIconContextItemSelected(pop, item,
                                item.getMenuInfo(), from);
                    return false;
                }

                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    return false;
                }
            });
            pop.getMenuInflater().inflate(menuId, pop.getMenu());
            Logger.LogDebug("PopupMenu.show()");
            pop.show();
            return true;
        }
        if (from == null)
            return false;
        from.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu cmenu, View v, ContextMenuInfo menuInfo) {
                new android.view.MenuInflater(v.getContext()).inflate(menuId, cmenu);
                onPrepareOptionsMenu(cmenu);

            }
        });
        boolean ret = from.showContextMenu();
        from.setOnCreateContextMenuListener(null);
        return ret;
    }

    public boolean inflateMenu(Menu menu, int menuItemId, MenuInflater inflater) {
        return false;
    }

    public boolean showIContextMenu(Menu menu, final View from, CharSequence title, int xOffset,
            int yOffset) {
        if (getSherlockActivity() == null)
            return false;
        final IconContextMenu mOpenMenu = new IconContextMenu(getSherlockActivity(), menu, from);
        if (mOpenMenu == null)
            return false;
        if (title != null && title.length() > 0)
            mOpenMenu.setTitle(title);
        if (DEBUG)
            Logger.LogDebug("Showing menu "
                    + menu
                    + (from != null ? " near 0x" + Integer.toHexString(from.getId()) : " by itself"));
        if (getSherlockActivity() != null)
            getSherlockActivity().onPrepareOptionsMenu(menu);
        mOpenMenu.setMenu(menu);
        mOpenMenu.setAnchor(from);
        mOpenMenu.setNumColumns(1);
        mOpenMenu.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
            public void onIconContextItemSelected(final IconContextMenu menu, MenuItem item,
                    Object info, View view) {
                if (onOptionsItemSelected(item))
                    menu.dismiss();
                else if (getExplorer() != null)
                    getExplorer().onIconContextItemSelected(menu, item, info, view);
            }
        });
        return mOpenMenu.show(xOffset, yOffset);
    }

    public boolean showIContextMenu(int menuId, final View from, CharSequence title, int xOffset,
            int yOffset) {
        if (getSherlockActivity() == null)
            return false;
        if (menuId == R.menu.context_file && !OpenExplorer.USE_PRETTY_CONTEXT_MENUS)
            return false;
        final IconContextMenu mOpenMenu = IconContextMenu.getInstance(getSherlockActivity(),
                menuId, from);
        if (mOpenMenu == null)
            return false;
        if (title != null && title.length() > 0)
            mOpenMenu.setTitle(title);
        if (DEBUG)
            Logger.LogDebug("Showing menu 0x"
                    + Integer.toHexString(menuId)
                    + (from != null ? " near 0x" + Integer.toHexString(from.getId()) : " by itself"));
        Menu menu = mOpenMenu.getMenu();
        if (getSherlockActivity() != null)
            getSherlockActivity().onPrepareOptionsMenu(menu);
        mOpenMenu.setMenu(menu);
        mOpenMenu.setAnchor(from);
        mOpenMenu.setNumColumns(1);
        mOpenMenu.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
            public void onIconContextItemSelected(final IconContextMenu menu, MenuItem item,
                    Object info, View view) {
                if (onOptionsItemSelected(item))
                    menu.dismiss();
                else if (getExplorer() != null)
                    getExplorer().onIconContextItemSelected(menu, item, info, view);
            }
        });
        return mOpenMenu.show(xOffset, yOffset);
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        // if(!OpenExplorer.BEFORE_HONEYCOMB) super.setHasOptionsMenu(hasMenu);
        // super.setHasOptionsMenu(hasMenu);
        mHasOptions = hasMenu;
    }

    public boolean hasOptionsMenu() {
        return mHasOptions;
    }

    public boolean onBackPressed() {
        if (getExplorer() != null) {
            try {
                getExplorer().removeFragment(this);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public MenuInflater getSupportMenuInflater() {
        if (getSherlockActivity() != null)
            return getSherlockActivity().getSupportMenuInflater();
        else
            return null;
    }

    public android.view.MenuInflater getMenuInflater() {
        if (getActivity() != null)
            return getActivity().getMenuInflater();
        return null;
    }

    protected String getViewSetting(OpenPath path, String key, String def) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            return def;
        else
            return getFragmentActivity().getSetting(path, key, def);
    }

    protected Boolean getViewSetting(OpenPath path, String key, Boolean def) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            return def;
        else
            return getFragmentActivity().getSetting(path, key, def);
    }

    protected Integer getViewSetting(OpenPath path, String key, Integer def) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            return def;
        else
            return getFragmentActivity().getSetting(path, key, def);
    }

    protected Float getViewSetting(OpenPath path, String key, Float def) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            return def;
        else
            return getFragmentActivity().getSetting(path, key, def);
    }

    protected void setViewSetting(OpenPath path, String key, String value) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            Logger.LogWarning("Unable to setViewSetting");
        else
            getFragmentActivity().setSetting(path, key, value);
    }

    protected void setViewSetting(OpenPath path, String key, Boolean value) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            Logger.LogWarning("Unable to setViewSetting");
        else
            getFragmentActivity().setSetting(path, key, value);
    }

    protected void setViewSetting(OpenPath path, String key, Integer value) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            Logger.LogWarning("Unable to setViewSetting");
        else
            getFragmentActivity().setSetting(path, key, value);
    }

    protected void setViewSetting(OpenPath path, String key, Float value) {
        if (getExplorer() == null || getExplorer().getPreferences() == null)
            Logger.LogWarning("Unable to setViewSetting");
        else
            getFragmentActivity().setSetting(path, key, value);
    }

    protected Integer getSetting(OpenPath file, String key, Integer defValue) {
        if (getSherlockActivity() == null)
            return defValue;
        return getFragmentActivity().getSetting(file, key, defValue);
    }

    protected String getSetting(OpenPath file, String key, String defValue) {
        if (getSherlockActivity() == null)
            return defValue;
        return getFragmentActivity().getSetting(file, key, defValue);
    }

    protected Boolean getSetting(String file, String key, Boolean defValue) {
        if (getSherlockActivity() == null)
            return defValue;
        if (getFragmentActivity().getPreferences() == null)
            return defValue;
        return getFragmentActivity().getPreferences().getSetting(file, key, defValue);
    }

    protected final void setSetting(String file, String key, Boolean value) {
        if (getSherlockActivity() == null)
            return;
        getFragmentActivity().getPreferences().setSetting(file, key, value);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OpenExplorer)
            setOnFragmentDPADListener((OpenExplorer)activity);
        final OpenPath path = (this instanceof OpenPathFragmentInterface) ? ((OpenPathFragmentInterface)this)
                .getPath() : null;
        if (DEBUG)
            Logger.LogDebug("}-- onAttach :: " + getClassName() + " @ " + path);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG)
            Logger.LogDebug("{-- onDetach :: "
                    + getClassName()
                    + (this instanceof OpenPathFragmentInterface
                            && ((OpenPathFragmentInterface)this).getPath() != null ? " @ "
                            + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Logger.LogDebug("[-- onDestroy :: "
                    + getClassName()
                    + (this instanceof OpenPathFragmentInterface
                            && ((OpenPathFragmentInterface)this).getPath() != null ? " @ "
                            + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!item.hasSubMenu()) {
            Logger.LogInfo("Click: MenuItem: " + item.getTitle().toString());
            queueToTracker(new Runnable() {
                public void run() {
                    if (getAnalyticsTracker() != null)
                        getAnalyticsTracker().trackEvent("Clicks", "MenuItem",
                                item.getTitle().toString(), 1);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    protected final void refreshOperations() {
        if (getExplorer() != null)
            getExplorer().refreshOperations();
    }

    protected void finishMode(ActionMode mode) {
        if (mode != null)
            mode.finish();
    }

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get instance of Event Handler.
     * 
     * @return
     */
    protected EventHandler getHandler() {
        return OpenExplorer.getEventHandler();
    }

    /**
     * Get instance of File Manager.
     * 
     * @return
     */
    protected FileManager getManager() {
        return OpenExplorer.getFileManager();
    }

    public Drawable getDrawable(int resId) {
        if (getSherlockActivity() == null)
            return null;
        if (getResources() == null)
            return null;
        return getResources().getDrawable(resId);
    }

    public OpenFragmentActivity getFragmentActivity() {
        return (OpenFragmentActivity)getActivity();
    }

    public OpenExplorer getExplorer() {
        return (OpenExplorer)getActivity();
    }

    public Context getApplicationContext() {
        if (getSherlockActivity() != null)
            return getSherlockActivity().getApplicationContext();
        else
            return null;
    }

    public static EventHandler getEventHandler() {
        return OpenExplorer.getEventHandler();
    }

    public static FileManager getFileManager() {
        return OpenExplorer.getFileManager();
    }

    @Override
    public ActionMode getActionMode() {
        if (getExplorer() != null)
            return getExplorer().getActionMode();
        else
            return null;
    }

    @Override
    public void setActionMode(ActionMode mode) {
        if (getExplorer() != null)
            getExplorer().setActionMode(mode);
    }

    @Override
    public OpenClipboard getClipboard() {
        if (getExplorer() != null)
            return getExplorer().getClipboard();
        else
            return null;
    }

    @Override
    public void removeFromClipboard(OpenPath file) {
        if (getClipboard() != null)
            getClipboard().remove(file);
    }

    @Override
    public boolean checkClipboard(OpenPath file) {
        if (getClipboard() != null)
            return getClipboard().contains(file);
        else
            return false;
    }

    public void onClick(final View v) {
        Logger.LogInfo("Click: Other: " + v != null ? v.getClass().getSimpleName() : "Unknown",
                ViewUtils.getText(v).toString());
        queueToTracker(new Runnable() {
            public void run() {
                if (getAnalyticsTracker() != null)
                    getAnalyticsTracker().trackEvent("Clicks",
                            v != null ? v.getClass().getSimpleName() : "Unknown",
                            ViewUtils.getText(v).toString(), 1);
            }
        });
    }

    public boolean onClick(final int id, final View from) {
        Logger.LogInfo("Click: Other: " + from != null ? from.getClass().getSimpleName()
                : "Unknown", ViewUtils.getText(from).toString());
        queueToTracker(new Runnable() {
            public void run() {
                if (getAnalyticsTracker() != null)
                    getAnalyticsTracker().trackEvent("Clicks",
                            from != null ? from.getClass().getSimpleName() : "Unknown",
                            ViewUtils.getText(from).toString(), 1);
            }
        });
        return false;
    }

    public boolean onLongClick(final View v) {
        Logger.LogInfo("Long Click: Other: " + ViewUtils.getText(v).toString());
        queueToTracker(new Runnable() {
            public void run() {
                if (v != null && getAnalyticsTracker() != null)
                    getAnalyticsTracker().trackEvent("Long-Clicks", v.getClass().toString(),
                            ViewUtils.getText(v).toString(), 1);
            }
        });
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG)
            Logger.LogDebug("]-- onCreate - "
                    + getClassName()
                    + (this instanceof OpenPathFragmentInterface
                            && ((OpenPathFragmentInterface)this).getPath() != null ? "#"
                            + ((OpenPathFragmentInterface)this).getPath().getPath() : ""));
        // CONTENT_FRAGMENT_FREE = false;
        setRetainInstance(false);
        super.onCreate(savedInstanceState);
    }

    public void invalidateOptionsMenu() {
        if (getExplorer() == null)
            return;
        try {
            getExplorer().invalidateOptionsMenu();
        } catch (Exception e) {
            Logger.LogError("Unable to invalidate fragment options menu.", e);
        }
    }

    public abstract Drawable getIcon();

    public abstract CharSequence getTitle();

    public void notifyPager() {
        if (getExplorer() != null)
            getExplorer().notifyPager();
    }

    public void sendToLogView(String txt, int color) {
        if (getExplorer() != null)
            getExplorer().sendToLogView(txt, color);
    }

    public void doClose() {
        if (getExplorer() != null && getExplorer().isViewPagerEnabled())
            getExplorer().closeFragment(this);
        else if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else if (getSherlockActivity() != null)
            getSherlockActivity().finish();
    }

    public View getActionView(MenuItem item) {
        return item.getActionView();
    }

    @Override
    public Context getContext() {
        if (getExplorer() != null)
            return getExplorer().getContext();
        else
            return getApplicationContext();
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
    public Preferences getPreferences() {
        return getExplorer().getPreferences();
    }

    @Override
    public void refreshBookmarks() {
        getExplorer().refreshBookmarks();
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
    public GoogleAnalyticsTracker getAnalyticsTracker() {
        if (getExplorer() != null)
            return getExplorer().getAnalyticsTracker();
        else
            return null;
    }

    @Override
    public void queueToTracker(Runnable run) {
        if (getExplorer() != null)
            getExplorer().queueToTracker(run);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (DEBUG)
            Logger.LogDebug("<-- onViewCreated - " + getClassName());
        super.onViewCreated(view, savedInstanceState);
    }

    public View getTitleView() {
        if (getExplorer() != null)
            return getExplorer().getPagerTitleView(this);
        return null;
    }

    @Override
    public int getThemedResourceId(int styleableId, int defaultResourceId) {
        if (getExplorer() == null)
            return defaultResourceId;
        return getExplorer().getThemedResourceId(styleableId, defaultResourceId);
    }

    /*
     * @Override public void onDestroy() { Logger.LogDebug("--> onDestroy - " +
     * getClassName()); super.onDestroy(); }
     * @Override public void onActivityCreated(Bundle savedInstanceState) {
     * Logger.LogDebug("<-- onActivityCreated - " + getClassName());
     * super.onActivityCreated(savedInstanceState); }
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) {
     * Logger.LogDebug("<-- onCreateView - " + getClassName()); return
     * super.onCreateView(inflater, container, savedInstanceState); }
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) {
     * Logger.LogDebug("<-onCreateView - " + getClassName());
     * //CONTENT_FRAGMENT_FREE = false; return super.onCreateView(inflater,
     * container, savedInstanceState); }
     * @Override public void onViewCreated(View view, Bundle savedInstanceState)
     * { super.onViewCreated(view, savedInstanceState);
     * Logger.LogDebug("<-onViewCreated - " + getClassName()); }
     * @Override public void onPause() { super.onPause();
     * Logger.LogDebug("->onPause - " + getClassName()); }
     * @Override public void onResume() { super.onResume();
     * Logger.LogDebug("<-onResume - " + getClassName()); }
     * @Override public void onStart() { super.onStart();
     * Logger.LogDebug("<-onStart - " + getClassName()); }
     * @Override public void onStop() { super.onStop();
     * Logger.LogDebug("->onStop - " + getClassName()); //CONTENT_FRAGMENT_FREE
     * = true; }
     * @Override public void onSaveInstanceState(Bundle outState) {
     * super.onSaveInstanceState(outState);
     * Logger.LogDebug("->onSaveInstanceState - " + getClassName()); }
     */
}

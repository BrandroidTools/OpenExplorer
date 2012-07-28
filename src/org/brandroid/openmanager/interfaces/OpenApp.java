package org.brandroid.openmanager.interfaces;

import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.LruCache;
import org.brandroid.utils.Preferences;

import com.actionbarsherlock.view.ActionMode;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Looper;

public interface OpenApp {
    public DataManager getDataManager();
    public ImageCacheService getImageCacheService();
    public DownloadCache getDownloadCache();
    public ThreadPool getThreadPool();
    public LruCache<String, Bitmap> getMemoryCache();
    public DiskLruCache getDiskCache();
    public ActionMode getActionMode();
    public void setActionMode(ActionMode mode);
    public OpenClipboard getClipboard();
    public ShellSession getShellSession();

    public Context getContext();
    public Looper getMainLooper();
    public ContentResolver getContentResolver();
    public Resources getResources();
	public Preferences getPreferences();
	public void refreshBookmarks();
	public GoogleAnalyticsTracker getAnalyticsTracker();
	
	public void queueToTracker(Runnable run);
	
	public int getThemedResourceId(int styleableId, int defaultResourceId);
}

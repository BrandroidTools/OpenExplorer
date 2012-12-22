
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

    /**
     * Taken from Gallery3D implementation. Not fully implemented. Return
     * instance of Data Manager.
     * 
     * @return
     */
    public DataManager getDataManager();

    /**
     * Taken from Gallery3D implementation. Not fully implemented. Return
     * instance of Image Cache Service.
     * 
     * @return
     */
    public ImageCacheService getImageCacheService();

    /**
     * Taken from Gallery3D implementation. Not fully implemented. Return
     * instance of Download Cache.
     * 
     * @return
     */
    public DownloadCache getDownloadCache();

    /**
     * Taken from Gallery3D implementation. Not fully implemented. Return
     * instance of Thread Pool.
     * 
     * @return
     */
    public ThreadPool getThreadPool();

    /**
     * Return instance of Memory Cache manager. This is temporary limited memory
     * utilizing Last Recently Used Cache. Used for storing thumbnails for
     * List/GridView.
     * 
     * @return
     */
    public LruCache<String, Bitmap> getMemoryCache();

    /**
     * Return instance of Disk Cache manager. This is long-term (relative to
     * {@link getMemoryCache()}) disk-based memory, used for caching image
     * thumbnails.
     * 
     * @return
     */
    public DiskLruCache getDiskCache();

    /**
     * Return instance of ActionMode (if activated).
     * 
     * @return {@code ActionMode} instance if activated, null if not.
     */
    public ActionMode getActionMode();

    public void setActionMode(ActionMode mode);

    /**
     * Return instance of system Clipboard object.
     * 
     * @return
     * @see org.brandroid.openmanager.adapters.OpenManager
     */
    public OpenClipboard getClipboard();

    /**
     * Return instance of active Shell Session. This should be used for Root
     * commands only.
     * 
     * @return
     */
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

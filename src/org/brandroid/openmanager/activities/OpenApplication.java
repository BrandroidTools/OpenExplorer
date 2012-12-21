
package org.brandroid.openmanager.activities;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LruCache;
import org.brandroid.utils.Preferences;

import com.actionbarsherlock.view.ActionMode;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.SparseArray;

public class OpenApplication extends Application implements OpenApp {
	private static final String DOWNLOAD_FOLDER = "download";
	private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

	private DataManager mDataManager;
	private ImageCacheService mImageCacheService;
	private ThreadPool mThreadPool;
	private DownloadCache mDownloadCache;
	private LruCache<String, Bitmap> mBitmapCache;
	private DiskLruCache mBitmapDiskCache;
	private ShellSession mShell;
	private ActionMode mActionMode;
	private SparseArray<Integer> mThemedAssets = new SparseArray<Integer>();

	@Override
	public void onCreate() {
		super.onCreate();
		setTheme(R.style.AppTheme_Dark);
		// Logger.LogDebug("OpenApplication.onCreate");
		// mTracker = GoogleAnalyticsTracker.getInstance();
		// GalleryUtils.initialize(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (mBitmapCache != null)
			mBitmapCache.clear();
		if (mBitmapDiskCache != null)
			try {
				mBitmapDiskCache.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

	}

	public void loadThemedAssets(Context c) {
		mThemedAssets.clear();
        TypedArray ta = c.getTheme().obtainStyledAttributes(R.styleable.AppTheme);
        mThemedAssets.put(R.styleable.AppTheme_checkboxButtonOff, ta.getResourceId(
                R.styleable.AppTheme_checkboxButtonOff, R.drawable.btn_check_off_holo_dark));
        mThemedAssets.put(R.styleable.AppTheme_checkboxButtonOn, ta.getResourceId(
                R.styleable.AppTheme_checkboxButtonOn, R.drawable.btn_check_on_holo_dark));
        mThemedAssets.put(R.styleable.AppTheme_actionIconClipboard, ta.getResourceId(
                R.styleable.AppTheme_actionIconClipboard, R.drawable.ic_menu_clipboard));
        mThemedAssets.put(R.styleable.AppTheme_dialogBackgroundColorPrimary,
                ta.getResourceId(R.styleable.AppTheme_dialogBackgroundColorPrimary, R.color.black));
        mThemedAssets.put(R.styleable.AppTheme_actionIconShare, ta.getResourceId(
                R.styleable.AppTheme_actionIconShare, R.drawable.ic_action_share_holo_dark));
		ta.recycle();
	}

	public int getThemedResourceId(int stylableId, int defaultResourceId) {
		if (mThemedAssets.indexOfKey(stylableId) < 0)
			return defaultResourceId;
		return mThemedAssets.get(stylableId);
	}

	public Context getContext() {
		return this;
	}

	public synchronized ActionMode getActionMode() {
		return mActionMode;
	}

	public synchronized void setActionMode(ActionMode mode) {
		mActionMode = mode;
	}

	public synchronized OpenClipboard getClipboard() {
		return null; // Override in Activity
	}

	public synchronized ShellSession getShellSession() {
		if (mShell == null)
			mShell = new ShellSession();
		return mShell;
	}

	public synchronized DataManager getDataManager() {
		if (mDataManager == null) {
			mDataManager = new DataManager(this);
			mDataManager.initializeSourceMap();
		}
		return mDataManager;
	}

	public synchronized ImageCacheService getImageCacheService() {
		if (mImageCacheService == null) {
			mImageCacheService = new ImageCacheService(getContext());
		}
		return mImageCacheService;
	}

	public synchronized ThreadPool getThreadPool() {
		if (mThreadPool == null) {
			mThreadPool = new ThreadPool();
		}
		return mThreadPool;
	}

	public synchronized DownloadCache getDownloadCache() {
		if (mDownloadCache == null) {
			File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);

			if (!cacheDir.isDirectory())
				cacheDir.mkdirs();

			if (!cacheDir.isDirectory()) {
                throw new RuntimeException("fail to create: " + cacheDir.getAbsolutePath());
			}
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
		}
		return mDownloadCache;
	}

	@Override
	public synchronized LruCache<String, Bitmap> getMemoryCache() {
		if (mBitmapCache == null)
			mBitmapCache = new LruCache<String, Bitmap>(
                    ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 10);
		return mBitmapCache;
	}

	@Override
	public DiskLruCache getDiskCache() {
		if (mBitmapDiskCache == null)
			try {
				mBitmapDiskCache = DiskLruCache.open(getFilesDir(), 1, 10, 200);
			} catch (IOException e) {
				Logger.LogError("Couldn't instantiate Disk Cache");
			}
		return mBitmapDiskCache;
	}

	@Override
	public Preferences getPreferences() {
		return new Preferences(getContext());
	}

	@Override
	public void refreshBookmarks() {

	}
}

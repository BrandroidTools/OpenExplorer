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

package org.brandroid.openmanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.concurrent.RejectedExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCommand;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenSFTP;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.ImageView;

public class ThumbnailCreator {
    // private static HashMap<String, Bitmap> mCacheMap = new HashMap<String,
    // Bitmap>();
    // private static LruCache<String, Bitmap> mCacheMap = new LruCache<String,
    // Bitmap>(200);
    private Handler mHandler;

    private boolean mStop = false;
    private static int iVideoThumbErrors = 0;

    public static boolean useCache = true;
    public static boolean showThumbPreviews = true;
    public static boolean showCenteredCroppedPreviews = false;

    private static Hashtable<String, Integer> fails = new Hashtable<String, Integer>();

    // private static Hashtable<String, Drawable> defaultDrawables = new
    // Hashtable<String, Drawable>();

    public interface OnUpdateImageListener {
        void updateImage(Bitmap d);
    }

    public static void postImageBitmap(final ImageView image, final Bitmap bmp) {
        if (Thread.currentThread().equals(OpenExplorer.UiThread))
            image.setImageBitmap(bmp);
        else
            image.post(new Runnable() {
                @Override
                public void run() {
                    image.setImageBitmap(bmp);
                }
            });
    }

    public static void postImageResource(final ImageView image, final int resId) {
        if (Thread.currentThread().equals(OpenExplorer.UiThread))
            image.setImageResource(resId);
        else
            image.post(new Runnable() {
                @Override
                public void run() {
                    image.setImageResource(resId);
                }
            });
    }

    public static void postImageFromPath(final ImageView mImage, final OpenPath file,
            final boolean useLarge) {
        mImage.post(new Runnable() {
            @Override
            public void run() {
                mImage.setImageBitmap(getFileExtIcon(file.getExtension(), mImage.getContext(),
                        useLarge));
            }
        });
    }

    public static boolean setThumbnail(final OpenApp app, final ImageView mImage,
            final OpenPath file, final int mWidth, final int mHeight,
            final OnUpdateImageListener mListener) {
        // if(mImage instanceof RemoteImageView)
        // {
        // return setThumbnail((RemoteImageView)mImage, file, mWidth, mHeight);
        // }
        if (file == null)
            return false;
        if (mImage == null)
            return false;
        final String mName = file.getName();
        final String ext = mName.substring(mName.lastIndexOf(".") + 1);
        final String sPath2 = mName.toLowerCase();
        final boolean useLarge = mWidth > 72;

        final Context mContext = mImage.getContext().getApplicationContext();

        if (!file.isDirectory() && file.isTextFile())
            postImageFromPath(mImage, file, useLarge);
        else if (mImage.getTag() == null)
            postImageResource(mImage, getDefaultResourceId(file, mWidth, mHeight));

        if (file.hasThumbnail()) {
            if (showThumbPreviews && !file.requiresThread()) {
                Bitmap thumb = // !mCacheMap.containsKey(file.getPath()) ? null
                // :
                getThumbnailCache(app, file.getPath(), mWidth, mHeight);

                if (thumb == null) {
                    mImage.setImageResource(getDefaultResourceId(file, mWidth, mHeight));
                    // ThumbnailTask task = new ThumbnailTask();
                    // ThumbnailStruct struct = new ThumbnailStruct(file,
                    // mListener, mWidth, mHeight);
                    // if(mImage.getTag() != null && mImage.getTag() instanceof
                    // ThumbnailTask)
                    // ((ThumbnailTask)mImage.getTag()).cancel(true);

                    try {
                        if (!fails.containsKey(file.getPath())) {
                            if (!app.getMemoryCache().containsKey(file.getPath()))
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            SoftReference<Bitmap> gen = generateThumb(app, file,
                                                    mWidth, mHeight, mContext);
                                            if (gen != null && gen.get() != null)
                                                mListener.updateImage(gen.get());
                                            else
                                                Logger.LogWarning("Couldn't generate thumb for "
                                                        + file.getPath());
                                        } catch (OutOfMemoryError e) {
                                            showThumbPreviews = false;
                                            Logger.LogWarning("No more memory for thumbs!");
                                        }
                                    }
                                }).start();
                            else
                                // new Thread(new Runnable() {public void run()
                                // {
                                mListener.updateImage(getThumbnailCache(app, file.getPath(),
                                        mWidth, mHeight));
                            // }}).start();
                        }
                        // mImage.setTag(task);
                        // if(struct != null) task.execute(struct);
                    } catch (RejectedExecutionException rej) {
                        Logger.LogError(
                                "Couldn't generate thumbnail because Thread pool was full.", rej);
                    }
                }
                if (thumb != null) {
                    // final BitmapDrawable bd = new
                    // BitmapDrawable(mContext.getResources(), thumb);
                    // bd.setGravity(Gravity.CENTER);
                    // mListener.updateImage(thumb);
                    mImage.setImageBitmap(thumb);
                    mImage.setTag(file.getPath());
                    /*
                     * mImage.post(new Runnable(){ public void run() {
                     * ImageUtils.fadeToDrawable(mImage, bd);
                     * mImage.setTag(file.getPath()); } });
                     */
                }

            }

        }
        return false;
    }

    public static String getImagePath(ImageView mImage) {
        Object ret = mImage.getTag();
        if (ret != null && ret instanceof String)
            return (String)ret;
        return "";
    }

    public static boolean setThumbnail(OpenApp app, final RemoteImageView mImage, OpenPath file,
            int mWidth, int mHeight) {
        final String mName = file.getName();
        final String ext = mName.substring(mName.lastIndexOf(".") + 1);
        final String sPath2 = mName.toLowerCase();
        final boolean useLarge = mWidth > 72;
        if (getImagePath(mImage).equals(file.getPath()))
            return true;

        final Context mContext = mImage.getContext().getApplicationContext();

        if (!file.isDirectory() && file.isTextFile()) {
            mImage.post(new Runnable() {
                @Override
                public void run() {
                    mImage.setImageBitmap(getFileExtIcon(ext, mContext, useLarge));
                }
            });
        } else if (mImage.getDrawable() == null)
            mImage.setImageDrawable(mContext.getResources().getDrawable(
                    getDefaultResourceId(file, mWidth, mHeight)));

        if (file.hasThumbnail()) {
            if (showThumbPreviews && !file.requiresThread()) {

                Bitmap thumb = ThumbnailCreator.getThumbnailCache(app, file.getPath(), mWidth,
                        mHeight);

                if (thumb == null) {
                    mImage.setImageDrawable(mContext.getResources().getDrawable(
                            getDefaultResourceId(file, mWidth, mHeight)));
                    // mImage.setImageFromFile(file, mWidth, mHeight);
                    /*
                     * ThumbnailTask task = new ThumbnailTask(); ThumbnailStruct
                     * struct = new ThumbnailStruct(file, mImage, mWidth,
                     * mHeight); if(mImage.getTag() != null && mImage.getTag()
                     * instanceof ThumbnailTask)
                     * ((ThumbnailTask)mImage.getTag()).cancel(true); try {
                     * mImage.setTag(task); if(struct != null)
                     * task.execute(struct); } catch(RejectedExecutionException
                     * rej) { Logger.LogError(
                     * "Couldn't generate thumbnail because Thread pool was full."
                     * , rej); }
                     */
                }
                if (thumb != null) {
                    BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), thumb);
                    bd.setGravity(Gravity.CENTER);
                    ImageUtils.fadeToDrawable(mImage, bd);
                    mImage.setTag(file.getPath());
                }

            }

        }
        return false;
    }

    public static Bitmap getFileExtIcon(String ext, Context mContext, Boolean useLarge) {
        Bitmap src = BitmapFactory.decodeResource(mContext.getResources(),
                useLarge ? R.drawable.lg_file : R.drawable.sm_file);
        Bitmap b = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint();
        c.drawBitmap(src, 0, 0, p);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setTextAlign(Align.CENTER);
        p.setTypeface(Typeface.MONOSPACE);
        float th = src.getHeight() * (3f / 16f);
        p.setTextSize(th);
        p.setShadowLayer(2.5f, 1, 1, Color.BLACK);
        c.drawText(ext, src.getWidth() / 2, (src.getHeight() / 2) + ((th / 3) * 2), p);
        c.save();
        return b;
    }

    public static Drawable getDefaultDrawable(OpenPath file, int mWidth, int mHeight, Context c) {
        if (c != null && c.getResources() != null)
            return c.getResources().getDrawable(getDefaultResourceId(file, mWidth, mHeight));
        else
            return null;
    }

    public static int getDefaultResourceId(OpenPath file, int mWidth, int mHeight) {
        final String mName = file.getName();
        final String ext = mName.substring(mName.lastIndexOf(".") + 1);
        final String mime = file.getMimeType();
        final String sPath2 = mName.toLowerCase();
        final boolean useLarge = mWidth > 36;
        boolean hasKids = false;
        try {
            if (!file.requiresThread() && file.isDirectory())
                hasKids = file.getChildCount(false) > 0;
            else if (file instanceof OpenCursor)
                hasKids = true;
        } catch (IOException e) {
            //TODO Catch exception properly
        }

        if (file.isDirectory()) {
            // Network Object Icons
            if (file instanceof OpenSMB) {
                return (useLarge ? R.drawable.lg_folder_pipe : R.drawable.sm_folder_pipe);
            }
            if (file instanceof OpenSFTP) {
                return (useLarge ? R.drawable.lg_folder_secure : R.drawable.sm_folder_secure);
            }
            if (file instanceof OpenFTP) {
                return (useLarge ? R.drawable.lg_ftp : R.drawable.sm_ftp);
            }

            // Local Filesystem Icons
            if (file.getAbsolutePath() != null && file.getAbsolutePath().equals("/")
                    && mName.equals("")) {
                return useLarge ? R.drawable.lg_drive : R.drawable.sm_drive;
            } else if (file instanceof OpenSmartFolder || sPath2.indexOf("download") > -1) {
                return (useLarge ? R.drawable.lg_download : R.drawable.sm_download);
            } else if ((mName.equalsIgnoreCase("Photos") || mName.equalsIgnoreCase("dcim")
                    || mName.equalsIgnoreCase("pictures") || mName.equalsIgnoreCase("camera"))) {
                return (useLarge ? R.drawable.lg_photo : R.drawable.sm_photo);
            } else if (mName.equalsIgnoreCase("Videos") || mName.equalsIgnoreCase("Movies")) {
                return (useLarge ? R.drawable.lg_movie : R.drawable.sm_movie);
            } else if (hasKids && mName.equals("Music")) {
                return (useLarge ? R.drawable.lg_music : R.drawable.sm_music);
            } else if (hasKids
                    && (sPath2.indexOf("ext") > -1 || sPath2.indexOf("sdcard") > -1 || sPath2
                            .indexOf("microsd") > -1)) {
                return (useLarge ? R.drawable.lg_sdcard : R.drawable.sm_sdcard);
            } else if (hasKids
                    && (sPath2.indexOf("usb") > -1 || sPath2.indexOf("/mnt/media/") > -1 || sPath2
                            .indexOf("removeable") > -1)) {
                return (useLarge ? R.drawable.lg_usb : R.drawable.sm_usb);
            } else if (hasKids) {
                return (useLarge ? R.drawable.lg_folder_full : R.drawable.sm_folder_full);
            } else {
                return (useLarge ? R.drawable.lg_folder : R.drawable.sm_folder);
            }

        } else if (file instanceof OpenCommand) {
            return ((OpenCommand)file).getDrawableId();

        } else if (ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx")) {
            return (useLarge ? R.drawable.lg_doc : R.drawable.sm_doc);

        } else if (ext.equalsIgnoreCase("xls") || ext.equalsIgnoreCase("xlsx")
                || ext.equalsIgnoreCase("csv") || ext.equalsIgnoreCase("xlsm")) {
            return (useLarge ? R.drawable.lg_excel : R.drawable.sm_excel);

        } else if (ext.equalsIgnoreCase("ppt") || ext.equalsIgnoreCase("pptx")) {
            return (useLarge ? R.drawable.lg_powerpoint : R.drawable.sm_powerpoint);

        } else if (mime.contains("zip") || mime.contains("tar")) {
            return (useLarge ? R.drawable.lg_zip : R.drawable.sm_zip);

        } else if (ext.equalsIgnoreCase("pdf")) {
            return (useLarge ? R.drawable.lg_pdf : R.drawable.sm_pdf);

        } else if (ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("html")) {
            return (useLarge ? R.drawable.lg_xml_html : R.drawable.sm_xml_html);

        } else if (ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wav")
                || ext.equalsIgnoreCase("wma") || ext.equalsIgnoreCase("m4p")
                || ext.equalsIgnoreCase("m4a") || ext.equalsIgnoreCase("ogg")) {
            return (useLarge ? R.drawable.lg_music : R.drawable.sm_music);

        } else if (file.isImageFile()) {
            return (useLarge ? R.drawable.lg_photo_50 : R.drawable.sm_photo_50);

        } else if (file.isAPKFile()) {
            return (useLarge ? R.drawable.lg_apk : R.drawable.sm_apk);

        } else if (file.isVideoFile()) {
            return (useLarge ? R.drawable.lg_movie : R.drawable.sm_movie);

        } else if (file instanceof OpenFTP && file.isDirectory()) {
            return (useLarge ? R.drawable.lg_ftp : R.drawable.sm_ftp);

        } else if (file instanceof OpenNetworkPath && file.isDirectory()) {
            return (useLarge ? R.drawable.lg_folder_secure : R.drawable.sm_folder_secure);

        } else if (file.isTextFile()) {
            return (useLarge ? R.drawable.lg_file : R.drawable.sm_file);

        } else {
            return (useLarge ? R.drawable.lg_unknown : R.drawable.sm_unknown);
        }
    }

    public static int getDrawerResourceId(OpenPath file) {
        final String mName = file.getName();
        final String ext = mName.substring(mName.lastIndexOf(".") + 1);
        final String sPath2 = mName.toLowerCase();
        final boolean useLarge = true;
        boolean hasKids = false;
        try {
            if (!file.requiresThread() && file.isDirectory())
                hasKids = file.getChildCount(false) > 0;
            else if (file instanceof OpenCursor)
                hasKids = true;
        } catch (IOException e) {
        }

        if (file.isDirectory()) {
            // Network Object Icons
            if (file instanceof OpenSMB) {
                return R.drawable.lg_folder_pipe;
            }
            if (file instanceof OpenSFTP) {
                return R.drawable.lg_folder_secure;
            }
            if (file instanceof OpenFTP) {
                return R.drawable.lg_ftp;
            }

            // Local Filesystem Icons
            if (file.getAbsolutePath() != null && file.getAbsolutePath().equals("/")
                    && mName.equals("")) {
                return R.drawable.lg_drive;
            } else if (file instanceof OpenSmartFolder || sPath2.indexOf("download") > -1) {
                return R.drawable.ic_drawer_download_holo_dark;
            } else if ((mName.equalsIgnoreCase("Photos") || mName.equalsIgnoreCase("dcim")
                    || mName.equalsIgnoreCase("pictures") || mName.equalsIgnoreCase("camera"))) {
                return R.drawable.ic_drawer_picture_holo_dark;
            } else if (mName.equalsIgnoreCase("Videos") || mName.equalsIgnoreCase("Movies")) {
                return R.drawable.ic_drawer_video_holo_dark;
            } else if (hasKids && mName.equals("Music")) {
                return R.drawable.ic_drawer_music_holo_dark;
            } else if (hasKids
                    && (sPath2.indexOf("ext") > -1 || sPath2.indexOf("sdcard") > -1 || sPath2
                            .indexOf("microsd") > -1)) {
                return R.drawable.lg_sdcard;
            } else if (hasKids
                    && (sPath2.indexOf("usb") > -1 || sPath2.indexOf("/mnt/media/") > -1 || sPath2
                            .indexOf("removeable") > -1)) {
                return R.drawable.lg_usb;
            } else {
                return R.drawable.ic_drawer_folder_holo_dark;
            }

        } else if (file instanceof OpenFTP && file.isDirectory()) {
            return (useLarge ? R.drawable.lg_ftp : R.drawable.sm_ftp);

        } else if (file instanceof OpenNetworkPath && file.isDirectory()) {
            return (useLarge ? R.drawable.lg_folder_secure : R.drawable.sm_folder_secure);

        } else {
            return (useLarge ? R.drawable.lg_unknown : R.drawable.sm_unknown);
        }
    }

    public static boolean hasThumbnailCached(OpenApp app, OpenPath file, int w, int h) {
        return app.getMemoryCache().containsKey(getCacheFilename(file.getPath(), w, h));
    }

    public static Bitmap getThumbnailCache(OpenApp app, OpenPath file, int w, int h) {
        return getThumbnailCache(app, getCacheFilename(file.getPath(), w, h), w, h);
    }

    public static Bitmap getThumbnailCache(OpenApp app, String name, int w, int h) {
        String cacheName = getCacheFilename(name, w, h);
        if (!app.getMemoryCache().containsKey(cacheName)) {
            File f = app.getContext().getFileStreamPath(cacheName);
            if (f.exists()) {
                if (f.length() > 0)
                    app.getMemoryCache().put(cacheName, BitmapFactory.decodeFile(f.getPath()));
                else
                    fails.put(name, 1);
            }
        }
        if (app.getMemoryCache().containsKey(cacheName))
            return app.getMemoryCache().get(cacheName);
        return null;
    }

    private static void putThumbnailCache(OpenApp app, String cacheName, Bitmap value) {
        // String cacheName = getCacheFilename(name, w, h);
        app.getMemoryCache().put(cacheName, value);
    }

    private static String getCacheFilename(String path, int w, int h) {
        return w + "_" + Utils.md5(path).replaceAll("[^A-Za-z0-9]", "-");
        // path.replaceAll("[^A-Za-z0-9]", "-") + ".jpg";
    }

    public static SoftReference<Bitmap> generateThumb(final OpenApp app, final OpenPath file,
            int mWidth, int mHeight, Context context) {
        return generateThumb(app, file, mWidth, mHeight, true, true, context);
    }

    public static SoftReference<Bitmap> generateThumb(final OpenApp app, final OpenPath file,
            int mWidth, int mHeight, boolean readCache, boolean writeCache, Context mContext) {
        final boolean useLarge = mWidth > 72;
        // SoftReference<Bitmap> mThumb = null;
        Bitmap bmp = null;
        // final Handler mHandler = next.Handler;

        // readCache = writeCache = true;

        Boolean useGeneric = false;
        String mParent = file.getParent() != null ? file.getParent().getName() : null;
        if (fails == null)
            fails = new Hashtable<String, Integer>();
        if (mParent != null
                && (fails.containsKey(file.getPath()) || (fails.containsKey(mParent) && fails
                        .get(mParent) > 10)))
            useGeneric = true;

        if (mContext != null) {
            if (!file.isDirectory() && file.isTextFile())
                return new SoftReference<Bitmap>(
                        getFileExtIcon(file.getName()
                                .substring(file.getName().lastIndexOf(".") + 1).toUpperCase(),
                                mContext, mWidth > 72));

            bmp = BitmapFactory.decodeResource(mContext.getResources(),
                    getDefaultResourceId(file, mWidth, mHeight));
            if (file.requiresThread() || useGeneric)
                return new SoftReference<Bitmap>(bmp);
        }

        String path = file.getPath();

        if ((file.isImageFile() || file.isVideoFile() || file.isAPKFile())
                && (bmp = getThumbnailCache(app, path, mWidth, mHeight)) != null)
            return new SoftReference<Bitmap>(bmp);

        final String mCacheFilename = getCacheFilename(path, mWidth, mHeight);

        // we already loaded this thumbnail, just return it.
        if (app.getMemoryCache().get(mCacheFilename) != null)
            return new SoftReference<Bitmap>(app.getMemoryCache().get(mCacheFilename));
        if (readCache && bmp == null)
            bmp = loadThumbnail(mContext, mCacheFilename);

        float density = mContext.getResources().getDisplayMetrics().density;
        mWidth *= density;
        mHeight *= density;

        if (bmp == null && !useGeneric && !OpenExplorer.LOW_MEMORY) {
            Boolean valid = false;
            if ((file instanceof OpenMediaStore || file instanceof OpenCursor)
                    && (!fails.containsKey(mParent) || fails.get(mParent) < 10)
                    && !new File(mCacheFilename).exists()) {
                if (!fails.containsKey(mParent))
                    fails.put(mParent, 0);
                OpenMediaStore om = (OpenMediaStore)file;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 1;
                opts.inPurgeable = true;
                opts.outHeight = mHeight;
                // if(!showCenteredCroppedPreviews)
                //    opts.outWidth = mWidth;
                int kind = mWidth > 96 ? MediaStore.Video.Thumbnails.MINI_KIND
                        : MediaStore.Video.Thumbnails.MICRO_KIND;
                try {
                    if (om.getParent().getName().equals("Photos"))
                        bmp = MediaStore.Images.Thumbnails.getThumbnail(
                                mContext.getContentResolver(), om.getMediaID(), kind, opts);
                    else // if(om.getParent().getName().equals("Videos"))
                    if (iVideoThumbErrors < 5)
                        bmp = MediaStore.Video.Thumbnails.getThumbnail(
                                mContext.getContentResolver(), om.getMediaID(), kind, opts);
                } catch (Exception e) {
                    iVideoThumbErrors++;
                    Logger.LogWarning("Couldn't get MediaStore thumbnail for " + file.getName(), e);
                }
                if (bmp != null) {
                    // Logger.LogDebug("Bitmap is " + bmp.getWidth() + "x" +
                    // bmp.getHeight() + " to " + mWidth + "x" + mHeight);
                    valid = true;
                } else
                    Logger.LogError("Unable to create MediaStore thumbnail for " + file);
            }
            if (!valid && file.isAPKFile() && !useGeneric && file instanceof OpenFile) {
                // Logger.LogInfo("Getting apk icon for " + file.getName());
                JarFile apk = null;
                InputStream in = null;
                try {
                    apk = new JarFile(((OpenFile)file).getFile());
                    JarEntry icon = apk.getJarEntry("res/drawable-hdpi/icon.apk");
                    if (icon != null && icon.getSize() > 0) {
                        in = apk.getInputStream(icon);
                        bmp = BitmapFactory.decodeStream(in);
                        in.close();
                        in = null;
                    }
                    if (!valid) {
                        PackageManager man = mContext.getPackageManager();
                        PackageInfo pinfo = man.getPackageArchiveInfo(file.getAbsolutePath(),
                                PackageManager.GET_ACTIVITIES);
                        if (pinfo != null) {
                            ApplicationInfo ainfo = pinfo.applicationInfo;
                            if (Build.VERSION.SDK_INT >= 8)
                                ainfo.publicSourceDir = ainfo.sourceDir = file.getPath();
                            Drawable mIcon = ainfo.loadIcon(man);
                            if (mIcon != null)
                                bmp = ((BitmapDrawable)mIcon).getBitmap();
                        }
                    }
                    if (!valid) {
                        Logger.LogWarning("Couldn't get icon for " + file.getAbsolutePath());
                        String iconName = "icon"; // getIconName(apk, file);
                        if (iconName.indexOf(" ") > -1)
                            iconName = "icon";
                        for (String s : new String[] {
                                "drawable-mdpi", "drawable", "drawable-hdpi", "drawable-ldpi"
                        }) {
                            icon = apk.getJarEntry("res/" + s + "/" + iconName + ".png");
                            if (icon != null && icon.getSize() > 0) {
                                Logger.LogDebug("Found fallback icon (res/" + s + "/" + iconName
                                        + ".png)");
                                in = apk.getInputStream(icon);
                                bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in),
                                        mWidth, mHeight, true);
                                in.close();
                                in = null;
                                break;
                            }
                        }
                    }
                    if (bmp == null)
                        bmp = BitmapFactory.decodeResource(mContext.getResources(),
                                useLarge ? R.drawable.lg_apk : R.drawable.sm_apk);
                    else
                        saveThumbnail(mContext, mCacheFilename, bmp);
                } catch (IOException ix) {
                    Logger.LogWarning("Invalid APK: " + file.getPath());
                } finally {
                    try {
                        if (apk != null)
                            apk.close();
                    } catch (IOException nix) {
                        Logger.LogError("Error closing APK while handling invalid APK exception.",
                                nix);
                    }
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException nix) {
                        Logger.LogError(
                                "Error closing input stream while handling invalid APK exception.",
                                nix);
                    }
                }
            } else if (!valid && file.isImageFile() && !file.requiresThread() && !useGeneric) {
                // mHeight *= 2; mWidth *= 2;
                // long len_kb = file.length() / 1024;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                // BitmapFactory.decodeFile(file.getPath(), options);
                options.inSampleSize = Math.min(options.outWidth / mWidth, options.outHeight
                        / mHeight);
                options.inJustDecodeBounds = false;
                options.inPurgeable = true;
                bmp = BitmapFactory.decodeFile(file.getPath(), options);
            } else if (bmp == null && file.getClass().equals(OpenFile.class)) {
                useGeneric = true;
                if (file.isDirectory())
                    bmp = BitmapFactory.decodeResource(mContext.getResources(),
                            useLarge ? R.drawable.lg_folder : R.drawable.sm_folder);
                else
                    bmp = BitmapFactory.decodeResource(mContext.getResources(),
                            useLarge ? R.drawable.lg_unknown : R.drawable.sm_unknown);
            }
        }

        if (bmp != null && (mWidth < bmp.getWidth() || mHeight < bmp.getHeight())) {
            if (file.isImageFile() && showCenteredCroppedPreviews)
                bmp = cropBitmap(bmp, mWidth, mHeight);
            else {
                int bw = bmp.getWidth();
                int bh = bmp.getHeight();

                if (bh > bw)
                    mWidth *= (float)bw / (float)bh;
                else
                    mHeight *= (float)bh / (float)bw;
                bmp = Bitmap.createScaledBitmap(bmp, mWidth, mHeight, true);
            }
        }

        if (bmp != null) {
            if (writeCache && !useGeneric)
                saveThumbnail(app.getContext(), mCacheFilename, bmp);
            app.getMemoryCache().put(mCacheFilename, bmp);
        } else {
            saveThumbnail(app.getContext(), mCacheFilename, null);
            fails.put(mParent, fails.containsKey(mParent) ? fails.get(mParent) + 1 : 1);
            rememberFailure(file.getPath());
        }
        // Logger.LogDebug("Created " + bmp.getWidth() + "x" + bmp.getHeight() +
        // " thumb (" + mWidth + "x" + mHeight + ")");
        return new SoftReference<Bitmap>(bmp);
    }

    private static void rememberFailure(String path) {
        fails.put(path, 1);
    }

    private static Bitmap cropBitmap(Bitmap src, int dw, int dh) {
        int sw = src.getWidth(), sh = src.getHeight();
        if (sw < dw && sh < dh)
            return src;
        // return Bitmap.createBitmap(src, (sw / 2) - (dh / 2), (sh / 2) - (dh /
        // 2), dw, dh, new Matrix(), false);
        Matrix m = new Matrix();
        float scale = Math.max(1, Math.min((float)dh / (float)sh, (float)dw / (float)sw));
        int ox = 0, oy = 0;
        if (sw - dw < sh - dh) // More Width needs to be cropped
        {
            ox = (sw / 2) - (dw / 2);
            if (ox >= 0)
                dw = Math.min(dw, Math.max(sw, sw - ox));
            else {
                ox = 0;
                oy = (sh / 2) - (dh / 2);
                dh = Math.min(dh, Math.max(sh, sw - oy));
            }
        } else {
            oy = (sh / 2) - (dh / 2);
            if (oy >= 0)
                dh = Math.min(dh, Math.max(sh, sw - oy));
            else {
                oy = 0;
                ox = (sw / 2) - (dw / 2);
                dw = Math.min(dw, Math.max(sw, sw - ox));
            }
        }
        // Logger.LogDebug("cropBitmap:(" + sw + "x" + sh + "):(" + ox + "," +
        // oy + ":" + dw + "x" + dh + ") @ " + scale);
        m.preScale(scale, scale);
        return Bitmap.createBitmap(src, Math.max(0, ox), Math.max(0, oy), Math.min(sw, dw),
                Math.min(sh, dh), m, scale != 1.0);
    }

    private static Bitmap loadThumbnail(Context mContext, String file) {
        if (mContext != null)
            return BitmapFactory.decodeFile(file);
        return null;
    }

    private static void saveThumbnail(Context mContext, String file, Bitmap bmp) {
        // Logger.LogVerbose("Saving thumb for " + file);
        FileOutputStream os = null;
        try {
            os = mContext.openFileOutput(file, 0);
            if (bmp != null)
                bmp.compress(CompressFormat.JPEG, 98, os);
        } catch (IOException e) {
            Logger.LogError("Unable to save thumbnail for " + file, e);
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                }
        }
    }

    private void sendThumbBack(OpenApp app, SoftReference<Bitmap> mThumb, String path) {
        final Bitmap d = mThumb.get();
        new BitmapDrawable(app.getResources(), d).setGravity(Gravity.CENTER);
        app.getMemoryCache().put(path, d);

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.obj = d;
                msg.sendToTarget();
            }
        });
    }

    public static void flushCache(OpenApp app, boolean deleteFiles) {
        // Logger.LogInfo("Flushing" + mCacheMap.size() + " from memory & " +
        // mContext.fileList().length + " from disk.");
        app.getMemoryCache().clear();
        if (!deleteFiles)
            return;
        for (String s : app.getContext().fileList())
            if (!s.toLowerCase().endsWith(".json"))
                app.getContext().deleteFile(s);
    }

}

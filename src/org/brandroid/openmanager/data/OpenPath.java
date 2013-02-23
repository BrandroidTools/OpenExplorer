
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.MimeTypes;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;

import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base class for all other File-based objects used in OpenExplorer.
 * 
 * @author Brandon
 */
public abstract class OpenPath implements Serializable, Parcelable, Comparable<OpenPath> {
    public static SortType Sorting = SortType.DATE_DESC;
    public static Boolean ShowHiddenFiles = false;

    private WeakReference<MediaObject> mObject;

    private static final long serialVersionUID = 332701810738149106L;
    private Object mTag = null;
    private OpenPathThreadUpdater mUpdater;
    protected static OpenPathDbAdapter mDb = null;
    public static Boolean AllowDBCache = true;
    
    public static java.text.DateFormat DateFormatInstance = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.ENGLISH);

    public abstract String getName();

    public abstract String getPath();

    public abstract String getAbsolutePath();

    public abstract void setPath(String path);

    public abstract long length();

    public abstract OpenPath getParent();

    public String getPrefix() {
        if (getParent() == null)
            return null;
        return getParent().getPath();
    }

    public abstract OpenPath getChild(String name);

    public OpenPath getChild(int index) {
        try {
            return ((OpenPath[])list())[index];
        } catch (IOException e) {
            return null;
        }
    }

    public OpenPath getChild(long index) {
        try {
            return ((OpenPath[])list())[(int)index];
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Used to list cached files in directories. Use listFiles to ensure cache
     * is current.
     * 
     * @return
     * @throws IOException
     */
    public abstract OpenPath[] list() throws IOException;

    /**
     * Provides a method to always get latest Files listing.
     * 
     * @throws IOException
     */
    public abstract OpenPath[] listFiles() throws IOException;

    /**
     * Only list child directories, if any.
     * 
     * @return Array of OpenPath representing child directories
     * @throws IOException
     */
    public OpenPath[] listDirectories() throws IOException {
        ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
        for (OpenPath path : list())
            if (path.isDirectory())
                ret.add(path);
        return ret.toArray(new OpenPath[ret.size()]);
    }

    /**
     * Get generic List of children (instead of Array)
     * 
     * @return ArrayList<OpenPath> of children
     * @throws IOException
     */
    public List<OpenPath> listFilesCollection() throws IOException {
        OpenPath[] files = listFiles();
        ArrayList<OpenPath> ret = new ArrayList<OpenPath>(files.length);
        for (OpenPath f : files)
            ret.add(f);
        return ret;
    }

    /**
     * Indicates parent of requested path. This is useful when vertigo is
     * encountered.
     * 
     * @param path Path of which the parent is requested.
     * @return indicating parent of path parameter
     */
    public static String getParent(String path) {
        if (path.equals("/"))
            return null;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        path = path.substring(0, path.lastIndexOf("/") + 1);
        if (path.endsWith("://") || path.endsWith(":/"))
            return null;
        return path;
    }

    /**
     * Get selected directory's child count, if applicable. If thread is
     * required, this will return 0.
     * 
     * @param countHidden Count hidden children
     * @return Integer representing number of children.
     * @throws IOException
     */
    public int getChildCount(boolean countHidden) throws IOException {
        if (requiresThread())
            return 0;
        if (countHidden)
            return list().length;
        else {
            int ret = 0;
            for (OpenPath kid : list())
                if (!kid.isHidden())
                    ret++;
            return ret;
        }
    }

    /**
     * Indicates if this file represents a <em>directory</em> on the underlying
     * file system.
     * 
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     */
    public abstract Boolean isDirectory();

    /**
     * Indicates if this file represents a <em>file</em> on the underlying file
     * system.
     * 
     * @return {@code true} if this file is a file, {@code false} otherwise.
     */
    public abstract Boolean isFile();

    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For Unix
     * systems a file is considered hidden if its name starts with a ".". For
     * Windows systems there is an explicit flag in the file system for this
     * purpose.
     * 
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     */
    public abstract Boolean isHidden();

    /**
     * Returns Uri representing path
     * 
     * @return Uri
     */
    public abstract Uri getUri();

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight. Returns 0 if the file
     * does not exist.
     * 
     * @return the time when this file was last modified.
     */
    public abstract Long lastModified();

    /**
     * Indicates whether the current context is allowed to read from this file.
     * 
     * @return {@code true} if this file can be read, {@code false} otherwise.
     */
    public abstract Boolean canRead();

    /**
     * Indicates whether the current context is allowed to write to this file.
     * 
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     */
    public abstract Boolean canWrite();

    /**
     * Tests whether or not this process is allowed to execute this file. Note
     * that this is a best-effort result; the only way to be certain is to
     * actually attempt the operation.
     * 
     * @return {@code true} if this file can be executed, {@code false}
     *         otherwise.
     * @since 1.6
     */
    public abstract Boolean canExecute();

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     * 
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    public abstract Boolean exists();

    /**
     * Special flag for OpenPath files that require a thread when listing
     * children or performing other operations. This is needed for networked as
     * well as root files.
     * 
     * @return {@code true} if a thread is required, {@code false} otherwise
     */
    public abstract Boolean requiresThread();

    /**
     * Deletes this file. Directories must be empty before they will be deleted.
     * <p>
     * Note that this method does <i>not</i> throw {@code IOException} on
     * failure. Callers must check the return value.
     * 
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     */
    public abstract Boolean delete();

    /**
     * Creates the directory named by the trailing filename of this file. Does
     * not create the complete path required to create this directory.
     * <p>
     * Note that this method does <i>not</i> throw {@code IOException} on
     * failure. Callers must check the return value.
     * 
     * @return {@code true} if the directory has been created, {@code false}
     *         otherwise.
     * @see #mkdirs
     */
    public abstract Boolean mkdir();

    /**
     * Create (if necessary) and return InputStream for reading from.
     * 
     * @return InputStream that can be read from.
     * @throws IOException
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Create (if necessary) and return OutputStream to write to.
     * 
     * @return OutputStream that can be written to.
     * @throws IOException
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     * 
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof OpenPath)
            return getAbsolutePath().equals(((OpenPath)o).getAbsolutePath());
        else
            return super.equals(o);
    }

    /**
     * Return special tag object used to store special information about object.
     * 
     * @return An object that has been stored with {@link setTag}
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Set special tag object that can be retrieved later.
     * 
     * @param o An object.
     */
    public void setTag(Object o) {
        mTag = o;
    }

    /**
     * Indicates length of list() results. In the case a thread is needed, this
     * should return an integer indicated the number of children stored in
     * cache.
     * 
     * @return Integer
     */
    public int getListLength() {
        return -1;
    }

    /**
     * Returns the relative sort ordering of the paths for this file and the
     * file {@code another}. The ordering is platform dependent.
     * 
     * @param another a file to compare this file to
     * @return an int determined by comparing the two paths. Possible values are
     *         described in the Comparable interface.
     * @see Comparable
     */
    public int compareTo(OpenPath other) {
        return compare(this, other);
    }

    /**
     * Get cached Thumbnail.
     * 
     * @param app OpenApp inhereted object that can provide Context to
     *            ThumbnailCreator
     * @param w Width of thumbnail
     * @param h Height of thumbnail
     * @return SoftReference<Bitmap> representing cached thumbnail
     * @see ThumbnailCreator
     */
    public SoftReference<Bitmap> getThumbnail(OpenApp app, int w, int h) {
        return ThumbnailCreator.generateThumb(app, this, w, h, app.getContext());
    }

    /**
     * Get cached Thumbnail.
     * 
     * @param app OpenApp inhereted object that can provide Context to
     *            ThumbnailCreator
     * @param w Width of thumbnail
     * @param h Height of thumbnail
     * @param read Read from cache?
     * @param write Write to cache?
     * @return SoftReference<Bitmap> representing cached thumbnail
     * @see ThumbnailCreator
     */
    public SoftReference<Bitmap> getThumbnail(OpenApp app, int w, int h, Boolean read, Boolean write) {
        return ThumbnailCreator.generateThumb(app, this, w, h, read, write, app.getContext());
    }

    /**
     * Compare two OpenPath files, with sorting taken into account
     * 
     * @param fa First OpenPath
     * @param fb Second OpenPath
     * @return an int determined by comparing the two paths. Possible values are
     *         described in the Comparable interface.
     * @see Comparable
     */
    public static int compare(OpenPath fa, OpenPath fb) {
        try {
            if (fa == null && fb != null)
                return 1;
            if (fb == null && fa != null)
                return 0;
            if (fb == null || fa == null)
                return 0;
            if (Sorting.foldersFirst()) {
                if (fb.isDirectory() && !fa.isDirectory())
                    return 1;
                if (fa.isDirectory() && !fb.isDirectory())
                    return -1;
            }
            String a = fa.getName();
            String b = fb.getName();
            Long sa = fa.length();
            Long sb = fb.length();
            Long ma = fa.lastModified();
            Long mb = fb.lastModified();
            if (a == null && b != null)
                return 1;
            if (a == null || b == null)
                return 0;
            switch (Sorting.getType()) {
                case ALPHA_DESC:
                    return b.toLowerCase().compareTo(a.toLowerCase());
                case ALPHA:
                    return a.toLowerCase().compareTo(b.toLowerCase());
                case SIZE_DESC:
                    if (sa == null && sb != null)
                        return 1;
                    if (sa == null || sb == null)
                        return 0;
                    return sa.compareTo(sb);
                case SIZE:
                    if (sb == null && sa != null)
                        return 1;
                    if (sa == null || sb == null)
                        return 0;
                    return sb.compareTo(sa);
                case DATE_DESC:
                    if (ma == null && mb != null)
                        return 1;
                    if (ma == null || mb == null)
                        return 0;
                    return ma.compareTo(mb);
                case DATE:
                    if (mb == null && ma != null)
                        return 1;
                    if (ma == null || mb == null)
                        return 0;
                    return mb.compareTo(ma);
                case TYPE:
                    String ea = a.substring(a.lastIndexOf(".") + 1, a.length()).toLowerCase();
                    String eb = b.substring(b.lastIndexOf(".") + 1, b.length()).toLowerCase();
                    return ea.compareTo(eb);
                case NONE:
                    return 0;
                default:
                    return a.toLowerCase().compareTo(b.toLowerCase());
            }
        } catch (Exception e) {
            Logger.LogError("Unable to sort.", e);
            return 0;
        }
    }

    /**
     * Get file extension (if any). This is any characters after the last period
     * in the filename. If there is no period, then a blank string will be
     * returned.
     * 
     * @return String representing file extension
     */
    public String getExtension() {
        String ret = getName();
        if (ret == null)
            return "";
        if (ret.indexOf(".") > -1)
            return ret.substring(ret.lastIndexOf(".") + 1);
        else
            return "";
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     * 
     * @return a bitmask indicating the set of special object types marshalled
     *         by the Parcelable.
     * @see Parcelable
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     * 
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written. May
     *            be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     * @see Parcelable
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getPath());
    }

    /**
     * Interface that has been implemented and provided as a public CREATOR
     * field that generates instances of OpenPath from a Parcel.
     */
    public static final Parcelable.Creator<OpenPath> CREATOR = new Parcelable.Creator<OpenPath>() {
        /**
         * Create a new instance of the Parcelable class, instantiating it from
         * the given Parcel whose data had previously been written by
         * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
         * 
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        public OpenPath createFromParcel(Parcel in) {
            String path = in.readString();
            try {
                return FileManager.getOpenCache(path, false, Sorting);
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * Create a new array of the Parcelable class.
         * 
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         *         initialized to null.
         */
        public OpenPath[] newArray(int size) {
            return new OpenPath[size];
        }
    };

    /**
     * Indicates directory depth
     * 
     * @return An int representing directory depth
     */
    public int getDepth() {
        if (getParent() == null || getParent().getPath().equals(getPath()))
            return 1;
        return 1 + getParent().getDepth();
    }

    /**
     * Indicates whether file is considered to be "text".
     * 
     * @return {@code true} if file is text, {@code false} if not.
     * @see isTextFile(String)
     */
    public boolean isTextFile() {
        return !isDirectory() && isTextFile(getName());
    }

    /**
     * Indicates whether requested file path is "text". This is done by
     * comparing file extension to a static list of extensions known to be text.
     * If the file has no file extension, it is also considered to be text.
     * 
     * @param file File path
     * @return {@code true} if file is text, {@code false} if not.
     */
    public static boolean isTextFile(String file) {
        if (file == null)
            return false;
        if (file.indexOf(".") == -1)
            return true;
        file = file.substring(file.lastIndexOf("/") + 1);
        String ext = file.substring(file.lastIndexOf(".") + 1);
        if (MimeTypes.Default != null)
            if (MimeTypes.Default.getMimeType(file).startsWith("text/"))
                return true;
        if (ext.equalsIgnoreCase("txt") || ext.equalsIgnoreCase("php")
                || ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")
                || ext.equalsIgnoreCase("xml"))
            return true;
        return false;
    }

    /**
     * Indicates whether this path is an image.
     * 
     * @return {@code true} if file is image, {@code false} if not.
     */
    public boolean isImageFile() {
        return !isDirectory() && isImageFile(getName());
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * comparing file extension to a static list of extensions known to be
     * images.
     * 
     * @param file File path
     * @return {@code true} if file is image, {@code false} if not.
     */
    public static boolean isImageFile(String file) {
        String ext = file.substring(file.lastIndexOf(".") + 1);
        if (MimeTypes.Default != null)
            if (MimeTypes.Default.getMimeType(file).startsWith("image/"))
                return true;
        if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg")
                || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif")
                || ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("tif"))
            return true;

        return false;
    }

    /**
     * Indicates whether current path is an Android App.
     * 
     * @return {@code true} if file is an Android App, {@code false} if not.
     */
    public boolean isAPKFile() {
        return !isDirectory() && isAPKFile(getName());
    }

    /**
     * Indicates whether requested file path is an Android App.
     * 
     * @param file File path
     * @return {@code true} if file is Android App, {@code false} if not.
     */
    public static boolean isAPKFile(String file) {
        file = file.substring(file.lastIndexOf("/") + 1);
        if (file.indexOf(".") > -1)
            file = file.substring(file.lastIndexOf(".") + 1);

        if (file.equalsIgnoreCase("apk"))
            return true;

        return false;
    }

    public boolean isArchive() {
        return getExtension().equalsIgnoreCase("zip");
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * checking Mimetype of file via {@link MimeTypes} class, and by comparing
     * file extension to a static list of extensions known to be videos.
     * 
     * @return {@code true} if file is a video, {@code false} if not.
     */
    public boolean isVideoFile() {
        return !isDirectory() && isVideoFile(getName());
    }

    /**
     * Indicates whether requested file path is an image. This is done by
     * checking Mimetype of file via {@link MimeTypes} class, and by comparing
     * file extension to a static list of extensions known to be videos.
     * 
     * @param file File path
     * @return {@code true} if file is video, {@code false} if not.
     */
    public static boolean isVideoFile(String path) {
        if (MimeTypes.Default != null)
            if (MimeTypes.Default.getMimeType(path).startsWith("video/"))
                return true;

        String ext = path.substring(path.lastIndexOf(".") + 1);
        if (ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("3gp")
                || ext.equalsIgnoreCase("avi") || ext.equalsIgnoreCase("webm")
                || ext.equalsIgnoreCase("m4v"))
            return true;
        return false;
    }

    /**
     * Indicates the potential existence of a dynamic thumbnail (videos, photos,
     * & apk files)
     * 
     * @return {@code true} if thumbnail exists, {@code false} if not.
     */
    public boolean hasThumbnail() {
        return isVideoFile() || isImageFile() || isAPKFile();
    }

    /**
     * If path requires a thread, this callback may be used when updates are
     * needed on the status of the threaded operation.
     * 
     * @param updater {@link OpenPathThreadUpdater} object that will be called
     *            when updates are ready to be made.
     */
    public void setThreadUpdateCallback(OpenPathThreadUpdater updater) {
        mUpdater = updater;
    }

    /**
     * Indicates set callback for thread-needing paths.
     * 
     * @return {@link OpenPathThreadUpdater} object if one has been set, null if
     *         one has not been set.
     */
    public OpenPathThreadUpdater getThreadUpdateCallback() {
        return mUpdater;
    }

    /**
     * Interface used to update the UI thread when a threaded path update is
     * ready. This should be used when only status is needed (such as file
     * compression).
     */
    public interface OpenPathThreadUpdater {
        /**
         * Callback to update UI thread of progress.
         * 
         * @param progress Progress value
         * @param total Total maximum progress
         */
        public void update(int progress, int total);

        /**
         * Callback to update UI thread of a status.
         * 
         * @param status String representing status of operation.
         */
        public void update(String status);
    }

    /**
     * Interface used to update the UI thread when child objects are found. This
     * should be used for things like listing children and searching.
     */
    public interface OpenContentUpdater {
        /**
         * Callback used to add OpenPath to List on UI thread.
         * 
         * @param file OpenPath of found file.
         */
        public void addContentPath(OpenPath file);

        /**
         * Callback used to designate when updates have completed
         */
        public void doneUpdating();
    }

    /**
     * Listener callback used to handle Updater callback
     */
    public interface OpenPathUpdateListener {
        /**
         * List files from a threaded path.
         * 
         * @param callback OpenContentUpdater callback to use when files are
         *            found.
         * @throws IOException
         */
        public void list(OpenContentUpdater callback) throws IOException;
    }

    /**
     * Interface for OpenPath objects that need a special function to copy files
     * from another location. This is currently only used for root-required
     * files.
     */
    public interface OpenPathCopyable {
        /**
         * Special function to copy files from another location
         * 
         * @param file OpenPath object to copy data from.
         * @return {@code true} if operation was successful, {@code false} if
         *         not.
         */
        public boolean copyFrom(OpenPath file);
        
        public boolean copyTo(OpenPath dest) throws IOException;
    }

    /**
     * Interface to use when getOutputStream() is not preferable
     */
    public interface OpenPathByteIO {
        /**
         * Read byes from Path.
         * 
         * @return Byte array representing data in file.
         */
        public byte[] readBytes();

        /**
         * Write data to file.
         * 
         * @param bytes Byte array to write to file.
         */
        public void writeBytes(byte[] bytes);
    }

    /**
     * Interface for OpenPath objects that need a "temporary" file. This should
     * be used for any threaded file type, at least under a certain file size.
     */
    public interface NeedsTempFile {
        /**
         * Copy data to temporary file.
         * 
         * @param task task that can be used to update UI thread.
         */
        public OpenFile tempDownload(AsyncTask<?, ?, ?> task) throws IOException;

        /**
         * Copy data from temporary file to actual file represented by this
         * OpenPath object.
         */
        public void tempUpload(AsyncTask<?, ?, ?> task) throws IOException;
        
        public String getTempFileName();
        public OpenFile getTempFile();
    }

    /**
     * Indicates mimetype of file.
     * 
     * @see MimeTypes
     */
    public String getMimeType() {
        if (MimeTypes.Default != null)
            return MimeTypes.Default.getMimeType(getPath());
        return "*/*";
    }

    /**
     * Indicates all OpenPath hierarchy (parent, grandparent, etc.).
     * 
     * @param andSelf Use selected path as first object.
     * @return List<OpenPath> collection of OpenPath objects.
     */
    public List<OpenPath> getAncestors(boolean andSelf) {
        ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
        OpenPath tmp = this;
        if (!andSelf)
            tmp = tmp.getParent();
        while (tmp != null) {
            ret.add(tmp);
            tmp = tmp.getParent();
        }
        return ret;
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * file.
     * 
     * @return a printable representation of this file.
     */
    @Override
    public String toString() {
        return getPath();
    }

    /**
     * Indicates to Adapter whether or not to show Path (i.e. can contain
     * multiple parent paths)
     * 
     * @return {@code true} if child path should be shown, {@code false} if not.
     */
    public boolean showChildPath() {
        return false;
    }

    /**
     * Get static reference of Database adapter used for cache.
     * 
     * @return OpenPathDbAdapter
     */
    public static OpenPathDbAdapter getDb() {
        if (AllowDBCache)
            return mDb;
        return null;
    }
    
    /**
     * Explicitly close the cache Database adapter.
     */
    public static void closeDb() {
        try {
            if(mDb != null)
                mDb.close();
        } catch(Exception e) { }
    }

    /**
     * Set static Database adapter used for cache.
     * 
     * @param openPathDbAdapter
     */
    public final static void setDb(OpenPathDbAdapter openPathDbAdapter) {
        mDb = openPathDbAdapter;
    }

    /**
     * Indicates whether file should be added to database cache.
     * 
     * @return {@code true} if file should be cached, {@code false] if not.

     */
    public boolean addToDb() {
        return addToDb(false);
    }

    /**
     * Indicates whether file cache should be updated.
     * 
     * @param delete {@code true} if update should be forced, {@code false} to
     *            cancel update if cache exists.
     * @return {@code true} if file should be cached, {@code false} if not.
     */
    public boolean addToDb(boolean delete) {
        if (mDb == null)
            return false;
        if (!AllowDBCache)
            return false;
        return mDb.createItem(this, delete) > 0;
    }

    /**
     * List files found in cached in database.
     * 
     * @param sort Sorting parameter.
     * @return True if entries were found, false if no cache available.
     */
    public boolean listFromDb(SortType sort) {
        return false;
    }

    public int deleteFolderFromDb() {
        if (!AllowDBCache)
            return 0;
        if (mDb != null)
            return mDb.deleteFolder(this);
        else
            return -1;
    }

    public final static void flushDbCache() {
        if (mDb != null)
            mDb.clear();
    }

    public int getAttributes() {
        return 0;
    }

    public void setObject(MediaObject object) {
        synchronized (Path.class) {
            Utils.assertTrue(mObject == null || mObject.get() == null);
            mObject = new WeakReference<MediaObject>(object);
        }
    }

    public MediaObject getObject() {
        synchronized (Path.class) {
            return (mObject == null) ? null : mObject.get();
        }
    }

    public static OpenPath fromString(String path) {
        return FileManager.getOpenCache(path);
    }

    public String[] split() {
        synchronized (Path.class) {
            return split(getPath());
        }
    }

    public static String[] split(String s) {
        int n = s.length();
        if (n == 0)
            return new String[0];
        if (s.charAt(0) != '/') {
            throw new RuntimeException("malformed path:" + s);
        }
        ArrayList<String> segments = new ArrayList<String>();
        int i = 1;
        while (i < n) {
            int brace = 0;
            int j;
            for (j = i; j < n; j++) {
                char c = s.charAt(j);
                if (c == '{')
                    ++brace;
                else if (c == '}')
                    --brace;
                else if (brace == 0 && c == '/')
                    break;
            }
            if (brace != 0) {
                throw new RuntimeException("unbalanced brace in path:" + s);
            }
            segments.add(s.substring(i, j));
            i = j + 1;
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    public String getSuffix() {
        return getName();
    }

    /**
     * Get string to show in "List View"
     * 
     * @param countHiddenChildren Count Hidden Files?
     * @param showLongDate Show Long Date?
     * @return String to show
     */
    public String getDetails(boolean countHiddenChildren) {
        String deets = "";

        try {
            if (isDirectory())
                deets += getChildCount(countHiddenChildren) + " %s";
            else if (isFile())
                deets += DialogHandler.formatSize(length());
        } catch (Exception e) {
        }

        return deets;
    }

    public CharSequence getFormattedDate(boolean showLongDate) {
        Long last = lastModified();
        if (last != null) {
            try {
                return new SimpleDateFormat(showLongDate ? "MM-dd-yyyy HH:mm" : "MM-dd-yy")
                        .format(last);
            } catch (Exception e) {
            }
        }
        return "";
    }

    /**
     * Create file if it does not exist. If it does, update last modified date
     * to current.
     * 
     * @return {@code true} if successfull, {@code false} otherwise.
     */
    public boolean touch() {
        return false;
    }

    /**
     * Clear list of memory-cached children.
     */
    public void clearChildren() {

    }

    /**
     * Complementary to requiresThread(), isLoaded() returns true when the
     * thread completes. For non-threaded paths, this always returns true.
     * 
     * @return True if data can be read.
     */
    public boolean isLoaded() {
        return !requiresThread();
    }

    /**
     * Can OpenExplorer handle this file type?
     * 
     * @return True if OpenExplorer can handle file type (and it is enabled in
     *         preferences)
     */
    public boolean canHandleInternally() {
        return false;
    }

}

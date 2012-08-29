package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.brandroid.openmanager.R;
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
import com.android.gallery3d.util.IdentityCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class OpenPath
	implements Serializable, Parcelable, Comparable<OpenPath>
{
	public static SortType Sorting = SortType.DATE_DESC;
	
    private WeakReference<MediaObject> mObject;
	
	private static final long serialVersionUID = 332701810738149106L;
	private Object mTag = null;
	private OpenPathThreadUpdater mUpdater;
	protected static OpenPathDbAdapter mDb = null;
	public static Boolean AllowDBCache = true;
	public abstract String getName();
	public abstract String getPath();
	public abstract String getAbsolutePath();
	public abstract void setPath(String path);
	public abstract long length();
	public abstract OpenPath getParent();
	public String getPrefix() {
		if(getParent() == null) return null;
		return getParent().getPath();
	}
	public abstract OpenPath getChild(String name);
	public OpenPath getChild(int index) {
		try {
		return ((OpenPath[])list())[index];
		} catch(IOException e) { return null; }
		}
	public OpenPath getChild(long index) {
		try {
		return ((OpenPath[])list())[(int) index];
		} catch(IOException e) { return null; }
		}
	
	/**
	 * Used to list cached files in directories. Use listFiles to ensure cache is current.
	 * @return
	 * @throws IOException
	 */
	public abstract OpenPath[] list() throws IOException;
	
	/**
	 * Provides a method to always get latest Files listing.
	 * @throws IOException
	 */
	public abstract OpenPath[] listFiles() throws IOException;
	
	public OpenPath[] listDirectories() throws IOException
	{
		ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
		for(OpenPath path : list())
			if(path.isDirectory())
				ret.add(path);
		return ret.toArray(new OpenPath[ret.size()]);
	}
	
	public List<OpenPath> listFilesCollection() throws IOException
	{
		OpenPath[] files = listFiles();
		ArrayList<OpenPath> ret = new ArrayList<OpenPath>(files.length);
		for(OpenPath f : files)
			ret.add(f);
		return ret;
	}
	
	public static String getParent(String path)
	{
		if(path.equals("/")) return null;
		if(path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		path = path.substring(0, path.lastIndexOf("/") + 1);
		if(path.endsWith("://") || path.endsWith(":/"))
			return null;
		return path;
	}
	public int getChildCount(boolean countHidden) throws IOException {
		if(requiresThread()) return 0;
		if(countHidden)
			return list().length;
		else {
			int ret = 0;
			for(OpenPath kid : list())
				if(!kid.isHidden())
					ret++;
			return ret;
		}
	}
	public abstract Boolean isDirectory();
	public abstract Boolean isFile();
	public abstract Boolean isHidden();
	public abstract Uri getUri();
	public abstract Long lastModified();
	public abstract Boolean canRead();
	public abstract Boolean canWrite();
	public abstract Boolean canExecute();
	public abstract Boolean exists();
	public abstract Boolean requiresThread();
	public abstract Boolean delete();
	public abstract Boolean mkdir();
	public abstract InputStream getInputStream() throws IOException;
	public abstract OutputStream getOutputStream() throws IOException;
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o instanceof OpenPath)
			return getAbsolutePath().equals(((OpenPath)o).getAbsolutePath());
		else
			return super.equals(o);
	}
	public Object getTag() { return mTag; }
	public void setTag(Object o) { mTag = o; }
	public int getListLength() { return -1; }
	public int compareTo(OpenPath other)
	{
		return compare(this, other);
	}
	public SoftReference<Bitmap> getThumbnail(OpenApp app, int w, int h) { return ThumbnailCreator.generateThumb(app, this, w, h, app.getContext()); }
	public SoftReference<Bitmap> getThumbnail(OpenApp app, int w, int h, Boolean read, Boolean write) { return ThumbnailCreator.generateThumb(app, this, w, h, read, write, app.getContext()); }
	
	public static int compare(OpenPath fa, OpenPath fb)
	{
		try {
			if(fa == null && fb != null)
				return 1;
			if(fb == null && fa != null)
				return 0;
			if(fb == null || fa == null)
				return 0;
			if(Sorting.foldersFirst())
			{
				if(fb.isDirectory() && !fa.isDirectory())
					return 1;
				if(fa.isDirectory() && !fb.isDirectory())
					return -1;
			}
			String a = fa.getName();
			String b = fb.getName();
			Long sa = fa.length();
			Long sb = fb.length();
			Long ma = fa.lastModified();
			Long mb = fb.lastModified();
			if(a == null && b != null)
				return 1;
			if(a == null || b == null)
				return 0;
			switch(Sorting.getType())
			{
				case ALPHA_DESC:
					return b.toLowerCase().compareTo(a.toLowerCase());
				case ALPHA:
					return a.toLowerCase().compareTo(b.toLowerCase());
				case SIZE_DESC:
					if(sa == null && sb != null) return 1;
					if(sa == null || sb == null) return 0;
					return sa.compareTo(sb);
				case SIZE:
					if(sb == null && sa != null) return 1;
					if(sa == null || sb == null) return 0;
					return sb.compareTo(sa);
				case DATE_DESC:
					if(ma == null && mb != null) return 1;
					if(ma == null || mb == null) return 0;
					return ma.compareTo(mb);
				case DATE:
					if(mb == null && ma != null) return 1;
					if(ma == null || mb == null) return 0;
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
		} catch(Exception e)
		{
			Logger.LogError("Unable to sort.", e);
			return 0;
		}
	}

	public String getExtension() {
		return getPath().substring(getPath().lastIndexOf(".") + 1);
	}
	
	public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getPath());
    }

    public static final Parcelable.Creator<OpenPath> CREATOR
            = new Parcelable.Creator<OpenPath>() {
        public OpenPath createFromParcel(Parcel in) {
        	String path = in.readString();
        	try {
				return FileManager.getOpenCache(path, false, Sorting);
			} catch (IOException e) {
				return null;
			}
        }

        public OpenPath[] newArray(int size) {
            return new OpenPath[size];
        }
    };
    
	public int getDepth() {
		if(getParent() == null || getParent().getPath().equals(getPath())) return 1;
		return 1 + getParent().getDepth();
	}

	public OpenPath[] getHierarchy()
	{
		OpenPath[] ret = new OpenPath[getDepth()];
		OpenPath tmp = this;
		for(int i = ret.length - 1; i >= 0; i--)
		{
			ret[i] = tmp;
			tmp = tmp.getParent();
		}
		return ret;
	}

	public boolean isTextFile() { return !isDirectory() && isTextFile(getName()); }
	public static boolean isTextFile(String file) {
		if(file == null) return false;
		if(file.indexOf(".") == -1) return true;
		String ext = file.substring(file.lastIndexOf(".") + 1);
		if(MimeTypes.Default != null)
			if(MimeTypes.Default.getMimeType(file).startsWith("text/"))
				return true;
		if(ext.equalsIgnoreCase("txt")
				|| ext.equalsIgnoreCase("php")
				|| ext.equalsIgnoreCase("html")
				|| ext.equalsIgnoreCase("htm")
				|| ext.equalsIgnoreCase("xml"))
			return true;
		return false;
	}
	
	public boolean isImageFile() { return !isDirectory() && isImageFile(getName()); }
	public static boolean isImageFile(String file) {
		String ext = file.substring(file.lastIndexOf(".") + 1);
		if(MimeTypes.Default != null)
			if(MimeTypes.Default.getMimeType(file).startsWith("image/"))
				return true;
		if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") ||
			ext.equalsIgnoreCase("jpeg")|| ext.equalsIgnoreCase("gif") ||
			ext.equalsIgnoreCase("tiff")|| ext.equalsIgnoreCase("tif"))
			return true;
		
		return false;
	}
	
	public boolean isAPKFile() { return !isDirectory() && isAPKFile(getName()); }
	public static boolean isAPKFile(String file) {
		String ext = file.substring(file.lastIndexOf(".") + 1);
		
		if (ext.equalsIgnoreCase("apk"))
			return true;
		
		return false;
	}
	public boolean isArchive() {
		return getExtension().equalsIgnoreCase("zip") || getExtension().equalsIgnoreCase("jar");
	}
	
	public boolean isVideoFile() { return !isDirectory() && isVideoFile(getName()); }
	public static boolean isVideoFile(String path)
	{
		if(MimeTypes.Default != null)
			if(MimeTypes.Default.getMimeType(path).startsWith("video/"))
				return true;
		
		String ext = path.substring(path.lastIndexOf(".") + 1);
		if(ext.equalsIgnoreCase("mp4") || 
			  ext.equalsIgnoreCase("3gp") || 
			  ext.equalsIgnoreCase("avi") ||
			  ext.equalsIgnoreCase("webm") || 
			  ext.equalsIgnoreCase("m4v"))
			return true;
		return false;
	}
	
	/**
	 * Indicates the potential existence of a dynamic thumbnail (videos, photos, & apk files)
	 * @return boolean indicating whether a dynamic thumbnail exists
	 */
	public boolean hasThumbnail() {
		return isVideoFile() || isImageFile() || isAPKFile();
	}
	
	public void setThreadUpdateCallback(OpenPathThreadUpdater updater)
	{
		mUpdater = updater;
	}
	public OpenPathThreadUpdater getThreadUpdateCallback() { return mUpdater; }
	
	public interface OpenPathThreadUpdater
	{
		public void update(int progress, int total);
		public void update(String status);
	}
	
	public interface OpenContentUpdater
	{
		public void addContentPath(OpenPath file);
		public void doneUpdating();
	}
	public interface OpenPathUpdateListener
	{
		public void list(OpenContentUpdater callback) throws IOException;
	}
	
	public interface OpenPathCopyable
	{
		public boolean copyFrom(OpenPath file);
	}
	
	/**
	 * OpenPathByteIO preferable over getOutputStream()
	 */ 
	public interface OpenPathByteIO
	{
		public byte[] readBytes();
		public void writeBytes(byte[] bytes);
	}
	
	public interface NeedsTempFile
	{
		public OpenFile tempDownload(AsyncTask<?, ?, ?> task) throws IOException;
		public void tempUpload(AsyncTask<?, ?, ?> task) throws IOException;
	}

	public String getMimeType() {
		if(MimeTypes.Default != null)
			return MimeTypes.Default.getMimeType(getPath());
		return "*/*";
	}
	
	public OpenPath[] getSiblings() throws IOException
	{
		OpenPath parent = getParent();
		if(parent == null) return new OpenPath[]{this};
		return parent.list();
	}
	
	public List<OpenPath> getAncestors(boolean andSelf)
	{
		ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
		OpenPath tmp = this;
		if(!andSelf)
			tmp = tmp.getParent();
		while(tmp != null)
		{
			ret.add(tmp);
			tmp = tmp.getParent();
		}
		return ret;
	}
	
	
	@Override
	public String toString() {
		return getPath();
	}
	
	/**
	 * Indicates to Adapter whether or not to show Path (i.e. can contain multiple parent paths)
	 * @return Boolean
	 */
	public boolean showChildPath()
	{
		return false;
	}
	
	public static OpenPathDbAdapter getDb() { if(AllowDBCache) return mDb; return null; }
	public final static void setDb(OpenPathDbAdapter openPathDbAdapter) {
		mDb = openPathDbAdapter;
	}
	public boolean addToDb() { return addToDb(false); }
	public boolean addToDb(boolean delete)
	{
		if(mDb == null) return false;
		if(!AllowDBCache) return false;
		return mDb.createItem(this, delete) > 0;
	}
	
	/**
	 * List files found in cached in database.
	 * @param sort Sorting parameter.
	 * @return True if entries were found, false if no cache available.
	 */
	public boolean listFromDb(SortType sort) { return false; }
	public int deleteFolderFromDb()
	{
		if(!AllowDBCache) return 0;
		if(mDb != null)
			return mDb.deleteFolder(this);
		else return -1;
	}
	
	public final static void flushDbCache()
	{
		if(mDb != null)
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
        if (n == 0) return new String[0];
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
                if (c == '{') ++brace;
                else if (c == '}') --brace;
                else if (brace == 0 && c == '/') break;
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

    // Splits a string to an array of strings.
    // For example, "{foo,bar,baz}" -> {"foo","bar","baz"}.
    public static String[] splitSequence(String s) {
        int n = s.length();
        if (s.charAt(0) != '{' || s.charAt(n-1) != '}') {
            throw new RuntimeException("bad sequence: " + s);
        }
        ArrayList<String> segments = new ArrayList<String>();
        int i = 1;
        while (i < n - 1) {
            int brace = 0;
            int j;
            for (j = i; j < n - 1; j++) {
                char c = s.charAt(j);
                if (c == '{') ++brace;
                else if (c == '}') --brace;
                else if (brace == 0 && c == ',') break;
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
	 * @param countHiddenChildren Count Hidden Files?
	 * @param showLongDate Show Long Date?
	 * @return String to show
	 */
	public String getDetails(boolean countHiddenChildren, boolean showLongDate)
	{
		String deets = "";
		
		try {
			if(isDirectory())
				deets += getChildCount(countHiddenChildren) + " %s | ";
			else if(isFile())
				deets += DialogHandler.formatSize(length()) + " | ";
		} catch (Exception e) { }
		
		Long last = lastModified();
		if(last != null)
		{
			try {
				deets += new SimpleDateFormat(showLongDate ? "MM-dd-yyyy HH:mm" : "MM-dd-yy")
							.format(last);
			} catch(Exception e) { }
		}
		
		return deets;
	}
	public boolean touch() {
		return false;
	}
	public void clearChildren() {
		
	}
	
	/**
	 * Complementary to requiresThread(), isLoaded() returns true when the thread completes. For non-threaded paths, this always returns true.
	 * @return True if data can be read.
	 */
	public boolean isLoaded() {
		return !requiresThread();
	}
	
	/**
	 * Can OpenExplorer handle this file type?
	 * @return True if OpenExplorer can handle file type (and it is enabled in preferences)
	 */
	public boolean canHandleInternally() {
		return false;
	}
    
}

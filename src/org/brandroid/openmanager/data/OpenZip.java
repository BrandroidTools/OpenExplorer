package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import android.net.Uri;

public class OpenZip extends OpenPath
{
	private final OpenFile mFile;
	private ZipFile mZip = null;
	private OpenPath[] mChildren = null;
	private ArrayList<OpenZipEntry> mEntries = null;
	private final Hashtable<String, List<OpenPath>> mFamily = new Hashtable<String, List<OpenPath>>();
	private final Hashtable<String, OpenZipVirtualPath> mVirtualPaths = new Hashtable<String, OpenZip.OpenZipVirtualPath>();
	private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
	
	public OpenZip(OpenFile zipFile)
	{
		mFile = zipFile;
		try {
			mZip = new ZipFile(mFile.getPath());
			//Logger.LogInfo("Zip file " + zipFile + " has " + length() + " entries");
		} catch (IOException e) {
			Logger.LogError("Couldn't open zip file (" + zipFile + ")");
		}
	}
	
	@Override
	public boolean canHandleInternally() {
		return Preferences.Pref_Zip_Internal;
	}
	
	public ZipFile getZip() { return mZip; } 

	@Override
	public String getName() {
		String ret = mFile.getName();
		if(ret.endsWith("/"))
			ret = ret.substring(0, ret.length() - 1);
		ret = ret.substring(ret.lastIndexOf("/") + 1);
		return ret;
	}

	@Override
	public String getPath() {
		return mFile.getPath();
	}

	@Override
	public String getAbsolutePath() {
		return mFile.getAbsolutePath();
	}

	@Override
	public void setPath(String path) {
		//mZip = new OpenFile(path);
	}

	@Override
	public long length() {
		return mFile.length();
	}

	@Override
	public OpenPath getParent() {
		return mFile.getParent();
	}

	@Override
	public OpenPath getChild(String name) {
		return new OpenZipEntry(this, this, mZip.getEntry(name));
	}
	
	@Override
	public int getChildCount(boolean countHidden) throws IOException {
		return 1; // This is only used when determining if folder is empty, which we assume is not.
	}
	
	@Override
	public int getListLength() {
		try {
			return mChildren != null ? mChildren.length : list().length;
		} catch (IOException e) { }
		return -1;
	}
	
	public List<OpenZipEntry> getAllEntries() throws IOException
	{
		if(mEntries != null)
			return mEntries;
		mEntries = new ArrayList<OpenZipEntry>();
		Enumeration<? extends ZipEntry> entries = mZip.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry ze = entries.nextElement();
			if(ze.isDirectory()) continue;
			String name = ze.getName();
			if(name.indexOf("/") > 0 && name.indexOf("/") < name.length() - 1)
				name = name.substring(0, name.lastIndexOf("/") + 1);
			else
				name = "";
			OpenPath vp = findVirtualPath(name);
			OpenZipEntry entry = new OpenZipEntry(this, vp, ze);
			mEntries.add(entry);
			addFamilyEntry(name, entry);
		}
		Set<String> keys = mFamily.keySet();
		for(String path : keys.toArray(new String[keys.size()]))
		{
			if(path.equals("")) continue;
			addFamilyPath(path);
		}
		return mEntries;
	}
	
	private OpenPath findVirtualPath(String name)
	{
		if(mVirtualPaths.containsKey(name)) return mVirtualPaths.get(name);
		OpenZipVirtualPath path = null;
		if(name.equals(""))
			return OpenZip.this;
		else
		{
			String par = name;
			if(par.endsWith("/")) par = par.substring(0, par.length() - 1);
			if(par.indexOf("/") > -1)
				par = par.substring(0, par.lastIndexOf("/") + 1);
			else par = "";
			path = new OpenZipVirtualPath(findVirtualPath(par), name);
		}
		mVirtualPaths.put(name, path);
		return path;
	}
	
	private void addFamilyPath(String path)
	{
		String parent = path;
		if(parent.endsWith("/"))
			parent = parent.substring(0, parent.length() - 1);
		parent = parent.substring(0, parent.lastIndexOf("/") + 1);
		if(!parent.equals("") && !parent.endsWith("/"))
			parent += "/";
		Logger.LogDebug("FamilyPath adding [" + path + "] to [" + parent + "]");
		List<OpenPath> kids = mFamily.get(parent);
		if(kids == null)
			kids = new ArrayList<OpenPath>();
		OpenPath vp = findVirtualPath(path);
		if(!kids.contains(vp))
			kids.add(vp);
		mFamily.put(parent, kids);
		if(!parent.equals(""))
			addFamilyPath(parent);
	}
	
	private void addFamilyEntry(String path, OpenZipEntry entry)
	{
		List<OpenPath> list = mFamily.get(path);
		if(list == null)
			list = new ArrayList<OpenPath>();
		if(DEBUG)
			Logger.LogDebug("Adding [" + entry.getName() + "] into [" + path + "]");
		list.add(entry);
		mFamily.put(path, list);
	}

	@Override
	public OpenPath[] list() throws IOException {
		if(mChildren == null)
			mChildren = listFiles();
		return mChildren;
	}

	@Override
	public OpenPath[] listFiles() throws IOException {
		Logger.LogVerbose("Listing OpenZip " + mFile);
		if(mZip == null)
			return mChildren;
		
		getAllEntries();
		
		mChildren = listFiles("");
		
		return mChildren;
	}
	
	public OpenPath[] listFiles(String rootRelative) throws IOException
	{
		Logger.LogDebug("OpenZip.listFiles(" + rootRelative + ")");
		if(!mFamily.containsKey(rootRelative)) {
			Logger.LogWarning("No children found for [" + rootRelative + "]");
			return new OpenPath[0];
		}
		List<OpenPath> ret = mFamily.get(rootRelative);
		Logger.LogVerbose(ret.size() + " children found for [" + rootRelative + "]");
		return ret.toArray(new OpenPath[ret.size()]);
	}
	
	@Override
	public Boolean isDirectory() {
		return false; // this used to be true, but was causing too many issues
	}

	@Override
	public Boolean isFile() {
		return false;
	}

	@Override
	public Boolean isHidden() {
		return mFile.isHidden();
	}

	@Override
	public Uri getUri() {
		return mFile.getUri();
	}

	@Override
	public Long lastModified() {
		return mFile.lastModified();
	}

	@Override
	public Boolean canRead() {
		return mFile.canRead();
	}

	@Override
	public Boolean canWrite() {
		return mFile.canWrite();
	}

	@Override
	public Boolean canExecute() {
		return false;
	}

	@Override
	public Boolean exists() {
		return mFile.exists();
	}

	@Override
	public Boolean requiresThread() {
		return false;
	}

	@Override
	public Boolean delete() {
		return mFile.delete();
	}

	@Override
	public Boolean mkdir() {
		return mFile.mkdir();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ZipInputStream(mFile.getInputStream());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new ZipOutputStream(mFile.getOutputStream());
	}
	
	public class OpenZipVirtualPath extends OpenPath
	{
		private final String path;
		private final OpenPath mParent;
		
		public OpenZipVirtualPath(OpenPath parent, String path)
		{
			mParent = parent;
			this.path = path;
		}

		@Override
		public String getName() {
			String name = path;
			if(name.endsWith("/"))
				name = name.substring(0, name.length() - 1);
			name = name.substring(name.lastIndexOf("/") + 1);
			return name;
		}

		@Override
		public String getPath() {
			return OpenZip.this.getPath() + "/" + path;
		}

		@Override
		public String getAbsolutePath() {
			return getPath();
		}

		@Override
		public void setPath(String path) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public long length() {
			try {
				return list().length;
			} catch(IOException e) { }
			return 0;
		}

		@Override
		public OpenPath getParent() {
			return mParent;
		}

		@Override
		public OpenPath getChild(String name) {
			try {
				for(OpenPath kid : list())
					if(kid.getName().equals(name))
						return kid;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public OpenPath[] list() throws IOException {
			return OpenZip.this.listFiles(path);
		}

		@Override
		public OpenPath[] listFiles() throws IOException {
			return list();
		}

		@Override
		public Boolean isDirectory() {
			return true;
		}

		@Override
		public Boolean isFile() {
			return false;
		}

		@Override
		public Boolean isHidden() {
			return getName().startsWith(".");
		}

		@Override
		public Uri getUri() {
			return Uri.parse(getAbsolutePath());
		}

		@Override
		public Long lastModified() {
			return OpenZip.this.lastModified();
		}

		@Override
		public Boolean canRead() {
			return true;
		}

		@Override
		public Boolean canWrite() {
			return false;
		}

		@Override
		public Boolean canExecute() {
			return false;
		}

		@Override
		public Boolean exists() {
			return true;
		}

		@Override
		public Boolean requiresThread() {
			return false;
		}

		@Override
		public Boolean delete() {
			return false;
		}

		@Override
		public Boolean mkdir() {
			return false;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}

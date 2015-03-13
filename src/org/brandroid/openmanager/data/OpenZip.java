
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
import org.brandroid.openmanager.data.OpenPath.*;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import android.net.Uri;

public class OpenZip extends OpenPath implements OpenStream, OpenPath.ListHandler {
    private final OpenFile mFile;
    private ZipFile mZip = null;
    private OpenPath[] mChildren = null;
    private ArrayList<OpenZipEntry> mEntries = null;
    private final Hashtable<String, List<OpenPath>> mFamily = new Hashtable<String, List<OpenPath>>();
    private final Hashtable<String, OpenZipVirtualPath> mVirtualPaths = new Hashtable<String, OpenZip.OpenZipVirtualPath>();
    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;

    public OpenZip(OpenFile zipFile) {
        mFile = zipFile;
        try {
            mZip = new ZipFile(mFile.getPath());
            // Logger.LogInfo("Zip file " + zipFile + " has " + length() +
            // " entries");
        } catch (IOException e) {
            Logger.LogError("Couldn't open zip file (" + zipFile + ")");
        }
    }
    
    @Override
    public Thread list(final ListListener listener) {
    	return thread(new Runnable() {
			public void run() {
				try {
					OpenPath[] kids = listFiles();
					postListReceived(kids, listener);
		        	//FileManager.setOpenCache(getPath(), OpenZip.this);
				} catch (Exception e) {
					postException(e, listener);
				}
			}
		});
    }
    
    @Override
    public void clearChildren() {
    	mEntries = new ArrayList<OpenZip.OpenZipEntry>();
    }

    @Override
    public boolean canHandleInternally() {
        return Preferences.Pref_Zip_Internal;
    }

    public ZipFile getZip() {
        return mZip;
    }

    @Override
    public String getName() {
        String ret = mFile.getName();
        if (ret.endsWith("/"))
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
    public long length() {
        return mFile.length();
    }

    @Override
    public OpenPath getParent() {
        return mFile.getParent();
    }

    @Override
    public OpenPath getChild(String name) {
        return new OpenZipEntry(this, mZip.getEntry(name));
    }

    @Override
    public int getChildCount(boolean countHidden) throws IOException {
        return 1; // This is only used when determining if folder is empty,
                  // which we assume is not.
    }

    @Override
    public int getListLength() {
        try {
            return mChildren != null ? mChildren.length : list().length;
        } catch (IOException e) {
        }
        return -1;
    }

    public static boolean isValidZip(OpenFile file)
    {
        OpenZip zip = new OpenZip(file);
        InputStream s = null;
        try {
            s = zip.getInputStream();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (s != null)
                    s.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public List<OpenZipEntry> getAllEntries() throws IOException {
        if (mEntries != null)
            return mEntries;
        mEntries = new ArrayList<OpenZipEntry>();
        Enumeration<? extends ZipEntry> entries = mZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            if (ze.isDirectory())
                continue;
            String parent = ze.getName();
            if (parent.indexOf("/") > 0 && parent.indexOf("/") < parent.length() - 1)
                parent = parent.substring(0, parent.lastIndexOf("/") + 1);
            else
                parent = "";
            OpenPath vp = findVirtualPath(parent);
            OpenZipEntry entry = new OpenZipEntry(vp, ze);
            mEntries.add(entry);
            addFamilyEntry(parent, entry);
        }
        Set<String> keys = mFamily.keySet();
        for (String path : keys.toArray(new String[keys.size()])) {
            if (path.equals(""))
                continue;
            addFamilyPath(path);
        }
        return mEntries;
    }

    public OpenPath findVirtualPath(String name) {
        if (mVirtualPaths.containsKey(name))
            return mVirtualPaths.get(name);
        OpenZipVirtualPath path = null;
        if (name.equals(""))
            return OpenZip.this;
        else {
            String par = name;
            if (par.endsWith("/"))
                par = par.substring(0, par.length() - 1);
            if (par.indexOf("/") > -1)
                par = par.substring(0, par.lastIndexOf("/") + 1);
            else
                par = "";
            path = new OpenZipVirtualPath(findVirtualPath(par), name);
        }
        mVirtualPaths.put(name, path);
        return path;
    }

    private void addFamilyPath(String path) {
        String parent = path;
        if (parent.endsWith("/"))
            parent = parent.substring(0, parent.length() - 1);
        parent = parent.substring(0, parent.lastIndexOf("/") + 1);
        if (!parent.equals("") && !parent.endsWith("/"))
            parent += "/";
        if (DEBUG)
            Logger.LogDebug("FamilyPath adding [" + path + "] to [" + parent + "]");
        List<OpenPath> kids = mFamily.get(parent);
        if (kids == null)
            kids = new ArrayList<OpenPath>();
        OpenPath vp = findVirtualPath(path);
        if (!kids.contains(vp))
            kids.add(vp);
        mFamily.put(parent, kids);
        if (!parent.equals(""))
            addFamilyPath(parent);
    }

    private void addFamilyEntry(String path, OpenZipEntry entry) {
        List<OpenPath> list = mFamily.get(path);
        if (list == null)
            list = new ArrayList<OpenPath>();
        if (DEBUG)
            Logger.LogDebug("Adding [" + entry.getName() + "] into [" + path + "]");
        list.add(entry);
        mFamily.put(path, list);
    }

    @Override
    public OpenPath[] list() throws IOException {
        if (mChildren == null)
            mChildren = listFiles();
        return mChildren;
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        if (DEBUG)
            Logger.LogVerbose("Listing OpenZip " + mFile);
        if (mZip == null)
            return mChildren;

        getAllEntries();

        mChildren = listFiles("");

        return mChildren;
    }

    public OpenPath[] listFiles(String rootRelative) throws IOException {
        if (DEBUG)
            Logger.LogDebug("OpenZip.listFiles(" + rootRelative + ")");
        if (!mFamily.containsKey(rootRelative)) {
            Logger.LogWarning("No children found for [" + rootRelative + "]");
            return new OpenPath[0];
        }
        List<OpenPath> ret = mFamily.get(rootRelative);
        if (DEBUG)
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
    
    public OpenPath getZipChild(String path)
    {
    	if(path.endsWith("/"))
    		return new OpenZipVirtualPath(path);
    	else {
    		OpenZipVirtualPath p = null;
    		if(path.indexOf("/") > -1)
    			p = new OpenZipVirtualPath(path.substring(0, path.lastIndexOf("/") + 1));
    		return new OpenZipEntry(p, mZip.getEntry(path));
    	}
    }

    public class OpenZipVirtualPath extends OpenPath implements ListHandler {
        private final String path;
        private final OpenPath mParent;
        private boolean mFinal = false;

        public OpenZipVirtualPath(OpenPath parent, String path) {
            mParent = parent;
            this.path = path;
            mFinal = true;
        }
        
        public OpenZipVirtualPath(String fullpath)
        {
        	if(fullpath.endsWith("/"))
        		fullpath = fullpath.substring(0, fullpath.length() - 1);
        	path = fullpath;
        	if(path.indexOf("/") > -1)
        		mParent = new OpenZipVirtualPath(path.substring(0, path.lastIndexOf("/")));
        	else mParent = OpenZip.this;
        	mFinal = false;
        }
        
        @Override
        public Thread list(final ListListener listener) {
        	if(DEBUG)
        		Logger.LogVerbose("OpenZipVirtualPath.list(" + mParent + "," + path + ")");
        	return thread(new Runnable() {
				public void run() {
					try {
						OpenPath[] kids = listFiles();
						postListReceived(kids, listener);
					} catch(Exception e) {
						postException(e, listener);
					}
				}
			});
        }
        
        @Override
        public void clearChildren() {
        }

        @Override
        public String getName() {
            String name = path;
            if (name.endsWith("/"))
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
        public long length() {
            try {
                return list().length;
            } catch (IOException e) {
            }
            return 0;
        }

        @Override
        public OpenPath getParent() {
            return mParent;
        }

        @Override
        public OpenPath getChild(String name) {
            try {
                for (OpenPath kid : list())
                    if (kid.getName().equals(name))
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
        	OpenPath[] ret = list();
        	//FileManager.setOpenCache(getPath(), this);
        	return ret;
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
            return true;
        }

        @Override
        public Boolean delete() {
            return false;
        }

        @Override
        public Boolean mkdir() {
            return false;
        }

    }

    public class OpenZipEntry extends OpenPath implements OpenStream {
        private final OpenPath mParent;
        private final ZipEntry ze;
        private OpenPath[] mChildren = null;

        public OpenZipEntry(OpenPath parent, ZipEntry entry) {
            mParent = parent;
            ze = entry;
            if (ze.getName().endsWith("/") || ze.isDirectory()) {
                try {
                    mChildren = list();
                } catch (IOException e) {
                }
            }
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
        	return mZip.getInputStream(ze);
        }
        
        @Override
        public OutputStream getOutputStream() throws IOException {
        	return null;
        }

        @Override
        public String getName() {
            String name = ze.getName();
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
            name = name.substring(name.lastIndexOf("/") + 1);
            return name;
        }

        @Override
        public String getPath() {
            return OpenZip.this.getPath() + "/" + ze.getName();
        }

        @Override
        public String getAbsolutePath() {
            return getPath();
        }

        @Override
        public long length() {
            return ze.getSize();
        }

        @Override
        public OpenPath getParent() {
            return mParent;
        }

        @Override
        public OpenPath getChild(String name) {
            try {
                for (OpenPath kid : list())
                    if (kid.getName().equals(name))
                        return kid;
            } catch (IOException e) {
            }
            return null;
        }

        @Override
        public OpenPath[] list() throws IOException {
            if (mChildren != null)
                return mChildren;
            return listFiles();
        }

        @Override
        public OpenPath[] listFiles() throws IOException {
            return OpenZip.this.listFiles(ze.getName());
        }

        @Override
        public int getListLength() {
            try {
                return list().length;
            } catch (IOException e) {
                return 0;
            }
        }

        @Override
        public String getDetails(boolean countHiddenChildren) {
            String ret = super.getDetails(countHiddenChildren);
            if (!isDirectory())
                ret += " (" + OpenPath.formatSize(ze.getCompressedSize()) + ")";
            return ret;
        }

        @Override
        public Boolean isDirectory() {
            return ze.isDirectory() || ze.getName().endsWith("/");
        }

        @Override
        public Boolean isFile() {
            return !ze.isDirectory();
        }

        @Override
        public Boolean isHidden() {
            return getName().startsWith(".");
        }

        @Override
        public Uri getUri() {
            return Uri.parse(getPath());
        }

        @Override
        public Long lastModified() {
            return ze.getTime();
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
    }
}

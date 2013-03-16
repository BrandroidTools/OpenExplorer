
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.data.OpenPath.*;
import org.brandroid.utils.Logger;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;

public class OpenRAR extends OpenPath implements OpenPath.OpenStream {
    private final OpenFile mFile;
    private Archive mRar = null;
    private OpenPath[] mChildren = null;
    private ArrayList<OpenRAREntry> mEntries = null;
    private boolean mValid = false;
    private final Hashtable<String, List<OpenPath>> mFamily = new Hashtable<String, List<OpenPath>>();
    private final Hashtable<String, OpenRARVirtualPath> mVirtualPaths = new Hashtable<String, OpenRAR.OpenRARVirtualPath>();
    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;

    public OpenRAR(OpenFile file) {
        mFile = file;
        try {
            mRar = new Archive(mFile.getFile());
            mValid = true;
            // Logger.LogInfo("Zip file " + zipFile + " has " + length() +
            // " entries");
        } catch (Exception e) {
            Logger.LogError("Couldn't open RAR file (" + file + ")");
        }
    }

    public boolean isValid() {
        return mValid;
    }

    @Override
    public boolean canHandleInternally() {
        return true;
    }

    public Archive getArchive() {
        return mRar;
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
        for (FileHeader hdr : mRar.getFileHeaders())
            if (hdr.getFileNameString().endsWith(name))
                return new OpenRAREntry(this, hdr);
        return null;
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

    public List<OpenRAREntry> getAllEntries() throws IOException {
        if (mEntries != null)
            return mEntries;
        mEntries = new ArrayList<OpenRAREntry>();
        for (FileHeader ze : mRar.getFileHeaders()) {
            if (ze.isDirectory())
                continue;
            String parent = "";
            if (ze.isFileHeader() && ze.isUnicode())
                parent = ze.getFileNameW();
            else
                parent = ze.getFileNameString();
            parent = parent.replace("\\", "/");
            if(parent.endsWith("/"))
                continue;
            if (parent.indexOf("/") > 0 && parent.indexOf("/") < parent.length() - 1)
                parent = parent.substring(0, parent.lastIndexOf("/") + 1);
            else
                parent = "";
            OpenPath vp = findVirtualPath(parent);
            OpenRAREntry entry = new OpenRAREntry(vp, ze);
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

    private OpenPath findVirtualPath(String name) {
        if (mVirtualPaths.containsKey(name))
            return mVirtualPaths.get(name);
        OpenRAR.OpenRARVirtualPath path = null;
        if (name.equals(""))
            return OpenRAR.this;
        else {
            String par = name;
            if (par.endsWith("/"))
                par = par.substring(0, par.length() - 1);
            if (par.indexOf("/") > -1)
                par = par.substring(0, par.lastIndexOf("/") + 1);
            else
                par = "";
            path = new OpenRARVirtualPath(findVirtualPath(par), name);
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

    private void addFamilyEntry(String path, OpenRAREntry entry) {
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
        if (mRar == null)
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
        return mFile.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return mFile.getOutputStream();
    }

    public class OpenRARVirtualPath extends OpenPath {
        private final String path;
        private final OpenPath mParent;

        public OpenRARVirtualPath(OpenPath parent, String path) {
            mParent = parent;
            this.path = path;
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
            return OpenRAR.this.getPath() + "/" + path;
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
            return OpenRAR.this.listFiles(path);
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
            return OpenRAR.this.lastModified();
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

    public class OpenRAREntry extends OpenPath implements OpenStream {
        private final OpenPath mParent;
        private final FileHeader ze;

        public OpenRAREntry(OpenPath parent, FileHeader entry) {
            mParent = parent;
            ze = entry;
            if (ze.getFileNameString().endsWith("/") || ze.isDirectory()) {
                try {
                    mChildren = list();
                } catch (IOException e) {
                }
            }
        }

        @Override
        public String getName() {
            String name = ze.getFileNameString();
            if (ze.isFileHeader() && ze.isUnicode())
                name = ze.getFileNameW();
            name = name.replace("\\", "/");
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
            name = name.substring(name.lastIndexOf("/") + 1);
            return name;
        }
        
        public String getRelativePath()
        {
            String ret = "";
            if (ze.isFileHeader() && ze.isUnicode())
                ret = ze.getFileNameW();
            else
                ret = ze.getFileNameString();
            return ret;
        }

        @Override
        public String getPath() {
            return OpenRAR.this.getPath() + "/" + getRelativePath();
        }

        @Override
        public String getAbsolutePath() {
            return getPath();
        }

        @Override
        public long length() {
            return ze.getFullUnpackSize();
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
            return null;
        }

        @Override
        public OpenPath[] listFiles() throws IOException {
            return null;
        }

        @Override
        public int getListLength() {
            return 0;
        }

        @Override
        public String getDetails(boolean countHiddenChildren) {
            String ret = super.getDetails(countHiddenChildren);
            if (!isDirectory())
                ret += " (" + DialogHandler.formatSize(ze.getFullPackSize()) + ")";
            if(ze.isEncrypted())
                ret += "*";
            return ret;
        }

        @Override
        public Boolean isDirectory() {
            return ze.isDirectory();
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
            return ze.getMTime().getTime();
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

        @SuppressLint("NewApi")
        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return mRar.getInputStream(ze);
            } catch (RarException e) {
                if (Build.VERSION.SDK_INT > 8)
                    throw new IOException("RarException while getting InputStream", e);
                else
                    throw new IOException("RarException while getting InputStream: "
                            + e.getMessage());
            }
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }
    }
}

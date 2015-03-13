
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.contrapunctus.lzma.LzmaInputStream;
import net.contrapunctus.lzma.LzmaOutputStream;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.EventHandler.OnWorkerUpdateListener;
import org.brandroid.openmanager.data.OpenPath.*;
import org.brandroid.utils.Logger;

import SevenZip.ArchiveExtractCallback;
import SevenZip.HRESULT;
import SevenZip.Handler;
import SevenZip.IArchiveExtractCallback;
import SevenZip.IInArchive;
import SevenZip.MyRandomAccessFile;
import SevenZip.SevenZipEntry;
import android.net.Uri;

public class OpenLZMA extends OpenPath implements OpenStream {
    private final OpenFile mFile;
    private MyRandomAccessFile mRAF = null;
    private IInArchive mLZMA = null;
    private OpenPath[] mChildren = null;
    private ArrayList<OpenLZMAEntry> mEntries = null;
    private final Hashtable<String, List<OpenPath>> mFamily = new Hashtable<String, List<OpenPath>>();
    private final Hashtable<String, OpenLZMAVirtualPath> mVirtualPaths = new Hashtable<String, OpenLZMA.OpenLZMAVirtualPath>();
    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;

    public OpenLZMA(OpenFile file) {
        mFile = file;
        try {
            mRAF = new MyRandomAccessFile(file.getPath(), "r");
            mLZMA = new Handler();
            mLZMA.Open(mRAF);
            // Logger.LogInfo("LZMA file " + LZMAFile + " has " + length() +
            // " entries");
        } catch (IOException e) {
            Logger.LogError("Couldn't open LZMA file (" + file + ")");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLZMA != null)
            try {
                mLZMA.close();
            } catch(Exception e) {
            }
        if (mRAF != null)
            try {
                mRAF.close();
            } catch(Exception e) {
            }
    }

    @Override
    public boolean canHandleInternally() {
        return true;
    }

    public IInArchive getLZMA() {
        return mLZMA;
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
        try {
            OpenPath[] kids = list();
            for (OpenPath kid : kids)
                if (kid.getName().equals(name))
                    return kid;
        } catch (Exception e) {
        }
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

    public static boolean isValidLZMA(OpenFile file)
    {
        OpenLZMA LZMA = new OpenLZMA(file);
        InputStream s = null;
        try {
            s = LZMA.getInputStream();
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

    private static int binarySearch(String[] array, String key)
    {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(key))
                return i;
        return -1;
    }

    public List<OpenLZMAEntry> getAllEntries() throws IOException {
        if (mEntries != null)
            return mEntries;
        mEntries = new ArrayList<OpenLZMAEntry>();
        for (int i = 0; i < mLZMA.size(); i++)
        {
            SevenZipEntry ze = mLZMA.getEntry(i);
            if (ze.isDirectory())
                continue;
            String parent = ze.getName();
            if (parent.indexOf("/") > 0 && parent.indexOf("/") < parent.length() - 1)
                parent = parent.substring(0, parent.lastIndexOf("/") + 1);
            else
                parent = "";
            OpenPath vp = findVirtualPath(parent);
            OpenLZMAEntry entry = new OpenLZMAEntry(vp, ze);
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
        OpenLZMAVirtualPath path = null;
        if (name.equals(""))
            return OpenLZMA.this;
        else {
            String par = name;
            if (par.endsWith("/"))
                par = par.substring(0, par.length() - 1);
            if (par.indexOf("/") > -1)
                par = par.substring(0, par.lastIndexOf("/") + 1);
            else
                par = "";
            path = new OpenLZMAVirtualPath(findVirtualPath(par), name);
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

    private void addFamilyEntry(String path, OpenLZMAEntry entry) {
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
            Logger.LogVerbose("Listing OpenLZMA " + mFile);
        if (mLZMA == null)
            return mChildren;

        getAllEntries();

        mChildren = listFiles("");

        return mChildren;
    }

    public OpenPath[] listFiles(String rootRelative) throws IOException {
        if (DEBUG)
            Logger.LogDebug("OpenLZMA.listFiles(" + rootRelative + ")");
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
        return new LzmaInputStream(mFile.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new LzmaOutputStream(mFile.getOutputStream());
    }

    public class OpenLZMAVirtualPath extends OpenPath {
        private final String path;
        private final OpenPath mParent;

        public OpenLZMAVirtualPath(OpenPath parent, String path) {
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
            return OpenLZMA.this.getPath() + "/" + path;
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
            return OpenLZMA.this.listFiles(path);
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
            return OpenLZMA.this.lastModified();
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

    public class OpenLZMAEntry extends OpenPath {
        private final OpenPath mParent;
        private final SevenZipEntry ze;
        private OpenPath[] mChildren = null;

        public OpenLZMAEntry(OpenPath parent, SevenZipEntry entry) {
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
        public String getName() {
            String name = ze.getName();
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
            name = name.substring(name.lastIndexOf("/") + 1);
            return name;
        }

        public String getRelativePath() {
            return ze.getName();
        }

        @Override
        public String getPath() {
            return OpenLZMA.this.getPath() + "/" + getRelativePath();
        }

        @Override
        public String getAbsolutePath() {
            return getPath();
        }

        @Override
        public long length() {
            return ze.getSize();
        }

        public OpenLZMA getLZMA() {
            return OpenLZMA.this;
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
            return OpenLZMA.this.listFiles(ze.getName());
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


package org.brandroid.openmanager.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.kamranzafar.jtar.*;

import com.jcraft.jzlib.GZIPInputStream;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.stericson.RootTools.RootToolsException;
import com.stericson.RootTools.Shell;

import android.net.Uri;
import android.os.Process;

public class OpenTar extends OpenPath implements OpenPath.OpenPathUpdateListener {
    private final OpenFile mFile;
    private OpenPath[] mChildren = null;
    private ArrayList<OpenTarEntry> mEntries = null;
    private final Hashtable<String, List<OpenPath>> mFamily = new Hashtable<String, List<OpenPath>>();
    private final Hashtable<String, OpenTarVirtualPath> mVirtualPaths = new Hashtable<String, OpenTar.OpenTarVirtualPath>();
    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;

    public OpenTar(OpenFile file) {
        if (OpenExplorer.IS_DEBUG_BUILD)
            Logger.LogDebug("Creating OpenTar(" + file.getPath() + ")");
        mFile = file;
    }

    @Override
    public boolean canHandleInternally() {
        return true;
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
    public void setPath(String path) {
        // mZip = new OpenFile(path);
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
            for (OpenTarEntry entry : getAllEntries())
                if (entry.getName().equalsIgnoreCase(name))
                    return entry;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public List<OpenTarEntry> getAllEntries() throws IOException {
        return getAllEntries(null);
    }

    public List<OpenTarEntry> getAllEntries(OpenContentUpdater updater) throws IOException {
        if (mEntries != null)
            return mEntries;
        mEntries = new ArrayList<OpenTarEntry>();
        TarInputStream tis = getInputStream();
        TarEntry te;
        //        try {
        //            RootTools.useRoot = false; //RootTools.closeAllShells();
        //            RootTools.sendShell("tar -tvf " + mFile.getPath(), new Result() {
        //                public void processError(String line) throws Exception {
        //                    Logger.LogVerbose("TAR Error: " + line);
        //                }
        //
        //                public void process(String
        //                        line) throws Exception { // -rw-rw-r-- root/sdcard_rw 7 2013-02-22 13:42:02 123.txt
        //                    String[] parts = line.split("  *", 6);
        //                    String perms = parts[0];
        //                    String[] owner = parts[1].split("/");
        //                    long size = Long.parseLong(parts[2]);
        //                    String date = parts[3];
        //                    String time = parts[4];
        //                    String filename = parts[parts.length - 1];
        //                    Logger.LogVerbose("TAR Kid: " + filename);
        //                }
        //
        //                public void onFailure(Exception ex) {
        //                }
        //
        //                public void onComplete(int diag) {
        //                }
        //            }, 10000);
        //        } catch (RootToolsException e) {
        //            Logger.LogError("Root exception getting tar!", e);
        //        } catch (TimeoutException e) {
        //            Logger.LogError("Timeout getting tar!", e);
        //        }

        int pos = 0;
        while ((te = tis.getNextEntry()) != null)
        {
            pos += te.getHeaderSize() + te.getSize();
            if (te.isDirectory())
                continue;
            if (te.getName().endsWith("/"))
                continue;
            String par = te.getHeader().namePrefix.toString();
            if (!par.equals("") && !par.endsWith("/"))
                par += "/";
            if (te.getName().indexOf("/") > -1)
                par += te.getName().substring(0, te.getName().lastIndexOf("/"));
            Logger.LogVerbose("TAR: " + par);
            OpenPath vp = findVirtualPath(par, updater);
            OpenTarEntry entry = new OpenTarEntry(vp, te, (int)(pos - te.getSize()));
            mEntries.add(entry);
            addFamilyEntry(par, entry);
        }
        Set<String> keys = mFamily.keySet();
        for (String path : keys.toArray(new String[keys.size()])) {
            if (path.equals(""))
                continue;
            addFamilyPath(path);
        }
        return mEntries;
    }

    private OpenPath findVirtualPath(String name, OpenContentUpdater updater) {
        if (mVirtualPaths.containsKey(name))
            return mVirtualPaths.get(name);
        OpenTarVirtualPath path = null;
        if (name.equals(""))
            return OpenTar.this;
        else {
            String par = name;
            if (par.endsWith("/"))
                par = par.substring(0, par.length() - 1);
            if (par.indexOf("/") > -1)
                par = par.substring(0, par.lastIndexOf("/") + 1);
            else
                par = "";
            path = new OpenTarVirtualPath(findVirtualPath(par, updater), name);
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
        OpenPath vp = findVirtualPath(path, null);
        if (!kids.contains(vp))
            kids.add(vp);
        mFamily.put(parent, kids);
        if (!parent.equals(""))
            addFamilyPath(parent);
    }

    private void addFamilyEntry(String path, OpenTarEntry entry) {
        List<OpenPath> list = mFamily.get(path);
        if (list == null)
            list = new ArrayList<OpenPath>();
        if (DEBUG)
            Logger.LogDebug("Adding [" + entry.getName() + "] into [" + path + "]");
        list.add(entry);
        mFamily.put(path, list);
    }

    @Override
    public void list(final OpenContentUpdater callback) throws IOException {
        final RootManager rm = new RootManager();
        new Thread(new Runnable() {
            public void run() {
                Logger.LogVerbose("Running tar list");
                try {
                    getAllEntries(callback);
                    for (OpenPath p : listFiles())
                        callback.addContentPath(p);
                    callback.doneUpdating();
                } catch (Exception e2) {
                    Logger.LogError("Error listing TAR #2.", e2);
                }
            }
        }).start();
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
            Logger.LogVerbose("Listing OpenTar " + mFile);

        if (mChildren != null)
            return mChildren;

        getAllEntries();

        mChildren = listFiles("");

        return mChildren;
    }

    public OpenPath[] listFiles(String rootRelative) throws IOException {
        if (DEBUG)
            Logger.LogDebug("OpenTar.listFiles(" + rootRelative + ")");
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
        return false;
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
        return true;
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
    public TarInputStream getInputStream() throws IOException {
        if (getMimeType().contains("x-"))
            return new TarInputStream(new BufferedInputStream(new GZIPInputStream(
                    new FileInputStream(mFile.getFile()))));
        else
            return new TarInputStream(new BufferedInputStream(new FileInputStream(mFile.getFile())));
    }

    @Override
    public TarOutputStream getOutputStream() throws IOException {
        return new TarOutputStream(new BufferedOutputStream(new FileOutputStream(mFile.getFile())));
    }

    public class OpenTarVirtualPath extends OpenPath {
        private final String path;
        private final OpenPath mParent;

        public OpenTarVirtualPath(OpenPath parent, String path) {
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
            return OpenTar.this.getPath() + "/" + path;
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
            return OpenTar.this.listFiles(path);
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
            return OpenTar.this.lastModified();
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

    public class OpenTarEntry extends OpenPath implements OpenPath.OpenPathCopyable {
        private final OpenPath mParent;
        private final TarEntry te;
        private OpenPath[] mChildren = null;
        private final int mOffset;

        public OpenTarEntry(OpenPath parent, TarEntry entry, int offset) {
            mParent = parent;
            te = entry;
            mOffset = offset;
            if (te.getName().endsWith("/") || te.isDirectory()) {
                try {
                    mChildren = list();
                } catch (IOException e) {
                }
            }
        }

        @Override
        public String getName() {
            String name = te.getName();
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
            name = name.substring(name.lastIndexOf("/") + 1);
            return name;
        }

        @Override
        public String getPath() {
            return OpenTar.this.getPath() + "/" + te.getName();
        }

        @Override
        public String getAbsolutePath() {
            return getPath();
        }

        @Override
        public void setPath(String path) {

        }

        @Override
        public long length() {
            return te.getSize();
        }

        public int getOffset() {
            return mOffset;
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
            return OpenTar.this.listFiles(te.getName());
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
            return super.getDetails(countHiddenChildren);
        }

        @Override
        public Boolean isDirectory() {
            return te.isDirectory() || te.getName().endsWith("/");
        }

        @Override
        public Boolean isFile() {
            return !te.isDirectory();
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
            return te.getModTime().getTime();
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
            TarInputStream fis = OpenTar.this.getInputStream();
            fis.setDefaultSkip(true);
            fis.skip(getOffset());
            return fis;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        public boolean copyFrom(OpenPath file) {
            return false;
        }

        public boolean copyTo(OpenPath dest) throws IOException {
            Logger.LogDebug("TAR copyTo " + dest);
            final int bsize = FileManager.BUFFER;
            byte[] ret = new byte[bsize];
            TarInputStream s = null;
            FileInputStream fis = null;
            OutputStream os = new BufferedOutputStream(dest.getOutputStream());
            s = new TarInputStream(new BufferedInputStream(new FileInputStream(new File(
                    OpenTar.this.getPath()))));
            s.setDefaultSkip(false);
            s.skip(getOffset());
            TarEntry entry;
            boolean valid = false;
            while ((entry = s.getNextEntry()) != null)
            {
                if (entry.getHeader().name.equals(getName()))
                {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                return false;
            int count = 0;
            int size = (int)te.getSize();
            int pos = 0;
            while ((count = s.read(ret, 0, Math.min(bsize, size - pos))) != -1)
            {
                os.write(ret, 0, Math.min(count, size - pos));
                pos += count;
                if (pos >= size)
                    break;
            }
            os.flush();
            os.close();
            s.close();
            return true;
        }

        public String getRelativePath() {
            return te.getName();
        }

        public OpenTar getTar() {
            return OpenTar.this;
        }

    }
}

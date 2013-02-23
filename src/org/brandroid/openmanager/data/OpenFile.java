
package org.brandroid.openmanager.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.data.OpenPath.OpenPathCopyable;
import org.brandroid.openmanager.util.DFInfo;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

@SuppressLint("NewApi")
public class OpenFile extends OpenPath implements OpenPathCopyable, OpenPath.OpenPathByteIO {
    private static final long serialVersionUID = 6436156952322586833L;
    private File mFile;
    private OpenFile[] mChildren = null;
    private boolean bGrandPeeked = false;
    // private String mRoot = null;
    private OutputStream output = null;
    private static OpenFile mInternalDrive = null;
    private static OpenFile mExternalDrive = null;
    private static OpenFile mUsbDrive = null;
    private static OpenFile mTempDir = null;
    private String deets = null;

    public OpenFile setRoot() {
        // / TODO fix this
        return this;
    }

    public OpenFile(File f) {
        mFile = f;
    }

    public OpenFile(String path) {
        mFile = new File(path);
    }

    public OpenFile(String folder, String name) {
        mFile = new File(folder, name);
    }

    public OpenFile(OpenFile folder, String name) {
        mFile = new File(folder.getFile(), name);
    }

    @Override
    public boolean canHandleInternally() {
        if (isTextFile() && Preferences.Pref_Text_Internal)
            return true;
        return false;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getPath() {
        return mFile.getPath();
    }

    @Override
    public long length() {
        return mFile.length();
    }

    @Override
    public int getListLength() {
        if (mChildren != null)
            return mChildren.length;
        else
            return -1;
    }

    public long getFreeSpace() {
        if (getDepth() > 3) {
            if (getPath().startsWith("/mnt/")) {
                OpenFile toCheck = this;
                for (int i = 3; i < getDepth(); i++) {
                    toCheck = toCheck.getParent();
                    if (toCheck == null)
                        break;
                }
                if (toCheck != null)
                    return toCheck.getFreeSpace();
            }
        }
        try {
            StatFs stat = new StatFs(getPath());
            if (stat.getFreeBlocks() > 0)
                return (long)stat.getFreeBlocks() * (long)stat.getBlockSize();
        } catch (Exception e) {
            Logger.LogWarning("Couldn't get Total Space.", e);
        }
        if (DFInfo.LoadDF().containsKey(getPath()))
            return (long)DFInfo.LoadDF().get(getPath()).getFree();
        return Build.VERSION.SDK_INT > 8 ? mFile.getFreeSpace() * 1024 * 1024 : 0;
    }

    public long getUsableSpace() {
        try {
            StatFs stat = new StatFs(getPath());
            if (stat.getAvailableBlocks() > 0)
                return (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
        } catch (Exception e) {
            Logger.LogWarning("Couldn't get Total Space.", e);
        }
        if (DFInfo.LoadDF().containsKey(getPath()))
            return (long)(DFInfo.LoadDF().get(getPath()).getSize() - DFInfo.LoadDF().get(getPath())
                    .getFree());
        else if (Build.VERSION.SDK_INT > 8)
            return mFile.getUsableSpace();
        else
            return 0;
    }

    public long getTotalSpace() {
        if (getDepth() > 4) {
            if (getPath().indexOf("/mnt/") > -1) {
                OpenFile toCheck = this;
                for (int i = 4; i < getDepth(); i++) {
                    toCheck = toCheck.getParent();
                    if (toCheck == null)
                        break;
                }
                if (toCheck != null)
                    return toCheck.getTotalSpace();
            }
        }
        try {
            StatFs stat = new StatFs(getPath());
            if (stat.getBlockCount() > 0)
                return (long)stat.getBlockCount() * (long)stat.getBlockSize();
        } catch (Exception e) {
            Logger.LogWarning("Couldn't get Total Space for " + getPath(), e);
        }
        if (DFInfo.LoadDF().containsKey(getPath()))
            return (long)DFInfo.LoadDF().get(getPath()).getSize();
        return Build.VERSION.SDK_INT > 8 ? mFile.getTotalSpace() * 1024 * 1024 : length();
    }

    public long getUsedSpace() {
        long ret = length();
        if (isDirectory())
            for (OpenPath kid : list())
                ret += ((OpenFile)kid).getUsedSpace();
        return ret;
    }

    public int countAllFiles() {
        if (!isDirectory())
            return 1;
        int ret = 0;
        for (OpenPath kid : list())
            ret += ((OpenFile)kid).countAllFiles();
        return ret;
    }

    public int countAllDirectories() {
        if (!isDirectory())
            return 0;
        int ret = 1;
        for (OpenPath kid : list())
            ret += ((OpenFile)kid).countAllFiles();
        return ret;
    }

    @Override
    public OpenFile getParent() {
        String parent = mFile.getParent();
        if (parent == null)
            return null;
        return new OpenFile(parent);
    }

    @Override
    public OpenFile[] listFiles() {
        return listFiles(false);
    }

    @Override
    public boolean listFromDb(SortType sort) {
        Cursor c = mDb.fetchItemsFromFolder(getPath().replace("/" + getName(), ""), sort);
        if (c == null) {
            Logger.LogWarning("Null found in DB");
            return false;
        }
        mChildren = new OpenFile[c.getCount()];
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String folder = c
                    .getString(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_FOLDER));
            String name = c.getString(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_NAME));
            int size = c.getInt(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_SIZE));
            int modified = c.getInt(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_MTIME));
            mChildren[c.getPosition()] = new OpenFile(folder + "/" + name);
            c.moveToNext();
        }
        Logger.LogVerbose("listFromDb found " + mChildren.length + " children");
        c.close();
        return true;
    }

    /*
     * @Override public int getChildCount() throws IOException {
     * Logger.LogDebug("Trying to get child count for " + mFile.getPath() +
     * " view NoRoot"); StringBuilder lines =
     * RootManager.Default.executeNoRoot("ls " + mFile.getPath() + " | wc -l");
     * if(lines == null || lines.length() == 0) return super.getChildCount();
     * Logger.LogDebug("Return value: " + lines.toString()); try { return
     * Integer.parseInt(lines.toString()); } catch(Exception e) { return
     * super.getChildCount(); } }
     */

    public static void setExternalMemoryDrive(OpenFile root) {
        mExternalDrive = root;
    }

    public static OpenFile getExternalMemoryDrive(boolean fallbackToInternal) // sd
    {
        if (mExternalDrive != null && mExternalDrive.exists())
            return mExternalDrive;
        for (OpenFile kid : getInternalMemoryDrive().getParent().listFiles())
            if ((kid.getName().toLowerCase().indexOf("ext") > -1 || kid.getName().toLowerCase()
                    .indexOf("sdcard1") > -1)
                    && !kid.getPath().equals(getInternalMemoryDrive().getPath())
                    && kid.canRead()
                    && kid.canWrite()) {
                return mExternalDrive = kid;
            }
        if (new File("/Removable").exists())
            for (OpenFile kid : new OpenFile("/Removable").listFiles())
                if (kid.getName().toLowerCase().indexOf("ext") > -1 && kid.canRead()
                        && !kid.getPath().equals(getInternalMemoryDrive().getPath())
                        && kid.list().length > 0) {
                    return mExternalDrive = kid;
                }
        if (!fallbackToInternal)
            return null;
        else
            return getInternalMemoryDrive();
    }

    public static void setInternalMemoryDrive(OpenFile root) {
        mInternalDrive = root;
    }

    public static OpenFile getInternalMemoryDrive() // internal
    {
        OpenFile ret = null;
        if (OpenExplorer.isNook()) {
            ret = new OpenFile("/mnt/media");
            if (ret != null && ret.canWrite())
                return mInternalDrive = ret;
        }
        if (mInternalDrive != null)
            return mInternalDrive;
        ret = new OpenFile(Environment.getExternalStorageDirectory());
        Logger.LogVerbose("Internal Storage: " + ret);
        if (ret == null || !ret.exists()) {
            OpenFile mnt = new OpenFile("/mnt");
            if (mnt != null && mnt.exists())
                for (OpenFile kid : mnt.listFiles())
                    if (kid.getName().toLowerCase().indexOf("sd") > -1)
                        if (kid.canWrite())
                            return mInternalDrive = kid;
        } else if (ret.getName().endsWith("1")) {
            OpenFile sdcard0 = new OpenFile(ret.getPath().substring(0, ret.getPath().length() - 1)
                    + "0");
            if (sdcard0 != null && sdcard0.exists())
                ret = sdcard0;
        }
        mInternalDrive = ret;
        return mInternalDrive;
    }

    public static void setUsbDrive(OpenFile usb) {
        mUsbDrive = usb;
    }

    public static OpenFile getUsbDrive() {
        if (mUsbDrive != null && mUsbDrive.exists()
                && mUsbDrive.getTotalSpace() != getExternalMemoryDrive(true).getTotalSpace())
            return mUsbDrive;
        OpenFile parent = getExternalMemoryDrive(true).getParent();
        if (Build.VERSION.SDK_INT > 15) {
            parent = new OpenFile("/storage/usbStorage/");
            if(parent.exists() && parent.length() > 0)
                return (mUsbDrive = parent);
            parent = new OpenFile("/mnt/sdcard/usbStorage/");
            if (parent.exists())
                if (parent.list().length == 1 && parent.getChild(0).exists())
                    return (mUsbDrive = (OpenFile)parent.getChild(0));
            parent = new OpenFile("/mnt/sdcard/usb_storage/");
            if (parent.exists())
                if (parent.list().length == 1 && parent.getChild(0).exists())
                    return (mUsbDrive = (OpenFile)parent.getChild(0));
            parent = getExternalMemoryDrive(true).getParent();
        }
        if (parent == null)
            return null;
        if (!parent.exists())
            return null;
        for (OpenFile kid : parent.listFiles())
            if (kid.getName().toLowerCase().contains("usb") && kid.exists() && kid.canRead()
                    && kid.list().length > 0 && kid.getTotalSpace() != parent.getTotalSpace())
                return (mUsbDrive = kid);
        if (Build.VERSION.SDK_INT > 15)
        {
            parent = getInternalMemoryDrive();
            for (OpenFile kid : parent.listFiles())
                if (kid.getName().toLowerCase().contains("usb") && kid.exists() && kid.canRead()
                        && kid.list().length > 0 && kid.getTotalSpace() != parent.getTotalSpace())
                {
                    if(kid.length() == 1 && kid.getChild(0).getName().startsWith("sda"))
                        kid = (OpenFile)kid.getChild(0);
                    return (mUsbDrive = kid);
                }
        }
        return null;
    }

    private OpenFile[] getOpenPaths(File[] files) {
        if (files == null)
            return new OpenFile[0];
        OpenFile[] ret = new OpenFile[files.length];
        for (int i = 0; i < files.length; i++)
            ret[i] = new OpenFile(files[i]);
        return ret;
    }

    private OpenFile[] getOpenPaths(String[] files, String base) {
        if (files == null)
            return new OpenFile[0];
        OpenFile[] ret = new OpenFile[files.length];
        for (int i = 0; i < files.length; i++)
            ret[i] = new OpenFile(files[i]);
        return ret;
    }

    public OpenFile[] listFiles(boolean grandPeek) {
        mChildren = getOpenPaths(mFile.listFiles());
        if (!grandPeek) {
            // Logger.LogDebug(mFile.getPath() + " has " + mChildren.length +
            // " children");
        } // else mChildren = listFilesNative(mFile);

        if ((mChildren == null || mChildren.length == 0) && !isDirectory()
                && mFile.getParentFile() != null)
            mChildren = getParent().listFiles(grandPeek);

        if (mChildren == null)
            return new OpenFile[0];

        if (grandPeek && !bGrandPeeked && mChildren != null && mChildren.length > 0) {
            for (int i = 0; i < mChildren.length; i++) {
                try {
                    if (!mChildren[i].isDirectory())
                        continue;
                    mChildren[i].listFiles();
                } catch (ArrayIndexOutOfBoundsException e) {
                    Logger.LogWarning("Grandchild lost!", e);
                }
            }
            bGrandPeeked = true;
        }

        return mChildren;
    }

    @Override
    public Boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public Uri getUri() {
        return Uri.fromFile(mFile);
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
        if (Build.VERSION.SDK_INT > 9)
            return mFile.canExecute();
        else
            return false;
    }

    @Override
    public Boolean exists() {
        return mFile.exists();
    }

    @Override
    public OpenPath[] list() {
        if (mChildren != null)
            return mChildren;
        return listFiles();
    }

    @Override
    public Boolean requiresThread() {
        return false;
    }

    @Override
    public String getAbsolutePath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public OpenFile getChild(String name) {
        // if(!base.isDirectory())
        // base = base.getParentFile();
        return new OpenFile(new File(getFile(), name));
    }

    @Override
    public Boolean isFile() {
        return mFile.isFile();
    }

    @Override
    public Boolean delete() {
        Logger.LogDebug("Deleting " + getPath());
        return mFile.delete();
    }

    @Override
    public Boolean mkdir() {
        return mFile.mkdirs();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(mFile);
    }

    public FileChannel getInputChannel() throws IOException {
        return ((FileInputStream)getInputStream()).getChannel();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (output == null)
            output = new FileOutputStream(mFile);
        return output;
    }

    public FileChannel getOutputChannel() throws IOException {
        return ((FileOutputStream)getOutputStream()).getChannel();
    }

    public boolean copyFrom(OpenFile sourceFile) {
        try {
            if (!exists())
                getFile().createNewFile();
        } catch (IOException e) {
        }
        boolean ret = false;
        FileChannel source = null;
        FileChannel dest = null;
        try {
            source = sourceFile.getInputChannel();
            dest = getOutputChannel();
            dest.transferFrom(source, 0, source.size());
            ret = true;
        } catch (IOException e) {
            Logger.LogError(
                    "Couldn't CopyFrom (" + sourceFile.getPath() + " -> " + getPath() + ")", e);
            ret = new OpenFileRoot(this).copyFrom(sourceFile);
        } finally {
            if (source != null)
                try {
                    source.close();
                } catch (IOException e) {
                }
            if (dest != null)
                try {
                    dest.close();
                } catch (IOException e) {
                }
        }
        return ret;
    }

    @Override
    public Boolean isHidden() {
        if (mFile.isHidden() || mFile.getName().startsWith("."))
            return true;
        if (mFile.getParent().equals("/mnt") && getUsableSpace() == 0)
            return true;
        return false;
    }

    @Override
    public void setPath(String path) {
        mFile = new File(path);
    }

    @Override
    public void clearChildren() {
        mChildren = null;
    }

    public boolean create() throws IOException {
        if (getParent() != null)
            getParent().mkdir();
        return mFile.createNewFile();
    }

    public String readAscii() {
        StringBuilder sb = new StringBuilder((int)length());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line + "\n");
        } catch (Exception e) {
            Logger.LogError("Couldn't read data from OpenFile(" + getPath() + ")", e);
            return new String(readBytes());
        }
        return sb.toString();
    }

    public byte[] readBytes() {
        byte[] ret = new byte[(int)length()];
        InputStream is = null;
        try {
            is = getInputStream();
            is.read(ret);
        } catch (Exception e) {
            Logger.LogError("Unable to read byte[] data from OpenFile(" + getPath() + ")", e);
            ret = new OpenFileRoot(this).readBytes();
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                }
        }
        return ret;
    }

    public String readHead(int lines) {
        StringBuilder sb = new StringBuilder((int)length());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()));
            String line;
            while (lines-- > 0 && (line = br.readLine()) != null)
                sb.append(line + "\n");
        } catch (Exception e) {
            Logger.LogError("Couldn't read data from OpenFile(" + getPath() + ")", e);
        }
        return sb.toString();
    }

    public void write(String data) {
        try {
            OutputStream s = getOutputStream();
            s.write(data.getBytes());
            s.flush();
            s.close();
        } catch (Exception e) {
            Logger.LogError("Couldn't write to OpenFile (" + getPath() + ")", e);
            new OpenFileRoot(this).writeBytes(data.getBytes());
        }
    }

    public void writeBytes(byte[] buffer) {
        OutputStream os = null;
        try {
            if (!exists())
                create();
            os = getOutputStream();
            os.write(buffer);
            os.flush();
            os.close();
        } catch (IOException e) {
            Logger.LogError("Couldn't write to OpenFile (" + getPath() + ")", e);
            new OpenFileRoot(this).writeBytes(buffer);
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                }
        }
    }

    public void rename(String name) {
        File newFile = new File(getParent().getFile(), name);
        if (newFile.exists())
            newFile.delete();
        Logger.LogDebug("Renaming " + getPath() + " to " + newFile.getPath());
        mFile.renameTo(newFile);
    }

    @Override
    public boolean copyFrom(OpenPath file) {
        if (file instanceof OpenFile)
            return copyFrom((OpenFile)file);
        return false;
    }

    public static void setTempFileRoot(OpenFile root) {
        mTempDir = root;
    }

    public static OpenFile getTempFileRoot() {
        if (mTempDir != null)
            return mTempDir;
        if (Environment.getExternalStorageDirectory() != null
                && Environment.getExternalStorageDirectory().exists())
            return new OpenFile(Environment.getExternalStorageDirectory()).getChild("temp");
        if (mExternalDrive != null)
            return mExternalDrive.getChild("OpenExplorer").getChild("temp");
        if (mInternalDrive != null)
            return mInternalDrive.getChild("OpenExplorer").getChild("temp");
        return null;
    }

    @Override
    public boolean touch() {
        try {
            if (exists())
                return getFile().setLastModified(new Date().getTime());
            else
                return getFile().createNewFile();
        } catch (Exception e) {
            return new OpenFileRoot(this).touch();
        }
    }

    public boolean isRemoveable() {
        if (getPath().indexOf("/remov") > -1)
            return true;
        if (getPath().indexOf("/mnt/media") > -1)
            return true;
        if (getPath().startsWith("/storage"))
            return true;
        if (getPath().toLowerCase().startsWith("usb"))
            return true;
        return false;
    }
    
    @Override
    public boolean showChildPath() {
        return false;
    }

    @Override
    public boolean copyTo(OpenPath dest) throws IOException {
        if(dest instanceof OpenFile)
            return ((OpenFile)dest).copyFrom(this);
        return false;
    }
}

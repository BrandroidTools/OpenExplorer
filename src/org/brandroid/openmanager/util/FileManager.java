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

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTPFile;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.FTPManager;
import org.brandroid.openmanager.data.OpenContent;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFileRoot;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenSCP;
import org.brandroid.openmanager.data.OpenSFTP;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSearch;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.data.OpenTar;
import org.brandroid.openmanager.data.OpenZip;
import org.brandroid.openmanager.data.OpenSearch.SearchProgressUpdateListener;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import com.jcraft.jsch.UserInfo;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.util.Log;

public class FileManager {
    public static final int BUFFER = 128 * 1024;

    private boolean mShowHiddenFiles = false;
    private SortType mSorting = SortType.ALPHA;
    private long mDirSize = 0;
    private static Hashtable<String, OpenPath> mOpenCache = new Hashtable<String, OpenPath>();
    public static UserInfo DefaultUserInfo;
    private OnProgressUpdateCallback mCallback = null;

    public interface OnProgressUpdateCallback {
        public void onProgressUpdateCallback(Integer... vals);
    }

    public void setProgressListener(OnProgressUpdateCallback listener) {
        mCallback = listener;
    }

    public void updateProgress(Integer... vals) {
        if (mCallback != null)
            mCallback.onProgressUpdateCallback(vals);
    }

    /**
     * Constructs an object of the class <br>
     * this class uses a stack to handle the navigation of directories.
     */
    public FileManager() {
    }

    /**
     * @param old the file to be copied
     * @param newDir the directory to move the file to
     * @return
     */
    /*
     * public int copyToDirectory(String old, String newDir) { final File
     * old_file = new File(old); final File temp_dir = new File(newDir); final
     * byte[] data = new byte[BUFFER]; int read = 0; if(old_file.isFile() &&
     * temp_dir.isDirectory() && temp_dir.canWrite()){ String file_name =
     * old.substring(old.lastIndexOf("/"), old.length()); File cp_file = new
     * File(newDir + file_name); if(cp_file.equals(old_file)) return 0; try {
     * BufferedInputStream i_stream = new BufferedInputStream( new
     * FileInputStream(old_file)); BufferedOutputStream o_stream = new
     * BufferedOutputStream( new FileOutputStream(cp_file)); while((read =
     * i_stream.read(data, 0, BUFFER)) != -1) o_stream.write(data, 0, read);
     * o_stream.flush(); i_stream.close(); o_stream.close(); } catch
     * (FileNotFoundException e) { Log.e("FileNotFoundException",
     * e.getMessage()); return -1; } catch (IOException e) {
     * Log.e("IOException", e.getMessage()); return -1; } }else
     * if(old_file.isDirectory() && temp_dir.isDirectory() &&
     * temp_dir.canWrite()) { String files[] = old_file.list(); String dir =
     * newDir + old.substring(old.lastIndexOf("/"), old.length()); int len =
     * files.length; if(!new File(dir).mkdir()) return -1; for(int i = 0; i <
     * len; i++) copyToDirectory(old + "/" + files[i], dir); } else
     * if(!temp_dir.canWrite()) return -1; return 0; }
     */

    /**
     * @param zipName
     * @param toDir
     * @param fromDir
     */
    public void extractZipFilesFromDir(OpenPath zip, OpenPath directory) {
        if (!directory.mkdir() && directory.isDirectory())
            return;
        extractZipFiles(zip, directory);
    }

    /**
     * @param zip_file
     * @param directory
     */
    public void extractZipFiles(OpenPath zip, OpenPath directory) {
        byte[] data = new byte[BUFFER];
        ZipEntry entry;
        ZipInputStream zipstream;

        directory.mkdir();

        try {
            zipstream = new ZipInputStream(zip.getInputStream());

            while ((entry = zipstream.getNextEntry()) != null) {
                OpenPath newFile = directory.getChild(entry.getName());
                if (!newFile.mkdir())
                    continue;

                int read = 0;
                FileOutputStream out = null;
                try {
                    out = (FileOutputStream)newFile.getOutputStream();
                    while ((read = zipstream.read(data, 0, BUFFER)) != -1)
                        out.write(data, 0, read);
                } catch (Exception e) {
                    Logger.LogError("Error unzipping " + zip.getAbsolutePath(), e);
                } finally {
                    zipstream.closeEntry();
                    if (out != null)
                        out.close();
                }
            }

        } catch (FileNotFoundException e) {
            Logger.LogError("Couldn't find file.", e);

        } catch (IOException e) {
            Logger.LogError("Couldn't extract zip.", e);
        }
    }

    /**
     * @param path
     */
    public void createZipFile(OpenPath zip, OpenPath[] files) {
        ZipOutputStream zout = null;
        try {
            zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                    ((OpenFile)zip).getFile()), BUFFER));

            int total = 0;
            OpenPath relPath = files[0].getParent();
            for (OpenPath file : files) {
                if (relPath == null
                        || (file.getParent() != null && relPath.getPath().startsWith(
                                file.getParent().getPath())))
                    relPath = file.getParent();
                total += file.length();
            }
            Logger.LogDebug("Zipping " + total + " bytes!");
            for (OpenPath file : files) {
                try {
                    zipIt(file, zout, total, relPath.getPath()
                            + (relPath.getPath().endsWith("/") ? "" : "/"));
                } catch (IOException e) {
                    Logger.LogError("Error zipping file.", e);
                }
            }

            zout.close();

        } catch (FileNotFoundException e) {
            Log.e("File not found", e.getMessage());

        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        } finally {
            if (zout != null)
                try {
                    zout.close();
                } catch (IOException e) {
                    Logger.LogError("Error closing zip file", e);
                }
        }
    }

    private void zipIt(OpenPath file, ZipOutputStream zout, int totalSize, String relativePath)
            throws IOException {
        byte[] data = new byte[BUFFER];
        int read;

        if (file.isFile()) {
            String name = file.getPath();
            if (relativePath != null && name.startsWith(relativePath))
                name = name.substring(relativePath.length());
            ZipEntry entry = new ZipEntry(name);
            zout.putNextEntry(entry);
            BufferedInputStream instream = new BufferedInputStream(file.getInputStream());
            // Logger.LogVerbose("zip_folder file name = " + entry.getName());
            int size = (int)file.length();
            int pos = 0;
            while ((read = instream.read(data, 0, BUFFER)) != -1) {
                pos += read;
                zout.write(data, 0, read);
                updateProgress(pos, size, totalSize);
            }

            zout.closeEntry();
            instream.close();

        } else if (file.isDirectory()) {
            // Logger.LogDebug("zip_folder dir name = " + file.getPath());
            for (OpenPath kid : file.list())
                totalSize += kid.length();
            for (OpenPath kid : file.list())
                zipIt(kid, zout, totalSize, relativePath);
        }
    }

    /**
     * @param filePath
     * @param newName
     * @return
     */
    public static boolean renameTarget(OpenFile file, String newName) {
        try {
            file.rename(newName);
            return true;
        } catch (Exception e) {
            Logger.LogError("Unable to rename file to [" + newName + "]: " + file, e);
            return false;
        }
    }

    /**
     * The full path name of the file to delete.
     * 
     * @param path name
     * @return Number of Files deleted
     */
    public int deleteTarget(OpenPath target) {

        int ret = 0;

        if (!target.exists())
            Logger.LogWarning("Unable to delete target as it no longer exists: " + target.getPath());

        if (target.isFile()) {
            if (target.delete())
                ret++;
            else
                Logger.LogError("Unable to delete target file: " + target.getPath());
        }

        else if (target.exists() && target.isDirectory() && target.canRead()) {
            OpenPath[] file_list = null;
            try {
                file_list = target.list();
            } catch (IOException e) {
                Logger.LogError("Error listing children to delete.", e);
            }

            if (file_list != null && file_list.length > 0) {

                for (int i = 0; i < file_list.length; i++)
                    ret += deleteTarget(file_list[i]);

            }
            if (target.exists()) {
                // Directory is now empty, please delete!
                if (target.delete())
                    ret++;
                else
                    Logger.LogError("Couldn't delete target " + target.getPath());
            } else
                Logger.LogWarning("Unable to delete target as it does not exist: "
                        + target.getPath());
        }
        return ret;
    }

    /**
     * converts integer from wifi manager to an IP address.
     * 
     * @param des
     * @return
     */
    public static String integerToIPAddress(int ip) {
        String ascii_address = "";
        int[] num = new int[4];

        num[0] = (ip & 0xff000000) >> 24;
        num[1] = (ip & 0x00ff0000) >> 16;
        num[2] = (ip & 0x0000ff00) >> 8;
        num[3] = ip & 0x000000ff;

        ascii_address = num[0] + "." + num[1] + "." + num[2] + "." + num[3];

        return ascii_address;
    }

    public static void clearOpenCache() {
        if (mOpenCache != null)
            mOpenCache.clear();
    }

    public static boolean hasOpenCache(String path) {
        return mOpenCache != null && mOpenCache.containsKey(path);
    }

    public static OpenPath removeOpenCache(String path) {
        return mOpenCache.remove(path);
    }

    public static OpenPath getOpenCache(String path) {
        return getOpenCache(path, null);
    }

    public static OpenPath getOpenCache(String path, Context c) {
        if (path == null)
            return null;
        OpenPath ret = null;
        if (path.startsWith("/"))
            ret = new OpenFile(path);
        else if (path.startsWith("ftp:/"))
            ret = new OpenFTP(path, null, new FTPManager());
        else if (path.startsWith("sftp:/"))
            ret = new OpenSFTP(path);
        else if (path.startsWith("smb:/"))
            try {
                ret = new OpenSMB(path);
            } catch (MalformedURLException e) {
                Logger.LogError("FileManager.getOpenCache unable to instantiate SMB");
            }
        else if (path.equals("Videos"))
            ret = OpenExplorer.getVideoParent();
        else if (path.equals("Photos"))
            ret = OpenExplorer.getPhotoParent();
        else if (path.equals("Music"))
            ret = OpenExplorer.getMusicParent();
        else if (path.equals("Downloads"))
            ret = OpenExplorer.getDownloadParent();
        else if (path.equals("External")
                && !checkForNoMedia(OpenFile.getExternalMemoryDrive(false)))
            ret = OpenFile.getExternalMemoryDrive(false);
        else if (path.equals("Internal") || path.equals("External"))
            ret = OpenFile.getInternalMemoryDrive();
        else if (path.startsWith("content://org.brandroid.openmanager/search/")) {
            String query = path.replace("content://org.brandroid.openmanager/search/", "");
            path = path.substring(query.indexOf("/") + 1);
            if (query.indexOf("/") > -1)
                query = Uri.decode(query.substring(0, query.indexOf("/")));
            else
                query = "";
            ret = new OpenSearch(query, getOpenCache(path), (SearchProgressUpdateListener)null);
        } else if (path.startsWith("content://") && c != null)
            ret = new OpenContent(Uri.parse(path), c);
        else
            ret = null;
        return ret;
    }

    public static boolean checkForNoMedia(OpenPath defPath) {
        if (defPath == null)
            return true;
        if (defPath instanceof OpenFile) {
            StatFs sf = new StatFs(defPath.getPath());
            if (sf.getBlockCount() == 0)
                return true;
            else
                return false;
        } else {
            try {
                return defPath.list() == null || defPath.list().length == 0;
            } catch (IOException e) {
                Logger.LogError("Error Checking for Media.", e);
                return true;
            }
        }
    }

    public static OpenPath getOpenCache(String path, Boolean bGetNetworkedFiles, SortType sort)
            throws IOException // , SmbAuthException, SmbException
    {
        if (path == null)
            return null;
        // Logger.LogDebug("Checking cache for " + path);
        if (mOpenCache == null)
            mOpenCache = new Hashtable<String, OpenPath>();
        OpenPath ret = mOpenCache.get(path);
        if (ret == null) {
            if (path.startsWith("ftp:/") && OpenServers.DefaultServers != null) {
                Logger.LogDebug("Checking cache for " + path);
                FTPManager man = new FTPManager(path);
                FTPFile file = new FTPFile();
                file.setName(path.substring(path.lastIndexOf("/") + 1));
                Uri uri = Uri.parse(path);
                OpenServer server = OpenServers.DefaultServers.findByHost("ftp", uri.getHost());
                man.setUser(server.getUser());
                man.setPassword(server.getPassword());
                ret = new OpenFTP(null, file, man);
            } else if (path.startsWith("scp:/")) {
                Uri uri = Uri.parse(path);
                ret = new OpenSCP(uri.getHost(), uri.getUserInfo(), uri.getPath(), null);
            } else if (path.startsWith("sftp:/") && OpenServers.DefaultServers != null) {
                Uri uri = Uri.parse(path);
                OpenServer server = OpenServers.DefaultServers.findByHost("sftp", uri.getHost());
                ret = new OpenSFTP(uri);
                SimpleUserInfo info = new SimpleUserInfo();
                if (server != null)
                    info.setPassword(server.getPassword());
                ((OpenSFTP)ret).setUserInfo(info);
            } else if (path.startsWith("smb:/") && OpenServers.DefaultServers != null) {
                try {
                    Uri uri = Uri.parse(path);
                    String user = uri.getUserInfo();
                    if (user != null && user.indexOf(":") > -1)
                        user = user.substring(0, user.indexOf(":"));
                    else
                        user = "";
                    OpenServer server = OpenServers.DefaultServers.findByPath("smb", uri.getHost(),
                            user, uri.getPath());
                    if (server == null)
                        server = OpenServers.DefaultServers.findByUser("smb", uri.getHost(), user);
                    if (server == null)
                        server = OpenServers.DefaultServers.findByHost("smb", uri.getHost());
                    if (user == "")
                        user = server.getUser();
                    if (server != null && server.getPassword() != null
                            && server.getPassword() != "")
                        user += ":" + server.getPassword();
                    if (!user.equals(""))
                        user += "@";
                    ret = new OpenSMB(uri.getScheme() + "://" + user + uri.getHost()
                            + uri.getPath());
                } catch (Exception e) {
                    Logger.LogError("Couldn't get samba from cache.", e);
                }
            } /*else if (path.startsWith("/data") || path.startsWith("/system")
                    || path.startsWith("/mnt/shell")
                    || (path.indexOf("/emulated/") > -1 && path.indexOf("/emulated/0") == -1))
                ret = new OpenFileRoot(new OpenFile(path));
              */
            else if (path.startsWith("/"))
                ret = new OpenFile(path);
            else if (path.startsWith("file://"))
                ret = new OpenFile(path.replace("file://", ""));
            else if (path.equals("Videos"))
                ret = OpenExplorer.getVideoParent();
            else if (path.equals("Photos"))
                ret = OpenExplorer.getPhotoParent();
            else if (path.equals("Music"))
                ret = OpenExplorer.getMusicParent();
            else if (path.equals("Downloads"))
                ret = OpenExplorer.getDownloadParent();
            if (ret == null)
                return ret;
            if (ret instanceof OpenFile && ret.isArchive() && Preferences.Pref_Zip_Internal)
                ret = new OpenZip((OpenFile)ret);
//            if (ret instanceof OpenFile
//                    && (ret.getMimeType().contains("tar")
//                            || ret.getExtension().equalsIgnoreCase("tar")
//                            || ret.getExtension().equalsIgnoreCase("win")))
//                ret = new OpenTar((OpenFile)ret);
            if (ret.requiresThread() && bGetNetworkedFiles) {
                if (ret.listFiles() != null)
                    setOpenCache(path, ret);
            } else if (ret instanceof OpenNetworkPath) {
                if (ret.listFromDb(sort))
                    setOpenCache(path, ret);
            }
        }
        // if(ret == null)
        // ret = setOpenCache(path, new OpenFile(path));
        // else setOpenCache(path, ret);
        return ret;
    }

    public static OpenPath setOpenCache(String path, OpenPath file) {
        // Logger.LogDebug("FileManager.setOpenCache(" + path + ")");
        mOpenCache.put(path, file);
        return file;
    }

    public static void addCacheToDb() {
        for (OpenPath path : mOpenCache.values())
            path.addToDb();
    }

    public OpenPath[] getChildren(OpenPath directory) throws IOException {
        // mDirContent.clear();
        if (directory == null)
            return new OpenPath[0];
        if (!directory.isDirectory())
            return new OpenPath[0];
        return directory.list();
    }

}

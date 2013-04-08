
package org.brandroid.openmanager.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenNetworkPath.NetworkListener;
import org.brandroid.utils.Logger;

import android.net.Uri;
import android.os.AsyncTask;

public class OpenVFS extends OpenNetworkPath implements OpenPath.ListHandler {

    private OpenVFS mParent;
    private static FileSystemManager mManager;
    private FileObject mFile;
    private String mPath;
    private OpenVFS[] mChildren = null;
    private static StaticUserAuthenticator mAuth;
    private static FileSystemOptions mOpts;

    public OpenVFS(String path)
    {
        mParent = null;
        try {
            mPath = path;
            if(!Thread.currentThread().equals(OpenExplorer.UiThread))
                mFile = getManager().resolveFile(path);
        } catch (Exception e) {
        }
    }

    public OpenVFS(OpenVFS parent, FileObject kid)
    {
        mParent = parent;
        mFile = kid;
    }

    public static FileSystemManager getManager()
    {
        if (mManager != null)
            return mManager;
        try {
            mManager = VFS.getManager();
        } catch (Exception e) {
            Logger.LogError("Unable to get VFS Manager!", e);
        }
        return mManager;
    }

    public static void setAuthenticator(StaticUserAuthenticator auth)
    {
        mAuth = auth;
        mOpts = new FileSystemOptions();
        try {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(mOpts, mAuth);
        } catch (FileSystemException e) {
            Logger.LogError("Unable to set default VFS Authenticator!", e);
        }
    }

    @Override
    public String getName() {
        if(mPath != null)
            return mPath.substring(mPath.lastIndexOf("/") + 1);
        else return mFile.getName().getBaseName();
    }

    @Override
    public String getPath() {
        if(mFile != null)
            return mFile.getName().getPath();
        else return mPath;
    }

    @Override
    public String getAbsolutePath() {
        return getPath();
    }

    @Override
    public long length() {
        return mChildren != null ? mChildren.length : 0;
    }

    @Override
    public OpenPath getParent() {
        return mParent;
    }

    @Override
    public OpenPath getChild(String name) {
        try {
            return new OpenVFS(this, mFile.getChild(name));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OpenVFS[] list() throws IOException {
        if (mChildren != null)
            return mChildren;
        return listFiles();
    }

    @Override
    public OpenVFS[] listFiles() throws IOException {
        List<OpenVFS> kids = new Vector<OpenVFS>();
        for (FileObject fo : mFile.getChildren())
            kids.add(new OpenVFS(this, fo));
        return mChildren;
    }

    @Override
    public Boolean isDirectory() {
        try {
            if(mFile != null)
                return mFile.getType() == FileType.FOLDER;
            else return true;
        } catch (FileSystemException e) {
            return false;
        }
    }

    @Override
    public Boolean isFile() {
        return !isDirectory();
    }

    @Override
    public Boolean isHidden() {
        try {
            if(mFile != null)
                return mFile.isHidden();
            else return getName().startsWith(".");
        } catch (FileSystemException e) {
            return getName().startsWith(".");
        }
    }

    @Override
    public Uri getUri() {
        return Uri.parse(getAbsolutePath());
    }

    @Override
    public Long lastModified() {
        try {
            if(mFile != null)
                return mFile.getContent().getLastModifiedTime();
            return 0l;
        } catch (FileSystemException e) {
            return null;
        }
    }

    @Override
    public Boolean canRead() {
        try {
            if(mFile != null)
                return mFile.isReadable();
            else return false;
        } catch (FileSystemException e) {
            return false;
        }
    }

    @Override
    public Boolean canWrite() {
        try {
            if(mFile != null)
                return mFile.isWriteable();
            else return false;
        } catch (FileSystemException e) {
            return false;
        }
    }

    @Override
    public Boolean canExecute() {
        return false;
    }

    @Override
    public Boolean exists() {
        try {
            if(mFile != null)
                return mFile.exists();
            else return false;
        } catch (FileSystemException e) {
            return true;
        }
    }

    @Override
    public Boolean delete() {
        try {
            if(mFile != null)
                return mFile.delete();
            else return false;
        } catch (FileSystemException e) {
            Logger.LogError("Unable to delete VFS", e);
            return false;
        }
    }

    @Override
    public Boolean mkdir() {
        try {
            if(mFile != null)
                mFile.createFolder();
            else return false;
            return true;
        } catch (FileSystemException e) {
            return false;
        }
    }

    @Override
    public Thread list(final ListListener listener) {
        return thread(new Runnable() {
            public void run() {
                try {
                    postListReceived(listFiles(), listener);
                } catch (Exception e) {
                    postException(e, listener);
                }
            }
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(mFile.getContent().getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new BufferedOutputStream(mFile.getContent().getOutputStream());
    }

    @Override
    public boolean syncUpload(OpenFile f, final NetworkListener l) {
        Logger.LogDebug("OpenVFS.copyFrom(" + f + ")");
        try {
            copyStreams(f.getInputStream(),
                    getOutputStream(),
                    true, true,
                    new ProgressUpdateListener() {

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public void onProgressUpdate(Integer... progress) {
                            l.OnNetworkCopyUpdate(progress);
                        }
                    });
            return true;
        } catch (Exception e) {
            if (l != null)
                l.OnNetworkFailure(this, f, e);
            return false;
        }
    }

    @Override
    public boolean syncDownload(OpenFile f, final NetworkListener l) {
        Logger.LogDebug("OpenVFS.copyTo(" + f + ")");
        try {
            copyStreams(mFile.getContent().getInputStream(),
                    f.getOutputStream(),
                    true, true,
                    new ProgressUpdateListener() {

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public void onProgressUpdate(Integer... progress) {
                            l.OnNetworkCopyUpdate(progress);
                        }
                    });
            return true;
        } catch (Exception e) {
            if (l != null)
                l.OnNetworkFailure(this, f, e);
            return false;
        }
    }

    @Override
    public OpenNetworkPath[] getChildren() {
        return mChildren;
    }

}

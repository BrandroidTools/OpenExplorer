
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenNetworkPath.NetworkListener;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.fragments.TextEditorFragment;
import org.brandroid.openmanager.fragments.TextEditorFragment.FileLoadTask;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

public abstract class OpenNetworkPath extends OpenPath implements OpenPath.NeedsTempFile {
    /**
	 * 
	 */
    private static final long serialVersionUID = -3829590216951441869L;
    protected UserInfo mUserInfo;
    private int mServersIndex = -1;
    public static final JSch DefaultJSch = new JSch();
    public static int Timeout = 20000;
    protected String mName = null;
    protected int mPort = -1;

    public interface NetworkListener {
        public static final NetworkListener DefaultListener = new NetworkListener() {

            @Override
            public void OnNetworkFailure(OpenNetworkPath np, OpenFile dest, Exception e) {
                Logger.LogWarning("Network Failure for " + np);
            }

            @Override
            public void OnNetworkCopyUpdate(Integer... progress) {

            }

            @Override
            public void OnNetworkCopyFinished(OpenNetworkPath np, OpenFile dest) {
                Logger.LogDebug("Network Copy Finished for " + np + " \u2661 " + dest);
            }
        };

        public void OnNetworkCopyFinished(OpenNetworkPath np, OpenFile dest);

        public void OnNetworkCopyUpdate(Integer... progress);

        public void OnNetworkFailure(OpenNetworkPath np, OpenFile dest, Exception e);
    }

    public interface OpenAuthCallback {
        public void OnAuthenticate(String url);

        public void OnAuthenticated(OpenPath path);
    }

    @Override
    public Boolean canWrite() {
        return false; // disable network writing until we can get it fixed
    }

    @Override
    public final Boolean requiresThread() {
        return true;
    }

    public void connect() throws IOException {
        Logger.LogVerbose("Connecting OpenNetworkPath");
    }

    public void disconnect() {
        Logger.LogVerbose("Disconnecting OpenNetworkPath");
    }

    public String getTempFileName() {
        return getUri().getScheme() + "-" + getName() + "-"
                + Utils.md5(getPath()).replaceAll("[^A-Za-z0-9\\.]", "-");
    }

    public OpenFile getTempFile() {
        OpenFile root = OpenFile.getTempFileRoot();
        if (root != null)
            return root.getChild(getTempFileName());
        return null;
    }

    public OpenFile tempDownload(AsyncTask task) throws IOException {
        Logger.LogDebug("tempDownload() on " + getPath());
        OpenFile tmp = getTempFile();
        if (tmp == null)
            throw new IOException("Unable to download Temp file");
        if (!tmp.exists())
            tmp.create();
        else if (tmp.length() > 0 && lastModified() != null && tmp.lastModified() != null
                && lastModified() < tmp.lastModified()) {
            Logger.LogWarning("Remote file is older than local temp file.");
            return tmp;
        }
        copyTo(tmp, task);
        return tmp;
    }

    public void tempUpload(AsyncTask task) throws IOException {
        Logger.LogDebug("tempUpload() on " + getPath());
        OpenFile tmp = getTempFile();
        if (tmp == null)
            throw new IOException("Unable to download Temp file");
        if (!tmp.exists())
            tmp.create();
        else if (lastModified() != null && tmp.lastModified() != null
                && lastModified() >= tmp.lastModified()) {
            Logger.LogWarning("Remote file is newer than local temp file.");
            return;
        }
        copyFrom(tmp, task);
    }

    /**
     * Upload file (used during tempUpload).
     * 
     * @param f Local file to upload.
     * @param l Network listener for logging (since exceptions are caught).
     * @return True if transfer was successful, false otherwise.
     */
    public abstract boolean syncUpload(OpenFile f, NetworkListener l);

    /**
     * Download file (used during tempDownload).
     * 
     * @param f Local file to download to.
     * @param l Network listener for logging (since exceptions are caught).
     * @return True if transfer was successful, false otherwise.
     */
    public abstract boolean syncDownload(OpenFile f, NetworkListener l);

    public boolean copyFrom(OpenFile f, final AsyncTask task) {
        if (task == null)
            return syncUpload(f, OpenNetworkPath.NetworkListener.DefaultListener);
        return syncUpload(f, new NetworkListener() {
            public void OnNetworkFailure(OpenNetworkPath np, OpenFile dest, Exception e) {
                Logger.LogError("copyFrom: Network failure for " + np, e);
            }

            public void OnNetworkCopyUpdate(Integer... progress) {
                if (task instanceof TextEditorFragment.FileLoadTask)
                    ((TextEditorFragment.FileLoadTask)task).showProgress(progress);
                else if (task instanceof TextEditorFragment.FileSaveTask)
                    ((TextEditorFragment.FileSaveTask)task).showProgress(progress);
            }

            public void OnNetworkCopyFinished(OpenNetworkPath np, OpenFile dest) {
                if (task instanceof TextEditorFragment.FileLoadTask)
                    ((TextEditorFragment.FileLoadTask)task).showProgress();
                else if (task instanceof TextEditorFragment.FileSaveTask)
                    ((TextEditorFragment.FileSaveTask)task).showProgress();
            }
        });
    }

    public boolean copyTo(OpenFile f, final AsyncTask task) {
        return syncDownload(f, new NetworkListener() {
            public void OnNetworkFailure(OpenNetworkPath np, OpenFile dest, Exception e) {
                Logger.LogError("copyTo: Network failure for " + np, e);
            }

            public void OnNetworkCopyUpdate(Integer... progress) {
                if (task instanceof TextEditorFragment.FileLoadTask)
                    ((TextEditorFragment.FileLoadTask)task).showProgress(progress);
                else if (task instanceof TextEditorFragment.FileSaveTask)
                    ((TextEditorFragment.FileSaveTask)task).showProgress(progress);
            }

            public void OnNetworkCopyFinished(OpenNetworkPath np, OpenFile dest) {
                if (task instanceof TextEditorFragment.FileLoadTask)
                    ((TextEditorFragment.FileLoadTask)task).showProgress();
                else if (task instanceof TextEditorFragment.FileSaveTask)
                    ((TextEditorFragment.FileSaveTask)task).showProgress();
            }
        });
    }

    public abstract boolean isConnected() throws IOException;

    /**
     * This does not change the actual path of the underlying object, just what
     * is displayed to the user.
     * 
     * @param name New title for OpenPath object
     */
    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getName(String defaultName) {
        return mName != null ? mName : defaultName;
    }

    public final String getRemotePath() {
        String name = getName();
        if (!name.endsWith("/") && !name.startsWith("/"))
            name = "/" + name;
        return getUri().getPath().replace(name, "");
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public UserInfo setUserInfo(UserInfo info) {
        mUserInfo = info;
        return mUserInfo;
    }

    public int getServersIndex() {
        return mServersIndex;
    }

    public void setServersIndex(int index) {
        mServersIndex = index;
    }

    public abstract OpenNetworkPath[] getChildren();

    @Override
    public String toString() {
        return getName(super.toString());
    }

    public void setPort(int port) {
        mPort = port;
    }

    public int getPort() {
        return mPort;
    }

    @Override
    public String getDetails(boolean countHiddenChildren) {
        String deets = "";

        if (!isDirectory())
            deets += DialogHandler.formatSize(length());

        return deets;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return tempDownload(null).getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return tempDownload(null).getOutputStream();
    }
}

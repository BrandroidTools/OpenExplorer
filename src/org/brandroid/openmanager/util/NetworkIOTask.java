
package org.brandroid.openmanager.util;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.util.RootManager.UpdateCallback;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

public class NetworkIOTask extends AsyncTask<OpenPath, OpenPath, OpenPath[]> implements
        RootManager.UpdateCallback {
    private OpenPath[] params = null;
    private boolean isCancellable = false;
    private boolean instanceRunning = false;
    public static int instanceNumber = 0;
    private final OnTaskUpdateListener mListener;
    private final static Hashtable<String, NetworkIOTask> mFileTasks = new Hashtable<String, NetworkIOTask>();

    public final static Hashtable<String, NetworkIOTask> getTasks() {
        return mFileTasks;
    }

    public final static boolean isTaskRunning(String path) {
        return mFileTasks.containsKey(path) && mFileTasks.get(path).instanceRunning;
    }

    public final static void cancelTask(String path) {
        if (isTaskRunning(path)) {
            mFileTasks.get(path).doCancel();
            mFileTasks.remove(path);
        }
    }

    public final static void cancelAllTasks() {
        for (NetworkIOTask task : mFileTasks.values())
            task.doCancel(true);
        mFileTasks.clear();
    }

    public final static void addTask(String path, NetworkIOTask task) {
        if (!isTaskRunning(path))
            mFileTasks.put(path, task);
    }

    public interface OnTaskUpdateListener {
        public void setProgressVisibility(boolean visible);

        public void updateData(OpenPath[] result);

        public void addFiles(OpenPath[] files);
    }

    public NetworkIOTask(OnTaskUpdateListener listener) {
        mListener = listener;
    }

    @Override
    protected void onCancelled() {
        if (!isCancellable)
            return;
        instanceRunning = false;
        Logger.LogDebug("FileIOTask.onCancelled", new Exception());
        if (params != null)
            for (OpenPath path : params) {
                try {
                    if (path instanceof OpenNetworkPath && ((OpenNetworkPath)path).isConnected())
                        ((OpenNetworkPath)path).disconnect();
                } catch (IOException e) {
                }
                mFileTasks.remove(path.getPath());
            }
    }

    @Override
    protected void onCancelled(OpenPath[] result) {
        if (!isCancellable)
            return;
        onCancelled();
    }

    public void doCancel() {
        doCancel(true);
    }

    public void doCancel(boolean mayInterrupt) {
        isCancellable = true;
        cancel(mayInterrupt);
    }

    @Override
    protected OpenPath[] doInBackground(OpenPath... params) {
        if (instanceRunning) {
            Logger.LogWarning("Instance is already running!");
            return null;
        }
        instanceRunning = true;
        this.params = params;
        publishProgress();
        Logger.LogDebug("Beginning #" + (++instanceNumber) + ". Listing " + params[0].getPath());
        ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
        for (OpenPath path : params) {
            Logger.LogVerbose("FileIOTask on " + path.getPath());
            if (path.requiresThread()) {
                SimpleUserInfo info = new SimpleUserInfo();
                OpenServer server = null;
                if (path instanceof OpenNetworkPath) {
                    int si = ((OpenNetworkPath)path).getServersIndex();
                    if (si > -1)
                        server = OpenServers.DefaultServers.get(si);
                    if (server != null && server.getPassword() != null
                            && server.getPassword() != "")
                        info.setPassword(server.getPassword());
                }

                OpenPath cachePath = null;
                OpenPath[] list = null;
                boolean success = false;
                try {
                    cachePath = FileManager.getOpenCache(path.getPath(), true, null);
                    if (cachePath != null) {
                        if (cachePath instanceof OpenNetworkPath)
                            list = ((OpenNetworkPath)cachePath).getChildren();
                        if (list == null)
                            list = cachePath.listFiles();
                    }
                    success = list != null;
                } catch (SmbException ae) {
                    cachePath = path;
                    Uri uri = Uri.parse(cachePath.getPath());
                    ((OpenNetworkPath)cachePath).setUserInfo(info);
                    try {
                        list = cachePath.listFiles();
                        success = list != null;
                    } catch (IOException e3) {
                        Logger.LogError("Error listing SMB Files", e3);
                        // getExplorer().showToast(R.string.s_error_ftp);
                    }
                } catch (IOException e2) {
                    Logger.LogError("Couldn't get Cache", e2);
                    // if(getExplorer() != null)
                    // getExplorer().showToast(R.string.s_error_ftp);
                    cachePath = path;
                } catch (Exception e) {
                    Logger.LogError("Null 1?", e);
                }
                if (cachePath == null)
                    cachePath = path;
                if (cachePath instanceof OpenNetworkPath && !success
                        && ((OpenNetworkPath)cachePath).getUserInfo() == null) {
                    ((OpenNetworkPath)cachePath).setUserInfo(info);
                }
                if (!success || list == null || list.length == 0)
                    try {
                        list = cachePath.listFiles();
                        if (list != null)
                            FileManager.setOpenCache(cachePath.getPath(), cachePath);
                    } catch (SmbAuthException e) {
                        Logger.LogWarning("Couldn't connect to SMB using: "
                                + ((OpenSMB)cachePath).getFile().getCanonicalPath());
                    } catch (IOException e) {
                        Logger.LogWarning("Couldn't list from cachePath", e);
                    } catch (Exception e) {
                        Logger.LogError("Null?", e);
                    }
                if (list != null) {
                    for (OpenPath f : list)
                        ret.add(f);
                } else {
                    Logger.LogError("Why is list still null?");
                    // if(getExplorer() != null)
                    // getExplorer().showToast(R.string.s_error_ftp);
                }
            } else {
                try {
                    for (OpenPath f : path.listFiles())
                        ret.add(f);
                } catch (IOException e) {
                    Logger.LogError("IOException listing children inside FileIOTask", e);
                }
            }
            // if(path instanceof OpenFTP)
            // ((OpenFTP)path).getManager().disconnect();
            // else if(path instanceof OpenNetworkPath)
            // ((OpenNetworkPath)path).disconnect();
            // getManager().pushStack(path);
        }
        Logger.LogDebug("NetworkIOTask found " + ret.size() + " items.");
        instanceRunning = false;
        return ret.toArray(new OpenPath[ret.size()]);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // mData.clear();
        mListener.setProgressVisibility(true);
    }

    @Override
    protected void onProgressUpdate(OpenPath... values) {
        super.onProgressUpdate(values);
        mListener.setProgressVisibility(true);
        mListener.addFiles(values);
    }

    @Override
    protected void onPostExecute(final OpenPath[] result) {
        instanceRunning = false;
        if (params.length > 0)
            mFileTasks.remove(params[0].getPath());
        if (result.length > 0) {
            final OpenPath mPath = result[0];
            if (OpenPath.AllowDBCache && mPath != null)
                new Thread(new Runnable() {
                    public void run() {
                        long start = new Date().getTime();
                        int dels = 0, adds = 0;
                        dels = params[0].deleteFolderFromDb();
                        OpenPathDbAdapter db = OpenPath.getDb();
                        if (db != null)
                            adds += db.createItem(result);
                        else
                            for (OpenPath path : result)
                                if (path != null)
                                    if (mPath.addToDb())
                                        adds++;
                        Logger.LogVerbose("Finished updating OpenPath DB Cache" + "(-" + dels
                                + ",+" + adds + ") in " + ((new Date().getTime() - start) / 1000)
                                + " seconds for " + params[0].getPath());
                        OpenPath.closeDb();
                    }
                }).start();
            mListener.updateData(result);
        }
        mListener.setProgressVisibility(false);
        // onCancelled(result);
        // mData2.clear();
    }

    @Override
    public void onUpdate() {
        Logger.LogDebug("onUpdate");
    }

    @Override
    public boolean onReceiveMessage(String msg) {
        Logger.LogDebug("Message Received: " + msg);
        if (msg.indexOf("\n") > -1)
            return true;
        else
            return false;
    }

    @Override
    public void onExit() {
        Logger.LogDebug("onExit");
    }

}

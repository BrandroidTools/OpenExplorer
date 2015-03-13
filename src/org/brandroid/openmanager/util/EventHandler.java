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

import SevenZip.ArchiveExtractCallback;
import SevenZip.HRESULT;
import SevenZip.Handler;
import SevenZip.IArchiveExtractCallback;
import SevenZip.IInArchive;
import SevenZip.MyRandomAccessFile;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.support.v4.app.NotificationCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.view.View;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.contrapunctus.lzma.LzmaInputStream;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.BluetoothActivity;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenLZMA;
import org.brandroid.openmanager.data.OpenLZMA.OpenLZMAEntry;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenNetworkPath.Cancellable;
import org.brandroid.openmanager.data.OpenNetworkPath.CloudDeleteListener;
import org.brandroid.openmanager.data.OpenNetworkPath.CloudOpsHandler;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath.OpenStream;
import org.brandroid.openmanager.data.OpenRAR;
import org.brandroid.openmanager.data.OpenRAR.OpenRAREntry;
import org.brandroid.openmanager.data.OpenTar.OpenTarEntry;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.data.OpenPath.OpenPathCopyable;
import org.brandroid.openmanager.data.OpenZip;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.FileManager.OnProgressUpdateCallback;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;
import org.itadaki.bzip2.BZip2InputStream;
import org.itadaki.bzip2.BZip2OutputStream;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarOutputStream;
import org.kamranzafar.jtar.TarUtils;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.jcraft.jzlib.GZIPOutputStream;

@SuppressWarnings({
        "unchecked", "rawtypes"
})
@SuppressLint("NewApi")
public class EventHandler {
    public static final EventType SEARCH_TYPE = EventType.SEARCH;
    public static final EventType COPY_TYPE = EventType.COPY;
    public static final EventType ZIP_TYPE = EventType.ZIP;
    public static final EventType DELETE_TYPE = EventType.DELETE;
    public static final EventType RENAME_TYPE = EventType.RENAME;
    public static final EventType MKDIR_TYPE = EventType.MKDIR;
    public static final EventType CUT_TYPE = EventType.CUT;
    public static final EventType TOUCH_TYPE = EventType.TOUCH;
    public static final EventType ERROR_TYPE = EventType.ERROR;
    public static final EventType EXTRACT_TYPE = EventType.EXTRACT;
    public static final int BACKGROUND_NOTIFICATION_ID = 123;
    private static final boolean ENABLE_MULTITHREADS = false; // !OpenExplorer.BEFORE_HONEYCOMB;

    static final int TAR_BUFFER = 2048;

    public enum EventType {
        SEARCH, COPY, CUT, DELETE, RENAME, MKDIR, TOUCH, EXTRACT, ZIP, ERROR
    }

    public enum CompressionType {
        ZIP, TAR, GZ, BZ2, LZMA, RAR
    }

    public static boolean SHOW_NOTIFICATION_STATUS = !OpenExplorer.isBlackBerry()
            && Build.VERSION.SDK_INT >= 8;

    private static NotificationManager mNotifier = null;
    private static int EventCount = 0;
    public static CompressionType DefaultCompressionType = CompressionType.ZIP;

    private OnWorkerUpdateListener mThreadListener;
    private TaskChangeListener mTaskListener;
    private FileManager mFileMang;

    private static ArrayList<BackgroundWork> mTasks = new ArrayList<BackgroundWork>();

    public static ArrayList<BackgroundWork> getTaskList() {
        return mTasks;
    }

    public static BackgroundWork[] getRunningTasks() {
        ArrayList<BackgroundWork> ret = new ArrayList<EventHandler.BackgroundWork>();
        for (BackgroundWork bw : mTasks)
            if (bw.getStatus() != Status.FINISHED)
                ret.add(bw);
        return ret.toArray(new BackgroundWork[ret.size()]);
    }

    public static boolean hasRunningTasks() {
        for (BackgroundWork bw : mTasks)
            if (bw.getStatus() == Status.RUNNING)
                return true;
        return false;
    }

    public static void cancelRunningTasks() {
        for (BackgroundWork bw : mTasks)
        {
        	Logger.LogVerbose("Cancelling " + bw.getTitle());
            if (bw.getStatus() == Status.RUNNING)
                bw.cancel(true);
        }
        if (mNotifier != null)
            mNotifier.cancelAll();
    }

    public void setTaskChangeListener(TaskChangeListener l) {
        mTaskListener = l;
    }

    public interface TaskChangeListener {
        public void OnTasksChanged(int taskCount);
    }

    public interface OnWorkerUpdateListener {
        public void onWorkerThreadComplete(EventType type, String... results);

        public void onWorkerProgressUpdate(int pos, int total);

        /**
         * Occurs when an error occurs during Event
         * 
         * @param type Type of requested event.
         * @param files List of OpenPath items.
         */
        public void onWorkerThreadFailure(EventType type, OpenPath... files);
    }

    private synchronized void OnWorkerProgressUpdate(int pos, int total) {
        if (mThreadListener == null)
            return;
        mThreadListener.onWorkerProgressUpdate(pos, total);
    }

    private synchronized void OnWorkerThreadComplete(EventType type, String... results) {
        if (mThreadListener != null)
            mThreadListener.onWorkerThreadComplete(type, results);
        if (mTaskListener != null)
            mTaskListener.OnTasksChanged(getTaskList().size());
    }

    private synchronized void OnWorkerThreadFailure(EventType type, OpenPath... files) {
        if (mThreadListener == null)
            return;
        mThreadListener.onWorkerThreadFailure(type, files);
        mThreadListener = null;
        if (mTaskListener != null)
            mTaskListener.OnTasksChanged(getTaskList().size());
    }

    public void setUpdateListener(OnWorkerUpdateListener e) {
        mThreadListener = e;
    }

    public EventHandler(FileManager filemanager) {
        mFileMang = filemanager;
    }

    public static String getResourceString(Context mContext, int... resIds) {
        String ret = "";
        for (int resId : resIds)
            ret += ("".equals(ret) ? "" : " ") + mContext.getText(resId);
        return ret;
    }

    private static int binarySearch(String[] array, String key)
    {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(key))
                return i;
        return -1;
    }

    public void deleteFile(final OpenPath file, final OpenApp mApp, boolean showConfirmation) {
        Collection<OpenPath> files = new ArrayList<OpenPath>();
        files.add(file);
        deleteFile(files, mApp, showConfirmation);
    }

    public void deleteFile(final Collection<OpenPath> path, final OpenApp mApp,
            boolean showConfirmation) {
        final OpenPath[] files = path.toArray(new OpenPath[path.size()]);
        String name;
        final Context mContext = mApp.getContext();

        if (files.length == 1)
            name = files[0].getName();
        else
            name = files.length + " " + getResourceString(mContext, R.string.s_files);

        if (!showConfirmation) {
            execute(new BackgroundWork(DELETE_TYPE, mContext, null), files);
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle(getResourceString(mContext, R.string.s_menu_delete) + " " + name)
                .setMessage(mContext.getString(R.string.s_alert_confirm_delete, name))
                .setIcon(R.drawable.ic_menu_delete)
                .setPositiveButton(getResourceString(mContext, R.string.s_menu_delete),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                execute(new BackgroundWork(DELETE_TYPE, mContext, null), files);
                            }
                        })
                .setNegativeButton(getResourceString(mContext, R.string.s_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
    }

    public void startSearch(final OpenPath base, final Context mContext) {
        final InputDialog dSearch = new InputDialog(mContext).setIcon(R.drawable.search)
                .setTitle(R.string.s_search).setCancelable(true)
                .setMessageTop(R.string.s_prompt_search_within).setDefaultTop(base.getPath())
                .setMessage(R.string.s_prompt_search);
        AlertDialog alert = dSearch.setPositiveButton(R.string.s_search, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                searchFile(new OpenFile(dSearch.getInputTopText()), dSearch.getInputText(),
                        mContext);
            }
        }).setNegativeButton(R.string.s_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        alert.setInverseBackgroundForced(true);
        alert.show();
    }

    public void renameFile(final OpenPath path, boolean isFolder, final Context mContext) {
        final InputDialog dRename = new InputDialog(mContext).setIcon(R.drawable.ic_rename)
                .setTitle(R.string.s_menu_rename).setCancelable(true)
                .setMessage(R.string.s_alert_rename).setDefaultText(path.getName());
        dRename.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String newName = dRename.getInputText().toString();
                BackgroundWork work = new BackgroundWork(RENAME_TYPE, mContext, path, newName);
                if (newName.length() > 0) {
                    execute(work, path);
                } else
                    dialog.dismiss();
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void sendFile(final Collection<OpenPath> path, final Context mContext) {
        String name;
        CharSequence[] list = {
                "Bluetooth", "Email"
        };
        final OpenPath[] files = new OpenPath[path.size()];
        path.toArray(files);
        final int num = path.size();

        if (num == 1)
            name = files[0].getName();
        else
            name = path.size() + " " + getResourceString(mContext, R.string.s_files) + ".";

        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle(
                getResourceString(mContext, R.string.s_title_send).toString().replace("xxx", name))
                .setIcon(R.drawable.bluetooth).setItems(list, new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent bt = new Intent(mContext, BluetoothActivity.class);

                                bt.putExtra("paths", files);
                                mContext.startActivity(bt);
                                break;

                            case 1:
                                ArrayList<Uri> uris = new ArrayList<Uri>();
                                Intent mail = new Intent();
                                mail.setType("application/mail");

                                if (num == 1) {
                                    mail.setAction(android.content.Intent.ACTION_SEND);
                                    mail.putExtra(Intent.EXTRA_STREAM, files[0].getUri());
                                    mContext.startActivity(mail);
                                    break;
                                }

                                for (int i = 0; i < num; i++)
                                    uris.add(files[i].getUri());

                                mail.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
                                mail.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                mContext.startActivity(mail);
                                break;
                        }
                    }
                }).create().show();
    }

    public void copyFile(OpenPath source, OpenPath destPath, Context mContext) {
        Collection<OpenPath> files = new ArrayList<OpenPath>();
        files.add(source);
        copyFile(files, destPath, mContext);
    }
    
    public BackgroundWork getWorker(EventType type, Context context, OpenPath intoPath, String... params)
    {
    	return new BackgroundWork(type, context, intoPath, params);
    }

    public void copyFile(final Collection<OpenPath> files, final OpenPath newPath,
            final Context mContext) {
        copyFile(files, newPath, mContext, true);
    }

    public void copyFile(final Collection<OpenPath> files, final OpenPath newPath,
            final Context mContext, final boolean copyOnly) {
        // for (OpenPath file : files)
        // copyFile(file, newPath.getChild(file.getName()), mContext);
        final EventType type = copyOnly ? COPY_TYPE : CUT_TYPE;
        for (final OpenPath file : files.toArray(new OpenPath[files.size()]))
        {
            if (checkDestinationExists(file, newPath, mContext, type))
                files.remove(file);
            else
                execute(new BackgroundWork(type,
                        mContext, newPath), file);
        }
    }

    private boolean checkDestinationExists(final OpenPath file, final OpenPath newPath,
            final Context mContext, final EventType type)
    {
        final OpenPath newFile = newPath.getChild(file.getName());
        if (newFile != null && newFile.exists())
        {
            DialogHandler.showMultiButtonDialog(mContext,
                    getResourceString(mContext, R.string.s_alert_destination_exists),
                    getResourceString(mContext, R.string.s_title_copying)
                            + " " + file.getName(),
                    new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case R.string.s_menu_rename:
                                    OpenPath destFile = newFile;
                                    int i = 1;
                                    while (destFile.exists())
                                        destFile = newPath.getChild(
                                                file.getName() + " (" + i++ + ")");
                                    showRenameOnCopyDialog(file, destFile, mContext);
                                    break;
                                case R.string.s_overwrite:
                                    execute(new BackgroundWork(type, mContext, newPath,
                                            file.getName()), file);
                                    break;
                            }
                            try {
                                dialog.dismiss();
                            } catch (Exception e) {
                                Logger.LogWarning(
                                        "Unable to cancel copyFile dialog.", e);
                            }
                        }
                    },
                    R.string.s_overwrite, R.string.s_skip, R.string.s_menu_rename);
            return true;
        }
        return false;
    }

    private void showRenameOnCopyDialog(final OpenPath sourceFile, final OpenPath destFile,
            final Context mContext)
    {
        final InputDialog dlg = new InputDialog(mContext)
                .setTitle(R.string.s_menu_rename)
                .setDefaultText(destFile.getName());
        dlg.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                OpenPath newDest = destFile.getParent().getChild(dlg.getInputText());
                newDest.touch();
                execute(new BackgroundWork(COPY_TYPE, mContext, newDest, newDest.getPath()),
                        sourceFile);

                dialog.dismiss();
            }
        }).setNegativeButton(android.R.string.no, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dlg.create().show();
    }

    public static AsyncTask execute(AsyncTask job) {
        if (OpenExplorer.BEFORE_HONEYCOMB)
            job.execute();
        else
            job.executeOnExecutor(getExecutor());
        return job;
    }

    private static Executor getExecutor() {
        if (ENABLE_MULTITHREADS)
            return AsyncTask.THREAD_POOL_EXECUTOR;
        else
            return AsyncTask.SERIAL_EXECUTOR;
    }

    public static AsyncTask execute(AsyncTask job, OpenFile... params) {
        if (OpenExplorer.BEFORE_HONEYCOMB)
            job.execute((Object[])params);
        else
            job.executeOnExecutor(getExecutor(), (Object[])params);
        return job;
    }

    public static AsyncTask<OpenPath, Integer, Integer> execute(
            AsyncTask<OpenPath, Integer, Integer> job, OpenPath... params) {
        if (OpenExplorer.BEFORE_HONEYCOMB)
            job.execute(params);
        else
            job.executeOnExecutor(getExecutor(), params);
        return job;
    }

    public static NetworkIOTask executeNetwork(NetworkIOTask job, OpenPath... params) {
        if (OpenExplorer.BEFORE_HONEYCOMB)
            job.execute(params);
        else
            job.executeOnExecutor(getExecutor(), params);
        return job;
    }

    public static AsyncTask execute(AsyncTask job, String... params) {
        if (OpenExplorer.BEFORE_HONEYCOMB)
            job.execute((Object[])params);
        else
            job.executeOnExecutor(getExecutor(), (Object[])params);
        return job;
    }

    public void cutFile(Collection<OpenPath> files, OpenPath newPath, Context mContext) {
        for (OpenPath file : files)
            if (!checkDestinationExists(file, newPath, mContext, CUT_TYPE))
                execute(new BackgroundWork(CUT_TYPE, mContext, newPath), file);
    }

    public void searchFile(OpenPath dir, String query, Context mContext) {
        execute(new BackgroundWork(SEARCH_TYPE, mContext, dir, query), dir);
    }

    public BackgroundWork zipFile(OpenPath into, Collection<OpenPath> files, Context mContext) {
        return zipFile(into, files, mContext, DefaultCompressionType);
    }

    public BackgroundWork zipFile(OpenPath into, Collection<OpenPath> files, Context mContext,
            CompressionType type) {
        return zipFile(into, files.toArray(new OpenPath[files.size()]), mContext, type);
    }

    public BackgroundWork zipFile(OpenPath into, OpenPath[] files, Context mContext,
            CompressionType type) {
        BackgroundWork bw = new BackgroundWork(ZIP_TYPE, mContext, into);
        bw.setCompressionType(type);
        return (BackgroundWork)execute(bw, files);
    }

    public void extractSet(final OpenPath file, final OpenPath dest, final Context mContext,
            final String... includes)
    {
        execute(new BackgroundWork(EXTRACT_TYPE, mContext, dest, includes), file);
    }

    /*
     * public void unZipFileTo(OpenPath zipFile, OpenPath toDir, Context
     * mContext) { new BackgroundWork(UNZIPTO_TYPE, mContext,
     * toDir).execute(zipFile); }
     */

    public static void createNewFile(final OpenPath folder, final String filename,
	        final OnWorkerUpdateListener threadListener) {
	    new Thread(new Runnable() {
	        public void run() {
	            if (folder.getChild(filename).touch())
	                threadListener.onWorkerThreadComplete(TOUCH_TYPE);
	            else
	                threadListener.onWorkerThreadFailure(TOUCH_TYPE);
	        }
	    }).start();
	    // BackgroundWork bw = new BackgroundWork(TOUCH_TYPE, context, folder,
	    // filename);
	    // bw.execute();
	}

	public static boolean createNewFolder(OpenPath folder, String folderName, Context context) {
	    return folder.getChild(folderName).mkdir();
	}

	public class BackgroundWork extends AsyncTask<OpenPath, Integer, Integer> implements
            OnProgressUpdateCallback {
        private final EventType mType;
        private final Context mContext;
        private final String[] mInitParams;
        private final OpenPath mIntoPath;
        private ProgressDialog mPDialog;
        private Notification mNote = null;
        private ArrayList<String> mSearchResults = null;
        private boolean isDownload = false;
        private boolean isCancellable = true;
        private int taskId = -1;
        private final Date mStart;
        private long mLastRate = 0;
        private long mElapsed = 0l;
        private long mRemain = 0l;
        private int mTotalCount = 0;
        private int mCurrentIndex = 0;
        private OpenPath mCurrentPath;
        private boolean notifReady = false;
        private final int[] mLastProgress = new int[3];
        private int notifIcon;
        private CompressionType mCompressType = CompressionType.ZIP;
        private Cancellable mCloudCancellor;

        private OnWorkerUpdateListener mListener;

        public void setWorkerUpdateListener(OnWorkerUpdateListener listener) {
            mListener = listener;
        }
        
        public OnWorkerUpdateListener getWorkerUpdateListener() { return mListener; }

        public void OnWorkerThreadComplete(EventType type, String... results) {
            if (mListener != null)
                mListener.onWorkerThreadComplete(type, results);
            EventHandler.this.OnWorkerThreadComplete(type, results);
        }

        public void OnWorkerProgressUpdate(int pos, int total) {
            if (mListener != null)
                mListener.onWorkerProgressUpdate(pos, total);
            EventHandler.this.OnWorkerProgressUpdate(pos, total);
        }

        public BackgroundWork(EventType type, Context context, OpenPath intoPath, String... params) {
//            if(OpenExplorer.IS_DEBUG_BUILD)
//                Logger.LogVerbose("EventHandler.BackgroundWork: " + type.toString() + ", " + intoPath.getAbsolutePath() + ", " + Utils.joinArray(params, "-"));
            mType = type;
            mContext = context;
            mInitParams = params;
            mIntoPath = intoPath;
            if (mNotifier == null)
                mNotifier = (NotificationManager)context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            taskId = mTasks.size();
            mStart = new Date();
            mTasks.add(this);
            if (mTaskListener != null)
                mTaskListener.OnTasksChanged(getRunningTasks().length);
        }

        public void setCompressionType(CompressionType type)
        {
            mCompressType = type;
        }

        public String getOperation() {
            switch (mType) {
                case DELETE:
                    return getResourceString(mContext, R.string.s_title_deleting).toString();
                case SEARCH:
                    return getResourceString(mContext, R.string.s_title_searching).toString();
                case COPY:
                	if(mIntoPath != null && mIntoPath instanceof OpenNetworkPath)
                		return getResourceString(mContext, R.string.s_title_uploading).toString();
                	else if (mCurrentPath != null && mCurrentPath instanceof OpenNetworkPath)
                		return getResourceString(mContext,  R.string.s_title_downloading).toString();
                    return getResourceString(mContext, R.string.s_title_copying).toString();
                case CUT:
                    return getResourceString(mContext, R.string.s_title_moving).toString();
                case EXTRACT:
                    return getResourceString(mContext, R.string.s_extracting).toString();
                case ZIP:
                    return getResourceString(mContext, R.string.s_title_zipping).toString();
                case MKDIR:
                    return getResourceString(mContext, R.string.s_menu_rename).toString();
                case TOUCH:
                    return getResourceString(mContext, R.string.s_create).toString();
            }
            return getResourceString(mContext, R.string.s_title_executing);
        }

        public String getTitle() {
            String title = getOperation();
            if (mCurrentPath != null) {
                title += " " + mCurrentPath.getName();
            }
            return title;
        }

        public String getSubtitle() {
            String ret = "";
            if (mTotalCount > 1)
                ret += "(" + (mCurrentIndex + 1) + "/" + mTotalCount + ") ";
            if (mIntoPath != null)
                ret += '\u2192' + " " + mIntoPath;
            return ret;
        }

        public String getLastRate() {
            return getLastRate(true);
        }

        public String getLastRate(Boolean auto) {
            if (getStatus() == Status.FINISHED)
                return getResourceString(mContext, R.string.s_complete);
            if (!auto) {
                if (mLastRate > 0)
                    return getResourceString(mContext, R.string.s_status_rate)
                            + OpenPath.formatSize(mLastRate).replace(" ", "").toLowerCase()
                            + "/s";
                else
                    return "";
            }
            if (getStatus() == Status.FINISHED)
                return getResourceString(mContext, R.string.s_complete);
            else if (getStatus() == Status.PENDING)
                return getResourceString(mContext, R.string.s_pending);
            if (isCancelled())
                return getResourceString(mContext, R.string.s_cancelled);
            if (mRemain > 0) {
                return getTimeRemaining();
            } else
                return getLastRate(false);
        }

        public String getTimeElapsed() {
            Integer min = (int)(mElapsed / 60000);
            Integer sec = (int)((mElapsed / 1000) % 60);
            if (min > 0)
                return min + ":" + (sec < 10 ? "0" : "") + sec;
            else
                return sec + "s";
        }

        public String getTimeRemaining() {
            return getTimeRemaining(true);
        }

        public String getTimeRemaining(boolean title) {
            if (mRemain <= 0)
                return "";
            Integer min = (int)(mRemain / 60);
            Integer sec = (int)(mRemain % 60);
            return (title ? getResourceString(mContext, R.string.s_status_remaining) : "")
                    + (min > 15 ? ">15m" : (min > 0 ? min + ":" : "")
                            + (min > 0 && sec < 10 ? "0" : "") + sec + (min > 0 ? "" : "s"));
        }

        @Override
        protected void onCancelled() {
            if (mCloudCancellor != null)
                mCloudCancellor.cancel();
            mNotifier.cancel(BACKGROUND_NOTIFICATION_ID);
            super.onCancelled();
            mTasks.remove(this);
        }

        @Override
        protected void onCancelled(Integer result) {
            mNotifier.cancel(BACKGROUND_NOTIFICATION_ID);
            super.onCancelled(result);
            mTasks.remove(this);
        }

        protected void onPreExecute() {
            boolean showDialog = true, showNotification = false;
            isCancellable = true;
            notifIcon = R.drawable.icon;
            switch (mType) {
                case DELETE:
                    showDialog = false;
                    break;
                case SEARCH:
                    notifIcon = android.R.drawable.ic_menu_search;
                    break;
                case COPY:
                    if (mIntoPath.requiresThread())
                        notifIcon = android.R.drawable.stat_sys_upload;
                    notifIcon = R.drawable.ic_menu_copy;
                    showDialog = false;
                    showNotification = true;
                    break;
                case CUT:
                    notifIcon = R.drawable.ic_menu_cut;
                    showDialog = false;
                    showNotification = true;
                    break;
                case ZIP:
                    showDialog = true;
                    showNotification = true;
                    break;
                case EXTRACT:
                    showDialog = true;
                    showNotification = true;
                    isCancellable = false;
                    break;
                default:
                    showDialog = showNotification = false;
                    break;
            }
            Logger.LogVerbose("Showing notification for " + getTitle());
            if (showDialog)
                try {
                    mPDialog = ProgressDialog.show(mContext, getTitle(),
                            getResourceString(mContext, R.string.s_title_wait).toString(), false,
                            isCancellable, new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface dialog) {
                                    cancelRunningTasks();
                                }
                            });
                } catch (Exception e) {
                }
            if (showNotification) {
                prepareNotification(notifIcon, isCancellable);
            }
        }

        public int getNotifIconResId() {
            if (isCancelled())
                return R.drawable.ic_action_remove_holo_dark;
            if (getStatus() == Status.FINISHED)
                return R.drawable.btn_check_on_holo_blue;
            return notifIcon;
        }

        public CharSequence getDetailedText() {
            String ret = "";
            ret += "Source: " + mCurrentPath.getParent() + "\n";
            ret += "Destination: " + mIntoPath + "\n";
            if (mLastProgress.length > 2 && mLastProgress[0] > 0 && mLastProgress[1] > 0) {
                ret += "Progress: " + OpenPath.formatSize(mLastProgress[0]) + " / "
                        + OpenPath.formatSize(mLastProgress[1]) + " ";
                ret += "(" + getLastRate(false) + ")\n";
            }
            ret += getResourceString(mContext, R.string.s_status_remaining) + " ";
            ret += getTimeElapsed();
            if (mRemain > 0)
                ret += " [" + getTimeRemaining(false) + "]";
            return ret;
        }

        @SuppressLint("NewApi")
        public Notification prepareNotification(int notifIcon, boolean isCancellable,
                Integer... values) {
            boolean showProgress = true;
            try {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                        .setContentTitle(getTitle())
                        .setContentText(getSubtitle())
                        .setLargeIcon(
                                BitmapFactory.decodeResource(mContext.getResources(),
                                        R.drawable.icon)).setSmallIcon(notifIcon);
                int progA = 0;
                if (showProgress) {
                    if (mLastProgress[1] > 0)
                        progA = (int)(((float)mLastProgress[0] / (float)mLastProgress[1]) * 1000f);
                    if (SHOW_NOTIFICATION_STATUS) {
                        mBuilder.setProgress(1000, progA, values.length == 0);
                        mBuilder.setNumber(progA / 10);
                    }
                } else {
                    mBuilder.setTicker(getTitle());
                }
                mBuilder.setContentIntent(makePendingIntent(
                        OpenExplorer.REQ_EVENT_VIEW));
                // mBuilder.setOnlyAlertOnce(true);
                mBuilder.setAutoCancel(true);
                NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
                style.bigText(getDetailedText());
                style.setBigContentTitle(getTitle());
                mBuilder.setStyle(style);
                if(isCancellable)
                	mBuilder.addAction(R.drawable.ic_menu_close_clear_cancel,
                                mContext.getResources().getText(R.string.s_cancel),
                                makePendingIntent(OpenExplorer.REQ_EVENT_CANCEL));
                mBuilder.addAction(R.drawable.ic_menu_info_details,
                                mContext.getText(R.string.s_menu_info),
                                makePendingIntent(OpenExplorer.REQ_EVENT_VIEW));
                if (Build.VERSION.SDK_INT < 11) {
                    RemoteViews noteView = new RemoteViews(mContext.getPackageName(),
                            R.layout.notification);
                    noteView.setTextViewText(android.R.id.title, getTitle());
                    noteView.setTextViewText(android.R.id.text2, getSubtitle());
                    noteView.setTextViewText(android.R.id.text1, getLastRate());
                    if (values.length == 0 && isDownload)
                        noteView.setImageViewResource(android.R.id.icon,
                                android.R.drawable.stat_sys_download);
                    else
                        noteView.setImageViewResource(android.R.id.icon,
                                android.R.drawable.stat_notify_sync);
                    noteView.setProgressBar(android.R.id.progress, 1000, progA, values.length == 0);
                    mBuilder.setContent(noteView);
                }
                mNote = mBuilder.build();
                notifReady = true;
            } catch (Exception e) {
                Logger.LogWarning("Couldn't post notification", e);
            }
            return mNote;
        }

        private PendingIntent makePendingIntent(int reqIntent) {
            Intent intent = new Intent(mContext, OpenExplorer.class);
            intent.putExtra("TaskId", taskId);
            intent.putExtra("RequestId", reqIntent);
            return PendingIntent.getActivity(mContext, reqIntent, intent, 0);
        }

        public void searchDirectory(OpenPath dir, String pattern, ArrayList<String> aList) {
            try {
                for (OpenPath p : dir.listFiles())
                    if (p.getName().matches(pattern))
                        aList.add(p.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                for (OpenPath p : dir.list())
                    if (p.isDirectory())
                        searchDirectory(p, pattern, aList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected Integer doInBackground(OpenPath... params) {
            //Logger.LogDebug("Starting Op!");
            mTotalCount = params.length;
            int ret = 0;

            mCurrentPath = params[0];

            switch (mType) {

                case DELETE:
                    for (int i = 0; i < mTotalCount; i++)
                    {
                        if(checkCloudDelete(params[i]))
                            ret++;
                        else
                            ret += mFileMang.deleteTarget(params[i]);
                    }
                    break;
                case SEARCH:
                    mSearchResults = new ArrayList<String>();
                    searchDirectory(mIntoPath, mInitParams[0], mSearchResults);
                    break;
                case RENAME:
                    OpenPath old = mIntoPath;
                    if (old instanceof OpenMediaStore)
                        old = ((OpenMediaStore)mIntoPath).getFile();
                    if (old instanceof OpenFile)
                        ret += FileManager.renameTarget((OpenFile)old, mInitParams[0]) ? 1 : 0;
                    break;
                case MKDIR:
                    for (OpenPath p : params)
                        ret += p.mkdir() ? 1 : 0;
                    break;
                case TOUCH:
                    for (OpenPath p : params)
                        ret += p.touch() ? 1 : 0;
                    break;
                case COPY:
                    for (int i = 0; i < params.length; i++) {
                        mCurrentIndex = i;
                        mCurrentPath = params[i];
                        if(OpenExplorer.IS_DEBUG_BUILD)
                            Logger.LogVerbose("EventHandler.BackgroundWork.doInBackground: " + BackgroundWork.this.mType.toString() + ", " + mIntoPath.getAbsolutePath() + ", " + Utils.joinArray(mInitParams, "-") + " --> " + mCurrentPath.getAbsolutePath());
                        if (mCurrentPath.requiresThread())
                            isDownload = true;
                        publishProgress();
                        try {
                            if (copyToDirectory(mCurrentPath, mIntoPath, 0))
                                ret++;
                        } catch (IOException e) {
                            Logger.LogError("Couldn't copy file (" + mCurrentPath.getName()
                                    + " to " + mIntoPath.getPath() + ")", e);
                        }
                    }
                    break;
                case CUT:
                    for (int i = 0; i < params.length; i++) {
                        mCurrentIndex = i;
                        mCurrentPath = params[i];
                        if (mCurrentPath.requiresThread())
                            isDownload = true;
                        publishProgress();
                        try {
                            if (copyToDirectory(mCurrentPath, mIntoPath, 0)) {
                                ret++;
                                mFileMang.deleteTarget(mCurrentPath);
                            }
                        } catch (IOException e) {
                            Logger.LogError("Couldn't copy file (" + mCurrentPath.getName()
                                    + " to " + mIntoPath.getPath() + ")", e);
                        }
                    }
                    break;

                case EXTRACT:
                    if (params[0] instanceof OpenStream)
                    {
                        int x = extractFiles(params[0], mIntoPath, mInitParams);
                        if (x > 0
                                && new Preferences(mContext).getBoolean("global",
                                        "pref_archive_postdelete", false))
                            params[0].delete();
                        ret += x;
                    }
                    break;

                case ZIP:
                    int x = compressFiles(mIntoPath, params);
                    if (x > 0
                            && new Preferences(mContext).getBoolean("global",
                                    "pref_archive_postdelete", false))
                        for (OpenPath p : params)
                            p.delete();
                    ret += x;
                    break;

            }

            return ret;
        }

        protected int compressFiles(OpenPath mArchive, OpenPath... files)
        {
            switch (mCompressType)
            {
                case GZ:
                case BZ2:
                case TAR:
                    OpenStream fs = (OpenStream)mArchive;
                    OutputStream os = null;
                    int ret = 0;
                    try {
                        mTotalCount = files.length;
                        os = new BufferedOutputStream(fs.getOutputStream());
                        if (mCompressType == CompressionType.GZ)
                            os = new GZIPOutputStream(os);
                        else if (mCompressType == CompressionType.BZ2)
                            os = new BZip2OutputStream(os);
                        if (files.length > 1)
                            os = new TarOutputStream(os);
                        if (files.length == 1)
                        {
                            mTotalCount = (int)files[0].length();
                            InputStream is = new BufferedInputStream(
                                    ((OpenStream)files[0]).getInputStream());
                            copyStreams(is, os, true, false);
                        } else {
                            mTotalCount = 0;
                            for (OpenPath file : files)
                                mTotalCount += file.length();
                            for (OpenPath file : files)
                            {
                                ((TarOutputStream)os).putNextEntry(
                                        new TarEntry(((OpenFile)file).getFile(), file.getName()));
                                InputStream is = new BufferedInputStream(
                                        ((OpenStream)file).getInputStream());
                                copyStreams(is, os, true, false);
                                // os.write(((OpenFile)file).readBytes());
                            }
                        }
                    } catch (IOException e) {
                        Logger.LogError("Unable to compress files!", e);
                        return -1;
                    } finally {
                        closeStream(os);
                    }
                    return 1;
                case ZIP:
                default:
                    mFileMang.setProgressListener(this);
                    publishProgress();
                    mFileMang.createZipFile(mIntoPath, files);
                    return mTotalCount;
            }
        }

        private void copyStreams(InputStream in, OutputStream out, boolean doCloseInput,
                boolean doCloseOutput) throws IOException {
            byte[] buffer = new byte[2048];
            int count = 0;
            int pos = 0;
            while ((count = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, count);
                pos += count;
                onProgressUpdateCallback(pos, mTotalCount);
            }
            if (doCloseInput)
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e) {
                }
            if (doCloseOutput)
                try {
                    if (out != null)
                        out.close();
                } catch (Exception e) {
                }
        }

        protected int extractFiles(OpenPath file, OpenPath into, String... includes) {
            int ret = 0;
            if (file.getMimeType().contains("rar") &&
                    (ret = extractRarFiles(new OpenRAR((OpenFile)file), into)) > 0)
                return ret;
            if ((file.getMimeType().contains("7z") || file.getMimeType().contains("lzma")) &&
                    (ret = extractLZMAFiles((OpenStream)file, into, includes)) > 0)
                return ret;
            if (file.getMimeType().contains("gz") &&
                    (ret = extractGZip(file, into)) > 0)
                return ret;
            if (file.getMimeType().contains("bz") &&
                    (ret = extractBZip2(file, into)) > 0)
                return ret;
            if (file.getMimeType().contains("zip") &&
                    (ret = extractZipFiles((OpenStream)file, into)) > 0)
                return ret;
            return 0;
        }

        private int extractBZip2(OpenPath file, OpenPath into)
        {
            InputStream input = null;
            OutputStream out = null;
            try {
                input = new BufferedInputStream(new BZip2InputStream(
                        ((OpenStream)file).getInputStream(), false));
                mTotalCount = (int)file.length();
                if (into.isDirectory())
                    into = into.getChild(file.getName().replace("." + file.getExtension(), ""));
                out = new BufferedOutputStream(((OpenStream)into).getOutputStream());
                copyStreams(input, out, true, false);
                return 1;
            } catch (Exception e) {
                return 0;
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }

        private int extractGZip(OpenPath file, OpenPath into)
        {
            InputStream input = null;
            OutputStream out = null;
            try {
                input = new BufferedInputStream(new GZIPInputStream(
                        ((OpenStream)file).getInputStream()));
                mTotalCount = (int)file.length();
                if (into.isDirectory())
                    into = into.getChild(file.getName().replace("." + file.getExtension(), ""));
                out = new BufferedOutputStream(((OpenStream)into).getOutputStream());
                copyStreams(input, out, true, true);
                return 1;
            } catch (Exception e) {
                return 0;
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }

        private Boolean extractTar(OpenPath source, OpenPath into, String... includes) {
            if (!into.exists() && !into.mkdir())
                return false;
            if ((source.getMimeType().contains("7z") || source.getMimeType().contains("lzma")) &&
                    extractLZMAFiles((OpenStream)source, into, includes) > -1)
                return true;
            try {
                TarUtils.untarTarFile(into.getPath(), source.getPath(), includes);
                return true;
            } catch (IOException e) {
                Logger.LogError("Unable to untar!", e);
                return false;
            }
        }

        /*
         * More efficient Channel based copying
         */
        private Boolean copyFileToDirectory(final OpenFile source, OpenFile into, final int total) {
            Logger.LogVerbose("Using Channel copy for " + source);
            into.mkdir();
            if (into.isDirectory())
                into = into.getChild(source.getName());
            if (source.getPath().equals(into.getPath()))
                return false;
            final OpenFile dest = (OpenFile)into;
            final long size = source.length();
            Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while ((int)dest.length() < total && !Thread.currentThread().isInterrupted()) {
                            long pos = dest.length();
                            publish((int)pos, (int)size, total);
                            try {
                                Thread.sleep(500);
                                if(Thread.currentThread().isInterrupted())
                                	break;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                });
            t.start();
            publish(0, (int)size, total);
            boolean ret = dest.copyFrom(source);
            if(t != null && !t.isInterrupted())
            	t.interrupt();
            return ret;
        }

        private Boolean checkCloudUpload(final OpenPath old, final OpenPath intoDir)
        {
            if (old instanceof OpenFile && intoDir instanceof OpenNetworkPath.CloudOpsHandler)
            {
                final CloudOpsHandler remote = ((OpenNetworkPath.CloudOpsHandler)intoDir);
                final OpenFile local = (OpenFile)old;
                final long srcLength = old.length();
                mCloudCancellor = remote.uploadToCloud(
                        local, new OpenNetworkPath.CloudProgressListener() {
                            public void onException(Exception e) {
                                Logger.LogError("Unable to upload to cloud", e);
                                onPostExecute(0);
                                mThreadListener.onWorkerThreadFailure(BackgroundWork.this.mType, old, intoDir);
                            }

                            @Override
                            public void onCloudComplete(String status) {
                                onPostExecute(1);
                                Logger.LogDebug("Cloud Upload finished");
                            }

                            @Override
                            public void onProgress(long bytes) {
                                onProgressUpdate((int)bytes, (int)srcLength);
                            }
                        });

                return true;
            }
            return false;
        }
        
        private Boolean checkCloudDelete(final OpenPath file)
        {
            if(file instanceof OpenNetworkPath.CloudOpsHandler)
            {
                final CloudOpsHandler remote = (CloudOpsHandler)file;
                return remote.delete(new CloudDeleteListener() {
                    public void onException(Exception e) {
                        Logger.LogError("Unable to delete cloud file." , e);
                    }
                    public void onDeleteComplete(String status) {
                        onPostExecute(1);
                    }
                });
            }
            return false;
        }

        private Boolean checkCloudDownload(final OpenPath from, OpenPath into)
        {
            if (into instanceof OpenFile && from instanceof OpenNetworkPath.CloudOpsHandler)
            {
                if(into.isDirectory())
                    into = into.getChild(from.getName());
                final CloudOpsHandler remote = ((OpenNetworkPath.CloudOpsHandler)from);
                final OpenFile local = (OpenFile)into;
                final long srcLength = from.length();
                mCloudCancellor = remote.downloadFromCloud(
                        local, new OpenNetworkPath.CloudProgressListener() {
                            public void onException(Exception e) {
                                Logger.LogError("Unable to download from cloud", e);
                                onPostExecute(0);
                                mThreadListener.onWorkerThreadFailure(BackgroundWork.this.mType, from, local);
                            }

                            @Override
                            public void onCloudComplete(String status) {
                                onPostExecute(1);
                            }

                            @Override
                            public void onProgress(long bytes) {
                                onProgressUpdate((int)bytes, (int)srcLength);
                            }
                        });

                return true;
            }
            return false;
        }

        private Boolean copyToDirectory(OpenPath old, OpenPath intoDir, int total)
                throws IOException {
            if (old.equals(intoDir))
                return false;
            if(checkCloudDownload(old, intoDir))
                return true;
            if(checkCloudUpload(old, intoDir))
                return true;
            if (old instanceof OpenFile && !old.isDirectory() && intoDir instanceof OpenFile)
                if (copyFileToDirectory((OpenFile)old, (OpenFile)intoDir, total))
                    return true;
            if (old instanceof OpenSMB && intoDir instanceof OpenSMB) {
                Logger.LogVerbose("EventHandler.copyToDirectory : Using OpenSMB Channel copy");
                if (((OpenSMB)old).copyTo((OpenSMB)intoDir))
                    return true;
            } /*
               * else if(old instanceof OpenSMB && intoDir instanceof OpenFile)
               * { Logger.LogVerbose(
               * "EventHandler.copyToDirectory : Using OpenSMB Copy");
               * ((OpenSMB)old).copyTo((OpenFile)intoDir, this);
               * if(intoDir.length() > 0) return true; }
               */
            if (!intoDir.canWrite())
                return false;
            if(OpenExplorer.IS_DEBUG_BUILD)
                Logger.LogVerbose("EventHandler.copyToDirectory : Using Stream copy");
            if (intoDir instanceof OpenCursor) {
                try {
                    if (old.isImageFile() && intoDir.getName().equals("Photos")) {
                        if (Images.Media.insertImage(mContext.getContentResolver(), old.getPath(),
                                old.getName(), null) != null)
                            return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            OpenPath newDir = intoDir;
            if (intoDir instanceof OpenSmartFolder)
                newDir = ((OpenSmartFolder)intoDir).getFirstDir();
            Logger.LogDebug("EventHandler.copyToDirectory : Trying to copy [" + old.getPath()
                    + "] to [" + intoDir.getPath() + "]...");
            if (old.getPath().equals(intoDir.getPath())) {
                return false;
            }
            if (old.isDirectory()) {
                newDir = newDir.getChild(old.getName());
                if (!newDir.exists() && !newDir.mkdir()) {
                    Logger.LogWarning("Couldn't create initial destination file.");
                }
            }
            byte[] data = new byte[FileManager.BUFFER];
            int read = 0;

            if (old.isDirectory() && newDir.isDirectory() && newDir.canWrite()) {
                OpenPath[] files = old.list();
                
                if(files == null)
                	files = old.listFiles();

                for (OpenPath file : files)
                    if (file != null)
                        total += (int)file.length();

                for (int i = 0; i < files.length; i++)
                    if (files[i] != null && !copyToDirectory(files[i], newDir, total)) {
                        Logger.LogWarning("Couldn't copy " + files[i].getName() + ".");
                        return false;
                    }

                return true;

                // } else if(old instanceof OpenSMB && newDir instanceof
                // OpenFile) {
                // ((OpenSMB)old).copyTo((OpenFile)newDir, this);
            } else if (old.isFile() && newDir.isDirectory() && newDir.canWrite()) {
                OpenPath newFile = newDir.getChild(old.getName());
                if (newFile.getPath().equals(old.getPath())) {
                    Logger.LogWarning("Old = new");
                    return false;
                }
                Logger.LogDebug("Creating File [" + newFile.getPath() + "]...");
                if (!newDir.exists() && !newFile.mkdir()) {
                    Logger.LogWarning("Couldn't create initial destination file.");
                    return false;
                }
                if (old instanceof OpenPathCopyable && newFile instanceof OpenStream)
                {
                    try {
                        if (((OpenPathCopyable)old).copyTo((OpenStream)newFile))
                            return true;
                    } catch (IOException e) {
                    }
                }

                boolean success = false;
                if (old instanceof OpenStream && newFile instanceof OpenStream)
                {
                	Logger.LogDebug("Copying Stream -> Stream");
                    int size = (int)old.length();
                    int pos = 0;

                    BufferedInputStream i_stream = null;
                    BufferedOutputStream o_stream = null;
                    try {
                        Logger.LogDebug("Writing " + newFile.getPath());
                        i_stream = new BufferedInputStream(((OpenStream)old).getInputStream());
                        o_stream = new BufferedOutputStream(((OpenStream)newFile).getOutputStream());

                        while ((read = i_stream.read(data, 0,
                                size > 0 ? Math.min(size - pos, FileManager.BUFFER) : FileManager.BUFFER)) != -1) {
                            o_stream.write(data, 0, read);
                            pos += read;
                            if (size > 0 && pos >= size)
                                break;
                            publishMyProgress(pos, Math.max(pos, size));
                        }

                        o_stream.flush();
                        i_stream.close();
                        o_stream.close();

                        success = true;

                    } catch (NullPointerException e) {
                        Logger.LogError("Null pointer trying to copy file.", e);
                    } catch (FileNotFoundException e) {
                        Logger.LogError("Couldn't find file to copy.", e);
                    } catch (IOException e) {
                        Logger.LogError("IOException copying file.", e);
                    } catch (Exception e) {
                        Logger.LogError("Unknown error copying file.", e);
                    } finally {
                        if (o_stream != null)
                            o_stream.close();
                        if (i_stream != null)
                            i_stream.close();
                    }
                }
                return success;

            } else if (!newDir.canWrite()) {
                Logger.LogWarning("Destination directory not writable.");
                return false;
            }

            Logger.LogWarning("Couldn't copy file for unknown reason.");
            OnWorkerThreadFailure(mType, old);
            return false;
        }

        private int extractRarFiles(OpenRAR rar, OpenPath directory) {

            if (!directory.exists() && !directory.mkdir())
                return -1;

            int ret = 0;

            List<OpenRAREntry> entries = new ArrayList<OpenRAR.OpenRAREntry>();
            try {
                entries = rar.getAllEntries();
                mTotalCount = entries.size();
            } catch (Exception e) {
                Logger.LogError("Couldn't get RAR entries!", e);
                return -1;
            }

            mTotalCount = (int)rar.length();

            for (OpenRAREntry entry : entries) {
                OpenPath newFile = directory.getChild(entry.getName());
                if (!newFile.getParent().exists() && !newFile.getParent().mkdir())
                    continue;
                if (!(newFile instanceof OpenStream))
                    continue;
                OutputStream out = null;
                try {
                    InputStream is = new BufferedInputStream(entry.getInputStream());
                    out = new BufferedOutputStream(((OpenStream)newFile).getOutputStream());
                    copyStreams(is, out, false, true);
                    ret++;
                } catch (Exception e) {
                    Logger.LogError("Couldn't unrar!", e);
                } finally {
                    closeStream(out);
                }
            }
            return ret;
        }

        private int extractZipFiles(OpenStream zip, OpenPath directory) {
            if (OpenExplorer.IS_DEBUG_BUILD)
                Logger.LogVerbose("Extracting ZIP: " + zip + " (into " + directory + ")");
            byte[] data = new byte[FileManager.BUFFER];
            ZipEntry entry;
            ZipInputStream zipstream = null;
            OutputStream out = null;

            int ret = -1;

            try {
                ZipFile zf = new ZipFile(((OpenFile)zip).getPath());
                mTotalCount = zf.size();
                zipstream = new ZipInputStream(zip.getInputStream());

                while ((entry = zipstream.getNextEntry()) != null) {
                    OpenPath newFile = directory.getChild(entry.getName());
                    if (!newFile.getParent().exists() && !newFile.getParent().mkdir())
                    {
                        Logger.LogWarning("Unable to create parent directory while unzipping");
                        continue;
                    }
                    if (!(newFile instanceof OpenStream))
                    {
                        Logger.LogWarning("ZIP: New File isn't a stream? " + newFile);
                        continue;
                    }

                    int read = 0;
                    try {
                        out = new BufferedOutputStream(((OpenStream)newFile).getOutputStream());
                        copyStreams(zipstream, out, false, true);
                        ret++;
                    } catch (Exception e) {
                        Logger.LogError("Unable to unzip file!", e);
                    } finally {
                        zipstream.closeEntry();
                    }
                }

            } catch (FileNotFoundException e) {
                ret = -1;
                Logger.LogError("Couldn't find zip?", e);
            } catch (IOException e) {
                ret = -1;
                Logger.LogError("Unable to unzip?", e);
            } finally {
                closeStream(out);
                closeStream(zipstream);
            }
            return ret;
        }

        private int extractLZMAFiles(OpenStream s7, final OpenPath directory, String... includes) {
            // Logger.LogVerbose("LZMA Trying to extract 7zip");
            OpenLZMA f7 = null;
            int ret = 0;

            try {
                if (s7 instanceof OpenLZMA)
                    f7 = (OpenLZMA)s7;
                else
                    f7 = new OpenLZMA((OpenFile)s7);

                mTotalCount = f7.getListLength();

                int[] indices = null;
                int i = 0;
                if (includes.length > 0)
                {
                    mTotalCount = includes.length;
                    indices = new int[includes.length];
                    for (OpenLZMAEntry ze : f7.getAllEntries())
                    {
                        int pos = binarySearch(includes, ze.getRelativePath());
                        Logger.LogVerbose("LZMA " + ze.getRelativePath());
                        if (pos > -1)
                            indices[i++] = pos;
                        if (i >= indices.length)
                            break;
                    }
                }

                ArchiveExtractCallback extractCallbackSpec = new ArchiveExtractCallback();
                String base = directory.getPath();
                if (!base.endsWith("/"))
                    base += "/";
                extractCallbackSpec.setBasePath(base);
                // Logger.LogVerbose("LZMA Base: " + base);
                IArchiveExtractCallback extractCallback = extractCallbackSpec;
                IInArchive arch = f7.getLZMA();

                extractCallbackSpec.Init(arch);
                int res = 0;
                if (indices == null)
                    res = arch.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract,
                            extractCallback);
                else
                    res = arch.Extract(indices, indices.length,
                            IInArchive.NExtract_NAskMode_kExtract, extractCallback);

                if (res == HRESULT.S_OK) {
                    if (extractCallbackSpec.NumErrors == 0)
                    {
                        Logger.LogDebug("LZMA complete?");
                        ret = mTotalCount;
                        return ret;
                    } else {
                        Logger.LogError("LZMA errors: " + extractCallbackSpec.NumErrors);
                    }
                } else {
                    Logger.LogError("Error while extracting LZMA!");
                }

                return ret;

            } catch (Exception e) {
                Logger.LogError("Unable to extract LZMA.", e);
                ret = -1;
            } finally {
            }
            return ret;
        }

        private void closeStream(java.io.Closeable s)
        {
            try {
                if (s != null)
                    s.close();
            } catch (Exception e) {
            }
        }

        public void publish(int current, int size, int total) {
            publishProgress(current, size, total);
            // OnWorkerProgressUpdate(current, total);
        }

        private long lastPublish = 0l;

        public void publishMyProgress(Integer... values) {
            if (new Date().getTime() - lastPublish < 500)
                return;
            lastPublish = new Date().getTime();
            publishProgress(values);
        }

        public int getProgressA() {
            return (int)(((float)mLastProgress[0] / (float)mLastProgress[1]) * 1000f);
        }

        public int getProgressB() {
            return (int)(((float)mLastProgress[0] / (float)mLastProgress[2]) * 1000f);
        }

        @SuppressWarnings("unused")
        @Override
        protected void onProgressUpdate(Integer... values) {
            int current = 0, size = 0, total = 0;
            if (values.length > 0)
                current = size = total = values[0];
            if (values.length > 1)
                size = total = values[1];
            if (values.length > 2)
                total = Math.max(total, values[2]);

            // if(mThreadListener != null)
            // mThreadListener.onWorkerProgressUpdate(current, size);

            mLastProgress[0] = current;
            mLastProgress[1] = size;
            mLastProgress[2] = total;

            if (size == 0)
                size = 1;
            if (total == 0)
                total = 1;

            int progA = (int)(((float)current / (float)size) * 1000f);
            int progB = (int)(((float)current / (float)total) * 1000f);

            mElapsed = new Date().getTime() - mStart.getTime();
            if (mElapsed / 1000 > 0) {
                mLastRate = ((long)current) / (mElapsed / 1000);
                if (mLastRate > 0)
                    mRemain = Utils.getAverage(mRemain, (long)(size - current) / mLastRate);
            }

            // publish(current, size, total);
            OnWorkerProgressUpdate(current, total);

            Logger.LogInfo("onProgressUpdate(" + current + ", " + size + ", " + total +
            		")-(" + progA + "," + progB + ")-> " + mRemain + "::" + mLastRate);

            // mNote.setLatestEventInfo(mContext, , contentText, contentIntent)

            try {
                if (values.length == 0)
                    mPDialog.setIndeterminate(true);
                else {
                    mPDialog.setIndeterminate(false);
                    mPDialog.setMax(1000);
                    mPDialog.setProgress(progA);
                    if (progB != progA)
                        mPDialog.setSecondaryProgress(progB);
                    else
                        mPDialog.setSecondaryProgress(0);
                }
            } catch (Exception e) {
            }

            if (SHOW_NOTIFICATION_STATUS) {

                try {
                    mNotifier.notify(BACKGROUND_NOTIFICATION_ID,
                            prepareNotification(notifIcon, isCancellable, values));
                } catch (Exception e) {
                    Logger.LogWarning("Couldn't update notification progress.", e);
                }

            }
        }

        public void updateView(final View view) {
            if (view == null)
                return;
            Runnable runnable = new Runnable() {
                public void run() {
                    ViewUtils.setImageResource(view, getNotifIconResId(), android.R.id.icon);
                    ViewUtils.setText(view, getTitle(), android.R.id.title);
                    ViewUtils.setText(view, getLastRate(), android.R.id.text1);
                    ViewUtils.setText(view, getSubtitle(), android.R.id.text2);
                    if (view.findViewById(android.R.id.progress) != null) {
                        int progA = (int)(((float)mLastProgress[0] / (float)mLastProgress[1]) * 1000f);
                        int progB = (int)(((float)mLastProgress[0] / (float)mLastProgress[2]) * 1000f);
                        if (getStatus() != Status.RUNNING)
                            ViewUtils.setViewsVisible(view, false, android.R.id.progress);
                        else {
                            ProgressBar pb = (ProgressBar)view.findViewById(android.R.id.progress);
                            if (getStatus() == Status.PENDING || mLastRate == 0)
                                pb.setIndeterminate(true);
                            else {
                                pb.setIndeterminate(false);
                                pb.setMax(1000);
                                pb.setProgress(progA);
                                pb.setSecondaryProgress(progB);
                            }
                        }
                    }
                }
            };
            if (!Thread.currentThread().equals(OpenExplorer.UiThread))
                view.post(runnable);
            else
                runnable.run();
        }

        @Override
        public void onProgressUpdateCallback(Integer... vals) {
            publishMyProgress(vals);
        }

        protected void onPostExecute(Integer result) {
            // NotificationManager mNotifier =
            // (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            Logger.LogDebug("EventHandler.onPostExecute(" + mIntoPath + ")");
            mNotifier.cancel(BACKGROUND_NOTIFICATION_ID);

            if (mPDialog != null && mPDialog.isShowing())
                mPDialog.dismiss();

            // mTasks.remove(this);

            if (getRunningTasks().length == 0)
                mNotifier.cancelAll();

            try {
                showToast(result);
            } catch (Exception e) {
                Logger.LogError("Unable to show EventHandle PostExecute Toast.", e);
            }
            OnWorkerThreadComplete(mType);
        }

        private void showToast(Integer result)
        {
            switch (mType) {

                case DELETE:
                    if (result == 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_none,
                                        R.string.s_msg_deleted), Toast.LENGTH_SHORT).show();
                    else if (result == -1)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_some,
                                        R.string.s_msg_deleted), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_all,
                                        R.string.s_msg_deleted), Toast.LENGTH_SHORT).show();
                    break;

                case SEARCH:
                    OnWorkerThreadComplete(mType,
                            mSearchResults.toArray(new String[mSearchResults.size()]));
                    return;

                case COPY:
                    if (result == null || result == 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_none,
                                        R.string.s_msg_copied), Toast.LENGTH_SHORT).show();
                    else if (result != null && result < 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_some,
                                        R.string.s_msg_copied), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_all,
                                        R.string.s_msg_copied), Toast.LENGTH_SHORT).show();
                    break;
                case CUT:
                    if (result == null || result == 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_none,
                                        R.string.s_msg_moved), Toast.LENGTH_SHORT).show();
                    else if (result != null && result < 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_some,
                                        R.string.s_msg_moved), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_all,
                                        R.string.s_msg_moved), Toast.LENGTH_SHORT).show();

                    break;

                case ZIP:
                    int typeRes = R.string.s_compressed;
                    if (result == null || result == 0)
                        Toast.makeText(
                                mContext,
                                mIntoPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_none, typeRes),
                                Toast.LENGTH_SHORT).show();
                    else if (result != null && result < 0)
                        Toast.makeText(
                                mContext,
                                mIntoPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_some, typeRes),
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                mIntoPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_all, typeRes),
                                Toast.LENGTH_SHORT).show();
                    break;

                case EXTRACT:
                    typeRes = R.string.s_extracted;
                    if (result == null || result == 0)
                        Toast.makeText(
                                mContext,
                                mCurrentPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_none, typeRes),
                                Toast.LENGTH_SHORT).show();
                    else if (result != null && result < 0)
                        Toast.makeText(
                                mContext,
                                mCurrentPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_some, typeRes),
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                mCurrentPath.getMimeType().replace("application/", "") + ": " +
                                        getResourceString(mContext, R.string.s_msg_all, typeRes),
                                Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    }

}

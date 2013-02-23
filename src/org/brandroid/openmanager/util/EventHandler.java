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
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.BluetoothActivity;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.data.OpenPath.OpenPathByteIO;
import org.brandroid.openmanager.data.OpenPath.OpenPathCopyable;
import org.brandroid.openmanager.data.OpenTar;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.FileManager.OnProgressUpdateCallback;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarUtils;

@SuppressWarnings({
        "unchecked", "rawtypes"
})
@SuppressLint("NewApi")
public class EventHandler {
    public static final EventType SEARCH_TYPE = EventType.SEARCH;
    public static final EventType COPY_TYPE = EventType.COPY;
    public static final EventType UNZIP_TYPE = EventType.UNZIP;
    public static final EventType UNZIPTO_TYPE = EventType.UNZIPTO;
    public static final EventType ZIP_TYPE = EventType.ZIP;
    public static final EventType DELETE_TYPE = EventType.DELETE;
    public static final EventType RENAME_TYPE = EventType.RENAME;
    public static final EventType MKDIR_TYPE = EventType.MKDIR;
    public static final EventType CUT_TYPE = EventType.CUT;
    public static final EventType TOUCH_TYPE = EventType.TOUCH;
    public static final EventType ERROR_TYPE = EventType.ERROR;
    public static final EventType UNTAR_TYPE = EventType.UNTAR;
    public static final EventType TAR_TYPE = EventType.TAR;
    public static final EventType GUNZIP_TYPE = EventType.UNTGZ;
    public static final EventType GZIP_TYPE = EventType.UNTGZ;
    public static final int BACKGROUND_NOTIFICATION_ID = 123;
    private static final boolean ENABLE_MULTITHREADS = false; // !OpenExplorer.BEFORE_HONEYCOMB;

    static final int TAR_BUFFER = 2048;

    public enum EventType {
        SEARCH, COPY, CUT, DELETE, RENAME, MKDIR, TOUCH, UNZIP, UNZIPTO, ZIP, ERROR, UNTAR, UNTGZ, TAR, TGZ, GUNZIP, GZIP
    }

    public static boolean SHOW_NOTIFICATION_STATUS = !OpenExplorer.isBlackBerry()
            && Build.VERSION.SDK_INT >= 8;

    private static NotificationManager mNotifier = null;
    private static int EventCount = 0;

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
            if (bw.getStatus() == Status.RUNNING)
                bw.cancel(true);
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
            ret += (ret == "" ? "" : " ") + mContext.getText(resId);
        return ret;
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

    /**
     * @param directory directory path to create the new folder in.
     */
    public static void createNewFolder(final OpenPath folder, final Context context,
            final OnWorkerUpdateListener threadListener) {
        final InputDialog dlg = new InputDialog(context).setTitle(R.string.s_title_newfolder)
                .setIcon(R.drawable.ic_menu_folder_add_dark).setMessage(R.string.s_alert_newfolder)
                .setMessageTop(R.string.s_alert_newfolder_folder)
                .setDefaultTop(folder.getPath(), false).setCancelable(true)
                .setNegativeButton(R.string.s_cancel, DialogHandler.OnClickDismiss);
        dlg.setPositiveButton(R.string.s_create, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = dlg.getInputText();
                if (name.length() > 0) {
                    if (!folder.getChild(name).exists()) {
                        if (!createNewFolder(folder, name, context)) {
                            // new folder wasn't created, and since we've
                            // already ruled out an existing folder, the folder
                            // can't be created for another reason
                            OpenPath path = folder.getChild(name);
                            Logger.LogError("Unable to create folder (" + path + ")");
                            if (threadListener != null)
                                threadListener.onWorkerThreadFailure(MKDIR_TYPE);
                            Toast.makeText(context, R.string.s_msg_folder_none, Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            if (threadListener != null)
                                threadListener.onWorkerThreadComplete(MKDIR_TYPE);
                        }
                    } else {
                        // folder exists, so let the user know
                        Toast.makeText(context,
                                getResourceString(context, R.string.s_msg_folder_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    dialog.dismiss();
                }
            }
        });
        dlg.create().show();
    }

    protected static boolean createNewFolder(OpenPath folder, String folderName, Context context) {
        return folder.getChild(folderName).mkdir();
    }

    public static void createNewFile(final OpenPath folder, final Context context,
            final OnWorkerUpdateListener threadListener) {
        final InputDialog dlg = new InputDialog(context).setTitle(R.string.s_title_newfile)
                .setIcon(R.drawable.ic_menu_new_file).setMessage(R.string.s_alert_newfile)
                .setMessageTop(R.string.s_alert_newfile_folder).setDefaultTop(folder.getPath())
                .setCancelable(true).setNegativeButton(R.string.s_cancel, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dlg.setPositiveButton(R.string.s_create, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = dlg.getInputText();
                if (name.length() > 0) {
                    createNewFile(folder, name, threadListener);
                } else {
                    dialog.dismiss();
                }
            }
        });
        dlg.create().show();
    }

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
                        mContext, newPath, file.getName()), file);
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
            job.execute(params);
        else
            job.executeOnExecutor(getExecutor(), params);
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
            job.execute(params);
        else
            job.executeOnExecutor(getExecutor(), params);
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
        return zipFile(into, files.toArray(new OpenPath[0]), mContext);
    }

    public BackgroundWork zipFile(OpenPath into, OpenPath[] files, Context mContext) {
        return (BackgroundWork)execute(new BackgroundWork(ZIP_TYPE, mContext, into), files);
    }

    public void unzipFile(final OpenPath file, final Context mContext) {
        final OpenPath into = file.getParent().getChild(
                file.getName().replace("." + file.getExtension(), ""));
        // AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        final InputDialog dUnz = new InputDialog(mContext);
        dUnz.setTitle(
                getResourceString(mContext, R.string.s_title_unzip).toString().replace("xxx",
                        file.getName()))
                .setMessage(
                        getResourceString(mContext, R.string.s_prompt_unzip).toString().replace(
                                "xxx", file.getName()))
                .setIcon(R.drawable.lg_zip)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        OpenPath path = new OpenFile(dUnz.getInputText());
                        if (!path.exists() && !path.mkdir()) {
                            Logger.LogError("Couldn't locate output path for unzip! "
                                    + path.getPath());
                            OnWorkerThreadFailure(UNZIPTO_TYPE);
                            return;
                        }
                        Logger.LogVerbose("Unzipping " + file.getPath() + " into " + path);
                        execute(new BackgroundWork(UNZIP_TYPE, mContext, path), file);
                    }
                }).setNegativeButton(android.R.string.cancel, null).setDefaultText(into.getPath())
                .create().show();
    }

    public void untarFile(final OpenPath file, final OpenPath dest, final Context mContext, final String... includes)
    {
        execute(new BackgroundWork(UNTAR_TYPE, mContext, dest, includes), file);
    }

    /*
     * public void unZipFileTo(OpenPath zipFile, OpenPath toDir, Context
     * mContext) { new BackgroundWork(UNZIPTO_TYPE, mContext,
     * toDir).execute(zipFile); }
     */

    public class BackgroundWork extends AsyncTask<OpenPath, Integer, Integer> implements
            OnProgressUpdateCallback {
        private final EventType mType;
        private final Context mContext;
        private final String[] mInitParams;
        private final OpenPath mIntoPath;
        private ProgressDialog mPDialog;
        private Notification mNote = null;
        private final int mNotifyId;
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

        private OnWorkerUpdateListener mListener;

        public void setWorkerUpdateListener(OnWorkerUpdateListener listener) {
            mListener = listener;
        }

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
            mType = type;
            mContext = context;
            mInitParams = params;
            mIntoPath = intoPath;
            if (mNotifier == null)
                mNotifier = (NotificationManager)context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            taskId = mTasks.size();
            mStart = new Date();
            mNotifyId = BACKGROUND_NOTIFICATION_ID + EventCount++;
            mTasks.add(this);
            if (mTaskListener != null)
                mTaskListener.OnTasksChanged(getRunningTasks().length);
        }

        public String getOperation() {
            switch (mType) {
                case DELETE:
                    return getResourceString(mContext, R.string.s_title_deleting).toString();
                case SEARCH:
                    return getResourceString(mContext, R.string.s_title_searching).toString();
                case COPY:
                    return getResourceString(mContext, R.string.s_title_copying).toString();
                case CUT:
                    return getResourceString(mContext, R.string.s_title_moving).toString();
                case UNZIP:
                case UNZIPTO:
                    return getResourceString(mContext, R.string.s_title_unzipping).toString();
                case ZIP:
                    return getResourceString(mContext, R.string.s_title_zipping).toString();
                case MKDIR:
                    return getResourceString(mContext, R.string.s_menu_rename).toString();
                case TOUCH:
                    return getResourceString(mContext, R.string.s_create).toString();
                case UNTAR:
                    return getResourceString(mContext, R.string.s_untarring).toString();
            }
            return getResourceString(mContext, R.string.s_title_executing);
        }

        public String getTitle() {
            String title = getOperation();
            if (mCurrentPath != null) {
                title += " " + '\u2192' + " " + mCurrentPath.getName();
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
                            + DialogHandler.formatSize(mLastRate).replace(" ", "").toLowerCase()
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
            mNotifier.cancel(mNotifyId);
            super.onCancelled();
            mTasks.remove(this);
        }

        @Override
        protected void onCancelled(Integer result) {
            mNotifier.cancel(mNotifyId);
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
                case UNZIP:
                case UNZIPTO:
                    showDialog = false;
                    showNotification = true;
                    break;
                case ZIP:
                    showDialog = false;
                    showNotification = true;
                    break;
                case UNTGZ:
                case UNTAR:
                    showDialog = true;
                    showNotification = false;
                    isCancellable = false;
                    break;
                default:
                    showDialog = showNotification = false;
                    break;
            }
            if (showDialog)
                try {
                    mPDialog = ProgressDialog.show(mContext, getTitle(),
                            getResourceString(mContext, R.string.s_title_wait).toString(), true,
                            true, new DialogInterface.OnCancelListener() {
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
                ret += "Progress: " + DialogHandler.formatSize(mLastProgress[0]) + " / "
                        + DialogHandler.formatSize(mLastProgress[1]) + " ";
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
                Intent intent = new Intent(mContext, OpenExplorer.class);
                intent.putExtra("TaskId", taskId);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                        OpenExplorer.REQUEST_VIEW, intent, 0);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setOngoing(false);
                mBuilder.setOnlyAlertOnce(true);
                NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
                style.bigText(getDetailedText());
                style.setBigContentTitle(getOperation() + " " + mCurrentPath.getName());
                mBuilder.setStyle(style);
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
            Logger.LogDebug("Starting Op!");
            mTotalCount = params.length;
            int ret = 0;

            mCurrentPath = params[0];

            switch (mType) {

                case DELETE:
                    for (int i = 0; i < mTotalCount; i++)
                        ret += mFileMang.deleteTarget(params[i]);
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
                case UNZIPTO:
                case UNZIP:
                    extractZipFiles(params[0], mIntoPath);
                    break;

                case ZIP:
                    mFileMang.setProgressListener(this);
                    publishProgress();
                    mFileMang.createZipFile(mIntoPath, params);
                    break;

                case UNTAR:
                    if (untarFile(mCurrentPath, mIntoPath, mInitParams))
                        ret++;
                    break;

                case UNTGZ:
                    try {
                        TarUtils.untarTGzFile(mIntoPath.getPath(), mCurrentPath.getPath());
                        ret++;
                    } catch (IOException e) {
                        Logger.LogError("Unable to untar file: " + mCurrentPath, e);
                    }
                    break;

                case TAR:
                    try {
                        TarUtils.tar(new java.io.File(mIntoPath.getPath()), mCurrentPath.getPath());
                        ret++;
                    } catch (IOException e) {
                        Logger.LogError("Unable to untar file: " + mCurrentPath, e);
                    }
                    break;
            }

            return ret;
        }

        private Boolean untarFile(OpenPath source, OpenPath into, String... includes) {
            if (!into.exists() && !into.mkdir())
                return false;
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
            final boolean[] running = new boolean[] {
                    true
            };
            final long size = source.length();
            if (size > 50000)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while ((int)dest.length() < total || running[0]) {
                            long pos = dest.length();
                            publish((int)pos, (int)size, total);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                running[0] = false;
                            }
                        }
                    }
                }).start();
            boolean ret = dest.copyFrom(source);
            running[0] = false;
            return ret;
        }

        private Boolean copyToDirectory(OpenPath old, OpenPath intoDir, int total)
                throws IOException {
            if (old.equals(intoDir))
                return false;
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
            if (intoDir instanceof OpenSmartFolder) {
                newDir = ((OpenSmartFolder)intoDir).getFirstDir();
                if (old instanceof OpenFile && newDir instanceof OpenFile)
                    return copyFileToDirectory((OpenFile)old, (OpenFile)newDir, total);
            }
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
                if (old instanceof OpenPathCopyable)
                {
                    try {
                        if (((OpenPathCopyable)old).copyTo(newFile))
                            return true;
                    } catch (IOException e) {
                    }
                }

                int size = (int)old.length();
                int pos = 0;

                BufferedInputStream i_stream = null;
                BufferedOutputStream o_stream = null;
                boolean success = false;
                try {
                    Logger.LogDebug("Writing " + newFile.getPath());
                    i_stream = new BufferedInputStream(old.getInputStream());
                    o_stream = new BufferedOutputStream(newFile.getOutputStream());

                    while ((read = i_stream.read(data, 0,
                            Math.min(size - pos, FileManager.BUFFER))) != -1) {
                        o_stream.write(data, 0, read);
                        pos += read;
                        if (pos >= size)
                            break;
                        publishMyProgress(pos, size);
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
                return success;

            } else if (!newDir.canWrite()) {
                Logger.LogWarning("Destination directory not writable.");
                return false;
            }

            Logger.LogWarning("Couldn't copy file for unknown reason.");
            return false;
        }

        public void extractZipFiles(OpenPath zip, OpenPath directory) {
            byte[] data = new byte[FileManager.BUFFER];
            ZipEntry entry;
            ZipInputStream zipstream;

            if (!directory.mkdir())
                return;

            try {
                zipstream = new ZipInputStream(zip.getInputStream());

                while ((entry = zipstream.getNextEntry()) != null) {
                    OpenPath newFile = directory.getChild(entry.getName());
                    if (!newFile.mkdir())
                        continue;

                    int read = 0;
                    FileOutputStream out = (FileOutputStream)newFile.getOutputStream();
                    while ((read = zipstream.read(data, 0, FileManager.BUFFER)) != -1)
                        out.write(data, 0, read);

                    zipstream.closeEntry();
                    out.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
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

            // Logger.LogInfo("onProgressUpdate(" + current + ", " + size + ", "
            // + total + ")-("
            // + progA + "," + progB + ")-> " + mRemain + "::" + mLastRate);

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
                    mNotifier.notify(mNotifyId,
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
            mNotifier.cancel(mNotifyId);

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

                case TAR:
                case TGZ:
                    break;

                case UNTGZ:
                case UNTAR:
                    if (result == null || result == 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_none,
                                        R.string.s_untar), Toast.LENGTH_SHORT).show();
                    else if (result != null && result < 0)
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_some,
                                        R.string.s_untar), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(
                                mContext,
                                getResourceString(mContext, R.string.s_msg_all,
                                        R.string.s_untar), Toast.LENGTH_SHORT).show();

                case UNZIPTO:
                    break;

                case UNZIP:
                    break;

                case ZIP:
                    break;
            }
        }
    }

}

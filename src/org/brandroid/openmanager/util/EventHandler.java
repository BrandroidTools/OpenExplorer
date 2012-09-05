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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.TextView;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.BluetoothActivity;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.FileManager.OnProgressUpdateCallback;
import org.brandroid.openmanager.util.NetworkIOTask.OnTaskUpdateListener;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

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
	public static final int BACKGROUND_NOTIFICATION_ID = 123;
	private static boolean ENABLE_MULTITHREADS = !OpenExplorer.BEFORE_HONEYCOMB && false;

	public enum EventType {
		SEARCH, COPY, CUT, DELETE, RENAME, MKDIR, TOUCH, UNZIP, UNZIPTO, ZIP, ERROR
	}

	public static boolean SHOW_NOTIFICATION_STATUS = !OpenExplorer
			.isBlackBerry() && Build.VERSION.SDK_INT > 9;

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
			mNotifier.cancel(BACKGROUND_NOTIFICATION_ID);
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
		 * @param type
		 *            Type of requested event.
		 * @param files
		 *            List of OpenPath items.
		 */
		public void onWorkerThreadFailure(EventType type, OpenPath... files);
	}

	private synchronized void OnWorkerProgressUpdate(int pos, int total) {
		if (mThreadListener == null)
			return;
		mThreadListener.onWorkerProgressUpdate(pos, total);
	}

	private synchronized void OnWorkerThreadComplete(EventType type,
			String... results) {
		if (mThreadListener != null)
			mThreadListener.onWorkerThreadComplete(type, results);
		if (mTaskListener != null)
			mTaskListener.OnTasksChanged(getTaskList().size());
	}

	private synchronized void OnWorkerThreadFailure(EventType type,
			OpenPath... files) {
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

	public void deleteFile(final OpenPath file, final OpenApp mApp,
			boolean showConfirmation) {
		Collection<OpenPath> files = new ArrayList<OpenPath>();
		files.add(file);
		deleteFile(files, mApp, showConfirmation);
	}

	public void deleteFile(final Collection<OpenPath> path, final OpenApp mApp,
			boolean showConfirmation) {
		final OpenPath[] files = new OpenPath[path.size()];
		path.toArray(files);
		String name;
		final Context mContext = mApp.getContext();

		if (files.length == 1)
			name = files[0].getName();
		else
			name = files.length + " "
					+ getResourceString(mContext, R.string.s_files);

		if (!showConfirmation) {
			execute(new BackgroundWork(DELETE_TYPE, mContext, null),files);
			return;
		}
		AlertDialog.Builder b = new AlertDialog.Builder(mContext);
		b.setTitle(
				getResourceString(mContext, R.string.s_menu_delete) + " "
						+ name)
				.setMessage(
						mContext.getString(R.string.s_alert_confirm_delete,
								name))
				.setIcon(R.drawable.ic_menu_delete)
				.setPositiveButton(
						getResourceString(mContext, R.string.s_menu_delete),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								execute(new BackgroundWork(DELETE_TYPE, mContext, null), files);
							}
						})
				.setNegativeButton(
						getResourceString(mContext, R.string.s_cancel),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create().show();
	}

	public void startSearch(final OpenPath base, final Context mContext) {
		final InputDialog dSearch = new InputDialog(mContext)
				.setIcon(R.drawable.search).setTitle(R.string.s_search)
				.setCancelable(true)
				.setMessageTop(R.string.s_prompt_search_within)
				.setDefaultTop(base.getPath())
				.setMessage(R.string.s_prompt_search);
		AlertDialog alert = dSearch
				.setPositiveButton(R.string.s_search, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						searchFile(new OpenFile(dSearch.getInputTopText()),
								dSearch.getInputText(), mContext);
					}
				}).setNegativeButton(R.string.s_cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		alert.setInverseBackgroundForced(true);
		alert.show();
	}

	public void renameFile(final OpenPath path, boolean isFolder,
			final Context mContext) {
		final InputDialog dRename = new InputDialog(mContext)
				.setIcon(R.drawable.ic_rename).setTitle(R.string.s_menu_rename)
				.setCancelable(true).setMessage(R.string.s_alert_rename)
				.setDefaultText(path.getName());
		dRename.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String newName = dRename.getInputText().toString();
						BackgroundWork work = new BackgroundWork(RENAME_TYPE,
								mContext, path, newName);
						if (newName.length() > 0) {
							execute(work);
						} else
							dialog.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create().show();
	}

	/**
	 * 
	 * @param directory
	 *            directory path to create the new folder in.
	 */
	public static void createNewFolder(final OpenPath folder,
			final Context context) {
		final InputDialog dlg = new InputDialog(context)
				.setTitle(R.string.s_title_newfolder)
				.setIcon(R.drawable.ic_menu_folder_add_dark)
				.setMessage(R.string.s_alert_newfolder)
				.setMessageTop(R.string.s_alert_newfolder_folder)
				.setDefaultTop(folder.getPath(), false).setCancelable(true)
				.setNegativeButton(R.string.s_cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dlg.setPositiveButton(R.string.s_create, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = dlg.getInputText();
				if (name.length() > 0) {
					if (!folder.getChild(name).exists()) {
						if (!createNewFolder(folder, name, context)) {
							// new folder wasn't created, and since we've
							// already ruled out an existing folder, the folder
							// can't be created for another reason
							
						}
					} else {
						// folder exists, so let the user know
						Toast.makeText(
								context,
								getResourceString(context,
										R.string.s_msg_folder_exists),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					dialog.dismiss();
				}
			}
		});
		dlg.create().show();
	}

	protected static boolean createNewFolder(OpenPath folder,
			String folderName, Context context) {
		return folder.getChild(folderName).mkdir();
	}

	public static void createNewFile(final OpenPath folder,
			final Context context) {
		final InputDialog dlg = new InputDialog(context)
				.setTitle(R.string.s_title_newfile)
				.setIcon(R.drawable.ic_menu_new_file)
				.setMessage(R.string.s_alert_newfile)
				.setMessageTop(R.string.s_alert_newfile_folder)
				.setDefaultTop(folder.getPath()).setCancelable(true)
				.setNegativeButton(R.string.s_cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dlg.setPositiveButton(R.string.s_create, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = dlg.getInputText();
				if (name.length() > 0) {
					createNewFile(folder, name, context);
				} else {
					dialog.dismiss();
				}
			}
		});
		dlg.create().show();
	}

	public static void createNewFile(final OpenPath folder,
			final String filename, Context context) {
		new Thread(new Runnable() {
			public void run() {
				folder.getChild(filename).touch();
			}
		}).start();
		// BackgroundWork bw = new BackgroundWork(TOUCH_TYPE, context, folder,
		// filename);
		// bw.execute();
	}

	public void sendFile(final Collection<OpenPath> path, final Context mContext) {
		String name;
		CharSequence[] list = { "Bluetooth", "Email" };
		final OpenPath[] files = new OpenPath[path.size()];
		path.toArray(files);
		final int num = path.size();

		if (num == 1)
			name = files[0].getName();
		else
			name = path.size() + " "
					+ getResourceString(mContext, R.string.s_files) + ".";

		AlertDialog.Builder b = new AlertDialog.Builder(mContext);
		b.setTitle(
				getResourceString(mContext, R.string.s_title_send).toString()
						.replace("xxx", name)).setIcon(R.drawable.bluetooth)
				.setItems(list, new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent bt = new Intent(mContext,
									BluetoothActivity.class);

							bt.putExtra("paths", files);
							mContext.startActivity(bt);
							break;

						case 1:
							ArrayList<Uri> uris = new ArrayList<Uri>();
							Intent mail = new Intent();
							mail.setType("application/mail");

							if (num == 1) {
								mail.setAction(android.content.Intent.ACTION_SEND);
								mail.putExtra(Intent.EXTRA_STREAM,
										files[0].getUri());
								mContext.startActivity(mail);
								break;
							}

							for (int i = 0; i < num; i++)
								uris.add(files[i].getUri());

							mail.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
							mail.putParcelableArrayListExtra(
									Intent.EXTRA_STREAM, uris);
							mContext.startActivity(mail);
							break;
						}
					}
				}).create().show();
	}

	public void copyFile(OpenPath source, OpenPath destPath, Context mContext) {
		if (!destPath.isDirectory())
			destPath = destPath.getParent();
		execute(new BackgroundWork(COPY_TYPE, mContext, destPath, source.getName()), source);
	}

	public void copyFile(Collection<OpenPath> files, OpenPath newPath,
			Context mContext) {
		OpenPath[] array = new OpenPath[files.size()];
		files.toArray(array);

		String title = array.length + " "
				+ mContext.getString(R.string.s_files);
		if (array.length == 1)
			title = array[0].getPath();
		execute(new BackgroundWork(COPY_TYPE, mContext, newPath, title), array);
	}

	@SuppressWarnings("unchecked")
	public static AsyncTask execute(AsyncTask job)
	{
		if(OpenExplorer.BEFORE_HONEYCOMB || !ENABLE_MULTITHREADS)
			job.execute();
		else
			job.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return job;
	}
	public static AsyncTask execute(AsyncTask job, OpenFile... params)
	{
		if(OpenExplorer.BEFORE_HONEYCOMB || !ENABLE_MULTITHREADS)
			job.execute(params);
		else
			job.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		return job;
	}
	public static AsyncTask<OpenPath, Integer, Integer> execute(AsyncTask<OpenPath, Integer, Integer> job, OpenPath... params)
	{
		if(OpenExplorer.BEFORE_HONEYCOMB || !ENABLE_MULTITHREADS)
			job.execute(params);
		else
			job.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		return job;
	}
	public static NetworkIOTask executeNetwork(NetworkIOTask job, OpenPath... params)
	{
		if(OpenExplorer.BEFORE_HONEYCOMB || !ENABLE_MULTITHREADS)
			job.execute(params);
		else
			job.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		return job;
	}
	public static AsyncTask execute(AsyncTask job, String... params)
	{
		if(OpenExplorer.BEFORE_HONEYCOMB || !ENABLE_MULTITHREADS)
			job.execute(params);
		else
			job.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		return job;
	}

	public void cutFile(Collection<OpenPath> files, OpenPath newPath, Context mContext) {
		OpenPath[] array = new OpenPath[files.size()];
		files.toArray(array);

		execute(new BackgroundWork(CUT_TYPE, mContext, newPath),array);
	}

	public void searchFile(OpenPath dir, String query, Context mContext) {
		execute(new BackgroundWork(SEARCH_TYPE, mContext, dir, query));
	}

	public BackgroundWork zipFile(OpenPath into, Collection<OpenPath> files,
			Context mContext) {
		return zipFile(into, files.toArray(new OpenPath[0]), mContext);
	}

	public BackgroundWork zipFile(OpenPath into, OpenPath[] files,
			Context mContext) {
		return (BackgroundWork)execute(new BackgroundWork(ZIP_TYPE, mContext, into), files);
	}

	public void unzipFile(final OpenPath file, final Context mContext) {
		final OpenPath into = file.getParent().getChild(
				file.getName().replace("." + file.getExtension(), ""));
		// AlertDialog.Builder b = new AlertDialog.Builder(mContext);
		final InputDialog dUnz = new InputDialog(mContext);
		dUnz.setTitle(
				getResourceString(mContext, R.string.s_title_unzip).toString()
						.replace("xxx", file.getName()))
				.setMessage(
						getResourceString(mContext, R.string.s_prompt_unzip)
								.toString().replace("xxx", file.getName()))
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
						Logger.LogVerbose("Unzipping " + file.getPath()
								+ " into " + path);
						execute(new BackgroundWork(UNZIP_TYPE, mContext, path), file);
					}
				}).setNegativeButton(android.R.string.cancel, null)
				.setDefaultText(into.getPath()).create().show();
	}

	/*
	 * public void unZipFileTo(OpenPath zipFile, OpenPath toDir, Context
	 * mContext) { new BackgroundWork(UNZIPTO_TYPE, mContext,
	 * toDir).execute(zipFile); }
	 */

	/**
	 * Do work on second thread class
	 * 
	 * @author Joe Berria
	 */
	public class BackgroundWork extends AsyncTask<OpenPath, Integer, Integer>
			implements OnProgressUpdateCallback {
		private final EventType mType;
		private final Context mContext;
		private final String[] mInitParams;
		private final OpenPath mIntoPath;
		private ProgressDialog mPDialog;
		private Notification mNote = null;
		private final int mNotifyId;
		private ArrayList<String> mSearchResults = null;
		private boolean isDownload = false;
		private int taskId = -1;
		private final Date mStart;
		private long mLastRate = 0;
		private long mRemain = 0l;
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

		public BackgroundWork(EventType type, Context context,
				OpenPath intoPath, String... params) {
			mType = type;
			mContext = context;
			mInitParams = params;
			mIntoPath = intoPath;
			if (mNotifier == null)
				mNotifier = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
			taskId = mTasks.size();
			mTasks.add(this);
			mStart = new Date();
			mNotifyId = BACKGROUND_NOTIFICATION_ID + EventCount++;
			if (mTaskListener != null)
				mTaskListener.OnTasksChanged(getRunningTasks().length);
		}

		public String getTitle() {
			String title = getResourceString(mContext,
					R.string.s_title_executing);
			switch (mType) {
			case DELETE:
				title = getResourceString(mContext, R.string.s_title_deleting)
						.toString();
				break;
			case SEARCH:
				title = getResourceString(mContext, R.string.s_title_searching)
						.toString();
				break;
			case COPY:
				title = getResourceString(mContext, R.string.s_title_copying)
						.toString();
				break;
			case CUT:
				title = getResourceString(mContext, R.string.s_title_moving)
						.toString();
				break;
			case UNZIP:
			case UNZIPTO:
				title = getResourceString(mContext, R.string.s_title_unzipping)
						.toString();
				break;
			case ZIP:
				title = getResourceString(mContext, R.string.s_title_zipping)
						.toString();
				break;
			case MKDIR:
				title = getResourceString(mContext, R.string.s_menu_rename)
						.toString();
				break;
			case TOUCH:
				title = getResourceString(mContext, R.string.s_create)
						.toString();
				break;
			}
			title += " " + '\u2192' + " " + mIntoPath;
			return title;
		}

		public String getSubtitle() {
			String subtitle = "";
			if (mInitParams != null && mInitParams.length > 0)
				subtitle = (mInitParams.length > 1 ? mInitParams.length + " "
						+ mContext.getString(R.string.s_files) : mInitParams[0]);
			return subtitle;
		}

		public String getLastRate() {
			if (getStatus() == Status.FINISHED)
				return getResourceString(mContext, R.string.s_complete);
			else if (getStatus() == Status.PENDING)
				return getResourceString(mContext, R.string.s_pending);
			if (isCancelled())
				return getResourceString(mContext, R.string.s_cancelled);
			if (mRemain > 0) {
				Integer min = (int) (mRemain / 60);
				Integer sec = (int) (mRemain % 60);
				return getResourceString(mContext, R.string.s_status_remaining)
						+ (min > 15 ? ">15m" : (min > 0 ? min + ":" : "")
								+ (min > 0 && sec < 10 ? "0" : "") + sec
								+ (min > 0 ? "" : "s"));
			}
			if (mLastRate > 0)
				return getResourceString(mContext, R.string.s_status_rate)
						+ DialogHandler.formatSize(mLastRate).replace(" ", "")
								.toLowerCase() + "/s";
			return "";
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
			boolean showDialog = true, showNotification = false, isCancellable = true;
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
			default:
				showDialog = showNotification = false;
				break;
			}
			if (showDialog)
				try {
					mPDialog = ProgressDialog.show(mContext, getTitle(),
							getResourceString(mContext, R.string.s_title_wait)
									.toString(), true, true,
							new DialogInterface.OnCancelListener() {
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
			return notifIcon;
		}

		@SuppressLint("NewApi")
		public void prepareNotification(int notifIcon, boolean isCancellable) {
			boolean showProgress = true;
			try {
				Intent intent = new Intent(mContext, OpenExplorer.class);
				intent.putExtra("TaskId", taskId);
				PendingIntent pendingIntent = PendingIntent.getActivity(
						mContext, OpenExplorer.REQUEST_VIEW, intent, 0);
				mNote = new Notification(notifIcon, getTitle(), System.currentTimeMillis());
				if (showProgress) {
					PendingIntent pendingCancel = PendingIntent.getActivity(
							mContext, OpenExplorer.REQUEST_VIEW, intent, 0);
					RemoteViews noteView = new RemoteViews(
							mContext.getPackageName(), R.layout.notification);
					if (isCancellable)
						noteView.setOnClickPendingIntent(android.R.id.button1,
								pendingCancel);
					else
						noteView.setViewVisibility(android.R.id.button1,
								View.GONE);
					noteView.setImageViewResource(android.R.id.icon,
							R.drawable.icon);
					noteView.setTextViewText(android.R.id.title, getTitle());
					noteView.setTextViewText(android.R.id.text2, getSubtitle());
					noteView.setViewVisibility(android.R.id.closeButton,
							View.GONE);
					if (SHOW_NOTIFICATION_STATUS) {
						noteView.setTextViewText(android.R.id.text1,
								getLastRate());
						noteView.setProgressBar(android.R.id.progress, 100, 0,
								true);
					}
					// noteView.setViewVisibility(R.id.title_search, View.GONE);
					// noteView.setViewVisibility(R.id.title_path, View.GONE);
					mNote.contentView = noteView;
					if (!OpenExplorer.BEFORE_HONEYCOMB)
						mNote.tickerView = noteView;
				} else {
					mNote.tickerText = getTitle();
				}
				mNote.contentIntent = pendingIntent;
				mNote.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
				// mNote.flags |= Notification.FLAG_ONGOING_EVENT;
				if (!isCancellable)
					mNote.flags |= Notification.FLAG_NO_CLEAR;
				// mNotifier.notify(mNotifyId, mNote);
				notifReady = true;
			} catch (Exception e) {
				Logger.LogWarning("Couldn't post notification", e);
			}
		}

		public void searchDirectory(OpenPath dir, String pattern,
				ArrayList<String> aList) {
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
			int len = params.length;
			int ret = 0;

			switch (mType) {

			case DELETE:
				for (int i = 0; i < len; i++)
					ret += mFileMang.deleteTarget(params[i]);
				break;
			case SEARCH:
				mSearchResults = new ArrayList<String>();
				searchDirectory(mIntoPath, mInitParams[0], mSearchResults);
				break;
			case RENAME:
				ret += FileManager.renameTarget(mIntoPath.getPath(),
						mInitParams[0]) ? 1 : 0;
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
				// / TODO: Add existing file check
				for (OpenPath file : params) {
					if (file.requiresThread()) {
						isDownload = true;
						this.publishProgress();
					}
					try {
						if (copyToDirectory(file, mIntoPath, 0))
							ret++;
					} catch (IOException e) {
						Logger.LogError("Couldn't copy file (" + file.getName()
								+ " to " + mIntoPath.getPath() + ")", e);
					}
				}
				break;
			case CUT:
				for (OpenPath file : params) {
					try {
						if (file.requiresThread()) {
							isDownload = true;
							this.publishProgress();
						}
						if (copyToDirectory(file, mIntoPath, 0)) {
							ret++;
							mFileMang.deleteTarget(file);
						}
					} catch (IOException e) {
						Logger.LogError("Couldn't copy file (" + file.getName()
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
			}

			return ret;
		}

		/*
		 * More efficient Channel based copying
		 */
		private Boolean copyFileToDirectory(final OpenFile source,
				OpenFile into, final int total) {
			Logger.LogVerbose("Using Channel copy");
			if (into.isDirectory() || !into.exists())
				into = into.getChild(source.getName());
			if (source.getPath().equals(into.getPath()))
				return false;
			final OpenFile dest = (OpenFile) into;
			final boolean[] running = new boolean[] { true };
			final long size = source.length();
			if (size > 50000)
				new Thread(new Runnable() {
					@Override
					public void run() {
						while ((int) dest.length() < total || running[0]) {
							long pos = dest.length();
							publish((int) pos, (int) size, total);
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

		private Boolean copyToDirectory(OpenPath old, OpenPath intoDir,
				int total) throws IOException {
			if (old.equals(intoDir)) return false;
			if (old instanceof OpenFile && !old.isDirectory()
					&& intoDir instanceof OpenFile)
				if (copyFileToDirectory((OpenFile) old, (OpenFile) intoDir,
						total))
					return true;
			if (old instanceof OpenSMB && intoDir instanceof OpenSMB) {
				Logger.LogVerbose("EventHandler.copyToDirectory : Using OpenSMB Channel copy");
				if (((OpenSMB) old).copyTo((OpenSMB) intoDir))
					return true;
			} /*else if(old instanceof OpenSMB && intoDir instanceof OpenFile) {
				Logger.LogVerbose("EventHandler.copyToDirectory : Using OpenSMB Copy");
				((OpenSMB)old).copyTo((OpenFile)intoDir, this);
				if(intoDir.length() > 0)
					return true;
			}*/
			if(!intoDir.canWrite()) return false;
			Logger.LogVerbose("EventHandler.copyToDirectory : Using Stream copy");
			if (intoDir instanceof OpenCursor) {
				try {
					if (old.isImageFile() && intoDir.getName().equals("Photos")) {
						if (Images.Media.insertImage(
								mContext.getContentResolver(), old.getPath(),
								old.getName(), null) != null)
							return true;
					}
				} catch (Exception e) {
					return false;
				}
			}
			OpenPath newDir = intoDir;
			if (intoDir instanceof OpenSmartFolder) {
				newDir = ((OpenSmartFolder) intoDir).getFirstDir();
				if (old instanceof OpenFile && newDir instanceof OpenFile)
					return copyFileToDirectory((OpenFile) old,
							(OpenFile) newDir, total);
			}
			Logger.LogDebug("EventHandler.copyToDirectory : Trying to copy [" + old.getPath() + "] to ["
					+ intoDir.getPath() + "]...");
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
						total += (int) file.length();

				for (int i = 0; i < files.length; i++)
					if (files[i] != null
							&& !copyToDirectory(files[i], newDir, total)) {
						Logger.LogWarning("Couldn't copy " + files[i].getName()
								+ ".");
						return false;
					}

				return true;

			//} else if(old instanceof OpenSMB && newDir instanceof OpenFile) {
			//	((OpenSMB)old).copyTo((OpenFile)newDir, this);
			} else if (old.isFile() && newDir.isDirectory()
					&& newDir.canWrite()) {
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

				int size = (int) old.length();
				int pos = 0;

				BufferedInputStream i_stream = null;
				BufferedOutputStream o_stream = null;
				boolean success = false;
				try {
					Logger.LogDebug("Writing " + newFile.getPath());
					i_stream = new BufferedInputStream(old.getInputStream());
					o_stream = new BufferedOutputStream(
							newFile.getOutputStream());

					while ((read = i_stream.read(data, 0, FileManager.BUFFER)) != -1) {
						o_stream.write(data, 0, read);
						pos += FileManager.BUFFER;
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
					FileOutputStream out = (FileOutputStream) newFile
							.getOutputStream();
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
			return (int) (((float) mLastProgress[0] / (float) mLastProgress[1]) * 1000f);
		}

		public int getProgressB() {
			return (int) (((float) mLastProgress[0] / (float) mLastProgress[2]) * 1000f);
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

			int progA = (int) (((float) current / (float) size) * 1000f);
			int progB = (int) (((float) current / (float) total) * 1000f);

			long running = new Date().getTime() - mStart.getTime();
			if (running / 1000 > 0) {
				mLastRate = ((long) current) / (running / 1000);
				if (mLastRate > 0)
					mRemain = Utils.getAverage(mRemain, (long) (size - current)
							/ mLastRate);
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
					RemoteViews noteView = mNote.contentView;
					noteView.setTextViewText(android.R.id.text1, getLastRate());
					if (values.length == 0 && isDownload)
						noteView.setImageViewResource(android.R.id.icon,
								android.R.drawable.stat_sys_download);
					else
						noteView.setImageViewResource(android.R.id.icon,
								android.R.drawable.stat_notify_sync);
					noteView.setProgressBar(android.R.id.progress, 1000, progA,
							values.length == 0);
					mNotifier.notify(mNotifyId, mNote);
					// noteView.notify();
					// noteView.
				} catch (Exception e) {
					Logger.LogWarning("Couldn't update notification progress.",
							e);
				}

			}
		}

		public void updateView(final View view) {
			if (view == null)
				return;
			Runnable runnable = new Runnable() {
				public void run() {
					ViewUtils.setImageResource(view, getNotifIconResId(),
							android.R.id.icon);
					ViewUtils.setText(view, getTitle(), android.R.id.title);
					ViewUtils.setText(view, getLastRate(), android.R.id.text1);
					ViewUtils.setText(view, getSubtitle(), android.R.id.text2);
					if (view.findViewById(android.R.id.progress) != null) {
						int progA = (int) (((float) mLastProgress[0] / (float) mLastProgress[1]) * 1000f);
						int progB = (int) (((float) mLastProgress[0] / (float) mLastProgress[2]) * 1000f);
						if (getStatus() == Status.FINISHED)
							ViewUtils.setViewsVisible(view, false,
									android.R.id.closeButton,
									android.R.id.progress);
						else {
							ProgressBar pb = (ProgressBar) view
									.findViewById(android.R.id.progress);
							pb.setIndeterminate(mLastRate == 0);
							pb.setMax(1000);
							pb.setProgress(progA);
							pb.setSecondaryProgress(progB);
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
			
			mTasks.remove(this);
			
			if(mTasks.size() == 0)
				mNotifier.cancelAll();

			switch (mType) {

			case DELETE:
				if (result == 0)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_none,
									R.string.s_msg_deleted), Toast.LENGTH_SHORT)
							.show();
				else if (result == -1)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_some,
									R.string.s_msg_deleted), Toast.LENGTH_SHORT)
							.show();
				else
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_all,
									R.string.s_msg_deleted), Toast.LENGTH_SHORT)
							.show();
				break;

			case SEARCH:
				OnWorkerThreadComplete(
						mType,
						mSearchResults.toArray(new String[mSearchResults.size()]));
				return;

			case COPY:
				if (result == null || result == 0)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_none,
									R.string.s_msg_copied), Toast.LENGTH_SHORT)
							.show();
				else if (result != null && result < 0)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_some,
									R.string.s_msg_copied), Toast.LENGTH_SHORT)
							.show();
				else
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_all,
									R.string.s_msg_copied), Toast.LENGTH_SHORT)
							.show();
				break;
			case CUT:
				if (result == null || result == 0)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_none,
									R.string.s_msg_moved), Toast.LENGTH_SHORT)
							.show();
				else if (result != null && result < 0)
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_some,
									R.string.s_msg_moved), Toast.LENGTH_SHORT)
							.show();
				else
					Toast.makeText(
							mContext,
							getResourceString(mContext, R.string.s_msg_all,
									R.string.s_msg_moved), Toast.LENGTH_SHORT)
							.show();

				break;

			case UNZIPTO:
				break;

			case UNZIP:
				break;

			case ZIP:
				break;
			}
			OnWorkerThreadComplete(mType);
		}
	}

}

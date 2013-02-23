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

package org.brandroid.openmanager.fragments;

import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.openmanager.adapters.HeatmapAdapter;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.HelpStringHelper;
import org.brandroid.openmanager.util.OpenChromeClient;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.view.MenuItem;

public class DialogHandler {

    public static View createFileInfoDialog(OpenApp app, LayoutInflater inflater, OpenPath mPath) {
        View v = inflater.inflate(R.layout.info_layout, null);
        /*
         * v.setOnTouchListener(new OnTouchListener() { public boolean
         * onTouch(View v, MotionEvent event) { getDialog().hide(); return
         * false; } });
         */

        try {
            populateFileInfoViews(app, v, mPath);
        } catch (IOException e) {
            Logger.LogError("Couldn't create info dialog", e);
        }

        return v;
    }

    public static View createFileHeatmapDialog(final OpenApp app, LayoutInflater inflater,
            OpenPath file) {
        View v = inflater.inflate(R.layout.heatmap_layout, null);

        final HeatmapAdapter adapter = new HeatmapAdapter(app, file);

        ListView lv = (ListView)v.findViewById(android.R.id.list);
        final TextView mTotalSize = (TextView)v.findViewById(R.id.heatmap_total);
        Button mRefresh = (Button)v.findViewById(R.id.heatmap_refresh);
        mRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyDataSetChanged();
            }
        });

        adapter.setHeatmapCallback(new HeatmapAdapter.HeatmapCallback() {
            @Override
            public void OnHeatmapTasksComplete(long mTotalBytes, boolean allDone) {
                mTotalSize.setText(app.getContext().getResources().getString(R.string.s_size)
                        + ": " + formatSize(mTotalBytes) + (allDone ? "" : "..."));
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OpenPath path = adapter.getItem(position);
                if (path.isDirectory())
                    showFileHeatmap(app, path);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                OpenPath path = adapter.getItem(position);
                IconContextMenu icm = new IconContextMenu(app.getContext(), R.menu.context_file,
                        view);
                icm.show();
                return true;
            }
        });

        lv.setAdapter(adapter);

        return v;
    }

    public static String formatSize(long size) {
        return formatSize(size, true);
    }

    public static String formatSize(long size, boolean includeUnits) {
        return formatSize(size, 2, includeUnits);
    }

    public static String formatSize(long size, int decimalPoints) {
        return formatSize(size, decimalPoints, true);
    }

    public static String formatSize(long size, int decimalPoints, boolean includeUnits) {
        int kb = 1024;
        int mb = kb * 1024;
        int gb = mb * 1024;
        String ssize = "";

        int factor = (10 ^ decimalPoints);

        if (size < kb)
            ssize = size + " B";
        else if (size > kb && size < mb)
            ssize = ((double)Math.round(((double)size / kb) * factor) / factor)
                    + (includeUnits ? " KB" : "");
        else if (size > mb && size < gb)
            ssize = ((double)Math.round(((double)size / mb) * factor) / factor)
                    + (includeUnits ? " MB" : "");
        else if (size > gb)
            ssize = ((double)Math.round(((double)size / gb) * factor) / factor)
                    + (includeUnits ? " GB" : "");

        return ssize;
    }

    public static void populateFileInfoViews(OpenApp app, View v, OpenPath file) throws IOException {

        if (file instanceof OpenMediaStore)
            file = ((OpenMediaStore)file).getFile();
        Date date = new Date();
        if (file.lastModified() != null)
            date = new Date(file.lastModified());

        TextView numDir = (TextView)v.findViewById(R.id.info_dirs_label);
        TextView numFile = (TextView)v.findViewById(R.id.info_files_label);
        TextView numSize = (TextView)v.findViewById(R.id.info_size);
        TextView numTotal = (TextView)v.findViewById(R.id.info_total_size);
        TextView numFree = (TextView)v.findViewById(R.id.info_free_size);

        // if (file.isDirectory()) {

        new CountAllFilesTask(numDir, numFile, numSize, numFree, numTotal).execute(file);

        // } else {
        // numFile.setText("-");
        // numDir.setText("-");
        // }

        // ((TextView)v.findViewById(R.id.info_name_label)).setText(file.getName());
        ((TextView)v.findViewById(R.id.info_time_stamp)).setText(date.toString());
        ((TextView)v.findViewById(R.id.info_path_label)).setText(file.getPath());
        ((TextView)v.findViewById(R.id.info_read_perm)).setText(file.canRead() + "");
        ((TextView)v.findViewById(R.id.info_write_perm)).setText(file.canWrite() + "");
        ((TextView)v.findViewById(R.id.info_execute_perm)).setText(file.canExecute() + "");

        if (file.isDirectory())
            ((ImageView)v.findViewById(R.id.info_icon)).setImageResource(R.drawable.lg_folder);
        else
            ((ImageView)v.findViewById(R.id.info_icon)).setImageDrawable(getFileIcon(app, file,
                    false));
    }

    public static void populateFileHeatmapList(OpenApp app, View v, OpenPath file) {
    }

    public static Drawable getFileIcon(OpenApp app, OpenPath file, boolean largeSize) {
        return new BitmapDrawable(ThumbnailCreator.generateThumb(app, file, 96, 96,
                app.getContext()).get());
    }

    public static class CountAllFilesTask extends AsyncTask<OpenPath, Integer, String[]> {
        private TextView mTextFiles, mTextDirs, mTextSize, mTextFree, mTextTotal;
        private int firstDirs = 0, firstFiles = 0;
        private int dirCount = 0, fileCount = 0;
        private long totalSize = 0, firstSize = 0;
        private long freeSize = 0l, diskTotal = 0l;

        public CountAllFilesTask(TextView mTextFiles, TextView mTextDirs, TextView mTextSize,
                TextView mTextFree, TextView mTextTotal) {
            this.mTextFiles = mTextFiles;
            this.mTextDirs = mTextDirs;
            this.mTextSize = mTextSize;
            this.mTextFree = mTextFree;
            this.mTextTotal = mTextTotal;
        }

        private void addPath(OpenPath p, boolean bFirst) {
            if (!p.isDirectory()) {
                fileCount++;
                totalSize += p.length();
                if (bFirst) {
                    firstFiles++;
                    firstSize += p.length();
                }
            } else {
                dirCount++;
                if (bFirst)
                    firstDirs++;
                try {
                    for (OpenPath f : p.list())
                        addPath(f, false);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (fileCount + dirCount % 50 == 0)
                publishProgress();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateTexts(mTextFiles, fileCount, mTextDirs, dirCount, mTextSize,
                    formatSize(totalSize), mTextFree, formatSize(freeSize), mTextTotal,
                    formatSize(diskTotal));
        }

        @Override
        protected String[] doInBackground(OpenPath... params) {
            OpenPath path = params[0];

            totalSize = path.length();
            if (!path.isDirectory()) {
                firstFiles = fileCount = 1;
                firstDirs = dirCount = 0;
            }

            publishProgress();

            if (path instanceof OpenFile) {
                freeSize = ((OpenFile)path).getFreeSpace();
                diskTotal = ((OpenFile)path).getTotalSpace();
                publishProgress();
            } else if (path instanceof OpenMediaStore) {
                OpenMediaStore ms = (OpenMediaStore)path;
                freeSize = ms.getFile().getFreeSpace();
                diskTotal = ms.getFile().getTotalSpace();
                publishProgress();
            } else if (path instanceof OpenSMB) {
                try {
                    SmbFile smb = ((OpenSMB)path).getFile();
                    freeSize = smb.getDiskFreeSpace();
                    String server = smb.getServer();
                    if (server == null)
                        diskTotal = smb.length();
                    else
                        diskTotal = new SmbFile((server.startsWith("smb://") ? "" : "smb://")
                                + server + (server.endsWith("/") ? "" : "/")).length();
                } catch (SmbException e) {
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                publishProgress();
            }

            if (path.isDirectory())
                addPath(path, true);

            String[] ret = new String[] {
                    firstDirs + (dirCount > firstDirs ? " (" + dirCount + ")" : ""),
                    firstFiles + (fileCount > firstFiles ? " (" + fileCount + ")" : ""),
                    formatSize(totalSize), formatSize(freeSize), formatSize(diskTotal)
            };
            return ret;
        }

        public void updateTexts(Object... params) {
            for (int i = 0; i < params.length - 1; i += 2) {
                if (params[i] != null && params[i] instanceof TextView)
                    ((TextView)params[i]).setText(params[i + 1].toString());
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateTexts(mTextFiles, "-", mTextDirs, "-", mTextSize, "-", mTextFree, "-",
                    mTextTotal, "-");
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (mTextFiles != null && result != null && result.length > 0)
                mTextFiles.setText(result[0]);
            if (mTextDirs != null && result != null && result.length > 1)
                mTextDirs.setText(result[1]);
            if (mTextSize != null && result != null && result.length > 2)
                mTextSize.setText(result[2]);

            if (mTextFree != null && result != null && result.length > 3 && !result[3].equals("0"))
                mTextFree.setText(result[3]);
            else if (mTextFree != null)
                ((View)mTextFree.getParent()).setVisibility(View.GONE);

            if (mTextTotal != null && result != null && result.length > 4 && !result[4].equals("0"))
                mTextTotal.setText(result[4]);
            else if (mTextTotal != null)
                ((View)mTextTotal.getParent()).setVisibility(View.GONE);
        }

    }

    public static void showFileInfo(final OpenApp app, final OpenPath path) {
        final Context mContext = app.getContext();
        try {
            new AlertDialog.Builder(mContext)
                    .setView(
                            createFileInfoDialog(app, (LayoutInflater)mContext
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE), path))
                    .setTitle(path.getName())
                    .setIcon(
                            new BitmapDrawable(mContext.getResources(), path.getThumbnail(app,
                                    ContentFragment.mListImageSize, ContentFragment.mListImageSize)
                                    .get())).create().show();
        } catch (Exception e) {
            Logger.LogError("Couldn't show File Info.", e);
        }
        // DialogHandler dialogInfo =
        // DialogHandler.newDialog(DialogHandler.DialogType.FILEINFO_DIALOG,
        // this);
        // dialogInfo.setFilePath(path.getPath());
        // dialogInfo.show(fragmentManager, "info");
    }

    public static void showFileHeatmap(final OpenApp app, final OpenPath path) {
        final Context mContext = app.getContext();
        try {
            new AlertDialog.Builder(mContext)
                    .setView(
                            createFileHeatmapDialog(app, (LayoutInflater)mContext
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE), path))
                    .setTitle(path.getName())
                    .setIcon(
                            new BitmapDrawable(mContext.getResources(), path.getThumbnail(app,
                                    ContentFragment.mListImageSize, ContentFragment.mListImageSize)
                                    .get())).create().show();
        } catch (Exception e) {
            Logger.LogError("Couldn't show File Info.", e);
        }
    }

    /**
     * Show a warning that has a specific count down to auto-cancel.
     * 
     * @param context Context.
     * @param msg Message String ID.
     * @param countSecs Length in seconds to show message before
     *            auto-cancelling.
     * @param onOK Callback for when "OK" is selected.
     * @return
     */
    public static AlertDialog showWarning(final Context context, int msg, int countSecs,
            DialogInterface.OnClickListener onOK) {
        final Timer timer = new Timer("Count Down");
        final AlertDialog dlg = new AlertDialog.Builder(context).setMessage(msg)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        timer.cancel();
                    }
                }).setCancelable(true).setPositiveButton(android.R.string.ok, onOK).show();
        final int[] cnt = new int[] {
                countSecs
        };
        final Button btCancel = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (dlg.isShowing()) {
                    btCancel.post(new Runnable() {
                        public void run() {
                            btCancel.setText(context.getResources().getString(
                                    android.R.string.cancel)
                                    + " (" + cnt[0]-- + ")");
                        }
                    });
                } else
                    cnt[0] = 0;
                if (cnt[0] <= 0)
                    cancel();
            }

            @Override
            public boolean cancel() {
                dlg.cancel();
                return super.cancel();
            }
        };
        timer.scheduleAtFixedRate(tt, 0, 1000);
        return dlg;
    }

    /**
     * Show a warning message with option to "Always Remember" choice
     * 
     * @param context Context.
     * @param text Message to show.
     * @param title Title of Dialog.
     * @param preferences Preferences object from which to pull pref_key. This
     *            will be placed in the "warn" SharedPreference.
     * @param pref_key Preference Key holding whether or not to show warning.
     * @param onYes Callback for when Yes is chosen. This will be called
     *            automatically if "Do not ask again" is selected.
     */
    public static void showConfirmationDialog(Context context, String text, String title,
            final Preferences preferences, final String pref_key,
            final DialogInterface.OnClickListener onYes) {

        if (!preferences.getBoolean("warn", pref_key, false)) {
            final View layout = inflate(context, R.layout.confirm_view);

            final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title)
                    .setView(layout).create();

            ViewUtils.setText(layout, text, R.id.confirm_message);

            ViewUtils.setOnClicks(layout, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.confirm_remember) {
                        CheckBox me = (CheckBox)v;
                        preferences.setSetting("warn", pref_key, me.isChecked());
                        ViewUtils.setViewsEnabled(layout, !me.isChecked(), R.id.confirm_no);
                    } else if (v.getId() == R.id.confirm_no) {
                        preferences.setSetting("warn", pref_key, false);
                        dialog.dismiss();
                    } else if (v.getId() == R.id.confirm_yes) {
                        onYes.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                }
            }, R.id.confirm_remember, R.id.confirm_yes, R.id.confirm_no);

            if (context != null)
                dialog.show();
        } else
            onYes.onClick(null, DialogInterface.BUTTON_POSITIVE);
    }

    public static void showConfirmationDialog(Context context, String text, String title,
            final Preferences preferences, final String pref_key,
            final DialogInterface.OnClickListener onClick,
            final Integer resYesId,
            final Integer resNoId,
            final Integer resCancelId) {

        int ret = preferences.getInt("warn", pref_key, -1);
        if(ret > -1)
        {
            onClick.onClick(null, ret);
            return;
        }
        if (ret == -1) {
            final View layout = inflate(context, R.layout.confirm_view);
            final CheckBox cb = (CheckBox)layout.findViewById(R.id.confirm_remember);
            
            final Builder builder = new AlertDialog.Builder(context).setTitle(title)
                    .setView(layout);
            builder.setPositiveButton(context.getResources().getString(resYesId),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(cb != null && cb.isChecked())
                                preferences.setSetting("warn", pref_key, resYesId);
                            onClick.onClick(dialog, resYesId);
                        }
                    });
            if (resNoId != null)
                builder.setNegativeButton(context.getResources().getString(resNoId),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb != null && cb.isChecked())
                                    preferences.setSetting("warn", pref_key, resNoId);
                                onClick.onClick(dialog, resNoId);
                            }
                        });
            if (resCancelId != null)
                builder.setNeutralButton(context.getResources().getString(resCancelId),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb != null && cb.isChecked())
                                    preferences.setSetting("warn", pref_key, resCancelId);
                                onClick.onClick(dialog, resCancelId);
                            }
                        });
            final AlertDialog dialog = builder.create();

            ViewUtils.setText(layout, text, R.id.confirm_message);
            ViewUtils.setViewsVisible(layout, false, R.id.confirm_buttons);
            
            if (context != null)
                dialog.show();
        }
    }

    public static View inflate(Context context, int layoutId) {
        return ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                layoutId, null);
    }

    public static boolean showHelpDialog(Context context, String msgCode) {
        int msg = -1, title = -1;
        msg = Utils.getResId("help_" + msgCode, context, R.string.class);
        if (msg <= 0)
            return false;
        title = Utils.getResId("help_" + msgCode + "_title", context, R.id.class);
        return showHelpDialog(context, msg, title);
    }

    public static boolean showHelpDialog(final Context context, int msg, int title) {
        try {
            final View layout = inflate(context, R.layout.alert_help_view);
            final AlertDialog dialog = new AlertDialog.Builder(context).setView(layout).create();
            TextView tvMessage = (TextView)layout.findViewById(R.id.help_message);
            if (tvMessage != null) {
                ViewUtils.setText(layout, msg, R.id.help_message);
                String allHelps = Arrays.toString(HelpStringHelper.getHelpSuffs().toArray())
                        .replace(", ", "|");
                allHelps = "(" + allHelps.substring(1, allHelps.length() - 2) + ")";
                Pattern p = Pattern.compile(allHelps);
                Linkify.addLinks(tvMessage, p, "content://org.brandroid.openmanager/help/");
            }
            if (title > 0)
                dialog.setTitle(context.getText(title));
            ViewUtils.setOnClicks(layout, new OnClickListener() {
                public void onClick(View v) {
                    if (v.getId() == R.id.help_skip) {
                        boolean skip = ((CheckBox)v).isChecked();
                        new Preferences(context).setSetting("warn", "help_skip", skip);
                    }
                }
            }, R.id.help_skip);
            dialog.show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static AlertDialog showMultiButtonDialog(Context context, String message, String title,
            final DialogInterface.OnClickListener listener, final int... buttonStringIds) {

        if (buttonStringIds.length <= 3)
        {
            Builder builder = new AlertDialog.Builder(context)
                    .setTitle(title).setMessage(message);
            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int ret = 0;
                    if (which == DialogInterface.BUTTON_NEUTRAL)
                        ret = 1;
                    else if (which == DialogInterface.BUTTON_NEGATIVE)
                        ret = 2;
                    listener.onClick(dialog, buttonStringIds[ret]);
                }
            };
            builder.setPositiveButton(buttonStringIds[0], click);
            if (buttonStringIds.length > 1)
                builder.setNeutralButton(buttonStringIds[1], click);
            if (buttonStringIds.length > 2)
                builder.setNegativeButton(buttonStringIds[2], click);
            AlertDialog dialog = builder.create();
            dialog.show();
            return dialog;
        } else {

            final View layout = inflate(context, R.layout.alert_multibutton_view);
            ViewUtils.setViewsVisible(layout, false, R.id.confirm_remember);
            ViewUtils.setText(layout, message, R.id.confirm_message);

            final AlertDialog dialog = new AlertDialog.Builder(context).setView(layout).create();

            if (title != null)
                dialog.setTitle(title);

            final ViewGroup buttons = (ViewGroup)layout.findViewById(R.id.buttons);

            buttons.removeAllViews();

            for (final int id : buttonStringIds) {
                Button btn = new Button(context);
                btn.setText(id);
                btn.setId(id);
                ViewGroup.LayoutParams lp = btn.getLayoutParams();
                if (lp == null || !(lp instanceof LinearLayout.LayoutParams))
                    lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                if (lp != null) {
                    ((LinearLayout.LayoutParams)lp).weight = 1;
                    btn.setLayoutParams(lp);
                }
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(dialog, id);
                    }
                });
                buttons.addView(btn);
            }

            dialog.show();
            return dialog;
        }
    }

    public static AlertDialog showConfirmationDialog(final Context context, String msg,
            String title, DialogInterface.OnClickListener onYes) {
        return showConfirmationDialog(context, msg, title, onYes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }, null);
    }

    public static AlertDialog showConfirmationDialog(final Context context, String msg,
            String title, DialogInterface.OnClickListener onYes,
            DialogInterface.OnClickListener onNo, DialogInterface.OnClickListener onCancel) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
                .setNegativeButton(R.string.s_no, onNo).setPositiveButton(R.string.s_yes, onYes)
                .setNeutralButton(R.string.s_cancel, onCancel).show();
    }

    public static AlertDialog showPickerDialog(final Context context, String title, OpenPath path,
            final PickerFragment.OnOpenPathPickedListener onPickListener) {
        final PickerFragment picker = new PickerFragment(context,
                OpenFile.getExternalMemoryDrive(true));
        picker.setOnOpenPathPickedListener(onPickListener);
        Bundle args = new Bundle();
        args.putParcelable("start", OpenFile.getExternalMemoryDrive(true));
        View view = picker.onCreateView(
                (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), null,
                args);
        picker.setDefaultName(path.getName());
        picker.onViewCreated(view, args);
        return new AlertDialog.Builder(context).setTitle(title).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPickListener.onOpenPathPicked(picker.getPath());
                    }
                }).setNegativeButton(android.R.string.cancel, null).setCancelable(true).show();
    }

    public static AlertDialog showSeekBarDialog(final Context context, String title, int progress,
            int max, OnSeekBarChangeListener onSeekListener) {
        FrameLayout view = new FrameLayout(context);
        SeekBar sb = new SeekBar(context);
        view.addView(sb);
        view.setPadding(20, 20, 20, 20);

        sb.setOnSeekBarChangeListener(onSeekListener);
        sb.setProgress(progress);
        sb.setMax(max);
        return new AlertDialog.Builder(context).setTitle(title).setView(view).show();
    }

    public static void showAboutDialog(final OpenApp mApp) {
        final Context mContext = mApp.getContext();
        LayoutInflater li = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.about, null);

        String sVersionInfo = "";
        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            sVersionInfo += pi.versionName;
            if (!pi.versionName.contains("" + pi.versionCode))
                sVersionInfo += " (" + pi.versionCode + ")";
            if (OpenExplorer.IS_DEBUG_BUILD)
                sVersionInfo += " *debug*";
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String sBuildTime = "";
        try {
            sBuildTime = SimpleDateFormat.getInstance().format(
                    new Date(new ZipFile(mContext.getPackageManager().getApplicationInfo(
                            mContext.getPackageName(), 0).sourceDir).getEntry("classes.dex")
                            .getTime()));
        } catch (Exception e) {
            Logger.LogError("Couldn't get Build Time.", e);
        }

        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        Display d = wm.getDefaultDisplay();
        String sHardwareInfo = "Display:\n";
        sHardwareInfo += "Size: " + d.getWidth() + "x" + d.getHeight() + "\n";
        if (dm != null)
            sHardwareInfo += "Density: " + dm.density + "\n";
        sHardwareInfo += "Rotation: " + d.getRotation() + "\n\n";
        sHardwareInfo += getNetworkInfo(mContext);
        sHardwareInfo += getDeviceInfo();
        ((TextView)view.findViewById(R.id.about_hardware)).setText(sHardwareInfo);

        final String sSubject = "Feedback for OpenExplorer " + sVersionInfo;
        OnClickListener email = new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("text/plain");
                // intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "\n" + getDeviceInfo());
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
                        "brandroid64@gmail.com"
                });
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, sSubject);
                try {
                    OpenFile fAttach = SettingsActivity.GetDefaultServerFile(mContext).getParent()
                            .getChild("oe_logs.txt");
                    ArrayList<Uri> uris = new ArrayList<Uri>();
                    uris.add(fAttach.getUri());
                    fAttach.write(Logger.getLogText());
                    intent.putExtra(android.content.Intent.EXTRA_STREAM, uris);
                } catch (Exception e) {
                    Logger.LogWarning("Unable to attach logs to contact email.", e);
                }
                mContext.startActivity(Intent.createChooser(intent,
                        mContext.getString(R.string.s_chooser_email)));
            }
        };
        OnClickListener viewsite = new OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri
                        .parse("http://brandroid.org/open/")));
            }
        };
        view.findViewById(R.id.about_email).setOnClickListener(email);
        view.findViewById(R.id.about_email_btn).setOnClickListener(email);
        view.findViewById(R.id.about_site).setOnClickListener(viewsite);
        view.findViewById(R.id.about_site_btn).setOnClickListener(viewsite);
        final View mRecentLabel = view.findViewById(R.id.about_recent_status_label);
        final WebView mRecent = (WebView)view.findViewById(R.id.about_recent);
        final OpenChromeClient occ = new OpenChromeClient();
        occ.mStatus = (TextView)view.findViewById(R.id.about_recent_status);
        mRecent.setWebChromeClient(occ);
        mRecent.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {
                occ.mStatus.setVisibility(View.GONE);
                mRecent.setVisibility(View.GONE);
                mRecentLabel.setVisibility(View.GONE);
            }
        });
        mRecent.setBackgroundColor(Color.TRANSPARENT);
        mRecent.loadUrl("http://brandroid.org/open/?show=recent");

        ((TextView)view.findViewById(R.id.about_version)).setText(sVersionInfo);
        if (sBuildTime != "")
            ((TextView)view.findViewById(R.id.about_buildtime)).setText(sBuildTime);
        else
            ((TableRow)view.findViewById(R.id.row_buildtime)).setVisibility(View.GONE);

        // fillShortcutsTable((TableLayout)view.findViewById(R.id.shortcuts_table));

        final View tab1 = view.findViewById(R.id.tab1);
        final View tab2 = view.findViewById(R.id.tab2);
        final View tab3 = view.findViewById(R.id.tab3);
        ((Button)view.findViewById(R.id.btn_recent)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tab1.setVisibility(View.VISIBLE);
                tab2.setVisibility(View.GONE);
                tab3.setVisibility(View.GONE);
            }
        });
        ((Button)view.findViewById(R.id.btn_hardware)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tab1.setVisibility(View.GONE);
                tab2.setVisibility(View.VISIBLE);
                tab3.setVisibility(View.GONE);
            }
        });
        ((Button)view.findViewById(R.id.btn_shortcuts)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tab1.setVisibility(View.GONE);
                tab2.setVisibility(View.GONE);
                tab3.setVisibility(View.VISIBLE);
            }
        });

        tab2.setVisibility(View.VISIBLE);
        tab3.setVisibility(View.GONE);

        AlertDialog mDlgAbout = new AlertDialog.Builder(mContext).setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok, OnClickDismiss)
                .setView(view).create();

        mDlgAbout.getWindow().getAttributes().windowAnimations = R.style.SlideDialogAnimation;
        mDlgAbout.getWindow().getAttributes().alpha = 0.9f;

        mDlgAbout.show();
    }

    public static DialogInterface.OnClickListener OnClickDismiss = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private static void fillShortcutsTable(TableLayout table) {
        final Context context = table.getContext();
        for (int sc : MenuUtils.getMenuShortcuts(context)) {
            TableRow tr = new TableRow(context);
            TextView tv1 = new TextView(context);
            tv1.setGravity(Gravity.RIGHT);
            tv1.setPadding(20, 0, 20, 0);
            char scc = (char)(sc + ('a' - KeyEvent.KEYCODE_A));
            tv1.setText("" + scc);
            TextView tv2 = new TextView(context);
            MenuItem item = MenuUtils.getMenuShortcut(sc);
            tv2.setText(item.getTitle());
            tr.addView(tv1);
            tr.addView(tv2);
            table.addView(tr);
        }
    }

    private static String getNetworkInfoInfo(NetworkInfo info) {
        String ret = "";
        ret += info.getSubtypeName() + "/ ";
        if (info.getState() != null)
            ret += "s=" + info.getState().name() + "/ ";
        if (info.getDetailedState() != null)
            ret += "d=" + info.getDetailedState().name() + "/ ";
        if (info.getExtraInfo() != null)
            ret += "e=" + info.getExtraInfo();
        return ret;
    }

    private static String getReadableIP(int ip) {
        int[] bytes = new int[4];
        bytes[0] = ip & 0xFF;
        bytes[1] = (ip >> 8) & 0xFF;
        bytes[2] = (ip >> 16) & 0xFF;
        bytes[3] = (ip >> 24) & 0xFF;
        return bytes[3] + "." + bytes[2] + "." + bytes[1] + "." + bytes[0];
    }

    private static String getNetworkInterfaces() {
        String ret = "IP Address:";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface ni = en.nextElement();
                for (Enumeration<InetAddress> enumIP = ni.getInetAddresses(); enumIP
                        .hasMoreElements();) {
                    InetAddress ip = enumIP.nextElement();
                    if (!ip.isLoopbackAddress())
                        ret += " " + ip.getHostAddress();
                }
            }
        } catch (SocketException e) {
            Logger.LogError("Couldn't get Network Interfaces", e);
        }
        ret += "\n";
        return ret;
    }

    public static String getNetworkInfo(Context c) {
        String ret = getNetworkInterfaces();

        ConnectivityManager conman = (ConnectivityManager)c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conman == null)
            ret += "No Connectivity?\n";
        else {
            ret += "Connectivity Info:\n" + conman.toString() + "\n";
            for (NetworkInfo ni : conman.getAllNetworkInfo()) {
                if (!ni.isAvailable())
                    continue;
                ret += "Network [" + ni.getTypeName() + "]: " + getNetworkInfoInfo(ni) + "\n";
            }
        }
        try {
            WifiManager wifi = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
            if (wifi == null)
                ret += "No wifi\n";
            else {
                ret += "Wifi Info:\n" + wifi.toString() + "\n";
                ret += "Status: " + (wifi.isWifiEnabled() ? "ENABLED" : "DISABLED") + "\n";
                ret += "ip=" + getReadableIP(wifi.getConnectionInfo().getIpAddress()) + "/ "
                        + "mac=" + wifi.getConnectionInfo().getMacAddress() + "/ " + "b="
                        + wifi.getConnectionInfo().getBSSID() + "/ " + "s="
                        + wifi.getConnectionInfo().getSSID();
                DhcpInfo dh = wifi.getDhcpInfo();
                if (dh == null)
                    ret += "No DHCP\n";
                else {
                    ret += "IP: " + getReadableIP(dh.ipAddress) + "\n";
                    ret += "Gateway: " + getReadableIP(dh.gateway) + "\n";
                    ret += "DNS: " + getReadableIP(dh.dns1) + " " + getReadableIP(dh.dns2) + "\n";
                }
            }
        } catch (SecurityException sec) {
            ret += "No Wifi permissions.\n";
        }
        return ret;
    }

    public static String getLangCode() {
        String lang = Locale.getDefault().toString().toUpperCase();
        if (lang.length() > 2)
            lang = lang.substring(0, 2);
        return lang;
    }

    public static String getDeviceInfo() {
        String ret = "";
        String sep = "\n";
        ret += sep + "Build Info:" + sep;
        ret += "SDK: " + Build.VERSION.SDK_INT + sep;
        if (OpenExplorer.SCREEN_WIDTH > -1)
            ret += "Screen: " + OpenExplorer.SCREEN_WIDTH + "x" + OpenExplorer.SCREEN_HEIGHT + sep;
        if (OpenExplorer.SCREEN_DPI > -1)
            ret += "DPI: " + OpenExplorer.SCREEN_DPI + sep;
        ret += "Lang: " + getLangCode() + sep;
        ret += "Runs: " + Preferences.Run_Count + sep;
        ret += "Fingerprint: " + Build.FINGERPRINT + sep;
        ret += "Manufacturer: " + Build.MANUFACTURER + sep;
        ret += "Model: " + Build.MODEL + sep;
        ret += "Product: " + Build.PRODUCT + sep;
        ret += "Brand: " + Build.BRAND + sep;
        ret += "Board: " + Build.BOARD + sep;
        ret += "Bootloader: " + Build.BOOTLOADER + sep;
        ret += "Hardware: " + Build.HARDWARE + sep;
        ret += "Display: " + Build.DISPLAY + sep;
        ret += "Language: " + Locale.getDefault().getDisplayLanguage() + sep;
        ret += "Country: " + Locale.getDefault().getDisplayCountry() + sep;
        ret += "Tags: " + Build.TAGS + sep;
        ret += "Type: " + Build.TYPE + sep;
        ret += "User: " + Build.USER + sep;
        if (Build.UNKNOWN != null)
            ret += "Unknown: " + Build.UNKNOWN + sep;
        ret += "ID: " + Build.ID;
        return ret;
    }

    public static String formatDuration(long ms) {
        int s = (int)(ms / 1000), m = s / 60, h = m / 60;
        m = m % 60;
        s = s % 60;
        return (ms > 360000 ? h + ":" : "")
                + (ms > 6000 ? (h == 0 || m >= 10 ? "" : "0") + m + ":" : "")
                + (ms > 6000 ? (s >= 10 ? "" : "0") + s : (ms < 1000 ? ms + "ms" : s + "s"));
    }

    public static void showServerWarning(final Context context) {
        if (Preferences.Warn_Networking)
            return;
        Preferences.Warn_Networking = true;
        showWarning(context, R.string.warn_networking, 20, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new Preferences(context).setSetting("warn", "networking", true);
            }
        });
    }

    public static boolean showServerDialog(final OpenApp app, final OpenFTP mPath,
            final BookmarkHolder mHolder, final boolean allowShowPass) {
        return DialogHandler.showServerDialog(app, mPath.getServersIndex(), -1, mHolder,
                allowShowPass);
    }

    public static boolean showServerDialog(final OpenApp app, final OpenNetworkPath mPath,
            final BookmarkHolder mHolder, final boolean allowShowPass) {
        return DialogHandler.showServerDialog(app, mPath.getServersIndex(), -1, mHolder,
                allowShowPass);
    }

    public static boolean showServerDialog(final OpenApp app, final int iServersIndex,
            int serverType, final BookmarkHolder mHolder, final boolean allowShowPass) {
        final Context context = app.getContext();
        final OpenServers servers = SettingsActivity.LoadDefaultServers(context);
        final OpenServer server = iServersIndex > -1 ? servers.get(iServersIndex)
                : new OpenServer().setName("New Server");
        if (serverType > -1) {
            if (serverType == 0)
                server.setType("ftp");
            else if (serverType == 1)
                server.setType("sftp");
            else if (serverType == 2)
                server.setType("smb");
        } else if (server.getType().equals("ftp"))
            serverType = 0;
        else if (server.getType().equals("sftp"))
            serverType = 1;
        else if (server.getType().equals("smb"))
            serverType = 2;
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.server, null);
        if (!OpenServer.setupServerDialog(server, iServersIndex, v))
            return false;
        int addStrId = iServersIndex >= 0 ? R.string.s_update : R.string.s_add;
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(v)
                .setIcon(
                        mHolder != null && mHolder.getIcon(app) != null ? mHolder.getIcon(app)
                                : context.getResources().getDrawable(R.drawable.sm_ftp))
                .setNegativeButton(context.getString(R.string.s_cancel), OnClickDismiss)
                .setNeutralButton(context.getString(R.string.s_remove),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (iServersIndex > -1)
                                    servers.remove(iServersIndex);
                                dialog.dismiss();
                                app.refreshBookmarks();
                            }
                        })
                .setPositiveButton(context.getString(addStrId),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (iServersIndex > -1)
                                    servers.set(iServersIndex, server);
                                else
                                    servers.add(server);
                                SettingsActivity.SaveToDefaultServers(servers, context);
                                app.refreshBookmarks();
                                dialog.dismiss();
                            }
                        }).setTitle(server.getName()).create();
        if (iServersIndex == -1)
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, null, (Message)null);
        /*
         * context.getString(R.string.test), new
         * DialogInterface.OnClickListener() {
         * @Override public void onClick(DialogInterface dialog, int which) {
         * OpenPath path = FileManager.getOpenCache(server.toString()); try {
         * ((OpenNetworkPath)path).connect(); Toast.makeText(context,
         * R.string.test_success, Toast.LENGTH_LONG); } catch(Exception e) {
         * Toast.makeText(context, R.string.httpError, Toast.LENGTH_LONG); } }
         * });
         */

        final AutoCompleteTextView mServerHost = (AutoCompleteTextView)v
                .findViewById(R.id.text_server);
        final ArrayList<String> mHosts = new ArrayList<String>();
        final ArrayAdapter<String> mHostAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, mHosts);
        if (mServerHost != null)
            mServerHost.setAdapter(mHostAdapter);

        final int iServerType = serverType;
        final int[] OnlyOnSMB = new int[] {}; // R.id.server_drop,
                                              // R.id.server_scan};
        final int[] NotOnSMB = new int[] {
                R.id.text_path, R.id.text_path_label, R.id.text_port, R.id.label_port,
                R.id.check_port
        };
        if (OnlyOnSMB.length > 0)
            ViewUtils.setViewsVisible(v, serverType == 2, OnlyOnSMB);
        if (NotOnSMB.length > 0)
            ViewUtils.setViewsVisible(v, serverType != 2, NotOnSMB);

        final Spinner mServerType = (Spinner)v.findViewById(R.id.server_type);
        mServerType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (OnlyOnSMB.length > 0)
                    ViewUtils.setViewsVisible(v, position == 2, OnlyOnSMB);
                if (NotOnSMB.length > 0)
                    ViewUtils.setViewsVisible(v, position != 2, NotOnSMB);
                server.setType("ftp");
                if (position == 1)
                    server.setType("sftp");
                if (position == 2)
                    server.setType("smb");
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mServerType.setSelection(serverType);

        try {
            dialog.show();
            showServerWarning(context);
        } catch (BadTokenException e) {
            Logger.LogError("Couldn't show dialog.", e);
            return false;
        }
        return true;
    }

}

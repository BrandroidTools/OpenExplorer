
package org.brandroid.openmanager.fragments;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.FolderPickerActivity;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.openmanager.adapters.LinesAdapter;
import org.brandroid.openmanager.data.FTPManager;
import org.brandroid.openmanager.data.OpenContent;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.NeedsTempFile;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.views.SeekBarActionView;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.view.CollapsibleActionView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class TextEditorFragment extends OpenFragment implements OnClickListener,
        OpenPathFragmentInterface, TextWatcher, OpenFragment.OnFragmentTitleLongClickListener,
        OnSeekBarChangeListener, OnTouchListener {
    private EditText mEditText;
    private ListView mViewList;
    private LinesAdapter mViewListAdapter = null;
    // private WebView mWebText;
    // private TableLayout mViewTable;
    // private ScrollView mViewScroller;
    private ProgressBar mProgress = null;
    private SeekBarActionView mFontSizeBar = null;
    private TextView mFilename = null;

    private final static int REQUEST_SAVE_AS = OpenExplorer.REQ_SAVE_FILE;

    private OpenPath mPath = null;
    private String mData = null;
    private boolean mDirty = false;
    private long lastClick = 0l;
    private float mTextSize = 10f;
    private boolean mSalvage = true;
    private boolean mWrap = true;
    private boolean mUseFontSizeDialog = false;

    private final static boolean USE_SEEK_ACTIONVIEW = true;

    private AsyncTask<?, ?, ?> mTask = null;

    private boolean mEditMode = false;

    public TextEditorFragment() {
        if (getArguments() != null && getArguments().containsKey("edit_path")) {
            OpenPath path = (OpenPath)getArguments().getParcelable("edit_path");
            Logger.LogDebug("Creating TextEditorFragment @ " + path + " from scratch");
            setPath(path);
        } else
            Logger.LogWarning("Creating orphan TextEditorFragment");
    }

    public TextEditorFragment(OpenPath path) {
        mPath = path;
        Logger.LogDebug("Creating TextEditorFragment @ " + mPath + " from path");
        Bundle b = new Bundle();
        if (path != null && path.getPath() != null)
            b.putParcelable("edit_path", path);
        // setArguments(b);
        setHasOptionsMenu(true);
    }

    public static TextEditorFragment getInstance(OpenPath path, Bundle args) {
        if (args == null)
            args = new Bundle();
        args.putParcelable("edit_path", path);
        TextEditorFragment ret = getInstance(args);
        ret.setPath(path);
        return ret;
    }

    public static TextEditorFragment getInstance(Bundle args) {
        OpenPath path = null;
        if (args != null && args.containsKey("edit_path"))
            path = (OpenPath)args.getParcelable("edit_path");
        TextEditorFragment ret = path != null ? new TextEditorFragment(path)
                : new TextEditorFragment();
        ret.setArguments(args);
        return ret;
    }

    public void setSalvagable(boolean doSave) {
        mSalvage = doSave;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showFilename() {
        showFilename(true);
    }

    @TargetApi(11)
    public void showFilename(final boolean animateOut) {
        if (mFilename == null)
            return;

        if (mPath != null)
            mFilename.setText(mPath.getPath());
        mFilename.setVisibility(View.VISIBLE);
        if (animateOut && Build.VERSION.SDK_INT > 10) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(mFilename, "alpha", 1f, 0f);
            anim.setDuration(1500).setStartDelay(1000);
            anim.start();
        }
        mFilename.postDelayed(new Runnable() {
            public void run() {
                mFilename.setVisibility(View.GONE);
            }
        }, 2500);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getView() != null)
            if (mViewList != null)
                mViewList.setOnTouchListener(this);
        mViewListAdapter = new LinesAdapter(activity, new String[] {});
        try {
            createFontSizeBar(activity);
        } catch (Exception e) {
            Logger.LogWarning("Unable to initialize Font Size Action View");
            mUseFontSizeDialog = true;
        }
        if (!Preferences.Warn_TextEditor && !getSetting("warn", "text_editor", false)) {
            Preferences.Warn_TextEditor = true;
            DialogHandler.showWarning(getActivity(), R.string.warn_text_editor, 10,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSetting("warn", "text_editor", true);
                        }
                    });
        }
        mFontSizeBar.setOnSeekBarChangeListener(this);
        showFilename();
    }

    private void createFontSizeBar(Context context) {
        mFontSizeBar = new SeekBarActionView(context);
        mFontSizeBar.setProgress((int)mTextSize);
        mFontSizeBar.setOnCloseClickListener(new SeekBarActionView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mViewListAdapter.notifySizeChanged();
                return false;
            }
        });
    }

    private void setPath(String path) {
        if (path.startsWith("content://"))
            mPath = new OpenContent(Uri.parse(path), getActivity());
        else
            try {
                mPath = FileManager.getOpenCache(path, false, null);
            } catch (IOException e) {
                mPath = new OpenFile(path);
            }
    }

    private void setPath(OpenPath path) {
        mPath = path;
    }

    public void setProgressVisibility(boolean visible) {
        if (mProgress != null)
            mProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (getExplorer() != null)
            getExplorer().setProgressBarVisibility(visible);
    }

    @Override
    public boolean onBackPressed() {
        if (mEditMode) {
            setEditable(false);
            return true;
        } else {
            doClose();
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);
        Bundle bundle = savedInstanceState;
        if (getArguments() != null)
            bundle = getArguments();
        if (mPath == null && bundle != null && bundle.containsKey("edit_path")) {
            OpenPath mPath = (OpenPath)bundle.getParcelable("edit_path");
            mData = null;
            Logger.LogDebug("load text editor (" + mPath + ")");
            if (mData == null && bundle.containsKey("edit_data"))
                mData = bundle.getString("edit_data");
            if (bundle.containsKey("edit_server")) {
                int serverIndex = bundle.getInt("edit_server");
                Logger.LogDebug("Loading server #" + serverIndex);
                if (serverIndex > -1) {
                    OpenServers servers = SettingsActivity.LoadDefaultServers(getActivity());
                    if (serverIndex < servers.size()) {
                        OpenServer server = servers.get(serverIndex);
                        FTPManager man = new FTPManager(server.getHost(), server.getUser(),
                                server.getPassword(), server.getPath());
                        Logger.LogDebug("Found server - " + server.getName());
                        mPath = new OpenFTP(mPath.getPath(), null, man);
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.text_full, menu);
        MenuItem mFontSize = menu.findItem(R.id.menu_view_font_size);
        // Class clz =
        // Class.forName("org.brandroid.openmanagerviews.SeekBarActionView");
        mUseFontSizeDialog = false;
        boolean isTop = false;
        if (getActivity() != null && getResources() != null
                && !getResources().getBoolean(R.bool.allow_split_actionbar))
            isTop = true;
        if (mFontSizeBar == null) {
            try {
                createFontSizeBar(getContext());
            } catch (Exception e) {
                Logger.LogWarning("Unable to initialize Font Size Action View.");
                mUseFontSizeDialog = true;
            }
        }
        if (mFontSize != null && mFontSizeBar != null && USE_SEEK_ACTIONVIEW) {
            try {
                mFontSize.setActionView(mFontSizeBar);
                mUseFontSizeDialog = true;
            } catch (IllegalStateException e) {
                Logger.LogWarning("Parent already set for Font Size Action View.");
            }
            mFontSize.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (getActivity() == null)
            return;
        if (menu == null)
            return;
        if (isDetached() || !isVisible())
            return;
        super.onPrepareOptionsMenu(menu);
        if (mFontSizeBar != null)
            mFontSizeBar.setProgress((int)(mTextSize * 2));
        MenuUtils.setMenuChecked(menu, mWrap, R.id.menu_text_wrap);
        MenuUtils.setMenuEnabled(menu, mPath.canWrite(), R.id.menu_save);
        MenuUtils.setMenuChecked(menu, mEditMode, R.id.menu_view_keyboard_toggle);
        MenuUtils.setMenuVisible(menu, true, R.id.menu_view_font_size);
        MenuUtils.setMenuChecked(menu, mViewListAdapter.getShowLines(), R.id.menu_view_lines);
        MenuUtils.setMenuVisible(menu, getResources().getBoolean(R.bool.allow_fullscreen),
                R.id.menu_view_fullscreen);
        MenuUtils.setMenuChecked(menu, OpenExplorer.IS_FULL_SCREEN, R.id.menu_view_fullscreen);
        // MenuUtils.setMenuVisible(menu, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // super.onOptionsItemSelected(item);
        if (DEBUG)
            Logger.LogDebug("TextEditorFragment.onOptionsItemSelected(" + item + ")");
        View action = getActionView(item);
        if (action != null && action instanceof CollapsibleActionView) {
            ((CollapsibleActionView)action).onActionViewExpanded();
            return true;
        }
        return onClick(item.getItemId(), action);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_text, null);
        view.setOnTouchListener(this);
        mEditText = (EditText)view.findViewById(R.id.text_edit);
        mViewList = (ListView)view.findViewById(android.R.id.list);
        mFilename = (TextView)view.findViewById(R.id.text_filename);
        showFilename();
        setTextSize(getViewSetting(null, "text_size", mTextSize), true);
        mWrap = getViewSetting(getPath(), "wrap", getViewSetting(null, "text_wrap", true));
        if (mViewListAdapter != null) {
            mViewListAdapter.setShowLines(getViewSetting(null, "text_lines", true));
            mViewListAdapter.setTextWrap(mWrap);
        }
        // mViewTable = (TableLayout)view.findViewById(R.id.text_view_table);
        // mViewScroller =
        // (ScrollView)view.findViewById(R.id.text_view_scroller);
        // mViewText = (TextView)view.findViewById(R.id.text_view);
        // mViewScroller = (ScrollView)view.findViewById(R.id.text_scroller);
        // mWebText = (WebView)view.findViewById(R.id.text_webview);
        // mWebText.getSettings().setJavaScriptEnabled(true);
        mProgress = ((ProgressBar)view.findViewById(android.R.id.progress));
        mEditText.addTextChangedListener(this);
        return view;
    }

    public void setTextSize(float sz) {
        setTextSize(sz, false);
    }

    public void setTextSize(float sz, boolean refresh) {
        mTextSize = sz;
        mEditText.setTextSize(sz);
        if (mViewListAdapter != null)
            mViewListAdapter.setTextSize(sz);
        if (refresh) {
            if (DEBUG)
                Logger.LogDebug("TextEditorFragment.setTextSize(" + sz + ")");
            if (mViewListAdapter != null)
                mViewListAdapter.notifySizeChanged();
            setViewSetting(null, "text_size", sz);
        }
        // mViewText.setTextSize(sz);
    }

    private void refreshList() {
        if (mViewListAdapter != null)
            mViewListAdapter.setLines(mData.split("\n"));

    }

    public void setText(final String txt) {
        if (txt == null)
            return;
        mData = txt;
        if (mEditMode)
            mEditText.post(new Runnable() {
                public void run() {
                    mEditText.setText(txt);
                }
            });
        else
            refreshList();
    }

    @Override
    public int getPagerPriority() {
        return 6;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEditable(mEditMode);
        if (mData == null && savedInstanceState != null
                && savedInstanceState.containsKey("edit_data")
                && (mPath == null || mPath.getPath().equals(savedInstanceState.get("edit_path"))))
            mData = savedInstanceState.getString("edit_data");
        if (mPath != null && mData == null) {
            // mData = getString(R.string.s_status_loading);
            if (mPath instanceof OpenFile) {
                // new Thread(new Runnable() {public void run() {
                try {
                    FileReader fr = new FileReader(((OpenFile)mPath).getFile());
                    char[] data = new char[(int)mPath.length()];
                    fr.read(data);
                    setText(new String(data));
                } catch (FileNotFoundException e) {
                    Logger.LogError("Couldn't find file to load - " + mPath.getPath(), e);
                } catch (IOException e) {
                    Logger.LogError("Couldn't read from file - " + mPath.getPath(), e);
                }
                // }}).start();
            } else
                mTask = new FileLoadTask().execute(mPath);
        } else if (mData != null)
            setText(mData);
        mViewList.setAdapter(mViewListAdapter);
        mViewList.setLongClickable(true);
        mViewList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                long time = new Date().getTime();
                if (time - lastClick < 500)
                    setEditable(true);
                else
                    lastClick = time;
            }
        });
        mViewList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setEditable(true);
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mData = null;
        super.onStop();
    }

    @Override
    public void setInitialSavedState(SavedState state) {
        if (!isAdded())
            try {
                super.setInitialSavedState(state);
                Logger.LogVerbose("setInitialSavedState @ TextEditor (" + mPath + ")");
            } catch (Exception e) {
                Logger.LogWarning("Unable to set Initial State for text editor (" + mPath + ")", e);
            }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mSalvage)
            return;
        if (mPath == null)
            return;
        Logger.LogVerbose("saveInstanceState @ TextEditor (" + mPath.getPath() + ")");
        outState.putParcelable("edit_path", mPath);
        if (mData != null && mData.length() < Preferences.Pref_Text_Max_Size)
            outState.putString("edit_data", mData);
        if (mPath instanceof OpenNetworkPath) {
            if (((OpenNetworkPath)mPath).getServersIndex() > -1) {
                Logger.LogDebug("Saving server #" + ((OpenNetworkPath)mPath).getServersIndex());
                outState.putInt("edit_server", ((OpenNetworkPath)mPath).getServersIndex());
            } else
                Logger.LogWarning("No server #");
        }
        outState.putFloat("size", mTextSize);
    }

    private void doSave() {
        cancelTask();
        mTask = new FileSaveTask(mPath);
        if (mDirty)
            mData = mEditText.getText().toString();
        EventHandler.execute((FileSaveTask)mTask, mData);
    }

    private void doSaveAs() {
        Intent intent = new Intent(getActivity(), FolderPickerActivity.class);
        intent.putExtra("start", (Parcelable)mPath);
        intent.putExtra("name", mPath.getName());
        intent.putExtra("req", REQUEST_SAVE_AS);
        intent.setData(mPath.getUri());
        startActivityForResult(intent, REQUEST_SAVE_AS);
    }

    @Override
    public void doClose() {
        mSalvage = false;
        cancelTask();
        super.doClose();
    }

    public void cancelTask() {
        if (mTask != null)
            mTask.cancel(true);
    }

    @Override
    public void onClick(View v) {
        onClick(v.getId(), v);
    }

    @Override
    public boolean onClick(int id, View from) {
        Context c = getActivity();
        if ((from == null || !from.isShown()) && getExplorer() != null)
            from = getExplorer().findViewById(id);
        if (from != null && from.getTag() != null && from.getTag() instanceof Menu)
            showMenu((Menu)from.getTag(), from, ViewUtils.getText(from));
        switch (id) {
            case R.id.menu_context_info:
                if (c != null)
                    DialogHandler.showFileInfo(this, getPath());
                return true;

            case R.id.menu_save:
                doSave();
                return true;
            case R.id.menu_save_as:
                doSaveAs();
                return true;
            case R.id.menu_close:
                doClose();
                return true;

            case R.id.menu_view_font_size:
                if (mUseFontSizeDialog && mFontSizeBar.isShown())
                    mFontSizeBar.onActionViewExpanded();
                else
                    DialogHandler.showSeekBarDialog(c, getString(R.string.s_view_font_size),
                            (int)mTextSize, 60, this);
                return true;

            case R.id.menu_view_keyboard_toggle:
                setEditable(!mEditMode);
                return true;
            case R.id.menu_view_lines:
                mViewListAdapter.setShowLines();
                setViewSetting(null, "text_lines", mViewListAdapter.getShowLines());
                return true;
            case R.id.menu_text_wrap:
                mWrap = !mWrap;
                ListView list = (ListView)getView().findViewById(android.R.id.list);
                if (list.getParent() instanceof HorizontalScrollView && mWrap) {
                    ((HorizontalScrollView)list.getParent()).removeAllViews();
                    ((ViewGroup)getView().findViewById(R.id.text_view_noscroll)).addView(list);
                    list.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
                } else if (!(list.getParent() instanceof HorizontalScrollView) && !mWrap) {
                    ((ViewGroup)list.getParent()).removeAllViews();
                    ((HorizontalScrollView)getView().findViewById(
                            R.id.text_view_scroller_horizontal)).addView(list);
                    list.getLayoutParams().width = LayoutParams.MATCH_PARENT;
                }
                mViewListAdapter.setTextWrap(mWrap);
                setViewSetting(getPath(), "wrap", mWrap);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.hasExtra("req"))
                requestCode = data.getIntExtra("req", requestCode);
        }
        if (requestCode == REQUEST_SAVE_AS && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra("path"))
                mPath = (OpenPath)data.getParcelableExtra("path");
            else if (data.getData() != null)
                mPath = FileManager.getOpenCache(data.getDataString());
            Logger.LogDebug("Saving " + mPath);
            cancelTask();
            mTask = new FileSaveTask(mPath);
            if (mDirty)
                mData = mEditText.getText().toString();
            EventHandler.execute((FileSaveTask)mTask, mData);
            notifyPager();
        }
    }

    private void setEditable(boolean editable) {
        if (mEditMode == editable)
            return;
        mEditMode = editable;
        if (editable) {
            // mWebText.setVisibility(View.GONE);
            // mViewScroller.setVisibility(View.GONE);
            // mViewScroller.setVisibility(View.GONE);
            mViewList.setVisibility(View.GONE);
            mEditText.setVisibility(View.VISIBLE);
            mEditText.removeTextChangedListener(this);
            setText(mData);
            mEditText.addTextChangedListener(this);
        } else {
            mEditText.removeTextChangedListener(this);
            if (mPath.canWrite() && mDirty) {
                DialogHandler.showConfirmationDialog(
                        getActivity(),
                        getString(R.string.s_alert_dirty, mPath.getName()).replace("%s",
                                mPath.getName()), getText(R.string.s_save).toString() + "?",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                doSave();
                                mEditText.setVisibility(View.GONE);
                                mViewList.setVisibility(View.VISIBLE);
                                refreshList();
                            }
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                mEditText.setVisibility(View.GONE);
                                mViewList.setVisibility(View.VISIBLE);
                            }
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setEditable(true);
                            }
                        });
            } else {
                mEditText.setVisibility(View.GONE);
                mViewList.setVisibility(View.VISIBLE);
            }
        }
    }

    public class FileSaveTask extends AsyncTask<String, Integer, Integer> {
        private OpenPath mPath;

        public FileSaveTask(OpenPath path) {
            mPath = path;
        }

        private long lastPub = 0l;

        public void showProgress(Integer... progress) {
            long now = new Date().getTime();
            if (now - lastPub < 500)
                return;
            lastPub = now;
            publishProgress(progress);
        }

        @Override
        protected Integer doInBackground(String... datas) {
            final String data = datas[0];
            OutputStream fos = null;
            try {
                byte[] bytes = data.getBytes();
                // if(mPath instanceof OpenPath.OpenPathByteIO)
                // ((OpenPath.OpenPathByteIO)mPath).writeBytes(data.getBytes());
                if (mPath instanceof NeedsTempFile) {
                    ((NeedsTempFile)mPath).getTempFile().write(data);
                    ((NeedsTempFile)mPath).tempUpload(this);
                } else {
                    fos = new BufferedOutputStream(mPath.getOutputStream());
                    fos.write(bytes);
                    fos.close();
                }
                if (mPath instanceof OpenNetworkPath)
                    ((OpenNetworkPath)mPath).disconnect();
                mData = data;
                return bytes.length;
            } catch (Exception e) {
                if (mPath instanceof NeedsTempFile)
                    Logger.LogError(
                            "Couldn't save temp file (" + ((NeedsTempFile)mPath).getTempFile()
                                    + ").", e);
                else
                    Logger.LogError("Couldn't save file (" + mPath + ").", e);
            }
            return -1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressVisibility(true);
            ViewUtils.setEnabled(false, mEditText);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            setProgressVisibility(false);
            Context c = TextEditorFragment.this.getActivity();
            if (result < 0) {
                Toast.makeText(c, R.string.httpErrorIO, Toast.LENGTH_LONG).show();
                return;
            }
            mDirty = false;
            showFilename();
            String msg = mPath + " " + c.getResources().getString(R.string.s_msg_saved) + " ("
                    + result + " b)";
            Logger.LogDebug(msg);
            Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
            ViewUtils.setEnabled(true, mEditText);
        }

    }

    public class FileLoadTask extends AsyncTask<OpenPath, Integer, String> {
        @Override
        protected String doInBackground(OpenPath... params) {
            OpenPath mPath = params[0];
            if (mPath instanceof NeedsTempFile)
                try {
                    ((NeedsTempFile)mPath).tempDownload(this);
                } catch (IOException e) {
                    Logger.LogError("Unable to download temp file while loading text file.", e);
                }
            String path = mPath.getPath();
            Logger.LogDebug("Getting " + path);
            if (mPath.canRead()) {
                Logger.LogDebug("File is " + mPath.length() + " bytes.");
                StringBuilder sb = new StringBuilder();
                if (mPath instanceof OpenPath.OpenPathByteIO)
                    sb.append(new String(((OpenPath.OpenPathByteIO)mPath).readBytes()));
                else {
                    InputStream is = null;
                    try {
                        is = mPath.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line = br.readLine()) != null)
                            sb.append(line + "\n");
                    } catch (SecurityException s) {
                        Logger.LogError("Couldn't open file due to security. " + path, s);
                        doClose();
                    } catch (RuntimeException r) {
                        Logger.LogError("File too large?", r);
                        if (getExplorer() != null)
                            getExplorer().showToast("Unable to open file. File too large?");
                        doClose();
                    } catch (FileNotFoundException f) {
                        Logger.LogError("File not found - " + path, f);
                        doClose();
                    } catch (Exception e) {
                        Logger.LogError("Couldn't find file - " + path, e);
                        doClose();
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (mPath instanceof OpenNetworkPath)
                    ((OpenNetworkPath)mPath).disconnect();
                return sb.toString();
            } else if (path.indexOf("ftp:/") > -1) {
                BufferedInputStream in = null;
                try {
                    URL url = new URL(path);
                    FTPManager ftp = FTPManager.getInstance(url);
                    in = new BufferedInputStream(ftp.getInputStream(url.getPath()));
                    byte[] buffer = new byte[4096];
                    StringBuilder sb = new StringBuilder();
                    while (in.read(buffer) > 0) {
                        for (byte b : buffer)
                            sb.append((char)b);
                    }
                    return sb.toString();
                } catch (MalformedURLException e) {
                    Logger.LogError("Bad URL for FTP - " + path, e);
                } catch (IOException e) {
                    Logger.LogError("Couldn't read from FTP - " + path, e);
                } finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                }

                // return FTPManager.getData(path);
            } else if (path.indexOf("sftp:/") > -1) {
                BufferedInputStream in = null;
                try {
                    in = new BufferedInputStream(mPath.getInputStream());
                    byte[] buffer = new byte[4096];
                    StringBuilder sb = new StringBuilder();
                    while (in.read(buffer) > 0) {
                        for (byte b : buffer)
                            sb.append((char)b);
                    }
                    return sb.toString();
                } catch (IOException e) {
                    Logger.LogError("Couldn't read from SFTP - " + path, e);
                } finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.setEnabled(false, mEditText);
            setProgressVisibility(true);
        }

        private long lastPub = 0l;

        public void showProgress(Integer... progress) {
            long now = new Date().getTime();
            if (now - lastPub < 500)
                return;
            lastPub = now;
            publishProgress(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                setText(result);
            setProgressVisibility(false);
            ViewUtils.setEnabled(true, mEditText);
        }
    }

    public OpenPath getPath() {
        return mPath;
    }

    @Override
    public Drawable getIcon() {
        if (getActivity() == null)
            return null;
        return new BitmapDrawable(getResources(), ThumbnailCreator.getFileExtIcon(getPath()
                .getExtension(), getActivity(), true));
    }

    @Override
    public CharSequence getTitle() {
        SpannableString ret = new SpannableString(getPath().getName());
        if (mDirty)
            ret.setSpan(new StyleSpan(Typeface.ITALIC), 0, ret.length(), 0);
        // ret.setSpan(new ImageSpan(getActivity(), R.drawable.ic_menu_save), 0,
        // 1, 0);
        // Logger.LogDebug("TextEditorFragment.getTitle() = " + ret.toString());
        return ret;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count == 0)
            return;
        mData = s.toString();
        mDirty = true;
        if (getExplorer() != null)
            getExplorer().notifyPager();
    }

    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onTitleLongClick(View titleView) {
        Menu menu = new MenuBuilder(getActivity());
        getSupportMenuInflater().inflate(R.menu.text_full, menu);
        return showMenu(menu.findItem(R.id.menu_text_ops).getSubMenu(), titleView, null);
    }

    public boolean isSalvagable() {
        return mSalvage;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;
        float fsize = (float)(progress + 1) / 2;
        setTextSize(fsize);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setTextSize(mTextSize, true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Logger.LogDebug(event.toString());
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            mFilename.setVisibility(View.VISIBLE);
        else if (event.getAction() == MotionEvent.ACTION_UP)
            showFilename();
        return false;
    }

}

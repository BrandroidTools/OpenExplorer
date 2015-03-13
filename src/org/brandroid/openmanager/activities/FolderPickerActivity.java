
package org.brandroid.openmanager.activities;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.OpenClipboard;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.PickerFragment;
import org.brandroid.openmanager.fragments.PickerFragment.OnOpenPathPickedListener;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.ShellSession;
import org.brandroid.utils.DiskLruCache;
import org.brandroid.utils.LruCache;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ThreadPool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class FolderPickerActivity extends SherlockFragmentActivity implements OnItemClickListener,
        OnClickListener, TextWatcher, OpenApp {
    private OpenPath mPath;
    private boolean mFoldersOnly = false;
    private String mDefaultName;
    private TextView mSelection, mTitle;
    private EditText mPickName;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme_Dark);
        
        setTitle(R.string.s_title_picker);

        setContentView(R.layout.picker_widget);
        mSelection = (TextView)findViewById(R.id.pick_path);
        mPickName = (EditText)findViewById(R.id.pick_filename);
        mTitle = (TextView)findViewById(android.R.id.title);

        Intent intent = getIntent();
        if (intent == null)
            intent = new Intent();
        if (savedInstanceState != null)
            checkBundle(savedInstanceState, intent);
        else if (intent.getData() != null)
            onNewIntent(intent);

        mFragmentManager = getSupportFragmentManager();
        setPath(mPath, true);
        ViewUtils.setViewsOnClick(this, this, android.R.id.button1, android.R.id.button2, R.id.check_folders);
        if (mPickName != null)
            mPickName.addTextChangedListener(this);
    }

    private void checkBundle(Bundle data, Intent intent) {
        if (data.containsKey("start")) {
            mPath = (OpenPath)data.getParcelable("start");
            intent.putExtra("picker", data.getParcelable("start"));
        } else if(data.containsKey("path")) {
        	mPath = FileManager.getOpenCache(data.getString("path"));
        	intent.putExtra("picker", (Parcelable)mPath);
        }
        else if ("file".equals(intent.getData().getScheme()))
        {
        	mPath = FileManager.getOpenCache(intent.getData().toString());
        	intent.putExtra("picker", (Parcelable)mPath);
        }
        else
            mPath = OpenFile.getExternalMemoryDrive(true);
        if (data.containsKey("name")) {
            mDefaultName = data.getString("name");
            intent.putExtra("name", mDefaultName);
        }
        if (data.containsKey("files"))
            mFoldersOnly = data.getBoolean("files", mFoldersOnly);
        
        //ViewUtils.setViewsChecked(this, mFoldersOnly, R.id.check_folders);
        //ViewUtils.setViewsVisible(this, !mFoldersOnly, R.id.pick_path_row, R.id.pick_path);
    }

    private void setPath(OpenPath path, boolean addToStack) {
        if (path == null)
            path = OpenFile.getExternalMemoryDrive(true);
        mPath = path;
        if(mTitle != null)
        {
            mTitle.setText(path.getPath());
            mTitle.setVisibility(View.VISIBLE);
        }
        if(mSelection != null)
            mSelection.setText(path.getPath());
        if (mPath.isFile()) {
            mDefaultName = mPath.getName();
            mPickName.setText(mDefaultName);
            mPath = mPath.getParent();
        }
        if (!addToStack)
            return;
        PickerFragment frag = PickerFragment.getInstance(mPath);
        frag.setShowSelection(false);
        frag.setOnOpenPathPickedListener(new OnOpenPathPickedListener() {
            @Override
            public void onOpenPathPicked(OpenPath path) {
                setPath(path, true);
            }

            public void onOpenPathShown(OpenPath path) {
                setPath(path, false);
            }
        });
        mFragmentManager.beginTransaction().replace(R.id.picker_widget, frag)
                .setBreadCrumbTitle(mPath.getPath()).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            OpenPath path = FileManager.getOpenCache(intent.getDataString());
            if (path != null && path.exists())
                mPath = path;
        }
        if (intent.getExtras() != null)
            checkBundle(intent.getExtras(), intent);
    }

    public PickerFragment getSelectedFragment() {
        return (PickerFragment)mFragmentManager.findFragmentById(R.id.picker_widget);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OpenPath path = getSelectedFragment().getPath();
        setPath(path, true);
    }

    private void returnPath() {
        Intent intent = getIntent();
        if (intent == null)
            intent = new Intent();
        OpenPath ret = mPath;
        if (!mFoldersOnly)
            ret = ret.getChild(mPickName.getText().toString());
        intent.setData(ret.getUri());
        intent.putExtra("path", (Parcelable)ret);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.R.id.button1:
                returnPath();
                break;
            case android.R.id.button2:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.check_folders:
            	mFoldersOnly = ((CheckBox)findViewById(R.id.check_folders)).isChecked();
                ViewUtils.setViewsVisible(this, !mFoldersOnly, R.id.pick_path_row, R.id.pick_path);
            	setPath(mPath, false);
            	break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        ViewUtils
                .setViewsEnabled(this, s != null && !s.toString().equals(""), android.R.id.button1);
    }
    
    public OpenApplication getOpenApplication() { return (OpenApplication)getApplication(); }

	@Override
	public DataManager getDataManager() {
		// TODO Auto-generated method stub
		return getOpenApplication().getDataManager();
	}

	@Override
	public ImageCacheService getImageCacheService() {
		// TODO Auto-generated method stub
		return getOpenApplication().getImageCacheService();
	}

	@Override
	public DownloadCache getDownloadCache() {
		// TODO Auto-generated method stub
		return getOpenApplication().getDownloadCache();
	}

	@Override
	public ThreadPool getThreadPool() {
		// TODO Auto-generated method stub
		return getOpenApplication().getThreadPool();
	}

	@Override
	public LruCache<String, Bitmap> getMemoryCache() {
		// TODO Auto-generated method stub
		return getOpenApplication().getMemoryCache();
	}

	@Override
	public DiskLruCache getDiskCache() {
		// TODO Auto-generated method stub
		return getOpenApplication().getDiskCache();
	}

	@Override
	public ActionMode getActionMode() {
		// TODO Auto-generated method stub
		return getOpenApplication().getActionMode();
	}

	@Override
	public void setActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OpenClipboard getClipboard() {
		// TODO Auto-generated method stub
		return getOpenApplication().getClipboard();
	}

	@Override
	public ShellSession getShellSession() {
		// TODO Auto-generated method stub
		return getOpenApplication().getShellSession();
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Preferences getPreferences() {
		// TODO Auto-generated method stub
		return getOpenApplication().getPreferences();
	}

	@Override
	public void refreshBookmarks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getThemedResourceId(int styleableId, int defaultResourceId) {
		// TODO Auto-generated method stub
		return getOpenApplication().getThemedResourceId(styleableId, defaultResourceId);
	}
}

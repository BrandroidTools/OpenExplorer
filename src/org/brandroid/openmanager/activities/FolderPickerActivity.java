
package org.brandroid.openmanager.activities;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.PickerFragment;
import org.brandroid.openmanager.fragments.PickerFragment.OnOpenPathPickedListener;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.utils.ViewUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class FolderPickerActivity extends FragmentActivity implements OnItemClickListener,
        OnClickListener, TextWatcher {
    private OpenPath mPath;
    private boolean pickDirOnly = false;
    private String mDefaultName;
    private TextView mSelection, mTitle;
    private EditText mPickName;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme_Dark);

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
        ViewUtils.setViewsOnClick(this, this, android.R.id.button1, android.R.id.button2);
        if (mPickName != null)
            mPickName.addTextChangedListener(this);
    }

    private void checkBundle(Bundle data, Intent intent) {
        if (data.containsKey("start")) {
            mPath = (OpenPath)data.getParcelable("start");
            intent.putExtra("picker", data.getParcelable("start"));
        } else
            mPath = OpenFile.getExternalMemoryDrive(true);
        if (data.containsKey("name")) {
            mDefaultName = data.getString("name");
            intent.putExtra("name", mDefaultName);
        }
        if (data.containsKey("files"))
            pickDirOnly = data.getBoolean("files", pickDirOnly);

        ViewUtils.setViewsVisible(this, !pickDirOnly, R.id.pick_path_row, R.id.pick_path);
        if (!pickDirOnly && (mDefaultName == null || mDefaultName == ""))
            ViewUtils.setViewsEnabled(this, false, android.R.id.button1);
    }

    private void setPath(OpenPath path, boolean addToStack) {
        if (path == null)
            path = OpenFile.getExternalMemoryDrive(true);
        mPath = path;
        mTitle.setText(path.getPath());
        mTitle.setVisibility(View.VISIBLE);
        mSelection.setText(path.getPath());
        if (mPath.isFile()) {
            mDefaultName = mPath.getName();
            mPickName.setText(mDefaultName);
            mPath = mPath.getParent();
        }
        if (!addToStack)
            return;
        PickerFragment frag = new PickerFragment(this, mPath);
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
        if (!pickDirOnly)
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
}

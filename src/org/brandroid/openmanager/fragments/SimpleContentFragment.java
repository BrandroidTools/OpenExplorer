
package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.ContentAdapter;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.views.OpenPathView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class SimpleContentFragment extends Fragment implements ContentAdapter.Callback {
    private OpenPath mPath;
    private GridView mGrid;
    private ContentAdapter mAdapter;
    private OnItemClickListener mClickCaller;
    private boolean mShowFiles = true;
    private boolean mShowUp = false;
    private Bundle mData;
    private OpenApp mApp;

    public SimpleContentFragment(OpenApp app, OpenPath path) {
        mApp = app;
        mPath = path;
    }

    public void setShowFiles(boolean showFiles) {
        mShowFiles = showFiles;
        if (mAdapter != null)
            mAdapter.setShowFiles(false);
    }

    public void setShowUp(boolean showUp) {
        mShowUp = showUp;
        if (mAdapter != null)
            mAdapter.setShowPlusParent(showUp);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("path", mPath);
        outState.putInt("top", mGrid.getFirstVisiblePosition());
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mClickCaller = l;
        if (mGrid != null)
            mGrid.setOnItemClickListener(l);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mData = savedInstanceState;
        else
            mData = getArguments();
        if (mData == null)
            mData = new Bundle();
        if (mData.containsKey("path"))
            mPath = (OpenPath)mData.getParcelable("path");
        mGrid = (GridView)inflater.inflate(R.layout.content_grid, container, false);
        mGrid.setNumColumns(container.getContext().getResources()
                .getInteger(R.integer.max_grid_columns));
        mAdapter = new ContentAdapter(mApp, this, OpenExplorer.VIEW_LIST, mPath);
        mAdapter.setShowDetails(false);
        mAdapter.setSorting(SortType.ALPHA);
        mAdapter.setShowPlusParent(mShowUp);
        mAdapter.setShowFiles(mShowFiles);
        mAdapter.updateData();
        mGrid.setAdapter(mAdapter);
        if (mData.containsKey("top"))
            mGrid.setSelection(mData.getInt("top"));
        return mGrid;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mClickCaller != null)
            mGrid.setOnItemClickListener(mClickCaller);
    }

    @Override
    public void onAdapterSelectedChanged(OpenPath path, boolean newSelected, int mSelectedCount) {
        // TODO Auto-generated method stub

    }
}

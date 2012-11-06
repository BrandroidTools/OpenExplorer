
package org.brandroid.openmanager.views;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.OpenPathAdapter;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

public class OpenPathList extends ListView {
    private OpenPath mPathParent = null;
    private OpenPathAdapter mAdapter = null;
    private final OpenApp mApp;

    public OpenPathList(OpenPath path, OpenApp app) {
        super(app.getContext());
        mApp = app;
        setPath(path);
    }

    public OpenPathList(OpenApp app, AttributeSet attrs, int defStyle) {
        super(app.getContext(), attrs, defStyle);
        mApp = app;
    }

    public void setPath(OpenPath path) {
        mPathParent = path;
        mAdapter = new OpenPathAdapter(mPathParent, R.layout.list_content_layout, mApp);
        setAdapter(mAdapter);
        invalidateViews();
    }

    @Override
    public ListAdapter getAdapter() {
        if (mPathParent == null)
            return super.getAdapter();
        if (mAdapter == null)
            mAdapter = new OpenPathAdapter(mPathParent, R.layout.list_content_layout, mApp);
        return mAdapter;
    }
}

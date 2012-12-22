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

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.OpenBookmarks;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.OpenInterfaces.OnBookMarkChangeListener;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class BookmarkFragment extends OpenFragment implements OnBookMarkChangeListener {

    private static OpenBookmarks mBookmarks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarks_fragment, container, false);
        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandAll();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getPagerPriority() {
        return 0;
    }

    public ExpandableListView getListView() {
        return (ExpandableListView)getView().findViewById(R.id.bookmarks_list);
    }

    public void setListAdapter(ExpandableListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Logger.LogDebug("Bookmark Fragment Created");

        final ExpandableListView lv = getListView();
        lv.setDividerHeight(0);
        if (mBookmarks == null)
            mBookmarks = new OpenBookmarks(getExplorer(), lv);
        // mBookmarks.setupListView(lv); //redundant?
        final ExpandableListAdapter adapter = mBookmarks.getListAdapter();
        setListAdapter(adapter);
        registerForContextMenu(lv);
        expandAll();
        super.onActivityCreated(savedInstanceState);
    }

    public void expandAll() {
        ExpandableListView lv = getListView();
        if (lv == null)
            return;
        for (int i = 0; i < lv.getCount(); i++)
            lv.expandGroup(i);
    }

    @Override
    public void onBookMarkAdd(OpenPath path) {
        if (mBookmarks != null)
            mBookmarks.onBookMarkAdd(path);
    }

    @Override
    public void scanBookmarks() {
        if (mBookmarks != null)
            mBookmarks.scanBookmarks();
    }

    @Override
    public CharSequence getTitle() {
        return null;
    }

    @Override
    public Drawable getIcon() {
        return null;
    }

}

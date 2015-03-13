
package org.brandroid.openmanager.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.ArrayPagerAdapter.OnPageTitleClickListener;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenSearch;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.data.OpenTar;
import org.brandroid.openmanager.data.OpenZip;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.fragments.OpenFragment;
import org.brandroid.openmanager.fragments.OpenPathFragmentInterface;
import org.brandroid.openmanager.fragments.SearchResultsFragment;
import org.brandroid.openmanager.fragments.TextEditorFragment;
import org.brandroid.utils.Logger;

import com.viewpagerindicator.TabPageIndicator.TabView;
import com.viewpagerindicator.TitleProvider;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;

public class OpenPathPagerAdapter extends FragmentStatePagerAdapter implements TitleProvider {
    private ArrayList<OpenPath> mChildren = new ArrayList<OpenPath>();
    private HashMap<OpenPath, OpenFragment> mFragments = new HashMap<OpenPath, OpenFragment>();
    private ArrayPagerAdapter.OnPageTitleClickListener mListener = null;
    private final FragmentManager mFragMan;
    protected boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;

    public OpenPathPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragMan = fm;
    }

    public void setOnPageTitleClickListener(OnPageTitleClickListener l) {
        mListener = l;
    }

    public OpenFragment getLastItem() {
        return getItem(getCount() - 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        OpenPath path = mChildren.get(position);
        if (path instanceof OpenMediaStore || path instanceof OpenCursor
                || path instanceof OpenSmartFolder || path instanceof OpenZip
                || path instanceof OpenTar)
            return path.getName();
        return path.getName() + (path.isDirectory() ? "/" : "");
    }

    public OpenFragment getFragment(int pos) {
        if (DEBUG)
            Logger.LogDebug("OpenPathPagerAdapter.getFragment(" + pos + ")");
        OpenPath path = mChildren.get(pos);
        if (mFragments.containsKey(path))
            return mFragments.get(path);
        return getItem(pos);
    }

    @Override
    public int getItemPosition(Object object) {
        if (DEBUG)
            Logger.LogDebug("OpenPathPagerAdapter.getItemPosition(" + object + ")");
        if (object instanceof OpenPathFragmentInterface)
            if (mChildren.contains(((OpenPathFragmentInterface)object).getPath()))
                return mChildren.indexOf(((OpenPathFragmentInterface)object).getPath());
        return FragmentPagerAdapter.POSITION_NONE;
    }

    /*
     * @Override public Object instantiateItem(ViewGroup container, int
     * position) { if(DEBUG)
     * Logger.LogDebug("OpenPathPagerAdapter.instantiateItem(" + position +
     * ")"); OpenPath path = mChildren.get(position); OpenFragment ret =
     * (OpenFragment)super.instantiateItem(container, position); if(ret
     * instanceof OpenPathFragmentInterface) {
     * if(!((OpenPathFragmentInterface)ret).getPath().equals(path)) { ret =
     * getItem(position); mFragMan.beginTransaction().attach(ret).commit(); } }
     * return ret; }
     * @Override public void destroyItem(ViewGroup container, int position,
     * Object object) { super.destroyItem(container, position, object);
     * if(object instanceof View) container.removeView((View)object); else
     * container.removeViewAt(position); }
     */

    @Override
    public OpenFragment getItem(int pos) {
        if (mChildren.size() <= pos || pos < 0)
            return null;
        OpenPath path = mChildren.get(pos);
        OpenFragment ret = null;
        if (path instanceof OpenSearch) {
            ret = SearchResultsFragment.getInstance((OpenSearch)path);
        } else {
            if (!path.isDirectory())
                ret = TextEditorFragment.getInstance(path, new Bundle());
            else
                ret = ContentFragment.getInstance(path);
        }
        ret.setRetainInstance(false);
        mFragments.put(path, ret);
        return ret;
    }

    public OpenPath getPath(int pos) {
        return mChildren.get(pos);
    }

    @Override
    public int getCount() {
        return mChildren.size();
    }

    @Override
    public boolean modifyTab(TabView tab, final int position) {
        if (mListener != null) {
            tab.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mListener.onPageTitleLongClick(position, v);
                }
            });
            tab.setLongClickable(true);
        }
        return true;
    }

    public void add(OpenPath path) {
        mChildren.add(path);
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(mChildren, new Comparator<OpenPath>() {
            public int compare(OpenPath lhs, OpenPath rhs) {
                int da = lhs.getDepth();
                int db = rhs.getDepth();
                if (da == db)
                    return 0;
                return da > db ? 1 : -1;
            }
        });
        super.notifyDataSetChanged();
    }

    public OpenFragment remove(int index) {
        OpenFragment ret = getItem(index);
        mFragments.remove(mChildren.get(index));
        mChildren.remove(index);
        mFragMan.beginTransaction().detach(ret).commit();
        notifyDataSetChanged();
        return ret;
    }

    public int indexOf(OpenPath path) {
        for (int i = 0; i < mChildren.size(); i++)
            if (mChildren.get(i).equals(path))
                return i;
        return -1;
    }

    public void add(int index, OpenPath path) {
        if (mChildren.contains(path))
            remove(mChildren.indexOf(path));
        if (index >= mChildren.size())
            add(path);
        else
            mChildren.add(index, path);
    }

}

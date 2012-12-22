
package org.brandroid.openmanager.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.fragments.OpenFragment;
import org.brandroid.openmanager.fragments.OpenPathFragmentInterface;
import org.brandroid.openmanager.fragments.TextEditorFragment;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.utils.Logger;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;
import com.viewpagerindicator.TabPageIndicator.TabView;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnLongClickListener;

public class PagerTabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {
    // private static Hashtable<OpenPath, Fragment> mPathMap = new
    // Hashtable<OpenPath, Fragment>();
    private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private OnPageTitleClickListener mListener = null;
    private final ViewPager mViewPager;
    private final Context mContext;
    private final TabPageIndicator mTabBar;

    static final class TabInfo {
        private final Class<? extends OpenFragment> clss;
        private final Bundle args;

        TabInfo(Class<? extends OpenFragment> _class, Bundle _args) {
            clss = _class;
            args = _args;
        }

        public int compareTo(TabInfo b) {
            if (args.containsKey("last") && b.args.containsKey("last")) {
                OpenPath pa = FileManager.getOpenCache(args.getString("last"));
                OpenPath pb = FileManager.getOpenCache(args.getString("last"));
                return pa.getPath().compareTo(pb.getPath());
            }
            return 0;
        }
    }

    public PagerTabsAdapter(FragmentActivity activity, ViewPager pager, TabPageIndicator tabBar) {
        super(activity.getSupportFragmentManager());
        mViewPager = pager;
        mContext = activity;
        mTabBar = tabBar;
    }

    public interface OnPageTitleClickListener {
        public boolean onPageTitleLongClick(int position, View view);
    }

    public void setOnPageTitleClickListener(OnPageTitleClickListener l) {
        mListener = l;
    }

    @Override
    public OpenFragment getItem(int pos) {
        if (pos >= getCount() || pos < 0)
            return null;
        TabInfo info = mTabs.get(pos);
        return OpenFragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    public OpenFragment getLastItem() {
        return getItem(getCount() - 1);
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    public synchronized void sort() {
        Collections.sort(mTabs, new Comparator<TabInfo>() {
            @Override
            public int compare(TabInfo a, TabInfo b) {
                return a.compareTo(b);
            }
        });
    }

    @Override
    public void notifyDataSetChanged() {
        sort();
        super.notifyDataSetChanged();
    }

    @Override
    public Parcelable saveState() {
        Parcelable psuper = null;
        try {
            psuper = super.saveState();
        } catch (Exception e) {
            Logger.LogError("Couldn't save ArrayPagerAdapter state.", e);
        }
        Bundle state = new Bundle();
        if (psuper != null) {
            if (psuper instanceof Bundle)
                state = (Bundle)psuper;
            else
                state.putParcelable("super", psuper);
        }
        if (getCount() > 0) {
            OpenPath[] items = new OpenPath[getCount()];
            for (int i = 0; i < getCount(); i++)
                if (getItem(i) instanceof OpenPathFragmentInterface)
                    items[i] = ((OpenPathFragmentInterface)getItem(i)).getPath();
            state.putParcelableArray("pages", items);
        }
        return state;
    }

    /*
     * @Override public void restoreState(Parcelable state, ClassLoader loader)
     * { if(state == null) return; Bundle bundle = null; if (state instanceof
     * SavedState) { super.restoreState(state, loader); bundle =
     * ((SavedState)state).getBundle(); } else if(state instanceof Bundle)
     * bundle = (Bundle)state; else bundle = new Bundle();
     * bundle.setClassLoader(loader); if(bundle.containsKey("super"))
     * super.restoreState(bundle.getParcelable("super"), loader);
     * if(!bundle.containsKey("pages")) return;
     * Logger.LogDebug("ArrayPagerAdapter restore :: " +
     * bundle.getParcelableArray("pages").toString()); Parcelable[] items =
     * bundle.getParcelableArray("pages"); mTabs.clear(); for(int i = 0; i <
     * items.length; i++) if(items[i] != null && items[i] instanceof OpenPath) {
     * OpenPath path = (OpenPath)items[i]; if(path.isDirectory())
     * mFrags.add(ContentFragment.getInstance(path)); else if(path.isTextFile()
     * || path.length() < Preferences.Pref_Text_Max_Size) mFrags.add(new
     * TextEditorFragment(path)); } }
     */

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof OpenPathFragmentInterface) {
            OpenPath tofind = ((OpenPathFragmentInterface)object).getPath();
            if (tofind == null)
                return super.getItemPosition(object);
            for (int i = 0; i < getCount(); i++) {
                OpenFragment f = getItem(i);
                if (f instanceof OpenPathFragmentInterface) {
                    OpenPath tocheck = ((OpenPathFragmentInterface)f).getPath();
                    if (tocheck != null && tocheck.getPath().equals(tofind.getPath()))
                        return i;
                }
            }
        }
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        OpenFragment f = getItem(position);
        if (f != null)
            return f.getTitle();
        return null;

        // CharSequence ret = null;

        /*
         * if(f instanceof TextEditorFragment) return
         * ((TextEditorFragment)f).getPath().getName(); else if(f instanceof
         * SearchResultsFragment) return ((SearchResultsFragment)f).getTitle();
         * OpenPath path = null; if(f instanceof ContentFragment) path =
         * ((ContentFragment)f).getPath(); if(path == null) return ret; else
         * if(path instanceof OpenNetworkPath && path.getParent() == null &&
         * ((OpenNetworkPath)path).getServersIndex() >= 0) return
         * OpenServers.DefaultServers
         * .get(((OpenNetworkPath)path).getServersIndex()).getName(); else
         * if(path.getName().equals("")) return path.getPath(); else
         * if(path.getParent() == null) return path.getName(); else
         * if(path.isDirectory() && !path.getName().endsWith("/")) return
         * path.getName() + "/"; else return path.getName();
         */
    }

    public List<OpenFragment> getNonContentFragments() {
        ArrayList<OpenFragment> ret = new ArrayList<OpenFragment>();
        for (int i = 0; i < getCount(); i++) {
            OpenFragment f = getItem(i);
            if (!(f instanceof ContentFragment))
                ret.add(f);
        }
        return ret;
    }

    public boolean add(OpenFragment frag) {
        return add(frag.getClass(), frag.getArguments());
    }

    public void add(int index, OpenFragment frag) {
        add(index, frag.getClass(), frag.getArguments());
    }

    public boolean add(Class<? extends OpenFragment> clss, Bundle args) {
        add(getCount() - 1, clss, args);
        return true;
    }

    public synchronized void add(int index, Class<? extends OpenFragment> clss, Bundle args) {
        if (args == null)
            args = new Bundle();
        TabInfo info = new TabInfo(clss, args);
        mTabs.add(info);
        OpenFragment frag = getItem(index);
        if (mTabBar != null && frag != null) {
            TabView tab = mTabBar.addTab(frag.getTitle(), index);
            tab.setTag(info);
        }
        notifyDataSetChanged();
    }

    public void add(List<OpenFragment> frags) {
        for (OpenFragment f : frags)
            add(f);
    }

    public OpenFragment remove(int index) {
        OpenFragment ret = getItem(index);
        mTabs.remove(index);
        return ret;
    }

    public boolean remove(OpenFragment frag) {
        for (int i = getCount() - 1; i >= 0; i--) {
            OpenFragment f = getItem(i);
            if (f.equals(frag)) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized void clear() {
        mTabs.clear();
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

    public int getLastPositionOfType(Class<? extends OpenFragment> clss) {
        for (int i = getCount() - 1; i >= 0; i--)
            if (mTabs.get(i).clss.equals(clss))
                return i;
        return -1;
    }

    public void set(int index, OpenFragment ret) {
        if (getCount() > index)
            mTabs.remove(index);
        add(index, ret);
    }
}

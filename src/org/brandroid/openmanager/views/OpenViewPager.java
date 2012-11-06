
package org.brandroid.openmanager.views;

import java.util.ArrayList;
import java.util.List;

import org.brandroid.openmanager.adapters.ArrayPagerAdapter;
import org.brandroid.openmanager.fragments.OpenFragment;
import org.brandroid.utils.Logger;

import com.viewpagerindicator.PageIndicator;
import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class OpenViewPager extends ViewPager {
    private PageIndicator mIndicator = null;
    private List<OnPageChangeListener> mListeners = new ArrayList<OnPageChangeListener>();
    private OnPageIndicatorChangeListener mIndicatorListener = null;
    private OnDPADListener mDPAD = null;
    private boolean mLocked = false;

    public interface OnPageIndicatorChangeListener {
        public void onPageIndicatorChange();
    }

    public interface OnDPADListener {
        public boolean onKeyPressLeft();

        public boolean onKeyPressRight();
    }

    public void setOnDPADListener(OnDPADListener dpad) {
        mDPAD = dpad;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && mDPAD != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && mDPAD.onKeyPressLeft())
                return true;
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && mDPAD.onKeyPressRight())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public OpenViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                for (OnPageChangeListener l : mListeners)
                    l.onPageSelected(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

                for (OnPageChangeListener l : mListeners)
                    l.onPageScrolled(arg0, arg1, arg2);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

                for (OnPageChangeListener l : mListeners)
                    l.onPageScrollStateChanged(arg0);
            }
        });
    }

    public void setOnPageIndicatorChangeListener(OnPageIndicatorChangeListener l) {
        mIndicatorListener = l;
    }

    @Override
    public void setAdapter(PagerAdapter a) {
        try {
            super.setAdapter(a);
            setIndicator(mIndicator);
            notifyDataSetChanged();
        } catch (Exception e) {
            Logger.LogError("Couldn't set ViewPager adapter.", e);
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        // super.setOnPageChangeListener(listener);
        mListeners.add(listener);
    }

    public PageIndicator getIndicator() {
        return mIndicator;
    }

    public void setIndicator(PageIndicator indicator) {
        if (mIndicator != null && indicator != null && !mIndicator.equals(indicator))
            return;
        mIndicator = indicator;
        if (mIndicator != null && getAdapter() != null) {
            mIndicator.setViewPager(this);
            mIndicator.notifyDataSetChanged();
        }
        if (mIndicatorListener != null)
            mIndicatorListener.onPageIndicatorChange();
    }

    public OpenFragment getCurrentFragment() {
        return ((ArrayPagerAdapter)getAdapter()).getItem(getCurrentItem());
    }

    public void notifyDataSetChanged() {
        getAdapter().notifyDataSetChanged();
        if (mIndicator != null)
            mIndicator.notifyDataSetChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (mLocked)
            return false;
        return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (mLocked)
            return false;
        return super.onInterceptTouchEvent(arg0);
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }
}

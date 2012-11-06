
package org.brandroid.openmanager.views;

import org.brandroid.openmanager.adapters.ContentAdapter;
import org.brandroid.openmanager.data.OpenPath;

import org.brandroid.openmanager.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class OpenPathView extends LinearLayout {

    private OpenPath mFile;
    private boolean mDownEvent;
    private int mCheckmarkX;
    private ContentAdapter mAdapter;
    private static final int TOUCH_SLOP = 24;
    private static int sScaledTouchSlop = -1;

    public OpenPathView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public OpenPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public OpenPathView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public OpenPath getOpenPath() {
        return mFile;
    }

    public void associateFile(OpenPath file, ContentAdapter adapter) {
        mFile = file;
        mAdapter = adapter;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        View view = this.findViewById(R.id.content_check);
        // int[] xy = new int[2];
        // view.getLocationOnScreen(xy);
        // mCheckmarkX=xy[0];
        int x = 0;
        while (view != null) {
            if (Build.VERSION.SDK_INT > 10)
                x += (int)view.getX();
            else
                x += view.getScrollX();
            ViewParent parent = view.getParent();
            if (parent != null && parent instanceof View)
                view = (View)parent;
            else
                break;
        }
        mCheckmarkX = x;
    }

    private void initializeSlop(Context context) {
        if (sScaledTouchSlop == -1) {
            final Resources res = context.getResources();
            final Configuration config = res.getConfiguration();
            final float density = res.getDisplayMetrics().density;
            final float sizeAndDensity;
            // TODO Pre Honeycomb devices will FC on the second condition, hence
            // the addition of the first. Not quite sure how this
            // code now treats pre-Honecomb large screen devices (like the fire)
            // but it probably isn't good. Need a way to safely determine
            // screen size.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                    && config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                sizeAndDensity = density * 1.5f;
            } else {
                sizeAndDensity = density;
            }
            sScaledTouchSlop = (int)(sizeAndDensity * TOUCH_SLOP + 0.5f);
        }
    }

    /**
     * Overriding this method allows us to "catch" clicks in the checkbox or
     * star and process them accordingly.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initializeSlop(getContext());

        boolean handled = false;
        int touchX = (int)event.getX();
        // int checkRight = mCoordinates.checkmarkX
        // + mCoordinates.checkmarkWidthIncludingMargins
        // + sScaledTouchSlop;

        int checkRight = mCheckmarkX - sScaledTouchSlop;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (touchX > checkRight) {
                    mDownEvent = true;
                    if (touchX > checkRight) {
                        handled = true;
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                mDownEvent = false;
                break;

            case MotionEvent.ACTION_UP:
                if (mDownEvent) {
                    if (touchX > checkRight) {
                        mAdapter.toggleSelected(mFile);
                        handled = true;
                        // } else if (touchX > starLeft) {
                        // mIsFavorite = !mIsFavorite;
                        // mAdapter.updateFavorite(this, mIsFavorite);
                        // handled = true;
                    }
                }
                break;
        }

        if (handled) {
            invalidate();
        } else {
            handled = super.onTouchEvent(event);
        }

        return handled;
    }

}

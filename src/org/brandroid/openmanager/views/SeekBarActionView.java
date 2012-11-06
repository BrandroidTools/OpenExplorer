
package org.brandroid.openmanager.views;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.interfaces.OpenActionView;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.openmanager.util.BetterPopupWindow.OnPopupShownListener;
import org.brandroid.utils.Logger;

import com.actionbarsherlock.view.CollapsibleActionView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarActionView extends LinearLayout implements OpenActionView, View.OnKeyListener {
    private OnCloseListener mOnCloseListener;
    private OnClickListener mOnClickListener;

    private boolean mExpandedInActionView = false;
    private SeekBar mSeekBar;
    private ImageView mActionIcon;
    private boolean mIconified = true;
    private boolean mIconifiedByDefault = false;
    private boolean mClearingFocus = true;
    private BetterPopupWindow pop = null;

    public interface OnCloseListener {
        /* Return true to cancel onClose event */
        public boolean onClose();
    }

    public SeekBarActionView(Context context) {
        this(context, null);
    }

    public SeekBarActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.seek_bar, this, true);
        mSeekBar = (SeekBar)findViewById(android.R.id.progress);
        mSeekBar.setOnKeyListener(this);
        mActionIcon = (ImageView)findViewById(R.id.action_icon);
        findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIconified(!mIconified);
            }
        });
    }

    public BetterPopupWindow getPopup(Context context, View anchor) {
        if (pop == null) {
            pop = new BetterPopupWindow(context, anchor);
            pop.setContentView(this);
            setIconifiedByDefault(false);
            setOnCloseClickListener(new OnCloseListener() {
                public boolean onClose() {
                    pop.dismiss();
                    return true;
                }
            });
        }
        return pop;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIconifiedByDefault)
            updateViewsVisibility(true);
    }

    public SeekBarActionView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mSeekBar.setOnSeekBarChangeListener(l);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mSeekBar.setOnFocusChangeListener(l);
    }

    public void setOnCloseClickListener(OnCloseListener l) {
        mOnCloseListener = l;
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    /** @hide */
    @Override
    public void clearFocus() {
        mClearingFocus = true;
        super.clearFocus();
        mSeekBar.clearFocus();
        mClearingFocus = false;
    }

    private void onCloseClicked() {

        if (mOnCloseListener == null || !mOnCloseListener.onClose()) {
            clearFocus();
            updateViewsVisibility(true);
        }
    }

    private void onSeekClicked() {
        updateViewsVisibility(false);
        if (mOnClickListener != null)
            mOnClickListener.onClick(this);
    }

    private void updateViewsVisibility(final boolean collapsed) {
        mIconified = collapsed;

        if (collapsed) {
            mSeekBar.setVisibility(GONE);
            findViewById(R.id.action_up).setVisibility(GONE);
        } else {
            findViewById(R.id.action_up).setVisibility(VISIBLE);
            mSeekBar.setVisibility(VISIBLE);
        }
    }

    /**
     * Iconifies or expands the ActionView. This is a temporary state and does
     * not override the default iconified state set by
     * {@link #setIconifiedByDefault(boolean)}. If the default state is
     * iconified, then a false here will only be valid until the user closes the
     * field. And if the default state is expanded, then a true here will only
     * clear the text field and not close it.
     * 
     * @param iconify a true value will collapse the ActionView to an icon,
     *            while a false will expand it.
     */
    public void setIconified(boolean iconify) {
        if (iconify) {

            onCloseClicked();
        } else {
            onSeekClicked();
        }
    }

    public void setIconifiedByDefault(boolean iconify) {
        mIconifiedByDefault = iconify;
    }

    /**
     * Returns the current iconified state of the SearchView.
     * 
     * @return true if the SearchView is currently iconified, false if the
     *         search field is fully visible.
     */
    public boolean isIconified() {
        return mIconified;
    }

    public void onActionViewExpanded() {
        if (mExpandedInActionView) {
            Logger.LogWarning("SeekBarActionView already expanded");
            return;
        }
        Logger.LogDebug("SeekBarActionView expanding");

        mExpandedInActionView = true;

        setIconified(false);
    }

    public void onActionViewCollapsed() {
        clearFocus();
        updateViewsVisibility(true);
        mExpandedInActionView = false;
    }

    public void setMax(int max) {
        mSeekBar.setMax(max);
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.equals(mSeekBar) && keyCode == KeyEvent.KEYCODE_BACK) {
            onCloseClicked();
            requestFocus();
            return true;
        }
        return false;
    }

}


package org.brandroid.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.brandroid.openmanager.activities.OpenExplorer;

import android.app.Activity;
import android.os.Build;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewUtils {

    public static int getOffsetLeft(View v) {
        int ret = getAbsoluteLeft(v);
        if (v.getParent() != null)
            ret -= getAbsoluteLeft((View)v.getParent());
        return ret;
    }

    public static int getAbsoluteLeft(View v) {
        try {
            int[] ret = new int[2];
            v.getLocationOnScreen(ret);
            return ret[0];
        } catch (Exception e) {
            int ret = v.getLeft();
            if (v.getParent() != null && v.getParent() instanceof View)
                ret += getAbsoluteLeft((View)v.getParent());
            return ret;
        }
    }

    public static int getAbsoluteTop(View v) {
        if (v == null)
            return 0;
        try {
            int[] ret = new int[2];
            v.getLocationInWindow(ret);
            return ret[1];
        } catch (Exception e) {
            int ret = v.getTop();
            if (v.getParent() != null && v.getParent() instanceof View)
                ret += getAbsoluteTop((View)v.getParent());
            return ret;
        }
    }

    /**
     * Get the first Absolute Top from a list of possible IDs.
     * 
     * @param a The parent activity.
     * @param ids The list of possible IDs.
     * @return The first Absolute Top. If none exit, returns -1;
     */
    public static int getAbsoluteTop(Activity a, int... ids) {
        for (int id : ids) {
            View v = a.findViewById(id);
            if (v != null && v.getVisibility() == View.VISIBLE)
                return getAbsoluteTop(v);
        }
        return -1;
    }

    public static void setViewsOnClick(Activity a, OnClickListener onclick, int... ids) {
        if (a == null)
            return;
        for (int id : ids)
            if (a.findViewById(id) != null)
                a.findViewById(id).setOnClickListener(onclick);
    }

    public static void setViewsEnabled(Activity a, final boolean enabled, int... ids) {
        if (a == null)
            return;
        for (int id : ids) {
            final View v = a.findViewById(id);
            if (v != null)
                // v.post(new Runnable(){public void run(){
                v.setEnabled(enabled);
            // }});
        }
    }

    public static void setViewsEnabled(View view, final boolean enabled, int... ids) {
        if (view == null)
            return;
        for (int id : ids) {
            final View v = view.findViewById(id);
            if (v != null)
                // v.post(new Runnable(){public void run(){
                v.setEnabled(enabled);
            // }});
        }
    }

    public static void setOnPrefChange(PreferenceManager pm, OnPreferenceChangeListener listener,
            String... keys) {
        for (String key : keys)
            if (pm.findPreference(key) != null)
                pm.findPreference(key).setOnPreferenceChangeListener(listener);
    }

    public static void setOnClicks(PreferenceManager pm, OnPreferenceClickListener listener,
            String... keys) {
        for (String key : keys)
            if (pm.findPreference(key) != null)
                pm.findPreference(key).setOnPreferenceClickListener(listener);
    }

    public static void setOnClicks(View parent, OnClickListener listener, int... ids) {
        for (int id : ids)
            if (parent.findViewById(id) != null)
                parent.findViewById(id).setOnClickListener(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> findChildByClass(ViewGroup parent, Class<T> class1) {
        ArrayList<T> ret = new ArrayList<T>();
        for (int i = 0; i < parent.getChildCount(); i++)
            if (parent.getChildAt(i).getClass().equals(class1))
                ret.add((T)parent.getChildAt(i));
        return ret;
    }

    public static void setOnTouchListener(View view, OnTouchListener l, boolean setChildrenToo) {
        view.setOnTouchListener(l);
        if (setChildrenToo) {
            ViewGroup vg = (ViewGroup)view;
            for (View v : vg.getTouchables())
                v.setOnTouchListener(l);
        }
    }

    public static void setText(Activity activity, final String text, int... textViewID) {
        boolean ui = Thread.currentThread().equals(OpenExplorer.UiThread);
        for (int id : textViewID) {
            final View v = activity.findViewById(id);
            if (v == null || !(v instanceof TextView))
                continue;
            if (ui)
                ((TextView)v).setText(text);
            else
                v.post(new Runnable() {
                    public void run() {
                        ((TextView)v).setText(text);
                    }
                });
        }
    }

    public static void setText(View parent, final CharSequence text, int... textViewID) {
        if (parent == null)
            return;
        boolean ui = Thread.currentThread().equals(OpenExplorer.UiThread);
        for (int id : textViewID) {
            final View v = parent.findViewById(id);
            if (v == null || !(v instanceof TextView))
                continue;
            if (ui)
                ((TextView)v).setText(text);
            else
                v.post(new Runnable() {
                    public void run() {
                        ((TextView)v).setText(text);
                    }
                });
        }
    }

    public static void setText(View parent, final int textId, int... textViewID) {
        if (parent == null)
            return;
        boolean ui = Thread.currentThread().equals(OpenExplorer.UiThread);
        for (int id : textViewID) {
            final View v = parent.findViewById(id);
            if (v == null || !(v instanceof TextView))
                continue;
            if (ui)
                ((TextView)v).setText(textId);
            else
                v.post(new Runnable() {
                    public void run() {
                        ((TextView)v).setText(textId);
                    }
                });
        }
    }

    public static void setViewsVisible(Activity a, final boolean visible, int... ids) {
        boolean ui = Thread.currentThread().equals(OpenExplorer.UiThread);
        if (a == null)
            return;
        for (int id : ids) {
            if (a == null)
                return;
            final View v = a.findViewById(id);
            if (v != null) {
                if (ui)
                    v.setVisibility(visible ? View.VISIBLE : View.GONE);
                else
                    v.post(new Runnable() {
                        public void run() {
                            v.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }
                    });
            }
        }
    }

    public static void setViewsChecked(final View parent, final boolean checked, final int... ids) {
        boolean ui = Thread.currentThread().equals(OpenExplorer.UiThread);
        if (!ui)
            parent.post(new Runnable() {
                public void run() {
                    setViewsChecked(parent, checked, ids);
                }
            });
        for (int id : ids) {
            final View v = parent.findViewById(id);
            if (v != null) {
                if (v instanceof CheckedTextView)
                    ((CheckedTextView)v).setChecked(checked);
                else if (v instanceof CheckBox)
                    ((CheckBox)v).setChecked(checked);
                else if (v instanceof ImageView)
                    ((ImageView)v)
                            .setImageResource(checked ? android.R.drawable.checkbox_on_background
                                    : android.R.drawable.checkbox_off_background);
                else if (v instanceof TextView)
                    ((TextView)v).setCompoundDrawables(
                            parent.getContext()
                                    .getResources()
                                    .getDrawable(
                                            checked ? android.R.drawable.checkbox_on_background
                                                    : android.R.drawable.checkbox_off_background),
                            null, null, null);
            }
        }
    }

    public static void setViewsVisibleNow(final View parent, final boolean visible,
            final int... ids) {
        if (parent == null)
            return;
        final int vis = visible ? View.VISIBLE : View.GONE;
        // parent.post(new Runnable(){public void run(){
        if (ids.length == 0) {
            if (parent.getVisibility() != vis)
                parent.setVisibility(vis);
        } else
            for (int id : ids) {
                View v = parent.findViewById(id);
                if (v != null && v.getVisibility() != vis)
                    v.setVisibility(vis);
            }
        // }});
    }

    public static void setViewsVisible(final View parent, final boolean visible, final int... ids) {
        if (parent == null)
            return;
        parent.post(new Runnable() {
            public void run() {
                setViewsVisibleNow(parent, visible, ids);
            }
        });
    }

    public static void toggleChecked(View view) {
        if (view instanceof CheckedTextView)
            ((CheckedTextView)view).setChecked(!((CheckedTextView)view).isChecked());
    }

    /**
     * Set alpha attribute for multiple views
     * 
     * @param alpha Floating point from 0 to 1
     * @param views Views to set alpha for
     */
    public static void setAlpha(float alpha, View... views) {
        for (View kid : views)
            ViewUtils.setAlpha(kid, alpha);
    }

    /**
     * Set alpha attribute for single view (on pre-Honeycomb, this works for
     * ImageView and TextView only). For SDK 11+, this works for any view.
     * 
     * @param v
     * @param alpha Floating point from 0 to 1
     */
    public static void setAlpha(View v, float alpha) {
        if (alpha > 1)
            alpha /= 255; // fix 8 bit int

        if (v == null)
            return;
        if (Build.VERSION.SDK_INT > 10) {
            Method m;
            try {
                m = View.class.getMethod("setAlpha", new Class[] {
                    Float.class
                });
                m.invoke(v, alpha);
                return;
            } catch (Exception e) {
            }
        }
        if (v instanceof ImageView)
            ((ImageView)v).setAlpha((int)(255 * alpha));
        else if (v instanceof TextView)
            ((TextView)v).setTextColor(((TextView)v).getTextColors().withAlpha((int)(255 * alpha)));
    }

    /**
     * Set alpha attribute for specified set of child views
     * 
     * @param alpha Floating point from 0 to 1
     * @param root Parent element
     * @param ids List of view ids to set alpha for
     */
    public static void setAlpha(float alpha, View root, int... ids) {
        for (int id : ids)
            setAlpha(root.findViewById(id), alpha);
    }

    public static boolean requestFocus(Activity a, int... ids) {
        for (int id : ids) {
            View v = a.findViewById(id);
            if (v != null && v.isShown() && v.isFocusable() && v.requestFocus())
                return true;
        }
        return false;
    }

    public static View inflateView(View view, int stubId) {
        if (view == null)
            return null;
        if (view.findViewById(stubId) == null)
            return null;
        if (!(view.findViewById(stubId) instanceof ViewStub))
            return null;
        return ((ViewStub)view.findViewById(stubId)).inflate();
    }

    public static void setImageResource(View parent, int drawableId, int... ids) {
        if (parent == null)
            return;
        if (ids.length == 0)
            if (parent instanceof ImageView)
                ((ImageView)parent).setImageResource(drawableId);
            else if (parent instanceof ImageButton)
                ((ImageButton)parent).setImageResource(drawableId);
            else
                parent.setBackgroundResource(drawableId);
        else
            for (int id : ids)
                setImageResource(parent.findViewById(id), drawableId);
    }

    public static View getFirstView(Activity a, int... ids) {
        if (a == null)
            return null;
        for (int id : ids) {
            View v = a.findViewById(id);
            if (v != null)
                return v;
        }
        return null;
    }

    public static int getChildIndex(ViewGroup parent, int searchId) {
        for (int i = 0; i < parent.getChildCount(); i++)
            if (parent.getChildAt(i).getId() == searchId)
                return i;
        return -1;
    }

    public static void inflateView(Activity a, int... stubIds) {
        for (int stubId : stubIds)
            if (a.findViewById(stubId) != null && a.findViewById(stubId) instanceof ViewStub)
                ((ViewStub)a.findViewById(stubId)).inflate();
    }

    public static CharSequence getText(View view) {
        if (view == null)
            return "";
        if (view instanceof TextView)
            return ((TextView)view).getText();
        return "";
    }

    public static void setEnabled(boolean enabled, View... views) {
        for (View v : views)
            if (v != null)
                v.setEnabled(enabled);
    }

    public static void setEnabled(View parent, boolean enabled, int... ids) {
        for (int id : ids)
            if (parent.findViewById(id) != null)
                parent.findViewById(id).setEnabled(enabled);
    }
}


package org.brandroid.openmanager.util;

import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.utils.ViewUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;

public class OpenIntentChooser implements android.view.View.OnClickListener {
    private AlertDialog mDialog;
    private Object mTag;
    private List<ResolveInfo> mListResolves;
    private IntentSelectedListener mListener;
    private boolean mDefaultSelected = true;
    private int mIndex = -1;
    private boolean mChooseOnClick = Build.VERSION.SDK_INT < 16;

    public interface IntentSelectedListener {
        void onIntentSelected(ResolveInfo item, boolean defaultSelected);

        void onUseSystemClicked();
    }

    public OpenIntentChooser(final OpenExplorer activity, final OpenPath file) {
        this(activity, IntentManager.getIntent(file, activity));
    }

    public OpenIntentChooser(final OpenExplorer activity, final Intent intent) {
        mListResolves = activity.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        setAdapter(activity, new OpenIntentAdapter(mListResolves));
    }

    public OpenIntentChooser(final Context activity, List<ResolveInfo> mResolves) {
        mListResolves = mResolves;
        setAdapter(activity, new OpenIntentAdapter(mResolves));
    }

    public OpenIntentChooser setAdapter(Context context, final OpenIntentAdapter adapter) {
        final View v = LayoutInflater.from(context).inflate(R.layout.chooser_layout, null);
        ViewUtils.setOnClicks(v, this, R.id.chooser_default, R.id.chooser_system,
                R.id.chooser_always, R.id.chooser_once);
        final GridView lv = (GridView)v.findViewById(android.R.id.list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mChooseOnClick) {
                    if (mIndex > -1)
                        lv.getChildAt(mIndex).setSelected(false);
                    view.setSelected(true);
                    mIndex = position;
                    ViewUtils.setEnabled(v, true, R.id.chooser_always, R.id.chooser_once);
                    return;
                }
                if (mListener != null && mChooseOnClick)
                    mListener.onIntentSelected(mListResolves.get(position), mDefaultSelected);
                if (mDialog != null)
                    mDialog.dismiss();
            }
        });
        mDialog = new AlertDialog.Builder(context).setView(v).create();
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public OpenIntentChooser setOnIntentSelectedListener(IntentSelectedListener listener) {
        mListener = listener;
        return this;
    }

    public OpenIntentChooser setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mDialog.setOnCancelListener(onCancelListener);
        return this;
    }

    public OpenIntentChooser setOnDismissListener(
            DialogInterface.OnDismissListener onDismissListener) {
        mDialog.setOnDismissListener(onDismissListener);
        return this;
    }

    public OpenIntentChooser setTitle(CharSequence title) {
        mDialog.setTitle(title);
        return this;
    }

    public OpenIntentChooser setTitle(int titleId) {
        mDialog.setTitle(titleId);
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooser_always:
                mDefaultSelected = true;
                if (mListener != null && mIndex > -1)
                    mListener.onIntentSelected(mListResolves.get(mIndex), true);
                if (mDialog != null)
                    mDialog.dismiss();
                break;
            case R.id.chooser_once:
                if (mListener != null && mIndex > -1)
                    mListener.onIntentSelected(mListResolves.get(mIndex), false);
                if (mDialog != null)
                    mDialog.dismiss();
                break;
            case R.id.chooser_default:
                mDefaultSelected = ((CheckBox)v).isChecked();
                break;
            case R.id.chooser_system:
                mDialog.cancel();
                if (mListener != null)
                    mListener.onUseSystemClicked();
                break;
        }
    }
}


package org.brandroid.openmanager.fragments;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.fragments.OpenFragment.Poppable;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LogViewerFragment extends OpenFragment implements OnClickListener, Poppable {
    private final List<CharSequence> mData = new Vector<CharSequence>();
    private BaseAdapter mAdapter = null;
    private boolean mAdded = false;
    private BetterPopupWindow mPopup = null;
    private ListView mListView = null;
    private LayoutInflater mInflater = null;
    private Context mContext;
    private String mLast = null;
    private ViewGroup mRootView = null;
    private int mTextResId = R.layout.edit_text_view_row;
    private final Handler mHandler;

    public LogViewerFragment() {
        mHandler = new Handler();
    }

    public static LogViewerFragment getInstance(Bundle args) {
        LogViewerFragment ret = new LogViewerFragment();
        ret.setArguments(args);
        return ret;
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private boolean checkLast(String txt) {
        if (mLast == null)
            return false;
        if (mLast.length() != txt.length())
            return false;
        if (mLast.equals(txt))
            return true;
        int maxDiffs = (mLast.length() / 10) * 9;
        for (int i = 0; i < mLast.length(); i++)
            if (mLast.charAt(i) == txt.charAt(i))
                if (maxDiffs-- <= 0)
                    return false;
        return true;
    }

    public boolean getAdded() {
        return mAdded;
    }

    public void setAdded(boolean added) {
        mAdded = added;
    }

    public void print(final String txt, final int color) {
        if (checkLast(txt))
            return;
        mLast = txt;
        // getActivity().runOnUiThread(
        if(mHandler == null) return;
        mHandler.post(new Runnable() {
            public void run() {
                mData.add(0, colorify(txt, color));
                if(mAdapter != null)
                    mAdapter.notifyDataSetChanged();
            }
        });
    }

    private String getTimeStamp()
    {
        Date d = new Date();
        int m = d.getMinutes();
        int s = d.getSeconds();
        String ret = "";
        if (m < 10)
            ret += "0";
        ret += m;
        if (s < 10)
            ret += "0";
        ret += s + " ";
        return ret;
    }

    private CharSequence colorify(String txt, int color) {
        if (color != 0) {
            color = Color.rgb(255 - Color.red(color), 255 - Color.green(color),
                    255 - Color.blue(color));
            String stamp = getTimeStamp();
            txt = stamp + txt;
            SpannableString line = new SpannableString(txt);
            line.setSpan(new ForegroundColorSpan(color), stamp.length(), line.length(), Spanned.SPAN_COMPOSING);
            return line;
        } else
            return txt;
    }

    public class LogViewerAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public CharSequence getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(mTextResId, parent, false);
            ViewUtils.setViewsVisible(view, false, R.id.text_line);
            ((TextView)view.findViewById(R.id.text_data)).setText(getItem(position));
            return view;
        }

    }

    @Override
    public int getPagerPriority() {
        return 100;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onClick(item.getItemId(), item, null);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View ret = inflater.inflate(R.layout.log_viewer, container, false);
        ret.setOnLongClickListener(this);
        ViewUtils.setOnClicks(ret, this, R.id.log_clear, R.id.log_copy);
        getListView();
        return ret;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new LogViewerAdapter();
        if (getListView() != null)
            getListView().setAdapter(mAdapter);
    }

    public ListView getListView() {

        if (mListView == null && getView() != null) {
            if (getView() instanceof ListView)
                mListView = (ListView)getView();
            else if (getView().findViewById(R.id.log_list) != null
                    && getView().findViewById(R.id.log_list) instanceof ListView)
                mListView = (ListView)getView().findViewById(R.id.log_list);
            if (mListView == null) {
                mListView = new ListView(getView().getContext());
                ((ViewGroup)getView()).addView(mListView);
            }
        }
        return mListView;
    }

    @Override
    public void onClick(View v) {
        onClick(v.getId(), null, v);
    }

    @SuppressWarnings("deprecation")
    public void onClick(int id, MenuItem item, View from) {
        switch (id) {
            case R.id.log_clear: // Clear
                mData.clear();
                notifyDataSetChanged();
                break;
            case R.id.log_copy: // Copy
                ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE);
                cm.setText(Utils.joinArray(mData.toArray(new CharSequence[mData.size()]), "\n"));
                Toast.makeText(mContext, R.string.s_alert_clipboard, Toast.LENGTH_LONG);
                break;
        // default: if(getExplorer() != null) getExplorer().onClick(id, item,
        // from);
        }
    }

    @Override
    public Drawable getIcon() {
        if (isDetached())
            return null;
        return getResources().getDrawable(R.drawable.ic_paper);
    }

    @Override
    public CharSequence getTitle() {
        if (isDetached())
            return "Network Log";
        if (getActivity() == null)
            return "Network Log";
        String ret = getResources().getString(R.string.s_pref_logview);
        if (ret == null)
            ret = "Network Log";
        return ret;
    }

    public ListAdapter getAdapter(Context c) {
        if (mAdapter == null) {
            mAdapter = new LogViewerAdapter();
        }
        return mAdapter;

    }

    @Override
    public View getView() {
        if (mRootView != null)
            return (View)mRootView;
        return super.getView();
    }

    public void setupPopup(Context c, View anchor) {
        if (mPopup == null) {
            mContext = c;
            mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRootView = new LinearLayout(c);
            View view = onCreateView(mInflater, mRootView, getArguments());
            onViewCreated(view, getArguments());
            mPopup = new BetterPopupWindow(c, anchor);
            mPopup.setContentView(mRootView);
        } else
            mPopup.setAnchor(anchor);
    }

    public BetterPopupWindow getPopup() {
        return mPopup;
    }

}


package org.brandroid.openmanager.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.fragments.OpenFragment.Poppable;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LogViewerFragment extends OpenFragment implements Poppable, OnItemClickListener {
    private final SparseArray<LogEntry> mData;
    private final int maxLogCount = 200;
    private int mIndex = 0;
    private LogViewerAdapter mAdapter = null;
    private boolean mAdded = false;
    private BetterPopupWindow mPopup = null;
    private ListView mListView = null;
    private LayoutInflater mInflater = null;
    private String mLast = null;
    private ViewGroup mRootView = null;
    private final int mTextResId = R.layout.edit_text_view_row;
    private final Handler mHandler;
    private static final DateFormat mDateFormat = SimpleDateFormat.getTimeInstance();

    public LogViewerFragment() {
        mHandler = new Handler();
        mData = new SparseArray<LogEntry>(maxLogCount);
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
                mData.put(mIndex++ % maxLogCount, new LogEntry(txt, color));
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

    public static CharSequence colorify(String txt, int color) {
        if (color != 0) {
            color = Color.rgb(
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color));
            //String stamp = getTimeStamp();
            //txt = stamp + txt;
            SpannableString line = new SpannableString(txt);
            line.setSpan(new ForegroundColorSpan(color), 0, line.length(), Spanned.SPAN_COMPOSING);
            return line;
        } else
            return txt;
    }

    public class LogViewerAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return Math.min(maxLogCount, mIndex);
        }

        @Override
        public LogEntry getItem(int position) {
            return mData.get((int)getItemId(position));
        }

        @Override
        public long getItemId(int position) {
            return ((mIndex - position - 1) % maxLogCount);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(mTextResId, parent, false);
            ViewUtils.setViewsVisible(view, false, R.id.text_line);
            LogEntry data = getItem(position);
            if(data == null)
                return view;
            Long stamp = data.getStamp();
            String source = data.getSource();
            SpannableStringBuilder txt = new SpannableStringBuilder();
            txt.append(data.getEntryNumber() + " - ")
                .append(colorify(source, Color.WHITE))
                .append(colorify(" - " + mDateFormat.format(new Date(stamp)) + "\n", Color.BLUE))
                .append(data.getSummary());
            ((TextView)view.findViewById(R.id.text_data)).setText(txt);
            return view;
        }

    }

    @Override
    public int getPagerPriority() {
        return 100;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View ret = inflater.inflate(R.layout.log_viewer, container, false);
        ret.setOnLongClickListener(this);
        ret.findViewById(R.id.log_clear).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mData.clear();
                notifyDataSetChanged();
            }
        });
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
            mListView.setOnItemClickListener(this);
        }
        return mListView;
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

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LogEntry entry = mAdapter.getItem(position);
        final CharSequence txt = entry.getData();
        final Context context = parent.getContext();
        final TextView msg = new TextView(context);
        msg.setTextSize(12f);
        msg.setText(txt);
        if(Build.VERSION.SDK_INT > 10)
            msg.setTextIsSelectable(true);
        try {
        new AlertDialog.Builder(context)
            .setView(msg)
            .setTitle(entry.getTitle())
            .setPositiveButton(R.string.s_menu_copy, new DialogInterface.OnClickListener() {
                @SuppressWarnings("deprecation")
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        ClipboardManager clip = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clip.setText(txt);
                        makeToast(context, txt.length() + " bytes added to clipboard.");
                    } catch(Exception e) {
                        if(context != null)
                            makeToast(context, "Unable to set clipboard. " + e.getMessage());
                    }
                }
            })
            .setNegativeButton(android.R.string.ok, null)
            .create().show();
        } catch(Exception e) {
            Logger.LogError("Unable to show dialog in LogViewer", e);
        }
    }
    
    public static class LogEntry
    {
        private final CharSequence mData;
        private final String mSource;
        private final Long mStamp;
        private final int mPos;
        private final CharSequence mSummary;
        private static int totalEntries = 0; 
        
        public LogEntry(CharSequence data, String source, Long stamp)
        {
            mData = data;
            mSource = source;
            mStamp = stamp;
            if(mData.length() > 150)
                mSummary = new SpannableStringBuilder(mData.subSequence(0, 150)).append("...");
            else mSummary = null;
            mPos = ++totalEntries;
        }
        
        public CharSequence getTitle()
        {
            return new SpannableStringBuilder()
                .append(colorify(getEntryNumber() + ": ", Color.DKGRAY))
                .append(colorify(getSource(), Color.WHITE))
                .append(colorify(" - " + mDateFormat.format(new Date(getStamp())), Color.DKGRAY));
        }
        
        public int getEntryNumber() {
            return mPos;
        }
        
        public LogEntry(String full, int color)
        {
            if(full.indexOf(" - ") > -1)
            {
                mSource = full.substring(0, full.indexOf(" - "));
                full = full.substring(full.indexOf(" - ") + 3);
            } else mSource = "???";
            mData = colorify(full, color);
            if(mData.length() > 150)
                mSummary = colorify(shrink(full.replaceAll("\\s\\s*", " "), 150), color);
            else mSummary = null;
            mStamp = new Date().getTime();
            mPos = ++totalEntries;
        }
        
        private String shrink(String c, int maxLength)
        {
            if(c.length() > maxLength)
                return c.substring(0, maxLength) + "...";
            return c;
        }
        
        public CharSequence getSummary() { return Utils.ifNull(mSummary, mData); }
        public CharSequence getData() { return mData; }
        public String getSource() { return mSource; }
        public Long getStamp() { return mStamp; }
    }

}

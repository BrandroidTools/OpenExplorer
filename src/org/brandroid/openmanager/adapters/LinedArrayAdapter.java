
package org.brandroid.openmanager.adapters;

import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.utils.ViewUtils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LinedArrayAdapter extends ArrayAdapter<CharSequence> {
    private LayoutInflater mInflater;
    private int mCount;
    private float mTextSize = 10f;
    private int mTextResId = R.layout.edit_text_view_row;
    private boolean mShowLineNumbers = true;

    public LinedArrayAdapter(Context context, int resId, List<CharSequence> data) {
        super(context, resId, data);
        mTextResId = resId;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        mCount = getCount();
        super.notifyDataSetChanged();
    }

    private String repeat(String str, int cnt) {
        if (cnt <= 0)
            return "";
        String ret = str;
        while (--cnt > 0)
            ret += str;
        return ret;
    }

    public void setTextSize(float size) {
        if (size == mTextSize)
            return;
        mTextSize = size;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = mInflater.inflate(mTextResId, parent, false);
        if (mShowLineNumbers) {
            Integer pos = position + 1;
            ((TextView)view.findViewById(R.id.text_line)).setText(repeat(" ", ((Integer)mCount)
                    .toString().length() - pos.toString().length())
                    + pos);
        }
        ViewUtils.setViewsVisible(view, mShowLineNumbers, R.id.text_line);
        ((TextView)view.findViewById(R.id.text_data)).setText(getItem(position));
        if (mTextSize != 10) {
            ((TextView)view.findViewById(R.id.text_line)).setTextSize(mTextSize - 1);
            ((TextView)view.findViewById(R.id.text_data)).setTextSize(mTextSize);
        }
        return view;
    }

    public void setShowLineNumbers(boolean showNums) {
        mShowLineNumbers = showNums;
    }
}

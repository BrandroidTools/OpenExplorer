
package org.brandroid.openmanager.util;

import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OpenIntentAdapter extends BaseAdapter {
    private List<ResolveInfo> mData;

    public OpenIntentAdapter(List<ResolveInfo> queriedIntents) {
        mData = queriedIntents;
    }

    public int getCount() {
        return mData.size();
    }

    public ResolveInfo getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ResolveInfo item = mData.get(position);

        Context c = parent.getContext();

        PackageManager pm = c.getPackageManager();

        View res = convertView;
        if (res == null) {
            res = LayoutInflater.from(c).inflate(R.layout.chooser_item, null);
        }

        TextView tv = (TextView)res.findViewById(android.R.id.text1);
        ImageView iv = (ImageView)res.findViewById(android.R.id.icon);

        CharSequence cs = item.loadLabel(pm);
        res.setTag(item);
        tv.setText(cs);
        Drawable d = (Drawable)item.loadIcon(pm);
        if (BitmapDrawable.class.equals(d))
            ((BitmapDrawable)d).setGravity(Gravity.CENTER);
        // ScaleDrawable sd = new ScaleDrawable(d, Gravity.CENTER,
        // OpenExplorer.DP_RATIO * 48, OpenExplorer.DP_RATIO * 48);
        // Rect size = d.getBounds();
        // Logger.LogDebug(cs.toString() + " Icon Size: " +
        // size.toShortString());
        iv.setImageDrawable(d);
        // res.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        return res;
    }
}

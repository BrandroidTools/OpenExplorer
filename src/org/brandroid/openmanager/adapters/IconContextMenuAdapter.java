
package org.brandroid.openmanager.adapters;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class IconContextMenuAdapter extends BaseAdapter {
    private Context context;
    private MenuBuilder myMenu;
    private boolean hasIcons = false;
    private boolean hasChecks = false;
    private int textLayoutId = R.layout.simple_list_item_multiple_choice;

    public IconContextMenuAdapter(Context context, Menu menu) {
        this.context = context;
        myMenu = new MenuBuilder(context);
        setMenu(menu);
    }

    public void setMenu(Menu menu) {
        if (menu == null)
            return;
        myMenu.clear();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (!item.isVisible())
                continue;
            if (item.getIcon() != null)
                hasIcons = true;
            if (item.isCheckable() || item.isChecked())
                hasChecks = true;
            myMenu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle())
                    .setIcon(item.getIcon()).setCheckable(item.isCheckable())
                    .setChecked(item.isChecked()).setVisible(item.isVisible())
                    .setEnabled(item.isEnabled());
        }
        notifyDataSetChanged();
    }

    public void setTextLayout(int layoutId) {
        textLayoutId = layoutId;
        notifyDataSetChanged();
    }

    // @Override
    public int getCount() {
        return myMenu.size();
    }

    // @Override
    public MenuItem getItem(int position) {
        if (position < myMenu.size() && position >= 0)
            return (MenuItem)myMenu.getItem(position);
        else
            return null;
    }

    public MenuItem findItem(int id) {
        return (MenuItem)myMenu.findItem(id);
    }

    // @Override
    public long getItemId(int position) {
        return position;
        // return getItem(position).getItemId();
    }

    // @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(parent, getItem(position), textLayoutId);
    }

    public static View createView(ViewGroup parent, MenuItem item, int textLayoutId) {
        Context context = parent.getContext();
        TextView res = (TextView)LayoutInflater.from(context).inflate(textLayoutId, parent, false);

        if (!item.isVisible())
            res.setVisibility(View.GONE);
        if (!item.isEnabled())
            res.setEnabled(false);
        else
            res.setEnabled(true);

        int checkId = android.R.drawable.checkbox_off_background;
        if (item.isCheckable() || item.isChecked())
            checkId = item.isChecked() ? android.R.drawable.checkbox_on_background
                    : android.R.drawable.checkbox_off_background;
        Drawable check = null;
        if (res instanceof CheckedTextView) {
            if (item.getGroupId() > 0 || item.getTitle().toString().indexOf("View") > -1
                    || item.getTitle().toString().indexOf("Sort") > -1)
                ((CheckedTextView)res).setCheckMarkDrawable(android.R.drawable.btn_radio);
            checkId = 0;
            if (item.isCheckable() || item.isChecked()) {
                ((CheckedTextView)res).setChecked(item.isChecked());
            } else {
                ((CheckedTextView)res).setCheckMarkDrawable(null);
            }
        } else if (checkId > 0)
            check = res.getResources().getDrawable(checkId);

        // else if(item.isChecked())

        CharSequence title = item.getTitle();

        if (OpenExplorer.IS_KEYBOARD_AVAILABLE) {
            char sc = item.getAlphabeticShortcut();
            if (sc > 0) {
                int pos = title.toString().toLowerCase().lastIndexOf(sc);
                if (pos > -1)
                    title = title.subSequence(0, pos) + "<b><u>" + title.subSequence(pos, pos + 1)
                            + "</u></b>" + title.subSequence(pos + 1, title.length());
                else
                    title = title + " (<b><u>" + (sc == '*' ? "DEL" : sc) + "</u></b>)";
            }
        }

        // res.setTextSize(context.getResources().getDimension(R.dimen.large_text_size));
        // res.setTextColor(context.getResources().getColor(android.R.color.primary_text_dark));

        int sz = (int)(res.getResources().getDimension(R.dimen.one_dp) * 32f);
        Drawable src = item.getIcon();
        if (src != null && src instanceof BitmapDrawable)
            ((BitmapDrawable)src).setGravity(Gravity.CENTER);
        Drawable icon = new LayerDrawable(new Drawable[] {
            src != null ? src : new ColorDrawable(android.R.color.white)
        });
        // Gravity.CENTER, sz, sz);

        ViewUtils.setAlpha(res, item.isEnabled() ? 1f : 0.4f);

        res.setEnabled(item.isEnabled());
        res.setTag(item);
        res.setText(Html.fromHtml(title.toString()));
        if (src != null || check != null) {
            res.setCompoundDrawablesWithIntrinsicBounds(icon, null, check, null);
            res.setCompoundDrawablePadding(8);
        }

        res.setPadding(8, 8, 8, 8);
        return res;
    }
}

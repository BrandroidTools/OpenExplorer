
package org.brandroid.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class MenuSubMenuImpl extends MenuBuilderNew implements SubMenu {
    private MenuItem mItem;

    public MenuSubMenuImpl(Context context) {
        super(context);
        mItem = new MenuItemImplNew(this, 0, 0, 0, 0, "pewp");
    }

    public MenuSubMenuImpl(MenuBuilderNew menu, int group, int id, int categoryOrder, int ordering,
            CharSequence title) {
        super(menu.getContext());
        mItem = new MenuItemImplNew(menu, group, id, categoryOrder, ordering, title);
    }

    public void clearHeader() {
        // TODO do this
    }

    public MenuItem getItem() {
        return mItem;
    }

    public SubMenu setHeaderIcon(int iconRes) {
        mItem.setIcon(iconRes);
        return this;
    }

    public SubMenu setHeaderIcon(Drawable icon) {
        mItem.setIcon(icon);
        return this;
    }

    public SubMenu setHeaderTitle(int titleRes) {
        mItem.setTitle(titleRes);
        return this;
    }

    public SubMenu setHeaderTitle(CharSequence title) {
        mItem.setTitle(title);
        return this;
    }

    public SubMenu setHeaderView(View view) {
        // mItem.set
        return this;
    }

    public SubMenu setIcon(int iconRes) {
        mItem.setIcon(iconRes);
        return this;
    }

    public SubMenu setIcon(Drawable icon) {
        mItem.setIcon(icon);
        return this;
    }
}

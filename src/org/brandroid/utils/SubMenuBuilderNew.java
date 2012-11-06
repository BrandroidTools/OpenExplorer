/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brandroid.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window.Callback;

/**
 * The model for a sub menu, which is an extension of the menu. Most methods are
 * proxied to the parent menu.
 */
public class SubMenuBuilderNew extends MenuBuilderNew implements SubMenu {
    private MenuBuilderNew mParentMenu;
    private MenuItemImplNew mItem;

    public SubMenuBuilderNew(Context context, MenuBuilderNew parentMenu, MenuItemImplNew item) {
        super(context);

        mParentMenu = parentMenu;
        mItem = item;
    }

    // @Override
    public void setQwertyMode(boolean isQwerty) {
        mParentMenu.setQwertyMode(isQwerty);
    }

    // @Override
    public boolean isQwertyMode() {
        return mParentMenu.isQwertyMode();
    }

    // @Override
    public void setShortcutsVisible(boolean shortcutsVisible) {
        // TODO mParentMenu.setShortcutsVisible(shortcutsVisible);
    }

    // @Override
    public boolean isShortcutsVisible() {
        // TODO return mParentMenu.isShortcutsVisible();
        return false;
    }

    public Menu getParentMenu() {
        return mParentMenu;
    }

    public MenuItem getItem() {
        return mItem;
    }

    // @Override
    public void setCallback(Callback callback) {
        // TODO mParentMenu.setCallback(callback);
    }

    // @Override
    public MenuBuilderNew getRootMenu() {
        return mParentMenu;
    }

    // @Override
    boolean dispatchMenuItemSelected(MenuBuilderNew menu, MenuItem item) {
        // TODO return super.dispatchMenuItemSelected(menu, item) ||
        // mParentMenu.dispatchMenuItemSelected(menu, item);
        return false;
    }

    public SubMenu setIcon(Drawable icon) {
        mItem.setIcon(icon);
        return this;
    }

    public SubMenu setIcon(int iconRes) {
        mItem.setIcon(iconRes);
        return this;
    }

    public SubMenu setHeaderIcon(Drawable icon) {
        return null; // TODO return (SubMenu)super.setHeaderIconInt(icon);
    }

    public SubMenu setHeaderIcon(int iconRes) {
        return null; // TODO return (SubMenu) super.setHeaderIconInt(iconRes);
    }

    public SubMenu setHeaderTitle(CharSequence title) {
        return null; // TODO return (SubMenu) super.setHeaderTitleInt(title);
    }

    public SubMenu setHeaderTitle(int titleRes) {
        return null; // TODO return (SubMenu) super.setHeaderTitleInt(titleRes);
    }

    public SubMenu setHeaderView(View view) {
        return null; // TODO return (SubMenu) super.setHeaderViewInt(view);
    }

    // @Override
    public boolean expandItemActionView(MenuItemImplNew item) {
        return false; // TODO return mParentMenu.expandItemActionView(item);
    }

    // @Override
    public boolean collapseItemActionView(MenuItemImplNew item) {
        return false; // TODO return mParentMenu.collapseItemActionView(item);
    }

    // @Override
    public String getActionViewStatesKey() {
        final int itemId = mItem != null ? mItem.getItemId() : 0;
        if (itemId == 0) {
            return null;
        }
        return null; // TODO return super.getActionViewStatesKey() + ":" +
                     // itemId;
    }

    public void clearHeader() {
        // TODO Auto-generated method stub

    }
}


package org.brandroid.openmanager.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.BetterPopupWindow;
import org.brandroid.openmanager.util.BetterPopupWindow.OnPopupShownListener;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuUtils;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.actionbarsherlock.internal.view.menu.MenuBuilder;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class IconContextMenu implements OnKeyListener {

    public interface IconContextItemSelectedListener {
        void onIconContextItemSelected(IconContextMenu menu, MenuItem item, Object info, View view);
    }

    private ScrollView mScroller;
    private TableLayout mTable;
    // private Dialog dialog;
    protected final BetterPopupWindow popup;
    private Menu menu;
    protected View anchor;
    private int mPosition;
    private int maxColumns = 1;
    private int mWidth = 0;
    private int rotation = 0;
    private boolean allowTouchFocus = false;
    private CharSequence mTitle = null;
    private int textLayoutId = R.layout.simple_list_item_multiple_choice;
    // private static final Hashtable<Integer, IconContextMenu> mInstances = new
    // Hashtable<Integer, IconContextMenu>();
    private static final Hashtable<Integer, Integer> mHeights = new Hashtable<Integer, Integer>();
    private static final int[] DOUBLE_WIDTH_IDS = new int[] {
        R.id.menu_context_download
    };
    private static final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;

    private OnKeyListener mKeyListener;
    private IconContextItemSelectedListener iconContextItemSelectedListener;
    private Object info;

    public IconContextMenu(Context context, int menuId, View from) {
        this(context, newMenu(context, menuId), from);
    }

    public static Menu newMenu(Context context, int menuId) {
        MenuBuilder menu = new MenuBuilder(context);
        try {
            new MenuInflater(context).inflate(menuId, menu);
        } catch (ClassCastException e) {
            Logger.LogWarning("Couldn't inflate menu (0x" + Integer.toHexString(menuId) + ")", e);
            return null;
        }
        return menu;
    }

    public IconContextMenu(Context context, Menu showMenu, final View from) {
        // root.setLayoutParams(new
        // LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        // ViewGroup.LayoutParams.WRAP_CONTENT));
        menu = showMenu;

        anchor = from;
        rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        // this.dialog = new AlertDialog.Builder(context);
        popup = new BetterPopupWindow(context, anchor);
        mScroller = (ScrollView)LayoutInflater.from(context).inflate(R.layout.icon_menu, null);
        mTable = (TableLayout)mScroller.findViewById(R.id.icon_menu_table);
        refreshTable();
        // mTable.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        // LayoutParams.WRAP_CONTENT));
        // mGrid.setNumColumns(maxColumns);
        // mTable.setOnKeyListener(this);
        // popup.setOnKeyListener(this);
        // mTable.setColumnStretchable(0, true);
        // setAdapter(context, new IconContextMenuAdapter(context, menu));
        if (mWidth == 0) {
            // mGrid.measure(LayoutParams.WRAP_CONTENT,
            // LayoutParams.WRAP_CONTENT);
            // mWidth = mGrid.getMeasuredWidth();
        }
        if (mWidth == 0)
            mWidth = LayoutParams.WRAP_CONTENT; // context.getResources().getDimensionPixelSize(R.dimen.popup_width);
        if (mWidth > 0)
            popup.setPopupWidth(mWidth);
        popup.setContentView(mScroller);
        // if(mWidth > 0)
        // popup.setPopupWidth(mWidth);
    }

    /*
     * private IconContextMenuAdapter getAdapter() { return
     * (IconContextMenuAdapter) mGrid.getAdapter(); } public void
     * setAdapter(Context context, final IconContextMenuAdapter adapter) {
     * mGrid.setAdapter(adapter); Logger.LogInfo("mGrid Adapter set");
     * mGrid.setOnItemClickListener(new OnItemClickListener() { public void
     * onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
     * if(!adapter.getItem(pos).isEnabled()) return;
     * if(iconContextItemSelectedListener != null) {
     * iconContextItemSelectedListener.onIconContextItemSelected(
     * IconContextMenu.this, adapter.getItem(pos), info, v); } } } ); //root.
     * //popup
     * .setBackgroundDrawable(context.getResources().getDrawable(R.drawable
     * .contextmenu_top_right)); //popup.setContentView(mList); /*this.dialog =
     * new AlertDialog.Builder(context) .setAdapter(adapter, new
     * DialogInterface.OnClickListener() { //@Override public void
     * onClick(DialogInterface dialog, int which) { if
     * (iconContextItemSelectedListener != null) {
     * iconContextItemSelectedListener
     * .onIconContextItemSelected(adapter.getItem(which), info); } } })
     * .setInverseBackgroundForced(true) .create(); / }
     */

    /**
     * Set whether or not to allow setFocusableInTouchMode on first item. This
     * should be set to true if the input is from a keyboard. However, if it is
     * from touch input, this should be left set to false.
     * 
     * @param allow Boolean indicating to allow setFocusableInTouchMode.
     */
    public void setAllowTouchFocus(boolean allow) {
        allowTouchFocus = allow;
    }

    public void refreshTable() {
        final Context context = mTable.getContext();
        mTable.post(new Runnable() {
            public void run() {
                mTable.setStretchAllColumns(false);
                mTable.setColumnStretchable(0, false);
                mTable.removeAllViews();
                // mTable.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);
                // mTable.setDividerDrawable(context.getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
                if (mTitle != null) {
                    if (mScroller.findViewById(R.id.icon_menu_top) != null
                            && mScroller.findViewById(R.id.icon_menu_top).findViewById(
                                    android.R.id.title) != null)
                        ViewUtils.setText(mScroller.findViewById(R.id.icon_menu_top), mTitle,
                                android.R.id.title);
                    else {
                        View ttl = LayoutInflater.from(context).inflate(R.layout.popup_title,
                                (ViewGroup)mScroller.findViewById(R.id.icon_menu_top), false);
                        ((TextView)ttl.findViewById(android.R.id.title)).setText(mTitle);
                        ((ViewGroup)mScroller.findViewById(R.id.icon_menu_top)).addView(ttl);
                    }
                }
                TableRow row = new TableRow(context);
                row.setOnKeyListener(IconContextMenu.this);
                Menu m2 = new MenuBuilder(context);
                ArrayList<Integer> ida = new ArrayList<Integer>();
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    if (item.isVisible()) {
                        MenuUtils.transferMenu(item, m2);
                        ida.add(item.getItemId());
                    }
                }
                Integer[] ids = ida.toArray(new Integer[ida.size()]);
                int id1 = 0, idL = 0;
                for (int i = 0; i < m2.size(); i++) {
                    final MenuItem item = m2.getItem(i);
                    final int id = item.getItemId();
                    int col = i % maxColumns;
                    boolean dbl = Utils.getArrayIndex(DOUBLE_WIDTH_IDS, id) > -1;
                    if (col == 0) {
                        if (i > 0)
                            mTable.addView(row);
                        row = new TableRow(context);
                        row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    }
                    View kid = IconContextMenuAdapter.createView(row, item, textLayoutId);
                    kid.setId(id);
                    kid.setBackgroundResource(android.R.drawable.list_selector_background);
                    kid.setFocusable(true);
                    if (i == 0) {
                        if (allowTouchFocus)
                            kid.setFocusableInTouchMode(true);
                        kid.requestFocus();
                    }
                    if (maxColumns == 1)
                        kid.setOnKeyListener(IconContextMenu.this);
                    kid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (iconContextItemSelectedListener != null)
                                iconContextItemSelectedListener.onIconContextItemSelected(
                                        IconContextMenu.this, item, menu, v);
                        }
                    });
                    if (textLayoutId == R.layout.simple_list_item_multiple_choice)
                        kid.setPadding(8, 8, 8, 8);
                    else
                        kid.setPadding(2, 2, 2, 2);
                    ((TextView)kid).setCompoundDrawablePadding(8);
                    row.addView(kid);
                    if (i == maxColumns - 1)
                        kid.setNextFocusUpId(ids[ids.length - 1]);
                    else if (i == 0)
                        kid.setNextFocusUpId(ids[ids.length - maxColumns]);
                    else if (i == ids.length - 1)
                        kid.setNextFocusDownId(ids[maxColumns - 1]);
                    else if (i == ids.length - maxColumns)
                        kid.setNextFocusDownId(ids[0]);
                }
                if (row.getChildCount() > 0) {
                    mTable.addView(row);
                }
                mTable.setStretchAllColumns(true);
                mTable.setOnKeyListener(IconContextMenu.this);
            }
        });
    }

    public void setOnKeyListener(OnKeyListener listener) {
        mKeyListener = listener;
        // popup.setOnKeyListener(listener);
        // if(mTable != null)
        // mTable.setOnKeyListener(listener);
    }

    public void setNumColumns(int cols) {
        maxColumns = cols;
        // if(mGrid != null)
        // mGrid.setNumColumns(cols);
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public Object getInfo() {
        return info;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        popup.setOnDismissListener(listener);
    }

    public void setOnIconContextItemSelectedListener(
            IconContextItemSelectedListener iconContextItemSelectedListener) {
        this.iconContextItemSelectedListener = iconContextItemSelectedListener;
    }

    public void setTitle(CharSequence title) {
        // popup.setTitle(title);
        mTitle = title;
    }

    private int getMenuSignature() {
        return rotation * 1000 + maxColumns * 100 + getMenuSignature(menu);
    }

    private int getMenuSignature(Menu menu) {
        if (menu == null)
            return 0;
        int ret = 0;
        for (int i = 0; i < menu.size(); i++)
            ret += getMenuSignature(menu.getItem(i));
        return ret;
    }

    private int getMenuSignature(MenuItem item) {
        if (item == null)
            return 0;
        if (!item.isVisible())
            return 0;
        if (item.getIcon() != null)
            return 3;
        if (item.isCheckable())
            return 2;
        return 1;
    }

    public boolean show() {
        return show(0, 0);
    }

    public boolean show(int left, int top) {
        refreshTable();
        // popup.showLikeQuickAction();
        final int menuSig = getMenuSignature();
        if (mHeights.containsKey(menuSig)) {
            if (DEBUG)
                Logger.LogDebug("Menu Signature (" + menuSig + ") found = " + mHeights.get(menuSig));
            popup.setPopupHeight(mHeights.get(menuSig));
        }
        popup.setPopupShownListener(new OnPopupShownListener() {
            @Override
            public void OnPopupShown(int width, int height) {
                if (DEBUG)
                    Logger.LogVerbose("Popup Height: " + height);
                mHeights.put(menuSig, height);
            }
        });
        return popup.showLikePopDownMenu(left, top);
    }

    public void dismiss() {
        popup.dismiss();
    }

    public void setPopupWidth(int w) {
        mWidth = w;
        if (popup != null)
            popup.setPopupWidth(w);
    }

    public static IconContextMenu getInstance(Context c, int menuId, View from) {
        // if(!mInstances.containsKey(menuId))
        {
            MenuBuilder menu = new MenuBuilder(c);
            new MenuInflater(c).inflate(menuId, menu);
            if (menu == null) {
                Logger.LogWarning("IconContextMenu getInstance(0x" + Integer.toHexString(menuId)
                        + ") is null");
                return null;
            }
            return new IconContextMenu(c, menu, from);
            // mInstances.put(menuId, new IconContextMenu(c, menu, from));
        }
        /*
         * else Logger.LogDebug("IContextMenu Instance Height: " +
         * mInstances.get(menuId).popup.getPopupHeight()); IconContextMenu ret =
         * mInstances.get(menuId); ret.setAnchor(from); return ret;
         */
    }

    public static void clearInstances() {
        // mInstances.clear();

    }

    public void setMenu(Menu newMenu) {
        /* getAdapter().setMenu(newMenu); */
        menu = newMenu;
    }

    public void setAnchor(View from) {
        this.anchor = from;
        popup.setAnchor(from);
    }

    public void setTextLayout(int layoutId) {
        // getAdapter().setTextLayout(layoutId);
        textLayoutId = layoutId;
        refreshTable();
    }

    public int getSelectedItemPosition() {
        return mPosition;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Logger.LogDebug("IconContextMenu.onKey(" + anchor + "," + keyCode + "," + event + ")");
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            dismiss();
            return true;
        }
        if (iconContextItemSelectedListener != null) {
            final MenuItem item = MenuUtils.getMenuShortcut(keyCode);
            if (item != null) {
                final View view = mTable.findViewById(item.getItemId());
                if (view != null) {
                    view.post(new Runnable() {
                        public void run() {
                            Toast.makeText(view.getContext(), item.getTitle(), Toast.LENGTH_SHORT)
                                    .show();
                            view.setFocusableInTouchMode(true);
                            view.requestFocus();
                            view.performClick();
                        }
                    });
                    return true;
                }
                iconContextItemSelectedListener.onIconContextItemSelected(this, item, event, v);
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (event.getAction() != KeyEvent.ACTION_UP)
                return true;
            dismiss();
            int id = 0;
            // List<View> focusables = null;
            View tmp = anchor;
            // if(anchor.requestFocus(keyCode == KeyEvent.KEYCODE_DPAD_LEFT ?
            // View.FOCUS_LEFT : View.FOCUS_RIGHT))
            // tmp = anchor.getRootView().findFocus();
            // else {
            View parent = (View)anchor.getParent();
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                // focusables = anchor.getFocusables(View.FOCUS_LEFT);
                do {
                    tmp = parent.findViewById(tmp.getNextFocusLeftId());
                } while (tmp != null && !tmp.isShown());
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                // focusables = anchor.getFocusables(View.FOCUS_RIGHT);
                do {
                    tmp = parent.findViewById(tmp.getNextFocusRightId());
                } while (tmp != null && !tmp.isShown());
            else {
                Logger.LogDebug("No sibling for 0x" + Integer.toHexString(v.getId()));
                return false;
            }
            // }

            /*
             * if(focusables == null) return false; for(int i = 1; i <
             * focusables.size(); i++) { tmp = focusables.get(i); if(tmp != null
             * && tmp.isShown()) break; }
             */
            if (tmp == null)
                return false;
            Logger.LogDebug("Next focusable form " + anchor + " is " + tmp);
            if (allowTouchFocus)
                tmp.setFocusableInTouchMode(true);
            tmp.requestFocus();
            if (tmp.getTag() != null && tmp.getTag() instanceof Menu)
                tmp.performClick();
            return true;
        }
        if (mKeyListener != null && mKeyListener.onKey(anchor, keyCode, event))
            return true;
        int pos = getSelectedItemPosition();
        int col = pos % maxColumns;
        if (col == 0 && keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
            return true;
        if (col == maxColumns - 1 && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
            return true;
        return false;
    }

    /*
     * public void setOnCancelListener(DialogInterface.OnCancelListener
     * onCancelListener) { popup.setOnCancelListener(onCancelListener); } public
     * void setOnDismissListener(DialogInterface.OnDismissListener
     * onDismissListener) { dialog.setOnDismissListener(onDismissListener); }
     * public void show() { dialog.show(); } public void dismiss() {
     * dialog.dismiss(); } public void cancel() { dialog.cancel(); } public
     * Dialog getDialog() { return dialog; }
     */
}

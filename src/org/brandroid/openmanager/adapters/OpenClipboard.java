
package org.brandroid.openmanager.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.Logger;
import org.brandroid.utils.ViewUtils;

import android.text.ClipboardManager;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * System Clipboard Wrapper. Also implements Adapter so that it can be directly
 * used by ListView.
 * 
 * @author BrandoCommando
 */
@SuppressLint("NewApi")
public class OpenClipboard extends BaseAdapter implements Set<OpenPath> {
    private static final long serialVersionUID = 8847538312028343319L;
    public boolean DeleteSource = false;
    public boolean ClearAfter = true;
    private final ArrayList<OpenPath> list = new ArrayList<OpenPath>();
    private final Context mContext;
    private OnClipboardUpdateListener listener = null;
    // private boolean mMultiselect = false;
    private OpenPath mCurrentPath = null;
    private OnClickListener mOnClickListener = null;

    public static class OpenClipItem {
        public OpenPath Path;
        public OpenClipboardOperation Operation;

        public OpenClipItem(OpenPath path, OpenClipboardOperation op) {
            Path = path;
            Operation = op;
        }
    }

    public enum OpenClipboardOperation {
        COPY, CUT
    }

    public interface OnClipboardUpdateListener {
        public void onClipboardUpdate();
    }

    @SuppressWarnings("deprecation")
    private void onClipboardUpdate() {
        super.notifyDataSetChanged();
        if (listener != null)
            listener.onClipboardUpdate();
        ClipboardManager clip = (ClipboardManager)mContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip == null)
            return;
        StringBuilder clipText = new StringBuilder();
        for (int i = 0; i < size(); i++)
            if (get(i) != null && get(i).getPath() != null)
                clipText.append(get(i).getPath() + "\n");
        if (!OpenExplorer.BEFORE_HONEYCOMB && clip instanceof android.content.ClipboardManager) {
            ClipData data = ClipData.newPlainText("files", clipText);
            try {
                ((android.content.ClipboardManager)clip).setPrimaryClip(data);
            } catch (Exception e) {
                Logger.LogWarning("Unable to set Clipboard data.");
                try {
                    Toast.makeText(mContext, "Error setting clipboard data", Toast.LENGTH_LONG)
                            .show();
                } catch (Exception te) {

                }
            }
        } else {
            clip.setText(clipText);
        }
    }

    public OpenClipboard(Context context) {
        super();
        this.mContext = context;
        readSystemClipboard();
    }

    private void readSystemClipboard() {
        ClipboardManager sysboard = (ClipboardManager)mContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (!OpenExplorer.BEFORE_HONEYCOMB) {
            ClipData data = ((android.content.ClipboardManager)sysboard).getPrimaryClip();
            if (data != null) {
                for (int i = 0; i < data.getItemCount(); i++) {
                    String txt = data.getItemAt(i).coerceToText(mContext).toString();
                    for (String s : txt.split("\n"))
                        if (s != null && new File(s).exists())
                            add(new OpenFile(s));
                }
                return;
            }
        } else {
            @SuppressWarnings("deprecation")
            CharSequence clipText = sysboard.getText();
            if (clipText != null) {
                for (String s : clipText.toString().split("\n"))
                    if (s != null && new File(s).exists())
                        add(new OpenFile(s));
            }
        }
    }

    /*
     * ActionMode should now control Multiselect public void startMultiselect()
     * { mMultiselect = true; onClipboardUpdate(); } public void
     * stopMultiselect() { mMultiselect = false; onClipboardUpdate(); } public
     * boolean isMultiselect() { return mMultiselect; }
     */

    /*
     * Path hint for drop down menu
     */
    public void setCurrentPath(OpenPath p) {
        mCurrentPath = p;
    }

    public void setClipboardUpdateListener(OnClipboardUpdateListener listener) {
        this.listener = listener;
    }

    public int getCount() {
        return list.size();
    }

    public boolean hasPastable() {
        for (OpenPath p : list)
            if (isPastable(p))
                return true;
        return false;
    }

    private boolean isPastable(OpenPath item) {
        if (mCurrentPath == null)
            return true;
        if (item.getParent() == null)
            return true;
        if (mCurrentPath.equals(item.getParent()))
            return false;
        return true;
    }

    public OpenPath getItem(int pos) {
        return list.get(pos);
    }

    public List<OpenPath> getAll() {
        return list;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View ret = convertView;
        if (ret == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.file_list_item, null);
        }
        int w = 32;
        // ret.setLayoutParams(new Gallery.LayoutParams(w, w));
        // double sz = (double)w * 0.7;

        final OpenPath file = (OpenPath)getItem(position);
        if (file == null)
            return ret;

        TextView text = (TextView)ret.findViewById(R.id.content_text);
        Context c = parent.getContext();
        Drawable d = ThumbnailCreator.getDefaultDrawable(file, w, w, c);
        boolean isCut = file.getTag() != null && file.getTag() instanceof Integer
                && ((Integer)file.getTag()).equals(R.id.menu_context_cut);
        if (isCut)
            d = new LayerDrawable(new Drawable[] {
                    d, c.getResources().getDrawable(R.drawable.ic_menu_cut)
            });
        float alpha = 1f;
        int checkbox = android.R.drawable.checkbox_on_background;
        if (!isPastable(file))
            alpha = 0.5f;

        ret.setPadding(ret.getPaddingLeft(), ret.getPaddingTop(), 0, ret.getPaddingBottom());

        if (text != null) {
            RemoteImageView image = (RemoteImageView)ret.findViewById(R.id.content_icon);
            TextView info = (TextView)ret.findViewById(R.id.content_info);
            ImageView check = (ImageView)ret.findViewById(R.id.content_check);
            if (check != null) {
                check.setImageResource(checkbox);
                check.setVisibility(View.VISIBLE);
                check.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mOnClickListener != null)
                            mOnClickListener.onClick(v);
                        else {
                            remove(file);
                            onClipboardUpdate();
                        }
                    }
                });
            }

            // info.setVisibility(View.GONE);
            if (file.isDirectory())
                info.setText(file.getListLength() + " "
                        + info.getContext().getResources().getString(R.string.s_files));
            else
                info.setText(DialogHandler.formatSize(file.length()));
            if (file != null) {
                if (file.getName() != null)
                    text.setText(file.getName());
                if (file.getPath() != null)
                    info.setText(info.getText() + " : " + file.getParent().getPath());
                image.setImageDrawable(d);
                ViewUtils.setAlpha(alpha, image, info, text);
                // ThumbnailCreator.setThumbnail(image, file, w, w); //(int)(w *
                // (3f/4f)), (int)(w * (3f/4f)));
            }
        } else if (ret instanceof TextView) {
            text = (TextView)ret;
            text.setText(file.getName());
            ViewUtils.setAlpha(text, alpha);
            text.setCompoundDrawables(d, null,
                    parent.getContext().getResources().getDrawable(checkbox), null);
        }

        return ret;
    }

    public Iterator<OpenPath> iterator() {
        return list.iterator();
    }

    public View addPath(OpenPath path) {
        add(path);
        int pos = indexOf(path);
        return getView(pos, null, null);
    }

    public boolean add(OpenPath path) {
        boolean ret = true;
        if (path.getTag() == null || !(path.getTag() instanceof Integer))
            path.setTag((Integer)(DeleteSource ? R.id.menu_context_cut : R.id.menu_context_copy));
        if (list.contains(path))
            ret = false;
        else
            ret = list.add(path);
        onClipboardUpdate();
        return ret;
    }

    public void add(int index, OpenPath path) {
        if (path == null)
            return;
        if (list.contains(path))
            return;
        if (path.getTag() == null || !(path.getTag() instanceof Integer))
            path.setTag((Integer)(DeleteSource ? R.id.menu_context_cut : R.id.menu_context_copy));
        if (!list.contains(path))
            list.add(index, path);
        onClipboardUpdate();
    }

    public boolean addAll(Collection<? extends OpenPath> collection) {
        list.removeAll(collection); // remove and re-add
        boolean ret = list.addAll(collection);
        list.remove(null);
        onClipboardUpdate();
        return ret;
    }

    public boolean addAll(int index, Collection<? extends OpenPath> collection) {
        list.removeAll(collection); // remove and re-add
        boolean ret = list.addAll(index, collection);
        list.remove(null);
        onClipboardUpdate();
        return ret;
    }

    public void clear() {
        list.clear();
        onClipboardUpdate();
    }

    public boolean contains(Object path) {
        return list.contains(path);
    }

    public boolean containsAll(Collection<?> paths) {
        return list.containsAll(paths);
    }

    public OpenPath get(int location) {
        return list.get(location);
    }

    public int indexOf(Object path) {
        return list.indexOf(path);
    }

    public int lastIndexOf(Object path) {
        return list.lastIndexOf(path);
    }

    public ListIterator<OpenPath> listIterator() {
        return list.listIterator();
    }

    public ListIterator<OpenPath> listIterator(int location) {
        return list.listIterator(location);
    }

    public OpenPath remove(int location) {
        OpenPath ret = list.remove(location);
        onClipboardUpdate();
        return ret;
    }

    public boolean remove(Object path) {
        boolean ret = list.remove(path);
        onClipboardUpdate();
        return ret;
    }

    public boolean removeAll(Collection<?> paths) {
        boolean ret = list.removeAll(paths);
        onClipboardUpdate();
        return ret;
    }

    public boolean retainAll(Collection<?> paths) {
        return list.retainAll(paths);
    }

    public OpenPath set(int index, OpenPath path) {
        OpenPath ret = list.set(index, path);
        onClipboardUpdate();
        return ret;
    }

    public int size() {
        return list.size();
    }

    public List<OpenPath> subList(int start, int end) {
        return list.subList(start, end);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return list.toArray(array);
    }

    public void setOnClickListener(OnClickListener listen) {
        mOnClickListener = listen;
    }

    public long getTotalSize() {
        long ret = 0;
        for(OpenPath p : list)
            if(p.length() > 0)
                ret += p.length();
        return ret;
    }

}

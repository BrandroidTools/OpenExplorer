package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenSearch;
import org.brandroid.openmanager.data.OpenSmartFolder;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.openmanager.util.ThumbnailCreator.OnUpdateImageListener;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.ViewUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * Main Adapter used in OpenExplorer. Adapts Grid & List View.
 */
public class ContentAdapter extends BaseAdapter {
	private final int KB = 1024;
	private final int MG = KB * KB;
	private final int GB = MG * KB;
	
	private final OpenPath mParent;
	private final ArrayList<OpenPath> mData2 = new ArrayList();
	public int mViewMode = OpenExplorer.VIEW_LIST;
	public boolean mShowThumbnails = true;
	private SortType mSorting = SortType.ALPHA;
	private CheckClipboardListener mClipper;
	private Context mContext;
	private boolean mPlusParent = false;
	private boolean mShowDetails = true;
	private boolean mShowFiles = true;
	
	public ContentAdapter(Context context, int mode, OpenPath parent) {
		//super(context, layout, data);
		mContext = context;
		mParent = parent;
		
		if(Preferences.Pref_ShowUp && mParent.getParent() != null)
			mPlusParent = true;
		mViewMode = mode;
	}
	
	public interface CheckClipboardListener
	{
		public boolean checkClipboard(OpenPath file);
		public boolean isMultiselect();
		public void removeFromClipboard(OpenPath file);
	}
	public void setCheckClipboardListener(CheckClipboardListener l) { mClipper = l; }
	public void setShowPlusParent(boolean showUp) { mPlusParent = showUp; }
	public void setShowDetails(boolean showDeets) { mShowDetails = showDeets; }
	public void setShowFiles(boolean showFiles) { mShowFiles = showFiles; }
	
	public Context getContext() { return mContext; }
	public Resources getResources() { if(mContext != null) return mContext.getResources(); else return null; }
	public int getViewMode() { return mViewMode; }
	public void setViewMode(int mode) { mViewMode = mode; notifyDataSetChanged(); }
	
	public void setSorting(SortType sort) { mSorting = sort; notifyDataSetChanged(); }
	public SortType getSorting() { return mSorting; }
	
	public void updateData() { updateData(getList()); } 
	public void updateData(final OpenPath[] items) { updateData(items, true); }
	private void updateData(final OpenPath[] items,
			final boolean doSort) {
		if(items == null) return;
		
		//new Thread(new Runnable(){public void run() {
		boolean showHidden = getSorting().showHidden();
		boolean foldersFirst = getSorting().foldersFirst();
		Logger.LogVerbose("updateData on " + items.length + " items (for " + mParent + ") : " +
				(showHidden ? "show" : "hide") + " + " + (foldersFirst ? "folders" : "files") + " + " + (doSort ? mSorting.toString() : "no sort"));
		
		mData2.clear();
		
		if(items != null)
		for(OpenPath f : items)
		{
			if(f == null) continue;
			if(!showHidden && f.isHidden()) continue;
			if(!f.requiresThread())
			{
				if(!f.exists()) continue;
				if(f.isFile() && !(f.length() >= 0)) continue;
			}
			if(!mShowFiles && !f.isDirectory())
				continue;
			mData2.add(f);
		}

		if(doSort)
			sort();
		
		notifyDataSetChanged();
	}
	
	public void sort() { sort(mSorting); }
	public void sort(SortType sort)
	{
		OpenPath.Sorting = sort;
		Collections.sort(mData2);
	}
	
	private OpenPath[] getList() {
		try {
			if(mParent.requiresThread() && Thread.currentThread().equals(OpenExplorer.UiThread))
				return null;
			else
				return mParent.list();
		} catch (IOException e) {
			Logger.LogError("Couldn't getList in ContentAdapter");
			return null;
		}
	}
	
	////@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		int mode = getViewMode() == OpenExplorer.VIEW_GRID ?
				R.layout.grid_content_layout : R.layout.list_content_layout;
		final boolean useLarge = mode == R.layout.grid_content_layout;
		
		if(view == null
					//|| view.getTag() == null
					//|| !BookmarkHolder.class.equals(view.getTag())
					//|| ((BookmarkHolder)view.getTag()).getMode() != mode
					)
		{
			LayoutInflater in = (LayoutInflater)getContext()
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view = in.inflate(mode, parent, false);
			//mHolder = new BookmarkHolder(file, mName, view, mode);
			//view.setTag(mHolder);
			//file.setTag(mHolder);
		} //else mHolder = (BookmarkHolder)view.getTag();
		
		final OpenPath file = getItem(position); //super.getItem(position);
		
		if(file == null) return view;
		
		Object o = file.getTag();
		if(o != null && o instanceof OpenPath && ((OpenPath)o).equals(file))
			return view;
		
		TextView mInfo = (TextView)view.findViewById(R.id.content_info);
		TextView mPathView = (TextView)view.findViewById(R.id.content_fullpath); 
		TextView mNameView = (TextView)view.findViewById(R.id.content_text);
		final ImageView mIcon = (ImageView)view.findViewById(R.id.content_icon);
		
		if(mPlusParent && position == 0)
		{
			mNameView.setText(mContext.getString(R.string.s_menu_up));
			mIcon.setImageResource(useLarge ? R.drawable.lg_folder_up : R.drawable.sm_folder_up);
			if(mInfo != null) mInfo.setText("");
			if(mPathView != null) mPathView.setText("");
			return view;
		}
		final String mName = file.getName();
		
		int mWidth = getResources().getInteger(getViewMode() == OpenExplorer.VIEW_GRID ?
				R.integer.content_grid_image_size :
				R.integer.content_list_image_size);
		int mHeight = mWidth;
		
		boolean showLongDate = false;
		if(getResources().getBoolean(R.bool.show_long_date))
			showLongDate = true;
		
		//mHolder.getIconView().measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//Logger.LogVerbose("Content Icon Size: " + mHolder.getIconView().getMeasuredWidth() + "x" + mHolder.getIconView().getMeasuredHeight());

		//view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//mHolder.setInfo(getFileDetails(file, false));
		
		if(mPathView != null)
		{
			if(mShowDetails && mParent.showChildPath())
			{
				String s = file.getPath().replace(file.getName(), "");
				mPathView.setVisibility(View.VISIBLE);
				mPathView.setText(s);
				showLongDate = false;
			}
			else mPathView.setVisibility(View.GONE);
				//mHolder.showPath(false);
		}

		if(mInfo != null)
		{
			if(mShowDetails)
				mInfo.setText(String.format(
						file.getDetails(getSorting().showHidden(), showLongDate),
						getResources().getString(R.string.s_files)));
			else mInfo.setText("");
		}

		if(mNameView != null)
			mNameView.setText(mName);

		boolean multi = mClipper != null && mClipper.isMultiselect();
		boolean copied = mClipper != null && mClipper.checkClipboard(file);
		int pad = view.getPaddingLeft();
		if(multi || copied)
			pad = 0;
		view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), pad, view.getPaddingBottom());
		ImageView mCheck = (ImageView)view.findViewById(R.id.content_check);
		if(mCheck != null)
		{
			if(mCheck.getVisibility() != (multi || copied ? View.VISIBLE : View.GONE))
				mCheck.setVisibility(multi || copied ? View.VISIBLE : View.GONE);
			if(copied)
				mCheck.setImageResource(android.R.drawable.checkbox_on_background);
		}
		if(copied)
		{
			mNameView.setTextAppearance(getContext(), R.style.Highlight);
			ViewUtils.setOnClicks(view, new View.OnClickListener() {
					public void onClick(View v) {
						mClipper.removeFromClipboard(file);
					}
				}, R.id.content_check);
		} else {
			mNameView.setTextAppearance(getContext(),  R.style.Large);
			if(!copied && multi)
				ViewUtils.setImageResource(view,
						android.R.drawable.checkbox_off_background,
						R.id.content_check);
		}
		
		if(file.isHidden())
			ViewUtils.setAlpha(0.5f, mNameView, mPathView, mInfo);
		else
			ViewUtils.setAlpha(1.0f, mNameView, mPathView, mInfo);
						
		//if(!mHolder.getTitle().equals(mName))
		//	mHolder.setTitle(mName);
		//RemoteImageView mIcon = (RemoteImageView)view.findViewById(R.id.content_icon);
		
		if(mIcon != null)
		{
			//mIcon.invalidate();
			if(file.isHidden())
				mIcon.setAlpha(100);
			else
				mIcon.setAlpha(255);
			if(file.isTextFile())
				mIcon.setImageBitmap(ThumbnailCreator.getFileExtIcon(file.getExtension(), getContext(), mWidth > 72));
			else if(!mShowThumbnails||!file.hasThumbnail())
			{
				mIcon.setImageResource(ThumbnailCreator.getDefaultResourceId(file, mWidth, mHeight));
			} else { //if(!ThumbnailCreator.getImagePath(mIcon).equals(file.getPath())) {
				//Logger.LogDebug("Bitmapping " + file.getPath());
				//if(OpenExplorer.BEFORE_HONEYCOMB) mIcon.setAlpha(0);
				ThumbnailCreator.setThumbnail(mIcon, file, mWidth, mHeight,
					new OnUpdateImageListener() {
						public void updateImage(final Bitmap b) {
							//if(!ThumbnailCreator.getImagePath(mIcon).equals(file.getPath()))
							{
								Runnable doit = new Runnable(){public void run(){
									if(!OpenExplorer.BEFORE_HONEYCOMB)
									{
										BitmapDrawable d = new BitmapDrawable(getResources(), b);
										d.setGravity(Gravity.CENTER);
										ImageUtils.fadeToDrawable(mIcon, d);
									} else {
										mIcon.setImageBitmap(b);
										//mIcon.setAlpha(255);
									}
									mIcon.setTag(file.getPath());
								}};
								if(!Thread.currentThread().equals(OpenExplorer.UiThread))
									mIcon.post(doit);
								else doit.run();
							}
						}
					});
			}
		}
		
		view.setTag(file);
		
		return view;
	}

	@Override
	public int getCount() {
		return mData2.size() + (mPlusParent ? 1 : 0);
	}

	@Override
	public OpenPath getItem(int position) {
		if(mPlusParent && position == 0)
			return mParent.getParent();
		int pos = position - (mPlusParent ? 1 : 0);
		if(pos < 0 || pos >= mData2.size()) return null;
		return mData2.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void add(OpenPath p) {
		mData2.add(p);
	}

	public boolean contains(OpenPath f) {
		return mData2.contains(f);
	}

	public ArrayList<OpenPath> getAll() {
		return mData2;
	}
	
}
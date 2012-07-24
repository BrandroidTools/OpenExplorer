package org.brandroid.openmanager.fragments;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import org.brandroid.carousel.CarouselViewHelper;
import org.brandroid.carousel.CarouselView;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CarouselFragment extends OpenFragment implements OpenPathFragmentInterface {
	private static final String TAG = "CarouselTestActivity";
	private static final int CARD_SLOTS = 56;
	private static final int SLOTS_VISIBLE = 7;

	protected static final boolean DBG = false;
	private static final int TEXTURE_HEIGHT = 256;
	private static final int TEXTURE_WIDTH = 256;
	private static final int DETAIL_TEXTURE_WIDTH = 200;
	private static final int DETAIL_TEXTURE_HEIGHT = 80;
	private static final int VISIBLE_DETAIL_COUNT = 3;
	private static boolean INCREMENTAL_ADD = false; // To debug incrementally adding cards
	private CarouselView mView;
	private Paint mPaint = new Paint();
	private Paint mBlackPaint = new Paint();
	
	private boolean mShowHiddenFiles = false;
	private boolean mFoldersFirst = true;
	private SortType mSorting = SortType.ALPHA;
	
	private CarouselViewHelper mHelper;
	private Bitmap mGlossyOverlay;
	private Bitmap mBorder;
	
	private OpenPath mPath;
	private OpenPath[] mPathItems;
	
	public int size() {
		if(mPathItems != null)
			return mPathItems.length;
		else return 0;
	}
	
	class LocalCarouselViewHelper extends CarouselViewHelper {
		private static final int PIXEL_BORDER = 3;
		private DetailTextureParameters mDetailTextureParameters
				= new DetailTextureParameters(5.0f, 5.0f, 3.0f, 10.0f);

		LocalCarouselViewHelper(Context context) {
			super(context);
		}

		@Override
		public void onCardSelected(final int id) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mPathItems[id].isDirectory())
						setPath(mPathItems[id]);
					else
						IntentManager.startIntent(mPathItems[id], getActivity());
				}});
			//postMessage("Selection", "Card " + id + " was selected");
		}

		@Override
		public void onDetailSelected(final int id, int x, int y) {
			onCardSelected(id);
		}

		@Override
		public void onCardLongPress(final int n, int touchPosition[], Rect detailCoordinates) {
			runOnUiThread(new Runnable(){public void run() {
			DialogHandler.showFileInfo(CarouselFragment.this, mPathItems[n]);
			}});
		}

		@Override
		public DetailTextureParameters getDetailTextureParameters(int id) {
			return mDetailTextureParameters;
		}

		@Override
		public Bitmap getTexture(int n) {
			final int textw = TEXTURE_WIDTH;
			final int texth = TEXTURE_HEIGHT;
			final int px = PIXEL_BORDER;

			Bitmap bitmap = Bitmap.createBitmap(textw, texth, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			
			int w = textw;
			int h = texth;
			
			SoftReference<Bitmap> thumb = null;
			
			final OpenPath mPath = mPathItems[n];
			mPaint.setColor(0x40808080);
			
			canvas.drawARGB(0, 0, 0, 0);
			mPaint.setColor(0xffffffff);
			mPaint.setAntiAlias(true);
			
			if(mPathItems != null && (thumb = ThumbnailCreator.generateThumb(CarouselFragment.this, mPath, textw, texth, getApplicationContext())) != null && thumb.get() != null)
			{
				Bitmap b = thumb.get();
				w = b.getWidth();
				h = b.getHeight();
				//canvas.drawRect(2, 2, w - 2, h - 2, mPaint);
				//canvas.drawRect(px, px, textw - px, texth, mBlackPaint);
				Matrix matrix = canvas.getMatrix();
				matrix.setRectToRect(
						new RectF(0, 0, w, h),
						new RectF(0, 0, w, h),
						ScaleToFit.START);
				canvas.drawBitmap(b, matrix, null);
			} else {
				canvas.drawRect(2, 2, w-2, h-2, mPaint);
				mPaint.setTextSize(100.0f);
				if(mPath == null)
					canvas.drawText("" + n, 2, h-10, mPaint);
				else
				{
					mPaint.setTextSize(30f);
					canvas.drawText(mPath.getName(), 2, h - 10, mPaint);
				}
				canvas.drawBitmap(mGlossyOverlay, null,
						new Rect(px, px, textw - px, texth - px), mPaint);
			}
			return bitmap;
		}

		@Override
		public Bitmap getDetailTexture(int n) {
			Bitmap bitmap = Bitmap.createBitmap(DETAIL_TEXTURE_WIDTH, DETAIL_TEXTURE_HEIGHT,
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawARGB(32, 10, 10, 10);
			mPaint.setTextSize(15.0f);
			mPaint.setAntiAlias(true);
			OpenPath mPath = mPathItems[n];
			if(mPath == null)
				canvas.drawText("Detail text for card " + n, 0, DETAIL_TEXTURE_HEIGHT/2, mPaint);
			else
			{
				Path p = new Path();
				RectF bounds = new RectF();
				String s = mPath.getName();
				int y = (int) mPaint.getTextSize() + 2;
				while(s != "")
				{
					mPaint.getTextPath(s, 0, s.length(), 0, 0, p);
					p.computeBounds(bounds, true);
					float lines = Math.max(1, bounds.right / DETAIL_TEXTURE_WIDTH);
					int chars = (int) (s.length() / lines); 
					canvas.drawText(s, 0, chars, 0, y, mPaint);
					if(chars >= s.length()) break;
					y += bounds.height() + 2;
					s = s.substring(chars).trim();
				}
			}
			return bitmap;
		}
	};
	
	private Runnable mAddCardRunnable = new Runnable() {
		public void run() {
			if (mView.getCardCount() < size()) {
				mView.createCards(mView.getCardCount() + 1);
				mView.postDelayed(mAddCardRunnable, 2000);
			}
		}
	};
	
	private void runOnUiThread(Runnable r) { getActivity().runOnUiThread(r); }

	void postMessage(final CharSequence title, final CharSequence msg) {
		runOnUiThread(new Runnable() {
			public void run() {
				new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setMessage(msg)
					.setPositiveButton("OK", null)
					.create()
					.show();
			}
		});
	}
	
	public void setPath(OpenPath path)
	{
		mPath = path;
		refreshData(null, false);
	}
	
	public CarouselFragment()
	{
		super();
		mPathItems = new OpenFile[0];
	}
	public CarouselFragment(OpenPath mParent)
	{
		super();
		if(OpenExplorer.BEFORE_HONEYCOMB)
		{
			Logger.LogError("Who is making me?", new Exception("WTF!"));
		}
		mPath = mParent;
		//getExplorer().updateTitle(mParent.getPath());
		refreshData(getArguments(), false);
	}
	
	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!isVisible() || isDetached()) return;
		super.onCreateOptionsMenu(menu, inflater);
		if(mPath == null || !mPath.isFile() || !IntentManager.isIntentAvailable(mPath, getExplorer()))
			MenuUtils.setMenuVisible(menu, false, R.id.menu_context_edit, R.id.menu_context_view);
		Logger.LogVerbose("ContentFragment.onCreateOptionsMenu");
		if(!menu.hasVisibleItems())
			inflater.inflate(R.menu.content_full, menu);
		MenuUtils.setMenuVisible(menu, OpenExplorer.IS_DEBUG_BUILD, R.id.menu_debug);
		if(!OpenExplorer.BEFORE_HONEYCOMB && OpenExplorer.USE_ACTION_BAR)
		{
			try {
			final SearchView mSearchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
			if(mSearchView != null)
				mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					@TargetApi(11)
					public boolean onQueryTextSubmit(String query) {
						mSearchView.clearFocus();
						Intent intent = getExplorer().getIntent();
						if(intent == null)
							intent = new Intent();
						intent.setAction(Intent.ACTION_SEARCH);
						Bundle appData = new Bundle();
						appData.putString("path", getExplorer().getDirContentFragment(false).getPath().getPath());
						intent.putExtra(SearchManager.APP_DATA, appData);
						intent.putExtra(SearchManager.QUERY, query);
						getExplorer().handleIntent(intent);
						return true;
					}
					public boolean onQueryTextChange(String newText) {
						return false;
					}
				});
			} catch(NullPointerException e) {
				Logger.LogError("Couldn't set up Search ActionView", e);
			}
		}
	}
	
	private SortType getSorting() { return mSorting; }
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		//Logger.LogVerbose("ContentFragment.onPrepareOptionsMenu");
		if(getActivity() == null) return;
		if(menu == null) return;
		if(!isAdded() || isDetached() || !isVisible()) return;
		super.onPrepareOptionsMenu(menu);
		if(OpenExplorer.BEFORE_HONEYCOMB)
			MenuUtils.setMenuVisible(menu, false, R.id.menu_view_carousel);
		
		switch(getSorting().getType())
		{
		case ALPHA:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_name_asc);
			break;
		case ALPHA_DESC:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_name_desc);
			break;
		case DATE:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_date_asc);
			break;
		case DATE_DESC:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_date_desc);
			break;
		case SIZE:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_size_asc);
			break;
		case SIZE_DESC:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_size_desc);
			break;
		case TYPE:
			MenuUtils.setMenuChecked(menu, true, R.id.menu_sort_type);
			break;
		}
		
		MenuUtils.setMenuChecked(menu, mFoldersFirst, R.id.menu_sort_folders_first);
		
		if(OpenExplorer.BEFORE_HONEYCOMB && menu.findItem(R.id.menu_multi) != null)
			menu.findItem(R.id.menu_multi).setIcon(null);
		
		//if(menu.findItem(R.id.menu_context_unzip) != null && getClipboard().getCount() == 0)
		//	menu.findItem(R.id.menu_context_unzip).setVisible(false);
		
		if(getClipboard() == null || getClipboard().size() == 0)
		{
			MenuUtils.setMenuVisible(menu, false, R.id.content_paste);
		} else {
			MenuItem mPaste = menu.findItem(R.id.content_paste);
			if(mPaste != null && getClipboard() != null && !isDetached())
				mPaste.setTitle(getString(R.string.s_menu_paste) + " (" + getClipboard().size() + ")");
			if(getActionMode() != null)
			{
				LayerDrawable d = (LayerDrawable) getResources().getDrawable(R.drawable.ic_menu_paste_multi);
				d.getDrawable(1).setAlpha(127);
				if(menu.findItem(R.id.content_paste) != null)
					menu.findItem(R.id.content_paste).setIcon(d);
			}
			if(mPaste != null)
				mPaste.setVisible(true);
		}
		
		MenuUtils.setMenuChecked(menu, true, R.id.menu_view_carousel, R.id.menu_view_grid, R.id.menu_view_list);
		
		MenuUtils.setMenuChecked(menu, getShowHiddenFiles(), R.id.menu_view_hidden);
		MenuUtils.setMenuVisible(menu, false, R.id.menu_view_thumbs);
		MenuUtils.setMenuVisible(menu, !OpenExplorer.BEFORE_HONEYCOMB, R.id.menu_view_carousel);
		
		//if(RootManager.Default.isRoot()) MenuUtils.setMenuChecked(menu, true, R.id.menu_root);	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item == null) return false;
		switch(item.getItemId())
		{
		case R.id.menu_sort_name_asc:	onSortingChanged(SortType.ALPHA); return true; 
		case R.id.menu_sort_name_desc:	onSortingChanged(SortType.ALPHA_DESC); return true; 
		case R.id.menu_sort_date_asc: 	onSortingChanged(SortType.DATE); return true;
		case R.id.menu_sort_date_desc: 	onSortingChanged(SortType.DATE_DESC); return true; 
		case R.id.menu_sort_size_asc: 	onSortingChanged(SortType.SIZE); return true; 
		case R.id.menu_sort_size_desc: 	onSortingChanged(SortType.SIZE_DESC); return true; 
		case R.id.menu_sort_type: 		onSortingChanged(SortType.TYPE); return true;
		case R.id.menu_view_hidden:
			onHiddenFilesChanged(!getShowHiddenFiles());
			return true;
		case R.id.menu_sort_folders_first:
			onFoldersFirstChanged(!getFoldersFirst());
			return true;
		}
		return false;
	}
	
	public boolean getFoldersFirst() { return mFoldersFirst; }
	public boolean getShowHiddenFiles() {
		return mShowHiddenFiles;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View mParent = inflater.inflate(R.layout.carousel_test, null);
		
		mView = (CarouselView)mParent.findViewById(R.id.carousel);
		if(!OpenExplorer.BEFORE_HONEYCOMB)
			mHelper = new LocalCarouselViewHelper(getActivity().getApplicationContext());
		
		return mParent;
	}
	
	@Override
	public void onViewCreated(View mParent, Bundle savedInstanceState) {
		super.onViewCreated(mParent, savedInstanceState);
		Logger.LogDebug("Carousel onViewCreated!");
		mView.getHolder().setFormat(PixelFormat.RGBA_8888);
		mPaint.setColor(0xffffffff);
		
		final Resources res = getResources();
		mHelper.setCarouselView(mView);
		mView.setSlotCount(CARD_SLOTS);
		mView.createCards(INCREMENTAL_ADD ? 1 : size());
		mView.setVisibleSlots(SLOTS_VISIBLE);
		mView.setStartAngle((float) -(2.0f * Math.PI * 5 / CARD_SLOTS));
		mBorder = BitmapFactory.decodeResource(res, R.drawable.border);
		mView.setDefaultBitmap(mBorder);
		mView.setLoadingBitmap(mBorder);
		//mView.setDetailTextureAlignment(CarouselView.DetailAlignment.CENTER_VERTICAL | CarouselView.DetailAlignment.LEFT);
		mView.setBackgroundColor(0.25f, 0.25f, 0.5f, 0.25f);
		//Theme t = getActivity().getTheme();
		//mView.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.screen_background_dark));
		mView.setRezInCardCount(3.0f);
		mView.setFadeInDuration(200);
		mView.setVisibleDetails(VISIBLE_DETAIL_COUNT);
		mView.setDragModel(CarouselView.DRAG_MODEL_PLANE);
		if (INCREMENTAL_ADD) {
			mView.postDelayed(mAddCardRunnable, 2000);
		}

		try {
			mGlossyOverlay = BitmapFactory.decodeResource(res, R.drawable.glossy_overlay);
		} catch(OutOfMemoryError e) {
			Logger.LogError("Out of memory!", e);
			mGlossyOverlay = null;
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		if(!OpenExplorer.BEFORE_HONEYCOMB)
			mHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(!OpenExplorer.BEFORE_HONEYCOMB)
			mHelper.onPause();
	}

	@Override
	public CharSequence getTitle() {
		return mPath.getName();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	}

	@Override
	public OpenPath getPath() {
		return mPath;
	}

	@Override
	public Drawable getIcon() {
		if(getActivity() != null)
			return getResources().getDrawable(ThumbnailCreator.getDefaultResourceId(getPath(), 32, 32));
		else return null;
	}
	
	public void refreshData(Bundle data, boolean allowSkips)
	{
		//ArrayList<OpenPath> mData = new ArrayList<OpenPath>();

		try {
			mPathItems = mPath.listFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Arrays.sort(mPathItems);
		
		//mPathItems = mData.toArray(new OpenPath[mData.size()]);
		//rerun();
	}

	public void onFoldersFirstChanged(boolean first)
	{
		setFoldersFirst(first);
		refreshData(null, false);
	}
	public void onHiddenFilesChanged()
	{
		onHiddenChanged(!getShowHiddenFiles());
	}
	//@Override
	public void onHiddenFilesChanged(boolean toShow)
	{
		Logger.LogInfo("onHiddenFilesChanged(" + toShow + ")");
		setShowHiddenFiles(toShow);
		//getManager().setShowHiddenFiles(state);
		refreshData(null, false);
	}
	//@Override
	public void onSortingChanged(SortType type) {
		setSorting(type);
		//getManager().setSorting(type);
		refreshData(null, false);
	}
	public void setFoldersFirst(boolean first) {
		mFoldersFirst = first;
		setViewSetting(mPath, "folders", first);
	}
	public void setShowHiddenFiles(boolean show)
	{
		mShowHiddenFiles = show;
		setViewSetting(mPath, "show", show);
	}
	
	public void setSorting(SortType type)
	{
		mSorting = type;
		setViewSetting(mPath, "sort", type.toString());
	}

	public static CarouselFragment getInstance(Bundle args) {
		CarouselFragment ret = new CarouselFragment();
		ret.setArguments(args);
		return ret;
	}
	
}

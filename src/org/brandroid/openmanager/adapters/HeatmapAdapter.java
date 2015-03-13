
package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HeatmapAdapter extends BaseAdapter {
    private long mTotalBytes = 0l, mLargest = 0l, mLastNotify = 0l;
    private final OpenPath mParent;
    private final OpenApp mApp;
    private final List<OpenPath> mPaths;
    private final HashMap<OpenPath, Long> mSizes = new HashMap<OpenPath, Long>();
    private final Hashtable<OpenPath, ScanSizeTask> mTasks = new Hashtable<OpenPath, HeatmapAdapter.ScanSizeTask>();
    private int mTaskCount = 0;
    private HeatmapCallback mCallback = null;

    public interface HeatmapCallback {
        public void OnHeatmapTasksComplete(long mTotalBytes, boolean allDone);
    }

    public void setHeatmapCallback(HeatmapCallback callback) {
        mCallback = callback;
    }

    public HeatmapAdapter(OpenApp app, OpenPath parent) {
        mApp = app;
        mParent = parent;
        mPaths = new ArrayList<OpenPath>();
        try {
            for(OpenPath p : mParent.listFiles())
            	mPaths.add(p);
        } catch (IOException e) {
            Logger.LogError("Couldn't list for Heatmap.", e);
        }
        Collections.sort(mPaths);
        for (OpenPath kid : mPaths) {
            if (kid.isDirectory()) {
                ScanSizeTask task = new ScanSizeTask(null, null);
                mTasks.put(kid, task);
            } else 
            	mSizes.put(kid, kid.length());
       }
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(mPaths, new Comparator<OpenPath>() {
            @Override
            public int compare(OpenPath lhs, OpenPath rhs) {
                Long sa = mSizes.get(lhs);
                Long sb = mSizes.get(rhs);
                if (sa != null && sb != null && !sa.equals(sb))
                    return sb.compareTo(sa);
                return rhs.getName().compareTo(lhs.getName());
            }
        });
        super.notifyDataSetChanged();
    }
    
    private void setBar(ProgressBar mBar, long size)
    {
        if (size > mLargest)
        {
            mLargest = size;
            long time = new Date().getTime();
            if(time - mLastNotify > 500)
            {
                mLastNotify = time;
                notifyDataSetChanged();
                return;
            }
        }
        if (mLargest > 0)
        {
            mBar.setIndeterminate(false);
            mBar.setMax((int)(mLargest / 1000));
            mBar.setProgress((int)(size / 1000));
            //Logger.LogDebug("Bar Progress: " + mBar.getProgress() + " / " + mBar.getMax());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (view == null)
            view = LayoutInflater.from(parent.getContext())
            			.inflate(R.layout.heatmap_row, parent, false);

        ImageView mIcon = (ImageView)view.findViewById(R.id.content_icon);
        final TextView mText = (TextView)view.findViewById(R.id.content_text);
        TextView mSize = (TextView)view.findViewById(R.id.content_count);
        ProgressBar mBar = (ProgressBar)view.findViewById(android.R.id.progress);

        final OpenPath path = getItem(position);
        mIcon.setImageBitmap(ThumbnailCreator
                .generateThumb(mApp, path, 32, 32, parent.getContext()).get());
        mText.setText(path.getName());
        mSize.setText(R.string.s_status_loading);

        if (mSizes.containsKey(path)) {
            long size = mSizes.get(path);
            mSize.setText("Size: " + OpenPath.formatSize(size));
            setBar(mBar, size);
        } else if (!path.isDirectory()) {
            long size = path.length();
            mSizes.put(path, size);
            mSize.setText("Size: " + OpenPath.formatSize(size));
            setBar(mBar, size);
        } else if (!mTasks.containsKey(path)) {
            mBar.setProgress(0);
            ScanSizeTask task = new ScanSizeTask(mSize, mBar);
            mTasks.put(path, task);
            task.execute(path);
        } else {
            ScanSizeTask task = mTasks.get(path);
            if(!task.hasViews())
            	task.setViews(mBar, mSize);
            if (task.getStatus() == Status.PENDING)
            	task.execute(path);
            if (task.getStatus() == Status.FINISHED) {
                long bytes = 0;
                try {
                    bytes = task.get();
                    mSizes.put(path, bytes);
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
                setBar(mBar, bytes);
                if(mSize != null)
                    mSize.setText("Size: " + OpenPath.formatSize(bytes));
            }
        }

        return view;
    }

    public class ScanSizeTask extends AsyncTask<OpenPath, Long, Long> {
        private ProgressBar mBar;
        private TextView mSizeText;

        public void setViews(ProgressBar bar, TextView txt) {
            mBar = bar;
            mSizeText = txt;
        }

        public ScanSizeTask(TextView sizeText, ProgressBar bar) {
            mSizeText = sizeText;
            mBar = bar;
        }
        
        public boolean hasViews() { return mBar != null && mSizeText != null; }

        @Override
        protected Long doInBackground(OpenPath... params) {
            OpenPath path = params[0];
            Long size = ScanDir(params[0], 0);
            mSizes.put(path, size);
            return size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mBar != null) {
                mBar.setIndeterminate(false);
                mBar.setProgress(0);
            }
            mTaskCount++;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            String descr = values[0].toString();
            if(mSizeText != null && mSizeText.getText().equals(descr))
            	mSizeText.setText(descr);
            if(mBar != null && values.length > 0) {
                int val = (int)((long)values[0]);
                if(mBar.getProgress() != val)
                    mBar.setProgress(val);
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            mLargest = Math.max(mLargest, result);
            if (mCallback != null)
                mCallback.OnHeatmapTasksComplete(mTotalBytes, false);
            if (mSizeText != null)
                mSizeText.setText("Size: " + OpenPath.formatSize(result));
            if (mBar != null) {
                mBar.setMax((int)mLargest);
                mBar.setProgress((int)((long)result));
            }
            notifyDataSetChanged();
            if (--mTaskCount <= 0) {
                if (mCallback != null)
                    mCallback.OnHeatmapTasksComplete(mTotalBytes, true);
                
            }
        }

        private Long ScanDir(OpenPath path, int depth) {
            Long mTotal = 0l;
            if (path.isDirectory()) {
                OpenPath[] kids = null;
                try {
                    kids = path.listFiles();
                } catch (IOException e) {
                }
                if (kids != null)
                    for (OpenPath kid : kids)
                    {
                        mTotal += ScanDir(kid, depth + 1);
                        if(depth <= 1)
                        	publishProgress(mTotal);
                    }
            } else {
                long bytes = path.length();
                mTotalBytes += bytes;
                mTotal += bytes;
            }
            return mTotal;
        }
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public OpenPath getItem(int position) {
        return mPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

	public void cancelTasks() {
		Enumeration<ScanSizeTask> tasks = mTasks.elements();
		while(tasks.hasMoreElements())
		{
			ScanSizeTask task = tasks.nextElement();
			task.cancel(true);
		}
	}

}

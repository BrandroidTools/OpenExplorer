
package org.brandroid.openmanager.views;

import java.util.Date;

import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class SpriteAnimatorSurfaceView extends View implements Runnable {
    Thread thread = null;
    private SurfaceHolder sh;
    volatile boolean running = false;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Point[] mStarts = null;
    private Bitmap[] mIcons = null;
    private Point mDest = null;
    private final DecelerateInterpolator interp;
    private PointF posF = new PointF();
    private long mStamp = 0;
    private int mAnimTime = 200;

    public SpriteAnimatorSurfaceView(Context context) {
        super(context);
        interp = new DecelerateInterpolator();
    }

    public void setSprites(ImageView[] views) {
        int i = 0;
        mIcons = new Bitmap[views.length];
        mStarts = new Point[views.length];
        for (ImageView v : views) {
            Rect r = new Rect();
            v.getGlobalVisibleRect(r);
            Point start = new Point(r.left, r.top);
            mStarts[i] = start;
            mIcons[i] = ((BitmapDrawable)v.getDrawable()).getBitmap();
        }
    }

    public void setDestination(Point pt) {
        mDest = pt;
    }

    public void setAnimationTime(int time) {
        mAnimTime = time;
    }

    public void onPauseSurfaceView() {
        boolean retry = true;
        running = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        onPauseSurfaceView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long diff = new Date().getTime() - mStamp;
        float pos = diff > 0 ? mAnimTime / diff : 0;
        for (int i = 0; i < mStarts.length; i++) {
            Point start = mStarts[i];
            Bitmap icon = mIcons[i];

            if (start == null || icon == null)
                continue;

            posF.x = start.x + ((mDest.x - start.x) * pos);
            posF.y = start.y + ((mDest.y - start.y) * pos);

            Logger.LogDebug("Animate #" + i + ": (" + posF.x + "," + posF.y + ")");

            canvas.drawBitmap(icon, posF.x, posF.y, paint);
        }
    }

    @Override
    public void run() {
        mStamp = new Date().getTime();
        if (mDest == null || mIcons == null)
            return;
        while (running) {
            postInvalidate();
        }
    }

}


package org.brandroid.openmanager.views;

import java.util.Date;

import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class IconAnimationPanel extends SurfaceView implements SurfaceHolder.Callback {
    private Point start, end;
    private Bitmap icon;
    private long timer = 0l;
    private int duration = 500;
    private AnimThread thread;

    public IconAnimationPanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new AnimThread(getHolder(), this);
    }

    public IconAnimationPanel setStart(Point pt) {
        start = pt;
        return this;
    }

    public IconAnimationPanel setEnd(Point pt) {
        end = pt;
        return this;
    }

    public IconAnimationPanel setIcon(Bitmap bmp) {
        icon = bmp;
        return this;
    }

    public IconAnimationPanel setDuration(int ms) {
        duration = ms;
        return this;
    }

    public void start() {
        timer = new Date().getTime();
        thread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (timer == 0)
            return;
        Point pt = new Point(
                (int)(((end.x - start.x) / duration) * (new Date().getTime() - timer)),
                (int)(((end.y - start.y) / duration) * (new Date().getTime() - timer)));
        Logger.LogVerbose("Drawing icon @ (" + pt.x + "," + pt.y + ")");
        canvas.drawBitmap(icon, pt.x, pt.y, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    class AnimThread extends Thread {
        private SurfaceHolder surface;
        private IconAnimationPanel panel;
        private boolean running = false;

        public AnimThread(SurfaceHolder holder, IconAnimationPanel panel) {
            surface = holder;
            this.panel = panel;
        }

        @Override
        public synchronized void start() {
            running = true;
            super.start();
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void run() {
            Canvas c;
            while (running) {
                c = null;
                try {
                    c = surface.lockCanvas();
                    synchronized (surface) {
                        panel.onDraw(c);
                    }
                } finally {
                    if (c != null)
                        surface.unlockCanvasAndPost(c);
                }
            }
        }
    }

}

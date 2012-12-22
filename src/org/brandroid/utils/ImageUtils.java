
package org.brandroid.utils;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.views.RemoteImageView;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.ImageView;

public class ImageUtils {

    public static void fadeToDrawable(final RemoteImageView mImage, final Drawable dest) {
        TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                mImage.getDrawable(), dest
        });
        td.setCrossFadeEnabled(true);
        mImage.setImageDrawable(td);
        td.startTransition(100);
    }

    public static void fadeToDrawable(ImageView mImage, Drawable dest) {
        fadeToDrawable(mImage, dest, 100);
    }

    public static void fadeToDrawable(final ImageView mImage, final Drawable dest, final int speed) {
        /*
         * if(!OpenExplorer.BEFORE_HONEYCOMB) { Drawable od =
         * mImage.getDrawable(); dest.setAlpha(0); LayerDrawable ld = new
         * LayerDrawable(new Drawable[]{od,dest}); mImage.setImageDrawable(ld);
         * ObjectAnimator.ofFloat(od, "alpha", 1.0f,
         * 0.0f).setDuration(100).start(); ObjectAnimator.ofFloat(dest, "alpha",
         * 0.0f, 1.0f).setDuration(100).start(); } else {
         */
        if (mImage == null)
            return;
        Drawable src = mImage.getDrawable();
        if (src == null) {
            mImage.post(new Runnable() {
                public void run() {
                    mImage.setImageDrawable(dest);
                }
            });
            return;
        } else if (dest == null)
            return;
        if (OpenExplorer.BEFORE_HONEYCOMB)
            mImage.post(new Runnable() {
                public void run() {
                    mImage.setImageDrawable(dest);
                }
            });
        else {
            final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                    src, dest
            });
            td.setCrossFadeEnabled(true);
            mImage.post(new Runnable() {
                public void run() {
                    mImage.setImageDrawable(td);
                }
            });
            td.startTransition(speed);
        }
        if (dest instanceof AnimationDrawable)
            ((AnimationDrawable)dest).start();
        // }
    }

    public static void fadeToDrawable(final View mView, final Drawable dest) {
        if (mView instanceof ImageView)
            fadeToDrawable((ImageView)mView, dest);
        else if (mView instanceof RemoteImageView)
            fadeToDrawable((RemoteImageView)mView, dest);
        else {
            TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                    mView.getBackground(), dest
            });
            td.setCrossFadeEnabled(true);
            mView.setBackgroundDrawable(td);
            td.startTransition(100);
        }
    }

}

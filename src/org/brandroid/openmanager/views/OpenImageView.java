
package org.brandroid.openmanager.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;

public class OpenImageView extends ImageView {
    public OpenImageView(Context context) {
        super(context);
    }

    public void fadeToDrawable(Drawable newImage) {
        Drawable old = super.getDrawable();
        newImage.setAlpha(0);
        LayerDrawable ld = new LayerDrawable(new Drawable[] {
                old, newImage
        });
        super.setImageDrawable(ld);
        ld.setCallback(new Callback() {

            @Override
            public void unscheduleDrawable(Drawable who, Runnable what) {
            }

            @Override
            public void scheduleDrawable(Drawable who, Runnable what, long when) {

            }

            @Override
            public void invalidateDrawable(Drawable who) {
            }
        });
        ld.scheduleDrawable(ld, new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

            }
        }, 100);
    }
}

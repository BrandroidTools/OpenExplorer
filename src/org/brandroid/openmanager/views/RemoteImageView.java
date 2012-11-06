/*  RemoteImageView.java
 *
 *  Created on May 15, 2011 by William Edward Woody
 */

package org.brandroid.openmanager.views;

import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.Cache;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RemoteImageView extends ImageView {
    public RemoteImageView(Context context) {
        super(context);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private OurCallback fCallback;

    private static class OurCallback implements Cache.Callback {
        private RemoteImageView pThis;

        OurCallback(RemoteImageView r) {
            pThis = r;
        }

        public void onImageLoaded(OpenPath url, Bitmap bitmap) {
            if (pThis != null) {
                Drawable newPic = new BitmapDrawable(bitmap);
                ImageUtils.fadeToDrawable(pThis, newPic);
                pThis.invalidate();
                pThis.fCallback = null; // our callback ended; remove reference
            } // else Logger.LogWarning("Nothing to load into!");
        }

        public void onFailure(OpenPath path, Exception th) {
            // Logger.LogError("pewp!", th);
            // Ignoring for now. Could display broken link image
            if (pThis != null) {
                pThis.fCallback = null; // our callback ended; remove reference
            }
        }

        @Override
        public OpenApp getApp() {
            return ((OpenApp)pThis.getContext());
        }
    }

    public void setImageFromFile(OpenPath path, int mWidth, int mHeight) {
        Logger.LogDebug("RemoteImageView.setImageFromFile(" + path.getPath() + ", " + mWidth + ", "
                + mHeight);
        fCallback = new OurCallback(this);
        Cache.get().getImage(path, mWidth, mHeight, fCallback).start();
    }

}

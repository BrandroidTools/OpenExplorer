
package org.brandroid.openmanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

public class Cache {
    /**
     * Our callback interface
     */
    public interface Callback {
        void onImageLoaded(OpenPath path, Bitmap bitmap);

        void onFailure(OpenPath path, Exception th);

        OpenApp getApp();
    }

    /**
     * Item in the queue which is waiting to be processed by our network
     * thread(s)
     */
    private static class QueueItem {
        OpenPath path;
        Callback callback;
        int Width, Height;

        QueueItem(OpenPath u, int w, int h, Callback c) {
            path = u;
            callback = c;
            Width = w;
            Height = h;
        }
    }

    // / The handler to thread to the UI thread
    private Handler fHandler;
    // / The event queue
    private LinkedList<QueueItem> fQueue;
    // / The global cache object, which will be created by the class loader on
    // load.
    // / Because this is normally called from our UI objects, this means our
    // Handler
    // / will be created on our UI thread
    public static Cache gCache = new Cache();

    /**
     * Internal runnable for our background loader thread
     */
    private class NetworkThread implements Runnable {
        public void run() {

            // for (;;) {
            /*
             * Dequeue next request
             */
            QueueItem q;

            synchronized (fQueue) {
                while (fQueue.isEmpty()) {
                    try {
                        fQueue.wait();
                    } catch (InterruptedException e) {
                    }
                    break;
                }

                /*
                 * Get the next item
                 */
                q = fQueue.removeLast();
            }
            final QueueItem qq = q;

            if (q.path.exists()) {
                Logger.LogDebug("***Found image to load!");
                SoftReference<Bitmap> sr = ThumbnailCreator.generateThumb(qq.callback.getApp(),
                        qq.path, qq.Width, qq.Height, qq.callback.getApp().getContext());
                final Bitmap bmp = sr != null ? sr.get() : null;
                if (bmp != null) {
                    fHandler.post(new Runnable() {
                        public void run() {
                            Logger.LogDebug("***calling onImageLoaded!");
                            qq.callback.onImageLoaded(qq.path, bmp);
                        }
                    });
                } else
                    Logger.LogWarning("***Image was null! " + qq.path);
                return;
            } else
                Logger.LogWarning("***Couldn't find image!");

            /*
             * Read the network
             */

            try {

                /*
                 * Set up the request and get the response
                 */
                // Start HTTP Client
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet get = new HttpGet(q.path.getPath());
                HttpResponse response = httpClient.execute(get);
                HttpEntity entity = response.getEntity();

                /*
                 * Get the bitmap from the URL response
                 */
                InputStream is = entity.getContent();
                final Bitmap bmap = BitmapFactory.decodeStream(is);
                is.close();

                entity.consumeContent();

                /*
                 * Send notification indicating we loaded the image on the main
                 * UI thread
                 */
                fHandler.post(new Runnable() {
                    public void run() {
                        qq.callback.onImageLoaded(qq.path, bmap);
                    }
                });
            } catch (final Exception ex) {
                fHandler.post(new Runnable() {
                    public void run() {
                        qq.callback.onFailure(qq.path, ex);
                    }
                });
            }
            // }

            // httpClient.getConnectionManager().shutdown();
        }
    }

    /**
     * Start up this object
     */
    private Cache() {
        fHandler = new Handler();
        fQueue = new LinkedList<QueueItem>();

        // mThread.start();
    }

    public void start() {
        Thread mThread = new Thread(new NetworkThread());
        mThread.setDaemon(true);
        try {
            mThread.start();
        } catch (IllegalThreadStateException e) {
        }
    }

    /**
     * Get the singleton cache object
     */
    public static Cache get() {
        return gCache;
    }

    /**
     * Get the image from the remote service. This will call the callback once
     * the image has been loaded
     * 
     * @param path
     * @param callback
     */
    public Cache getImage(OpenPath path, int w, int h, Callback callback) {
        synchronized (fQueue) {
            fQueue.addFirst(new QueueItem(path, w, h, callback));
            fQueue.notify();
        }
        return this;
    }

    /**
     * Remove from the queue all requests with the specified callback. Done when
     * the result is no longer needed because the view is going away.
     * 
     * @param callback
     */
    public void removeCallback(Callback callback) {
        synchronized (fQueue) {
            Iterator<QueueItem> iter = fQueue.iterator();
            while (iter.hasNext()) {
                QueueItem i = (QueueItem)iter.next();
                if (i.callback == callback) {
                    iter.remove();
                }
            }
        }
    }
}

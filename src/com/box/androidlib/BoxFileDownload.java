/*******************************************************************************
 * Copyright 2011 Box.net.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;


/**
 * Contains logic for downloading a user's file from Box API and reporting errors that may have occurred. You should not call this directly, and instead use
 * {@link com.box.androidlib.Box#download(String, long, File, Long, FileDownloadListener)} or
 * {@link com.box.androidlib.BoxSynchronous#download(String, long, File, Long, FileDownloadListener)} to download.
 * 
 * @author developers@box.net
 */
public class BoxFileDownload {

    /**
     * auth token from Box.
     */
    private final String mAuthToken;
    /**
     * FileDownloadListener that can notify you of download progress.
     */
    private FileDownloadListener mListener;
    /**
     * Handler to execute onProgress callbacks.
     */
    private Handler mHandler;
    /**
     * Runnable for FileDownloadListener.onProgress.
     */
    private Runnable mOnProgressRunnable;
    /**
     * Used to track how many bytes have been transferred so far.
     */
    private long mBytesTransferred;

    /**
     * size of buffer used when reading from download input stream.
     */
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    /**
     * if downloaded file is less than this size (in bytes), then inspect it for an error message from Box API.
     */
    private static final int FILE_ERROR_SIZE = 100;

    /**
     * The minimum time in milliseconds that must pass between each call to FileDownloadListener.onProgress. This is to avoid excessive calls which may lock up
     * the device.
     */
    private static final int ON_PROGRESS_UPDATE_THRESHOLD = 300;

    /**
     * Instantiate a new BoxFileDownload.
     * 
     * @param authToken
     *            Auth token from Box
     */
    public BoxFileDownload(final String authToken) {
        mAuthToken = authToken;
    }

    /**
     * Set a download listener which allows you to monitor download progress and see the response status.
     * 
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @param handler
     *            The handler through which FileDownloadListener.onProgress will be invoked.
     */
    public void setListener(final FileDownloadListener listener, final Handler handler) {
        mListener = listener;
        mHandler = handler;
        mOnProgressRunnable = new Runnable() {

            @Override
            public void run() {
                mListener.onProgress(mBytesTransferred);
            }
        };
    }

    /**
     * Execute a file download.
     * 
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationOutputStreams
     *            OutputStreams to which the data should be written to as it is downloaded.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @return a response handler
     * @throws IOException
     *             Can be thrown if there was a connection error, or if destination file could not be written.
     */
    public DefaultResponseParser execute(final long fileId, final OutputStream[] destinationOutputStreams, final Long versionId) throws IOException {

        final DefaultResponseParser handler = new DefaultResponseParser();

        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(BoxConfig.getInstance().getDownloadUrlScheme());
        builder.encodedAuthority(BoxConfig.getInstance().getDownloadUrlAuthority());
        builder.path(BoxConfig.getInstance().getDownloadUrlPath());
        builder.appendPath(mAuthToken);
        builder.appendPath(String.valueOf(fileId));
        if (versionId != null) {
            builder.appendPath(String.valueOf(versionId));
        }

        List<BasicNameValuePair> customQueryParams = BoxConfig.getInstance().getCustomQueryParameters();
        if (customQueryParams != null && customQueryParams.size() > 0) {
            for (BasicNameValuePair param : customQueryParams) {
                builder.appendQueryParameter(param.getName(), param.getValue());
            }
        }

        // We normally prefer to use HttpUrlConnection, however that appears to fail for certain types of files. For downloads, it appears DefaultHttpClient
        // works more reliably.
        final DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(httpclient.getParams(), BoxConfig.getInstance().getUserAgent());
        HttpGet httpGet;

        String theUri = builder.build().toString();
        if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
            //DevUtils.logcat("User-Agent : " + HttpProtocolParams.getUserAgent(httpclient.getParams()));
            DevUtils.logcat("Box Request: " + theUri);
        }
        try {
            httpGet = new HttpGet(new URI(theUri));
        }
        catch (URISyntaxException e) {
            throw new IOException("Invalid Download URL");
        }
        httpGet.setHeader("Connection", "close");
        httpGet.setHeader("Accept-Language", BoxConfig.getInstance().getAcceptLanguage());
        HttpResponse httpResponse = httpclient.execute(httpGet);

        int responseCode = httpResponse.getStatusLine().getStatusCode();

        if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
            DevUtils.logcat("Box Response: " + responseCode);
//            Header[] headers = httpResponse.getAllHeaders();
//            for (Header header : headers) {
//                DevUtils.logcat("Response Header: " + header.toString());
//            }
        }

        // Server returned a 503 Service Unavailable. Usually means a temporary unavailability.
        if (responseCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
//            if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
//                DevUtils.logcat("HTTP Response Code: " + HttpStatus.SC_SERVICE_UNAVAILABLE);
//            }
            handler.setStatus(ResponseListener.STATUS_SERVICE_UNAVAILABLE);
            return handler;
        }

        InputStream is = httpResponse.getEntity().getContent();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Grab the first 100 bytes and check for an error string.
            // Not a good way for the server to respond with errors but that's the way it is for now.
            final byte[] errorCheckBuffer = new byte[FILE_ERROR_SIZE];
            // Three cases here: error, no error && file size == 0, no error && file size > 0.
            // The first case will be handled by parsing the error check buffer. The second case will result in mBytesTransferred == 0,
            // no real bytes will be transferred but we still treat as success case. The third case is normal success case.
            mBytesTransferred = Math.max(0, is.read(errorCheckBuffer));
            final String str = new String(errorCheckBuffer).trim();
            if (str.equals(FileDownloadListener.STATUS_DOWNLOAD_WRONG_AUTH_TOKEN)) {
                handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_WRONG_AUTH_TOKEN);
            }
            else if (str.equals(FileDownloadListener.STATUS_DOWNLOAD_RESTRICTED)) {
                handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_RESTRICTED);
            }
            // No error detected
            else {
                // Copy the file to destination if > 0 bytes of file transferred.
                if (mBytesTransferred > 0) {
                    for (int i = 0; i < destinationOutputStreams.length; i++) {
                        destinationOutputStreams[i].write(errorCheckBuffer, 0, (int) mBytesTransferred); // Make sure we don't lose that first 100 bytes.
                    }

                    // Read the rest of the stream and write to the destination OutputStream.
                    final byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                    int bufferLength = 0;
                    long lastOnProgressPost = 0;
                    while (!Thread.currentThread().isInterrupted() && (bufferLength = is.read(buffer)) > 0) {
                        for (int i = 0; i < destinationOutputStreams.length; i++) {
                            destinationOutputStreams[i].write(buffer, 0, bufferLength);
                        }
                        mBytesTransferred += bufferLength;
                        long currTime = SystemClock.uptimeMillis();
                        if (mListener != null && mHandler != null && currTime - lastOnProgressPost > ON_PROGRESS_UPDATE_THRESHOLD) {
                            lastOnProgressPost = currTime;
                            mHandler.post(mOnProgressRunnable);
                        }
                    }
                    mHandler.post(mOnProgressRunnable);
                }
                for (int i = 0; i < destinationOutputStreams.length; i++) {
                    destinationOutputStreams[i].close();
                }
                handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_OK);

                // If download thread was interrupted, set to
                // STATUS_DOWNLOAD_CANCELED
                if (Thread.currentThread().isInterrupted()) {
                    httpGet.abort();
                    handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_CANCELLED);
                }
            }
        }
        else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_PERMISSIONS_ERROR);
        }
        else {
            handler.setStatus(FileDownloadListener.STATUS_DOWNLOAD_FAIL);
        }
        httpResponse.getEntity().consumeContent();
        if (httpclient != null && httpclient.getConnectionManager() != null) {
            httpclient.getConnectionManager().closeIdleConnections(500, TimeUnit.MILLISECONDS);
        }
        is.close();

        return handler;
    }
}

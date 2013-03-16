/*******************************************************************************
 * Copyright 2011 Box.net.
 * 
 * Licensed import com.box.androidlib.Utils.DevUtils; under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;


/**
 * Contains logic for uploading to Box and reporting errors that may have occurred. You should not call this directly, and instead use
 * {@link com.box.androidlib.Box#upload(String, String, String, long, FileUploadListener)} or
 * {@link com.box.androidlib.BoxSynchronous#upload(String, String, File, String, long, FileUploadListener)} to download.
 * 
 * @author developers@box.net
 */
public class BoxFileUpload {

    /**
     * auth token.
     */
    private final String mAuthToken;
    /**
     * response listener.
     */
    private FileUploadListener mListener;
    /**
     * Handler to execute onProgress callbacks.
     */
    private Handler mHandler;

    /**
     * The minimum time in milliseconds that must pass between each firing of progress listener. This is to avoid excessive calls which may lock up the device.
     */
    private static final int ON_PROGRESS_UPDATE_THRESHOLD = 300;

    /**
     * Instantiate a new BoxFileUpload.
     * 
     * @param authToken
     *            auth token
     */
    public BoxFileUpload(final String authToken) {
        mAuthToken = authToken;
    }

    /**
     * Set an upload listener which allows you to monitor download progress and see the response status.
     * 
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(BoxFile, String)}
     * @param handler
     *            The handler through which FileUploadListener.onProgress will be invoked.
     */
    public void setListener(final FileUploadListener listener, final Handler handler) {
        mListener = listener;
        mHandler = handler;
    }

    /**
     * Execute a file upload.
     * 
     * @param action
     *            Set to {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD} or {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}
     * @param sourceInputStream
     *            Input stream targeting the data for the file you wish to create/upload to Box.
     * @param filename
     *            The desired filename on Box after upload (just the file name, do not include the path)
     * @param destinationId
     *            If action is {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that
     *            is being overwritten, or copied.
     * @return A FileResponseParser with information about the upload.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @throws FileNotFoundException
     *             File being uploaded either doesn't exist, is not a file, or cannot be read
     * @throws MalformedURLException
     *             Make sure you have specified a valid upload action
     */
    public FileResponseParser execute(final String action, final InputStream sourceInputStream, final String filename, final long destinationId)
        throws IOException, MalformedURLException, FileNotFoundException {

        if (!action.equals(Box.UPLOAD_ACTION_UPLOAD) && !action.equals(Box.UPLOAD_ACTION_OVERWRITE) && !action.equals(Box.UPLOAD_ACTION_NEW_COPY)) {
            throw new MalformedURLException("action must be upload, overwrite or new_copy");
        }

        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(BoxConfig.getInstance().getUploadUrlScheme());
        builder.encodedAuthority(BoxConfig.getInstance().getUploadUrlAuthority());
        builder.path(BoxConfig.getInstance().getUploadUrlPath());
        builder.appendPath(action);
        builder.appendPath(mAuthToken);
        builder.appendPath(String.valueOf(destinationId));
        if (action.equals(Box.UPLOAD_ACTION_OVERWRITE)) {
            builder.appendQueryParameter("file_name", filename);
        }
        else if (action.equals(Box.UPLOAD_ACTION_NEW_COPY)) {
            builder.appendQueryParameter("new_file_name", filename);
        }

        List<BasicNameValuePair> customQueryParams = BoxConfig.getInstance().getCustomQueryParameters();
        if (customQueryParams != null && customQueryParams.size() > 0) {
            for (BasicNameValuePair param : customQueryParams) {
                builder.appendQueryParameter(param.getName(), param.getValue());
            }
        }

        if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
            DevUtils.logcat("Uploading : " + filename + "  Action= " + action + " DestinionID + " + destinationId);
            DevUtils.logcat("Upload URL : " + builder.build().toString());
        }
        // Set up post body
        final HttpPost post = new HttpPost(builder.build().toString());
        final MultipartEntityWithProgressListener reqEntity = new MultipartEntityWithProgressListener(HttpMultipartMode.BROWSER_COMPATIBLE, null,
            Charset.forName(HTTP.UTF_8));

        if (mListener != null && mHandler != null) {
            reqEntity.setProgressListener(new MultipartEntityWithProgressListener.ProgressListener() {

                @Override
                public void onTransferred(final long bytesTransferredCumulative) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mListener.onProgress(bytesTransferredCumulative);
                        }
                    });
                }
            });
        }

        reqEntity.addPart("file_name", new InputStreamBody(sourceInputStream, filename) {

            @Override
            public String getFilename() {
                return filename;
            }
        });
        post.setEntity(reqEntity);

        // Send request
        final HttpResponse httpResponse;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(httpClient.getParams(), BoxConfig.getInstance().getUserAgent());
        post.setHeader("Accept-Language", BoxConfig.getInstance().getAcceptLanguage());
        try {
            httpResponse = httpClient.execute(post);
        }
        catch (final IOException e) {
            // Detect if the download was cancelled through thread interrupt. See CountingOutputStream.write() for when this exception is thrown.
            if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
                DevUtils.logcat("IOException Uploading " + filename + " Exception Message: " + e.getMessage() + e.toString());
                DevUtils.logcat(" Exception : " + e.toString());
                e.printStackTrace();
                DevUtils.logcat("Upload URL : " + builder.build().toString());
            }
            if ((e.getMessage() != null && e.getMessage().equals(FileUploadListener.STATUS_CANCELLED)) || Thread.currentThread().isInterrupted()) {
                final FileResponseParser handler = new FileResponseParser();
                handler.setStatus(FileUploadListener.STATUS_CANCELLED);
                return handler;
            }
            else {
                throw e;
            }
        }
        finally {
            if (httpClient != null && httpClient.getConnectionManager() != null) {
                httpClient.getConnectionManager().closeIdleConnections(500, TimeUnit.MILLISECONDS);
            }
        }
        if (BoxConfig.getInstance().getHttpLoggingEnabled()) {
            DevUtils.logcat("HTTP Response Code: " + httpResponse.getStatusLine().getStatusCode());
            Header[] headers = httpResponse.getAllHeaders();
            DevUtils.logcat("User-Agent : " + HttpProtocolParams.getUserAgent(httpClient.getParams()));
            for (Header header : headers) {
                DevUtils.logcat("Response Header: " + header.toString());
            }
        }

        // Server returned a 503 Service Unavailable. Usually means a temporary unavailability.
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            final FileResponseParser handler = new FileResponseParser();
            handler.setStatus(ResponseListener.STATUS_SERVICE_UNAVAILABLE);
            return handler;
        }

        String status = null;
        BoxFile boxFile = null;
        final InputStream is = httpResponse.getEntity().getContent();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        httpResponse.getEntity().consumeContent();
        final String xml = sb.toString();

        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
            final Node statusNode = doc.getElementsByTagName("status").item(0);
            if (statusNode != null) {
                status = statusNode.getFirstChild().getNodeValue();
            }
            final Element fileEl = (Element) doc.getElementsByTagName("file").item(0);
            if (fileEl != null) {
                try {
                    boxFile = Box.getBoxFileClass().newInstance();
                }
                catch (InstantiationException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < fileEl.getAttributes().getLength(); i++) {
                    boxFile.parseAttribute(fileEl.getAttributes().item(i).getNodeName(), fileEl.getAttributes().item(i).getNodeValue());
                }
            }

            // errors are NOT returned as properly formatted XML yet so in this case the raw response is the error status code see
            // http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download
            if (status == null) {
                status = xml;
            }
        }
        catch (final SAXException e) {
            // errors are NOT returned as properly formatted XML yet so in this case the raw response is the error status code see
            // http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download
            status = xml;
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        final FileResponseParser handler = new FileResponseParser();
        handler.setFile(boxFile);
        handler.setStatus(status);
        return handler;
    }

    /**
     * Extension of MultiPartEntity which adds the ability to monitor file upload progress. Inspiration for code from: http://stackoverflow.com/questions
     * /3213899/cant-grab-progress-on-http-post -file-upload-android/3246050#3246050
     */
    private static class MultipartEntityWithProgressListener extends MultipartEntity {

        /**
         * progress listener.
         */
        private ProgressListener mListener;

        /**
         * Instance of CountingOutputStream.
         */
        private CountingOutputStream mCountingOutputStream;

        /**
         * base constructor.
         * 
         * @param mode
         *            mode
         * @param boundary
         *            boundary
         * @param charset
         *            charset
         */
        public MultipartEntityWithProgressListener(final HttpMultipartMode mode, final String boundary, final Charset charset) {
            super(mode, boundary, charset);
        }

        @Override
        protected String generateContentType(final String boundary, final Charset charset) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("multipart/form-data; boundary=");
            buffer.append(boundary);
            if (charset != null) {
                // Box upload servers appear to fail if the charset is specified. So this method is almost identical to the parent's version, except that the 2
                // lines below are commented out.
                // buffer.append("; charset=");
                // buffer.append(charset.name());
            }
            return buffer.toString();
        }

        /**
         * Set a ProgressListener that will fire events when bytes have been written to the outputstream. Subscribe to ProgressListener.transferred()
         * 
         * @param progressListener
         *            progress listener.
         */
        public void setProgressListener(final ProgressListener progressListener) {
            mListener = progressListener;
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException {
            if (mCountingOutputStream == null) {
                mCountingOutputStream = new CountingOutputStream(outstream, mListener);
            }
            super.writeTo(mCountingOutputStream);
            mListener.onTransferred(mCountingOutputStream.getBytesTransferred());
        }

        /**
         * Interface definition for a callback to be invoked during data transfer.
         */
        public interface ProgressListener {

            /**
             * Called periodically during data transfer to report the number of bytes that have been transferred.
             * 
             * @param bytesTransferredCumulative
             *            The number of bytes (cumulative) that have been transfered
             */
            void onTransferred(long bytesTransferredCumulative);
        }

        /**
         * FilterOutputStream that fires progress callbacks so we can monitor upload progress.
         */
        private static class CountingOutputStream extends FilterOutputStream {

            /**
             * progress listener.
             */
            private final ProgressListener mProgresslistener;
            /**
             * number of bytes transferred so far.
             */
            private long bytesBransferred;
            /**
             * last timestamp of progress listener being fired.
             */
            private long lastOnProgressPost = 0;

            /**
             * constructor that also takes a progress listener.
             * 
             * @param out
             *            output stream
             * @param progressListener
             *            progress listener
             */
            public CountingOutputStream(final OutputStream out, final ProgressListener progressListener) {
                super(out);
                mProgresslistener = progressListener;
                bytesBransferred = 0;
            }

            @Override
            public void write(final byte[] buffer, final int offset, final int length) throws IOException {
                out.write(buffer, offset, length);
                bytesBransferred += length;
                long currTime = SystemClock.uptimeMillis();
                if (mProgresslistener != null && currTime - lastOnProgressPost > ON_PROGRESS_UPDATE_THRESHOLD) {
                    lastOnProgressPost = currTime;
                    mProgresslistener.onTransferred(bytesBransferred);
                }
                // Allow canceling of downloads through thread interrupt.
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException(FileUploadListener.STATUS_CANCELLED);
                }
            }

            /**
             * Get the number of bytes transferred so far.
             * 
             * @return Number of bytes transferred so far.
             */
            public long getBytesTransferred() {
                return bytesBransferred;
            }
        }
    }
}

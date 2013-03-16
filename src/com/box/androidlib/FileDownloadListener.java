/*******************************************************************************
 * Copyright 2011 Box.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

/**
 * Interface definition for a callback to be invoked when Box.download() is
 * called.
 */
public interface FileDownloadListener extends ResponseListener {
    /**
     * download status to indicate successful download with no errors.
     */
    String STATUS_DOWNLOAD_OK = "download_ok";
    /**
     * download status to indicate that the API key was invalid.
     */
    String STATUS_DOWNLOAD_RESTRICTED = "restricted";
    /**
     * download status to indicate that the download failed for unknown reason.
     */
    String STATUS_DOWNLOAD_FAIL = "download_fail";
    /**
     * download status to indicate that the download was canceled.
     */
    String STATUS_DOWNLOAD_CANCELLED = "download_cancelled";    
    /**
     * download status to indicate that the download failed because the auth
     * token was invalid.
     */
    String STATUS_DOWNLOAD_WRONG_AUTH_TOKEN = "wrong auth token";
    /**
     * download status to indicate that the user did not have permissions to
     * download the file.
     */
    String STATUS_DOWNLOAD_PERMISSIONS_ERROR = "permissions_error";

    /**
     * Called when the file has been downloaded. Note: even if a file is
     * downloaded, you must still check if the file itself contains an error
     * code instead of the expected file contents. Refer to
     * http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download
     * for details.
     * 
     * @param status
     *            This will be 'download_ok' if the file has been download.
     */
    void onComplete(String status);

    /**
     * Called periodically during the download. You can use this to monitor
     * download progress.
     * 
     * @param bytesDownloaded
     *            The number of bytes that have been downloaded so far. Note
     *            that this will eventually slightly exceed the size of the file
     *            being download because of overhead of HTTP requests (e.g. HTTP
     *            headers)
     */
    void onProgress(long bytesDownloaded);

}

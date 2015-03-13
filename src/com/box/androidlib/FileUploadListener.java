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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;


/**
 * Interface definition for a callback to be invoked when Box.upload() is
 * called.
 */
public interface FileUploadListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_UPLOAD_OK = "upload_ok";
    /**
     * If operation was not successful. The user is not logged into your
     * application. Your authentication_token is not valid.
     */
    String STATUS_WRONG_AUTH_TOKEN = "wrong auth token";
    /**
     * If operation was not successful. Some of the files were not successfully
     * uploaded.
     */
    String STATUS_UPLOAD_SOME_FILES_FAILED = "upload_some_files_failed";
    /**
     * If operation was not successful. There is not enough space in this user's
     * account to accommodate the new files.
     */
    String STATUS_NOT_ENOUGH_FREE_SPACE = "not_enough_free_space";
    /**
     * If operation was not successful. A file is too large to be uploaded to a
     * user's account (Lite users have a 25 MB upload limit per file, premium
     * users have a 1 GB limit per file).
     */
    String STATUS_FILESIZE_LIMIT_EXCEEDED = "filesize_limit_exceeded";
    /**
     * If operation was not successful. The user does not have uploading
     * privileges for that particular folder.
     */
    String STATUS_ACCESS_DENIED = "access_denied";
    /** If operation was not successful. The specified folder_id is not valid. */
    String STATUS_UPLOAD_WRONG_FOLDER_ID = "upload_wrong_folder_id";
    /**
     * If operation was not successful. The name of the file contains invalid
     * characters not accepted by Box.net.
     */
    String STATUS_INVALID_FILE_NAME = "upload_invalid_file_name";
    /**
     * If the upload was canceled.
     */
    String STATUS_CANCELLED = "upload_cancelled";
    
    /**
     * Called periodically during upload so you can monitor upload progress.
     * 
     * @param bytesTransferredCumulative
     *            The number of bytes transferred so far. Note that this will
     *            eventually slightly exceed the size of the file being uploaded
     *            because of overhead of HTTP requests (e.g. HTTP headers)
     */
    void onProgress(long bytesTransferredCumulative);

    /**
     * Called when the file has been uploaded.
     * 
     * @param boxFile
     *            The BoxFile that has been uploaded, or null if there was an
     *            error
     * @param status
     *            See
     *            http://developers.box.net/w/page/12923951/ApiFunction_Upload
     *            -and-Download for the possible status codes that may be
     *            returned
     */
    void onComplete(BoxFile boxFile, String status);

    /**
     * Called if a FileNotFoundException was thrown.
     * 
     * @param e
     *            The FileNotFoundException that was thrown
     */
    void onFileNotFoundException(FileNotFoundException e);

    /**
     * Called if the upload url is invalid, usually because an invalid action
     * was specified.
     * 
     * @param e
     *            The MalformedURLException that was thrown
     */
    void onMalformedURLException(MalformedURLException e);
}

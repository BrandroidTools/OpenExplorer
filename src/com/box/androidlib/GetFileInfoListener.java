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
 * Interface definition for a callback to be invoked when Box.getFileInfo() is
 * called.
 */
public interface GetFileInfoListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_S_GET_FILE_INFO = "s_get_file_info";
    /**
     * If operation was not successful. The file_id is either invalid, or not
     * accessible by that user.
     */
    String STATUS_E_ACCESS_DENIED = "e_access_denied";

    /**
     * Called when the API request has completed.
     *
     * @param boxFile
     *            A BoxFile, or null if there was an error
     * @param status
     *            Status code from Box API
     */
    void onComplete(BoxFile boxFile, String status);
}

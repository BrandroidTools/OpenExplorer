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
 * Interface definition for a callback to be invoked when
 * Box.toggleFolderEmail() is called.
 */
public interface ToggleFolderEmailListener extends ResponseListener {
    
    /** Operation was successful. */
    String STATUS_S_TOGGLE_FOLDER_EMAIL = "s_toggle_folder_email";
    
    /** Operation was unsuccessful. */
    String STATUS_E_TOGGLE_FOLDER_EMAIL = "e_toggle_folder_email";
    
    /**
     * Called when the API request has completed.
     * 
     * @param folderEmail
     *            The email address to use for file uploads
     * @param status
     *            Status code from Box API
     */
    void onComplete(String folderEmail, String status);
}

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
 * Interface definition for a callback to be invoked when Box.copy() is called.
 */
public interface CopyListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_S_COPY_NODE = "s_copy_node";
    /** If operation was not successful. */
    String STATUS_E_COPY_NODE = "e_copy_node";
    /**
     * If operation was not successful. The file cannot be copied because a file
     * of the same name already exists in that folder.
     */
    String STATUS_FILENAME_IN_USE = "e_filename_in_use";

    /**
     * Called when the API request has completed.
     * 
     * @param status
     *            Status code from Box API
     */
    void onComplete(String status);
}

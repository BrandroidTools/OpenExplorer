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
 * Interface definition for a callback to be invoked when Box.addToMyBox() is called.
 */
public interface AddToMyBoxListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_ADDTOMYBOX_OK = "e_copy_node";
    /** If operation was not successful. */
    String STATUS_ADDTOMYBOX_ERROR = "addtomybox_error";
    /** If operation was not successful. */
    String STATUS_S_LINK_EXISTS = "s_link_exists";

    /**
     * Called when the API request has completed.
     * 
     * @param status
     *            Status code from Box API
     */
    void onComplete(String status);
}

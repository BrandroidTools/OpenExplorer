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
 * Interface definition for a callback to be invoked when Box.publicUnshare() is
 * called.
 */
public interface PublicUnshareListener extends ResponseListener {
    
    /** Operation was successful. */
    String STATUS_UNSHARE_OK = "unshare_ok";
    
    /** The item_id may be invalid. Verify that the item_id is a valid id for an item in the user's account. */
    String STATUS_WRONG_NODE = "wrong_node";
    
    /** For all other errors. Verify that your target is 'file' or 'folder', and that the target_id is valid. */
    String STATUS_UNSHARE_ERROR = "unshare_error";    
    
    /**
     * Called when the API request has completed.
     * 
     * @param status
     *            Status code from Box API
     */
    void onComplete(String status);
}

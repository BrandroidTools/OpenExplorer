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

import java.util.ArrayList;


/**
 * Interface definition for a callback to be invoked when Box.getComments() is
 * called.
 */
public interface GetCommentsListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_GET_COMMENTS_OK = "get_comments_ok";
    /**
     * If operation was not successful. Another error occured in your call. You
     * may want to verify that you provided a valid item type and id.
     */
    String STATUS_GET_COMMENTS_ERROR = "get_comments_error";

    /**
     * Called when the API request has completed.
     * 
     * @param comments
     *            A list of comments, or null if there was an error
     * @param status
     *            Status code from Box API
     */
    void onComplete(ArrayList<Comment> comments, String status);
}

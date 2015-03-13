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
 * Interface definition for a callback to be invoked when Box.addComment() is
 * called.
 */
public interface AddCommentListener extends ResponseListener {

    /** Comment successfully added. */
    String STATUS_ADD_COMMENT_OK = "add_comment_ok";
    /**
     * If the result was not successful. Another error occurred in your call.
     * You may want to verify that you provided a valid item type and id.
     */
    String STATUS_ADD_COMMENT_ERROR = "add_comment_error";

    /**
     * Called when the API request has completed.
     *
     * @param comment
     *            The comment that was added, or null if there was an error
     * @param status
     *            Status code from Box API
     */
    void onComplete(Comment comment, String status);
}

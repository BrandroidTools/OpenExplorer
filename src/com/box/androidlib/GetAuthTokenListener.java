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
 * Interface definition for a callback to be invoked when Box.getAuthToken() is
 * called.
 */
public interface GetAuthTokenListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_GET_AUTH_TOKEN_OK = "get_auth_token_ok";
    /** If operation was not successful. Generic error for other invalid inputs. */
    String STATUS_GET_AUTH_TOKEN_ERROR = "get_auth_token_error";

    /**
     * Called when the API request has completed.
     * 
     * @param user
     *            A BoxUser, or null if there was an error. Use
     *            BoxUser.getAuthToken() to get the user's auth token.
     * @param status
     *            Status code from Box API
     */
    void onComplete(User user, String status);
}

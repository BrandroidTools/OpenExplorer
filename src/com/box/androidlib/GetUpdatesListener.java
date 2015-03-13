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
 * Interface definition for a callback to be invoked when Box.getUpdates() is
 * called.
 */
public interface GetUpdatesListener extends ResponseListener {
    
    /** Operation was successful. */
    String STATUS_S_GET_UPDATES = "s_get_updates";
    /**
     * One or more of the timestamps is not a valid unix timestamp.
     */
    String STATUS_E_INVALID_TIMESTAMP = "e_invalid_timestamp";
    
    /**
     * Called when the API request has completed.
     * 
     * @param updates
     *            A list Update objects, or null if there was an error
     * @param status
     *            See http://developers.box.net/w/page/22926051/
     *            ApiFunction_get_updates for possible status codes that may be
     *            returned
     */
    void onComplete(ArrayList<Update> updates, String status);
}

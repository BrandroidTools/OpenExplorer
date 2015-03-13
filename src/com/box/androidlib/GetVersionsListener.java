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
 * Interface definition for a callback to be invoked when Box.getVersions() is
 * called.
 */
public interface GetVersionsListener extends ResponseListener {

    /** Operation was successful. */
    String STATUS_S_GET_VERSIONS = "s_get_versions";
    /**
     * Versions are not accessible on the item specified. It may be the case
     * that the user doesn't have rights to access past versions.
     */
    String STATUS_E_NO_ACCESS = "e_no_access";

    /**
     * Called when the API request has completed.
     * 
     * @param versions
     *            A list of Version objects, or null if there was an error
     * @param status
     *            Status code from Box API
     */
    void onComplete(ArrayList<Version> versions, String status);
}

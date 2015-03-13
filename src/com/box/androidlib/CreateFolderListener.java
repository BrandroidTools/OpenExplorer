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
 * Interface definition for a callback to be invoked when Box.createFolder() is
 * called.
 */
public interface CreateFolderListener extends ResponseListener {
    /** If operation was successful. */
    String STATUS_CREATE_OK = "create_ok";
    /**
     * If operation was not successful. The folder_id provided is not a valid
     * folder_id for the user's account.
     */
    String STATUS_NO_PARENT = "status_no_parent";
    /**
     * If operation was not successful. A folder with the same name already
     * exists in that location.
     */
    String STATUS_S_FOLDER_EXISTS = "s_folder_exists";
    /**
     * If operation was not successful. The name provided for the new folder
     * contained invalid characters or too many characters.
     */
    String STATUS_INVALID_FOLDER_NAME = "invalid_folder_name";
    /**
     * If operation was not successful. The user does not have the necessary
     * permissions to perform the specified operation. Most likely the user is
     * trying to create a folder within a collaborated folder, for which the
     * user has view-only permission.
     */
    String STATUS_E_NO_ACCESS = "e_no_access";
    /**
     * If operation was not successful. A folder name was not properly provided.
     */
    String STATUS_E_NO_FOLDER_NAME = "e_no_folder_name";
    /**
     * If operation was not successful. The folder name contained more than 100
     * characters, exceeding the folder name length limit.
     */
    String STATUS_FOLDER_NAME_TOO_BIG = "folder_name_too_big";
    /**
     * If operation was not successful. Another invalid input was provided
     * (example: an invalid value for the 'share' parameter).
     */
    String STATUS_E_INPUT_PARAMS = "e_input_params";

    /**
     * Called when the API request has completed.
     * 
     * @param boxFolder
     *            A BoxFolder representing the folder that was just created, or
     *            null if there was a problem.
     * @param status
     *            Status code from Box API
     */
    void onComplete(BoxFolder boxFolder, String status);
}

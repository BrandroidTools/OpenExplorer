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
 * Interface definition for a callback to be invoked when Box.inviteCollaborators() is
 * called.
 */
public interface InviteCollaboratorsListener extends ResponseListener {

    /** Operation was successful. */
    String STATUS_S_INVITE_COLLABORATORS = "s_invite_collaborators";
    
    /** Returned if the user is already a collaborator. */
    String STATUS_USER_ALREADY_COLLABORATOR = "user_already_collaborator";
    
    /** Returned if collaborators cannot be invited for the item. */
    String STATUS_CANNOT_INVITE_SUBUSERS = "cannot_invite_subusers";
    
    /** Returned if you try to invite yourself. */
    String STATUS_CANNOT_INVITE_SELF = "e_cannot_invite_self";
    
    /** Returned if the number of collaborators has exceeded the limit. */
    String STATUS_E_COLLABORATORS_LIMIT_REACHED = "e_collaborators_limit_reached";
    
    /** Returned if you the user did not have permissions to invite collaborators for the item. */
    String STATUS_E_INSUFFICIENT_PERMISSIONS = "e_insufficient_permissions";
    
    /**
     * Called when the API request has completed.
     * 
     * @param status
     *            Status code from Box API
     */
    void onComplete(String status);
}

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
 * Interface definition for a callback to be invoked when Box.registerNewUser()
 * is called.
 */
public interface RegisterNewUserListener extends ResponseListener {
    
    /** Operation was successful. */
    String STATUS_SUCCESSFUL_REGISTER = "successful_register";
    
    /** Not a valid email address. */
    String STATUS_EMAIL_INVALID = "email_invalid";
    
    /** E-mail already registered. */
    String STATUS_EMAIL_ALREADY_REGISTERED = "email_already_registered";
    
    /** Operation was not successful beacuase enterprise roll in required for this account. */
    String STATUS_E_REGISTER_ENTERPRISE_ROLL_IN_REQUIRED = "e_register_enterprise_roll_in_required";
        
    /** Operation was not successful since email confirmation is needed. */
    String STATUS_EMAIL_CONFIRMATION_NEEDED = "e_email_confirmation_needed";
    
    /** Operation was not successful for some other reason. */
    String STATUS_E_REGISTER = "e_register";
    
    /**
     * Called when the API request has completed.
     * 
     * @param user
     *            A BoxUser representing the user that was registered, or null
     *            if there was an error
     * @param status
     *            Status code from Box API
     */
    void onComplete(User user, String status);
}

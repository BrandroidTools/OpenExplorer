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
 * Interface definition for a callback to be invoked when Box.getTicket() is
 * called.
 */
public interface GetTicketListener extends ResponseListener {
    
    /**
     * Operation was successful.
     */
    String STATUS_GET_TICKET_OK = "get_ticket_ok";
    /**
     * Check that you specified a valid API key.
     */
    String STATUS_WRONG_INPUT = "wrong_input";
    
    /**
     * Called when the API request has completed.
     * 
     * @param ticket
     *            The ticket which you can use to load an authentication
     *            webpage. See
     *            http://developers.box.net/w/page/12923915/ApiAuthentication
     * @param status
     *            See http://developers.box.net/w/page/12923936/
     *            ApiFunction_get_ticket for possible status codes that may be
     *            returned
     */
    void onComplete(String ticket, String status);
}

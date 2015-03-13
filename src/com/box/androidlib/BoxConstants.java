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
 * Constants used in library.
 *
 * @author developers@box.net
 */
public final class BoxConstants {

    /**
     * No instantiation allowed.
     */
    private BoxConstants() {
    }

    /**
     * The URL of the login webpage. A ticket ( see
     * {@link com.box.androidlib.Box#getTicket(com.box.androidlib.ResponseListeners.GetTicketListener)}
     * ) should be appended to the URL.
     */
    public static final String LOGIN_URL = "https://app.box.com/api/1.0/auth/";

}

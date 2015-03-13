/*******************************************************************************
 * Copyright 2011 Box.net.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import android.net.Uri;

/**
 * Convenience methods for genering Box API urls.
 * 
 * @author developers@box.net
 */
public final class BoxUriBuilder {

    /**
     * No instantiation allowed.
     */
    private BoxUriBuilder() {
    }

    /**
     * Get a base URI builder with only the API scheme, authority and path.
     * 
     * @return Uri builder
     */
    public static Uri.Builder getBuilder() {
        return new Uri.Builder().scheme(BoxConfig.getInstance().getApiUrlScheme()).encodedAuthority(BoxConfig.getInstance().getApiUrlAuthority())
            .path(BoxConfig.getInstance().getApiUrlPath());
    }

    /**
     * Get a URI builder with an API key.
     * 
     * @param apiKey
     *            OpenBox app API key.
     * @return Uri builder
     */
    public static Uri.Builder getBuilder(final String apiKey) {
        return getBuilder().appendQueryParameter("api_key", apiKey);
    }

    /**
     * Get a URI builder with an API key and an auth token.
     * 
     * @param apiKey
     *            OpenBox app API key.
     * @param authToken
     *            Auth token
     * @return Uri Builder
     */
    public static Uri.Builder getBuilder(final String apiKey, final String authToken) {
        return getBuilder(apiKey).appendQueryParameter("auth_token", authToken);
    }

    /**
     * Get a URI builder with an API key, auth token and an API action.
     * 
     * @param apiKey
     *            OpenBox app API key.
     * @param authToken
     *            Auth token
     * @param action
     *            Box API action (e.g. get_account_tree)
     * @return Uri Builder
     */
    public static Uri.Builder getBuilder(final String apiKey, final String authToken, final String action) {
        return getBuilder(apiKey, authToken).appendQueryParameter("action", action);
    }
}

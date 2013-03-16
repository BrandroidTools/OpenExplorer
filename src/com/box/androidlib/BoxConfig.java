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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

/**
 * Class for retrieving configuration parameters.
 * 
 * @author developers@box.net
 * 
 */
public final class BoxConfig {

    /** Default API url scheme. */
    private static final String API_URL_SCHEME = "https";
    /** Default API url authority. */
    private static final String API_URL_AUTHORITY = "www.box.net";
    /** Default API url path. */
    private static final String API_URL_PATH = "/api/1.0/rest";

    /** Default Upload url scheme. */
    private static final String UPLOAD_URL_SCHEME = "https";
    /** Default Upload url authority. */
    private static final String UPLOAD_URL_AUTHORITY = "upload.box.net";
    /** Default Upload url path. */
    private static final String UPLOAD_URL_PATH = "/api/1.0/";

    /** Default Download url scheme. */
    private static final String DOWNLOAD_URL_SCHEME = "https";
    /** Default Download url authority. */
    private static final String DOWNLOAD_URL_AUTHORITY = "www.box.net";
    /** Default Download url path. */
    private static final String DOWNLOAD_URL_PATH = "/api/1.0/download/";
    /** Default User-Agent String. */
    private static final String USER_AGENT = "BoxAndroidLibrary";

    /** API url scheme. */
    private String mApiUrlScheme = API_URL_SCHEME;
    /** API url authority. */
    private String mApiUrlAuthority = API_URL_AUTHORITY;
    /** API url path. */
    private String mApiUrlPath = API_URL_PATH;

    /** Upload url scheme. */
    private String mUploadUrlScheme = UPLOAD_URL_SCHEME;
    /** Upload url authority. */
    private String mUploadUrlAuthority = UPLOAD_URL_AUTHORITY;
    /** Upload url path. */
    private String mUploadUrlPath = UPLOAD_URL_PATH;

    /** Download url path. */
    private String mDownloadUrlScheme = DOWNLOAD_URL_SCHEME;
    /** Download url authority. */
    private String mDownloadUrlAuthority = DOWNLOAD_URL_AUTHORITY;
    /** Download url path. */
    private String mDownloadUrlPath = DOWNLOAD_URL_PATH;
    /** User-Agent String to use. */
    private String mUserAgent = USER_AGENT;
    /** Enable Http Logging Flag NEVER ENABLE HTTP LOGGIN FOR PRODUCTION BUILDS. */
    private boolean mEnableHttpLogging = false;

    /** Time to wait before connection timeout. */
    private static int mConnectionTimout = 0;

    /** Custom query parameters to be added to all Box API requests. */
    private List<BasicNameValuePair> mCustomQueryParams = new ArrayList<BasicNameValuePair>();

    /** Singleton instance. */
    private static BoxConfig mInstance;

    /**
     * Private constructor.
     */
    private BoxConfig() {
    }

    /**
     * Get a singleton instance.
     * 
     * @return instance of BoxConfig.
     */
    public static BoxConfig getInstance() {
        if (mInstance == null) {
            mInstance = new BoxConfig();
        }
        return mInstance;
    }

    /**
     * Set a custom API URL scheme.
     * 
     * @param scheme
     *            Custom scheme
     */
    public void setApiUrlScheme(final String scheme) {
        mApiUrlScheme = scheme;
    }

    /**
     * Get the API URL scheme.
     * 
     * @return API URL scheme
     */
    public String getApiUrlScheme() {
        return mApiUrlScheme;
    }

    /**
     * Set a custom API URL Authority.
     * 
     * @param authority
     *            Custom Authority
     */
    public void setApiUrlAuthority(final String authority) {
        mApiUrlAuthority = authority;
    }

    /**
     * Get the API URL Authority.
     * 
     * @return API URL Authority
     */
    public String getApiUrlAuthority() {
        return mApiUrlAuthority;
    }

    /**
     * Set a custom API URL path.
     * 
     * @param path
     *            Custom path
     */
    public void setApiUrlPath(final String path) {
        mApiUrlPath = path;
    }

    /**
     * Get the API URL path.
     * 
     * @return API URL path
     */
    public String getApiUrlPath() {
        return mApiUrlPath;
    }

    /**
     * Set a custom Upload URL scheme.
     * 
     * @param scheme
     *            Custom scheme
     */
    public void setUploadUrlScheme(final String scheme) {
        mUploadUrlScheme = scheme;
    }

    /**
     * Get the Upload URL scheme.
     * 
     * @return Upload URL scheme
     */
    public String getUploadUrlScheme() {
        return mUploadUrlScheme;
    }

    /**
     * Set a custom Upload URL Authority.
     * 
     * @param authority
     *            Custom Authority
     */
    public void setUploadUrlAuthority(final String authority) {
        mUploadUrlAuthority = authority;
    }

    /**
     * Get the Upload URL Authority.
     * 
     * @return Upload URL Authority
     */
    public String getUploadUrlAuthority() {
        return mUploadUrlAuthority;
    }

    /**
     * Set a custom Upload URL path.
     * 
     * @param path
     *            Custom path
     */
    public void setUploadUrlPath(final String path) {
        mUploadUrlPath = path;
    }

    /**
     * Get the Upload URL path.
     * 
     * @return Upload URL path
     */
    public String getUploadUrlPath() {
        return mUploadUrlPath;
    }

    /**
     * Set a custom Download URL scheme.
     * 
     * @param scheme
     *            Custom scheme
     */
    public void setDownloadUrlScheme(final String scheme) {
        mDownloadUrlScheme = scheme;
    }

    /**
     * Get the Download URL scheme.
     * 
     * @return Download URL scheme
     */
    public String getDownloadUrlScheme() {
        return mDownloadUrlScheme;
    }

    /**
     * Set a custom Download URL Authority.
     * 
     * @param authority
     *            Custom Authority
     */
    public void setDownloadUrlAuthority(final String authority) {
        mDownloadUrlAuthority = authority;
    }

    /**
     * Get the Download URL Authority.
     * 
     * @return Download URL Authority
     */
    public String getDownloadUrlAuthority() {
        return mDownloadUrlAuthority;
    }

    /**
     * Set a custom Download URL path.
     * 
     * @param path
     *            Custom path
     */
    public void setDownloadUrlPath(final String path) {
        mDownloadUrlPath = path;
    }

    /**
     * Get the Download URL path.
     * 
     * @return Download URL path
     */
    public String getDownloadUrlPath() {
        return mDownloadUrlPath;
    }

    /**
     * Set the amount of time in milliseconds that calls to the server should wait before timing out. Default is 0 which stands for infinite timeout.
     * 
     * @param timeout
     *            Desired connection timeout.
     */
    public void setConnectionTimeOut(final int timeout) {
        mConnectionTimout = timeout;
    }

    /**
     * Get the amount of time in milliseconds that calls to the server should wait before timing out.
     * 
     * @return The current connection timeout set.
     */
    public int getConnectionTimeOut() {
        return mConnectionTimout;
    }

    /**
     * Set the String to use as the User-Agent HTTP header.
     * 
     * @param agent
     *            User-Agent String
     */
    public void setUserAgent(final String agent) {
        mUserAgent = agent;
    }

    /**
     * Get the User-Agent String to apply to the HTTP(S) calls.
     * 
     * @return String to use for User-Agent.
     */
    public String getUserAgent() {
        return mUserAgent;
    }

    /**
     * Add a custom query parameter that will be added to all Box API requests. In general you should not need to use this.
     * 
     * @param key
     *            Key.
     * @param value
     *            Value.
     */
    public void appendCustomQueryParameterToAllRequests(String key, String value) {
        mCustomQueryParams.add(new BasicNameValuePair(key, value));
    }

    /**
     * Clear out all custom query parameters that may have been set.
     */
    public void clearCustomQueryParameters() {
        mCustomQueryParams.clear();
    }

    /**
     * Get a list of all custom query parameters that have been set. This may return null or an empty List if none have been set. The list returned is not
     * modifiable. Attempts to modify the returned list, whether direct or via its iterator, result in an UnsupportedOperationException.
     * 
     * @return Unmodifiable list of custom query parameters.
     */
    public List<BasicNameValuePair> getCustomQueryParameters() {
        return Collections.unmodifiableList(mCustomQueryParams);
    }

    /**
     * SHOULD NEVER BE ENABLED FOR PRODUCTION BUILDS
     * 
     * Set the flag to enable HTTP Logging.
     * 
     * @param flag
     *            boolean to Enable Http Logging
     * 
     *            SHOULD NEVER BE ENABLED FOR PRODUCTION BUILDS
     */
    public void setEnableHttpLogging(final boolean flag) {
        mEnableHttpLogging = flag;
    }

    /**
     * Get the flag indicating whether Http Logging is enabled.
     * 
     * @return boolean.
     */
    public boolean getHttpLoggingEnabled() {
        return mEnableHttpLogging;
    }

    /** Accept-Language header value for English (US). */
    private static final String ENGLISH_ACCEPT_LANGUAGE = "en-us";

    /**
     * Get the Accept-Language HTTP header that we should set.
     * 
     * @return Accept-Language HTTP header.
     */
    public String getAcceptLanguage() {
        if (Locale.getDefault() == null) {
            return ENGLISH_ACCEPT_LANGUAGE;
        }
        StringBuffer sb = new StringBuffer();
        String language = Locale.getDefault().getLanguage().toLowerCase().trim();
        if (language.length() > 0) {
            sb.append(language);
            String country = Locale.getDefault().getCountry().toLowerCase().trim();
            if (country.length() > 0) {
                sb.append("-");
                sb.append(country);
            }
        }
        if (sb.length() == 0) {
            sb.append(ENGLISH_ACCEPT_LANGUAGE);
        }
        else if (!sb.toString().equals(ENGLISH_ACCEPT_LANGUAGE)) {
            sb.append(", ");
            sb.append(ENGLISH_ACCEPT_LANGUAGE);
        }
        return sb.toString();
    }
}

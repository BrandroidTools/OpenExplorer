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


/**
 * Represents an Box user.
 * 
 * @author developers@box.net
 */
public class User extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * User id.
     */
    protected long mUserId;
    /**
     * If the user is a guest, the access_id will be the user_id of the guest's parent. If this is a full user, the access_id will be the same as the user_id.
     */
    protected long mAccessId;
    /**
     * Auth token.
     */
    protected String mAuthToken;
    /**
     * Login (username).
     */
    protected String mLogin;
    /**
     * E-mail.
     */
    protected String mEmail;
    /**
     * The total amount of space allocated to that account.
     */
    protected long mSpaceAmount;
    /**
     * The amount of space currently utilized by the user.
     */
    protected long mSpaceUsed;
    /**
     * The maximum size in bytes for any individual file uploaded by the user.
     */
    protected long mMaxUploadSize;

    /**
     * Set user id.
     * 
     * @param userId
     *            user id
     */
    public void setId(final long userId) {
        mUserId = userId;
    }

    /**
     * Get user id.
     * 
     * @return user id.
     */
    public long getId() {
        return mUserId;
    }

    /**
     * Get user id.
     * 
     * @deprecated Use #getId() instead
     * @return user id
     */
    @Deprecated
    public long getUserId() {
        return mUserId;
    }

    /**
     * Set access id. If the user is a guest, the access_id will be the user_id of the guest's parent. If this is a full user, the access_id will be the same as
     * the user_id.
     * 
     * @param accessId
     *            access id
     */
    public void setAccessId(final long accessId) {
        mAccessId = accessId;
    }

    /**
     * Get access id. If the user is a guest, the access_id will be the user_id of the guest's parent. If this is a full user, the access_id will be the same as
     * the user_id.
     * 
     * @return access id
     */
    public long getAccessId() {
        return mAccessId;
    }

    /**
     * Set auth token.
     * 
     * @param authToken
     *            auth token
     */
    public void setAuthToken(final String authToken) {
        mAuthToken = authToken;
    }

    /**
     * Get auth token.
     * 
     * @return auth token
     */
    public String getAuthToken() {
        return mAuthToken;
    }

    /**
     * Set login (username).
     * 
     * @param login
     *            login (username)
     */
    public void setLogin(final String login) {
        mLogin = login;
    }

    /**
     * Get login (username).
     * 
     * @return login (username)
     */
    public String getLogin() {
        return mLogin;
    }

    /**
     * Set user's e-mail address.
     * 
     * @param email
     *            e-mail address
     */
    public void setEmail(final String email) {
        mEmail = email;
    }

    /**
     * Get user's e-mail address.
     * 
     * @return user's e-mail address.
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Set the total amount of space allocated to that account.
     * 
     * @param spaceAmount
     *            space amount
     */
    public void setSpaceAmount(final long spaceAmount) {
        mSpaceAmount = spaceAmount;
    }

    /**
     * Get the total amount of space allocated to that account.
     * 
     * @return space amount
     */
    public long getSpaceAmount() {
        return mSpaceAmount;
    }

    /**
     * Set the amount of space currently utilized by the user.
     * 
     * @param spaceUsed
     *            space used
     */
    public void setSpaceUsed(final long spaceUsed) {
        mSpaceUsed = spaceUsed;
    }

    /**
     * Get the amount of space currently utilized by the user.
     * 
     * @return space used
     */
    public long getSpaceUsed() {
        return mSpaceUsed;
    }

    /**
     * Set the maximum size in bytes for any individual file uploaded by the user.
     * 
     * @param maxUploadSize
     *            max upload size
     */
    public void setMaxUploadSize(final long maxUploadSize) {
        mMaxUploadSize = maxUploadSize;
    }

    /**
     * Get the maximum size in bytes for any individual file uploaded by the user.
     * 
     * @return max upload size
     */
    public long getMaxUploadSize() {
        return mMaxUploadSize;
    }

    /**
     * Utility method to parse attributes into DAO member data. Used by SAX parsers.
     * 
     * @param key
     *            Corresponds to attribute names and element names returned by Box API
     * @param value
     *            The value to be set
     */
    public void parseAttribute(final String key, final String value) {
        if (key.equals("user_id") || key.equals("id")) {
            setId(BoxUtils.parseLong(value));
        }
        if (key.equals("login")) {
            setLogin(value);
        }
        else if (key.equals("email")) {
            setEmail(value);
        }
        else if (key.equals("space_amount")) {
            setSpaceAmount(BoxUtils.parseLong(value));
        }
        else if (key.equals("space_used")) {
            setSpaceUsed(BoxUtils.parseLong(value));
        }
        else if (key.equals("max_upload_size")) {
            setMaxUploadSize(BoxUtils.parseLong(value));
        }
        else if (key.equals("access_id")) {
            setAccessId(BoxUtils.parseLong(value));
        }
        else if (key.equals("auth_token")) {
            setAuthToken(value);
        }
    }
}

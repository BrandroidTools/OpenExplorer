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


/**
 * Represents an update item from the user's updates tab on Box.
 * 
 * @author developers@box.net
 */
public class Update extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * update id.
     */
    protected long mId;
    /**
     * user id.
     */
    protected long mUserId;
    /**
     * user name.
     */
    protected String mUserName;
    /**
     * user e-mail.
     */
    protected String mUserEmail;
    /**
     * updated timestamp.
     */
    protected long mUpdated;
    /**
     * update type.
     */
    protected String mUpdateType;
    /**
     * folder id.
     */
    protected long mFolderId;
    /**
     * folder name.
     */
    protected String mFolderName;
    /**
     * whether the update is shared.
     */
    protected boolean mShared;
    /**
     * shared name.
     */
    protected String mSharedName;
    /**
     * owner id.
     */
    protected long mOwnerId;
    /**
     * folder path.
     */
    protected String mFolderPath;
    /**
     * whether the update has collaboration access.
     */
    protected boolean mCollabAccess;
    /**
     * list of files associated with this update.
     */
    protected ArrayList<BoxFile> mFiles = new ArrayList<BoxFile>();
    /**
     * list of folders associated with this update.
     */
    protected ArrayList<BoxFolder> mFolders = new ArrayList<BoxFolder>();

    /**
     * Get update id.
     * 
     * @return update id.
     */
    public long getId() {
        return mId;
    }

    /**
     * Set update id.
     * 
     * @param id
     *            update id.
     */
    public void setId(final long id) {
        mId = id;
    }

    /**
     * Get user id.
     * 
     * @return user id.
     */
    public long getUserId() {
        return mUserId;
    }

    /**
     * Set user id.
     * 
     * @param userId
     *            user id.
     */
    public void setUserId(final long userId) {
        mUserId = userId;
    }

    /**
     * Get user name.
     * 
     * @return username
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * Set user name.
     * 
     * @param userName
     *            username
     */
    public void setUserName(final String userName) {
        mUserName = userName;
    }

    /**
     * Get user e-mail.
     * 
     * @return user e-mail.
     */
    public String getUserEmail() {
        return mUserEmail;
    }

    /**
     * Set user e-mail.
     * 
     * @param userEmail
     *            user-email.
     */
    public void setUserEmail(final String userEmail) {
        mUserEmail = userEmail;
    }

    /**
     * Get updated timestamp.
     * 
     * @return updated timestamp.
     */
    public long getUpdated() {
        return mUpdated;
    }

    /**
     * Set updated timestamp.
     * 
     * @param updated
     *            updated timestamp.
     */
    public void setUpdated(final long updated) {
        mUpdated = updated;
    }

    /**
     * Get update type (e.g. "downloaded")
     * 
     * @return update type
     */
    public String getUpdateType() {
        return mUpdateType;
    }

    /**
     * Set update type.
     * 
     * @param updateType
     *            update type
     */
    public void setUpdateType(final String updateType) {
        mUpdateType = updateType;
    }

    /**
     * Get folder id.
     * 
     * @return folder id.
     */
    public long getFolderId() {
        return mFolderId;
    }

    /**
     * Set folder id.
     * 
     * @param folderId
     *            folder id
     */
    public void setFolderId(final long folderId) {
        mFolderId = folderId;
    }

    /**
     * Get folder name.
     * 
     * @return folder name
     */
    public String getFolderName() {
        return mFolderName;
    }

    /**
     * Set folder name.
     * 
     * @param folderName
     *            folder name
     */
    public void setFolderName(final String folderName) {
        mFolderName = folderName;
    }

    /**
     * Get whether the folder is shared.
     * 
     * @return true if the folder is shared, false otherwise
     */
    public boolean isShared() {
        return mShared;
    }

    /**
     * Set whether the folder is shared.
     * 
     * @param shared
     *            true if shared, false otherwise
     */
    public void setShared(final boolean shared) {
        mShared = shared;
    }

    /**
     * Get shared name which is a unique identifier for the file, which can be used to generate a shared page.
     * 
     * @return shared name.
     */
    public String getSharedName() {
        return mSharedName;
    }

    /**
     * Set shared name.
     * 
     * @param sharedName
     *            shared name.
     */
    public void setSharedName(final String sharedName) {
        mSharedName = sharedName;
    }

    /**
     * Get owner user id.
     * 
     * @return user id
     */
    public long getOwnerId() {
        return mOwnerId;
    }

    /**
     * Set owner user id.
     * 
     * @param ownerId
     *            owner user id.
     */
    public void setOwnerId(final long ownerId) {
        mOwnerId = ownerId;
    }

    /**
     * Get folder path from root.
     * 
     * @return folder path from root
     */
    public String getFolderPath() {
        return mFolderPath;
    }

    /**
     * Set folder path from root.
     * 
     * @param folderPath
     *            folder path from root.
     */
    public void setFolderPath(final String folderPath) {
        mFolderPath = folderPath;
    }

    /**
     * Get whether there are any collaborators who have access.
     * 
     * @return true if there are collaborators, false otherwise
     */
    public boolean isCollabAccess() {
        return mCollabAccess;
    }

    /**
     * Set whether there are any collaborators who have access.
     * 
     * @param collabAccess
     *            true if there are collaborators, false otherwise
     */
    public void setCollabAccess(final boolean collabAccess) {
        mCollabAccess = collabAccess;
    }

    /**
     * Get list of files in the update.
     * 
     * @return list of files
     */
    public ArrayList<BoxFile> getFiles() {
        return mFiles;
    }

    /**
     * Get list of folders in the update.
     * 
     * @return list of folders
     */
    public ArrayList<BoxFolder> getFolders() {
        return mFolders;
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
        if (key.equals("update_id")) {
            setId(BoxUtils.parseLong(value));
        }
        else if (key.equals("user_id")) {
            setUserId(BoxUtils.parseLong(value));
        }
        else if (key.equals("user_name")) {
            setUserName(value);
        }
        else if (key.equals("user_email")) {
            setUserEmail(value);
        }
        else if (key.equals("updated")) {
            setUpdated(BoxUtils.parseLong(value));
        }
        else if (key.equals("update_type")) {
            setUpdateType(value);
        }
        else if (key.equals("folder_id")) {
            setFolderId(BoxUtils.parseLong(value));
        }
        else if (key.equals("folder_name")) {
            setFolderName(value);
        }
        else if (key.equals("shared")) {
            setShared(value.equals("1") ? true : false);
        }
        else if (key.equals("shared_name")) {
            setSharedName(value);
        }
        else if (key.equals("owner_id")) {
            setOwnerId(BoxUtils.parseLong(value));
        }
        else if (key.equals("folder_path")) {
            setFolderPath(value);
        }
        else if (key.equals("collab_access")) {
            setCollabAccess(value.equals("1") ? true : false);
        }
    }
}

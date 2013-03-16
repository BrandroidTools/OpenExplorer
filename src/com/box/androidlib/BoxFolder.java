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
import java.util.List;


/**
 * Represents a folder on Box.
 * 
 * @author developers@box.net
 */
public class BoxFolder extends DAO {

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Folder id.
     */
    protected long mId;
    /**
     * Folder name.
     */
    protected String mFolderName;
    /**
     * Folder description.
     */
    protected String mDescription;
    /**
     * User id of the owner of the folder.
     */
    protected long mUserId;
    /**
     * Is the folder shared.
     */
    protected boolean mShared;
    /**
     * Share name.
     */
    protected String mSharedName;
    /**
     * Shared link URL.
     */
    protected String mSharedLink;
    /**
     * Whether the folder has collaborators.
     */
    protected boolean mHasCollaborators;
    /**
     * Permissions.
     */
    protected String mPermissions;
    /**
     * Folder password.
     */
    protected String mPassword;
    /**
     * Folder size in bytes.
     */
    protected long mSize;
    /**
     * The number of files in the folder.
     */
    protected long mFileCount;
    /**
     * Creation timestamp.
     */
    protected long mCreated;
    /**
     * Updated timestamp.
     */
    protected long mUpdated;
    /**
     * Thumbnail for tree display.
     */
    protected String mThumbnail;
    /**
     * Small thumbnail for tree display.
     */
    protected String mSmallThumbnail;
    /**
     * Large thumbnail for tree display.
     */
    protected String mLargeThumbnail;
    /**
     * Larger thumbnail for tree display.
     */
    protected String mLargerThumbnail;
    /**
     * Represents the path from the root directory to this folder.
     */
    protected String mPath;
    /**
     * Represents the path from the root directory to this folder.
     */
    protected String mFolderPath;
    /**
     * A unique identifier of a publicly shared file. This can be used to generate shared page URLs.
     */
    protected String mPublicName;
    /**
     * The folder_id of the folder's parent.
     */
    protected long mParentFolderId = -1;
    /**
     * The parent folder. Set to transient to avoid circular references when serializing to JSON.
     */
    protected transient BoxFolder mParentFolder;
    /**
     * The path of folder ids from the root to this folder.
     */
    protected String mFolderPathIds;
    /**
     * List of files in the folder.
     */
    protected ArrayList<BoxFile> mFilesInFolder = new ArrayList<BoxFile>();
    /**
     * List of folders in the folder.
     */
    protected ArrayList<BoxFolder> mFoldersInFolder = new ArrayList<BoxFolder>();
    /**
     * List of tag ids in the folder.
     */
    protected ArrayList<Long> mTagIds = new ArrayList<Long>();
    /**
     * List of web links in the folder.
     */
    protected ArrayList<WebLink> mWebLinksInFolder = new ArrayList<WebLink>();

    // Setters

    /**
     * Set file id.
     * 
     * @param id
     *            file id
     */
    public void setId(final long id) {
        mId = id;
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
     * SEt folder description.
     * 
     * @param desc
     *            folder description.
     */
    public void setDescription(final String desc) {
        mDescription = desc;
    }

    /**
     * Set owner user id.
     * 
     * @param userId
     *            user id
     */
    public void setUserId(final long userId) {
        mUserId = userId;
    }

    /**
     * Set whether the folder is shared.
     * 
     * @param isShared
     *            True if shared, false otherwise
     */
    public void setShared(final boolean isShared) {
        mShared = isShared;
    }

    /**
     * Set shared name (a unique identifier for the file, which can be used to generate a shared page).
     * 
     * @param sharedName
     *            shared name
     */
    public void setSharedName(final String sharedName) {
        mSharedName = sharedName;
    }

    /**
     * Set shared link.
     * 
     * @param sharedLink
     *            shared link URL.
     */
    public void setSharedLink(final String sharedLink) {
        mSharedLink = sharedLink;
    }

    /**
     * Set whether the folder has collaborators or not.
     * 
     * @param hasCollaborators
     *            True if the folder has collaborators.
     */
    public void setHasCollaborators(final boolean hasCollaborators) {
        mHasCollaborators = hasCollaborators;
    }

    /**
     * Set creation timestamp.
     * 
     * @param timestamp
     *            creation timestamp
     */
    public void setCreated(final long timestamp) {
        mCreated = timestamp;
    }

    /**
     * Set updated timestamp.
     * 
     * @param timestamp
     *            updated timestamp
     */
    public void setUpdated(final long timestamp) {
        mUpdated = timestamp;
    }

    /**
     * Set thumbnail url.
     * 
     * @param url
     *            Thumbnail url.
     */
    public void setThumbnail(final String url) {
        mThumbnail = url;
    }

    /**
     * Set small thumbnail url.
     * 
     * @param url
     *            Small thumbnail url
     */
    public void setSmallThumbnail(final String url) {
        mSmallThumbnail = url;
    }

    /**
     * Set large thumbnail url.
     * 
     * @param url
     *            Large thumbnail url
     */
    public void setLargeThumbnail(final String url) {
        mLargeThumbnail = url;
    }

    /**
     * Set larger thumbnail url.
     * 
     * @param url
     *            Larger thumbnail url
     */
    public void setLargerThumbnail(final String url) {
        mLargerThumbnail = url;
    }

    /**
     * Set folder path from root.
     * 
     * @param pathFromRoot
     *            path from root
     */
    public void setPath(final String pathFromRoot) {
        mPath = pathFromRoot;
    }

    /**
     * Set folder path from root.
     * 
     * @param pathFromRoot
     *            path from root
     */
    public void setFolderPath(final String pathFromRoot) {
        mFolderPath = pathFromRoot;
    }

    /**
     * Set public name.
     * 
     * @param publicName
     *            public name
     */
    public void setPublicName(final String publicName) {
        mPublicName = publicName;
    }

    /**
     * Set parent folder id.
     * 
     * @param parentFolderId
     *            parent folder id.
     */
    public void setParentFolderId(final long parentFolderId) {
        mParentFolderId = parentFolderId;
    }

    /**
     * Set the folder path ids.
     * 
     * @param folderPathIds
     *            folder path ids
     */
    public void setFolderPathIds(final String folderPathIds) {
        mFolderPathIds = folderPathIds;
    }

    /**
     * Set size in bytes.
     * 
     * @param numBytes
     *            size in bytes
     */
    public void setSize(final long numBytes) {
        mSize = numBytes;
    }

    /**
     * Set permissions.
     * 
     * @param perms
     *            permissions
     */
    public void setPermissions(final String perms) {
        mPermissions = perms;
    }

    /**
     * Set password.
     * 
     * @param pass
     *            password.
     */
    public void setPassword(final String pass) {
        mPassword = pass;
    }

    /**
     * Set file count.
     * 
     * @param fileCount
     *            number of files in folder.
     */
    public void setFileCount(final long fileCount) {
        mFileCount = fileCount;
    }

    /**
     * Set parent folder.
     * 
     * @param parentFolder
     *            parent folder
     */
    public void setParentFolder(final BoxFolder parentFolder) {
        mParentFolder = parentFolder;
    }

    // Getters

    /**
     * Get folder id.
     * 
     * @return folder id
     */
    public long getId() {
        return mId;
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
     * Get description.
     * 
     * @return description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get user id.
     * 
     * @return user id
     */
    public long userId() {
        return mUserId;
    }

    /**
     * Get whether the folder is shared or not.
     * 
     * @return true if shared, false otherwise
     */
    public boolean getShared() {
        return mShared;
    }

    /**
     * Get shared name, which is a unique identifier for the file, which can be used to generate a shared page.
     * 
     * @return shared name
     */
    public String getSharedName() {
        return mSharedName;
    }

    /**
     * Get shared link, which is a URL to share with others to view the file.
     * 
     * @return shared link
     */
    public String getSharedLink() {
        return mSharedLink;
    }

    /**
     * Get whether the folders has collaborators.
     * 
     * @return True if the folder has collaborators, false otherwise.
     */
    public boolean getHasCollaborators() {
        return mHasCollaborators;
    }

    /**
     * Get creation timestamp.
     * 
     * @return creation timestamp
     */
    public long getCreated() {
        return mCreated;
    }

    /**
     * Get timestamp of the most recent time in which the file was updated.
     * 
     * @return updated timestamp
     */
    public long getUpdated() {
        return mUpdated;
    }

    /**
     * Get the thumbnail url.
     * 
     * @return Thumbnail url.
     */
    public String getThumbnail() {
        if (mThumbnail != null) {
            return mThumbnail;
        }
        else if (mLargerThumbnail != null) {
            return mLargerThumbnail;
        }
        else if (mLargeThumbnail != null) {
            return mLargeThumbnail;
        }
        else if (mSmallThumbnail != null) {
            return mSmallThumbnail;
        }
        else {
            return null;
        }
    }

    /**
     * Get the small thumbnail url.
     * 
     * @return Small thumbnail url
     */
    public String getSmallThumbnail() {
        return mSmallThumbnail;
    }

    /**
     * Get the large thumbnail url.
     * 
     * @return Large thumbnail url.
     */
    public String getLargeThumbnail() {
        return mLargeThumbnail;
    }

    /**
     * Get the larger thumbnail url.
     * 
     * @return Larger thumbnail url.
     */
    public String getLargerThumbnail() {
        return mLargerThumbnail;
    }

    /**
     * Get the path from the root to the folder. In general this method is not useful and returns nothing. Only when using the search api is this populated.
     * 
     * @return path from root
     */
    public String getPath() {
        return mPath;
    }

    /**
     * Get the path from the root to the folder.
     * 
     * @return path from root
     */
    public String getFolderPath() {
        return mFolderPath;
    }

    /**
     * Get public name which is a unique identifier of a publicly shared file. This can be used to generate shared page URLs.
     * 
     * @return public name
     */
    public String getPublicName() {
        return mPublicName;
    }

    /**
     * Get the folder id of the folder in which this folder resides.
     * 
     * @return Parent folder id. 0 means the root folder. -1 means the parent folder id is not known by this object.
     */
    public long getParentFolderId() {
        return mParentFolderId;
    }

    /**
     * Get folder size in bytes.
     * 
     * @return folder size in bytes
     */
    public long getSize() {
        return mSize;
    }

    /**
     * Get the file count. Note that this is not always returned in all API requests.
     * 
     * @return the number of files in the folder
     */
    public long getFileCount() {
        return mFileCount;
    }

    /**
     * Get permissions.
     * 
     * @return permissions
     */
    public String getPermissions() {
        return mPermissions;
    }

    /**
     * Get folder password.
     * 
     * @return password
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Get parent folder.
     * 
     * @return parent folder
     */
    public BoxFolder getParentFolder() {
        return mParentFolder;
    }

    /**
     * Get the folder path ids. This will only return values if this BoxFolder was constructed through a getAccountTree() call, and Box.PARAM_SHOW_PATH_IDS is
     * set.
     * 
     * @return The folder path ids. (e.g. /54325/643563/425)
     */
    public String getFolderPathIds() {
        return mFolderPathIds;
    }

    /**
     * Get list of files in the folder.
     * 
     * @return list of files in folder
     */
    public List<? extends BoxFile> getFilesInFolder() {
        return mFilesInFolder;
    }

    /**
     * Get list of web links in the folder.
     * 
     * @return list of files in folder
     */
    public List<? extends WebLink> getWebLinksInFolder() {
        return mWebLinksInFolder;
    }

    /**
     * Add a child file to this folder. Response parsers always use this method to add child files to the folder rather than directly adding to the list through
     * getFilesInFolder().add(). So if you want to override how the list of child files are populated, you can override this method in your own custom BoxFolder
     * class.
     * 
     * @param boxFile
     *            The BoxFile to add as a child of this folder.
     */
    public void addChildFile(final BoxFile boxFile) {
        mFilesInFolder.add(boxFile);
    }

    /**
     * Add a child web link to this folder. Response parsers always use this method to add child files to the folder rather than directly adding to the list
     * through getFilesInFolder().add(). So if you want to override how the list of child files are populated, you can override this method in your own custom
     * BoxFolder class.
     * 
     * @param webLink
     *            The web link to add as a child of this folder.
     */
    public void addChildWebLink(final WebLink webLink) {
        mWebLinksInFolder.add(webLink);
    }

    /**
     * 
     * @param webLinkId
     *            the web link id to be removed from the folder if a link with such an id exists.
     */
    public void removeChildWebLink(final long webLinkId) {
        for (int i = 0; i < mWebLinksInFolder.size(); i++) {
            if (mWebLinksInFolder.get(i).getId() == webLinkId) {
                mWebLinksInFolder.remove(i);
                return;
            }
        }

    }

    /**
     * Get list of folders in folder.
     * 
     * @return list of folders in folder
     */
    public List<? extends BoxFolder> getFoldersInFolder() {
        return mFoldersInFolder;
    }

    /**
     * Add a child folder to this folder. Response parsers always use this method to add child folders to the folder rather than directly adding to the list
     * through getFoldersInFolder().add(). So if you want to override how the list of child folders are populated, you can override this method in your own
     * custom BoxFolder class.
     * 
     * @param boxFolder
     *            The BoxFolder to add as a child of this folder.
     */
    public void addChildFolder(final BoxFolder boxFolder) {
        mFoldersInFolder.add(boxFolder);
    }

    /**
     * Get list of tag ids in folder.
     * 
     * @return list of tag ids in folder
     */
    public ArrayList<Long> getTagIds() {
        return mTagIds;
    }

    /**
     * Set upward references of all child folders and files to the parent folder.
     */
    public void repairParentFolderReferences() {
        for (int i = 0; i < mFoldersInFolder.size(); i++) {
            mFoldersInFolder.get(i).setParentFolderId(getId());
            mFoldersInFolder.get(i).setParentFolder(this);
            mFoldersInFolder.get(i).repairParentFolderReferences();
        }
        for (int i = 0; i < mFilesInFolder.size(); i++) {
            mFilesInFolder.get(i).setFolderId(getId());
            mFilesInFolder.get(i).setFolder(this);
        }
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
        if (key.equals("folder_id") || key.equals("id")) {
            setId(BoxUtils.parseLong(value));
        }
        else if (key.equals("folder_name") || key.equals("name")) {
            setFolderName(value);
        }
        else if (key.equals("shared")) {
            setShared(value.equals("1") ? true : false);
        }
        else if (key.equals("shared_name")) {
            setSharedName(value);
        }
        else if (key.equals("shared_link")) {
            setSharedLink(value);
        }
        else if (key.equals("size")) {
            setSize(BoxUtils.parseSizeString(value));
        }
        else if (key.equals("pic_l")) {
            setLargeThumbnail(value);
        }
        else if (key.equals("pic_s")) {
            setSmallThumbnail(value);
        }
        else if (key.equals("pic_x")) {
            setLargerThumbnail(value);
        }
        else if (key.equals("created")) {
            setCreated(BoxUtils.parseLong(value));
        }
        else if (key.equals("updated")) {
            setUpdated(BoxUtils.parseLong(value));
        }
        else if (key.equals("file_count")) {
            setFileCount(BoxUtils.parseLong(value));
        }
        else if (key.equals("user_id")) {
            setUserId(BoxUtils.parseLong(value));
        }
        else if (key.equals("path")) {
            setPath(value);
        }
        else if (key.equals("public_name")) {
            setPublicName(value);
        }
        else if (key.equals("parent_folder_id")) {
            setParentFolderId(BoxUtils.parseLong(value));
        }
        else if (key.equals("password")) {
            setPassword(value);
        }
        else if (key.equals("thumbnail")) {
            setThumbnail(value);
        }
        else if (key.equals("small_thumbnail")) {
            setSmallThumbnail(value);
        }
        else if (key.equals("large_thumbnail")) {
            setLargeThumbnail(value);
        }
        else if (key.equals("larger_thumbnail")) {
            setLargerThumbnail(value);
        }
        else if (key.equals("permissions")) {
            setPermissions(value);
        }
        else if (key.equals("has_collaborators")) {
            setHasCollaborators(value.equals("1") ? true : false);
        }
        else if (key.equals("folder_path_ids")) {
            setFolderPathIds(value);
        }
        else if (key.equals("folder_path")) {
            setFolderPath(value);
        }
        else if (key.equals("description")) {
            setDescription(value);
        }
    }
}

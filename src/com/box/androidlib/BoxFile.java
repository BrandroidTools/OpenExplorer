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
 * Represents a file on Box.
 * 
 * @author developers@box.net
 */
public class BoxFile extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the file.
     */
    protected long mId;
    /**
     * The name of the file on Box.
     */
    protected String mFileName;
    /**
     * The description of the file on Box.
     */
    protected String mDescription;
    /**
     * The folder_id of the folder that contains the file.
     */
    protected long mFolderId = -1;
    /**
     * The BoxFolder that this file belongs to. Set to transient to avoid circular references when serializing to JSON.
     */
    protected transient BoxFolder mFolder;
    /**
     * Whether or not the file is shared.
     */
    protected boolean mShared;
    /**
     * The shared name of the file.
     */
    protected String mSharedName;
    /**
     * The Sha1 sum of the file.
     */
    protected String mSha1;
    /**
     * Created timestamp.
     */
    protected long mCreated;
    /**
     * Updated timestamp.
     */
    protected long mUpdated;
    /**
     * The size of the file.
     */
    protected long mSize;
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
     * Preview thumbnail for tree display.
     */
    protected String mPreviewThumbnail;
    /**
     * Permissions.
     */
    protected String mPermissions;
    /**
     * List of tag ids associated with the file.
     */
    protected ArrayList<Long> mTagIds = new ArrayList<Long>();
    /**
     * Number of comments.
     */
    protected int mCommentCount;

    // Setters

    /**
     * Set the file_id.
     * 
     * @param fileId
     *            The id of the file.
     */
    public void setId(final long fileId) {
        mId = fileId;
    }

    /**
     * Set file name.
     * 
     * @param fileName
     *            The nname of the file
     */
    public void setFileName(final String fileName) {
        mFileName = fileName;
    }

    /**
     * Set file description.
     * 
     * @param desc
     *            The file description
     */
    public void setDescription(final String desc) {
        mDescription = desc;
    }

    /**
     * Set folder id.
     * 
     * @param folderId
     *            The folder_id of the folder that contains the file
     */
    public void setFolderId(final long folderId) {
        mFolderId = folderId;
    }

    /**
     * Set the parent folder.
     * 
     * @param folder
     *            The BoxFolder that owns this file.
     */
    public void setFolder(final BoxFolder folder) {
        mFolder = folder;
    }

    /**
     * Set whether the file is shared.
     * 
     * @param isShared
     *            true if the folder is shared, false otherwise
     */
    public void setShared(final boolean isShared) {
        mShared = isShared;
    }

    /**
     * Set the shared name.
     * 
     * @param sharedName
     *            The shared name.
     */
    public void setSharedName(final String sharedName) {
        mSharedName = sharedName;
    }

    /**
     * Set the sha1 sum.
     * 
     * @param sha1
     *            The sha1 sum.
     */
    public void setSha1(final String sha1) {
        mSha1 = sha1;
    }

    /**
     * Set the created timestamp.
     * 
     * @param timestamp
     *            creation timestamp
     */
    public void setCreated(final long timestamp) {
        mCreated = timestamp;
    }

    /**
     * Set the updated timestamp.
     * 
     * @param timestamp
     *            update timestamp.
     */
    public void setUpdated(final long timestamp) {
        mUpdated = timestamp;
    }

    /**
     * Set file size in bytes.
     * 
     * @param numBytes
     *            file size in bytes
     */
    public void setSize(final long numBytes) {
        mSize = numBytes;
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
     * Set preview thumbnail url.
     * 
     * @param url
     *            Preview thumbnail url
     */
    public void setPreviewThumbnail(final String url) {
        mPreviewThumbnail = url;
    }

    /**
     * Set permissions string.
     * 
     * @param perms
     *            permissions string
     */
    public void setPermissions(final String perms) {
        mPermissions = perms;
    }

    // Getters

    /**
     * Get the file id.
     * 
     * @return The file's id
     */
    public long getId() {
        return mId;
    }

    /**
     * Get the file name.
     * 
     * @return file name
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * Get the file's description.
     * 
     * @return file description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get the folder_id of the folder that the file resides in.
     * 
     * @return folder id. 0 means the root folder. -1 means the folder id is not known by this object.
     */
    public long getFolderId() {
        return mFolderId;
    }

    /**
     * Get the parent folder.
     * 
     * @return The BoxFolder that owns this file. This may be null if this BoxFile was created through an API call that did not contain its parent folder.
     */
    public BoxFolder getFolder() {
        return mFolder;
    }

    /**
     * Get whether the file is shared or not.
     * 
     * @return true if shared, false otherwise.
     */
    public boolean getShared() {
        return mShared;
    }

    /**
     * Get the shared name.
     * 
     * @return shared name
     */
    public String getSharedName() {
        return mSharedName;
    }

    /**
     * Get the sha1 sum of the file.
     * 
     * @return sha1 sum
     */
    public String getSha1() {
        return mSha1;
    }

    /**
     * Get the timestamp when the file was created.
     * 
     * @return creation timestamp
     */
    public long getCreated() {
        return mCreated;
    }

    /**
     * Get the timestamp when the file was last updated.
     * 
     * @return timestamp of last update.
     */
    public long getUpdated() {
        return mUpdated;
    }

    /**
     * Get the file size in bytes.
     * 
     * @return file size
     */
    public long getSize() {
        return mSize;
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
     * Get the preview thumbnail url.
     * 
     * @return Preview thumbnail url.
     */
    public String getPreviewThumbnail() {
        return mPreviewThumbnail;
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
     * Get list of tag ids that have been applied to the file.
     * 
     * @return list of tag ids
     */
    public ArrayList<Long> getTagIds() {
        return mTagIds;
    }

    /**
     * Get the number of comments that have been made on this file.
     * 
     * @return Number of comments.
     */
    public int getCommentCount() {
        return mCommentCount;
    }

    /**
     * Set the number of comments. Usually only called by the response parsers.
     * 
     * @param count
     *            Number of comments.
     */
    public void setCommentCount(final int count) {
        mCommentCount = count;
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
        if (key.equals("file_id") || key.equals("id")) {
            setId(BoxUtils.parseLong(value));
        }
        else if (key.equals("folder_id")) {
            setFolderId(BoxUtils.parseLong(value, -1));
        }
        else if (key.equals("file_name") || key.equals("name")) {
            setFileName(value);
        }
        else if (key.equals("shared_name")) {
            setSharedName(value);
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
        else if (key.equals("size")) {
            setSize(BoxUtils.parseSizeString(value));
        }
        else if (key.equals("sha1")) {
            setSha1(value);
        }
        else if (key.equals("created")) {
            setCreated(BoxUtils.parseLong(value));
        }
        else if (key.equals("updated")) {
            setUpdated(BoxUtils.parseLong(value));
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
        else if (key.equals("preview_thumbnail")) {
            setPreviewThumbnail(value);
        }
        else if (key.equals("permissions")) {
            setPermissions(value);
        }
        else if (key.equals("comment_count")) {
            setCommentCount(BoxUtils.parseInt(value));
        }
        else if (key.equals("description")) {
            setDescription(value);
        }
    }
}

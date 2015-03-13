package com.box.androidlib;


/**
 * Represents a collaboration on Box.
 * 
 */
public class Collaboration extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /** Collaboration id. */
    private long mId;
    /** Item role name. */
    private String mItemRoleName;
    /** Acceptance status. */
    private String mStatus;
    /** Type (folder or file). */
    private String mItemType;
    /** Folder or file id. */
    private long mItemId;
    /** Folder or file name. */
    private String mItemName;
    /** User id of owner. */
    private long mItemUserId;
    /** Username of owner. */
    private String mItemUserName;
    /** User id of collaborator. */
    private long mUserId;
    /** User name of collaborator. */
    private String mName;
    /** Email of collaborator. */
    private String mEmail;

    /**
     * @return ID of the collaboration.
     */
    public long getId() {
        return mId;
    }

    /**
     * @return Role of the collaborator. Can be Box.ITEM_ROLE_EDITOR or Box.ITEM_ROLE_VIEWER.
     */
    public String getItemRoleName() {
        return mItemRoleName;
    }

    /**
     * @return Acceptance status. Either "pending" or "accepted".
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * @return The type of item. Either Box.TYPE_FILE or Box.TYPE_FOLDER. As there is currently no way to invite collaborators to files, this will always be
     *         Box.TYPE_FOLDER.
     */
    public String getItemType() {
        return mItemType;
    }

    /**
     * @return The id of the item being collaborated on. As there is currently no way to invite collaborators to files, this will always be the folder id.
     */
    public long getItemId() {
        return mItemId;
    }

    /**
     * @return The name of the item being collaborated on. As there is currently no way to invite collaborators to files, this will always be the folder name.
     */
    public String getItemName() {
        return mItemName;
    }

    /**
     * @return The user id of the owner of the item.
     */
    public long getItemUserId() {
        return mItemUserId;
    }

    /**
     * @return The username of the owner of the item.
     */
    public String getItemUserName() {
        return mItemUserName;
    }

    /**
     * @return The user id of the user whose access is indicated by this collaboration.
     */
    public long getUserId() {
        return mUserId;
    }

    /**
     * @return The username of the user whose access is indicated by this collaboration.
     */
    public String getName() {
        return mName;
    }

    /**
     * @return The email address of user whose access is indicated by this collaboration.
     */
    public String getEmail() {
        return mEmail;
    }

    // Setters

    /**
     * Set the collaboration id.
     * 
     * @param id
     *            collaboration id
     */
    public void setId(final long id) {
        mId = id;
    }

    /**
     * Set the item role.
     * 
     * @param itemRoleName
     *            Item role name.
     */
    public void setItemRoleName(final String itemRoleName) {
        mItemRoleName = itemRoleName;
    }

    /**
     * Set collaboration status.
     * 
     * @param status
     *            Collaboration status.
     */
    public void setStatus(final String status) {
        mStatus = status;
    }

    /**
     * Set item type.
     * 
     * @param itemType
     *            Item type.
     */
    public void setItemType(final String itemType) {
        mItemType = itemType;
    }

    /**
     * Set item id.
     * 
     * @param itemId
     *            Item id.
     */
    public void setItemId(final long itemId) {
        mItemId = itemId;
    }

    /**
     * Set item name.
     * 
     * @param itemName
     *            Item name.
     */
    public void setItemName(final String itemName) {
        mItemName = itemName;
    }

    /**
     * Set item user id.
     * 
     * @param itemUserId
     *            Item user id.
     */
    public void setItemUserId(final long itemUserId) {
        mItemUserId = itemUserId;
    }

    /**
     * Set item user name.
     * 
     * @param itemUserName
     *            Item user name.
     */
    public void setItemUserName(final String itemUserName) {
        mItemUserName = itemUserName;
    }

    /**
     * Set user id.
     * 
     * @param userId
     *            User id.
     */
    public void setUserId(final long userId) {
        mUserId = userId;
    }

    /**
     * Set user name targeted by this collaboration.
     * 
     * @param name
     *            Username.
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Set email.
     * 
     * @param email
     *            Email.
     */
    public void setEmail(final String email) {
        mEmail = email;
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
        if (key.equals("id")) {
            setId(BoxUtils.parseLong(value, -1));
        }
        else if (key.equals("item_role_name")) {
            setItemRoleName(value);
        }
        else if (key.equals("status")) {
            setStatus(value);
        }
        else if (key.equals("item_type")) {
            setItemType(value);
        }
        else if (key.equals("item_id")) {
            setItemId(BoxUtils.parseLong(value, -1));
        }
        else if (key.equals("item_name")) {
            setItemName(value);
        }
        else if (key.equals("item_user_id")) {
            setItemUserId(BoxUtils.parseLong(value, -1));
        }
        else if (key.equals("item_user_name")) {
            setItemUserName(value);
        }
        else if (key.equals("user_id")) {
            setUserId(BoxUtils.parseLong(value, -1));
        }
        else if (key.equals("name")) {
            setName(value);
        }
        else if (key.equals("email")) {
            setEmail(value);
        }
    }
}

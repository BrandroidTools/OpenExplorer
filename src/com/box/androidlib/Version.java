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
 * Represents a version of a Box file.
 * 
 * @author developers@box.net
 */
public class Version extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Version id.
     */
    protected long mVersionId;
    /**
     * Author username.
     */
    protected String mAuthor;
    /**
     * Update timestamp.
     */
    protected long mUpdated;

    /**
     * Get version id.
     * 
     * @return version id
     */
    public long getId() {
        return mVersionId;
    }

    /**
     * Set version id.
     * 
     * @param versionId
     *            version id
     */
    public void setId(final long versionId) {
        mVersionId = versionId;
    }

    /**
     * Get author username.
     * 
     * @return author username.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Set author username.
     * 
     * @param author
     *            author username
     */
    public void setAuthor(final String author) {
        mAuthor = author;
    }

    /**
     * Get updated timestamp.
     * 
     * @return updated timestamp
     */
    public long getUpdated() {
        return mUpdated;
    }

    /**
     * Set updated timestamp.
     * 
     * @param updated
     *            updated timestamp
     */
    public void setUpdated(final long updated) {
        mUpdated = updated;
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
        if (key.equals("version_id") || key.equals("id")) {
            setId(BoxUtils.parseLong(value));
        }
        else if (key.equals("author")) {
            setAuthor(value);
        }
        else if (key.equals("updated")) {
            setUpdated(BoxUtils.parseLong(value));
        }
    }

}

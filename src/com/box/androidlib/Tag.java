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
 * Represents a tag on Box.
 * 
 * @author developers@box.net
 */
public class Tag extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * tag value.
     */
    protected String mTag;
    /**
     * tag id.
     */
    protected long mId;

    /**
     * Get tag value.
     * 
     * @return tag value
     */
    public String getTag() {
        return mTag;
    }

    /**
     * Set tag value.
     * 
     * @param tag
     *            tag value
     */
    public void setTag(final String tag) {
        mTag = tag;
    }

    /**
     * Get tag id.
     * 
     * @return tag id
     */
    public long getId() {
        return mId;
    }

    /**
     * Set tag id.
     * 
     * @param id
     *            tag id
     */
    public void setId(final long id) {
        mId = id;
    }

}

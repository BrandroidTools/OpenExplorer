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
 * Represents a the result of a search on Box. Contains the files and folders returned.
 * 
 * @author developers@box.net
 */
public class SearchResult extends DAO {

    /**
     * Serialialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of folders found in search.
     */
    protected ArrayList<BoxFolder> mFolders = new ArrayList<BoxFolder>();
    /**
     * List of files found in search.
     */
    protected ArrayList<BoxFile> mFiles = new ArrayList<BoxFile>();

    /**
     * Get list of folders found in search.
     * 
     * @return list of folders
     */
    public ArrayList<BoxFolder> getFolders() {
        return mFolders;
    }

    /**
     * Get list of files found in search.
     * 
     * @return list of files
     */
    public ArrayList<BoxFile> getFiles() {
        return mFiles;
    }
}

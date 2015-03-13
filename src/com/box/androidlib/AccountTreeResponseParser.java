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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Response parser for get_account_tree API request.
 * 
 * @author developers@box.net
 */
public class AccountTreeResponseParser extends DefaultResponseParser {

    /**
     * The BoxFolder that whose tree is being requested.
     */
    private BoxFolder mTargetFolder = null;
    /**
     * The BoxFolder currently being parsed.
     */
    private BoxFolder mCurrFolder = null;
    /**
     * The BoxFile currently being parsed.
     */
    private BoxFile mBoxFile = null;
    /**
     * The WebLink currently being parsed.
     */
    private WebLink mWebLink = null;
    
    
    /**
     * Enum definition to indicate whether we are currently parsing a file or folder element.
     */
    private enum FileOrFolder {
        /** Indicates that we are processing a file. */
        FILE,
        /** Indicates that we are processing a folder. */
        FOLDER,
        /** Indicates that we are processing a weblink. */
        WEBLINK
    };

    /**
     * Instance of FileOrFolder.
     */
    private FileOrFolder mFileOrFolder;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        try {
            if (localName.equals("folder")) {
                mFileOrFolder = FileOrFolder.FOLDER;
                if (mTargetFolder == null) {
                    mTargetFolder = Box.getBoxFolderClass().newInstance();
                    mCurrFolder = mTargetFolder;
                    for (int i = 0; i < attributes.getLength(); i++) {
                        mCurrFolder.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                    }
                }
                else {
                    final BoxFolder parentFolder = mCurrFolder;
                    mCurrFolder = Box.getBoxFolderClass().newInstance();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        mCurrFolder.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                    }
                    mCurrFolder.setParentFolder(parentFolder);
                    mCurrFolder.setParentFolderId(parentFolder.getId());
                    parentFolder.addChildFolder(mCurrFolder);
                }
            }
            else if (localName.equals("file")) {
                mFileOrFolder = FileOrFolder.FILE;
                mBoxFile = Box.getBoxFileClass().newInstance();
                for (int i = 0; i < attributes.getLength(); i++) {
                    mBoxFile.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                }
                mCurrFolder.addChildFile(mBoxFile);
                mBoxFile.setFolder(mCurrFolder);
                mBoxFile.setFolderId(mCurrFolder.getId());
            }
            else if (localName.equals("tag")) {
                if (mFileOrFolder == FileOrFolder.FILE) {
                    mBoxFile.getTagIds().add(BoxUtils.parseLong(attributes.getValue("id")));
                }
                else if (mFileOrFolder == FileOrFolder.FOLDER) {
                    mCurrFolder.getTagIds().add(BoxUtils.parseLong(attributes.getValue("id")));
                }
                else if (mFileOrFolder == FileOrFolder.WEBLINK) {
                    mWebLink.getTagIds().add(BoxUtils.parseLong(attributes.getValue("id")));
                }
            }
            else if (localName.equals("web_link")) {                
                mFileOrFolder = FileOrFolder.WEBLINK;
                mWebLink = new WebLink();
                for (int i = 0; i < attributes.getLength(); i++) {
                    mWebLink.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                }
                mCurrFolder.addChildWebLink(mWebLink);
                mWebLink.setFolder(mCurrFolder);
                mWebLink.setFolderId(mCurrFolder.getId());

            }

        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("folder")) {
            mCurrFolder = mCurrFolder.getParentFolder();
        }
    }

    /**
     * Get BoxFolder representing the tree that was requested.
     * 
     * @return BoxFolder
     */
    public BoxFolder getFolder() {
        return mTargetFolder;
    }
}

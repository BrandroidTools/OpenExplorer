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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Parser for Box API response containing a list of updates. Used for API action get_updates.
 * 
 * @author developers@box.net
 */
public class UpdatesResponseParser extends DefaultResponseParser {

    /**
     * List of updates to populate.
     */
    private ArrayList<Update> updates;
    /**
     * The update currently being parsed.
     */
    private Update update;
    /**
     * The file currently being parsed.
     */
    private BoxFile file;
    /**
     * The folder currently being parsed.
     */
    private BoxFolder folder;

    /**
     * Enum definition used to indicate what type of element we are currently parsing.
     */
    private enum CurrentlyParsing {
        /** Indicates we are parsing an update element. */
        UPDATE,
        /** Indicates we are parsing a file element. */
        FILE,
        /** Indicates we are parsing a folder element. */
        FOLDER
    };

    /**
     * Used to track what type of element we are currently parsing.
     */
    private CurrentlyParsing mCurrentlyParsing;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("updates")) {
            updates = new ArrayList<Update>();
        }
        else if (localName.equals("update")) {
            update = new Update();
            if (updates != null) {
                updates.add(update);
            }
            mCurrentlyParsing = CurrentlyParsing.UPDATE;
        }
        else if (localName.equals("file")) {
            try {
                file = Box.getBoxFileClass().newInstance();
                for (int i = 0; i < attributes.getLength(); i++) {
                    file.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                }
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (update != null) {
                file.setFolderId(update.getFolderId());
                update.getFiles().add(file);
            }
            mCurrentlyParsing = CurrentlyParsing.FILE;
        }
        else if (localName.equals("folder")) {
            try {
                folder = Box.getBoxFolderClass().newInstance();
                for (int i = 0; i < attributes.getLength(); i++) {
                    folder.parseAttribute(attributes.getLocalName(i), attributes.getValue(i));
                }
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (update != null) {
                folder.setParentFolderId(update.getFolderId());
                update.getFolders().add(folder);
            }
            mCurrentlyParsing = CurrentlyParsing.FOLDER;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (mCurrentlyParsing != null) {
            switch (mCurrentlyParsing) {
                case UPDATE:
                    update.parseAttribute(localName, mTextNode.toString());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get the list of updates parsed out of the response.
     * 
     * @return list of updates
     */
    public ArrayList<Update> getUpdates() {
        return updates;
    }
}

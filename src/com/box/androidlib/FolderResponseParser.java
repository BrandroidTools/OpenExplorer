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
 * Parser for a Box API response containing a single Box Folder.
 * 
 * @author developers@Box.net
 */
public class FolderResponseParser extends DefaultResponseParser {

    /**
     * The BoxFolder representing the response.
     */
    private BoxFolder mBoxFolder;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("folder")) {
            try {
                mBoxFolder = Box.getBoxFolderClass().newInstance();
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (mBoxFolder != null) {
            mBoxFolder.parseAttribute(localName, mTextNode.toString());
        }
    }

    /**
     * Get the BoxFolder parsed from the API response.
     * 
     * @return BoxFolder parsed from the API response
     */
    public BoxFolder getFolder() {
        return mBoxFolder;
    }
}

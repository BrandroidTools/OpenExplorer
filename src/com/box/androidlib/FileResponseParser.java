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
 * Parser for a BoxAPI response containing a single Box file object.
 * 
 * @author developers@box.net
 */
public class FileResponseParser extends DefaultResponseParser {

    /**
     * The BoxFile representing the response.
     */
    private BoxFile mFile;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("response")) {
            try {
                mFile = Box.getBoxFileClass().newInstance();
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
        if (mFile != null) {
            mFile.parseAttribute(localName, mTextNode.toString());
        }
    }

    /**
     * Get the BoxFile parsed from the API response.
     * 
     * @return BoxFile parsed from the API response
     */
    public BoxFile getFile() {
        return mFile;
    }

    /**
     * Set BoxFile.
     * 
     * @param file
     *            BoxFile
     */
    public void setFile(final BoxFile file) {
        mFile = file;
    }
}

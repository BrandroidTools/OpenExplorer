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
 * Parser for a Box API response containing a list of versions.
 * 
 * @author developers@box.net
 */
public class VersionsResponseParser extends DefaultResponseParser {

    /**
     * The list of versions to populate.
     */
    private final ArrayList<Version> versions = new ArrayList<Version>();
    /**
     * The version currently being parsed.
     */
    private Version version = null;

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("version")) {
            version = new Version();
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("versions")) {
            versions.add(version);
        }
        else if (localName.equals("version")) {
            versions.add(version);
        }
        else {
            if (version != null) {
                version.parseAttribute(localName, mTextNode.toString());
            }
        }
    }

    /**
     * Get the list of versions parsed out of the Box API response.
     * 
     * @return list of versions.
     */
    public ArrayList<Version> getVersions() {
        return versions;
    }

}

/*******************************************************************************
 * Copyright 2011 Box.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Parser for a response form Box API containing a list of tags.
 * 
 * @author developers@box.net
 */
public class TagsResponseParser extends DefaultResponseParser {

    /**
     * List of tags to populate.
     */
    private final ArrayList<Tag> mTags = new ArrayList<Tag>();
    /**
     * The tag currently being parsed.
     */
    private Tag mTag;

    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("tag")) {
            mTag = new Tag();
            mTags.add(mTag);
            mTag.setId(BoxUtils.parseLong(attributes.getValue("id")));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("tag")) {
            mTag.setTag(mTextNode.toString());
        }
    }

    /**
     * Get the list of tags parsed out from the Box API response.
     * 
     * @return list of tags
     */
    public ArrayList<Tag> getTags() {
        return mTags;
    }
}

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

import org.xml.sax.SAXException;

/**
 * Parser for a Box API response for the public_share action.
 */
public class PublicShareResponseParser extends DefaultResponseParser {

    /**
     * public name.
     */
    private String publicName;

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("public_name")) {
            publicName = mTextNode.toString();
        }
    }

    /**
     * Get public name, which is a unique identifier used to generate shared
     * pages and widgets.
     * 
     * @return public name.
     */
    public String getPublicName() {
        return publicName;
    }
}

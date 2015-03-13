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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Parser for a Box API response containing a user.
 * 
 * @author developers@box.net
 */
public class UserResponseParser extends DefaultResponseParser {

    /**
     * The user object to be populated.
     */
    private User mUser;
    /**
     * Auth token.
     */
    private String mAuthToken;

    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("user")) {
            mUser = new User();
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if (mUser != null) {
            mUser.parseAttribute(localName, mTextNode.toString());
        }
        if (localName.equals("auth_token")) {
            mAuthToken = mTextNode.toString();
        }
    }

    /**
     * Get the user parsed out of the response.
     * 
     * @return Box user
     */
    public User getUser() {
        if (mUser != null) {
            mUser.setAuthToken(mAuthToken);
        }
        return mUser;
    }
}

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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base response parser for Box API response XML. Parses out the status of the
 * response.
 *
 * @author developers@box.net
 */
public class DefaultResponseParser extends DefaultHandler {

    /**
     * Used to track who the parent of the current element is.
     */
    protected final Stack<String> mParentStack = new Stack<String>();
    /**
     * Status of the response.
     */
    protected String mStatus = "";
    /**
     * The current text node being parsed.
     */
    protected StringBuilder mTextNode = new StringBuilder();

    /**
     * Get the status of the response from Box API.
     *
     * @return status
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * Set the status of the response from Box API.
     *
     * @param status
     *            status
     */
    public void setStatus(final String status) {
        mStatus = status;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) throws SAXException {
        mTextNode.setLength(0);
        mParentStack.push(localName);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        mParentStack.pop();
        if (localName.equals("status") && mParentStack.size() == 1
            && mParentStack.peek().equals("response")) {
            // Need to be sure that the parent element is the root <response>
            // element. Responses from Box API always follow the basic format:
            // <?xml version='1.0' encoding='UTF-8' ?>
            // <response><status>ok</status>....</response>
            mStatus = mTextNode.toString();
        }
    }

    @Override
    public void characters(final char[] buffer, final int start, final int length)
        throws SAXException {
        mTextNode.append(buffer, start, length);
    }
}

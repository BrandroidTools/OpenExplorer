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
 * Parser for Box API response for toggle_folder_email action.
 * 
 * @author developers@box.net
 */
public class ToggleFolderEmailResponseParser extends DefaultResponseParser {

    /**
     * Upload e-mail.
     */
    private String uploadEmail = "";

    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) throws SAXException {
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("upload_email")) {
            uploadEmail = mTextNode.toString();
        }
    }

    /**
     * Get the upload e-mail that will accept files.
     * 
     * @return upload e-mail
     */
    public String getUploadEmail() {
        return uploadEmail;
    }

}

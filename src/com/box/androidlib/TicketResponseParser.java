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
 * Parser for Box API response containing a ticket.
 * 
 * @author developers@box.net
 */
public class TicketResponseParser extends DefaultResponseParser {

    /**
     * Ticket parsed out of the response.
     */
    private String mTicket;

    @Override
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("ticket")) {
            mTicket = mTextNode.toString();
        }
    }

    /**
     * Get the ticket parsed out of the response.
     * 
     * @return ticket
     */
    public String getTicket() {
        return mTicket;
    }
}

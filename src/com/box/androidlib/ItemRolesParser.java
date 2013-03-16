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
 * Parser for a Box API response for the get_item_roles_for_item action.
 */
public class ItemRolesParser extends DefaultResponseParser {

    private boolean parsingItemRoles = false;
    private ArrayList<ItemRole> itemRoles = new ArrayList<ItemRole>();
    private ItemRole tmpItemRole;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("item_roles")) {
            parsingItemRoles = true;
        }
        if (parsingItemRoles) {
            if (localName.equals("item_role")) {
                tmpItemRole = new ItemRole();
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals("item_roles")) {
            parsingItemRoles = false;
        }
        if (parsingItemRoles) {
            if (tmpItemRole != null) {
                if (localName.equals("name")) {
                    tmpItemRole.setName(mTextNode.toString());
                }
                else if (localName.equals("item_permissions")) {
                    tmpItemRole.setPermissions(mTextNode.toString());
                }
                else if (localName.equals("item_role")) {
                    itemRoles.add(tmpItemRole);
                    tmpItemRole = null;
                }
            }
        }

    }

    /**
     * Get the list of ItemRoles.
     * 
     * @return List of ItemRoles.
     */
    public ArrayList<ItemRole> getItemRoles() {
        return itemRoles;
    }
}

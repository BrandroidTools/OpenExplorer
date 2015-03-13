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
 * Parser for a response from Box API containing a list of comments.
 * 
 * @author developers@box.net
 */
public class CommentsResponseParser extends DefaultResponseParser {

    /** The comment currently being parsed. */
    private Comment mComment;
    /** The reply-comment currently being parsed. */
    private Comment mReplyComment;
    /** The list of comments representing the response from Box API. */
    private ArrayList<Comment> mComments;

    /**
     * Enum definition to indicate whether we are currently parsing a comment or a subcomment.
     */
    private enum ParentOrChild {
        /** Indicates that we are processing a parent comment. */
        PARENT,
        /** Indicates that we are processing a child comment. */
        CHILD
    };

    /**
     * Instance of ParentOrChild.
     */
    private ParentOrChild mParentOrChild = ParentOrChild.PARENT;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("comments")) {
            mComments = new ArrayList<Comment>();
        }
        else if (localName.equals("comment")) {
            mParentOrChild = ParentOrChild.PARENT;
            mComment = new Comment();
        }
        else if (localName.equals("item")) {
            mParentOrChild = ParentOrChild.CHILD;
            mReplyComment = new Comment();
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (localName.equals("comment")) {
            if (mComments != null && mComment != null) {
                for (Comment replyComment : mComment.getReplyComments()) {
                    replyComment.setParentCommentId(mComment.getId());
                }
                mComments.add(mComment);
                mComment = null;
                mParentOrChild = null;
            }
        }
        else if (localName.equals("item")) {
            if (mComment != null && mReplyComment != null) {
                mComment.addReplyComment(mReplyComment);
                mReplyComment = null;
                mParentOrChild = ParentOrChild.PARENT;
            }
        }
        else if (mParentOrChild == ParentOrChild.PARENT && mComment != null) {
            mComment.parseAttribute(localName, mTextNode.toString());
        }
        else if (mParentOrChild == ParentOrChild.CHILD && mReplyComment != null) {
            mReplyComment.parseAttribute(localName, mTextNode.toString());
        }
    }

    /**
     * Get the list of comments returned from Box API. Might be null if there was an error.
     * 
     * @return list of Comments.
     */
    public ArrayList<Comment> getComments() {
        return mComments;
    }
}

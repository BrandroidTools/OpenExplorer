/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Warning! This file is generated. Modify at your own risk.
 */

package com.google.api.services.drive.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.DateTime;

/**
 * A JSON representation of a comment on a file in Google Drive.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the Drive API. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-api-java-client/wiki/Json">http://code.google.com/p/google-api-java-client/wiki/Json</a>
 * </p>
 *
 * <p>
 * Upgrade warning: starting with version 1.12 {@code getResponseHeaders()} is removed, instead use
 * {@link com.google.api.client.http.json.JsonHttpRequest#getLastResponseHeaders()}
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Comment extends GenericJson {

  /**
   * A region of the document represented as a JSON string. See anchor documentation for details on
   * how to define and interpret anchor properties.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String anchor;

  /**
   * The user who wrote this comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User author;

  /**
   * The ID of the comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String commentId;

  /**
   * The plain text content used to create this comment. This is not HTML safe and should only be
   * used as a starting point to make edits to a comment's content.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String content;

  /**
   * The context of the file which is being commented on.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Context context;

  /**
   * The date when this comment was first created.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime createdDate;

  /**
   * Whether this comment has been deleted. If a comment has been deleted the content will be
   * cleared and this will only represent a comment that once existed.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean deleted;

  /**
   * The file which this comment is addressing.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String fileId;

  /**
   * The title of the file which this comment is addressing.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String fileTitle;

  /**
   * HTML formatted content for this comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String htmlContent;

  /**
   * This is always drive#comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The date when this comment or any of its replies were last modified.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime modifiedDate;

  /**
   * Replies to this post.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<CommentReply> replies;

  static {
    // hack to force ProGuard to consider CommentReply used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(CommentReply.class);
  }

  /**
   * A link back to this comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * The status of this comment. Status can be changed by posting a reply to a comment with the
   * desired status. - "open" - The comment is still open.  - "resolved" - The comment has been
   * resolved by one of its replies.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String status;

  /**
   * A region of the document represented as a JSON string. See anchor documentation for details on
   * how to define and interpret anchor properties.
   * The value returned may be {@code null}.
   */
  public String getAnchor() {
    return anchor;
  }

  /**
   * A region of the document represented as a JSON string. See anchor documentation for details on
   * how to define and interpret anchor properties.
   * The value set may be {@code null}.
   */
  public Comment setAnchor(String anchor) {
    this.anchor = anchor;
    return this;
  }

  /**
   * The user who wrote this comment.
   * The value returned may be {@code null}.
   */
  public User getAuthor() {
    return author;
  }

  /**
   * The user who wrote this comment.
   * The value set may be {@code null}.
   */
  public Comment setAuthor(User author) {
    this.author = author;
    return this;
  }

  /**
   * The ID of the comment.
   * The value returned may be {@code null}.
   */
  public String getCommentId() {
    return commentId;
  }

  /**
   * The ID of the comment.
   * The value set may be {@code null}.
   */
  public Comment setCommentId(String commentId) {
    this.commentId = commentId;
    return this;
  }

  /**
   * The plain text content used to create this comment. This is not HTML safe and should only be
   * used as a starting point to make edits to a comment's content.
   * The value returned may be {@code null}.
   */
  public String getContent() {
    return content;
  }

  /**
   * The plain text content used to create this comment. This is not HTML safe and should only be
   * used as a starting point to make edits to a comment's content.
   * The value set may be {@code null}.
   */
  public Comment setContent(String content) {
    this.content = content;
    return this;
  }

  /**
   * The context of the file which is being commented on.
   * The value returned may be {@code null}.
   */
  public Context getContext() {
    return context;
  }

  /**
   * The context of the file which is being commented on.
   * The value set may be {@code null}.
   */
  public Comment setContext(Context context) {
    this.context = context;
    return this;
  }

  /**
   * The date when this comment was first created.
   * The value returned may be {@code null}.
   */
  public DateTime getCreatedDate() {
    return createdDate;
  }

  /**
   * The date when this comment was first created.
   * The value set may be {@code null}.
   */
  public Comment setCreatedDate(DateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  /**
   * Whether this comment has been deleted. If a comment has been deleted the content will be
   * cleared and this will only represent a comment that once existed.
   * The value returned may be {@code null}.
   */
  public Boolean getDeleted() {
    return deleted;
  }

  /**
   * Whether this comment has been deleted. If a comment has been deleted the content will be
   * cleared and this will only represent a comment that once existed.
   * The value set may be {@code null}.
   */
  public Comment setDeleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  /**
   * The file which this comment is addressing.
   * The value returned may be {@code null}.
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * The file which this comment is addressing.
   * The value set may be {@code null}.
   */
  public Comment setFileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  /**
   * The title of the file which this comment is addressing.
   * The value returned may be {@code null}.
   */
  public String getFileTitle() {
    return fileTitle;
  }

  /**
   * The title of the file which this comment is addressing.
   * The value set may be {@code null}.
   */
  public Comment setFileTitle(String fileTitle) {
    this.fileTitle = fileTitle;
    return this;
  }

  /**
   * HTML formatted content for this comment.
   * The value returned may be {@code null}.
   */
  public String getHtmlContent() {
    return htmlContent;
  }

  /**
   * HTML formatted content for this comment.
   * The value set may be {@code null}.
   */
  public Comment setHtmlContent(String htmlContent) {
    this.htmlContent = htmlContent;
    return this;
  }

  /**
   * This is always drive#comment.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#comment.
   * The value set may be {@code null}.
   */
  public Comment setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The date when this comment or any of its replies were last modified.
   * The value returned may be {@code null}.
   */
  public DateTime getModifiedDate() {
    return modifiedDate;
  }

  /**
   * The date when this comment or any of its replies were last modified.
   * The value set may be {@code null}.
   */
  public Comment setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  /**
   * Replies to this post.
   * The value returned may be {@code null}.
   */
  public java.util.List<CommentReply> getReplies() {
    return replies;
  }

  /**
   * Replies to this post.
   * The value set may be {@code null}.
   */
  public Comment setReplies(java.util.List<CommentReply> replies) {
    this.replies = replies;
    return this;
  }

  /**
   * A link back to this comment.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this comment.
   * The value set may be {@code null}.
   */
  public Comment setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

  /**
   * The status of this comment. Status can be changed by posting a reply to a comment with the
   * desired status. - "open" - The comment is still open.  - "resolved" - The comment has been
   * resolved by one of its replies.
   * The value returned may be {@code null}.
   */
  public String getStatus() {
    return status;
  }

  /**
   * The status of this comment. Status can be changed by posting a reply to a comment with the
   * desired status. - "open" - The comment is still open.  - "resolved" - The comment has been
   * resolved by one of its replies.
   * The value set may be {@code null}.
   */
  public Comment setStatus(String status) {
    this.status = status;
    return this;
  }

  /**
   * The context of the file which is being commented on.
   */
  public static final class Context extends GenericJson {

    /**
     * The MIME type of the context snippet.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String type;

    /**
     * Data representation of the segment of the file being commented on. In the case of a text file
     * for example, this would be the actual text that the comment is about.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String value;

    /**
     * The MIME type of the context snippet.
     * The value returned may be {@code null}.
     */
    public String getType() {
      return type;
    }

    /**
     * The MIME type of the context snippet.
     * The value set may be {@code null}.
     */
    public Context setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Data representation of the segment of the file being commented on. In the case of a text file
     * for example, this would be the actual text that the comment is about.
     * The value returned may be {@code null}.
     */
    public String getValue() {
      return value;
    }

    /**
     * Data representation of the segment of the file being commented on. In the case of a text file
     * for example, this would be the actual text that the comment is about.
     * The value set may be {@code null}.
     */
    public Context setValue(String value) {
      this.value = value;
      return this;
    }

  }

}

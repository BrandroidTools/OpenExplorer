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
 * A JSON representation of a reply to a comment on a file in Google Drive.
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
public final class CommentReply extends GenericJson {

  /**
   * The user who wrote this reply.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User author;

  /**
   * The plain text content used to create this reply. This is not HTML safe and should only be used
   * as a starting point to make edits to a reply's content. This field is required on inserts if no
   * verb is specified (resolve/reopen).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String content;

  /**
   * The date when this reply was first created.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime createdDate;

  /**
   * Whether this reply has been deleted. If a reply has been deleted the content will be cleared
   * and this will only represent a reply that once existed.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean deleted;

  /**
   * HTML formatted content for this reply.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String htmlContent;

  /**
   * This is always drive#commentReply.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The date when this reply was last modified.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime modifiedDate;

  /**
   * The ID of the reply.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String replyId;

  /**
   * The action this reply performed to the parent comment. When creating a new reply this is the
   * action to be perform to the parent comment. Possible values are: - "resolve" - To resolve a
   * comment.  - "reopen" - To reopen (un-resolve) a comment.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String verb;

  /**
   * The user who wrote this reply.
   * The value returned may be {@code null}.
   */
  public User getAuthor() {
    return author;
  }

  /**
   * The user who wrote this reply.
   * The value set may be {@code null}.
   */
  public CommentReply setAuthor(User author) {
    this.author = author;
    return this;
  }

  /**
   * The plain text content used to create this reply. This is not HTML safe and should only be used
   * as a starting point to make edits to a reply's content. This field is required on inserts if no
   * verb is specified (resolve/reopen).
   * The value returned may be {@code null}.
   */
  public String getContent() {
    return content;
  }

  /**
   * The plain text content used to create this reply. This is not HTML safe and should only be used
   * as a starting point to make edits to a reply's content. This field is required on inserts if no
   * verb is specified (resolve/reopen).
   * The value set may be {@code null}.
   */
  public CommentReply setContent(String content) {
    this.content = content;
    return this;
  }

  /**
   * The date when this reply was first created.
   * The value returned may be {@code null}.
   */
  public DateTime getCreatedDate() {
    return createdDate;
  }

  /**
   * The date when this reply was first created.
   * The value set may be {@code null}.
   */
  public CommentReply setCreatedDate(DateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  /**
   * Whether this reply has been deleted. If a reply has been deleted the content will be cleared
   * and this will only represent a reply that once existed.
   * The value returned may be {@code null}.
   */
  public Boolean getDeleted() {
    return deleted;
  }

  /**
   * Whether this reply has been deleted. If a reply has been deleted the content will be cleared
   * and this will only represent a reply that once existed.
   * The value set may be {@code null}.
   */
  public CommentReply setDeleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  /**
   * HTML formatted content for this reply.
   * The value returned may be {@code null}.
   */
  public String getHtmlContent() {
    return htmlContent;
  }

  /**
   * HTML formatted content for this reply.
   * The value set may be {@code null}.
   */
  public CommentReply setHtmlContent(String htmlContent) {
    this.htmlContent = htmlContent;
    return this;
  }

  /**
   * This is always drive#commentReply.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#commentReply.
   * The value set may be {@code null}.
   */
  public CommentReply setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The date when this reply was last modified.
   * The value returned may be {@code null}.
   */
  public DateTime getModifiedDate() {
    return modifiedDate;
  }

  /**
   * The date when this reply was last modified.
   * The value set may be {@code null}.
   */
  public CommentReply setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  /**
   * The ID of the reply.
   * The value returned may be {@code null}.
   */
  public String getReplyId() {
    return replyId;
  }

  /**
   * The ID of the reply.
   * The value set may be {@code null}.
   */
  public CommentReply setReplyId(String replyId) {
    this.replyId = replyId;
    return this;
  }

  /**
   * The action this reply performed to the parent comment. When creating a new reply this is the
   * action to be perform to the parent comment. Possible values are: - "resolve" - To resolve a
   * comment.  - "reopen" - To reopen (un-resolve) a comment.
   * The value returned may be {@code null}.
   */
  public String getVerb() {
    return verb;
  }

  /**
   * The action this reply performed to the parent comment. When creating a new reply this is the
   * action to be perform to the parent comment. Possible values are: - "resolve" - To resolve a
   * comment.  - "reopen" - To reopen (un-resolve) a comment.
   * The value set may be {@code null}.
   */
  public CommentReply setVerb(String verb) {
    this.verb = verb;
    return this;
  }

}

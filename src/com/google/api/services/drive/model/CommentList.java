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

/**
 * A JSON representation of a list of comments on a file in Google Drive.
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
public final class CommentList extends GenericJson {

  /**
   * List of comments.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Comment> items;

  static {
    // hack to force ProGuard to consider Comment used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(Comment.class);
  }

  /**
   * This is always drive#commentList.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The token to use to request the next page of results.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String nextPageToken;

  /**
   * List of comments.
   * The value returned may be {@code null}.
   */
  public java.util.List<Comment> getItems() {
    return items;
  }

  /**
   * List of comments.
   * The value set may be {@code null}.
   */
  public CommentList setItems(java.util.List<Comment> items) {
    this.items = items;
    return this;
  }

  /**
   * This is always drive#commentList.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#commentList.
   * The value set may be {@code null}.
   */
  public CommentList setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The token to use to request the next page of results.
   * The value returned may be {@code null}.
   */
  public String getNextPageToken() {
    return nextPageToken;
  }

  /**
   * The token to use to request the next page of results.
   * The value set may be {@code null}.
   */
  public CommentList setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
    return this;
  }

}

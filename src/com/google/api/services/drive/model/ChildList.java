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
 * A list of children of a file.
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
public final class ChildList extends GenericJson {

  /**
   * The ETag of the list.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * The actual list of children.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<ChildReference> items;

  static {
    // hack to force ProGuard to consider ChildReference used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(ChildReference.class);
  }

  /**
   * This is always drive#childList.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * A link to the next page of children.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String nextLink;

  /**
   * The page token for the next page of children.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String nextPageToken;

  /**
   * A link back to this list.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * The ETag of the list.
   * The value returned may be {@code null}.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * The ETag of the list.
   * The value set may be {@code null}.
   */
  public ChildList setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * The actual list of children.
   * The value returned may be {@code null}.
   */
  public java.util.List<ChildReference> getItems() {
    return items;
  }

  /**
   * The actual list of children.
   * The value set may be {@code null}.
   */
  public ChildList setItems(java.util.List<ChildReference> items) {
    this.items = items;
    return this;
  }

  /**
   * This is always drive#childList.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#childList.
   * The value set may be {@code null}.
   */
  public ChildList setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * A link to the next page of children.
   * The value returned may be {@code null}.
   */
  public String getNextLink() {
    return nextLink;
  }

  /**
   * A link to the next page of children.
   * The value set may be {@code null}.
   */
  public ChildList setNextLink(String nextLink) {
    this.nextLink = nextLink;
    return this;
  }

  /**
   * The page token for the next page of children.
   * The value returned may be {@code null}.
   */
  public String getNextPageToken() {
    return nextPageToken;
  }

  /**
   * The page token for the next page of children.
   * The value set may be {@code null}.
   */
  public ChildList setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
    return this;
  }

  /**
   * A link back to this list.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this list.
   * The value set may be {@code null}.
   */
  public ChildList setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

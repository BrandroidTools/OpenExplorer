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
import com.google.api.client.json.JsonString;

/**
 * A list of changes for a user.
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
public final class ChangeList extends GenericJson {

  /**
   * The ETag of the list.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * The actual list of changes.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Change> items;

  static {
    // hack to force ProGuard to consider Change used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(Change.class);
  }

  /**
   * This is always drive#changeList.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The current largest change ID.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long largestChangeId;

  /**
   * A link to the next page of changes.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String nextLink;

  /**
   * The page token for the next page of changes.
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
  public ChangeList setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * The actual list of changes.
   * The value returned may be {@code null}.
   */
  public java.util.List<Change> getItems() {
    return items;
  }

  /**
   * The actual list of changes.
   * The value set may be {@code null}.
   */
  public ChangeList setItems(java.util.List<Change> items) {
    this.items = items;
    return this;
  }

  /**
   * This is always drive#changeList.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#changeList.
   * The value set may be {@code null}.
   */
  public ChangeList setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The current largest change ID.
   * The value returned may be {@code null}.
   */
  public Long getLargestChangeId() {
    return largestChangeId;
  }

  /**
   * The current largest change ID.
   * The value set may be {@code null}.
   */
  public ChangeList setLargestChangeId(Long largestChangeId) {
    this.largestChangeId = largestChangeId;
    return this;
  }

  /**
   * A link to the next page of changes.
   * The value returned may be {@code null}.
   */
  public String getNextLink() {
    return nextLink;
  }

  /**
   * A link to the next page of changes.
   * The value set may be {@code null}.
   */
  public ChangeList setNextLink(String nextLink) {
    this.nextLink = nextLink;
    return this;
  }

  /**
   * The page token for the next page of changes.
   * The value returned may be {@code null}.
   */
  public String getNextPageToken() {
    return nextPageToken;
  }

  /**
   * The page token for the next page of changes.
   * The value set may be {@code null}.
   */
  public ChangeList setNextPageToken(String nextPageToken) {
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
  public ChangeList setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

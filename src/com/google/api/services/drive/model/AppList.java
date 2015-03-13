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
 * A list of third-party applications which the user has installed or given access to Google Drive.
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
public final class AppList extends GenericJson {

  /**
   * The ETag of the list.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * The actual list of apps.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<App> items;

  static {
    // hack to force ProGuard to consider App used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(App.class);
  }

  /**
   * This is always drive#appList.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

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
  public AppList setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * The actual list of apps.
   * The value returned may be {@code null}.
   */
  public java.util.List<App> getItems() {
    return items;
  }

  /**
   * The actual list of apps.
   * The value set may be {@code null}.
   */
  public AppList setItems(java.util.List<App> items) {
    this.items = items;
    return this;
  }

  /**
   * This is always drive#appList.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#appList.
   * The value set may be {@code null}.
   */
  public AppList setKind(String kind) {
    this.kind = kind;
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
  public AppList setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

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
 * A reference to a file's parent.
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
public final class ParentReference extends GenericJson {

  /**
   * The ID of the parent.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String id;

  /**
   * Whether or not the parent is the root folder.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean isRoot;

  /**
   * This is always drive#parentReference.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * A link to the parent.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String parentLink;

  /**
   * A link back to this reference.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * The ID of the parent.
   * The value returned may be {@code null}.
   */
  public String getId() {
    return id;
  }

  /**
   * The ID of the parent.
   * The value set may be {@code null}.
   */
  public ParentReference setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Whether or not the parent is the root folder.
   * The value returned may be {@code null}.
   */
  public Boolean getIsRoot() {
    return isRoot;
  }

  /**
   * Whether or not the parent is the root folder.
   * The value set may be {@code null}.
   */
  public ParentReference setIsRoot(Boolean isRoot) {
    this.isRoot = isRoot;
    return this;
  }

  /**
   * This is always drive#parentReference.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#parentReference.
   * The value set may be {@code null}.
   */
  public ParentReference setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * A link to the parent.
   * The value returned may be {@code null}.
   */
  public String getParentLink() {
    return parentLink;
  }

  /**
   * A link to the parent.
   * The value set may be {@code null}.
   */
  public ParentReference setParentLink(String parentLink) {
    this.parentLink = parentLink;
    return this;
  }

  /**
   * A link back to this reference.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this reference.
   * The value set may be {@code null}.
   */
  public ParentReference setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

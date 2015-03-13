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
 * A permission for a file.
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
public final class Permission extends GenericJson {

  /**
   * Additional roles for this user. Only commenter is currently allowed.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> additionalRoles;

  /**
   * The authkey parameter required for this permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String authKey;

  /**
   * The ETag of the permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * The ID of the permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String id;

  /**
   * This is always drive#permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The name for this permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String name;

  /**
   * A link to the profile photo, if available.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String photoLink;

  /**
   * The primary role for this user. Allowed values are: - owner  - reader  - writer
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String role;

  /**
   * A link back to this permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * The account type. Allowed values are: - user  - group  - domain  - anyone
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String type;

  /**
   * The email address or domain name for the entity. This is not populated in responses.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String value;

  /**
   * Whether the link is required for this permission.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean withLink;

  /**
   * Additional roles for this user. Only commenter is currently allowed.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getAdditionalRoles() {
    return additionalRoles;
  }

  /**
   * Additional roles for this user. Only commenter is currently allowed.
   * The value set may be {@code null}.
   */
  public Permission setAdditionalRoles(java.util.List<String> additionalRoles) {
    this.additionalRoles = additionalRoles;
    return this;
  }

  /**
   * The authkey parameter required for this permission.
   * The value returned may be {@code null}.
   */
  public String getAuthKey() {
    return authKey;
  }

  /**
   * The authkey parameter required for this permission.
   * The value set may be {@code null}.
   */
  public Permission setAuthKey(String authKey) {
    this.authKey = authKey;
    return this;
  }

  /**
   * The ETag of the permission.
   * The value returned may be {@code null}.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * The ETag of the permission.
   * The value set may be {@code null}.
   */
  public Permission setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * The ID of the permission.
   * The value returned may be {@code null}.
   */
  public String getId() {
    return id;
  }

  /**
   * The ID of the permission.
   * The value set may be {@code null}.
   */
  public Permission setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * This is always drive#permission.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#permission.
   * The value set may be {@code null}.
   */
  public Permission setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The name for this permission.
   * The value returned may be {@code null}.
   */
  public String getName() {
    return name;
  }

  /**
   * The name for this permission.
   * The value set may be {@code null}.
   */
  public Permission setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * A link to the profile photo, if available.
   * The value returned may be {@code null}.
   */
  public String getPhotoLink() {
    return photoLink;
  }

  /**
   * A link to the profile photo, if available.
   * The value set may be {@code null}.
   */
  public Permission setPhotoLink(String photoLink) {
    this.photoLink = photoLink;
    return this;
  }

  /**
   * The primary role for this user. Allowed values are: - owner  - reader  - writer
   * The value returned may be {@code null}.
   */
  public String getRole() {
    return role;
  }

  /**
   * The primary role for this user. Allowed values are: - owner  - reader  - writer
   * The value set may be {@code null}.
   */
  public Permission setRole(String role) {
    this.role = role;
    return this;
  }

  /**
   * A link back to this permission.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this permission.
   * The value set may be {@code null}.
   */
  public Permission setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

  /**
   * The account type. Allowed values are: - user  - group  - domain  - anyone
   * The value returned may be {@code null}.
   */
  public String getType() {
    return type;
  }

  /**
   * The account type. Allowed values are: - user  - group  - domain  - anyone
   * The value set may be {@code null}.
   */
  public Permission setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * The email address or domain name for the entity. This is not populated in responses.
   * The value returned may be {@code null}.
   */
  public String getValue() {
    return value;
  }

  /**
   * The email address or domain name for the entity. This is not populated in responses.
   * The value set may be {@code null}.
   */
  public Permission setValue(String value) {
    this.value = value;
    return this;
  }

  /**
   * Whether the link is required for this permission.
   * The value returned may be {@code null}.
   */
  public Boolean getWithLink() {
    return withLink;
  }

  /**
   * Whether the link is required for this permission.
   * The value set may be {@code null}.
   */
  public Permission setWithLink(Boolean withLink) {
    this.withLink = withLink;
    return this;
  }

}

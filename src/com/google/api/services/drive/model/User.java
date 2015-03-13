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
 * The JSON template for a user.
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
public final class User extends GenericJson {

  /**
   * A plain text displayable name for this user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String displayName;

  /**
   * Whether this user is the same as the authenticated user for whom the request was made.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean isAuthenticatedUser;

  /**
   * This is always drive#user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The user's ID as visible in the permissions collection.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String permissionId;

  /**
   * The user's profile picture.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Picture picture;

  /**
   * A plain text displayable name for this user.
   * The value returned may be {@code null}.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * A plain text displayable name for this user.
   * The value set may be {@code null}.
   */
  public User setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Whether this user is the same as the authenticated user for whom the request was made.
   * The value returned may be {@code null}.
   */
  public Boolean getIsAuthenticatedUser() {
    return isAuthenticatedUser;
  }

  /**
   * Whether this user is the same as the authenticated user for whom the request was made.
   * The value set may be {@code null}.
   */
  public User setIsAuthenticatedUser(Boolean isAuthenticatedUser) {
    this.isAuthenticatedUser = isAuthenticatedUser;
    return this;
  }

  /**
   * This is always drive#user.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#user.
   * The value set may be {@code null}.
   */
  public User setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The user's ID as visible in the permissions collection.
   * The value returned may be {@code null}.
   */
  public String getPermissionId() {
    return permissionId;
  }

  /**
   * The user's ID as visible in the permissions collection.
   * The value set may be {@code null}.
   */
  public User setPermissionId(String permissionId) {
    this.permissionId = permissionId;
    return this;
  }

  /**
   * The user's profile picture.
   * The value returned may be {@code null}.
   */
  public Picture getPicture() {
    return picture;
  }

  /**
   * The user's profile picture.
   * The value set may be {@code null}.
   */
  public User setPicture(Picture picture) {
    this.picture = picture;
    return this;
  }

  /**
   * The user's profile picture.
   */
  public static final class Picture extends GenericJson {

    /**
     * A URL that points to a profile picture of this user.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String url;

    /**
     * A URL that points to a profile picture of this user.
     * The value returned may be {@code null}.
     */
    public String getUrl() {
      return url;
    }

    /**
     * A URL that points to a profile picture of this user.
     * The value set may be {@code null}.
     */
    public Picture setUrl(String url) {
      this.url = url;
      return this;
    }

  }
}

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
 * The apps resource provides a list of the apps that a user has installed, with information about
 * each app's supported MIME types, file extensions, and other details.
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
public final class App extends GenericJson {

  /**
   * Whether the app is authorized to access data on the user's Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean authorized;

  /**
   * The various icons for the app.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Icons> icons;

  static {
    // hack to force ProGuard to consider Icons used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(Icons.class);
  }

  /**
   * The ID of the app.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String id;

  /**
   * Whether the app is installed.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean installed;

  /**
   * This is always drive#app.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The name of the app.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String name;

  /**
   * The type of object this app creates (e.g. Chart). If empty, the app name should be used
   * instead.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String objectType;

  /**
   * The list of primary file extensions.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> primaryFileExtensions;

  /**
   * The list of primary mime types.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> primaryMimeTypes;

  /**
   * The product URL.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String productUrl;

  /**
   * The list of secondary file extensions.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> secondaryFileExtensions;

  /**
   * The list of secondary mime types.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> secondaryMimeTypes;

  /**
   * Whether this app supports creating new objects.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean supportsCreate;

  /**
   * Whether this app supports importing Google Docs.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean supportsImport;

  /**
   * Whether the app is selected as the default handler for the types it supports.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean useByDefault;

  /**
   * Whether the app is authorized to access data on the user's Drive.
   * The value returned may be {@code null}.
   */
  public Boolean getAuthorized() {
    return authorized;
  }

  /**
   * Whether the app is authorized to access data on the user's Drive.
   * The value set may be {@code null}.
   */
  public App setAuthorized(Boolean authorized) {
    this.authorized = authorized;
    return this;
  }

  /**
   * The various icons for the app.
   * The value returned may be {@code null}.
   */
  public java.util.List<Icons> getIcons() {
    return icons;
  }

  /**
   * The various icons for the app.
   * The value set may be {@code null}.
   */
  public App setIcons(java.util.List<Icons> icons) {
    this.icons = icons;
    return this;
  }

  /**
   * The ID of the app.
   * The value returned may be {@code null}.
   */
  public String getId() {
    return id;
  }

  /**
   * The ID of the app.
   * The value set may be {@code null}.
   */
  public App setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Whether the app is installed.
   * The value returned may be {@code null}.
   */
  public Boolean getInstalled() {
    return installed;
  }

  /**
   * Whether the app is installed.
   * The value set may be {@code null}.
   */
  public App setInstalled(Boolean installed) {
    this.installed = installed;
    return this;
  }

  /**
   * This is always drive#app.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#app.
   * The value set may be {@code null}.
   */
  public App setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The name of the app.
   * The value returned may be {@code null}.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the app.
   * The value set may be {@code null}.
   */
  public App setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The type of object this app creates (e.g. Chart). If empty, the app name should be used
   * instead.
   * The value returned may be {@code null}.
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * The type of object this app creates (e.g. Chart). If empty, the app name should be used
   * instead.
   * The value set may be {@code null}.
   */
  public App setObjectType(String objectType) {
    this.objectType = objectType;
    return this;
  }

  /**
   * The list of primary file extensions.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getPrimaryFileExtensions() {
    return primaryFileExtensions;
  }

  /**
   * The list of primary file extensions.
   * The value set may be {@code null}.
   */
  public App setPrimaryFileExtensions(java.util.List<String> primaryFileExtensions) {
    this.primaryFileExtensions = primaryFileExtensions;
    return this;
  }

  /**
   * The list of primary mime types.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getPrimaryMimeTypes() {
    return primaryMimeTypes;
  }

  /**
   * The list of primary mime types.
   * The value set may be {@code null}.
   */
  public App setPrimaryMimeTypes(java.util.List<String> primaryMimeTypes) {
    this.primaryMimeTypes = primaryMimeTypes;
    return this;
  }

  /**
   * The product URL.
   * The value returned may be {@code null}.
   */
  public String getProductUrl() {
    return productUrl;
  }

  /**
   * The product URL.
   * The value set may be {@code null}.
   */
  public App setProductUrl(String productUrl) {
    this.productUrl = productUrl;
    return this;
  }

  /**
   * The list of secondary file extensions.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getSecondaryFileExtensions() {
    return secondaryFileExtensions;
  }

  /**
   * The list of secondary file extensions.
   * The value set may be {@code null}.
   */
  public App setSecondaryFileExtensions(java.util.List<String> secondaryFileExtensions) {
    this.secondaryFileExtensions = secondaryFileExtensions;
    return this;
  }

  /**
   * The list of secondary mime types.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getSecondaryMimeTypes() {
    return secondaryMimeTypes;
  }

  /**
   * The list of secondary mime types.
   * The value set may be {@code null}.
   */
  public App setSecondaryMimeTypes(java.util.List<String> secondaryMimeTypes) {
    this.secondaryMimeTypes = secondaryMimeTypes;
    return this;
  }

  /**
   * Whether this app supports creating new objects.
   * The value returned may be {@code null}.
   */
  public Boolean getSupportsCreate() {
    return supportsCreate;
  }

  /**
   * Whether this app supports creating new objects.
   * The value set may be {@code null}.
   */
  public App setSupportsCreate(Boolean supportsCreate) {
    this.supportsCreate = supportsCreate;
    return this;
  }

  /**
   * Whether this app supports importing Google Docs.
   * The value returned may be {@code null}.
   */
  public Boolean getSupportsImport() {
    return supportsImport;
  }

  /**
   * Whether this app supports importing Google Docs.
   * The value set may be {@code null}.
   */
  public App setSupportsImport(Boolean supportsImport) {
    this.supportsImport = supportsImport;
    return this;
  }

  /**
   * Whether the app is selected as the default handler for the types it supports.
   * The value returned may be {@code null}.
   */
  public Boolean getUseByDefault() {
    return useByDefault;
  }

  /**
   * Whether the app is selected as the default handler for the types it supports.
   * The value set may be {@code null}.
   */
  public App setUseByDefault(Boolean useByDefault) {
    this.useByDefault = useByDefault;
    return this;
  }

  /**
   * Model definition for AppIcons.
   */
  public static final class Icons extends GenericJson {

    /**
     * Category of the icon. Allowed values are: - application - icon for the application  - document
     * - icon for a file associated with the app  - documentShared - icon for a shared file associated
     * with the app
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String category;

    /**
     * URL for the icon.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String iconUrl;

    /**
     * Size of the icon. Represented as the maximum of the width and height.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer size;

    /**
     * Category of the icon. Allowed values are: - application - icon for the application  - document
     * - icon for a file associated with the app  - documentShared - icon for a shared file associated
     * with the app
     * The value returned may be {@code null}.
     */
    public String getCategory() {
      return category;
    }

    /**
     * Category of the icon. Allowed values are: - application - icon for the application  - document
     * - icon for a file associated with the app  - documentShared - icon for a shared file associated
     * with the app
     * The value set may be {@code null}.
     */
    public Icons setCategory(String category) {
      this.category = category;
      return this;
    }

    /**
     * URL for the icon.
     * The value returned may be {@code null}.
     */
    public String getIconUrl() {
      return iconUrl;
    }

    /**
     * URL for the icon.
     * The value set may be {@code null}.
     */
    public Icons setIconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
      return this;
    }

    /**
     * Size of the icon. Represented as the maximum of the width and height.
     * The value returned may be {@code null}.
     */
    public Integer getSize() {
      return size;
    }

    /**
     * Size of the icon. Represented as the maximum of the width and height.
     * The value set may be {@code null}.
     */
    public Icons setSize(Integer size) {
      this.size = size;
      return this;
    }

  }

}

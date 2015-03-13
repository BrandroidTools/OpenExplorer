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
 * An item with user information and settings.
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
public final class About extends GenericJson {

  /**
   * Information about supported additional roles per file type. The most specific type takes
   * precedence.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<AdditionalRoleInfo> additionalRoleInfo;

  static {
    // hack to force ProGuard to consider AdditionalRoleInfo used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(AdditionalRoleInfo.class);
  }

  /**
   * The domain sharing policy for the current user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String domainSharingPolicy;

  /**
   * The ETag of the item.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * The allowable export formats.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<ExportFormats> exportFormats;

  static {
    // hack to force ProGuard to consider ExportFormats used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(ExportFormats.class);
  }

  /**
   * List of additional features enabled on this account.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Features> features;

  static {
    // hack to force ProGuard to consider Features used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(Features.class);
  }

  /**
   * The allowable import formats.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<ImportFormats> importFormats;

  static {
    // hack to force ProGuard to consider ImportFormats used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(ImportFormats.class);
  }

  /**
   * A boolean indicating whether the authenticated app is installed by the authenticated user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean isCurrentAppInstalled;

  /**
   * This is always drive#about.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The largest change id.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long largestChangeId;

  /**
   * List of max upload sizes for each file type. The most specific type takes precedence.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<MaxUploadSizes> maxUploadSizes;

  static {
    // hack to force ProGuard to consider MaxUploadSizes used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(MaxUploadSizes.class);
  }

  /**
   * The name of the current user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String name;

  /**
   * The current user's ID as visible in the permissions collection.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String permissionId;

  /**
   * The total number of quota bytes.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long quotaBytesTotal;

  /**
   * The number of quota bytes used by Google Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long quotaBytesUsed;

  /**
   * The number of quota bytes used by all Google apps (Drive, Picasa, etc.).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long quotaBytesUsedAggregate;

  /**
   * The number of quota bytes used by trashed items.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long quotaBytesUsedInTrash;

  /**
   * The number of remaining change ids.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long remainingChangeIds;

  /**
   * The id of the root folder.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String rootFolderId;

  /**
   * A link back to this item.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * The authenticated user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User user;

  /**
   * Information about supported additional roles per file type. The most specific type takes
   * precedence.
   * The value returned may be {@code null}.
   */
  public java.util.List<AdditionalRoleInfo> getAdditionalRoleInfo() {
    return additionalRoleInfo;
  }

  /**
   * Information about supported additional roles per file type. The most specific type takes
   * precedence.
   * The value set may be {@code null}.
   */
  public About setAdditionalRoleInfo(java.util.List<AdditionalRoleInfo> additionalRoleInfo) {
    this.additionalRoleInfo = additionalRoleInfo;
    return this;
  }

  /**
   * The domain sharing policy for the current user.
   * The value returned may be {@code null}.
   */
  public String getDomainSharingPolicy() {
    return domainSharingPolicy;
  }

  /**
   * The domain sharing policy for the current user.
   * The value set may be {@code null}.
   */
  public About setDomainSharingPolicy(String domainSharingPolicy) {
    this.domainSharingPolicy = domainSharingPolicy;
    return this;
  }

  /**
   * The ETag of the item.
   * The value returned may be {@code null}.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * The ETag of the item.
   * The value set may be {@code null}.
   */
  public About setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * The allowable export formats.
   * The value returned may be {@code null}.
   */
  public java.util.List<ExportFormats> getExportFormats() {
    return exportFormats;
  }

  /**
   * The allowable export formats.
   * The value set may be {@code null}.
   */
  public About setExportFormats(java.util.List<ExportFormats> exportFormats) {
    this.exportFormats = exportFormats;
    return this;
  }

  /**
   * List of additional features enabled on this account.
   * The value returned may be {@code null}.
   */
  public java.util.List<Features> getFeatures() {
    return features;
  }

  /**
   * List of additional features enabled on this account.
   * The value set may be {@code null}.
   */
  public About setFeatures(java.util.List<Features> features) {
    this.features = features;
    return this;
  }

  /**
   * The allowable import formats.
   * The value returned may be {@code null}.
   */
  public java.util.List<ImportFormats> getImportFormats() {
    return importFormats;
  }

  /**
   * The allowable import formats.
   * The value set may be {@code null}.
   */
  public About setImportFormats(java.util.List<ImportFormats> importFormats) {
    this.importFormats = importFormats;
    return this;
  }

  /**
   * A boolean indicating whether the authenticated app is installed by the authenticated user.
   * The value returned may be {@code null}.
   */
  public Boolean getIsCurrentAppInstalled() {
    return isCurrentAppInstalled;
  }

  /**
   * A boolean indicating whether the authenticated app is installed by the authenticated user.
   * The value set may be {@code null}.
   */
  public About setIsCurrentAppInstalled(Boolean isCurrentAppInstalled) {
    this.isCurrentAppInstalled = isCurrentAppInstalled;
    return this;
  }

  /**
   * This is always drive#about.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#about.
   * The value set may be {@code null}.
   */
  public About setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The largest change id.
   * The value returned may be {@code null}.
   */
  public Long getLargestChangeId() {
    return largestChangeId;
  }

  /**
   * The largest change id.
   * The value set may be {@code null}.
   */
  public About setLargestChangeId(Long largestChangeId) {
    this.largestChangeId = largestChangeId;
    return this;
  }

  /**
   * List of max upload sizes for each file type. The most specific type takes precedence.
   * The value returned may be {@code null}.
   */
  public java.util.List<MaxUploadSizes> getMaxUploadSizes() {
    return maxUploadSizes;
  }

  /**
   * List of max upload sizes for each file type. The most specific type takes precedence.
   * The value set may be {@code null}.
   */
  public About setMaxUploadSizes(java.util.List<MaxUploadSizes> maxUploadSizes) {
    this.maxUploadSizes = maxUploadSizes;
    return this;
  }

  /**
   * The name of the current user.
   * The value returned may be {@code null}.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the current user.
   * The value set may be {@code null}.
   */
  public About setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The current user's ID as visible in the permissions collection.
   * The value returned may be {@code null}.
   */
  public String getPermissionId() {
    return permissionId;
  }

  /**
   * The current user's ID as visible in the permissions collection.
   * The value set may be {@code null}.
   */
  public About setPermissionId(String permissionId) {
    this.permissionId = permissionId;
    return this;
  }

  /**
   * The total number of quota bytes.
   * The value returned may be {@code null}.
   */
  public Long getQuotaBytesTotal() {
    return quotaBytesTotal;
  }

  /**
   * The total number of quota bytes.
   * The value set may be {@code null}.
   */
  public About setQuotaBytesTotal(Long quotaBytesTotal) {
    this.quotaBytesTotal = quotaBytesTotal;
    return this;
  }

  /**
   * The number of quota bytes used by Google Drive.
   * The value returned may be {@code null}.
   */
  public Long getQuotaBytesUsed() {
    return quotaBytesUsed;
  }

  /**
   * The number of quota bytes used by Google Drive.
   * The value set may be {@code null}.
   */
  public About setQuotaBytesUsed(Long quotaBytesUsed) {
    this.quotaBytesUsed = quotaBytesUsed;
    return this;
  }

  /**
   * The number of quota bytes used by all Google apps (Drive, Picasa, etc.).
   * The value returned may be {@code null}.
   */
  public Long getQuotaBytesUsedAggregate() {
    return quotaBytesUsedAggregate;
  }

  /**
   * The number of quota bytes used by all Google apps (Drive, Picasa, etc.).
   * The value set may be {@code null}.
   */
  public About setQuotaBytesUsedAggregate(Long quotaBytesUsedAggregate) {
    this.quotaBytesUsedAggregate = quotaBytesUsedAggregate;
    return this;
  }

  /**
   * The number of quota bytes used by trashed items.
   * The value returned may be {@code null}.
   */
  public Long getQuotaBytesUsedInTrash() {
    return quotaBytesUsedInTrash;
  }

  /**
   * The number of quota bytes used by trashed items.
   * The value set may be {@code null}.
   */
  public About setQuotaBytesUsedInTrash(Long quotaBytesUsedInTrash) {
    this.quotaBytesUsedInTrash = quotaBytesUsedInTrash;
    return this;
  }

  /**
   * The number of remaining change ids.
   * The value returned may be {@code null}.
   */
  public Long getRemainingChangeIds() {
    return remainingChangeIds;
  }

  /**
   * The number of remaining change ids.
   * The value set may be {@code null}.
   */
  public About setRemainingChangeIds(Long remainingChangeIds) {
    this.remainingChangeIds = remainingChangeIds;
    return this;
  }

  /**
   * The id of the root folder.
   * The value returned may be {@code null}.
   */
  public String getRootFolderId() {
    return rootFolderId;
  }

  /**
   * The id of the root folder.
   * The value set may be {@code null}.
   */
  public About setRootFolderId(String rootFolderId) {
    this.rootFolderId = rootFolderId;
    return this;
  }

  /**
   * A link back to this item.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this item.
   * The value set may be {@code null}.
   */
  public About setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

  /**
   * The authenticated user.
   * The value returned may be {@code null}.
   */
  public User getUser() {
    return user;
  }

  /**
   * The authenticated user.
   * The value set may be {@code null}.
   */
  public About setUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * Model definition for AboutAdditionalRoleInfo.
   */
  public static final class AdditionalRoleInfo extends GenericJson {

    /**
     * The supported additional roles per primary role.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.util.List<RoleSets> roleSets;

    static {
      // hack to force ProGuard to consider RoleSets used, since otherwise it would be stripped out
      // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
      com.google.api.client.util.Data.nullOf(RoleSets.class);
    }

    /**
     * The content type that this additional role info applies to.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String type;

    /**
     * The supported additional roles per primary role.
     * The value returned may be {@code null}.
     */
    public java.util.List<RoleSets> getRoleSets() {
      return roleSets;
    }

    /**
     * The supported additional roles per primary role.
     * The value set may be {@code null}.
     */
    public AdditionalRoleInfo setRoleSets(java.util.List<RoleSets> roleSets) {
      this.roleSets = roleSets;
      return this;
    }

    /**
     * The content type that this additional role info applies to.
     * The value returned may be {@code null}.
     */
    public String getType() {
      return type;
    }

    /**
     * The content type that this additional role info applies to.
     * The value set may be {@code null}.
     */
    public AdditionalRoleInfo setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Model definition for AboutAdditionalRoleInfoRoleSets.
     */
    public static final class RoleSets extends GenericJson {

      /**
       * The supported additional roles with the primary role.
       * The value may be {@code null}.
       */
      @com.google.api.client.util.Key
      private java.util.List<String> additionalRoles;

      /**
       * A primary permission role.
       * The value may be {@code null}.
       */
      @com.google.api.client.util.Key
      private String primaryRole;

      /**
       * The supported additional roles with the primary role.
       * The value returned may be {@code null}.
       */
      public java.util.List<String> getAdditionalRoles() {
        return additionalRoles;
      }

      /**
       * The supported additional roles with the primary role.
       * The value set may be {@code null}.
       */
      public RoleSets setAdditionalRoles(java.util.List<String> additionalRoles) {
        this.additionalRoles = additionalRoles;
        return this;
      }

      /**
       * A primary permission role.
       * The value returned may be {@code null}.
       */
      public String getPrimaryRole() {
        return primaryRole;
      }

      /**
       * A primary permission role.
       * The value set may be {@code null}.
       */
      public RoleSets setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
        return this;
      }

    }
  }

  /**
   * Model definition for AboutExportFormats.
   */
  public static final class ExportFormats extends GenericJson {

    /**
     * The content type to convert from.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String source;

    /**
     * The possible content types to convert to.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.util.List<String> targets;

    /**
     * The content type to convert from.
     * The value returned may be {@code null}.
     */
    public String getSource() {
      return source;
    }

    /**
     * The content type to convert from.
     * The value set may be {@code null}.
     */
    public ExportFormats setSource(String source) {
      this.source = source;
      return this;
    }

    /**
     * The possible content types to convert to.
     * The value returned may be {@code null}.
     */
    public java.util.List<String> getTargets() {
      return targets;
    }

    /**
     * The possible content types to convert to.
     * The value set may be {@code null}.
     */
    public ExportFormats setTargets(java.util.List<String> targets) {
      this.targets = targets;
      return this;
    }

  }

  /**
   * Model definition for AboutFeatures.
   */
  public static final class Features extends GenericJson {

    /**
     * The name of the feature.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String featureName;

    /**
     * The request limit rate for this feature, in queries per second.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Double featureRate;

    /**
     * The name of the feature.
     * The value returned may be {@code null}.
     */
    public String getFeatureName() {
      return featureName;
    }

    /**
     * The name of the feature.
     * The value set may be {@code null}.
     */
    public Features setFeatureName(String featureName) {
      this.featureName = featureName;
      return this;
    }

    /**
     * The request limit rate for this feature, in queries per second.
     * The value returned may be {@code null}.
     */
    public Double getFeatureRate() {
      return featureRate;
    }

    /**
     * The request limit rate for this feature, in queries per second.
     * The value set may be {@code null}.
     */
    public Features setFeatureRate(Double featureRate) {
      this.featureRate = featureRate;
      return this;
    }

  }

  /**
   * Model definition for AboutImportFormats.
   */
  public static final class ImportFormats extends GenericJson {

    /**
     * The imported file's content type to convert from.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String source;

    /**
     * The possible content types to convert to.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.util.List<String> targets;

    /**
     * The imported file's content type to convert from.
     * The value returned may be {@code null}.
     */
    public String getSource() {
      return source;
    }

    /**
     * The imported file's content type to convert from.
     * The value set may be {@code null}.
     */
    public ImportFormats setSource(String source) {
      this.source = source;
      return this;
    }

    /**
     * The possible content types to convert to.
     * The value returned may be {@code null}.
     */
    public java.util.List<String> getTargets() {
      return targets;
    }

    /**
     * The possible content types to convert to.
     * The value set may be {@code null}.
     */
    public ImportFormats setTargets(java.util.List<String> targets) {
      this.targets = targets;
      return this;
    }

  }

  /**
   * Model definition for AboutMaxUploadSizes.
   */
  public static final class MaxUploadSizes extends GenericJson {

    /**
     * The max upload size for this type.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key @JsonString
    private Long size;

    /**
     * The file type.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String type;

    /**
     * The max upload size for this type.
     * The value returned may be {@code null}.
     */
    public Long getSize() {
      return size;
    }

    /**
     * The max upload size for this type.
     * The value set may be {@code null}.
     */
    public MaxUploadSizes setSize(Long size) {
      this.size = size;
      return this;
    }

    /**
     * The file type.
     * The value returned may be {@code null}.
     */
    public String getType() {
      return type;
    }

    /**
     * The file type.
     * The value set may be {@code null}.
     */
    public MaxUploadSizes setType(String type) {
      this.type = type;
      return this;
    }

  }

}

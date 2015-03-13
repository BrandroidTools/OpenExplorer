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
import com.google.api.client.util.DateTime;

/**
 * The metadata for a file.
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
public final class File extends GenericJson {

  /**
   * A link for opening the file in using a relevant Google editor or viewer.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String alternateLink;

  /**
   * Create time for this file (formatted ISO8601 timestamp).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime createdDate;

  /**
   * A short description of the file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String description;

  /**
   * Short lived download URL for the file. This is only populated for files with content stored in
   * Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String downloadUrl;

  /**
   * Whether the file can be edited by the current user.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean editable;

  /**
   * A link for embedding the file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String embedLink;

  /**
   * ETag of the file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * Whether this file has been explicitly trashed, as opposed to recursively trashed. This will
   * only be populated if the file is trashed.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean explicitlyTrashed;

  /**
   * Links for exporting Google Docs to specific formats.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.Map<String, String> exportLinks;

  /**
   * The file extension used when downloading this file. This field is read only. To set the
   * extension, include it in the title when creating the file. This is only populated for files
   * with content stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String fileExtension;

  /**
   * The size of the file in bytes. This is only populated for files with content stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long fileSize;

  /**
   * A link to the file's icon.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String iconLink;

  /**
   * The ID of the file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String id;

  /**
   * Metadata about image media. This will only be present for image types, and its contents will
   * depend on what can be parsed from the image content.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private ImageMediaMetadata imageMediaMetadata;

  /**
   * Indexable text attributes for the file (can only be written)
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private IndexableText indexableText;

  /**
   * The type of file. This is always drive#file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * A group of labels for the file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Labels labels;

  /**
   * The last user to modify this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User lastModifyingUser;

  /**
   * Name of the last user to modify this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String lastModifyingUserName;

  /**
   * Last time this file was viewed by the user (formatted RFC 3339 timestamp).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime lastViewedByMeDate;

  /**
   * An MD5 checksum for the content of this file. This is populated only for files with content
   * stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String md5Checksum;

  /**
   * The MIME type of the file. This is only mutable on update when uploading new content. This
   * field can be left blank, and the mimetype will be determined from the uploaded content's MIME
   * type.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String mimeType;

  /**
   * Last time this file was modified by the user (formatted RFC 3339 timestamp). Note that setting
   * modifiedDate will also update the modifiedByMe date for the user which set the date.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime modifiedByMeDate;

  /**
   * Last time this file was modified by anyone (formatted RFC 3339 timestamp). This is only mutable
   * on update when the setModifiedDate parameter is set.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime modifiedDate;

  /**
   * The original filename if the file was uploaded manually, or the original title if the file was
   * inserted through the API. Note that renames of the title will not change the original filename.
   * This will only be populated on files with content stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String originalFilename;

  /**
   * Name(s) of the owner(s) of this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<String> ownerNames;

  /**
   * The owner(s) of this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<User> owners;

  static {
    // hack to force ProGuard to consider User used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(User.class);
  }

  /**
   * Collection of parent folders which contain this file. Setting this field will put the file in
   * all of the provided folders. On insert, if no folders are provided, the file will be placed in
   * the default root folder.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<ParentReference> parents;

  static {
    // hack to force ProGuard to consider ParentReference used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(ParentReference.class);
  }

  /**
   * The number of quota bytes used by this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long quotaBytesUsed;

  /**
   * A link back to this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * Whether the file has been shared.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean shared;

  /**
   * Time at which this file was shared with the user (formatted RFC 3339 timestamp).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime sharedWithMeDate;

  /**
   * Thumbnail for the file. Only accepted on upload and for files that are not already thumbnailed
   * by Google.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Thumbnail thumbnail;

  /**
   * A link to the file's thumbnail.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String thumbnailLink;

  /**
   * The title of this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String title;

  /**
   * The permissions for the authenticated user on this file.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Permission userPermission;

  /**
   * A link for downloading the content of the file in a browser using cookie based authentication.
   * In cases where the content is shared publicly, the content can be downloaded without any
   * credentials.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String webContentLink;

  /**
   * A link only available on public folders for viewing their static web assets (HTML, CSS, JS,
   * etc) via Google Drive's Website Hosting.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String webViewLink;

  /**
   * Whether writers can share the document with other users.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean writersCanShare;

  /**
   * A link for opening the file in using a relevant Google editor or viewer.
   * The value returned may be {@code null}.
   */
  public String getAlternateLink() {
    return alternateLink;
  }

  /**
   * A link for opening the file in using a relevant Google editor or viewer.
   * The value set may be {@code null}.
   */
  public File setAlternateLink(String alternateLink) {
    this.alternateLink = alternateLink;
    return this;
  }

  /**
   * Create time for this file (formatted ISO8601 timestamp).
   * The value returned may be {@code null}.
   */
  public DateTime getCreatedDate() {
    return createdDate;
  }

  /**
   * Create time for this file (formatted ISO8601 timestamp).
   * The value set may be {@code null}.
   */
  public File setCreatedDate(DateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  /**
   * A short description of the file.
   * The value returned may be {@code null}.
   */
  public String getDescription() {
    return description;
  }

  /**
   * A short description of the file.
   * The value set may be {@code null}.
   */
  public File setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Short lived download URL for the file. This is only populated for files with content stored in
   * Drive.
   * The value returned may be {@code null}.
   */
  public String getDownloadUrl() {
    return downloadUrl;
  }

  /**
   * Short lived download URL for the file. This is only populated for files with content stored in
   * Drive.
   * The value set may be {@code null}.
   */
  public File setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  /**
   * Whether the file can be edited by the current user.
   * The value returned may be {@code null}.
   */
  public Boolean getEditable() {
    return editable;
  }

  /**
   * Whether the file can be edited by the current user.
   * The value set may be {@code null}.
   */
  public File setEditable(Boolean editable) {
    this.editable = editable;
    return this;
  }

  /**
   * A link for embedding the file.
   * The value returned may be {@code null}.
   */
  public String getEmbedLink() {
    return embedLink;
  }

  /**
   * A link for embedding the file.
   * The value set may be {@code null}.
   */
  public File setEmbedLink(String embedLink) {
    this.embedLink = embedLink;
    return this;
  }

  /**
   * ETag of the file.
   * The value returned may be {@code null}.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * ETag of the file.
   * The value set may be {@code null}.
   */
  public File setEtag(String etag) {
    this.etag = etag;
    return this;
  }

  /**
   * Whether this file has been explicitly trashed, as opposed to recursively trashed. This will
   * only be populated if the file is trashed.
   * The value returned may be {@code null}.
   */
  public Boolean getExplicitlyTrashed() {
    return explicitlyTrashed;
  }

  /**
   * Whether this file has been explicitly trashed, as opposed to recursively trashed. This will
   * only be populated if the file is trashed.
   * The value set may be {@code null}.
   */
  public File setExplicitlyTrashed(Boolean explicitlyTrashed) {
    this.explicitlyTrashed = explicitlyTrashed;
    return this;
  }

  /**
   * Links for exporting Google Docs to specific formats.
   * The value returned may be {@code null}.
   */
  public java.util.Map<String, String> getExportLinks() {
    return exportLinks;
  }

  /**
   * Links for exporting Google Docs to specific formats.
   * The value set may be {@code null}.
   */
  public File setExportLinks(java.util.Map<String, String> exportLinks) {
    this.exportLinks = exportLinks;
    return this;
  }

  /**
   * The file extension used when downloading this file. This field is read only. To set the
   * extension, include it in the title when creating the file. This is only populated for files
   * with content stored in Drive.
   * The value returned may be {@code null}.
   */
  public String getFileExtension() {
    return fileExtension;
  }

  /**
   * The file extension used when downloading this file. This field is read only. To set the
   * extension, include it in the title when creating the file. This is only populated for files
   * with content stored in Drive.
   * The value set may be {@code null}.
   */
  public File setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
    return this;
  }

  /**
   * The size of the file in bytes. This is only populated for files with content stored in Drive.
   * The value returned may be {@code null}.
   */
  public Long getFileSize() {
    return fileSize;
  }

  /**
   * The size of the file in bytes. This is only populated for files with content stored in Drive.
   * The value set may be {@code null}.
   */
  public File setFileSize(Long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  /**
   * A link to the file's icon.
   * The value returned may be {@code null}.
   */
  public String getIconLink() {
    return iconLink;
  }

  /**
   * A link to the file's icon.
   * The value set may be {@code null}.
   */
  public File setIconLink(String iconLink) {
    this.iconLink = iconLink;
    return this;
  }

  /**
   * The ID of the file.
   * The value returned may be {@code null}.
   */
  public String getId() {
    return id;
  }

  /**
   * The ID of the file.
   * The value set may be {@code null}.
   */
  public File setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Metadata about image media. This will only be present for image types, and its contents will
   * depend on what can be parsed from the image content.
   * The value returned may be {@code null}.
   */
  public ImageMediaMetadata getImageMediaMetadata() {
    return imageMediaMetadata;
  }

  /**
   * Metadata about image media. This will only be present for image types, and its contents will
   * depend on what can be parsed from the image content.
   * The value set may be {@code null}.
   */
  public File setImageMediaMetadata(ImageMediaMetadata imageMediaMetadata) {
    this.imageMediaMetadata = imageMediaMetadata;
    return this;
  }

  /**
   * Indexable text attributes for the file (can only be written)
   * The value returned may be {@code null}.
   */
  public IndexableText getIndexableText() {
    return indexableText;
  }

  /**
   * Indexable text attributes for the file (can only be written)
   * The value set may be {@code null}.
   */
  public File setIndexableText(IndexableText indexableText) {
    this.indexableText = indexableText;
    return this;
  }

  /**
   * The type of file. This is always drive#file.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * The type of file. This is always drive#file.
   * The value set may be {@code null}.
   */
  public File setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * A group of labels for the file.
   * The value returned may be {@code null}.
   */
  public Labels getLabels() {
    return labels;
  }

  /**
   * A group of labels for the file.
   * The value set may be {@code null}.
   */
  public File setLabels(Labels labels) {
    this.labels = labels;
    return this;
  }

  /**
   * The last user to modify this file.
   * The value returned may be {@code null}.
   */
  public User getLastModifyingUser() {
    return lastModifyingUser;
  }

  /**
   * The last user to modify this file.
   * The value set may be {@code null}.
   */
  public File setLastModifyingUser(User lastModifyingUser) {
    this.lastModifyingUser = lastModifyingUser;
    return this;
  }

  /**
   * Name of the last user to modify this file.
   * The value returned may be {@code null}.
   */
  public String getLastModifyingUserName() {
    return lastModifyingUserName;
  }

  /**
   * Name of the last user to modify this file.
   * The value set may be {@code null}.
   */
  public File setLastModifyingUserName(String lastModifyingUserName) {
    this.lastModifyingUserName = lastModifyingUserName;
    return this;
  }

  /**
   * Last time this file was viewed by the user (formatted RFC 3339 timestamp).
   * The value returned may be {@code null}.
   */
  public DateTime getLastViewedByMeDate() {
    return lastViewedByMeDate;
  }

  /**
   * Last time this file was viewed by the user (formatted RFC 3339 timestamp).
   * The value set may be {@code null}.
   */
  public File setLastViewedByMeDate(DateTime lastViewedByMeDate) {
    this.lastViewedByMeDate = lastViewedByMeDate;
    return this;
  }

  /**
   * An MD5 checksum for the content of this file. This is populated only for files with content
   * stored in Drive.
   * The value returned may be {@code null}.
   */
  public String getMd5Checksum() {
    return md5Checksum;
  }

  /**
   * An MD5 checksum for the content of this file. This is populated only for files with content
   * stored in Drive.
   * The value set may be {@code null}.
   */
  public File setMd5Checksum(String md5Checksum) {
    this.md5Checksum = md5Checksum;
    return this;
  }

  /**
   * The MIME type of the file. This is only mutable on update when uploading new content. This
   * field can be left blank, and the mimetype will be determined from the uploaded content's MIME
   * type.
   * The value returned may be {@code null}.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * The MIME type of the file. This is only mutable on update when uploading new content. This
   * field can be left blank, and the mimetype will be determined from the uploaded content's MIME
   * type.
   * The value set may be {@code null}.
   */
  public File setMimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * Last time this file was modified by the user (formatted RFC 3339 timestamp). Note that setting
   * modifiedDate will also update the modifiedByMe date for the user which set the date.
   * The value returned may be {@code null}.
   */
  public DateTime getModifiedByMeDate() {
    return modifiedByMeDate;
  }

  /**
   * Last time this file was modified by the user (formatted RFC 3339 timestamp). Note that setting
   * modifiedDate will also update the modifiedByMe date for the user which set the date.
   * The value set may be {@code null}.
   */
  public File setModifiedByMeDate(DateTime modifiedByMeDate) {
    this.modifiedByMeDate = modifiedByMeDate;
    return this;
  }

  /**
   * Last time this file was modified by anyone (formatted RFC 3339 timestamp). This is only mutable
   * on update when the setModifiedDate parameter is set.
   * The value returned may be {@code null}.
   */
  public DateTime getModifiedDate() {
    return modifiedDate;
  }

  /**
   * Last time this file was modified by anyone (formatted RFC 3339 timestamp). This is only mutable
   * on update when the setModifiedDate parameter is set.
   * The value set may be {@code null}.
   */
  public File setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  /**
   * The original filename if the file was uploaded manually, or the original title if the file was
   * inserted through the API. Note that renames of the title will not change the original filename.
   * This will only be populated on files with content stored in Drive.
   * The value returned may be {@code null}.
   */
  public String getOriginalFilename() {
    return originalFilename;
  }

  /**
   * The original filename if the file was uploaded manually, or the original title if the file was
   * inserted through the API. Note that renames of the title will not change the original filename.
   * This will only be populated on files with content stored in Drive.
   * The value set may be {@code null}.
   */
  public File setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
    return this;
  }

  /**
   * Name(s) of the owner(s) of this file.
   * The value returned may be {@code null}.
   */
  public java.util.List<String> getOwnerNames() {
    return ownerNames;
  }

  /**
   * Name(s) of the owner(s) of this file.
   * The value set may be {@code null}.
   */
  public File setOwnerNames(java.util.List<String> ownerNames) {
    this.ownerNames = ownerNames;
    return this;
  }

  /**
   * The owner(s) of this file.
   * The value returned may be {@code null}.
   */
  public java.util.List<User> getOwners() {
    return owners;
  }

  /**
   * The owner(s) of this file.
   * The value set may be {@code null}.
   */
  public File setOwners(java.util.List<User> owners) {
    this.owners = owners;
    return this;
  }

  /**
   * Collection of parent folders which contain this file. Setting this field will put the file in
   * all of the provided folders. On insert, if no folders are provided, the file will be placed in
   * the default root folder.
   * The value returned may be {@code null}.
   */
  public java.util.List<ParentReference> getParents() {
    return parents;
  }

  /**
   * Collection of parent folders which contain this file. Setting this field will put the file in
   * all of the provided folders. On insert, if no folders are provided, the file will be placed in
   * the default root folder.
   * The value set may be {@code null}.
   */
  public File setParents(java.util.List<ParentReference> parents) {
    this.parents = parents;
    return this;
  }

  /**
   * The number of quota bytes used by this file.
   * The value returned may be {@code null}.
   */
  public Long getQuotaBytesUsed() {
    return quotaBytesUsed;
  }

  /**
   * The number of quota bytes used by this file.
   * The value set may be {@code null}.
   */
  public File setQuotaBytesUsed(Long quotaBytesUsed) {
    this.quotaBytesUsed = quotaBytesUsed;
    return this;
  }

  /**
   * A link back to this file.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this file.
   * The value set may be {@code null}.
   */
  public File setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

  /**
   * Whether the file has been shared.
   * The value returned may be {@code null}.
   */
  public Boolean getShared() {
    return shared;
  }

  /**
   * Whether the file has been shared.
   * The value set may be {@code null}.
   */
  public File setShared(Boolean shared) {
    this.shared = shared;
    return this;
  }

  /**
   * Time at which this file was shared with the user (formatted RFC 3339 timestamp).
   * The value returned may be {@code null}.
   */
  public DateTime getSharedWithMeDate() {
    return sharedWithMeDate;
  }

  /**
   * Time at which this file was shared with the user (formatted RFC 3339 timestamp).
   * The value set may be {@code null}.
   */
  public File setSharedWithMeDate(DateTime sharedWithMeDate) {
    this.sharedWithMeDate = sharedWithMeDate;
    return this;
  }

  /**
   * Thumbnail for the file. Only accepted on upload and for files that are not already thumbnailed
   * by Google.
   * The value returned may be {@code null}.
   */
  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  /**
   * Thumbnail for the file. Only accepted on upload and for files that are not already thumbnailed
   * by Google.
   * The value set may be {@code null}.
   */
  public File setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
    return this;
  }

  /**
   * A link to the file's thumbnail.
   * The value returned may be {@code null}.
   */
  public String getThumbnailLink() {
    return thumbnailLink;
  }

  /**
   * A link to the file's thumbnail.
   * The value set may be {@code null}.
   */
  public File setThumbnailLink(String thumbnailLink) {
    this.thumbnailLink = thumbnailLink;
    return this;
  }

  /**
   * The title of this file.
   * The value returned may be {@code null}.
   */
  public String getTitle() {
    return title;
  }

  /**
   * The title of this file.
   * The value set may be {@code null}.
   */
  public File setTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * The permissions for the authenticated user on this file.
   * The value returned may be {@code null}.
   */
  public Permission getUserPermission() {
    return userPermission;
  }

  /**
   * The permissions for the authenticated user on this file.
   * The value set may be {@code null}.
   */
  public File setUserPermission(Permission userPermission) {
    this.userPermission = userPermission;
    return this;
  }

  /**
   * A link for downloading the content of the file in a browser using cookie based authentication.
   * In cases where the content is shared publicly, the content can be downloaded without any
   * credentials.
   * The value returned may be {@code null}.
   */
  public String getWebContentLink() {
    return webContentLink;
  }

  /**
   * A link for downloading the content of the file in a browser using cookie based authentication.
   * In cases where the content is shared publicly, the content can be downloaded without any
   * credentials.
   * The value set may be {@code null}.
   */
  public File setWebContentLink(String webContentLink) {
    this.webContentLink = webContentLink;
    return this;
  }

  /**
   * A link only available on public folders for viewing their static web assets (HTML, CSS, JS,
   * etc) via Google Drive's Website Hosting.
   * The value returned may be {@code null}.
   */
  public String getWebViewLink() {
    return webViewLink;
  }

  /**
   * A link only available on public folders for viewing their static web assets (HTML, CSS, JS,
   * etc) via Google Drive's Website Hosting.
   * The value set may be {@code null}.
   */
  public File setWebViewLink(String webViewLink) {
    this.webViewLink = webViewLink;
    return this;
  }

  /**
   * Whether writers can share the document with other users.
   * The value returned may be {@code null}.
   */
  public Boolean getWritersCanShare() {
    return writersCanShare;
  }

  /**
   * Whether writers can share the document with other users.
   * The value set may be {@code null}.
   */
  public File setWritersCanShare(Boolean writersCanShare) {
    this.writersCanShare = writersCanShare;
    return this;
  }

  /**
   * Metadata about image media. This will only be present for image types, and its contents will
   * depend on what can be parsed from the image content.
   */
  public static final class ImageMediaMetadata extends GenericJson {

    /**
     * The aperture used to create the photo (f-number).
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Float aperture;

    /**
     * The make of the camera used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String cameraMake;

    /**
     * The model of the camera used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String cameraModel;

    /**
     * The color space of the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String colorSpace;

    /**
     * The date and time the photo was taken (EXIF format timestamp).
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String date;

    /**
     * The exposure bias of the photo (APEX value).
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Float exposureBias;

    /**
     * The exposure mode used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String exposureMode;

    /**
     * The length of the exposure, in seconds.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Float exposureTime;

    /**
     * Whether a flash was used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean flashUsed;

    /**
     * The focal length used to create the photo, in millimeters.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Float focalLength;

    /**
     * The height of the image in pixels.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer height;

    /**
     * The ISO speed used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer isoSpeed;

    /**
     * The lens used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String lens;

    /**
     * Geographic location information stored in the image.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Location location;

    /**
     * The smallest f-number of the lens at the focal length used to create the photo (APEX value).
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Float maxApertureValue;

    /**
     * The metering mode used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String meteringMode;

    /**
     * The rotation in clockwise degrees from the image's original orientation.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer rotation;

    /**
     * The type of sensor used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String sensor;

    /**
     * The distance to the subject of the photo, in meters.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer subjectDistance;

    /**
     * The white balance mode used to create the photo.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String whiteBalance;

    /**
     * The width of the image in pixels.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Integer width;

    /**
     * The aperture used to create the photo (f-number).
     * The value returned may be {@code null}.
     */
    public Float getAperture() {
      return aperture;
    }

    /**
     * The aperture used to create the photo (f-number).
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setAperture(Float aperture) {
      this.aperture = aperture;
      return this;
    }

    /**
     * The make of the camera used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getCameraMake() {
      return cameraMake;
    }

    /**
     * The make of the camera used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setCameraMake(String cameraMake) {
      this.cameraMake = cameraMake;
      return this;
    }

    /**
     * The model of the camera used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getCameraModel() {
      return cameraModel;
    }

    /**
     * The model of the camera used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setCameraModel(String cameraModel) {
      this.cameraModel = cameraModel;
      return this;
    }

    /**
     * The color space of the photo.
     * The value returned may be {@code null}.
     */
    public String getColorSpace() {
      return colorSpace;
    }

    /**
     * The color space of the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setColorSpace(String colorSpace) {
      this.colorSpace = colorSpace;
      return this;
    }

    /**
     * The date and time the photo was taken (EXIF format timestamp).
     * The value returned may be {@code null}.
     */
    public String getDate() {
      return date;
    }

    /**
     * The date and time the photo was taken (EXIF format timestamp).
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setDate(String date) {
      this.date = date;
      return this;
    }

    /**
     * The exposure bias of the photo (APEX value).
     * The value returned may be {@code null}.
     */
    public Float getExposureBias() {
      return exposureBias;
    }

    /**
     * The exposure bias of the photo (APEX value).
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setExposureBias(Float exposureBias) {
      this.exposureBias = exposureBias;
      return this;
    }

    /**
     * The exposure mode used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getExposureMode() {
      return exposureMode;
    }

    /**
     * The exposure mode used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setExposureMode(String exposureMode) {
      this.exposureMode = exposureMode;
      return this;
    }

    /**
     * The length of the exposure, in seconds.
     * The value returned may be {@code null}.
     */
    public Float getExposureTime() {
      return exposureTime;
    }

    /**
     * The length of the exposure, in seconds.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setExposureTime(Float exposureTime) {
      this.exposureTime = exposureTime;
      return this;
    }

    /**
     * Whether a flash was used to create the photo.
     * The value returned may be {@code null}.
     */
    public Boolean getFlashUsed() {
      return flashUsed;
    }

    /**
     * Whether a flash was used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setFlashUsed(Boolean flashUsed) {
      this.flashUsed = flashUsed;
      return this;
    }

    /**
     * The focal length used to create the photo, in millimeters.
     * The value returned may be {@code null}.
     */
    public Float getFocalLength() {
      return focalLength;
    }

    /**
     * The focal length used to create the photo, in millimeters.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setFocalLength(Float focalLength) {
      this.focalLength = focalLength;
      return this;
    }

    /**
     * The height of the image in pixels.
     * The value returned may be {@code null}.
     */
    public Integer getHeight() {
      return height;
    }

    /**
     * The height of the image in pixels.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setHeight(Integer height) {
      this.height = height;
      return this;
    }

    /**
     * The ISO speed used to create the photo.
     * The value returned may be {@code null}.
     */
    public Integer getIsoSpeed() {
      return isoSpeed;
    }

    /**
     * The ISO speed used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setIsoSpeed(Integer isoSpeed) {
      this.isoSpeed = isoSpeed;
      return this;
    }

    /**
     * The lens used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getLens() {
      return lens;
    }

    /**
     * The lens used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setLens(String lens) {
      this.lens = lens;
      return this;
    }

    /**
     * Geographic location information stored in the image.
     * The value returned may be {@code null}.
     */
    public Location getLocation() {
      return location;
    }

    /**
     * Geographic location information stored in the image.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setLocation(Location location) {
      this.location = location;
      return this;
    }

    /**
     * The smallest f-number of the lens at the focal length used to create the photo (APEX value).
     * The value returned may be {@code null}.
     */
    public Float getMaxApertureValue() {
      return maxApertureValue;
    }

    /**
     * The smallest f-number of the lens at the focal length used to create the photo (APEX value).
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setMaxApertureValue(Float maxApertureValue) {
      this.maxApertureValue = maxApertureValue;
      return this;
    }

    /**
     * The metering mode used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getMeteringMode() {
      return meteringMode;
    }

    /**
     * The metering mode used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setMeteringMode(String meteringMode) {
      this.meteringMode = meteringMode;
      return this;
    }

    /**
     * The rotation in clockwise degrees from the image's original orientation.
     * The value returned may be {@code null}.
     */
    public Integer getRotation() {
      return rotation;
    }

    /**
     * The rotation in clockwise degrees from the image's original orientation.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setRotation(Integer rotation) {
      this.rotation = rotation;
      return this;
    }

    /**
     * The type of sensor used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getSensor() {
      return sensor;
    }

    /**
     * The type of sensor used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setSensor(String sensor) {
      this.sensor = sensor;
      return this;
    }

    /**
     * The distance to the subject of the photo, in meters.
     * The value returned may be {@code null}.
     */
    public Integer getSubjectDistance() {
      return subjectDistance;
    }

    /**
     * The distance to the subject of the photo, in meters.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setSubjectDistance(Integer subjectDistance) {
      this.subjectDistance = subjectDistance;
      return this;
    }

    /**
     * The white balance mode used to create the photo.
     * The value returned may be {@code null}.
     */
    public String getWhiteBalance() {
      return whiteBalance;
    }

    /**
     * The white balance mode used to create the photo.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setWhiteBalance(String whiteBalance) {
      this.whiteBalance = whiteBalance;
      return this;
    }

    /**
     * The width of the image in pixels.
     * The value returned may be {@code null}.
     */
    public Integer getWidth() {
      return width;
    }

    /**
     * The width of the image in pixels.
     * The value set may be {@code null}.
     */
    public ImageMediaMetadata setWidth(Integer width) {
      this.width = width;
      return this;
    }

    /**
     * Geographic location information stored in the image.
     */
    public static final class Location extends GenericJson {

      /**
       * The altitude stored in the image.
       * The value may be {@code null}.
       */
      @com.google.api.client.util.Key
      private Double altitude;

      /**
       * The latitude stored in the image.
       * The value may be {@code null}.
       */
      @com.google.api.client.util.Key
      private Double latitude;

      /**
       * The longitude stored in the image.
       * The value may be {@code null}.
       */
      @com.google.api.client.util.Key
      private Double longitude;

      /**
       * The altitude stored in the image.
       * The value returned may be {@code null}.
       */
      public Double getAltitude() {
        return altitude;
      }

      /**
       * The altitude stored in the image.
       * The value set may be {@code null}.
       */
      public Location setAltitude(Double altitude) {
        this.altitude = altitude;
        return this;
      }

      /**
       * The latitude stored in the image.
       * The value returned may be {@code null}.
       */
      public Double getLatitude() {
        return latitude;
      }

      /**
       * The latitude stored in the image.
       * The value set may be {@code null}.
       */
      public Location setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
      }

      /**
       * The longitude stored in the image.
       * The value returned may be {@code null}.
       */
      public Double getLongitude() {
        return longitude;
      }

      /**
       * The longitude stored in the image.
       * The value set may be {@code null}.
       */
      public Location setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
      }

    }
  }

  /**
   * Indexable text attributes for the file (can only be written)
   */
  public static final class IndexableText extends GenericJson {

    /**
     * The text to be indexed for this file.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String text;

    /**
     * The text to be indexed for this file.
     * The value returned may be {@code null}.
     */
    public String getText() {
      return text;
    }

    /**
     * The text to be indexed for this file.
     * The value set may be {@code null}.
     */
    public IndexableText setText(String text) {
      this.text = text;
      return this;
    }

  }

  /**
   * A group of labels for the file.
   */
  public static final class Labels extends GenericJson {

    /**
     * Whether this file is hidden from the user.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean hidden;

    /**
     * Whether viewers are prevented from downloading this file.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean restricted;

    /**
     * Whether this file is starred by the user.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean starred;

    /**
     * Whether this file has been trashed.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean trashed;

    /**
     * Whether this file has been viewed by this user.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private Boolean viewed;

    /**
     * Whether this file is hidden from the user.
     * The value returned may be {@code null}.
     */
    public Boolean getHidden() {
      return hidden;
    }

    /**
     * Whether this file is hidden from the user.
     * The value set may be {@code null}.
     */
    public Labels setHidden(Boolean hidden) {
      this.hidden = hidden;
      return this;
    }

    /**
     * Whether viewers are prevented from downloading this file.
     * The value returned may be {@code null}.
     */
    public Boolean getRestricted() {
      return restricted;
    }

    /**
     * Whether viewers are prevented from downloading this file.
     * The value set may be {@code null}.
     */
    public Labels setRestricted(Boolean restricted) {
      this.restricted = restricted;
      return this;
    }

    /**
     * Whether this file is starred by the user.
     * The value returned may be {@code null}.
     */
    public Boolean getStarred() {
      return starred;
    }

    /**
     * Whether this file is starred by the user.
     * The value set may be {@code null}.
     */
    public Labels setStarred(Boolean starred) {
      this.starred = starred;
      return this;
    }

    /**
     * Whether this file has been trashed.
     * The value returned may be {@code null}.
     */
    public Boolean getTrashed() {
      return trashed;
    }

    /**
     * Whether this file has been trashed.
     * The value set may be {@code null}.
     */
    public Labels setTrashed(Boolean trashed) {
      this.trashed = trashed;
      return this;
    }

    /**
     * Whether this file has been viewed by this user.
     * The value returned may be {@code null}.
     */
    public Boolean getViewed() {
      return viewed;
    }

    /**
     * Whether this file has been viewed by this user.
     * The value set may be {@code null}.
     */
    public Labels setViewed(Boolean viewed) {
      this.viewed = viewed;
      return this;
    }

  }

  /**
   * Thumbnail for the file. Only accepted on upload and for files that are not already thumbnailed by
   * Google.
   */
  public static final class Thumbnail extends GenericJson {

    /**
     * The URL-safe Base64 encoded bytes of the thumbnail image.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String image;

    /**
     * The MIME type of the thumbnail.
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private String mimeType;

    /**
     * The URL-safe Base64 encoded bytes of the thumbnail image.
     * The value returned may be {@code null}.
     */
    public String getImage() {
      return image;
    }

    /**
     * The URL-safe Base64 encoded bytes of the thumbnail image.
     * The value set may be {@code null}.
     */
    public Thumbnail setImage(String image) {
      this.image = image;
      return this;
    }

    /**
     * The MIME type of the thumbnail.
     * The value returned may be {@code null}.
     */
    public String getMimeType() {
      return mimeType;
    }

    /**
     * The MIME type of the thumbnail.
     * The value set may be {@code null}.
     */
    public Thumbnail setMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

  }

}

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
 * A revision of a file.
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
public final class Revision extends GenericJson {

  /**
   * Short term download URL for the file. This will only be populated on files with content stored
   * in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String downloadUrl;

  /**
   * The ETag of the revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String etag;

  /**
   * Links for exporting Google Docs to specific formats.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.Map<String, String> exportLinks;

  /**
   * The size of the revision in bytes. This will only be populated on files with content stored in
   * Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long fileSize;

  /**
   * The ID of the revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String id;

  /**
   * This is always drive#revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * The last user to modify this revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User lastModifyingUser;

  /**
   * Name of the last user to modify this revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String lastModifyingUserName;

  /**
   * An MD5 checksum for the content of this revision. This will only be populated on files with
   * content stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String md5Checksum;

  /**
   * The MIME type of the revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String mimeType;

  /**
   * Last time this revision was modified (formatted RFC 3339 timestamp).
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private DateTime modifiedDate;

  /**
   * The original filename when this revision was created. This will only be populated on files with
   * content stored in Drive.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String originalFilename;

  /**
   * Whether this revision is pinned to prevent automatic purging. This will only be populated and
   * can only be modified on files with content stored in Drive which are not Google Docs. Revisions
   * can also be pinned when they are created through the drive.files.insert/update/copy by using
   * the pinned query parameter.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean pinned;

  /**
   * Whether subsequent revisions will be automatically republished. This is only populated and can
   * only be modified for Google Docs.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean publishAuto;

  /**
   * Whether this revision is published. This is only populated and can only be modified for Google
   * Docs.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean published;

  /**
   * A link to the published revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String publishedLink;

  /**
   * Whether this revision is published outside the domain. This is only populated and can only be
   * modified for Google Docs.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean publishedOutsideDomain;

  /**
   * A link back to this revision.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * Short term download URL for the file. This will only be populated on files with content stored
   * in Drive.
   * The value returned may be {@code null}.
   */
  public String getDownloadUrl() {
    return downloadUrl;
  }

  /**
   * Short term download URL for the file. This will only be populated on files with content stored
   * in Drive.
   * The value set may be {@code null}.
   */
  public Revision setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  /**
   * The ETag of the revision.
   * The value returned may be {@code null}.
   */
  public String getEtag() {
    return etag;
  }

  /**
   * The ETag of the revision.
   * The value set may be {@code null}.
   */
  public Revision setEtag(String etag) {
    this.etag = etag;
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
  public Revision setExportLinks(java.util.Map<String, String> exportLinks) {
    this.exportLinks = exportLinks;
    return this;
  }

  /**
   * The size of the revision in bytes. This will only be populated on files with content stored in
   * Drive.
   * The value returned may be {@code null}.
   */
  public Long getFileSize() {
    return fileSize;
  }

  /**
   * The size of the revision in bytes. This will only be populated on files with content stored in
   * Drive.
   * The value set may be {@code null}.
   */
  public Revision setFileSize(Long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  /**
   * The ID of the revision.
   * The value returned may be {@code null}.
   */
  public String getId() {
    return id;
  }

  /**
   * The ID of the revision.
   * The value set may be {@code null}.
   */
  public Revision setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * This is always drive#revision.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#revision.
   * The value set may be {@code null}.
   */
  public Revision setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * The last user to modify this revision.
   * The value returned may be {@code null}.
   */
  public User getLastModifyingUser() {
    return lastModifyingUser;
  }

  /**
   * The last user to modify this revision.
   * The value set may be {@code null}.
   */
  public Revision setLastModifyingUser(User lastModifyingUser) {
    this.lastModifyingUser = lastModifyingUser;
    return this;
  }

  /**
   * Name of the last user to modify this revision.
   * The value returned may be {@code null}.
   */
  public String getLastModifyingUserName() {
    return lastModifyingUserName;
  }

  /**
   * Name of the last user to modify this revision.
   * The value set may be {@code null}.
   */
  public Revision setLastModifyingUserName(String lastModifyingUserName) {
    this.lastModifyingUserName = lastModifyingUserName;
    return this;
  }

  /**
   * An MD5 checksum for the content of this revision. This will only be populated on files with
   * content stored in Drive.
   * The value returned may be {@code null}.
   */
  public String getMd5Checksum() {
    return md5Checksum;
  }

  /**
   * An MD5 checksum for the content of this revision. This will only be populated on files with
   * content stored in Drive.
   * The value set may be {@code null}.
   */
  public Revision setMd5Checksum(String md5Checksum) {
    this.md5Checksum = md5Checksum;
    return this;
  }

  /**
   * The MIME type of the revision.
   * The value returned may be {@code null}.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * The MIME type of the revision.
   * The value set may be {@code null}.
   */
  public Revision setMimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * Last time this revision was modified (formatted RFC 3339 timestamp).
   * The value returned may be {@code null}.
   */
  public DateTime getModifiedDate() {
    return modifiedDate;
  }

  /**
   * Last time this revision was modified (formatted RFC 3339 timestamp).
   * The value set may be {@code null}.
   */
  public Revision setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
    return this;
  }

  /**
   * The original filename when this revision was created. This will only be populated on files with
   * content stored in Drive.
   * The value returned may be {@code null}.
   */
  public String getOriginalFilename() {
    return originalFilename;
  }

  /**
   * The original filename when this revision was created. This will only be populated on files with
   * content stored in Drive.
   * The value set may be {@code null}.
   */
  public Revision setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
    return this;
  }

  /**
   * Whether this revision is pinned to prevent automatic purging. This will only be populated and
   * can only be modified on files with content stored in Drive which are not Google Docs. Revisions
   * can also be pinned when they are created through the drive.files.insert/update/copy by using
   * the pinned query parameter.
   * The value returned may be {@code null}.
   */
  public Boolean getPinned() {
    return pinned;
  }

  /**
   * Whether this revision is pinned to prevent automatic purging. This will only be populated and
   * can only be modified on files with content stored in Drive which are not Google Docs. Revisions
   * can also be pinned when they are created through the drive.files.insert/update/copy by using
   * the pinned query parameter.
   * The value set may be {@code null}.
   */
  public Revision setPinned(Boolean pinned) {
    this.pinned = pinned;
    return this;
  }

  /**
   * Whether subsequent revisions will be automatically republished. This is only populated and can
   * only be modified for Google Docs.
   * The value returned may be {@code null}.
   */
  public Boolean getPublishAuto() {
    return publishAuto;
  }

  /**
   * Whether subsequent revisions will be automatically republished. This is only populated and can
   * only be modified for Google Docs.
   * The value set may be {@code null}.
   */
  public Revision setPublishAuto(Boolean publishAuto) {
    this.publishAuto = publishAuto;
    return this;
  }

  /**
   * Whether this revision is published. This is only populated and can only be modified for Google
   * Docs.
   * The value returned may be {@code null}.
   */
  public Boolean getPublished() {
    return published;
  }

  /**
   * Whether this revision is published. This is only populated and can only be modified for Google
   * Docs.
   * The value set may be {@code null}.
   */
  public Revision setPublished(Boolean published) {
    this.published = published;
    return this;
  }

  /**
   * A link to the published revision.
   * The value returned may be {@code null}.
   */
  public String getPublishedLink() {
    return publishedLink;
  }

  /**
   * A link to the published revision.
   * The value set may be {@code null}.
   */
  public Revision setPublishedLink(String publishedLink) {
    this.publishedLink = publishedLink;
    return this;
  }

  /**
   * Whether this revision is published outside the domain. This is only populated and can only be
   * modified for Google Docs.
   * The value returned may be {@code null}.
   */
  public Boolean getPublishedOutsideDomain() {
    return publishedOutsideDomain;
  }

  /**
   * Whether this revision is published outside the domain. This is only populated and can only be
   * modified for Google Docs.
   * The value set may be {@code null}.
   */
  public Revision setPublishedOutsideDomain(Boolean publishedOutsideDomain) {
    this.publishedOutsideDomain = publishedOutsideDomain;
    return this;
  }

  /**
   * A link back to this revision.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this revision.
   * The value set may be {@code null}.
   */
  public Revision setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

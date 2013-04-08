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
 * Representation of a change to a file.
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
public final class Change extends GenericJson {

  /**
   * Whether the file has been deleted.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Boolean deleted;

  /**
   * The updated state of the file. Present if the file has not been deleted.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private File file;

  /**
   * The ID of the file associated with this change.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String fileId;

  /**
   * The ID of the change.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @JsonString
  private Long id;

  /**
   * This is always drive#change.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String kind;

  /**
   * A link back to this change.
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private String selfLink;

  /**
   * Whether the file has been deleted.
   * The value returned may be {@code null}.
   */
  public Boolean getDeleted() {
    return deleted;
  }

  /**
   * Whether the file has been deleted.
   * The value set may be {@code null}.
   */
  public Change setDeleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  /**
   * The updated state of the file. Present if the file has not been deleted.
   * The value returned may be {@code null}.
   */
  public File getFile() {
    return file;
  }

  /**
   * The updated state of the file. Present if the file has not been deleted.
   * The value set may be {@code null}.
   */
  public Change setFile(File file) {
    this.file = file;
    return this;
  }

  /**
   * The ID of the file associated with this change.
   * The value returned may be {@code null}.
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * The ID of the file associated with this change.
   * The value set may be {@code null}.
   */
  public Change setFileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  /**
   * The ID of the change.
   * The value returned may be {@code null}.
   */
  public Long getId() {
    return id;
  }

  /**
   * The ID of the change.
   * The value set may be {@code null}.
   */
  public Change setId(Long id) {
    this.id = id;
    return this;
  }

  /**
   * This is always drive#change.
   * The value returned may be {@code null}.
   */
  public String getKind() {
    return kind;
  }

  /**
   * This is always drive#change.
   * The value set may be {@code null}.
   */
  public Change setKind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * A link back to this change.
   * The value returned may be {@code null}.
   */
  public String getSelfLink() {
    return selfLink;
  }

  /**
   * A link back to this change.
   * The value set may be {@code null}.
   */
  public Change setSelfLink(String selfLink) {
    this.selfLink = selfLink;
    return this;
  }

}

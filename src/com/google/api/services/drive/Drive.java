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
 * This file was generated.
 *  with google-apis-code-generator 1.2.0 (build: 2013-03-04 17:44:07 UTC)
 *  on 2013-03-14 at 15:29:45 UTC 
 */

package com.google.api.services.drive;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonString;
import com.google.common.base.Preconditions;

/**
 * Service definition for Drive (v2).
 *
 * <p>
 * The API to interact with Drive.
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="https://developers.google.com/drive/" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link DriveRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * <p>
 * Upgrade warning: this class now extends {@link AbstractGoogleJsonClient}, whereas in prior
 * version 1.8 it extended {@link com.google.api.client.googleapis.services.GoogleClient}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Drive extends AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    Preconditions.checkState(GoogleUtils.VERSION.equals("1.13.2-beta"),
        "You are currently running with version %s of google-api-client. " +
        "You need version 1.13.2-beta of google-api-client to run version " +
        "1.13.2-beta of the Drive API library.", GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://www.googleapis.com/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "drive/v2/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   * @deprecated (scheduled to be removed in 1.13)
   */
  @Deprecated
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public Drive(HttpTransport transport, JsonFactory jsonFactory,
      HttpRequestInitializer httpRequestInitializer) {
    super(transport,
        jsonFactory,
        DEFAULT_ROOT_URL,
        DEFAULT_SERVICE_PATH,
        httpRequestInitializer,
        false);
  }

  /**
   * @param transport HTTP transport
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @param rootUrl root URL of the service
   * @param servicePath service path
   * @param jsonObjectParser JSON object parser
   * @param googleClientRequestInitializer Google request initializer or {@code null} for none
   * @param applicationName application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   * @param suppressPatternChecks whether discovery pattern checks should be suppressed on required
   *        parameters
   */
  Drive(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer,
      String rootUrl,
      String servicePath,
      JsonObjectParser jsonObjectParser,
      GoogleClientRequestInitializer googleClientRequestInitializer,
      String applicationName,
      boolean suppressPatternChecks) {
    super(transport,
        httpRequestInitializer,
        rootUrl,
        servicePath,
        jsonObjectParser,
        googleClientRequestInitializer,
        applicationName,
        suppressPatternChecks);
  }

  @Override
  protected void initialize(AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * An accessor for creating requests from the About collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.About.List request = drive.about().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public About about() {
    return new About();
  }

  /**
   * The "about" collection of methods.
   */
  public class About {

    /**
     * Gets the information about the current user along with Drive API settings
     *
     * Create a request for the method "about.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public Get get() throws java.io.IOException {
      Get result = new Get();
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.About> {

      private static final String REST_PATH = "about";

      /**
       * Gets the information about the current user along with Drive API settings
       *
       * Create a request for the method "about.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected Get() {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.About.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /**
     * When calculating the number of remaining change IDs, whether to include shared files and
     * public files the user has opened. When set to false, this counts only change IDs for owned
     * files and any shared or public files that the user has explictly added to a folder in Drive.
     */
      @com.google.api.client.util.Key
      private Boolean includeSubscribed;

      /** When calculating the number of remaining change IDs, whether to include shared files and public
     files the user has opened. When set to false, this counts only change IDs for owned files and any
     shared or public files that the user has explictly added to a folder in Drive. [default: true]
       */
      public Boolean getIncludeSubscribed() {
        return includeSubscribed;
      }

      /**
     * When calculating the number of remaining change IDs, whether to include shared files and
     * public files the user has opened. When set to false, this counts only change IDs for owned
     * files and any shared or public files that the user has explictly added to a folder in Drive.
     */
      public Get setIncludeSubscribed(Boolean includeSubscribed) {
        this.includeSubscribed = includeSubscribed;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * When calculating the number of remaining change IDs, whether to include shared files and public
     files the user has opened. When set to false, this counts only change IDs for owned files and any
     shared or public files that the user has explictly added to a folder in Drive.
       * </p>
       */
      public boolean isIncludeSubscribed() {
        if (includeSubscribed == null || includeSubscribed == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return includeSubscribed;
      }

      /** Maximum number of remaining change IDs to count */
      @com.google.api.client.util.Key
      private Long maxChangeIdCount;

      /** Maximum number of remaining change IDs to count [default: 1]
       */
      public Long getMaxChangeIdCount() {
        return maxChangeIdCount;
      }

      /** Maximum number of remaining change IDs to count */
      public Get setMaxChangeIdCount(Long maxChangeIdCount) {
        this.maxChangeIdCount = maxChangeIdCount;
        return this;
      }

      /** Change ID to start counting from when calculating number of remaining change IDs */
      @com.google.api.client.util.Key
      private Long startChangeId;

      /** Change ID to start counting from when calculating number of remaining change IDs
       */
      public Long getStartChangeId() {
        return startChangeId;
      }

      /** Change ID to start counting from when calculating number of remaining change IDs */
      public Get setStartChangeId(Long startChangeId) {
        this.startChangeId = startChangeId;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Apps collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Apps.List request = drive.apps().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Apps apps() {
    return new Apps();
  }

  /**
   * The "apps" collection of methods.
   */
  public class Apps {

    /**
     * Gets a specific app.
     *
     * Create a request for the method "apps.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param appId The ID of the app.
     * @return the request
     */
    public Get get(String appId) throws java.io.IOException {
      Get result = new Get(appId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.App> {

      private static final String REST_PATH = "apps/{appId}";

      /**
       * Gets a specific app.
       *
       * Create a request for the method "apps.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param appId The ID of the app.
       * @since 1.13
       */
      protected Get(String appId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.App.class);
        this.appId = Preconditions.checkNotNull(appId, "Required parameter appId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the app. */
      @com.google.api.client.util.Key
      private String appId;

      /** The ID of the app.
       */
      public String getAppId() {
        return appId;
      }

      /** The ID of the app. */
      public Get setAppId(String appId) {
        this.appId = appId;
        return this;
      }

    }
    /**
     * Lists a user's installed apps.
     *
     * Create a request for the method "apps.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public List list() throws java.io.IOException {
      List result = new List();
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.AppList> {

      private static final String REST_PATH = "apps";

      /**
       * Lists a user's installed apps.
       *
       * Create a request for the method "apps.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected List() {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.AppList.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

    }

  }

  /**
   * An accessor for creating requests from the Changes collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Changes.List request = drive.changes().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Changes changes() {
    return new Changes();
  }

  /**
   * The "changes" collection of methods.
   */
  public class Changes {

    /**
     * Gets a specific change.
     *
     * Create a request for the method "changes.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param changeId The ID of the change.
     * @return the request
     */
    public Get get(String changeId) throws java.io.IOException {
      Get result = new Get(changeId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.Change> {

      private static final String REST_PATH = "changes/{changeId}";

      /**
       * Gets a specific change.
       *
       * Create a request for the method "changes.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param changeId The ID of the change.
       * @since 1.13
       */
      protected Get(String changeId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.Change.class);
        this.changeId = Preconditions.checkNotNull(changeId, "Required parameter changeId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the change. */
      @com.google.api.client.util.Key
      private String changeId;

      /** The ID of the change.
       */
      public String getChangeId() {
        return changeId;
      }

      /** The ID of the change. */
      public Get setChangeId(String changeId) {
        this.changeId = changeId;
        return this;
      }

    }
    /**
     * Lists the changes for a user.
     *
     * Create a request for the method "changes.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public List list() throws java.io.IOException {
      List result = new List();
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.ChangeList> {

      private static final String REST_PATH = "changes";

      /**
       * Lists the changes for a user.
       *
       * Create a request for the method "changes.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected List() {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.ChangeList.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /**
     * Whether to include shared files and public files the user has opened. When set to false, the
     * list will include owned files plus any shared or public files the user has explictly added to
     * a folder in Drive.
     */
      @com.google.api.client.util.Key
      private Boolean includeSubscribed;

      /** Whether to include shared files and public files the user has opened. When set to false, the list
     will include owned files plus any shared or public files the user has explictly added to a folder
     in Drive. [default: true]
       */
      public Boolean getIncludeSubscribed() {
        return includeSubscribed;
      }

      /**
     * Whether to include shared files and public files the user has opened. When set to false, the
     * list will include owned files plus any shared or public files the user has explictly added to
     * a folder in Drive.
     */
      public List setIncludeSubscribed(Boolean includeSubscribed) {
        this.includeSubscribed = includeSubscribed;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to include shared files and public files the user has opened. When set to false, the list
     will include owned files plus any shared or public files the user has explictly added to a folder
     in Drive.
       * </p>
       */
      public boolean isIncludeSubscribed() {
        if (includeSubscribed == null || includeSubscribed == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return includeSubscribed;
      }

      /** Change ID to start listing changes from. */
      @com.google.api.client.util.Key
      private Long startChangeId;

      /** Change ID to start listing changes from.
       */
      public Long getStartChangeId() {
        return startChangeId;
      }

      /** Change ID to start listing changes from. */
      public List setStartChangeId(Long startChangeId) {
        this.startChangeId = startChangeId;
        return this;
      }

      /** Whether to include deleted items. */
      @com.google.api.client.util.Key
      private Boolean includeDeleted;

      /** Whether to include deleted items. [default: true]
       */
      public Boolean getIncludeDeleted() {
        return includeDeleted;
      }

      /** Whether to include deleted items. */
      public List setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to include deleted items.
       * </p>
       */
      public boolean isIncludeDeleted() {
        if (includeDeleted == null || includeDeleted == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return includeDeleted;
      }

      /** Maximum number of changes to return. */
      @com.google.api.client.util.Key
      private Integer maxResults;

      /** Maximum number of changes to return. [default: 100] [minimum: 0]
       */
      public Integer getMaxResults() {
        return maxResults;
      }

      /** Maximum number of changes to return. */
      public List setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
      }

      /** Page token for changes. */
      @com.google.api.client.util.Key
      private String pageToken;

      /** Page token for changes.
       */
      public String getPageToken() {
        return pageToken;
      }

      /** Page token for changes. */
      public List setPageToken(String pageToken) {
        this.pageToken = pageToken;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Children collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Children.List request = drive.children().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Children children() {
    return new Children();
  }

  /**
   * The "children" collection of methods.
   */
  public class Children {

    /**
     * Removes a child from a folder.
     *
     * Create a request for the method "children.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param folderId The ID of the folder.
     * @param childId The ID of the child.
     * @return the request
     */
    public Delete delete(String folderId, String childId) throws java.io.IOException {
      Delete result = new Delete(folderId, childId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{folderId}/children/{childId}";

      /**
       * Removes a child from a folder.
       *
       * Create a request for the method "children.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param folderId The ID of the folder.
       * @param childId The ID of the child.
       * @since 1.13
       */
      protected Delete(String folderId, String childId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.folderId = Preconditions.checkNotNull(folderId, "Required parameter folderId must be specified.");
        this.childId = Preconditions.checkNotNull(childId, "Required parameter childId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the folder. */
      @com.google.api.client.util.Key
      private String folderId;

      /** The ID of the folder.
       */
      public String getFolderId() {
        return folderId;
      }

      /** The ID of the folder. */
      public Delete setFolderId(String folderId) {
        this.folderId = folderId;
        return this;
      }

      /** The ID of the child. */
      @com.google.api.client.util.Key
      private String childId;

      /** The ID of the child.
       */
      public String getChildId() {
        return childId;
      }

      /** The ID of the child. */
      public Delete setChildId(String childId) {
        this.childId = childId;
        return this;
      }

    }
    /**
     * Gets a specific child reference.
     *
     * Create a request for the method "children.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param folderId The ID of the folder.
     * @param childId The ID of the child.
     * @return the request
     */
    public Get get(String folderId, String childId) throws java.io.IOException {
      Get result = new Get(folderId, childId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.ChildReference> {

      private static final String REST_PATH = "files/{folderId}/children/{childId}";

      /**
       * Gets a specific child reference.
       *
       * Create a request for the method "children.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param folderId The ID of the folder.
       * @param childId The ID of the child.
       * @since 1.13
       */
      protected Get(String folderId, String childId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.ChildReference.class);
        this.folderId = Preconditions.checkNotNull(folderId, "Required parameter folderId must be specified.");
        this.childId = Preconditions.checkNotNull(childId, "Required parameter childId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the folder. */
      @com.google.api.client.util.Key
      private String folderId;

      /** The ID of the folder.
       */
      public String getFolderId() {
        return folderId;
      }

      /** The ID of the folder. */
      public Get setFolderId(String folderId) {
        this.folderId = folderId;
        return this;
      }

      /** The ID of the child. */
      @com.google.api.client.util.Key
      private String childId;

      /** The ID of the child.
       */
      public String getChildId() {
        return childId;
      }

      /** The ID of the child. */
      public Get setChildId(String childId) {
        this.childId = childId;
        return this;
      }

    }
    /**
     * Inserts a file into a folder.
     *
     * Create a request for the method "children.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param folderId The ID of the folder.
     * @param content the {@link com.google.api.services.drive.model.ChildReference}
     * @return the request
     */
    public Insert insert(String folderId, com.google.api.services.drive.model.ChildReference content) throws java.io.IOException {
      Insert result = new Insert(folderId, content);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.ChildReference> {

      private static final String REST_PATH = "files/{folderId}/children";

      /**
       * Inserts a file into a folder.
       *
       * Create a request for the method "children.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param folderId The ID of the folder.
       * @param content the {@link com.google.api.services.drive.model.ChildReference}
       * @since 1.13
       */
      protected Insert(String folderId, com.google.api.services.drive.model.ChildReference content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.ChildReference.class);
        this.folderId = Preconditions.checkNotNull(folderId, "Required parameter folderId must be specified.");
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** The ID of the folder. */
      @com.google.api.client.util.Key
      private String folderId;

      /** The ID of the folder.
       */
      public String getFolderId() {
        return folderId;
      }

      /** The ID of the folder. */
      public Insert setFolderId(String folderId) {
        this.folderId = folderId;
        return this;
      }

    }
    /**
     * Lists a folder's children.
     *
     * Create a request for the method "children.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param folderId The ID of the folder.
     * @return the request
     */
    public List list(String folderId) throws java.io.IOException {
      List result = new List(folderId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.ChildList> {

      private static final String REST_PATH = "files/{folderId}/children";

      /**
       * Lists a folder's children.
       *
       * Create a request for the method "children.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param folderId The ID of the folder.
       * @since 1.13
       */
      protected List(String folderId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.ChildList.class);
        this.folderId = Preconditions.checkNotNull(folderId, "Required parameter folderId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID of the folder. */
      @com.google.api.client.util.Key
      private String folderId;

      /** The ID of the folder.
       */
      public String getFolderId() {
        return folderId;
      }

      /** The ID of the folder. */
      public List setFolderId(String folderId) {
        this.folderId = folderId;
        return this;
      }

      /** Query string for searching children. */
      @com.google.api.client.util.Key
      private String q;

      /** Query string for searching children.
       */
      public String getQ() {
        return q;
      }

      /** Query string for searching children. */
      public List setQ(String q) {
        this.q = q;
        return this;
      }

      /** Page token for children. */
      @com.google.api.client.util.Key
      private String pageToken;

      /** Page token for children.
       */
      public String getPageToken() {
        return pageToken;
      }

      /** Page token for children. */
      public List setPageToken(String pageToken) {
        this.pageToken = pageToken;
        return this;
      }

      /** Maximum number of children to return. */
      @com.google.api.client.util.Key
      private Integer maxResults;

      /** Maximum number of children to return. [default: 100] [minimum: 0]
       */
      public Integer getMaxResults() {
        return maxResults;
      }

      /** Maximum number of children to return. */
      public List setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Comments collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Comments.List request = drive.comments().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Comments comments() {
    return new Comments();
  }

  /**
   * The "comments" collection of methods.
   */
  public class Comments {

    /**
     * Deletes a comment.
     *
     * Create a request for the method "comments.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @return the request
     */
    public Delete delete(String fileId, String commentId) throws java.io.IOException {
      Delete result = new Delete(fileId, commentId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}";

      /**
       * Deletes a comment.
       *
       * Create a request for the method "comments.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @since 1.13
       */
      protected Delete(String fileId, String commentId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Delete setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

    }
    /**
     * Gets a comment by ID.
     *
     * Create a request for the method "comments.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @return the request
     */
    public Get get(String fileId, String commentId) throws java.io.IOException {
      Get result = new Get(fileId, commentId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.Comment> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}";

      /**
       * Gets a comment by ID.
       *
       * Create a request for the method "comments.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @since 1.13
       */
      protected Get(String fileId, String commentId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.Comment.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Get setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /**
     * If set, this will succeed when retrieving a deleted comment, and will include any deleted
     * replies.
     */
      @com.google.api.client.util.Key
      private Boolean includeDeleted;

      /** If set, this will succeed when retrieving a deleted comment, and will include any deleted replies.
     [default: false]
       */
      public Boolean getIncludeDeleted() {
        return includeDeleted;
      }

      /**
     * If set, this will succeed when retrieving a deleted comment, and will include any deleted
     * replies.
     */
      public Get setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * If set, this will succeed when retrieving a deleted comment, and will include any deleted replies.
       * </p>
       */
      public boolean isIncludeDeleted() {
        if (includeDeleted == null || includeDeleted == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return includeDeleted;
      }

    }
    /**
     * Creates a new comment on the given file.
     *
     * Create a request for the method "comments.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param content the {@link com.google.api.services.drive.model.Comment}
     * @return the request
     */
    public Insert insert(String fileId, com.google.api.services.drive.model.Comment content) throws java.io.IOException {
      Insert result = new Insert(fileId, content);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.Comment> {

      private static final String REST_PATH = "files/{fileId}/comments";

      /**
       * Creates a new comment on the given file.
       *
       * Create a request for the method "comments.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param content the {@link com.google.api.services.drive.model.Comment}
       * @since 1.13
       */
      protected Insert(String fileId, com.google.api.services.drive.model.Comment content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.Comment.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Insert setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Lists a file's comments.
     *
     * Create a request for the method "comments.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @return the request
     */
    public List list(String fileId) throws java.io.IOException {
      List result = new List(fileId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.CommentList> {

      private static final String REST_PATH = "files/{fileId}/comments";

      /**
       * Lists a file's comments.
       *
       * Create a request for the method "comments.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @since 1.13
       */
      protected List(String fileId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.CommentList.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public List setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /**
     * The continuation token, used to page through large result sets. To get the next page of
     * results, set this parameter to the value of "nextPageToken" from the previous response.
     */
      @com.google.api.client.util.Key
      private String pageToken;

      /** The continuation token, used to page through large result sets. To get the next page of results,
     set this parameter to the value of "nextPageToken" from the previous response.
       */
      public String getPageToken() {
        return pageToken;
      }

      /**
     * The continuation token, used to page through large result sets. To get the next page of
     * results, set this parameter to the value of "nextPageToken" from the previous response.
     */
      public List setPageToken(String pageToken) {
        this.pageToken = pageToken;
        return this;
      }

      /**
     * Only discussions that were updated after this timestamp will be returned. Formatted as an RFC
     * 3339 timestamp.
     */
      @com.google.api.client.util.Key
      private String updatedMin;

      /** Only discussions that were updated after this timestamp will be returned. Formatted as an RFC 3339
     timestamp.
       */
      public String getUpdatedMin() {
        return updatedMin;
      }

      /**
     * Only discussions that were updated after this timestamp will be returned. Formatted as an RFC
     * 3339 timestamp.
     */
      public List setUpdatedMin(String updatedMin) {
        this.updatedMin = updatedMin;
        return this;
      }

      /**
     * If set, all comments and replies, including deleted comments and replies (with content
     * stripped) will be returned.
     */
      @com.google.api.client.util.Key
      private Boolean includeDeleted;

      /** If set, all comments and replies, including deleted comments and replies (with content stripped)
     will be returned. [default: false]
       */
      public Boolean getIncludeDeleted() {
        return includeDeleted;
      }

      /**
     * If set, all comments and replies, including deleted comments and replies (with content
     * stripped) will be returned.
     */
      public List setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * If set, all comments and replies, including deleted comments and replies (with content stripped)
     will be returned.
       * </p>
       */
      public boolean isIncludeDeleted() {
        if (includeDeleted == null || includeDeleted == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return includeDeleted;
      }

      /** The maximum number of discussions to include in the response, used for paging. */
      @com.google.api.client.util.Key
      private Integer maxResults;

      /** The maximum number of discussions to include in the response, used for paging. [default: 20]
     [minimum: 0] [maximum: 100]
       */
      public Integer getMaxResults() {
        return maxResults;
      }

      /** The maximum number of discussions to include in the response, used for paging. */
      public List setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
      }

    }
    /**
     * Updates an existing comment. This method supports patch semantics.
     *
     * Create a request for the method "comments.patch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Patch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param content the {@link com.google.api.services.drive.model.Comment}
     * @return the request
     */
    public Patch patch(String fileId, String commentId, com.google.api.services.drive.model.Comment content) throws java.io.IOException {
      Patch result = new Patch(fileId, commentId, content);
      initialize(result);
      return result;
    }

    public class Patch extends DriveRequest<com.google.api.services.drive.model.Comment> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}";

      /**
       * Updates an existing comment. This method supports patch semantics.
       *
       * Create a request for the method "comments.patch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Patch#execute()} method to invoke the remote operation. <p> {@link
       * Patch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param content the {@link com.google.api.services.drive.model.Comment}
       * @since 1.13
       */
      protected Patch(String fileId, String commentId, com.google.api.services.drive.model.Comment content) {
        super(Drive.this, "PATCH", REST_PATH, content, com.google.api.services.drive.model.Comment.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public Patch setAlt(String alt) {
        return (Patch) super.setAlt(alt);
      }

      @Override
      public Patch setFields(String fields) {
        return (Patch) super.setFields(fields);
      }

      @Override
      public Patch setKey(String key) {
        return (Patch) super.setKey(key);
      }

      @Override
      public Patch setOauthToken(String oauthToken) {
        return (Patch) super.setOauthToken(oauthToken);
      }

      @Override
      public Patch setPrettyPrint(Boolean prettyPrint) {
        return (Patch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Patch setQuotaUser(String quotaUser) {
        return (Patch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Patch setUserIp(String userIp) {
        return (Patch) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Patch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Patch setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

    }
    /**
     * Updates an existing comment.
     *
     * Create a request for the method "comments.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param content the {@link com.google.api.services.drive.model.Comment}
     * @return the request
     */
    public Update update(String fileId, String commentId, com.google.api.services.drive.model.Comment content) throws java.io.IOException {
      Update result = new Update(fileId, commentId, content);
      initialize(result);
      return result;
    }

    public class Update extends DriveRequest<com.google.api.services.drive.model.Comment> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}";

      /**
       * Updates an existing comment.
       *
       * Create a request for the method "comments.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param content the {@link com.google.api.services.drive.model.Comment}
       * @since 1.13
       */
      protected Update(String fileId, String commentId, com.google.api.services.drive.model.Comment content) {
        super(Drive.this, "PUT", REST_PATH, content, com.google.api.services.drive.model.Comment.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public Update setAlt(String alt) {
        return (Update) super.setAlt(alt);
      }

      @Override
      public Update setFields(String fields) {
        return (Update) super.setFields(fields);
      }

      @Override
      public Update setKey(String key) {
        return (Update) super.setKey(key);
      }

      @Override
      public Update setOauthToken(String oauthToken) {
        return (Update) super.setOauthToken(oauthToken);
      }

      @Override
      public Update setPrettyPrint(Boolean prettyPrint) {
        return (Update) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Update setQuotaUser(String quotaUser) {
        return (Update) super.setQuotaUser(quotaUser);
      }

      @Override
      public Update setUserIp(String userIp) {
        return (Update) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Update setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Update setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Files collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Files.List request = drive.files().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Files files() {
    return new Files();
  }

  /**
   * The "files" collection of methods.
   */
  public class Files {

    /**
     * Creates a copy of the specified file.
     *
     * Create a request for the method "files.copy".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Copy#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to copy.
     * @param content the {@link com.google.api.services.drive.model.File}
     * @return the request
     */
    public Copy copy(String fileId, com.google.api.services.drive.model.File content) throws java.io.IOException {
      Copy result = new Copy(fileId, content);
      initialize(result);
      return result;
    }

    public class Copy extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}/copy";

      /**
       * Creates a copy of the specified file.
       *
       * Create a request for the method "files.copy".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Copy#execute()} method to invoke the remote operation. <p> {@link
       * Copy#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to copy.
       * @param content the {@link com.google.api.services.drive.model.File}
       * @since 1.13
       */
      protected Copy(String fileId, com.google.api.services.drive.model.File content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Copy setAlt(String alt) {
        return (Copy) super.setAlt(alt);
      }

      @Override
      public Copy setFields(String fields) {
        return (Copy) super.setFields(fields);
      }

      @Override
      public Copy setKey(String key) {
        return (Copy) super.setKey(key);
      }

      @Override
      public Copy setOauthToken(String oauthToken) {
        return (Copy) super.setOauthToken(oauthToken);
      }

      @Override
      public Copy setPrettyPrint(Boolean prettyPrint) {
        return (Copy) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Copy setQuotaUser(String quotaUser) {
        return (Copy) super.setQuotaUser(quotaUser);
      }

      @Override
      public Copy setUserIp(String userIp) {
        return (Copy) super.setUserIp(userIp);
      }

      /** The ID of the file to copy. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to copy.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to copy. */
      public Copy setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      @com.google.api.client.util.Key
      private Boolean convert;

      /** Whether to convert this file to the corresponding Google Docs format. [default: false]
       */
      public Boolean getConvert() {
        return convert;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      public Copy setConvert(Boolean convert) {
        this.convert = convert;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to convert this file to the corresponding Google Docs format.
       * </p>
       */
      public boolean isConvert() {
        if (convert == null || convert == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return convert;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      @com.google.api.client.util.Key
      private String ocrLanguage;

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes.
       */
      public String getOcrLanguage() {
        return ocrLanguage;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      public Copy setOcrLanguage(String ocrLanguage) {
        this.ocrLanguage = ocrLanguage;
        return this;
      }

      /** Whether to pin the head revision of the new copy. */
      @com.google.api.client.util.Key
      private Boolean pinned;

      /** Whether to pin the head revision of the new copy. [default: false]
       */
      public Boolean getPinned() {
        return pinned;
      }

      /** Whether to pin the head revision of the new copy. */
      public Copy setPinned(Boolean pinned) {
        this.pinned = pinned;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to pin the head revision of the new copy.
       * </p>
       */
      public boolean isPinned() {
        if (pinned == null || pinned == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return pinned;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      @com.google.api.client.util.Key
      private Boolean ocr;

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. [default: false]
       */
      public Boolean getOcr() {
        return ocr;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      public Copy setOcr(Boolean ocr) {
        this.ocr = ocr;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads.
       * </p>
       */
      public boolean isOcr() {
        if (ocr == null || ocr == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return ocr;
      }

      /** The timed text track name. */
      @com.google.api.client.util.Key
      private String timedTextTrackName;

      /** The timed text track name.
       */
      public String getTimedTextTrackName() {
        return timedTextTrackName;
      }

      /** The timed text track name. */
      public Copy setTimedTextTrackName(String timedTextTrackName) {
        this.timedTextTrackName = timedTextTrackName;
        return this;
      }

      /** The language of the timed text. */
      @com.google.api.client.util.Key
      private String timedTextLanguage;

      /** The language of the timed text.
       */
      public String getTimedTextLanguage() {
        return timedTextLanguage;
      }

      /** The language of the timed text. */
      public Copy setTimedTextLanguage(String timedTextLanguage) {
        this.timedTextLanguage = timedTextLanguage;
        return this;
      }

    }
    /**
     * Permanently deletes a file by ID. Skips the trash.
     *
     * Create a request for the method "files.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to delete.
     * @return the request
     */
    public Delete delete(String fileId) throws java.io.IOException {
      Delete result = new Delete(fileId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}";

      /**
       * Permanently deletes a file by ID. Skips the trash.
       *
       * Create a request for the method "files.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to delete.
       * @since 1.13
       */
      protected Delete(String fileId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the file to delete. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to delete.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to delete. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Gets a file's metadata by ID.
     *
     * Create a request for the method "files.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file in question.
     * @return the request
     */
    public Get get(String fileId) throws java.io.IOException {
      Get result = new Get(fileId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}";

      /**
       * Gets a file's metadata by ID.
       *
       * Create a request for the method "files.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file in question.
       * @since 1.13
       */
      protected Get(String fileId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      initializeMediaDownload();
      }

      /** Upgrade warning: in prior version 1.8 this method was called {@code download}. */
      @Override
      public void executeMediaAndDownloadTo(java.io.OutputStream outputStream) throws java.io.IOException {
        super.executeMediaAndDownloadTo(outputStream);
      }

      @Override
      public java.io.InputStream executeMediaAsInputStream() throws java.io.IOException {
        return super.executeMediaAsInputStream();
      }

      @Override
      public com.google.api.client.http.HttpResponse executeMedia() throws java.io.IOException {
        return super.executeMedia();
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID for the file in question. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file in question.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file in question. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** Whether to update the view date after successfully retrieving the file. */
      @com.google.api.client.util.Key
      private Boolean updateViewedDate;

      /** Whether to update the view date after successfully retrieving the file. [default: false]
       */
      public Boolean getUpdateViewedDate() {
        return updateViewedDate;
      }

      /** Whether to update the view date after successfully retrieving the file. */
      public Get setUpdateViewedDate(Boolean updateViewedDate) {
        this.updateViewedDate = updateViewedDate;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to update the view date after successfully retrieving the file.
       * </p>
       */
      public boolean isUpdateViewedDate() {
        if (updateViewedDate == null || updateViewedDate == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return updateViewedDate;
      }

      /** This parameter is deprecated and has no function. */
      @com.google.api.client.util.Key
      private String projection;

      /** This parameter is deprecated and has no function.
       */
      public String getProjection() {
        return projection;
      }

      /** This parameter is deprecated and has no function. */
      public Get setProjection(String projection) {
        this.projection = projection;
        return this;
      }

    }
    /**
     * Insert a new file.
     *
     * Create a request for the method "files.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param content the {@link com.google.api.services.drive.model.File}
     * @return the request
     */
    public Insert insert(com.google.api.services.drive.model.File content) throws java.io.IOException {
      Insert result = new Insert(content);
      initialize(result);
      return result;
    }

    /**
     * Insert a new file.
     *
     * Create a request for the method "files.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * <p>
     * This method should be used for uploading media content.
     * </p>
     *
     *
     * @param content the {@link com.google.api.services.drive.model.File} media metadata or {@code null} if none
     * @param mediaContent The media HTTP content or {@code null} if none.
     * @return the request
     * @throws java.io.IOException if the initialization of the request fails
     */
    public Insert insert(com.google.api.services.drive.model.File content,
        com.google.api.client.http.AbstractInputStreamContent mediaContent) throws java.io.IOException {
      Insert result = new Insert(content, mediaContent);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files";

      /**
       * Insert a new file.
       *
       * Create a request for the method "files.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param content the {@link com.google.api.services.drive.model.File}
       * @since 1.13
       */
      protected Insert(com.google.api.services.drive.model.File content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.File.class);
      }

      /**
       * Insert a new file.
       *
       * Create a request for the method "files.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * <p>
       * This constructor should be used for uploading media content.
       * </p>
       *
       *
       * @param content the {@link com.google.api.services.drive.model.File} media metadata or {@code null} if none
       * @param mediaContent The media HTTP content or {@code null} if none.
       * @since 1.13
       */
      protected Insert(com.google.api.services.drive.model.File content,
          com.google.api.client.http.AbstractInputStreamContent mediaContent) {
        super(Drive.this, "POST", "/upload/" + getServicePath() + REST_PATH, content, com.google.api.services.drive.model.File.class);
        initializeMediaUpload(mediaContent);
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      @com.google.api.client.util.Key
      private Boolean convert;

      /** Whether to convert this file to the corresponding Google Docs format. [default: false]
       */
      public Boolean getConvert() {
        return convert;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      public Insert setConvert(Boolean convert) {
        this.convert = convert;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to convert this file to the corresponding Google Docs format.
       * </p>
       */
      public boolean isConvert() {
        if (convert == null || convert == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return convert;
      }

      /** Whether to use the content as indexable text. */
      @com.google.api.client.util.Key
      private Boolean useContentAsIndexableText;

      /** Whether to use the content as indexable text. [default: false]
       */
      public Boolean getUseContentAsIndexableText() {
        return useContentAsIndexableText;
      }

      /** Whether to use the content as indexable text. */
      public Insert setUseContentAsIndexableText(Boolean useContentAsIndexableText) {
        this.useContentAsIndexableText = useContentAsIndexableText;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to use the content as indexable text.
       * </p>
       */
      public boolean isUseContentAsIndexableText() {
        if (useContentAsIndexableText == null || useContentAsIndexableText == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return useContentAsIndexableText;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      @com.google.api.client.util.Key
      private String ocrLanguage;

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes.
       */
      public String getOcrLanguage() {
        return ocrLanguage;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      public Insert setOcrLanguage(String ocrLanguage) {
        this.ocrLanguage = ocrLanguage;
        return this;
      }

      /** Whether to pin the head revision of the uploaded file. */
      @com.google.api.client.util.Key
      private Boolean pinned;

      /** Whether to pin the head revision of the uploaded file. [default: false]
       */
      public Boolean getPinned() {
        return pinned;
      }

      /** Whether to pin the head revision of the uploaded file. */
      public Insert setPinned(Boolean pinned) {
        this.pinned = pinned;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to pin the head revision of the uploaded file.
       * </p>
       */
      public boolean isPinned() {
        if (pinned == null || pinned == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return pinned;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      @com.google.api.client.util.Key
      private Boolean ocr;

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. [default: false]
       */
      public Boolean getOcr() {
        return ocr;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      public Insert setOcr(Boolean ocr) {
        this.ocr = ocr;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads.
       * </p>
       */
      public boolean isOcr() {
        if (ocr == null || ocr == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return ocr;
      }

      /** The timed text track name. */
      @com.google.api.client.util.Key
      private String timedTextTrackName;

      /** The timed text track name.
       */
      public String getTimedTextTrackName() {
        return timedTextTrackName;
      }

      /** The timed text track name. */
      public Insert setTimedTextTrackName(String timedTextTrackName) {
        this.timedTextTrackName = timedTextTrackName;
        return this;
      }

      /** The language of the timed text. */
      @com.google.api.client.util.Key
      private String timedTextLanguage;

      /** The language of the timed text.
       */
      public String getTimedTextLanguage() {
        return timedTextLanguage;
      }

      /** The language of the timed text. */
      public Insert setTimedTextLanguage(String timedTextLanguage) {
        this.timedTextLanguage = timedTextLanguage;
        return this;
      }

    }
    /**
     * Lists the user's files.
     *
     * Create a request for the method "files.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public List list() throws java.io.IOException {
      List result = new List();
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.FileList> {

      private static final String REST_PATH = "files";

      /**
       * Lists the user's files.
       *
       * Create a request for the method "files.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected List() {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.FileList.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** Query string for searching files. */
      @com.google.api.client.util.Key
      private String q;

      /** Query string for searching files.
       */
      public String getQ() {
        return q;
      }

      /** Query string for searching files. */
      public List setQ(String q) {
        this.q = q;
        return this;
      }

      /** Page token for files. */
      @com.google.api.client.util.Key
      private String pageToken;

      /** Page token for files.
       */
      public String getPageToken() {
        return pageToken;
      }

      /** Page token for files. */
      public List setPageToken(String pageToken) {
        this.pageToken = pageToken;
        return this;
      }

      /** This parameter is deprecated and has no function. */
      @com.google.api.client.util.Key
      private String projection;

      /** This parameter is deprecated and has no function.
       */
      public String getProjection() {
        return projection;
      }

      /** This parameter is deprecated and has no function. */
      public List setProjection(String projection) {
        this.projection = projection;
        return this;
      }

      /** Maximum number of files to return. */
      @com.google.api.client.util.Key
      private Integer maxResults;

      /** Maximum number of files to return. [default: 100] [minimum: 0]
       */
      public Integer getMaxResults() {
        return maxResults;
      }

      /** Maximum number of files to return. */
      public List setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
      }

    }
    /**
     * Updates file metadata and/or content. This method supports patch semantics.
     *
     * Create a request for the method "files.patch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Patch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to update.
     * @param content the {@link com.google.api.services.drive.model.File}
     * @return the request
     */
    public Patch patch(String fileId, com.google.api.services.drive.model.File content) throws java.io.IOException {
      Patch result = new Patch(fileId, content);
      initialize(result);
      return result;
    }

    public class Patch extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}";

      /**
       * Updates file metadata and/or content. This method supports patch semantics.
       *
       * Create a request for the method "files.patch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Patch#execute()} method to invoke the remote operation. <p> {@link
       * Patch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to update.
       * @param content the {@link com.google.api.services.drive.model.File}
       * @since 1.13
       */
      protected Patch(String fileId, com.google.api.services.drive.model.File content) {
        super(Drive.this, "PATCH", REST_PATH, content, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Patch setAlt(String alt) {
        return (Patch) super.setAlt(alt);
      }

      @Override
      public Patch setFields(String fields) {
        return (Patch) super.setFields(fields);
      }

      @Override
      public Patch setKey(String key) {
        return (Patch) super.setKey(key);
      }

      @Override
      public Patch setOauthToken(String oauthToken) {
        return (Patch) super.setOauthToken(oauthToken);
      }

      @Override
      public Patch setPrettyPrint(Boolean prettyPrint) {
        return (Patch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Patch setQuotaUser(String quotaUser) {
        return (Patch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Patch setUserIp(String userIp) {
        return (Patch) super.setUserIp(userIp);
      }

      /** The ID of the file to update. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to update.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to update. */
      public Patch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      @com.google.api.client.util.Key
      private Boolean convert;

      /** Whether to convert this file to the corresponding Google Docs format. [default: false]
       */
      public Boolean getConvert() {
        return convert;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      public Patch setConvert(Boolean convert) {
        this.convert = convert;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to convert this file to the corresponding Google Docs format.
       * </p>
       */
      public boolean isConvert() {
        if (convert == null || convert == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return convert;
      }

      /** Whether to update the view date after successfully updating the file. */
      @com.google.api.client.util.Key
      private Boolean updateViewedDate;

      /** Whether to update the view date after successfully updating the file. [default: true]
       */
      public Boolean getUpdateViewedDate() {
        return updateViewedDate;
      }

      /** Whether to update the view date after successfully updating the file. */
      public Patch setUpdateViewedDate(Boolean updateViewedDate) {
        this.updateViewedDate = updateViewedDate;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to update the view date after successfully updating the file.
       * </p>
       */
      public boolean isUpdateViewedDate() {
        if (updateViewedDate == null || updateViewedDate == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return updateViewedDate;
      }

      /** Whether to set the modified date with the supplied modified date. */
      @com.google.api.client.util.Key
      private Boolean setModifiedDate;

      /** Whether to set the modified date with the supplied modified date. [default: false]
       */
      public Boolean getSetModifiedDate() {
        return setModifiedDate;
      }

      /** Whether to set the modified date with the supplied modified date. */
      public Patch setSetModifiedDate(Boolean setModifiedDate) {
        this.setModifiedDate = setModifiedDate;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to set the modified date with the supplied modified date.
       * </p>
       */
      public boolean isSetModifiedDate() {
        if (setModifiedDate == null || setModifiedDate == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return setModifiedDate;
      }

      /** Whether to use the content as indexable text. */
      @com.google.api.client.util.Key
      private Boolean useContentAsIndexableText;

      /** Whether to use the content as indexable text. [default: false]
       */
      public Boolean getUseContentAsIndexableText() {
        return useContentAsIndexableText;
      }

      /** Whether to use the content as indexable text. */
      public Patch setUseContentAsIndexableText(Boolean useContentAsIndexableText) {
        this.useContentAsIndexableText = useContentAsIndexableText;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to use the content as indexable text.
       * </p>
       */
      public boolean isUseContentAsIndexableText() {
        if (useContentAsIndexableText == null || useContentAsIndexableText == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return useContentAsIndexableText;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      @com.google.api.client.util.Key
      private String ocrLanguage;

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes.
       */
      public String getOcrLanguage() {
        return ocrLanguage;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      public Patch setOcrLanguage(String ocrLanguage) {
        this.ocrLanguage = ocrLanguage;
        return this;
      }

      /** Whether to pin the new revision. */
      @com.google.api.client.util.Key
      private Boolean pinned;

      /** Whether to pin the new revision. [default: false]
       */
      public Boolean getPinned() {
        return pinned;
      }

      /** Whether to pin the new revision. */
      public Patch setPinned(Boolean pinned) {
        this.pinned = pinned;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to pin the new revision.
       * </p>
       */
      public boolean isPinned() {
        if (pinned == null || pinned == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return pinned;
      }

      /**
     * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     * current head revision is replaced. If true, a new blob is created as head revision, and
     * previous revisions are preserved (causing increased use of the user's data storage quota).
     */
      @com.google.api.client.util.Key
      private Boolean newRevision;

      /** Whether a blob upload should create a new revision. If not set or false, the blob data in the
     current head revision is replaced. If true, a new blob is created as head revision, and previous
     revisions are preserved (causing increased use of the user's data storage quota). [default: true]
       */
      public Boolean getNewRevision() {
        return newRevision;
      }

      /**
     * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     * current head revision is replaced. If true, a new blob is created as head revision, and
     * previous revisions are preserved (causing increased use of the user's data storage quota).
     */
      public Patch setNewRevision(Boolean newRevision) {
        this.newRevision = newRevision;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     current head revision is replaced. If true, a new blob is created as head revision, and previous
     revisions are preserved (causing increased use of the user's data storage quota).
       * </p>
       */
      public boolean isNewRevision() {
        if (newRevision == null || newRevision == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return newRevision;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      @com.google.api.client.util.Key
      private Boolean ocr;

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. [default: false]
       */
      public Boolean getOcr() {
        return ocr;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      public Patch setOcr(Boolean ocr) {
        this.ocr = ocr;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads.
       * </p>
       */
      public boolean isOcr() {
        if (ocr == null || ocr == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return ocr;
      }

      /** The language of the timed text. */
      @com.google.api.client.util.Key
      private String timedTextLanguage;

      /** The language of the timed text.
       */
      public String getTimedTextLanguage() {
        return timedTextLanguage;
      }

      /** The language of the timed text. */
      public Patch setTimedTextLanguage(String timedTextLanguage) {
        this.timedTextLanguage = timedTextLanguage;
        return this;
      }

      /** The timed text track name. */
      @com.google.api.client.util.Key
      private String timedTextTrackName;

      /** The timed text track name.
       */
      public String getTimedTextTrackName() {
        return timedTextTrackName;
      }

      /** The timed text track name. */
      public Patch setTimedTextTrackName(String timedTextTrackName) {
        this.timedTextTrackName = timedTextTrackName;
        return this;
      }

    }
    /**
     * Set the file's updated time to the current server time.
     *
     * Create a request for the method "files.touch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Touch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to update.
     * @return the request
     */
    public Touch touch(String fileId) throws java.io.IOException {
      Touch result = new Touch(fileId);
      initialize(result);
      return result;
    }

    public class Touch extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}/touch";

      /**
       * Set the file's updated time to the current server time.
       *
       * Create a request for the method "files.touch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Touch#execute()} method to invoke the remote operation. <p> {@link
       * Touch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to update.
       * @since 1.13
       */
      protected Touch(String fileId) {
        super(Drive.this, "POST", REST_PATH, null, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Touch setAlt(String alt) {
        return (Touch) super.setAlt(alt);
      }

      @Override
      public Touch setFields(String fields) {
        return (Touch) super.setFields(fields);
      }

      @Override
      public Touch setKey(String key) {
        return (Touch) super.setKey(key);
      }

      @Override
      public Touch setOauthToken(String oauthToken) {
        return (Touch) super.setOauthToken(oauthToken);
      }

      @Override
      public Touch setPrettyPrint(Boolean prettyPrint) {
        return (Touch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Touch setQuotaUser(String quotaUser) {
        return (Touch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Touch setUserIp(String userIp) {
        return (Touch) super.setUserIp(userIp);
      }

      /** The ID of the file to update. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to update.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to update. */
      public Touch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Moves a file to the trash.
     *
     * Create a request for the method "files.trash".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Trash#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to trash.
     * @return the request
     */
    public Trash trash(String fileId) throws java.io.IOException {
      Trash result = new Trash(fileId);
      initialize(result);
      return result;
    }

    public class Trash extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}/trash";

      /**
       * Moves a file to the trash.
       *
       * Create a request for the method "files.trash".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Trash#execute()} method to invoke the remote operation. <p> {@link
       * Trash#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to trash.
       * @since 1.13
       */
      protected Trash(String fileId) {
        super(Drive.this, "POST", REST_PATH, null, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Trash setAlt(String alt) {
        return (Trash) super.setAlt(alt);
      }

      @Override
      public Trash setFields(String fields) {
        return (Trash) super.setFields(fields);
      }

      @Override
      public Trash setKey(String key) {
        return (Trash) super.setKey(key);
      }

      @Override
      public Trash setOauthToken(String oauthToken) {
        return (Trash) super.setOauthToken(oauthToken);
      }

      @Override
      public Trash setPrettyPrint(Boolean prettyPrint) {
        return (Trash) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Trash setQuotaUser(String quotaUser) {
        return (Trash) super.setQuotaUser(quotaUser);
      }

      @Override
      public Trash setUserIp(String userIp) {
        return (Trash) super.setUserIp(userIp);
      }

      /** The ID of the file to trash. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to trash.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to trash. */
      public Trash setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Restores a file from the trash.
     *
     * Create a request for the method "files.untrash".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Untrash#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to untrash.
     * @return the request
     */
    public Untrash untrash(String fileId) throws java.io.IOException {
      Untrash result = new Untrash(fileId);
      initialize(result);
      return result;
    }

    public class Untrash extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}/untrash";

      /**
       * Restores a file from the trash.
       *
       * Create a request for the method "files.untrash".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Untrash#execute()} method to invoke the remote operation. <p>
       * {@link Untrash#initialize(AbstractGoogleClientRequest)} must be called to initialize this
       * instance immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to untrash.
       * @since 1.13
       */
      protected Untrash(String fileId) {
        super(Drive.this, "POST", REST_PATH, null, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Untrash setAlt(String alt) {
        return (Untrash) super.setAlt(alt);
      }

      @Override
      public Untrash setFields(String fields) {
        return (Untrash) super.setFields(fields);
      }

      @Override
      public Untrash setKey(String key) {
        return (Untrash) super.setKey(key);
      }

      @Override
      public Untrash setOauthToken(String oauthToken) {
        return (Untrash) super.setOauthToken(oauthToken);
      }

      @Override
      public Untrash setPrettyPrint(Boolean prettyPrint) {
        return (Untrash) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Untrash setQuotaUser(String quotaUser) {
        return (Untrash) super.setQuotaUser(quotaUser);
      }

      @Override
      public Untrash setUserIp(String userIp) {
        return (Untrash) super.setUserIp(userIp);
      }

      /** The ID of the file to untrash. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to untrash.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to untrash. */
      public Untrash setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Updates file metadata and/or content.
     *
     * Create a request for the method "files.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file to update.
     * @param content the {@link com.google.api.services.drive.model.File}
     * @return the request
     */
    public Update update(String fileId, com.google.api.services.drive.model.File content) throws java.io.IOException {
      Update result = new Update(fileId, content);
      initialize(result);
      return result;
    }

    /**
     * Updates file metadata and/or content.
     *
     * Create a request for the method "files.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * <p>
     * This method should be used for uploading media content.
     * </p>
     *
     * @param fileId The ID of the file to update.
     * @param content the {@link com.google.api.services.drive.model.File} media metadata or {@code null} if none
     * @param mediaContent The media HTTP content or {@code null} if none.
     * @return the request
     * @throws java.io.IOException if the initialization of the request fails
     */
    public Update update(String fileId, com.google.api.services.drive.model.File content,
        com.google.api.client.http.AbstractInputStreamContent mediaContent) throws java.io.IOException {
      Update result = new Update(fileId, content, mediaContent);
      initialize(result);
      return result;
    }

    public class Update extends DriveRequest<com.google.api.services.drive.model.File> {

      private static final String REST_PATH = "files/{fileId}";

      /**
       * Updates file metadata and/or content.
       *
       * Create a request for the method "files.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file to update.
       * @param content the {@link com.google.api.services.drive.model.File}
       * @since 1.13
       */
      protected Update(String fileId, com.google.api.services.drive.model.File content) {
        super(Drive.this, "PUT", REST_PATH, content, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      /**
       * Updates file metadata and/or content.
       *
       * Create a request for the method "files.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * <p>
       * This constructor should be used for uploading media content.
       * </p>
       *
       * @param fileId The ID of the file to update.
       * @param content the {@link com.google.api.services.drive.model.File} media metadata or {@code null} if none
       * @param mediaContent The media HTTP content or {@code null} if none.
       * @since 1.13
       */
      protected Update(String fileId, com.google.api.services.drive.model.File content,
          com.google.api.client.http.AbstractInputStreamContent mediaContent) {
        super(Drive.this, "PUT", "/upload/" + getServicePath() + REST_PATH, content, com.google.api.services.drive.model.File.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        initializeMediaUpload(mediaContent);
      }

      @Override
      public Update setAlt(String alt) {
        return (Update) super.setAlt(alt);
      }

      @Override
      public Update setFields(String fields) {
        return (Update) super.setFields(fields);
      }

      @Override
      public Update setKey(String key) {
        return (Update) super.setKey(key);
      }

      @Override
      public Update setOauthToken(String oauthToken) {
        return (Update) super.setOauthToken(oauthToken);
      }

      @Override
      public Update setPrettyPrint(Boolean prettyPrint) {
        return (Update) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Update setQuotaUser(String quotaUser) {
        return (Update) super.setQuotaUser(quotaUser);
      }

      @Override
      public Update setUserIp(String userIp) {
        return (Update) super.setUserIp(userIp);
      }

      /** The ID of the file to update. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file to update.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file to update. */
      public Update setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      @com.google.api.client.util.Key
      private Boolean convert;

      /** Whether to convert this file to the corresponding Google Docs format. [default: false]
       */
      public Boolean getConvert() {
        return convert;
      }

      /** Whether to convert this file to the corresponding Google Docs format. */
      public Update setConvert(Boolean convert) {
        this.convert = convert;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to convert this file to the corresponding Google Docs format.
       * </p>
       */
      public boolean isConvert() {
        if (convert == null || convert == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return convert;
      }

      /** Whether to update the view date after successfully updating the file. */
      @com.google.api.client.util.Key
      private Boolean updateViewedDate;

      /** Whether to update the view date after successfully updating the file. [default: true]
       */
      public Boolean getUpdateViewedDate() {
        return updateViewedDate;
      }

      /** Whether to update the view date after successfully updating the file. */
      public Update setUpdateViewedDate(Boolean updateViewedDate) {
        this.updateViewedDate = updateViewedDate;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to update the view date after successfully updating the file.
       * </p>
       */
      public boolean isUpdateViewedDate() {
        if (updateViewedDate == null || updateViewedDate == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return updateViewedDate;
      }

      /** Whether to set the modified date with the supplied modified date. */
      @com.google.api.client.util.Key
      private Boolean setModifiedDate;

      /** Whether to set the modified date with the supplied modified date. [default: false]
       */
      public Boolean getSetModifiedDate() {
        return setModifiedDate;
      }

      /** Whether to set the modified date with the supplied modified date. */
      public Update setSetModifiedDate(Boolean setModifiedDate) {
        this.setModifiedDate = setModifiedDate;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to set the modified date with the supplied modified date.
       * </p>
       */
      public boolean isSetModifiedDate() {
        if (setModifiedDate == null || setModifiedDate == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return setModifiedDate;
      }

      /** Whether to use the content as indexable text. */
      @com.google.api.client.util.Key
      private Boolean useContentAsIndexableText;

      /** Whether to use the content as indexable text. [default: false]
       */
      public Boolean getUseContentAsIndexableText() {
        return useContentAsIndexableText;
      }

      /** Whether to use the content as indexable text. */
      public Update setUseContentAsIndexableText(Boolean useContentAsIndexableText) {
        this.useContentAsIndexableText = useContentAsIndexableText;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to use the content as indexable text.
       * </p>
       */
      public boolean isUseContentAsIndexableText() {
        if (useContentAsIndexableText == null || useContentAsIndexableText == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return useContentAsIndexableText;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      @com.google.api.client.util.Key
      private String ocrLanguage;

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes.
       */
      public String getOcrLanguage() {
        return ocrLanguage;
      }

      /** If ocr is true, hints at the language to use. Valid values are ISO 639-1 codes. */
      public Update setOcrLanguage(String ocrLanguage) {
        this.ocrLanguage = ocrLanguage;
        return this;
      }

      /** Whether to pin the new revision. */
      @com.google.api.client.util.Key
      private Boolean pinned;

      /** Whether to pin the new revision. [default: false]
       */
      public Boolean getPinned() {
        return pinned;
      }

      /** Whether to pin the new revision. */
      public Update setPinned(Boolean pinned) {
        this.pinned = pinned;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to pin the new revision.
       * </p>
       */
      public boolean isPinned() {
        if (pinned == null || pinned == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return pinned;
      }

      /**
     * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     * current head revision is replaced. If true, a new blob is created as head revision, and
     * previous revisions are preserved (causing increased use of the user's data storage quota).
     */
      @com.google.api.client.util.Key
      private Boolean newRevision;

      /** Whether a blob upload should create a new revision. If not set or false, the blob data in the
     current head revision is replaced. If true, a new blob is created as head revision, and previous
     revisions are preserved (causing increased use of the user's data storage quota). [default: true]
       */
      public Boolean getNewRevision() {
        return newRevision;
      }

      /**
     * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     * current head revision is replaced. If true, a new blob is created as head revision, and
     * previous revisions are preserved (causing increased use of the user's data storage quota).
     */
      public Update setNewRevision(Boolean newRevision) {
        this.newRevision = newRevision;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether a blob upload should create a new revision. If not set or false, the blob data in the
     current head revision is replaced. If true, a new blob is created as head revision, and previous
     revisions are preserved (causing increased use of the user's data storage quota).
       * </p>
       */
      public boolean isNewRevision() {
        if (newRevision == null || newRevision == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return newRevision;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      @com.google.api.client.util.Key
      private Boolean ocr;

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. [default: false]
       */
      public Boolean getOcr() {
        return ocr;
      }

      /** Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads. */
      public Update setOcr(Boolean ocr) {
        this.ocr = ocr;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to attempt OCR on .jpg, .png, .gif, or .pdf uploads.
       * </p>
       */
      public boolean isOcr() {
        if (ocr == null || ocr == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return ocr;
      }

      /** The language of the timed text. */
      @com.google.api.client.util.Key
      private String timedTextLanguage;

      /** The language of the timed text.
       */
      public String getTimedTextLanguage() {
        return timedTextLanguage;
      }

      /** The language of the timed text. */
      public Update setTimedTextLanguage(String timedTextLanguage) {
        this.timedTextLanguage = timedTextLanguage;
        return this;
      }

      /** The timed text track name. */
      @com.google.api.client.util.Key
      private String timedTextTrackName;

      /** The timed text track name.
       */
      public String getTimedTextTrackName() {
        return timedTextTrackName;
      }

      /** The timed text track name. */
      public Update setTimedTextTrackName(String timedTextTrackName) {
        this.timedTextTrackName = timedTextTrackName;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Parents collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Parents.List request = drive.parents().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Parents parents() {
    return new Parents();
  }

  /**
   * The "parents" collection of methods.
   */
  public class Parents {

    /**
     * Removes a parent from a file.
     *
     * Create a request for the method "parents.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param parentId The ID of the parent.
     * @return the request
     */
    public Delete delete(String fileId, String parentId) throws java.io.IOException {
      Delete result = new Delete(fileId, parentId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}/parents/{parentId}";

      /**
       * Removes a parent from a file.
       *
       * Create a request for the method "parents.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param parentId The ID of the parent.
       * @since 1.13
       */
      protected Delete(String fileId, String parentId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.parentId = Preconditions.checkNotNull(parentId, "Required parameter parentId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the parent. */
      @com.google.api.client.util.Key
      private String parentId;

      /** The ID of the parent.
       */
      public String getParentId() {
        return parentId;
      }

      /** The ID of the parent. */
      public Delete setParentId(String parentId) {
        this.parentId = parentId;
        return this;
      }

    }
    /**
     * Gets a specific parent reference.
     *
     * Create a request for the method "parents.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param parentId The ID of the parent.
     * @return the request
     */
    public Get get(String fileId, String parentId) throws java.io.IOException {
      Get result = new Get(fileId, parentId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.ParentReference> {

      private static final String REST_PATH = "files/{fileId}/parents/{parentId}";

      /**
       * Gets a specific parent reference.
       *
       * Create a request for the method "parents.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param parentId The ID of the parent.
       * @since 1.13
       */
      protected Get(String fileId, String parentId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.ParentReference.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.parentId = Preconditions.checkNotNull(parentId, "Required parameter parentId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the parent. */
      @com.google.api.client.util.Key
      private String parentId;

      /** The ID of the parent.
       */
      public String getParentId() {
        return parentId;
      }

      /** The ID of the parent. */
      public Get setParentId(String parentId) {
        this.parentId = parentId;
        return this;
      }

    }
    /**
     * Adds a parent folder for a file.
     *
     * Create a request for the method "parents.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param content the {@link com.google.api.services.drive.model.ParentReference}
     * @return the request
     */
    public Insert insert(String fileId, com.google.api.services.drive.model.ParentReference content) throws java.io.IOException {
      Insert result = new Insert(fileId, content);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.ParentReference> {

      private static final String REST_PATH = "files/{fileId}/parents";

      /**
       * Adds a parent folder for a file.
       *
       * Create a request for the method "parents.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param content the {@link com.google.api.services.drive.model.ParentReference}
       * @since 1.13
       */
      protected Insert(String fileId, com.google.api.services.drive.model.ParentReference content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.ParentReference.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Insert setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Lists a file's parents.
     *
     * Create a request for the method "parents.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @return the request
     */
    public List list(String fileId) throws java.io.IOException {
      List result = new List(fileId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.ParentList> {

      private static final String REST_PATH = "files/{fileId}/parents";

      /**
       * Lists a file's parents.
       *
       * Create a request for the method "parents.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @since 1.13
       */
      protected List(String fileId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.ParentList.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public List setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Permissions collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Permissions.List request = drive.permissions().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Permissions permissions() {
    return new Permissions();
  }

  /**
   * The "permissions" collection of methods.
   */
  public class Permissions {

    /**
     * Deletes a permission from a file.
     *
     * Create a request for the method "permissions.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param permissionId The ID for the permission.
     * @return the request
     */
    public Delete delete(String fileId, String permissionId) throws java.io.IOException {
      Delete result = new Delete(fileId, permissionId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}/permissions/{permissionId}";

      /**
       * Deletes a permission from a file.
       *
       * Create a request for the method "permissions.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param permissionId The ID for the permission.
       * @since 1.13
       */
      protected Delete(String fileId, String permissionId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.permissionId = Preconditions.checkNotNull(permissionId, "Required parameter permissionId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the permission. */
      @com.google.api.client.util.Key
      private String permissionId;

      /** The ID for the permission.
       */
      public String getPermissionId() {
        return permissionId;
      }

      /** The ID for the permission. */
      public Delete setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
      }

    }
    /**
     * Gets a permission by ID.
     *
     * Create a request for the method "permissions.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param permissionId The ID for the permission.
     * @return the request
     */
    public Get get(String fileId, String permissionId) throws java.io.IOException {
      Get result = new Get(fileId, permissionId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.Permission> {

      private static final String REST_PATH = "files/{fileId}/permissions/{permissionId}";

      /**
       * Gets a permission by ID.
       *
       * Create a request for the method "permissions.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param permissionId The ID for the permission.
       * @since 1.13
       */
      protected Get(String fileId, String permissionId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.Permission.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.permissionId = Preconditions.checkNotNull(permissionId, "Required parameter permissionId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the permission. */
      @com.google.api.client.util.Key
      private String permissionId;

      /** The ID for the permission.
       */
      public String getPermissionId() {
        return permissionId;
      }

      /** The ID for the permission. */
      public Get setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
      }

    }
    /**
     * Inserts a permission for a file.
     *
     * Create a request for the method "permissions.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param content the {@link com.google.api.services.drive.model.Permission}
     * @return the request
     */
    public Insert insert(String fileId, com.google.api.services.drive.model.Permission content) throws java.io.IOException {
      Insert result = new Insert(fileId, content);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.Permission> {

      private static final String REST_PATH = "files/{fileId}/permissions";

      /**
       * Inserts a permission for a file.
       *
       * Create a request for the method "permissions.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param content the {@link com.google.api.services.drive.model.Permission}
       * @since 1.13
       */
      protected Insert(String fileId, com.google.api.services.drive.model.Permission content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.Permission.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Insert setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** A custom message to include in notification emails. */
      @com.google.api.client.util.Key
      private String emailMessage;

      /** A custom message to include in notification emails.
       */
      public String getEmailMessage() {
        return emailMessage;
      }

      /** A custom message to include in notification emails. */
      public Insert setEmailMessage(String emailMessage) {
        this.emailMessage = emailMessage;
        return this;
      }

      /** Whether to send notification emails when sharing to users or groups. */
      @com.google.api.client.util.Key
      private Boolean sendNotificationEmails;

      /** Whether to send notification emails when sharing to users or groups. [default: true]
       */
      public Boolean getSendNotificationEmails() {
        return sendNotificationEmails;
      }

      /** Whether to send notification emails when sharing to users or groups. */
      public Insert setSendNotificationEmails(Boolean sendNotificationEmails) {
        this.sendNotificationEmails = sendNotificationEmails;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether to send notification emails when sharing to users or groups.
       * </p>
       */
      public boolean isSendNotificationEmails() {
        if (sendNotificationEmails == null || sendNotificationEmails == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return true;
        }
        return sendNotificationEmails;
      }

    }
    /**
     * Lists a file's permissions.
     *
     * Create a request for the method "permissions.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @return the request
     */
    public List list(String fileId) throws java.io.IOException {
      List result = new List(fileId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.PermissionList> {

      private static final String REST_PATH = "files/{fileId}/permissions";

      /**
       * Lists a file's permissions.
       *
       * Create a request for the method "permissions.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @since 1.13
       */
      protected List(String fileId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.PermissionList.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public List setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Updates a permission. This method supports patch semantics.
     *
     * Create a request for the method "permissions.patch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Patch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param permissionId The ID for the permission.
     * @param content the {@link com.google.api.services.drive.model.Permission}
     * @return the request
     */
    public Patch patch(String fileId, String permissionId, com.google.api.services.drive.model.Permission content) throws java.io.IOException {
      Patch result = new Patch(fileId, permissionId, content);
      initialize(result);
      return result;
    }

    public class Patch extends DriveRequest<com.google.api.services.drive.model.Permission> {

      private static final String REST_PATH = "files/{fileId}/permissions/{permissionId}";

      /**
       * Updates a permission. This method supports patch semantics.
       *
       * Create a request for the method "permissions.patch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Patch#execute()} method to invoke the remote operation. <p> {@link
       * Patch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param permissionId The ID for the permission.
       * @param content the {@link com.google.api.services.drive.model.Permission}
       * @since 1.13
       */
      protected Patch(String fileId, String permissionId, com.google.api.services.drive.model.Permission content) {
        super(Drive.this, "PATCH", REST_PATH, content, com.google.api.services.drive.model.Permission.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.permissionId = Preconditions.checkNotNull(permissionId, "Required parameter permissionId must be specified.");
      }

      @Override
      public Patch setAlt(String alt) {
        return (Patch) super.setAlt(alt);
      }

      @Override
      public Patch setFields(String fields) {
        return (Patch) super.setFields(fields);
      }

      @Override
      public Patch setKey(String key) {
        return (Patch) super.setKey(key);
      }

      @Override
      public Patch setOauthToken(String oauthToken) {
        return (Patch) super.setOauthToken(oauthToken);
      }

      @Override
      public Patch setPrettyPrint(Boolean prettyPrint) {
        return (Patch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Patch setQuotaUser(String quotaUser) {
        return (Patch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Patch setUserIp(String userIp) {
        return (Patch) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Patch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the permission. */
      @com.google.api.client.util.Key
      private String permissionId;

      /** The ID for the permission.
       */
      public String getPermissionId() {
        return permissionId;
      }

      /** The ID for the permission. */
      public Patch setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
      }

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. */
      @com.google.api.client.util.Key
      private Boolean transferOwnership;

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. [default:
     false]
       */
      public Boolean getTransferOwnership() {
        return transferOwnership;
      }

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. */
      public Patch setTransferOwnership(Boolean transferOwnership) {
        this.transferOwnership = transferOwnership;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether changing a role to 'owner' should also downgrade the current owners to writers.
       * </p>
       */
      public boolean isTransferOwnership() {
        if (transferOwnership == null || transferOwnership == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return transferOwnership;
      }

    }
    /**
     * Updates a permission.
     *
     * Create a request for the method "permissions.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param permissionId The ID for the permission.
     * @param content the {@link com.google.api.services.drive.model.Permission}
     * @return the request
     */
    public Update update(String fileId, String permissionId, com.google.api.services.drive.model.Permission content) throws java.io.IOException {
      Update result = new Update(fileId, permissionId, content);
      initialize(result);
      return result;
    }

    public class Update extends DriveRequest<com.google.api.services.drive.model.Permission> {

      private static final String REST_PATH = "files/{fileId}/permissions/{permissionId}";

      /**
       * Updates a permission.
       *
       * Create a request for the method "permissions.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param permissionId The ID for the permission.
       * @param content the {@link com.google.api.services.drive.model.Permission}
       * @since 1.13
       */
      protected Update(String fileId, String permissionId, com.google.api.services.drive.model.Permission content) {
        super(Drive.this, "PUT", REST_PATH, content, com.google.api.services.drive.model.Permission.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.permissionId = Preconditions.checkNotNull(permissionId, "Required parameter permissionId must be specified.");
      }

      @Override
      public Update setAlt(String alt) {
        return (Update) super.setAlt(alt);
      }

      @Override
      public Update setFields(String fields) {
        return (Update) super.setFields(fields);
      }

      @Override
      public Update setKey(String key) {
        return (Update) super.setKey(key);
      }

      @Override
      public Update setOauthToken(String oauthToken) {
        return (Update) super.setOauthToken(oauthToken);
      }

      @Override
      public Update setPrettyPrint(Boolean prettyPrint) {
        return (Update) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Update setQuotaUser(String quotaUser) {
        return (Update) super.setQuotaUser(quotaUser);
      }

      @Override
      public Update setUserIp(String userIp) {
        return (Update) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Update setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the permission. */
      @com.google.api.client.util.Key
      private String permissionId;

      /** The ID for the permission.
       */
      public String getPermissionId() {
        return permissionId;
      }

      /** The ID for the permission. */
      public Update setPermissionId(String permissionId) {
        this.permissionId = permissionId;
        return this;
      }

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. */
      @com.google.api.client.util.Key
      private Boolean transferOwnership;

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. [default:
     false]
       */
      public Boolean getTransferOwnership() {
        return transferOwnership;
      }

      /** Whether changing a role to 'owner' should also downgrade the current owners to writers. */
      public Update setTransferOwnership(Boolean transferOwnership) {
        this.transferOwnership = transferOwnership;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * Whether changing a role to 'owner' should also downgrade the current owners to writers.
       * </p>
       */
      public boolean isTransferOwnership() {
        if (transferOwnership == null || transferOwnership == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return transferOwnership;
      }

    }

  }

  /**
   * An accessor for creating requests from the Replies collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Replies.List request = drive.replies().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Replies replies() {
    return new Replies();
  }

  /**
   * The "replies" collection of methods.
   */
  public class Replies {

    /**
     * Deletes a reply.
     *
     * Create a request for the method "replies.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     * @return the request
     */
    public Delete delete(String fileId, String commentId, String replyId) throws java.io.IOException {
      Delete result = new Delete(fileId, commentId, replyId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies/{replyId}";

      /**
       * Deletes a reply.
       *
       * Create a request for the method "replies.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param replyId The ID of the reply.
       * @since 1.13
       */
      protected Delete(String fileId, String commentId, String replyId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
        this.replyId = Preconditions.checkNotNull(replyId, "Required parameter replyId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Delete setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /** The ID of the reply. */
      @com.google.api.client.util.Key
      private String replyId;

      /** The ID of the reply.
       */
      public String getReplyId() {
        return replyId;
      }

      /** The ID of the reply. */
      public Delete setReplyId(String replyId) {
        this.replyId = replyId;
        return this;
      }

    }
    /**
     * Gets a reply.
     *
     * Create a request for the method "replies.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     * @return the request
     */
    public Get get(String fileId, String commentId, String replyId) throws java.io.IOException {
      Get result = new Get(fileId, commentId, replyId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.CommentReply> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies/{replyId}";

      /**
       * Gets a reply.
       *
       * Create a request for the method "replies.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param replyId The ID of the reply.
       * @since 1.13
       */
      protected Get(String fileId, String commentId, String replyId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.CommentReply.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
        this.replyId = Preconditions.checkNotNull(replyId, "Required parameter replyId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Get setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /** The ID of the reply. */
      @com.google.api.client.util.Key
      private String replyId;

      /** The ID of the reply.
       */
      public String getReplyId() {
        return replyId;
      }

      /** The ID of the reply. */
      public Get setReplyId(String replyId) {
        this.replyId = replyId;
        return this;
      }

      /** If set, this will succeed when retrieving a deleted reply. */
      @com.google.api.client.util.Key
      private Boolean includeDeleted;

      /** If set, this will succeed when retrieving a deleted reply. [default: false]
       */
      public Boolean getIncludeDeleted() {
        return includeDeleted;
      }

      /** If set, this will succeed when retrieving a deleted reply. */
      public Get setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * If set, this will succeed when retrieving a deleted reply.
       * </p>
       */
      public boolean isIncludeDeleted() {
        if (includeDeleted == null || includeDeleted == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return includeDeleted;
      }

    }
    /**
     * Creates a new reply to the given comment.
     *
     * Create a request for the method "replies.insert".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Insert#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param content the {@link com.google.api.services.drive.model.CommentReply}
     * @return the request
     */
    public Insert insert(String fileId, String commentId, com.google.api.services.drive.model.CommentReply content) throws java.io.IOException {
      Insert result = new Insert(fileId, commentId, content);
      initialize(result);
      return result;
    }

    public class Insert extends DriveRequest<com.google.api.services.drive.model.CommentReply> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies";

      /**
       * Creates a new reply to the given comment.
       *
       * Create a request for the method "replies.insert".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Insert#execute()} method to invoke the remote operation. <p> {@link
       * Insert#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param content the {@link com.google.api.services.drive.model.CommentReply}
       * @since 1.13
       */
      protected Insert(String fileId, String commentId, com.google.api.services.drive.model.CommentReply content) {
        super(Drive.this, "POST", REST_PATH, content, com.google.api.services.drive.model.CommentReply.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public Insert setAlt(String alt) {
        return (Insert) super.setAlt(alt);
      }

      @Override
      public Insert setFields(String fields) {
        return (Insert) super.setFields(fields);
      }

      @Override
      public Insert setKey(String key) {
        return (Insert) super.setKey(key);
      }

      @Override
      public Insert setOauthToken(String oauthToken) {
        return (Insert) super.setOauthToken(oauthToken);
      }

      @Override
      public Insert setPrettyPrint(Boolean prettyPrint) {
        return (Insert) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Insert setQuotaUser(String quotaUser) {
        return (Insert) super.setQuotaUser(quotaUser);
      }

      @Override
      public Insert setUserIp(String userIp) {
        return (Insert) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Insert setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Insert setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

    }
    /**
     * Lists all of the replies to a comment.
     *
     * Create a request for the method "replies.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @return the request
     */
    public List list(String fileId, String commentId) throws java.io.IOException {
      List result = new List(fileId, commentId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.CommentReplyList> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies";

      /**
       * Lists all of the replies to a comment.
       *
       * Create a request for the method "replies.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @since 1.13
       */
      protected List(String fileId, String commentId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.CommentReplyList.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public List setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public List setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /**
     * The continuation token, used to page through large result sets. To get the next page of
     * results, set this parameter to the value of "nextPageToken" from the previous response.
     */
      @com.google.api.client.util.Key
      private String pageToken;

      /** The continuation token, used to page through large result sets. To get the next page of results,
     set this parameter to the value of "nextPageToken" from the previous response.
       */
      public String getPageToken() {
        return pageToken;
      }

      /**
     * The continuation token, used to page through large result sets. To get the next page of
     * results, set this parameter to the value of "nextPageToken" from the previous response.
     */
      public List setPageToken(String pageToken) {
        this.pageToken = pageToken;
        return this;
      }

      /** If set, all replies, including deleted replies (with content stripped) will be returned. */
      @com.google.api.client.util.Key
      private Boolean includeDeleted;

      /** If set, all replies, including deleted replies (with content stripped) will be returned. [default:
     false]
       */
      public Boolean getIncludeDeleted() {
        return includeDeleted;
      }

      /** If set, all replies, including deleted replies (with content stripped) will be returned. */
      public List setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
      }

      /**
       * Convenience method that returns only {@link Boolean#TRUE} or {@link Boolean#FALSE}.
       *
       * <p>
       * Boolean properties can have four possible values:
       * {@code null}, {@link com.google.api.client.util.Data#NULL_BOOLEAN}, {@link Boolean#TRUE}
       * or {@link Boolean#FALSE}.
       * </p>
       *
       * <p>
       * This method returns {@link Boolean#TRUE} if the default of the property is {@link Boolean#TRUE}
       * and it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * {@link Boolean#FALSE} is returned if the default of the property is {@link Boolean#FALSE} and
       * it is {@code null} or {@link com.google.api.client.util.Data#NULL_BOOLEAN}.
       * </p>
       *
       * <p>
       * If set, all replies, including deleted replies (with content stripped) will be returned.
       * </p>
       */
      public boolean isIncludeDeleted() {
        if (includeDeleted == null || includeDeleted == com.google.api.client.util.Data.NULL_BOOLEAN) {
          return false;
        }
        return includeDeleted;
      }

      /** The maximum number of replies to include in the response, used for paging. */
      @com.google.api.client.util.Key
      private Integer maxResults;

      /** The maximum number of replies to include in the response, used for paging. [default: 20] [minimum:
     0] [maximum: 100]
       */
      public Integer getMaxResults() {
        return maxResults;
      }

      /** The maximum number of replies to include in the response, used for paging. */
      public List setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
      }

    }
    /**
     * Updates an existing reply. This method supports patch semantics.
     *
     * Create a request for the method "replies.patch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Patch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     * @param content the {@link com.google.api.services.drive.model.CommentReply}
     * @return the request
     */
    public Patch patch(String fileId, String commentId, String replyId, com.google.api.services.drive.model.CommentReply content) throws java.io.IOException {
      Patch result = new Patch(fileId, commentId, replyId, content);
      initialize(result);
      return result;
    }

    public class Patch extends DriveRequest<com.google.api.services.drive.model.CommentReply> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies/{replyId}";

      /**
       * Updates an existing reply. This method supports patch semantics.
       *
       * Create a request for the method "replies.patch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Patch#execute()} method to invoke the remote operation. <p> {@link
       * Patch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param replyId The ID of the reply.
       * @param content the {@link com.google.api.services.drive.model.CommentReply}
       * @since 1.13
       */
      protected Patch(String fileId, String commentId, String replyId, com.google.api.services.drive.model.CommentReply content) {
        super(Drive.this, "PATCH", REST_PATH, content, com.google.api.services.drive.model.CommentReply.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
        this.replyId = Preconditions.checkNotNull(replyId, "Required parameter replyId must be specified.");
      }

      @Override
      public Patch setAlt(String alt) {
        return (Patch) super.setAlt(alt);
      }

      @Override
      public Patch setFields(String fields) {
        return (Patch) super.setFields(fields);
      }

      @Override
      public Patch setKey(String key) {
        return (Patch) super.setKey(key);
      }

      @Override
      public Patch setOauthToken(String oauthToken) {
        return (Patch) super.setOauthToken(oauthToken);
      }

      @Override
      public Patch setPrettyPrint(Boolean prettyPrint) {
        return (Patch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Patch setQuotaUser(String quotaUser) {
        return (Patch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Patch setUserIp(String userIp) {
        return (Patch) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Patch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Patch setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /** The ID of the reply. */
      @com.google.api.client.util.Key
      private String replyId;

      /** The ID of the reply.
       */
      public String getReplyId() {
        return replyId;
      }

      /** The ID of the reply. */
      public Patch setReplyId(String replyId) {
        this.replyId = replyId;
        return this;
      }

    }
    /**
     * Updates an existing reply.
     *
     * Create a request for the method "replies.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     * @param content the {@link com.google.api.services.drive.model.CommentReply}
     * @return the request
     */
    public Update update(String fileId, String commentId, String replyId, com.google.api.services.drive.model.CommentReply content) throws java.io.IOException {
      Update result = new Update(fileId, commentId, replyId, content);
      initialize(result);
      return result;
    }

    public class Update extends DriveRequest<com.google.api.services.drive.model.CommentReply> {

      private static final String REST_PATH = "files/{fileId}/comments/{commentId}/replies/{replyId}";

      /**
       * Updates an existing reply.
       *
       * Create a request for the method "replies.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param commentId The ID of the comment.
       * @param replyId The ID of the reply.
       * @param content the {@link com.google.api.services.drive.model.CommentReply}
       * @since 1.13
       */
      protected Update(String fileId, String commentId, String replyId, com.google.api.services.drive.model.CommentReply content) {
        super(Drive.this, "PUT", REST_PATH, content, com.google.api.services.drive.model.CommentReply.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.commentId = Preconditions.checkNotNull(commentId, "Required parameter commentId must be specified.");
        this.replyId = Preconditions.checkNotNull(replyId, "Required parameter replyId must be specified.");
      }

      @Override
      public Update setAlt(String alt) {
        return (Update) super.setAlt(alt);
      }

      @Override
      public Update setFields(String fields) {
        return (Update) super.setFields(fields);
      }

      @Override
      public Update setKey(String key) {
        return (Update) super.setKey(key);
      }

      @Override
      public Update setOauthToken(String oauthToken) {
        return (Update) super.setOauthToken(oauthToken);
      }

      @Override
      public Update setPrettyPrint(Boolean prettyPrint) {
        return (Update) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Update setQuotaUser(String quotaUser) {
        return (Update) super.setQuotaUser(quotaUser);
      }

      @Override
      public Update setUserIp(String userIp) {
        return (Update) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Update setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the comment. */
      @com.google.api.client.util.Key
      private String commentId;

      /** The ID of the comment.
       */
      public String getCommentId() {
        return commentId;
      }

      /** The ID of the comment. */
      public Update setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
      }

      /** The ID of the reply. */
      @com.google.api.client.util.Key
      private String replyId;

      /** The ID of the reply.
       */
      public String getReplyId() {
        return replyId;
      }

      /** The ID of the reply. */
      public Update setReplyId(String replyId) {
        this.replyId = replyId;
        return this;
      }

    }

  }

  /**
   * An accessor for creating requests from the Revisions collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Drive drive = new Drive(...);}
   *   {@code Drive.Revisions.List request = drive.revisions().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Revisions revisions() {
    return new Revisions();
  }

  /**
   * The "revisions" collection of methods.
   */
  public class Revisions {

    /**
     * Removes a revision.
     *
     * Create a request for the method "revisions.delete".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Delete#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param revisionId The ID of the revision.
     * @return the request
     */
    public Delete delete(String fileId, String revisionId) throws java.io.IOException {
      Delete result = new Delete(fileId, revisionId);
      initialize(result);
      return result;
    }

    public class Delete extends DriveRequest<Void> {

      private static final String REST_PATH = "files/{fileId}/revisions/{revisionId}";

      /**
       * Removes a revision.
       *
       * Create a request for the method "revisions.delete".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Delete#execute()} method to invoke the remote operation. <p> {@link
       * Delete#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param revisionId The ID of the revision.
       * @since 1.13
       */
      protected Delete(String fileId, String revisionId) {
        super(Drive.this, "DELETE", REST_PATH, null, Void.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.revisionId = Preconditions.checkNotNull(revisionId, "Required parameter revisionId must be specified.");
      }

      @Override
      public Delete setAlt(String alt) {
        return (Delete) super.setAlt(alt);
      }

      @Override
      public Delete setFields(String fields) {
        return (Delete) super.setFields(fields);
      }

      @Override
      public Delete setKey(String key) {
        return (Delete) super.setKey(key);
      }

      @Override
      public Delete setOauthToken(String oauthToken) {
        return (Delete) super.setOauthToken(oauthToken);
      }

      @Override
      public Delete setPrettyPrint(Boolean prettyPrint) {
        return (Delete) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Delete setQuotaUser(String quotaUser) {
        return (Delete) super.setQuotaUser(quotaUser);
      }

      @Override
      public Delete setUserIp(String userIp) {
        return (Delete) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Delete setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the revision. */
      @com.google.api.client.util.Key
      private String revisionId;

      /** The ID of the revision.
       */
      public String getRevisionId() {
        return revisionId;
      }

      /** The ID of the revision. */
      public Delete setRevisionId(String revisionId) {
        this.revisionId = revisionId;
        return this;
      }

    }
    /**
     * Gets a specific revision.
     *
     * Create a request for the method "revisions.get".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Get#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @param revisionId The ID of the revision.
     * @return the request
     */
    public Get get(String fileId, String revisionId) throws java.io.IOException {
      Get result = new Get(fileId, revisionId);
      initialize(result);
      return result;
    }

    public class Get extends DriveRequest<com.google.api.services.drive.model.Revision> {

      private static final String REST_PATH = "files/{fileId}/revisions/{revisionId}";

      /**
       * Gets a specific revision.
       *
       * Create a request for the method "revisions.get".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Get#execute()} method to invoke the remote operation. <p> {@link
       * Get#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @param revisionId The ID of the revision.
       * @since 1.13
       */
      protected Get(String fileId, String revisionId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.Revision.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.revisionId = Preconditions.checkNotNull(revisionId, "Required parameter revisionId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Get setAlt(String alt) {
        return (Get) super.setAlt(alt);
      }

      @Override
      public Get setFields(String fields) {
        return (Get) super.setFields(fields);
      }

      @Override
      public Get setKey(String key) {
        return (Get) super.setKey(key);
      }

      @Override
      public Get setOauthToken(String oauthToken) {
        return (Get) super.setOauthToken(oauthToken);
      }

      @Override
      public Get setPrettyPrint(Boolean prettyPrint) {
        return (Get) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Get setQuotaUser(String quotaUser) {
        return (Get) super.setQuotaUser(quotaUser);
      }

      @Override
      public Get setUserIp(String userIp) {
        return (Get) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public Get setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID of the revision. */
      @com.google.api.client.util.Key
      private String revisionId;

      /** The ID of the revision.
       */
      public String getRevisionId() {
        return revisionId;
      }

      /** The ID of the revision. */
      public Get setRevisionId(String revisionId) {
        this.revisionId = revisionId;
        return this;
      }

    }
    /**
     * Lists a file's revisions.
     *
     * Create a request for the method "revisions.list".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link List#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID of the file.
     * @return the request
     */
    public List list(String fileId) throws java.io.IOException {
      List result = new List(fileId);
      initialize(result);
      return result;
    }

    public class List extends DriveRequest<com.google.api.services.drive.model.RevisionList> {

      private static final String REST_PATH = "files/{fileId}/revisions";

      /**
       * Lists a file's revisions.
       *
       * Create a request for the method "revisions.list".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link List#execute()} method to invoke the remote operation. <p> {@link
       * List#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID of the file.
       * @since 1.13
       */
      protected List(String fileId) {
        super(Drive.this, "GET", REST_PATH, null, com.google.api.services.drive.model.RevisionList.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public List setAlt(String alt) {
        return (List) super.setAlt(alt);
      }

      @Override
      public List setFields(String fields) {
        return (List) super.setFields(fields);
      }

      @Override
      public List setKey(String key) {
        return (List) super.setKey(key);
      }

      @Override
      public List setOauthToken(String oauthToken) {
        return (List) super.setOauthToken(oauthToken);
      }

      @Override
      public List setPrettyPrint(Boolean prettyPrint) {
        return (List) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public List setQuotaUser(String quotaUser) {
        return (List) super.setQuotaUser(quotaUser);
      }

      @Override
      public List setUserIp(String userIp) {
        return (List) super.setUserIp(userIp);
      }

      /** The ID of the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID of the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID of the file. */
      public List setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

    }
    /**
     * Updates a revision. This method supports patch semantics.
     *
     * Create a request for the method "revisions.patch".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Patch#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param revisionId The ID for the revision.
     * @param content the {@link com.google.api.services.drive.model.Revision}
     * @return the request
     */
    public Patch patch(String fileId, String revisionId, com.google.api.services.drive.model.Revision content) throws java.io.IOException {
      Patch result = new Patch(fileId, revisionId, content);
      initialize(result);
      return result;
    }

    public class Patch extends DriveRequest<com.google.api.services.drive.model.Revision> {

      private static final String REST_PATH = "files/{fileId}/revisions/{revisionId}";

      /**
       * Updates a revision. This method supports patch semantics.
       *
       * Create a request for the method "revisions.patch".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Patch#execute()} method to invoke the remote operation. <p> {@link
       * Patch#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param revisionId The ID for the revision.
       * @param content the {@link com.google.api.services.drive.model.Revision}
       * @since 1.13
       */
      protected Patch(String fileId, String revisionId, com.google.api.services.drive.model.Revision content) {
        super(Drive.this, "PATCH", REST_PATH, content, com.google.api.services.drive.model.Revision.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.revisionId = Preconditions.checkNotNull(revisionId, "Required parameter revisionId must be specified.");
      }

      @Override
      public Patch setAlt(String alt) {
        return (Patch) super.setAlt(alt);
      }

      @Override
      public Patch setFields(String fields) {
        return (Patch) super.setFields(fields);
      }

      @Override
      public Patch setKey(String key) {
        return (Patch) super.setKey(key);
      }

      @Override
      public Patch setOauthToken(String oauthToken) {
        return (Patch) super.setOauthToken(oauthToken);
      }

      @Override
      public Patch setPrettyPrint(Boolean prettyPrint) {
        return (Patch) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Patch setQuotaUser(String quotaUser) {
        return (Patch) super.setQuotaUser(quotaUser);
      }

      @Override
      public Patch setUserIp(String userIp) {
        return (Patch) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Patch setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the revision. */
      @com.google.api.client.util.Key
      private String revisionId;

      /** The ID for the revision.
       */
      public String getRevisionId() {
        return revisionId;
      }

      /** The ID for the revision. */
      public Patch setRevisionId(String revisionId) {
        this.revisionId = revisionId;
        return this;
      }

    }
    /**
     * Updates a revision.
     *
     * Create a request for the method "revisions.update".
     *
     * This request holds the parameters needed by the the drive server.  After setting any optional
     * parameters, call the {@link Update#execute()} method to invoke the remote operation.
     *
     * @param fileId The ID for the file.
     * @param revisionId The ID for the revision.
     * @param content the {@link com.google.api.services.drive.model.Revision}
     * @return the request
     */
    public Update update(String fileId, String revisionId, com.google.api.services.drive.model.Revision content) throws java.io.IOException {
      Update result = new Update(fileId, revisionId, content);
      initialize(result);
      return result;
    }

    public class Update extends DriveRequest<com.google.api.services.drive.model.Revision> {

      private static final String REST_PATH = "files/{fileId}/revisions/{revisionId}";

      /**
       * Updates a revision.
       *
       * Create a request for the method "revisions.update".
       *
       * This request holds the parameters needed by the the drive server.  After setting any optional
       * parameters, call the {@link Update#execute()} method to invoke the remote operation. <p> {@link
       * Update#initialize(AbstractGoogleClientRequest)} must be called to initialize this instance
       * immediately after invoking the constructor. </p>
       *
       * @param fileId The ID for the file.
       * @param revisionId The ID for the revision.
       * @param content the {@link com.google.api.services.drive.model.Revision}
       * @since 1.13
       */
      protected Update(String fileId, String revisionId, com.google.api.services.drive.model.Revision content) {
        super(Drive.this, "PUT", REST_PATH, content, com.google.api.services.drive.model.Revision.class);
        this.fileId = Preconditions.checkNotNull(fileId, "Required parameter fileId must be specified.");
        this.revisionId = Preconditions.checkNotNull(revisionId, "Required parameter revisionId must be specified.");
      }

      @Override
      public Update setAlt(String alt) {
        return (Update) super.setAlt(alt);
      }

      @Override
      public Update setFields(String fields) {
        return (Update) super.setFields(fields);
      }

      @Override
      public Update setKey(String key) {
        return (Update) super.setKey(key);
      }

      @Override
      public Update setOauthToken(String oauthToken) {
        return (Update) super.setOauthToken(oauthToken);
      }

      @Override
      public Update setPrettyPrint(Boolean prettyPrint) {
        return (Update) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Update setQuotaUser(String quotaUser) {
        return (Update) super.setQuotaUser(quotaUser);
      }

      @Override
      public Update setUserIp(String userIp) {
        return (Update) super.setUserIp(userIp);
      }

      /** The ID for the file. */
      @com.google.api.client.util.Key
      private String fileId;

      /** The ID for the file.
       */
      public String getFileId() {
        return fileId;
      }

      /** The ID for the file. */
      public Update setFileId(String fileId) {
        this.fileId = fileId;
        return this;
      }

      /** The ID for the revision. */
      @com.google.api.client.util.Key
      private String revisionId;

      /** The ID for the revision.
       */
      public String getRevisionId() {
        return revisionId;
      }

      /** The ID for the revision. */
      public Update setRevisionId(String revisionId) {
        this.revisionId = revisionId;
        return this;
      }

    }

  }

  /**
   * Builder for {@link Drive}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory,
        HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link Drive}. */
    @Override
    public Drive build() {
      return new Drive(getTransport(),
          getHttpRequestInitializer(),
          getRootUrl(),
          getServicePath(),
          getObjectParser(),
          getGoogleClientRequestInitializer(),
          getApplicationName(),
          getSuppressPatternChecks());
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    /**
     * Set the {@link DriveRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setDriveRequestInitializer(
        DriveRequestInitializer driveRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(driveRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}

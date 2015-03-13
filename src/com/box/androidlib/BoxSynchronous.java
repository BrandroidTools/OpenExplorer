/*******************************************************************************
 * Copyright 2011 Box.net.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.box.androidlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.message.BasicNameValuePair;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.net.Uri;
import android.os.Handler;


/**
 * Use this class to execute requests <b>synchronously</b> against the Box REST API. Full details about the Box API can be found at
 * {@link <a href="http://developers.box.net/w/page/12923956/ApiOverview">http://developers.box.net/w/page/12923956/ApiOverview</a>} . You must have an OpenBox
 * application with a valid API key to use the Box API. All methods in this class are executed in the invoking thread, and therefore are NOT safe to execute in
 * the UI thread of your application. You should only use this class if you already have worker threads or AsyncTasks that you want to incorporate the Box API
 * into. Otherwise, it is recommended that you use {@link com.box.androidlib.Box}.
 * 
 * @author developers@box.net
 */
public class BoxSynchronous {

    /**
     * Singleton instance of BoxSynchronous.
     */
    private static BoxSynchronous instance;
    /**
     * The API key of the OpenBox app.
     */
    private String mApiKey;

    /**
     * Constructs a new instance of BoxSynchronous with a given API key.
     * 
     * @param apiKey
     *            The API key of your OpenBox application
     */
    protected BoxSynchronous(final String apiKey) {
        mApiKey = apiKey;
    }

    /**
     * Returns an instance of BoxSynchronous with the given API key. You must have an API key to execute requests against the Box API. If you do not already
     * have an API key, create an OpenBox application at https://www.box.net/developers/
     * 
     * @param apiKey
     *            The API key of your OpenBox application
     * @return An instance of BoxSynchronous
     */
    public static BoxSynchronous getInstance(final String apiKey) {
        if (instance == null) {
            instance = new BoxSynchronous(apiKey);
        }
        return instance;
    }
    
    private static final boolean mLoggingEnabled = true;

    /**
     * Get the API Key that this instance of BoxSynchronous is using.
     * 
     * @return API Key
     */
    public final String getApiKey() {
        return mApiKey;
    }

    /**
     * Change the API Key that this instance of BoxSynchronous is using.
     * 
     * @param apiKey
     *            API Key
     * 
     */
    public final void setApiKey(final String apiKey) {
        mApiKey = apiKey;
    }

    /**
     * This method is used in the authentication process. The ticket obtained from this method is used to generate an authentication page for the user to login.
     * Executes API action get_ticket:
     * {@link <a href="http://developers.box.net/w/page/12923936/ApiFunction_get_ticket">http://developers.box.net/w/page/12923936/ApiFunction_get_ticket</a>}
     * IOException e.g. no Internet connection
     * 
     * @return parser used to parse response
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final TicketResponseParser getTicket() throws IOException {
        final TicketResponseParser parser = new TicketResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey).appendQueryParameter("action", "get_ticket").build());
        return parser;
    }

    /**
     * This method is used in the authorization process. You should call this method after the user has authorized oneself on the Box partner authentication
     * page. Executes API action get_auth_token:
     * {@link <a href="http://developers.box.net/w/page/12923930/ApiFunction_get_auth_token"> http://developers.box.net/w/page/12923930/ApiFunction_get_auth_token</a>}
     * 
     * @param ticket
     *            Obtained from the get_ticket API action.
     * @return parser used to parse response
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final UserResponseParser getAuthToken() throws IOException {
        final UserResponseParser parser = new UserResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey).appendPath("oauth2").appendPath("authorize")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", PrivatePreferences.getBoxAPIKey()).build());
        return parser;
    }

    /**
     * This method is used to get the user's account information.
     * 
     * @param authToken
     *            the auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @return parser used to parse response
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final UserResponseParser getAccountInfo(final String authToken) throws IOException {
        final UserResponseParser parser = new UserResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "get_account_info").build());
        return parser;
    }

    /**
     * Log the user out.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923939/ApiFunction_logout"> http://developers.box.net/w/page/12923939/ApiFunction_logout</a>}
     *         )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String logout(final String authToken) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "logout").build());
        return parser.getStatus();
    }

    /**
     * Register a new user. Executes API action register_new_user:
     * {@link <a href="http://developers.box.net/w/page/12923945/ApiFunction_register_new_user"> http://developers.box.net/w/page/12923945/ApiFunction_register_new_user</a>}
     * 
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @param username
     *            The username to be created. This should be the user's e-mail address.
     * @param password
     *            The password for the user.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final UserResponseParser registerNewUser(final String username, final String password) throws IOException {
        final UserResponseParser parser = new UserResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey).appendQueryParameter("action", "register_new_user").appendQueryParameter("login", username)
            .appendQueryParameter("password", password).build());
        return parser;
    }

    /**
     * This method is used to verify whether a user email is available, or already in use. Executes API action verify_registration_email:
     * {@link <a href="http://developers.box.net/w/page/12923952/ApiFunction_verify_registration_email"> http://developers.box.net/w/page/12923952/ApiFunction_verify_registration_email</a>}
     * 
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @param email
     *            The user's e-mail address
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String verifyRegistrationEmail(final String email) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey).appendQueryParameter("action", "verify_registration_email").appendQueryParameter("login", email)
            .build());
        return parser.getStatus();
    }

    /**
     * This method is used to get a tree representing all of the user's files and folders. Executes API action get_account_tree:
     * {@link <a href="http://developers.box.net/w/page/12923929/ApiFunction_get_account_tree"> http://developers.box.net/w/page/12923929/ApiFunction_get_account_tree</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param folderId
     *            The ID of the root folder from which the tree begins. If this value is 0, the user's full account tree is returned.
     * @param params
     *            An array of strings. Possible values are {@link com.box.androidlib.Box#PARAM_ONELEVEL}, {@link com.box.androidlib.Box#PARAM_NOFILES},
     *            {@link com.box.androidlib.Box#PARAM_NOZIP}, {@link com.box.androidlib.Box#PARAM_SIMPLE}. Currently, {@link com.box.androidlib.Box#PARAM_NOZIP}
     *            is always included automatically.
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final AccountTreeResponseParser getAccountTree(final String authToken, final long folderId, final String[] params) throws IOException {

        // nozip should always be included
        final ArrayList<String> paramsList;
        if (params == null) {
            paramsList = new ArrayList<String>();
        }
        else {
            paramsList = new ArrayList<String>(Arrays.asList(params));
        }
        if (!paramsList.contains(Box.PARAM_NOZIP)) {
            paramsList.add(Box.PARAM_NOZIP);
        }

        final AccountTreeResponseParser parser = new AccountTreeResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "get_account_tree");
        builder.appendQueryParameter("folder_id", String.valueOf(folderId));
        for (int i = 0; i < paramsList.size(); i++) {
            builder.appendQueryParameter("params[" + i + "]", paramsList.get(i));
        }
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * This method retrieves the details for a specified file. Executes API action get_file_info:
     * {@link <a href="http://developers.box.net/w/page/12923934/ApiFunction_get_file_info">http://developers.box.net/w/page/12923934/ApiFunction_get_file_info</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param fileId
     *            The id of the file for with you want to obtain more information.
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final FileResponseParser getFileInfo(final String authToken, final long fileId) throws IOException {
        final FileResponseParser parser = new FileResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "get_file_info").appendQueryParameter("file_id", String.valueOf(fileId)).build());
        return parser;
    }

    /**
     * Create a folder in a user's account.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param parentFolderId
     *            The folder_id of the folder in which the new folder will be created
     * @param folderName
     *            The name of the folder to be created
     * @param share
     *            Set to true to be allow the folder to be shared
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final FolderResponseParser createFolder(final String authToken, final long parentFolderId, final String folderName, final boolean share)
        throws IOException {
        final FolderResponseParser parser = new FolderResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "create_folder").appendQueryParameter("name", folderName)
                .appendQueryParameter("parent_id", String.valueOf(parentFolderId)).appendQueryParameter("share", share ? "1" : "0").build());
        return parser;
    }

    /**
     * Copies a file into another folder. Executes API action copy:
     * {@link <a href="http://developers.box.net/w/page/12923924/APIFunction_copy"> http://developers.box.net/w/page/12923924/APIFunction_copy</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be copied. Currently, this should only be set to {@link com.box.androidlib.Box#TYPE_FILE}
     * @param targetId
     *            The id of the item to be copied (i.e. file id)
     * @param destinationId
     *            The id of the folder to which the item will be copied
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923924/APIFunction_copy"> http://developers.box.net/w/page/12923924/APIFunction_copy</a>} )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String copy(final String authToken, final String type, final long targetId, final long destinationId) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "copy").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("destination_id", String.valueOf(destinationId)).build());
        return parser.getStatus();
    }

    /**
     * Delete a file or folder. Executes API action delete:
     * {@link <a href="http://developers.box.net/w/page/12923926/ApiFunction_delete"> http://developers.box.net/w/page/12923926/ApiFunction_delete</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be deleted. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file id or folder id to delete
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923926/ApiFunction_delete"> http://developers.box.net/w/page/12923926/ApiFunction_delete</a>}
     *         )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String delete(final String authToken, final String type, final long targetId) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "delete").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).build());
        return parser.getStatus();
    }

    /**
     * This method moves a file or folder into another folder. Executes API action move:
     * {@link <a href="http://developers.box.net/w/page/12923941/ApiFunction_move">http://developers.box.net/w/page/12923941/ApiFunction_move</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be moved. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to be moved
     * @param destinationId
     *            The folder_id of the folder in which the file will be moved to.
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923941/ApiFunction_move)">http://developers.box.net/w/page/12923941/ApiFunction_move)</a>}
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String move(final String authToken, final String type, final long targetId, final long destinationId) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "move").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("destination_id", String.valueOf(destinationId)).build());
        return parser.getStatus();
    }

    /**
     * Rename a file or folder. Executes API action rename:
     * {@link <a href="http://developers.box.net/w/page/12923947/ApiFunction_rename">http://developers.box.net/w/page/12923947/ApiFunction_rename</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be renamed. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to be renamed
     * @param newName
     *            The new name to be applied to the item
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923947/ApiFunction_rename">http://developers.box.net/w/page/12923947/ApiFunction_rename</a>} )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String rename(final String authToken, final String type, final long targetId, final String newName) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "rename").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("new_name", newName).build());
        return parser.getStatus();
    }

    /**
     * This method gets a list of items that would normally be obtained through search. Executes API action search:
     * {@link <a href="http://developers.box.net/w/page/22888693/ApiFunction_search">http://developers.box.net/w/page/22888693/ApiFunction_search</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param query
     *            The text to search for
     * @param sort
     *            The method in which the results may be sorted. Set to {@link com.box.androidlib.Box#SORT_RELEVANCE}, {@link com.box.androidlib.Box#SORT_NAME},
     *            {@link com.box.androidlib.Box#SORT_DATE} or {@link com.box.androidlib.Box#SORT_SIZE}
     * @param page
     *            The page number, which begins on page 1. This coincides with the per_page parameter
     * @param perPage
     *            The number of search results to display per page
     * @param direction
     *            Set to either {@link com.box.androidlib.Box#DIRECTION_ASC} or {@link com.box.androidlib.Box#DIRECTION_DESC}
     * @param params
     *            Array of string params that can include {@link com.box.androidlib.Box#SEARCH_PARAM_SHOW_DESCRIPTION} and/or
     *            {@link com.box.androidlib.Box#SEARCH_PARAM_SHOW_PATH}
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final SearchResponseParser search(final String authToken, final String query, final String sort, final int page, final int perPage,
        final String direction, final String[] params) throws IOException {
        final SearchResponseParser parser = new SearchResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "search").appendQueryParameter("query", query)
            .appendQueryParameter("sort", sort).appendQueryParameter("page", String.valueOf(page)).appendQueryParameter("per_page", String.valueOf(perPage))
            .appendQueryParameter("direction", direction);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                builder.appendQueryParameter("params[" + i + "]", params[i]);
            }
        }
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * This method gets a list of items that would normally be obtained through search. Executes API action search:
     * {@link <a href="http://developers.box.net/w/page/22888693/ApiFunction_search">http://developers.box.net/w/page/22888693/ApiFunction_search</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param query
     *            The text to search for
     * @param sort
     *            The method in which the results may be sorted. Set to {@link com.box.androidlib.Box#SORT_RELEVANCE}, {@link com.box.androidlib.Box#SORT_NAME},
     *            {@link com.box.androidlib.Box#SORT_DATE} or {@link com.box.androidlib.Box#SORT_SIZE}
     * @param page
     *            The page number, which begins on page 1. This coincides with the per_page parameter
     * @param perPage
     *            The number of search results to display per page
     * @param direction
     *            Set to either {@link com.box.androidlib.Box#DIRECTION_ASC} or {@link com.box.androidlib.Box#DIRECTION_DESC}
     * @param params
     *            Array of string params that can include {@link com.box.androidlib.Box#SEARCH_PARAM_SHOW_DESCRIPTION} and/or
     *            {@link com.box.androidlib.Box#SEARCH_PARAM_SHOW_PATH}
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @param subfolderIds
     *            Array of subfolder ids to restrict the scope of the search to.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final SearchResponseParser search(final String authToken, final String query, final String sort, final int page, final int perPage,
        final String direction, final String[] params, final long[] subfolderIds) throws IOException {
        final SearchResponseParser parser = new SearchResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "search").appendQueryParameter("query", query)
            .appendQueryParameter("sort", sort).appendQueryParameter("page", String.valueOf(page)).appendQueryParameter("per_page", String.valueOf(perPage))
            .appendQueryParameter("direction", direction);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                builder.appendQueryParameter("params[" + i + "]", params[i]);
            }
        }
        if (subfolderIds != null) {
            for (int i = 0; i < subfolderIds.length; i++) {
                builder.appendQueryParameter("subfolder_ids[]", Long.toString(subfolderIds[i]));
            }
        }
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * This method applies tags to a designated file or folder. Executes API action add_to_tag:
     * {@link <a href="http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag">http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to which the tags will be applied. Set to {@link com.box.androidlib.Box#TYPE_FILE} or
     *            {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to which the tags will be applied
     * @param tagNames
     *            Array of tag names
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag">http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag</a>}
     *         )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String addToTag(final String authToken, final String type, final long targetId, final String[] tagNames) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "add_to_tag").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId));
        if (tagNames != null) {
            for (int i = 0; i < tagNames.length; i++) {
                builder.appendQueryParameter("tags[" + i + "]", tagNames[i]);
            }
        }
        saxRequest(parser, builder.build());
        return parser.getStatus();
    }

    /**
     * Get the comments of a file or folder. Executes API action get_comments:
     * {@link <a href="http://developers.box.net/w/page/22889464/ApiFunction_get_comments">http://developers.box.net/w/page/22889464/ApiFunction_get_comments</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final CommentsResponseParser getComments(final String authToken, final String type, final long targetId) throws IOException {
        final CommentsResponseParser parser = new CommentsResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "get_comments").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).build());
        return parser;
    }

    /**
     * Add a comment to a file or folder. Executes API action add_comment:
     * {@link <a href="http://developers.box.net/w/page/22920168/ApiFunction_add_comment">http://developers.box.net/w/page/22920168/ApiFunction_add_comment</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to which the tags will be applied. Set to {@link com.box.androidlib.Box#TYPE_FILE} or
     *            {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param message
     *            The comment's message
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final CommentResponseParser addComment(final String authToken, final String type, final long targetId, final String message) throws IOException {
        final CommentResponseParser parser = new CommentResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "add_comment").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("message", message).build());
        return parser;
    }

    /**
     * Delete a comment. Executes API action delete_comment:
     * {@link <a href="http://developers.box.net/w/page/22920573/ApiFunction_delete_comment">http://developers.box.net/w/page/22920573/ApiFunction_delete_comment</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param commentId
     *            The comment_id of the comment
     * @return the status returned by Box API (see
     *         {@link <a href="http://developers.box.net/w/page/22920573/ApiFunction_delete_comment">http://developers.box.net/w/page/22920573/ApiFunction_delete_comment</a>}
     *         )
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String deleteComment(final String authToken, final long commentId) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "delete_comment").appendQueryParameter("target_id", String.valueOf(commentId)).build());
        return parser.getStatus();
    }

    /**
     * Retrieve a list of tags in the user's account. Executes API action export_tags:
     * {@link <a href="http://developers.box.net/w/page/12923927/ApiFunction_export_tags">http://developers.box.net/w/page/12923927/ApiFunction_export_tags</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final TagsResponseParser exportTags(final String authToken) throws IOException {
        final TagsResponseParser parser = new TagsResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "export_tags").build());
        return parser;
    }

    /**
     * Returns the contents of a user's Updates tab on box.net. Executes API action get_updates:
     * {@link <a href="http://developers.box.net/w/page/22926051/ApiFunction_get_updates">http://developers.box.net/w/page/22926051/ApiFunction_get_updates</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param beginTimeStamp
     *            a unix_timestamp of the earliest point in time to obtain an update (the time of the oldest update you want to display)
     * @param endTimeStamp
     *            a unix_timestamp of the latest point in time to obtain an update
     * @param params
     *            array of parameters. Currently, the only supported parameter is {@link com.box.androidlib.Box#PARAM_NOZIP}
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final UpdatesResponseParser getUpdates(final String authToken, final long beginTimeStamp, final long endTimeStamp, final String[] params)
        throws IOException {
        final UpdatesResponseParser parser = new UpdatesResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "get_updates");
        builder.appendQueryParameter("begin_timestamp", String.valueOf(beginTimeStamp));
        builder.appendQueryParameter("end_timestamp", String.valueOf(endTimeStamp));

        // use_attributes should always be included
        final ArrayList<String> paramsList;
        if (params == null) {
            paramsList = new ArrayList<String>();
        }
        else {
            paramsList = new ArrayList<String>(Arrays.asList(params));
        }
        if (!paramsList.contains(Box.PARAM_USE_ATTRIBUTES)) {
            paramsList.add(Box.PARAM_USE_ATTRIBUTES);
        }
        for (int i = 0; i < paramsList.size(); i++) {
            builder.appendQueryParameter("params[" + i + "]", paramsList.get(i));
        }

        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * Enables or disables the upload email address for a folder. Executes API action toggle_folder_email:
     * {@link <a href="http://developers.box.net/w/page/35640290/ApiFunction_toggle_folder_email">http://developers.box.net/w/page/35640290/ApiFunction_toggle_folder_email</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param folderId
     *            The folder_id for which the upload email will be enabled or disabled
     * @param enable
     *            Set to true to enable, false to disable
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final ToggleFolderEmailResponseParser toggleFolderEmail(final String authToken, final long folderId, final boolean enable) throws IOException {
        final ToggleFolderEmailResponseParser parser = new ToggleFolderEmailResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "toggle_folder_email").appendQueryParameter("folder_id", String.valueOf(folderId))
            .appendQueryParameter("enable", enable ? "1" : "0").build());
        return parser;
    }

    /**
     * Retrieve the past versions of a file. Executes API action get_versions:
     * {@link <a href="http://developers.box.net/w/page/12923937/ApiFunction_get_versions">http://developers.box.net/w/page/12923937/ApiFunction_get_versions</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item. Currently, this must be set to {@link com.box.androidlib.Box#TYPE_FILE}
     * @param targetId
     *            The file_id or folder_id of the item
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final VersionsResponseParser getVersions(final String authToken, final String type, final long targetId) throws IOException {
        final VersionsResponseParser parser = new VersionsResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "get_versions").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).build());
        return parser;
    }

    /**
     * Set a past version of a file to be the current version. Executes API action make_current_version:
     * {@link <a href="http://developers.box.net/w/page/12923940/ApiFunction_make_current_version">http://developers.box.net/w/page/12923940/ApiFunction_make_current_version</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param versionId
     *            The version_id that you would like to set as current
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final VersionsResponseParser makeCurrentVersion(final String authToken, final long versionId) throws IOException {
        final VersionsResponseParser parser = new VersionsResponseParser();
        saxRequest(parser, BoxUriBuilder.getBuilder(mApiKey, authToken, "make_current_version").appendQueryParameter("version_id", String.valueOf(versionId))
            .build());
        return parser;
    }

    /**
     * Set the description of a file or folder. Executes API action set_description:
     * {@link <a href="http://developers.box.net/w/page/12923949/ApiFunction_set_description">http://developers.box.net/w/page/12923949/ApiFunction_set_description</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param description
     *            The description to be applied to the item.
     * @return the status returned by Box API
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String setDescription(final String authToken, final String type, final long targetId, final String description) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "set_description").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("description", description).build());
        return parser.getStatus();
    }

    /**
     * This method makes a file or folder shareable, and may initiate sharing through Box email notifications. Executes API action public_share:
     * {@link <a href="http://developers.box.net/w/page/12923943/ApiFunction_public_share">http://developers.box.net/w/page/12923943/ApiFunction_public_share</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be shared. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param password
     *            The password to protect the folder or file.
     * @param shareMsg
     *            A message to be included in a notification email.
     * @param emails
     *            An array of emails to notify users about the newly shared file or folder.
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final PublicShareResponseParser publicShare(final String authToken, final String type, final long targetId, final String password,
        final String shareMsg, final String[] emails) throws IOException {
        final PublicShareResponseParser parser = new PublicShareResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "public_share").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId));
        builder.appendQueryParameter("message", shareMsg == null ? "" : shareMsg);
        builder.appendQueryParameter("password", password == null ? "" : password);
        if (emails != null) {
            for (int i = 0; i < emails.length; i++) {
                builder.appendQueryParameter("emails[" + i + "]", emails[i]);
            }
        }
        else {
            builder.appendQueryParameter("emails[]", "");
        }
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * Unshare a file or folder. Executes API method api_unshare:
     * {@link <a href="http://developers.box.net/w/page/12923944/ApiFunction_public_unshare">http://developers.box.net/w/page/12923944/ApiFunction_public_unshare</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be unshared. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @return the status code returned from Box API
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String publicUnshare(final String authToken, final String type, final long targetId) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        saxRequest(
            parser,
            BoxUriBuilder.getBuilder(mApiKey, authToken, "public_unshare").appendQueryParameter("target", type)
                .appendQueryParameter("target_id", String.valueOf(targetId)).build());
        return parser.getStatus();
    }

    /**
     * Privately shares a file or folder with other users. Executes API action private_unshare:
     * {@link <a href="http://developers.box.net/w/page/12923942/ApiFunction_private_share">http://developers.box.net/w/page/12923942/ApiFunction_private_share</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be shared. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param message
     *            A message to be included in a notification email
     * @param emails
     *            An array of emails to notify users about the newly shared file or folder.
     * @param notify
     *            Set to true to send a notification email to users. If set to false, notifications will not be sent
     * @return the status code returned from Box API
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String privateShare(final String authToken, final String type, final long targetId, final String message, final String[] emails,
        final boolean notify) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "private_share").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("message", message != null ? message : "")
            .appendQueryParameter("notify", notify ? "1" : "0");
        if (emails != null) {
            for (int i = 0; i < emails.length; i++) {
                builder.appendQueryParameter("emails[" + i + "]", emails[i]);
            }
        }
        saxRequest(parser, builder.build());
        return parser.getStatus();
    }

    /**
     * Invites one or more users/emails to collaborate on a folder. Executes API action invite_collaborators:
     * {@link <a href="http://developers.box.net/w/page/12923938/ApiFunction_invite_collaborators">http://developers.box.net/w/page/12923938/ApiFunction_invite_collaborators</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be shared. Set to {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param userIds
     *            An array of user ids that the folder will be collaborated with. Can be null.
     * @param emails
     *            An array of email addresses that the folder will be collaborated with. Can be null.
     * @param itemRoleName
     *            Set to either {@link com.box.androidlib.Box#ITEM_ROLE_VIEWER} or {@link com.box.androidlib.Box#ITEM_ROLE_EDITOR}.
     * @param resendInvite
     *            Whether the invitation is being re-sent.
     * @param noEmail
     *            If true, no e-mail notification will be sent about the invitation.
     * @param params
     *            An array of parameters. TODO missing documentation for this. Set as null for now.
     * @return the status code returned from Box API
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public String inviteCollaborators(final String authToken, final String type, final long targetId, final long[] userIds, final String[] emails,
        final String itemRoleName, final boolean resendInvite, final boolean noEmail, final String[] params) throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "invite_collaborators").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId)).appendQueryParameter("item_role_name", itemRoleName)
            .appendQueryParameter("resend_invite", resendInvite ? "1" : "0").appendQueryParameter("no_email", noEmail ? "1" : "0");
        if (emails != null) {
            for (int i = 0; i < emails.length; i++) {
                builder.appendQueryParameter("emails[" + i + "]", emails[i]);
            }
        }
        else {
            builder.appendQueryParameter("emails[]", "");
        }
        if (userIds != null) {
            for (int i = 0; i < userIds.length; i++) {
                builder.appendQueryParameter("user_ids[" + i + "]", String.valueOf(userIds[i]));
            }
        }
        else {
            builder.appendQueryParameter("user_ids[]", "");
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                builder.appendQueryParameter("params[" + i + "]", params[i]);
            }
        }
        else {
            builder.appendQueryParameter("params[]", "");
        }
        saxRequest(parser, builder.build());
        return parser.getStatus();
    }

    /**
     * Get the possible roles that can be given to a collaborator when an item is collaborated.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be shared. Set to {@link com.box.androidlib.Box#TYPE_FILE} or {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public ItemRolesParser getItemRolesForItem(final String authToken, final String type, final long targetId) throws IOException {
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "get_item_roles_for_item").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId));
        final ItemRolesParser parser = new ItemRolesParser();
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * Obtain a list of collaborators for a file or folder. Executes API action invite_collaborators:
     * {@link <a href="http://developers.box.net/w/page/12923933/ApiFunction_get_collaborations">http://developers.box.net/w/page/12923933/ApiFunction_get_collaborations</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param type
     *            The type of item to be shared. Set to {@link com.box.androidlib.Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @return CollaborationsResponseParser.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public CollaborationsResponseParser getCollaborations(final String authToken, final String type, final long targetId) throws IOException {
        CollaborationsResponseParser parser = new CollaborationsResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "get_collaborations").appendQueryParameter("target", type)
            .appendQueryParameter("target_id", String.valueOf(targetId));
        saxRequest(parser, builder.build());
        return parser;
    }

    /**
     * This method copies a file that publicly shared by another individual and into a user's a designated folder in the user's Box. Executes API action
     * {@link <a href="http://developers.box.net/w/page/12923920/ApiFunction_add_to_mybox">http://developers.box.net/w/page/12923920/ApiFunction_add_to_mybox</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param fileId
     *            The id of the file you wish to copy.
     * @param publicName
     *            The unique public name of the shared file that you wish to copy.
     * @param folderId
     *            The folder_id of the folder to which you will copy the item.
     * @param tags
     *            A List of tags to apply to the file when copied into the user's own folder. Can be set to null if there are no tags.
     * @return the status code returned from Box API
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    public final String addToMyBox(final String authToken, final Long fileId, final String publicName, final long folderId, final String[] tags)
        throws IOException {
        final DefaultResponseParser parser = new DefaultResponseParser();
        final Uri.Builder builder = BoxUriBuilder.getBuilder(mApiKey, authToken, "add_to_mybox").appendQueryParameter("folder_id", String.valueOf(folderId));
        if (fileId != null) {
            builder.appendQueryParameter("file_id", String.valueOf(fileId));
        }
        else {
            builder.appendQueryParameter("file_id", "");
        }
        if (publicName != null) {
            builder.appendQueryParameter("public_name", publicName);
        }
        else {
            builder.appendQueryParameter("public_name", "");
        }
        if (tags != null) {
            for (int i = 0; i < tags.length; i++) {
                builder.appendQueryParameter("tags[" + i + "]", tags[i]);
            }
        }
        else {
            builder.appendQueryParameter("tags", "");
        }
        saxRequest(parser, builder.build());
        return parser.getStatus();
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * If you want to cancel a download in progress, you must interrupt the thread that you executed this method in. For a more convenient way to cancel, use
     * Box.download() which returns a Cancelable.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationFile
     *            A java.io.File resource to which the downloaded file will be written. Ensure that this points to a valid file-path that can be written to.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @param handler
     *            The handler through which FileDownloadListener.onProgress will be invoked.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @return a response handler
     */
    public final DefaultResponseParser download(final String authToken, final long fileId, final File destinationFile, final Long versionId,
        final FileDownloadListener listener, final Handler handler) throws IOException {
        return download(authToken, fileId, new FileOutputStream(destinationFile), versionId, listener, handler);
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * If you want to cancel a download in progress, you must interrupt the thread that you executed this method in. For a more convenient way to cancel, use
     * Box.download() which returns a Cancelable.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationOutputStream
     *            OutputStream to which the data being downloaded will be written.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @param handler
     *            The handler through which FileDownloadListener.onProgress will be invoked.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @return a response handler
     */
    public final DefaultResponseParser download(final String authToken, final long fileId, final OutputStream destinationOutputStream, final Long versionId,
        final FileDownloadListener listener, final Handler handler) throws IOException {
        return download(authToken, fileId, new OutputStream[] {destinationOutputStream}, versionId, listener, handler);
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * If you want to cancel a download in progress, you must interrupt the thread that you executed this method in. For a more convenient way to cancel, use
     * Box.download() which returns a Cancelable.
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationOutputStreams
     *            OutputStreams to which the data being downloaded will be written.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @param handler
     *            The handler through which FileDownloadListener.onProgress will be invoked.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @return a response handler
     */
    public final DefaultResponseParser download(final String authToken, final long fileId, final OutputStream[] destinationOutputStreams, final Long versionId,
        final FileDownloadListener listener, final Handler handler) throws IOException {
        final BoxFileDownload download = new BoxFileDownload(authToken);
        download.setListener(listener, handler);
        return download.execute(fileId, destinationOutputStreams, versionId);
    }

    /**
     * Upload a file from the device to a folder at Box. Uses the upload API as described here: {@see {@link <a href=
     * "http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download" >http://developers.box.net/w/page/12923951/ApiFunction_Upload-and- Download}
     * </a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param action
     *            Set to {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD} or {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}
     * @param file
     *            A File resource pointing to the file you wish to upload. Make sure File.isFile() and File.canRead() are true for this resource.
     * @param filename
     *            The desired filename on Box after upload (just the file name, do not include the path)
     * @param destinationId
     *            If action is {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that
     *            is being overwritten, or copied.
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(com.box.androidlib.DAO.BoxFile, String)}
     * @param handler
     *            The handler through which FileUploadListener.onProgress will be invoked.
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @throws FileNotFoundException
     *             File being uploaded either doesn't exist, is not a file, or cannot be read
     * @throws MalformedURLException
     *             Make sure you have specified a valid upload action
     */
    public final FileResponseParser upload(final String authToken, final String action, final File file, final String filename, final long destinationId,
        final FileUploadListener listener, final Handler handler) throws FileNotFoundException, MalformedURLException, IOException {
        final BoxFileUpload upload = new BoxFileUpload(authToken);
        upload.setListener(listener, handler);
        return upload.execute(action, new FileInputStream(file), filename, destinationId);
    }

    /**
     * Upload a file from the device to a folder at Box. Uses the upload API as described here: {@see {@link <a href=
     * "http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download" >http://developers.box.net/w/page/12923951/ApiFunction_Upload-and- Download}
     * </a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link BoxSynchronous#getAuthToken(String)}
     * @param action
     *            Set to {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD} or {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}
     * @param sourceInputStream
     *            Input stream targetting the data to be uploaded.
     * @param filename
     *            The desired filename on Box after upload (just the file name, do not include the path)
     * @param destinationId
     *            If action is {@link com.box.androidlib.Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link com.box.androidlib.Box#UPLOAD_ACTION_OVERWRITE} or {@link com.box.androidlib.Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that
     *            is being overwritten, or copied.
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(com.box.androidlib.DAO.BoxFile, String)}
     * @param handler
     *            The handler through which FileUploadListener.onProgress will be invoked.
     * @return the response parser used to capture the data of interest from the response. See the doc for the specific parser type returned to see what data is
     *         now available. All parsers implement getStatus() at a minimum.
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     * @throws FileNotFoundException
     *             File being uploaded either doesn't exist, is not a file, or cannot be read
     * @throws MalformedURLException
     *             Make sure you have specified a valid upload action
     */
    public final FileResponseParser upload(final String authToken, final String action, final InputStream sourceInputStream, final String filename,
        final long destinationId, final FileUploadListener listener, final Handler handler) throws FileNotFoundException, MalformedURLException, IOException {
        final BoxFileUpload upload = new BoxFileUpload(authToken);
        upload.setListener(listener, handler);
        return upload.execute(action, sourceInputStream, filename, destinationId);
    }

    /**
     * Executes an Http request and triggers response parsing by the specified parser.
     * 
     * @param parser
     *            A BoxResponseParser configured to consume the response and capture data that is of interest
     * @param uri
     *            The Uri of the request
     * @throws IOException
     *             Can be thrown if there is no connection, or if some other connection problem exists.
     */
    protected static void saxRequest(final DefaultResponseParser parser, final Uri uri) throws IOException {
        Uri theUri = uri;
        List<BasicNameValuePair> customQueryParams = BoxConfig.getInstance().getCustomQueryParameters();
        if (customQueryParams != null && customQueryParams.size() > 0) {
            Uri.Builder builder = theUri.buildUpon();
            for (BasicNameValuePair param : customQueryParams) {
                builder.appendQueryParameter(param.getName(), param.getValue());
            }
            theUri = builder.build();
        }

        try {
            final XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            xmlReader.setContentHandler(parser);
            HttpURLConnection conn = (HttpURLConnection) (new URL(theUri.toString())).openConnection();
            conn.setRequestProperty("User-Agent", BoxConfig.getInstance().getUserAgent());
            conn.setRequestProperty("Accept-Language", BoxConfig.getInstance().getAcceptLanguage());
            conn.setConnectTimeout(BoxConfig.getInstance().getConnectionTimeOut());
            if (mLoggingEnabled) {
                DevUtils.logcat("URL: " + theUri.toString());
//                Iterator<String> keys = conn.getRequestProperties().keySet().iterator();
//                while (keys.hasNext()) {
//                    String key = keys.next();
//                    DevUtils.logcat("Request Header: " + key + " => " + conn.getRequestProperties().get(key));
//                }
            }

            int responseCode = -1;
            try {
                conn.connect();
                responseCode = conn.getResponseCode();
                if(mLoggingEnabled)
                    DevUtils.logcat("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    xmlReader.parse(new InputSource(inputStream));
                    inputStream.close();
                }
                else if (responseCode == -1) {
                    parser.setStatus(ResponseListener.STATUS_UNKNOWN_HTTP_RESPONSE_CODE);
                }
            }
            catch (IOException e) {
                try {
                    responseCode = conn.getResponseCode();
                }
                catch (NullPointerException ee) {
                    // Honeycomb devices seem to throw a null pointer exception sometimes which traces to HttpURLConnectionImpl.
                }
                // Server returned a 503 Service Unavailable. Usually means a temporary unavailability.
                if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                    parser.setStatus(ResponseListener.STATUS_SERVICE_UNAVAILABLE);
                }
                else {
                    throw e;
                }
            }
            finally {
                conn.disconnect();
            }
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }
        catch (final FactoryConfigurationError e) {
            e.printStackTrace();
        }
    }
}

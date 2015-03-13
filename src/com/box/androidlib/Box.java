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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenNetworkPath.Cancellable;

import android.os.Handler;


/**
 * Use this class to execute requests <b>asynchronously</b> against the Box REST API. Full details about the Box API can be found at
 * {@link <a href="http://developers.box.net/w/page/12923956/ApiOverview">http://developers.box.net/w/page/12923956/ApiOverview</a>} . You must have an OpenBox
 * application with a valid API key to use the Box API. All methods in this class put the Box API request into a background thread, and therefore do not block
 * and are safe to execute in the UI thread of your application. Results of the request are obtained through a ResponseListener that is passed into the methods.<br/>
 * <br/>
 * ResponseListener callbacks are invoked in the UI Thread. So, for example, if you execute Box.getTicket(getTicketListener), then
 * getTicketListener.onComplete() will be executed on the UI thread. <br/>
 * <br/>
 * If you would like to execute requests synchronously (e.g. you already have a worker thread or AsyncTask you want to put the request into), then use
 * {@link com.box.androidlib.BoxSynchronous}.
 * 
 * @author developers@box.net
 */
public class Box {

    /**
     * Used in a variety of API methods to indicate that the type of object being acted on is a file.
     */
    public static final String TYPE_FILE = "file";
    /**
     * Used in a variety of API methods to indicate that the type of object being acted on is a folder.
     */
    public static final String TYPE_FOLDER = "folder";
    /**
     * Used in a variety of API methods to indicate that the type of object being acted on is a folder.
     */
    public static final String TYPE_WEB_LINK = "web_link";
    /**
     * Used in API methods such as addComment.
     */
    public static final String TYPE_COMMENT = "comment";
    /**
     * Parameter for getAccountTree() so that you will only get one level of files and folders.
     */
    public static final String PARAM_ONELEVEL = "onelevel";
    /**
     * Parameter for getAccountTree() so that only folders are returned.
     */
    public static final String PARAM_NOFILES = "nofiles";
    /**
     * Parameter for getAccountTree() indicating that the tree XML should not be zipped.
     */
    public static final String PARAM_NOZIP = "nozip";
    /**
     * Parameter for getAccountTree() indicating that only a limited set of attributes should be returned to make for smaller, more efficient output (folders
     * only contain the 'name' and 'id' attributes, and files will contain the 'name', 'id', 'created', and 'size' attributes).
     */
    public static final String PARAM_SIMPLE = "simple";
    /**
     * Parameter for certain API requests such as get_udpates that tells server to return attributes instead of elements.
     */
    public static final String PARAM_USE_ATTRIBUTES = "use_attributes";
    /**
     * Parameter used in getAccountTree to indicate that the server should return the number of comments.
     */
    public static final String PARAM_COMMENT_COUNT = "comment_count";
    /**
     * Parameter that can be used in getAccountTree to request that path ids should be returned.
     */
    public static final String PARAM_SHOW_PATH_IDS = "show_path_ids";
    /**
     * Parameter that can be used in getAccountTree to request that path names should be returned.
     */
    public static final String PARAM_SHOW_PATH_NAMES = "show_path_names";
    /**
     * Used in search to set the sort criteria of results to be by relevance.
     */
    public static final String SORT_RELEVANCE = "relevance";
    /**
     * Used in search to set the sort criteria of results to be by name.
     */
    public static final String SORT_NAME = "name";
    /**
     * Used in search to set the sort criteria of results to be by date.
     */
    public static final String SORT_DATE = "date";
    /**
     * Used in search to set the sort criteria of results to be by size.
     */
    public static final String SORT_SIZE = "size";
    /**
     * Used in search to specify that results should be returned in ascending. order.
     **/
    public static final String DIRECTION_ASC = "ASC";
    /**
     * Used in search to specify that results should be returned in descending. order.
     **/
    public static final String DIRECTION_DESC = "DESC";
    /**
     * Parameter for search to indicate that search results should include the descriptions of the files and folders returned.
     **/
    public static final String SEARCH_PARAM_SHOW_DESCRIPTION = "show_description";
    /**
     * Parameter for search to indicate that search results should include the absolute path where a file or folder is located.
     **/
    public static final String SEARCH_PARAM_SHOW_PATH = "show_path";
    /**
     * If upload action is set to this, the will will be uploaded, and if a file by the same name already exists in the folder, it will be overwritten.
     */
    public static final String UPLOAD_ACTION_UPLOAD = "upload";
    /**
     * If upload action is set to this, the upload will explicitly overwrite a particular file (useful especially for Box Platform Actions).
     */
    public static final String UPLOAD_ACTION_OVERWRITE = "overwrite";
    /**
     * If upload action is set to this, a new copy of the file will be made.
     */
    public static final String UPLOAD_ACTION_NEW_COPY = "new_copy";

    /**
     * Used in invite_collaborators to specify a viewer role.
     **/
    public static final String ITEM_ROLE_VIEWER = "viewer";
    /**
     * Used in invite_collaborators tp specify an editor role.
     **/
    public static final String ITEM_ROLE_EDITOR = "editor";

    /**
     * The BoxFile class that ResponseParsers will instantiate.
     */
    private static Class<? extends BoxFile> mBoxFileClass = BoxFile.class;
    /**
     * The BoxFolder class that ResponseParsers will instantiate.
     */
    private static Class<? extends BoxFolder> mBoxFolderClass = BoxFolder.class;

    /**
     * The API key of the OpenBox app.
     */
    private String mApiKey;
    /**
     * Singleton instance of Box.
     */
    private static Box instance;
    /**
     * Handler through which response listener callbacks will be invoked.
     */
    private static final Handler mHandler = OpenExplorer.getHandler();

    /**
     * Constructs a new instance of Box with a given API key.
     * 
     * @param apiKey
     *            The API key of your OpenBox application
     */
    protected Box(final String apiKey) {
        mApiKey = apiKey;
    }

    /**
     * Returns an instance of Box with the given API key. You must have an API key to execute requests against the Box API. If you do not already have an API
     * key, create an OpenBox application at {@link <a href="https://www.box.net/developers/">https://www.box.net/developers/</a>} <br/>
     * <br/>
     * <b>This method must be executed in the UI thread.</b>
     * 
     * @param apiKey
     *            The API key of your OpenBox application
     * @return An instance of Box
     */
    public static Box getInstance(final String apiKey) {
        if (instance == null) {
            instance = new Box(apiKey);
        }
        return instance;
    }

    /**
     * Set the BoxFolder class that will be created by response parsers. This can be used if you want to have your own custom class that extends BoxFolder.
     * 
     * @param boxFolderClass
     *            The BoxFolder class.
     */
    public static void setBoxFolderClass(final Class<? extends BoxFolder> boxFolderClass) {
        mBoxFolderClass = boxFolderClass;
    }

    /**
     * Get the BoxFolder class to be instantiated by response parsers.
     * 
     * @return The BoxFile class that will be created by response parsers.
     */
    public static Class<? extends BoxFolder> getBoxFolderClass() {
        return mBoxFolderClass;
    }

    /**
     * Set the BoxFile class that will be created by response parsers. This can be used if you want to have your own custom class that extends BoxFile.
     * 
     * @param boxFileClass
     *            The BoxFile class.
     */
    public static void setBoxFileClass(final Class<? extends BoxFile> boxFileClass) {
        mBoxFileClass = boxFileClass;
    }

    /**
     * Get the BoxFile class to be instantiated by response parsers.
     * 
     * @return The BoxFolder class that will be created by response parsers.
     */
    public static Class<? extends BoxFile> getBoxFileClass() {
        return mBoxFileClass;
    }

    /**
     * Get the API Key that this instance of BoxSynchronous is using.
     * 
     * @return API Key
     */
    public final String getApiKey() {
        return mApiKey;
    }

    /**
     * set the API Key that this instance of BoxSynchronous is using.
     * 
     * @param apiKey
     *            API Key
     */
    public final void setApiKey(final String apiKey) {
        mApiKey = apiKey;
    }

    /**
     * This method is used in the authorization process. You should call this method after the user has authorized oneself on the Box partner authentication
     * page. Executes API action get_auth_token:
     * {@link <a href="http://developers.box.net/w/page/12923930/ApiFunction_get_auth_token">http://developers.box.net/w/page/12923930/ApiFunction_get_auth_token</a>}
     * 
     * @param ticket
     *            The ticket that you obtained through {@link Box#getTicket(GetTicketListener)}
     * @param listener
     *            The callback that will run
     */
    public final void getAuthToken(final String ticket, final GetAuthTokenListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final UserResponseParser response = BoxSynchronous.getInstance(mApiKey).getAuthToken();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getUser(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method is used to get the user's account information. Executes API action get_account_info:
     * {@link <a href="http://developers.box.net/w/page/12923928/ApiFunction_get_account_info">http://developers.box.net/w/page/12923928/ApiFunction_get_account_info</a>}
     * 
     * @param authToken
     *            the auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param listener
     *            The callback that will run
     */
    public final void getAccountInfo(final String authToken, final GetAccountInfoListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final UserResponseParser response = BoxSynchronous.getInstance(mApiKey).getAccountInfo(authToken);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getUser(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Log the user out. Executes API action logout:
     * {@link <a href="http://developers.box.net/w/page/12923939/ApiFunction_logout">http://developers.box.net/w/page/12923939/ApiFunction_logout</a>}
     * 
     * @param authToken
     *            the auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param listener
     *            The callback that will run
     */
    public final void logout(final String authToken, final LogoutListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).logout(authToken);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Register a new user. Executes API action register_new_user:
     * {@link <a href="http://developers.box.net/w/page/12923945/ApiFunction_register_new_user">http://developers.box.net/w/page/12923945/ApiFunction_register_new_user</a>}
     * 
     * @param username
     *            The username to be created. This should be the user's e-mail address.
     * @param password
     *            The password for the user.
     * @param listener
     *            The callback that will run
     */
    public final void registerNewUser(final String username, final String password, final RegisterNewUserListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final UserResponseParser response = BoxSynchronous.getInstance(mApiKey).registerNewUser(username, password);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getUser(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method is used to verify whether a user email is available, or already in use. Executes API action verify_registration_email:
     * {@link <a href="http://developers.box.net/w/page/12923952/ApiFunction_verify_registration_email">http://developers.box.net/w/page/12923952/ApiFunction_verify_registration_email</a>}
     * 
     * @param email
     *            The user's e-mail address
     * @param listener
     *            The callback that will run
     */
    public final void verifyRegistrationEmail(final String email, final VerifyRegistrationEmailListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).verifyRegistrationEmail(email);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method is used to get a tree representing all of the user's files and folders. Executes API action get_account_tree:
     * {@link <a href="http://developers.box.net/w/page/12923929/ApiFunction_get_account_tree">http://developers.box.net/w/page/12923929/ApiFunction_get_account_tree</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param folderId
     *            The ID of the root folder from which the tree begins. If this value is 0, the user's full account tree is returned.
     * @param params
     *            An array of strings. Possible values are {@link Box#PARAM_ONELEVEL}, {@link Box#PARAM_NOFILES}, {@link Box#PARAM_NOZIP},
     *            {@link Box#PARAM_SIMPLE}. Currently, {@link com.box.androidlib.Box#PARAM_NOZIP} is always included automatically.
     * @param listener
     *            The callback that will run
     */
    public final Thread getAccountTree(final String authToken, final long folderId, final String[] params, final GetAccountTreeListener listener) {

        Thread ret = new Thread() {

            @Override
            public void run() {
                try {
                    final AccountTreeResponseParser response = BoxSynchronous.getInstance(mApiKey).getAccountTree(authToken, folderId, params);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getFolder(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        };
        ret.start();
        return ret;
    }

    /**
     * This method retrieves the details for a specified file. Executes API action get_file_info:
     * {@link <a href="http://developers.box.net/w/page/12923934/ApiFunction_get_file_info">http://developers.box.net/w/page/12923934/ApiFunction_get_file_info</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param fileId
     *            The id of the file for with you want to obtain more information.
     * @param listener
     *            The callback that will run
     */
    public final void getFileInfo(final String authToken, final long fileId, final GetFileInfoListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final FileResponseParser response = BoxSynchronous.getInstance(mApiKey).getFileInfo(authToken, fileId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getFile(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Create a folder in a user's account.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param parentFolderId
     *            The folder_id of the folder in which the new folder will be created
     * @param folderName
     *            The name of the folder to be created
     * @param share
     *            Set to true to be allow the folder to be shared
     * @param listener
     *            The callback that will run
     */
    public final void createFolder(final String authToken, final long parentFolderId, final String folderName, final boolean share,
        final CreateFolderListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final FolderResponseParser response = BoxSynchronous.getInstance(mApiKey).createFolder(authToken, parentFolderId, folderName, share);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getFolder(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Copies a file into another folder. Executes API action copy:
     * {@link <a href="http://developers.box.net/w/page/12923924/APIFunction_copy">http://developers.box.net/w/page/12923924/APIFunction_copy</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be copied. Currently, this should only be set to {@link Box#TYPE_FILE}
     * @param targetId
     *            The id of the item to be copied (i.e. file id)
     * @param destinationId
     *            The id of the folder to which the item will be copied
     * @param listener
     *            The callback that will run
     */
    public final void copy(final String authToken, final String type, final long targetId, final long destinationId, final CopyListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).copy(authToken, type, targetId, destinationId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Delete a file or folder. Executes API action delete:
     * {@link <a href="http://developers.box.net/w/page/12923926/ApiFunction_delete">http://developers.box.net/w/page/12923926/ApiFunction_delete</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be deleted. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file id or folder id to delete
     * @param listener
     *            The callback that will run
     */
    public final void delete(final String authToken, final String type, final long targetId, final DeleteListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).delete(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method moves a file or folder into another folder. Executes API action move:
     * {@link <a href="http://developers.box.net/w/page/12923941/ApiFunction_move">http://developers.box.net/w/page/12923941/ApiFunction_move</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be moved. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to be moved
     * @param destinationId
     *            The folder_id of the folder in which the file will be moved to.
     * @param listener
     *            The callback that will run
     */
    public final void move(final String authToken, final String type, final long targetId, final long destinationId, final MoveListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).move(authToken, type, targetId, destinationId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Rename a file or folder. Executes API action rename:
     * {@link <a href="http://developers.box.net/w/page/12923947/ApiFunction_rename">http://developers.box.net/w/page/12923947/ApiFunction_rename</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be renamed. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to be renamed
     * @param newName
     *            The new name to be applied to the item
     * @param listener
     *            The callback that will run
     */
    public final void rename(final String authToken, final String type, final long targetId, final String newName, final RenameListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).rename(authToken, type, targetId, newName);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method gets a list of items that would normally be obtained through search. Executes API action search:
     * {@link <a href="http://developers.box.net/w/page/22888693/ApiFunction_search">http://developers.box.net/w/page/22888693/ApiFunction_search</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param query
     *            The text to search for
     * @param sort
     *            The method in which the results may be sorted. Set to {@link Box#SORT_RELEVANCE}, {@link Box#SORT_NAME}, {@link Box#SORT_DATE} or
     *            {@link Box#SORT_SIZE}
     * @param page
     *            The page number, which begins on page 1. This coincides with the per_page parameter
     * @param perPage
     *            The number of search results to display per page
     * @param direction
     *            Set to either {@link Box#DIRECTION_ASC} or {@link Box#DIRECTION_DESC}
     * @param params
     *            Array of string params that can include {@link Box#SEARCH_PARAM_SHOW_DESCRIPTION} and/or {@link Box#SEARCH_PARAM_SHOW_PATH}
     * @param listener
     *            The callback that will run
     */
    public final void search(final String authToken, final String query, final String sort, final int page, final int perPage, final String direction,
        final String[] params, final SearchListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final SearchResponseParser response = BoxSynchronous.getInstance(mApiKey).search(authToken, query, sort, page, perPage, direction, params);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getSearchResult(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method applies tags to a designated file or folder. Executes API action add_to_tag:
     * {@link <a href="http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag">http://developers.box.net/w/page/12923921/ApiFunction_add_to_tag</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to which the tags will be applied. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item to which the tags will be applied
     * @param tagNames
     *            Array of tag names
     * @param listener
     *            The callback that will run
     */
    public final void addToTag(final String authToken, final String type, final long targetId, final String[] tagNames, final AddToTagListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).addToTag(authToken, type, targetId, tagNames);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Get the comments of a file or folder. Executes API action get_comments:
     * {@link <a href="http://developers.box.net/w/page/22889464/ApiFunction_get_comments">http://developers.box.net/w/page/22889464/ApiFunction_get_comments</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param listener
     *            The callback that will run
     */
    public final void getComments(final String authToken, final String type, final long targetId, final GetCommentsListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final CommentsResponseParser response = BoxSynchronous.getInstance(mApiKey).getComments(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getComments(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Add a comment to a file or folder. Executes API action add_comment:
     * {@link <a href="http://developers.box.net/w/page/22920168/ApiFunction_add_comment">http://developers.box.net/w/page/22920168/ApiFunction_add_comment</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to which the tags will be applied. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param message
     *            The comment's message
     * @param listener
     *            The callback that will run
     */
    public final void addComment(final String authToken, final String type, final long targetId, final String message, final AddCommentListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final CommentResponseParser response = BoxSynchronous.getInstance(mApiKey).addComment(authToken, type, targetId, message);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getComment(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Delete a comment. Executes API action delete_comment:
     * {@link <a href="http://developers.box.net/w/page/22920573/ApiFunction_delete_comment">http://developers.box.net/w/page/22920573/ApiFunction_delete_comment</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param commentId
     *            The comment_id of the comment
     * @param listener
     *            The callback that will run
     */
    public final void deleteComment(final String authToken, final long commentId, final DeleteCommentListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).deleteComment(authToken, commentId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Retrieve a list of tags in the user's account. Executes API action export_tags:
     * {@link <a href="http://developers.box.net/w/page/12923927/ApiFunction_export_tags">http://developers.box.net/w/page/12923927/ApiFunction_export_tags</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param listener
     *            The callback that will run
     */
    public final void exportTags(final String authToken, final ExportTagsListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final TagsResponseParser response = BoxSynchronous.getInstance(mApiKey).exportTags(authToken);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getTags(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Returns the contents of a user's Updates tab on box.net. Executes API action get_updates:
     * {@link <a href="http://developers.box.net/w/page/22926051/ApiFunction_get_updates">http://developers.box.net/w/page/22926051/ApiFunction_get_updates</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param beginTimeStamp
     *            a unix_timestamp of the earliest point in time to obtain an update (the time of the oldest update you want to display)
     * @param endTimeStamp
     *            a unix_timestamp of the latest point in time to obtain an update
     * @param params
     *            array of parameters. Currently, the only supported parameter is {@link Box#PARAM_NOZIP}
     * @param listener
     *            The callback that will run
     */
    public final void getUpdates(final String authToken, final long beginTimeStamp, final long endTimeStamp, final String[] params,
        final GetUpdatesListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final UpdatesResponseParser response = BoxSynchronous.getInstance(mApiKey).getUpdates(authToken, beginTimeStamp, endTimeStamp, params);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getUpdates(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Enables or disables the upload email address for a folder. Executes API action toggle_folder_email:
     * {@link <a href="http://developers.box.net/w/page/35640290/ApiFunction_toggle_folder_email">http://developers.box.net/w/page/35640290/ApiFunction_toggle_folder_email</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param folderId
     *            The folder_id for which the upload email will be enabled or disabled
     * @param enable
     *            Set to true to enable, false to disable
     * @param listener
     *            The callback that will run
     */
    public final void toggleFolderEmail(final String authToken, final long folderId, final boolean enable, final ToggleFolderEmailListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final ToggleFolderEmailResponseParser response = BoxSynchronous.getInstance(mApiKey).toggleFolderEmail(authToken, folderId, enable);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getUploadEmail(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Retrieve the past versions of a file. Executes API action get_versions:
     * {@link <a href="http://developers.box.net/w/page/12923937/ApiFunction_get_versions">http://developers.box.net/w/page/12923937/ApiFunction_get_versions</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item. Currently, this must be set to {@link Box#TYPE_FILE}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param listener
     *            The callback that will run
     */
    public final void getVersions(final String authToken, final String type, final long targetId, final GetVersionsListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final VersionsResponseParser response = BoxSynchronous.getInstance(mApiKey).getVersions(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getVersions(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Set a past version of a file to be the current version. Executes API action make_current_version:
     * {@link <a href="http://developers.box.net/w/page/12923940/ApiFunction_make_current_version">http://developers.box.net/w/page/12923940/ApiFunction_make_current_version</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param versionId
     *            The version_id that you would like to set as current
     * @param listener
     *            The callback that will run
     */
    public final void makeCurrentVersion(final String authToken, final long versionId, final MakeCurrentVersionListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final VersionsResponseParser response = BoxSynchronous.getInstance(mApiKey).makeCurrentVersion(authToken, versionId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getVersions(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Set the description of a file or folder. Executes API action set_description:
     * {@link <a href="http://developers.box.net/w/page/12923949/ApiFunction_set_description">http://developers.box.net/w/page/12923949/ApiFunction_set_description</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param description
     *            The description to be applied to the item.
     * @param listener
     *            The callback that will run
     */
    public final void setDescription(final String authToken, final String type, final long targetId, final String description,
        final SetDescriptionListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).setDescription(authToken, type, targetId, description);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method makes a file or folder shareable, and may initiate sharing through Box email notifications. Executes API action public_share:
     * {@link <a href="http://developers.box.net/w/page/12923943/ApiFunction_public_share">http://developers.box.net/w/page/12923943/ApiFunction_public_share</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be shared. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param password
     *            The password to protect the folder or file.
     * @param shareMsg
     *            A message to be included in a notification email.
     * @param emails
     *            An array of emails to notify users about the newly shared file or folder.
     * @param listener
     *            The callback that will run
     */
    public final void publicShare(final String authToken, final String type, final long targetId, final String password, final String shareMsg,
        final String[] emails, final PublicShareListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final PublicShareResponseParser response = BoxSynchronous.getInstance(mApiKey).publicShare(authToken, type, targetId, password, shareMsg,
                        emails);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getPublicName(), response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Unshare a file or folder. Executes API method api_unshare:
     * {@link <a href="http://developers.box.net/w/page/12923944/ApiFunction_public_unshare">http://developers.box.net/w/page/12923944/ApiFunction_public_unshare</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be unshared. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param listener
     *            The callback that will run
     */
    public final void publicUnshare(final String authToken, final String type, final long targetId, final PublicUnshareListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).publicUnshare(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Privately shares a file or folder with other users. Executes API action private_unshare:
     * {@link <a href="http://developers.box.net/w/page/12923942/ApiFunction_private_share">http://developers.box.net/w/page/12923942/ApiFunction_private_share</a>}
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param type
     *            The type of item to be shared. Set to {@link Box#TYPE_FILE} or {@link Box#TYPE_FOLDER}
     * @param targetId
     *            The file_id or folder_id of the item
     * @param message
     *            A message to be included in a notification email
     * @param emails
     *            An array of emails to notify users about the newly shared file or folder.
     * @param notify
     *            Set to true to send a notification email to users. If set to false, notifications will not be sent
     * @param listener
     *            The callback that will run
     */
    public final void privateShare(final String authToken, final String type, final long targetId, final String message, final String[] emails,
        final boolean notify, final PrivateShareListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).privateShare(authToken, type, targetId, message, emails, notify);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
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
     * @param listener
     *            The callback that will run
     */
    public void inviteCollaborators(final String authToken, final String type, final long targetId, final long[] userIds, final String[] emails,
        final String itemRoleName, final boolean resendInvite, final boolean noEmail, final String[] params, final InviteCollaboratorsListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).inviteCollaborators(authToken, type, targetId, userIds, emails, itemRoleName,
                        resendInvite, noEmail, params);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
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
     * @param listener
     *            The callback that will run
     */
    public void getItemRolesForItem(final String authToken, final String type, final long targetId, final GetItemRolesForItemListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final ItemRolesParser parser = BoxSynchronous.getInstance(mApiKey).getItemRolesForItem(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(parser.getItemRoles(), parser.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
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
     * @param listener
     *            The callback that will run
     */
    public void getCollaborations(final String authToken, final String type, final long targetId, final GetCollaborationsListener listener) {
        new Thread() {

            @Override
            public void run() {
                try {
                    final CollaborationsResponseParser parser = BoxSynchronous.getInstance(mApiKey).getCollaborations(authToken, type, targetId);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(parser.getCollaborations(), parser.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * This method copies a file that publicly shared by another individual and into a user's a designated folder in the user's Box. Executes API action
     * add_to_mybox:
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
     * @param listener
     *            The callback that will run
     */
    public final void addToMyBox(final String authToken, final Long fileId, final String publicName, final long folderId, final String[] tags,
        final AddToMyBoxListener listener) {

        new Thread() {

            @Override
            public void run() {
                try {
                    final String status = BoxSynchronous.getInstance(mApiKey).addToMyBox(authToken, fileId, publicName, folderId, tags);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(status);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * This method returns a Cancelable which you can use to cancel a download in progress.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
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
     * @return A Cancelable that allows you to try to cancel a download in progress.
     */
    public final Cancellable download(final String authToken, final long fileId, final File destinationFile, final Long versionId,
        final FileDownloadListener listener) {
        try {
            return download(authToken, fileId, new FileOutputStream(destinationFile), versionId, listener);
        }
        catch (FileNotFoundException e) {
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * This method returns a Cancelable which you can use to cancel a download in progress.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationOutputStream
     *            Destination OutputStream that downloaded data will be written to.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @return A Cancelable that allows you to try to cancel a download in progress.
     */
    public final Cancellable download(final String authToken, final long fileId, final OutputStream destinationOutputStream, final Long versionId,
        final FileDownloadListener listener) {

        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    final DefaultResponseParser response = BoxSynchronous.getInstance(mApiKey).download(authToken, fileId, destinationOutputStream, versionId,
                        listener, mHandler);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        };
        thread.start();

        Cancellable cancelable = new Cancellable() {

            @Override
            public boolean cancel() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        };
        return cancelable;
    }

    /**
     * Download a file. Uses the download API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * This method returns a Cancelable which you can use to cancel a download in progress.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param fileId
     *            The file_id of the file to be downloaded
     * @param destinationOutputStreams
     *            Destination OutputStreams that downloaded data will be written to.
     * @param versionId
     *            The version_id of the version of the file to download. Set to null to download the latest version of the file.
     * @param listener
     *            A file download listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileDownloadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileDownloadListener#onComplete(String)}
     * @return A Cancelable that allows you to try to cancel a download in progress.
     */
    public final Cancelable download(final String authToken, final long fileId, final OutputStream[] destinationOutputStreams, final Long versionId,
        final FileDownloadListener listener) {

        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    final DefaultResponseParser response = BoxSynchronous.getInstance(mApiKey).download(authToken, fileId, destinationOutputStreams, versionId,
                        listener, mHandler);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getStatus());
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        };
        thread.start();

        Cancelable cancelable = new Cancelable() {

            @Override
            public boolean cancel() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        };
        return cancelable;
    }

    /**
     * Upload a file from the device to a folder at Box. Uses the upload API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * This method returns a Cancelable which you can use to cancel an upload in progress.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param action
     *            Set to {@link Box#UPLOAD_ACTION_UPLOAD} or {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}
     * @param file
     *            A File resource pointing to the file you wish to upload. Make sure File.isFile() and File.canRead() are true for this resource.
     * @param filename
     *            The desired filename on Box after upload (just the file name, do not include the path)
     * @param destinationId
     *            If action is {@link Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that is being overwrriten, or copied.
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(com.box.androidlib.BoxFile, String)}
     * @return A Cancelable that allows you to try to cancel an upload in progress.
     */
    public final Cancellable upload(final String authToken, final String action, final File file, final String filename, final long destinationId,
        final FileUploadListener listener) {

        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    final FileResponseParser response = BoxSynchronous.getInstance(mApiKey).upload(authToken, action, file, filename, destinationId, listener,
                        mHandler);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getFile(), response.getStatus());
                        }
                    });
                }
                catch (final FileNotFoundException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onFileNotFoundException(e);
                        }
                    });
                }
                catch (final MalformedURLException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onMalformedURLException(e);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        };
        thread.start();

        Cancellable cancelable = new Cancellable() {

            @Override
            public boolean cancel() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        };
        return cancelable;
    }

    /**
     * Upload a file from the device to a folder at Box. Uses the upload API as described here:
     * {@link <a href="http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download">http://developers.box.net/w/page/12923951/ApiFunction_Upload-and-Download</a>}
     * 
     * This method returns a Cancelable which you can use to cancel an upload in progress.
     * 
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param action
     *            Set to {@link Box#UPLOAD_ACTION_UPLOAD} or {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}
     * @param sourceInputStream
     *            An input stream targeting the data you wish to upload.
     * @param filename
     *            The desired filename on Box after upload (just the file name, do not include the path)
     * @param destinationId
     *            If action is {@link Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that is being overwrriten, or copied.
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(com.box.androidlib.BoxFile, String)}
     * @return A Cancelable that allows you to try to cancel an upload in progress.
     */
    public final Cancelable upload(final String authToken, final String action, final InputStream sourceInputStream, final String filename,
        final long destinationId, final FileUploadListener listener) {

        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    final FileResponseParser response = BoxSynchronous.getInstance(mApiKey).upload(authToken, action, sourceInputStream, filename,
                        destinationId, listener, mHandler);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onComplete(response.getFile(), response.getStatus());
                        }
                    });
                }
                catch (final FileNotFoundException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onFileNotFoundException(e);
                        }
                    });
                }
                catch (final MalformedURLException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onMalformedURLException(e);
                        }
                    });
                }
                catch (final IOException e) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.onIOException(e);
                        }
                    });
                }
            }
        };
        thread.start();

        Cancelable cancelable = new Cancelable() {

            @Override
            public boolean cancel() {
                if (thread.isAlive()) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        };
        return cancelable;
    }

    /**
     * Upload a file to Box. This method is depracated.
     * 
     * @deprecated replaced by {@link #upload(String, String, File, String, long)}. Do not use this.
     * @param action
     *            Set to {@link Box#UPLOAD_ACTION_UPLOAD} or {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}
     * @param absoluteFilePath
     *            Absolute filepath of the file to be uploaded.
     * @param authToken
     *            The auth token retrieved through {@link Box#getAuthToken(String, GetAuthTokenListener)}
     * @param destinationId
     *            If action is {@link Box#UPLOAD_ACTION_UPLOAD}, then this is the folder id where the file will uploaded to. If action is
     *            {@link Box#UPLOAD_ACTION_OVERWRITE} or {@link Box#UPLOAD_ACTION_NEW_COPY}, then this is the file_id that is being overwrriten, or copied.
     * @param listener
     *            A file upload listener. You will likely be interested in callbacks
     *            {@link com.box.androidlib.FileUploadListener#onProgress(long)} and
     *            {@link com.box.androidlib.FileUploadListener#onComplete(com.box.androidlib.box.DAO.BoxFile, String)}
     */
    @Deprecated
    public final void upload(final String action, final String absoluteFilePath, final String authToken, final long destinationId,
        final FileUploadListener listener) {
        upload(authToken, action, new File(absoluteFilePath), absoluteFilePath.substring(absoluteFilePath.lastIndexOf("/") + 1), destinationId, listener);
    }
}

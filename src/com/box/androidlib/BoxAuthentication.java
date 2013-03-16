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

import java.io.IOException;

import org.brandroid.openmanager.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Activity for authenticating Box users. See
 * {@link <a href="http://developers.box.net/w/page/12923915/ApiAuthentication">http://developers.box.net/w/page/12923915/ApiAuthentication</a>} for details
 * about authentication.
 * 
 * @author developers@box.net
 */
public class BoxAuthentication extends Activity {

    /**
     * Response code to indicate successful authentication.
     */
    public static final int AUTH_RESULT_SUCCESS = 1;
    /**
     * Response code to indicate failed authentication.
     */
    public static final int AUTH_RESULT_FAIL = 2;

    /**
     * Instance of Box.
     */
    private Box box;
    /**
     * API key.
     */
    private String mApiKey;
    /**
     * Login webview.
     */
    private WebView mLoginWebView;
    /**
     * Track whether an auth token has been found yet.
     */
    private boolean mAuthTokenFound = false;
    /**
     * Max number of retries on for getAuthToken.
     */
    private static final int GET_AUTH_TOKEN_MAX_RETRIES = 5;
    /**
     * Delay in milliseconds between each getAuthToken retry.
     */
    private static final int GET_AUTH_TOKEN_INTERVAL = 500;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiKey = getIntent().getStringExtra("API_KEY").trim();
        if (mApiKey == null || mApiKey.length() == 0) {
            setResult(AUTH_RESULT_FAIL);
            finish();
            return;
        }

        setContentView(R.layout.box_authentication);

        // Get a ticket. We need a ticket in order to load the login webpage.
        // Once we have a ticket, launch a login webview.
        box = Box.getInstance(mApiKey);
        box.getTicket(new GetTicketListener() {

            @Override
            public void onComplete(final String ticket, final String status) {
                if (status.equals("get_ticket_ok")) {
                    loadLoginWebview(ticket);
                }
                else {
                    onGetTicketFail();
                }
            }

            @Override
            public void onIOException(final IOException e) {
                onGetTicketFail();
            }
        });
    }

    /**
     * Load the login webview.
     * 
     * @param ticket
     *            Ticket from Box API action get_ticket
     */
    private void loadLoginWebview(final String ticket) {
        // Load the login webpage. Note how the ticket must be appended to the
        // login url.
        String loginUrl = BoxConstants.LOGIN_URL + ticket;
        mLoginWebView = (WebView) findViewById(R.id.loginWebView);
        mLoginWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mLoginWebView.getSettings().setJavaScriptEnabled(true);
        mLoginWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(final WebView view, final String url) {
                // Listen for page loads and execute Box.getAuthToken() after
                // each one to see if the user has successfully logged in.
                getAuthToken(ticket, 0);
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (url != null && url.startsWith("market://")) {
                    try {
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    }
                    catch (ActivityNotFoundException e) {
                        // e.printStackTrace();
                    }
                }
                return false;
            }
        });
        mLoginWebView.loadUrl(loginUrl);
    }

    /**
     * Called if a ticket could not be obtained from Box API.
     */
    private void onGetTicketFail() {
        setResult(AUTH_RESULT_FAIL);
        finish();
    }

    /**
     * Try to get an auth token. Due to a bug with Android webviews, it is possible for WebViewClient.onPageFinished to be called before the page has actually
     * loaded. So we may have to try the getAuthToken request several times.
     * http://stackoverflow.com/questions/3702627/onpagefinished-not-firing-correctly-when-rendering-web-page
     * 
     * @param ticket
     *            Box ticket
     * @param tries
     *            the number of attempts that have been made
     */
    private void getAuthToken(final String ticket, final int tries) {
        if (tries >= GET_AUTH_TOKEN_MAX_RETRIES) {
            return;
        }
        final Handler handler = new Handler();
        box.getAuthToken(ticket, new GetAuthTokenListener() {

            @Override
            public void onComplete(final User user, final String status) {
                if (status.equals("get_auth_token_ok") && user != null) {
                    onAuthTokenRetreived(user);
                }
                else if (status.equals("error_unknown_http_response_code")) {
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            getAuthToken(ticket, tries + 1);
                        }
                    }, GET_AUTH_TOKEN_INTERVAL);
                }
            }

            @Override
            public void onIOException(final IOException e) {
            }
        });
    }

    /**
     * Called when an auth token has been obtained.
     * 
     * @param authToken
     *            Box auth token
     */
    private void onAuthTokenRetreived(final User authToken) {
        if (mAuthTokenFound) {
            return;
        }
        mAuthTokenFound = true;
        Intent intent = new Intent();
        intent.putExtra("AUTH_TOKEN", authToken.getAuthToken());
        intent.putExtra("AUTH_LOGIN", authToken.getLogin());
        setResult(AUTH_RESULT_SUCCESS, intent);
        finish();
    }
}

package com.dropbox.client2.android;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AbstractSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Keeps track of a logged in user and contains configuration options for the
 * {@link DropboxAPI}. Has methods specific to Android for authenticating
 * users via the Dropbox app or web site.
 * <br><br>
 * A typical authentication flow when no user access token pair is saved is as
 * follows:
 * <br>
 * <pre>
 * AndroidAuthSession session = new AndroidAuthSession(myAppKeys, myAccessType);
 *
 * // When user wants to link to Dropbox, within an activity:
 * session.startAuthentication(this);
 *
 * // When user returns to your activity, after authentication:
 * if (session.authenticationSuccessful()) {
 *   try {
 *     session.finishAuthentication();
 *
 *     AccessTokenPair tokens = session.getAccessTokenPair();
 *     // Store tokens.key, tokens.secret somewhere
 *   } catch (IllegalStateException e) {
 *     // Error handling
 *   }
 * }</pre>
 * <br>
 * When a user returns to your app and you have tokens stored, just create a
 * new session with them:
 * <br>
 * <pre>
 * AndroidAuthSession session = new AndroidAuthSession(
 *     myAppKeys, myAccessType, new AccessTokenPair(storedAccessKey, storedAccessSecret));
 * </pre>
 */
public class AndroidAuthSession extends AbstractSession {

    /**
     * Creates a new session to authenticate Android apps with the given app
     * key pair and access type. The session will not be linked because it has
     * no access token or secret.
     */
    public AndroidAuthSession(AppKeyPair appKeyPair, AccessType type) {
        super(appKeyPair, type);
    }

    /**
     * Creates a new session to authenticate Android apps with the given app
     * key pair and access type. The session will be linked to the account
     * corresponding to the given access token pair.
     */
    public AndroidAuthSession(AppKeyPair appKeyPair, AccessType type,
            AccessTokenPair accessTokenPair) {
        super(appKeyPair, type, accessTokenPair);
    }

    /**
     * Starts the Dropbox authentication process by launching an external app
     * (either the Dropbox app if available or a web browser) where the user
     * will log in and allow your app access.
     *
     * @param context the {@link Context} which to use to launch the
     *         Dropbox authentication activity. This will typically be an
     *         {@link Activity} and the user will be taken back to that
     *         activity after authentication is complete (i.e., your activity
     *         will receive an {@code onResume()}).
     *
     * @throws IllegalStateException if you have not correctly set up the
     *         AuthActivity in your manifest, meaning that the Dropbox app will
     *         not be able to redirect back to your app after auth.
     */
    public void startAuthentication(Context context) {
        AppKeyPair appKeyPair = getAppKeyPair();

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + appKeyPair.key;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(testIntent, 0);

        if (0 == activities.size()) {
            throw new IllegalStateException("URI scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
        } else if (activities.size() > 1) {
            // Check to make sure there's no other app with this scheme.
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Security alert");
            builder.setMessage("Another app on your phone may be trying to " +
                    "pose as the app you are currently using. The malicious " +
                    "app cannot access your account, but linking to Dropbox " +
                    "has been disabled as a precaution. Please contact " +
                    "support@dropbox.com.");
            builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        } else {
            // Just one activity registered for the URI scheme. Now make sure
            // it's within the same package so when we return from web auth
            // we're going back to this app and not some other app.
            String authPackage = activities.get(0).activityInfo.packageName;
            if (!context.getPackageName().equals(authPackage)) {
                throw new IllegalStateException("There must be an " +
                        "AuthActivity within your app's package registered " +
                        "for your URI scheme (" + scheme + "). However, it " +
                        "appears that an activity in a different package is " +
                        "registered for that scheme instead. If you have " +
                        "multiple apps that all want to use the same access" +
                        "token pair, designate one of them to do " +
                        "authentication and have the other apps launch it " +
                        "and then retrieve the token pair from it.");
            }
        }

        // Start Dropbox auth activity.
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_INTERNAL_CONSUMER_KEY,
                appKeyPair.key);
        intent.putExtra(AuthActivity.EXTRA_INTERNAL_CONSUMER_SECRET,
                appKeyPair.secret);
        if (!(context instanceof Activity)) {
            // If starting the intent outside of an Activity, must include
            // this. See startActivity(). Otherwise, we prefer to stay in
            // the same task.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * Returns whether the user successfully authenticated with Dropbox.
     * Reasons for failure include the user canceling authentication, network
     * errors, and improper setup from within your app.
     */
    public boolean authenticationSuccessful() {
        Intent data = AuthActivity.lastResult;

        if (data == null) {
            return false;
        }

        String token = data.getStringExtra(AuthActivity.EXTRA_ACCESS_TOKEN);
        String secret = data.getStringExtra(AuthActivity.EXTRA_ACCESS_SECRET);
        String uid = data.getStringExtra(AuthActivity.EXTRA_UID);

        if (token != null && !token.equals("") &&
                secret != null && !secret.equals("") &&
                uid != null && !uid.equals("")) {
            return true;
        }

        return false;
    }

    /**
     * Sets up a user's access token and secret in this session when you return
     * to your activity from the Dropbox authentication process. Should be
     * called from your activity's {@code onActivityResult()} method, but only
     * after checking that {@link #authenticationSuccessful()} is {@code true}.
     *
     * @return the authenticated user's Dropbox UID.
     *
     * @throws IllegalStateException if authentication was not successful prior
     *         to this call (check with {@link #authenticationSuccessful()}.
     */
    public String finishAuthentication() throws IllegalStateException {
        Intent data = AuthActivity.lastResult;

        if (data == null) {
            throw new IllegalStateException();
        }

        String token = data.getStringExtra(AuthActivity.EXTRA_ACCESS_TOKEN);
        String secret = data.getStringExtra(AuthActivity.EXTRA_ACCESS_SECRET);
        String uid = data.getStringExtra(AuthActivity.EXTRA_UID);

        if (token != null && !token.equals("") &&
                secret != null && !secret.equals("") &&
                uid != null && !uid.equals("")) {
            AccessTokenPair tokens = new AccessTokenPair(token, secret);
            setAccessTokenPair(tokens);
            return uid;
        }

        throw new IllegalStateException();
    }

    @Override
    public void unlink() {
        super.unlink();
        AuthActivity.lastResult = null;
    }
}

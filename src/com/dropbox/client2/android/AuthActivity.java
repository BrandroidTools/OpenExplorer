package com.dropbox.client2.android;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.brandroid.utils.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.RESTUtility;

public class AuthActivity extends Activity {

    /**
     * The extra that goes in an intent to provide your consumer key for
     * Dropbox authentication. You won't ever have to use this.
     */
    public static final String EXTRA_CONSUMER_KEY = "CONSUMER_KEY";

    /**
     * The extra that goes in an intent when returning from Dropbox auth to
     * provide the user's access token, if auth succeeded. You won't ever have
     * to use this.
     */
    public static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * The extra that goes in an intent when returning from Dropbox auth to
     * provide the user's access token secret, if auth succeeded. You won't
     * ever have to use this.
     */
    public static final String EXTRA_ACCESS_SECRET = "ACCESS_SECRET";

    /**
     * The extra that goes in an intent when returning from Dropbox auth to
     * provide the user's Dropbox UID, if auth succeeded. You won't ever have
     * to use this.
     */
    public static final String EXTRA_UID = "UID";

    /**
     * Used for internal authentication. You won't ever have to use this.
     */
    public static final String EXTRA_CONSUMER_SIG = "CONSUMER_SIG";

    /**
     * Used for internal authentication. You won't ever have to use this.
     */
    public static final String EXTRA_CALLING_PACKAGE = "CALLING_PACKAGE";

    /*
     * The authenticate action can be changed in the future if the interface
     * between the official app and the developer's portion changes as a way to
     * track versions.
     */
    /**
     * The Android action which the official Dropbox app will accept to
     * authenticate a user. You won't ever have to use this.
     */
    public static final String ACTION_AUTHENTICATE_V1 = "com.dropbox.android.AUTHENTICATE_V1";

    public static final int AUTH_VERSION = 1;

    // For communication between AndroidAuthSesssion and this activity.
    static final String EXTRA_INTERNAL_CONSUMER_KEY = "EXTRA_INTERNAL_CONSUMER_KEY";
    static final String EXTRA_INTERNAL_CONSUMER_SECRET = "EXTRA_INTERNAL_CONSUMER_SECRET";
    static Intent lastResult = null;

    private String consumerKey = null;
    private String consumerSecret = null;
    private boolean hasDelegated = false;
    private boolean gotNewIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            consumerKey = savedInstanceState.getString("consumerKey");
            consumerSecret = savedInstanceState.getString("consumerSecret");
            hasDelegated = savedInstanceState.getBoolean("hasDelegated");
        }

        if (consumerKey == null) {
            Intent intent = getIntent();
            consumerKey = intent.getStringExtra(EXTRA_INTERNAL_CONSUMER_KEY);
            consumerSecret = intent.getStringExtra(EXTRA_INTERNAL_CONSUMER_SECRET);
        }

        setTheme(android.R.style.Theme_Translucent_NoTitleBar);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("consumerKey", consumerKey);
        outState.putString("consumerSecret", consumerSecret);
        outState.putBoolean("hasDelegated", hasDelegated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        Logger.LogDebug("DropBox Auth: Resume!");
        onNewIntent(getIntent());

        if (consumerKey == null || consumerSecret == null) {
            finish();
            return;
        }

        if (!hasDelegated) {
            // Create intent to auth with official app.
            Intent officialIntent = new Intent();
            officialIntent.setClassName("com.dropbox.android",
                    "com.dropbox.android.activity.auth.DropboxAuth");
            officialIntent.setAction(ACTION_AUTHENTICATE_V1);
            officialIntent.putExtra(EXTRA_CONSUMER_KEY,
                    consumerKey);
            officialIntent.putExtra(EXTRA_CONSUMER_SIG, getConsumerSig());
            officialIntent.putExtra(EXTRA_CALLING_PACKAGE, getPackageName());

            if (hasDropboxApp(officialIntent)) {
                startActivity(officialIntent);
            } else {
                startWebAuth();
            }

            hasDelegated = true;
        } else if (!gotNewIntent) {
            // We somehow returned to this activity without being forwarded
            // here by the official app. Most likely caused by improper setup,
            // but could have other reasons if Android is acting up and killing
            // activities used in our process.
            lastResult = new Intent();
            finish();
            return;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String token = null, secret = null, uid = null;
        
        Logger.LogDebug("DropBox Auth: onNewIntent (" + (intent != null ? intent.getExtras() : "") + ")!");

        if (intent.hasExtra(EXTRA_ACCESS_TOKEN)) {
            // Dropbox app auth.
            token = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
            secret = intent.getStringExtra(EXTRA_ACCESS_SECRET);
            uid = intent.getStringExtra(EXTRA_UID);
        } else {
            // Web auth.
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.getPath();
                if ("/connect".equals(path)) {
                    try {
                        token = uri.getQueryParameter("oauth_token");
                        secret = uri.getQueryParameter("oauth_token_secret");
                        uid = uri.getQueryParameter("uid");
                    } catch (UnsupportedOperationException e) {}
                }
            }
        }
        
        SharedPreferences sp = (SharedPreferences)getSharedPreferences("dropbox", Context.MODE_PRIVATE);
        sp.edit().putString("token", token).putString("secret", secret).putString("uid", uid).commit();
        
        setIntent(intent);
        setResult(1, intent);

        // Pass along everything to the calling app.
        lastResult = new Intent();
        if (token != null && !token.equals("") &&
                secret != null && !secret.equals("") &&
                uid != null && !uid.equals("")) {
            // Successful auth.
            lastResult.putExtra(EXTRA_ACCESS_TOKEN, token);
            lastResult.putExtra(EXTRA_ACCESS_SECRET, secret);
            lastResult.putExtra(EXTRA_UID, uid);
        }

        gotNewIntent = true;
        finish();
    }

    private String getConsumerSig() {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {}
        m.update(consumerSecret.getBytes(), 0, consumerSecret.length());
        BigInteger i = new BigInteger(1, m.digest());
        String s = String.format("%1$040X", i);
        return s.substring(32);
    }

    private boolean hasDropboxApp(Intent intent) {
        PackageManager manager = getPackageManager();

        if (0 == manager.queryIntentActivities(intent, 0).size()) {
            // The official app doesn't exist, or only an older version
            // is available.
            return false;
        } else {
            // The official app exists. Make sure it's the correct one by
            // checking signing keys.
            ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
            if (resolveInfo == null) {
                return false;
            }

            final PackageInfo packageInfo;
            try {
                packageInfo = manager.getPackageInfo(
                        resolveInfo.activityInfo.packageName,
                        PackageManager.GET_SIGNATURES);
            } catch (NameNotFoundException e) {
                return false;
            }

            for (Signature signature : packageInfo.signatures) {
                for (String dbSignature : DROPBOX_APP_SIGNATURES) {
                    if (dbSignature.equals(signature.toCharsString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void startWebAuth() {
        String path = "/connect";

        String[] params = {
                "k", consumerKey,
                "s", getConsumerSig(),
        };

        String url = RESTUtility.buildURL("www.dropbox.com",
                DropboxAPI.VERSION, path, params);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private static final String[] DROPBOX_APP_SIGNATURES = {
        "308202223082018b02044bd207bd300d06092a864886f70d01010405003058310b3" +
        "009060355040613025553310b300906035504081302434131163014060355040713" +
        "0d53616e204672616e636973636f3110300e060355040a130744726f70626f78311" +
        "2301006035504031309546f6d204d65796572301e170d3130303432333230343930" +
        "315a170d3430303431353230343930315a3058310b3009060355040613025553310" +
        "b3009060355040813024341311630140603550407130d53616e204672616e636973" +
        "636f3110300e060355040a130744726f70626f783112301006035504031309546f6" +
        "d204d6579657230819f300d06092a864886f70d010101050003818d003081890281" +
        "8100ac1595d0ab278a9577f0ca5a14144f96eccde75f5616f36172c562fab0e98c4" +
        "8ad7d64f1091c6cc11ce084a4313d522f899378d312e112a748827545146a779def" +
        "a7c31d8c00c2ed73135802f6952f59798579859e0214d4e9c0554b53b26032a4d2d" +
        "fc2f62540d776df2ea70e2a6152945fb53fef5bac5344251595b729d48102030100" +
        "01300d06092a864886f70d01010405000381810055c425d94d036153203dc0bbeb3" +
        "516f94563b102fff39c3d4ed91278db24fc4424a244c2e59f03bbfea59404512b8b" +
        "f74662f2a32e37eafa2ac904c31f99cfc21c9ff375c977c432d3b6ec22776f28767" +
        "d0f292144884538c3d5669b568e4254e4ed75d9054f75229ac9d4ccd0b7c3c74a34" +
        "f07b7657083b2aa76225c0c56ffc",
        "308201e53082014ea00302010202044e17e115300d06092a864886f70d010105050" +
        "03037310b30090603550406130255533110300e060355040a1307416e64726f6964" +
        "311630140603550403130d416e64726f6964204465627567301e170d31313037303" +
        "93035303331375a170d3431303730313035303331375a3037310b30090603550406" +
        "130255533110300e060355040a1307416e64726f6964311630140603550403130d4" +
        "16e64726f696420446562756730819f300d06092a864886f70d010101050003818d" +
        "003081890281810096759fe5abea6a0757039b92adc68d672efa84732c3f959408e" +
        "12efa264545c61f23141026a6d01eceeeaa13ec7087087e5894a3363da8bf5c69ed" +
        "93657a6890738a80998e4ca22dc94848f30e2d0e1890000ae2cddf543b20c0c3828" +
        "deca6c7944b5ecd21a9d18c988b2b3e54517dafbc34b48e801bb1321e0fa49e4d57" +
        "5d7f0203010001300d06092a864886f70d0101050500038181002b6d4b65bcfa6ec" +
        "7bac97ae6d878064d47b3f9f8da654995b8ef4c385bc4fbfbb7a987f60783ef0348" +
        "760c0708acd4b7e63f0235c35a4fbcd5ec41b3b4cb295feaa7d5c27fa562a02562b" +
        "7e1f4776b85147be3e295714986c4a9a07183f48ea09ae4d3ea31b88d0016c65b93" +
        "526b9c45f2967c3d28dee1aff5a5b29b9c2c8639"};
}


package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.ServerSetupActivity;
import org.brandroid.openmanager.data.OpenNetworkPath.Cancellable;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.SimpleCrypto;
import org.brandroid.utils.Utils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.RESTUtility.RequestMethod;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class OpenDropBox extends OpenNetworkPath implements OpenNetworkPath.CloudOpsHandler,
        OpenPath.OpenPathSizable, OpenPath.SpaceHandler, OpenPath.ThumbnailHandler,
        OpenPath.OpenStream,
        OpenPath.ThumbnailOverlayInterface {

    private static final long serialVersionUID = 5742031992345655964L;
    private final OpenDropBox mParent;
    private Entry mEntry;
    private final DropboxAPI<AndroidAuthSession> mAPI;
    private List<OpenPath> mChildren = null;
    private String mName = null;
    private Account mAccount;
    private String mAuthState;

    private final static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
    
    static {
        Logger.setHandler("Dropbox");
        //Logger.setHandler(HttpRequestBase.class.getName());
    }

    public OpenDropBox(DropboxAPI<AndroidAuthSession> api)
    {
        mAPI = api;
        mParent = null;
    }

    public OpenDropBox(OpenDropBox parent, Entry child)
    {
        mParent = parent;
        mAPI = mParent.mAPI;
        mEntry = child;
    }
    
    public static boolean isEnabled(Context context) {
        Preferences prefs = new Preferences(context);
        if(!prefs.getBoolean("global", "pref_cloud_dropbox_enabled", true)) return false;
        if(prefs.getString("global", "pref_cloud_dropbox_key", PrivatePreferences.getKey("dropbox_key")).equals("")) return false;
        if(prefs.getString("global", "pref_cloud_dropbox_secret", PrivatePreferences.getKey("dropbox_secret")).equals("")) return false;
        return true;
    }

    @Override
    public boolean copyTo(OpenNetworkPath path, final CloudCompletionListener callback) {
        if (path instanceof OpenDropBox)
        {
            final OpenDropBox folder = (OpenDropBox)path;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final Entry entry = mAPI.copy(getDBPath(), folder.getDBPath());
                        post(new Runnable() {
                            public void run() {
                                callback.onCloudComplete(entry.path + " created!");
                            }
                        });
                    } catch (Exception e) {
                        postException(e, callback);
                    }
                }
            }).start();
        }
        return false;
    }

    public boolean delete(final CloudDeleteListener callback) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    mAPI.delete(mEntry.path);
                    post(new Runnable() {
                        public void run() {
                            callback.onDeleteComplete(mEntry.path + " deleted!");
                        }
                    });
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        }).start();
        return true;
    }

    public String getDBPath()
    {
        if (mEntry != null)
            return mEntry.path;
        return getPath();
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    public interface GetAccountInfoCallback extends OpenPath.ExceptionListener {
        public void onGetAccountInfo(Account account);
    }

    public void getAccountInfo(final GetAccountInfoCallback callback)
    {
        Account account = getAccountInfo();
        if (account != null)
            callback.onGetAccountInfo(account);
        else
            new Thread(new Runnable() {
                public void run() {
                    try {
                        AndroidAuthSession session = getSession();

                        @SuppressWarnings("unchecked")
                        Map<String, Object> accountInfo =
                                (Map<String, Object>)RESTUtility.request(RequestMethod.GET,
                                        session.getAPIServer(), "/account/info", AUTH_VERSION,
                                        new String[] {
                                                "locale", session.getLocale().toString()
                                        },
                                        session);

                        mAccount = new Account(accountInfo);

                        OpenServer server = getServer();
                        if (server != null)
                        {
                            server.setSetting("account",
                                    new JSONObject(accountInfo).toString());
                        }

                        post(new Runnable() {
                            public void run() {
                                callback.onGetAccountInfo(mAccount);
                            }
                        });
                    } catch (final DropboxException e) {
                        postException(e, callback);
                    }
                }
            }).start();
    }

    public Account getAccountInfo()
    {
        if (mAccount != null)
            return mAccount;
        if (mParent != null)
            return mParent.getAccountInfo();
        if (getServer() != null)
        {
            if (getServer().has("account"))
            {
                String sa = getServer().get("account");
                JSONParser jp = new JSONParser();
                try {
                    Map<String, Object> map = (Map<String, Object>)jp.parse(sa);
                    mAccount = new Account(map);
                    return mAccount;
                } catch (ParseException e) {
                }
            }
        }
        return null;
    }

    @Override
    public OpenNetworkPath[] getChildren() {
        if (mChildren != null)
            return mChildren.toArray(new OpenDropBox[mChildren.size()]);
        return null;
    }

    public String getToken() {
        return mAPI.getSession().getAccessTokenPair().secret;
    }

    @Override
    public Thread list(final ListListener listener) {
        if (mChildren != null)
            listener.onListReceived(getChildren());

        mChildren = new Vector<OpenPath>();
        return thread(new Runnable() {
            public void run() {
                try {
                    Entry e = mAPI.metadata(getPath(), -1, null, true, null);
                    for (Entry kid : e.contents)
                    {
                        OpenDropBox child = new OpenDropBox(OpenDropBox.this, kid);
                        mChildren.add(child);
                    }
                    postListReceived(getChildren(), listener);
                } catch (DropboxException e) {
                    postException(e, listener);
                }
            }
        });
    }

    public void list(final OpenContentUpdateListener callback) throws IOException {
        if (mChildren != null)
        {
            for (OpenPath kid : mChildren)
                callback.addContentPath(kid);
            callback.doneUpdating();
            return;
        }
        mChildren = new Vector<OpenPath>();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Entry e = mAPI.metadata(getPath(), -1, null, true, null);
                    for (Entry kid : e.contents)
                    {
                        OpenDropBox child = new OpenDropBox(OpenDropBox.this, kid);
                        mChildren.add(child);
                        callback.addContentPath(child);
                    }
                    callback.doneUpdating();
                } catch (DropboxException e) {
                    callback.onException(e);
                }
            }
        }).start();
    }

    public AndroidAuthSession getSession() {
        return mAPI.getSession();
    }

    public static AppKeyPair getAppKeyPair()
    {
        return new AppKeyPair(PrivatePreferences.getKey("DROPBOX_KEY"),
                PrivatePreferences.getKey("DROPBOX_SECRET"));
    }

    public static AndroidAuthSession buildSession(OpenServer server) {
        AppKeyPair appKeyPair = getAppKeyPair();
        AndroidAuthSession session;

        String[] stored = null;
        String pw = server.getPassword();
        if (pw != null && !pw.equals(""))
        {
            if (DEBUG)
                Logger.LogDebug("DropBox pw: " + pw);
            if (pw.indexOf(",") == -1)
            {
                try {
                    pw = SimpleCrypto.decrypt(pw, OpenServers.getDecryptKey());
                } catch (Exception e) {
                }
            }
            stored = pw.split(",");
        }
        if (stored != null && stored.length == 2) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, AccessType.DROPBOX, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, AccessType.DROPBOX);
        }

        return session;
    }

    /**
     * Starts the Dropbox authentication process by launching an external app
     * (either the Dropbox app if available or a web browser) where the user
     * will log in and allow your app access.
     * 
     * @param context the {@link Context} which to use to launch the Dropbox
     *            authentication activity. This will typically be an
     *            {@link Activity} and the user will be taken back to that
     *            activity after authentication is complete (i.e., your activity
     *            will receive an {@code onResume()}).
     * @throws IllegalStateException if you have not correctly set up the
     *             AuthActivity in your manifest, meaning that the Dropbox app
     *             will not be able to redirect back to your app after auth.
     */
    public static boolean startAuthentication(final Activity c, final WebView web, String state) {
        AppKeyPair appKeyPair = getAppKeyPair();

        Intent intent = getOfficialIntent(c, appKeyPair.key, appKeyPair.secret, state);
        if (!OpenServers.getDefaultServers().hasServerType("db")
                && hasDropboxApp(c, intent)) {
            c.startActivityForResult(intent, OpenExplorer.REQ_AUTHENTICATE_DROPBOX);
            return true;
        } else {
            //startWebAuth(c, appKeyPair);
            return false;
            //web.loadUrl(AuthActivity.getConnectUrl(appKeyPair.key, getConsumerSig(appKeyPair.secret)));
            //web.setVisibility(View.VISIBLE);
        }
    }

    private static void startWebAuth(Context c, AppKeyPair kp) {
        String path = "/connect";

        String[] params = {
                "k", kp.key,
                "s", getConsumerSig(kp.secret),
        };

        String url = RESTUtility.buildURL("www.dropbox.com",
                DropboxAPI.VERSION, path, params);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        c.startActivity(intent);
    }

    public static boolean handleIntent(Intent intent, OpenServer out)
    {
        if (intent == null || intent.getExtras() == null)
            return false;
        if (DEBUG)
            Logger.LogDebug("OpenDropBox.handleIntent: " + intent.getExtras().keySet().toString());
        String token, secret, uid;
        if (intent.hasExtra(EXTRA_ACCESS_TOKEN)) {
            // Dropbox app auth.
            token = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
            secret = intent.getStringExtra(EXTRA_ACCESS_SECRET);
            uid = intent.getStringExtra(EXTRA_UID);
        } else {
            // Web auth.
            Uri uri = intent.getData();
            if (uri != null) {
                if (DEBUG)
                    Logger.LogDebug("OpenDropBox.handle Web Auth: " + uri.toString());
                String path = uri.getPath();
                if ("/connect".equals(path)) {
                    try {
                        token = uri.getQueryParameter("oauth_token");
                        secret = uri.getQueryParameter("oauth_token_secret");
                        uid = uri.getQueryParameter("uid");
                    } catch (UnsupportedOperationException e) {
                        return false;
                    }
                } else
                    return false;
            } else
                return false;
        }
        if (token != null && !token.equals(""))
        {
            out.setPassword(token + "," + secret);
            out.setUser(uid);
            if (DEBUG)
                Logger.LogDebug("OpenDropBox auth finish. Token: " + token + "  Secret: " + secret);
            return true;
        }
        return false;
    }

    public static Intent getOfficialIntent(Activity a, String key, String secret, String state)
    {
        Intent officialIntent = new Intent();
        officialIntent.setClassName("com.dropbox.android",
                "com.dropbox.android.activity.auth.DropboxAuth");
        officialIntent.setAction(ACTION_AUTHENTICATE_V2);
        officialIntent.putExtra(EXTRA_CONSUMER_KEY, key);
        officialIntent.putExtra(EXTRA_CONSUMER_SIG, getConsumerSig(secret));
        officialIntent.putExtra(EXTRA_CALLING_PACKAGE, a.getPackageName());
        officialIntent.putExtra(EXTRA_CALLING_CLASS, a.getClass().getName());
        officialIntent.putExtra(EXTRA_AUTH_STATE, state);
        return officialIntent;
    }

    public static boolean hasDropboxApp(Context c, Intent intent) {
        PackageManager manager = c.getPackageManager();

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

    public static String getConsumerSig(String consumerSecret) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
        }
        m.update(consumerSecret.getBytes(), 0, consumerSecret.length());
        BigInteger i = new BigInteger(1, m.digest());
        String s = String.format("%1$040X", i);
        return s.substring(32);
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
                    "526b9c45f2967c3d28dee1aff5a5b29b9c2c8639"
    };

    public static String createStateNonce() {
        final int NONCE_BYTES = 16; // 128 bits of randomness.
        byte randomBytes[] = new byte[NONCE_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NONCE_BYTES; ++i) {
            sb.append(String.format("%02x", (randomBytes[i]&0xff)));
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        if (mEntry != null)
            return mEntry.fileName();
        if (mName != null)
            return mName;
        if (getServer() != null)
            return getServer().getName();
        if (getAccountInfo() != null)
            return getAccountInfo().displayName;
        return "??";
    }

    @Override
    public String getPath() {
        String ret = "/";
        if (mEntry != null)
            ret = mEntry.path;
        return ret;
        // return "db-" + mAPI.getSession().getAppKeyPair().key + "://1" + ret;
    }

    public void setPath(String path)
    {
        mEntry = new Entry();
        mEntry.path = path;
    }

    @Override
    public String getAbsolutePath() {
        TokenPair access = mAPI.getSession().getAccessTokenPair();
        String ret = "db-" + PrivatePreferences.getKey("dropbox_key") + "://";
        if (access != null)
            ret += Utils.urlencode(access.key) + ":" + Utils.urlencode(access.secret) + "@";
        ret += "www.dropbox.com" + getPath();
        return ret;
    }

    @Override
    public long length() {
        if (mChildren != null)
            return mChildren.size();
        if (mEntry != null)
            return mEntry.bytes;
        return 0;
    }

    @Override
    public OpenPath getParent() {
        if (mParent != null)
            return mParent;
        return null;
    }

    @Override
    public OpenPath getChild(String name) {
        try {
            for (OpenPath p : listFiles())
                if (p.getName().equals(name))
                    return p;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public OpenPath[] list() throws IOException {
        if (mChildren != null)
            return mChildren.toArray(new OpenDropBox[mChildren.size()]);
        return null;
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        if (mChildren != null)
            return mChildren.toArray(new OpenPath[mChildren.size()]);
        return list();
    }

    @Override
    public Boolean isDirectory() {
        if (mEntry == null)
            return true;
        return mEntry.isDir;
    }

    @Override
    public Boolean isFile() {
        return !isDirectory();
    }

    @Override
    public Boolean isHidden() {
        return false;
    }

    @Override
    public Uri getUri() {
        return Uri.parse(getAbsolutePath());
    }

    @Override
    public Long lastModified() {
        if (mEntry != null)
        {
            Date d = RESTUtility.parseDate(mEntry.modified);
            return d.getTime();
        }
        return 0l;
    }

    @Override
    public Boolean canRead() {
        return true;
    }

    @Override
    public String getMimeType() {
        if (mEntry != null && mEntry.mimeType != null)
            return mEntry.mimeType;
        return super.getMimeType();
    }

    @Override
    public Boolean canWrite() {
        return false;
    }

    @Override
    public Boolean canExecute() {
        return false;
    }

    @Override
    public Boolean exists() {
        if (mEntry != null)
            return !mEntry.isDeleted;
        return true;
    }

    @Override
    public Boolean delete() {
        return false;
    }

    @Override
    public Boolean mkdir() {
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return mAPI.getFileStream(mEntry.path, mEntry.rev);
        } catch (DropboxException e) {
            if (Build.VERSION.SDK_INT > 8)
                throw new IOException("DropboxException getting InputStream!", e);
            else
                throw new IOException("DropboxException getting InputStream: " + e.getMessage());
        }
    }

    @Override
    public boolean syncUpload(final OpenFile f, final NetworkListener l) {
        InputStream is = null;
        try {
            is = f.getInputStream();
            mAPI.putFile(getPath(), is, f.length(), null,
                    new ProgressListener() {
                        public void onProgress(long bytes, long total) {
                            l.OnNetworkCopyUpdate((int)bytes, (int)total);
                        }
                    });
        } catch (Exception e) {
            l.OnNetworkFailure(this, f, e);
        }
        return false;
    }

    @Override
    public boolean syncDownload(final OpenFile f, final NetworkListener l) {
        OutputStream os = null;
        try {
            os = f.getOutputStream();
            DropboxFileInfo fi = mAPI.getFile(getPath(), null, os,
                    new ProgressListener() {
                        public void onProgress(long bytes, long total) {
                            l.OnNetworkCopyUpdate((int)bytes, (int)total);
                        }
                    });
            return fi != null;
        } catch (Exception e) {
            l.OnNetworkFailure(this, f, e);
        }
        return false;
    }

    @Override
    public boolean hasThumbnail() {
        if (mEntry != null)
            return mEntry.thumbExists;
        return super.hasThumbnail();
    }

    @Override
    public boolean getThumbnail(final OpenApp app, final int w, final ThumbnailReturnCallback callback) {
        if (!hasThumbnail())
            return false;
        new Thread(new Runnable() {
            public void run() {
                ThumbSize sz = ThumbSize.ICON_32x32;
                if (w > 128)
                    sz = ThumbSize.ICON_256x256;
                else if (w > 64)
                    sz = ThumbSize.ICON_128x128;
                else if (w > 32)
                    sz = ThumbSize.ICON_64x64;
                try {
                    DropboxInputStream input = mAPI.getThumbnailStream(mEntry.path, sz,
                            ThumbFormat.PNG);
                    final BitmapDrawable bd = new BitmapDrawable(app.getResources(), input);
                    post(new Runnable() {
                        public void run() {
                            callback.onThumbReturned(bd);
                        }
                    });
                } catch (DropboxException e) {

                }
            }
        }).start();
        return false;
    }

    @Override
    public boolean isImageFile() {
        if (mEntry != null)
            return mEntry.thumbExists;
        return false;
    }

    @Override
    public long getTotalSpace() {
        if (mAccount != null)
            return mAccount.quota;
        if (getServer() != null)
            return getServer().get("quota", 0l);
        return 0;
    }

    @Override
    public long getUsedSpace() {
        if (mAccount != null)
            return mAccount.quotaNormal;
        if (getServer() != null)
            return getServer().get("normal", 0l);
        return 0;
    }

    public long getFreeSpace() {
        return getTotalSpace() - getUsedSpace();
    }

    /**
     * The extra that goes in an intent to provide your consumer key for Dropbox
     * authentication. You won't ever have to use this.
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
     * provide the user's access token secret, if auth succeeded. You won't ever
     * have to use this.
     */
    public static final String EXTRA_ACCESS_SECRET = "ACCESS_SECRET";

    /**
     * The extra that goes in an intent when returning from Dropbox auth to
     * provide the user's Dropbox UID, if auth succeeded. You won't ever have to
     * use this.
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

    /**
     * Used for internal authentication. You won't ever have to use this.
     */
    public static final String EXTRA_CALLING_CLASS = "CALLING_CLASS";

    /**
     * Used for internal authentication. You won't ever have to use this.
     */
    public static final String EXTRA_AUTH_STATE = "AUTH_STATE";

    public static final String ACTION_AUTHENTICATE_V1 = "com.dropbox.android.AUTHENTICATE_V1";
    public static final String ACTION_AUTHENTICATE_V2 = "com.dropbox.android.AUTHENTICATE_V2";

    public static final int AUTH_VERSION = 2;

    // For communication between AndroidAuthSesssion and this activity.
    static final String EXTRA_INTERNAL_CONSUMER_KEY = "EXTRA_INTERNAL_CONSUMER_KEY";
    static final String EXTRA_INTERNAL_CONSUMER_SECRET = "EXTRA_INTERNAL_CONSUMER_SECRET";

    @Override
    public Drawable getOverlayDrawable(Context c, boolean large) {
        return c.getResources().getDrawable(
                large ? R.drawable.lg_dropbox_overlay : R.drawable.sm_dropbox_overlay);
    }

    public void unlink() {
        getSession().unlink();
    }

    @Override
    public void getSpace(final SpaceListener callback) {
        if(mAccount != null)
        {
            callback.onSpaceReturned(mAccount.quota, mAccount.quotaNormal, mAccount.quotaShared);
        }
        if(getServer() != null && getServer().get("quota", 0) > 0)
        {
            callback.onSpaceReturned(getTotalSpace(), getUsedSpace(), getThirdSpace());
            return;
        }
        thread(new Runnable() {
            public void run() {
                try {
                    final Account account = mAPI.accountInfo();
                    OpenServer server = getServer();
                    server.setSetting("quota", account.quota);
                    server.setSetting("normal", account.quotaNormal);
                    server.setSetting("shared", account.quotaShared);
                    post(new Runnable() {
                        public void run() {
                            callback.onSpaceReturned(
                                    account.quota,
                                    account.quotaNormal,
                                    account.quotaShared
                                    );
                        }
                    });

                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        });
    }

    @Override
    public long getThirdSpace() {
        if(mAccount != null)
            return mAccount.quotaShared;
        return 0;
    }

    @Override
    public Cancellable uploadToCloud(final OpenFile file, final CloudProgressListener callback) {
        return runCloud(new Runnable() {
            public void run() {
                try {
                    mAPI.putFile(getFilePath(), file.getInputStream(), file.length(), null, new ProgressListener() {
                        public void onProgress(long bytes, long total) {
                            callback.onProgress(bytes);
                        }
                    });
                    post(new Runnable() {
                        public void run() {
                            callback.onCloudComplete("Upload complete");
                        }
                    });
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        }, callback);
    }
    
    private String getFilePath() {
        if(mEntry != null)
            return mEntry.path;
        return super.getRemotePath();
    }

    @Override
    public Cancellable downloadFromCloud(final OpenFile file, final CloudProgressListener callback) {
        return runCloud(new Runnable() {
            public void run() {
                try {
                    mAPI.getFile(getFilePath(), null, file.getOutputStream(), new ProgressListener() {
                        public void onProgress(final long bytes, long total) {
                            post(new Runnable() {
                                public void run() {
                                    callback.onProgress(bytes);
                                }
                            });
                        }
                    });
                    post(new Runnable() {
                        public void run() {
                            callback.onCloudComplete("Download complete");
                        }
                    });
                } catch(Exception e) {
                    postException(e, callback);
                }
            }
        }, callback);
    }

    @Override
    public boolean touch(final CloudCompletionListener callback) {
        runCloud(new Runnable() {
            public void run() {
                try {
                    mAPI.putFileRequest(getFilePath(), null, 0, null, null);
                } catch(Exception e) {
                    postException(e, callback);
                }
            }
        }, callback);
        return true;
    }
    
    @Override
    public void clearChildren() {
        mChildren.clear();
    }
}

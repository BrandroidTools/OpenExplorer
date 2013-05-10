
package org.brandroid.openmanager.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenPath.SpaceHandler;
import org.brandroid.openmanager.interfaces.OpenApp;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

public class OpenDrive extends OpenNetworkPath implements OpenNetworkPath.CloudOpsHandler,
        OpenPath.ThumbnailOverlayInterface, OpenPath.ThumbnailHandler, SpaceHandler,
        OpenPath.OpenPathUpdateHandler {

    public final static String DRIVE_SCOPE_AUTH_TYPE = "oauth2:" + DriveScopes.DRIVE;

    public Drive mDrive;
    private GoogleCredential mCredential;
    private String mName = null;
    private String mFolderId = "root";
    private OpenDrive mParent;
    private final File mFile;
    private static final WeakHashMap<String, OpenDrive> mGlobalFiles = new WeakHashMap<String, OpenDrive>();
    private static final WeakHashMap<String, List<OpenDrive>> mGlobalChildren = new WeakHashMap<String, List<OpenDrive>>();
    public static final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    public static final JsonFactory mJsonFactory = new GsonFactory();
    public static final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
    private static final String mFolderFields = "items(downloadUrl,editable,fileSize,iconLink,thumbnailLink,id,kind,labels/hidden,mimeType,modifiedDate,parents(id,isRoot),thumbnail/image,title),nextPageToken";

    public OpenDrive(String accessToken, String refreshToken)
    {
        mCredential = new GoogleCredential.Builder()
                .setClientSecrets(
                        PrivatePreferences.getKey("drive_key"),
                        PrivatePreferences.getKey("drive_secret"))
                .setTransport(mTransport)
                .setJsonFactory(mJsonFactory)
                .build()
                .setRefreshToken(refreshToken)
                .setAccessToken(accessToken);
        setCredential(mCredential);
        mParent = null;
        mFile = null;
        Logger.setHandler("com.google.api.client.http");
    }

    public OpenDrive(OpenDrive parent, File file)
    {
        mParent = parent;
        mCredential = mParent.mCredential;
        mDrive = mParent.mDrive;
        mFile = file;
        mFolderId = file.getId();
    }
    
    public static boolean isEnabled(Context context) {
        Preferences prefs = new Preferences(context);
        if(!prefs.getBoolean("global", "pref_cloud_drive_enabled", true)) return false;
        if(prefs.getString("global", "pref_cloud_drive_key", PrivatePreferences.getKey("drive_key")).equals("")) return false;
        if(prefs.getString("global", "pref_cloud_drive_secret", PrivatePreferences.getKey("drive_secret")).equals("")) return false;
        return true;
    }

    public GoogleCredential getCredential() {
        return mCredential;
    }

    public void setCredential(GoogleCredential cred) {
        mCredential = cred;
        mDrive = new Drive
                .Builder(mTransport, mJsonFactory, mCredential)
                        .setApplicationName("OpenExplorer/1.0")
                        .build();
    }

    @Override
    public void setName(String name) {
        mName = name;
    }
    
    @Override
    public CharSequence getTitle(Context context) {
        SpannableStringBuilder ret = new SpannableStringBuilder(getName());
        if(mParent == null)
        {
            Resources r = context.getResources();
            float h = r.getDimension(R.dimen.abs__action_bar_default_height) * 0.75f;
            Drawable d = new BitmapDrawable(r, Bitmap.createScaledBitmap(
                    ((BitmapDrawable)r.getDrawable(R.drawable.icon_drive)).getBitmap(),
                    (int)h, (int)h, false));
            ret.insert(0, " ").setSpan(new ImageSpan(d), 0, 1, Spannable.SPAN_COMPOSING);
        }
        return ret;
    }

    @Override
    public String getName() {
        if (mFile != null)
        {
            if (mFile.getTitle() != null)
                return mFile.getTitle();
            if (mFile.getDescription() != null)
                return mFile.getDescription();
            if (mFile.getOriginalFilename() != null)
                return mFile.getOriginalFilename();
        }
        return mName;
    }
    
    @Override
    public void clearChildren() {
        if(mGlobalChildren.containsKey(getId()))
            mGlobalChildren.remove(getId());
    }

    @Override
    public boolean syncUpload(OpenFile f, NetworkListener l) {
        return false;
    }

    @Override
    public boolean syncDownload(OpenFile f, NetworkListener l) {
        try {
            OutputStream os = new BufferedOutputStream(f.getOutputStream());
            InputStream in = new BufferedInputStream(getInputStream());
            copyStreams(in, os);
            return true;
        } catch (Exception e) {
            l.OnNetworkFailure(this, f, e);
        }
        return false;
    }

    @Override
    public OpenNetworkPath[] getChildren() {
        if (mGlobalChildren != null && mGlobalChildren.containsKey(mFolderId))
            return mGlobalChildren.get(mFolderId).toArray(new OpenDrive[0]);
        return new OpenNetworkPath[0];
    }

    @Override
    public String getPath() {
        String ret = "";
        if(getParent() != null)
            ret = getParent().getPath();
        else ret = getPathPrefix(false);
        if(!ret.endsWith("/"))
            ret += "/";
        if(mFile != null)
            ret += mFile.getId();
        if(isDirectory() && !ret.endsWith("/"))
            ret += "/";
        return ret;
    }

    public String getPathPrefix(boolean includeToken)
    {
        String ret = "drive://";
        if (mCredential != null && mCredential.getServiceAccountUser() != null)
            ret += Utils.urlencode(mCredential.getServiceAccountUser()) + ":";
        else if(getServer() != null)
            ret += Utils.urlencode(getServer().getUser()) + ":";
        if (includeToken && mCredential != null)
            ret += Uri.encode(mCredential.getAccessToken()) + "@";
        ret += "drive.brandroid.org/";
        return ret;
    }
    
    public interface TicketResponseCallback extends OpenPath.ExceptionListener {
        public void onTicketReceived(String ticket);
    }
    
    public static void getTicket(final TicketResponseCallback callback)
    {
        new Thread(new Runnable() {
            public void run() {
                String url = "https://accounts.google.com/o/oauth2/auth";
                String params = "scope=" + Uri.encode(DriveScopes.DRIVE);
                params += "&client_id=" + Uri.encode(PrivatePreferences.getKey("drive_key"));
                params += "&response_type=code&redirect_uri=" + Uri.encode("urn:ietf:wg:oauth:2.0:oob");
                HttpURLConnection uc = null;
                OutputStreamWriter out = null;
                BufferedReader br = null;
                try {
                    url += "?" + params;
                    Logger.LogVerbose("Ticket URL: " + url);
                    uc = (HttpURLConnection)new URL(url).openConnection();
//                    uc.setDoOutput(true);
//                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                    uc.setRequestMethod("POST");
//                    uc.setReadTimeout(2000);
//                    out = new OutputStreamWriter(uc.getOutputStream());
//                    out.write(params);
//                    out.flush();
//                    out.close();
                    uc.connect();
                    StringBuilder sb = new StringBuilder();
                    Logger.LogVerbose("Ticket response: " + uc.getResponseCode());
                    if(uc.getResponseCode() < 400)
                    {
                        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                        String line = "";
                        while((line = br.readLine()) != null)
                        {
                            if(line.indexOf("<title>") > -1)
                            {
                                sb = new StringBuilder(line.replaceAll("<[^>]*>", ""));
                                break;
                            }
                            sb.append(line);
                        }
                    }
                    final String ticket = sb.toString();
                    post(new Runnable() {
                        public void run() {
                            callback.onTicketReceived(ticket);
                        }
                    });
                } catch(Exception e) {
                    postException(e, callback);
                }
                finally {
                    if(br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                        }
                }
            }
        }).start();
    }
    
    public static void refreshToken(final String token, final TicketResponseCallback callback)
    {
        new Thread(new Runnable() {
            public void run() {
                String url = "https://accounts.google.com/o/oauth2/token";
                String params = "scope=" + Uri.encode(DriveScopes.DRIVE);
                params += "&client_id=" + Uri.encode(PrivatePreferences.getKey("drive_key"));
                params += "&client_secret=" + Uri.encode(PrivatePreferences.getKey("drive_secret"));
                params += "&refresh_token=" + Uri.encode(token);
                params += "&grant_type=authorization_code";
                HttpURLConnection uc = null;
                OutputStreamWriter out = null;
                BufferedReader br = null;
                try {
                    //url += "?" + params;
                    Logger.LogVerbose("Token URL: " + url + "?" + params);
                    uc = (HttpURLConnection)new URL(url).openConnection();
                    uc.setDoOutput(true);
                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    uc.setRequestMethod("POST");
                    uc.setReadTimeout(2000);
                    out = new OutputStreamWriter(uc.getOutputStream());
                    out.write(params);
                    out.flush();
                    out.close();
                    uc.connect();
                    StringBuilder sb = new StringBuilder();
                    Logger.LogVerbose("Ticket response: " + uc.getResponseCode());
                    if(uc.getResponseCode() < 400)
                    {
                        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                        String line = "";
                        while((line = br.readLine()) != null)
                        {
                            if(line.indexOf("<title>") > -1)
                            {
                                sb = new StringBuilder(line.replaceAll("<[^>]*>", ""));
                                break;
                            }
                            sb.append(line);
                        }
                    }
                    final String ticket = sb.toString();
                    post(new Runnable() {
                        public void run() {
                            callback.onTicketReceived(ticket);
                        }
                    });
                } catch(Exception e) {
                    postException(e, callback);
                }
                finally {
                    if(br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                        }
                }
            }
        }).start();
    }
    
    public static String getTokenAuthURL()
    {
        String url = "https://accounts.google.com/o/oauth2/auth";
        String params = "scope=" + Uri.encode(DriveScopes.DRIVE);
        params += "&client_id=" + Uri.encode(PrivatePreferences.getKey("drive_key"));
        params += "&response_type=code&redirect_uri=" + Uri.encode("urn:ietf:wg:oauth:2.0:oob");
        return url + "?" + params;
    }
    
    public interface TokenResponseCallback extends OpenPath.ExceptionListener {
        public void onTokenReceived(String accessToken, String refreshToken);
    }
    
    public static void getToken(final String authCode, final TokenResponseCallback callback)
    {
        new Thread(new Runnable() {
            public void run() {
                String url = "https://accounts.google.com/o/oauth2/token";
                String params = "code=" + Uri.encode(authCode);
                params += "&client_id=" + Uri.encode(PrivatePreferences.getKey("drive_key"));
                params += "&client_secret=" + Uri.encode(PrivatePreferences.getKey("drive_secret"));
                params += "&redirect_uri=" + Uri.encode("urn:ietf:wg:oauth:2.0:oob");
                params += "&grant_type=authorization_code";
                HttpURLConnection uc = null;
                OutputStreamWriter out = null;
                BufferedReader br = null;
                try {
                    //url += "?" + params;
                    Logger.LogVerbose("Token URL: " + url + "?" + params);
                    uc = (HttpURLConnection)new URL(url).openConnection();
                    uc.setDoOutput(true);
                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    uc.setRequestMethod("POST");
                    uc.setReadTimeout(2000);
                    out = new OutputStreamWriter(uc.getOutputStream());
                    out.write(params);
                    out.flush();
                    out.close();
                    uc.connect();
                    StringBuilder sb = new StringBuilder();
                    Logger.LogVerbose("Ticket response: " + uc.getResponseCode());
                    //if(uc.getResponseCode() < 400)
                    {
                        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                        String line = "";
                        while((line = br.readLine()) != null)
                        {
                            if(line.indexOf("<title>") > -1)
                            {
                                sb = new StringBuilder(line.replaceAll("<[^>]*>", ""));
                                break;
                            }
                            sb.append(line);
                        }
                    }
                    JSONParser jp = new JSONParser();
                    final JSONObject json = new JSONObject(sb.toString());
                    post(new Runnable() {
                        public void run() {
                            callback.onTokenReceived(
                                    json.optString("access_token", ""),
                                    json.optString("refresh_token", ""));
                        }
                    });
                } catch(Exception e) {
                    postException(e, callback);
                }
                finally {
                    if(br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                        }
                }
            }
        }).start();
    }

    @Override
    public String getAbsolutePath() {
        String ret = "";
        if(getParent() != null)
            ret = getParent().getPath();
        else ret = getPathPrefix(true);
        if(!ret.endsWith("/"))
            ret += "/";
        if(mFile != null)
            ret += mFile.getId();
        if(isDirectory() && !ret.endsWith("/"))
            ret += "/";
        return ret;
    }

    @Override
    public long length() {
        if (mFile != null && mFile.getFileSize() != null)
            return mFile.getFileSize();
        return -1;
    }
    
    @Override
    public String getMimeType() {
        if(mFile != null && mFile.getMimeType() != null)
            return mFile.getMimeType();
        return super.getMimeType();
    }

    @Override
    public OpenPath getParent() {
        return mParent;
    }

    @Override
    public OpenPath getChild(String name) {
        return null;
    }

    @Override
    public OpenPath[] list() throws IOException {
        if (mGlobalChildren != null)
            return getChildren();
        return listFiles();
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        if (Thread.currentThread().equals(OpenExplorer.UiThread))
            return getChildren();
        getOpenDrives(this, mDrive.files().list()
                .setFields(mFolderFields)
                .execute().getItems());
        return getChildren();
    }

    @Override
    public Boolean isDirectory() {
        if (mFile != null)
        {
            if (mFile.getMimeType().equals("application/vnd.google-apps.folder"))
                return true;
            return false;
        }
        return true;
    }

    @Override
    public Boolean isFile() {
        return !isDirectory();
    }

    @Override
    public Boolean isHidden() {
        if (mFile != null)
            if (mFile.getLabels() != null)
                return mFile.getLabels().getHidden();
        return getName().startsWith(".");
    }

    @Override
    public Uri getUri() {
        return Uri.parse(getAbsolutePath());
    }

    @Override
    public Long lastModified() {
        if (mFile != null)
            return mFile.getModifiedDate().getValue();
        return null;
    }

    @Override
    public Boolean canRead() {
        if(mFile != null && Utils.isNullOrEmpty(mFile.getDownloadUrl()))
            return false;
        return true;
    }

    @Override
    public Boolean canWrite() {
        if (mFile != null)
            return mFile.getEditable();
        return true;
    }

    @Override
    public Boolean canExecute() {
        return false;
    }

    @Override
    public Boolean exists() {
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

    @Override
    public Drawable getOverlayDrawable(Context c, boolean large) {
        return c.getResources().getDrawable(
                large ? R.drawable.lg_drive_overlay : R.drawable.sm_drive_overlay);
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public boolean copyTo(OpenNetworkPath path, final CloudCompletionListener callback) {
        if (path instanceof OpenDrive)
        {
            final OpenDrive folder = (OpenDrive)path;
            thread(new Runnable() {
                public void run() {
                    try {
                        mDrive.files().copy(getFile().getId(), folder.getFile()).execute();
                        post(new Runnable() {
                            public void run() {
                                callback.onCloudComplete("Copy completed successfully");
                            }
                        });
                    } catch (Exception e) {
                        postException(e, callback);
                    }
                }
            });
        }
        return false;
    }

    @Override
    public boolean copyTo(OpenFile f, AsyncTask task) {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(f.getOutputStream());
            InputStream in = getInputStream();
            copyStreams(in, out);
            return true;
        } catch (Exception e) {
            org.brandroid.utils.Logger.LogError("Unable to download Drive file!", e);
            return false;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                }
        }
    }

    @Override
    public boolean delete(final CloudDeleteListener callback) {
        thread(new Runnable() {
            public void run() {
                try {
                    mDrive.files().delete(getFile().getId()).execute();
                    post(new Runnable() {
                        public void run() {
                            callback.onDeleteComplete("Delete complete.");
                        }
                    });
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        });
        return false;
    }

    private static OpenDrive[] getOpenDrives(OpenDrive parent, List<File> files)
    {
        List<OpenDrive> ret = new Vector<OpenDrive>();
        for (File f : files)
        {
            OpenDrive d = new OpenDrive(parent, f);
            d.cacheParents();
            if (d.getParent().equals(parent))
                ret.add(d);
        }
        return ret.toArray(new OpenDrive[ret.size()]);
    }

    public boolean hasParent(String folderId)
    {
        for (ParentReference p : getFile().getParents())
        {
            if (folderId.equals("root") && p.getIsRoot())
                return true;
            if (p.getId().equals(folderId))
                return true;
        }
        return false;
    }

    public void cacheParents()
    {
        boolean changedParent = false;
        for (ParentReference p : getFile().getParents())
        {
            String id = p.getId();
            // if(p.getIsRoot())
            // id = "root";
            if (!id.equals(mFolderId) && !(p.getIsRoot() && mFolderId.equals("root"))
                    && mGlobalFiles != null && mGlobalFiles.containsKey(id) && !changedParent)
            {
                if(DEBUG)
                    Logger.LogVerbose("Drive.setParent(" + mGlobalFiles.get(id) + ") from " + mParent);
                mParent = mGlobalFiles.get(id);
                changedParent = true;
            }
            List<OpenDrive> kids;
            if (!mGlobalChildren.containsKey(id) || mGlobalChildren.get(id) == null)
                kids = new Vector<OpenDrive>();
            else
                kids = mGlobalChildren.get(id);
            kids.add(this);
            mGlobalChildren.put(id, kids);
        }
    }
    
    public Cancellable list(final OpenContentUpdateListener callback) {
        return cancelify(thread(new Runnable() {
            public void run() {
                try {
//                    if (mFolderId.equals("root"))
//                    {
//                        mFolderId = mDrive.files().get(mFolderId).setFields("id").execute()
//                                .getId();
//                        Logger.LogDebug("Drive root = " + mFolderId);
//                    }
                    String pg = "";
                    List<OpenDrive> kids = mGlobalChildren.get(mFolderId);
                    if (kids == null)
                        kids = new Vector<OpenDrive>();
                    else if (kids.size() > 0)
                        callback.addContentPath(kids.toArray(new OpenDrive[kids.size()]));
                    for (;;)
                    {
                        Files.List lst = mDrive
                                .files()
                                .list()
                                .setQ("'" + mFolderId + "' in parents")
                                .setFields(mFolderFields)
                                .setMaxResults(100);
                        if (!pg.equals(""))
                            lst.setPageToken(pg);
                        final FileList fl = lst.execute();
                        if (fl.size() == 0)
                            break;
                        Vector<OpenDrive> theseKids = new Vector<OpenDrive>();
                        for (OpenDrive kid : getOpenDrives(OpenDrive.this, fl.getItems()))
                        {
                            if (kids.contains(kid))
                                continue;
                            else if (kid.hasParent(mFolderId))
                                theseKids.add(kid);
                        }
                        if (theseKids.size() > 0)
                        {
                            final OpenDrive[] ka = theseKids
                                    .toArray(new OpenDrive[theseKids.size()]);
                            post(new Runnable() {
                                public void run() {
                                    callback.addContentPath(ka);
                                }
                            });
                        }
                        if (pg.equals(fl.getNextPageToken()))
                            break;
                        pg = fl.getNextPageToken();
                        if (pg == null || pg.equals(""))
                            break;
                        if(Thread.currentThread().isInterrupted())
                            break;
                    }
                } catch (Exception e) {
                    Logger.LogError("Exception while listing Drive", e);
                    postException(e, callback);
                }
            }
        }));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.getInputStream");
        InputStream in = null;
        String url = mFile.getDownloadUrl();
        if(url == null || url.length() == 0) return in;
        return new BufferedInputStream(mDrive.getRequestFactory()
                    .buildGetRequest(new GenericUrl(url))
                .execute().getContent());
    }

    @Override
    public boolean copyFrom(final OpenFile f, AsyncTask task) {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.copyFrom");
        try {
            final InputStream in = new BufferedInputStream(f.getInputStream());
            mDrive.files().insert(getFile(), new AbstractInputStreamContent(getExtension()) {
                public boolean retrySupported() {
                    return true;
                }

                public long getLength() throws IOException {
                    return f.length();
                }

                public InputStream getInputStream() throws IOException {
                    return in;
                }
            }).execute();
            return true;
        } catch (Exception e) {

        }
        return super.copyFrom(f, task);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.getOutputStream");
        return null;
    }
    
    public String getId()
    {
        if(mFile != null)
            return mFile.getId();
        return mFolderId;
    }
    
    public OpenDrive setId(String id)
    {
        if(Utils.isNullOrEmpty(id)) return this;
        if(id.indexOf("@") > -1) return this;
        if(id.equals(mName)) return this;
        if(DEBUG)
            Logger.LogVerbose("Drive.setId(" + id + ") on " + mFolderId);
        mFolderId = id;
        return this;
    }
    
    @Override
    public boolean hasThumbnail() {
        if(mFile != null && !Utils.isNullOrEmpty(mFile.getThumbnailLink()))
            return true;
        else return false;
    }
    
    @Override
    public String getThumbnailCacheFilename(int w) {
        if(hasThumbnail())
            return ThumbnailCreator.getCacheFilename(getThumbnailUrl(), w, w);
        else return super.getThumbnailCacheFilename(w);
    }
    
    public String getThumbnailUrl() {
        if(!hasThumbnail()) return null;
        String url = mFile.getThumbnailLink();
        if(Utils.isNullOrEmpty(url))
            url = mFile.getIconLink();
        return url;
    }
    
    @Override
    public boolean getThumbnail(final OpenApp app, final int w, final ThumbnailReturnCallback callback) {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.getThumbnail");
        if(hasThumbnail())
        thread(new Runnable() {
            public void run() {
                try {
                    InputStream in = new BufferedInputStream(mDrive.getRequestFactory()
                            .buildGetRequest(new GenericUrl(getThumbnailUrl()))
                            .execute().getContent());
                    callback.onThumbReturned(new BitmapDrawable(app.getResources(), in));
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        });
        else
            callback.onThumbReturned(IntentManager.getDefaultIcon(this, app));
        return true;
    }

    @Override
    public Cancellable uploadToCloud(final OpenFile file, final CloudProgressListener callback) {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.uploadToCloud");
        return runCloud(new Runnable() {
            public void run() {
                try {
                    final AbstractInputStreamContent input = new AbstractInputStreamContent(
                            "*/*") {

                        @Override
                        public boolean retrySupported() {
                            return false;
                        }

                        @Override
                        public long getLength() throws IOException {
                            return file.length();
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return file.getInputStream();
                        }
                    };
                    mDrive.files().update(getId(), getFile(), input).execute();
                    postCompletion("Upload complete", callback);
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        }, callback);
    }

    @Override
    public Cancellable downloadFromCloud(final OpenFile file, final CloudProgressListener callback) {
        if(DEBUG)
            Logger.LogVerbose("OpenDrive.downloadFromCloud");
        return runCloud(new Runnable() {
            public void run() {
                try {
                    String url = mFile.getDownloadUrl();
                    if(url == null || url.length() == 0) {
                        callback.onException(new Exception("Bad Download URL"));
                        return;
                    }
                    copyStreams(getInputStream(), file.getOutputStream(), true, true, new ProgressUpdateListener() {
                        public boolean isCancelled() {
                            return false;
                        }
                        public void onProgressUpdate(Integer... progress) {
                            if(progress.length > 0)
                                callback.onProgress((long)progress[0]);
                        }
                    });
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        }, callback);
    }

    @Override
    public boolean touch(final CloudCompletionListener callback) {
        thread(new Runnable() {
            public void run() {
                try {
                    mDrive.files().touch(getId());
                    postCompletion("File touched", callback);
                } catch (Exception e) {
                    postException(e, callback);
                }
            }
        });
        return true;
    }
    
    @Override
    public void getSpace(final SpaceListener callback) {
        final OpenServer server = getServer();
        if(server != null && server.has("total"))
        {
            callback.onSpaceReturned(server.get("total", 0),
                    server.get("agg", 0), 0);
            return;
        }
        thread(new Runnable() {
            public void run() {
                try {
                    About me = mDrive.about().get().execute();
                    if(server != null)
                    {
                        server
                            .setSetting("total", me.getQuotaBytesTotal())
                            .setSetting("agg", me.getQuotaBytesUsedAggregate())
                            .setSetting("used", me.getQuotaBytesUsed());
                    }
                    callback.onSpaceReturned(
                            me.getQuotaBytesTotal(),
                            me.getQuotaBytesUsedAggregate(),
                            me.getQuotaBytesUsed());
                } catch(Exception e) {
                    postException(e, callback);
                }
            }
        });
    }

}

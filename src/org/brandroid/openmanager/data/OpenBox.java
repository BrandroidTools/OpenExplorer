
package org.brandroid.openmanager.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Utils;
import org.brandroid.utils.ViewUtils;

import com.box.androidlib.Box;
import com.box.androidlib.BoxFile;
import com.box.androidlib.BoxFolder;
import com.box.androidlib.DAO;
import com.box.androidlib.FileDownloadListener;
import com.box.androidlib.FileUploadListener;
import com.box.androidlib.GetAccountTreeListener;
import com.box.androidlib.GetAuthTokenListener;
import com.box.androidlib.GetTicketListener;
import com.box.androidlib.User;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class OpenBox extends OpenNetworkPath implements OpenPath.OpenPathSizable,
        OpenPath.ThumbnailOverlayInterface, OpenPath.ListHandler {

    private static final long serialVersionUID = 5742031992345655964L;
    private final Box mBox;
    private final User mUser;
    private final DAO mFile;
    private final OpenBox mParent;
    private Long mId = 0l;

    private List<OpenPath> mChildren = null;

    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;

    public OpenBox(User user)
    {
        mUser = user;
        mBox = Box.getInstance(PrivatePreferences.getBoxAPIKey());
        mFile = new BoxFolder();
        ((BoxFolder)mFile).setId(0);
        mParent = null;
    }

    public OpenBox(OpenBox parent, DAO child)
    {
        mParent = parent;
        mBox = parent.mBox;
        mUser = parent.mUser;
        mFile = child;
    }

    @Override
    public OpenNetworkPath[] getChildren() {
        if (mChildren != null)
            return mChildren.toArray(new OpenBox[mChildren.size()]);
        return null;
    }

    public BoxFile getFile()
    {
        if (mFile instanceof BoxFile)
            return ((BoxFile)mFile);
        return null;
    }

    public BoxFolder getFolder()
    {
        if (mFile instanceof BoxFolder)
            return (BoxFolder)mFile;
        if (mParent != null)
            return mParent.getFolder();
        return null;
    }

    public long getFolderId()
    {
        BoxFolder folder = getFolder();
        if (folder != null)
            return folder.getId();
        return 0;
    }

    public long getId()
    {
        if (mId > 0)
            return mId;
        if (isDirectory())
            return getFolder().getId();
        return getFile().getId();
    }

    public String getToken() {
        return mUser.getAuthToken();
    }

    @Override
    public void list(final ListListener listener) {
        if(mChildren != null)
            listener.onListReceived(getChildren());
        mChildren = new Vector<OpenPath>();
        if (DEBUG)
            Logger.LogDebug("Box listing for " + getId() + "!");
        mBox.getAccountTree(getToken(), getId(), null, new GetAccountTreeListener() {
            
            @Override
            public void onIOException(IOException e) {
                postException(e, listener);
            }
            
            @Override
            public void onComplete(BoxFolder targetFolder, String status) {
                if (targetFolder != null)
                {
                    for (BoxFolder f : targetFolder.getFoldersInFolder())
                    {
                        OpenBox kid = new OpenBox(OpenBox.this, f);
                        mChildren.add(kid);
                    }
                    for (BoxFile f : targetFolder.getFilesInFolder())
                    {
                        OpenBox kid = new OpenBox(OpenBox.this, f);
                        mChildren.add(kid);
                    }
                }
                postListReceived(getChildren(), listener);
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
        if (DEBUG)
            Logger.LogDebug("Box listing for " + getId() + "!");
        mBox.getAccountTree(getToken(), getId(), new String[0], new GetAccountTreeListener() {

            @Override
            public void onIOException(IOException e) {
                callback.onUpdateException(e);
            }

            @Override
            public void onComplete(BoxFolder targetFolder, String status) {
                if (status.equals("not_logged_in"))
                    callback.onUpdateException(new Exception(status));
                if (DEBUG)
                    Logger.LogDebug("Box.onComplete!: " + status + " " + targetFolder);
                if (targetFolder != null)
                {
                    for (BoxFolder f : targetFolder.getFoldersInFolder())
                    {
                        OpenBox kid = new OpenBox(OpenBox.this, f);
                        mChildren.add(kid);
                        callback.addContentPath(kid);
                    }
                    for (BoxFile f : targetFolder.getFilesInFolder())
                    {
                        OpenBox kid = new OpenBox(OpenBox.this, f);
                        mChildren.add(kid);
                        callback.addContentPath(kid);
                    }
                }
                callback.doneUpdating();
            }
        });
    }

    @Override
    public String getName() {
        if (isDirectory() && getFolder().getId() == 0)
        {
            if(getServer() != null)
                return getServer().getName();
            if(mUser != null)
                return mUser.getLogin();
        }
        if (isDirectory() && getFolder().getFolderName() != null)
        {
            if (getFolder().getFolderName().equals(""))
                return mUser.getLogin();
            else
                return getFolder().getFolderName();
        }
        if (isFile() && getFile().getFileName() != null)
            return getFile().getFileName();
        return "???";
    }

    @Override
    public String getPath() {
        String ret = "/";
        if (getFolderId() != 0)
            ret = getParent().getPath();
        ret += getName();
        if(isDirectory() && !ret.endsWith("/"))
            ret += "/";
        return ret;
    }

    @Override
    public String getAbsolutePath() {
        return "box://" + Utils.urlencode(mUser.getLogin()) + ":" + Utils.urlencode(getToken()) + "@m.box.com" + "/" + getId();
    }

    @Override
    public long length() {
        if (isFile())
            return getFile().getSize();
        return getFolder().getFileCount();
    }

    @Override
    public OpenPath getParent() {
        if (mParent != null)
            return mParent;
        if (getId() == 0)
            return null;
        if (isDirectory() && getFolder().getParentFolder() != null)
            return new OpenBox(this, getFolder().getParentFolder());
        if (isFile() && getFile().getFolder() != null)
            return new OpenBox(this, getFile().getFolder());
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
        if(mChildren != null)
            return mChildren.toArray(new OpenBox[mChildren.size()]);
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
        return (mFile instanceof BoxFolder);
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
        if (isDirectory())
            return getFolder().getUpdated() * 1000;
        return getFile().getUpdated() * 1000;
    }

    @Override
    public Boolean canRead() {
        return true;
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
    public boolean syncUpload(final OpenFile f, final NetworkListener l) {
        if(DEBUG)
            Logger.LogDebug("OpenFTP.copyFrom(" + f + ")");
        try {
            mBox.upload(getToken(), Box.UPLOAD_ACTION_UPLOAD, f.getFile(), f.getName(),
                    getFolderId(), new FileUploadListener() {
                        public void onIOException(IOException e) {
                            l.OnNetworkFailure(OpenBox.this, f, e);
                        }

                        public void onProgress(long bytesTransferredCumulative) {
                            l.OnNetworkCopyUpdate((int)bytesTransferredCumulative);
                        }

                        public void onMalformedURLException(MalformedURLException e) {
                            l.OnNetworkFailure(OpenBox.this, f, e);
                        }

                        public void onFileNotFoundException(FileNotFoundException e) {
                            l.OnNetworkFailure(OpenBox.this, f, e);
                        }

                        public void onComplete(BoxFile boxFile, String status) {
                            l.OnNetworkCopyFinished(OpenBox.this, f);
                        }
                    });
            return true;
        } catch (Exception e) {
            if (l != null)
                l.OnNetworkFailure(this, f, e);
            return false;
        }
    }

    @Override
    public boolean syncDownload(final OpenFile f, final NetworkListener l) {
        mBox.download(getToken(), getId(), f.getFile(), null, new FileDownloadListener() {
            public void onIOException(IOException e) {
                l.OnNetworkFailure(OpenBox.this, f, e);
            }

            public void onProgress(long bytesDownloaded) {
                l.OnNetworkCopyUpdate((int)bytesDownloaded);
            }

            public void onComplete(String status) {
                l.OnNetworkCopyFinished(OpenBox.this, f);
            }
        });
        return false;
    }

    public static void handleBoxWebview(final View baseView, final OpenServer server,
            final WebView web)
    {
        final Box box = Box.getInstance(PrivatePreferences.getBoxAPIKey());
        box.getTicket(new GetTicketListener() {

            @Override
            public void onComplete(final String ticket, final String status) {
                if (status.equals("get_ticket_ok")) {
                    loadLoginWebview(baseView, server, box, ticket, web);
                }
                else {
                    Toast.makeText(baseView.getContext(), "Unable to get Box ticket!",
                            Toast.LENGTH_LONG).show();
                    Logger.LogError("Unable to get Box ticket!");
                }
            }

            @Override
            public void onIOException(final IOException e) {
                Toast.makeText(baseView.getContext(),
                        "Exception getting Box ticket! " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
                Logger.LogError("Exception getting Box ticket!", e);
            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void loadLoginWebview(final View baseView, final OpenServer server,
            final Box box, final String ticket, final WebView mLoginWebView)
    {
        String loginUrl = "https://m.box.net/api/1.0/auth/" + ticket;

        mLoginWebView.setVisibility(View.VISIBLE);
        mLoginWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mLoginWebView.getSettings().setJavaScriptEnabled(true);
        mLoginWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(final WebView view, final String url) {
                // Listen for page loads and execute Box.getAuthToken() after
                // each one to see if the user has successfully logged in.
                getAuthToken(baseView, server, box, ticket, 0);
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (url != null && url.startsWith("market://")) {
                    try {
                        view.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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

    private static void getAuthToken(final View baseView, final OpenServer server, final Box box,
            final String ticket, final int tries) {
        if (tries >= 5) {
            return;
        }
        final Handler handler = new Handler();
        box.getAuthToken(ticket, new GetAuthTokenListener() {

            @Override
            public void onComplete(final User user, final String status) {
                if (status.equals("get_auth_token_ok") && user != null) {
                    server.setUser(user.getLogin());
                    server.setName(user.getLogin());
                    server.setPassword(user.getAuthToken());
                    String dao = DAO.toJSON(user);
                    User u2 = DAO.fromJSON(dao, User.class);
                    u2.setAuthToken("");
                    dao = DAO.toJSON(u2);
                    server.setSetting("dao", dao);
                    ViewUtils.setViewsEnabled(baseView, false, R.id.server_logout);
                    ViewUtils.setViewsEnabled(baseView, true, R.id.server_authenticate);
                }
                else if (status.equals("error_unknown_http_response_code")) {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getAuthToken(baseView, server, box, ticket, tries + 1);
                        }
                    }, 500);
                }
            }

            @Override
            public void onIOException(final IOException e) {
            }
        });
    }

    @Override
    public long getTotalSpace() {
        return mUser.getSpaceAmount();
    }

    @Override
    public long getUsedSpace() {
        return mUser.getSpaceUsed();
    }
    
    @Override
    public long getFreeSpace() {
        return getTotalSpace() - getUsedSpace();
    }

    public void setId(long id) {
        mId = id;
    }

    @Override
    public Drawable getOverlayDrawable(Context c, boolean large) {
        return c.getResources().getDrawable(large ? R.drawable.lg_box_overlay : R.drawable.sm_box_overlay);
    }
}

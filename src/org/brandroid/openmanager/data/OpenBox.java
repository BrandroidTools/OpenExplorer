
package org.brandroid.openmanager.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.adapters.OpenPathDbAdapter;
import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.openmanager.util.SortType;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import org.brandroid.utils.Utils;
import com.box.androidlib.Box;
import com.box.androidlib.BoxConfig;
import com.box.androidlib.BoxFile;
import com.box.androidlib.BoxFolder;
import com.box.androidlib.CopyListener;
import com.box.androidlib.DAO;
import com.box.androidlib.DeleteListener;
import com.box.androidlib.DevUtils;
import com.box.androidlib.FileDownloadListener;
import com.box.androidlib.FileUploadListener;
import com.box.androidlib.GetAccountInfoListener;
import com.box.androidlib.GetAccountTreeListener;
import com.box.androidlib.User;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

public class OpenBox extends OpenNetworkPath implements OpenPath.SpaceHandler,
        OpenPath.ThumbnailOverlayInterface, OpenNetworkPath.CloudOpsHandler,
        OpenPath.OpenPathSizable {

    private static final long serialVersionUID = 5742031992345655964L;
    private final Box mBox;
    private final User mUser;
    private final DAO mFile;
    private final OpenBox mParent;
    private Long mId = 0l;

    private List<OpenBox> mChildren = null;

    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
    
    static {
        BoxConfig.getInstance().setEnableHttpLogging(true);
        DevUtils.setLogHandler(Logger.getDefaultHandler());
    }

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
    
    public static boolean isEnabled(Context context) {
        Preferences prefs = new Preferences(context);
        if(!prefs.getBoolean("global", "pref_cloud_box_enabled", true)) return false;
        if(prefs.getString("global", "pref_cloud_box_key", PrivatePreferences.getKey("box_key")).equals("")) return false;
        if(prefs.getString("global", "pref_cloud_box_secret", PrivatePreferences.getKey("box_secret")).equals("")) return false;
        return true;
    }

    @Override
    public void getSpace(final SpaceListener callback) {
        if (mUser != null && mUser.getSpaceAmount() > 0)
        {
            callback.onSpaceReturned(mUser.getSpaceAmount(), mUser.getSpaceUsed(),
                    mUser.getMaxUploadSize());
            return;
        }
        try {
            mBox.getAccountInfo(mUser.getAuthToken(), new GetAccountInfoListener() {
                public void onIOException(IOException e) {
                    callback.onException(e);
                }

                public void onComplete(User user, String status) {
                    if (user != null)
                    {
                        callback.onSpaceReturned(
                                user.getSpaceAmount(),
                                user.getSpaceUsed(),
                                0
                                );
                    }
                }
            });
        } catch (Exception e) {
            postException(e, callback);
        }
    }

    public boolean copyTo(final OpenNetworkPath path,
            final OpenNetworkPath.CloudCompletionListener callback) {
        if (path instanceof OpenBox)
        {
            final OpenBox folder = (OpenBox)path;
            mBox.copy(mUser.getAuthToken(), getBoxType(), getId(), folder.getFolderId(),
                    new CopyListener() {
                        public void onIOException(IOException e) {
                            callback.onException(e);
                        }

                        public void onComplete(String status) {
                            callback.onCloudComplete(status);
                        }
                    });
            return true;
        }
        return false;
    }

    public String getBoxType()
    {
        if (isDirectory())
            return Box.TYPE_FOLDER;
        return Box.TYPE_FILE;
    }

    @Override
    public boolean delete(final CloudDeleteListener callback) {
        mBox.delete(mUser.getAuthToken(), getBoxType(),
                getId(), new DeleteListener() {

                    @Override
                    public void onIOException(IOException e) {
                        callback.onException(e);
                    }

                    @Override
                    public void onComplete(String status) {
                        callback.onDeleteComplete(status);
                    }
                });
        return true;
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
    public boolean listFromDb(SortType sort) {
        if (!OpenPath.AllowDBCache)
            return false;
        String folder = getPath(); // .replace("/" + getName(), "");
        if (!isDirectory())
            folder = folder.replace("/" + getName(), "");
        if (!folder.endsWith("/"))
            folder += "/";
        if (folder.endsWith("//"))
            folder = folder.substring(0, folder.length() - 1);
        Cursor c = mDb.fetchItemsFromFolder(folder, sort);
        if (c == null)
            return false;
        if (c.getCount() == 0) {
            c.close();
            c = mDb.fetchItemsFromFolder(folder.substring(0, folder.length() - 1), sort);
        }
        mChildren.clear();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // String folder =
            // c.getString(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_FOLDER));
            DAO file = new BoxFile();
            boolean f = true;
            String name = c.getString(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_NAME));
            int size = c.getInt(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_SIZE));
            int modified = c.getInt(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_MTIME));
            String path = folder + name;
            int atts = c.getInt(OpenPathDbAdapter.getKeyIndex(OpenPathDbAdapter.KEY_ATTRIBUTES));
            if (path.endsWith("//"))
                path = path.substring(0, path.length() - 1);
            if(name.endsWith("/"))
            {
                BoxFolder f2 = new BoxFolder();
                f2.setFolderName(name);
                f2.setUpdated((long)modified);
                f2.setId((long)atts);
                f2.setFileCount((long)size);
                file = f2;
            } else {
                BoxFile f3 = new BoxFile();
                f3.setFileName(name);
                f3.setSize((long)size);
                f3.setUpdated((long)modified);
                f3.setId((long)atts);
                file = f3;
            }
            OpenBox child = new OpenBox(this, file);
            mChildren.add(child);
            c.moveToNext();
        }
        c.close();
        return true;
    }
    
    @Override
    public int getAttributes() {
        return (int)getId();
    }

    @Override
    public Thread list(final ListListener listener) {
        if (mChildren != null)
            listener.onListReceived(getChildren());
        mChildren = new Vector<OpenBox>();
        if (DEBUG)
            Logger.LogDebug("Box listing for " + getId() + "!");
        return mBox.getAccountTree(getToken(), getId(), null, new GetAccountTreeListener() {

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
    
    public void clearChildren() {
        if(mChildren != null)
            mChildren.clear();
    }

    public void list(final OpenContentUpdateListener callback) throws IOException {
        if (mChildren != null)
        {
            for (OpenPath kid : mChildren)
                callback.addContentPath(kid);
            callback.doneUpdating();
            return;
        }
        mChildren = new Vector<OpenBox>();
        if (DEBUG)
            Logger.LogDebug("Box listing for " + getId() + "!");
        mBox.getAccountTree(getToken(), getId(), new String[0], new GetAccountTreeListener() {

            @Override
            public void onIOException(IOException e) {
                callback.onException(e);
            }

            @Override
            public void onComplete(BoxFolder targetFolder, String status) {
                if (status.equals("not_logged_in"))
                    callback.onException(new Exception(status));
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
            if (getServer() != null)
                return getServer().getName();
            if (mUser != null)
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
        String ret = null;
        if (getFolderId() != 0)
            ret = getParent().getPath();
        else ret = "box://" + Utils.urlencode(mUser.getLogin()) + "@m.box.com/";
        ret += getName();
        if (isDirectory() && !ret.endsWith("/"))
            ret += "/";
        return ret;
    }

    @Override
    public String getAbsolutePath() {
        return "box://" + Utils.urlencode(mUser.getLogin()) + ":" + Utils.urlencode(getToken())
                + "@m.box.com" + "/" + getId();
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
        if (mChildren != null)
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
    public boolean syncUpload(final OpenFile f, final NetworkListener l) {
        if (DEBUG)
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

    public void setId(long id) {
        mId = id;
    }

    @Override
    public Drawable getOverlayDrawable(Context c, boolean large) {
        return c.getResources().getDrawable(
                large ? R.drawable.lg_box_overlay : R.drawable.sm_box_overlay);
    }

    @Override
    public long getTotalSpace() {
        if (mUser != null)
            return mUser.getSpaceAmount();
        return 0;
    }

    @Override
    public long getUsedSpace() {
        if (mUser != null)
            return mUser.getSpaceUsed();
        return 0;
    }

    @Override
    public long getThirdSpace() {
        //if (mUser != null)
        //return mUser.getMaxUploadSize();
        return 0;
    }
    
    @Override
    public Cancellable downloadFromCloud(OpenFile file, final CloudProgressListener callback) {
        return mBox.download(getToken(), getId(), file.getFile(), null, new FileDownloadListener() {
            
            @Override
            public void onIOException(IOException e) {
                callback.onException(e);
            }
            
            @Override
            public void onProgress(long bytesDownloaded) {
                callback.onProgress(bytesDownloaded);
            }
            
            @Override
            public void onComplete(String status) {
                callback.onCloudComplete(status);
            }
        });
    }

    @Override
    public Cancellable uploadToCloud(final OpenFile file, final CloudProgressListener callback) {
        return mBox.upload(getToken(), Box.UPLOAD_ACTION_UPLOAD, file.getFile(), file.getName(),
                getFolderId(), new FileUploadListener() {
            
            @Override
            public void onIOException(IOException e) {
                callback.onException(e);
            }
            
            @Override
            public void onProgress(long bytesTransferredCumulative) {
                callback.onProgress(bytesTransferredCumulative);
            }
            
            @Override
            public void onMalformedURLException(MalformedURLException e) {
                callback.onException(e);
            }
            
            @Override
            public void onFileNotFoundException(FileNotFoundException e) {
                callback.onException(e);
            }
            
            @Override
            public void onComplete(BoxFile boxFile, String status) {
                callback.onCloudComplete(status);
            }
        });
    }

    @Override
    public boolean touch(CloudCompletionListener callback) {
        // TODO Auto-generated method stub
        return false;
    }
}

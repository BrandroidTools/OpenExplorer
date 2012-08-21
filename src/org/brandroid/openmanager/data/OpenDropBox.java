package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

public class OpenDropBox extends OpenNetworkPath
{
	private static final String DB_KEY = "vjbm22k8blhj61g";
	private static final String DB_SECRET = "rxseumehu4gts7r";
	private boolean connected = false;
	private OnAPIResponseListener mListener;
	private final AndroidAuthSession session;
	private final DropboxAPI<AndroidAuthSession> api;
	private String mDir = "/";
	private Entry mEntry = null;
	private OpenDropBox mParent = null;
	private boolean mListed = false;
	private List<OpenDropBox> mChildren = new ArrayList<OpenDropBox>();
	
	public enum RequestType
	{
		request_token, authorize, access_token, account_info,
		files_get, file_put, files_post,
		metadata, delta, revisions, restore, search,
		shares, media, copy_ref, thumbnails,
		copy, create_folder, delete, move
	}
	
	public void setAPIResponseListener(OnAPIResponseListener l) { mListener = l; }
	
	public interface OnAPIResponseListener
	{
		public void OnResponse(RequestType type, Header[] headers, InputStream stream);
		public void OnRequestError(RequestType type, Exception exception, Object... params);
	}
	
	public OpenDropBox()
	{
		session = buildSession();
		api = new DropboxAPI<AndroidAuthSession>(session);
	}
	
	public OpenDropBox(OpenDropBox parent, Entry entry)
	{
		mParent = parent;
		session = mParent.getSession();
		api = mParent.getAPI();
		mDir = entry.path;
		mEntry = entry;
	}
	
	public AndroidAuthSession getSession() { return session; }
	public DropboxAPI<AndroidAuthSession> getAPI() { return api; } 
	
	 /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = Preferences.getPreferences("dropbox");
        String key = prefs.getString("key", null);
        String secret = prefs.getString("secret", null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    
    private SharedPreferences getPreferences()
    {
    	return Preferences.getPreferences("dropbox");
    }

    public AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(DB_KEY, DB_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, AccessType.DROPBOX, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, AccessType.DROPBOX);
        }

        return session;
    }
    
    public void start(Context c)
    {
    	if(!connected)
    		session.startAuthentication(c);
    }
    
	@Override
	public void connect() throws IOException {
		if(!session.authenticationSuccessful())
			throw new IOException("Unable to connect to Dropbox");
		connected = true;
	}

	@Override
	public boolean isConnected() throws IOException {
		return connected;
	}

	@Override
	public OpenNetworkPath[] getChildren() {
		return mChildren.toArray(new OpenDropBox[mChildren.size()]);
	}

	@Override
	public String getPath() {
		String ret = "dropbox://";
		try {
			if(api.accountInfo() != null)
				ret += api.accountInfo().uid + "/";
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret += mDir;
		return ret;
	}

	@Override
	public String getAbsolutePath() {
		return getPath();
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long length() {
		if(mEntry != null)
			return mEntry.bytes; 
		return 0;
	}

	@Override
	public OpenPath getParent() {
		if(mParent != null)
			return mParent;
		return null;
	}

	@Override
	public OpenPath getChild(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpenPath[] list() throws IOException {
		if(mListed) return getChildren();
		return listFiles();
	}

	@Override
	public OpenPath[] listFiles() throws IOException
	{
		try {
			Entry entry = api.metadata(mDir, 0, "", true, null);
			List<Entry> kids = entry.contents;
			mChildren.clear(); 
			for(Entry kid : kids)
				mChildren.add(new OpenDropBox(this, kid));
			mListed = true;
		} catch (DropboxException e) {
			throw new IOException("Unable to list DropBox", e);
		}
		return getChildren();
	}

	@Override
	public Boolean isDirectory() {
		if(mEntry != null)
			return mEntry.isDir; 
		return null;
	}

	@Override
	public Boolean isFile() {
		if(mEntry != null)
			return !mEntry.isDir; 
		return null;
	}

	@Override
	public Boolean isHidden() {
		if(mEntry != null)
			return mEntry.isDeleted;
		return null;
	}

	@Override
	public Uri getUri() {
		return Uri.parse(getPath());
	}

	@Override
	public Long lastModified() {
		if(mEntry != null)
			return Long.parseLong(mEntry.clientMtime); 
		return null;
	}

	@Override
	public Boolean canRead() {
		if(mEntry != null)
			return !mEntry.isDeleted;
		return false;
	}

	@Override
	public Boolean canWrite() {
		if(!super.canWrite()) return false;
		if(mEntry != null)
			return !mEntry.isDeleted;
		return false;
	}

	@Override
	public Boolean canExecute() {
		return false;
	}

	@Override
	public Boolean exists() {
		if(mEntry != null)
			return !mEntry.isDeleted;
		return false;
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
	public boolean syncUpload(OpenFile f, NetworkListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean syncDownload(OpenFile f, NetworkListener l) {
		// TODO Auto-generated method stub
		return false;
	}

}

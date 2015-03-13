package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.Uri;

public class OpenURL extends OpenNetworkPath implements OpenPath.OpenStream, OpenNetworkPath.PipeNeeded {
	
	private String mURL;
	private int responseCode = 0;
	private int responseLength = 0;
	private HttpURLConnection conn = null;
	private boolean mConnected = false;
	
	public OpenURL(String url)
	{
		mURL = url;
	}
	
	public int getResponseCode() throws IOException
	{
		connect();
        responseCode = conn.getResponseCode();
        responseLength = conn.getContentLength();
        return responseCode;
	}


	@Override
	public boolean syncUpload(OpenFile f, NetworkListener l) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		connect();
		return conn.getInputStream();
	}
	
	@Override
	public void connect() throws IOException {
		//disconnect();
		if(!isConnected())
		{
			conn = (HttpURLConnection) new URL(mURL).openConnection();
			conn.setDoInput(true);
			conn.connect();
		}
		mConnected = true;
	}
	
	@Override
	public void disconnect() {
		if(conn != null && mConnected)
			conn.disconnect();
		mConnected = false;
	}
	
	@Override
	public boolean isConnected() throws IOException {
		return conn != null && mConnected;
	}

	@Override
	public boolean syncDownload(OpenFile f, NetworkListener l) {
        try {
        	if(!f.copyFrom(this))
        	{
        		f.delete();
        		l.OnNetworkFailure(this, f, null);
        		return false;
        	}
            l.OnNetworkCopyFinished(this, f);
            return true;
        } catch (Exception e) {
            l.OnNetworkFailure(this, f, e);
        }
        return false;
	}

	@Override
	public OpenNetworkPath[] getChildren() {
		return null;
	}

	@Override
	public String getPath() {
		return mURL;
	}
	
	@Override
	public String getName() {
		return getUri().getLastPathSegment();
	}

	@Override
	public String getAbsolutePath() {
		return mURL;
	}

	@Override
	public long length() {
		return responseLength;
	}

	@Override
	public OpenPath getParent() {
		return new OpenURL(mURL.replace(getUri().getLastPathSegment(), ""));
	}

	@Override
	public OpenPath getChild(String name) {
		return null;
	}

	@Override
	public OpenPath[] list() throws IOException {
		return null;
	}

	@Override
	public OpenPath[] listFiles() throws IOException {
		return null;
	}

	@Override
	public Boolean isDirectory() {
		return mURL.endsWith("/");
	}

	@Override
	public Boolean isFile() {
		return !mURL.endsWith("/");
	}

	@Override
	public Boolean isHidden() {
		return false;
	}

	@Override
	public Uri getUri() {
		return Uri.parse(mURL);
	}

	@Override
	public Long lastModified() {
		return null;
	}

	@Override
	public Boolean canRead() {
		return exists();
	}

	@Override
	public Boolean canExecute() {
		return false;
	}

	@Override
	public Boolean exists() {
		try {
			return getResponseCode() < 400;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

}

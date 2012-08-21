package org.brandroid.openmanager.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.brandroid.openmanager.util.PrivatePreferences;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences.OnPreferenceInteraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.util.Pair;

public class OpenBox extends OpenNetworkPath
{
	private final OnPreferenceInteraction mPrefs;
	private String mTicket, mToken;
	private final OpenAuthCallback mAuthCallback;
	private final static String mApiKey = PrivatePreferences.getBoxAPIKey();
	private List<OpenBox> mChildren;
	private Long mSize, mTotal, mModified, mCreated;
	private String mName, mType, mShareLink;
	private int mBoxID = 0;
	private boolean mShareable = true;
	private OpenBox mParent;
	private boolean mConnected = false;
	
	public OpenBox(OnPreferenceInteraction prefs, OpenAuthCallback onAuth)
	{
		mPrefs = prefs;
		mAuthCallback = onAuth;
	}
	private OpenBox(OpenBox parent, JSONObject child)
	{
		mParent = parent;
		mPrefs = parent.mPrefs;
		mAuthCallback = parent.mAuthCallback;
		mBoxID = child.optInt("id", mBoxID);
		iterateJSON(child);
	}
	public void connect()
	{
		mToken = mPrefs.getSetting("token", mToken);
		if(mToken != null) {
			mConnected = true;
			return;
		}
		mTicket = mPrefs.getSetting("ticket", mTicket);
		if(mTicket == null)
		{
			mTicket = getTicket();
			mPrefs.setSetting("ticket", mTicket);
		}
		Logger.LogDebug("Ticket: " + mTicket);
		if(mTicket == null)
			Logger.LogError("Ticket shouldn't be null");
		mToken = mPrefs.getSetting("token", mToken);
		if(mToken == null && mTicket != null)
			mAuthCallback.OnAuthenticate("https://www.box.com/api/1.0/auth/" + mTicket);
		else if(mToken != null)
			mConnected = true;
	}
	private void iterateJSON(JSONObject json)
	{
		mBoxID = json.optInt("id");
		mName = json.optString("name");
		mType = json.optString("type");
		mSize = json.optLong("size");
		Logger.LogDebug(mBoxID + " :: " + mName + " :: " + mType + " :: " + mSize);
		if(json.has("created_at"))
		{
			try {
				mCreated = new SimpleDateFormat().parse(json.optString("created_at")).getTime();
			} catch (java.text.ParseException e) { }
		}
		if(json.has("modified_at"))
		{
			try {
				mModified = new SimpleDateFormat().parse(json.optString("modified_at")).getTime();
			} catch (java.text.ParseException e) { }
		}
	}
	
	private String getTicket()
	{
		if(mTicket != null) return mTicket;
		String url = "https://www.box.com/api/1.0/rest?action=get_ticket&api_key=" + PrivatePreferences.getBoxAPIKey();
		Logger.LogDebug("Requesting ticket from " + url);
		try {
			Pair<HttpURLConnection, XmlPullParser> xp = getResponseXml(url);
			XmlPullParser xpp = xp.second;
			int type = 0;
			String lname = null;
			while((type = xpp.next()) != XmlPullParser.END_DOCUMENT)
			{
				String name = xpp.getName();
				String value = xpp.getText();
				Logger.LogDebug("XML Event: " + type + ": " + name + "=" + value);
				if(type == XmlPullParser.TEXT && lname != null && lname.equals("ticket"))
					mTicket = xpp.getText();
				if(name != null)
					lname = name;
			}
			xp.first.disconnect();
		} catch (XmlPullParserException e) {
			Logger.LogError("Bad XML on OpenBox.getTicket()", e);
		} catch (IOException e) {
			Logger.LogError("IOException on OpenBox.getTicket()", e);
		} catch(Exception e) {
			Logger.LogError("Unknown Exception on OpenBox.getTicket()", e);
		}
		return mTicket;
	}

	public void OnAuthenticated() {
		String url = "https://www.box.com/api/1.0/rest?action=get_auth_token&api_key=" + mApiKey + "&ticket=" + getTicket();
		try {
			Pair<HttpURLConnection, XmlPullParser> xp = getResponseXml(url);
			XmlPullParser xpp = xp.second;
			int type = 0;
			while((type = xpp.next()) != XmlPullParser.END_DOCUMENT)
			{
				if(type == XmlPullParser.START_TAG)
				{
					if(xpp.getName().equals("auth_token"))
					{
						mToken = xpp.getText();
						mPrefs.setSetting("token", mToken);
					} else if(xpp.getName().equals("space_amount"))
					{
						try {
							mPrefs.setSetting("total", xpp.getText());
							mTotal = Long.parseLong(xpp.getText());
						} catch(NumberFormatException e) { }
					} else if(xpp.getName().equals("space_used"))
					{
						try {
							mPrefs.setSetting("used", xpp.getText());
							mSize = Long.parseLong(xpp.getText());
						} catch(NumberFormatException e) { }
					}
				}
			}
			xp.first.disconnect();
		} catch (XmlPullParserException e) {
			Logger.LogError("Bad XML on OpenBox.OnAuthenticated()", e);
		} catch (IOException e) {
			Logger.LogError("IOException on OpenBox.OnAuthenticated()", e);
		}
	}
	
	private Pair<HttpURLConnection, XmlPullParser> getResponseXml(String url) throws IOException, XmlPullParserException
	{
		XmlPullParser ret = XmlPullParserFactory.newInstance().newPullParser();
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
		conn.connect();
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			ret.setInput(reader);
		} else {
			throw new IOException("Invalid response: " + conn.getResponseMessage());
		}
		return new Pair<HttpURLConnection, XmlPullParser>(conn, ret);
	}
	private String getResponseText(String url) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
		StringBuilder sb = new StringBuilder();
		if(mToken != null)
			conn.addRequestProperty("Authorization", "BoxAuth api_key=" + mApiKey + "&auth_token=" + mToken);
		Logger.LogDebug("Requesting " + url);
		conn.connect();
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
		{
			BufferedReader sr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			char[] buffer = new char[4096];
			int read = 0;
			while((read = sr.read(buffer, 0, 4096)) > 0)
				sb.append(buffer, 0, read);
		} else throw new IOException("Invalid Response Code: " + conn.getResponseCode());
		conn.disconnect();
		return sb.toString();
	}
	private JSONObject getResponseJSON(String url) throws IOException, JSONException
	{
		return new JSONObject(getResponseText(url));
	}

	@Override
	public boolean isConnected() throws IOException {
		return mConnected;
	}
	
	@Override
	public String getName() {
		return mName;
	}

	@Override
	public OpenNetworkPath[] getChildren() {
		if(mChildren != null)
			return mChildren.toArray(new OpenBox[mChildren.size()]);
		else return null;
	}

	@Override
	public String getPath() {
		if(mParent == null)
			return "box:/";
		else return mParent.getPath() + "/" + getName();
	}

	@Override
	public String getAbsolutePath() {
		if(mParent == null)
			return "box://" + mToken;
		else return mParent.getAbsolutePath() + "/" + mBoxID;
	}

	@Override
	public void setPath(String path) {
	}

	@Override
	public long length() {
		return mSize != null ? mSize : 0;
	}

	@Override
	public OpenPath getParent() {
		return mParent;
	}

	@Override
	public OpenPath getChild(String name) {
		if(mChildren != null)
			for(OpenBox kid : mChildren)
				if(kid.getName().equals(name))
					return kid;
		return null;
	}

	@Override
	public OpenPath[] list() throws IOException {
		if(mChildren == null)
			return listFiles();
		return getChildren();
	}

	@Override
	public OpenPath[] listFiles() throws IOException {
		if(!mConnected)
			connect();
		String url = "https://www.box.com/api/2.0/folders/" + mBoxID;
		try {
			JSONObject json = getResponseJSON(url);
			iterateJSON(json);
			mChildren = new ArrayList<OpenBox>();
			if(json.has("items"))
			{
				JSONArray kids = json.optJSONArray("items").optJSONArray(0);
				for(int i = 0; i < kids.length(); i++)
					mChildren.add(new OpenBox(this, kids.optJSONObject(i)));
			} else if(json.has("item_collection")) {
				JSONObject ic = json.optJSONObject("item_collection");
				JSONArray kids = ic.optJSONArray("entries");
				if(kids != null)
					for(int i = 0; i < kids.length(); i++)
						mChildren.add(new OpenBox(this, kids.optJSONObject(i)));
			} else Logger.LogWarning("No children in OpenBox");
		} catch (JSONException e) {
			Logger.LogError("JSONException in OpenBox.listFiles()", e);
		}
		return getChildren();
	}

	@Override
	public Boolean isDirectory() {
		return mType.equals("folder");
	}

	@Override
	public Boolean isFile() {
		return mType.equals("file");
	}

	@Override
	public Boolean isHidden() {
		return mShareable;
	}

	@Override
	public Uri getUri() {
		return null;
	}

	@Override
	public Long lastModified() {
		return mModified;
	}

	@Override
	public Boolean canRead() {
		return true;
	}

	@Override
	public Boolean canWrite() {
		if(!super.canWrite()) return false;
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

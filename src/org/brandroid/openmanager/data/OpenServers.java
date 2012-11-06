
package org.brandroid.openmanager.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.brandroid.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class OpenServers {
    private static final long serialVersionUID = 6279070404986957630L;
    private ArrayList<OpenServer> mData = new ArrayList<OpenServer>();
    public static OpenServers DefaultServers = null;

    public OpenServers() {
        mData = new ArrayList<OpenServer>();
    }

    public OpenServers(JSONArray arr, String decryptPW) {
        this();
        if (arr == null)
            return;
        for (int i = 0; i < arr.length(); i++)
            try {
                add(new OpenServer(arr.getJSONObject(i), decryptPW));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public OpenServer findByPath(String type, String host, String user, String path) {
        for (int i = 0; i < mData.size(); i++) {
            OpenServer server = mData.get(i);
            if (server.getHost().equalsIgnoreCase(host) && server.getType().equalsIgnoreCase(type)
                    && server.getUser().equalsIgnoreCase(user)
                    && server.getPath().replace("/", "").equalsIgnoreCase(path.replace("/", "")))
                return server;
        }
        return null;
    }

    public OpenServer findByUser(String type, String host, String user) {
        for (int i = 0; i < mData.size(); i++) {
            OpenServer server = mData.get(i);
            if (server.getHost().equalsIgnoreCase(host) && server.getType().equalsIgnoreCase(type)
                    && (user == "" || server.getUser().equalsIgnoreCase(user)))
                return server;
        }
        return null;
    }

    public OpenServer findByHost(String type, String host) {
        for (int i = 0; i < mData.size(); i++) {
            OpenServer server = mData.get(i);
            if (server.getType().equalsIgnoreCase(type) && server.getHost().equalsIgnoreCase(host))
                return server;
        }
        return null;
    }

    public boolean add(OpenServer value) {
        if (!value.isValid()) {
            Logger.LogWarning("Invalid Server: " + value);
            return false;
        }
        Logger.LogDebug("Adding Server: " + value);
        return mData.add(value);
    }

    public OpenServer remove(int index) {
        return mData.remove(index);
    }

    public boolean set(int index, OpenServer value) {
        Logger.LogDebug("Setting Server #" + index + ": " + value);
        if (!value.isValid())
            return false;
        if (index >= size())
            return add(value);
        else {
            try {
                mData.set(index, value);
            } catch (RuntimeException e) {
                Logger.LogError("Couldn't add server.", e);
                return false;
            }
            return true;
        }
    }

    public int size() {
        return mData.size();
    }

    public OpenServer get(int index) {
        return mData.get(index);
    }

    public JSONArray getJSONArray() {
        return getJSONArray(false, null);
    }

    public JSONArray getJSONArray(Boolean encryptPW, Context context) {
        JSONArray ret = new JSONArray();
        for (int i = 0; i < mData.size(); i++)
            ret.put(mData.get(i).getJSONObject(encryptPW, context));
        return ret;
    }
}

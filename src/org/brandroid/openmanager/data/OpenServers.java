
package org.brandroid.openmanager.data;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;

public class OpenServers implements Iterable<OpenServer> {
    private static final long serialVersionUID = 6279070404986957630L;
    private List<OpenServer> mData;
    private static OpenServers DefaultServers = new OpenServers();
    private static boolean mDefaultServersSet = false;
    private static String mDecryptKey;
    private final boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && false;

    public OpenServers() {
        mData = new CopyOnWriteArrayList<OpenServer>();
    }

    public OpenServers(JSONArray arr) {
        this();
        if (arr == null)
            return;
        for (int i = 0; i < arr.length(); i++)
            try {
                OpenServer server = new OpenServer(arr.getJSONObject(i));
                server.setServerIndex(i);
                add(server);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        clean();
    }
    
    public void clean() {
        for(OpenServer server : mData)
            server.clean();
    }

    public static boolean hasDefaultServers() {
        return mDefaultServersSet;
    }

    public static OpenServers setDefaultServers(OpenServers servers) {
        mDefaultServersSet = true;
        DefaultServers = servers;
        return servers;
    }

    public static OpenServers getDefaultServers() {
        return DefaultServers;
    }

    public static void setDecryptKey(String key) {
        mDecryptKey = key;
    }

    protected static String getDecryptKey() {
        return mDecryptKey;
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
            if ((host == null || server.getHost().equalsIgnoreCase(host))
                    && server.getType().equalsIgnoreCase(type)
                    && (user == null || "".equals(user)
                    || server.getUser().equalsIgnoreCase(user)))
                return server;
        }
        return null;
    }

    public boolean hasServerType(String type) {
        for (OpenServer server : mData)
            if (server.getType().equalsIgnoreCase(type))
                return true;
        return false;
    }

    public List<OpenServer> findByType(String type) {
        Vector<OpenServer> ret = new Vector<OpenServer>();
        for (OpenServer server : mData)
            if (server.getType().equals(type))
                ret.add(server);
        return ret;
    }

    public OpenServer findByHost(String type, String host) {
        for (int i = 0; i < mData.size(); i++) {
            OpenServer server = mData.get(i);
            if (server.getType().equalsIgnoreCase(type))
            {
            	if(server.getHost().equalsIgnoreCase(host))
            		return server;
            	else Logger.LogWarning("Server type (" + server.getType() + ") but not host (" + server.getHost() + " != " + host + ")");
            }
        }
        return null;
    }

    public boolean add(OpenServer value) {
        if (!value.isValid()) {
            Logger.LogWarning("Invalid Server: " + value);
            return false;
        }
        if (DEBUG)
            Logger.LogDebug("Adding Server: " + value);
        return mData.add(value);
    }

    public OpenServer remove(int index) {
        return mData.remove(index);
    }

    public boolean set(int index, OpenServer value) {
        if (DEBUG)
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

    @Override
    public Iterator<OpenServer> iterator() {
        return mData.iterator();
    }
}

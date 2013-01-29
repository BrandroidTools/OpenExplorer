
package org.brandroid.openmanager.data;

import java.util.Hashtable;
import java.util.Iterator;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.utils.Logger;
import org.brandroid.utils.SimpleCrypto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class OpenServer {
    private final JSONObject mData;

    public OpenServer() {
        mData = new JSONObject();
        // mData = new Hashtable<String, String>();
    }

    public OpenServer(JSONObject obj, String decryptPW) {
        mData = obj;
        if (decryptPW != null && !decryptPW.equals(""))
            try {
                String mPassword = mData.optString("password");
                mPassword = SimpleCrypto.decrypt(decryptPW, mPassword);
                mData.put("password", mPassword);
            } catch (Exception e) {
                Logger.LogError("Error decrypting password.", e);
            }
        /*
         * mData = new Hashtable<String, String>(); if(obj != null) { Iterator
         * keys = obj.keys(); while(keys.hasNext()) { String key =
         * (String)keys.next(); setSetting(key, obj.optString(key,
         * obj.opt(key).toString())); } }
         */
    }

    public OpenServer(String host, String path, String user, String password) {
        mData = new JSONObject();
        // mData = new Hashtable<String, String>();
        setHost(host);
        setPath(path);
        setUser(user);
        setPassword(password);
    }

    public boolean isValid() {
        return mData != null && mData.has("host");
    }

    private static String[] getNames(JSONObject o) {
        JSONArray a = o.names();
        String[] ret = new String[a.length()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = a.optString(i, a.opt(i).toString());
        return ret;
    }

    public JSONObject getJSONObject(Boolean encryptPW, Context context) {
        JSONObject ret = null;
        try {
            ret = new JSONObject(mData, getNames(mData));
        } catch (JSONException e1) {
            return null;
        }
        try {
            if (encryptPW && context != null)
                try {
                    String mPassword = ret.optString("password");
                    ret.put("password", SimpleCrypto.encrypt(
                            SettingsActivity.GetSignatureKey(context), getPassword()));
                } catch (Exception e) {
                    // ret.put("password", getPassword());
                }
            ret.put("dir", getPath());
        } catch (JSONException e) {
        }
        /*
         * for(String s : mData.keySet()) try { ret.put(s, mData.get(s)); }
         * catch (JSONException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */
        return ret;
    }

    public String getType() {
        return mData.optString("type");
    }

    public OpenServer setSetting(String key, String value) {
        try {
            mData.put(key, value);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * if(key.equalsIgnoreCase("name")) mName = value; else
         * if(key.equalsIgnoreCase("host")) mHost = value; else
         * if(key.equalsIgnoreCase("user")) mUser = value; else
         * if(key.equalsIgnoreCase("password")) mPassword = value; else
         * if(key.equalsIgnoreCase("dir")) mPath = value; else
         * if(key.equalsIgnoreCase("type")) mType = value; else
         * if(key.equalsIgnoreCase("port")) mPort = Integer.parseInt(value);
         */
        /*
         * else if(key != null && value != null) { if(mData == null) mData = new
         * Hashtable<String, String>(); mData.put(key, value); }
         */
        return this;
    }

    public String getHost() {
        return mData.optString("host");
    }

    public String getPath() {
        String mPath = mData.optString("dir");
        return mPath + (mPath.equals("") || mPath.endsWith("/") ? "" : "/");
    }

    public String getUser() {
        return mData.optString("user");
    }

    public String getPassword() {
        return mData.optString("password");
    }

    public String getName() {
        String mName = mData.optString("name");
        return mName != null && !mName.equals("") ? mName : getHost();
    }

    public OpenServer setHost(String host) {
        setSetting("host", host);
        return this;
    }

    public OpenServer setPath(String path) {
        setSetting("path", path);
        return this;
    }

    public OpenServer setUser(String user) {
        setSetting("user", user);
        return this;
    }

    public OpenServer setPassword(String password) {
        setSetting("password", password);
        return this;
    }

    public OpenServer setName(String name) {
        setSetting("name", name);
        return this;
    }

    public OpenServer setType(String type) {
        setSetting("type", type);
        return this;
    }

    public OpenServer setPort(int port) {
        setSetting("port", "" + port);
        return this;
    }

    public boolean has(String name) {
        return mData.has(name);
    }

    public String get(String name, String defValue) {
        return mData.optString(name, defValue);
    }

    public String getString(String key) {
        if (key.equals("name"))
            return getName();
        if (key.equals("host"))
            return getHost();
        if (key.equals("dir"))
            return getPath();
        if (key.equals("path"))
            return getPath();
        if (key.equals("user"))
            return getUser();
        if (key.equals("password"))
            return getPassword();
        if (key.equals("type"))
            return getType();
        if (key.equals("port"))
            return "" + getPort();
        return mData.optString(key);
    }

    public static boolean setupServerDialog(final OpenServer server, final int iServersIndex,
            final View parentView) {
        View v = parentView.findViewById(R.id.text_server);
        if (!(v instanceof EditText))
            return false;
        final EditText mHost = (EditText)parentView.findViewById(R.id.text_server);
        final EditText mUser = (EditText)parentView.findViewById(R.id.text_user);
        final EditText mPassword = (EditText)parentView.findViewById(R.id.text_password);
        final EditText mTextPath = (EditText)parentView.findViewById(R.id.text_path);
        final EditText mTextName = (EditText)parentView.findViewById(R.id.text_name);
        final CheckBox mCheckPassword = (CheckBox)parentView.findViewById(R.id.check_password);
        final Spinner mTypeSpinner = (Spinner)parentView.findViewById(R.id.server_type);
        final EditText mTextPort = (EditText)parentView.findViewById(R.id.text_port);
        final CheckBox mCheckPort = (CheckBox)parentView.findViewById(R.id.check_port);
        if (iServersIndex > -1) {
            // mCheckPassword.setVisibility(View.GONE);
            mHost.setText(server.getHost());
            mUser.setText(server.getUser());
            mPassword.setText(server.getPassword());
            if (mTextPath != null)
                mTextPath.setText(server.getPath());
            if (mTextName != null)
                mTextName.setText(server.getName());
            if (server.getPort() > 0) {
                if (mCheckPort != null)
                    mCheckPort.setChecked(false);
                if (mTextPort != null)
                    mTextPort.setText("" + server.getPort());
            } else if (mCheckPort != null)
                mCheckPort.setChecked(true);
            String[] types = mTypeSpinner.getResources()
                    .getStringArray(R.array.server_types_values);
            int pos = 0;
            for (int i = 0; i < types.length; i++)
                if (types[i].equals(server.getType())) {
                    pos = i;
                    break;
                }
            mTypeSpinner.setSelection(pos);
        }

        mHost.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && server.getName().equals(""))
                    mTextName.setText(mHost.getText());
            }
        });
        mTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                String[] types = arg0.getResources().getStringArray(R.array.server_types_values);
                if (position >= types.length || position < 0)
                    return;
                String type = types[position];
                server.setType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (mCheckPassword.getVisibility() == View.VISIBLE)
            mCheckPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        mPassword.setTransformationMethod(new SingleLineTransformationMethod());
                    } else {
                        mPassword.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        mPassword.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            });
        mHost.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                server.setHost(s.toString());
            }
        });
        mUser.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                server.setUser(s.toString());
            }
        });
        if (mPassword != null)
            mPassword.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    server.setPassword(s.toString());
                }
            });
        if (mTextPath != null)
            mTextPath.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    server.setPath(s.toString());
                }
            });
        if (mTextName != null)
            mTextName.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    server.setName(s.toString());
                }
            });
        if (mTextPort != null) {
            mTextPort.setEnabled(!mCheckPort.isChecked());
            mTextPort.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals("") && !s.toString().matches("[^0-9]"))
                        server.setPort(Integer.parseInt(s.toString()));
                }
            });
        }
        if (mCheckPort != null) {
            if (!mCheckPort.isChecked())
                mCheckPort.setText("");
            mCheckPort.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mTextPort.setEnabled(!isChecked);
                    if (!isChecked)
                        server.setPort(-1);
                    else {
                        try {
                            server.setPort(Integer.parseInt(mTextPort.getText().toString()));
                        } catch (Exception e) {
                            Logger.LogWarning("Invalid Port: " + mTextPort.getText().toString());
                        }
                    }
                }
            });
        }
        return true;
    }

    public int getPort() {
        try {
            return Integer.parseInt(mData.optString("port"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        String ret = getType();
        ret += "://";
        if (!getUser().equals("")) {
            ret += getUser();
            if (!getPassword().equals(""))
                ret += ":" + getPassword();
            ret += "@";
        }
        ret += getHost();
        if (getPort() > 0)
            ret += ":" + getPort();
        ret += getPath();
        return ret;
    }
}

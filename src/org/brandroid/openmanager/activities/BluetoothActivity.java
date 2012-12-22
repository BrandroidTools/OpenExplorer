/*
    Open Explorer, an open source file explorer & text editor
    Copyright (C) 2011 Brandon Bowles <brandroid64@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.brandroid.openmanager.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.util.Log;

import java.util.Set;
import java.util.ArrayList;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.R.drawable;
import org.brandroid.openmanager.R.id;
import org.brandroid.openmanager.R.layout;
import org.brandroid.openmanager.data.BookmarkHolder;

public class BluetoothActivity extends Activity implements OnClickListener, OnItemClickListener {
    private static final int REQUEST_ENABLE = 0x0;

    private static final int BUTTON_ENABLE = 0x01;
    private static final int BUTTON_FIND = 0x02;
    private static final int BUTTON_CLEAR = 0x03;
    private static final int DIALOG_UPDATE = 0x04;
    private static final int DIALOG_CANCEL = 0x05;
    private static final int DIALOG_FINISH = 0x06;

    private ArrayList<String> mDeviceData;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDeviceAdapter mDelegate;
    private boolean mUnRegister = false;
    private TextView mMessageView;
    private Button mButton;
    private ProgressDialog mProgress;

    private String[] mFilePaths;

    private Handler handle = new Handler() {

        public void handleMessage(Message msg) {
            int progress = msg.arg1;
            int size = msg.arg2;

            switch (msg.what) {
                case DIALOG_UPDATE:
                    String name = (String)msg.obj;
                    mProgress.setMessage("Sending " + name);
                    mProgress.setMax(size);
                    mProgress.setProgress((mProgress.getProgress() + progress) / size);
                    break;

                case DIALOG_CANCEL:
                    String error = (String)msg.obj;

                    mProgress.cancel();
                    Toast.makeText(BluetoothActivity.this, "Error, " + error, Toast.LENGTH_LONG)
                            .show();
                    break;

                case DIALOG_FINISH:
                    mProgress.cancel();
                    Toast.makeText(BluetoothActivity.this, "Files successfully sent",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    /*
     * String name, is the variable that will contain the name and MAC address
     * of the found bluetooth device. plus the device class in the format
     * (name)\n(MAC):deviceClass. This is so we can easily parse the string to
     * show to the user while having all the necessary information.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String name;

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass btClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);

                name = device.getName() + "\n" + device.getAddress() + ":"
                        + btClass.getDeviceClass();

                if (mDeviceData.get(0).equals("No Devices"))
                    mDeviceData.clear();

                if (!mDeviceData.contains(name)) {
                    mDeviceData.add(name);
                    mMessageView.setText("Found " + mDeviceData.size() + " devices");
                    mDelegate.notifyDataSetChanged();

                } else {
                    mMessageView.setText("Found " + mDeviceData.size() + " devices");
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 13)
            setTheme(android.R.style.Theme_Holo_Dialog_MinWidth);
        else
            setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.bluetooth_layout);

        mFilePaths = getIntent().getExtras().getStringArray("paths");

        mDeviceData = new ArrayList<String>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Bluetooth error").setMessage("This device does not support bluetooth")
                    .setIcon(R.drawable.bluetooth)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }

        mDelegate = new BluetoothDeviceAdapter(this, R.layout.grid_content_layout, mDeviceData);

        GridView gview = (GridView)findViewById(R.id.bt_gridview);
        gview.setAdapter(mDelegate);
        gview.setOnItemClickListener(this);

        mMessageView = (TextView)findViewById(R.id.bt_message);
        mButton = (Button)findViewById(R.id.bt_button);
        mButton.setOnClickListener(this);

        if (!mBluetoothAdapter.isEnabled()) {
            mMessageView.setText("Bluetooth is turned off");
            mButton.setText("Enable Bluetooth");
            mButton.setId(BUTTON_ENABLE);

        } else {
            mMessageView.setText("Bluetooth is turned on");
            mButton.setText("Scan for Devices");
            mButton.setId(BUTTON_FIND);

            findPairedDevices();
        }
    }

    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case BUTTON_ENABLE:
                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enable, REQUEST_ENABLE);
                }
                break;

            case BUTTON_FIND:
                mMessageView.setText("Finding devices, please wait...");
                mUnRegister = true;

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter);
                mBluetoothAdapter.startDiscovery();

                mButton.setText("Clear list");
                mButton.setId(BUTTON_CLEAR);
                break;

            case BUTTON_CLEAR:
                mButton.setText("Scan for Devices");
                mButton.setId(BUTTON_FIND);
                mMessageView.setText("Bluetooth is turned on");

                mDeviceData.clear();
                mDeviceData.add("No Devices");
                mDelegate.notifyDataSetChanged();
                break;
        }
    }

    public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
        final String name = mDeviceData.get(pos);
        String deviceName;

        if (name.equals("No Devices"))
            return;

        mProgress = new ProgressDialog(BluetoothActivity.this);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setMessage("Sending...");

        deviceName = name.substring(0, name.lastIndexOf('\n'));

        // don't discover devices when trying to pair or connect.
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        new AlertDialog.Builder(this)
                .setTitle("Bluetooth Transfer")
                .setMessage(
                        "Would you like to send this " + mFilePaths.length + " file(s) to "
                                + deviceName + "?").setIcon(R.drawable.bluetooth)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mProgress.show();
                        new ClientSocketThread(name, handle).start();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_OK) {
            mMessageView.setText("Bluetooth is turned on.");
            mButton.setText("Scan for Devices");
            mButton.setId(BUTTON_FIND);

            findPairedDevices();

        } else if (requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth was not be turned on.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        if (mUnRegister)
            unregisterReceiver(mReceiver);
    }

    private void findPairedDevices() {
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();

        if (paired.size() > 0) {
            for (BluetoothDevice device : paired)
                mDeviceData.add(device.getName() + "\n" + device.getAddress() + ":"
                        + device.getBluetoothClass().getDeviceClass());

            mMessageView.setText("Found " + paired.size() + " paired devices");

        } else {
            mDeviceData.add("No Devices");
            mMessageView.setText("Found no paired devices");
        }

        mDelegate.notifyDataSetChanged();
    }

    /**
     * @author Joe Berria
     */
    private class ClientSocketThread extends Thread {
        private static final String DEV_UUID = "00001105-0000-1000-8000-00805F9B34FB";
        private BluetoothDevice mRemoteDevice = null;
        private BluetoothSocket mSocket = null;
        private String mMACAddres;
        private Handler mHandle;

        public ClientSocketThread(String device, Handler handle) {
            mMACAddres = device.substring(device.lastIndexOf('\n') + 1, device.lastIndexOf(":"))
                    .toUpperCase();

            mHandle = handle;

            try {
                mRemoteDevice = mBluetoothAdapter.getRemoteDevice(mMACAddres);
                mSocket = mRemoteDevice
                        .createRfcommSocketToServiceRecord(UUID.fromString(DEV_UUID));

            } catch (IllegalArgumentException e) {
                mSocket = null;
                Message msg = new Message();
                msg.obj = e.getMessage();
                mHandle.sendEmptyMessage(DIALOG_CANCEL);

            } catch (IOException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                mHandle.sendEmptyMessage(DIALOG_CANCEL);
            }
        }

        public void run() {
            // make sure we are not discovering
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();

            try {
                if (mSocket != null)
                    mSocket.connect();
                else
                    return;

                communicateData(mSocket);

            } catch (IOException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                msg.what = DIALOG_CANCEL;
                mHandle.sendMessage(msg);

                try {
                    mSocket.close();
                } catch (IOException ee) {
                }

                return;
            }
        }

        private void communicateData(BluetoothSocket socket) {
            OutputStream writeStrm = null;
            FileInputStream readStrm = null;
            int len = mFilePaths.length;
            byte[] buffer = new byte[1024];

            Log.e("ClientSocketThread", "communicateData is called");

            try {
                for (int i = 0; i < len; i++) {
                    File file = new File(mFilePaths[i]);
                    writeStrm = socket.getOutputStream();
                    readStrm = new FileInputStream(file);
                    int read = 0;

                    Message msg = new Message();
                    msg.obj = (String)file.getName();
                    msg.arg2 = (int)file.length();

                    while ((read = readStrm.read(buffer)) != -1) {
                        msg.arg1 = read;
                        msg.what = DIALOG_UPDATE;

                        writeStrm.write(buffer);
                        mHandle.sendMessage(msg);
                    }

                    Log.e("OPENMANAGER", "file is done");
                }

                writeStrm.close();
                cancel();

            } catch (IOException e) {
                Message msg = mHandle.obtainMessage();
                msg.obj = e.getMessage();
                msg.what = DIALOG_CANCEL;
                mHandle.sendMessage(msg);

            }
        }

        private void cancel() {
            try {
                if (mSocket != null)
                    mSocket.close();

                mHandle.sendEmptyMessage(DIALOG_FINISH);

            } catch (IOException e) {
            }
        }
    }

    /**
     * @author Joe Berria
     */
    private class ServerSocketThread extends Thread {

        public ServerSocketThread() {

        }

        public void run() {

        }
    }

    /**
     * @author Joe Berria
     */
    private class BluetoothDeviceAdapter extends ArrayAdapter<String> {
        BookmarkHolder mHolder;

        public BluetoothDeviceAdapter(Context context, int res, ArrayList<String> data) {
            super(context, res, data);
        }

        public View getView(int position, View view, ViewGroup parent) {
            String name = mDeviceData.get(position);
            int classType;

            if (view == null) {
                LayoutInflater in = (LayoutInflater)BluetoothActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                view = in.inflate(R.layout.grid_content_layout, parent, false);

                mHolder = new BookmarkHolder(name, view, R.layout.grid_content_layout);

                view.setTag(mHolder);

            } else {
                mHolder = (BookmarkHolder)view.getTag();
            }

            if (!name.equals("No Devices")) {
                classType = Integer
                        .valueOf(name.substring(name.lastIndexOf(":") + 1, name.length()));

                // if((classType & 0x200) > 0)
                // mHolder.setIconResource(R.drawable.cellphone);
                // if((classType & 0x100) > 0)
                // mHolder.setIconResource(R.drawable.computer);

                mHolder.setTitle(name.substring(0, name.lastIndexOf(":")));

            } else {
                // mHolder.setIconResource(R.drawable.computer);
                mHolder.setTitle(name);
            }

            // mHolder.mMainText.setMaxLines(5);

            return view;
        }
    }
}

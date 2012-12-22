/*
    Open Explorer, an open source file explorer & text editor
    Copyright (C) 2011, 2012 Brandon Bowles <brandroid64@gmail.com>

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

package org.brandroid.openmanager.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.utils.ByteQueue;
import org.brandroid.utils.Logger;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RootManager {
    public interface UpdateCallback {
        void onUpdate();

        /**
         * Main Callback Function for RootManager.
         * 
         * @param msg
         * @return Return true to signal to RootManager to stop waiting for
         *         Process. False if more is expected.
         */
        boolean onReceiveMessage(String msg);

        void onExit();
    }

    private static boolean rootRequested = false;
    private static boolean rootEnabled = false;
    private static final boolean singleProcess = true;
    private static String busybox = null;
    private Process myProcess = null;
    private UpdateCallback mNotify = null;
    private DataInputStream is;
    private DataOutputStream os;
    private Thread mPollingThread;
    private ByteQueue mByteQueue;
    private byte[] mReceiveBuffer;
    private String mLastWrite = null;
    private final static int BufferLength = 128 * 1024;
    private int mPending = 0;

    private static final int NEW_INPUT = 1;

    public static final RootManager Default;

    static {
        Default = new RootManager();
    }

    private boolean mIsRunning = false;
    private Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mIsRunning) {
                Logger.LogWarning("Handler exited");
                return;
            }
            if (msg.what == NEW_INPUT) {
                readFromProcess();
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        exitRoot();
        try {
            if (os != null)
                os.close();
        } catch (Exception e) {
        }
        try {
            if (is != null)
                is.close();
        } catch (Exception e) {
        }
        super.finalize();
    }

    public void onUpdate() {
        mLastWrite = null;
        if (mNotify != null)
            mNotify.onUpdate();
    }

    public void onReceiveMessage(String msg) {
        if (mNotify != null)
            mNotify.onReceiveMessage(msg);
    }

    public RootManager setUpdateCallback(RootManager.UpdateCallback notify) {
        if (notify == null)
            mLastWrite = null;
        mNotify = notify;
        return this;
    }

    public RootManager() {

        mReceiveBuffer = new byte[BufferLength];
        mByteQueue = new ByteQueue(BufferLength);

        mPollingThread = new Thread() {
            private byte[] mBuffer = new byte[BufferLength];

            @Override
            public void run() {
                try {
                    while (true) {
                        int read = is.read(mBuffer);
                        if (read == -1) {
                            // EOF -- process exited
                            onExit();
                            return;
                        }
                        mByteQueue.write(mBuffer, 0, read);
                        mMsgHandler.sendMessage(mMsgHandler.obtainMessage(NEW_INPUT));
                    }
                } catch (IOException e) {
                    Logger.LogError("Polling couldn't write", e);
                } catch (InterruptedException e) {
                    Logger.LogError("Polling interrupted", e);
                }
            }
        };
    }

    /**
     * Mount /system as Read-Write or Read-Only. Not thoroughly tested. Use at
     * your own risk.
     * 
     * @return True if operation appears to be successful, False if an error is
     *         returned.
     */
    public static boolean mountSystem(boolean rw) {
        if (!rootEnabled)
            return false;
        String sysDev = "/dev/block/mmcblk1p23";
        OpenFile f = new OpenFile("/proc/mounts");
        if(f == null) return false;
        String data = f.readAscii();
        if(data == null) return false;
        for (String s : data.split("\n")) {
            if (s.indexOf("/system") > -1) {
                sysDev = s.substring(0, s.indexOf(" "));
                break;
            }
        }

        try {
            if (RootTools.sendShell(
                    "mount -o " + (rw ? "rw" : "ro") + ",remount -t yaffs2 " + sysDev + " /system",
                    1000).size() > 0)
                return false;
        } catch (Exception e) {
            Logger.LogError("Unable to mount system", e);
            return false;
        }
        return true;
    }

    public static boolean isSystemMounted() {
        OpenFile f = new OpenFile("/proc/mounts");
        if(f == null) return false;
        String data = f.readAscii();
        if(data == null) return false;
        for (String s : data.split("\n")) {
            if (s.indexOf("/system") > -1)
                return s.indexOf("rw,") > -1;
        }
        return false;
    }

    public String getDriveLabel(String path, String sDefault) {
        if (!rootEnabled)
            return sDefault;
        HashSet<String> blkid = execute("blkid `cat /proc/mounts | grep \""
                + sDefault.replace("\"", "\\\"") + "\"`");
        for (String line : blkid) {
            int i = line.toLowerCase().indexOf("LABEL=");
            if (i > -1) {
                line = line.substring(i + 6);
                if (line.startsWith("\""))
                    line = line.substring(1, line.indexOf("\"", 1));
                else
                    line = line.substring(0, line.indexOf(" "));
                return line;
            }
        }
        return sDefault;
    }

    /**
     * Look for new input from the ptty, send it to the terminal emulator.
     */
    private void readFromProcess() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        mReceiveBuffer = new byte[bytesAvailable];
        int bytesToRead = bytesAvailable; // Math.min(bytesAvailable,
                                          // mReceiveBuffer.length);
        try {
            int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
            if (bytesRead == 0 || mReceiveBuffer[0] == 0) {
                onUpdate();
                return;
            } else {
                String tmp = new String(mReceiveBuffer);
                if (tmp.startsWith("\n"))
                    tmp = tmp.substring(1);
                onReceiveMessage(tmp);
            }
            if (mReceiveBuffer[mReceiveBuffer.length - 1] == 0)
                onUpdate();
            // mEmulator.append(mReceiveBuffer, 0, bytesRead);
        } catch (InterruptedException e) {
            Logger.LogError("readFromProcess interrupted?", e);
        }
    }

    public Process getSuProcess() throws IOException {
        if (rootRequested && !rootEnabled)
            return null;
        if (myProcess == null || !singleProcess) {
            myProcess = new ProcessBuilder().command("su", "-c sh").redirectErrorStream(true)
                    .start();
            is = new DataInputStream(myProcess.getInputStream());
            os = new DataOutputStream(myProcess.getOutputStream());
            // myProcess = Runtime.getRuntime().exec("su -c sh");
        }
        return myProcess;
    }

    public String getBusyBox() {
        final boolean[] waiting = new boolean[] {
            true
        };
        if (busybox == null) {
            write("which busybox", new UpdateCallback() {
                public void onUpdate() {
                    Logger.LogDebug("WHICH update");
                    waiting[0] = false;
                }

                public boolean onReceiveMessage(String msg) {
                    Logger.LogDebug("WHICH receive " + msg);
                    if (msg.startsWith("/"))
                        busybox = msg + " ";
                    else
                        busybox = "";
                    waiting[0] = false;
                    return true;
                }

                public void onExit() {
                    Logger.LogDebug("WHICH exit");
                    waiting[0] = false;
                }
            });
        }
        while (waiting[0]) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (busybox == null)
            busybox = "";
        else if (!busybox.startsWith("/"))
            busybox = "";
        if (busybox.indexOf("\n") > -1)
            busybox = busybox.substring(0, busybox.indexOf("\n"));

        return busybox;
    }

    public boolean isRoot() {
        return rootEnabled;
    }

    public boolean isRootRequested() {
        return rootRequested;
    }

    @SuppressWarnings("unused")
    public void exitRoot() {
        rootEnabled = false;
        rootRequested = false;
        if (myProcess != null) // exit su
        {
            try {
                if (os == null)
                    os = new DataOutputStream(myProcess.getOutputStream());
                os.writeBytes("exit\n");
                os.flush();
                os.close();
                myProcess.destroy();
            } catch (Exception e) {
            }
        }
    }

    private void stop() {
        if (!mIsRunning)
            return;
        mIsRunning = false;
        if (mPollingThread != null && mPollingThread.getState() == State.WAITING)
            mPollingThread.stop();
    }

    private void start() {
        if (mIsRunning)
            return;
        mIsRunning = true;
        if (mPollingThread.getState() == State.NEW)
            mPollingThread.start();
    }

    public void write(final String cmd, final UpdateCallback callback) {
        Logger.LogDebug("RootManager.write(" + cmd + ", " + callback + ")");
        if (mLastWrite != null && cmd.equals(mLastWrite)) {
            setUpdateCallback(callback);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (mLastWrite != null)
                        Logger.LogDebug("Waiting for last write (" + mLastWrite + ") to write ("
                                + cmd + ")");
                    try {
                        while (mLastWrite != null) {
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                    }
                    mLastWrite = cmd;
                    if (getSuProcess() == null) // No root
                        return;

                    start();

                    if (os == null)
                        os = new DataOutputStream(myProcess.getOutputStream());

                    setUpdateCallback(callback);
                    Logger.LogDebug("Writing to process: " + cmd);
                    os.writeBytes(cmd + "\n");
                    os.flush();
                } catch (IOException e) {
                    Logger.LogError("Error writing to Root output stream.", e);
                }
            }
        }).start();
    }

    public HashSet<String> execute(String cmd) {
        HashSet<String> ret = new HashSet<String>();
        if (!isRoot()) {
            Logger.LogWarning("Root execute without request?");
            return ret;
        }
        try {
            Process suProcess = getSuProcess();

            if (os == null)
                os = new DataOutputStream(suProcess.getOutputStream());
            if (is == null)
                is = new DataInputStream(suProcess.getInputStream());

            start();

            if (null != os && null != is) {
                // Logger.LogDebug("Writing " + commands.length + " commands.");
                Logger.LogDebug("RootManager.execute(" + cmd + ")");
                os.writeBytes(cmd + "\n");
                os.flush();

                // app crash if this doesn't happen
                os.writeBytes("exit\n");
                os.flush();

                int retVal = suProcess.waitFor();
                Logger.LogDebug("Root return value: " + retVal);

                String line = null;
                if ((line = is.readLine()) == null) // skip 1st newline
                    while ((line = is.readLine()) != null) {
                        ret.add(line);
                        onReceiveMessage(line);
                        Logger.LogDebug("Root return line: " + line);
                        if (ret.size() > 500)
                            break;
                    }

                Logger.LogDebug("Root done reading");
            } else {
                Logger.LogWarning("One of the streams was null.");
                return null;
            }
        } catch (Exception e) {
            Logger.LogError("Error executing commands [" + cmd + "]", e);
        } finally {
        }
        return ret;
    }

    @SuppressWarnings("deprecation")
    public boolean requestRoot() {
        if (rootRequested)
            return rootEnabled;
        try {
            Process suProcess = myProcess != null ? myProcess : Runtime.getRuntime().exec("su");

            if (os == null)
                os = new DataOutputStream(suProcess.getOutputStream());
            if (is == null)
                is = new DataInputStream(suProcess.getInputStream());

            // start();

            if (null != os && null != is) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = is.readLine();
                boolean exitSu = false;
                if (null == currUid) {
                    rootEnabled = false;
                    Logger.LogDebug("Can't get root access or denied by user");
                } else if (true == currUid.contains("uid=0")) {
                    rootEnabled = true;
                    exitSu = true;
                    Logger.LogDebug("Root access granted");
                } else {
                    rootEnabled = false;
                    exitSu = true;
                    Logger.LogDebug("Root access rejected: " + currUid);
                }

                if (!rootEnabled) {
                    // mPollingThread.stop();
                }
                if (exitSu) {
                    // os.writeBytes("exit\n");
                    // os.flush();
                }
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output
            // stream (os) after su failed, meaning that the device is not
            // rooted

            rootEnabled = false;
            Logger.LogError("Root access rejected [" + e.getClass().getName() + "]", e);
        }

        rootRequested = true;

        return rootEnabled;
    }

    public static boolean tryExecute(String... commands) {
        boolean retval = false;

        try {
            // ArrayList<String> commands = getCommandsToExecute();
            if (null != commands && commands.length > 0) {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                // os.writeBytes("exit\n");
                // os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        retval = true;
                    } else {
                        // Root access denied
                        retval = false;
                    }
                } catch (Exception ex) {
                    Log.e("OpenManager", "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w("OpenManager", "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w("OpenManager", "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w("OpenManager", "Error executing internal operation", ex);
        }

        return retval;
    }

    public void onExit() {
        mIsRunning = false;
        if (mNotify != null)
            mNotify.onExit();
    }

    public static boolean hasBusybox() {
        return false;
    }
}

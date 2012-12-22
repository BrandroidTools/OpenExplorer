
package org.brandroid.openmanager.util;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;
import org.brandroid.utils.ByteQueue;
import org.brandroid.utils.Logger;

import android.R.layout;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ShellSession {
    private RootManager.UpdateCallback mNotify;

    private int mProcId;
    private FileDescriptor mFd;
    private FileInputStream mInput;
    private FileOutputStream mOutput;
    private Thread mWatcherThread;
    private Thread mPollingThread;
    private ByteQueue mByteQueue;
    private byte[] mReceiveBuffer;

    private CharBuffer mWriteCharBuffer;
    private ByteBuffer mWriteByteBuffer;
    private CharsetEncoder mUTF8Encoder;

    private String mProcessExitMessage;

    private static final int NEW_INPUT = 1;
    private static final int PROCESS_EXITED = 2;

    /**
     * Callback to be invoked when a TermSession finishes.
     */
    public interface FinishCallback {
        void onSessionFinish(ShellSession session);
    }

    private FinishCallback mFinishCallback;

    private boolean mIsRunning = false;
    private Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mIsRunning) {
                return;
            }
            if (msg.what == NEW_INPUT) {
                readFromProcess();
            } else if (msg.what == PROCESS_EXITED) {
                onProcessExit((Integer)msg.obj);
            }
        }
    };

    public void setUpdateCallback(RootManager.UpdateCallback listener) {
        mNotify = listener;
    }

    public ShellSession() {
        int[] processId = new int[1];
        mWatcherThread = new Thread() {
            @Override
            public void run() {
                Logger.LogVerbose("waiting for: " + mProcId);
                int result = 0; // Exec.waitFor(mProcId);
                Logger.LogVerbose("Subprocess exited: " + result);
                mMsgHandler.sendMessage(mMsgHandler.obtainMessage(PROCESS_EXITED, result));
            }
        };
        mWatcherThread.setName("Process watcher");

        mWriteCharBuffer = CharBuffer.allocate(2);
        mWriteByteBuffer = ByteBuffer.allocate(4);
        mUTF8Encoder = Charset.forName("UTF-8").newEncoder();
        mUTF8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
        mUTF8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        mReceiveBuffer = new byte[4 * 1024];
        mByteQueue = new ByteQueue(4 * 1024);

        mPollingThread = new Thread() {
            private byte[] mBuffer = new byte[4096];

            @Override
            public void run() {
                try {
                    while (true) {
                        int read = mInput.read(mBuffer);
                        if (read == -1) {
                            // EOF -- process exited
                            return;
                        }
                        mByteQueue.write(mBuffer, 0, read);
                        mMsgHandler.sendMessage(mMsgHandler.obtainMessage(NEW_INPUT));
                    }
                } catch (IOException e) {
                } catch (InterruptedException e) {
                }
            }
        };
        mPollingThread.setName("Input reader");
    }

    public void setProcessExitMessage(String message) {
        mProcessExitMessage = message;
    }

    private void onProcessExit(int result) {
        /*
         * if (mSettings.closeWindowOnProcessExit()) { if (mFinishCallback !=
         * null) { mFinishCallback.onSessionFinish(this); } finish(); } else if
         * (mProcessExitMessage != null) {
         */
        // try {
        String sMsg = "\r\n[" + mProcessExitMessage + "]";
        // mEmulator.append(msg, 0, msg.length);
        // mNotify.onUpdate();
        if (mNotify != null)
            mNotify.onReceiveMessage(sMsg);
        // } catch (UnsupportedEncodingException e) {
        // Never happens
        // }
        // }
    }

    public void start() {
        mIsRunning = true;
        mPollingThread.start();
    }

    public void finish() {
        // Exec.hangupProcessGroup(mProcId);
        // Exec.close(mFd);
        mIsRunning = false;
        // mTranscriptScreen.finish();
    }

    /**
     * Look for new input from the ptty, send it to the terminal emulator.
     */
    private void readFromProcess() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
        try {
            int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
            if (mNotify != null)
                mNotify.onReceiveMessage(new String(mReceiveBuffer));
            // mEmulator.append(mReceiveBuffer, 0, bytesRead);
        } catch (InterruptedException e) {
        }

        if (mNotify != null) {
            mNotify.onUpdate();
        }
    }

    private void createSubprocess(int[] processId) {
        String shell = "/system/bin/sh -";
        ArrayList<String> argList = parse(shell);
        String arg0 = argList.get(0);
        String[] args = argList.toArray(new String[1]);

        String termType = "SCREEN";
        String[] env = new String[1];
        env[0] = "TERM=" + termType;

        // Environment
        // mFd = Exec.createSubprocess(arg0, args, env, processId);
    }

    private ArrayList<String> parse(String cmd) {
        final int PLAIN = 0;
        final int WHITESPACE = 1;
        final int INQUOTE = 2;
        int state = WHITESPACE;
        ArrayList<String> result = new ArrayList<String>();
        int cmdLen = cmd.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cmdLen; i++) {
            char c = cmd.charAt(i);
            if (state == PLAIN) {
                if (Character.isWhitespace(c)) {
                    result.add(builder.toString());
                    builder.delete(0, builder.length());
                    state = WHITESPACE;
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    builder.append(c);
                }
            } else if (state == WHITESPACE) {
                if (Character.isWhitespace(c)) {
                    // do nothing
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    state = PLAIN;
                    builder.append(c);
                }
            } else if (state == INQUOTE) {
                if (c == '\\') {
                    if (i + 1 < cmdLen) {
                        i += 1;
                        builder.append(cmd.charAt(i));
                    }
                } else if (c == '"') {
                    state = PLAIN;
                } else {
                    builder.append(c);
                }
            }
        }
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        return result;
    }

    public void write(String data) {
        try {
            mOutput.write(data.getBytes("UTF-8"));
            mOutput.flush();
        } catch (IOException e) {
            // Ignore exception
            // We don't really care if the receiver isn't listening.
            // We just make a best effort to answer the query.
        }
    }

}

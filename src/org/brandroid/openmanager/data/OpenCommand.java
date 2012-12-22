
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

import org.brandroid.openmanager.util.ThumbnailCreator;

import android.graphics.Bitmap;
import android.net.Uri;

public class OpenCommand extends OpenPath {
    public static final int COMMAND_ADD_SERVER = 0;

    private final String mText;
    private final int mCommand;
    private int mDrawableId = 0;

    public OpenCommand(String text, int command) {
        this(text, command, 0);
    }

    public OpenCommand(String text, int command, int drawableId) {
        mText = text;
        mCommand = command;
        mDrawableId = drawableId;
    }

    public int getCommand() {
        return mCommand;
    }

    @Override
    public String getName() {
        return mText;
    }

    @Override
    public String getPath() {
        return mText;
    }

    @Override
    public String getAbsolutePath() {
        return mText;
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public OpenPath getParent() {
        return null;
    }

    @Override
    public OpenPath getChild(String name) {
        return null;
    }

    @Override
    public OpenPath[] list() throws IOException {
        return new OpenPath[0];
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        return new OpenPath[0];
    }

    @Override
    public Boolean isDirectory() {
        return false;
    }

    @Override
    public Boolean isFile() {
        return false;
    }

    @Override
    public Boolean isHidden() {
        return false;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public Long lastModified() {
        return null;
    }

    @Override
    public Boolean canRead() {
        return false;
    }

    @Override
    public Boolean canWrite() {
        return false;
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
    public Boolean requiresThread() {
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
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public void setDrawableId(int id) {
        mDrawableId = id;
    }

    public int getDrawableId() {
        return mDrawableId;
    }

}

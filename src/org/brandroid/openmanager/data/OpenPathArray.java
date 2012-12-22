
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;

public class OpenPathArray extends OpenPath {
    private OpenPath[] children;

    public OpenPathArray(OpenPath[] kids) {
        children = kids;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAbsolutePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPath(String path) {
        // TODO Auto-generated method stub

    }

    @Override
    public long length() {
        return children.length;
    }

    @Override
    public OpenPath getParent() {
        return null;
    }

    @Override
    public OpenPath getChild(String name) {
        for (OpenPath kid : children)
            if (kid.getName().equals(name))
                return kid;
        return null;
    }

    @Override
    public OpenPath[] list() throws IOException {
        return children;
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        return children;
    }

    @Override
    public Boolean isDirectory() {
        return true;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long lastModified() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean canRead() {
        return true;
    }

    @Override
    public Boolean canWrite() {
        return false;
    }

    @Override
    public Boolean canExecute() {
        return true;
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

}

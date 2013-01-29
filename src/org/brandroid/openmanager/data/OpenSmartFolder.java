
package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.brandroid.utils.Logger;

import android.net.Uri;
import android.os.Environment;

public class OpenSmartFolder extends OpenPath {
    private static final long serialVersionUID = 2559289318412235438L;
    private String mName = null;
    private final ArrayList<SmartSearch> mSearches;
    private final ArrayList<OpenPath> mChildren;
    private boolean mLoaded = false;

    public OpenSmartFolder(String name) {
        mName = name;
        mChildren = new ArrayList<OpenPath>();
        mSearches = new ArrayList<OpenSmartFolder.SmartSearch>();
    }

    public static class SmartSearch implements Comparable<SmartSearch> {
        private final OpenPath mParent;
        private final SearchType mType;
        private final Object[] mParams;

        public enum SearchType {
            All, AllRecursive, NameEquals, NameNotEquals, SizeUnder, SizeAbove, DateBefore, DateAfter, TypeIn
        }

        public SmartSearch(OpenPath parent) {
            mParent = parent;
            mType = SearchType.All;
            mParams = null;
        }

        public SmartSearch(OpenPath parent, SearchType type, Object... params) {
            mParent = parent;
            mType = type;
            mParams = params;
        }

        @Override
        public int compareTo(SmartSearch another) {
            return another.mParent.compareTo(mParent);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SmartSearch)
                return ((SmartSearch)o).mParent.equals(mParent);
            return super.equals(o);
        }

    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void addSearch(SmartSearch search) {
        if (search.mParent == null || !search.mParent.exists())
            return;
        if (!mSearches.contains(search)) {
            mLoaded = false;
            mSearches.add(search);
            try {
                doSearch(search);
            } catch (IOException e) {
                Logger.LogError("Error adding Search");
            }
            mLoaded = true;
        }
    }

    @Override
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getPath() {
        return mName;
    }

    @Override
    public String getAbsolutePath() {
        if (getFirstDir() != null)
            return getFirstDir().getAbsolutePath();
        else
            return mName;
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public long length() {
        return mChildren.size();
    }

    @Override
    public int getListLength() {
        return (int)length();
    }

    @Override
    public OpenPath getParent() {
        return null;
    }

    @Override
    public boolean showChildPath() {
        return true;
    }

    @Override
    public OpenPath getChild(String name) {
        for (OpenPath f : mChildren)
            if (f.getName().equals(name))
                return f;
        return getFirstDir().getChild(name);
    }

    @Override
    public OpenPath[] list() throws IOException {
        if (mChildren.size() > 0)
            return mChildren.toArray(new OpenPath[mChildren.size()]);
        else
            return listFiles();
    }

    private void doSearch(SmartSearch search) throws IOException {
        switch (search.mType) {
            case All:
                for (OpenPath p : search.mParent.listFiles())
                    if (!p.isDirectory() || p.getChildCount(false) > 0)
                        if (!mChildren.contains(p))
                            mChildren.add(p);
                break;
            case TypeIn:
                ArrayList<String> types = new ArrayList<String>();
                for (Object o : search.mParams)
                    if (o instanceof String && ((String)o).length() <= 8)
                        types.add(((String)o).toLowerCase());
                for (OpenPath kid : search.mParent.listFiles())
                    if (types.contains(kid.getExtension().toLowerCase()))
                        if (!mChildren.contains(kid))
                            mChildren.add(kid);
                break;
            default:
                throw new IOException("Type " + search.mType.toString() + " not yet implemented");
        }
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        mLoaded = false;
        mChildren.clear();
        for (SmartSearch search : mSearches)
            doSearch(search);
        mLoaded = true;
        return mChildren.toArray(new OpenPath[mChildren.size()]);
    }

    public OpenPath getFirstDir() {
        for (SmartSearch s : mSearches)
            if (s.mParent instanceof OpenFile)
                return s.mParent;
        if (mSearches.size() > 0)
            return mSearches.get(0).mParent;
        else
            return new OpenFile(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
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
        return getFirstDir().getUri();
    }

    @Override
    public Long lastModified() {
        return getFirstDir().lastModified();
    }

    @Override
    public Boolean canRead() {
        return true;
    }

    @Override
    public Boolean canWrite() {
        return getFirstDir().canWrite();
    }

    @Override
    public Boolean canExecute() {
        return getFirstDir().canExecute();
    }

    @Override
    public Boolean exists() {
        return getFirstDir().exists();
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
        return getFirstDir().mkdir();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getFirstDir().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getFirstDir().getOutputStream();
    }

    @Override
    public void clearChildren() {
        mChildren.clear();
    }
}

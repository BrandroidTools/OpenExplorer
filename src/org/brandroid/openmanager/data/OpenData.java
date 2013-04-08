package org.brandroid.openmanager.data;

import java.lang.ref.WeakReference;

import org.brandroid.openmanager.util.FileManager;

public class OpenData {
    private String mName;
    private String mFullPath;
    private Long mSize;
    private Long mDate;
    private WeakReference<OpenPath> mPath;
    
    public String getName()
    {
        return mName;
    }
    
    public long lastModified()
    {
        if(mDate != null)
            return mDate;
        return 0l;
    }
    
    public long length()
    {
        if(mSize != null)
            return mSize;
        return 0l;
    }
    
    public OpenPath getPath() {
        if(mPath != null && mPath.get() != null)
            return mPath.get();
        return FileManager.getOpenCache(mFullPath);
    }
}

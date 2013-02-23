
package org.brandroid.openmanager.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brandroid.openmanager.data.OpenPath.OpenPathByteIO;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.utils.Logger;
import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PatternMatcher;

public class OpenFileRoot extends OpenPath implements OpenPath.OpenPathUpdateListener,
        OpenPath.NeedsTempFile, OpenPath.OpenPathCopyable, OpenPathByteIO {

    private static final long serialVersionUID = -1540464774342269126L;
    private String mPath;
    private String mName;
    private String mPerms;
    private String mSym;
    private Long mDate = null;
    private Long mSize = null;
    private WeakReference<List<OpenPath>> mChildren = null;
    private boolean mLoaded = false;

    public OpenFileRoot(OpenPath src) {
        mPath = src.getParent().getPath();
        if (!mPath.endsWith("/"))
            mPath += "/";
        mName = src.getName();
        if (src.isDirectory() && !mName.endsWith("/"))
            mName += "/";
        mDate = src.lastModified();
        mSize = src.length();
    }

    public OpenFileRoot(String parent, String listing) {
        mPath = parent;
        if (!mPath.endsWith("/"))
            mPath += "/";
        PatternMatcher pmLong = new PatternMatcher(" [1-2][0-9][0-9][0-9]\\-[0-9]+\\-[0-9]+ ",
                PatternMatcher.PATTERN_SIMPLE_GLOB);
        boolean bLong = pmLong.match(listing);
        Pattern p = Pattern.compile("[0-9][0-9]\\:[0-9][0-9] "
                + (bLong ? "[1-2][0-9][0-9][0-9] " : ""));
        Matcher m = p.matcher(listing);
        boolean success = false;
        if (m.matches()) {
            mName = listing.substring(m.end());
            try {
                String sDate = listing.substring(m.start(), m.end() - 1).trim();
                mDate = Date.parse(sDate);
                mSize = Long.parseLong(listing.substring(listing.lastIndexOf(" ", m.start()),
                        m.start() - 1).trim());
                success = true;
            } catch (Exception e) {
                Logger.LogError("Couldn't parse date.", e);
            }
            mPerms = listing.split(" ")[0];
        }
        if (!success) {
            String[] parts = listing.split(" +");
            if (parts.length > 5) {
                mPerms = parts[0];
                int i = 4;
                try {
                    if (parts.length >= 7)
                        mSize = Long.parseLong(parts[i++]);
                } catch (NumberFormatException e) {
                }
                try {
                    if (parts[i + 1].matches("(Sun|Mon|Tue|Wed|Thu|Fri|Sat)"))
                        i++;
                    String sDate = parts[i + 1] + " " + parts[i + 2];
                    if (parts.length > i + 3 && parts[i + 4].length() <= 4)
                        sDate += " " + parts[i + 4];
                    else {
                        sDate += " " + (Calendar.getInstance().get(Calendar.YEAR) + 1900);
                        i--;
                    }
                    if (parts.length > i + 2 && parts[i + 3].indexOf(":") > -1)
                        sDate += " " + parts[i + 3]; // Add Time
                    mDate = DateFormatInstance.parse(sDate).getTime();
                    success = true;
                } catch (Exception e) {
                }
            }
            mName = parts[parts.length - 1];
        }
        if (mName.indexOf(" -> ") > -1) {
            mSym = mName.substring(mName.indexOf(" -> ") + 4);
            mName = mName.substring(0, mName.indexOf(" -> ") - 1).trim();
        }
    }

    @Override
    public boolean isLoaded() {
        return mLoaded;
    }

    @Override
    public Boolean requiresThread() {
        return true;
    }

    @Override
    public Boolean exists() {
        return true;
    }

    @Override
    public boolean addToDb() {
        return super.addToDb();
    }

    @Override
    public Boolean canRead() {
        // assume we can read if we've gotten to this point
        return true;
    }

    @Override
    public String getName() {
        return mName + (isDirectory() && !mName.endsWith("/") ? "/" : "");
    }

    @Override
    public String getPath() {
        return mPath + mName;
    }

    @Override
    public String getAbsolutePath() {
        return getPath();
    }

    @Override
    public void setPath(String path) {
        mPath = path.substring(0, path.lastIndexOf("/"));
        mName = path.substring(path.lastIndexOf("/") + 1);
        OpenFile f = new OpenFile(path);
        mSize = f.length();
        mDate = f.lastModified();
    }

    @Override
    public long length() {
        return mSize != null ? mSize : new File(getPath()).length();
    }

    @Override
    public OpenPath getParent() {
        return FileManager.getOpenCache(mPath);
    }

    @Override
    public OpenPath getChild(String name) {
        try {
            for (OpenPath kid : list())
                if (kid.getName().equals(name))
                    return kid;
        } catch (IOException e) {
        }
        return null;
    }

    public List<OpenPath> getChildren() {
        return mChildren != null ? mChildren.get() : null;
    }

    @Override
    public OpenPath[] list() throws IOException {
        if (mChildren == null)
            return new OpenPath[0];
        if (getChildren() != null)
            return getChildren().toArray(new OpenPath[getChildren().size()]);
        else
            return new OpenPath[0];
    }

    private void addChild(OpenPath kid) {
        if (getChildren() == null) {
            ArrayList<OpenPath> tmp = new ArrayList<OpenPath>();
            mChildren = new WeakReference<List<OpenPath>>(tmp);
        }
        if (!mChildren.get().contains(kid))
            mChildren.get().add(kid);
    }

    public void list(final OpenContentUpdater callback) throws IOException {
        mLoaded = false;
        if (getChildren() != null) {
            for (OpenPath kid : getChildren())
                callback.addContentPath(kid);
            return;
        }
        String path = getPath();
        if (!path.endsWith("/"))
            path += "/";
        Logger.LogDebug("Trying to list " + path + " via Su with Callback");
        final String[] buff = new String[] {
            null
        };
        String lsopts = getLSOpts();
        String bb = RootTools.getBusyBoxVersion();
        if (bb == null)
            bb = "";
        if (bb.equals(""))
            lsopts = "";
        final String w = (lsopts.equals("") ? "" : "busybox ") + "ls -l" + lsopts + " " + path;
        Command cmd = new Command(0, 10, w) {
            @Override
            public void output(int id, String line) {
                if (line.indexOf("\n") > -1)
                    for (String s : line.split("\n"))
                        output(id, s);
                else {
                    OpenFileRoot kid = new OpenFileRoot(getPath(), line);
                    addChild(kid);
                    callback.addContentPath(kid);
                }
            }

            @Override
            public void commandFinished(int id) {
                callback.doneUpdating();
            }
        };
        Shell.startRootShell().add(cmd);
    }

    @Override
    public OpenPath[] listFiles() throws IOException {
        String path = getPath();
        if (!path.endsWith("/"))
            path += "/";
        Logger.LogDebug("Trying to list " + path + " via Su");
        if (mChildren != null)
            mChildren.clear();
        else
            mChildren = new WeakReference<List<OpenPath>>(new ArrayList<OpenPath>());
        String opts = getLSOpts();
        String cmd = "ls -l" + opts + " " + path;
        Command command = new Command(0, 500, cmd) {
            @Override
            public void output(int id, String line) {
                if (line.indexOf("\n") > -1) {
                    for (String s : line.split("\n"))
                        output(id, s);
                    return;
                }
                addChild(new OpenFileRoot(getPath(), line));
            }
        };
        try {
            command.waitForFinish(500);
        } catch (Exception e) {
        }
        /*
         * execute(cmd, !opts.equals("")); while(!data.equals("")) { String
         * child = data; if(data.indexOf("\n") > -1) { child = data.substring(0,
         * data.indexOf("\n")); data = data.substring(data.indexOf("\n") + 1); }
         * else data = ""; if(child.split(" ").length > 4) addChild(new
         * OpenFileRoot(getPath(), child)); else
         * Logger.LogWarning("Skipping Row while listing: " + child); }
         */
        return list();
    }

    private String getLSOpts() {
        String lsOpts = "e"; // full date & time
        if (ShowHiddenFiles)
            lsOpts += "A";
        switch (Sorting.getType()) {
            case ALPHA_DESC:
                lsOpts += "r";
            case ALPHA:
                break;
            case DATE_DESC:
                lsOpts += "r";
            case DATE:
                lsOpts += "t";
                break;
            case SIZE_DESC:
                lsOpts += "r";
            case SIZE:
                lsOpts += "S";
                break;
            case TYPE:
                lsOpts += "X";
                break;
        }
        return lsOpts;
    }

    public File getFile() {
        return new File(getPath());
    }

    @Override
    public Boolean isDirectory() {
        if (mPerms != null)
            return mPerms.startsWith("d");
        return getFile().isDirectory();
    }

    @Override
    public Boolean isFile() {
        return !isDirectory();
    }

    @Override
    public Boolean isHidden() {
        return mName.startsWith(".");
    }

    @Override
    public Uri getUri() {
        return Uri.parse(getPath());
    }

    @Override
    public Long lastModified() {
        return mDate != null ? mDate : new File(getPath()).lastModified();
    }

    @Override
    public Boolean canWrite() {
        if (mPerms != null)
            return mPerms.indexOf("w") > -1;
        return getFile().canWrite();
    }

    @SuppressLint("NewApi")
    @Override
    public Boolean canExecute() {
        if (mPerms != null)
            return mPerms.indexOf("x") > -1;
        return Build.VERSION.SDK_INT > 8 ? getFile().canExecute() : true;
    }

    @Override
    public Boolean delete() {
        execute("rm -rf " + getPath(), false);
        return true;
    }

    @Override
    public Boolean mkdir() {
        return getFile().mkdir();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return tempDownload(null).getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return tempDownload(null).getOutputStream();
    }

    @Override
    public String getDetails(boolean countHiddenChildren) {
        String deets = "";
        if (getChildren() != null)
            deets = getChildren().size() + " %s | ";
        else if (isFile())
            deets = DialogHandler.formatSize(length());
        return deets;
    }

    public String getTempFileName() {
        return getPath().replaceAll("[^A-Za-z0-9\\.]", "-");
    }

    public OpenFile getTempFile() {
        OpenFile root = OpenFile.getTempFileRoot();
        if (root != null)
            return root.getChild(getTempFileName());
        return null;
    }

    @Override
    public OpenFile tempDownload(AsyncTask<?, ?, ?> task) throws IOException {
        OpenFile tmp = getTempFile();
        if (tmp == null)
            throw new IOException("Unable to download Temp file");
        if (!tmp.exists())
            tmp.create();
        else if (lastModified() <= tmp.lastModified())
            return tmp;
        copyTo(tmp);
        return tmp;
    }

    @Override
    public void tempUpload(AsyncTask<?, ?, ?> task) throws IOException {
        OpenFile tmp = getTempFile();
        if (tmp == null)
            throw new IOException("Unable to download Temp file");
        if (!tmp.exists())
            tmp.create();
        else if (lastModified() <= tmp.lastModified())
            return;
        copyFrom(tmp);
    }

    private String execute(final String cmd, boolean useBusyBox) {
        final boolean[] waiting = new boolean[] {
            true
        };
        final String[] ret = new String[] {
            ""
        };
        final String bb = useBusyBox && RootTools.isBusyboxAvailable() ? "busybox " : "";
        try {
            new Command(0, 500, bb + cmd) {
                @Override
                public void output(int id, String line) {
                    ret[0] = ret[0] + (ret[0] == "" ? "" : "\n") + line;
                }
            }.waitForFinish(500);
        } catch (Exception e) {
            Logger.LogError("Could not execute: " + cmd, e);
        }
        return ret[0];
    }

    public static boolean copy(final OpenPath src, final OpenPath dest) {
        return RootTools.copyFile(src.getPath(), dest.getPath(), false, false);
    }

    public boolean copyTo(OpenFile dest) {
        return copy(this, dest);
    }

    @Override
    public boolean copyFrom(OpenPath file) {
        if (file instanceof OpenFile) {
            RootTools.copyFile(file.getPath(), getPath(), true, false);
        }
        return copy(file, this);
    }

    @Override
    public byte[] readBytes() {
        OpenFile tmp = getTempFile();
        try {
            if (!(tmp.exists() && tmp.length() > 0 && tmp.lastModified() >= lastModified()))
                tempDownload(null);
            tmp = getTempFile();
            if (tmp != null)
                return tmp.readAscii().getBytes();
        } catch (Exception e) {
            Logger.LogError("Unable to read root file: " + getPath(), e);
        }
        return null;

    }

    @Override
    public void writeBytes(byte[] bytes) {
        String ret = execute("cat > " + getPath() + "\n" + new String(bytes), false);
        Logger.LogDebug("writeBytes response: " + ret);
    }

    @Override
    public boolean copyTo(OpenPath dest) throws IOException {
        return false;
    }
}

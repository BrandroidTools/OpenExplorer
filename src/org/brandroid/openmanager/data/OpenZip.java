package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.brandroid.utils.Logger;

import android.net.Uri;

public class OpenZip extends OpenPath
{
	private final OpenFile mFile;
	private ZipFile mZip = null;
	private OpenPath[] mChildren = null; 
	
	public OpenZip(OpenFile zipFile)
	{
		mFile = zipFile;
		try {
			mZip = new ZipFile(mFile.getPath());
			//Logger.LogInfo("Zip file " + zipFile + " has " + length() + " entries");
		} catch (IOException e) {
			Logger.LogError("Couldn't open zip file (" + zipFile + ")");
		}
	}
	
	public ZipFile getZip() { return mZip; } 

	@Override
	public String getName() {
		return mFile.getName();
	}

	@Override
	public String getPath() {
		return mFile.getPath();
	}

	@Override
	public String getAbsolutePath() {
		return mFile.getAbsolutePath();
	}

	@Override
	public void setPath(String path) {
		//mZip = new OpenFile(path);
	}

	@Override
	public long length() {
		return mZip.size();
	}

	@Override
	public OpenPath getParent() {
		return mFile.getParent();
	}

	@Override
	public OpenPath getChild(String name) {
		return new OpenZipEntry(this, mZip.getEntry(name));
	}
	
	@Override
	public int getChildCount(boolean countHidden) throws IOException {
		return 1;
	}
	
	@Override
	public int getListLength() {
		try {
			return mChildren != null ? mChildren.length : list().length;
		} catch (IOException e) { }
		return -1;
	}

	@Override
	public OpenPath[] list() throws IOException {
		if(mChildren == null)
			mChildren = listFiles();
		return mChildren;
	}

	@Override
	public OpenPath[] listFiles() throws IOException {
		Logger.LogVerbose("Listing OpenZip " + mFile);
		if(mZip != null)
			mChildren = new OpenPath[mZip.size()];
		else return mChildren;
		Enumeration<? extends ZipEntry> entries = mZip.entries();
		int i = 0;
		while(entries.hasMoreElements())
		{
			mChildren[i++] = new OpenZipEntry(this, entries.nextElement());
			//Logger.LogDebug("Zip Entry #" + i + " = " + mChildren[i - 1].getName());
		}
		return mChildren;
	}

	@Override
	public Boolean isDirectory() {
		return false; // this used to be true, but was causing too many issues
	}

	@Override
	public Boolean isFile() {
		return false;
	}

	@Override
	public Boolean isHidden() {
		return mFile.isHidden();
	}

	@Override
	public Uri getUri() {
		return mFile.getUri();
	}

	@Override
	public Long lastModified() {
		return mFile.lastModified();
	}

	@Override
	public Boolean canRead() {
		return mFile.canRead();
	}

	@Override
	public Boolean canWrite() {
		return mFile.canWrite();
	}

	@Override
	public Boolean canExecute() {
		return false;
	}

	@Override
	public Boolean exists() {
		return mFile.exists();
	}

	@Override
	public Boolean requiresThread() {
		return false;
	}

	@Override
	public Boolean delete() {
		return mFile.delete();
	}

	@Override
	public Boolean mkdir() {
		return mFile.mkdir();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ZipInputStream(mFile.getInputStream());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new ZipOutputStream(mFile.getOutputStream());
	}

}

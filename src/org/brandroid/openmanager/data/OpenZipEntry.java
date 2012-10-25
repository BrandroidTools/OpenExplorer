package org.brandroid.openmanager.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

import org.brandroid.openmanager.fragments.DialogHandler;

import android.net.Uri;

public class OpenZipEntry extends OpenPath
{
	private final OpenZip mZip;
	private final OpenPath mParent;
	private final ZipEntry ze;
	private OpenPath[] mChildren = null;
	
	public OpenZipEntry(OpenZip zip, OpenPath parent, ZipEntry entry)
	{
		mZip = zip;
		mParent = parent;
		ze = entry;
		if(ze.getName().endsWith("/") || ze.isDirectory())
		{
			try {
				mChildren = list();
			} catch(IOException e) { }
		}
	}

	@Override
	public String getName() {
		String name = ze.getName();
		if(name.endsWith("/"))
			name = name.substring(0, name.length() - 1);
		name = name.substring(name.lastIndexOf("/") + 1);
		return name;
	}

	@Override
	public String getPath() {
		return mZip.getPath() + "/" + ze.getName();
	}

	@Override
	public String getAbsolutePath() {
		return getPath();
	}

	@Override
	public void setPath(String path) {
		
	}

	@Override
	public long length() {
		return ze.getSize();
	}

	@Override
	public OpenPath getParent() {
		return mParent;
	}

	@Override
	public OpenPath getChild(String name) {
		try {
			for(OpenPath kid : list())
				if(kid.getName().equals(name))
					return kid;
		} catch(IOException e) { }
		return null;
	}

	@Override
	public OpenPath[] list() throws IOException {
		if(mChildren != null)
			return mChildren;
		return listFiles();
	}

	@Override
	public OpenPath[] listFiles() throws IOException
	{
		return mZip.listFiles(ze.getName());
	}
	
	@Override
	public int getListLength() {
		try {
			return list().length;
		} catch(IOException e) {
			return 0;
		}
	}
	
	@Override
	public String getDetails(boolean countHiddenChildren) {
		String ret = super.getDetails(countHiddenChildren);
		if(!isDirectory())
			ret += " (" + DialogHandler.formatSize(ze.getCompressedSize()) + ")";
		return ret;
	}

	@Override
	public Boolean isDirectory() {
		return ze.isDirectory() || ze.getName().endsWith("/");
	}

	@Override
	public Boolean isFile() {
		return !ze.isDirectory();
	}

	@Override
	public Boolean isHidden() {
		return getName().startsWith(".");
	}

	@Override
	public Uri getUri() {
		return Uri.parse(getPath());
	}

	@Override
	public Long lastModified() {
		return ze.getTime();
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
		return mZip.getZip().getInputStream(ze);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

}

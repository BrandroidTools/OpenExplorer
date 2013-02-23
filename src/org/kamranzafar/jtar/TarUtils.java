/**
 * Copyright 2012 Kamran Zafar 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package org.kamranzafar.jtar;

import java.io.*;
import java.util.zip.GZIPInputStream;

import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.utils.Logger;

import junit.framework.Assert;

/**
 * @author Kamran
 */
public class TarUtils {

    static final int TAR_BUFFER = 2048;

    /**
     * Determines the tar file size of the given folder/file path
     * 
     * @param path
     * @return
     */
    public static long calculateTarSize(File path) {
        return tarSize(path) + TarConstants.EOF_BLOCK;
    }

    private static long tarSize(File dir) {
        long size = 0;

        if (dir.isFile()) {
            return entrySize(dir.length());
        } else {
            File[] subFiles = dir.listFiles();

            if (subFiles != null && subFiles.length > 0) {
                for (File file : subFiles) {
                    if (file.isFile()) {
                        size += entrySize(file.length());
                    } else {
                        size += tarSize(file);
                    }
                }
            } else {
                // Empty folder header
                return TarConstants.HEADER_BLOCK;
            }
        }

        return size;
    }

    private static long entrySize(long fileSize) {
        long size = 0;
        size += TarConstants.HEADER_BLOCK; // Header
        size += fileSize; // File size

        long extra = size % TarConstants.DATA_BLOCK;

        if (extra > 0) {
            size += (TarConstants.DATA_BLOCK - extra); // pad
        }

        return size;
    }

    public static String trim(String s, char c) {
        StringBuffer tmp = new StringBuffer(s);
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        return tmp.toString();
    }

    /**
     * Tar the given folder
     * 
     * @throws IOException
     */
    public static void tar(File to, String folder) throws IOException {
        FileOutputStream dest = new FileOutputStream(to);
        TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));

        tarFolder(null, folder, out);

        out.close();

        System.out.println("Calculated tar size: "
                + TarUtils.calculateTarSize(new File("/home/kamran/tmp/tartest")));
        System.out.println("Actual tar size: " + new File("/home/kamran/tmp/tartest.tar").length());
    }

    /**
     * Untar the tar file
     * 
     * @throws IOException
     */
    public static void untarTarFile(String destFolder, String tar, String... include)
            throws IOException {
        OpenFile zf = new OpenFile(tar);
        TarInputStream tis = null;
        if (zf.getMimeType().contains("x-"))
            tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(
                    zf.getInputStream())));
        else
            tis = new TarInputStream(new BufferedInputStream(zf.getInputStream()));
        untar(tis, destFolder, include);

        tis.close();
    }

    /**
     * Untar the tar file
     * 
     * @throws IOException
     */
    public static void untarTarFileDefaultSkip(String destFolder, String tar) throws IOException {
        File zf = new File(tar);

        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
        tis.setDefaultSkip(true);

        untar(tis, destFolder);

        tis.close();
    }

    /**
     * Untar the gzipped-tar file
     * 
     * @throws IOException
     */
    public static void untarTGzFile(String destFolder, String tgz) throws IOException {
        File zf = new File(tgz);

        TarInputStream tis = new TarInputStream(new BufferedInputStream(new GZIPInputStream(
                new FileInputStream(zf))));

        untar(tis, destFolder);

        tis.close();
    }

    private static void untar(TarInputStream tis, String destFolder, String... includes)
            throws IOException {

        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {
            byte data[] = new byte[TAR_BUFFER];
            boolean doWrite = checkIncludes(entry.getName(), includes);
            if (!doWrite)
                continue;
            BufferedOutputStream dest = null;

            int count;

            if (entry.isDirectory()) {
                new File(destFolder + "/" + entry.getName()).mkdirs();
                continue;
            }

            String name = entry.getName();
            if (includes.length > 0)
                name = name.substring(name.lastIndexOf("/") + 1); // if we are pasting into a directory, only use the actual filename

            String file = destFolder + "/" + name;

            Logger.LogVerbose("Extracting from tar: " + entry.getName() + " --> " + file);

            OpenFile of = new OpenFile(file);
            if (includes.length > 0)
                of.getParent().mkdir();

            OutputStream fos = of.getOutputStream();
            dest = new BufferedOutputStream(fos);

            while ((count = tis.read(data)) != -1) {
                dest.write(data, 0, count);
            }

            dest.flush();
            dest.close();
            if (!doWrite)
                of.delete();
        }
    }

    private static boolean checkIncludes(String path, String... includes)
    {
        if (includes.length == 0)
            return true;
        for (String s : includes)
            if (s.equalsIgnoreCase(path))
                return true;
        return false;
    }

    public static boolean checkUntar(String tar, String destFolder, String... includes)
    {
        File zf = new File(tar);

        TarInputStream tis = null;
        boolean ret = false;
        try {
            tis = new TarInputStream(new BufferedInputStream(new FileInputStream(zf)));
            ret = checkUntar(tis, destFolder, includes);
        } catch (IOException e) {
            Logger.LogError("Unable to check tar.", e);
        } finally {
            try {
                tis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return ret;
    }

    private static boolean checkUntar(TarInputStream tis, String destFolder, String... includes)
            throws IOException {
        TarEntry entry;
        while ((entry = tis.getNextEntry()) != null) {

            if (!checkIncludes(entry.getName(), includes))
                continue;

            File f = new File(destFolder + "/"
                    + (includes.length > 0 ? entry.getHeader().name : entry.getName()));
            if (f.exists())
                return true;

        }
        return false;
    }

    public static void tarFolder(String parent, String path, TarOutputStream out)
            throws IOException {
        BufferedInputStream origin = null;
        File f = new File(path);
        String files[] = f.list();

        // is file
        if (files == null) {
            files = new String[1];
            files[0] = f.getName();
        }

        parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName()
                + "/");

        for (int i = 0; i < files.length; i++) {
            System.out.println("Adding: " + files[i]);
            File fe = f;
            byte data[] = new byte[TAR_BUFFER];

            if (f.isDirectory()) {
                fe = new File(f, files[i]);
            }

            if (fe.isDirectory()) {
                String[] fl = fe.list();
                if (fl != null && fl.length != 0) {
                    tarFolder(parent, fe.getPath(), out);
                } else {
                    TarEntry entry = new TarEntry(fe, parent + files[i] + "/");
                    out.putNextEntry(entry);
                }
                continue;
            }

            FileInputStream fi = new FileInputStream(fe);
            origin = new BufferedInputStream(fi);

            TarEntry entry = new TarEntry(fe, parent + files[i]);
            out.putNextEntry(entry);

            int count;

            while ((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }

            out.flush();

            origin.close();
        }
    }

    public static void fileEntry(String fileName) throws IOException {

        File f = new File(fileName);
        long fileSize = f.length();
        long modTime = f.lastModified() / 1000;

        // Create a header object and check the fields
        TarHeader fileHeader = TarHeader.createHeader(fileName, fileSize, modTime, false);
        Assert.assertEquals(fileName, fileHeader.name.toString());
        Assert.assertEquals(TarHeader.LF_NORMAL, fileHeader.linkFlag);
        Assert.assertEquals(fileSize, fileHeader.size);
        Assert.assertEquals(modTime, fileHeader.modTime);

        // Create an entry from the header
        TarEntry fileEntry = new TarEntry(fileHeader);
        Assert.assertEquals(fileName, fileEntry.getName());

        // Write the header into a buffer, create it back and compare them
        byte[] headerBuf = new byte[TarConstants.HEADER_BLOCK];
        fileEntry.writeEntryHeader(headerBuf);
        TarEntry createdEntry = new TarEntry(headerBuf);
        Assert.assertTrue(fileEntry.equals(createdEntry));
    }
}

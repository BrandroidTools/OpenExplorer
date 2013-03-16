package net.contrapunctus.lzma;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Command
{
    static void copy(InputStream in, OutputStream out) throws IOException
    {
        final int BUFSIZE = 4096;
        byte[] buf = new byte[BUFSIZE];
        int n = in.read(buf);
        while(n != -1) {
            out.write(buf, 0, n);
            n = in.read(buf);
        }
        out.close();
    }

    public static void main(String[] args) throws IOException
    {
        copy(System.in, new LzmaOutputStream(System.out));
    }
}

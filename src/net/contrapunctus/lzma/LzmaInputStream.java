// LzmaInputStream.java -- transparently decompress LZMA while reading
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import SevenZip.LZMADecoder;
import SevenZip.LZMAEncoder;

import java.io.*;

public class LzmaInputStream extends FilterInputStream
{
    protected DecoderThread dth;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_LzmaStreams"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    public LzmaInputStream( InputStream _in )
    {
        super( null );
        dth = new DecoderThread( _in );
        in = ConcurrentBufferInputStream.create( dth.q );
        if(DEBUG) dbg.printf("%s << %s (%s)%n", this, in, dth.q);
        dth.start( );
    }

    public int read() throws IOException
    {
        int k = in.read();
        dth.maybeThrow();
        return k;
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
        int k = in.read(b, off, len);
        dth.maybeThrow();
        return k;
    }

    public void close( ) throws IOException
    {
        if(DEBUG) dbg.printf("%s closed%n", this);
        super.close( );
    }

    public String toString( )
    {
        return String.format("lzmaIn@%x", hashCode());
    }
}

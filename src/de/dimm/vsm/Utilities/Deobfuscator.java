/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Administrator
 */
public class Deobfuscator extends InputStream
{
    InputStream is;
    public Deobfuscator( String file ) throws FileNotFoundException
    {
        // WE NEED MARK / RESET, SO WE USE BIS
        is = new BufferedInputStream( new FileInputStream( file ), 128*1024 );
    }
   
    @Override
    public synchronized void reset() throws IOException
    {
        is.reset();
    }

    @Override
    public synchronized void mark( int arg0 )
    {
        is.mark(arg0);
    }
    

    @Override
    public boolean markSupported()
    {
        boolean ok = is.markSupported();
        return ok;
    }
    
    @Override
    public int read() throws IOException
    {
        int data = is.read();
        if (data != -1)
        {
            byte bb = (byte) data;
            bb = (byte)~bb;
            return bb;
        }
        return data;
    }
    @Override
    public int read(byte[] b) throws IOException
    {
        int ret = is.read(b);
        for (int i = 0; i < b.length; i++)
        {
            b[i] = (byte)~b[i];
        }
        return ret;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int ret = is.read(b, off, len);
        for (int i = 0; i < len; i++)
        {
            b[i + off] = (byte)~b[i+ off];
        }
        return ret;        
    }
    
}

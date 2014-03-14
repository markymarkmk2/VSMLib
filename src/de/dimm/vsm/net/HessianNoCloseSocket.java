/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
class NoCLoseInputStream extends FilterInputStream
{

    public NoCLoseInputStream(InputStream is)
    {
        super(is);
    }

    @Override
    public void close() throws IOException
    {

    }
    void realClose()
    {
        try
        {
            in.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(NoCLoseInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public class HessianNoCloseSocket  extends Socket
{
    NoCLoseInputStream is;
    int connTimeout;
    int txTimeout;

    public HessianNoCloseSocket( int connTimeout, int txTimeout )
    {
        this.is = null;
        this.connTimeout = connTimeout;
        this.txTimeout = txTimeout;
    }



    @Override
    public synchronized void close() throws IOException
    {
        //super.close();
    }

    @Override
    public void connect( SocketAddress endpoint ) throws IOException
    {
        super.connect(endpoint, connTimeout);
        if (txTimeout > 0)
            setSoTimeout(txTimeout);
        is = new NoCLoseInputStream(super.getInputStream());
    }


    @Override
    public InputStream getInputStream() throws IOException
    {
        return is;
    }
    void realClose() throws IOException
    {
        if (is != null)
            is.realClose();
        super.close();
    }
}
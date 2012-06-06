/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class CommThread extends Thread
{
    Socket socket;

    public CommThread( Runnable r, String name )
    {
        super(r, name );
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket( Socket socket )
    {
        this.socket = socket;
    }

    
}

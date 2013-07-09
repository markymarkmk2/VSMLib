/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class RemoteFSElemWrapper implements Serializable
{
    long handle;
    boolean xa;
    boolean virtualFS;


    public RemoteFSElemWrapper( long handle, boolean xa, boolean virtualFS )
    {
        this.handle = handle;
        this.xa = xa;
        this.virtualFS = virtualFS; 
    }

    public long getHandle()
    {
        return handle;
    }

    public boolean isXa()
    {
        return xa;
    }

    public boolean isVirtualFS()
    {
        return virtualFS;
    }

   

}

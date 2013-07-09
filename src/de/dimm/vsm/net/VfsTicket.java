/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Administrator
 */
public class VfsTicket  implements Serializable
{
     String errorText = null;
     boolean ok = false;
     boolean sync = true;
     StoragePoolWrapper wrapper;

    public VfsTicket( StoragePoolWrapper wrapper )
    {
        this.wrapper = wrapper;
    }


    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof VfsTicket)
        {
            VfsTicket t = (VfsTicket)obj;
            return t.getPoolIdx() == getPoolIdx() && t.getSubPath().equals(getSubPath())  && t.getIp().equals(getIp());
        }
        return super.equals(obj);
    }

   

    public boolean isSync()
    {
        return sync;
    }

    public void setSync( boolean sync )
    {
        this.sync = sync;
    }

    public int getPort()
    {
        return wrapper.getPort();
    }
    
    
    

   

    public long getPoolIdx()
    {
        return wrapper.getPoolIdx();
    }

    public String getSubPath()
    {
        return wrapper.getBasePath();
    }

    public String getIp()
    {
        return wrapper.getAgentIp();
    }
    
    
    


     
    @Override
    public String toString()
    {
        return getSubPath();
    }

    public String getErrorText()
    {
        return errorText;
    }

    public void setErrorText( String errorText )
    {
        this.errorText = errorText;
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk( boolean ok )
    {
        this.ok = ok;
    }
    
}

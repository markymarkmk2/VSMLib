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
public class CdpTicket  implements Serializable
{
     long poolIdx;
     long schedIdx;
     long clientInfoIdx;
     long clientVolumeIdx;
     String errorText;
     boolean ok;

    public CdpTicket( long poolIdx, long schedIdx, long clientInfoIdx, long clientVolumeIdx )
    {
        this.poolIdx = poolIdx;
        this.schedIdx = schedIdx;
        this.clientInfoIdx = clientInfoIdx;
        this.clientVolumeIdx = clientVolumeIdx;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof CdpTicket)
        {
            CdpTicket t = (CdpTicket)obj;
            return t.poolIdx == poolIdx && t.schedIdx == schedIdx && t.clientInfoIdx == clientInfoIdx && t.clientVolumeIdx == clientVolumeIdx;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 41 * hash + (int) (this.poolIdx ^ (this.poolIdx >>> 32));
        hash = 41 * hash + (int) (this.schedIdx ^ (this.schedIdx >>> 32));
        hash = 41 * hash + (int) (this.clientInfoIdx ^ (this.clientInfoIdx >>> 32));
        hash = 41 * hash + (int) (this.clientVolumeIdx ^ (this.clientVolumeIdx >>> 32));
        return hash;
    }


     
    @Override
    public String toString()
    {
        return "SC:" + schedIdx + " CI:" + clientInfoIdx + " CV:" + clientVolumeIdx;
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

    public long getPoolIdx()
    {
        return poolIdx;
    }

    public long getSchedIdx()
    {
        return schedIdx;
    }

    public long getClientInfoIdx()
    {
        return clientInfoIdx;
    }

    public long getClientVolumeIdx()
    {
        return clientVolumeIdx;
    }
    
    


}

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
public class StoragePoolWrapper implements Serializable
{
    long idx;
    long poolIdx;
    StoragePoolQry qry;
    boolean physicallyMounted;
    boolean poolHandlerCreated;
    boolean closeOnUnmount;

    public StoragePoolWrapper( long idx, long poolIdx, StoragePoolQry qry, boolean _poolHandlerWasCreated )
    {
        this.idx = idx;
        this.poolIdx = poolIdx;
        this.qry = qry;
        physicallyMounted = false;
        poolHandlerCreated = _poolHandlerWasCreated;
    }

    public boolean isCloseOnUnmount()
    {
        return closeOnUnmount;
    }

    public void setCloseOnUnmount( boolean closeOnUnmount )
    {
        this.closeOnUnmount = closeOnUnmount;
    }
    

    public boolean isPoolHandlerCreated()
    {
        return poolHandlerCreated;
    }
    

    public long getIdx()
    {
        return idx;
    }

    public StoragePoolQry getQry()
    {
        return qry;
    }



    public long getPoolIdx()
    {
        return poolIdx;
    }

    public long getTs()
    {
        return qry.getSnapShotTs();
    }

    public boolean isReadOnly()
    {
        return qry.isReadOnly();
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof StoragePoolWrapper)
        {
            return idx == ((StoragePoolWrapper)obj).idx;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + (int) (this.idx ^ (this.idx >>> 32));
        return hash;
    }

    public boolean isPhysicallyMounted()
    {
        return physicallyMounted;
    }

    public void setPhysicallyMounted( boolean physicallyMounted )
    {
        this.physicallyMounted = physicallyMounted;
    }
    

}

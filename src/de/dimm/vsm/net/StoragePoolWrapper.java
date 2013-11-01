/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.MountEntry;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class StoragePoolWrapper implements Serializable, IWrapper
{
    long idx;
    long poolIdx;
    StoragePoolQry qry;
    boolean physicallyMounted;
    boolean poolHandlerCreated;
    boolean closeOnUnmount;
    String mountEntryKey;
    String mountEntrySubPath;

    String agentIp;
    int port;

//    public StoragePoolWrapper()
//    {
//    }
    public StoragePoolWrapper( long idx, long poolIdx, String agentIp, int port, StoragePoolQry qry, boolean _poolHandlerWasCreated )
    {
        this.idx = idx;
        this.poolIdx = poolIdx;
        this.qry = qry;
        this.agentIp = agentIp;
        this.port = port;
        physicallyMounted = false;
        poolHandlerCreated = _poolHandlerWasCreated;
    }

    @Override
    public String toString()
    {
        return "Pool " + poolIdx + " Agent " + agentIp + " Qry: " + qry;
    }
    

    public String getBasePath()
    {
        if (StringUtils.isEmpty(mountEntrySubPath))
            return "/" + agentIp + "/" + port;
        return mountEntrySubPath;
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
    

//    public long getIdx()
//    {
//        return idx;
//    }

    @Override
    public StoragePoolQry getQry()
    {
        return qry;
    }
    @Override
    public long getWrapperIdx()
    {
        return idx;
    }


    @Override
    public long getPoolIdx()
    {
        return poolIdx;
    }

    @Override
    public long getTs()
    {
        return qry.getSnapShotTs();
    }

    @Override
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

    public void setMountEntry( MountEntry mountEntry )
    {
        this.mountEntryKey = mountEntry.getKey();
        mountEntrySubPath = mountEntry.getSubPath();
    }

    public String getMountEntryKey()
    {
        return mountEntryKey;
    }
    
    public String resolveRelPath(String path)
    {
        if (path.contains(":\\"))
        {
            path = path.replace(":\\", "/");
            path = path.replace("\\", "/");
        }
        if (path.contains("\\\\"))
        {
            path = path.replace("\\\\", "/");
            path = path.replace("\\", "/");
        }
        if (path.startsWith("/"))
            return getBasePath() + path;
        else
            return getBasePath() +"/" + path;
    }

    public int getPort()
    {
        return port;
    }

    public String getAgentIp()
    {
        return agentIp;
    }
    

    

}

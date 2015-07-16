/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.net.interfaces.IWrapper;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class SearchWrapper  implements Serializable, IWrapper
{
    long wrapperIdx;
    long poolIdx;

    StoragePoolQry qry;
    String webDavToken;

    SearchWrapper( long newIdx, long idx, StoragePoolQry qry )
    {
        wrapperIdx = newIdx;
        poolIdx = idx;
        this.qry = qry;
    }

    @Override
    public long getPoolIdx()
    {
        return poolIdx;
    }

    @Override
    public StoragePoolQry getQry()
    {
        return qry;
    }

    @Override
    public long getWrapperIdx()
    {
        return wrapperIdx;
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
        if (obj instanceof SearchWrapper)
        {
            return wrapperIdx == ((SearchWrapper)obj).wrapperIdx;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + (int) (this.wrapperIdx ^ (this.wrapperIdx >>> 32));
        return hash;
    }
    @Override
    public String getWebDavToken() {
        return webDavToken;
    }

    public void setWebDavToken( String webDavToken ) {
        this.webDavToken = webDavToken;
    }


    
}

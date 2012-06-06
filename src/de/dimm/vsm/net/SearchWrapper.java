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
public class SearchWrapper  implements Serializable
{
    long wrapperIdx;
    long poolIdx;

    StoragePoolQry qry;

    SearchWrapper( long newIdx, long idx, StoragePoolQry qry )
    {
        wrapperIdx = newIdx;
        poolIdx = idx;
        this.qry = qry;
    }

    public long getPoolIdx()
    {
        return poolIdx;
    }

    public StoragePoolQry getQry()
    {
        return qry;
    }

    public long getWrapperIdx()
    {
        return wrapperIdx;
    }
    


    
}

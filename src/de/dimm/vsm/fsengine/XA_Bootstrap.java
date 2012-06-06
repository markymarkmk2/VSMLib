/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.records.XANode;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class XA_Bootstrap implements Serializable
{

    long idx;
    long fileNodeIdx;
    long ts;
    long dedupBlockIdx;
    String hashvalue;
    boolean reorganize;   // NODE HAS TO BE MOVED FROM HASHBLOCK BUFFER TO ORIGFILE
    long blockOffset;
    int blockLen;

    public XA_Bootstrap( XANode xa )
    {
        idx = xa.getIdx();
        fileNodeIdx = xa.getFileNode().getIdx();
        ts = xa.getTs();
        dedupBlockIdx = 0;
        if (xa.getDedupBlock() != null)
            dedupBlockIdx = xa.getDedupBlock().getIdx();
        hashvalue = xa.getHashvalue();
        reorganize = xa.isReorganize();
        blockLen = xa.getBlockLen();
        blockOffset = xa.getBlockOffset();        
    }    

    void setBlock( XANode hb )
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

   
}

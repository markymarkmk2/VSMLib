/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.records.DedupHashBlock;
import de.dimm.vsm.records.HashBlock;
import de.dimm.vsm.records.XANode;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class HB_Bootstrap implements Serializable
{

    private long idx;
    private long fileNodeIdx;
    private long ts;
    private long dedupBlockIdx;
    private String hashvalue;
    private boolean reorganize;   // NODE HAS TO BE MOVED FROM HASHBLOCK BUFFER TO ORIGFILE
    private long blockOffset;
    private int blockLen;

    public HB_Bootstrap( HashBlock hb )
    {
        idx = hb.getIdx();
        fileNodeIdx = hb.getFileNode().getIdx();
        ts = hb.getTs();
        dedupBlockIdx = hb.getDedupBlock().getIdx();
        hashvalue = hb.getHashvalue();
        reorganize = hb.isReorganize();
        blockLen = hb.getBlockLen();
        blockOffset = hb.getBlockOffset();
    }
    
    public HashBlock getHashBlock( DedupHashBlock dd)
    {
        HashBlock hb = new HashBlock();
        hb.setBlockLen(blockLen);
        hb.setIdx(idx);
        hb.setBlockOffset(blockOffset);
        hb.setTs(ts);
        hb.setReorganize(reorganize);
        hb.setHashvalue(hashvalue);
        hb.setDedupBlock(dd);
        return hb;
    }
    
  

    public void setBlock( HashBlock hb )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getBlockLen()
    {
        return blockLen;
    }
    
    public String getHashvalue()
    {
        return hashvalue;
    }  

    public long getFileNodeIdx()
    {
        return fileNodeIdx;
    }

    public long getDedupBlockIdx()
    {
        return dedupBlockIdx;
    }

    public long getIdx()
    {
        return idx;
    }
    
}

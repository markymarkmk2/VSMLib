/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.records.HashBlock;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class HB_Bootstrap implements Serializable
{

    long idx;
    long fileNodeIdx;
    long ts;
    long dedupBlockIdx;
    String hashvalue;
    boolean reorganize;   // NODE HAS TO BE MOVED FROM HASHBLOCK BUFFER TO ORIGFILE
    long blockOffset;
    int blockLen;

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

    public void setBlock( HashBlock hb )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

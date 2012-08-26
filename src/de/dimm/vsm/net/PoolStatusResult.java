/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

/**
 *
 * @author Administrator
 */
public class PoolStatusResult {

    long dedupDataLen;
    long actFsDataLen;
    long actFileCnt;
    long totalFsDataLen;
    long totalFileCnt;
    
    long removeDDCnt;
    long removeDDLen;

    long ddTotalBlocks;

    public PoolStatusResult( long dedupDataLen, long actFsDataLen, long actFileCnt, long totalFsDataLen, long totalFileCnt, long removeDDCnt, long removeDDLen, long ddTotalBlocks )
    {
        this.dedupDataLen = dedupDataLen;
        this.actFsDataLen = actFsDataLen;
        this.actFileCnt = actFileCnt;
        this.totalFsDataLen = totalFsDataLen;
        this.totalFileCnt = totalFileCnt;
        this.removeDDCnt = removeDDCnt;
        this.removeDDLen = removeDDLen;
        this.ddTotalBlocks = ddTotalBlocks;
    }

    public long getActFileCnt()
    {
        return actFileCnt;
    }

    public long getActFsDataLen()
    {
        return actFsDataLen;
    }

    public long getDdTotalBlocks()
    {
        return ddTotalBlocks;
    }

    public long getDedupDataLen()
    {
        return dedupDataLen;
    }

    public long getRemoveDDCnt()
    {
        return removeDDCnt;
    }

    public long getRemoveDDLen()
    {
        return removeDDLen;
    }

    public long getTotalFileCnt()
    {
        return totalFileCnt;
    }


    public long getTotalFsDataLen()
    {
        return totalFsDataLen;
    }

    public int getDedupRatio()
    {
        int ratio = 0;
        if (dedupDataLen > 0 && totalFsDataLen > 0 && actFsDataLen > 0)
        {
            ratio = 100 - (int)(100 * dedupDataLen / actFsDataLen + 0.5);
        }
        return ratio;
    }
    public int getTotalDedupRatio()
    {
        int ratio = 0;
        if (dedupDataLen > 0 && totalFsDataLen > 0 && actFsDataLen > 0)
        {
            ratio = 100 - (int)(100 * dedupDataLen / totalFsDataLen + 0.5);
        }
        return ratio;
    }


    

    
}

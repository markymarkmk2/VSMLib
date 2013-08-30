/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.records.PoolNodeFileLink;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class PNFL_Bootstrap implements Serializable
{
    long idx;
    long fileIdx;
    long nodeIdx;
    long creationDateMs;

    public PNFL_Bootstrap( PoolNodeFileLink node )
    {
        if (node.getCreation() != null)
            creationDateMs = node.getCreation().getTime();
        else
            creationDateMs = System.currentTimeMillis();
        
        fileIdx = node.getFileNode().getIdx();
        nodeIdx = node.getStorageNode().getIdx();
        idx = node.getIdx();        
    }  

    public long getCreationDateMs()
    {
        return creationDateMs;
    }
   
    public PoolNodeFileLink getNode(long idx)
    {
        PoolNodeFileLink node = new PoolNodeFileLink();
        node.setIdx(idx);
        node.setCreation( new Date(creationDateMs));
        return node;        
    }        
    public void setNode(PoolNodeFileLink node)
    {
        node.setIdx(idx);
        node.setCreation( new Date(creationDateMs));
    } 

    public long getFileIdx()
    {
        return fileIdx;
    }

    public long getNodeIdx()
    {
        return nodeIdx;
    }
    
}
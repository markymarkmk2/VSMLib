/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.FileSystemElemNode;
import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class FSEA_Bootstrap implements Serializable
{

    String name;
    long creationDateMs;
    long modificationDateMs;
    long accessDateMs;
    long base_ts;
    long this_ts;
    int posix_mode;
    long file_size;
    long stream_size;
    int uid;
    int gid;
    String aclInfo;

    public FSEA_Bootstrap( FileSystemElemAttributes node )
    {
        name = node.getName();
        creationDateMs = node.getCreationDateMs();
        modificationDateMs = node.getModificationDateMs();
        accessDateMs = node.getAccessDateMs();
        base_ts = 0;//node.getBase_ts();
        this_ts = node.getTs();
        posix_mode = node.getPosixMode();
        file_size = node.getFsize();
        stream_size = node.getStreamSize();
        uid = node.getUid();
        gid = node.getGid();
        aclInfo = node.getAclInfoData();
    }

    public void setNode( FileSystemElemAttributes node )
    {
        node.setName(name);
        node.setAclInfoData(aclInfo);
        node.setAccessDateMs(accessDateMs);
        node.setCreationDateMs(creationDateMs);
        node.setModificationDateMs(modificationDateMs);
        //node.setBase_ts(base_ts);
        node.setTs(this_ts);
        node.setFsize(file_size);
        node.setStreamSize(stream_size);
        node.setPosixMode(posix_mode);
        node.setUid(uid);
        node.setGid(gid);
    }

    public long getAccessDateMs()
    {
        return accessDateMs;
    }
//
//    public long getBase_ts()
//    {
//        return base_ts;
//    }

    public long getCreationDateMs()
    {
        return creationDateMs;
    }

    public long getFsize()
    {
        return file_size;
    }

   

    public int getGid()
    {
        return gid;
    }

    public long getModificationDateMs()
    {
        return modificationDateMs;
    }

    public String getName()
    {
        return name;
    }

    public int getPosixMode()
    {
        return posix_mode;
    }

    public long getTs()
    {
        return this_ts;
    }


    public int getUid()
    {
        return uid;
    }

    public String getAclInfo()
    {
        return aclInfo;
    }

    public long getStreamSize()
    {
        return stream_size;
    }
    
    public FileSystemElemAttributes getNode(long idx)
    {
        FileSystemElemAttributes attr = new FileSystemElemAttributes();
        attr.setAccessDateMs(accessDateMs);
        attr.setAclInfoData(aclInfo);
        attr.setCreationDateMs(creationDateMs);
        attr.setDeleted(false);
        attr.setGid(gid);
        attr.setModificationDateMs(modificationDateMs);
        attr.setName(name);
        attr.setPosixMode(posix_mode);
        attr.setStreamSize(stream_size);
        attr.setFsize(file_size);
        attr.setTs(this_ts);
        attr.setUid(uid);
                        
        return attr;        
    }    
    
}
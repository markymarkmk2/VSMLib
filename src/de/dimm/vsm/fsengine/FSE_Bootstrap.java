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
public class FSE_Bootstrap implements Serializable
{

    String typ;
    String name;
    long creationDateMs;
    long modificationDateMs;
    long accessDateMs;
    long base_ts;
    long this_ts;
    int posix_mode;
    long file_size;
    long stream_size;
    long attr_idx;
    int uid;
    int gid;
    String aclInfo;
    String filesystem_type;

    public FSE_Bootstrap( FileSystemElemNode fsenode )
    {
        FileSystemElemAttributes node = fsenode.getAttributes();

        typ = fsenode.getTyp();
        filesystem_type = fsenode.getFilesystem_type();

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
        attr_idx = node.getIdx();
    }

    public void setNode( FileSystemElemNode fsenode )
    {
        fsenode.setTyp(typ);
        fsenode.setFilesystem_type(filesystem_type);

        FileSystemElemAttributes node = fsenode.getAttributes();

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
        node.setIdx(attr_idx);
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

    public String getFilesystem_type()
    {
        return filesystem_type;
    }

    public int getGid()
    {
        return gid;
    }

    public long getAttrIdx()
    {
        return attr_idx;
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

    public String getTyp()
    {
        return typ;
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

    public FileSystemElemNode getNode(long idx)
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
        attr.setIdx(attr_idx);
        
        FileSystemElemNode node = new FileSystemElemNode();
        node.setFilesystem_type(filesystem_type);
        node.setTyp(typ);
        node.setIdx(idx);
        node.setAttributes(attr);
        
        return node;        
    }

   
    
}
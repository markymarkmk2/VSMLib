/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.fsengine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import org.catacombae.jfuse.util.Log;

/**
 *
 * @author Administrator
 */
public class PsMaker {

    public final static String[] PS = {
        " PreparedStatement 1: select max(idx) from MessageLog, [",
        " PreparedStatement 2: insert into MESSAGELOG (idx,creation,errLevel,userId,moduleName,messageId,additionText,exceptionName,exceptionText,exceptionStack) values (?,?,?,?,?,?,?,?,?,?), [",
        " PreparedStatement 3: select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.idx=?, [idx",
        " PreparedStatement 4: select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.pool_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 5: select T1.idx,T1.name,T1.creation,T1.rootDir_idx,T1.landingZone from STORAGEPOOL T1 where T1.idx=?, [idx",
        " PreparedStatement 6: select T1.idx,T1.type,T1.ip,T1.port,T1.username,T1.pwd,T1.searchbase,T1.searchattribute,T1.mailattribute,T1.domainlist,T1.excludefilter,T1.flags,T1.ldapdomain,T1.ldapfilter,T1.groupIdentifier,T1.ntDomainName from ACCOUNTCONNECTOR T1 where T1.idx=?, [idx",
        " PreparedStatement 7: select T1.idx,T1.role_idx,T1.token,T1.flags,T1.optionStr from ROLEOPTION T1 where T1.role_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 8: select T1.idx,T1.name,T1.opts,T1.emailText,T1.smtpdata_idx from MAILGROUP T1 where T1.idx=?, [idx",
        " PreparedStatement 9: select T1.idx,T1.name,T1.serverip,T1.serverport,T1.smtpfrom,T1.username,T1.userpwd,T1.ssl,T1.tls from SMTPLOGINDATA T1 where T1.idx=?, [idx",
        " PreparedStatement 10: select T1.idx,T1.disabled,T1.offsetStartMs,T1.dayNumber,T1.overrideSnapshotEnabled,T1.sched_idx from JOB T1 where T1.sched_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 11: select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.sched_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 12: select T1.argument,T1.isDir,T1.isFullPath,T1.includeMatches,T1.ignorecase,T1.mode,T1.idx,T1.clinfo_idx from EXCLUDES T1 where T1.clinfo_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 13: select T1.idx,T1.clinfo_idx,T1.volumePath,T1.disabled,T1.cdp,T1.snapshot,T1.staylocal,T1.fsType from CLIENTVOLUME T1 where T1.clinfo_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 14: select max(idx) from BackupJobResult, [",
        " PreparedStatement 15: insert into BACKUPJOBRESULT (idx,startTime,endTime,ok,status,schedule_idx) values (?,?,?,?,?,?), [",
        " PreparedStatement 16: select max(idx) from BackupVolumeResult, [",
        " PreparedStatement 17: insert into BACKUPVOLUMERESULT (idx,startTime,endTime,ok,status,jobResult_idx,volume_idx,filesChecked,filesTransfered,dataChecked,dataTransfered) values (?,?,?,?,?,?,?,?,?,?,?), [",
        " PreparedStatement 18: select T1.idx,T1.fileNode_idx,T1.storageNode_idx,T1.creation from POOLNODEFILELINK T1 where T1.fileNode_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 19: select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.parent_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 20: select max(idx) from FileSystemElemAttributes, [",
        " PreparedStatement 21: insert into FILESYSTEMELEMATTRIBUTES (idx,name,creationDateMs,modificationDateMs,accessDateMs,deleted,ts,posixMode,fsize,xasize,uid,gid,uidName,gidName,xattribute,aclinfo,file_idx,flags) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?), [",
        " PreparedStatement 22: update FILESYSTEMELEMNODE set typ=?,filesystem_type=?,pool_idx=?,parent_idx=?,attributes_idx=?,flags=? where idx=?, [",
        " PreparedStatement 23: update FILESYSTEMELEMATTRIBUTES set name=?,creationDateMs=?,modificationDateMs=?,accessDateMs=?,deleted=?,ts=?,posixMode=?,fsize=?,xasize=?,uid=?,gid=?,uidName=?,gidName=?,xattribute=?,aclinfo=?,file_idx=?,flags=? where idx=?, [",
        " PreparedStatement 24: select max(idx) from FileSystemElemNode, [",
        " PreparedStatement 25: insert into FILESYSTEMELEMNODE (idx,typ,filesystem_type,pool_idx,parent_idx,attributes_idx,flags) values (?,?,?,?,?,?,?), [",
        " PreparedStatement 26: select max(idx) from PoolNodeFileLink, [",
        " PreparedStatement 27: insert into POOLNODEFILELINK (idx,fileNode_idx,storageNode_idx,creation) values (?,?,?,?), [",
        " PreparedStatement 28: select T1.idx,T1.pool_idx,T1.storageNode_idx,T1.blockLen,T1.hashvalue from DEDUPHASHBLOCK T1 where T1.idx=?, [idx",
        " PreparedStatement 29: select max(idx) from HashBlock, [",
        " PreparedStatement 30: insert into HASHBLOCK (idx,fileNode_idx,ts,dedupBlock_idx,hashvalue,reorganize,blockOffset,blockLen) values (?,?,?,?,?,?,?,?), [",
        " PreparedStatement 31: select max(idx) from XANode, [",
        " PreparedStatement 32: insert into XANODE (idx,fileNode_idx,ts,hashvalue,reorganize,blockOffset,blockLen,dedupBlock_idx,streamInfo) values (?,?,?,?,?,?,?,?,?), [",
        " PreparedStatement 33: update BACKUPVOLUMERESULT set startTime=?,endTime=?,ok=?,status=?,jobResult_idx=?,volume_idx=?,filesChecked=?,filesTransfered=?,dataChecked=?,dataTransfered=? where idx=?, [",
        " PreparedStatement 34: update BACKUPJOBRESULT set startTime=?,endTime=?,ok=?,status=?,schedule_idx=? where idx=?, [",
        " PreparedStatement 35: select T1.idx,T1.startTime,T1.endTime,T1.lastAccess,T1.ok,T1.name,T1.directory_idx,T1.sourceType,T1.sourceIdx,T1.totalSize from ARCHIVEJOB T1 where T1.idx=?, [idx",
        " PreparedStatement 36: select T1.idx,T1.startTime,T1.endTime,T1.ok,T1.status,T1.jobResult_idx,T1.volume_idx,T1.filesChecked,T1.filesTransfered,T1.dataChecked,T1.dataTransfered from BACKUPVOLUMERESULT T1 where T1.jobResult_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 37: update ROLE set name=?,opts=?,accountmatch=?,license=?,flags=?,user4eyes=?,pwd4eyes=?,accountConnector_idx=? where idx=?, [",
        " PreparedStatement 38: select T1.idx,T1.hotfolder_idx,T1.elem,T1.errtext from HOTFOLDERERROR T1 where T1.hotfolder_idx=? order by T1.idx asc, [idx",
        " PreparedStatement 39: select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.idx=?, [idx",
        " PreparedStatement 40: select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.idx=?, [idx",
        " PreparedStatement 41: select T1.idx,T1.name,T1.creation,T1.scheduleStart,T1.disabled,T1.pool_idx,T1.isCycle,T1.cycleLengthMs from SCHEDULE T1 where T1.idx=?, [idx",
        " PreparedStatement 42: update SCHEDULE set name=?,creation=?,scheduleStart=?,disabled=?,pool_idx=?,isCycle=?,cycleLengthMs=? where idx=?, ["};
    

    public static final String[] noPoolDbs =  {"MessageLog", "ROLE", "accountConnector", "HOTFOLDER", "ROLEOPTION", "MAILGROUP", "SMTPLOGINDATA", "HOTFOLDERERROR"};
    private HashMap<String, PreparedStatement> stMap = new HashMap<>();
    
    public void createPoolPs(Connection conn) throws SQLException
    {
        for( int i = 0; i < PS.length; i++)
        {            
            String line = PS[i];
            boolean skip = false;
            for (int n = 0; n < noPoolDbs.length; n++)
            {
                if (line.toUpperCase().contains(" " + noPoolDbs[n].toUpperCase() + " " ) ||
                        line.toUpperCase().contains(" " + noPoolDbs[n].toUpperCase() + "," ))
                {
                    skip = true;
                    break;
                }
            }
            if (skip)
                continue;
            
            String[] parts = line.split(":");
            int idx =  parts[1].indexOf(", [");
            String qry = parts[1].substring(0, idx).trim();
            String[] keys = new String[0]; 
            if (parts[1].length()  > idx + 3)
            {
                keys = parts[1].substring(idx + 3).trim().split(",");
            }
            getPs(conn, qry, keys);            
        }        
    }
    public PreparedStatement getPs(Connection conn, String qry, String[] keys) throws SQLException
    {
        String key = qry + ", " + Arrays.deepToString(keys);
        PreparedStatement st = stMap.get(key);
        if (st == null)
        {
            if (keys.length > 0)
                st = conn.prepareStatement(qry, keys);
            else
                st = conn.prepareStatement(qry);
            stMap.put(key, st);
            Log.warning("PreparedStatement " + key);
        }
        return st;
    }
    
    public PreparedStatement getPs(Connection conn, String qry) throws SQLException
    {
        return getPs(conn, qry,  new String[0]);
    }
    
    public void rebuild(Connection conn) throws SQLException
    {
        stMap.clear();
        createPoolPs(conn);
    }
    
}

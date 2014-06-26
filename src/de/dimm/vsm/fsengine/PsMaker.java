/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.fsengine;

import de.dimm.vsm.log.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.catacombae.jfuse.util.Log;

/**
 *
 * @author Administrator
 */
public class PsMaker {

    public final static String[] PS = {
        "select max(idx) from MessageLog, [",
        "insert into MESSAGELOG (idx,creation,errLevel,userId,moduleName,messageId,additionText,exceptionName,exceptionText,exceptionStack) values (?,?,?,?,?,?,?,?,?,?), [",
        "select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.idx=?, [idx",
        "select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.pool_idx=? order by T1.idx asc, [idx",
        "select T1.idx,T1.name,T1.creation,T1.rootDir_idx,T1.landingZone from STORAGEPOOL T1 where T1.idx=?, [idx",
        "select T1.idx,T1.type,T1.ip,T1.port,T1.username,T1.pwd,T1.searchbase,T1.searchattribute,T1.mailattribute,T1.domainlist,T1.excludefilter,T1.flags,T1.ldapdomain,T1.ldapfilter,T1.groupIdentifier,T1.ntDomainName from ACCOUNTCONNECTOR T1 where T1.idx=?, [idx",
        "select T1.idx,T1.role_idx,T1.token,T1.flags,T1.optionStr from ROLEOPTION T1 where T1.role_idx=? order by T1.idx asc, [idx",
        "select T1.idx,T1.name,T1.opts,T1.emailText,T1.smtpdata_idx from MAILGROUP T1 where T1.idx=?, [idx",
        "select T1.idx,T1.name,T1.serverip,T1.serverport,T1.smtpfrom,T1.username,T1.userpwd,T1.ssl,T1.tls from SMTPLOGINDATA T1 where T1.idx=?, [idx",
        "select T1.idx,T1.disabled,T1.offsetStartMs,T1.dayNumber,T1.overrideSnapshotEnabled,T1.sched_idx from JOB T1 where T1.sched_idx=? order by T1.idx asc, [idx",
        "select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.sched_idx=? order by T1.idx asc, [idx",
        "select T1.argument,T1.isDir,T1.isFullPath,T1.includeMatches,T1.ignorecase,T1.mode,T1.idx,T1.clinfo_idx from EXCLUDES T1 where T1.clinfo_idx=? order by T1.idx asc, [idx",
        "select T1.idx,T1.clinfo_idx,T1.volumePath,T1.disabled,T1.cdp,T1.snapshot,T1.staylocal,T1.fsType from CLIENTVOLUME T1 where T1.clinfo_idx=? order by T1.idx asc, [idx",
        "select max(idx) from BackupJobResult, [",
        "insert into BACKUPJOBRESULT (idx,startTime,endTime,ok,status,schedule_idx) values (?,?,?,?,?,?), [",
        "select max(idx) from BackupVolumeResult, [",
        "insert into BACKUPVOLUMERESULT (idx,startTime,endTime,ok,status,jobResult_idx,volume_idx,filesChecked,filesTransfered,dataChecked,dataTransfered) values (?,?,?,?,?,?,?,?,?,?,?), [",
        "select T1.idx,T1.fileNode_idx,T1.storageNode_idx,T1.creation from POOLNODEFILELINK T1 where T1.fileNode_idx=? order by T1.idx asc, [idx",
        "select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.parent_idx=? order by T1.idx asc, [idx",
        "select max(idx) from FileSystemElemAttributes, [",
        "insert into FILESYSTEMELEMATTRIBUTES (idx,name,creationDateMs,modificationDateMs,accessDateMs,deleted,ts,posixMode,fsize,xasize,uid,gid,uidName,gidName,xattribute,aclinfo,file_idx,flags) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?), [",
        "update FILESYSTEMELEMNODE set typ=?,filesystem_type=?,pool_idx=?,parent_idx=?,attributes_idx=?,flags=? where idx=?, [",
        "update FILESYSTEMELEMATTRIBUTES set name=?,creationDateMs=?,modificationDateMs=?,accessDateMs=?,deleted=?,ts=?,posixMode=?,fsize=?,xasize=?,uid=?,gid=?,uidName=?,gidName=?,xattribute=?,aclinfo=?,file_idx=?,flags=? where idx=?, [",
        "select max(idx) from FileSystemElemNode, [",
        "insert into FILESYSTEMELEMNODE (idx,typ,filesystem_type,pool_idx,parent_idx,attributes_idx,flags) values (?,?,?,?,?,?,?), [",
        "select max(idx) from PoolNodeFileLink, [",
        "insert into POOLNODEFILELINK (idx,fileNode_idx,storageNode_idx,creation) values (?,?,?,?), [",
        "select T1.idx,T1.pool_idx,T1.storageNode_idx,T1.blockLen,T1.hashvalue from DEDUPHASHBLOCK T1 where T1.idx=?, [idx",
        "select max(idx) from HashBlock, [",
        "insert into HASHBLOCK (idx,fileNode_idx,ts,dedupBlock_idx,hashvalue,reorganize,blockOffset,blockLen) values (?,?,?,?,?,?,?,?), [",
        "select max(idx) from XANode, [",
        "insert into XANODE (idx,fileNode_idx,ts,hashvalue,reorganize,blockOffset,blockLen,dedupBlock_idx,streamInfo) values (?,?,?,?,?,?,?,?,?), [",
        "update BACKUPVOLUMERESULT set startTime=?,endTime=?,ok=?,status=?,jobResult_idx=?,volume_idx=?,filesChecked=?,filesTransfered=?,dataChecked=?,dataTransfered=? where idx=?, [",
        "update BACKUPJOBRESULT set startTime=?,endTime=?,ok=?,status=?,schedule_idx=? where idx=?, [",
        "select T1.idx,T1.startTime,T1.endTime,T1.lastAccess,T1.ok,T1.name,T1.directory_idx,T1.sourceType,T1.sourceIdx,T1.totalSize from ARCHIVEJOB T1 where T1.idx=?, [idx",
        "select T1.idx,T1.startTime,T1.endTime,T1.ok,T1.status,T1.jobResult_idx,T1.volume_idx,T1.filesChecked,T1.filesTransfered,T1.dataChecked,T1.dataTransfered from BACKUPVOLUMERESULT T1 where T1.jobResult_idx=? order by T1.idx asc, [idx",
        "update ROLE set name=?,opts=?,accountmatch=?,license=?,flags=?,user4eyes=?,pwd4eyes=?,accountConnector_idx=? where idx=?, [",
        "select T1.idx,T1.hotfolder_idx,T1.elem,T1.errtext from HOTFOLDERERROR T1 where T1.hotfolder_idx=? order by T1.idx asc, [idx",
        "select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.idx=?, [idx",
        "select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.idx=?, [idx",
        "select T1.idx,T1.name,T1.creation,T1.scheduleStart,T1.disabled,T1.pool_idx,T1.isCycle,T1.cycleLengthMs from SCHEDULE T1 where T1.idx=?, [idx",
        "update SCHEDULE set name=?,creation=?,scheduleStart=?,disabled=?,pool_idx=?,isCycle=?,cycleLengthMs=? where idx=?, [",
        "update FILESYSTEMELEMATTRIBUTES set name=?,creationDateMs=?,modificationDateMs=?,accessDateMs=?,deleted=?,ts=?,posixMode=?,fsize=?,xasize=?,uid=?,gid=?,uidName=?,gidName=?,xattribute=?,aclinfo=?,file_idx=?,flags=? where idx=?, [idx",
        "select max(idx) from FileSystemElemAttributes, [",
        "insert into FILESYSTEMELEMATTRIBUTES (idx,name,creationDateMs,modificationDateMs,accessDateMs,deleted,ts,posixMode,fsize,xasize,uid,gid,uidName,gidName,xattribute,aclinfo,file_idx,flags) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?), [",
        "update FILESYSTEMELEMNODE set typ=?,filesystem_type=?,pool_idx=?,parent_idx=?,attributes_idx=?,flags=? where idx=?, [idx",
        "select max(idx) from FileSystemElemNode, [",
        "insert into FILESYSTEMELEMNODE (idx,typ,filesystem_type,pool_idx,parent_idx,attributes_idx,flags) values (?,?,?,?,?,?,?), [",
        "select max(idx) from PoolNodeFileLink, [",
        "insert into POOLNODEFILELINK (idx,fileNode_idx,storageNode_idx,creation) values (?,?,?,?), [",
        "select max(idx) from HashBlock, [",
        "insert into HASHBLOCK (idx,fileNode_idx,ts,dedupBlock_idx,hashvalue,reorganize,blockOffset,blockLen) values (?,?,?,?,?,?,?,?), [",
        "select max(idx) from XANode, [",
        "insert into XANODE (idx,fileNode_idx,ts,hashvalue,reorganize,blockOffset,blockLen,dedupBlock_idx,streamInfo) values (?,?,?,?,?,?,?,?,?), [",
        "select max(idx) from BackupVolumeResult, [",
        "update BACKUPVOLUMERESULT set startTime=?,endTime=?,ok=?,status=?,jobResult_idx=?,volume_idx=?,filesChecked=?,filesTransfered=?,dataChecked=?,dataTransfered=? where idx=?, [idx",
        "update BACKUPJOBRESULT set startTime=?,endTime=?,ok=?,status=?,schedule_idx=? where idx=?, [idx",
        "insert into BACKUPVOLUMERESULT (idx,startTime,endTime,ok,status,jobResult_idx,volume_idx,filesChecked,filesTransfered,dataChecked,dataTransfered) values (?,?,?,?,?,?,?,?,?,?,?), [",
        "select T1.idx,T1.fileNode_idx,T1.storageNode_idx,T1.creation from POOLNODEFILELINK T1 where T1.fileNode_idx=? order by T1.idx asc;PoolNodeFileLinkfileNode_idx2",
        "select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.idx=?;ABSTRACTSTORAGENODE2",
        "select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.idx=?;FILESYSTEMELEMNODE1",
        "select max(idx) from FileSystemElemAttributes, []",
        "insert into FILESYSTEMELEMATTRIBUTES (idx,name,creationDateMs,modificationDateMs,accessDateMs,deleted,ts,posixMode,fsize,xasize,uid,gid,uidName,gidName,xattribute,aclinfo,file_idx,flags) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?), [",
        "update FILESYSTEMELEMNODE set typ=?,filesystem_type=?,pool_idx=?,parent_idx=?,attributes_idx=?,flags=? where idx=?, [idx",
        "select T1.idx,T1.typ,T1.filesystem_type,T1.pool_idx,T1.parent_idx,T2.idx,T2.name,T2.creationDateMs,T2.modificationDateMs,T2.accessDateMs,T2.deleted,T2.ts,T2.posixMode,T2.fsize,T2.xasize,T2.uid,T2.gid,T2.uidName,T2.gidName,T2.xattribute,T2.aclinfo,T2.file_idx,T2.flags,T1.flags from FILESYSTEMELEMNODE T1,FILESYSTEMELEMATTRIBUTES T2 where T1.attributes_idx=T2.idx and T1.parent_idx=? order by T1.idx asc;FileSystemElemNodeparent_idx2",
        "select max(idx) from FileSystemElemNode, [",
        "insert into FILESYSTEMELEMNODE (idx,typ,filesystem_type,pool_idx,parent_idx,attributes_idx,flags) values (?,?,?,?,?,?,?), [",
        "select max(idx) from PoolNodeFileLink, [",
        "insert into POOLNODEFILELINK (idx,fileNode_idx,storageNode_idx,creation) values (?,?,?,?), [",
        "select T1.idx,T1.pool_idx,T1.storageNode_idx,T1.blockLen,T1.hashvalue from DEDUPHASHBLOCK T1 where T1.idx=?;DEDUPHASHBLOCK1",
        "select max(idx) from HashBlock, [",
        "insert into HASHBLOCK (idx,fileNode_idx,ts,dedupBlock_idx,hashvalue,reorganize,blockOffset,blockLen) values (?,?,?,?,?,?,?,?), [",
        "select max(idx) from XANode, [",
        "insert into XANODE (idx,fileNode_idx,ts,hashvalue,reorganize,blockOffset,blockLen,dedupBlock_idx,streamInfo) values (?,?,?,?,?,?,?,?,?), [",
        "update FILESYSTEMELEMATTRIBUTES set name=?,creationDateMs=?,modificationDateMs=?,accessDateMs=?,deleted=?,ts=?,posixMode=?,fsize=?,xasize=?,uid=?,gid=?,uidName=?,gidName=?,xattribute=?,aclinfo=?,file_idx=?,flags=? where idx=?, [idx]",
        "select T1.idx,T1.name,T1.creation,T1.rootDir_idx,T1.landingZone from STORAGEPOOL T1 where T1.idx=?;STORAGEPOOL1",
        "select T1.mountPoint,T1.idx,T1.name,T1.creation,T1.nodeMode,T1.nodeType,T1.pool_idx,T1.cloneNode_idx from ABSTRACTSTORAGENODE T1 where T1.pool_idx=? order by T1.idx asc;AbstractStorageNodepool_idx2",
        "select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.idx=?;CLIENTINFO1",
        "select T1.idx,T1.name,T1.creation,T1.scheduleStart,T1.disabled,T1.pool_idx,T1.isCycle,T1.cycleLengthMs from SCHEDULE T1 where T1.idx=?;SCHEDULE2",
        "select T1.argument,T1.isDir,T1.isFullPath,T1.includeMatches,T1.ignorecase,T1.mode,T1.idx,T1.clinfo_idx from EXCLUDES T1 where T1.clinfo_idx=? order by T1.idx asc;Excludesclinfo_idx1",
        "select T1.idx,T1.clinfo_idx,T1.volumePath,T1.disabled,T1.cdp,T1.snapshot,T1.staylocal,T1.fsType from CLIENTVOLUME T1 where T1.clinfo_idx=? order by T1.idx asc;ClientVolumeclinfo_idx1",
        "select T1.idx,T1.disabled,T1.offsetStartMs,T1.dayNumber,T1.overrideSnapshotEnabled,T1.sched_idx from JOB T1 where T1.sched_idx=? order by T1.idx asc;Jobsched_idx1",
        "select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.sched_idx=? order by T1.idx asc;ClientInfosched_idx1",
        "select max(idx) from DedupHashBlock",
        "insert into DEDUPHASHBLOCK (idx,pool_idx,storageNode_idx,blockLen,hashvalue) values (?,?,?,?,?)", 
        "select T1.idx,T1.fileNode_idx,T1.ts,T1.dedupBlock_idx,T1.hashvalue,T1.reorganize,T1.blockOffset,T1.blockLen from HASHBLOCK T1 where T1.fileNode_idx=? order by T1.idx asc;HashBlockfileNode_idx1",
        "delete from POOLNODEFILELINK where idx=?",
        "delete from FILESYSTEMELEMATTRIBUTES where idx=?",
        "delete from FILESYSTEMELEMNODE where idx=?",
        "select T1.idx,T1.fileNode_idx,T1.ts,T1.hashvalue,T1.reorganize,T1.blockOffset,T1.blockLen,T1.dedupBlock_idx,T1.streamInfo from XANODE T1 where T1.fileNode_idx=? order by T1.idx asc;XANodefileNode_idx1",
        "select T1.idx,T1.name,T1.creation,T1.scheduleStart,T1.disabled,T1.pool_idx,T1.isCycle,T1.cycleLengthMs from SCHEDULE T1 where T1.idx=?;SCHEDULE1",
        "select T1.idx,T1.startTime,T1.endTime,T1.ok,T1.status,T1.schedule_idx from BACKUPJOBRESULT T1 where T1.idx=?;BACKUPJOBRESULT1",
        "select T1.idx,T1.ip,T1.port,T1.disabled,T1.compression,T1.onlyNewer,T1.encryption,T1.sched_idx from CLIENTINFO T1 where T1.idx=?;CLIENTINFO1",
        "select T1.idx,T1.name,T1.creationDateMs,T1.modificationDateMs,T1.accessDateMs,T1.deleted,T1.ts,T1.posixMode,T1.fsize,T1.xasize,T1.uid,T1.gid,T1.uidName,T1.gidName,T1.xattribute,T1.aclinfo,T1.file_idx,T1.flags from FILESYSTEMELEMATTRIBUTES T1 where T1.file_idx=? order by T1.idx asc;FileSystemElemAttributesfile_idx1",
            

        
                
    };
    static final Map<String,Integer> psCntMap = new HashMap<>();
    static {
        psCntMap.put("JOBSCHED_IDX", Integer.valueOf(3));
        psCntMap.put("CLIENTINFOSCHED_IDX", Integer.valueOf(3));
        psCntMap.put("CLIENTVOLUMECLINFO_IDX", Integer.valueOf(3));
        psCntMap.put("HASHBLOCKFILENODE_IDX", Integer.valueOf(3));
        psCntMap.put("FILESYSTEMELEMNODE", Integer.valueOf(50));
    }    
    
    public static final String[] noPoolDbs = {"MessageLog", "ROLE", "accountConnector", "HOTFOLDER", "ROLEOPTION", "MAILGROUP", "SMTPLOGINDATA", "HOTFOLDERERROR"};
    private HashMap<String, PreparedStatement> stMap = new HashMap<>();
    private int MAX_ID_PS_CNT = 10;
    private boolean skipMsg =false;

    public void createPoolPs( Connection conn ) throws SQLException {
        skipMsg = true;
        try
        {
            for (int i = 0; i < PS.length; i++) {
                String line = PS[i];
                boolean skip = false;
                for (int n = 0; n < noPoolDbs.length; n++) {
                    if (line.toUpperCase().contains(" " + noPoolDbs[n].toUpperCase() + " ")
                            || line.toUpperCase().contains(" " + noPoolDbs[n].toUpperCase() + ",")) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }





                String[] qryComment = line.split(";");
                String qry = qryComment[0];

                int idx = qry.indexOf(", [");            
                if (idx > 0) {
                    qry = qry.substring(0, idx).trim();                
                }

                if (qryComment.length == 2) {
                    String psIdentifier = qryComment[1];
                    int nridx = psIdentifier.length() - 1;
                    while (Character.isDigit(psIdentifier.charAt(nridx)))
                    {
                        nridx--;
                    }
                    psIdentifier = psIdentifier.substring(0, nridx + 1);
                    int max_id_ps_cnt = getMaxIdPsCnt(psIdentifier) + 1;
                    for (int n = 1; n < max_id_ps_cnt; n++)
                    {
                        getPs(conn, qry,  psIdentifier + n);
                    }

                }
                else {
                    getPs(conn, qry);
                }
            }
        }
        catch (Exception ex)
        {
            LogManager.err_db("Fehler bei createPoolPs", ex);            
        }
        finally{
            skipMsg = false;
        }
    }


    public PreparedStatement getPs( Connection conn, String qry ) throws SQLException {
        return getPs(conn, qry, "");
    }

    public PreparedStatement getPs( Connection conn, String qry, String identifier ) throws SQLException {
        String key = qry + ";" + identifier;
        PreparedStatement st = stMap.get(key.toUpperCase());
        if (st == null) {
            if (qry.toLowerCase().startsWith("select "))
            {
                // We read w/o update or write
                st = conn.prepareStatement(qry, ResultSet.TYPE_FORWARD_ONLY,  ResultSet.CONCUR_READ_ONLY );
            }
            else
            {
                st = conn.prepareStatement(qry);
            }
            
            stMap.put(key.toUpperCase(), st);
            if (!skipMsg)
            {
                
                Log.warning("PreparedStatement " + key);
                
                // THIS SEEMST TO HELP AGAINST SPURIOUS EXCEPTIONS DURING RUNTIME
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex) {
                    LogManager.err_db("Fehler bei getPs", ex);      
                }
            }
        }
        return st;
    }


    public void rebuild( Connection conn ) throws SQLException {
        stMap.clear();
        createPoolPs(conn);
    }

    
    private int getMaxIdPsCnt( String psIdentifier ) {
        Integer val = psCntMap.get(psIdentifier.toUpperCase());
        if (val != null)
            return val.intValue();
        
        return MAX_ID_PS_CNT;
    }
}

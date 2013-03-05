/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.jobs.JobEntry;
import de.dimm.vsm.net.LogQuery;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.ScheduleStatusEntry;
import de.dimm.vsm.net.SearchEntry;
import de.dimm.vsm.net.SearchStatus;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.MessageLog;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.tasks.TaskEntry;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public interface GuiServerApi
{
    // WARNING: THIS HAS TO FIT TO RestoreContext ON SERVER!!!!

    public static final int RF_RECURSIVE = 0x0001;
    public static final int RF_FULLPATH = 0x0002;
    public static final int RF_SKIPHOTFOLDER_TIMSTAMPDIR = 0x0004;
    public static final int RF_INCREMENTAL = 0x0008;

    public static final int RF_COMPRESSION = 0x0010;
    public static final int RF_ENCRYPTION = 0x0020;

    boolean startBackup( Schedule sched, User user ) throws Exception;
    public boolean abortBackup( final Schedule sched );
    StoragePoolWrapper mountVolume( String agentIp, int agentPort, StoragePool pool, Date timestamp, String subPath, User user, String drive ) throws IOException;
    public StoragePoolWrapper mountVolume( final String agentIp, final int agentPort, final StoragePoolWrapper poolWrapper, final String drive ) throws IOException;
    boolean unmountVolume( StoragePoolWrapper wrapper );
    boolean unmountAllVolumes();
    StoragePoolWrapper getMounted( String agentIp, int agentPort, StoragePool pool );
    boolean remountVolume( StoragePoolWrapper wrapper );

    StoragePoolWrapper openPoolView( StoragePool pool, Date timestamp, String subPath, User user );
    StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, String subPath, User user );
    StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, boolean showDeleted, String subPath, User user );
    StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, FileSystemElemNode node, User user );
    StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly,  boolean showDeleted, FileSystemElemNode node, User user );
    List<RemoteFSElem> listDir( StoragePoolWrapper wrapper, RemoteFSElem path ) throws SQLException;
    void closePoolView( StoragePoolWrapper wrapper );
    boolean removeFSElem( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PoolReadOnlyException;
    boolean undeleteFSElem( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PoolReadOnlyException;
    boolean deleteFSElem( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PoolReadOnlyException;
    boolean restoreFSElem( IWrapper wrapper, RemoteFSElem path, String targetIP, int targetPort, String targetPath, int flags, User user ) throws SQLException, PoolReadOnlyException, IOException;
    boolean restoreFSElems( IWrapper wrapper, List<RemoteFSElem> path, String targetIP, int targetPort, String targetPath, int flags, User user ) throws SQLException, PoolReadOnlyException, IOException;

    public FileSystemElemNode createFileSystemElemNode( StoragePool pool, String path, String type )throws IOException,  PoolReadOnlyException, PathResolveException;
    public FileSystemElemNode createFileSystemElemNode( StoragePoolWrapper wrapper, String path, String type )throws IOException,  PoolReadOnlyException, PathResolveException;

    public List<ScheduleStatusEntry> listSchedulerStats();

    public Properties getAgentProperties( String ip, int port, boolean withMsg );

    public SearchWrapper search( StoragePool pool, ArrayList<SearchEntry> slist );
    public SearchWrapper search( StoragePool pool, ArrayList<SearchEntry> slist, int max );
    public SearchWrapper searchJob( StoragePool pool, ArrayList<SearchEntry> slist, int max );
    public List<RemoteFSElem> getSearchResult( SearchWrapper wrapper, int start, int limit );
    public List<ArchiveJob> getJobSearchResult( SearchWrapper wrapper, int start, int limit );
    public SearchStatus getSearchStatus( SearchWrapper wrapper);
    public void updateReadIndex( StoragePool pool );

//    boolean restoreFSElem( IWrapper wrapper, RemoteFSElem path, String targetIP, int targetPort, String targetPath, int flags, User user ) throws SQLException, PoolReadOnlyException, IOException;
//    boolean restoreFSElems( IWrapper wrapper, List<RemoteFSElem> path, String targetIP, int targetPort, String targetPath, int flags, User user ) throws SQLException, PoolReadOnlyException, IOException;
    public List<RemoteFSElem> listSearchDir( SearchWrapper wrapper, RemoteFSElem path ) throws SQLException;
    public void closeSearch( SearchWrapper wrapper);

    public StoragePoolWrapper getMounted( String ip, int port, SearchWrapper searchWrapper );
    public StoragePoolWrapper mountVolume( String ip, int port, SearchWrapper searchWrapper, User object, String drive );

    public void reSearch( SearchWrapper searchWrapper, ArrayList<SearchEntry> slist );

    public JobEntry[] listJobs(User user);

    public void moveNode( AbstractStorageNode node, AbstractStorageNode toNode, User user ) throws SQLException;

    public void emptyNode( AbstractStorageNode node, User user ) throws SQLException;

    public TaskEntry[] listTasks();

    public MessageLog[] listLogs( int cnt, long offsetIdx, LogQuery lq );

    public MessageLog[] listLogsSince( long idx, LogQuery lq );

    public long getLogCounter();

    //public InputStream openStream( StoragePoolWrapper wrapper, RemoteFSElem path );
    public InputStream openStream( IWrapper wrapper, RemoteFSElem path );
    public String resolvePath(  IWrapper wrapper, RemoteFSElem path ) throws SQLException, PathResolveException;
    //public String resolvePath(  StoragePoolWrapper wrapper, RemoteFSElem path )throws SQLException, PathResolveException;

    public boolean importMMArchiv(HotFolder node, long fromIdx, long tillIdx, boolean withOldJobs, User user) throws Exception;

    public boolean restoreJob( SearchWrapper searchWrapper, ArchiveJob job, String ip, int port, String path, int rflags, User user ) throws SQLException, PoolReadOnlyException, IOException;
    public boolean removeJob( SearchWrapper searchWrapper, ArchiveJob job) throws SQLException, PoolReadOnlyException;

    public void syncNode( AbstractStorageNode t, AbstractStorageNode cloneNode, User user  ) throws SQLException, IOException;

    public boolean isBusyNode( AbstractStorageNode node );

    public boolean initNode( AbstractStorageNode node, User user );
    
    public void initCheck( User user, String checkName, Object arg, Object optArg);
    public List<String> getCheckNames(Class<?> clazz);
    public List<MountEntry> getAllMountEntries();
    public List<MountEntry> getMountedMountEntries();
    public void unMountEntry( MountEntry mountEntry);
    public StoragePoolWrapper mountEntry( User user,  MountEntry mountEntry) throws IOException;
    
}

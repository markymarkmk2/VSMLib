/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Administrator
 */
public interface StoragePoolHandlerInterface {

    String getVersion();

    RemoteFSElem create_fse_node( StoragePoolWrapper pool, String fileName, String type ) throws IOException, PoolReadOnlyException, PathResolveException;

    RemoteFSElem resolve_node( StoragePoolWrapper pool, String path ) throws SQLException;

    long getTotalBlocks(StoragePoolWrapper pool );

    long getUsedBlocks(StoragePoolWrapper pool );

    int getBlockSize(StoragePoolWrapper pool );

    void mkdir( StoragePoolWrapper pool, String pathName ) throws IOException, PoolReadOnlyException, PathResolveException;

    public long open_fh( StoragePoolWrapper pool, long nodeIdx, boolean forWrite ) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    public long open_stream( StoragePoolWrapper pool, long nodeIdx, boolean forWrite ) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    
    long create_fh( StoragePoolWrapper pool, String vsmPath, String type) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    long create_stream( StoragePoolWrapper pool, String vsmPath, String type, int streamInfo) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;

    String getName(StoragePoolWrapper pool);

    boolean delete_fse_node( StoragePoolWrapper pool, String path ) throws PoolReadOnlyException, SQLException;
    
    boolean delete_fse_node( StoragePoolWrapper pool, long idx ) throws PoolReadOnlyException, SQLException;

    List<RemoteFSElem> get_child_nodes( StoragePoolWrapper pool, RemoteFSElem handler ) throws SQLException;

    void move_fse_node( StoragePoolWrapper pool, String from, String to ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;

    public void set_ms_times( StoragePoolWrapper pool, long idx, long toJavaTime, long toJavaTime0, long toJavaTime1 ) throws IOException, SQLException, PoolReadOnlyException;

    public boolean exists( StoragePoolWrapper pool, RemoteFSElem fseNode ) throws IOException;

    public boolean isReadOnly( StoragePoolWrapper pool, long idx ) throws IOException, SQLException;

    public void force( StoragePoolWrapper pool, long idx, boolean b ) throws IOException;

    public byte[] read( StoragePoolWrapper pool, long idx, int length, long offset ) throws IOException;

    public void create( StoragePoolWrapper pool, long idx ) throws IOException, PoolReadOnlyException;

    public void truncateFile( StoragePoolWrapper pool, long idx, long size ) throws IOException, SQLException, PoolReadOnlyException;

    public void close_fh( StoragePoolWrapper pool, long idx ) throws IOException;

    public void writeFile( StoragePoolWrapper pool, long idx, byte[] b, int length, long offset ) throws IOException, SQLException, PoolReadOnlyException;

    void set_attribute( StoragePoolWrapper pool, RemoteFSElem fseNode, String string, Integer valueOf ) throws IOException, SQLException, PoolReadOnlyException;

    String read_symlink( StoragePoolWrapper pool, RemoteFSElem fseNode );

    void create_symlink( StoragePoolWrapper pool, RemoteFSElem fseNode, String to ) throws IOException, PoolReadOnlyException;

    void truncate( StoragePoolWrapper pool, RemoteFSElem fseNode, long size ) throws IOException, PoolReadOnlyException;

    void set_last_modified( StoragePoolWrapper pool, RemoteFSElem fseNode, long l ) throws IOException, SQLException, PoolReadOnlyException;
    void set_ms_filetimes( StoragePoolWrapper pool, RemoteFSElem fseNode, long ctime , long atime , long mtime ) throws IOException, SQLException, PoolReadOnlyException;

    String get_xattribute( StoragePoolWrapper pool, RemoteFSElem fseNode, String name ) throws SQLException ;

    void set_last_accessed( StoragePoolWrapper pool, RemoteFSElem fseNode, long l ) throws IOException, SQLException, PoolReadOnlyException;

    List<String> list_xattributes( StoragePoolWrapper pool, RemoteFSElem fseNode );

    void add_xattribute( StoragePoolWrapper pool, RemoteFSElem fseNode, String name, String valStr );

    void set_mode( StoragePoolWrapper pool, RemoteFSElem fseNode, int mode ) throws IOException, SQLException, PoolReadOnlyException;

    void set_owner_id( StoragePoolWrapper pool, RemoteFSElem fseNode, int uid ) throws IOException, SQLException, PoolReadOnlyException;

    void set_group_id( StoragePoolWrapper pool, RemoteFSElem fseNode, int gid ) throws IOException, SQLException, PoolReadOnlyException;

     boolean isReadOnly(StoragePoolWrapper pool);

    public long length( StoragePoolWrapper poolWrapper, long idx );

}

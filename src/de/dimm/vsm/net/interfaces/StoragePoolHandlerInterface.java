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
 * 
 * !!!!! WICHTIG !!!!!! Hesian unterstützt keine Polymorphen methoden, alles muss unterschiedliche Methodennamen haben !!!!!!!!!!!!!!
 */
public interface StoragePoolHandlerInterface {

    String getVersion();

    RemoteFSElem create_fse_node( IWrapper pool, String fileName, String type ) throws IOException, PoolReadOnlyException, PathResolveException;

    RemoteFSElem resolve_node( IWrapper pool, String path ) throws SQLException;

    long getTotalBlocks(IWrapper pool );

    long getUsedBlocks(IWrapper pool );

    int getBlockSize(IWrapper pool );

    void mkdir( IWrapper pool, String pathName ) throws IOException, PoolReadOnlyException, PathResolveException;

    public long open_fh( IWrapper pool, long nodeIdx, boolean forWrite ) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    public long open_stream( IWrapper pool, long nodeIdx, boolean forWrite ) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    
    long create_fh( StoragePoolWrapper pool, String vsmPath, String type) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;
    long create_stream( StoragePoolWrapper pool, String vsmPath, String type, int streamInfo) throws IOException, PoolReadOnlyException, SQLException, PathResolveException;

    String getName(IWrapper pool);

    boolean delete_fse_node_path( IWrapper pool, String path ) throws IOException, PoolReadOnlyException, SQLException;
    
    boolean delete_fse_node_idx( IWrapper pool, long idx ) throws IOException, PoolReadOnlyException, SQLException;

    List<RemoteFSElem> get_child_nodes( IWrapper pool, RemoteFSElem handler ) throws SQLException;

    void move_fse_node( IWrapper pool, String from, String to ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;
    void move_fse_node_idx( IWrapper pool, long idx, String to ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;

    public void set_ms_times( IWrapper pool, long idx, long toJavaTime, long toJavaTime0, long toJavaTime1 ) throws IOException, SQLException, PoolReadOnlyException;

    public boolean exists( IWrapper pool, RemoteFSElem fseNode ) throws IOException;

    public boolean isReadOnly( IWrapper pool, long idx ) throws IOException, SQLException;

    public void force( IWrapper pool, long idx, boolean b ) throws IOException;

    public byte[] read( IWrapper pool, long idx, int length, long offset ) throws IOException;

    public void create( IWrapper pool, long idx ) throws IOException, PoolReadOnlyException;

    public void truncateFile( IWrapper pool, long idx, long size ) throws IOException, SQLException, PoolReadOnlyException;

    public void close_fh( IWrapper pool, long idx ) throws IOException;

    public void writeFile ( IWrapper pool, long idx, byte[] data, int length, long offset ) throws IOException, SQLException, PoolReadOnlyException;
    public void writeBlock( IWrapper pool, long idx, String hash, byte[] b, int length, long offset ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;
    public boolean checkBlock( IWrapper pool, String hash ) throws IOException, SQLException;

    void set_attribute( IWrapper pool, RemoteFSElem fseNode, String string, Integer valueOf ) throws IOException, SQLException, PoolReadOnlyException;

    String read_symlink( IWrapper pool, RemoteFSElem fseNode );

    void create_symlink( IWrapper pool, RemoteFSElem fseNode, String to ) throws IOException, PoolReadOnlyException;

    void truncate( IWrapper pool, RemoteFSElem fseNode, long size ) throws IOException, PoolReadOnlyException;

    void set_last_modified( IWrapper pool, RemoteFSElem fseNode, long l ) throws IOException, SQLException, PoolReadOnlyException;
    void set_ms_filetimes( IWrapper pool, RemoteFSElem fseNode, long ctime , long atime , long mtime ) throws IOException, SQLException, PoolReadOnlyException;

    String get_xattribute( IWrapper pool, RemoteFSElem fseNode, String name ) throws SQLException ;

    void set_last_accessed( IWrapper pool, RemoteFSElem fseNode, long l ) throws IOException, SQLException, PoolReadOnlyException;

    List<String> list_xattributes( IWrapper pool, RemoteFSElem fseNode );

    void add_xattribute( IWrapper pool, RemoteFSElem fseNode, String name, String valStr );

    void set_mode( IWrapper pool, RemoteFSElem fseNode, int mode ) throws IOException, SQLException, PoolReadOnlyException;

    void set_owner_id( IWrapper pool, RemoteFSElem fseNode, int uid ) throws IOException, SQLException, PoolReadOnlyException;

    void set_group_id( IWrapper pool, RemoteFSElem fseNode, int gid ) throws IOException, SQLException, PoolReadOnlyException;

     boolean isReadOnly(IWrapper pool);

    public long length( IWrapper poolWrapper, long idx );

    public void updateAttributes( IWrapper poolWrapper, long fileNo, RemoteFSElem elem )throws IOException, SQLException, PoolReadOnlyException;

}

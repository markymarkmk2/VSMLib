/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import de.dimm.vsm.net.RemoteFSElem;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Administrator
 */
public interface RemoteFSApi {

    RemoteFSElem create_fse_node(  String fileName, String type ) throws IOException, PoolReadOnlyException, PathResolveException;

    RemoteFSElem resolve_node( String path ) throws SQLException;

    long getTotalBlocks( );

    long getUsedBlocks();

    int getBlockSize();

    void mkdir( String pathName ) throws IOException, PoolReadOnlyException, PathResolveException;

    String getName();

    boolean remove_fse_node(  String path ) throws IOException, PoolReadOnlyException, SQLException;
    boolean remove_fse_node_idx(  long idx ) throws IOException, PoolReadOnlyException, SQLException;

    List<RemoteFSElem> get_child_nodes( RemoteFSElem handler ) throws SQLException;

    void move_fse_node(  String from, String to ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;
    void move_fse_node_idx(  long from, String to ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;

//    void set_ms_times( RemoteFSElem fseNode, long toJavaTime, long toJavaTime0, long toJavaTime1 ) throws IOException, SQLException, PoolReadOnlyException;

//    long open_file_handle_no( String path, boolean create ) throws IOException;

    long open_file_handle_no( RemoteFSElem node, boolean create ) throws SQLException, IOException, PoolReadOnlyException, PathResolveException;

    boolean exists( RemoteFSElem fseNode )throws IOException;

    boolean isReadOnly(long idx )throws IOException, SQLException;

    void set_ms_times( long idx, long ctime, long atime, long mtime ) throws IOException, SQLException, PoolReadOnlyException;

    void force( long idx, boolean b )throws IOException;

    byte[] read( long idx, int length, long offset )throws IOException;

    void close( long idx )throws IOException;

    void create( long idx )throws IOException, PoolReadOnlyException;

    void truncateFile( long idx, long size )throws IOException, SQLException, PoolReadOnlyException;

    void writeFile( long idx, byte[] b, int length, long offset )throws IOException, SQLException, PoolReadOnlyException;
    void writeBlock( long idx, String hash, byte[] b, int length, long offset ) throws IOException, SQLException, PoolReadOnlyException, PathResolveException;

   
}

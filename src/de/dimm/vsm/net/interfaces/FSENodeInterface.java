/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

;
import de.dimm.vsm.Exceptions.DBConnException;
import de.dimm.vsm.records.FileSystemElemNode;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public interface FSENodeInterface
{
    void add_xattribute( FileSystemElemNode node, String name, String valStr );

    void create_symlink( FileSystemElemNode node, String to ) throws IOException;

    boolean exists(FileSystemElemNode node);

    long getId(FileSystemElemNode node);

    FileSystemElemNode getNode(FileSystemElemNode node);

    Object get_GUID(FileSystemElemNode node);

    long get_creation_date(FileSystemElemNode node);

    int get_group_id(FileSystemElemNode node);

    long get_last_accessed(FileSystemElemNode node);

    long get_last_modified(FileSystemElemNode node);

    int get_mode(FileSystemElemNode node);

    String get_name(FileSystemElemNode node);

    int get_owner_id(FileSystemElemNode node);

    String get_path(FileSystemElemNode node);

    long get_size(FileSystemElemNode node);

    long get_timestamp(FileSystemElemNode node);

    long get_unix_access_date(FileSystemElemNode node);

    long get_unix_creation_date(FileSystemElemNode node);

    long get_unix_modification_date(FileSystemElemNode node);

    String get_xattribute( String name );

    boolean isDirectory(FileSystemElemNode node);

    boolean isFile(FileSystemElemNode node);

    boolean isHardLink(FileSystemElemNode node);

    boolean isSymbolicLink(FileSystemElemNode node);

    ArrayList<String> list_xattributes(FileSystemElemNode node);

    FileHandle open_file_handle( FileSystemElemNode node, boolean create ) throws IOException, SQLException, DBConnException;

    String read_symlink(FileSystemElemNode node) throws IOException;

    void rename_To( FileSystemElemNode node, String string ) throws IOException, SQLException, DBConnException;

    void set_attribute( FileSystemElemNode node, String string, Integer valueOf );

    void set_creation_date( FileSystemElemNode node, long l ) throws SQLException, DBConnException;

    void set_group_id( FileSystemElemNode node, int gid ) throws IOException, SQLException, DBConnException;

    void set_last_accessed( FileSystemElemNode node, long l ) throws SQLException, DBConnException;

    void set_last_modified( FileSystemElemNode node, long l ) throws SQLException, DBConnException;

    void set_mode( FileSystemElemNode node, int mode ) throws IOException, SQLException, DBConnException;

    void set_ms_times( FileSystemElemNode node, long c, long a, long m ) throws SQLException, DBConnException;

    void set_owner_id( FileSystemElemNode node, int uid ) throws IOException, SQLException, DBConnException;

    void truncate( FileSystemElemNode node, long size ) throws IOException, SQLException, DBConnException;



}

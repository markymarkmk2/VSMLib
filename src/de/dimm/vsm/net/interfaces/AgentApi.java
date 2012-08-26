/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.AttributeList;
import de.dimm.vsm.net.CdpTicket;
import de.dimm.vsm.net.CompEncDataResult;
import de.dimm.vsm.net.HashDataResult;
import de.dimm.vsm.net.InvalidCdpTicketException;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.RemoteFSElemWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.records.Excludes;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public interface AgentApi
{
    public static final String OP_OS = "os.name";
    public static final String OP_OS_VER = "os.version";
    public static final String OP_OS_ARCH = "os.arch";
    public static final String OP_AG_VER = "ag.version";

    public static final String SOP_OS = "os.name";
    public static final String SOP_OS_VER = "os.version";
    public static final String SOP_SV_VER = "sv.version";
    public static final String SOP_IP = "sv.ip";
    public static final String SOP_PORT = "sv.port";


    public static final String OP_AG_ENC = "ag.encryption";
    public static final String OP_AG_COMP = "ag.compression";
    public static final String OP_CDP_EXCLUDES = "ag.cdpexcludes";


    public static final int FL_RDONLY = 0;
    public static final int FL_RDWR = 1;
    public static final int FL_TRUNC = 2;
    public static final int FL_CREATE = 4;

    public static final String OP_FORCE_RSRC = "OP_FORCE_RSRC";
    
    ArrayList<RemoteFSElem> list_dir( RemoteFSElem dir, boolean lazyAclInfo );
    String readAclInfo( RemoteFSElem dir );
    ArrayList<RemoteFSElem> list_roots( );
    boolean create_dir( RemoteFSElem dir ) throws IOException;

    RemoteFSElemWrapper open_data(RemoteFSElem file, int flags ) throws IOException;
    boolean close_data(RemoteFSElemWrapper file) throws IOException;
    
    AttributeList get_attributes( RemoteFSElem file );
    /*
    InputStream open_is_data( RemoteFSElem file) throws IOException;
    InputStream open_is_attribute( RemoteFSElem file, String attr_name) throws IOException;
    InputStream open_is_acls( RemoteFSElem file) throws IOException;
    OutputStream open_os_data( RemoteFSElem file) throws IOException;
    OutputStream open_os_attribute( RemoteFSElem file, String attr_name) throws IOException;
    OutputStream open_os_acls( RemoteFSElem file) throws IOException;*/
    Properties get_properties();
    void set_options(Properties p);
    String read_hash( RemoteFSElemWrapper file, long pos, int bsize, String alg ) throws IOException;
    String read_hash_complete( RemoteFSElem file, String alg ) throws IOException;
    byte[] read( RemoteFSElemWrapper file, long pos, int bsize) throws IOException;
    CompEncDataResult readEncryptedCompressed( RemoteFSElemWrapper file, long pos, int bsize, boolean enc, boolean comp) throws IOException;
    HashDataResult read_and_hash( RemoteFSElemWrapper file, long pos, int bsize) throws IOException;
    CompEncDataResult read_and_hash_encrypted_compressed( RemoteFSElemWrapper file, long pos, int bsize, boolean enc, boolean comp) throws IOException;
    int write( RemoteFSElemWrapper file, byte[] data, long pos);
    int writeEncryptedCompressed( RemoteFSElemWrapper file, byte[] data, long pos, int encLen, boolean enc, boolean comp);
    byte[] read_complete( RemoteFSElem file) throws IOException;
    SnapshotHandle create_snapshot( RemoteFSElem file);
    boolean release_snapshot( SnapshotHandle handle );

    CdpTicket init_cdp( InetAddress addr, int port, boolean ssl, boolean tcp, RemoteFSElem file, long poolIdx, long schedIdx, long clientInfoIdx, long clientVolumeIdx ) throws IOException;
    boolean check_cdp( CdpTicket ticket ) throws InvalidCdpTicketException;
    boolean pause_cdp( CdpTicket ticket  ) throws InvalidCdpTicketException;
    boolean stop_cdp( CdpTicket ticket ) throws InvalidCdpTicketException;
    List<CdpTicket> getCdpTickets();
    void set_cdp_excludes(  CdpTicket ticket, List<Excludes> exclList ) throws InvalidCdpTicketException;

    boolean mountVSMFS( InetAddress addr, int port, StoragePoolWrapper pool, /*Date timestamp, String subPath, User user,*/ String drive);
    boolean unmountVSMFS( InetAddress addr, int port, StoragePoolWrapper pool);
    boolean isMountedVSMFS( InetAddress addr, int port, StoragePoolWrapper pool);

    public RemoteFSElemWrapper open_stream_data( RemoteFSElem remoteFSElem, int FL_RDONLY ) throws IOException;


    byte[] fetch_null_data( int bsize);

    boolean set_filetimes_named( RemoteFSElem elem );
    boolean set_filetimes( RemoteFSElemWrapper elem );
    boolean set_attributes( RemoteFSElemWrapper elem );

    RemoteFSElem check_hotfolder( RemoteFSElem mountPath, long getSetttleTime_s, final String filter, boolean onlyFiles, boolean onlyDirs, int itemIdx );

    public void deleteDir( RemoteFSElem path, boolean b ) throws IOException;

    public boolean create_symlink( RemoteFSElem remoteFSElem );

    public boolean create_other( RemoteFSElem remoteFSElem );


}

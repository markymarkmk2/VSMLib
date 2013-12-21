/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/**
 *
 * @author mw
 */
public interface FileHandle
{   
  
    public void force( boolean b ) throws IOException;

    public int read( byte[] b, int length, long offset ) throws IOException;

    public byte[] read( int length, long offset ) throws IOException;
    
    public void close() throws IOException;

    public void create() throws IOException, PoolReadOnlyException;

    public void truncateFile( long size ) throws IOException, SQLException, PoolReadOnlyException;
    
    public void writeFile( byte[] b, int length, long offset ) throws IOException, SQLException, PoolReadOnlyException;
    public void writeBlock( String hashValue, byte[] data, int length, long offset ) throws IOException, PathResolveException, PoolReadOnlyException, UnsupportedEncodingException, SQLException;    

    public boolean delete() throws PoolReadOnlyException;

    public long length();

    public boolean exists();
   
}

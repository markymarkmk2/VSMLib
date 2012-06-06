/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HashBlock;
import de.dimm.vsm.records.XANode;
import java.io.IOException;

/**
 *
 * @author mw
 */
public interface BootstrapHandle
{     
    public boolean delete();

    public void write_bootstrap( FileSystemElemNode node ) throws IOException;
    public void read_bootstrap( FileSystemElemNode node ) throws IOException;
    public void write_bootstrap( HashBlock hb ) throws IOException;
    public void read_bootstrap( HashBlock hb ) throws IOException;
    public void write_bootstrap( XANode xa ) throws IOException;
    public void read_bootstrap( XANode hb ) throws IOException;
    public <T> void write_object( T object ) throws IOException;
    public <T> T read_object( T object) throws IOException;
    
   
}

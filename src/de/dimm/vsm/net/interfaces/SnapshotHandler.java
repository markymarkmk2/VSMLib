/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.RemoteFSElem;
import java.util.ArrayList;


/**
 *
 * @author Administrator
 */
public interface SnapshotHandler
{
    ArrayList<SnapshotHandle> snap_shots = new ArrayList<SnapshotHandle>();

   
    
    public SnapshotHandle create_snapshot( RemoteFSElem file );

    public boolean release_snapshot( SnapshotHandle handle );

    public void init();

}

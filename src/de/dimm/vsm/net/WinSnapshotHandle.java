/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.net.interfaces.SnapshotHandle;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class WinSnapshotHandle extends SnapshotHandle
{
    int handle;
    Date creation;

    public WinSnapshotHandle( int handle)
    {
        this.handle = handle;
        this.creation = new Date();
    }


    @Override
    public Date getCreated()
    {
        return creation;
    }

    public int getHandle()
    {
        return handle;
    }

}


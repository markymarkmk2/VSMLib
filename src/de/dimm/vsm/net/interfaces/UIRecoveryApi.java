/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.StoragePool;

/**
 *
 * @author Administrator
 */
public interface UIRecoveryApi
{
    public void scanDatabase( User user, AbstractStorageNode node);
    public void rebuildBootstraps( User user, StoragePool pool);
        
}

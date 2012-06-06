/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Exceptions;

import de.dimm.vsm.records.StoragePool;

/**
 *
 * @author Administrator
 */
public class PoolReadOnlyException extends Exception {

    /**
     * Creates a new instance of <code>PoolReadOnlyException</code> without detail message.
     */
    public PoolReadOnlyException(StoragePool pool)
    {
        super( "Storagepool " + pool.getName() + " is readonly" );
    }


    /**
     * Constructs an instance of <code>PoolReadOnlyException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PoolReadOnlyException(String msg) {
        super(msg);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.StoragePoolQry;

/**
 *
 * @author Administrator
 */
public interface IWrapper
{

    public long getPoolIdx();
    public StoragePoolQry getQry();
    public long getWrapperIdx();
    public long getTs();

    public boolean isReadOnly();

}

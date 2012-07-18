/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.fsengine.GenericEntityManager;
import de.dimm.vsm.fsengine.JDBCLazyList;


/**
 *
 * @author Administrator
 */
public class PrefilledLazyList<T> extends JDBCLazyList
{
    public PrefilledLazyList()
    {
    }
   
    public PrefilledLazyList( GenericEntityManager _handler, Class<T> cl, String fieldname, long ownerIdx)
    {
        super(cl, fieldname, ownerIdx);
        realize( _handler );
    }

    


}

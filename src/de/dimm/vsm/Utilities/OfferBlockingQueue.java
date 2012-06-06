/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author mw
 */
public class OfferBlockingQueue<E> extends ArrayBlockingQueue<E>
{
    public OfferBlockingQueue( int n )
    {
        super(n);
    }

    @Override
    public boolean offer( E e )
    {
        try
        {
            put(e);
        }
        catch (InterruptedException interruptedException)
        {
            return false;
        }
        return true;
    }


}
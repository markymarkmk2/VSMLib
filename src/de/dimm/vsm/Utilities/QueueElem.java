/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

/**
 *
 * @author Administrator
 */
public abstract class QueueElem
{
    boolean finished;

    public abstract boolean run();

    public boolean isFinished()
    {
        return finished;
    }

    public void setFinished( boolean b )
    {
        finished = b;
    }
}

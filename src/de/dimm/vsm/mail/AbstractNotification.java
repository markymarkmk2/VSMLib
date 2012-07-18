/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import de.dimm.vsm.Utilities.VariableResolver;

/**
 *
 * @author Administrator
 */
public abstract class AbstractNotification  implements Notification
{
    int fired;
    NotificationEntry entry;

    public AbstractNotification(NotificationEntry entry )
    {
        this.entry = entry;
    }

    @Override
    public void fire(String extraText, VariableResolver vr)
    {
        if (entry.isSingleShot())
        {
            if (fired > 0)
                return;
        }
        fired++;
        fireNotification(extraText, vr);

    }
    protected abstract void fireNotification(String extraText, VariableResolver vr);

    @Override
    public abstract String getKey();

    @Override
    public boolean isFired()
    {
        return fired > 0;
    }

    @Override
    public int getFiredCnt()
    {
        return fired;
    }



   
    @Override
    public void release()
    {
        fired = 0;
    }

    @Override
    public String toString()
    {
        return entry.toString() + (fired > 0? "fired " + fired + "x" : "");
    }

    @Override
    public NotificationEntry getEntry()
    {
        return entry;
    }











}

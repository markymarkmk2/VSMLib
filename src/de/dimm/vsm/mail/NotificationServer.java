/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import de.dimm.vsm.Utilities.VariableResolver;
import de.dimm.vsm.fsengine.GenericEntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Administrator
 */
public abstract class NotificationServer
{

    final HashMap<String, Notification> notificationMap;
    final List<NotificationEntry> notificationEntries;

    public NotificationServer()
    {
        notificationMap = new HashMap<String, Notification>();
        notificationEntries = new ArrayList<NotificationEntry>();
    }


    protected NotificationEntry getNotificationEntry(String key)
    {
        for (int i = 0; i < notificationEntries.size(); i++)
        {
            NotificationEntry notificationEntry = notificationEntries.get(i);
            if (notificationEntry.getKey().equals(key))
                return notificationEntry;

        }
        return null;
    }
    protected boolean existsNotification( Notification no )
    {
        return notificationMap.containsKey(no.getKey());
    }
    protected void addNotification(Notification no)
    {
         notificationMap.put( no.getKey(), no);
    }
    protected void removeNotification(Notification no)
    {
         notificationMap.remove( no.getKey());
    }
    protected void clear()
    {
        notificationMap.clear();
    }
        
    public void fire( String key, String extraText, VariableResolver vr )
    {
        // FIRE ALL MATCHING NOTIFICATIONS (COULD BE MORE THE SAME NOTIFICATIONENTRY WITH DIFFERENT GROUPS)
        synchronized(notificationMap)
        {
            Collection<Notification> collno = notificationMap.values();
            for (Notification no : collno)
            {
                if (no.getEntry().getKey().equals(key))
                {
                    no.fire(extraText, vr);
                }
            }
        }
    }
    
    public void release( String key )
    {
        synchronized(notificationMap)
        {
            Collection<Notification> collno = notificationMap.values();
            for (Notification no : collno)
            {
                if (no.getEntry().getKey().equals(key))
                {
                    no.release();
                }
            }
        }
    }

    public abstract void loadNotifications(GenericEntityManager em) throws IOException;

    public void addNotificationEntry( NotificationEntry e )
    {
        if (!notificationEntries.contains(e))
        {
            notificationEntries.add(e);
        }
    }

    public List<NotificationEntry> listNotificationEntries()
    {
        return notificationEntries;
    }

    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import de.dimm.vsm.Utilities.VariableResolver;
import de.dimm.vsm.records.MailGroup;

/**
 *
 * @author Administrator
 */
public class SmtpNotification extends AbstractNotification
{
    MailGroup group;
    SmtpNotificationServer server;

    public SmtpNotification( NotificationEntry entry, MailGroup group, SmtpNotificationServer server )
    {
        super(entry);
        
        this.group = group;
        this.server = server;
    }



    @Override
    protected void fireNotification(String extraText, VariableResolver vr)
    {
        server.fireNotification(entry, group, extraText, vr);
    }

    @Override
    public String getKey()
    {
        return entry.key + group.getName();
    }

    


}

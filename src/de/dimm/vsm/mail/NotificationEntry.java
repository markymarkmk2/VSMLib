/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.mail;

import de.dimm.vsm.Utilities.DefaultTextProvider;
import de.dimm.vsm.Utilities.TextProvider;
import de.dimm.vsm.Utilities.VariableResolver;
import java.util.Objects;

/**
 *
 * @author Administrator
 */
public class NotificationEntry
{
 public enum Level
    {
        INFO,
        WARNING,
        ERROR,
        GROUP
    };
    String key;
    String subject;
    String text;
    Level level;
    boolean singleShot;
    

    public NotificationEntry( String key, String subject, String text, Level level, boolean singleShot )
    {
        this.key = key;
        this.subject = subject;
        this.text = text;
        this.level = level;
        this.singleShot = singleShot;        
    }

    public boolean isGroupNotificationEntry()
    {
        return level == Level.GROUP;
    }
    public String[] getSubKeys()
    {
        if (isGroupNotificationEntry())
        {
            String[] ret = text.split(",");
            return ret;
        }
        return null;
    }
    

    public String getKey()
    {
        return key;
    }

    public Level getLevel()
    {
        return level;
    }

    public String getText(VariableResolver vr)
    {
        if (vr != null)
            return vr.resolveVariableText(text);
        
        return text;
    }

    public boolean isSingleShot()
    {
        return singleShot;
    }

    public String getSubject(VariableResolver vr)
    {
        if (vr != null)
            return vr.resolveVariableText(subject);

        return subject;
    }


    @Override
    public String toString()
    {
        return key + " " + text;
    }
    public String getLeveltext()
    {
        TextProvider provider = DefaultTextProvider.getProvider();
        switch (level)
        {
            case INFO: return provider.Txt("Info");
            case WARNING: return provider.Txt("Warnung");
            case ERROR: return provider.Txt("Fehler");
            case GROUP: return provider.Txt("Gruppe");
        }
        return provider.Txt("Unbekannt");
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof NotificationEntry)
        {
            NotificationEntry ne = (NotificationEntry)obj;
            return ne.getKey().equals(key);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.key);
        return hash;
    }


}

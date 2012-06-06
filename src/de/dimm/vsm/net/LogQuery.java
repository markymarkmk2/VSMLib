/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.records.MessageLog;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Administrator
 */
public class LogQuery
{
    public static final int LV_DEBUG = 0x01;
    public static final int LV_INFO = 0x02;
    public static final int LV_ERROR = 0x04;
    public static final int LV_WARN = 0x08;
    public static int LV_MASK = 0x0f;

    int levelFlags;
    String qry;
    Date olderThan;
    int userId;

    public LogQuery( int levelFlags, String qry, Date olderThan )
    {
        this.levelFlags = levelFlags;
        this.qry = qry;
        this.olderThan = olderThan;
        userId = MessageLog.UID_SYSTEM;
    }

    public int getLevelFlags()
    {
        return levelFlags;
    }

    public Date getOlderThan()
    {
        return olderThan;
    }

    public String getQry()
    {
        return qry;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId( int userId )
    {
        this.userId = userId;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof LogQuery)
        {
            LogQuery lq = (LogQuery) obj;
            if (levelFlags != lq.levelFlags)
                return false;

            if (olderThan == null && lq.olderThan != null)
                return false;
            if (olderThan != null && lq.olderThan == null)
                return false;

            if (olderThan != null && olderThan.getTime() != lq.olderThan.getTime())
                return false;

            if (qry == null && lq.qry != null)
                return false;
            if (qry != null && lq.qry == null)
                return false;

            if (qry != null && !qry.equals( lq.qry ))
                return false;

            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + this.levelFlags;
        hash = 97 * hash + Objects.hashCode(this.qry);
        hash = 97 * hash + Objects.hashCode(this.olderThan);
        return hash;
    }

    
    
}

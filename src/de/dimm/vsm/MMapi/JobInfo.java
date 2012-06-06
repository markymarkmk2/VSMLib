/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

import de.dimm.vsm.Utilities.ParseToken;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class JobInfo
{
    String name;
    long size;
    Date created;
    long idx;
    long dirIdx;
    String directory;

    public JobInfo(String s)
    {
        parse(s);
    }


    final void parse( String s )
    {
        ParseToken pt = new ParseToken(s);
        name = pt.GetString("NA:");
        size = pt.GetLongValue("KB:") * 1024;
        long t = pt.GetLongValue("TI:") * 1000;
        created = new Date( t );
        idx = pt.GetLongValue("ID:");
        dirIdx = pt.GetLongValue("DD:");
        directory = pt.GetString("DI:");
    }

    public Date getCreated()
    {
        return created;
    }

    public long getDirIdx()
    {
        return dirIdx;
    }

    public String getDirectory()
    {
        return directory;
    }

    public long getIdx()
    {
        return idx;
    }

    public String getName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }

    
}

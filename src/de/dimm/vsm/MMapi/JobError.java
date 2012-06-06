/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

import de.dimm.vsm.Utilities.SizeStr;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class JobError
{
    public JobInfo ji;
    public JobStatus js;

    public JobError( JobInfo ji, JobStatus js )
    {
        this.ji = ji;
        this.js = js;
    }

    public Date getCreated()
    {
        return ji.getCreated();
    }

    public String getCreatedTxt()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(ji.getCreated());
    }

    public String getDirectory()
    {
        return ji.getDirectory();
    }

    public long getIdx()
    {
        return ji.getIdx();
    }

    public String getName()
    {
        return ji.getName();
    }

    public String getSize()
    {
        return SizeStr.format(ji.getSize());
    }

    public String getStateText()
    {
	return js.getStateText(js.getState());
    }

    public int getState()
    {
        return js.getState();
    }

    public String getStatus()
    {
        return js.getStatus();
    }

    public int getTaskId()
    {
        return js.getTaskId();
    }

    public String getJob()
    {
        return js.getJob();
    }
}
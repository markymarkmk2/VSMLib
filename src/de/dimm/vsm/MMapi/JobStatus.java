/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

import de.dimm.vsm.Utilities.ParseToken;

/**
 *
 * @author Administrator
 */
public class JobStatus
{
    public static final int JOB_SLEEPING = 0;
    public static final int JOB_BUSY = 1;
    public static final int JOB_READY = 2;
    public static final int JOB_USERQRY = 3;
    public static final int JOB_ERROR = 4;
    public static final int JOB_DELAYED = 5;
    public static final int JOB_WAITING = 6;
    public static final int JOB_USER_READY = 7;
    public static final int JOB_WARNING = 8;

    int taskId;
    int state;
    String job;
    String status;

    public JobStatus(String s)
    {
        parse(s);
    }

// sprintf( buff, "Task: %ld State: %d (%s) : %s", get_job_id(), (int)job_state, StateText( job_state ), status );
    final void parse( String s )
    {
        ParseToken pt = new ParseToken(s);
        taskId = (int)pt.GetLongValue("Task: ");
        state = (int)pt.GetLongValue("State: ");
        job = pt.GetString("Job-ID: ");
        int idx = s.indexOf(") : ");
        if (idx > 0)
            status = s.substring(idx + 4);
    }

    String getStateText( int  s)
    {
	switch(s)
	{
		case JOB_SLEEPING:		return "Sleeping";
		case JOB_BUSY:			return "Busy";
		case JOB_DELAYED:		return "Delayed";
		case JOB_WAITING:		return "Waiting";
		case JOB_ERROR:			return "Error";
		case JOB_USERQRY:               return "User request";
		case JOB_READY:			return "Ready";
		case JOB_USER_READY:            return "Ready to confirm";
		case JOB_WARNING:		return "Warning";
		default:			break;
	}
	return "Invalid state";
    }

    public int getState()
    {
        return state;
    }

    public String getStatus()
    {
        return status;
    }

    public int getTaskId()
    {
        return taskId;
    }

    public String getJob()
    {
        return job;
    }

    public void setState( int s )
    {
        state = s;
    }
    
}
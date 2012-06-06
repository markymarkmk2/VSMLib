/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.tasks;

import de.dimm.vsm.tasks.TaskInterface.TASKSTATE;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class TaskEntry
{
    
    TaskInterface job;
    Date started;
    InteractionEntry actInteractionEntry;
    Thread thr;

    @Override
    public String toString()
    {
        return job.getIdx() + " " + getTaskStatus().toString() + " " + getStatusStr() + " " + getProcessPercent() + " ";
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof TaskEntry)
        {
            TaskEntry te = (TaskEntry)obj;
            if (getIdx() != te.getIdx())
                return false;

            if (!te.getStatusStr().equals(getStatusStr()))
                return false;

            if (te.getTaskStatus() != getTaskStatus())
                return false;

            return true;
        }
        else
            return super.equals(obj);
    }




    public TaskEntry(TaskInterface job)
    {
        this.job = job;
        
    }
    public String getName()
    {
        return job.getName();
    }

    public Date getStarted()
    {
        return started;
    }

    

    public TaskInterface getTask()
    {
        return job;
    }


    public void setStarted( Date started )
    {
        this.started = started;
    }
    
    @Override
    public int hashCode()
    {
        return job.hashCode();
    }

    public long getIdx()
    {
        return job.getIdx();
    }

   
    public String getStatusStr()
    {
        return job.getStatusStr();
    }
    public String getStatistic()
    {
        return job.getStatisticStr();
    }

    public int getProcessPercent()
    {
        return job.getProcessPercent();
    }
    public String getProcessPercentDimension()
    {
        return job.getProcessPercentDimension();
    }
    public TASKSTATE getTaskStatus()
    {
        return job.getTaskState();
    }

    public void setTaskStatus( TASKSTATE st )
    {
        job.setTaskState( st );
    }


    void setThread( Thread thr )
    {
        this.thr = thr;
    }

    public String hash()
    {
        return toString();
    }



}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.jobs;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.jobs.JobInterface.JOBSTATE;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class JobEntry
{
    long idx;
    JobInterface job;
    Date started;
    InteractionEntry actInteractionEntry;
    Thread thr;

    public User getUser()
    {
        return job.getUser();
    }

    @Override
    public String toString()
    {
        return idx + " " + getJobStatus().toString() + " " + getStatusStr() + " " + getProcessPercent() + " " + getStarted().toString();
    }



    public JobEntry(JobInterface job)
    {
        this.job = job;
        started = new Date();
    }

    public JobInterface getJob()
    {
        return job;
    }

    public Date getStarted()
    {
        return started;
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
        return idx;
    }

    public void setIdx( long idx )
    {
        this.idx = idx;
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
    public JOBSTATE getJobStatus()
    {
        return job.getJobState();
    }

    public void setJobStatus( JOBSTATE st )
    {
        job.setJobState( st );
    }
//    public void setProcessPercent( int p)
//    {
//        job.setProcessPercent(p);
//    }


    void setThread( Thread thr )
    {
        this.thr = thr;
    }



}

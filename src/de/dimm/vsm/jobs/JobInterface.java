/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.jobs;

import de.dimm.vsm.auth.User;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public interface JobInterface
{



    public enum JOBSTATE
    {
        RUNNING,
        SLEEPING,
        WAITING,
        NEEDS_INTERACTION,
        FINISHED_ERROR,
        FINISHED_OK,
        FINISHED_OK_REMOVE,
        MANUAL_START,
        ABORTED,
        ABORTING
    }

    public JOBSTATE getJobState();
    public void setJobState( JOBSTATE jOBSTATE );

    public InteractionEntry getInteractionEntry();
    public String getStatusStr();

    public String getStatisticStr();
    public Date getStartTime();
    public Object getResultData();

    public String getProcessPercent();
    public String getProcessPercentDimension();

    public void abortJob();
    @Override
    public int hashCode();
    public void run();
    public User getUser();
    public void close();


}

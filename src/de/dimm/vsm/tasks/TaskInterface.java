/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.tasks;

/**
 *
 * @author Administrator
 */
public interface TaskInterface
{

    public long getIdx();

    public enum TASKSTATE
    {
        STARTED,
        RUNNING,
        SLEEPING,
        WAITING,
        NEEDS_INTERACTION,
        PAUSED
    }

    public TASKSTATE getTaskState();
    public void setTaskState( TASKSTATE jOBSTATE );

    public InteractionEntry getInteractionEntry();
    public String getStatusStr();
    public String getName();

    public String getStatisticStr();

    public int getProcessPercent();
    public String getProcessPercentDimension();

    

    
    


}

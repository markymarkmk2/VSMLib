/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class SearchStatus implements Serializable
{
    String status;
    boolean busy;
    long resultCnt;

    public SearchStatus( String status, boolean busy, long resultCnt )
    {
        this.status = status;
        this.busy = busy;
        this.resultCnt = resultCnt;
    }

    

    public boolean isBusy()
    {
        return busy;
    }

    public long getResultCnt()
    {
        return resultCnt;
    }

    public String getStatus()
    {
        return status;
    }


    

}

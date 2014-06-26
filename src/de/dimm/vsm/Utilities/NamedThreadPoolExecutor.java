/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;

import de.dimm.vsm.log.LogManager;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */


class NamedThreadFactory implements  ThreadFactory
{

    String name;
    ThreadPoolExecutor pool;
    boolean isCommThread;

    public NamedThreadFactory( String name, boolean isCommThread )
    {
        this.name = name;
        this.isCommThread = isCommThread;
    }
    

    @Override
    public Thread newThread( Runnable r )
    {
        String thr_name = name;

        if (pool != null)
        {
            thr_name = name + "-" + pool.getPoolSize();
        }
        if (isCommThread)
            return  new CommThread(r, thr_name);
        
        return new Thread(r, thr_name);
    }

    void set_pool( ThreadPoolExecutor ret )
    {
        pool = ret;
    }

    @Override
    public String toString()
    {
        return name;
    }

}

public class NamedThreadPoolExecutor extends ThreadPoolExecutor implements RejectedExecutionHandler
{
    String name;

    public NamedThreadPoolExecutor( String _name, int queue_size, int core_threads, int max_threads, int time, TimeUnit unit)
    {
        this(_name, queue_size, core_threads, max_threads, time, unit, false);
    }
    public NamedThreadPoolExecutor( String _name, int queue_size, int core_threads, int max_threads, int time, TimeUnit unit, boolean commThreads)
    {
        super( core_threads, max_threads, time, unit, new OfferBlockingQueue<Runnable>(queue_size), new NamedThreadFactory( _name, commThreads ));
        NamedThreadFactory thr_fact = (NamedThreadFactory) getThreadFactory();
        thr_fact.set_pool( this );

        setRejectedExecutionHandler(this);
    }

    @Override
    public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
    {
        LogManager.msg_system(LogManager.LVL_WARN, "Detected reject for pool " + name);
        r.run();
    }


}
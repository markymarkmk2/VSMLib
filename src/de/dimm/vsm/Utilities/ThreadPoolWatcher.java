/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import de.dimm.vsm.log.LogManager;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



    /**
 *
 * @author mw
 */
public class ThreadPoolWatcher
{
    ArrayList<NamedThreadPoolExecutor> pool_list;
    String pool_name;

    public ThreadPoolWatcher(String _name)
    {
        pool_name = _name;
        pool_list = new ArrayList<>();
    }


    public boolean shutdown_thread_pools( int ms)
    {
        LogManager.msg_system(LogManager.LVL_DEBUG, "Shutting down Thread Pool " + pool_name + " (" + pool_list.size() + " Pools)");

        for (int i = 0; i < pool_list.size(); i++)
        {
            ThreadPoolExecutor pool = pool_list.get(i);
            pool.shutdown();
        }

        boolean pool_finished = true;

        int cycle = 1000;
        while(ms > 0)
        {
            pool_finished = true;
            for (int i = 0; i < pool_list.size(); i++)
            {
                NamedThreadPoolExecutor pool = pool_list.get(i);
                try
                {
                    if (!pool.awaitTermination(10, TimeUnit.MILLISECONDS))
                    {
                        pool_finished = false;
                    }
                }
                catch (InterruptedException interruptedException)
                {
                    pool_finished = false;
                    LogManager.msg_system(LogManager.LVL_WARN, "awaitTermination Thread Pool " + pool.toString() + " aborted", interruptedException);
                }
            }
            if (pool_finished == true)
                break;

            try
            {
                Thread.sleep(cycle);
            }
            catch (InterruptedException interruptedException)
            {
            }
            ms -= cycle;
        }


        if (!pool_finished)
            LogManager.msg_system(LogManager.LVL_WARN, "Shutting down Thread Pool failed");
        else
            LogManager.msg_system(LogManager.LVL_DEBUG, "Shutting down Thread Pool succeeded");

        return pool_finished;
    }

    public boolean abort_thread_pool( ExecutorService pool)
    {
        String name = "";
        if (pool instanceof NamedThreadPoolExecutor)
        {
            name = " " + ((NamedThreadPoolExecutor)pool).toString();
        }
        LogManager.msg_system(LogManager.LVL_DEBUG, "Aborting Thread Pool" + name);

        pool.shutdownNow();

        boolean pool_finished = true;
        try
        {
            if (!pool.awaitTermination(1000, TimeUnit.MILLISECONDS))
            {
                pool_finished = false;
            }
        }
        catch (InterruptedException interruptedException)
        {
            pool_finished = false;
            LogManager.msg_system(LogManager.LVL_WARN, "awaitTermination Thread Pool aborted"  + name, interruptedException);
        }


        if (!pool_finished)
            LogManager.msg_system(LogManager.LVL_DEBUG, "Aborting Thread Pool failed" + name);
        else
            LogManager.msg_system(LogManager.LVL_DEBUG, "Aborting Thread Pool succeeded" + name);

        return pool_finished;
    }

    public boolean shutdown_thread_pool( ExecutorService pool, int ms)
    {
        String name = "";
        if (pool instanceof NamedThreadPoolExecutor)
        {
            name = " " + ((NamedThreadPoolExecutor)pool).toString();
        }
        LogManager.msg_system(LogManager.LVL_DEBUG, "Shutting down Thread Pool" + name);

        pool.shutdown();

        boolean pool_finished = true;
        try
        {
            if (!pool.awaitTermination(ms, TimeUnit.MILLISECONDS))
            {
                pool_finished = false;
            }
        }
        catch (InterruptedException interruptedException)
        {
            pool_finished = false;
            LogManager.msg_system(LogManager.LVL_WARN, "awaitTermination Thread Pool aborted"  + name, interruptedException);
        }


        if (!pool_finished)
            LogManager.msg_system(LogManager.LVL_DEBUG, "Shutting down Thread Pool failed" + name);
        else
            LogManager.msg_system(LogManager.LVL_DEBUG, "Shutting down Thread Pool succeeded" + name);

        return pool_finished;
    }

    public ThreadPoolExecutor create_blocking_thread_pool( String name, int threads, int queue_size)
    {
        return create_blocking_thread_pool(name, threads, queue_size, false);
    }

    public ThreadPoolExecutor create_blocking_thread_pool( String name, int threads, int queue_size, boolean commThreads )
    {
        //OfferBlockingQueue<Runnable> run_queue = new OfferBlockingQueue<Runnable>(queue_size);

       // NamedThreadFactory thr_fact = new NamedThreadFactory(pool_name + "-" + name);

        //NamedThreadPoolExecutor ret = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.MINUTES, run_queue, thr_fact);
        NamedThreadPoolExecutor ret = new NamedThreadPoolExecutor( name, queue_size, threads, threads, 60, TimeUnit.MINUTES, commThreads);
        
        pool_list.add( ret );

        return ret;
    }

}

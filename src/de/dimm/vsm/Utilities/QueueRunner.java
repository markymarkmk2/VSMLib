/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import de.dimm.vsm.log.LogManager;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;





class QueueRunnerTask implements Runnable
{
    final QueueRunner aiw;

    public QueueRunnerTask( QueueRunner aiw )
    {
        this.aiw = aiw;
    }

    @Override
    public void run()
    {
        while (aiw.keepRunning)
        {
            try
            {
                QueueElem elem = aiw.workList.poll(aiw.sleepMilisecondOnEmpty, TimeUnit.MILLISECONDS);
                if (elem != null)
                {
                   
                    elem.run();

                    // BLOCKING FOR CHECKING IS READY
                    try
                    {
                        aiw.readyLock.lock();
                        elem.setFinished( true );
                        aiw.isReady.signal();
                        
                    }
                    finally
                    {
                        aiw.readyLock.unlock();
                    }
                }
            }
            catch (Exception e)
            {
                LogManager.msg_system(LogManager.LVL_ERR, "Abbruch in QueueRunner", e);
                aiw.caughtError = true;
            }
        }
        aiw.isWRunning = false;
        LogManager.msg_system(LogManager.LVL_INFO, "QueueRunner beendet");
    }
}

/**
 *
 * @author Administrator
 */
public class QueueRunner
{
    BlockingQueue<QueueElem> workList;
    boolean keepRunning = true;
    boolean isWRunning;
    boolean caughtError;

    long sleepMilisecondOnEmpty = 100;
    ReentrantLock readyLock;
    Condition isReady;
    boolean isIdle;

    QueueElem lastAddedElem;
    Thread writeThread;
    int lastQueueLen;

    


    int maxQueueLen;

    public QueueRunner(String name, int maxQueueLen)
    {
        this.maxQueueLen = maxQueueLen;
        writeThread = new Thread( new QueueRunnerTask(this), name);

        init();
    }
    final void init()
    {
        workList = new ArrayBlockingQueue<QueueElem>(maxQueueLen);
        readyLock = new ReentrantLock();
        isReady = readyLock.newCondition();
        startThread();
    }

    private void startThread()
    {
        writeThread.start();
    }
    public void close()
    {
        try
        {
            flush();
        }
        catch (Exception exc)
        {
        }
        keepRunning = false;
    }

    int idxCnt = 0;
    void waitForFinish()
    {
        try
        {
            readyLock.lock();

            // THIS IS SYNCHRONIZING WITH LAST READY READY CONDITION AFTER WRITE OF LAST_ELEM
            if (lastAddedElem != null && !lastAddedElem.isFinished())
            {
                // AWAIT RELEASES AND REAQUIRES LOCK
                isReady.await();                
            }
        }
        catch(Exception exc)
        {
            caughtError = true;
        }
        finally
        {
            readyLock.unlock();
        }
        // THIS HAS A REFERENCE TI INDEXER, SO WE CLEAR IF NOT USED ANYMORE
        lastAddedElem = null;
    }

    public void flush()
    {
        int maxCnt = 300;
        while (!workList.isEmpty() && maxCnt-- > 0)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {

            }
        }
        waitForFinish();
    }

    public boolean isWriteError()
    {
        return caughtError;
    }

    void reset()
    {
        caughtError = false;
    }

    public void addElem( QueueElem elem)
    {
        try
        {            
            lastAddedElem = elem;
            workList.put(elem);            
        }
        catch (InterruptedException interruptedException)
        {
            caughtError = true;
        }
    }

    public int getQueueLen()
    {
        return workList.size();
    }

   
    
}

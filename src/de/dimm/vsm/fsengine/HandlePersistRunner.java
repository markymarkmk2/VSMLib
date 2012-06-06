/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.log.LogManager;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

abstract class HandleEntityElem
{
    JDBCEntityManager em;
    Object o;
    boolean ready = false;

    public HandleEntityElem( JDBCEntityManager em, Object o )
    {
        this.em = em;
        this.o = o;
    }


   
    abstract void work() throws SQLException;
}

class HandlePersistElem extends HandleEntityElem
{
    long idx;

    public HandlePersistElem( JDBCEntityManager em, Object o, long idx )
    {
        super(em, o);
        this.idx = idx;
    }

    @Override
    void work() throws SQLException
    {
        em.raw_persist(o, idx);
    }
}
class HandleMergeElem extends HandleEntityElem
{

    public HandleMergeElem( JDBCEntityManager em, Object o )
    {
        super(em, o);
    }

    @Override
    void work() throws SQLException
    {
        em.raw_merge(o);
    }
}
class HandleCommitElem extends HandleEntityElem
{

    public HandleCommitElem( JDBCEntityManager em)
    {
        super(em, null);
    }

    @Override
    void work() throws SQLException
    {
        em.commit_transaction();
    }
}
class HandleCloseElem extends HandleEntityElem
{

    public HandleCloseElem( JDBCEntityManager em)
    {
        super(em, null);
    }

    @Override
    void work() throws SQLException
    {
        em.close_transaction();
    }
}



class EntityRunner implements Runnable
{
    final HandlePersistRunner aiw;

    public EntityRunner( HandlePersistRunner aiw )
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
                HandleEntityElem elem = aiw.workList.poll(aiw.sleepMilisecondOnEmpty, TimeUnit.MILLISECONDS);
                if (elem != null)
                {
                    try
                    {
                        elem.work();
                    }
                    catch (SQLException sQLException)
                    {
                        aiw.exception = sQLException;
                    }
                    // BLOCKING FOR CHECKING IS READY
                    try
                    {
                        aiw.readyLock.lock();
                        elem.ready = true;
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
                e.printStackTrace();
                aiw.exception = e;
            }
        }
        aiw.isWRunning = false;
    }
}

/**
 *
 * @author Administrator
 */
public class HandlePersistRunner
{
    public static final int MAX_WRITE_QUEUE_LEN = 5000;
    public static final int MAX_OPEN_STATEMENTS = 200;
    
    final BlockingQueue<HandleEntityElem> workList;

    Exception exception;

    boolean keepRunning = true;
    boolean isWRunning;
   

    long sleepMilisecondOnEmpty = 100;
    ReentrantLock readyLock;
    Condition isReady;
    boolean isIdle;

    HandleEntityElem lastAddedElem;
    Thread workThread;
    int lastQueueLen;

    boolean performanceDiagnostic;


    public HandlePersistRunner()
    {
        workList = new ArrayBlockingQueue<HandleEntityElem>(MAX_WRITE_QUEUE_LEN);
        readyLock = new ReentrantLock();
        isReady = readyLock.newCondition();

        workThread = new Thread( new EntityRunner(this), "HandlePersistRunner");

        startThread();
    }

    public boolean isPerformanceDiagnostic()
    {
        return performanceDiagnostic;
    }



    private void startThread()
    {
        workThread.start();
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
    void waitForFinish() throws InterruptedException
    {
        if (lastAddedElem == null)
            return;
        
        try
        {
            readyLock.lock();

            // THIS IS SYNCHRONIZING WITH LAST READY READY CONDITION AFTER WRITE OF LAST_ELEM
            if (!lastAddedElem.ready)
            {
                // AWAIT RELEASES AND REAQUIRES LOCK
                isReady.await();                
            }
        }
        catch(Exception exc)
        {
            exception = exc;
        }
        finally
        {
            readyLock.unlock();
        }
    }

    void flush() throws InterruptedException
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

    public boolean hasException()
    {
        return exception != null;
    }

    void reset()
    {
        exception = null;
    }

    private void addElem( HandleEntityElem elem)
    {
        try
        {            
            lastAddedElem = elem;
            workList.put(elem);
        }
        catch (InterruptedException interruptedException)
        {
            exception = interruptedException;
        }
    }

    public <T> void em_persist( T o, JDBCEntityManager em ) throws SQLException
    {
        if (exception != null)
        {
            Exception tmp = exception;
            exception = null;
            LogManager.err_db("Fehler in PersistQueue", tmp);
        }

        long newIndex = JDBCEntityManager.newIndexValue(em, o);

        try
        {
            Field f = o.getClass().getDeclaredField("idx");
            boolean ac = f.isAccessible();
            f.setAccessible(true);
            f.setLong(o, newIndex);
            f.setAccessible(ac);
        }
        catch (Exception e)
        {
            LogManager.err_db("Fehler beim Setzen von newIndex", e);
            throw new RuntimeException(exception.getMessage());
        }

        HandlePersistElem elem = new HandlePersistElem(em, o, newIndex);

        addElem( elem );

        auto_check_commit_transaction(em);


        // ADD TO CACHE
        if (em.c_Persist2Cache)
        {
            //System.out.println("Persisting " + o.getClass().getSimpleName() + ":" + idx);
            Cache c = em.getCache(JDBCEntityManager.OBJECT_CACHE);
            String key = em.makeKeyFromObj( newIndex, o );
            c.putIfAbsent(new Element(key, o));
        }
    }

    public <T> T em_merge( T t, JDBCEntityManager em ) throws SQLException
    {
        if (exception != null)
        {
            Exception tmp = exception;
            exception = null;
            LogManager.err_db("Fehler in PersistQueue", tmp);
        }
        HandleMergeElem elem = new HandleMergeElem(em, t);

        addElem( elem );

        auto_check_commit_transaction(em);

        if (em.c_Update2Cache)
        {
            long idx = idx = em.getIdx(t);
            String key = em.makeKeyFromObj( idx, t );
            // UPDATE CACHE IF IT IS IN CACHE
            Cache c = em.getCache(JDBCEntityManager.OBJECT_CACHE);

            // NO PUT IF ABSENT,WE COMPARE VALUE
            Element cachedObject = c.get(key);
            if (cachedObject != null)
            {
                if (cachedObject.getValue() != t)
                {
                    c.put(new Element(key, t));
                }
            }
        }
        return t;
    }
    public void commit_transaction( JDBCEntityManager em ) throws SQLException
    {
        if (exception != null)
        {
            Exception tmp = exception;
            exception = null;
            throw new SQLException(tmp);
        }

        HandleCommitElem elem = new HandleCommitElem(em);

        addElem( elem );

        statementCnt = 0;
    }
    public void close_transaction( JDBCEntityManager em ) throws SQLException
    {
        if (exception != null)
        {
            Exception tmp = exception;
            exception = null;
            throw new SQLException(tmp);
        }

        HandleCloseElem elem = new HandleCloseElem(em);

        addElem( elem );

        try
        {
            waitForFinish();
        }
        catch (InterruptedException interruptedException)
        {
        }
        statementCnt = 0;
    }


    public void check_commit_transaction(JDBCEntityManager em) throws SQLException
    {
        // THIS IS DONE INTERNALLY
    }

    int statementCnt = 0;
    private void auto_check_commit_transaction(JDBCEntityManager em) throws SQLException
    {
        statementCnt++;
        if (statementCnt > MAX_OPEN_STATEMENTS)
        {
            commit_transaction(em);
        }
    }

    public int getQueueLen()
    {
        return workList.size();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.log.LogManager;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Element;

/**
 *
 * @author Administrator
 */
public class JDBCLazyList<T> extends LazyList
{
    long ownerIdx;
    Class<T> cl;
    String fieldname;

    public JDBCLazyList()
    {
    }
    // ENTRY FROM DB CREATION
    public JDBCLazyList( Class<T> cl, String fieldname, long ownerIdx)
    {
        super(cl, fieldname, ownerIdx);

        this.ownerIdx = ownerIdx;
        this.fieldname = fieldname;
        this.realList = null;
        this.cl = cl;
    }
    
    
    @Override
    public List<T> realize(GenericEntityManager _handler)
    {
        synchronized(mtx)
        {
            if (realList != null)
            {
                return realList;
            }
            realList = realizeWithSt( _handler);
        }
        
        return realList;
    }
    
    private List<T> realizeWithSt(GenericEntityManager _handler)
    {

        ResultSet rs = null;
        if (!(_handler instanceof JDBCEntityManager))
        {
            throw new RuntimeException( "Unsupported EntityManager " + _handler.toString());
        }
        JDBCEntityManager handler = (JDBCEntityManager)_handler;

        List<T> newList = new ArrayList<>();

        String statementName = cl.getSimpleName() + fieldname;

        Statement st = null;

        try
        {
            st = handler.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            
            String qry = handler.buildSelectString(cl, "T1." + fieldname + "=" + ownerIdx, "order by T1.idx asc", true, "Missing");

            rs = st.executeQuery(qry);
            // READ LIST ENTRIES
            while (rs.next())
            {
                long l2 = rs.getLong("idx");
                String key = handler.makeKeyFromStr( l2, cl.getSimpleName() );

                // TRY TO GET OBJECT FROM CACHE
                ConcurrentCache c = handler.getCache(JDBCEntityManager.OBJECT_CACHE);
                T obj = null;
                Element cacheElem = c.get(key);
                if (cacheElem != null)
                {
                    handler.incHitCount();
                    obj = (T)cacheElem.getValue();
                }

                // NOT FOUND, GET FROM DB
                if (obj == null)
                {
                    handler.incMissCount();
                    obj = handler.createObject(rs, cl);

                    if (obj == null)
                    {
                        throw new IOException( "Cannot resolve Object " + key );
                    }
                    Element el = new Element(key, obj);
                    c.put(el);
                }                   
                
                newList.add(obj);
                
                boolean doOOM = false;
                if (doOOM) {
                    throw new OutOfMemoryError("TestTest");
                }
            }
        }
        catch (Exception sQLException)
        {
            LogManager.err_db("Error resolving LayzyList for " + statementName + ownerIdx , sQLException);            
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (st != null)
                {
                    st.close();
                }
            }
            catch (SQLException sQLException)
            {
                LogManager.err_db("Error closing LayzyList" + statementName + ownerIdx , sQLException);    
            }            
        }
        return newList;
    }

    
    
    private List<T> realizeWithPS(GenericEntityManager _handler)
    {
        
        ResultSet rs = null;
        if (!(_handler instanceof JDBCEntityManager))
        {
            throw new RuntimeException( "Unsupported EntityManager " + _handler.toString());
        }
        JDBCEntityManager handler = (JDBCEntityManager)_handler;

        List<T> newList = new ArrayList<>();

        String statementName = cl.getSimpleName() + fieldname;

        int pscount = 0;
        PreparedStatement ps = null;

        try
        {
            // REGISTER PREPARED STATEMENT FOR THIS CLASS AND QUERY
            pscount = handler.addOpenLinkSet(statementName);

            // RETRIEVE STATEMENT
            ps = handler.createSelectChildrenPS(cl, fieldname);

            if (ps == null)
            {
                LogManager.err_db("Creating missing LayzyList PS for " + statementName + ownerIdx );
               ps = handler._getSelectStatement(cl, "T1." + fieldname + "=?", "order by T1.idx asc", true, "Missing");
            }
            // SET ID
            ps.setLong(1, ownerIdx);

            //System.out.println("Loading Lazy List " + cl.getSimpleName() + " Id: " + ownerIdx);
            try
            {
                rs = ps.executeQuery();
            }
            catch (SQLException sQLException)
            {
                LogManager.err_db("Error resolving LayzyList for " + statementName + ownerIdx + ", retrying", sQLException);
                Thread.sleep(1000);

                rs = ps.executeQuery();
            }
            // READ LIST ENTRIES
            while (rs.next())
            {
                long l2 = rs.getLong("idx");
                String key = handler.makeKeyFromStr( l2, cl.getSimpleName() );

                // TRY TO GET OBJECT FROM CACHE
                ConcurrentCache c = handler.getCache(JDBCEntityManager.OBJECT_CACHE);
                T obj = null;
                Element cacheElem = c.get(key);
                if (cacheElem != null)
                {
                    handler.incHitCount();
                    obj = (T)cacheElem.getValue();
                }

                // NOT FOUND, GET FROM DB
                if (obj == null)
                {
                    handler.incMissCount();
                    obj = handler.createObject(rs, cl);

                    if (obj == null)
                    {
                        throw new IOException( "Cannot resolve Object " + key );
                    }
                    Element el = new Element(key, obj);
                    c.put(el);
                }

                if (obj == null)
                    LogManager.err_db("Obj is null");
                if (newList == null)
                    LogManager.err_db("List is null");
                newList.add(obj);
            }
            // SET NEW LIST

            //System.out.println("Loaded " + realList.size() + " entries");

            
        }
        catch (Exception sQLException)
        {
            LogManager.err_db("Error resolving LayzyList for " + statementName + ownerIdx , sQLException);            
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
            }
            catch (SQLException sQLException)
            {
            }
            handler.removeOpenLinkSet(statementName, pscount);
        }
        return newList;
    }
}

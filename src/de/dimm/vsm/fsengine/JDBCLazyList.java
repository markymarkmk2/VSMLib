/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import de.dimm.vsm.log.LogManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.sf.ehcache.Cache;
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

        setDBLink(cl, fieldname, ownerIdx);
    }

    public final void setDBLink(Class<T> cl, String fieldname, long ownerIdx)
    {
        this.ownerIdx = ownerIdx;
        this.fieldname = fieldname;
        this.realList = null;
        this.cl = cl;
    }

    
    
    
    public void realize(GenericEntityManager _handler)
    {
        if (realList != null)
        {
            return;
        }

        if (!(_handler instanceof JDBCEntityManager))
        {
            throw new RuntimeException( "Unsupported EntityManager " + _handler.toString());
        }
        JDBCEntityManager handler = (JDBCEntityManager)_handler;

        realList = new ArrayList<T>();

        String statementName = cl.getSimpleName() + fieldname;

        int pscount = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            // REGISTER PREPARED STATEMENT FOR THIS CLASS AND QUERY
            pscount = handler.addOpenLinkSet(statementName);

            // RETRIEVE STATEMENT
            ps = handler.createSelectChildrenPS(cl, fieldname);

            if (ps == null)
            {
               ps = handler._getSelectStatement(cl, "T1." + fieldname + "=?", "order by T1.idx asc");
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
            while (!rs.isClosed() && rs.next())
            {
                long l2 = rs.getLong("idx");
                String key = handler.makeKeyFromStr( l2, cl.getSimpleName() );

                // TRY TO GET OBJECT FROM CACHE
                Cache c = handler.getCache(JDBCEntityManager.OBJECT_CACHE);
                Object obj = null;
                Element cacheElem = c.get(key);
                if (cacheElem != null)
                {
                    handler.incHitCount();
                    obj = cacheElem.getValue();
                }

                // NOT FOUND, GET FROM DB
                if (obj == null)
                {
                    handler.incMissCount();
                    obj = handler.createObject(rs, cl);

                    Element el = new Element(key, obj);
                    c.put(el);
                }

                realList.add(obj);
            }
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
    }

}

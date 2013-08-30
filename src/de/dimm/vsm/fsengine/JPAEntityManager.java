/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author Administrator
 */
public class JPAEntityManager implements GenericEntityManager
{
    public static final boolean immediateCommit = false;
    public static final int MAX_OPEN_STATEMENTS = 100;
    int statemenCnt = 0;

    EntityManager em;
    EntityTransaction tx;

    public JPAEntityManager( EntityManager em )
    {
        this.em = em;
    }

        public void setEm( EntityManager em )
    {
        this.em = em;
    }

    @Override
    public void em_persist( Object o )
    {
        em.persist(o);
    }
    @Override
    public void em_persist( Object o, boolean withCache )
    {
        em.persist(o);
    }

    @Override
    public void em_detach( Object o )
    {
        em.detach(o);
    }

    @Override
    public void em_remove( Object o )
    {
        em.remove(o);
    }
    @Override
    public void em_refresh( Object o )
    {
        em.refresh(o);
    }

    @Override
    public <T> T em_merge( T t )
    {
        return em.merge(t);
    }

    @Override
    public <T> T em_find( Class<T> t, long idx )
    {
        return em.find(t, idx);
    }

    @Override
    public void close_entitymanager()
    {
        try
        {
            em.flush();
        }
        catch (Exception e)
        {
        }
        em.close();
    }



    public void tx_commit()
    {
        if (tx == null)
        {
            throw new RuntimeException( "Transaction was not open");
        }

        try
        {
            if (tx.isActive())
            {
                if (!tx.getRollbackOnly())
                {
                    tx.commit();
                    //em.flush();

                }
                else
                {
                    System.out.println("Forced Rollback on commit_transaction");
                    tx.rollback();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Commit failed in commit_transaction");
            
            try
            {
                tx.rollback();
            }
            catch (Exception ee)
            {
            }
        }
    }
    @Override
    public void close_transaction()
    {
        statemenCnt = 0;
        if (tx != null)
        {
            if (!tx.getRollbackOnly())
                tx.commit();
            else
            {
                System.out.println("Forced Rollback on close");
                tx.rollback();
            }


            tx = null;
        }
    }
    @Override
    public void check_open_transaction()
    {

        if (tx == null || !tx.isActive())
        {
            tx = em.getTransaction();
            tx.begin();
            statemenCnt = 0;
        }
    }
    @Override
    public void check_commit_transaction()
    {
        statemenCnt++;
        if (immediateCommit || statemenCnt >MAX_OPEN_STATEMENTS)
        {
            commit_transaction();
        }
    }
    @Override
    public boolean is_transaction_active()
    {
        if (tx == null)
        {
            return false;
        }
        return tx.isActive();
    }
    @Override
    public void commit_transaction()
    {
        if (tx == null)
        {
            throw new RuntimeException( "Transaction was not open");
        }

        tx_commit();
        try
        {
            if (tx.isActive())
            {
                if (!tx.getRollbackOnly())
                {
                    tx.commit();
                    //em.flush();

                }
                else
                {
                    System.out.println("Forced Rollback on commit_transaction");
                    tx.rollback();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Commit failed in commit_transaction");
            
            try
            {
                tx.rollback();
            }
            catch (Exception ee)
            {
            }
        }
        tx.begin();
        statemenCnt = 0;
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults )
    {
        TypedQuery<T> tqr = em.createQuery(string, aClass );
        tqr.setMaxResults(maxResults);
        return tqr.getResultList();
    }
    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass )
    {
        TypedQuery<T> tqr = em.createQuery(string, aClass );
        return tqr.getResultList();
    }
    @Override
    public <T> T createSingleResultQuery( String string, Class<T> aClass )
    {
        TypedQuery<T> tqr = em.createQuery(string, aClass );
        return tqr.getSingleResult();
    }

    @Override
    public List<Object[]> createNativeQuery( String string, int qryCount )
    {
        Query tqr = em.createNativeQuery(string, "" );
        tqr.setMaxResults(qryCount);
        return tqr.getResultList();
    }

    @Override
    public List<Object[]> createNativeQuery( String string, int qryCount, int maxSeconds )
    {
        Query tqr = em.createNativeQuery(string, "" );
        tqr.setMaxResults(qryCount);
        return tqr.getResultList();
    }


    @Override
    public void rollback_transaction()
    {
        if (tx != null)
            tx.rollback();
    }

    public void setSuppressNotFound( boolean b)
    {
    
    }
    public int nativeUpdate( String string )
    {
        em.getTransaction().begin();
        java.sql.Connection conn = em.unwrap(java.sql.Connection.class);

        Statement st = null;
        try
        {

            st = conn.createStatement();
            int n = st.executeUpdate(string);

            return n;
        }
        catch (Exception sQLException)
        {
            
        }
        finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                }
                catch (SQLException sQLException)
                {
                }
            }
            em.getTransaction().commit();

        }
        return 0;
    }

    public boolean nativeCall( String string )
    {
        em.getTransaction().begin();
        java.sql.Connection conn = em.unwrap(java.sql.Connection.class);
        Statement st = null;
        try
        {

            st = conn.createStatement();
            boolean b = st.execute(string);

            return b;
        }
        catch (Exception sQLException)
        {
            
        }
        finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                }
                catch (SQLException sQLException)
                {
                }
            }
            em.getTransaction().commit();    
        }
        return false;
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, boolean distinct ) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Long getIdx( Object o )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, int maxSeconds ) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, boolean distinct, int maxSeconds ) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

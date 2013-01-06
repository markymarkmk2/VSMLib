/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import com.thoughtworks.xstream.XStream;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.StoragePool;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.rowset.serial.SerialBlob;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;

class NewIndexEntry
{

    public NewIndexEntry( String className, long lastNewIndex )
    {
        this.className = className;
        this.lastNewIndex = lastNewIndex;
    }

    String className;
    long lastNewIndex;
}


class LinkEntry
{
    Class subObject;
    Field field;

    public LinkEntry( Class subObject, Field field )
    {
        this.subObject = subObject;

        this.field = field;
    }
}

class FieldEntry
{

    Class clazz;
    String name;
    String dbfieldname;
    Field field;

    public FieldEntry( Class clazz, String name, String dbfieldname, Field field )
    {
        this.clazz = clazz;
        this.name = name;
        this.dbfieldname = dbfieldname;
        this.field = field;
    }
}



/**
 *
 * @author Administrator
 */
public class JDBCEntityManager implements GenericEntityManager
{
    public static final int MAX_CONNECTIONS = 50;

    //static MiniConnectionPoolManager poolManager = null;


    public static final boolean immediateCommit = false;

    public static final int MAX_OPEN_STATEMENTS = 400;
    HashMap<String, LinkEntry> linkMap;
    private int missCount;
    private int hitCount;
    public static final String OBJECT_CACHE = "ObjectCache";
    public static final String DEDUPBLOCK_CACHE = "DedupCache";

    HashMap<String, PreparedStatement> deleteStatementMap;
    HashMap<String, FieldEntry> fieldMap;
    int find_stack_count = 0;
    HashMap<String, PreparedStatement> insertStatementMap;
    HashMap<String, String> linkSelectPSMap = new HashMap<String, String>();
    HashMap<String, PreparedStatement> linkStatementMap;
    HashMap<String, Object> objectMap;
    PreparedStatement selectChildren;
    HashMap<String, String> selectPSMap = new HashMap<String, String>();
    HashMap<String, PreparedStatement> selectStatementMap;
    int statemenCnt = 0;
    Savepoint tx;
    HashMap<String, PreparedStatement> updateStatementMap;
    HashMap<String, PreparedStatement> newIndexStatementMap;

    long poolIdx;
    public boolean c_Persist2Cache = false;

    // THESE 2 MUST BE ON, OTHERWISE WE GET STACK OVERFLOW INSIDE CREATE OBJECT
    public boolean c_Create2Cache = true;
    public boolean c_CreateSubObject2Cache = true;

    public  boolean c_CreateSingleQuery2Cache = true;
    public  boolean c_Update2Cache = true;

    private Connection jdbcConnection;
    JDBCConnectionFactory connFactory;

    final public Connection getConnection() throws SQLException
    {
        if (jdbcConnection == null)
        {
            if (wasClosed)
            {
                LogManager.msg_db(LogManager.LVL_WARN, "Opening already closed connection " + this.toString() );
            }
            else
            {
                LogManager.msg_db(LogManager.LVL_INFO, "Opening connection " + this.toString() );
            }
            jdbcConnection = connFactory.createConnection();
            jdbcConnection.setAutoCommit(false);
        }
        return jdbcConnection;
    }

    public void setPoolIdx( long poolIdx )
    {
        this.poolIdx = poolIdx;
    }

    public Connection reopenConnection() throws SQLException
    {
        if (jdbcConnection != null)
        {

            jdbcConnection.commit();
            jdbcConnection.close();
            openCommits--;
        }
        LogManager.msg_db(LogManager.LVL_INFO, "ReOpening connection " + this.toString() );
        
        jdbcConnection = connFactory.createConnection();
        jdbcConnection.setAutoCommit(false);

        return jdbcConnection;
    }
    

    public JDBCEntityManager( long idx, JDBCConnectionFactory cf) throws SQLException
    {
        this.connFactory = cf;
        LogManager.msg_db(LogManager.LVL_INFO, "Creating EntityManager " + this.toString() );
        this.jdbcConnection = getConnection();
        this.poolIdx = idx;
        
        insertStatementMap = new HashMap<String, PreparedStatement>();
        deleteStatementMap = new HashMap<String, PreparedStatement>();
        updateStatementMap = new HashMap<String, PreparedStatement>();
        selectStatementMap = new HashMap<String, PreparedStatement>();
        linkStatementMap = new HashMap<String, PreparedStatement>();
        newIndexStatementMap = new HashMap<String, PreparedStatement>();
        linkMap = new HashMap<String, LinkEntry>();
        fieldMap = new HashMap<String, FieldEntry>();               

    }
//    public static void initializeJDBCPool(EntityManagerFactory emf) throws IOException
//    {
//        if (poolManager != null)
//        {
//            return;
//        }
//        poolManager = initializeJDBCPool(emf, null);
//    }


    public static MiniConnectionPoolManager initializeJDBCPool(EntityManagerFactory emf, String jdbcUrl) throws IOException
    {
        try
        {
            
            //Object poolingDriver = emf.getProperties().get("vsm.persistence.jdbc.pooldatasource");
            Object pwd = emf.getProperties().get("javax.persistence.jdbc.password");
            Object user = emf.getProperties().get("javax.persistence.jdbc.user");
            String url = emf.getProperties().get("javax.persistence.jdbc.url").toString();
            if (jdbcUrl != null)
                url = jdbcUrl;


            ConnectionPoolDataSource cds = null;


            if (url.startsWith("jdbc:derby"))
            {
                Class cl = Class.forName("org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource");
                cds = (ConnectionPoolDataSource) cl.newInstance();
                org.apache.derby.jdbc.EmbeddedDataSource derby_cds = (org.apache.derby.jdbc.EmbeddedDataSource)cds;
                derby_cds.setDatabaseName ("c:/temp/testDB");

                String start = "jdbc:derby:";
                String[] urlParts = url.substring(start.length()).split(";");
                String db = urlParts[0];
                derby_cds.setDatabaseName(db);
                derby_cds.setUser(user.toString());
                derby_cds.setPassword(pwd.toString());



                for (int i = 1; i < urlParts.length; i++)
                {
                    String string = urlParts[i];
                    if (string.indexOf("create=true") >= 0)
                    {
                        derby_cds.setCreateDatabase ("create");
                    }
                }
            }


            MiniConnectionPoolManager pm = new MiniConnectionPoolManager(cds, MAX_CONNECTIONS);

            return pm;
        }
        catch (Exception exc)
        {
            throw new IOException("cannot load jdbc connection", exc);
        }
    }

//    public static MiniConnectionPoolManager getJdbcPoolManager() throws IOException
//    {
//
//        return poolManager;
//    }


    <T> PreparedStatement _getSelectStatement( Class<T> o, String add_qry ) throws SQLException
    {
        return _getSelectStatement(o, add_qry, null);
    }

    <T> PreparedStatement _getSelectStatement( Class<T> o, String add_qry, String orderBy ) throws SQLException
    {
        try
        {
            String selectQryString = build_select_string(o, /*addTable*/null, add_qry, orderBy);
            String[] keys = new String[1];
            keys[0] = "idx";

                       
            PreparedStatement ps = getConnection().prepareStatement(selectQryString, keys);
            return ps;
        }
        catch (Exception exception)
        {
            LogManager.err_db("Error creating select statement " + o.getSimpleName() + " " + add_qry + " " + orderBy, exception);
        }
        return null;
    }

    int addOpenLinkSet( String string )
    {
        int i = 0;
        while (linkSelectPSMap.containsKey(string + i))
        {
            i++;
            if (i > MAX_PS)
            {
                LogManager.err_db("Nested too deep in PreparedStatement Map " + (string + i));
                break;
            }
        }
        linkSelectPSMap.put(string + i, string);
        return i;
    }

    int addOpenSet( String string )
    {
        int i = 0;
        while (selectPSMap.containsKey(string + i))
        {
            i++;
            if (i > MAX_PS)
            {
                LogManager.err_db("Nested too deep in PreparedStatement Map " + (string + i));
                break;
            }
        }
        selectPSMap.put(string + i, string);
        return i;
    }

    <T> String build_select_string( Class<T> o, String add_qry) throws SQLException
    {
        return build_select_string(o, null, add_qry,  null, false);
    }
    <T> String build_select_string( Class<T> o, String altTableString, String add_qry, String orderBy) throws SQLException
    {
        return build_select_string(o, altTableString, add_qry, orderBy, false);
    }

    <T> String build_select_string( Class<T> o, String altTableString, String add_qry, String orderBy, boolean distinct ) throws SQLException
    {
        try
        {
            String table = o.getSimpleName().toUpperCase();
            String[] keys = new String[1];
            keys[0] = "idx";
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            if (distinct)
                sb.append("distinct ");


            StringBuilder sb_fields = new StringBuilder();
            
            ArrayList<String> links = new ArrayList<String>();
            ArrayList<String> tables = new ArrayList<String>();
            tables.add(table);
            sb_fields.append(getFieldList(o, o, tables, links, null));
            sb.append(sb_fields);
            if (tables.size() == 1)
            {
                sb.append(" from ");
                sb.append(table);
                sb.append(" T1");
                if (altTableString != null)
                {
                    sb.append(",");
                    sb.append(altTableString);
                }

                sb.append(" where ");
                if (add_qry != null)
                {
                    sb.append(add_qry);
                }
                else
                {
                    sb.append("T1.idx=?");
                }
            }
            else
            {
                sb.append(" from ");
                StringBuilder sb_where = new StringBuilder();
                for (int i = 0; i < tables.size(); i++)
                {
                    if (sb_where.length() > 0)
                    {
                        sb_where.append(",");
                    }
                    sb_where.append(tables.get(i));
                    sb_where.append(" ");
                    String tablePrefix = "T" + Integer.toString(i + 1);
                    sb_where.append(tablePrefix);
                }
                sb.append(sb_where);
                
                if (altTableString != null)
                {
                    sb.append(",");
                    sb.append(altTableString);
                }

                sb.append(" where ");
                sb_where.setLength(0);
                for (int i = 0; i < links.size(); i++)
                {
                    if (sb_where.length() > 0)
                    {
                        sb_where.append(" and ");
                    }
                    sb_where.append(links.get(i));
                }
                sb.append(sb_where);
                if (add_qry != null)
                {
                    sb.append(" and ");
                    sb.append(add_qry);
                }
                else
                {
                    sb.append(" and T1.idx=?");
                }
            }
            if (orderBy != null)
            {
                sb.append(" ");
                sb.append(orderBy);
            }
            return sb.toString();
        }
        catch (Exception exception)
        {
            LogManager.err_db( "Error in build_select_string", exception );
            throw new SQLException("Error in build_select_string", exception);
        }
    }

    
    @Override
    public void check_commit_transaction() throws SQLException
    {
        statemenCnt++;
        if (immediateCommit || statemenCnt > MAX_OPEN_STATEMENTS)
        {
            commit_transaction();
        }
    }

   
    @Override
    public void check_open_transaction()
    {
        if (tx != null)
            return;

        statemenCnt = 0;
        tx = null;
        try
        {

            tx = getConnection().setSavepoint();
            openCommits++;
        }
        catch (SQLException sQLException)
        {
            LogManager.err_db( "Error in check_open_transaction", sQLException );
        }
    }


    boolean wasClosed = false;
    @Override
    public void close_entitymanager()
    {
        if (wasClosed)
        {
            LogManager.msg_db(LogManager.LVL_WARN, "Closing already closed connection " + this.toString() );
        }

        if (jdbcConnection == null)
            return;

        LogManager.msg_db(LogManager.LVL_INFO, "Closing connection " + this.toString() );
        try
        {
            jdbcConnection.commit();
            openCommits--;
        }
        catch (SQLException ex)
        {
            LogManager.msg_db(LogManager.LVL_ERR, "Committing closing connection failed" + ex.getMessage() );
        }
        try
        {            
            jdbcConnection.close();
            jdbcConnection = null;
        }
        catch (SQLException ex)
        {
            LogManager.msg_db(LogManager.LVL_ERR, "Closing connection failed" + ex.getMessage() );
        }
        linkStatementMap.clear();        
        linkSelectPSMap.clear();
        selectPSMap.clear();
        insertStatementMap.clear();
        newIndexStatementMap.clear();
        deleteStatementMap.clear();
        updateStatementMap.clear();
        wasClosed = true;
    }

    
    @Override
    public void close_transaction()
    {
        statemenCnt = 0;
        if (jdbcConnection != null)
        {
            tx_commit();
        }
    }

    
    @Override
    public void commit_transaction() throws SQLException
    {
        statemenCnt = 0;
        tx_commit();
        tx = jdbcConnection.setSavepoint();
        openCommits++;
    }

    public String makeKeyFromObj( long idx , Object o )
    {
        return Long.toString(idx) + o.getClass().getSimpleName() + poolIdx;
    }
    public String makeKeyFromObj( Long idx, Object o  )
    {
        return idx.toString() + o.getClass().getSimpleName() + poolIdx;
    }
    public String makeKeyFromStr( Long idx, String className  )
    {
        return idx.toString() + className + poolIdx;
    }

    private String copy( String s )
    {
        if (s == null)
            return s;
        
        return String.copyValueOf(s.toCharArray());
    }
    
    public <T> T createObject( ResultSet rs, Class<T> t ) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        T ret_val = null;
        HashMap<String, Object> localObjectMap = new HashMap<String, Object>();
        ArrayList<Object> newObjects = new ArrayList<Object>();
        String tablename = t.getSimpleName();
        int cols = rs.getMetaData().getColumnCount();
        Object objectToFill = null;
        Object lastObjectToFill = null;
        for (int i = 1; i <= cols; i++)
        {
            String tablePrefix = rs.getMetaData().getTableName(i);
            String name = rs.getMetaData().getColumnName(i);
            lastObjectToFill = objectToFill;
            objectToFill = localObjectMap.get(tablePrefix);
            if (objectToFill == null)
            {
                LinkEntry le = getLinkEntry(tablename, tablePrefix);
                Class subObjectCl = le.subObject;
                objectToFill = subObjectCl.newInstance();
                // THE FIRST INSTANTIATED OBJECT IS THE MAIN OBJECT (NESTED)
                if (ret_val == null)
                {
                    ret_val = (T) objectToFill;
                }
                localObjectMap.put(tablePrefix, objectToFill);
                newObjects.add(objectToFill);
                if (lastObjectToFill != null)
                {
                    // SET DB LINK
                    Field setter = le.field;
                    boolean ac = setter.isAccessible();
                    setter.setAccessible(true);
                    setter.set(lastObjectToFill, objectToFill);
                    setter.setAccessible(ac);
                    //                        String subObjectName = le.fieldName;
                    //                        String methodName = "set" + Character.toUpperCase(subObjectName.charAt(0)) + subObjectName.substring(1);
                    //
                    //                        //Field f = lastObjectToFill.getClass().getDeclaredField(le.fieldName);
                    //                        Method linkSetter = lastObjectToFill.getClass().getMethod(methodName, objectToFill.getClass());
                    //                        linkSetter.invoke(lastObjectToFill, objectToFill);
                }
            }
            FieldEntry fe = getClassforIgnCaseField(objectToFill.getClass().getSimpleName(), name);
            //String methodName = "set" + Character.toUpperCase(name.charAt(0)) + fe.name.substring(1);
            try
            {
                Field setter = fe.field;
                boolean ac = setter.isAccessible();
                setter.setAccessible(true);

                if (name.equalsIgnoreCase("idx") && (fe.field.getDeclaringClass() == ret_val.getClass()))
                {
                    // SET IDX
                    Long idx = (Long) rs.getObject(i);
                    setter.set(objectToFill, idx);

                    // AND ADD TO CACHE IF NECESSARY
                    if (c_Create2Cache)
                    {
                        Cache c = getCache(OBJECT_CACHE);
                        String key = makeKeyFromObj( idx, objectToFill  );
                        if (objectToFill == null)
                            throw new IOException("Null??");

                        c.putIfAbsent( new Element(key, objectToFill));
                    }
                }
                //System.out.println(fe.clazz.getName());
                else if(fe.clazz.getName().equals("boolean"))
                {
                    setter.setBoolean(objectToFill, rs.getBoolean(i));
                }
                else if(fe.clazz.getName().equals("char"))
                {
                    String s = rs.getString(i);
                    if (s.length() > 0)
                        setter.setChar(objectToFill, s.charAt(0));
                    else
                        setter.setChar(objectToFill, (char)0);
                }
                else if(name.equalsIgnoreCase("XATTRIBUTE"))
                {
                    // DECOUPLE DATA FROM DB-CACHE
                    String s = copy(rs.getString(i));
                    setter.set(objectToFill, s);
                }
                else if(fe.clazz.getSimpleName().equals("RemoteFSElem") )
                {
                    Blob blob = (Blob)rs.getObject(i);
                    if (blob != null)
                    {
                        InputStream is = blob.getBinaryStream();
                        try
                        {
                            XStream xs = new XStream();
                            Object so = xs.fromXML(is);
                            setter.set(objectToFill, so);
                        }
                        catch (Exception exception)
                        {
                            LogManager.err_db("Cannot deserialize Object " + fe.clazz.getSimpleName() + ": " + exception.getMessage());
                            setter.set(objectToFill, null);
                        }
                    }
                    else
                    {
                        setter.set(objectToFill, null);
                    }
                }
                else if (fe.clazz.getName().indexOf(".dimm.") >= 0)
                {
                    Long idx = (Long) rs.getObject(i);
                    Object newObject = null;
                    if (idx == null)
                    {
                        setter.set(objectToFill, newObject);
                    }
                    else if (ret_val != null && (fe.clazz == ret_val.getClass()) && idx.equals(getIdx(ret_val)))
                    {
                        // SET OURSELVES TO RELATED OBJECTS
                        setter.set(objectToFill, ret_val);
                    }
                    else
                    {
                        Object g = setter.get(objectToFill);
                        if (g == null || !getIdx(g).equals(idx))
                        {
                            Element elem = null;

                            if( c_CreateSubObject2Cache)
                            {
                                Cache c = getCache(OBJECT_CACHE);
                                String key = makeKeyFromStr( idx, fe.clazz.getSimpleName() );
                                elem = c.get(key);
                                if (elem != null)
                                {
                                    incHitCount();
                                    newObject = elem.getValue();
                                    //updateLazyListHandlers(newObject);
                                }
                            }

                            if (elem == null)
                            {
                                find_stack_count++;
                                if (find_stack_count > 30)
                                {
                                    System.out.println("Find stack: " + find_stack_count + " " + fe.clazz.getSimpleName() + idx);
                                }
                                if (find_stack_count < MAX_PS)
                                {
                                    newObject = em_find(fe.clazz, idx);
                                }
                                find_stack_count--;
                            }
                            // TODO: FETCH N
                            setter.setAccessible(true); // SET AGAIN, MAYBE WE HAVE RECURSED AND RESET ALREADY
                            setter.set(objectToFill, newObject);
                        }
                    }
                }
                else if (fe.clazz.getSimpleName().equals("Date"))
                {
                    setter.set(objectToFill, rs.getTimestamp(i));
                }
                else
                {
                    Object val = rs.getObject(i);
                    // DECOUPLE DATA FROM DB-CACHE
                    if (val instanceof String)
                        val = copy(val.toString());
                    
                    setter.set(objectToFill, val);
                    //                    if (name.toUpperCase().equals("IDX") && val instanceof Long)
                    //                    {
                    //                        // ADD OBJECT WITH INDEX TO CACHE
                    //                        Cache c = getCache(OBJECT_CACHE);
                    //                        Long idx = (Long)val;
                    //                        String key = idx.toString() + objectToFill.getClass().getSimpleName();
                    //                        if (c.get(key) != null)
                    //                        {
                    //                            System.out.println("Found object in cache, shouldnt be there!" );
                    //                        }
                    //                        c.put( new Element( key, objectToFill));
                    //                    }
                }
                setter.setAccessible(ac);
            }
            catch (Exception exc)
            {
                LogManager.err_db("CreateObject Todo resolve Object: " + fe.clazz.getName(), exc);
            }
        }
        // FILL LAZY LIOSTS FOR ALL NEW OBJECTS
        for (int i = 0; i < newObjects.size(); i++)
        {
            Object object = newObjects.get(i);
            fill_lazy_lists(object);
        }
        return ret_val;
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass ) throws SQLException
    {
        return createQuery(string, aClass, 0);
    }
    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults) throws SQLException
    {
        return createQuery(string, aClass, maxResults, false);
    }
    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, int maxSeconds ) throws SQLException
    {
        return createQuery(string, aClass, maxResults, false, maxSeconds);
    }

    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, boolean distinct) throws SQLException
    {
        return createQuery(string, aClass, maxResults, distinct, 0);
    }
   
    @Override
    public <T> List<T> createQuery( String string, Class<T> aClass, int maxResults, boolean distinct, int maxSeconds ) throws SQLException
    {
        // FIRST DETECT TABLE ENTRY
        int table_idx = string.toLowerCase().indexOf(aClass.getSimpleName().toLowerCase());

        // DETECT EXISTING JOIN TABLE ENTRY
        int altTableIdx = string.indexOf(",", table_idx);


        // FIRST DETECT ADDITIONAL WHERE / ORDER STATEMENTS
        int where_clause_idx = string.toLowerCase().indexOf(" where ");


        if (altTableIdx > 0 && where_clause_idx == -1)
        {
            LogManager.err_db("Missing where on join: " + string);
            return null;
        }
        String altTableString = null;

        if (altTableIdx > 0)
        {
            altTableString = string.substring(altTableIdx + 1, where_clause_idx);
        }

        String where_clause_string = "1=1";
        String order_clause = null;
        
        // RESPECT JOIN: select x from filesystemelemnode t1, filesystemelemattributes a where
        if(where_clause_idx > 0)
        {
            where_clause_string = string.substring(where_clause_idx + " where ".length());
        }
        else
        {
            int order_clause_idx = string.toLowerCase().lastIndexOf(" order ");
            if (order_clause_idx > 0)
            {
                order_clause = string.substring(order_clause_idx);
            }
        }

        List<T> list = new ArrayList<T>();
        Statement st = null;
        try
        {
            T t = null;
            st = getConnection().createStatement();
            String selectQryString = build_select_string(aClass, altTableString, where_clause_string, order_clause, distinct);

            if (maxResults > 0)
            {
                st.setMaxRows(maxResults);
                selectQryString += " fetch first " + maxResults + " rows only";
            }
            ResultSet rs = st.executeQuery(selectQryString);
            while (rs.next())
            {
                try
                {
                    t = (T) createObject(rs, aClass);
                    list.add(t);
                }
                catch (Exception exception)
                {
                    throw new SQLException("Cannot createObject", exception);
                }
            }
            rs.close();

            return list;
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
        }        
    }
    @Override
    public List<Object[]> createNativeQuery( String string, int maxResults ) throws SQLException
    {
        return createNativeQuery(string, maxResults, 0);
    }

    @Override
    public List<Object[]> createNativeQuery( String string, int maxResults, int maxSeconds ) throws SQLException
    {
        List<Object[]> list = new ArrayList<Object[]>();
        Statement st = null;
        try
        {

            st = getConnection().createStatement();

            if (maxResults > 0)
            {
                st.setMaxRows(maxResults);
                st.setFetchSize( maxResults );
            }
            ResultSet rs = st.executeQuery(string);
            while (rs.next())
            {
                int cols = rs.getMetaData().getColumnCount();
                Object[] arr = new Object[cols];

                for (int i = 0; i < arr.length; i++)
                {
                    arr[i] = rs.getObject(i + 1);
                }
                list.add(arr);
            }
            rs.close();

            return list;
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
        }
    }


    final <T> PreparedStatement createSelectChildrenPS( Class<T> cl, String linkField ) throws SQLException
    {
        //GNENERATE KEY
        String key = cl.getSimpleName() + linkField + linkSelectPSMap.size();
        
        PreparedStatement ps = linkStatementMap.get(key);
        if (ps != null)
        {
            return ps;
        }
        ps = _getSelectStatement(cl, "T1." + linkField + "=?", "order by T1.idx asc");
        if (ps != null)
            linkStatementMap.put(key, ps);
        
        return ps;
    }


    @Override
    public <T> T createSingleResultQuery( String string, Class<T> aClass ) throws SQLException
    {
        int where_clause_idx = string.toLowerCase().lastIndexOf(" where ");
        if (where_clause_idx > 0)
        {
            String where_clause = string.substring(where_clause_idx + " where ".length());
            Statement st = null;
            try
            {
                T t = null;
                st = getConnection().createStatement();
                String selectQryString = build_select_string(aClass, where_clause);
                selectQryString += " fetch first " + 1 + " rows only";
                st.setMaxRows(1);
                
                ResultSet rs = st.executeQuery(selectQryString);
                if (rs.next())
                {
                    try
                    {
                        t = (T) createObject(rs, aClass);
                    }
                    catch (Exception exception)
                    {
                        throw new SQLException("Cannot createSingleResultQuery", exception);
                    }
                }
                rs.close();
                if (t != null)
                {
                    if (c_CreateSingleQuery2Cache)
                    {
                        Cache c = getCache(OBJECT_CACHE);
                        String key = makeKeyFromStr( getIdx(t), aClass.getSimpleName());
                        if (t == null)
                            throw new SQLException("Null??");
                        c.putIfAbsent(new Element(key, t));
                    }
                }
                return t;
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
            }
        }
        return null;
    }

   
    @Override
    public void em_detach( Object o )
    {
        //System.out.println("Detaching " + o.getClass().getSimpleName() + ":" + getIdx(o));
        // REMOVE FROM CACHE
        long idx = getIdx(o);
        String key = makeKeyFromStr( idx, o.getClass().getSimpleName() );

        Cache c = getCache(OBJECT_CACHE);
        c.remove(key);
        
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            if (skipField(field))
            {
                continue;
            }
            if (field.getType().getSimpleName().equals("List"))
            {
                try
                {
                    boolean ac = field.isAccessible();
                    field.setAccessible(true);
                    Object list = field.get(o);
                    if (list instanceof LazyList)
                    {
                        LazyList ll = (LazyList) list;
                        ll.unRealize();
                    }
                    field.setAccessible(ac);
                }
                catch (Exception exception)
                {
                    LogManager.err_db("cannot detach object " + o.toString(), exception);
                }
            }
        }
    }
/*
    void updateLazyListHandlers( Object o )
    {
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            if (field.getType().getSimpleName().equals("List"))
            {
                try
                {
                    boolean ac = field.isAccessible();
                    field.setAccessible(true);
                    List list = (List) field.get(o);
                    if (list instanceof LazyList)
                    {
                        LazyList ll = (LazyList) list;
                        ll.setHandler(this);
                    }
                    field.setAccessible(ac);
                }
                catch (Exception exception)
                {
                    LogManager.err_db("cannot update ll handler for object " + o.toString(), exception);
                }
            }
        }
    }
 * */

    
    @Override
    public <T> T em_find( Class<T> t, long idx )
    {
        ResultSet rs = null;
        T ret_val = null;
        try
        {
            String key = makeKeyFromStr( idx, t.getSimpleName() );

            Cache c = getCache(OBJECT_CACHE);
            Element cachedObject = c.get(key);
            if (cachedObject != null)
            {
                incHitCount();
                ret_val = (T)cachedObject.getValue();
//                updateLazyListHandlers( ret_val );
                return ret_val;
            }

            incMissCount();
            int pscount = addOpenSet(t.getSimpleName());
                PreparedStatement ps = getSelectStatement(t, null);
                ps.setLong(1, idx);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                System.out.println("Cannot resolve DB Object " + key);
                rs.close();
                removeOpenSet(t.getSimpleName(), pscount);
                return null;
                //throw new SQLException("Find failed for " + key );
            }

            //System.out.println("Finding " + t.getSimpleName() + ":" + idx);
            ret_val = createObject(rs, t);
            
            removeOpenSet(t.getSimpleName(), pscount);
            Element el = new Element(key, ret_val);
            c.put(el);
            return ret_val;
        }
        catch (Exception e)
        {   
            LogManager.err_db("Error resolving Object " + t.getSimpleName() + idx, e);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {}
            }
        }
        return null;
    }

    
    @Override
    public <T> T em_merge( T t ) throws SQLException
    {
        t = raw_merge( t );     
        
        if (c_Update2Cache)
        {
            long idx = getIdx(t);
            String key = makeKeyFromObj( idx, t );
            // UPDATE CACHE IF IT IS IN CACHE
            Cache c = getCache(OBJECT_CACHE);

            // NO PUT IF ABSENT,WE COMPARE VALUE
            Element cachedObject = c.get(key);
            if (cachedObject != null)
            {
                if (cachedObject.getValue() != t)
                {
                    if (t == null)
                        throw new SQLException("Null??");

                    c.put(new Element(key, t));
                }
            }
        }
        return t;
    }
    public <T> T raw_merge( T t ) throws SQLException
    {
        final PreparedStatement ps;
        // TODO CHECK FOR UPDATE

        ps = getUpdateStatement(t);

        long idx = 0;

        // SYNCHRONIZE ON INSERT STATEMENT -> EVERY TABLE IS SYNCHRONIZED ON INSERT -> WE CAN USE MAX(IDX) TO GET NEXT INDEX
        synchronized (ps )
        {
            Field[] fields = t.getClass().getDeclaredFields();
            int fcnt = 0;
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                // SKIP CONSTANTS LISTS
                if (skipField(field))
                {
                    continue;
                }
                if (existsAnnotation(field, Id.class))
                {
                    continue;
                }
                if (existsAnnotation(field, OneToMany.class))
                {
                    continue;
                }
                try
                {
                    if (field.getName().equalsIgnoreCase("attributes"))
                    {
                        FileSystemElemAttributes attr = ((FileSystemElemNode)t).getAttributes();
                        if (attr != null)
                        {
                            ps.setObject(fcnt + 1, attr.getIdx());
                        }
                        else
                        {
                            ps.setObject(fcnt + 1, 0);
                        }
                    }
                    else
                    {
                        ps.setObject(fcnt + 1, getFieldValue(field, t));
                    }
                }
                catch (SQLException sQLException)
                {
                    throw sQLException;
                }
                catch (Exception noSuchMethodException)
                {
                    throw new RuntimeException(noSuchMethodException.getMessage());
                }
                fcnt++;
            }
            idx = getIdx(t);
            ps.setLong(fcnt + 1, idx);
            //System.out.println("Merging " + t.getClass().getSimpleName() + ":" + idx);
            int cnt = ps.executeUpdate();
            if (cnt != 1)
            {
                throw new SQLException("Update of " + t.toString() + " gave result count " + cnt);
            }
        }

       
        return t;
    }

    @Override
    public void em_persist( Object o ) throws SQLException
    {
        em_persist(o, false);
    }
    
    @Override
    public void em_persist( Object o, boolean noCache ) throws SQLException
    {

        long newIndex = newIndexValue(this, o);

        try
        {
            Field f = o.getClass().getDeclaredField("idx");
            boolean ac = f.isAccessible();
            f.setAccessible(true);
            f.setLong(o, newIndex);
            f.setAccessible(ac);
        }
        catch (Exception exception)
        {
             throw new RuntimeException(exception.getMessage());
        }
        raw_persist(o, newIndex);


        // ADD TO CACHE
        if (c_Persist2Cache && !noCache)
        {
            long idx = getIdx(o);
            //System.out.println("Persisting " + o.getClass().getSimpleName() + ":" + idx);
            Cache c = getCache(OBJECT_CACHE);
            String key = makeKeyFromObj( idx, o );
            if (o == null)
                throw new SQLException("Null??");
            c.putIfAbsent(new Element(key, o));
        }
    }

    public void raw_persist( Object o, long newIndex ) throws SQLException
    {
        Field[] fields = o.getClass().getDeclaredFields();
        int fcnt = 0;

        final PreparedStatement ps = getInsertStatement(o);

        // SYNCHRONIZE ON INSERT STATEMENT -> EVERY TABLE IS SYNCHRONIZED ON INSERT -> WE CAN USE MAX(IDX) TO GET NEXT INDEX
        synchronized (ps )
        {
            // TODO: HIGHSPEED SETTING OF KNOWN CLASSES??
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                if (skipField(field))
                {
                    continue;
                }
                if (existsAnnotation(field, OneToMany.class))
                {
                    continue;
                }
                try
                {
                    // NEW INDEX
                    if (existsAnnotation(field, Id.class))
                    {
                        field.setAccessible(true);
                        field.set(o, newIndex);
                        ps.setObject(fcnt + 1, newIndex);
                        fcnt++;
                        continue;
                    }

                    // SPECIAL HANDLING OF ATTRIBUTE FIELD -> NO FOREIGN KEYS
                    if (field.getName().equalsIgnoreCase("attributes"))
                    {
                        FileSystemElemAttributes attr = ((FileSystemElemNode)o).getAttributes();
                        ps.setObject(fcnt + 1, attr.getIdx());
                        fcnt++;
                        continue;
                    }
                    ps.setObject(fcnt + 1, getFieldValue(field, o));
                }
                catch (SQLException sQLException)
                {
                    throw sQLException;
                }
                catch (Exception noSuchMethodException)
                {
                    throw new RuntimeException(noSuchMethodException.getMessage());
                }

                fcnt++;
            }

            try
            {
                ps.execute();
            }
            catch (SQLIntegrityConstraintViolationException integrity)
            {
                LogManager.err_db("Class:" + o.getClass().getSimpleName() + " " + o.toString());
                throw integrity;
            }
        }


    }


    @Override
    public void em_refresh( Object o )
    {
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];

            try
            {
                // CLEAN ALL LAZY LISTS
                boolean ac = field.isAccessible();
                field.setAccessible(true);
                Object _ll = field.get(o);
                if (_ll == null)
                    continue;
                
                if (_ll instanceof  LazyList)
                {
                    LazyList ll = (LazyList) _ll;
                    ll.unRealize();
                    //ll.handler = this;
                    
                }
                field.setAccessible(ac);
                

            }
            catch (IllegalArgumentException illegalArgumentException)
            {
            }
            catch (IllegalAccessException illegalAccessException)
            {
            }
        }
    }

    PreparedStatement stDelPoolNodeFileLink = null;
    PreparedStatement stDelFileSystemElemAttributes = null;
    PreparedStatement stDelHashBlock = null;
    PreparedStatement stDelXANode = null;
    //PreparedStatement stSelChildren = null;
    PreparedStatement stDelNode = null;
    
    void em_remove_fse( long nodeIdx) throws SQLException
    {
        // CAUTION: THIS MUST MATCH TO THE CASCAE ENTRIES IN FileSystemElemNode
        if (stDelPoolNodeFileLink == null)
            stDelPoolNodeFileLink = getConnection().prepareStatement("delete from PoolNodeFileLink where fileNode_idx=?");
        if (stDelFileSystemElemAttributes == null)
            stDelFileSystemElemAttributes = getConnection().prepareStatement("delete from FileSystemElemAttributes where file_idx=?");
        if (stDelHashBlock == null)
            stDelHashBlock = getConnection().prepareStatement("delete from HashBlock where fileNode_idx=?");
        if (stDelXANode == null)
            stDelXANode = getConnection().prepareStatement("delete from XANode where fileNode_idx=?");
//        if (stSelChildren == null)
//            stSelChildren = getConnection().prepareStatement("select idx from FileSystemElemNode where parent_idx=?");
        if (stDelNode == null)
            stDelNode = getConnection().prepareStatement("delete from FileSystemElemNode where idx=?");

        stDelPoolNodeFileLink.setLong(1, nodeIdx);
        stDelPoolNodeFileLink.execute();

        stDelFileSystemElemAttributes.setLong(1, nodeIdx);
        stDelFileSystemElemAttributes.execute();
       
        stDelHashBlock.setLong(1, nodeIdx);
        stDelHashBlock.execute();
       
        stDelXANode.setLong(1, nodeIdx);
        stDelXANode.execute();
        

//        ArrayList<Long> kids = new ArrayList<Long>();
//        stSelChildren.setLong(1, nodeIdx);
//        ResultSet rs = stSelChildren.executeQuery();
//        while( rs.next() )
//        {
//            kids.add( rs.getLong(1));
//        }
//
//
//
//        for (int i = 0; i < kids.size(); i++)
//        {
//            Long k = kids.get(i);
//            em_remove_fse( k );
//        }

        stDelNode.setLong(1, nodeIdx);
        stDelNode.execute();

        check_commit_transaction();

    }
    
    class AnnotationEntry 
    {
        Annotation ann;
    }
    String getKey( Field field, Class<?> clazz) {
        String key = field.getDeclaringClass().getSimpleName() + "." +  field.getName() + "." + clazz.getSimpleName();
        return key;
    }
            
    Map<String,AnnotationEntry> annotationMap = new HashMap<String,AnnotationEntry>();
    AnnotationEntry getAnnotationEntry( Field field, Class<?> clazz) {
        String key = getKey( field, clazz);
        return annotationMap.get(key);        
    }
    boolean existsAnnotation( Field field, Class<? extends Annotation> clazz) {
         AnnotationEntry entry = getAnnotationEntry(field, clazz);
         if (entry == null) {
             entry = new AnnotationEntry();
             if (field.isAnnotationPresent(clazz)) {
                 Annotation ann = field.getAnnotation(clazz);
                 entry.ann = ann;
             }
             String key = getKey( field, clazz);
             annotationMap.put(key, entry);
         }
         return (entry.ann != null);
    }
    private <T extends Annotation> T  getAnnotation( Field field, Class<T> clazz) {
         AnnotationEntry entry = getAnnotationEntry(field, clazz);
         if (entry == null) {
             entry = new AnnotationEntry();
             if (field.isAnnotationPresent(clazz)) {
                 Annotation ann = field.getAnnotation(clazz);
                 entry.ann = ann;
             }
             String key = getKey( field, clazz);
             annotationMap.put(key, entry);
         }
         return (T)entry.ann;
    }

   
    @Override
    public void em_remove( Object o ) throws SQLException
    {
//        if (o instanceof FileSystemElemNode)
//        {
//            FileSystemElemNode node = (FileSystemElemNode) o;
//
//            // UPDATE CACHE
//            Cache c = getCache(OBJECT_CACHE);
//            String key = makeKeyFromObj( node.getIdx(), o );
//            Element cachedObject = c.get(key);
//            if (cachedObject != null)
//            {
//                c.remove(key);
//            }
//
//            em_remove_fse( node.getIdx());
//            return;
//        }
        Field[] fields = o.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            boolean remove_child = false;
            List childs = null;
            if (existsAnnotation(field, OneToMany.class))
            {
                OneToMany otm = getAnnotation(field, OneToMany.class);
                CascadeType[] ct = otm.cascade();
                for (int j = 0; j < ct.length; j++)
                {
                    CascadeType cascadeType = ct[j];
                    if (cascadeType.equals(CascadeType.REMOVE) || cascadeType.equals(CascadeType.DETACH))
                    {
                        remove_child = true;
                        field.setAccessible(true);
                        try
                        {
                            childs = ((LazyList)field.get(o)).getList(this);
                        }
                        catch (Exception exception)
                        {
                            throw new SQLException("Cannot access child", exception);
                        }
                        break;
                    }
                }
            }
            if (remove_child)
            {
                try
                {

                    while (childs.size() > 0)
                    {
                        Object child = childs.remove(0);
                        em_remove(child);

                        // WE MAYBY HAVE TO COMMIT A LARGE NUMBER OF DELETES
                        check_commit_transaction();
                    }
                }
                catch (Exception exception)
                {
                    //exception.printStackTrace();
                    throw new SQLException("Cannot remove child", exception);
                }
            }
        }
        PreparedStatement ps = getDeleteStatement(o);
        long idx = getIdx(o);
        ps.setLong(1, idx);
        //System.out.println("Removing " + o.getClass().getSimpleName() + ":" + idx);
//        if (o.getClass() == FileSystemElemNode.class)
//        {
//            List<Object[]> list = createNativeQuery( "Select idx from FileSystemElemAttributes T1 where t1.file_idx=" + idx , 10 );
//            System.out.println("Attribs with same FileIdx:" + list.size());
//             list = createNativeQuery( "Select idx from FileSystemElemNode T1 where t1.parent_idx=" + idx , 10 );
//            System.out.println("Children with same ParentIdx:" + list.size());
//
//        }
        ps.execute();
//        conn.commit();

        // UPDATE CACHE
        Cache c = getCache(OBJECT_CACHE);
        String key = makeKeyFromObj( idx, o );
        Element cachedObject = c.get(key);
        if (cachedObject != null)
        {
            c.remove(key);
        }
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            boolean remove_child = false;
            List childs = null;
            if (existsAnnotation(field, OneToOne.class))
            {
                OneToOne otm = getAnnotation(field, OneToOne.class);
                CascadeType[] ct = otm.cascade();
                for (int j = 0; j < ct.length; j++)
                {
                    CascadeType cascadeType = ct[j];
                    if (cascadeType.equals(CascadeType.REMOVE) || cascadeType.equals(CascadeType.DETACH))
                    {
                        remove_child = true;
                        childs = new ArrayList();
                        field.setAccessible(true);
                        try
                        {
                            childs.add(field.get(o));
                        }
                        catch (Exception exception)
                        {
                            throw new SQLException("Cannot access child", exception);
                        }
                        break;
                    }
                }
            }
            if (remove_child)
            {
                try
                {
                    while (childs.size() > 0)
                    {
                        Object child = childs.remove(0);
                        em_remove(child);

                        // WE MAYBY HAVE TO COMMIT A LARGE NUMBER OF DELETES
                        check_commit_transaction();
                    }
                }
                catch (Exception exception)
                {
                    throw new SQLException("Cannot remove child", exception);
                }
            }
        }
    }

    // ANNOTATION PARSING SEEMS TO TAKE LONG AND WASTS RESOURCES (YOURKIT PROFILER)
    class OtmMap
    {
        String otmtype;
        String field;
        Class type;

        public OtmMap( String otmtype, String field, Class type )
        {
            this.otmtype = otmtype;
            this.field = field;
            this.type = type;
        }
        
        
    }
    HashMap<String,OtmMap> fieldOtmHashMap = new HashMap<String, OtmMap>();

    private static final String OTM_NONE = "otm_none";
    private static final String OTM_LAZY = "otm_lazy";
    private static final String OTM_EAGER = "otm_eager";


    OtmMap buildOtmMap( String key, Field elem, Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        OtmMap map = null;
        OneToMany otm = getAnnotation(elem, OneToMany.class);
        if (otm != null)
        {
            Method m = o.getClass().getMethod("getIdx", (Class[]) null);
            String field = otm.mappedBy() + "_idx";
            Class type = (Class) ((ParameterizedType) elem.getGenericType()).getActualTypeArguments()[0];
            if (otm.fetch() == FetchType.EAGER)
            {
                map = new OtmMap(OTM_EAGER, field, type);                
            }
            else
            {
                map = new OtmMap(OTM_LAZY, field, type);                
            }
        }
        else
        {
            map = new OtmMap(OTM_NONE, null, null);            
        }
        return map;
    }



    void fill_lazy_lists( Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Field[] fields = o.getClass().getDeclaredFields();
        for (int idx = 0; idx < fields.length; idx++)
        {
            Field elem = fields[idx];

            if (skipField( elem  ))
                continue;

            String key = o.getClass().getSimpleName() + "." + elem.getName();

            if (List.class.isAssignableFrom(elem.getType()))
            {
                OtmMap map = fieldOtmHashMap.get(key);
                if (map == null)
                {
                    map = buildOtmMap(key, elem, o);
                    fieldOtmHashMap.put(key, map);
                }

                if (!map.otmtype.equals(OTM_NONE))
                {
                    Method m = o.getClass().getMethod("getIdx", (Class[]) null);
                    long ret_val_idx = (Long) m.invoke(o, (Object[]) null);
                    String field = map.field;
                    Class type = map.type;
                    boolean ac = elem.isAccessible();
                    elem.setAccessible(true);
                    if (map.otmtype.equals(OTM_EAGER))
                    {
                        // SONDERFALL, STORAGENODES IM NICHT INITIALISIERTEN POOLHANDLER, EAGER
                        if (poolIdx == 0)
                        {
                            if (o instanceof StoragePool)
                            {
                                poolIdx = ret_val_idx;
                            }
                            else
                            {
                                System.out.println("Pool0: " + o.getClass().getSimpleName());
                            }
                        }
                       JDBCLazyList ll = new JDBCLazyList(type, field, ret_val_idx/*, this*/);
                       ll.realize(this);
                       elem.set(o, ll);
                    }
                    if (map.otmtype.equals(OTM_LAZY))
                    {
                        JDBCLazyList ll = new JDBCLazyList(type, field, ret_val_idx/*, this*/);
                        elem.set(o, ll);                        
                    }
                    elem.setAccessible(ac);
                }
            }
        }
    }
    static void unrealizeChildren(FileSystemElemNode node)
    {
        //node.getChildren().unRealize();
        if (node.getChildren().isRealized())
        {
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                FileSystemElemNode child = node.getChildren().get(i);
                child.getHashBlocks().unRealize();
                child.getXaNodes().unRealize();
                child.getLinks().unRealize();


                if (child.getChildren().isRealized())
                {
                    unrealizeChildren(child);
                }
            }
            node.getChildren().unRealize();
        }
    }
    void clearCacheObj( Object o )
    {
        try
        {
            if (o instanceof FileSystemElemNode)
            {
                FileSystemElemNode node = (FileSystemElemNode) o;
                unrealizeChildren(node);

            }
            em_detach(o);
        }
        catch (Exception e)
        {
            System.out.println("Exception during cache object clear:" + e.getMessage());
        }
    }

    public Cache getCache( String id )
    {
        
        CacheManager.create();
        if (!CacheManager.getInstance().cacheExists(id))
        {
            CacheEventListener listener = new CacheEventListenerAdapter()
            {

                @Override
                public void notifyElementEvicted( Ehcache cache, Element element )
                {
                    if (element.getObjectValue() != null)
                    {
                        clearCacheObj( element.getObjectValue() );
                    }
                    else
                    {
                        System.out.println("Evicting null, key was " + element.getKey());
                    }
                }

                @Override
                public void notifyElementRemoved( Ehcache cache, Element element ) throws CacheException
                {
                    if (element.getObjectValue() != null)
                    {
                        clearCacheObj( element.getObjectValue() );
                    }
                    else
                    {
                        //System.out.println("Removing null, key was " + element.getKey());
                    }
                }
            };

            Cache memoryOnlyCache = new Cache(id, 50000, false, false, /*timetoLive s*/180, /*timetoIdle s*/180);
            CacheManager.getInstance().addCache(memoryOnlyCache);
            memoryOnlyCache.getCacheEventNotificationService().registerListener(listener);
            memoryOnlyCache.setStatisticsEnabled(true);
        }
        return CacheManager.getInstance().getCache(id);
    }
//    public Cache getDedupBlockCache(  )
//    {
//        CacheManager.create();
//        String id = DEDUPBLOCK_CACHE;
//        if (!CacheManager.getInstance().cacheExists(id))
//        {
//            Cache memoryOnlyCache = new Cache(id, 150000, false, false, 3600, 3600);
//            CacheManager.getInstance().addCache(memoryOnlyCache);
//            memoryOnlyCache.setStatisticsEnabled(true);
//        }
//        return CacheManager.getInstance().getCache(id);
//    }


    FieldEntry getClassforIgnCaseField( String table, String fieldname )
    {
        String key = table + fieldname;
        return fieldMap.get(key.toUpperCase());
    }

    PreparedStatement getDeleteStatement( Object o ) throws SQLException
    {
        String table = o.getClass().getSimpleName().toUpperCase();
        PreparedStatement ps = deleteStatementMap.get(table);
        if (ps != null)
        {
            return ps;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ");
        sb.append(table);
        sb.append(" where idx=?");
        ps = getConnection().prepareStatement(sb.toString());
        deleteStatementMap.put(table, ps);
        return ps;
    }

    <T> String getFieldList( Class<T> baseObject, Class o, ArrayList<String> tables, ArrayList<String> links, Field linkField ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Field[] fields = o.getDeclaredFields();
        StringBuilder sb_fields = new StringBuilder();
        String tablePrefix = "T" + tables.size();
        registerLink(baseObject.getSimpleName(), o.getSimpleName(), o, linkField);
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            // DO NOT SKIP ID, WE ARE IN SELECT
            //            if (field.isAnnotationPresent(javax.persistence.Id.class))
            //            {
            //                continue;
            //            }
            // SKIP CONSTANTS LISTS
            if (skipField(field))
            {
                continue;
            }
            if (existsAnnotation(field, OneToMany.class))
            {
                continue;
            }
            registerField(o.getSimpleName(), field.getName(), field.getType(), getFieldName(field), field);
            if (sb_fields.length() > 0)
            {
                sb_fields.append(",");
            }                        
            if (field.getName().equals("attributes"))
            {
                String table = field.getType().getSimpleName().toUpperCase();
                tables.add(table);
                // CREATE LINK "T0.ATTRIBUTES_IDX=T1.IDX"
                StringBuilder sb_link = new StringBuilder();
                sb_link.append(tablePrefix);
                sb_link.append(".");
                sb_link.append(getFieldName(field));
                sb_link.append("=T");
                sb_link.append(tables.size());
                sb_link.append(".idx");
                links.add(sb_link.toString());                
                sb_fields.append(getFieldList(baseObject, field.getType(), tables, links, field));
                continue;
            }
            String f = getFieldName(field);
            sb_fields.append(tablePrefix);
            sb_fields.append(".");
            sb_fields.append(f);
        }
        return sb_fields.toString();
    }

    String getFieldName( Field field )
    {
        String f = field.getName();
        if (existsAnnotation(field, ManyToOne.class))
        {
            f += "_idx";
        }
        else if(existsAnnotation(field, OneToOne.class))
        {
            f += "_idx";
        }
        else if(field.getName().equals("attributes"))
        {
            f += "_idx";
        }

        return f;
    }

    Object getFieldValue( Field field, Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if (o == null)
            return null;
        
        if (existsAnnotation(field, ManyToOne.class))
        {
            Object linkObjIdx = getObjectIdx(field, o);
            if (linkObjIdx == null)
            {
                return null;
            }
            return linkObjIdx.toString();
        }
        if (existsAnnotation(field, OneToOne.class))
        {
            Long l = getObjectIdx(field, o);
            if (l == null)
            {
                return null;
            }
            return l.toString();
        }

        Object value = getValue(field, o);
        if (value instanceof RemoteFSElem)
        {
            try
            {
                XStream xs = new XStream();
                String xml = xs.toXML(value);
                SerialBlob blob = new SerialBlob(xml.getBytes("UTF-8"));
                return blob;
            }
            catch (Exception exception)
            {
                throw new IllegalArgumentException("cannot store RemoteFsElem");
            }
        }
        if (value instanceof Long)
        {
            return value;
        }
        if (value instanceof String)
        {
            return value.toString();
        }
        if (value instanceof Date)
        {
            Date d = (Date) value;
            return new Timestamp(d.getTime());
        }
        return value;
    }

    Method getGetter( Field field, Object o ) throws NoSuchMethodException
    {
        String f = field.getName();
        String mName = "get" + Character.toUpperCase(f.charAt(0)) + f.substring(1);
        Method m = o.getClass().getMethod(mName, (Class[]) null);
        return m;
    }

    public int getHitCount()
    {
        return hitCount;
    }

    public static int getOpenCommits()
    {
        return openCommits;
    }
    

    
    @Override
    public Long getIdx( Object o )
    {
        try
        {
            Method m = o.getClass().getMethod("getIdx", (Class[]) null);
            Long idx = (Long) m.invoke(o, (Object[]) null);
            return idx;
        }
        catch (Exception exception)
        {
        throw new RuntimeException("Illegal Object for getIdx", exception);
        }
    }

    PreparedStatement getInsertStatement( Object o ) throws SQLException
    {
        String table = o.getClass().getSimpleName().toUpperCase();
        PreparedStatement ps = insertStatementMap.get(table);
        if (ps != null)
        {
            return ps;
        }
        String[] keys = new String[1];
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(table);
        sb.append(" (");
        Field[] fields = o.getClass().getDeclaredFields();
        StringBuilder sb_fields = new StringBuilder();
        int fcnt = 0;
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            // SKIP CONSTANTS LISTS
            if (skipField(field))
            {
                continue;
            }
            if (existsAnnotation(field, Id.class))
            {
                keys[0] = field.getName();
                //                continue;
            }
            if (existsAnnotation(field, OneToMany.class))
            {
                continue;
            }
            if (sb_fields.length() > 0)
            {
                sb_fields.append(",");
            }
            String f = getFieldName(field);
            fcnt++;
            sb_fields.append(f);
        }
        sb.append(sb_fields);
        sb.append(") values (");
        for (int i = 0; i < fcnt; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        ps = getConnection().prepareStatement(sb.toString());
        insertStatementMap.put(table, ps);
        return ps;
    }

    LinkEntry getLinkEntry( String table, String subClassname )
    {
        String key = table + subClassname;
        return linkMap.get(key.toUpperCase());
    }

    public int getMissCount()
    {
        return missCount;
    }

    Object getObject( Field field, Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        field.setAccessible(true);
        return field.get(o);
        //        Method m = getGetter(field, o);
        //        return m.invoke(o, (Object[]) null);
    }

    Long getObjectIdx( Field field, Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object object = getObject(field, o);
        if (object == null)
        {
            return null;
        }
        Long idx = getIdx(object);
        //        Method m = object.getClass().getMethod("getIdx", (Class[]) null);
        //        Long idx = (Long) m.invoke(object, (Object[]) null);
        return idx;
    }

    <T> PreparedStatement getSelectStatement( Class<T> o, String add_qry ) throws SQLException
    {
        String table = o.getSimpleName().toUpperCase();
        //GNENERATE KEY
        String key = table + selectPSMap.size();

        PreparedStatement ps = selectStatementMap.get(key);
        if (ps != null)
        {
            return ps;
        }
        ps = _getSelectStatement(o, add_qry);
        selectStatementMap.put(key, ps);
        return ps;
    }

    private boolean skipField( Field field )
    {
        int m = field.getModifiers();
        if ((m & Modifier.STATIC) != 0)
        {
            return true;
        }
        if ((m & Modifier.FINAL) != 0)
        {
            return true;
        }
        final String name = field.getName();

        if (existsAnnotation(field, Transient.class) && !name.equals("attributes"))
        {
            return true;
        }
        if (name.equalsIgnoreCase("attributes_idx"))
        {
            return true;
        }


        return false;
    }

    PreparedStatement getUpdateStatement( Object o ) throws SQLException
    {
        String table = o.getClass().getSimpleName().toUpperCase();
        PreparedStatement ps = updateStatementMap.get(table);
        if (ps != null)
        {
            return ps;
        }
        String[] keys = new String[1];
        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        sb.append(table);
        sb.append(" set ");
        Field[] fields = o.getClass().getDeclaredFields();
        StringBuilder sb_fields = new StringBuilder();
        int fcnt = 0;
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            // SKIP CONSTANTS LISTS
            if (skipField(field))
            {
                continue;
            }
            if (existsAnnotation(field, Id.class))
            {
                keys[0] = field.getName();
                continue;
            }
            if (existsAnnotation(field, OneToMany.class))
            {
                continue;
            }
            if (sb_fields.length() > 0)
            {
                sb_fields.append(",");
            }
            String f = getFieldName(field);
            fcnt++;
            sb_fields.append(f);
            sb_fields.append("=?");
        }
        sb_fields.append(" where idx=?");
        sb.append(sb_fields);
        ps = getConnection().prepareStatement(sb.toString(), keys);
        updateStatementMap.put(table, ps);
        return ps;
    }

    Object getValue( Field field, Object o ) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        field.setAccessible(true);
        return field.get(o);
        //        Method m = getGetter(field, o);
        //        return m.invoke(o, (Object[]) null);
    }

    void incHitCount()
    {
        hitCount++;
    }

    void incMissCount()
    {
        missCount++;
    }

    
    @Override
    public boolean is_transaction_active()
    {
        return tx != null;
    }

    void registerField( String table, String fieldname, Class cl, String dbfieldname, Field field )
    {
        String key = table + dbfieldname;
        if (fieldMap.containsKey(key.toUpperCase()))
        {
            return;
        }
        FieldEntry fe = new FieldEntry(cl, fieldname, dbfieldname, field);
        fieldMap.put(key.toUpperCase(), fe);
    }

    void registerLink( String table, String subClassname, Class subObject, Field field )
    {
        String key = table + subClassname;
        if (linkMap.containsKey(key.toUpperCase()))
        {
            return;
        }
        LinkEntry le = new LinkEntry(subObject, field);
        linkMap.put(key.toUpperCase(), le);
    }

    void removeOpenLinkSet( String string, int n )
    {
        linkSelectPSMap.remove(string + n);
    }

    void removeOpenSet( String string, int n )
    {
        selectPSMap.remove(string + n);
    }

    
    @Override
    public void rollback_transaction()
    {
        try
        {
            if (tx != null)
            {
                jdbcConnection.rollback(tx);
                tx = null;
            }
            else
            {
                jdbcConnection.rollback();
            }
        }
        catch (SQLException sQLException)
        {
            LogManager.err_db("Rollback failed in rollback_transaction:" + sQLException.getMessage());
        }
    }

    static int openCommits = 0;

    
    public void tx_commit()
    {

        if (jdbcConnection == null)
        {
            throw new RuntimeException("Transaction was not open");
        }
        try
        {
             jdbcConnection.commit();
             openCommits--;
             tx = null;
        }
        catch (Exception e)
        {
            LogManager.err_db("Commit failed in commit_transaction", e);
            try
            {
                jdbcConnection.rollback();
                openCommits--;
            }
            catch (Exception ee)
            {
            }
        }
    }
    public static final int MAX_PS = 50;

    PreparedStatement getNewIndexStatement( String table ) throws SQLException
    {
        PreparedStatement ps = newIndexStatementMap.get(table);
        if (ps != null)
        {
            return ps;
        }
        String s = "select max(idx) from " + table;
        ps = getConnection().prepareStatement(s);
        newIndexStatementMap.put(table, ps);
        return ps;
    }


    final static HashMap<String,NewIndexEntry> newIndexCacheMap = new HashMap<String, NewIndexEntry>();

    // EM_PERSIST IS ALREADY SYNCHRONIZED, BUT WE DO IT ANYWAY
    public static synchronized long newIndexValue( JDBCEntityManager em, Object newObject ) throws SQLException
    {
        if (newObject instanceof StoragePool)
        {
            // POOR MANS 64BIT UUID: TIMESTAMP
            return System.currentTimeMillis();
        }

        // TODO: SPEED THIS UP WITH OWN COUNTERS, SEQENCE DATABASE ETC.
        String simpleName = newObject.getClass().getSimpleName();
        String key = em.poolIdx + simpleName;

        NewIndexEntry newIndex = newIndexCacheMap.get(key);
        if (newIndex != null)
        {
            newIndex.lastNewIndex++;
            return newIndex.lastNewIndex;
        }
        
        PreparedStatement ps = em.getNewIndexStatement(simpleName);

        ResultSet rs = ps.executeQuery();
        rs.next();
        long idx = rs.getLong(1);
        rs.close();
        
        idx++;
        NewIndexEntry ne = new NewIndexEntry(simpleName, idx);

        newIndexCacheMap.put(key, ne );

        return idx;
    }

    @Override
    public int nativeUpdate( String string )
    {
        Statement st = null;
        try
        {
            st = getConnection().createStatement();
            int n = st.executeUpdate(string);

            return n;
        }
        catch (Exception sQLException)
        {
            LogManager.err_db("nativeUpdate failed for " + string, sQLException);
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
        }
        return 0;
    }

    @Override
    public boolean nativeCall( String string )
    {
        Statement st = null;
        try
        {

            st = getConnection().createStatement();
            boolean b = st.execute(string);

            return b;
        }
        catch (Exception sQLException)
        {
            LogManager.err_db("nativeCall failed for " + string, sQLException);
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
        }
        return false;
    }

    void setConnection( Connection conn ) throws SQLException
    {
        if (jdbcConnection != null)
        {
            jdbcConnection.commit();
            jdbcConnection.close();
            openCommits--;
        }
        this.jdbcConnection = conn;
    }

    // DERBY TIMESTAMP INPUT
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public String getTimestamp( Date d )
    {
        return sdf.format(d);
    }

    public void writeCacheStatistics( String id )
    {
        Cache ch = getCache(id);
        Statistics st = ch.getStatistics();
        System.out.println("Size: " + ch.getSize() + ": " + st.toString() );
    }

    
}

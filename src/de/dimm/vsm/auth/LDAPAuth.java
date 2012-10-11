/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.auth;


import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.records.Role;
import java.util.ArrayList;
import java.util.Hashtable;

import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;





class LDAPUserContext implements UserContext
{
    String dn;
    LdapContext ctx;
    String niceName;

    public LDAPUserContext( String sid, LdapContext ctx, String niceName )
    {
        this.dn = sid;
        this.ctx = ctx;
        this.niceName = niceName;
    }
}

public class LDAPAuth extends GenericRealmAuth
{

    String admin_name;
    String admin_pwd;
    String user_search_base;
    LdapContext ctx;

    String search_attribute;
    String mail_field_list;
    String ldapfilter;
    String groupIdentifier;

    LDAPUserContext user_context;

    public static final Hashtable<Integer,String> ldap_error_map = new Hashtable<Integer, String>();

    void init_ldap_error_list()
    {
        ldap_error_map.put( 0, "successful");
        ldap_error_map.put( 1, "operations error");
        ldap_error_map.put( 2, "protocol error");
        ldap_error_map.put( 3, "timelimit exceed");
        ldap_error_map.put( 4, "sizelimit exceeded");
        ldap_error_map.put( 5, "compare false");
        ldap_error_map.put( 6, "compare true");
        ldap_error_map.put( 7, "strong auth not supported");
        ldap_error_map.put( 8, "strong auth required");
        ldap_error_map.put( 9, "partial results");
        ldap_error_map.put( 10,"referral");
        ldap_error_map.put( 11,"admin limit exceeded");
        ldap_error_map.put( 16,"no such attribute");
        ldap_error_map.put( 17,"undefined type");
        ldap_error_map.put( 18,"inappropriate matching");
        ldap_error_map.put( 19,"constraint violation");
        ldap_error_map.put( 20,"type or value exists");
        ldap_error_map.put( 21,"invalid syntax");
        ldap_error_map.put( 32,"no such object");
        ldap_error_map.put( 33,"alias problem");
        ldap_error_map.put( 34,"invalid DN syntax");
        ldap_error_map.put( 35,"is leaf");
        ldap_error_map.put( 36,"alias deref problem");
        ldap_error_map.put( 48,"inappropriate auth");
        ldap_error_map.put( 49,"invalid credentials");
        ldap_error_map.put( 50,"insufficient access");
        ldap_error_map.put( 51,"busy");
        ldap_error_map.put( 52,"unavailable");
        ldap_error_map.put( 53,"unwilling to perform");
        ldap_error_map.put( 54,"loop detect");
        ldap_error_map.put( 64,"naming violation");
        ldap_error_map.put( 65,"object class violation");
        ldap_error_map.put( 66,"not allowed on nonleaf");
        ldap_error_map.put( 67,"not allowed on RDN");
        ldap_error_map.put( 68,"already exists");
        ldap_error_map.put( 69,"no object class mods");
        ldap_error_map.put( 70,"results too large");
        ldap_error_map.put( 80,"other");
        ldap_error_map.put( 81,"server down");
        ldap_error_map.put( 82,"local error");
        ldap_error_map.put( 83,"encoding error");
        ldap_error_map.put( 84,"decoding error");
        ldap_error_map.put( 85,"timeout");
        ldap_error_map.put( 86,"auth unknown");
        ldap_error_map.put( 87,"filter error");
        ldap_error_map.put( 88,"user cancelled");
        ldap_error_map.put( 89,"param error");
        ldap_error_map.put( 90,"no memory");
        ldap_error_map.put( 91,"connect error");
    }
    int get_ldap_err_from_exc( Exception exc )
    {
        int idx = exc.getMessage().indexOf("error code");
        if (idx >= 0)
        {
            StringTokenizer str = new StringTokenizer(exc.getMessage().substring(idx + 11), " " );
            if (str.hasMoreTokens())
            {
                try
                {
                    int err_code = Integer.parseInt(str.nextToken());
                    return err_code;
                }
                catch (NumberFormatException numberFormatException)
                {
                }
            }
        }
        return -1;
    }
    String get_ldap_err_text( Exception exc )
    {
        int ldap_err = get_ldap_err_from_exc(exc);
        String ret = ldap_error_map.get(ldap_err);
        if (ret == null)
            ret = exc.getMessage();

        return ret;
    }

    LDAPAuth( String admin_name, String admin_pwd, String ldap_host, String user_search_base, int ldap_port, long  flags, String search_attribute, String mfl, String ldapfilter, String groupIdentifier )
    {
        super(flags, ldap_host, ldap_port);
        this.admin_name = admin_name;
        this.admin_pwd = admin_pwd;
        this.user_search_base = user_search_base;
        this.search_attribute = search_attribute;
        this.ldapfilter = ldapfilter;
        this.groupIdentifier = groupIdentifier;

        if (this.search_attribute == null  || this.search_attribute.length() == 0)
            this.search_attribute = "cn";

        this.mail_field_list = mfl;
        if (mail_field_list == null || mail_field_list.length() == 0)
            mail_field_list = "mail";
        
        this.flags = flags;
        if (ldap_port == 0)
        {
            ldap_port = is_ssl() ? 636 : 389;
        }
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }

    String get_user_search_base() throws NamingException
    {
        if (user_search_base != null && user_search_base.length() > 0)
            return user_search_base;

        if (ctx != null)
        {
            Attributes attributes = ctx.getAttributes(ctx.getNameInNamespace());
            String ret = "CN=Users";
            Attribute attribute = attributes.get("defaultNamingContext");
            if (attribute != null && attribute.get() != null)
                ret += "," + attribute.get().toString();
            return ret;
        }
        return "CN=Users";
    }


    @Override
    public void close_user_context()
    {
        close_user(user_context);
        user_context = null;
    }
//Gruppen für Rollenverwaltung

    Hashtable<String,String> create_sec_env()
    {
        Hashtable<String,String> env = new Hashtable<String,String>();
        String protokoll = "ldap://";
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.timeout", "10000");
        //        env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");

        if (is_ssl())
        {
            protokoll = "ldaps://";
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            String java_home = System.getProperty("java.home").trim();
            String ca_cert_file = java_home + "/lib/security/cacerts";
            System.setProperty("javax.net.ssl.trustStore", ca_cert_file);
            env.put("javax.net.ssl.trustStore", ca_cert_file);
        }
        //Der entsprechende Domänen-Controller:LDAP-Port
        env.put(Context.PROVIDER_URL, protokoll + host + ":" + port);
        return env;

    }

    
    @Override
    public boolean connect()
    {
        Hashtable<String,String> connect_env = create_sec_env();
        String admin_dn = "";
        try
        {                        
            if (admin_dn.length() > 0)
            {
                String rootSearchBase = get_user_search_base();
            
                // ABSOLUTE DN?  (cn=manager,dc=test,dc=de)
                if (admin_name.toLowerCase().indexOf("dc=") >= 0)
                {
                    admin_dn =  admin_name;
                }
                else
                {
                    // NO, JUST ATTRIB NAME
                    admin_dn =  "cn=" + admin_name + "," + rootSearchBase;
                }


                connect_env.put(Context.SECURITY_PRINCIPAL, admin_dn);
                connect_env.put(Context.SECURITY_CREDENTIALS, admin_pwd);
                LogManager.msg_auth( LogManager.LVL_DEBUG, "connect: " + admin_dn);
            }
            else
            {
                LogManager.msg_auth( LogManager.LVL_DEBUG, "Anonymous connect");
            }
            
            connect_env.put(Context.REFERRAL, "follow");
            
            ctx = new InitialLdapContext(connect_env, null);
            return true;
        }
        catch (Exception exc)
        {

            LogManager.msg_auth( LogManager.LVL_DEBUG, "connect: " + admin_name);
            connect_env.put(Context.SECURITY_PRINCIPAL, admin_name);
            try
            {
                ctx = new InitialLdapContext(connect_env, null);
                return true;
            }
            catch (Exception exception)
            {
            }
            error_txt = exc.getMessage();
            LogManager.msg_auth( LogManager.LVL_ERR, "Connect failed: " + admin_dn + ": " + exc.getMessage() );
        }
        return false;
    }

    
    @Override
    public boolean disconnect()
    {
        try
        {
            if (ctx != null)
            {
                ctx.close();
            }
            ctx = null;
            return true;
        }
        catch (Exception exc)
        {
            error_txt = exc.getMessage();
        }
        return false;
    }

   

    @Override
    public String get_user_attribute( String attr_name )
    {
        return get_user_attribute(user_context, attr_name);
    }

    @Override
    public boolean is_connected()
    {
        return ctx != null;
    }

    @Override
    public ArrayList<String> list_groups(User user) throws NamingException
    {
        String group = "group";
        if (groupIdentifier != null && !groupIdentifier.isEmpty())
            group = groupIdentifier;

        return list_dn_qry("cn", "(memberUid=" + user.getLoginName() + ")(objectClass=" + group + ")");
    }
    @Override
    public ArrayList<String> list_groups(/*String userName*/) throws NamingException
    {
        String group = "group";
        if (groupIdentifier != null && !groupIdentifier.isEmpty())
            group = groupIdentifier;

        return list_dn_qry("cn", "(objectClass=" + group + ")");
    }

    @Override
    public ArrayList<String> list_mailaliases_for_userlist( ArrayList<String> users ) throws NamingException
    {
        ArrayList<String> mail_list = new ArrayList<String>();
        if (users.isEmpty())
            return mail_list;
        // RETURN VALS


        String[] mail_fields_array = mail_field_list.split(",");
        for ( int m = 0; m < mail_fields_array.length; m++)
        {
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningAttributes(new String[] {mail_fields_array[m]});
            // USER ROOT
            String rootSearchBase = get_user_search_base();
            // BUILD ORED LIST OF DNs
            StringBuffer ldap_qry = new StringBuffer();
            if (users.size() > 1)
            {
                ldap_qry.append("(|");
                for (int i = 0; i < users.size(); i++)
                {
                    String string = users.get(i);
                    ldap_qry.append("(" + search_attribute + "=" + string + ")");
                }
                ldap_qry.append(")");
            }
            else
            {
                ldap_qry.append("(" + search_attribute + "=" + users.get(0) + ")");
            }

            LogManager.msg_auth( LogManager.LVL_DEBUG, "list_mailaliases_for_userlist: " + rootSearchBase + " " + ldap_qry);

            NamingEnumeration<SearchResult> results = ctx.search(rootSearchBase, ldap_qry.toString(), ctrl);
            while (results.hasMoreElements())
            {
                SearchResult searchResult = (SearchResult) results.nextElement();
                
                String field = mail_fields_array[m];
                String mail = null;
                Attribute field_attribute = searchResult.getAttributes().get(field);
                if ( field_attribute != null)
                {
                    mail = field_attribute.get().toString();
                }
                if (mail != null && mail.length() > 0)
                {
                    if (!mail_list.contains(mail))
                        mail_list.add(mail);
                }
            }
        }
        return mail_list;
    }

    @Override
    public ArrayList<String> list_users_for_group( String group ) throws NamingException
    {
        String ldapfilter_term = "";
        if (ldapfilter != null && ldapfilter.length() > 0)
        {
            ldapfilter_term = "(" + ldapfilter + ")";
        }

        String ldap_qry = "(" + search_attribute + "=*)";

        if (group != null && group.length() > 0)
        {
            ldap_qry = "(&(" + search_attribute + "=*)(memberOf=CN=" + group + ")" + ldapfilter_term + ")";
        }
        else if(ldapfilter_term.length() > 0)
        {
            ldap_qry = "(&(" + search_attribute + "=*)" + ldapfilter_term + ")";
        }

        return list_dn_qry(search_attribute, ldap_qry);
    }

    // DETECT WHICH LOGIN METHOD IS CORRECT
    static boolean open_user_succeeded = false;
    @Override
    public boolean open_user_context( String user_principal, String pwd )
    {
        user_context = null;
        if (!open_user_succeeded)
        {
            // TRY ANONYMOUS
            user_context = open_user_anonymous(user_principal, pwd);
        }

        if (user_context == null)
        {
            // THEN WITH REAL USERAUTH
            user_context = open_user(user_principal, pwd);
            if (user_context != null)
                open_user_succeeded = true;
        }

        return user_context == null ? false : true;
    }


    LDAPUserContext open_user( String user_name, String pwd )
    {
        try
        {
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute, "cn"
                    });
            
            
            // TRY TO FETCH SEARCH ATTRIBUTE AND CN IF AVAILABLE
            
            String rootSearchBase = get_user_search_base();
            String searchFilter = "(" + search_attribute + "=" + user_name + ")";
            if (ldapfilter != null && ldapfilter.length() > 0)
            {
                searchFilter = "(&" + searchFilter + "(" + ldapfilter + ")";
            }
            
            LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user: " + rootSearchBase + " " + searchFilter);
            
            NamingEnumeration<SearchResult> enumeration = null;
            try
            {
                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);
                if (!enumeration.hasMore())
                {
                    enumeration = null;
                }
            }
            catch (Exception exc)
            {
                LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user with cn failed trying w/o cn: " + rootSearchBase + " " + searchFilter);
                enumeration = null;
            }

            if (enumeration == null)
            {
                // TRY AGAIN W/O CN ATTRIBUTE
                 ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute
                    });

                
                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);

                if (!enumeration.hasMore())
                {
                    LogManager.msg_auth( LogManager.LVL_ERR, "open_user_failed: " + rootSearchBase + " " + searchFilter);
                    return null;
                }
            }

            // NOW GET THE DN FOR THIS USER
            SearchResult sr = enumeration.next(); //This is the enumeration object obtained on step II above
            //String name = sr.getName();
            String dn = sr.getNameInNamespace();
            if (dn == null || dn.length() == 0)
                dn = sr.getName() + "," + rootSearchBase;

            String niceName = user_name;
            Attributes res_attr = sr.getAttributes();
            Attribute cn_adn = res_attr.get("cn");
            if (cn_adn != null)
                niceName = cn_adn.get().toString();


//            Attributes res_attr = sr.getAttributes();
//            Attribute adn = res_attr.get(search_attribute);
//            String user_dn = adn.get().toString();
//
//            String full_user_dn = search_attribute + "=" + user_dn + "," + rootSearchBase;
            

            // NOW GO FOR LOGIN USER WITH DN
            LdapContext user_ctx = null;
            Hashtable<String,String> connect_env = create_sec_env();


            connect_env.put(Context.SECURITY_PRINCIPAL, dn);
            connect_env.put(Context.SECURITY_CREDENTIALS, pwd);
            connect_env.put( Context.REFERRAL, "follow" );

            //ctx.reconnect(connCtls);

            try
            {
                LogManager.msg_auth( LogManager.LVL_DEBUG, "auth_user: " + dn);
                user_ctx = new InitialLdapContext(connect_env, null);
            }
            catch (NamingException namingException)
            {
                // RETRY WITH cn ATTRIBUTE IF AVAILABLE
                if (cn_adn != null)
                {                                        
                    String _user_dn = cn_adn.get().toString();
                    dn = "cn=" + _user_dn + "," + rootSearchBase;

                    connect_env.put(Context.SECURITY_PRINCIPAL, dn);

                    LogManager.msg_auth( LogManager.LVL_DEBUG, "auth_user: " + dn);
                    user_ctx = new InitialLdapContext(connect_env, null);
                }
                else
                {
                    LogManager.msg_auth( LogManager.LVL_ERR, "auth_failed: " + rootSearchBase + " " +
                                        searchFilter + ": " + namingException.getMessage());
                    return null;
                }
            }
          
            return new LDAPUserContext(dn, user_ctx, niceName);
        }
        catch (Exception namingException)
        {
            error_txt = namingException.getMessage();
            LogManager.msg_auth( LogManager.LVL_ERR, "auth_failed: " + namingException.getMessage());
            LogManager.printStackTrace(namingException);
        }
        return null;
    }


    LDAPUserContext open_user_anonymous( String user_name, String pwd )
    {
        try
        {
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute, "cn"
                    });

            // TRY TO FETCH SEARCH ATTRIBUTE AND CN IF AVAILABLE
            boolean has_cn = true;
            String rootSearchBase = get_user_search_base();

            String searchFilter = "(" + search_attribute + "=" + user_name + ")";
            int cnt  = 0;

            LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user: " + rootSearchBase + " " + searchFilter);
            /*NamingEnumeration<SearchResult> enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);
            if (!enumeration.hasMore())
            {
                return null;
            }*/

            NamingEnumeration<SearchResult> enumeration = null;
            try
            {
                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);
                if (!enumeration.hasMore())
                {
                    enumeration = null;
                }
            }
            catch (Exception exc)
            {
                LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user with cn failed trying w/o cn: " + rootSearchBase + " " + searchFilter);
                enumeration = null;
            }

            if (enumeration == null)
            {
                // TRY AGAIN W/O CN ATTRIBUTE
                 ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute
                    });

                has_cn = false;
                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);

                if (!enumeration.hasMore())
                {
                    LogManager.msg_auth( LogManager.LVL_ERR, "open_user_failed: " + rootSearchBase + " " + searchFilter);
                    return null;
                }
            }
            String niceName = user_name;

            // NOW GET THE DN FOR THIS USER
            SearchResult sr = enumeration.next(); //This is the enumeration object obtained on step II above
            Attributes res_attr = sr.getAttributes();
            Attribute adn = res_attr.get(search_attribute);
            String user_dn = adn.get().toString();
            Attribute cn_adn = res_attr.get("cn");
            if (cn_adn != null)
                niceName = cn_adn.get().toString();
            
            String full_user_dn = search_attribute + "=" + user_dn + "," + rootSearchBase;
                   
            // Set up the search controls
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(new String[0]);       // Return no attrs
            ctls.setSearchScope(SearchControls.OBJECT_SCOPE); // Search object only

            // Invoke search method that will use the LDAP "compare" operation
            NamingEnumeration answer = ctx.search( full_user_dn, "(password={0})", new Object[]{pwd}, ctls);
            if (!answer.hasMore())
            {
                if (has_cn)
                {                    
                    String cn_user_dn = cn_adn.get().toString();
                    answer = ctx.search( cn_user_dn, "(password={0})", new Object[]{pwd}, ctls);
                }
            }
            
            if (!answer.hasMore())
            {
                LogManager.msg_auth( LogManager.LVL_ERR, "auth_user_failed: " + rootSearchBase + " " + searchFilter);
                return null;
            }

            return new LDAPUserContext(user_dn, ctx, niceName);
        }
        catch (Exception namingException)
        {
            error_txt = namingException.getMessage();
             LogManager.msg_auth( LogManager.LVL_ERR, "open_user_anonymous failed: " + user_name + ": " + namingException.getMessage());
            //LogManager.printStackTrace(namingException);
        }
        return null;
    }


    String get_user_attribute( LDAPUserContext uctx, String attr_name )
    {
        Attributes search_attributes = new BasicAttributes(search_attribute, uctx.dn);

        String[] return_attributes = new String[1];
        return_attributes[0] = attr_name;

        try
        {
            String rootSearchBase = get_user_search_base();
            LogManager.msg_auth( LogManager.LVL_DEBUG, "get_user_attribute: " + rootSearchBase + " " + search_attributes);
            NamingEnumeration<SearchResult> results = uctx.ctx.search(rootSearchBase, search_attributes, return_attributes);
            if (results.hasMoreElements())
            {
                SearchResult searchResult = (SearchResult) results.nextElement();
                return searchResult.getAttributes().get(attr_name).get().toString();
            }
        }
        catch (NamingException namingException)
        {
            LogManager.msg_auth( LogManager.LVL_ERR, "get_user_attribute failed: " + attr_name + " " + namingException.getMessage());
            LogManager.printStackTrace(namingException);
        }
        return null;
    }

    void close_user( LDAPUserContext uctx )
    {
        try
        {
            if (uctx.ctx != null)
                uctx.ctx.close();
        }
        catch (NamingException namingException)
        {
        }
    }

    ArrayList<String> list_dn_qry( String attribute, String ldap_qry ) throws NamingException
    {
        SearchControls ctrl = new SearchControls();
        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctrl.setReturningAttributes(new String[]
                {
                    attribute
                });

        String rootSearchBase = get_user_search_base();

        LogManager.msg_auth( LogManager.LVL_DEBUG, "DN_Qry: " + rootSearchBase + " " + ldap_qry);
        NamingEnumeration<SearchResult> results = ctx.search(rootSearchBase, ldap_qry, ctrl);

        ArrayList<String> dn_list = new ArrayList<String>();

        while (results.hasMoreElements())
        {
            SearchResult searchResult = (SearchResult) results.nextElement();
            Attribute attr = searchResult.getAttributes().get(attribute);
            if (attr == null)
            {
                LogManager.msg_auth( LogManager.LVL_WARN, "Missing attribute " + attribute + " in result " + searchResult.toString()  );
                continue;
            }
            if (attr.get() == null || attr.get().toString().isEmpty())
            {
                LogManager.msg_auth( LogManager.LVL_WARN, "Missing attribute content" + attribute + " in result " + searchResult.toString() );
                continue;
            }
            dn_list.add( searchResult.getAttributes().get(attribute).get().toString() );
        }

        return dn_list;
    }
    

    /**
     * @param args
     */
    public static void main( String[] args )
    {
        try
        {
            LDAPAuth test = new LDAPAuth("Administrator", "helikon", "192.168.1.120", "", 0, /*flags*/0, "uid", "mail", "", "");
            if (test.connect())
            {
                LDAPUserContext uctx = test.open_user( "mark@localhost", "12345" );
                if (uctx != null)
                {
                    String mail = test.get_user_attribute(uctx, "mail");

                    test.close_user(uctx);
                }
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public User createUser( Role role, String loginName )
    {
        if (user_context == null)
            return null;


        User user = new User(user_context.dn, loginName, user_context.niceName);
        user.setRole(role);

        return user;
    }

    @Override
    public User load_user( String user_name )
    {
        try
        {
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute, "cn"
                    });


            // TRY TO FETCH SEARCH ATTRIBUTE AND CN IF AVAILABLE

            String rootSearchBase = get_user_search_base();
            String searchFilter = "(" + search_attribute + "=" + user_name + ")";
            if (ldapfilter != null && ldapfilter.length() > 0)
            {
                searchFilter = "(&" + searchFilter + "(" + ldapfilter + ")";
            }

            LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user: " + rootSearchBase + " " + searchFilter);

            NamingEnumeration<SearchResult> enumeration = null;
            try
            {
                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);
                if (!enumeration.hasMore())
                {
                    enumeration = null;
                }
            }
            catch (Exception exc)
            {
                LogManager.msg_auth( LogManager.LVL_DEBUG, "open_user with cn failed trying w/o cn: " + rootSearchBase + " " + searchFilter);
                enumeration = null;
            }

            if (enumeration == null)
            {
                // TRY AGAIN W/O CN ATTRIBUTE
                 ctrl.setReturningAttributes(new String[]
                    {
                        search_attribute
                    });


                enumeration = ctx.search(rootSearchBase, searchFilter, ctrl);

                if (!enumeration.hasMore())
                {
                    LogManager.msg_auth( LogManager.LVL_ERR, "open_user_failed: " + rootSearchBase + " " + searchFilter);
                    return null;
                }
            }

            // NOW GET THE DN FOR THIS USER
            SearchResult sr = enumeration.next(); //This is the enumeration object obtained on step II above
            //String name = sr.getName();
            String dn = sr.getNameInNamespace();
            if (dn == null || dn.length() == 0)
                dn = sr.getName() + "," + rootSearchBase;

            String niceName = user_name;
            Attributes res_attr = sr.getAttributes();
            Attribute cn_adn = res_attr.get("cn");
            if (cn_adn != null)
                niceName = cn_adn.get().toString();


            User user = new User(dn, user_name, niceName);

            ArrayList<String>groups = list_groups(user);

            user.setGroups(groups, null);

            return user;
        }
        catch (Exception exc)
        {
            LogManager.msg_auth( LogManager.LVL_ERR, "load_user_failed: " + user_name, exc);
            return null;
        }
    }

}

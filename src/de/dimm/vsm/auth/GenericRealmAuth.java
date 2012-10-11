/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;


import de.dimm.vsm.CS_Constants;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.records.Role;
import java.util.ArrayList;
import java.util.Properties;
import javax.naming.NamingException;

/**
 *
 * @author mw
 */

          

 
public abstract class GenericRealmAuth
{
    public static final int CONN_MODE_MASK = 0x000f;
    public static final int CONN_MODE_INSECURE = 0x0001;
    public static final int CONN_MODE_FALLBACK = 0x0002;
    public static final int CONN_MODE_TLS = 0x0003;
    public static final int CONN_MODE_SSL = 0x0004;

    public static final int FL_MASK = 0xfff0;
    public static final int FL_ALLOW_EMPTY_PWD = 0x0010;

    private final String DEFAULT_SSL_FACTORY = "home.shared.Utilities.DefaultSSLSocketFactory";
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

  //  Role role;
    String error_txt;
    long flags;
    String host;
    int port;
    AccountConnector act;


    public GenericRealmAuth(  long flags, String host, int port )
    {
        this.flags = flags;
        this.host = host;
        this.port = port;
    }

    public static GenericRealmAuth factory_create_realm( AccountConnector act)
    {
        GenericRealmAuth realm = null;

        String typ = act.getType();
        
        if (typ.equals(AccountConnector.TY_DBS))
        {
            realm = new DBSAuth(  );
        }
        if (typ.equals(AccountConnector.TY_AD))
        {
            realm = new ActiveDirectoryAuth( act );
        }
        if (typ.equals(AccountConnector.TY_LDAP))
        {
            realm = new LDAPAuth( act.getUsername(), act.getPwd(), act.getIp(), act.getSearchbase(), act.getPort(), act.getFlags(), act.getSearchattribute(), 
                    act.getMailattribute(), act.getLdapfilter(), act.getGroupIdentifier() );
        }
        if (typ.equals(AccountConnector.TY_POP))
        {
            realm = new POP3Auth( act.getIp(), act.getPort(), act.getFlags() );
        }
        if (typ.equals(AccountConnector.TY_IMAP))
        {
            realm = new IMAPAuth( act.getIp(), act.getPort(), act.getFlags() );
        }
        if (typ.equals(AccountConnector.TY_SMTP))
        {
            realm = new SMTPAuth( act.getIp(), act.getPort(), act.getFlags() );
        }
        
        if (realm != null)
            realm.set_params(act);

        return realm;
    }


    public abstract boolean connect();
    public abstract boolean disconnect();
    public abstract boolean is_connected();
   
    public abstract boolean open_user_context( String user_principal, String pwd );
    public abstract void close_user_context();

       
    public String get_error_txt()
    {
        return error_txt;
    }
   

  
    public ArrayList<String> list_groups() throws NamingException
    {
        return new ArrayList<String>();
    }
    public ArrayList<String> list_groups(User user) throws NamingException
    {
        return new ArrayList<String>();
    }

    // THIS IS OVERRIDDEN IN LDAP
    public ArrayList<String> list_mailaliases_for_userlist( ArrayList<String> users ) throws NamingException
    {
        ArrayList<String>mail_list = new ArrayList<String>();

//        Set<MailUser> mail_user = act.getMandant().getMailusers();
//        for (Iterator<MailUser> it = mail_user.iterator(); it.hasNext();)
//        {
//            MailUser mailUser = it.next();
//
//            for (int i = 0; i < users.size(); i++)
//            {
//                String user = users.get(i);
//
//                // IS IT CLEVER TO COMPARE W/O CASE ????
//                if (mailUser.getUsername().compareToIgnoreCase(user) == 0)
//                {
//                    // ADD NATIVE EMAIL
//                    mail_list.add(mailUser.getEmail());
//
//                    // ADD ALIASES
//                    Set<MailAddress> add_email = mailUser.getAddMailAddresses();
//                    if (add_email != null)
//                    {
//                        for (Iterator<MailAddress> it1 = add_email.iterator(); it1.hasNext();)
//                        {
//                            MailAddress mailAddress = it1.next();
//                            mail_list.add(mailAddress.getEmail());
//                        }
//                    }
//                }
//            }
//        }
        return mail_list;
    }


    public ArrayList<String> list_users_for_group( String group ) throws NamingException
    {
        ArrayList<String>users = new ArrayList<String>();

//        Set<MailUser> mail_users = act.getMandant().getMailusers();
//        for (Iterator<MailUser> it = mail_users.iterator(); it.hasNext();)
//        {
//            MailUser mailUser = it.next();
//            if ((mailUser.getFlags() & CS_Constants.ACCT_DISABLED) == CS_Constants.ACCT_DISABLED)
//                continue;
//
//            users.add( mailUser.getUsername() );
//        }
        return users;
    }
    public String get_user_attribute( String attr_name )
    {
        return null;
    }

    public String get_ssl_socket_classname( boolean with_cert )
    {
        if (with_cert)
        {
            return SSL_FACTORY;
        }
        else
        {
            return DEFAULT_SSL_FACTORY;
        }
    }

    Properties set_conn_props( Properties props, String protocol, int port )
    {
        boolean with_cert = test_flag(CS_Constants.ACCT_HAS_TLS_CERT);

        if (test_flag( CS_Constants.ACCT_USE_TLS_IF_AVAIL))
        {
            props.put("mail." + protocol + ".starttls.enable", "true");
            props.put("mail." + protocol + ".socketFactory.fallback", "true");
            props.put("mail." + protocol + ".startTLS.socketFactory.class", get_ssl_socket_classname(with_cert));
        }
        else if (test_flag( CS_Constants.ACCT_USE_TLS_FORCE))
        {
            props.put("mail." + protocol + ".starttls.enable", "true");
            props.put("mail." + protocol + ".socketFactory.fallback", "false");
            props.put("mail." + protocol + ".startTLS.socketFactory.class", get_ssl_socket_classname(with_cert));
        }
        else if (test_flag( CS_Constants.ACCT_USE_SSL))
        {
            //protocol = protocol + "s";
            props.put("mail." + protocol + ".socketFactory.port", port);
            props.put("mail." + protocol + ".socketFactory.class","dimm.home.mailarchiv.Utilities.MSSSLSocketFactory");
        }
        
        if (test_flag( CS_Constants.ACCT_HAS_TLS_CERT))
        {
            String ca_cert_file = System.getProperty("javax.net.ssl.trustStore");
            props.put("javax.net.ssl.trustStore", ca_cert_file);
        }

        // DEFAULTTIMOUT IS 10 S
        // FAILS ON IMAP LOGIN
        props.put("mail." + protocol + ".connectiontimeout", 10 * 1000);
        props.put("mail." + protocol + ".timeout", 300 * 1000);

        props.put( "mail.debug", "false");
        if (LogManager.has_auth_lvl( LogManager.LVL_DEBUG))
            props.put( "mail.debug", "true");


        return props;
    }

    boolean is_ssl()
    {
        return test_flag( CS_Constants.ACCT_USE_SSL );
    }

    protected boolean test_flag( int test_flag )
    {
        return (flags & test_flag) == test_flag;
    }

    public ArrayList<String> get_mailaliaslist_for_user( String user ) throws NamingException
    {

        ArrayList<String> user_list = new ArrayList<String>();
        user_list.add(user);
        ArrayList<String> mail_list = list_mailaliases_for_userlist(  user_list );
        if ((act.getFlags() & CS_Constants.ACCT_USER_IS_MAIL) == CS_Constants.ACCT_USER_IS_MAIL)
        {
            String user_mail = get_mail_from_user( user );
            if (!mail_list.contains(user_mail))
            {
                mail_list.add(user_mail);
            }
        }
        return mail_list;
    }

    String get_dbs_mail_for_user( String user )
    {
        String ret = null;

//        Set<MailUser> mail_users = act.getMandant().getMailusers();
//        for (Iterator<MailUser> it = mail_users.iterator(); it.hasNext();)
//        {
//            MailUser mailUser = it.next();
//
//            if (mailUser.getUsername().compareTo(user) == 0)
//            {
//                // ADD NATIVE EMAIL
//                ret = mailUser.getEmail();
//                break;
//            }
//        }
        // IF NOT IN DB WE CHECK IF FLAG (USER IS MAIL) IS SET
        if (ret == null && (act.getFlags() & CS_Constants.ACCT_USER_IS_MAIL) == CS_Constants.ACCT_USER_IS_MAIL)
        {
            ret = get_mail_from_user( ret );
        }
        return ret;
    }
    String get_mail_from_user( String user )
    {
        String ret = null;

        if ((act.getFlags() & CS_Constants.ACCT_USER_IS_MAIL) == CS_Constants.ACCT_USER_IS_MAIL)
        {
            if (act.getMailattribute() != null && act.getMailattribute().length() > 0)
            {
                if (user.indexOf('@') == -1 && act.getMailattribute().indexOf('@') == -1)
                    ret = user + "@" + act.getMailattribute();
                else
                    ret = user + act.getMailattribute();
            }
            else
            {
                ret = user;
            }
        }
        return ret;
    }


   

    private void set_params( AccountConnector act )
    {
        this.act = act;
    }

    public abstract User createUser( Role role, String loginName );
    public abstract User load_user( String user_name );

}




/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.auth;

import de.dimm.vsm.CS_Constants;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.records.Role;
import java.net.Socket;

import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;




class IMAPUserContext
{
}



public class IMAPAuth extends GenericRealmAuth
{    
    Socket imap_sock;
    Store store;


    IMAPUserContext user_context;

    IMAPAuth(  String host, int port, long flags )
    {       
        super(flags, host, port);

        if (port == 0)
        {
            port = 143;
            if (is_ssl())
                port = 993;
        }
    }
   

    @Override
    public void close_user_context()
    {
        close_user(user_context);
        user_context = null;
    }


    
    @Override
    public boolean connect()
    {
        boolean ret = false;
        try
        {
            imap_sock = new Socket(host, port);
            if (imap_sock.isConnected())
            {                
                ret = true;
                imap_sock.close();
            }            
        }
        catch (Exception exc)
        {
            error_txt = exc.getMessage();
            
        }
        return ret;
    }
    @Override
    public boolean disconnect()
    {
        try
        {
            return true;
        }
        catch (Exception exc)
        {
            error_txt = exc.getMessage();
        }
        return false;
    }


    @Override
    public boolean is_connected()
    {
        return imap_sock != null;
    }


    @Override
    public boolean open_user_context( String user_principal, String pwd )
    {
        user_context = open_user(user_principal, pwd);
        return user_context == null ? false : true;
    }
    

    IMAPUserContext open_user( String user_principal, String pwd )
    {
        Properties props = new Properties();
        props.put("mail.host", host);
        props.put("mail.port", port);

        props = set_conn_props(props, "imap", port);
        String protocol = "imap";
        try
        {
            protocol = "imap";
            if (test_flag( CS_Constants.ACCT_USE_SSL))
            {
                protocol = "imaps";
            }
            Session mailConnection = Session.getDefaultInstance(props, null);
            store = mailConnection.getStore(protocol);

            store.connect(host, port, user_principal, pwd);

            if (store.isConnected())
            {
                return new IMAPUserContext();
            }
        }
        catch (Exception exc)
        {
            LogManager.msg_auth( LogManager.LVL_ERR, "Cannot connect to IMAP server " + protocol + "://" + host + ":" + port, exc );
        }
        return null;
    }

    void close_user( IMAPUserContext uctx )
    {
        try
        {
            if (store != null)
            {
                store.close();
                store = null;
            }
        }
        catch (MessagingException messagingException)
        {
        }
    }



    @Override
    public User createUser( Role role, String loginName  )
    {
        if (store == null)
            return null;


        User user = new User(loginName, loginName, loginName);
        user.setRole(role);

        return user;
        
    }

    @Override
    public User load_user( String user_name )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
  
}

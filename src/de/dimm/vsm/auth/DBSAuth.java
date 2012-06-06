/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.auth;

import de.dimm.vsm.records.Role;







class DBSUserContext
{
    //MailUser muser;

    public DBSUserContext(/* MailUser muser*/ )
    {
        //this.muser = muser;
    }

}
public class DBSAuth extends GenericRealmAuth
{    
    
    DBSUserContext user_context;
    boolean connected = false;
    

    DBSAuth()
    {
        super(0, "", 0);
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
        connected = true;
        return connected;
    }
    @Override
    public boolean disconnect()
    {
        connected = false;
        return true;
    }
    @Override
    public boolean is_connected()
    {
        return connected;
    }

   

    @Override
    public boolean open_user_context( String user_principal, String pwd )
    {
        user_context = open_user(user_principal, pwd);
        return user_context == null ? false : true;
    }

   
    
    DBSUserContext open_user( String user_principal, String pwd )
    {
       
        return null;
    }


    void close_user( DBSUserContext uctx )
    {       
    }

    @Override
    public User createUser( Role role )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User load_user( String user_name )
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
     
}

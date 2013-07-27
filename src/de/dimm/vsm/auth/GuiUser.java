/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

import java.util.Date;

/**
 *
 * @author Administrator
 */
public class GuiUser
{
    Date lastLogin;
    User user;

    public GuiUser( User user, Date lastLogin  )
    {
        this.lastLogin = lastLogin;
        this.user = user;
    }


    @Override
    public String toString()
    {
        return user.getNiceName();
    }

    public Date getLastLogin()
    {
        return lastLogin;
    }

    public boolean isSuperUser()
    {
        return user.isAdmin();
    }

    public User getUser()
    {
        return user;
    }
    

}

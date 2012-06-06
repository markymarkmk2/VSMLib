/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class GuiWrapper
{
    long loginIdx;
    GuiServerApi api;
    User user;
    Date lastLogin;

    public GuiWrapper( long loginIdx, GuiServerApi api, User user, Date lastLogin )
    {
        this.loginIdx = loginIdx;
        this.api = api;
        this.user = user;
        this.lastLogin = lastLogin;
        
    }

    public GuiServerApi getApi()
    {
        return api;
    }

    public long getLoginIdx()
    {
        return loginIdx;
    }

    public User getUser()
    {
        return user;
    }
    
    public Date getLastLogin()
    {
        return lastLogin;
    }   
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.records.AccountConnector;
import de.dimm.vsm.records.Role;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class UserManager
{
    final HashMap<String,User> userMap;
    List<Role> roles;

    public UserManager(List<Role> roles)
    {
        this.roles = roles;
        userMap = new HashMap<>();
    }
    public boolean existsUser(String username)
    {
        return userMap.containsKey(username);
    }

    public User getUser(String username)
    {
        User u = userMap.get(username);
        if (u != null)
            return u;

        u = loadUser( username);

        userMap.put(username, u);
        
        return u;
    }

    public void addUser( String username, User user )
    {
         userMap.put(username, user);
    }


    User loadUser(String username)
    {
        try
        {
            for (int i = 0; i < roles.size(); i++)
            {
                Role role = roles.get(i);
                String filter = role.getAccountmatch();        
                if (!username.matches(filter))
                    continue;

                AccountConnector acc = role.getAccountConnector();

                GenericRealmAuth auth = GenericRealmAuth.factory_create_realm(acc);
                if (auth.connect())
                {
                    User user = auth.load_user(username);
                    if (user != null)
                    {
                        user.setRole(role);
                        return user;
                    }
                }
            }
        }
        catch (Exception exc)
        {
            LogManager.msg_auth(LogManager.LVL_ERR, "Abbruch beim Authentifizieren von " + username, exc);
        }
        return null;

    }
//
//    public List<String> getGroupsForUser( String userName )
//    {
//        User u = getUser( userName );
//        if (u != null)
//            return u.getGroups();
//
//        return null;
//    }

}

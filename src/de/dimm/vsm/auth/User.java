/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

import de.dimm.vsm.fsengine.ArrayLazyList;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.records.RoleOption;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class User implements Serializable
{
    String userName;
    String loginName;
    String niceName;
    List<String> groups;
    
    boolean ignoreAcl;
    Role role;

    public User( String userName, String loginName, String niceName )
    {
        this.userName = userName;
        this.niceName = niceName;
        this.loginName = loginName;
        
        groups = new ArrayList<String>();
    }

    public void setGroups( List<String> groups )
    {
        this.groups.clear();
        this.groups.addAll(groups);
    }

    public List<String> getGroups()
    {
        return groups;
    }

    public String getNiceName()
    {
        return niceName;
    }

    public String getUserName()
    {
        return userName;
    }

    @Override
    public String toString()
    {
        return getNiceName();
    }

    public String getLoginName()
    {
        return loginName;
    }
    


    public boolean isAdmin()
    {
        return role.hasRoleOption( RoleOption.RL_ADMIN);
    }

    public static User createSystemInternal()
    {
        User user = new User("system", "system", "system");
        Role role = new Role();
        ArrayLazyList<RoleOption> rolist = new ArrayLazyList<RoleOption>();
        rolist.add(new RoleOption(0, role, RoleOption.RL_ADMIN, 0, ""));
        role.setRoleOptions(rolist);
        user.setRole(role);
        return user;
    }

    public void setIgnoreAcl( boolean ignoreAcl )
    {
        this.ignoreAcl = ignoreAcl;
    }

    public boolean isIgnoreAcl()
    {
        return ignoreAcl;
    }

    public void setRole( Role role )
    {
        this.role = role;
    }

    public Role getRole()
    {
        return role;
    }

    


}

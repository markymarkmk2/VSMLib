/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public final class VSMAclEntry implements Serializable
{

    private final AclEntryType type;
    private final String principalName;
    protected boolean group;
    private final Set<AclEntryPermission> perms;
    private final Set<AclEntryFlag> flags;

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof VSMAclEntry)
        {
            VSMAclEntry v = (VSMAclEntry)obj;
            if (v.hashCode() == hashCode())
                return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 19 * hash + Objects.hashCode(this.principalName);
        hash = 19 * hash + (this.group ? 1 : 0);
        hash = 19 * hash + Objects.hashCode(this.perms);
        hash = 19 * hash + Objects.hashCode(this.flags);
        return hash;
    }




    // private constructor
    public VSMAclEntry(AclEntryType type,
                     String principalName, boolean isGroup,
                     Set<AclEntryPermission> perms,
                     Set<AclEntryFlag> flags)
    {
        this.type = type;
        this.principalName = principalName;
        this.group = isGroup;
        this.perms = perms;
        this.flags = flags;
    }
    /**
     * Returns the string representation of this ACL entry.
     *
     * @return  the string representation of this entry
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // who
        sb.append(principalName);
        sb.append(':');

        // permissions
        for (AclEntryPermission perm: perms) {
            sb.append(perm.name());
            sb.append('/');
        }
        sb.setLength(sb.length()-1); // drop final slash
        sb.append(':');

        // flags
        if (!flags.isEmpty()) {
            for (AclEntryFlag flag: flags) {
                sb.append(flag.name());
                sb.append('/');
            }
            sb.setLength(sb.length()-1);  // drop final slash
            sb.append(':');
        }

        // type
        sb.append(type.name());
        return sb.toString();
    }

    public AclEntryType type()
    {
        return type;
    }

    public String principalName()
    {
        return principalName;
    }

    public boolean isGroup()
    {
        return group;
    }
    

    public Set<AclEntryFlag> flags()
    {
        return flags;
    }

    public Set<AclEntryPermission> permissions()
    {
        return perms;
    }

    
}

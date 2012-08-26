/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.auth.UserManager;
import de.dimm.vsm.records.FileSystemElemAttributes;
import de.dimm.vsm.records.FileSystemElemNode;
import java.io.Serializable;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class StoragePoolQry implements Serializable
{
    User user;
    private boolean readOnly;
    private long snapShotTs;
    ArrayList<SearchEntry> slist;
    boolean showDeleted;
    
    public StoragePoolQry( User user, boolean readOnly, long snapShotTs, boolean showDeleted )
    {
        this.user = user;
        this.readOnly = readOnly;
        this.snapShotTs = snapShotTs;
        this.showDeleted = showDeleted;        
    }

    public StoragePoolQry( User user, boolean readOnly, long snapShotTs )
    {
        this(user, readOnly, snapShotTs, false);
    }

    public StoragePoolQry(  User user, ArrayList<SearchEntry> slist )
    {
        this.user = user;
        if (!user.isAdmin())
            this.readOnly = true;
        this.slist = slist;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public boolean isShowDeleted()
    {
        return showDeleted;
    }

    
    public long getSnapShotTs()
    {
        return snapShotTs;
    }

    public boolean hasSearchList()
    {
        return slist != null;
    }
    public boolean matchesSearchListTimestamp( List<SearchEntry> slist, FileSystemElemAttributes attr )
    {
        boolean ret = true;

        for (int i = 0; i < slist.size(); i++)
        {
            SearchEntry searchEntry = slist.get(i);

            boolean local_nonneg_ret = true;

            if (searchEntry.getArgType().equals(SearchEntry.ARG_ADATE) ||
                    searchEntry.getArgType().equals(SearchEntry.ARG_CDATE) ||
                    searchEntry.getArgType().equals(SearchEntry.ARG_MDATE))
            {
                long d = attr.getAccessDateMs();
                if (searchEntry.getArgType().equals(SearchEntry.ARG_CDATE))
                    d = attr.getCreationDateMs();
                if (searchEntry.getArgType().equals(SearchEntry.ARG_MDATE))
                    d = attr.getModificationDateMs();


                if (searchEntry.getArgOp().equals(SearchEntry.OP_BETWEEN))
                {
                    String[] ts = searchEntry.getArgValue().split(SearchEntry.BETWEEN_SEPERATOR);
                    long tfrom = Long.parseLong(ts[0]);
                    long ttill = Long.parseLong(ts[1]);

                    local_nonneg_ret =  (d >= tfrom && d <= ttill);
                }
                else
                {
                    long t = Long.parseLong(searchEntry.getArgValue());

                    if (searchEntry.getArgOp().equals(SearchEntry.OP_EQUAL))
                        local_nonneg_ret = (d == t);
                    else if (searchEntry.getArgOp().equals(SearchEntry.OP_LT))
                        local_nonneg_ret = (d < t);
                    else if (searchEntry.getArgOp().equals(SearchEntry.OP_LE))
                        local_nonneg_ret = (d <= t);
                    else if (searchEntry.getArgOp().equals(SearchEntry.OP_GT))
                        local_nonneg_ret = (d >= t);
                    else if (searchEntry.getArgOp().equals(SearchEntry.OP_GE))
                        local_nonneg_ret = (d > t);
                }
            }
            local_nonneg_ret = searchEntry.isPrevious_neg() ? !local_nonneg_ret : local_nonneg_ret;

            // FOUND AN AND WITH FALSE?
            if (!searchEntry.isPrevious_or() && !local_nonneg_ret)
                return false;

            if ( searchEntry.getChildren().size() > 0)
            {
                boolean child_ret = matchesSearchListTimestamp( searchEntry.getChildren(), attr );

                if (!searchEntry.isPrevious_or() && !child_ret)
                    return false;
            }

            return local_nonneg_ret;
        }

        return ret;
    }

    public boolean matchesSearchListTimestamp( FileSystemElemAttributes attr )
    {
        return matchesSearchListTimestamp(slist, attr);
    }
    boolean isAccessAllow( VSMAclEntry entry )
    {
        if (entry.type() == AclEntryType.ALLOW)
        {
            for (Iterator<AclEntryPermission> it = entry.permissions().iterator(); it.hasNext();)
            {
                AclEntryPermission aep = it.next();
                if (aep == AclEntryPermission.READ_DATA || aep == AclEntryPermission.LIST_DIRECTORY)
                {
                    return true;
                }
            }
        }
        return false;
    }
    boolean isAccessDeny( VSMAclEntry entry )
    {
        if (entry.type() == AclEntryType.DENY)
        {
            for (Iterator<AclEntryPermission> it = entry.permissions().iterator(); it.hasNext();)
            {
                AclEntryPermission aep = it.next();
                if (aep == AclEntryPermission.READ_DATA || aep == AclEntryPermission.LIST_DIRECTORY)
                {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean matchesUser( FileSystemElemNode node, FileSystemElemAttributes attr, UserManager userManager )
    {
        if (!isAllowedByVsmMapping(node))
            return false;

        if (user.isAdmin())
            return true;

        if (user.isIgnoreAcl())
            return true;

        // FIRST POSIX TODO: CHECK IF user IS POSIX USER ON THIS MACHINE
        if (node.isDirectory())
        {
            // OTHER READ EXECUTE ?
            if ((attr.getPosixMode() & 05) == 05)
                return true;
        }
        else
        {
            // OTHER READ ?
            if ((attr.getPosixMode() & 04) == 04)
                return true;
        }
        
        String aclInfoData = attr.getAclInfoData();        

        if (aclInfoData != null)
        {
            AttributeContainer ac = AttributeContainer.unserialize(aclInfoData);      
            
            // THIS SHOULD NEVER HAPPEN, ONLY IF WE CANNET INTERPRET THE ACLS -> CLIENT VERSION DOESNT MATCH
            if (ac == null)
                return true;


            List<VSMAclEntry> acls = ac.getAcl();
            for (int i = 0; i < acls.size(); i++)
            {
                VSMAclEntry vSMAclEntry = acls.get(i);

                // SKIP ALL UNNECESSARY ACLS
                if (node.isDirectory())
                {
                    if (!vSMAclEntry.permissions().contains(AclEntryPermission.LIST_DIRECTORY) &&
                            !vSMAclEntry.permissions().contains(AclEntryPermission.EXECUTE) )
                        continue;
                }
                else
                {
                    if (!vSMAclEntry.permissions().contains(AclEntryPermission.READ_DATA))
                        continue;
                }

                if (vSMAclEntry.isGroup())
                {
                    if (vSMAclEntry.principalName().equals("VORDEFINIERT\\Benutzer") || vSMAclEntry.principalName().equals("BUILTIN\\Users"))
                    {
                        if (isAccessDeny( vSMAclEntry ))
                            return false;
                        if (isAccessAllow( vSMAclEntry ))
                            return true;
                    }

                    for (int j = 0; j < user.getGroups().size(); j++)
                    {
                        // DOES THIS ACL HANDLE READ RIGHTS
                        String group = user.getGroups().get(j);
                        if (group.equalsIgnoreCase(vSMAclEntry.principalName()))
                        {                            
                            if (isAccessDeny( vSMAclEntry ))
                                return false;
                            if (isAccessAllow( vSMAclEntry ))
                                return true;
                            // TODO: ALARM, AUDIT
                        }
                    }
                }
                else
                {
                    // POSIX OWNER
                    if (vSMAclEntry.principalName().equals("OWNER@"))
                    {
                        if (ac.getUserName() != null && ac.getUserName().equalsIgnoreCase(user.getUserName()))
                        {
                            if (isAccessDeny( vSMAclEntry ))
                                return false;
                            if (isAccessAllow( vSMAclEntry ))
                                return true;
                        }
                    }
                    // POSIX GROUP
                    else if(vSMAclEntry.principalName().equals("GROUP@"))
                    {
                        List<String> groups = userManager.getGroupsForUser( ac.getUserName() );

                        for (int j = 0; j < user.getGroups().size(); j++)
                        {
                            // DOES THIS ACL HANDLE READ RIGHTS
                            String ugroup = user.getGroups().get(j);

                            for (int g = 0; g < groups.size(); g++)
                            {
                                String group = groups.get(g);
                                if (group.equalsIgnoreCase(ugroup))
                                {
                                    if (isAccessDeny( vSMAclEntry ))
                                        return false;
                                    if (isAccessAllow( vSMAclEntry ))
                                        return true;
                                    // TODO: ALARM, AUDIT
                                }
                            }
                        }
                    }
                    // POSIX OTHER
                    else if(vSMAclEntry.principalName().equals("EVERYONE@") || vSMAclEntry.principalName().equals("\\Everyone"))
                    {
                        if (isAccessDeny( vSMAclEntry ))
                            return false;
                        if (isAccessAllow( vSMAclEntry ))
                            return true;
                        // TODO: ALARM, AUDIT
                    }
                    else if(vSMAclEntry.principalName().equalsIgnoreCase(user.getUserName()))
                    {
                        if (isAccessDeny( vSMAclEntry ))
                            return false;
                        if (isAccessAllow( vSMAclEntry ))
                            return true;
                            // TODO: ALARM, AUDIT
                    }
                }
            }
            return false;
        }
        // NO ACL FOUND, ALLOW EVERYTHING
        return true;
    }

    public static void build_relative_virtual_path( FileSystemElemNode file_node, StringBuilder sb )
    {
        sb.setLength(0);
        sb.insert(0, file_node.getName());
        sb.insert(0, "/");

        int max_depth = 1024;

        while( file_node.getParent() != null)
        {
            file_node = file_node.getParent();
            if (!file_node.getName().equals("/"))
            {
                sb.insert(0, file_node.getName());
                sb.insert(0, "/");
            }

            if (max_depth-- <= 0)
                throw new RuntimeException("Path_is_too_deep");
        }
    }

    private boolean isAllowedByVsmMapping(FileSystemElemNode node)
    {
        if (user.getFsMapper().isEmpty())
            return true;

        StringBuilder sb = new StringBuilder();
        build_relative_virtual_path ( node, sb );
        String path = sb.toString();

        return user.getFsMapper().isAllowed( path );
    }

}

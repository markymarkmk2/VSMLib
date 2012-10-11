/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

import de.dimm.vsm.fsengine.ArrayLazyList;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.records.RoleOption;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class User implements Serializable
{
    String userName;
    String loginName;
    String niceName;
    Map<String,Integer> groups;

    public boolean isAllowed( String principalName )
    {
        String principalUsername = principalName;
        int domainIndex = principalName.lastIndexOf("\\");
        if (domainIndex >= 0)
            principalUsername = principalName.substring(domainIndex + 1);

        if (principalUsername.equalsIgnoreCase(userName) || principalUsername.equalsIgnoreCase(loginName))
            return true;

        return false;
    }

    public boolean isMemberOfGroup( String gname )
    {
        return groups.containsKey(gname);
//        for (int j = 0; j < groups.size(); j++)
//        {
//            String group = groups.get(j);
//            if (group.equalsIgnoreCase(principalName))
//            {
//                return true;
//            }
//        }
//        return false;
    }

    public class VsmFsMapper implements Serializable
    {
        List<VsmFsEntry> vsmList;

        public VsmFsMapper()
        {
            this.vsmList = new ArrayList<VsmFsEntry>();
        }

        public List<VsmFsEntry> getVsmList()
        {
            return vsmList;
        }


        public boolean isEmpty()
        {
            return vsmList == null || vsmList.isEmpty();
        }

        public boolean isAllowed( String path )
        {
            if (isEmpty())
                return true;
            for (int i = 0; i < vsmList.size(); i++)
            {
                VsmFsEntry vsmFsEntry = vsmList.get(i);
                if (vsmFsEntry.isAllowed(path))
                    return true;
            }
            return false;
        }
        public String mapVsmToUserPath( String path )
        {
            if (isEmpty())
                return path;
            for (int i = 0; i < vsmList.size(); i++)
            {
                VsmFsEntry vsmFsEntry = vsmList.get(i);
                if (vsmFsEntry.isAllowed(path))
                {
                    String ret = vsmFsEntry.getuPath();
                    if (path.length() > vsmFsEntry.getvPathLen())
                        ret += path.substring(vsmFsEntry.getvPathLen());

                    return ret;
                }
            }
            return null;
        }

        // CREATE AND COMPACT A LIST OF ALL ALLOWED SERVER-ROOT-PATHS FOR THE LIST OF OFFERED ROOTS
        public List<RemoteFSElem> fixVsmMappingRootPath( List<RemoteFSElem> elems )
        {
            if (isEmpty())
                return elems;

            Map<String,RemoteFSElem> result = new HashMap<String, RemoteFSElem>();

            for (int r = 0; r < elems.size(); r++)
            {
                RemoteFSElem remoteFSElem = elems.get(r);

                for (int i = 0; i < vsmList.size(); i++)
                {
                    VsmFsEntry vsmFsEntry = vsmList.get(i);
                    if (vsmFsEntry.getvPath().startsWith(remoteFSElem.getPath()))
                    {
                        String path = vsmFsEntry.getvPath();
//                        if (path.length() > remoteFSElem.getPath().length() && path.charAt(remoteFSElem.getPath().length()) != '/' )
//                            continue;

                        RemoteFSElem newElem;
                        if  (remoteFSElem.getMtimeMs() == 0)
                            newElem = new RemoteFSElem(path, FileSystemElemNode.FT_DIR, System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 0 );
                        else
                            newElem = new RemoteFSElem(path, FileSystemElemNode.FT_DIR, remoteFSElem.getMtimeMs(), remoteFSElem.getCtimeMs(), remoteFSElem.getAtimeMs(), 0, 0 );
                        
                        result.put(vsmFsEntry.getvPath(), newElem);
                    }
                }
            }

            return new ArrayList<RemoteFSElem>(result.values());
        }
    }

    public class  VsmFsEntry  implements Serializable
    {
        int vPathLen;
        String vPath;
        String uPath;

        public VsmFsEntry( String vPath, String uPath )
        {
            this.vPath = vPath;
            this.uPath = uPath;
            vPathLen = vPath.length();
        }

        public int getvPathLen()
        {
            return vPathLen;
        }

        
        public String getuPath()
        {
            return uPath;
        }

        public String getvPath()
        {
            return vPath;
        }

        public boolean isAllowed(String path)
        {
            if (path.startsWith(getvPath()))
            {
                if (path.length() == getvPathLen())
                    return true;
                if (path.charAt(getvPathLen()) == '/')
                    return true;
            }
            return false;
        }

       
    }
    public static final String FS_MAPPINGFOLDER = "fsmapping";
    
    boolean ignoreAcl;
    Role role;
   
    VsmFsMapper fsMapper;

    public User( String userName, String loginName, String niceName )
    {
        this.userName = userName;
        this.niceName = niceName;
        this.loginName = loginName;
        
        groups = new HashMap<String,Integer>();
        
        fsMapper = new VsmFsMapper();
    }

    public void setGroups( List<String> groups, List<Integer> gids )
    {
        this.groups.clear();
        for (int i = 0; i < groups.size(); i++)
        {
            Integer gid = -1; 
            if (gids != null)
                gid = gids.get(i);
            this.groups.put(groups.get(i), gid );
        }
    }

//    public List<String> getGroups()
//    {
//        return groups;
//    }

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

    public VsmFsMapper getFsMapper()
    {
        return fsMapper;
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
        if (role.hasRoleOption(RoleOption.RL_FSMAPPINGFILE))
        {
            loadVsmMapping();
        }
    }

    public Role getRole()
    {
        return role;
    }

    private void loadVsmMapping()
    {
        fsMapper.getVsmList().clear();

        String content = null;
        for (int i = 0; i < role.getRoleOptions().size(); i++)
        {
            RoleOption opt =  role.getRoleOptions().get(i);
            if (opt.getToken() == null || !opt.getToken().equals(RoleOption.RL_FSMAPPINGFILE))
                continue;

            final File f = new File( FS_MAPPINGFOLDER,opt.getOptionStr());
            try
            {
                char[] cbuff = new char[(int)f.length()];
                FileReader fw = new FileReader(f);
                fw.read(cbuff);
                fw.close();

                content = new String(cbuff);
            }
            catch (Exception iOException)
            {
                throw new IllegalArgumentException( "Fehler beim Lesen der Mappingdatei" , iOException);
            }
            if (content == null)
                continue;

            loadVsmMappingContent( content );
        }
    }

    private void loadVsmMappingContent( String s )
    {
        s = s.replace('\r', '\n');
        String[] arr = s.split("\n");
        for (int i = 0; i < arr.length; i++)
        {
            String string = arr[i];
            if (string.trim().isEmpty())
                continue;
            if (string.charAt(0) == '#')
                continue;

            String[] entry = string.split(",");
            if (entry.length != 2)
                continue;
            String v = entry[0].trim();
            String u = entry[1].trim();
            if (v.isEmpty())
                continue;
            if (u.isEmpty())
                continue;
            if (v.charAt(0) != '/')
                continue;
            

            fsMapper.getVsmList().add( new VsmFsEntry(v, u));
        }
    }

    public String mapVsmToUserPath( String path )
    {
        return fsMapper.mapVsmToUserPath(path);
    }


    


}

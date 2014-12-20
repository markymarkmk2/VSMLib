/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

import de.dimm.vsm.fsengine.ArrayLazyList;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.Role;
import de.dimm.vsm.records.RoleOption;
import de.dimm.vsm.records.StoragePool;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    public static final String FS_MAPPINGFOLDER = "fsmapping";
    public static final String GROUP_MAPPINGFOLDER = "groupmapping";
    public static final String MAPPING_EXT = ".txt";
    
    public static final String SKIP_POSIX_ACL_OPT = "skipPosixAcl";
    
    boolean ignoreAcl= false;
    Role role;
   
    VsmFsMapper fsMapper;
    
    List<String> allowGroups = new ArrayList<>();
    List<String> denyGroups = new ArrayList<>();
    List<String> allowUsers = new ArrayList<>();
    List<String> denyUsers = new ArrayList<>();
    boolean skipPosixIntrinsicAcl = false;
    
    //Todo: Sichtbarkeit Default für Dateien ohne ACL j/n -> @EVERYONE abschalten
    

    public boolean isAllowed( String principalName )
    {
        String principalUsername = principalName;
        int domainIndex = principalName.lastIndexOf("\\");
        if (domainIndex >= 0)
            principalUsername = principalName.substring(domainIndex + 1);

        String name = principalUsername.toLowerCase();
        
        if (allowUsers.contains(name)) {
            return true;
        }
        if (denyUsers.contains(name)) {
            return false;
        }
            
            
        if (principalUsername.equalsIgnoreCase(userName) || principalUsername.equalsIgnoreCase(loginName))
            return true;

        return false;
    }

    public boolean isMemberOfGroup( String gname )
    {
        String principalGname = gname;
        int domainIndex = principalGname.lastIndexOf("\\");
        if (domainIndex >= 0)
            principalGname = principalGname.substring(domainIndex + 1);
        
        String lgname = principalGname.toLowerCase();
        if (allowGroups.contains(lgname)) {
            return true;
        }
        if (denyGroups.contains(lgname)) {
            return false;
        }
        return groups.containsKey(lgname);
    }

    public boolean skipPosixIntrinsicAcl() {
        return skipPosixIntrinsicAcl;
    }

    public class VsmFsMapper implements Serializable
    {
        List<VsmFsEntry> vsmList;
        List<VsmExcludeEntry> vsmExclList;

        public VsmFsMapper()
        {
            this.vsmList = new ArrayList<>();
            this.vsmExclList = new ArrayList<>();
        }

        public List<VsmFsEntry> getVsmList()
        {
            return vsmList;
        }

        public List<VsmExcludeEntry> getVsmExclList()
        {
            return vsmExclList;
        }
        
        public boolean isExcluded( RemoteFSElem path )
        {
            for (int i = 0; i < vsmExclList.size(); i++)
            {                
                VsmExcludeEntry entry = vsmExclList.get(i);
                if (entry.excludes(path))
                    return true;
            }
            return false;
        }
        
        public boolean isExcluded( FileSystemElemNode node )
        {
            String path = getPath(node);
            
            for (int i = 0; i < vsmExclList.size(); i++)
            {                
                VsmExcludeEntry entry = vsmExclList.get(i);
                if (entry.excludes(path, node.isDirectory()))
                    return true;
            }
            return false;
        }
        
        public boolean isExcluded( String path )
        {
            for (int i = 0; i < vsmExclList.size(); i++)
            {                
                VsmExcludeEntry entry = vsmExclList.get(i);
                if (entry.excludes(path, false))
                    return true;
            }
            return false;
        }

        public boolean isEmpty()
        {
            return vsmList.isEmpty();
        }

        public boolean isAllowed( String path )
        {
            for (int i = 0; i < vsmList.size(); i++)
            {                
                VsmFsEntry vsmFsEntry = vsmList.get(i);
                if (vsmFsEntry.isAllowed(path))
                    return true;
            }
            return false;
        }
        public boolean isReadWrite( String path )
        {
            for (int i = 0; i < vsmList.size(); i++)
            {                
                VsmFsEntry vsmFsEntry = vsmList.get(i);
                if (vsmFsEntry.isAllowed(path))
                    return vsmFsEntry.isReadWrite();
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

            Map<String,RemoteFSElem> result = new HashMap<>();
            
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
                            newElem = RemoteFSElem.createDir(path );
                        else
                            newElem = new RemoteFSElem(path, FileSystemElemNode.FT_DIR, remoteFSElem.getMtimeMs(), remoteFSElem.getCtimeMs(), remoteFSElem.getAtimeMs(), 0, 0 );
                        
                        result.put(vsmFsEntry.getvPath(), newElem);
                    }
                }
            }

            return new ArrayList<>(result.values());
        }

        

    }
    public class  VsmExcludeEntry  implements Serializable
    {
        String mask;
        boolean dirOnly;
        boolean fileOnly;
        boolean incl;

        public VsmExcludeEntry( String mask, boolean dirOnly, boolean fileOnly, boolean incl )
        {
            this.mask = mask;
            this.dirOnly = dirOnly;
            this.fileOnly = fileOnly;
            this.incl = incl;
        }        
                
        private boolean excludes( RemoteFSElem elem )
        {
            return excludes(elem.getName(), elem.isDirectory());            
        }
        
        private boolean excludes( String path, boolean isDir )
        {  
             if (!fileOnly && dirOnly && !isDir)
                return false;
             if (!dirOnly && fileOnly && isDir)
                return false;
             
            if (path.matches(mask))
            {
                return (incl) ? false : true;
            }
            return (incl) ? true : false;            
        }

    }

    private String getPath( FileSystemElemNode elem )
    {
        StringBuilder sb = new StringBuilder();
        while( elem != null)
        {                    
            if (!elem.getName().equals("/"))
            {
                sb.insert(0, elem);
                sb.insert(0, "/");
            }
            elem = elem.getParent();
        }
        return sb.toString();
    }

    public class  VsmFsEntry  implements Serializable
    {
        int vPathLen;
        String vPath;
        String uPath;
        boolean readWrite;
        String poolName;

        public VsmFsEntry( String vPath, String uPath, boolean readWrite, String poolName )
        {
            this.vPath = vPath;
            this.uPath = uPath;
            this.readWrite = readWrite;
            vPathLen = vPath.length();  
            this.poolName = poolName;
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

        public boolean isReadWrite()
        {
            return readWrite;
        }

        public String getPoolName()
        {
            return poolName;
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
        

        @Override
        public String toString() {
            return vPath + " -> " + uPath;
        }

        public boolean isPool( StoragePool pool )
        {
            if (StringUtils.isEmpty(poolName))
                return true;
            
            return poolName.equals(pool.getName());
        }
   
    }


    public User( String userName, String loginName, String niceName )
    {
        this.userName = userName;
        this.niceName = niceName;
        this.loginName = loginName;
        
        groups = new HashMap<>();
        
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
            this.groups.put(groups.get(i).toLowerCase(), gid );
        }
    }
    public void setGroups( Set<String> groups)
    {
        this.groups.clear();
        for (String group : groups)
        {
            this.groups.put(group.toLowerCase(), -1 );
        }
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

    public VsmFsMapper getFsMapper()
    {
        return fsMapper;
    }

    public boolean hasRoleOption(String option)
    {
        return role.hasRoleOption( option );
    }
    


    public boolean isAdmin()
    {
        return role.hasRoleOption( RoleOption.RL_ADMIN);
    }

    /**
     * Das ist der User, mit dem intern gemountet wird, also muss der auch schreiben können
     * @return 
     */
    public static User createSystemInternal()
    {
        User user = new User("system", "system", "internal");
        Role role = new Role();
        ArrayLazyList<RoleOption> rolist = new ArrayLazyList<>();
        rolist.add(new RoleOption(0, role, RoleOption.RL_ADMIN, 0, ""));
        rolist.add(new RoleOption(0, role, RoleOption.RL_READ_WRITE, 0, ""));
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

    public void loadVsmMapping()
    {
        fsMapper.getVsmList().clear();

        
        for (int i = 0; i < role.getRoleOptions().size(); i++)
        {
            String content = null;
            RoleOption opt =  role.getRoleOptions().get(i);
            if (opt.getToken() == null || !opt.getToken().equals(RoleOption.RL_FSMAPPINGFILE))
                continue;

            File f = new File( FS_MAPPINGFOLDER,opt.getOptionStr() + MAPPING_EXT);
            if (!f.exists()) {
                f = new File( FS_MAPPINGFOLDER,opt.getOptionStr());
            }
           
            try (FileReader fw = new FileReader(f))
            {
                char[] cbuff = new char[(int)f.length()];
                int len = fw.read(cbuff);
                content = new String(cbuff, 0, len);
            }
            catch (Exception iOException)
            {
                throw new IllegalArgumentException( "Fehler beim Lesen der Mappingdatei" , iOException);
            }

            loadVsmMappingContent( content );
        }
    }

    private void loadVsmMappingContent( String s )
    {
        if (StringUtils.isEmpty(s))
            return;
        
        s = s.replace('\r', '\n');
        String[] arr = s.split("\n");
        for (int i = 0; i < arr.length; i++)
        {
            String string = arr[i];
            if (string.trim().isEmpty())
                continue;
            if (string.charAt(0) == '#')
                continue;
            
            if (string.startsWith("Exclude"))
            {
                String[] entry = string.split(",");
                if (entry.length < 1)
                    continue;
                
                String mask = entry[1].trim();
                if (mask.isEmpty())
                    continue;
                
                boolean dirOnly = false;
                boolean fileOnly = false;
                boolean incl = false;
                for (int j = 1; j < entry.length; j++)
                {
                    if (entry[j].trim().toLowerCase().startsWith("inc"))
                        incl = true;
                    
                    if (entry[j].trim().toLowerCase().equals("dir"))
                        dirOnly = true;
                    
                    if (entry[j].trim().toLowerCase().equals("file"))
                        fileOnly = true;
                }
                
                fsMapper.getVsmExclList().add( new VsmExcludeEntry(mask, dirOnly, fileOnly, incl));
            }
            else
            {
                String[] entry = string.split(",");
                if (entry.length < 2)
                    continue;
                String vsmPath = entry[0].trim();
                String userPath = entry[1].trim();
                if (vsmPath.isEmpty())
                    continue;
                if (userPath.isEmpty())
                    continue;
                if (vsmPath.charAt(0) != '/')
                    continue;
                
                // Read Options
                boolean readWrite = false;
                String poolName = null;
                for (int j = 2; j < entry.length; j++)
                {
                    if (entry[j].trim().toLowerCase().equals("rw"))
                    {
                        readWrite = true;
                    }
                    
                    if (entry[j].trim().toLowerCase().startsWith("pool:"))
                    {
                        poolName = entry[j].trim().substring("pool:".length());
                    }
                }
                
                fsMapper.getVsmList().add( new VsmFsEntry(vsmPath, userPath, readWrite, poolName));
            }
        }
    }

    public String mapVsmToUserPath( String path )
    {
        return fsMapper.mapVsmToUserPath(path);
    }
    
    public void loadGroupMapping()
    {
        for (int i = 0; i < role.getRoleOptions().size(); i++)
        {
            String content = null;
            RoleOption opt =  role.getRoleOptions().get(i);
            if (opt.getToken() == null || !opt.getToken().equals(RoleOption.RL_GROUPMAPPINGFILE))
                continue;

            File f = new File( GROUP_MAPPINGFOLDER,opt.getOptionStr() + MAPPING_EXT);
            if (!f.exists()) {
                f = new File( GROUP_MAPPINGFOLDER,opt.getOptionStr());
            }
           
            try (FileReader fw = new FileReader(f))
            {
                char[] cbuff = new char[(int)f.length()];
                int len = fw.read(cbuff);
                content = new String(cbuff, 0, len);
            }
            catch (Exception iOException)
            {
                throw new IllegalArgumentException( "Fehler beim Lesen der Mappingdatei" , iOException);
            }

            loadGroupMappingContent( content );
        }
    }

    private void loadGroupMappingContent( String s )
    {
        // Systax:
        /*
         * [User|Group]:<Name>:[Allow:Deny]:[User:Group]:<Name>[,<Name>...]
         */
        if (StringUtils.isEmpty(s))
            return;
        
        s = s.replace('\r', '\n');
        String[] arr = s.split("\n");
        for (int i = 0; i < arr.length; i++)
        {
            String string = arr[i].trim().toLowerCase();
            if (string.isEmpty())
            {
                continue;
            }
            if (string.charAt(0) == '#')
                continue;
            
            if (string.trim().equalsIgnoreCase(SKIP_POSIX_ACL_OPT)) {
                LogManager.msg_auth(LogManager.LVL_DEBUG, "skipPosixAcl wurde aktiviert" );     
                skipPosixIntrinsicAcl = true;
                continue;
            }
            String[] entry = string.split(":");
            if (entry.length < 5) {
                LogManager.msg_auth(LogManager.LVL_ERR, "Ungültiger Eintrag in GroupMapping, zu wenige Argumente: "  + string );            
                continue;
            }
            String type =  entry[0].trim();
            String entity =  entry[1].trim();
            String action = entry[2].trim();
            String argsType = entry[3].trim();
            String argsArr = entry[4].trim();
            String[] args = argsArr.split(",");
            if (args.length < 1) {
                LogManager.msg_auth(LogManager.LVL_ERR, "Ungültiger Eintrag in GroupMapping, zu wenige Werte: "  + string );            
                continue;
            }
            
            // Not for us
            if (!entity.equals("*")) {
                if (type.equals("user")) {
                    if (!loginName.equalsIgnoreCase(entity)) {
                        continue;
                    }                    
                }
                if (type.equals("group")) {
                    boolean found = false;
                    for ( String group: groups.keySet()) {
                        if (group.equalsIgnoreCase(entity)) {
                            found = true;
                            break;
                        }                                        
                    }
                    if (!found) {
                        continue;
                    }
                }
            }
            LogManager.msg_auth(LogManager.LVL_DEBUG, "Found valid group map entry: "  + string );     
            
            List<String> argsList = new ArrayList<>();
            for (int a = 0; a < args.length; a++) {
                argsList.add(args[a].trim());
            }
            
            LogManager.msg_auth(LogManager.LVL_DEBUG, this.toString() + ": " + action + "ing " + argsType + "(s): "  + argsArr );  
            if (argsType.equals("user") && action.equals("allow"))
            {                
                allowUsers.addAll(argsList);
            }
            if (argsType.equals("user") && action.equals("deny"))
            {
                denyUsers.addAll(argsList);
            }
            if (argsType.equals("group") && action.equals("allow"))
            {
                allowGroups.addAll(argsList);
            }
            if (argsType.equals("group") && action.equals("deny"))
            {
                allowGroups.addAll(argsList);
            }
        }
    }        
}

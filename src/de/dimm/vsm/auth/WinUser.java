/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.auth;

/**
 *
 * @author Administrator
 */
public class WinUser extends User {

    String samAccount;
    String ntDomainName;
    public WinUser(String userName, String loginName, String niceName, String samAccount, String ntDomainName)
    {
        super(userName, loginName, niceName);
        this.samAccount = samAccount;
        if (ntDomainName != null)
            this.ntDomainName = ntDomainName.toUpperCase();
    }

    public String getSamAccount()
    {
        return samAccount;
    }

    public String getNtDomainName()
    {
        return ntDomainName;
    }
    

    @Override
    public boolean isAllowed( String principalName )
    {
        if (super.isAllowed( principalName ))
            return true;

        if (samAccount != null)
        {
            String principalUsername = principalName;
            int domainIndex = principalName.lastIndexOf("\\");
            // IF WE HAVE DOMAIN IN NAME AND WE HAVE A DOMAINNAME IN USER -> COMPARE
            if (domainIndex >= 0)
            {
                if (ntDomainName != null && !ntDomainName.isEmpty())
                {
                    // WRONG DOMAIN?
                    if (!principalName.toUpperCase().startsWith(ntDomainName))
                        return false;
                }

                principalUsername = principalName.substring(domainIndex + 1);
            }

            if (principalUsername.equalsIgnoreCase(samAccount) || principalUsername.equalsIgnoreCase(userName) || principalUsername.equalsIgnoreCase(loginName))
                return true;
        }

        return false;
    }

    @Override
    public boolean isMemberOfGroup( String principalName )
    {
        if (super.isAllowed( principalName ))
            return true;

        if (samAccount != null)
        {
            String principalGroupName = principalName;
            int domainIndex = principalName.lastIndexOf("\\");
            // IF WE HAVE DOMAIN IN NAME AND WE HAVE A DOMAINNAME IN USER -> COMPARE
            if (domainIndex >= 0)
            {
                if (ntDomainName != null && !ntDomainName.isEmpty())
                {
                    // WRONG DOMAIN?
                    if (!principalName.toUpperCase().startsWith(ntDomainName))
                        return false;
                }

                principalGroupName = principalName.substring(domainIndex + 1);
            }

            if (super.isMemberOfGroup(principalGroupName))
                return true;
        }
        return false;
    }



}

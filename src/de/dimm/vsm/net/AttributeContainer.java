/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.Utilities.ZipUtilities;
import com.thoughtworks.xstream.XStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;



/**
 *
 * @author Administrator
 */
public class AttributeContainer implements Serializable
{
    static final long serialVersionUID = 1L;

    protected List<VSMAclEntry> acl;

    //UserPrincipal user; IS NOT COMPARABLE BETWEEN OSSES, WE RESOLVE

    protected String userName; // Solaris: Username "mw"  Win: accountName "STORE\\mw"
    
    
//    String userId; // Solaris: UID "60004", Win: SID "S-1-5-21-2419059004-171269979-2118980239-1000"
//    String userType; // Solaris: "user"/ "group"  Win: sidType "1"

    protected Map<String, byte[]> userAttributes;

    public List<VSMAclEntry> getAcl()
    {
        return acl;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setAcl( List<VSMAclEntry> acl )
    {
        this.acl = acl;
    }

    public void setUserAttributes( Map<String, byte[]> userAttributes )
    {
        this.userAttributes = userAttributes;
    }

    public Map<String, byte[]> getUserAttributes()
    {
        return userAttributes;
    }
    

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof AttributeContainer)
        {
            AttributeContainer ac = (AttributeContainer)obj;
            if (!ac.userName.equals(userName))
            {
                return false;
            }

            if (userAttributes == null && ac.userAttributes != null)
                return false;
            if (userAttributes != null && ac.userAttributes == null)
                return false;

            if (userAttributes != null)
            {
                if (userAttributes.size() != ac.userAttributes.size())
                    return false;

                Set<Entry<String,byte[]>> set = userAttributes.entrySet();
                for (Entry<String, byte[]> entry : set)
                {
                    byte[] v = ac.getUserAttributes().get(entry.getKey());
                    if (v == null)
                        return false;

                    byte[] _v = entry.getValue();
                    if (v.length != _v.length)
                        return false;

                    for (int i = 0; i < v.length; i++)
                    {
                        if (v[i] != _v[i])
                            return false;
                    }
                }
            }

            if (acl == null && ac.acl != null)
                return false;
            if (acl != null && ac.acl == null)
                return false;

            if (acl != null)
            {
                if (acl.size() != ac.acl.size())
                    return false;

                for (int i = 0; i < acl.size(); i++)
                {
                    VSMAclEntry vSMAclEntry = acl.get(i);
                    VSMAclEntry _vSMAclEntry = ac.acl.get(i);

                    if (!vSMAclEntry.equals(_vSMAclEntry))
                        return false;
                }
            }
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.acl);
        hash = 59 * hash + Objects.hashCode(this.userName);
        return hash;
    }



  public static String serialize( AttributeContainer c)
    {
        XStream xs = new XStream();
        try
        {
            String ds = xs.toXML(c);
            String cs = ZipUtilities.deflateString(ds);
            return cs;
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
        return null;
    }

    public static AttributeContainer unserialize( String cs)
    {
        XStream xs = new XStream();
        try
        {
            String ds = ZipUtilities.inflateString(cs);
            Object o = xs.fromXML(ds);
            if (AttributeContainer.class.isAssignableFrom(o.getClass()))
            {
                return (AttributeContainer) o;
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // who
        if (getUserName() != null)
        {
            sb.append("Owner: ");
            sb.append(getUserName());
            sb.append("\n");
        }

        if (acl != null)
        {
            for (int i = 0; i < acl.size(); i++)
            {
                VSMAclEntry vSMAclEntry = acl.get(i);
            sb.append(vSMAclEntry.toString());
            sb.append("\n");

            }
        }

        return sb.toString();
    }



    

}

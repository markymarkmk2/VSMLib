/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net;

import de.dimm.vsm.hash.StringUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *
 * @author Administrator
 */
public class IpResolver
{
    public static boolean isValidIp(String ip)
    {
        if (StringUtils.isEmpty(ip))
            return false;
        
        String[] classes = ip.split("\\.");
        if (classes.length != 4)
            return false;
        
        for (int i = 0; i < classes.length; i++)
        {
            String string = classes[i];
            if (StringUtils.isEmpty(string))
                return false;
            if (!Character.isDigit(string.charAt(0)))
                return false;
            
            try
            {
                int n = Integer.parseInt(string);
                if (n < 0 || n > 255)
                    throw new IOException("Format error");
            }
            catch (NumberFormatException | IOException exc)
            {
                return false;
            }            
        }
        return true;        
    }
    
    public static String resolveIp(String ip )
    {
        try
        {
            InetAddress adr = InetAddress.getByName(ip);
            return adr.getHostName();
        }
        catch (UnknownHostException unknownHostException)
        {
            
        }
        return ip;        
    }
    
}

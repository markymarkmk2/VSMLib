/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net;

import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.log.LogManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Administrator
 */
public class IpResolver
{
    private static Map<String,String>ipMap = new HashMap<>();
    
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
        String name = ipMap.get( ip );
        if (name == null) {
            try
            {
                long start = System.currentTimeMillis();
                
                InetAddress adr = InetAddress.getByName(ip);
                name = adr.getHostName();
                long end = System.currentTimeMillis();                
                LogManager.msg_db(LogManager.LVL_DEBUG, "Nameresolving of " + ip + "(" + name + ") took " + Long.toString(end -start) + "  ms");
            }
            catch (UnknownHostException unknownHostException)
            {
                LogManager.msg_db(LogManager.LVL_ERR, "Nameresolving of " + ip + "(" + name + ") failed: " + unknownHostException.getMessage());
                name = ip;
            }
        }
        ipMap.put( ip, name );
        return name;        
    }
    
    public static void resetCache() {
        ipMap.clear();
    }    
}

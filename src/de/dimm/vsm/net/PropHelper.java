/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class PropHelper
{

    public static int getIntProp( Properties p, String key, int def )
    {
        if (p == null)
            return def;

        String v = p.getProperty(key);
        if (v == null)
            return def;

        try
        {
            int ret = Integer.parseInt(v);
            return ret;
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return def;
    }
    public static long getLongProp( Properties p, String key, long def )
    {
        if (p == null)
            return def;

        String v = p.getProperty(key);
        if (v == null)
            return def;

        try
        {
            long ret = Long.parseLong(v);
            return ret;
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return def;
    }
    public static boolean getBoolProp( Properties p, String key, boolean def )
    {
        if (p == null)
            return def;

        String v = p.getProperty(key);
        if (v == null)
            return def;

        boolean ret = false;
        char ch = v.toLowerCase().charAt(0);
        if ((ch == '1') || (ch == 'j'))
            ret = true;

        return ret;
    }
}

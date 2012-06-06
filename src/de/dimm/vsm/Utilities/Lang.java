/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

/**
 *
 * @author Administrator
 */
public class Lang
{
    static TextProvider provider;

    public static String Txt( String key )
    {
        if (provider != null)
            return provider.Txt( key );
        else
            return key.replace('_', ' ');
    }
}

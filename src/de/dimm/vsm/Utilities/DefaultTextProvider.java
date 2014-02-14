/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

class EmptyTextProvider implements TextProvider
{

    @Override
    public String Txt( String key )
    {
        key = key.replace('_', ' ');
        return key;
    }
    @Override
    public String GuiTxt( String key )
    {
        key = key.replace('_', ' ');
        return key;
    }
}

/**
 *
 * @author Administrator
 */
public class DefaultTextProvider
{
    static TextProvider provider = new EmptyTextProvider();

    public static void setProvider( TextProvider prov)
    {
        provider = prov;
    }

    public static TextProvider getProvider()
    {
        return provider;
    }
    public static String Txt( String key )
    {
        return provider.Txt(key);
    }


}

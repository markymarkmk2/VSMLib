/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

class BaseVariableResolver implements VariableResolver
{

    @Override
    public String resolveVariableText( String s )
    {
        if (s.indexOf("$DATE") >= 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String vr = sdf.format( new Date());
            s = s.replace("$DATE", vr );
        }
        if (s.indexOf("$DATETIME") >= 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
            String vr = sdf.format( new Date());
            s = s.replace("$DATETIME", vr );
        }
        if (s.indexOf("$TIME") >= 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
            String vr = sdf.format( new Date());
            s = s.replace("$TIME", vr );
        }
        return s;
    }

   
}

/**
 *
 * @author Administrator
 */
public class DefaultVariableResolver
{
    static VariableResolver resolver = new BaseVariableResolver();

    public static void setProvider( VariableResolver prov)
    {
        resolver = prov;
    }

    public static VariableResolver getProvider()
    {
        return resolver;
    }
    
    public static String resolveVariableText( String s )
    {
        return resolver.resolveVariableText(s);
    }



}

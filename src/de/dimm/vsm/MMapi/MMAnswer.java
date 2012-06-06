/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

/**
 *
 * @author Administrator
 */
public class MMAnswer
{
    int code;
    String txt;

    public MMAnswer( int code, String txt )
    {
        this.code = code;
        this.txt = txt;
    }

    @Override
    public String toString()
    {
        return code + ": " + txt;
    }

    public int getCode()
    {
        return code;
    }

    public String getTxt()
    {
        return txt;
    }
    

}
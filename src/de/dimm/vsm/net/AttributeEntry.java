/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class AttributeEntry implements Serializable
{
    
    String name;
    byte[] data;

    public AttributeEntry( String name, byte[] data )
    {
        this.name = name;
        this.data = data;
    }


    public String getEntry()
    {
        return name;
    }

    public byte[] getData()
    {
        return data;
    }
    

    

}

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
public class HashDataResult implements Serializable
{
    String hashValue;
    byte[] data;

    public HashDataResult( String hashValue, byte[] data )
    {
        this.hashValue = hashValue;
        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }

    public String getHashValue()
    {
        return hashValue;
    }
  
}

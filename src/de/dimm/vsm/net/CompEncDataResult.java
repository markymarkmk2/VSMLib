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
public class CompEncDataResult implements Serializable
{
    
    byte[] data;
    int compLen;
    int encLen;
    String hashValue;

    public CompEncDataResult( byte[] data, int compLen, int encLen )
    {
        this.data = data;
        this.compLen = compLen;
        this.encLen = encLen;
    }

    public CompEncDataResult( byte[] data, int compLen, int encLen, String hashValue )
    {
        this.data = data;
        this.compLen = compLen;
        this.encLen = encLen;
        this.hashValue = hashValue;
    }



    public byte[] getData()
    {
        return data;
    }

    public int getCompLen()
    {
        return compLen;
    }

    public int getEncLen()
    {
        return encLen;
    }

    public String getHashValue()
    {
        return hashValue;
    }
    

  
}

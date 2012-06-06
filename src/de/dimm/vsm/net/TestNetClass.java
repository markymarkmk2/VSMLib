/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class TestNetClass implements Serializable
{

    String name = "Moin";
    ArrayList<String> list;
    int n = 42;

    public TestNetClass()
    {
        list = new ArrayList<String>();
        list.add("Tadaaah");
        list.add("Tadaaahesss");
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public ArrayList<String> getList()
    {
        return list;
    }

    public void setN( int n )
    {
        this.n = n;
    }
    




}

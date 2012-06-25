/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class AttributeList implements Serializable
{
    List<AttributeEntry> list;

    public AttributeList()
    {
        list = new ArrayList<AttributeEntry>();
    }

    void add( AttributeEntry e )
    {
         list.add(e);
    }

    public List<AttributeEntry> getList()
    {
        return list;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            AttributeEntry attributeEntry = list.get(i);
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(attributeEntry.toString());
        }
        return sb.toString();
    }

}

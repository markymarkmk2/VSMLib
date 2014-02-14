/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.text;

/**
 *
 * @author Administrator
 */
public class MissingTextException extends RuntimeException
{
    public MissingTextException( String message )
    {
        super(message);
    }        

    @Override
    public boolean equals( Object obj )
    {
        if (obj instanceof MissingTextException)
        {
            MissingTextException m = (MissingTextException)obj;
            return m.getMessage().equals(getMessage());            
        }
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
    
}

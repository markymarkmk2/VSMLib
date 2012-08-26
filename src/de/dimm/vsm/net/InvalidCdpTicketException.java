/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

/**
 *
 * @author Administrator
 */
public class InvalidCdpTicketException extends Exception {

    /**
     * Creates a new instance of <code>InvalidCdpTicketException</code> without detail message.
     */
    public InvalidCdpTicketException(CdpTicket t)
    {
        super( "Unknown Ticket " + t.toString() );
    }

}

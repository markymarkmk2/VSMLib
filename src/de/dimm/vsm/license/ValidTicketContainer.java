/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.license;

/**
 *
 * @author mw
 */
public class ValidTicketContainer
{
    LicenseTicket ticket;
    boolean valid;

    public ValidTicketContainer( LicenseTicket ticket, boolean valid )
    {
        this.ticket = ticket;
        this.valid = valid;
    }
    public boolean check()
    {
        valid = ticket.isValid();
        return valid;
    }

    public boolean isValid()
    {
        return valid;
    }

    public LicenseTicket getTicket()
    {
        return ticket;
    }

    public Object getProduct()
    {
        return ticket.getProduct();
    }

    
}

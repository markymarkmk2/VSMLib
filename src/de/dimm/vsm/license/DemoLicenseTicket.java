/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package de.dimm.vsm.license;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mw
 */
public class DemoLicenseTicket extends LicenseTicket
{
    private Date expires;

    public void createTicket( String p, int un, int mod, int day, int month, int year ) throws IOException, ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if (day <= 0 || month <= 0 || year < 2010)
            throw new IllegalArgumentException("Invalid time data");
        
        String date_str = "" + (day < 10 ? "0" : "") + day + "." + (month < 10 ? "0" : "") + month + "." + year;
        this.expires = sdf.parse(date_str);
        product = p;
        modules = mod;
        units = un;
        serial = 123456;
        type = LT_DEMO;
        setKey( calculate_key() );
    }



    /**
     * @return the expires
     */
    public Date getExpires()
    {
        return expires;
    }

    @Override
    public boolean isValid()
    {
        if (!super.isValid())
            return false;
        
        Date now = new Date();

        return now.before(expires);
    }
    @Override
    protected String get_license_hash_str()
    {
        return super.get_license_hash_str() + "," + expires.getTime();
    }

    @Override
    public String toString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
       

        return product + " expires:" + sdf.format(expires) +  " serial:" + serial + " units:" + units + " module:" + Long.toHexString(modules);
    }
    public String get_text()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        return sdf.format(expires);
    }

}
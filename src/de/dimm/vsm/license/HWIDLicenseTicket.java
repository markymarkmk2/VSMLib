/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.license;

import de.dimm.vsm.log.LogListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author mw
 */
public class HWIDLicenseTicket extends LicenseTicket
{    
    private String hwid;

    private static String VM_LICFILE = "diwhsm.reg";

    /**
     * @return the hwid
     */
    public String getHwid()
    {
        return hwid;
    }

    public void createTicket( String p, int _serial, int un, int mod, String _hw_id ) throws IOException
    {
        hwid = _hw_id;
        product = p;
        modules = mod;
        units = un;
        serial = _serial;
        type = LT_DEMO;
        setKey( calculate_key() );
    }


    static boolean test_vm_machine = false;

    public static String generate_hwid() throws IOException
    {
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements())
            {
                NetworkInterface ni = en.nextElement();

                // WE SKIP EMPTY OR TOO SHORT HW-ADDRESS, MUST BE AT LEAST 6 BYTE (48BIT)
                if (ni.getName().startsWith("lo") || ni.getHardwareAddress() == null || ni.getHardwareAddress().length < 6)
                    continue;

                byte[] mac = ni.getHardwareAddress();
                int sum = 0;
                for (int i = 0; i < 6; i++)
                {
                    byte b = mac[i];
                    sum += b;
                }
                // WE DO NOT ACCEPT EMPTY, THIS IS PROBABLY A VIRTUAL MACHINE
                if (sum == 0)
                    continue;

                if (test_vm_machine)
                    continue;

                String str_mac = new String(Base64.encode(mac), "UTF-8");
                return str_mac;
            }
        }
        catch (Exception exc)
        {
            throw new IOException(exc.getLocalizedMessage());
        }
        if (test_vm_machine)
            System.err.println("WARNING!!!! TEST VM HWID!!!!!");


        // IF WE GET HERE, WE CANNOT LICENSE VIA MAC, WE USE TIMESTAMP OF LICENSE DIRECTORY
        File lic_file = new File(VM_LICFILE);
        if (!lic_file.exists())
        {
            create_virtual_hw_lic_file();
        }

        String str_mac = read_virtual_hw_lic_file();

        return str_mac;
    }
    public static boolean is_virtual_license()
    {
        return new File(VM_LICFILE).exists();
    }
    public static String read_virtual_license()
    {
        String str_mac = read_virtual_hw_lic_file();
        return str_mac;
    }

    static void create_virtual_hw_lic_file()
    {
        File lic_file = new File(VM_LICFILE);
        Random rnd = new Random(System.currentTimeMillis());

        byte[] mac = new byte[6];

        rnd.nextBytes(mac);

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(lic_file);
            fos.write(mac);
        }
        catch (IOException iOException)
        {
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException iOException)
                {
                }
            }
        }
    }

    static String read_virtual_hw_lic_file()
    {
        File lic_file = new File(VM_LICFILE);
        if (lic_file.exists())
        {
            byte[] mac = new byte[6];
            FileInputStream fr = null;
            int rlen = 0;
            try
            {
                fr = new FileInputStream(lic_file);
                rlen = fr.read(mac);

                if (rlen == 6)
                {
                    String str_mac = new String(Base64.encode(mac), "UTF-8");
                    return str_mac;
                }
            }
            catch (IOException iOException)
            {
            }
            finally
            {
                if (fr != null)
                {
                    try
                    {
                        fr.close();
                    }
                    catch (IOException iOException)
                    {
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValid()
    {
        if (!super.isValid())
        {
            return false;
        }
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements())
            {
                byte[] mac = en.nextElement().getHardwareAddress();

                if (test_vm_machine)
                    continue;

                if (mac != null)
                {
                    String str_mac = new String(Base64.encode(mac), "UTF-8");
                    if (str_mac.compareToIgnoreCase(hwid) == 0)
                    {
                        return true;
                    }
                }
            }
            String vhwid = read_virtual_hw_lic_file();
            if (vhwid != null && vhwid.compareTo(hwid) == 0)
            {
                return true;
            }


            lastErrMessage = "HWID_does_not_match";
        }
        catch (Exception exc)
        {
            lastErrMessage = "Cannot_check_HWID: " + exc.getLocalizedMessage();
            if (ll != null)
                ll.log_msg(LogListener.LVL_ERR, LogListener.TYP_LICENSE,  lastErrMessage);
        }
        return false;
    }

    @Override
    String get_license_hash_str()
    {
        return super.get_license_hash_str() + "," +hwid;
    }
    @Override
    public String toString()
    {
        return super.toString() + " HWID:" + hwid;
    }

  
}
package de.dimm.vsm.Utilities;


public class SizeStr extends Object
{    
    public static double SI_TB_SIZE = 1e12;
    public static double SI_GB_SIZE = 1e9;
    public static double SI_MB_SIZE = 1e6;
    public static double SI_KB_SIZE = 1e3;

    public static double DV_TB_SIZE = 1024*1024*1024*1024l;
    public static double DV_GB_SIZE = 1024*1024*1024l;
    public static double DV_MB_SIZE = 1024*1024l;
    public static double DV_KB_SIZE = 1024l;
    
    double size;
    boolean _is_nok;
    static boolean defaultSiMode = true;
    boolean siMode = defaultSiMode;

    public static void setSI( boolean si)
    {
        defaultSiMode = si;
    }
    public double get_t_size()
    {
        return (siMode? SI_TB_SIZE : DV_TB_SIZE);
    }
    public double get_g_size()
    {
        return (siMode? SI_GB_SIZE : DV_GB_SIZE);
    }
    public double get_m_size()
    {
        return (siMode? SI_MB_SIZE : DV_MB_SIZE);
    }
    public double get_k_size()
    {
        return (siMode? SI_KB_SIZE : DV_KB_SIZE);
    }

    public static double get_t_size(boolean _siMode)
    {
        return (_siMode? SI_TB_SIZE : DV_TB_SIZE);
    }
    public static double get_g_size(boolean _siMode)
    {
        return (_siMode? SI_GB_SIZE : DV_GB_SIZE);
    }
    public static double get_m_size(boolean _siMode)
    {
        return (_siMode? SI_MB_SIZE : DV_MB_SIZE);
    }
    public static double get_k_size(boolean _siMode)
    {
        return (_siMode? SI_KB_SIZE : DV_KB_SIZE);
    }

    
    public SizeStr( double s )
    {
        size = s;
        _is_nok = false;
    }
    public SizeStr( double s,boolean siMode )
    {
        size = s;
        _is_nok = false;
        setSiMode( siMode );
    }

    public final void setSiMode( boolean siMode )
    {
        this.siMode = siMode;
    }
    
    public void set_nok( boolean b )
    {
        _is_nok = b;
    }
    public boolean is_nok()
    {
        return _is_nok;
    }
    public int parse(String s)
    {
        String number = s;
        String dim = "";
        size = 0;
        try
        {
            int space_idx = s.indexOf(1,' ');
            if (space_idx > 0)
            {
                number = s.substring(0, space_idx );
                dim = s.substring(space_idx + 1);
            }
            
            size = Double.parseDouble(number);
            switch(dim.charAt(0))
            {
                case 'T': size *= get_t_size(); break;
                case 'G': size *= get_g_size(); break;
                case 'M': size *= get_m_size(); break;
                case 'k': size *= get_k_size(); break;
            }
        }
        catch (Exception exc)
        {
            return 1;
        }
        return 0;
    }
    public static String format( String v )
    {
        double dv = 0;
        try
        {
            dv = Double.parseDouble(v);
            return format(dv);
        }
        catch (NumberFormatException numberFormatException)
        {
        }
        return "";

    }
    public static String format( double v )
    {
        return format(v, defaultSiMode);
    }
    public static String format( double v, boolean _si )
    {
        String postfix = "";


        if (v > 1.2e12)
        {
            postfix = "T";
            v /= get_t_size(_si);
        }
        else if (v > 1.2e9)
        {
            postfix = "G";
            v /= get_g_size(_si);
        }
        else if (v > 1.2e6)
        {
            postfix = "M";
            v /= get_m_size(_si);
        }
        else if (v > 1.2e3)
        {
            postfix = "k";
            v /= get_k_size(_si);
        }

        long l = (long)(v + 0.5);
        v -= l;
        long m = 0;
        if (l == 0)
        {
            m = (long)(v*100 + 0.5);
            v = l + m / 100.0;
        }
        else if (l < 20)
        {
            m = (long)(v*10 + 0.5);
            v = l + m / 10.0;
        }

        if (m == 0)
        {
            if (l == 0)
                return "-";

            return new Long(l).toString() + " " + postfix;
        }

        return new Double(v).toString() + " " + postfix;

    }
        
    @Override
    public String toString()
    {
        return format( size );
    }
    
    public String toString(int digits)
    {
        double v = size;
        long l = (long)(v + 0.5);
        v -= l;
        long m = 0;
        int teiler = 10;
        for (int i = 0; i < (digits-1); i++)
            teiler *= 10;
        m = (long)(v*teiler + 0.5);
        v = l + (double)m / teiler;
        return new Double(v).toString();
    }
    
    public double get_size()
    {
        return size;
    }
    public void set_size( double s )
    {
        size = s;
    }

    public static int getNormSize( long size )
    {
        if (size < 1000)
        {
            return (int) size;
        }

        size /= 1000;
        if (size < 1000)
        {
            return (int) size;
        }

        size /= 1000;
        if (size < 1000)
        {
            return (int) size;
        }

        size /= 1000;
        if (size < 1000)
        {
            return (int) size;
        }

        size /= 1000;
        return (int) size;

    }
    public static String[] getDimArray()
    {
        return new String[] {"", "k", "M", "G", "T" };
    }

    public static String getNormSizeDim( long size )
    {
        if (size < 1000)
        {
            return "";
        }

        size /= 1000;
        if (size < 1000)
        {
            return "k";
        }

        size /= 1000;
        if (size < 1000)
        {
            return "M";
        }

        size /= 1000;
        if (size < 1000)
        {
            return "G";
        }

        size /= 1000;
        return "T";
    }

    public static long getSizeFromNormSize( String text )
    {
        long s = -1;

        String[] arr = text.split(" ");

        s = Long.parseLong(arr[0]);

        if (arr.length == 1)
        {
            return s;
        }

        String dim = arr[1];


        s *= 1000;
        if (dim.equals("k"))
        {
            return s;
        }

        s *= 1000;
        if (dim.equals("M"))
        {
            return s;
        }

        s *= 1000;
        if (dim.equals("G"))
        {
            return s;
        }

        s *= 1000;
        if (dim.equals("T"))
        {
            return s;
        }

        return -1;
    }
}
            
        

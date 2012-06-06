/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;

/**
 *
 * @author Administrator
 */
public class Hex
{

    private final static char[] HEX =
    {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static long toLong( String hexadecimal ) throws NumberFormatException
    {
        char[] chars;
        char c;
        long value;
        int i;
        byte b;

        if (hexadecimal == null)
        {
            throw new IllegalArgumentException();
        }

        chars = hexadecimal.toUpperCase().toCharArray();
        if (chars.length != 16)
        {
            return Long.parseLong(hexadecimal, 16);
        }

        value = 0;
        b = 0;
        for (i = 0; i < 16; i++)
        {
            c = chars[i];
            if (c >= '0' && c <= '9')
            {
                value = ((value << 4) | (0xff & (c - '0')));
            }
            else if (c >= 'A' && c <= 'F')
            {
                value = ((value << 4) | (0xff & (c - 'A' + 10)));
            }
            else
            {
                throw new NumberFormatException("Invalid hex character: " + c);
            }
        }

        return value;
    }

    public static String fromLong( long value)
    {
        return fromLong(value, false);
    }
    public static String fromLong( long value, boolean fixedLen )
    {
        char[] hexs;
        int i;
        int c;

        if (!fixedLen)
        {
            return Long.toHexString(value).toUpperCase();
        }

        hexs = new char[16];
        for (i = 0; i < 16; i++)
        {
            c = (int) (value & 0xf);
            hexs[16 - i - 1] = HEX[c];
            value = value >> 4;
        }
        return new String(hexs);
    }

    public static void main( String[] arg )
    {
        int i;
        long[] test =
        {
            -1234567890, 1234567890, 987654321, -987654321,  0x7FFFFFFFFFFFFFFFl, 0xFFFFFFFFFFFFFFFFl, 0x1234567890FFFFFFl, 0, -1, -1l
        };
        long v;
        String s;
        long jv;
        long jjv;
        String js;

        for (i = 0; i < test.length; i++)
        {
            s = Hex.fromLong(test[i]);
            js = Long.toHexString(test[i]);
            
            v = 0;
            jv = 0;

            System.err.println("Long " + test[i] + " Hex: " + s);

            try
            {
                v = Hex.toLong(s);
                jv = Hex.toLong(js);               
            }
            catch (NumberFormatException ex)
            {
                System.err.println(ex.getMessage());
            }

            if (v != test[i])
            {
                System.err.println("Not same " + test[i] + " " + v);
                System.exit(1);
            }
            if (jv != test[i])
            {
                System.err.println("Java Not same " + test[i] + " " + jv);
                System.exit(1);
            }
           
        }
        System.err.println("Test completed satisfactory");
    }
}

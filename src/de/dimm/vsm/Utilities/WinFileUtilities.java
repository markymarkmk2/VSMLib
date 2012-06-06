/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

/**
 *
 * @author Administrator
 */
public class WinFileUtilities
{

/*			case 0xf020: return '\"';
			case 0xf021: return '*';
			case 0xf022: return '/';
			case 0xf023: return '<';
			case 0xf024: return '>';
			case 0xf025: return '?';
			case 0xf026: return '\\';
			case 0xf027: return '|';
			case 0xf028: return ' ';
			case 0xf029: return '.';
  */


    public static final String WINFORBIDDENS = "\"*/<>?\\|:";
    public static final char[] WINREPLACEMENTS_C = {0xf020, 0xf021, 0xf022, 0xf023, 0xf024, 0xf025, 0xf026, 0xf027, 0xa789 };
    public static final String WINREPLACEMENTS = new String(WINREPLACEMENTS_C);

    public static String win_to_sys_path( String path )
    {
        String[] path_arr = path.split("\\\\");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < path_arr.length; i++)
        {
            String string = path_arr[i];

            if (string.length() == 0)
                continue;

            sb.append('/');

            String sys_path = win_to_sys_name( string );
            sb.append(sys_path);
        }
        return sb.toString();
    }
    public static String sys_to_win_path( String path )
    {
        String[] path_arr = path.split("/");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < path_arr.length; i++)
        {
            String string = path_arr[i];

            if (string.length() == 0)
                continue;
            
            sb.append('\\');

            String sys_path = win_to_sys_name( string );
            sb.append(sys_path);
        }
        return sb.toString();
    }

    public static String sys_name_to_win_name( String path )
    {
        StringBuilder sb = null;

        for (int i = 0; i < path.length(); i++)
        {
            char ch = path.charAt(i);
            if (Character.isLetterOrDigit(ch))
            {
                if (sb != null)
                {
                    sb.append(ch);
                }
                continue;
            }
            int idx = WINFORBIDDENS.indexOf(ch);
            if (idx < 0)
            {
                if (sb != null)
                {
                    sb.append(ch);
                }
                continue;
            }
            if (sb == null)
            {
                sb = new StringBuilder();
                if (i > 0)
                {
                    sb.append(path.substring(0, i));
                }
            }
            if (WINREPLACEMENTS_C[idx] != 0)
            {
                sb.append(WINREPLACEMENTS_C[idx]);
            }
        
        }
        if (sb != null)
            return sb.toString();

        return path;
    }
    public static String win_to_sys_name( String path )
    {
        StringBuilder sb = null;

        for (int i = 0; i < path.length(); i++)
        {
            char ch = path.charAt(i);
            if (Character.isLetterOrDigit(ch))
            {
                if (sb != null)
                {
                    sb.append(ch);
                }
                continue;
            }
            int idx = WINREPLACEMENTS.indexOf(ch);
            if (idx < 0)
            {
                if (sb != null)
                {
                    sb.append(ch);
                }
                continue;
            }

            if (sb == null)
            {
                sb = new StringBuilder();
                if (i > 0)
                {
                    sb.append(path.substring(0, i));
                }
            }
            sb.append(WINFORBIDDENS.charAt(idx));
        }
        if (sb != null)
            return sb.toString();

        return path;
    }
    public static String set_trail_backslash( String s )
    {
        if (s.length() == 0)
            return s;
        
        if (s.charAt(s.length() - 1) == '\\')
            return s;

        return s + "\\";
    }
    public static String remove_trail_backslash( String s )
    {
        if (s.length() == 0)
            return s;

        if (s.charAt(s.length() - 1) != '\\')
            return s;

        return s.substring(0 , s.length() - 1);
    }

    public static int getFirstDirOffset( String fullpath )
    {
        // TOO SHORT TO DETERMINE!
        if (fullpath.length() <= 3)
            return -1;

        // DRIVE PATH: C:\temp"
        if (fullpath.charAt(1) == ':')
            return 3;

        // UNC \\SERVER\VOL
        if (fullpath.startsWith("\\\\"))
        {
            // SKIP VOLNAME
            int idx = fullpath.indexOf('\\', 2);
            if (idx < 0 || idx + 1 >= fullpath.length())
                return -1;

            // SKIP VOLNAME
            idx = fullpath.indexOf('\\', idx + 1);

            if (idx < 0 || idx + 1 >= fullpath.length())
                return -1;


            return idx + 1;
        }
        return -1;
    }
}

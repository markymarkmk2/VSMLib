/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import org.apache.commons.validator.EmailValidator;

/**
 *
 * @author mw
 */
public class Validator
{

    static public boolean is_valid_email(String sEmail)
    {
        try
        {
            EmailValidator emailValidator = EmailValidator.getInstance();
            return emailValidator.isValid(sEmail);
        }
        catch (Exception exception)
        {
        }
        return false;
    }
    static public boolean is_valid_name(String name, int max_len)
    {
        try
        {
            if (name.length() >0 && name.length() < max_len)
                return true;
        }
        catch (Exception exception)
        {
        }
        return false;
    }
    static public boolean is_valid_path(String path, int max_len)
    {
        try
        {
            if (path.length() >0 && path.length() < max_len)
                return true;
        }
        catch (Exception exception)
        {
        }
        return false;
    }
    static public boolean is_valid_port(String port)
    {
        try
        {
            int p = Integer.parseInt(port);
            if (p > 0 && p < 0xFFFF)
                return true;
        }
        catch (Exception exception)
        {
        }
        return false;
    }

    public static boolean is_valid_int( String text, int min, int max )
    {
        try
        {
            int p = Integer.parseInt(text);
            if (min != max)
            {
                if (p < min || p > max)
                    return false;
            }
            return true;
        }
        catch (Exception exception)
        {
        }
        return false;
    }
    public static boolean is_valid_user( String text )
    {
        boolean ret = true;
        if (text.length() < 3)
        {
            ret = false;
        }
        boolean has_ws = false;
        for (int i = 0; i < text.length(); i++)
        {
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch))
                has_ws = true;
        }
        if (has_ws)
        {
            ret = false;
        }

        return ret;
    }


    public final static String PWD_SPECIAL_CHARS = "?$%&!)(/{}[]_.,;:_#+*^§=";
    public final static String PWD_TOO_SHORT = "too_short";
    public final static String PWD_NO_DIGITS = "no_digits";
    public final static String PWD_NO_LETTERS = "no_letters";
    public final static String PWD_NO_SPECIALS = "no_specials";
    public final static String PWD_HAS_WS = "has_whitespaces";
    public static boolean is_valid_strong_pwd( String text, StringBuffer err )
    {
        boolean ret = true;

        if (text.length() < 8)
        {
            if (err != null)
                err.append(PWD_TOO_SHORT);
            ret = false;
        }
        boolean has_digit = false;
        boolean has_alpha = false;
        boolean has_special = false;
        boolean has_ws = false;

        for (int i = 0; i < text.length(); i++)
        {
            char ch = text.charAt(i);
            if (Character.isDigit(ch))
                has_digit = true;
            if (Character.isLetter(ch))
                has_alpha = true;
            if (PWD_SPECIAL_CHARS.indexOf(ch) >= 0)
                has_special = true;
            if (Character.isWhitespace(ch))
                has_ws = true;
        }

        if (!has_digit)
        {
            if (err != null)
                err.append(PWD_NO_DIGITS);
            ret = false;
        }
        if (!has_alpha)
        {
            if (err != null)
                err.append(PWD_NO_LETTERS);
            ret = false;
        }
        if (!has_special)
        {
            if (err != null)
                err.append(PWD_NO_SPECIALS);
            ret = false;
        }
        if (has_ws)
        {
            if (err != null)
                err.append(PWD_HAS_WS);
            ret = false;
        }

        return ret;
    }



}

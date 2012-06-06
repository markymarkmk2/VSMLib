/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm;


/**
 *
 * @author mw
 */
public class CS_Constants
{
    public static String HASH_ALGORITHM = "";

    private CS_Constants()
    {
    }
    public static int FILE_HASH_BLOCKSIZE = 1024*1024;


    public static final String TEXTLIST_DELIM = ",";

    public static int STREAM_BUFFER_LEN = 128*1024;

    public static String BACK_STARTDATE_FORMAT = "dd.MM.yyyy";


    public static final String[] BY_CYCLE_UNITS =
    {
        "minute","hour", "day", "week"
    };
    public static final int[] BY_CYCLE_UNITS_SECS =
    {
        60,(60*60), (60*60*24), (60*60*24*7)
    };

    // USERMODE
    public enum USERMODE
    {
        UL_INVALID,
        UL_DUMMY,
        UL_USER,
        UL_ADMIN,
        UL_SYSADMIN
    };


    // USED FOR ENCRYPTION END DECRYPTION OF INTERNAL SECRETS
    public static String get_InternalPassPhrase()
    {
        return "hrXblks4G_oip9!zf";
    }
    public static String get_KeyAlgorithm()
    {
        return "PBEWithMD5AndDES";
    }

    // 8-byte Salt
    static byte[] salt =
    {
        (byte) 0x19, (byte) 0x09, (byte) 0x58, (byte) 0x0f,
        (byte) 'h', (byte) 'e', (byte) 'l', (byte) 'i'
    };

    // THIS IS FIXED, IF USER LOOSES THIS, DATA IS LOST FOR EVER
    public static byte[] get_KeyPBESalt()
    {
        return salt;
    }
    public static int get_KeyPBEIteration()
    {
        return 13;
    }


    public static final int ACCT_DISABLED = 0x001;
    public static final int ACCT_USE_SSL = 0x002;
    public static final int ACCT_USE_TLS_IF_AVAIL = 0x004;
    public static final int ACCT_USE_TLS_FORCE = 0x008;
    public static final int ACCT_HAS_TLS_CERT = 0x010;
    public static final int ACCT_ANONYMOUS = 0x020;
    public static final int ACCT_USER_IS_MAIL = 0x040;
}

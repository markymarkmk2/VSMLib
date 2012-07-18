/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.Utilities;


import de.dimm.vsm.log.LogListener;
import de.dimm.vsm.CS_Constants;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author mw
 */
public class CryptTools
{
    public enum ENC_MODE
    {
        ENCRYPT,
        DECRYPT
    };

    static final XTEA8 xtea8Internal;
    static
    {
        xtea8Internal = new XTEA8();
        xtea8Internal.setKey("ThIsIsNoTvErYsEcUrE42!".getBytes());
    }


    public  static String getSha256String( byte[] data ) throws NoSuchAlgorithmException
    {
        MessageDigest md = null;
        md = MessageDigest.getInstance("SHA-256");
        return new String(Base64.encodeBase64(md.digest(data)));
    }
    public  static byte[] getSha256( byte[] data ) throws NoSuchAlgorithmException
    {
        MessageDigest md = null;
        md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * Computes RFC 2104-compliant HMAC signature.
     * * @param data
     * The data to be signed.
     * @param key
     * The signing key.
     * @return
     * The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws
     * java.security.SignatureException when signature generation fails
     */
    public static String calculateRFC2104HMAC( String data, String key ) throws java.security.SignatureException
    {
        String result;
        try
        {

            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = new String(Base64.encodeBase64(rawHmac));

        }
        catch (Exception e)
        {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }


    public static byte[] crypt( byte[] data, String passPhrase, int iterationCount, byte[] salt, String algorithm , LogListener ll, ENC_MODE encrypt )
    {
        try
        {
            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
            // Create the ciphers
            if (encrypt == ENC_MODE.ENCRYPT)
            {
                cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            }
            else
            {
                cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            }


            // Encrypt
            byte[] enc = cipher.doFinal(data);

            return enc;
        }

        catch (Exception ex)
        {
            if (ll != null)
                ll.log_msg(LogListener.LVL_ERR, LogListener.TYP_SECURITY, "CryptException", ex);
        }
        return null;
    }

    public static String crypt( String str, String passPhrase, int iterationCount, byte[] salt, String algorithm , LogListener ll, ENC_MODE encrypt )
    {
        try
        {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = crypt( utf8, passPhrase, iterationCount, salt, algorithm, ll, encrypt );

            // Encode bytes to base64 to get a string
            return new String( Base64.encodeBase64(enc) );
            //return new sun.misc.BASE64Encoder().encode(enc);
        }
        catch (UnsupportedEncodingException ex)
        {
            if (ll != null)
                ll.log_msg(LogListener.LVL_ERR, LogListener.TYP_SECURITY, "CryptException", ex);
        }
        return null;
    }

    public static byte[] crypt( byte[] data, String passPhrase, LogListener ll, ENC_MODE encrypt )
    {

        int iterationCount = CS_Constants.get_KeyPBEIteration();
        byte[] salt = CS_Constants.get_KeyPBESalt();
        String algorithm = CS_Constants.get_KeyAlgorithm();

        return crypt(data, passPhrase, iterationCount, salt, algorithm, ll, encrypt);
    }

    public static String crypt( String str, String passPhrase, LogListener ll, ENC_MODE encrypt )
    {
        try
        {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = crypt( utf8, passPhrase, ll, encrypt );

            // Encode bytes to base64 to get a string
            return new String( Base64.encodeBase64(enc) );
            //return new sun.misc.BASE64Encoder().encode(enc);
        }
        catch (UnsupportedEncodingException ex)
        {
            if (ll != null)
                ll.log_msg(LogListener.LVL_ERR, LogListener.TYP_SECURITY, "Crypt failed", ex);
        }
        return null;
    }



   public static String crypt_internal( String str, LogListener ll, ENC_MODE encrypt )
    {
        String passPhrase = CS_Constants.get_InternalPassPhrase();
        try
        {
            byte[] src;
            if (encrypt == ENC_MODE.DECRYPT)
            {
                src = Base64.decodeBase64(str.getBytes("UTF8"));
            }
            else
            {
                // Encode the string into bytes using utf-8
                src = str.getBytes("UTF8");
            }

            // Encrypt
            byte[] trg = crypt( src, passPhrase, ll, encrypt );
            if (trg == null)
                return null;

            if (encrypt == ENC_MODE.ENCRYPT)
            {
                // Encode bytes to base64 to get a string
                return new String( Base64.encodeBase64(trg) );
            }

            return new String( trg, "UTF8" );
            //return new sun.misc.BASE64Encoder().encode(enc);
        }
        catch (UnsupportedEncodingException ex)
        {
            if (ll != null)
                ll.log_msg(LogListener.LVL_ERR, LogListener.TYP_SECURITY, "CryptInternal failed", ex);
        }
        return null;
    }

    public static String encodeUrlsafe( byte[] hash ) throws UnsupportedEncodingException
    {
        return new String(Base64.encodeBase64(hash, false, true), "UTF8");
    }


    public static byte[] encryptXTEA8( byte[] data, int len  )
    {
        if (len % 8 != 0)
        {
            byte[] buff = new byte[ (len / 8 + 1) * 8 ];
            System.arraycopy(data, 0, buff, 0, len);
            for( int i = len; i < buff.length; i++)
                buff[i] = 0;

            xtea8Internal.encrypt(buff, 0,  buff.length);

            return buff;
        }
        else
        {
            xtea8Internal.encrypt(data, 0, data.length);
            return data;
        }
    }
    public static byte[] encryptXTEA8( byte[] data )
    {
        return encryptXTEA8(data, data.length);
    }
    public static byte[] decryptXTEA8( byte[] data, int encLen )
    {
        return decryptXTEA8(data, data.length, encLen);
    }
    public static byte[] decryptXTEA8( byte[] data, int len, int encLen )
    {
        if (len % 8 != 0)
        {
            throw new RuntimeException( "Invalid decryption Blocksize" );
        }
        else
        {
            xtea8Internal.decrypt(data, 0, len);
            if (len != encLen)
            {
                byte buff[] = new byte[encLen];
                System.arraycopy(data, 0, buff, 0, buff.length);
                data = buff;
            }
            return data;
        }
    }

    

}    


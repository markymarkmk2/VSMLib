/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class ZipUtilitiesTest {

    public static final String teststr = "            get_clen(\"askdjhaskjhdkajsawieoiu\");             "
            + "get_clen(askdjhaskjhdkajsawieoiuaskdjhaskjhdkajsawieoiuaskdjhaskjhdkajsawieoiu);            "
            + "get_clen(Thus is a onnonrepititiveText with lots of hdtus jusd78qwnx09q34nxqn9zwrgfg98473652920nxpojfhf56aölc0998rjq2jqwd9pfuqnmx xi4081308x1r);";



    public ZipUtilitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compress method, of class ZipUtilities.
     */
    @Test
    public void testCompress()
    {
        System.out.println("compress");
        String stream = teststr;
        
        System.out.println("UnCompressed len: " + stream.length());
        String result = ZipUtilities.compress(stream);
        System.out.println("Compressed len: " + result.length());
        String result2 = ZipUtilities.uncompress(result);
        assertEquals(stream, result2);
    }

   

    /**
     * Test of deflateString method, of class ZipUtilities.
     */
    @Test
    public void testDeflateString()
    {
        System.out.println("deflateString");
        String stream = teststr;
        
        
        System.out.println("UnCompressed len: " + stream.length());
        String result = ZipUtilities.deflateString(stream);
        System.out.println("Compressed len: " + result.length());
        String result2 = ZipUtilities.inflateString(result);
        assertEquals(stream, result2);

    }

    

    /**
     * Test of toBase64 method, of class ZipUtilities.
     */
    @Test
    public void testToBase64_String() throws UnsupportedEncodingException
    {
        System.out.println("toBase64");
        String stream = teststr;
        String expResult = "";
        String result = ZipUtilities.toBase64(stream);

        String result2 = ZipUtilities.fromBase64(result);

        assertEquals(stream, result2);
        
    }

   


}
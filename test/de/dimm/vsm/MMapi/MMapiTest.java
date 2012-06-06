/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.MMapi;

import java.util.List;
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
public class MMapiTest {

    public MMapiTest() {
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

    static String host = "localhost";
    static int port = 11112;
    /**
     * Test of connect method, of class MMapi.
     */
    @Test
    public void testConnect() throws Exception
    {
        System.out.println("connect");
        MMapi instance = new MMapi(host, port);
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);
        assertTrue(instance.isConnected());
        instance.disconnect();
        assertFalse(instance.isConnected());

        System.out.println("connect timeout");
        long s = System.currentTimeMillis();
        instance = new MMapi("192.168.2.234", port);

        assertFalse(instance.connect());
        assertFalse(instance.isConnected());

        long e = System.currentTimeMillis();

        assertTrue((e-s) >= 5000);

    }


    /**
     * Test of sendMM method, of class MMapi.
     */
    @Test
    public void testSendMM_String() throws Exception
    {
        System.out.println("sendMM");
        
        MMapi instance = new MMapi(host, port);
        instance.connect();

        MMAnswer result = instance.sendMM("list_jobs");
        assertNotNull( result);
        assertEquals(0, result.code);

        List<String>commands = MMapi.getAnswerList( instance._sendMM("?", 1000) );
        assertTrue(commands.size() > 10);
        
        instance.disconnect();


    }

    /**
     * Test of sendMM method, of class MMapi.
     */
    @Test
    public void testSpeedMM() throws Exception
    {
        System.out.println("sendMM");

        MMapi instance = new MMapi(host, port);
        instance.connect();

        long s = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++)
        {
            MMAnswer result = instance.sendMM("list_jobs");
            assertNotNull( result);
            assertEquals(0, result.code);
        }
        long e = System.currentTimeMillis();

        System.out.println("Speed: " +  Double.toString((e-s)/1000.0) + " ms per call");

        instance.disconnect();


    }


}
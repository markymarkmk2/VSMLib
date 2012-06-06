/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.Utilities;

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
public class WinFileUtilitiesTest {

    public WinFileUtilitiesTest() {
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
     * Test of win_to_sys_path method, of class WinFileUtilities.
     */
    @Test
    public void testWin_to_sys_path()
    {
        System.out.println("win_to_sys_path");
        String path = "c:\\test\\daten";
        String expResult = "c:/test/daten";
        String result = WinFileUtilities.win_to_sys_path(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of sys_to_win_path method, of class WinFileUtilities.
     */
    @Test
    public void testSys_to_win_path()
    {
        System.out.println("sys_to_win_path");
        String expResult = "c:\\test\\daten";
        String path = "c:/test/daten";
        String result = WinFileUtilities.sys_to_win_path(path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

    /**
     * Test of sys_name_to_win_name method, of class WinFileUtilities.
     */
    @Test
    public void testSys_name_to_win_name()
    {
        System.out.println("sys_name_to_win_name");
        String path = "Blah?:\\/'*<>";
        String expResult = path;
        String result = WinFileUtilities.sys_name_to_win_name(path);
        result = WinFileUtilities.win_to_sys_name(result);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

  
    /**
     * Test of set_trail_backslash method, of class WinFileUtilities.
     */
    @Test
    public void testSet_trail_backslash()
    {
        System.out.println("set_trail_backslash");
        String s = "C:\\daten";
        String expResult = s + "\\";
        String result = WinFileUtilities.set_trail_backslash(s);
        assertEquals(expResult, result);
        s = "C:\\";
        expResult = s;
        result = WinFileUtilities.set_trail_backslash(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

    /**
     * Test of remove_trail_backslash method, of class WinFileUtilities.
     */
    @Test
    public void testRemove_trail_backslash()
    {
        System.out.println("remove_trail_backslash");
        String s = "C:\\daten\\";
        String expResult = "C:\\daten";
        String result = WinFileUtilities.remove_trail_backslash(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getFirstDirOffset method, of class WinFileUtilities.
     */
    @Test
    public void testGetFirstDirOffset()
    {
        System.out.println("getFirstDirOffset");
        String fullpath = "C:\\daten";
        int expResult = 3;
        int result = WinFileUtilities.getFirstDirOffset(fullpath);
        assertEquals(expResult, result);

        fullpath = "C:\\";
        expResult = -1;
        result = WinFileUtilities.getFirstDirOffset(fullpath);
        assertEquals(expResult, result);

        fullpath = "\\\\SERVER\\VOL\\dir";
        expResult = 13;
        result = WinFileUtilities.getFirstDirOffset(fullpath);
        assertEquals(expResult, result);

        fullpath = "\\\\SERVER\\VOL\\";
        expResult = -1;
        result = WinFileUtilities.getFirstDirOffset(fullpath);
        assertEquals(expResult, result);

    }

}
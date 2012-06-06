/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.TestNetClass;
import java.io.InputStream;

/**
 *
 * @author Administrator
 */
public interface NamedService
{
    public String getName();

    public byte[] get_data( int len );

    public InputStream download(String filename, int len);

    public TestNetClass get_test();
    public void put_test( TestNetClass c);

}

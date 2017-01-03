/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.hessian;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 *
 * @author mark
 */
public class InetAddressDeserializer extends AbstractDeserializer {

    @Override
    public Class getType() {
        return InetAddress.class;
    }

    @Override
    public Object readObject( AbstractHessianInput in ) throws IOException {
        byte[] arr = in.readBytes();
        InetAddress addr = Inet4Address.getByAddress(arr);

        return addr; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object readObject( AbstractHessianInput in, Object[] fields ) throws IOException {
        return readObject(in); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object readObject( AbstractHessianInput in, String[] fieldNames ) throws IOException {
        return readObject(in); //To change body of generated methods, choose Tools | Templates.
    }

}

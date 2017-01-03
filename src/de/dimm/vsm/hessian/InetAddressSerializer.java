/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.hessian;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author mark
 */
public class InetAddressSerializer extends AbstractSerializer {

    @Override
    protected Class<?> getClass( Object obj ) {
        return super.getClass(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeObject( Object obj, AbstractHessianOutput out )
            throws IOException {
        if (obj == null) {
            out.writeNull();
        }
        else {
            InetAddress addr = (InetAddress) obj;
            Class cl = obj.getClass();

            if (out.addRef(obj)) {
                return;
            }

            int ref = out.writeObjectBegin(cl.getName());

            if (ref < -1) {
                out.writeString("value");
                out.writeBytes(addr.getAddress());
                out.writeMapEnd();
            }
            else {
                if (ref == -1) {
                    out.writeInt(1);
                    out.writeString("value");
                    out.writeObjectBegin(cl.getName());
                }
                out.writeBytes(addr.getAddress());
            }
        }
    }

}

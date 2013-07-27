/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.hessian;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class JDBCLazyListSerializer extends AbstractSerializer
{
  @Override
  public void writeObject(Object obj, AbstractHessianOutput out)
    throws IOException
  {
    
      out.writeNull();
  }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.hessian;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;
import de.dimm.vsm.records.FileSystemElemNode;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class FileSystemElementNodeSerializer extends AbstractSerializer
{
  @Override
  public void writeObject(Object obj, AbstractHessianOutput out)
    throws IOException
  {
    if (obj == null)
      out.writeNull();
    else {
      Class cl = obj.getClass();

      if (out.addRef(obj))
	return;
      
      int ref = out.writeObjectBegin(cl.getName());

      if (ref < -1) {
	out.writeString("value");
	out.writeLong(((FileSystemElemNode) obj).getIdx());
	out.writeMapEnd();
      }
      else {
	if (ref == -1) {
	  out.writeInt(1);
	  out.writeString("value");
	  out.writeObjectBegin(cl.getName());
	}

	out.writeLong(((FileSystemElemNode) obj).getIdx());
      }
    }
  }
}

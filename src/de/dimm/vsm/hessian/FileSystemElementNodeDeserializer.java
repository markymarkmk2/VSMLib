/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.hessian;

import com.caucho.hessian.HessianException;
import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 *
 * @author Administrator
 */
public class FileSystemElementNodeDeserializer  extends AbstractDeserializer {
  private Class _cl;
  private Constructor _constructor;
  
  public FileSystemElementNodeDeserializer()
  {
    try {
      _cl = FileSystemElementNodeDeserializer.class;
      _constructor = _cl.getConstructor((Class[])null);
    } catch (NoSuchMethodException e) {
      throw new HessianException(e);
    }
  }
  
  public Class getType()
  {
    return _cl;
  }
  
  public Object readMap(AbstractHessianInput in)
    throws IOException
  {
    int ref = in.addRef(null);
    
    long initValue = Long.MIN_VALUE;
    
    while (! in.isEnd()) {
      String key = in.readString();

      if (key.equals("value"))
	initValue = in.readUTCDate();
      else
	in.readString();
    }

    in.readMapEnd();

    Object value = create(initValue);

    in.setRef(ref, value);

    return value;
  }
  
  public Object readObject(AbstractHessianInput in,
                           Object []fields)
    throws IOException
  {
    String []fieldNames = (String []) fields;
    
    int ref = in.addRef(null);
    
    long initValue = Long.MIN_VALUE;

    for (int i = 0; i < fieldNames.length; i++) {
      String key = fieldNames[i];

      if (key.equals("value"))
	initValue = in.readLong();
      else
	in.readObject();
    }

    Object value = create(initValue);

    in.setRef(ref, value);

    return value;
  }

  private Object create(long initValue)
    throws IOException
  {
    if (initValue == Long.MIN_VALUE)
      throw new IOException(_cl.getName() + " expects name.");

    try {
      return _constructor.newInstance((Object[]) null);
    } catch (Exception e) {
      throw new IOExceptionWrapper(e);
    }
  }
}

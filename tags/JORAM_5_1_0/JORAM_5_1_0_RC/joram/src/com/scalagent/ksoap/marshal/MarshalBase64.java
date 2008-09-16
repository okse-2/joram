/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.ksoap.marshal;

import java.io.*;
import com.scalagent.ksoap.*;

public class MarshalBase64 implements Marshal {

  static byte [] BA_WORKAROUND = new byte[0];
  public static Class BYTE_ARRAY_CLASS = BA_WORKAROUND.getClass();   
    
  public Object readInstance(SoapReader reader,
                             String namespace, 
                             String name,
                             PropertyInfo expected) throws IOException {
    
    reader.parser.read();
    Object result = Base64.decode(reader.parser.readText());
    reader.parser.read();
    return result;
  }
  

  public void writeInstance(SoapWriter writer, 
                            Object obj) throws IOException {
    writer.writer.write(Base64.encode((byte[]) obj));
  }

  public void register(ClassMap cm) {
    cm.addMapping(cm.xsd,"base64Binary", 
                  MarshalBase64.BYTE_ARRAY_CLASS,this);   
    cm.addMapping(ClassMap.enc,"base64", 
                  MarshalBase64.BYTE_ARRAY_CLASS,this);
  }
}

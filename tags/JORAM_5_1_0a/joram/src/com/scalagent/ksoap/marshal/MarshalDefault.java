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
import org.kxml.parser.*;
import com.scalagent.ksoap.*;

public class MarshalDefault implements Marshal {
  
  public Object readInstance(SoapReader reader, 
                             String namespace,
                             String name,
                             PropertyInfo expected) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalDefault.readInstance(" + reader + "," + 
                       namespace + "," + 
                       name + "," + 
                       expected + ")");
    
    reader.parser.read();
    String text = reader.parser.readText();
    reader.parser.read();
    switch (name.charAt(0)) {
    case 's': return text;
    case 'i': return new Integer(Integer.parseInt(text));
    case 'l': return new Long(Long.parseLong(text));
    case 'b': return new Boolean(stringToBoolean(text));
    default: 
      KSoapTracing.log(KSoapTracing.ERROR,
                       "MarshalDefault.readInstance(...) EXCEPTION");
      throw new RuntimeException();
    }
  }

  public void writeInstance(SoapWriter writer, 
                            Object instance) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalDefault.writeInstance(" + writer + 
                       "," + instance + ")");
    writer.writer.write(instance.toString());
  }
  
  public void register(ClassMap cm) {
    cm.addMapping(cm.xsd,"int",ClassMap.INTEGER_CLASS,this); 
    cm.addMapping(cm.xsd,"long",ClassMap.LONG_CLASS,this); 
    cm.addMapping(cm.xsd,"string",ClassMap.STRING_CLASS,this); 
    cm.addMapping(cm.xsd,"boolean",ClassMap.BOOLEAN_CLASS,this); 
  }

  static boolean stringToBoolean(String s) {
    if (s == null) return false;
    s = s.trim().toLowerCase();
    return (s.equals("1") || s.equals("true"));
  }
}

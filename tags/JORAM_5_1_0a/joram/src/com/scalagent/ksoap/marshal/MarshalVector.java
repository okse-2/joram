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
import java.util.*;

import com.scalagent.ksoap.*;
import org.kxml.*;
import org.kxml.io.*;
import org.kxml.parser.*;


public class MarshalVector implements Marshal {
  public static final PropertyInfo OBJECT_TYPE = 
      new PropertyInfo("object",new Object());

  public Object readInstance(SoapReader reader,
                             String namespace,
                             String name,
                             PropertyInfo expected) throws IOException {

    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalVector.readInstance(" + reader + "," + 
                       namespace + "," + 
                       name + "," + 
                       expected + ")");

    Vector instance = new Vector();
    int i = 0;
    AbstractXmlParser xmlParser = reader.parser;
    SoapObject sO = new SoapObject(null,null);
    sO.addProperty(new PropertyInfo("item",OBJECT_TYPE),null);

    StartTag start = (StartTag) xmlParser.peek();
    String toParse = start.getAttribute(ClassMap.enc,"arrayType").getValue();
    int cut = toParse.indexOf(":Map[");
    toParse = toParse.substring(cut+5);
    cut = toParse.indexOf("]");
    toParse = toParse.substring(0,cut);
    int size = Integer.parseInt(toParse);

    xmlParser.read();
    xmlParser.skip();

    while (xmlParser.peek().getType() != Xml.END_TAG) {
      start = (StartTag) xmlParser.peek();
      Attribute attr = start.getAttribute(ClassMap.xsi,"type");
      if (attr != null) {
        String type = attr.getValue();
        cut = type.indexOf(':');
        name = type.substring(cut + 1);
        String prefix = "";
        if (cut > 0) 
          prefix = type.substring(0,cut);
        namespace = start.getPrefixMap().getNamespace(prefix);
      }
      
      Object value =
        reader.read(null,-1,namespace,name,OBJECT_TYPE);
      reader.parser.skip();

      if (value != null) {
        sO.setProperty(i++,value);
        if (size == i) {
          xmlParser.read();
          xmlParser.skip();
          break;
        } else {
          continue;
        }
      }
      xmlParser.read();
      xmlParser.skip();
    }

    for (int j = 0; j < sO.getPropertyCount(); j++)
      instance.addElement(sO.getProperty(j));
    
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalVector : instance=" + instance);
    return instance;
  }
  
  public void writeInstance(SoapWriter writer,
                            Object instance) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalVector.writeInstance(" + writer + 
                       "," + instance + ")");

    Vector vector = (Vector) instance;
    int cnt = vector.size();

    SoapPrimitive sp =
      ClassMap.getSoapPrimitive(instance.getClass().getName());
    if (cnt > 0) {
      sp = ClassMap.getSoapPrimitive(vector.firstElement().getClass().getName());
    }

    writer.writer.attribute(ClassMap.enc,"arrayType",
                            "xsd:anyType[" + cnt + "]");

    SoapObject so = new SoapObject(null,null);
    for (int i = 0; i < cnt; i++) {
      Object elem = vector.elementAt(i);
      so.addProperty(new PropertyInfo("item",elem),null);
      so.setProperty(0,elem);
      writer.writeObjectBody(so);
    }
  }
  
  public void register(ClassMap cm) {
    cm.addMapping(ClassMap.enc,
                  "Array",
                  ClassMap.VECTOR_CLASS,
                  this);
  }
}

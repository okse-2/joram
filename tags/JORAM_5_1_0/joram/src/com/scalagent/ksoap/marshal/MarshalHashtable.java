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
import org.kxml.Xml;
import org.kxml.parser.*;


public class MarshalHashtable implements Marshal {
  public static final PropertyInfo OBJECT_TYPE = 
      new PropertyInfo("object",new Object());

  public Object readInstance(SoapReader reader,
                             String namespace,
                             String name,
                             PropertyInfo expected) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalHashtable.readInstance(" + reader + "," + 
                       namespace + "," + 
                       name + "," + 
                       expected + ")");
    Hashtable instance = new Hashtable();
    AbstractXmlParser xmlParser = reader.parser;

    xmlParser.read();
    xmlParser.skip();
    while (xmlParser.peek().getType() != Xml.END_TAG) {
      SoapObject item = new ItemSoapObject(instance);
      
      xmlParser.read();
      xmlParser.skip();
      
      Object key =
        reader.read(item,0,null,null,OBJECT_TYPE);
      reader.parser.skip();
      if (key != null)
        item.setProperty(0,key);
      
      Object value =
        reader.read(item,1,null,null,OBJECT_TYPE);
      reader.parser.skip();
      if (value != null)
        item.setProperty(1,value);
      
      xmlParser.read();
      xmlParser.skip();
    }
    
    reader.parser.read();

    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalHashtable : instance=" + instance);
    return instance;
  }
  
  public void writeInstance(SoapWriter writer,
                            Object instance) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "MarshalHashtable.writeInstance(" + writer + 
                       "," + instance + ")");

    Hashtable h = (Hashtable) instance;
    SoapObject item = new SoapObject(null, null);
    item.addProperty(new PropertyInfo("key",OBJECT_TYPE),null);
    item.addProperty(new PropertyInfo("value",OBJECT_TYPE),null);
    for (Enumeration keys = h.keys(); keys.hasMoreElements();) {
      writer.writer.startTag("item");
      Object key = keys.nextElement();
      item.setProperty(0,key);
      item.setProperty(1,h.get(key));
      writer.writeObjectBody(item);
      writer.writer.endTag();
    }
  }

  class ItemSoapObject extends SoapObject {
    Hashtable h;
    int resolvedIndex = -1;
    
    ItemSoapObject(Hashtable h) {
      super(null, null);
      this.h = h;
      addProperty(new PropertyInfo("key",OBJECT_TYPE),null);
      addProperty(new PropertyInfo("value",OBJECT_TYPE),null);
    }
    
    public void setProperty(int index, Object value) {
      if (resolvedIndex == -1) {
        super.setProperty(index,value);
        resolvedIndex = index;
      } else {
        Object resolved =
          resolvedIndex == 0
          ? getProperty (0)
          : getProperty (1);
        
        if (index == 0)
          h.put(value,resolved);
        else
          h.put(resolved,value);
      }
    }
  }
  
  public void register(ClassMap cm) {
    cm.addMapping("http://xml.apache.org/xml-soap",
                  "Map",
                  new Hashtable().getClass(),
                  this);
  }
}

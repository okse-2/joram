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
package com.scalagent.ksoap;

import java.util.*;
import java.io.*;

import org.kxml.*;
import org.kxml.io.*;
import org.kxml.parser.*;

import com.scalagent.ksoap.marshal.Marshal;

public class SoapReader {
  ClassMap classMap;
  Hashtable idMap = new Hashtable();
  public AbstractXmlParser parser;
  
  class FwdRef {
    FwdRef next;
    Object obj;
    int index;
  }

  public SoapReader(AbstractXmlParser parser,
                    ClassMap classMap) {
    this.parser = parser;
    this.classMap = classMap;
  }

  public Object read() throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.read()");
    String name = null;
    SoapObject sO = null;

    while (true) {
      parser.skip();
      int type = parser.peek().getType();

      if (type == Xml.END_TAG || type == Xml.END_DOCUMENT) {
        parser.skip();
        parser.read(Xml.END_TAG, null, null);
        break;
      } else if (type != Xml.START_TAG) {
        KSoapTracing.log(KSoapTracing.ERROR,
                         "Unexpected event: " + parser.peek());
        throw new IOException("Unexpected event: " + parser.peek());
      }

      StartTag start = (StartTag) parser.peek();
     
      Object o = read(null, 
                      -1,
                      start.getNamespace(), 
                      start.getName(),
                      new PropertyInfo("object",new Object()));

      if (o != null) {
        sO.addProperty(start.getName(),o);
        parser.read();
      } else {
        sO = new SoapObject(start.getNamespace(),start.getName());
        parser.read();
        parser.skip();
      }
      name = start.getName();
    }
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.read() : sO=" + sO);
    return sO;
  }

  public Object read(Object owner, 
                     int index,
                     String namespace, 
                     String name,
                     PropertyInfo expected)
    throws IOException {
    
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.read(" +owner+ "," +
                       index + "," +
                       namespace + "," +
                       name + "," +
                       expected + ")");

    Object obj = null;
    StartTag start = (StartTag) parser.peek();
    Attribute attr = start.getAttribute(classMap.xsi,"nil");
      
    if (attr == null) 
      attr = start.getAttribute(classMap.xsi,"null");
      
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.read attr=" + attr);
    if (attr != null && stringToBoolean(attr.getValue())) {
      obj = null;
      parser.read();
      parser.skip();
      parser.read(Xml.END_TAG, null, null);
    } else {
      attr = start.getAttribute(classMap.xsi,"type");
      if (KSoapTracing.dbgReader)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "SoapReader.read attr1=" + attr);
      if (attr != null) {
        String type = attr.getValue();
        int cut = type.indexOf(':');
        name = type.substring(cut + 1);
        String prefix = "";
        if (cut > 0) 
          prefix = type.substring(0,cut);
        namespace = start.getPrefixMap().getNamespace(prefix);
        if (KSoapTracing.dbgReader)
          KSoapTracing.log(KSoapTracing.DEBUG,
                           "SoapReader.read type=" + type + 
                           "  name=" + name + 
                           "  prefix=" + prefix + 
                           "  namespace=" + namespace);
      } else if (name == null && namespace == null) {
        if (start.getAttribute(classMap.enc,"arrayType") != null) {
          namespace = classMap.enc;
          name = "Array";
        }
      }
      Marshal marshal = ClassMap.getSoapMarshal(new SoapPrimitive(namespace,name,null,null));
      if (KSoapTracing.dbgReader)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "SoapReader.read(...) marshal=" + marshal);
      if (marshal != null)
        obj = marshal.readInstance(this,namespace,name,expected);
    }
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.read(...) obj=" +obj);
    return obj;
  }

  protected void readObject(SoapObject obj) throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapReader.readObject(" + obj + ")");
    parser.read();

    int testIndex = -1;
    int sourceIndex = 0;
    int cnt = obj.getPropertyCount();
    PropertyInfo info = null;

    while (true) {
      parser.skip();
      if (parser.peek().getType() == Xml.END_TAG) break;

      StartTag start = (StartTag) parser.peek();
      String name = start.getName();
      int countdown = cnt;

      while (true) {
        if (countdown-- == 0) {
          KSoapTracing.log(KSoapTracing.ERROR,
                           "Unknwon Property: " + name);
          throw new RuntimeException("Unknwon Property: " + name);
        }
        
        if (++testIndex >= cnt) testIndex = 0;
        
        info = obj.getPropertyInfo(testIndex);
        if (info != null) {
          if (info.name == null) {
            if (testIndex == sourceIndex)
              break;
          } else {
            if (info.name.equals(name))
              break;
          }
        }
      }
      
      obj.setProperty(testIndex, 
                      read(obj,testIndex,null,null,info));
      sourceIndex = 0;
    }
    
    parser.read(Xml.END_TAG,null,null);
  }
    
  private boolean stringToBoolean(String s) {
    if (s == null) return false;
    s = s.trim().toLowerCase();
    return (s.equals("1") || s.equals("true"));
  }
}

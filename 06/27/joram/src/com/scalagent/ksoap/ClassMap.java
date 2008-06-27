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

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;
import org.kxml.*;
import com.scalagent.ksoap.marshal.*;


public class ClassMap {
  public static final Class OBJECT_CLASS = new Object().getClass();
  public static final Class STRING_CLASS = "".getClass();
  public static final Class INTEGER_CLASS = new Integer(0).getClass();
  public static final Class LONG_CLASS = new Long(0).getClass();
  public static final Class BOOLEAN_CLASS = new Boolean(true).getClass();
  public static final Class VECTOR_CLASS = new java.util.Vector().getClass();
  
  public static final String xsi = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String xsd = "http://www.w3.org/2001/XMLSchema";
  public static final String env = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final String enc = "http://schemas.xmlsoap.org/soap/encoding/";
  
  protected int cnt;
  public PrefixMap prefixMap;
  
  static final MarshalDefault DEFAULT_MARSHAL = new MarshalDefault();
  protected static Hashtable nameToClass = new Hashtable();
  protected static Hashtable classToName = new Hashtable();
  
  
  public ClassMap() {
    PrefixMap basePrefixMap =	new PrefixMap(
      new PrefixMap(PrefixMap.DEFAULT,"SOAP-ENV",env), 
      "SOAP-ENC",enc);
    prefixMap = new PrefixMap(
      new PrefixMap(basePrefixMap,"xsd", xsd), 
      "xsi",xsi);
    
    DEFAULT_MARSHAL.register(this);
  }
  
  public void addMapping(String namespace, String name, 
                         Class clazz, Marshal marshal) {
    SoapPrimitive sp = new SoapPrimitive(namespace,name,clazz,marshal);
    nameToClass.put(sp,sp.getMarshal());
    classToName.put(clazz.getName(),sp);
    if (prefixMap.getPrefix(namespace) == null) 
      prefixMap = new PrefixMap(prefixMap, "n"+(cnt++),namespace);
  }

  public static SoapPrimitive getSoapPrimitive(String className) {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "ClassMap.getSoapPrimitive(" + className + ")");
    return (SoapPrimitive) classToName.get(className);
  }

  public static Marshal getSoapMarshal(SoapPrimitive sp) {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "ClassMap.getSoapPrimitive(" + sp + ")");
    return (Marshal) nameToClass.get(sp);
  }
}

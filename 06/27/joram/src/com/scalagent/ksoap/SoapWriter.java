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
import java.util.*;

import org.kxml.*;
import org.kxml.io.*;

import com.scalagent.ksoap.marshal.Marshal;

public class SoapWriter {

  public AbstractXmlWriter writer;
  public ClassMap classMap;

  public SoapWriter(AbstractXmlWriter writer,
                    ClassMap classMap) {
    this.writer = writer;
    this.classMap = classMap;
  }

  public void write(SoapObject obj) throws IOException {
    if (KSoapTracing.dbgWriter)
      KSoapTracing.log(KSoapTracing.DEBUG,"SoapWriter.write(" + obj + ")");
    
    writer.startTag(classMap.prefixMap,
                    obj.getNamespace(),
                    obj.getName());

    if (KSoapTracing.dbgWriter)
      KSoapTracing.log(KSoapTracing.DEBUG,"SoapWriter.write : nameSpace=" + 
                       obj.getNamespace() + ", action=" + obj.getName());
    writer.attribute("id","o0");
    writer.attribute(classMap.enc,"root","1");
    writeObjectBody(obj);
    writer.endTag();
  }

  public void writeObjectBody(SoapObject obj) throws IOException {
    if (KSoapTracing.dbgWriter)
      KSoapTracing.log(KSoapTracing.DEBUG,"SoapWriter.writeObjectBody(" + obj + ")");
    PropertyInfo info = null;
    int cnt = obj.getPropertyCount();
    for (int i = 0; i < cnt; i++) {
      info = obj.getPropertyInfo(i);
      if (info != null) {
        writer.startTag(info.name);
        writeProperty(obj.getProperty(i),info);
        writer.endTag();
      }
    }
  }

  public void writeProperty(Object obj,
                               PropertyInfo propertyInfo) throws IOException {
    if (KSoapTracing.dbgWriter)
      KSoapTracing.log(KSoapTracing.DEBUG,"SoapWriter.writeProperty(" + obj + 
                       "," + propertyInfo + ")");
    if (obj == null) {
      writer.attribute(classMap.xsi,"null","true");
      return;
    }

    if (obj instanceof SoapObject)
      writeObjectBody((SoapObject) obj);
    else {
      SoapPrimitive sp = ClassMap.getSoapPrimitive(obj.getClass().getName());
      if (sp != null) {
        Marshal marshal = sp.getMarshal();
        if (marshal != null) {
          String prefix = writer.getPrefixMap().getPrefix(sp.getNameSpace());
          writer.attribute(classMap.xsi,"type",prefix+":"+sp.getName());
          marshal.writeInstance(this,obj);
        } else {
          KSoapTracing.log(KSoapTracing.ERROR,
                           "can't writeProperty (undefined).");
          throw new IOException("can't writeProperty (undefined).");
        }
      }
    }
  }
}

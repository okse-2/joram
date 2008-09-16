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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.kjndi.ksoap;

import java.lang.*;
import java.util.Vector;
import java.util.Hashtable;

import com.scalagent.ksoap.SoapObject;

public class ConversionSoapHelper {
  
  public static final String NAMESPACE = "urn:JndiService";

  public static SoapObject getSoapObject(String action, String name, Object object) {

    SoapObject sO = null;

    if (action.equals("bind")) {
      sO = getSoapBind(action,name,object);
    } else if (action.equals("lookup")) {
      sO = getSoapLookup(action,name,object);
    } else if (action.equals("rebind")) {
      sO = getSoapRebind(action,name,object);
    } else if (action.equals("unbind")) {
      sO = getSoapUnbind(action,name,object);
    }
    return sO;
  }

  private static SoapObject getSoapBind(String action, String name, Object obj) {
    SoapObject sO = new SoapObject(NAMESPACE, action);
//      sO.addProperty("xxx",xxx);
//      System.out.println("JNDI bind :  xxx = "+ xxx);//NTA tmp
    return sO;
  }

  private static SoapObject getSoapLookup(String action, String name, Object obj) {
    SoapObject sO = new SoapObject(NAMESPACE, action);
    sO.addProperty("name",name);
    return sO;
  }

  private static SoapObject getSoapRebind(String action, String name, Object obj) {
    SoapObject sO = new SoapObject(NAMESPACE, action);
//      sO.addProperty("xxx",xxx);
//      System.out.println("JNDI bind :  xxx = "+ xxx);//NTA tmp
    return sO;
  }

  private static SoapObject getSoapUnbind(String action, String name, Object obj) {
    SoapObject sO = new SoapObject(NAMESPACE, action);
    sO.addProperty("name",name);
    return sO;
  }


  /**
   * convert a SoapObject to Object
   */
  public static Object getObject(SoapObject sO) 
    throws Exception {

    Hashtable h = null;
    String nameSpace = sO.getNamespace();
    String name = sO.getName();

    if (nameSpace.equals(NAMESPACE)) {
      if (name.equals("lookupResponse") || 
          name.equals("bindResponse") ||
          name.equals("unbindResponse") ||
          name.equals("rebindResponse")) {
        Object o = sO.getProperty("return");
        h = (Hashtable) sO.getProperty("return");
      } else if (name.equals("sendResponse")) {
        return null;
      } else {
        throw new Exception("SoapObject " + name
                            + " can't be converted to a Hashtable.");
      }
    } else {
      throw new Exception("SoapObject " + nameSpace
                          + " != urn:JndiService.");
    }
    
    String className = (String) h.get("className");
    if (className == null) 
      throw new Exception("SoapObject " + name
                          + " no className found.");
    
      if (className.equals("org.objectweb.joram.client.jms.soap.SoapConnectionFactory")) {
      return com.scalagent.kjoram.ksoap.SoapConnectionFactory.decode(h);
    } else if (className.equals("org.objectweb.joram.client.jms.Queue")){
      return com.scalagent.kjoram.Queue.decode(h);
    } else if (className.equals("org.objectweb.joram.client.jms.Topic")){
      return com.scalagent.kjoram.Topic.decode(h);
    } else if (className.equals("org.objectweb.joram.client.jms.TemporaryQueue")){
      return com.scalagent.kjoram.TemporaryQueue.decode(h);
    } else if (className.equals("org.objectweb.joram.client.jms.TemporaryTopic")){
      return com.scalagent.kjoram.TemporaryTopic.decode(h);
    }
      throw new Exception("SoapObject " + className
                          + " can't be converted to an Object.");
  }
}

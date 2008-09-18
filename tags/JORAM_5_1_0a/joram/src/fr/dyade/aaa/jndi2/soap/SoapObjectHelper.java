/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.jndi2.soap;

import java.util.Hashtable;

import javax.naming.NamingException;

/**
 * The <code>SoapObjectHelper</code> class provides static methods for
 * coding and decoding objects for the SOAP protocol.
 *
 * @see SoapObjectItf
 */
public class SoapObjectHelper {
  /**
   * Codes an given object into a Hashtable transportable by the SOAP protocol.
   *
   * @exception NamingException  If the object could not be coded.
   */
  public static Hashtable soapCode(Object obj) throws NamingException {
    if (obj instanceof SoapObjectItf) {
      Hashtable res = ((SoapObjectItf) obj).code();
      res.put("className", obj.getClass().getName());
      return res;
    } else {
      throw new NamingException("Object " + obj.getClass().getName()
                                + " not codable into a SOAP Hashtable.");
    }
  }

  /**
   * Decodes a given Hashtable into an object.
   *
   * @exception NamingException  If the Hashtable could not be decoded.
   */
  public static Object soapDecode(Hashtable codedObject) throws NamingException {
    Object object = null;

    try {
      String className = (String) codedObject.get("className");
      Class clazz = Class.forName(className);
      object = clazz.newInstance();
    } catch (Throwable exc) {
      throw new NamingException("could not decode Hashtable [" + codedObject
                                + "] into an object: " + exc);
    }

    if (object instanceof SoapObjectItf) {
      ((SoapObjectItf)object).decode(codedObject);
      return object;
    } else {
      throw new NamingException("hashtable [" + codedObject
                                + "] decoded into an unexpected object ["
                                + object + "]");
    }
  }
}

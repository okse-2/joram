/*
 * Copyright (C) 2002 - INRIA
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
package com.scalagent.kjoram.ksoap;

import com.scalagent.kjoram.excepts.MessageFormatException;
import com.scalagent.kjoram.jms.*;
import com.scalagent.ksoap.SoapObject;

import java.lang.*;
import java.util.Vector;
import java.util.Hashtable;

/**
 * The <code>ConversionSoapHelper</code> class allows the conversion
 * from an AbstractJmsRequest(or AbstractJmsReply) to a SoapObject and vice versa.
 */
public class ConversionSoapHelper {
  
  public static final String NAMESPACE = "urn:ProxyService";

  /**
   * convert an AbstractJmsRequest to a SoapObject
   */
  public static SoapObject getSoapObject(AbstractJmsRequest request, 
                                         String name, 
                                         int cnxId) 
    throws MessageFormatException {
    SoapObject sO = null;

    if (request instanceof GetReply) {
      sO = getSoapObject((GetReply)request);
    } else {
      sO = new SoapObject(NAMESPACE, "send");
      sO.addProperty("name",name);
      sO.addProperty("cnxId",new Integer(cnxId));
      Hashtable h = request.soapCode();
      if (h != null)
        sO.addProperty("map",h);
    }
    return sO;
  }

  /**
   * convert a SoapObject to Object
   */
  public static Object getObject(SoapObject sO) 
    throws MessageFormatException {

    Hashtable h = null;
    String nameSpace = sO.getNamespace();
    String name = sO.getName();

    if (nameSpace.equals("urn:ProxyService")) {
      if (name.equals("getReplyResponse")) {
        Object o = sO.getProperty("return");
        h = (Hashtable) sO.getProperty("return");
      } else if (name.equals("setConnectionResponse")) {
        return new Integer(getSetCnx(sO));
      } else if (name.equals("sendResponse")) {
        return null;
      } else {
        throw new MessageFormatException("SoapObject " + name
                                         + " can't be converted to a Hashtable.");
      }
    } else {
      throw new MessageFormatException("SoapObject " + nameSpace
                                       + " != urn:ProxyService.");
    }

    String className = (String) h.get("className");
    if (className == null) 
      throw new MessageFormatException(	"SoapObject " + name
                                        + " no className found.");

      if (className.equals("org.objectweb.joram.shared.client.CnxConnectReply")){
      return CnxConnectReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.ServerReply")){
      return ServerReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.MomExceptionReply")){
      return MomExceptionReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.ProducerMessages")){
      return ProducerMessages.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.QBrowseReply")){
      return QBrowseReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.SessCreateTDReply")){
      return SessCreateTDReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.CnxCloseReply")){
      return CnxCloseReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.CnxConnectReply")){
      return CnxConnectReply.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.ConsumerMessages")){
      return ConsumerMessages.soapDecode(h);
    } else if (className.equals("org.objectweb.joram.shared.client.GetAdminTopicReply")){
      return GetAdminTopicReply.soapDecode(h);
    } else {
      throw new MessageFormatException(	"SoapObject " + className
                                        + " can't be converted to an Object.");
    }
  }
  
  static SoapObject getSoapObject(SetCnx request) {
    SoapObject sO = new SoapObject(NAMESPACE, "setConnection");
    sO.addProperty("name", request.name);
    sO.addProperty("password", request.password);
    sO.addProperty("timeout", request.timeout);
    return sO;
  }

  static int getSetCnx(SoapObject sO) {
    int ret = -1;
    try {
      ret =  ((Integer) sO.getProperty("return")).intValue();
      if (ret < 0)
        ret = -1;
    } catch (Exception e) {ret = -1;}
    return ret;
  }

  static private SoapObject getSoapObject(GetReply request) {
    SoapObject sO = new SoapObject(NAMESPACE, "getReply");
    sO.addProperty("name", request.name);
    sO.addProperty("cnxId", new Integer(request.cnxId));
    return sO;
  }
}

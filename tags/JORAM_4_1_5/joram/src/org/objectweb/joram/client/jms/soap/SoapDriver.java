/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
package org.objectweb.joram.client.jms.soap;

import org.objectweb.joram.shared.client.*;

import org.apache.soap.Constants;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.util.xml.QName;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.Hashtable;
import java.lang.reflect.Method;

/**
 * A <code>SoapDriver</code> gets server deliveries through RCP SOAP calls.
 */
class SoapDriver extends org.objectweb.joram.client.jms.Driver
{
  /** The user's name */
  private String name;
  /** The URL of the SOAP service. */
  private URL url;
  /** The object used for the RPC call. */
  private Call call;

  /**
   * Constructs a <code>SoapDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   * @param url  The URL of the SOAP service.
   * @param name The user's name.
   * @param key  Identifier of the SOAP connection.
   */
  SoapDriver(org.objectweb.joram.client.jms.Connection cnx, 
             URL url, String name, int key)
  {
    super(cnx);
    this.url = url;
    this.name = name;

    // Building the Call object:
    call = new Call();

    SOAPMappingRegistry mappingReg = new SOAPMappingRegistry();
    BeanSerializer beanSer = new BeanSerializer();

    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "AbstractJmsReply"),
                                  AbstractJmsReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxConnectReply"),
                                  CnxConnectReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "ServerReply"),
                                  ServerReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "SessCreateTDReply"),
                                  SessCreateTDReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxCloseReply"),
                                  CnxCloseReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "GetAdminTopicReply"),
                                  GetAdminTopicReply.class, beanSer, beanSer);

    call.setSOAPMappingRegistry(mappingReg);
    call.setTargetObjectURI("urn:ProxyService");
    call.setMethodName("getReply");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
        
    Vector params = new Vector();
    params.addElement(
      new Parameter("name", String.class, name, null));
    params.addElement(
      new Parameter("cnxId", int.class, new Integer(key), null));

    call.setParams(params);
  }


  /**
   * Returns an <code>AbstractJmsReply</code> delivered by the server.
   *
   * @exception IOException  If the SOAP call failed, or if the SOAP service
   *              is unable to process the call, or if the driver closes.
   */
  protected AbstractJmsReply getDelivery() throws IOException
  {
    Response resp = null;
    AbstractJmsReply reply = null;

    try {
      resp = call.invoke(url,"");
    }
    catch (SOAPException exc) {
      throw new IOException("The SOAP call failed: " + exc.getMessage());
    }

    if (resp.generatedFault()) {
      throw new IOException("The SOAP service failed to process the call: "
                            + resp.getFault().getFaultString());
    }   
    
    try {
      Hashtable h = (Hashtable) resp.getReturnValue().getValue();
            
      String className = (String) h.get("className");
      Class clazz = Class.forName(className);
      Class [] classParam = { new Hashtable().getClass() };
      Method m = clazz.getMethod("soapDecode",classParam);
      reply = (AbstractJmsReply) m.invoke(null,new Object[]{h});
    } catch (Exception exc) {
      throw new IOException(exc.getMessage());
    }

    return reply;
  }

  /** Shuts down the driver. */
  public void shutdown()
  {}
}

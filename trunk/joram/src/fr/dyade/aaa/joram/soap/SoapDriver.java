/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.soap;

import fr.dyade.aaa.mom.jms.*;

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


/**
 * A <code>SoapDriver</code> gets server deliveries through RCP SOAP calls.
 */
class SoapDriver extends fr.dyade.aaa.joram.Driver
{
  /** The URL of the SOAP service. */
  private URL url;
  /** The object used for the RPC call. */
  private Call call;

  /**
   * Constructs a <code>SoapDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   * @param url  The URL of the SOAP service.
   * @param key  Identifier of the SOAP connection.
   */
  SoapDriver(fr.dyade.aaa.joram.Connection cnx, URL url, int key)
  {
    super(cnx);
    this.url = url;

    // Building the Call object:
    call = new Call();

    SOAPMappingRegistry mappingReg = new SOAPMappingRegistry();
    BeanSerializer beanSer = new BeanSerializer();

    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "reply"),
                                  AbstractJmsReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "connectReply"),
                                  CnxConnectReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "serverReply"),
                                  ServerReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "createTDReply"),
                                  SessCreateTDReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "closeReply"),
                                  CnxCloseReply.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "adminReply"),
                                  GetAdminTopicReply.class, beanSer, beanSer);

    call.setSOAPMappingRegistry(mappingReg);
    call.setTargetObjectURI("urn:ProxyService");
    call.setMethodName("getReply");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    
    Parameter param =
      new Parameter("cnxId", int.class, new Integer(key), null);

    Vector params = new Vector();
    params.add(param);

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

    Vector vec = (Vector) resp.getReturnValue().getValue();

    Object obj = vec.get(0);

    // The vector might carry a close reply...
    if (obj instanceof CnxCloseReply)
      throw new IOException("Driver is closing.");
    // ... or a general JMS reply...
    else if (obj instanceof AbstractJmsReply)
      return (AbstractJmsReply) obj;
    // ... or codes a specific reply:
    else {
      if (((String) obj).equals("ConsumerMessages"))
        return ConsumerMessages.soapDecode(vec);
      else if (((String) obj).equals("QBrowseReply"))
        return QBrowseReply.soapDecode(vec);
      else
        return MomExceptionReply.soapDecode(vec);
    }
  }

  /** Shuts down the driver. */
  public void shutdown()
  {}
}

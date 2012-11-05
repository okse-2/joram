/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.soap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.apache.soap.Constants;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.util.xml.QName;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseReply;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.CnxConnectReply;
import org.objectweb.joram.shared.client.CnxConnectRequest;
import org.objectweb.joram.shared.client.CnxStartRequest;
import org.objectweb.joram.shared.client.CnxStopRequest;
import org.objectweb.joram.shared.client.ConsumerAckRequest;
import org.objectweb.joram.shared.client.ConsumerCloseSubRequest;
import org.objectweb.joram.shared.client.ConsumerDenyRequest;
import org.objectweb.joram.shared.client.ConsumerReceiveRequest;
import org.objectweb.joram.shared.client.ConsumerSetListRequest;
import org.objectweb.joram.shared.client.ConsumerSubRequest;
import org.objectweb.joram.shared.client.ConsumerUnsetListRequest;
import org.objectweb.joram.shared.client.ConsumerUnsubRequest;
import org.objectweb.joram.shared.client.GetAdminTopicReply;
import org.objectweb.joram.shared.client.GetAdminTopicRequest;
import org.objectweb.joram.shared.client.QBrowseRequest;
import org.objectweb.joram.shared.client.ServerReply;
import org.objectweb.joram.shared.client.SessAckRequest;
import org.objectweb.joram.shared.client.SessCreateTDReply;
import org.objectweb.joram.shared.client.SessCreateTQRequest;
import org.objectweb.joram.shared.client.SessCreateTTRequest;
import org.objectweb.joram.shared.client.SessDenyRequest;
import org.objectweb.joram.shared.client.TempDestDeleteRequest;
import org.objectweb.joram.shared.security.Identity;

/**
 * A <code>SoapConnection</code> links a Joram client and a Joram platform
 * with HTTP connections.
 * <p>
 * Requests and replies travel through the connections in SOAP (XML) format.
 */
public class SoapRequestChannel implements RequestChannel { 
  /** The user's identity */
  private Identity identity;
  
  private FactoryParameters factParams;

  /** URL of the SOAP service this object communicates with. */
  private URL serviceUrl = null;

  /** SOAP call object for sending the requests. */
  private Call sendCall;

  private Call receiveCall;

  /** Identifier of the connection. */
  private int cnxId;

  /**
   * Creates a <code>SoapConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param identity
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public SoapRequestChannel(FactoryParameters factParams2, 
                        Identity identity) throws JMSException {
    factParams = factParams2;
    this.identity = identity;
  }
  
  public void setTimer(Timer timer) {
    // No timer is useful
  }
  
  public void connect() throws Exception {
    connect(factParams, identity);

    // Building the Call object for sending the requests:
    SOAPMappingRegistry mappingReg = new SOAPMappingRegistry();
    BeanSerializer beanSer = new BeanSerializer();

    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "AbstractJmsRequest"),
                                  AbstractJmsRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxConnectRequest"),
                                  CnxConnectRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxStartRequest"),
                                  CnxStartRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxStopRequest"),
                                  CnxStopRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "CnxCloseRequest"),
                                  CnxCloseRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "ConsumerAckRequest"),
                                  ConsumerAckRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "ConsumerDenyRequest"),
                                  ConsumerDenyRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", 
                                  "ConsumerReceiveRequest"),
                                  ConsumerReceiveRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", 
                                  "ConsumerSetListRequest"),
                                  ConsumerSetListRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService",
                                  "ConsumerUnsetListRequest"),
                                  ConsumerUnsetListRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "ConsumerSubRequest"),
                                  ConsumerSubRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "ConsumerUnsubRequest"),
                                  ConsumerUnsubRequest.class,
                                  beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService",
                                  "ConsumerCloseSubRequest"),
                                  ConsumerCloseSubRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "QBrowseRequest"),
                                  QBrowseRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "SessAckRequest"),
                                  SessAckRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "SessDenyRequest"),
                                  SessDenyRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "SessCreateTQRequest"),
                                  SessCreateTQRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "SessCreateTTRequest"),
                                  SessCreateTTRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "TempDestDeleteRequest"),
                                  TempDestDeleteRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "GetAdminTopicRequest"),
                                  GetAdminTopicRequest.class, beanSer,
                                  beanSer);

    sendCall = new Call();
    sendCall.setSOAPMappingRegistry(mappingReg);
    sendCall.setTargetObjectURI("urn:ProxyService");
    sendCall.setMethodName("send");
    sendCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);

    mappingReg = new SOAPMappingRegistry();
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

    receiveCall = new Call();
    receiveCall.setSOAPMappingRegistry(mappingReg);
    receiveCall.setTargetObjectURI("urn:ProxyService");
    receiveCall.setMethodName("getReply");
    receiveCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
  }

  /**
   * Sending a JMS request through the SOAP protocol.
   *
   * @exception IllegalStateException  If the SOAP service fails.
   */
  public synchronized void send(AbstractJmsRequest request) throws Exception {
    Hashtable h = request.soapCode();

    // Setting the call's parameters:
    Vector params = new Vector();
    params.addElement(new Parameter("name", String.class, identity.getUserName(), null));
    params.add(new Parameter("cnxId", Integer.class,
                             new Integer(cnxId), null));
    params.add(new Parameter("map", Hashtable.class, h, null));
    sendCall.setParams(params);

    // Sending the request, checking the reply:
    try {
      Response resp = sendCall.invoke(serviceUrl,"");

      // Check the response.
      if (resp.generatedFault ()) {
        throw new IllegalStateException("The SOAP service failed to process"
                                        + " the call: "
                                        + resp.getFault().getFaultString());
      }
    } catch (SOAPException exc) {
      throw new IllegalStateException("The SOAP call failed: "
                                      + exc.getMessage());
    }
  }

  /** Closes the <code>SoapConnection</code>. */
  public void close() {}

  /**
   * Actually tries to set a first SOAP connection with the server.
   *
   * @param params   Factory parameters.
   * @param identity identity.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the SOAP service fails.
   */
  private void connect(FactoryParameters factParams, Identity identity) throws JMSException {
    // Setting the timer values:
    long startTime = System.currentTimeMillis();
    long endTime = startTime + factParams.connectingTimer * 1000;
    long currentTime;
    long nextSleep = 2000;
    boolean tryAgain;
    int attemptsC = 0;
    Response resp;
    String error;

    try {
      serviceUrl = new URL("http://"
                           + factParams.getHost()
                           + ":"
                           + factParams.getPort()
                           + "/soap/servlet/rpcrouter");
    }
    catch (MalformedURLException exc) {}

    // Building the Call object for checking the user's identification:
    Call checkCall = new Call();
    checkCall.setTargetObjectURI("urn:ProxyService");
    checkCall.setMethodName("setConnection");
    checkCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);

    Vector params = new Vector();
    Hashtable h;
    try {
      h = identity.soapCode();
    } catch (IOException e) {
      throw new JMSException("EXCEPTION:: connect identity.soapCode(): " + e.getMessage());
    }
    params.add(new Parameter("identityMap", Hashtable.class, h, null));
    params.addElement(new Parameter("timeout",
                                    Integer.class,
                                    new Integer(factParams.cnxPendingTimer),
                                    null));

    checkCall.setParams(params);

    while (true) {
      tryAgain = false;
      attemptsC++;
      error = null;

      try {
        resp = checkCall.invoke(serviceUrl,"");

        // SOAP sends a fault back: the service is possibly not started or
        // not running.
        if (resp.generatedFault ()) {
          error = resp.getFault().getFaultString();
          tryAgain = true;
        }
        // RPC call worked:
        else {
          Integer result = (Integer) resp.getReturnValue().getValue();

          // The returned value is either the key of the connection, or -1
          // if the user is invalid:
          if (result.intValue() == -1) {
            throw new JMSSecurityException("Can't open the connection with"
                                           + " the server on host "
                                           + factParams.getHost()
                                           + " and port "
                                           + factParams.getPort()
                                           + ": invalid user identification.");
          }
          cnxId = result.intValue();
          break;
        }
      }
      // SOAP call failed: the server may not be started.
      catch (SOAPException exc) {
        tryAgain = true;
        error = exc.getMessage();
      }
      // Trying again to connect:
      if (tryAgain) {
        currentTime = System.currentTimeMillis();
        // Keep on trying as long as timer is ok:
        if (currentTime < endTime) {

          if (currentTime + nextSleep > endTime)
            nextSleep = endTime - currentTime;

          // Sleeping for a while:
          try {
            Thread.sleep(nextSleep);
          }
          catch (InterruptedException intExc) {}

          // Trying again!
          nextSleep = nextSleep * 2;
          continue;
        }
        // If timer is over, throwing an IllegalStateException:
        else {
          long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
          throw new IllegalStateException("Could not open the connection"
                                          + " with server on host "
                                          + factParams.getHost()
                                          + " and port "
                                          + factParams.getPort()
                                          + " after " + attemptsC
                                          + " attempts during "
                                          + attemptsT + " secs: "
                                          + error);
        }
      }
    }
  }

  public AbstractJmsReply receive() throws Exception {
    Vector params = new Vector();
    params.addElement(new Parameter("name", String.class, identity.getUserName(), null));
    params.addElement(new Parameter("cnxId", int.class, new Integer(cnxId), null));    
    receiveCall.setParams(params);
    
    Response resp = null;
    AbstractJmsReply reply = null;
    
    try {
      resp = receiveCall.invoke(serviceUrl, "");
    } catch (SOAPException exc) {
      throw new IOException("The SOAP call failed: " + exc.getMessage());
    }

    if (resp.generatedFault()) {
      throw new IOException("The SOAP service failed to process the call: "
                            + resp.getFault().getFaultString());
    }   
    
    try {
      Hashtable h = (Hashtable) resp.getReturnValue().getValue();
      reply = (AbstractJmsReply) AbstractJmsMessage.soapDecode(h);
    } catch (Exception exc) {
      throw new IOException(exc.getMessage());
    }

    return reply;
  }
}

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

import fr.dyade.aaa.joram.Driver;
import fr.dyade.aaa.joram.FactoryParameters;
import fr.dyade.aaa.mom.jms.*;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.util.xml.QName;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;


/**
 * A <code>SoapConnection</code> links a Joram client and a Joram platform
 * with HTTP connections.
 * <p>
 * Requests and replies travel through the connections in SOAP (XML) format.
 */
public class SoapConnection implements fr.dyade.aaa.joram.ConnectionItf 
{ 
  /** URL of the SOAP service this object communicates with. */
  private URL serviceUrl = null;
  /** SOAP call object for sending the requests. */
  private Call sendCall;
  /** Identifier of the connection. */
  private int cnxId;

  /**
   * Creates a <code>SoapConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param name  Name of user.
   * @param password  Password of user.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public SoapConnection(FactoryParameters factParams, String name,
                        String password) throws JMSException
  {
    connect(factParams, name, password);

    // Building the Call object for sending the requests:
    SOAPMappingRegistry mappingReg = new SOAPMappingRegistry();
    BeanSerializer beanSer = new BeanSerializer();

    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "request"),
                                  AbstractJmsRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "connectRequest"),
                                  CnxConnectRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "startRequest"),
                                  CnxStartRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "stopRequest"),
                                  CnxStopRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "closeRequest"),
                                  CnxCloseRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "consAckRequest"),
                                  ConsumerAckRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "consDenyRequest"),
                                  ConsumerDenyRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "receiveRequest"),
                                  ConsumerReceiveRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "setListRequest"),
                                  ConsumerSetListRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "unsetListRequest"),
                                  ConsumerUnsetListRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "subRequest"),
                                  ConsumerSubRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "unsubRequest"),
                                  ConsumerUnsubRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "closeSubRequest"),
                                  ConsumerCloseSubRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "browseRequest"),
                                  QBrowseRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "sessAckRequest"),
                                  SessAckRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "sessDenyRequest"),
                                  SessDenyRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "createTQRequest"),
                                  SessCreateTQRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "createTTRequest"),
                                  SessCreateTTRequest.class, beanSer, beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "tdDeleteRequest"),
                                  TempDestDeleteRequest.class, beanSer,
                                  beanSer);
    mappingReg.mapTypes(Constants.NS_URI_SOAP_ENC,
                        new QName("urn:ProxyService", "adminRequest"),
                                  GetAdminTopicRequest.class, beanSer,
                                  beanSer);

    sendCall = new Call();
    sendCall.setSOAPMappingRegistry(mappingReg);
    sendCall.setTargetObjectURI("urn:ProxyService");
    sendCall.setMethodName("send");
    sendCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
  }


  /**
   * Creates a driver for the connection.
   *
   * @param cnx  The calling <code>Connection</code> instance.
   */
  public Driver createDriver(fr.dyade.aaa.joram.Connection cnx)
  {
    Driver driver = new SoapDriver(cnx, serviceUrl, cnxId);
    driver.setDaemon(true);
    return driver;
  }

  /**
   * Sending a JMS request through the SOAP protocol.
   *
   * @exception IllegalStateException  If the SOAP service fails.
   */
  public synchronized void send(AbstractJmsRequest request)
                           throws IllegalStateException
  {
    Vector vec = null;

    // A ProducerMessages is specifically coded into a Vector:
    if (request instanceof ProducerMessages)
      vec = ((ProducerMessages) request).soapCode();
    // Other requests will be coded by SOAP:
    else {
      vec = new Vector();
      vec.add(request);
    }

    // Setting the call's parameters:
    Vector params = new Vector();
    params.add(new Parameter("cnxId", Integer.class,
                             new Integer(cnxId), null));
    params.add(new Parameter("vec", Vector.class, vec, null));
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
    }
    catch (SOAPException exc) {
      throw new IllegalStateException("The SOAP call failed: "
                                      + exc.getMessage());
    }
  }

  /** Closes the <code>SoapConnection</code>. */
  public void close()
  { 
    try {
      send(new CnxCloseRequest());
    }
    catch (Exception exc) {}
  }

  /**
   * Actually tries to set a first SOAP connection with the server.
   *
   * @param params  Factory parameters.
   * @param name  The user's name.
   * @param password  The user's password.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the SOAP service fails.
   */
  private void connect(FactoryParameters factParams, String name,
                       String password)
               throws JMSException
  {
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
    params.addElement(new Parameter("name", String.class, name, null));
    params.addElement(new Parameter("password", String.class, password, null));
    params.addElement(new Parameter("timeout",
                                    Integer.class,
                                    new Integer(factParams.soapCnxPendingTimer),
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
}

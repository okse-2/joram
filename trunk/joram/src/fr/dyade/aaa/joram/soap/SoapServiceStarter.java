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

import org.apache.soap.rpc.Parameter;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;
import org.apache.soap.server.TypeMapping;
import org.apache.soap.util.xml.QName;

/**
 * Utility class allowing to start JORAM SOAP service and the embedded
 * server.
 */
public class SoapServiceStarter
{
  /**
   * Deploys and starts JORAM SOAP service and the embedded JORAM server.
   *
   * @param args  Name of host hosting Tomcat, tomcat's HTTP port (generally
   *          8080), identifier of the embedded server, name of the embedded
   *          server.
   *
   * @exception exception  If the deployment fails because Tomcat is not
   *              started. 
   */
  public static void main(String[] args) throws Exception
  {
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    int serverId = Integer.parseInt(args[2]);
    String serverName = args[3];

    java.net.URL url = new java.net.URL("http://" + host + ":" + port
                                        + "/soap/servlet/rpcrouter");

    System.out.println("Starting the SOAP service on host "
                       + host
                       + " listening on port "
                       + port);

    ServiceManagerClient smc = new ServiceManagerClient(url);
    smc.deploy(getDeploymentDescriptor());

    System.out.println("SOAP service deployed.");

    org.apache.soap.rpc.Call call = new org.apache.soap.rpc.Call();
    call.setTargetObjectURI("urn:ProxyService");
    call.setMethodName("start");

    java.util.Vector params = new java.util.Vector();
    params.add(new Parameter("serverId", Integer.class,
                             new Integer(serverId), null));
    params.add(new Parameter("serverName", String.class, serverName, null));
    call.setParams(params);

    System.out.println("Starting the " + serverName + " embedded server.");

    org.apache.soap.rpc.Response resp = call.invoke(url,"");

    System.out.println("Server " + serverId + " started.");
  }

  /**
   * Builds and returns the <code>DeploymentDescriptor</code> of
   * JORAM SOAP service.
   */
  private static DeploymentDescriptor getDeploymentDescriptor()
  {
    DeploymentDescriptor dd = new DeploymentDescriptor();
    
    dd.setID("urn:ProxyService");

    dd.setProviderType(DeploymentDescriptor.PROVIDER_JAVA);
    dd.setProviderClass("fr.dyade.aaa.mom.proxies.soap.SoapProxyService");
    dd.setScope(DeploymentDescriptor.SCOPE_APPLICATION);
    
    String[] methods = {"start", "setConnection", "send", "getReply"};
    dd.setMethods(methods);

    String[] listener = {"org.apache.soap.server.DOMFaultListener"};
    dd.setFaultListener(listener);

    dd.setMappings(getTypeMappings());

    return dd;
  }

  /** Builds and return the type mappings for JORAM SOAP service. */
  private static TypeMapping[] getTypeMappings()
  {
    TypeMapping[] mappings = new TypeMapping[27];

    String encoding = "http://schemas.xmlsoap.org/soap/encoding/";
    String bSerializer = "org.apache.soap.encoding.soapenc.BeanSerializer";

    int i = 0;

    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "request"),
                      "fr.dyade.aaa.mom.jms.AbstractJmsRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "connectRequest"),
                      "fr.dyade.aaa.mom.jms.CnxConnectRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "startRequest"),
                      "fr.dyade.aaa.mom.jms.CnxStartRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "stopRequest"),
                      "fr.dyade.aaa.mom.jms.CnxStopRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "closeRequest"),
                      "fr.dyade.aaa.mom.jms.CnxCloseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "consAckRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "consDenyRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "receiveRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerReceiveRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "setListRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerSetListRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
       new TypeMapping(encoding,
                       new QName("urn:ProxyService", "unsetListRequest"),
                       "fr.dyade.aaa.mom.jms.ConsumerUnsetListRequest",
                       bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "subRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "closeSubRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerCloseSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "unsubRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerUnsubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "browseRequest"),
                      "fr.dyade.aaa.mom.jms.QBrowseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "sessAckRequest"),
                      "fr.dyade.aaa.mom.jms.SessAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "sessDenyRequest"),
                      "fr.dyade.aaa.mom.jms.SessDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "createTQRequest"),
                      "fr.dyade.aaa.mom.jms.SessCreateTQRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "createTTRequest"),
                      "fr.dyade.aaa.mom.jms.SessCreateTTRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "tdDeleteRequest"),
                      "fr.dyade.aaa.mom.jms.TempDestDeleteRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "adminRequest"),
                      "fr.dyade.aaa.mom.jms.GetAdminTopicRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "reply"),
                      "fr.dyade.aaa.mom.jms.AbstractJmsReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "serverReply"),
                      "fr.dyade.aaa.mom.jms.ServerReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "excReply"),
                      "fr.dyade.aaa.mom.jms.MomExceptionReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "connectReply"),
                      "fr.dyade.aaa.mom.jms.CnxConnectReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "closeReply"),
                      "fr.dyade.aaa.mom.jms.CnxCloseReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "createTDReply"),
                      "fr.dyade.aaa.mom.jms.SessCreateTDReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "adminReply"),
                      "fr.dyade.aaa.mom.jms.GetAdminTopicReply",
                      bSerializer, bSerializer);

    return mappings;
  }
}

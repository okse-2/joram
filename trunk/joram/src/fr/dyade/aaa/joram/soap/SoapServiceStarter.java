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
                      new QName("urn:ProxyService", "AbstractJmsRequest"),
                      "fr.dyade.aaa.mom.jms.AbstractJmsRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxConnectRequest"),
                      "fr.dyade.aaa.mom.jms.CnxConnectRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxStartRequest"),
                      "fr.dyade.aaa.mom.jms.CnxStartRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxStopRequest"),
                      "fr.dyade.aaa.mom.jms.CnxStopRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxCloseRequest"),
                      "fr.dyade.aaa.mom.jms.CnxCloseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerAckRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerDenyRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerReceiveRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerReceiveRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerSetListRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerSetListRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
       new TypeMapping(encoding,
                       new QName("urn:ProxyService",
                                 "ConsumerUnsetListRequest"),
                       "fr.dyade.aaa.mom.jms.ConsumerUnsetListRequest",
                       bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerSubRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerCloseSubRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerCloseSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerUnsubRequest"),
                      "fr.dyade.aaa.mom.jms.ConsumerUnsubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "QBrowseRequest"),
                      "fr.dyade.aaa.mom.jms.QBrowseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessAckRequest"),
                      "fr.dyade.aaa.mom.jms.SessAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessDenyRequest"),
                      "fr.dyade.aaa.mom.jms.SessDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTQRequest"),
                      "fr.dyade.aaa.mom.jms.SessCreateTQRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTTRequest"),
                      "fr.dyade.aaa.mom.jms.SessCreateTTRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "TempDestDeleteRequest"),
                      "fr.dyade.aaa.mom.jms.TempDestDeleteRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "GetAdminTopicRequest"),
                      "fr.dyade.aaa.mom.jms.GetAdminTopicRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "AbstractJmsReply"),
                      "fr.dyade.aaa.mom.jms.AbstractJmsReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ServerReply"),
                      "fr.dyade.aaa.mom.jms.ServerReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "MomExceptionReply"),
                      "fr.dyade.aaa.mom.jms.MomExceptionReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxConnectReply"),
                      "fr.dyade.aaa.mom.jms.CnxConnectReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxCloseReply"),
                      "fr.dyade.aaa.mom.jms.CnxCloseReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTDReply"),
                      "fr.dyade.aaa.mom.jms.SessCreateTDReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "GetAdminTopicReply"),
                      "fr.dyade.aaa.mom.jms.GetAdminTopicReply",
                      bSerializer, bSerializer);

    return mappings;
  }
}

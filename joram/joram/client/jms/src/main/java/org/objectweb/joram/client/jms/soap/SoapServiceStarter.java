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

import org.apache.soap.rpc.Parameter;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;
import org.apache.soap.server.TypeMapping;
import org.apache.soap.util.xml.QName;

/**
 * Utility class allowing to start JORAM SOAP service and the embedded
 * server.
 */
public class SoapServiceStarter {
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
  public static void main(String[] args) throws Exception {
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

    call.invoke(url,"");

    System.out.println("Server " + serverId + " started.");
  }

  /**
   * Builds and returns the <code>DeploymentDescriptor</code> of
   * JORAM SOAP service.
   */
  private static DeploymentDescriptor getDeploymentDescriptor() {
    DeploymentDescriptor dd = new DeploymentDescriptor();
    
    dd.setID("urn:ProxyService");

    dd.setProviderType(DeploymentDescriptor.PROVIDER_JAVA);
    dd.setProviderClass("org.objectweb.joram.mom.proxies.soap.SoapProxyService");
    dd.setScope(DeploymentDescriptor.SCOPE_APPLICATION);
    
    String[] methods = {"start", "setConnection", "send", "getReply"};
    dd.setMethods(methods);

    String[] listener = {"org.apache.soap.server.DOMFaultListener"};
    dd.setFaultListener(listener);

    dd.setMappings(getTypeMappings());

    return dd;
  }

  /** Builds and return the type mappings for JORAM SOAP service. */
  private static TypeMapping[] getTypeMappings() {
    TypeMapping[] mappings = new TypeMapping[27];

    String encoding = "http://schemas.xmlsoap.org/soap/encoding/";
    String bSerializer = "org.apache.soap.encoding.soapenc.BeanSerializer";

    int i = 0;

    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "AbstractJmsRequest"),
                      "org.objectweb.joram.shared.client.AbstractJmsRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxConnectRequest"),
                      "org.objectweb.joram.shared.client.CnxConnectRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxStartRequest"),
                      "org.objectweb.joram.shared.client.CnxStartRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxStopRequest"),
                      "org.objectweb.joram.shared.client.CnxStopRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxCloseRequest"),
                      "org.objectweb.joram.shared.client.CnxCloseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerAckRequest"),
                      "org.objectweb.joram.shared.client.ConsumerAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerDenyRequest"),
                      "org.objectweb.joram.shared.client.ConsumerDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerReceiveRequest"),
                      "org.objectweb.joram.shared.client.ConsumerReceiveRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerSetListRequest"),
                      "org.objectweb.joram.shared.client.ConsumerSetListRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
       new TypeMapping(encoding,
                       new QName("urn:ProxyService",
                                 "ConsumerUnsetListRequest"),
                       "org.objectweb.joram.shared.client.ConsumerUnsetListRequest",
                       bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerSubRequest"),
                      "org.objectweb.joram.shared.client.ConsumerSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerCloseSubRequest"),
                      "org.objectweb.joram.shared.client.ConsumerCloseSubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ConsumerUnsubRequest"),
                      "org.objectweb.joram.shared.client.ConsumerUnsubRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "QBrowseRequest"),
                      "org.objectweb.joram.shared.client.QBrowseRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessAckRequest"),
                      "org.objectweb.joram.shared.client.SessAckRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessDenyRequest"),
                      "org.objectweb.joram.shared.client.SessDenyRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTQRequest"),
                      "org.objectweb.joram.shared.client.SessCreateTQRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTTRequest"),
                      "org.objectweb.joram.shared.client.SessCreateTTRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "TempDestDeleteRequest"),
                      "org.objectweb.joram.shared.client.TempDestDeleteRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "GetAdminTopicRequest"),
                      "org.objectweb.joram.shared.client.GetAdminTopicRequest",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "AbstractJmsReply"),
                      "org.objectweb.joram.shared.client.AbstractJmsReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "ServerReply"),
                      "org.objectweb.joram.shared.client.ServerReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "MomExceptionReply"),
                      "org.objectweb.joram.shared.client.MomExceptionReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxConnectReply"),
                      "org.objectweb.joram.shared.client.CnxConnectReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "CnxCloseReply"),
                      "org.objectweb.joram.shared.client.CnxCloseReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "SessCreateTDReply"),
                      "org.objectweb.joram.shared.client.SessCreateTDReply",
                      bSerializer, bSerializer);
    mappings[i++] =
      new TypeMapping(encoding,
                      new QName("urn:ProxyService", "GetAdminTopicReply"),
                      "org.objectweb.joram.shared.client.GetAdminTopicReply",
                      bSerializer, bSerializer);

    return mappings;
  }
}

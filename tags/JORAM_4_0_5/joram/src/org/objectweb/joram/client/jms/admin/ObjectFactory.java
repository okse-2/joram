/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Nicolas Tachker (INRIA)
 * Contributor(s): Frederic Maistre (INRIA)
 */
package org.objectweb.joram.client.jms.admin;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.QueueConnectionFactory;
import org.objectweb.joram.client.jms.TopicConnectionFactory;
import org.objectweb.joram.client.jms.XAConnectionFactory;
import org.objectweb.joram.client.jms.XAQueueConnectionFactory;
import org.objectweb.joram.client.jms.XATopicConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.TemporaryTopic;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XAQueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XATopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.soap.SoapConnectionFactory;
import org.objectweb.joram.client.jms.soap.QueueSoapConnectionFactory;
import org.objectweb.joram.client.jms.soap.TopicSoapConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.ClusterDestination;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.naming.*;


/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory
{
  String localCF =
    "org.objectweb.joram.client.jms.local.LocalConnectionFactory";
  String localQCF =
    "org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory";
  String localTCF =
    "org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory";
  String localXACF =
    "org.objectweb.joram.client.jms.local.XALocalConnectionFactory";
  String localXAQCF =
    "org.objectweb.joram.client.jms.local.XAQueueLocalConnectionFactory";
  String localXATCF =
    "org.objectweb.joram.client.jms.local.XATopicLocalConnectionFactory";

  String tcpCF =
    "org.objectweb.joram.client.jms.tcp.TcpConnectionFactory";
  String tcpQCF =
    "org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory";
  String tcpTCF =
    "org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory";
  String tcpXACF =
    "org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory";
  String tcpXAQCF =
    "org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory";
  String tcpXATCF =
    "org.objectweb.joram.client.jms.tcp.XATopicTcpConnectionFactory";

  String soapCF =
    "org.objectweb.joram.client.jms.soap.SoapConnectionFactory";
  String soapQCF =
    "org.objectweb.joram.client.jms.soap.QueueSoapConnectionFactory";
  String soapTCF =
    "org.objectweb.joram.client.jms.soap.TopicSoapConnectionFactory";

  String queue = "org.objectweb.joram.client.jms.Queue";
  String topic = "org.objectweb.joram.client.jms.Topic";
  String tempQueue = "org.objectweb.joram.client.jms.TemporaryQueue";
  String tempTopic = "org.objectweb.joram.client.jms.TemporaryTopic";
  String deadMQueue = "org.objectweb.joram.client.jms.admin.DeadMQueue";
  String user = "org.objectweb.joram.client.jms.admin.User";


  /** Returns an instance of an object given its reference. */
  public Object getObjectInstance(Object obj,
                                  Name name,
                                  Context ctx,
                                  java.util.Hashtable env) throws Exception
  {
    Reference ref = (Reference) obj;

    String id = null;
    Object adminObj = null;

    try {
      id = (String) ref.get("adminObj.id").getContent();
      adminObj = AdministeredObject.getInstance(id);
    }
    catch (Exception exc) {}

    if (adminObj != null)
      return adminObj;

    if (ref.getClassName().equals(tcpCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      ConnectionFactory cnxFact =
        new TcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcpQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      QueueConnectionFactory cnxFact =
        new QueueTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcpTCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      TopicConnectionFactory cnxFact =
        new TopicTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcpXACF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAConnectionFactory cnxFact =
        new XATcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcpXAQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAQueueConnectionFactory cnxFact =
        new XAQueueTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcpXATCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XATopicConnectionFactory cnxFact =
        new XATopicTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(localCF)) {
      return new LocalConnectionFactory();
    }
    else if (ref.getClassName().equals(localQCF)) {
      return new QueueLocalConnectionFactory();
    }
    else if (ref.getClassName().equals(localTCF)) {
      return new TopicLocalConnectionFactory();
    }
    else if (ref.getClassName().equals(localXACF)) {
      return new XALocalConnectionFactory();
    }
    else if (ref.getClassName().equals(localXAQCF)) {
      return new XAQueueLocalConnectionFactory();
    }
    else if (ref.getClassName().equals(localXATCF)) {
      return new XATopicLocalConnectionFactory();
    }
    else if (ref.getClassName().equals(soapCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      int timer = new Integer(soapCnxTimeout).intValue() / 1000;
      ConnectionFactory cnxFact =
        new SoapConnectionFactory(host,
                                  (new Integer(port)).intValue(),
                                  timer); 
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(soapQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      int timer = new Integer(soapCnxTimeout).intValue() / 1000;
      ConnectionFactory cnxFact =
        new QueueSoapConnectionFactory(host,
                                       (new Integer(port)).intValue(),
                                       timer); 
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(soapTCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      int timer = new Integer(soapCnxTimeout).intValue() / 1000;
      ConnectionFactory cnxFact =
        new TopicSoapConnectionFactory(host,
                                       (new Integer(port)).intValue(),
                                       timer); 
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(queue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Queue(destName);
    }
    else if (ref.getClassName().equals(topic)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Topic(destName);
    }
    else if (ref.getClassName().equals(tempQueue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryQueue(destName, null);
    }
    else if (ref.getClassName().equals(tempTopic)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryTopic(destName, null);
    }
    else if (ref.getClassName().equals(deadMQueue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new DeadMQueue(destName);
    }
    else if (ref.getClassName().equals(user)) {
      String userName = (String) ref.get("user.name").getContent();
      String userId = (String) ref.get("user.id").getContent();
      return new User(userName, userId);
    }
    else {
      String clazz = ref.getClassName();
      try {
        if ((clazz != null) && (clazz.length() > 0)) {
          Destination dest = (Destination) Class.forName(clazz).newInstance();
          if (dest instanceof ClusterDestination) {
            boolean isQueue = 
              new Boolean((String) ref.get("cluster.isQueue").getContent()).booleanValue();
            Hashtable h = new Hashtable();
            int i = 0;
            if (isQueue) {
              while (true) {
                if (ref.get("cluster.key"+i) == null) break;
                h.put((String) ref.get("cluster.key"+i).getContent(),
                      new Queue((String) ref.get("cluster.destName"+i).getContent()));
                i++;
              }
            } else {
              while (true) {
                if (ref.get("cluster.key"+i) == null) break;
                h.put((String) ref.get("cluster.key"+i).getContent(),
                      new Topic((String) ref.get("cluster.destName"+i).getContent()));
                i++;
              }
            }
            ((ClusterDestination) dest).init(h);
            return dest;
          }
        }
      } catch (Exception exc) {exc.printStackTrace();}
    }
    return null;
  }
}

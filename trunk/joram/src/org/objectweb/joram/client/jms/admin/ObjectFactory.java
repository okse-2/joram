/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Frederic Maistre (Bull SA)
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.naming.*;

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

import org.objectweb.joram.client.jms.ha.local.HALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.QueueHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.TopicHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XAHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XAQueueHALocalConnectionFactory;
import org.objectweb.joram.client.jms.ha.local.XATopicHALocalConnectionFactory;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATopicTcpConnectionFactory;

import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.QueueHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.TopicHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XAQueueHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.XATopicHATcpConnectionFactory;

import org.objectweb.joram.client.jms.soap.SoapConnectionFactory;
import org.objectweb.joram.client.jms.soap.QueueSoapConnectionFactory;
import org.objectweb.joram.client.jms.soap.TopicSoapConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.ClusterDestination;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory {
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

  String haLocalCF =
      "org.objectweb.joram.client.jms.ha.local.HALocalConnectionFactory";
  String haLocalQCF =
      "org.objectweb.joram.client.jms.ha.local.QueueHALocalConnectionFactory";
  String haLocalTCF =
      "org.objectweb.joram.client.jms.ha.local.TopicHALocalConnectionFactory";
  String haLocalXACF =
      "org.objectweb.joram.client.jms.ha.local.XAHALocalConnectionFactory";
  String haLocalXAQCF =
      "org.objectweb.joram.client.jms.ha.local.XAQueueHALocalConnectionFactory";
  String haLocalXATCF =
      "org.objectweb.joram.client.jms.ha.local.XAHATopicLocalConnectionFactory";

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

  String haTcpCF =
      "org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory";
  String haTcpQCF =
      "org.objectweb.joram.client.jms.ha.tcp.QueueHATcpConnectionFactory";
  String haTcpTCF =
      "org.objectweb.joram.client.jms.ha.tcp.TopicHATcpConnectionFactory";
  String haTcpXACF =
      "org.objectweb.joram.client.jms.ha.tcp.XAHATcpConnectionFactory";
  String haTcpXAQCF =
      "org.objectweb.joram.client.jms.ha.tcp.XAQueueHATcpConnectionFactory";
  String haTcpXATCF =
      "org.objectweb.joram.client.jms.ha.tcp.XATopicHATcpConnectionFactory";


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
                                  java.util.Hashtable env) throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "ObjectFactory.getObjectInstance(" +
        obj + ',' + name + ',' + ctx + ',' + env + ')');

    Reference ref = (Reference) obj;

    String id = null;
    Object adminObj = null;

    if (ref.getClassName().equals(tcpCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      ConnectionFactory cnxFact =
        new TcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(tcpQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      QueueConnectionFactory cnxFact =
        new QueueTcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(tcpTCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      TopicConnectionFactory cnxFact =
        new TopicTcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(tcpXACF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      XAConnectionFactory cnxFact =
        new XATcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(tcpXAQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      XAQueueConnectionFactory cnxFact =
        new XAQueueTcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(tcpXATCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      XATopicConnectionFactory cnxFact =
        new XATopicTcpConnectionFactory(host, (new Integer(port)).intValue());
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpCF)) {
      String url = (String) ref.get("cFactory.url").getContent();
      ConnectionFactory cnxFact =
        new HATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpQCF)) {
      String url = (String) ref.get("cFactory.url").getContent();        QueueConnectionFactory cnxFact =
                                                                           new QueueHATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpTCF)) {
      String url = (String) ref.get("cFactory.url").getContent();        TopicConnectionFactory cnxFact =
                                                                           new TopicHATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpXACF)) {
      String url = (String) ref.get("cFactory.url").getContent();        XAConnectionFactory cnxFact =
                                                                           new XAHATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpXAQCF)) {
      String url = (String) ref.get("cFactory.url").getContent();        XAQueueConnectionFactory cnxFact =
                                                                           new XAQueueHATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(haTcpXATCF)) {
      String url = (String) ref.get("cFactory.url").getContent();        XATopicConnectionFactory cnxFact =
                                                                           new XATopicHATcpConnectionFactory(url);
      String reliableClass = (String) ref.get("reliableClass").getContent();
      cnxFact.setReliableClass(reliableClass);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(localCF)) {
      return new LocalConnectionFactory();
    } else if (ref.getClassName().equals(localQCF)) {
      return new QueueLocalConnectionFactory();
    } else if (ref.getClassName().equals(localTCF)) {
      return new TopicLocalConnectionFactory();
    } else if (ref.getClassName().equals(localXACF)) {
      return new XALocalConnectionFactory();
    } else if (ref.getClassName().equals(localXAQCF)) {
      return new XAQueueLocalConnectionFactory();
    } else if (ref.getClassName().equals(localXATCF)) {
      return new XATopicLocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalCF)) {
      return new HALocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalQCF)) {
      return new QueueHALocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalTCF)) {
      return new TopicHALocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalXACF)) {
      return new XAHALocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalXAQCF)) {
      return new XAQueueHALocalConnectionFactory();
    } else if (ref.getClassName().equals(haLocalXATCF)) {
      return new XATopicHALocalConnectionFactory();
    } else if (ref.getClassName().equals(soapCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      ConnectionFactory cnxFact =
        new SoapConnectionFactory(host,
                                  (new Integer(port)).intValue(),
                                  -1);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(soapQCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      ConnectionFactory cnxFact =
        new QueueSoapConnectionFactory(host,
                                       (new Integer(port)).intValue(),
                                       -1);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(soapTCF)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      ConnectionFactory cnxFact =
        new TopicSoapConnectionFactory(host,
                                       (new Integer(port)).intValue(),
                                       -1);
      cnxFact.getParameters().fromReference(ref);
      return cnxFact;
    } else if (ref.getClassName().equals(queue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Queue(destName);
    } else if (ref.getClassName().equals(topic)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Topic(destName);
    } else if (ref.getClassName().equals(tempQueue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryQueue(destName, null);
    } else if (ref.getClassName().equals(tempTopic)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryTopic(destName, null);
    } else if (ref.getClassName().equals(deadMQueue)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new DeadMQueue(destName);
    } else if (ref.getClassName().equals(user)) {
      String userName = (String) ref.get("user.name").getContent();
      String userId = (String) ref.get("user.id").getContent();
      return new User(userName, userId);
    } else {
      String clazz = ref.getClassName();
      try {
        if ((clazz != null) && (clazz.length() > 0)) {
          Destination dest = (Destination) Class.forName(clazz).newInstance();
          if (dest instanceof ClusterDestination) {
            Hashtable h = new Hashtable();
            int i = 0;
            if (dest.isQueue()) {
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
            ((ClusterDestination) dest).setCluster(h);
            return dest;
          }
        }
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgClient.log(
            BasicLevel.ERROR, "", exc);
      }
    }
    return null;
  }
}

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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): Frederic Maistre (INRIA)
 */
package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.joram.ConnectionFactory;
import fr.dyade.aaa.joram.QueueConnectionFactory;
import fr.dyade.aaa.joram.TopicConnectionFactory;
import fr.dyade.aaa.joram.XAConnectionFactory;
import fr.dyade.aaa.joram.XAQueueConnectionFactory;
import fr.dyade.aaa.joram.XATopicConnectionFactory;
import fr.dyade.aaa.joram.FactoryParameters;
import fr.dyade.aaa.joram.Queue;
import fr.dyade.aaa.joram.Topic;
import fr.dyade.aaa.joram.TemporaryQueue;
import fr.dyade.aaa.joram.TemporaryTopic;
import fr.dyade.aaa.joram.tcp.TcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.QueueTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.TopicTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XATcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XAQueueTcpConnectionFactory;
import fr.dyade.aaa.joram.tcp.XATopicTcpConnectionFactory;

import javax.naming.*;

/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory
{
  String tcp_cf_ClassName = "fr.dyade.aaa.joram.tcp.TcpConnectionFactory";
  String tcp_qcf_ClassName = "fr.dyade.aaa.joram.tcp.QueueTcpConnectionFactory";
  String tcp_tcf_ClassName = "fr.dyade.aaa.joram.tcp.TopicTcpConnectionFactory";
  String tcp_xacf_ClassName = "fr.dyade.aaa.joram.tcp.XATcpConnectionFactory";
  String tcp_xaqcf_ClassName =
    "fr.dyade.aaa.joram.tcp.XAQueueTcpConnectionFactory";
  String tcp_xatcf_ClassName =
    "fr.dyade.aaa.joram.tcp.XATopicTcpConnectionFactory";
  String q_ClassName = "fr.dyade.aaa.joram.Queue";
  String t_ClassName = "fr.dyade.aaa.joram.Topic";
  String tq_ClassName = "fr.dyade.aaa.joram.TemporaryQueue";
  String tt_ClassName = "fr.dyade.aaa.joram.TemporaryTopic";
  String dmq_ClassName = "fr.dyade.aaa.joram.admin.DeadMQueue";
  String user_ClassName = "fr.dyade.aaa.joram.admin.User";
  String cluster_ClassName = "fr.dyade.aaa.joram.admin.Cluster";


  /** Returns an instance of an object given its reference. */
  public Object getObjectInstance(Object obj, Name name, Context ctx,
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

    if (ref.getClassName().equals(tcp_cf_ClassName)) {
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
    else if (ref.getClassName().equals(tcp_qcf_ClassName)) {
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
    else if (ref.getClassName().equals(tcp_tcf_ClassName)) {
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
    else if (ref.getClassName().equals(tcp_xacf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAConnectionFactory cnxFact =
        new XATcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcp_xaqcf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAQueueConnectionFactory cnxFact =
        new XAQueueTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcp_xatcf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XATopicConnectionFactory cnxFact =
        new XATopicTcpConnectionFactory(host, (new Integer(port)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(q_ClassName)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Queue(destName);
    }
    else if (ref.getClassName().equals(t_ClassName)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new Topic(destName);
    }
    else if (ref.getClassName().equals(tq_ClassName)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryQueue(destName, null);
    }
    else if (ref.getClassName().equals(tt_ClassName)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new TemporaryTopic(destName, null);
    }
    else if (ref.getClassName().equals(dmq_ClassName)) {
      String destName = (String) ref.get("dest.name").getContent();
      return new DeadMQueue(destName);
    }
    else if (ref.getClassName().equals(user_ClassName)) {
      String userName = (String) ref.get("user.name").getContent();
      String userId = (String) ref.get("user.id").getContent();
      return new User(userName, userId);
    }
    else if (ref.getClassName().equals(cluster_ClassName)) {
      Cluster cluster = new Cluster(id);
      String topics = (String) ref.get("cluster.topics").getContent();
      String locked = (String) ref.get("cluster.locked").getContent();
      java.util.StringTokenizer st = new java.util.StringTokenizer(topics);
      java.util.Vector vector = new java.util.Vector();
      while (st.hasMoreTokens())
        vector.add(st.nextToken());
      cluster.topics = vector;
      cluster.locked = (new Boolean(locked)).booleanValue();
      return cluster;
    }
    else
      return null;
  }
}

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
import fr.dyade.aaa.joram.Queue;
import fr.dyade.aaa.joram.Topic;
import fr.dyade.aaa.joram.TemporaryQueue;
import fr.dyade.aaa.joram.TemporaryTopic;

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory
{
  String cf_ClassName = "fr.dyade.aaa.joram.ConnectionFactory";
  String qcf_ClassName = "fr.dyade.aaa.joram.QueueConnectionFactory";
  String tcf_ClassName = "fr.dyade.aaa.joram.TopicConnectionFactory";
  String xacf_ClassName = "fr.dyade.aaa.joram.XAConnectionFactory";
  String xaqcf_ClassName = "fr.dyade.aaa.joram.XAQueueConnectionFactory";
  String xatcf_ClassName = "fr.dyade.aaa.joram.XATopicConnectionFactory";
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

    if (ref.getClassName().equals(cf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      ConnectionFactory cnxFact = new ConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
      cnxFact.setTxTimer((new Integer(txTimer)).intValue());
      return cnxFact;
    }
    else if (ref.getClassName().equals(qcf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      QueueConnectionFactory cnxFact = new QueueConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
      cnxFact.setTxTimer((new Integer(txTimer)).intValue());
      return cnxFact;
    }
    else if (ref.getClassName().equals(tcf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      TopicConnectionFactory cnxFact = new TopicConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
      cnxFact.setTxTimer((new Integer(txTimer)).intValue());
      return cnxFact;
    }
    else if (ref.getClassName().equals(xacf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAConnectionFactory cnxFact = new XAConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
      return cnxFact;
    }
    else if (ref.getClassName().equals(xaqcf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XAQueueConnectionFactory cnxFact = new XAQueueConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
      return cnxFact;
    }
    else if (ref.getClassName().equals(xatcf_ClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      XATopicConnectionFactory cnxFact = new XATopicConnectionFactory(url);
      cnxFact.setCnxTimer((new Integer(cnxTimer)).intValue());
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

/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import java.util.Hashtable;

import javax.naming.*;

import org.objectweb.monolog.api.BasicLevel;

/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory
{
  String qcfClassName = "fr.dyade.aaa.joram.QueueConnectionFactory";
  String xaqcfClassName = "fr.dyade.aaa.joram.XAQueueConnectionFactory";
  String tcfClassName = "fr.dyade.aaa.joram.TopicConnectionFactory";
  String xatcfClassName = "fr.dyade.aaa.joram.XATopicConnectionFactory";
  String qClassName = "fr.dyade.aaa.joram.Queue";
  String tClassName = "fr.dyade.aaa.joram.Topic";

  /** Returns the looked up object. */
  public Object getObjectInstance(Object obj, Name name, Context ctx,
                                  Hashtable env) throws Exception
  {
    Reference ref = (Reference) obj;

    if (ref.getClassName().equals(qcfClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      Integer cnxTimer =
        new Integer((String) ref.get("cFactory.cnxT").getContent());
      Integer txTimer = 
        new Integer((String) ref.get("cFactory.txT").getContent());

      ConnectionFactory cf =
        (ConnectionFactory) ConnectionFactory.getInstance(url); 

      QueueConnectionFactory qcf = null;

      if (cf instanceof QueueConnectionFactory)
        qcf = (QueueConnectionFactory) cf;

      if (qcf == null)
        qcf = new QueueConnectionFactory(url);
      else 
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + qcf + " retrieved by naming service.");

      qcf.setCnxTimer(cnxTimer.intValue());
      qcf.setTxTimer(txTimer.intValue());
      return qcf;
    }
    else if (ref.getClassName().equals(xaqcfClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      Integer cnxTimer =
        new Integer((String) ref.get("cFactory.cnxT").getContent());
      Integer txTimer = 
        new Integer((String) ref.get("cFactory.txT").getContent());

      ConnectionFactory cf =
        (ConnectionFactory) ConnectionFactory.getInstance(url);

      XAQueueConnectionFactory xaqcf = null;

      if (cf instanceof XAQueueConnectionFactory)
        xaqcf = (XAQueueConnectionFactory) cf;

      if (xaqcf == null)
        xaqcf = new XAQueueConnectionFactory(url);
      else 
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + xaqcf + " retrieved by naming service.");

      xaqcf.setCnxTimer(cnxTimer.intValue());
      return xaqcf;
    }
    else if (ref.getClassName().equals(tcfClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      Integer cnxTimer =
        new Integer((String) ref.get("cFactory.cnxT").getContent());
      Integer txTimer = 
        new Integer((String) ref.get("cFactory.txT").getContent());

      ConnectionFactory cf =
        (ConnectionFactory) ConnectionFactory.getInstance(url);

      TopicConnectionFactory tcf = null;

      if (cf instanceof TopicConnectionFactory)
        tcf = (TopicConnectionFactory) cf;

      if (tcf == null)
        tcf = new TopicConnectionFactory(url);
      else 
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + tcf + " retrieved by naming service.");

      tcf.setCnxTimer(cnxTimer.intValue());
      tcf.setTxTimer(txTimer.intValue());
      return tcf;
    }
    else if (ref.getClassName().equals(xatcfClassName)) {
      String url = (String) ref.get("cFactory.url").getContent();
      Integer cnxTimer =
        new Integer((String) ref.get("cFactory.cnxT").getContent());
      Integer txTimer = 
        new Integer((String) ref.get("cFactory.txT").getContent());

      ConnectionFactory cf =
        (ConnectionFactory) ConnectionFactory.getInstance(url);

      XATopicConnectionFactory xatcf = null;

      if (cf instanceof XATopicConnectionFactory)
        xatcf = (XATopicConnectionFactory) cf;

      if (xatcf == null)
        xatcf = new XATopicConnectionFactory(url);
      else 
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + xatcf + " retrieved by naming service.");

      xatcf.setCnxTimer(cnxTimer.intValue());
      return xatcf;
    }
    else if (ref.getClassName().equals(qClassName)) {
      String qName = (String) ref.get("dest.name").getContent();
      Queue q = (Queue) Destination.getInstance(qName);

      if (q == null)
        q = new Queue(qName);
      else
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + q + " retrieved by naming service.");
      return q;
    }
    else if (ref.getClassName().equals(tClassName)) {
      String tName = (String) ref.get("dest.name").getContent();
      Topic t = (Topic) Destination.getInstance(tName);

      if (t == null)
        t = new Topic(tName);
      else
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Administered object "
                                     + t + " retrieved by naming service.");
      return t;
    }
    else
      return null;
  }
}

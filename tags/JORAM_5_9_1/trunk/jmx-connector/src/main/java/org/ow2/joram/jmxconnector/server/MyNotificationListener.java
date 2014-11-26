/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 */
package org.ow2.joram.jmxconnector.server;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import org.ow2.joram.jmxconnector.shared.NotificationDesc;

import fr.dyade.aaa.common.Debug;

/**
 * This class is a specific <code>NotificationListener</code> that sends
 * notification emits by the local <code>MBeanServer</code> to a JMS queue.
 * 
 * This class is called by the server connector before doing the call JMX
 * <i>addNotificationListener</i>, after creating the object
 * ObjectNotificationListener a notification listener is created that will be
 * passed as a parameter of the method JMX <i>addNotificationListener.</i>
 * </ul>
 */
public class MyNotificationListener implements NotificationListener {
  private static final Logger logger = Debug.getLogger(MyNotificationListener.class.getName());

  Session session;
  MessageProducer producer;
  Destination dest;
  ObjectName name;
  
  public MyNotificationListener(Session session,
                                MessageProducer producer,
                                Destination dest,
                                ObjectName name) {
    this.session = session;
    this.producer = producer;
    this.dest = dest;
    this.name = name;
  }

  /**
   * Invoked when a JMX notification occurs.
   * The implementation of this method should return as soon as possible, to avoid
   * blocking its notification broadcaster.
   *
   * @param notification The notification.    
   * @param handback An opaque object which helps the listener to associate information
   * regarding the MBean emitter.
   * 
   * @see NotificationListener
   */
  public void handleNotification(Notification not, Object handback) {
    synchronized (session) {
      ObjectMessage msg = null;
      try {
        msg = session.createObjectMessage();
        NotificationDesc desc = new NotificationDesc(name, not);
        msg.setObject(desc);
      } catch (JMSException exc) {
        logger.log(BasicLevel.ERROR, "ObjectNotificationListener: Cannot create message.", exc);
        return;
      }
      try {
        producer.send(dest, msg);
      } catch (JMSException exc) {
        logger.log(BasicLevel.ERROR, "ObjectNotificationListener: Cannot send message.", exc);
        return;
      }
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ObjectNotificationListener: " + not + ", " + handback + "sent.");
  }
}

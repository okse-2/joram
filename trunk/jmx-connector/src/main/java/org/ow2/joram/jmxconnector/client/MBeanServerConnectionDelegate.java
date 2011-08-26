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
 * Initial developer(s): D.E. Boumchedda (ScalAgent D.T.)
 * Contributor(s): A. Freyssinet (ScalAgent D.T.)
 */
package org.ow2.joram.jmxconnector.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;


import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.jmxconnector.shared.*;

import fr.dyade.aaa.common.Debug;

/***
 * Acts as a delegate for the MBeanServerConnection In the class
 * <b>MBeanServerConnectionDelegate<b> is implemented all the methods that can
 * be call'd by the administration tool (JConsole)
 */
public class MBeanServerConnectionDelegate implements MBeanServerConnection, MessageListener {
  private static final Logger logger = Debug.getLogger(MBeanServerConnectionDelegate.class.getName());

  final static int  defaultValueOfSizeOfPoolRequestor = 10;

  static int value = 0;
  static HashMap<ObjectName, List<NotificationListenerDesc>> hashTableNotificationListener;

  private Connection cnx;
  private String qname;

  private Session session = null;
  private MessageConsumer cons = null;
  private Queue qnot = null;
  private String qnotid;

  PoolRequestor poolRequestors;

  public MBeanServerConnectionDelegate(Connection cnx, String qname) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MBeanServerConnectionDelegate<init>: " + qname);

    this.cnx = cnx;
    this.qname = qname;

    hashTableNotificationListener = new HashMap();

    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    qnot = session.createTemporaryQueue();
    qnotid = qnot.getQueueName();
    cons = session.createConsumer(qnot);
    cons.setMessageListener(this);


    // Creates the pool of requestors
    int SizePoolRequestor = 0;
    try {
      SizePoolRequestor = Integer.parseInt(System.getProperty("SizePoolRequestor"));
    } catch (Exception e) {}
    if(SizePoolRequestor <= 0)
      SizePoolRequestor = defaultValueOfSizeOfPoolRequestor;
    poolRequestors = new PoolRequestor(cnx, qname, SizePoolRequestor);
  }

  /**
   * <b>onMessage</b> in this method is implemented the listener of the
   * QNotification destination for receiving the notifications issued by the
   * registered MBean in the MBeanServer.
   * 
   * @param msg the message received.
   * @throws JMSException
   */
  public void onMessage(Message msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MBSConnectionDelegate.onMessage(..): " + msg);

    if (! (msg instanceof ObjectMessage)) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnector.onMessage: message received is not an ObjectMessage:" + msg);
      return;
    }

    NotificationDesc notificationDesc = null;
    try {
      notificationDesc = (NotificationDesc) ((ObjectMessage) msg).getObject();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "MBSConnectionDelegate.onMessage:", exc);
      return;
    }

    try {
      List<NotificationListenerDesc> list = hashTableNotificationListener.get(notificationDesc.name);
      if (list != null) {
        Iterator<NotificationListenerDesc> it = list.iterator();
        while (it.hasNext()) {
          try {
            NotificationListenerDesc listenerDesc = it.next();
            if ((listenerDesc.filter != null) &&
                ! listenerDesc.filter.isNotificationEnabled(notificationDesc.not)) {
              continue;
            }
            listenerDesc.listener.handleNotification(notificationDesc.not, listenerDesc.handback);
          } catch (Exception exc) {
            logger.log(BasicLevel.ERROR,
                       "MBSConnectionDelegate.onMessage: Error handling notification", exc);
          }
        }
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "MBSConnectionDelegate.onMessage:", exc);
    }
  }

  public ObjectInstance createMBean(String className,
                                    ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.createMBean(" + className + ", " + name + ')');

    return (ObjectInstance) poolRequestors.request(new CreateMBean(className, name));
  }

  public ObjectInstance createMBean(String className,
                                    ObjectName name,
                                    ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.createMBean(" + className + ", " + name + ", ..)" );

    return (ObjectInstance) poolRequestors.request(new CreateMBean1(className, name, loaderName));
  }

  public ObjectInstance createMBean(String className,
                                    ObjectName name,
                                    Object[] params,
                                    String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.createMBean(" + className + ", " + name + ", ..)" );

    return (ObjectInstance) poolRequestors.request(new CreateMBean2(className, name, params, signature));
  }

  public ObjectInstance createMBean(String className,
                                    ObjectName name,
                                    ObjectName loaderName,
                                    Object[] params,
                                    String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.createMBean(" + className + ", " + name + ", ..)" );

    return (ObjectInstance) poolRequestors.request(new CreateMBean3(className, name, loaderName, params, signature));
  }

  public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.unregisterMBean(" + name + ')');

    poolRequestors.request(new UnregisterMbean(name));
  }

  public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getObjectInstance(" + name + ')');
    
     return (ObjectInstance)poolRequestors.request(new GetObjectInstance(name));
  }

  public Set queryMBeans(ObjectName name,
                         QueryExp query) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.queryMBeans(" + name + ", " + query + ')');

    return (Set) poolRequestors.request(new QueryMbeans(name, query));
  }

  public Set queryNames(ObjectName name,
                        QueryExp query) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.queryNames(" + name + ", " + query + ')');
    
    return (Set) poolRequestors.request(new QueryName(name, query));
  }

  public boolean isRegistered(ObjectName name) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.isRegistered(" + name + ')');

    return ((Boolean) poolRequestors.request(new IsRegistered(name))).booleanValue();
  }

  public Integer getMBeanCount() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
      "MBSConnectionDelegate.getMBeanCount()");

    return (Integer) poolRequestors.request(new GetMBeanCount());
  }

  public Object getAttribute(ObjectName name,
                             String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getAttribute(" + name + ", " + attribute + ')');

    return poolRequestors.request(new GetAttribute(name, attribute));
  }

  public AttributeList getAttributes(ObjectName name,
                                     String[] attributes) throws InstanceNotFoundException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getAttributes(" + name + ", ..)");

    return (AttributeList) poolRequestors.request(new GetAttributes(name, attributes));
  }

  public void setAttribute(ObjectName name,
                           Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.setAttribute(" + name + ", " + attribute + ')');

    poolRequestors.request(new SetAttribute(name, attribute));
  }

  public AttributeList setAttributes(ObjectName name,
                                     AttributeList attributes) throws InstanceNotFoundException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.setAttributes(" + name + ", ..)");

    return (AttributeList) poolRequestors.request(new SetAttributes(name, attributes));
  }

  public Object invoke(ObjectName name,
                       String operationName,
                       Object[] params,
                       String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.invoke(" + name + ", " + operationName + ", ..)");

    return poolRequestors.request(new Invoke(name, operationName, params, signature));
  }

  public String getDefaultDomain() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getDefaultDomain()");

    return (String) poolRequestors.request(new GetDefaultDomain());
  }

  public String[] getDomains() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getDomains()");

    return (String[]) poolRequestors.request(new GetDomains());
  }

  /**
   * <b>addNotificationListener</b> When a requestor uses this method, an object
   * is created <i>objectAddNotificationListenerStored</i>, with the same
   * parameters call , then this object is stored in <i>the
   * hashTableNotificationListener</i> HashMap that will allow us later find the
   * object corresponding to the <i>addNotificationListener</i> call, so we can
   * remove the right object when a <i>removeNotificationListener</i> is made..
   * 
   * @param ObjectName
   * @param NotificationListener
   * @param NotificationFilter
   * @param Object
   */
  public void addNotificationListener(ObjectName name,
                                      NotificationListener listener,
                                      NotificationFilter filter,
                                      Object handback) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.addNotificationListener(" + name + ", ..)");

    NotificationListenerDesc desc = new NotificationListenerDesc(name, listener, filter, handback);
    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
    if (list == null) {
      // Send the request to the JMX connector server then insert the
      // NotificationListener descriptor in the map.
      list = new Vector();
      desc.key = (Long) poolRequestors.request(new AddNotificationListener(name, qnotid));
      System.out.println("addNotificationListener: name=" + name + ", key=" + desc.key);
      list.add(desc);
      
      hashTableNotificationListener.put(name, list);
    } else {
      // The subscription is already active, just add the NotificationListener
      // descriptor in the map.
      desc.key = list.get(0).key;
      System.out.println("addNotificationListener: name=" + name + ", key=" + desc.key);
      list.add(list.size(), desc);
    }
  }

  /**
   * <b>addNotificationListener</b> When a requestor uses this method, an object
   * is created <i>objectAddNotificationListenerStored</i>, with the same
   * parameters call , then this object is stored in <i>the
   * hashTableNotificationListener</i> HashMap that will allow us later find the
   * object corresponding to the <i>addNotificationListener</i> call, so we can
   * remove the right object when a <i>removeNotificationListener</i> is made..
   * 
   * @param ObjectName
   * @param ObjectName
   * @param NotificationFilter
   * @param Object
   */
  public void addNotificationListener(ObjectName name,
                                      ObjectName listener,
                                      NotificationFilter filter,
                                      Object handback) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.addNotificationListener(" + name + ", ..)");

    throw new IOException("Not yet implemented");
    
//    NotificationListenerDesc desc = new NotificationListenerDesc(name, null, filter, handback);
//    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
//    if (list == null) {
//      // Send the request to the JMX connector server then insert the
//      // NotificationListener descriptor in the map.
//      list = new Vector();
//      desc.key = (Long) poolRequestors.request(new AddNotificationListener(name, qnotid));
//      list.add(desc);
//      
//      hashTableNotificationListener.put(name, list);
//    } else {
//      // The subscription is already active, just add the NotificationListener
//      // descriptor in the map.
//      desc.key = list.get(0).key;
//      list.add(list.size(), desc);
//    }
  }

  /**
   * <b>removeNotificationListener</b> This method is called to remove a
   * listener registered after call to the <i>addNotificationListener</i>, to
   * remove the good listener we consult the
   * <i>hashTableNotificationListener</i> HashMap to find the right objetLister
   * registered, once the object found, we recover the <i>key</i> which is
   * passed in parameter like <i>handback</i>, of the
   * <i>RemoveNotificationListeber</i> object which will be send, so that we can
   * on the other side (server side), find the right listener from receiving the
   * key, and delete it by using the method JMX
   * <i>removeNotificationListener</i>.
   * 
   * @param ObjectName
   * @param ObjectName
   */
  public void removeNotificationListener(ObjectName name,
                                         ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.removeNotificationListener(" + name + ", ..)");

    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
    if (list != null) {
      Long key = list.get(0).key;
      hashTableNotificationListener.remove(name);
      poolRequestors.request(new RemoveNotificationListener(key));
    } else {
      // There is no listener registered for this MBean
      throw new ListenerNotFoundException("No registered listener for: " + name);
    }
  }

  /**
   * <b>removeNotificationListener</b> This method is called to remove a
   * listener registered after call to the <i>addNotificationListener</i>, to
   * remove the good listener we consult the
   * <i>hashTableNotificationListener</i> HashMap to find the right objetLister
   * registered, once the object found, we recover the <i>key</i> which is
   * passed in parameter like <i>handback</i>, of the
   * <i>RemoveNotificationListeber</i> object which will be send, so that we can
   * on the other side (server side), find the right listener from receiving the
   * key, and delete it by using the method JMX
   * <i>removeNotificationListener</i>.
   * 
   * @param ObjectName
   * @param ObjectName
   * @param NotificationFilter
   * @param Object
   */
  public void removeNotificationListener(ObjectName name,
                                         ObjectName listener,
                                         NotificationFilter filter,
                                         Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.removeNotificationListener(" + name + ", ..)");

    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
    if (list != null) {
      Long key = list.get(0).key;
      Iterator<NotificationListenerDesc> it = list.iterator();
      while (it.hasNext()) {
        NotificationListenerDesc desc = it.next();
        if (desc.listener.equals(listener) && (desc.filter == filter) && (desc.handback == handback)) {
          it.remove();
          
          // The MBean must have a listener that exactly matches the given listener,
          // filter, and handback parameters. If there is more than one such listener,
          // only one is removed.

          break;
        }
      }
      
      if (list.isEmpty()) {
        hashTableNotificationListener.remove(name);
        poolRequestors.request(new RemoveNotificationListener(key));
      }
    } else {
      // There is no listener registered for this MBean
      throw new ListenerNotFoundException("No registered listener for: " + name);
    }
  }

  /**
   * <b>removeNotificationListener</b> This method is called to remove a
   * listener registered after call to the <i>addNotificationListener</i>, to
   * remove the good listener we consult the
   * <i>hashTableNotificationListener</i> HashMap to find the right objetLister
   * registered, once the object found, we recover the <i>key</i> which is
   * passed in parameter like <i>handback</i>, of the
   * <i>RemoveNotificationListeber</i> object which will be send, so that we can
   * on the other side (server side), find the right listener from receiving the
   * key, and delete it by using the method JMX
   * <i>removeNotificationListener</i>.
   * 
   * @param ObjectName
   * @param NotificationListener
   */
  public void removeNotificationListener(ObjectName name,
                                         NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.removeNotificationListener(" + name + ", ..)");

    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
    if (list != null) {
      Long key = list.get(0).key;
      hashTableNotificationListener.remove(name);
      poolRequestors.request(new RemoveNotificationListener(key));
    } else {
      // There is no listener registered for this MBean
      throw new ListenerNotFoundException("No registered listener for: " + name);
    }
  }

  /**
   * <b>removeNotificationListener</b> This method is called to remove a
   * listener registered after call to the <i>addNotificationListener</i>, to
   * remove the good listener we consult the
   * <i>hashTableNotificationListener</i> HashMap to find the right objetLister
   * registered, once the object found, we recover the <i>key</i> which is
   * passed in parameter like <i>handback</i>, of the
   * <i>RemoveNotificationListeber</i> object which will be send, so that we can
   * on the other side (server side), find the right listener from receiving the
   * key, and delete it by using the method JMX
   * <i>removeNotificationListener</i>.
   * 
   * @param ObjectName
   * @param NotificationListener
   * @param NotificationFilter
   * @param Object
   * @throws JMSException
   */
  public void removeNotificationListener(ObjectName name,
                                         NotificationListener listener,
                                         NotificationFilter filter,
                                         Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.removeNotificationListener(" + name + ", ..)");

    List<NotificationListenerDesc> list = hashTableNotificationListener.get(name);
    if (list != null) {
      Long key = list.get(0).key;
      Iterator<NotificationListenerDesc> it = list.iterator();
      while (it.hasNext()) {
        NotificationListenerDesc desc = it.next();
        if (desc.listener.equals(listener) && (desc.filter == filter) && (desc.handback == handback)) {
          it.remove();
          
          // The MBean must have a listener that exactly matches the given listener,
          // filter, and handback parameters. If there is more than one such listener,
          // only one is removed.

          break;
        }
      }

      if (list.isEmpty()) {
        hashTableNotificationListener.remove(name);
        poolRequestors.request(new RemoveNotificationListener(key));
      }
    } else {
      // There is no listener registered for this MBean
      throw new ListenerNotFoundException("No registered listener for: " + name);
    }
  }

  public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.getMBeanInfo(" + name+ ')');

    return (MBeanInfo) poolRequestors.request(new GetMBeanInfo(name));
  }

  public boolean isInstanceOf(ObjectName name,
                              String className) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "MBSConnectionDelegate.isInstanceOf(" + name + ", " + className + ')');

    return ((Boolean) poolRequestors.request(new IsInstanceOf(name, className))).booleanValue();
  }
}

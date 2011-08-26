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
package org.ow2.joram.jmxconnector.server;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.jmxconnector.shared.AddNotificationListener;
import org.ow2.joram.jmxconnector.shared.CreateMBean;
import org.ow2.joram.jmxconnector.shared.CreateMBean1;
import org.ow2.joram.jmxconnector.shared.CreateMBean2;
import org.ow2.joram.jmxconnector.shared.CreateMBean3;
import org.ow2.joram.jmxconnector.shared.GetAttribute;
import org.ow2.joram.jmxconnector.shared.GetAttributes;
import org.ow2.joram.jmxconnector.shared.GetDefaultDomain;
import org.ow2.joram.jmxconnector.shared.GetDomains;
import org.ow2.joram.jmxconnector.shared.GetMBeanCount;
import org.ow2.joram.jmxconnector.shared.GetMBeanInfo;
import org.ow2.joram.jmxconnector.shared.GetObjectInstance;
import org.ow2.joram.jmxconnector.shared.Invoke;
import org.ow2.joram.jmxconnector.shared.IsInstanceOf;
import org.ow2.joram.jmxconnector.shared.IsRegistered;
import org.ow2.joram.jmxconnector.shared.QueryMbeans;
import org.ow2.joram.jmxconnector.shared.QueryName;
import org.ow2.joram.jmxconnector.shared.RemoveNotificationListener;
import org.ow2.joram.jmxconnector.shared.SetAttribute;
import org.ow2.joram.jmxconnector.shared.SetAttributes;
import org.ow2.joram.jmxconnector.shared.UnregisterMbean;

import fr.dyade.aaa.common.Debug;

/**
 * <p>JMS Implementation of a JMX connector server.</p>
 *
 * <p>When starting a JMSConnectorServer initiates a connection and creates a 
 * destination to receive JMX requests from client. For each request it builds
 * the response and sends it to the client using the JMSReplyTo field.</p>
 * 
 * @see javax.management.remote.JMXConnectorServer
 */
public class JMSConnectorServer extends JMXConnectorServer implements MessageListener {
  private static final Logger logger = Debug.getLogger(JMSConnectorServer.class.getName());
  
  private JMXServiceURL url;
  private final Map env;
  
  private boolean stopped = true;
  
  private MBeanServer mbs = null;
  
  Session session, session2;
  MessageProducer producer, producer2;
  Queue qToto;
  Object handback;
  long key = 0L;
  HashMap<Long, MyNotificationListener> listeners;
  
  public JMSConnectorServer(JMXServiceURL url, Map env, MBeanServer mbs) {
    this.url = url;
    this.env = env;
    this.mbs = mbs;
  }

  public void start() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSConnectorServer.start()");

    stopped = false;    
    
    listeners = new HashMap();
    
    ConnectionFactory connectionFactory = null;    
    try {
      // Gets ConnectioFactory parameters from URL
      connectionFactory = TcpConnectionFactory.create(url.getHost(), url.getPort());
      String[] credentials = null;
      if (env != null)
        credentials = (String[]) env.get("jmx.remote.credentials");
      
      // Creates the Connection
      Connection cnx = null;
      if ((credentials != null) && (credentials.length == 2)) {
        cnx = connectionFactory.createConnection(credentials[0], credentials[1]);
      } else {
        cnx = connectionFactory.createConnection();
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JMSConnectorServer.connect: Connection established.");

      // Creates the Session
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      // Gets queue name from URL
      String qname = url.getURLPath();
      if ((qname == null) || qname.length() == 0) {
        qname = "MXQ" + ManagementFactory.getRuntimeMXBean().getName();
      } else {
        if (qname.charAt(0) == '/')
          qname = qname.substring(1);
      }
      Queue queue = (Queue) session.createQueue(qname);
      MessageConsumer consumer = session.createConsumer(queue);
      producer = session.createProducer(null);
      consumer.setMessageListener(this);

      session2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer2 = session2.createProducer(null);
      
      cnx.start();
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  public void stop() throws IOException {
    // TODO(AF):
    if (!stopped) {
      stopped = true;
      // .....
    }
  }

  public boolean isActive() {
    return !stopped;
  }

  public JMXServiceURL getAddress() {
    return url;
  }

  public Map<String, ?> getAttributes() {
    return null;
  }
  
  public void onMessage(Message msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSConnectorServer.onMessage: " + msg);

    if (! (msg instanceof ObjectMessage)) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServer.onMessage: message received is not an ObjectMessage:" + msg);
      return;
    }

    Object request = null;
    try {
      request = ((ObjectMessage) msg).getObject();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServer.onMessage: Cannot get request in message:" + msg, exc);
      return;
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSConnectorServer.onMessage: handling " + request);

    Destination replyTo = null;
    String msgId = null;
    try {
      replyTo = msg.getJMSReplyTo();
      msgId = msg.getJMSMessageID();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServer.onMessage: replyTo field is undefined.", exc);
      return;
    }

    Object reply = null;
    try {
      if (request instanceof GetAttribute) {
        GetAttribute getAtt = (GetAttribute) request;
        reply = mbs.getAttribute(getAtt.name, getAtt.attributes);
      } else if (request instanceof SetAttribute) {
        SetAttribute setAtt = (SetAttribute) request;
        mbs.setAttribute(setAtt.name, setAtt.attribute);
      } else if (request instanceof Invoke) {
        Invoke invoke = (Invoke) request;
        reply = mbs.invoke(invoke.name, invoke.operationName, invoke.parametres, invoke.signature);
      } else if (request instanceof GetMBeanInfo) {
        GetMBeanInfo info = (GetMBeanInfo) request;
        reply = mbs.getMBeanInfo(info.name);
      } else if (request instanceof IsRegistered) {
        IsRegistered isRegistered = (IsRegistered) request;
        reply = new Boolean(mbs.isRegistered(isRegistered.name));
      } else if (request instanceof IsInstanceOf) {
        IsInstanceOf isInstanceOf = (IsInstanceOf) request;
        reply = new Boolean(mbs.isInstanceOf(isInstanceOf.name, isInstanceOf.className));
      } else if (request instanceof QueryName) {
        QueryName query = (QueryName) request;
        reply = mbs.queryNames(query.name, query.query);
      } else if (request instanceof GetAttributes) {
        GetAttributes getAtts = (GetAttributes) request;
        reply = mbs.getAttributes(getAtts.name, getAtts.attributes);
      } else if (request instanceof GetDefaultDomain) {
        reply = mbs.getDefaultDomain();
      } else if (request instanceof CreateMBean) {
        CreateMBean createMbean = (CreateMBean) request;
        reply = mbs.createMBean(createMbean.className, createMbean.name);
      } else if (request instanceof CreateMBean1) {
        CreateMBean1 createMBean = (CreateMBean1) request;
        reply = mbs.createMBean(createMBean.className, createMBean.name, createMBean.loaderName);
      } else if (request instanceof CreateMBean2) {
        CreateMBean2 objectCreateMBean2 = (CreateMBean2) request;
        reply = mbs.createMBean(objectCreateMBean2.className, objectCreateMBean2.name,
                                objectCreateMBean2.parametres, objectCreateMBean2.signature);
      } else if (request instanceof CreateMBean3) {
        CreateMBean3 objectCreateMBean3 = (CreateMBean3) request;
        reply = mbs.createMBean(objectCreateMBean3.className, objectCreateMBean3.name,
                                objectCreateMBean3.loaderName, objectCreateMBean3.parametres, objectCreateMBean3.signature);
      } else if (request instanceof UnregisterMbean) {
        UnregisterMbean objectUnregisterMbean = (UnregisterMbean) request;
        mbs.unregisterMBean(objectUnregisterMbean.name);
        reply = "Unregistering of the MBean : " + objectUnregisterMbean.name + "is done";
      } else if (request instanceof GetObjectInstance) {
        GetObjectInstance objectGetObjectInstance = (GetObjectInstance) request;
        reply = mbs.getObjectInstance(objectGetObjectInstance.name);
      } else if (request instanceof QueryMbeans) {
        QueryMbeans objectQueryMbeans = (QueryMbeans) request;
        reply = mbs.queryMBeans(objectQueryMbeans.name, objectQueryMbeans.query);
      } else if (request instanceof GetMBeanCount) {
        reply = mbs.getMBeanCount();
      } else if (request instanceof SetAttributes) {
        SetAttributes objectSetAttributes = (SetAttributes) request;
        reply = mbs.setAttributes(objectSetAttributes.name, objectSetAttributes.attributes);
      } else if (request instanceof GetDomains) {
        reply = mbs.getDomains();
      } else if (request instanceof AddNotificationListener) {
        AddNotificationListener addNotificationListener = (AddNotificationListener) request;
        MyNotificationListener listener = new MyNotificationListener(session2,
                                                                     producer2,
                                                                     new Queue(addNotificationListener.qname),
                                                                     addNotificationListener.name);
        reply = new Long(key++);
        listeners.put((Long) reply, listener);
        mbs.addNotificationListener(addNotificationListener.name, listener, null, null);
      } else if (request instanceof RemoveNotificationListener) {
        RemoveNotificationListener objectRemoveNotificationListener = (RemoveNotificationListener) request;
        MyNotificationListener listener = listeners.remove(objectRemoveNotificationListener.key);
        mbs.removeNotificationListener(listener.name, listener);
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR,
                 "JMSConnectorServer.onMessage: error handling request: " + request, exc);
      
      // Send an error reply containing the exception to free the requestor
      reply = exc;
    }

    ObjectMessage replyMsg = null;
    try {
      replyMsg = session.createObjectMessage();
      if (reply != null)
        replyMsg.setObject((Serializable) reply);
      replyMsg.setJMSCorrelationID(msgId);
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "JMSConnectorServer.onMessage: Cannot create message.", exc);
      return;
    }
    try {
      producer.send(replyTo, replyMsg);
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "JMSConnectorServer.onMessage: Cannot send message.", exc);
      return;
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "JMSConnectorServer.onMessage: reply " + reply + " sent.");
  }
}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>MonitoringTopicImpl</code> class implements the monitoring
 * behavior, regularly delivering monitoring messages to subscribers.
 */
public class MonitoringTopicImpl extends TopicImpl implements MonitoringTopicImplMBean {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Prefix for monitored MBeans names */
  private static final String monitoringPrefix = "MBeanMonitoring:";

  /** Time between two monitoring events. One minute by default. */
  protected long period = 60000;
  
  /** Counter of messages produced by this Monitoring topic. */
  private long msgCounter = 0;
  
  /** The various elements to monitor. */
  private Properties monitoringProperties;
  
  /** Tells if the messages produced are persistent. */
  private boolean isPersistent = false;
  
  /** The priority of produced messages. */
  private int priority = 4;
  
  /** The duration of produced messages. */
  private long expiration = -1;
  
  /**
   * Number of messages received. Can't use nbMsgsReceiveSinceCreation because
   * we process our own created messages.
   */
  private int receivedMessagesCount = 0;

  public MonitoringTopicImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    monitoringProperties = new Properties();
    if (prop != null) {
      Enumeration enumProperties = prop.keys();
      while (enumProperties.hasMoreElements()) {
        String propName = (String) enumProperties.nextElement();
        Object property = prop.get(propName);
        if (property instanceof String && propName.startsWith(monitoringPrefix)) {
          monitoringProperties.put(propName.substring(monitoringPrefix.length()), property);
        }
      }
      try {
        if (prop.get("period") != null)
          period = ConversionHelper.toLong(prop.get("period"));
        if (prop.get("persistent") != null)
          isPersistent = ConversionHelper.toBoolean(prop.get("persistent"));
        if (prop.get("priority") != null)
          priority = ConversionHelper.toInt(prop.get("priority"));
        if (prop.get("expiration") != null)
          expiration = ConversionHelper.toLong(prop.get("expiration"));
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, exc);
      }
    } else {
      // Default monitoring options
      monitoringProperties.put("Joram#" + AgentServer.getServerId() + ":type=User,*",
          "NbMsgsDeliveredSinceCreation, NbMsgsSentToDMQSinceCreation, PendingMessageCount");
      monitoringProperties.put("Joram#" + AgentServer.getServerId() + ":type=Destination,*",
          "NbMsgsDeliverSinceCreation, NbMsgsReceiveSinceCreation, NbMsgsSentToDMQSinceCreation");
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
  }

  /**
   * Returns the time between two monitoring events, one minute if not set.
   * 
   * @return the period value of this queue; one minute if not set.
   */
  public long getPeriod() {
    return period;
  }
  
  public ClientMessages preProcess(AgentId from, ClientMessages msgs) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Change monitoring properties.");
    // New monitoring properties
    monitoringProperties.clear();
    Message msg = (Message) msgs.getMessages().get(msgs.getMessages().size() - 1);
    if (msg.properties != null) {
      Enumeration enumProperties = msg.properties.keys();
      while (enumProperties.hasMoreElements()) {
        String propName = (String) enumProperties.nextElement();
        Object property = msg.properties.get(propName);
        if (property instanceof String && propName.startsWith(monitoringPrefix)) {
          monitoringProperties.put(propName.substring(monitoringPrefix.length()), property);
        }
      }
      try {
        if (msg.properties.get("period") != null)
          period = ConversionHelper.toLong(msg.properties.get("period"));
        if (msg.properties.get("persistent") != null)
          isPersistent = ConversionHelper.toBoolean(msg.properties.get("persistent"));
        if (msg.properties.get("priority") != null)
          priority = ConversionHelper.toInt(msg.properties.get("priority"));
        if (msg.properties.get("expiration") != null)
          expiration = ConversionHelper.toLong(msg.properties.get("expiration"));
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, exc);
      }
    }
    receivedMessagesCount += msgs.getMessages().size();
    return null;
  }
  
  /**
   * Sets or unsets the period for this queue.
   * 
   * @param period
   *          The period value to be set or -1 for unsetting previous value.
   */
  public void setPeriod(long period) {
    if ((this.period == -1L) && (period != -1L)) {
      // Schedule the CleaningTask.
      forward(getId(), new WakeUpNot());
    }
    this.period = period;
  }
  
  private String createMessageId() {
    msgCounter++;
    return "ID:" + getId().toString() + '_' + msgCounter;
  }

  /**
   * When the topic is waken up, collect the monitoring information required and
   * send it.
   */
  public void wakeUpNot(WakeUpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " MonitoringTopicImpl.wakeUpNot(" + not + ")");
    
    long currentTime = System.currentTimeMillis();
    Message message = new Message();
    message.id = createMessageId();
    message.timestamp = currentTime;
    message.persistent = isPersistent;
    message.setDestination(getId().toString(), Topic.TOPIC_TYPE);
    message.priority = priority;
    if (expiration > -1) {
      message.expiration = currentTime + expiration;
    } else {
      message.expiration = currentTime + 2 * period;
    }
    
    Enumeration enumMBeans = monitoringProperties.keys();
    while (enumMBeans.hasMoreElements()) {
      String mbeanName = (String) enumMBeans.nextElement();
      String mbeanAttr = (String) monitoringProperties.get(mbeanName);
      
      try {
        Set mBeans = MXWrapper.queryNames(new ObjectName(mbeanName));
        if (mBeans != null) {
          for (Iterator iterator = mBeans.iterator(); iterator.hasNext();) {
            ObjectName mBean = (ObjectName) iterator.next();
            StringTokenizer st = new StringTokenizer(mbeanAttr, ",");
            while (st.hasMoreTokens()) {
              String token = st.nextToken();
              if (token.equals("*")) {
                try {
                  MBeanAttributeInfo[] attributes = MXWrapper.getAttributes(mBean);
                  if (attributes != null) {
                    for (int i = 0; i < attributes.length; i++) {
                      setMessageProperty(message, mBean, attributes[i].getName());
                    }
                  }
                } catch (Exception exc) {
                  if (logger.isLoggable(BasicLevel.WARN))
                    logger.log(BasicLevel.ERROR, " getAttributes  on " + mBean + " error.", exc);
                }
              } else {
                setMessageProperty(message, mBean, token.trim());
              }
            }
          }
        }
      } catch (MalformedObjectNameException exc) {
        logger.log(BasicLevel.ERROR, "Invalid MBean name : " + mbeanName, exc);
      }
    }

    ClientMessages clientMessages = new ClientMessages(-1, -1, message);
    processMessages(clientMessages);
  }
  
  private void setMessageProperty(Message message, ObjectName mbeanName, String attrName) {
    try {
      Object monit = MXWrapper.getAttribute(mbeanName, attrName);
      if (monit != null) {
        if (monit instanceof Boolean || monit instanceof Byte || monit instanceof Short
            || monit instanceof Integer || monit instanceof Long || monit instanceof Float
            || monit instanceof Double || monit instanceof String) {
          message.setProperty(mbeanName + "," + attrName, monit);
        } else {
          message.setProperty(mbeanName + "," + attrName, monit.toString());
        }
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, " getAttribute " + attrName + " on " + mbeanName + " error.", exc);
    }
  }
  
  public long getNbMsgsReceiveSinceCreation() {
    return receivedMessagesCount;
  }

}

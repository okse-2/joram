/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;

/**
 * The <code>MonitoringQueueImpl</code> class implements the monitoring
 * behavior, it delivers monitoring messages when requested.
 */
public class MonitoringQueueImpl extends QueueImpl implements MonitoringQueueImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Counter of messages produced by this Monitoring topic. */
  private long msgCounter = 0;

  /** The various elements to monitor. */
  private Vector elements;
  
  /** Tells if the messages produced are persistent. */
  private boolean isPersistent = false;
  
  /**
   * Returns true if the messages produced are persistent.
   * 
   * @return true if the messages produced are persistent.
   */
  public boolean isMessagePersistent() {
    return isPersistent;
  }
  
  /**
   * Sets the DeliveryMode value for the produced messages.
   * if the parameter is true the messages produced are persistent.
   * 
   * @param isPersistent if true the messages produced are persistent.
   */
  public void setMessagePersistent(boolean isPersistent) {
    this.isPersistent = isPersistent;
  }
  
  /** The priority of produced messages. */
  private int priority = 4;
  
  /**
   * Returns the priority  of produced messages.
   * 
   * @return the priority of produced messages.
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets the priority of produced messages.
   * 
   * @param priority the priority to set.
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /** The duration of produced messages. */
  private long expiration = -1;

  /**
   * Returns the expiration value for produced messages.
   * 
   * @return the expiration value for produced messages.
   */
  public long getExpiration() {
    return expiration;
  }

  /**
   * Sets the expiration value for produced messages.
   * 
   * @param expiration the expiration to set.
   */
  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  /**
   * Constructs a <code>MonitoringQueueImpl</code> instance.
   *
   * @param adminId  Identifier of the administrator of the queue.
   * @param properties     The initial set of properties.
   */
  public MonitoringQueueImpl(AgentId adminId, Properties properties) {
    super(adminId, properties);
    elements = new Vector();

    if (properties != null) {
      Enumeration e = properties.keys();
      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();

        try {
          if (name.equals("period"))
            period = ConversionHelper.toLong(properties.get("period"));
          else if (name.equals("persistent"))
            isPersistent = ConversionHelper.toBoolean(properties.get("persistent"));
          else if (name.equals("priority"))
            priority = ConversionHelper.toInt(properties.get("priority"));
          else if (name.equals("expiration"))
            expiration = ConversionHelper.toLong(properties.get("expiration"));
          else {
            String attributes = (String) properties.get(name);
            elements.add(new MonitoringElement(name, attributes));
          }
        } catch (Exception exc) {
          logger.log(BasicLevel.ERROR, "MonitoringTopicImpl.<init>: bad initialization.", exc);
        }
      }
    }
  }
  
  /**
   * Initializes the destination.
   * 
   * @param firstTime   true when first called by the factory
   */
  public void initialize(boolean firstTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "initialize(" + firstTime + ')');
    
    super.initialize(firstTime);
  }
  
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringQueueImpl. preProcess(" + from + ", " + cm + ')');
    
    long period = this.period;
    
    Vector msgs = cm.getMessages();
    for (int i=0; i<msgs.size(); i++) {
      Message msg = (Message) msgs.elementAt(i);
      
      if (msg.properties != null) {
        // New monitoring properties
        elements.clear();

        Enumeration e = msg.properties.keys();
        while (e.hasMoreElements()) {
          String name = (String) e.nextElement();

          try {
            if (name.equals("period"))
              period = ConversionHelper.toLong(msg.properties.get("period"));
            else if (name.equals("persistent"))
              isPersistent = ConversionHelper.toBoolean(msg.properties.get("persistent"));
            else if (name.equals("priority"))
              priority = ConversionHelper.toInt(msg.properties.get("priority"));
            else if (name.equals("expiration"))
              expiration = ConversionHelper.toLong(msg.properties.get("expiration"));
            else {
              String attributes = (String) msg.properties.get(name);
              elements.add(new MonitoringElement(name, attributes));
            }
          } catch (Exception exc) {
            logger.log(BasicLevel.ERROR, "MonitoringTopicImpl.<init>: bad configuration.", exc);
          }
        }
      
        msg.properties = null;
        MonitoringHelper.getJMXValues(msg, elements);
      }
    }
    
    setPeriod(period);

    return cm;
  }
  
  private String createMessageId() {
    msgCounter++;
    return "ID:" + getId().toString() + '_' + msgCounter;
  }

  /**
   * When the queue is waken up, collect the monitoring information required and
   * send it.
   */
  public void wakeUpNot(WakeUpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " MonitoringTopicImpl.wakeUpNot(" + not + ")");
    
    super.wakeUpNot(not);
    
    long currentTime = System.currentTimeMillis();
    Message message = new Message();
    message.id = createMessageId();
    message.timestamp = currentTime;
    message.persistent = isPersistent;
    message.setDestination(getId().toString(), message.TOPIC_TYPE);
    message.priority = priority;
    if (expiration > 0) {
      message.expiration = currentTime + expiration;
    } else {
      message.expiration = 0;
    }
    
    MonitoringHelper.getJMXValues(message, elements);
    ClientMessages clientMessages = new ClientMessages(-1, -1, message);
    addClientMessages(clientMessages);
  }

  /**
   * Returns the comma separated list of all monitored attributes.
   * 
   * @return the comma separated list of all monitored attributes.
   */
  public String[] getMonitoredAttributes() {
    return MonitoringHelper.getMonitoredAttributes(elements);
  }
  
  /**
   * Add the specified attributes to the list of monitored attributes.
   * If the Mbean is already monitored, the specified list of attributes
   * overrides the existing one.
   * 
   * @param MBeanName   the name of the MBean.
   * @param attributes  the comma separated list of attributes to monitor.
   */
  public void addMonitoredAttributes(String MBeanName, String attributes) {
    MonitoringHelper.addMonitoredAttributes(elements, MBeanName, attributes);
  }
  
  /**
   * Removes all the attributes of the specified MBean in the list of
   * monitored attributes.
   * 
   * @param mbean the name of the MBean.
   */
  public void delMonitoredAttributes(String mbean) {
    MonitoringHelper.delMonitoredAttributes(elements, mbean);
  }

  public String toString() {
    return "MonitoringQueueImpl:" + getId().toString();
  }
}

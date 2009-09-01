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
package com.scalagent.joram.mom.dest.collector;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.joram.mom.dest.TopicImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.WakeUpTask;

/**
 * The <code>CollectorTopicImpl</code> class implements the MOM collector topic behavior,
 * basically distributing the received messages to subscribers.
 */
public class CollectorTopicImpl extends TopicImpl implements CollectorDestination, CollectorTopicImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(CollectorTopicImpl.class.getName());

  public static final String DEFAULT_COLLECTOR = "com.scalagent.joram.mom.dest.collector.URLCollector";
  
  private Properties prop = null;
  private Collector collector;
  private long messageExpiration = 0;
  private boolean messagePersistent = true;
  private long count = 0;
  private WakeUpTask task;
  
  /**
   * constructor.
   * 
   * @param adminId admin agent.
   * @param prop properties.
   */
  public CollectorTopicImpl(AgentId adminId, Properties prop) {
    super(adminId, prop);
    this.prop = prop;
    setMessageExpiration(prop.getProperty("collector.expirationMessage"));
    setMessagePersistent(prop.getProperty("collector.persistentMessage"));
    
    String className = prop.getProperty("collector.ClassName", DEFAULT_COLLECTOR);
    Class clazz;
    try {
      clazz = Class.forName(className);
      collector = (Collector) clazz.newInstance();
      collector.setCollectorDestination(this);
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.<init> prop = " + prop + ", collector = " + collector);
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  /** 
   * create wake up task and wake up this collector.
   * 
   * @see org.objectweb.joram.mom.dest.TopicImpl#initialize(boolean)
   */
  public void initialize(boolean firstTime) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.initialize(" + firstTime + ')'); 
    super.initialize(firstTime);
    if (firstTime) {
      task = new WakeUpTask(getId(), CollectorWakeUpNot.class);
      // execute collector wake up.
      collectorWakeUp();
    }
  }
  
  /**
   * set properties, messageExpiration, messagePersistence 
   * and wake up this collector if period changed.
   * 
   * @param prop properties.
   */
  public void setProperties(Properties prop) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.setProperties(" + prop + ')');
    
    String period = this.prop.getProperty("collector.period");
    this.prop = prop;
    setMessageExpiration(prop.getProperty("collector.expirationMessage"));
    setMessagePersistent(prop.getProperty("collector.persistentMessage"));
    if (! period.equals(prop.getProperty("collector.period"))) {
      // execute collector wake up.
      collectorWakeUp();
    }
  }
  /**
   * add a property (used by MBean).
   * 
   * @param key
   * @param value
   * @see com.scalagent.joram.mom.dest.collector.CollectorQueueImplMBean#addProperty(java.lang.String, java.lang.String)
   */
  public void setProperty(String key, String value) {
    if (this.prop == null) 
      return;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,"setProperty(" + key + ", " + value + ')');

    if (key.equals("collector.period")) {
      if (! value.equals(prop.getProperty("collector.period"))) {
        this.prop.setProperty(key, value);
        collectorWakeUp();
      }
    } else {
      this.prop.setProperty(key, value);
      setProperties(this.prop);
    }
  }
  
  /**
   * set the message expiration.
   * 
   * @param expiration message expiration.
   */
  public void setMessageExpiration(String expiration) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.setMessageExpiration(" + expiration + ')');
    
    if (expiration != null) {
      messageExpiration = Long.valueOf(expiration).longValue();
    }
  }
  
  /**
   * set the message persistence.
   * 
   * @param persistent true if persistent.
   */
  public void setMessagePersistent(String persistent) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.setMessagePersistent(" + persistent + ')');
    
    if (persistent != null) {
      messagePersistent = Boolean.valueOf(persistent).booleanValue();
    }
  }
  
  /**
   * get the collector period.
   * 
   * @return string collector period.
   */
  public String getCollectorPeriod() {
    return prop.getProperty("collector.period");
  }
  
  /**
   * wake up the collector (do check)
   * and schedule task.
   */
  public void collectorWakeUp() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.collectorWakeUp()");
    
    try {
      collector.check();
    } catch (IOException e) {
      // TODO Auto-generated catch block
    }
    
    CollectorHelper.scheduleTask(task, getCollectorPeriod());
  }
  
  /**
   * update collector properties.
   * 
   * @see org.objectweb.joram.mom.dest.DestinationImpl#preProcess(fr.dyade.aaa.agent.AgentId, org.objectweb.joram.mom.notifications.ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages msgs) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Change collector properties preProcess(" + from + ", " + msgs + ')');
    
    Properties prop = new Properties();
    try  {
      Message msg = (Message) msgs.getMessages().get(msgs.getMessages().size() - 1);
      if (msg.properties != null) {
        Enumeration enumProperties = msg.properties.keys();
        while (enumProperties.hasMoreElements()) {
          String key = (String) enumProperties.nextElement();
          Object value = msg.properties.get(key);
          prop.put(key, value);
        }
      }
      if (prop != null && !prop.isEmpty()) {
        CollectorHelper.cancelTask(task);
        task = new WakeUpTask(getId(), CollectorWakeUpNot.class);
        setProperties(prop);
      }
      return null;
    } catch (Exception e) {
      return msgs;
    }
  }
  
  /**
   * get properties.
   * 
   * @see com.scalagent.joram.mom.dest.collector.CollectorDestination#getProperties()
   */
  public Properties getProperties() {
    return prop;
  }

  /**
   * send message and properties.
   * construct the client messages and send message to the subscribers.
   * 
   * @param type message type.
   * @param body the message body.
   * @param properties the message properties.
   * 
   * @see com.scalagent.joram.mom.dest.collector.CollectorDestination#sendMessage(int, byte[], java.util.Properties)
   */
  public void sendMessage(int type, byte[] body, org.objectweb.joram.shared.util.Properties properties) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.sendMessage(" + type + ", " + body + ", " + properties + ')'); 
    
    // create shared message
    Message msg = CollectorHelper.createMessage(
        type, 
        body, 
        properties, 
        messageExpiration, 
        messagePersistent,
        "collectorTopic_" + count);
    // increment message counter
    count++;
    
    // create client message
    ClientMessages clientMsgs = CollectorHelper.createClientMessages(msg);

    // Forwarding the messages to the father or the cluster fellows, if any:
    forwardMessages(clientMsgs);
    // Processing the messages:
    processMessages(clientMsgs);
  }
}

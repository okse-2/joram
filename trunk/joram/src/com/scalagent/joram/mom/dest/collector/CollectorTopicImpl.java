/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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
import java.util.Vector;

import org.objectweb.joram.mom.dest.TopicImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;

/**
 * The <code>CollectorTopicImpl</code> class implements the MOM collector topic behavior,
 * basically distributing the received messages to subscribers.
 */
public class CollectorTopicImpl extends TopicImpl implements CollectorDestination, CollectorTopicImplMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(CollectorTopicImpl.class.getName());
  
  private Collector collector;
  private long messageExpiration = 0;
  private boolean messagePersistent = false;
  private long count = 0;
  
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
   * Constructs a <code>CollectorTopicImpl</code> instance.
   * 
   * @param adminId Identifier of the administrator of the topic.
   * @param prop    The initial set of properties.
   */
  public CollectorTopicImpl(AgentId adminId, Properties properties) {
    super(adminId, properties);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.<init> prop = " + properties );
    
    if (properties != null) {
      Enumeration e = properties.keys();
      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();
        
        try {
          if (name.equals(WAKEUP_PERIOD)) {
            //nothing to do, see DestinationImpl
          } else if (name.equals(PERSISTENT_MSG))
            isPersistent = ConversionHelper.toBoolean(properties.get(PERSISTENT_MSG));
          else if (name.equals(PRIORITY_MSG))
            priority = ConversionHelper.toInt(properties.get(PRIORITY_MSG));
          else if (name.equals(EXPIRATION_MSG))
            expiration = ConversionHelper.toLong(properties.get(EXPIRATION_MSG));
          else if (name.equals(CLASS_NAME)) {
            String className = ConversionHelper.toString(properties.get(CLASS_NAME));
            if (className == null)
              className = DEFAULT_COLLECTOR;
            createCollector(className, properties);
          }
        } catch (Exception exc) {
          logger.log(BasicLevel.ERROR, "CollectorTopicImpl.<init>: bad initialization.", exc);
        }
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.<init> period = " + getPeriod() + ", collector = " + collector);
    }
  }
 
  private void createCollector(String className, Properties properties) 
  throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    Class clazz;
    clazz = Class.forName(className);
    collector = (Collector) clazz.newInstance();
    collector.setCollectorDestination(this);
    collector.setProperties(properties);
  }

  private Properties transform(fr.dyade.aaa.common.stream.Properties properties) {
    if (properties == null)
      return null;
    Properties prop = new Properties();
    Enumeration e = properties.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      prop.put(key, properties.get(key));
    }
    return prop;
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
      try {
        if (collector != null)
          collector.check();
      } catch (IOException e) {
        // TODO Auto-generated catch block
      }
    }
  }
  
  /**
   * wake up the collector (do check)
   * and schedule task.
   */
  public void wakeUpNot(WakeUpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorTopicImpl.collectorWakeUp()"); 
    
    super.wakeUpNot(not);
    
    try {
      collector.check();
    } catch (IOException e) {
      // TODO Auto-generated catch block
    }  
  }
  
  /**
   * update collector properties.
   * 
   * @see org.objectweb.joram.mom.dest.DestinationImpl#preProcess(fr.dyade.aaa.agent.AgentId, org.objectweb.joram.mom.notifications.ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Change collector properties. preProcess(" + from + ", " + cm + ')');
    
    long period = getPeriod();
    
    
    Vector msgs = cm.getMessages();
    for (int i=0; i<msgs.size(); i++) {
      Message msg = (Message) msgs.elementAt(i);
      
      if (msg.properties != null) {
        Enumeration enumProperties = msg.properties.keys();
        while (enumProperties.hasMoreElements()) {
          String key = (String) enumProperties.nextElement();
          
          try {
            if (key.equals(WAKEUP_PERIOD))
              period = ConversionHelper.toLong(msg.properties.get(WAKEUP_PERIOD));
            else if (key.equals(PERSISTENT_MSG))
              isPersistent = ConversionHelper.toBoolean(msg.properties.get(PERSISTENT_MSG));
            else if (key.equals(PRIORITY_MSG))
              priority = ConversionHelper.toInt(msg.properties.get(PRIORITY_MSG));
            else if (key.equals(EXPIRATION_MSG))
              expiration = ConversionHelper.toLong(msg.properties.get(EXPIRATION_MSG));
            else if (key.equals(CLASS_NAME)) {
              String className = ConversionHelper.toString(msg.properties.get(CLASS_NAME));
              if (className == null)
                className = DEFAULT_COLLECTOR;
              createCollector(className, transform(msg.properties));
            }
          } catch (Exception exc) {
            logger.log(BasicLevel.ERROR, "CollectorTopicImpl.<init>: bad configuration.", exc);
          }
        }
      }
      msg.properties = null;
    }
    setPeriod(period);
      
    return null;
  }

  public String toString() {
    return "CollectorTopicImpl:" + getId().toString();
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
  public void sendMessage(int type, byte[] body, fr.dyade.aaa.common.stream.Properties properties) {
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

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;

import fr.dyade.aaa.common.Debug;

public class Helper {

  public static final String BUNDLE_CF_PROP = "rest.jms.connectionFactory";
  public static final String BUNDLE_JNDI_FACTORY_INITIAL_PROP = "rest.jndi.factory.initial";
  public static final String BUNDLE_JNDI_FACTORY_HOST_PROP = "rest.jndi.factory.host";
  public static final String BUNDLE_JNDI_FACTORY_PORT_PROP = "rest.jndi.factory.port";
  public static final String BUNDLE_IDLE_TIMEOUT_PROP = "rest.idle.timeout";
  public static final String BUNDLE_CLEANER_PERIOD_PROP = "rest.cleaner.period";
  
  public static Logger logger = Debug.getLogger(Helper.class.getName());
  private static final AtomicLong counter = new AtomicLong(100);
  private static Helper helper = null;
  private InitialContext ictx;
  private HashMap<String, RestClientContext> restClientCtxs;
  private HashMap<String, SessionContext> sessionCtxs;
  private String cfName;
  private BundleContext bundleContext;
  private long globalIdleTimeout;
  private Properties jndiProps;
  
  private Helper() {
    restClientCtxs = new HashMap<String, RestClientContext>();
    sessionCtxs = new HashMap<String, SessionContext>();
  }
  
  static public Helper getInstance() {
    if (helper == null)
      helper = new Helper();
    return helper;
  }
  
  public void setGlobalProperties(BundleContext bundleContext) throws NamingException {
    this.bundleContext = bundleContext;
    // set the connection factory
    setConnectionFactoryName(bundleContext.getProperty(BUNDLE_CF_PROP));
    
    // set the jndi properties
    if (bundleContext.getProperty(BUNDLE_JNDI_FACTORY_INITIAL_PROP) != null && 
        bundleContext.getProperty(BUNDLE_JNDI_FACTORY_HOST_PROP) != null &&
        bundleContext.getProperty(BUNDLE_JNDI_FACTORY_PORT_PROP) != null) {
      jndiProps = new Properties();
      jndiProps.setProperty("java.naming.factory.initial", bundleContext.getProperty(BUNDLE_JNDI_FACTORY_INITIAL_PROP));
      jndiProps.setProperty("java.naming.factory.host", bundleContext.getProperty(BUNDLE_JNDI_FACTORY_HOST_PROP));
      jndiProps.setProperty("java.naming.factory.port", bundleContext.getProperty(BUNDLE_JNDI_FACTORY_PORT_PROP));
    } else {
      jndiProps = new Properties();
      jndiProps.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      jndiProps.setProperty("java.naming.factory.host", "localhost");
      jndiProps.setProperty("java.naming.factory.port", "16400");
    }
    // TODO: use the osgi service jndi ?
//    ServiceReference<ObjectFactory> ref = context.getServiceReference(javax.naming.spi.ObjectFactory.class);
//    ObjectFactory jndiFactory = bundleContext.getService(ref);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "jndiProperties = " + jndiProps);
    
    // set default idle timeout
    String value = bundleContext.getProperty(BUNDLE_IDLE_TIMEOUT_PROP);
    if (value != null && !value.isEmpty())
      globalIdleTimeout = Long.parseLong(value);
   }
  
  /**
   * @return the restClientCtxs
   */
  public HashMap<String, RestClientContext> getRestClientCtxs() {
    return restClientCtxs;
  }

  /**
   * @param cfName the connection factory name
   */
  public void setConnectionFactoryName(String cfName) {
    if (cfName != null) {
      this.cfName = cfName;
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Helper.setConnectionFactoryName = " + this.cfName);
    } else {
      this.cfName = "cf";
    }
  }
  
  /**
   * @throws Exception
   */
  public void closeAll() throws Exception {
    ArrayList<RestClientContext> values = new ArrayList<RestClientContext>(restClientCtxs.values());
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.closeAll " + values);
    for (RestClientContext restClientCtx : values) {
      close(restClientCtx.getClientId());
    }
    // close jndi
    if (ictx != null)
      ictx.close();
  }
  
  /**
   * @param clientId the client id
   */
  public void close(String clientId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.close " + clientId);
    RestClientContext restClientCtx = restClientCtxs.get(clientId);
    if (restClientCtx != null) {
      ArrayList<String> names = new ArrayList<String>(restClientCtx.getSessionCtxNames());
      for (String name : names) {
        closeSessionCtx(name);
      }
      restClientCtxs.remove(clientId);
    }
  }

  /**
   * @param ctxName the producer or consumer name
   */
  public void closeSessionCtx(String ctxName) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.closeSessionCtx " + ctxName);
    SessionContext ctx = sessionCtxs.get(ctxName);
    if (ctx != null) {
      RestClientContext restClientCtx = ctx.getClientCtx();
      ctx.getJmsContext().close();
      sessionCtxs.remove(ctxName);
      restClientCtx.removeSessionCtxNames(ctxName);
      if (restClientCtx.getSessionCtxNames().isEmpty()) {
        restClientCtx.getJmsContext().close();
        restClientCtxs.remove(restClientCtx.getClientId());
      }
    }
  }

  /**
   * Lookup the destination
   * 
   * @param destName the destination name
   * @return the destination if found
   * @throws NamingException
   */
  public Destination lookupDestination(String destName) throws NamingException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.lookupDestination " + destName);
    Object obj = lookup(destName);
    if (obj instanceof Queue || obj instanceof Topic) {
      return (Destination) obj;
    }
    return null;
  }
  
  public Object lookup(String name) throws NamingException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.lookup " + name);
    if (ictx == null)
      ictx = new InitialContext(jndiProps);
    return ictx.lookup(name);
  }
  
  /**
   * Create a producer
   * 
   * @param userName the user name
   * @param password the password
   * @param clientId the client id
   * @param prodName the producer name
   * @param dest the JMS destination
   * @param sessionMode the session mode
   * @param deliveryMode the delivery mode
   * @param deliveryDelay the delivery delay
   * @param correlationID the correlation id
   * @param priority the priority
   * @param timeToLive the time to live
   * @param destName need for the jms create
   * @param isQueue
   * @param idleTimeout 
   * @return the producer name
   * @throws Exception
   */
  public String createProducer(
      String userName,
      String password,
      String clientId,
      String prodName,
      Destination dest, 
      int sessionMode, 
      int deliveryMode,
      long deliveryDelay,
      String correlationID, 
      int priority, 
      long timeToLive,
      String destName,
      boolean isQueue, 
      long idleTimeout) throws Exception {
    
    String prodId = prodName;
    if (prodId == null) {
      // create the new producer Id
      prodId = createProducerId();
    }
    
    // Get the rest client context
    RestClientContext restClientCtx = getClientContext(clientId);
    if (restClientCtx.getJmsContext() == null) {
      if (restClientCtx.getConnectionFactory() == null) {
        restClientCtx.setConnectionFactory((ConnectionFactory) lookup(cfName));
        if (idleTimeout != 0)
          restClientCtx.setIdleTimeout(idleTimeout);
        else if (globalIdleTimeout > 0)
          restClientCtx.setIdleTimeout(globalIdleTimeout);
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Helper.createProducer cf = " + restClientCtx.getConnectionFactory());
      JMSContext jmsContext;
      if (userName != null && !userName.isEmpty())
        jmsContext = restClientCtx.getConnectionFactory().createContext(userName, password);
      else
        jmsContext = restClientCtx.getConnectionFactory().createContext();
      jmsContext.setClientID(restClientCtx.getClientId());
      jmsContext.setAutoStart(false);//the defaul is true.
      restClientCtx.setJmsContext(jmsContext);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Helper.createProducer jmsContext = " + restClientCtx.getJmsContext());
    }
    
    SessionContext prodContext = sessionCtxs.get(prodId);
    if ( prodContext == null) {
      // create a new producer context
      prodContext = new ProducerContext(restClientCtx);
      prodContext.setJmsContext(restClientCtx.getJmsContext().createContext(sessionMode));
      JMSProducer  producer = prodContext.getJmsContext().createProducer();
      producer.setDeliveryMode(deliveryMode);
      if (correlationID != null)
        producer.setJMSCorrelationID(correlationID);
      producer.setPriority(priority);
      producer.setTimeToLive(timeToLive);
      producer.setDeliveryDelay(deliveryDelay);
      ((ProducerContext) prodContext).setProducer(producer);
      sessionCtxs.put(prodId, prodContext);
      restClientCtx.addSessionCtxNames(prodId);

      Destination destination = dest;
      // set the destination
      if (destination == null) {
        // create the jms destination
        if (isQueue)
          destination = restClientCtx.getJmsContext().createQueue(destName);
        else
          destination = restClientCtx.getJmsContext().createTopic(destName);
      }
      prodContext.setDest(destination);
    }
    return prodId;
  }
  
  public String createConsumer(
      String userName,
      String password,
      String clientId,
      String consName,
      Destination dest, 
      int sessionMode,
      String messageSelector,
      boolean noLocal,
      boolean durable,
      boolean shared,
      String name,
      String destName,
      boolean isQueue, 
      long idleTimeout) throws Exception {
    
    String consId = consName;
    if (consId == null) {
      // create the new consumer Id
      consId = createConsumerId();
    }
    
    // Get the rest client context
    RestClientContext restClientCtx = getClientContext(clientId);
    if (restClientCtx.getJmsContext() == null) {
      if (restClientCtx.getConnectionFactory() == null) {
        restClientCtx.setConnectionFactory((ConnectionFactory) lookup(cfName));
        if (idleTimeout != 0)
          restClientCtx.setIdleTimeout(idleTimeout);
        else if (globalIdleTimeout > 0)
          restClientCtx.setIdleTimeout(globalIdleTimeout);
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Helper.createConsumer cf = " + restClientCtx.getConnectionFactory());
      JMSContext jmsContext;
      if (userName != null && !userName.isEmpty())
        jmsContext = restClientCtx.getConnectionFactory().createContext(userName, password);
      else
        jmsContext = restClientCtx.getConnectionFactory().createContext();
      restClientCtx.setJmsContext(jmsContext);
      restClientCtx.getJmsContext().setClientID(restClientCtx.getClientId());
      restClientCtx.getJmsContext().setAutoStart(false);//the defaul is true.
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Helper.createConsumer jmsContext = " + restClientCtx.getJmsContext());
    }

    SessionContext consumerContext = sessionCtxs.get(consId);
    if (consumerContext == null) {
      // create the consumer context
      consumerContext = new ConsumerContext(restClientCtx);

      Destination destination = dest;
      // set the destination
      if (destination == null) {
        // create the jms destination
        if (isQueue)
          destination = restClientCtx.getJmsContext().createQueue(destName);
        else
          destination = restClientCtx.getJmsContext().createTopic(destName);
      }
      consumerContext.setDest(destination);

      consumerContext.setJmsContext(restClientCtx.getJmsContext().createContext(sessionMode));
      //TODO : test if dest == TOPIC for durable and shared
      if (durable && !shared) {
        if (messageSelector == null) {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createDurableConsumer((Topic) consumerContext.getDest(), name));
        } else {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createDurableConsumer((Topic) consumerContext.getDest(), name, messageSelector, noLocal));
        }
      } else if (durable && shared) {
        if (messageSelector == null) {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createSharedDurableConsumer((Topic) consumerContext.getDest(), name));
        } else {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createSharedDurableConsumer((Topic) consumerContext.getDest(), name, messageSelector));
        }
      } else if (shared) {
        if (messageSelector == null) {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createSharedConsumer((Topic) consumerContext.getDest(), name));
        } else {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createSharedConsumer((Topic) consumerContext.getDest(), name, messageSelector));
        }
      } else {
        if (messageSelector == null) {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createConsumer(consumerContext.getDest()));
        } else {
          ((ConsumerContext) consumerContext).setConsumer(consumerContext.getJmsContext().createConsumer(consumerContext.getDest(), messageSelector, noLocal));
        } 
      }

      // put the consumer context in consumers map
      sessionCtxs.put(consId, consumerContext);
      restClientCtx.addSessionCtxNames(consId);

      // start the connection if autostart != true
      if (!consumerContext.getJmsContext().getAutoStart())
        consumerContext.getJmsContext().start();
    }
    
    return consId;
  }

  private void setMapMessage(Map<String, Object> jsonMap, MapMessage msg) throws Exception {
    if (jsonMap == null)
      return;

    // parse the json map
    for (String key : jsonMap.keySet()) {
      Object value = jsonMap.get(key);
      if (value instanceof ArrayList) {
        ArrayList<String> array =(ArrayList<String>) value; 
        try {
          if (array.size() == 2) {
            String className = array.get(1);
            Constructor<?> constructor = Class.forName(className).getConstructor(String.class);
            value = constructor.newInstance(array.get(0));
          }
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "setMapMessage: ignore map entry " + key + ", " + value + " : " + e.getMessage());
          continue;
        }
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "setMapMessage: " + key + ", value = " + value + ", " + value.getClass().getSimpleName());

      switch (value.getClass().getSimpleName()) {
      case "String":
        msg.setString(key, (String) value);
        break;
      case "Boolean":
        msg.setBoolean(key, (Boolean)value);
        break;
      case "Integer":
        msg.setInt(key, (Integer)value);
        break;
      case "Double":
        msg.setDouble(key, (Double)value);
        break;
      case "Float":
        msg.setFloat(key, (Float)value);
        break;
      case "Short":
        msg.setShort(key, (Short)value);
        break;
      case "Char":
        msg.setChar(key, (char)value);
        break;
      case "Byte":
        msg.setByte(key, (Byte)value);
        break;
      case "Bytes":
        msg.setBytes(key, (byte[])value);
        break;

      default:
        try {
          msg.setObjectProperty(key, value);
        } catch (Exception e) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "ignore jms setObjectProperties(" + key + ", " + value + ") : " + e.getMessage());
        }
        break;
      }
    }
  }
 
  private Object getValue(Map map, String key) throws Exception {
    Object value = map.get(key);
    if (value instanceof ArrayList) {
      ArrayList<String> array =(ArrayList<String>) value; 
      try {
        if (array.size() == 2) {
          String className = array.get(1);
          Constructor<?> constructor = Class.forName(className).getConstructor(String.class);
          value = constructor.newInstance(array.get(0));
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "getValue(" + key + ", " + value + ") : " + e.getMessage());
        throw e;
      }
    }
    return value;
  }
  
  /**
   * @param prodName
   * @param type
   * @param jmsHeaders
   * @param jmsProps
   * @param jmsBody
   * @param deliveryMode
   * @param deliveryTime
   * @param priority
   * @param timeToLive
   * @param correlationID
   * @return
   * @throws Exception
   */
  public long send(
      String prodName,
      String type, 
      Map<String, Object> jmsHeaders, 
      Map<String, Object> jmsProps, 
      Object jmsBody, 
      int deliveryMode, 
      long deliveryTime, 
      int priority, 
      long timeToLive, 
      String correlationID) throws Exception {

    try {
      ProducerContext producerCtx = (ProducerContext) sessionCtxs.get(prodName);
      if (producerCtx == null)
        throw new Exception(prodName + " not found.");
      
      Message msg = null;
      
      if (type.equals(TextMessage.class.getSimpleName())) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "send text message = " + jmsBody);
        // create the text message
        msg = producerCtx.getJmsContext().createTextMessage((String) jmsBody);
        
      } else if(type.equals(BytesMessage.class.getSimpleName())) {
        // create the byte message
        if (jmsBody instanceof ArrayList) {
          msg = producerCtx.getJmsContext().createBytesMessage();
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "send bytes message = " + jmsBody);
          byte[] bytes = new byte[((ArrayList) jmsBody).size()];
          for (int i = 0; i < ((ArrayList) jmsBody).size(); i++) {
            Object value = ((ArrayList) jmsBody).get(i);
            bytes[i] = ((Number) value).byteValue();
          }
          ((BytesMessage) msg).writeBytes(bytes);
          ((BytesMessage) msg).reset();
        } else {
          throw new Exception("BytesMessage: invalid jmsBody = " + jmsBody.getClass().getName());
        }

      } else if(type.equals(MapMessage.class.getSimpleName())) {
        // create the map message
        if (jmsBody instanceof Map) {
          msg = producerCtx.getJmsContext().createMapMessage();
          setMapMessage((Map) jmsBody, (MapMessage) msg);
        } else {
          throw new Exception("MapMessage: invalid jmsBody = " + jmsBody.getClass().getName());
        }

      } else if(type.equals(ObjectMessage.class.getSimpleName())) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "send object message = " + jmsBody);
        // create the object message
        //msg = producerCtx.getJmsContext().createObjectMessage((Serializable) jmsBody);
        throw new Exception("type: " + type + ", not yet implemented");
        
      } else if(type.equals(StreamMessage.class.getSimpleName())) {
        // create the stream message
        //msg = producerCtx.getJmsContext().createStreamMessage();
        throw new Exception("type: " + type + ", not yet implemented");

      } else {
        throw new Exception("Unknown message type: " + type); 
      }
      
      if (jmsHeaders != null) {
        // Header
        if (deliveryMode == -1) {
          Integer value = (Integer) getValue(jmsHeaders, "DeliveryMode");
          if (value != null)
            msg.setJMSDeliveryMode(value);
        }
        if (deliveryTime == -1) {
          Long value = (Long) getValue(jmsHeaders, "DeliveryTime");
          if (value != null)
            msg.setJMSDeliveryTime(value);
        }
        if (priority == -1) {
          Integer value = (Integer) getValue(jmsHeaders, "Priority");
          if (value != null)
            msg.setJMSPriority(value);
        }
        if (timeToLive == -1) {
          Long value = (Long) getValue(jmsHeaders, "Expiration");
          if (value != null)
            msg.setJMSExpiration(value);
        }
        if (correlationID == null) {
          String value = (String) getValue(jmsHeaders, "CorrelationID");
          if (value != null)
            msg.setJMSCorrelationID(value);
        }
      }
      if (jmsProps != null) {
        // Properties
        for (String key : jmsProps.keySet()) {
          Object value = null; 
          try {
            value = getValue(jmsProps, key);
          } catch (Exception e) {
            if (logger.isLoggable(BasicLevel.ERROR))
              logger.log(BasicLevel.ERROR, "ignore set jms properties(" + key + ", " + value + ") : " + e.getMessage());
            continue;
          }
          if (value == null)
            continue;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "set jms properties: " + key + ", value = " + value + ", " + value.getClass().getSimpleName());
          
          switch (value.getClass().getSimpleName()) {
          case "String":
            msg.setStringProperty(key, (String) value);
            break;
          case "Boolean":
            msg.setBooleanProperty(key, (Boolean)value);
            break;
          case "Integer":
            msg.setIntProperty(key, (Integer)value);
            break;
          case "Double":
            msg.setDoubleProperty(key, (Double)value);
            break;
          case "Float":
            msg.setFloatProperty(key, (Float)value);
            break;
          case "Short":
            msg.setShortProperty(key, (Short)value);
            break;
          case "Byte":
            msg.setByteProperty(key, (Byte)value);
            break;

          default:
            try {
            msg.setObjectProperty(key, value);
            } catch (Exception e) {
              if (logger.isLoggable(BasicLevel.ERROR))
                logger.log(BasicLevel.ERROR, "ignore jms setObjectProperties(" + key + ", " + value + ") : " + e.getMessage());
            }
            break;
          }
        }
      }
      
      if (deliveryMode > -1)
        msg.setJMSDeliveryMode(deliveryMode);
      if (deliveryTime > -1)
        msg.setJMSDeliveryTime(deliveryTime);
      if (priority > -1)
        msg.setJMSPriority(priority);
      if (timeToLive > -1)
        msg.setJMSExpiration(timeToLive);
      if (correlationID != null)
        msg.setJMSCorrelationID(correlationID);
      
      // send the message
      producerCtx.getProducer().send(producerCtx.getDest(), msg);
      // Increment the last id
      producerCtx.incLastId();
      //update activity
      producerCtx.getClientCtx().setLastActivity(System.currentTimeMillis());
      
      return producerCtx.getLastId();
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, e);
      throw e;
    }
  }
  
  public Message consume(
      String consName,
      long timeout,
      boolean noLocal,
      boolean durable,
      boolean shared,
      String name,
      long msgId) throws Exception {
    
    ConsumerContext consumerCtx = (ConsumerContext) sessionCtxs.get(consName);
    if (consumerCtx == null)
      throw new Exception(consName + " not found.");
    
    Message message = consumerCtx.getMessage(msgId);
    if (message != null)
      return message;
    
    if (timeout > 0)
     message = consumerCtx.getConsumer().receive(timeout);
    else if (timeout == 0)
      message = consumerCtx.getConsumer().receiveNoWait();
    else {
      message = consumerCtx.getConsumer().receive();
      if (message == null) {
        throw new JMSException("The consumer expire (timeout)");
      }
    }
    
    //update activity
    consumerCtx.getClientCtx().setLastActivity(System.currentTimeMillis());
    
    if (message == null) {
      return null;
    }
    
    if (consumerCtx.getJmsContext().getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
      long id = msgId;
      if (id == -1)
        id = consumerCtx.incLastId();
      consumerCtx.put(id, message);
    } else {
      consumerCtx.incLastId();
    }
    
    return message;
  }

  public String createClientId() {
    return "clientID" + counter.getAndIncrement();
  }
  
  public String createProducerId() {
    return "prod" + counter.getAndIncrement();
  }
  
  public String createConsumerId() {
    return "cons" + counter.getAndIncrement();
  }
  
  public SessionContext getSessionCtx(String name) {
    return sessionCtxs.get(name);
  }
  
  public RestClientContext getClientContext(String id) {
    RestClientContext ret =  restClientCtxs.get(id);
    if (ret == null) {
      String clientID = id;
      if (clientID == null) {
        clientID = createClientId();
      }
      ret = new RestClientContext(clientID);
      restClientCtxs.put(clientID, ret);
    }
    return ret;
  }

  public void commit(String name) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.commit " + name);
    SessionContext ctx = sessionCtxs.get(name);
    if (ctx == null)
      throw new Exception(name + " not found.");
    if (ctx.getJmsContext().getTransacted())
      ctx.getJmsContext().commit();
  }
  
  public void acknowledgeAllMsg(String consName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.acknowledgeAllMsg " + consName);
    ConsumerContext consumerCtx = (ConsumerContext) sessionCtxs.get(consName);
    if (consumerCtx == null)
      throw new Exception(consName + " not found.");
    if (consumerCtx.getJmsContext().getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
      consumerCtx.getJmsContext().acknowledge();
      consumerCtx.clear();
    }
  }

  public void acknowledgeMsg(String consName, long id) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Helper.acknowledgeMsg " + consName + ", " + id);
    ConsumerContext consumerCtx = (ConsumerContext) sessionCtxs.get(consName);
    if (consumerCtx == null)
      throw new Exception(consName + " not found.");
    if (consumerCtx.getJmsContext().getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
      Message msg = consumerCtx.getMessage(id);
      msg.acknowledge();
      consumerCtx.removeMessage(id);
    }
  }
}

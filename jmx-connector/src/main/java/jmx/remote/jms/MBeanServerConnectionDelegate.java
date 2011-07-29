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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

import java.awt.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
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
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import jmx.remote.jms.structures.*;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Pool;

/***
 * Acts as a delegate for the MBeanServerConnection In the class
 * <b>MBeanServerConnectionDelegate<b> is implemented all the methods that can
 * be call'd by the administration tool (JConsole)
 * 
 * @author Djamel-Eddine Boumchedda
 * @version $Revision: 1.1 $
 */
public class MBeanServerConnectionDelegate implements MBeanServerConnection {
  private static final Logger logger = Debug.getLogger(MBeanServerConnectionDelegate.class.getName());
  protected Connection connection;
  MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
  static HashMap hashTableNotificationListener;
  Object key;
  static int value = 0;
  PoolRequestor poolRequestors;
  final static int  defaultValueOfSizeOfPoolRequestor = 10;

  public MBeanServerConnectionDelegate(Connection connection) throws IOException {
    this.connection = connection;
    String path = new File("").getAbsolutePath();
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Instantiation of the MBeanServerConnectionDelegate Class  : "+this.getClass().getName());
    }
    hashTableNotificationListener = new HashMap();
    poolRequestors = new PoolRequestor(connection);
    int SizePoolRequestor;
    try {
      SizePoolRequestor = Integer.parseInt(System.getProperty("SizePoolRequestor"));
      if(SizePoolRequestor <= 0){
        SizePoolRequestor = defaultValueOfSizeOfPoolRequestor;
        ShowMessageInformations showMessageInformations = new ShowMessageInformations(null,
            "Wrong input the size of the Pool Requestor, you Should choose an integer more than  0, A default value is choosen which is "+defaultValueOfSizeOfPoolRequestor,
            "Wrong Size Pool Requestor", JOptionPane.ERROR_MESSAGE);
      }
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Size of the PoolRequestor : "+SizePoolRequestor);
      }
      
      poolRequestors.initPool(SizePoolRequestor);
    } catch (Exception e) {

      SizePoolRequestor = defaultValueOfSizeOfPoolRequestor;
      poolRequestors.initPool(SizePoolRequestor);
      ShowMessageInformations showMessageInformations = new ShowMessageInformations(null,
          "Wrong input the size of the Pool Requestor, you Should choose an integer more than  0, A default value is choosen which is"+defaultValueOfSizeOfPoolRequestor,
          "Wrong Size Pool Requestor", JOptionPane.ERROR_MESSAGE);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Size of the PoolRequestor : "+SizePoolRequestor);
      }
      e.printStackTrace();
    }
  }

  public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
      IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : ObjectInstance createMBean(String className, ObjectName name)" );
    }
    CreateMBean createMbean = new CreateMBean(className, name);
    ObjectInstance objectInstanceResult = null;
    try {
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(createMbean);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }

      return objectInstanceResult;

    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;
  }

  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, InstanceNotFoundException, IOException {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)" );
      }
      CreateMBean1 createMBean1 = new CreateMBean1(className, name, loaderName);
      ObjectInstance objectInstanceResult = null;
      try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(createMBean1);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;
  }

  public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, IOException {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)" );
      }
      CreateMBean2 createMBean2 = new CreateMBean2(className, name, params, signature);
      ObjectInstance objectInstanceResult = null;
      try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(createMBean2);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;

  }

  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,
      Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException,
      IOException {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,Object[] params, String[] signature)" );
      }
      CreateMBean3 createMBean3 = new CreateMBean3(className, name, loaderName, params, signature);
      ObjectInstance objectInstanceResult = null;
      try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(createMBean3);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;

  }

  public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException,
      IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void unregisterMBean(ObjectName name)" );
      }
    UnregisterMbean unregisterMbean = new UnregisterMbean(name);
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(unregisterMbean);
      requestor.doReceive();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : ObjectInstance getObjectInstance(ObjectName name)" );
      }
    GetObjectInstance getObjectInstance = new GetObjectInstance(name);
    ObjectInstance objectInstanceResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getObjectInstance);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;
  }

  public Set queryMBeans(ObjectName name, QueryExp query) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : Set queryMBeans(ObjectName name, QueryExp query)" );
      }
    QueryMbeans queryMbeans = new QueryMbeans(name, query);
    Set setResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(queryMbeans);
      setResult = (Set) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return setResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return setResult;
  }

  public Set queryNames(ObjectName name, QueryExp query) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : Set queryNames(ObjectName name, QueryExp query)" );
      }
    QueryName queryNames = new QueryName(name, query);
    Set setResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(queryNames);
      setResult = (Set) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return setResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return setResult;

  }

  public boolean isRegistered(ObjectName name) throws IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : boolean isRegistered(ObjectName name)" );
      }
    IsRegistered isRegistered = new IsRegistered(name);
    Boolean booleanResult = false;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(isRegistered);
      booleanResult = (Boolean) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return booleanResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return booleanResult;
  }

  public Integer getMBeanCount() throws IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : Integer getMBeanCount()" );
      }
    GetMBeanCount getMBeanCount = new GetMBeanCount();
    Integer integerResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getMBeanCount);
      integerResult = (Integer) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return integerResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return integerResult;
  }

  public Object getAttribute(ObjectName name, String attribute) throws MBeanException,
      AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : Object getAttribute(ObjectName name, String attribute)" );
      }
    GetAttribute getAttribute = new GetAttribute(name, attribute);
    Object objectResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getAttribute);
      objectResult = requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectResult;
  }

  public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException,
      ReflectionException, IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : AttributeList getAttributes(ObjectName name, String[] attributes)" );
      }
    GetAttributes getAttributes = new GetAttributes(name, attributes);
    AttributeList attributeListResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getAttributes);
      attributeListResult = (AttributeList) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }

    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return attributeListResult;
  }

  public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException,
      AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException,
      IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void setAttribute(ObjectName name, Attribute attribute)" );
      }
    SetAttribute setAttribute = new SetAttribute(name, attribute);
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(setAttribute);
      requestor.doReceive();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public AttributeList setAttributes(ObjectName name, AttributeList attributes)
      throws InstanceNotFoundException, ReflectionException, IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : AttributeList setAttributes(ObjectName name, AttributeList attributes)" );
      }
    SetAttributes setAttributes = new SetAttributes(name, attributes);
    AttributeList attributeListResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(setAttributes);
      attributeListResult = (AttributeList) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return attributeListResult;
  }

  public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)" );
      }
    Invoke invoke = new Invoke(name, operationName, params, signature);
    Object objectResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(invoke);
      objectResult = requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectResult;

  }

  public String getDefaultDomain() throws IOException {
    Requestor requestor = null;
    GetDefaultDomain getDefaultDomain = new GetDefaultDomain();
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : String getDefaultDomain()" );
      }
    String stringResult = "getDefaultDomain is not done yet";
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getDefaultDomain);
      stringResult = (String) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return stringResult;

    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return stringResult;
  }

  public String[] getDomains() throws IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : String[] getDomains()" );
      }
    GetDomains getDomains = new GetDomains();
    String[] stringResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getDomains);
      stringResult = (String[]) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return stringResult;

    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return stringResult;
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
   * @throws JMSException
   */
  public void addNotificationListener(ObjectName name, NotificationListener listener,
      NotificationFilter filter, Object handback) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void addNotificationListener(ObjectName name, NotificationListener listener,NotificationFilter filter, Object handback)" );
      }
    value++;
    try {
      key = new Integer(value);
      AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
          name, listener, filter, handback);
      hashTableNotificationListener.put(key, objectAddNotificationListenerStored);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Content of  hashTableNotificationListener : "+hashTableNotificationListener);
        }
      AddNotificationListener addNotificationListener = new AddNotificationListener(name, filter, key);
      try {
        Requestor requestor;
        requestor = (Requestor) poolRequestors.allocRequestor();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
          logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
        }
        requestor.subscribeToNotifications(addNotificationListener);
        poolRequestors.freeRequestor(requestor);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
        }
        System.out
            .println("--> L'objet addNotificationListener contenant le name,filter et la key a ete envoye");

      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
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
   * @throws JMSException
   */

  public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,Object handback)" );
      }
    value++;
    try {
      key = new Integer(value);
      AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
          name, listener, filter, handback);
      hashTableNotificationListener.put(key, objectAddNotificationListenerStored);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Content of  hashTableNotificationListener : "+hashTableNotificationListener);
        }
      AddNotificationListener addNotificationListener = new AddNotificationListener(name, filter, key);
      try {
        Requestor requestor;
        requestor = (Requestor) poolRequestors.allocRequestor();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
          logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
        }
        requestor.subscribeToNotifications(addNotificationListener);
        poolRequestors.freeRequestor(requestor);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
        }
        System.out
            .println("--> L'objet addNotificationListener contenant le name,filter et la key a ete envoye");

      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
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
   * @throws JMSException
   */

  public void removeNotificationListener(ObjectName name, ObjectName listener)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void removeNotificationListener(ObjectName name, ObjectName listener)" );
      }
    Object keyRestored = null;
    value--;
    AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
        name, listener, null, null);
    Iterator<Map.Entry<Integer, AddNotificationListenerStored>> it = hashTableNotificationListener.entrySet()
        .iterator();
    Map.Entry<Integer, AddNotificationListenerStored> pairKeyListener;

    while (it.hasNext()) {

      pairKeyListener = it.next();
      if (pairKeyListener.getValue().equals(objectAddNotificationListenerStored)) {
        keyRestored = pairKeyListener.getKey();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Value of the KeyRestored of removeNotificationListener(ObjectName name,NotificationListener listener) : "+keyRestored );
          }
        it.remove();// remove(keyRestored);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "New Content of  hashTableNotificationListener after calling the method removeNotificationListener(ObjectName name, ObjectName listener) : "+hashTableNotificationListener);
          }
        RemoveNotificationListener2 objectRemoveNotificationListener2 = new RemoveNotificationListener2(name,
            listener, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
            logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
          }
          requestor.doRequete(objectRemoveNotificationListener2);
          poolRequestors.freeRequestor(requestor);
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
          }
        } catch (JMSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

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
   * @throws JMSException
   */

  public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,Object handback)" );
      }
    Object keyRestored = null;
    value--;
    AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
        name, listener, filter, handback);
    Iterator<Map.Entry<Integer, AddNotificationListenerStored>> it = hashTableNotificationListener.entrySet()
        .iterator();
    Map.Entry<Integer, AddNotificationListenerStored> pairKeyListener;

    while (it.hasNext()) {

      pairKeyListener = it.next();
      if (pairKeyListener.getValue().equals(objectAddNotificationListenerStored)) {
        keyRestored = pairKeyListener.getKey();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Value of the KeyRestored of removeNotificationListener(ObjectName name,NotificationListener listener) : "+keyRestored );
          }
        it.remove();// remove(keyRestored);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "New Content of  hashTableNotificationListener  after calling the method removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) : "+hashTableNotificationListener);
          }
        RemoveNotificationListener3 objectRemoveNotificationListener3 = new RemoveNotificationListener3(name,
            listener, filter, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
            logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
          }
          requestor.doRequete(objectRemoveNotificationListener3);
          poolRequestors.freeRequestor(requestor);
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
          }
          break; // The MBean must have a listener that exactly matches the
                 // given listener, filter, and handback parameters. If there is
                 // more than one such listener, only one is removed.
          
        } catch (JMSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

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
   * @throws JMSException
   */

  public void removeNotificationListener(ObjectName name, NotificationListener listener)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void removeNotificationListener(ObjectName name, NotificationListener listener)");
      }
    Object keyRestored = null;
    value--;
    AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
        name, listener, null, null);
    Iterator<Map.Entry<Integer, AddNotificationListenerStored>> it = hashTableNotificationListener.entrySet()
        .iterator();
    Map.Entry<Integer, AddNotificationListenerStored> pairKeyListener;

    while (it.hasNext()) {

      pairKeyListener = it.next();
      if (pairKeyListener.getValue().equals(objectAddNotificationListenerStored)) {
        keyRestored = pairKeyListener.getKey();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Value of the KeyRestored of removeNotificationListener(ObjectName name,NotificationListener listener) : "+keyRestored );
          }
        it.remove();// remove(keyRestored);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "New Content of  hashTableNotificationListener after calling the method removeNotificationListener(ObjectName name, NotificationListener listener) : "+hashTableNotificationListener);
          }

        RemoveNotificationListener objectRemoveNotificationListener = new RemoveNotificationListener(name,
            keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
            logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
          }
          requestor.doRequete(objectRemoveNotificationListener);
          poolRequestors.freeRequestor(requestor);
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
          }
        } catch (JMSException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

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

  public void removeNotificationListener(ObjectName name, NotificationListener listener,
      NotificationFilter filter, Object handback) throws InstanceNotFoundException,
      ListenerNotFoundException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : void removeNotificationListener(ObjectName name, NotificationListener listener,NotificationFilter filter, Object handback)");
      }
    Object keyRestored = null;
    value--;
    AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
        name, listener, filter, handback);
    Iterator<Map.Entry<Integer, AddNotificationListenerStored>> it = hashTableNotificationListener.entrySet()
        .iterator();
    Map.Entry<Integer, AddNotificationListenerStored> pairKeyListener;

    while (it.hasNext()) {

      pairKeyListener = it.next();
      if (pairKeyListener.getValue().equals(objectAddNotificationListenerStored)) {
        keyRestored = pairKeyListener.getKey();
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Value of the KeyRestored of removeNotificationListener(ObjectName name,NotificationListener listener) : "+keyRestored );
          }
        it.remove();// remove(keyRestored);
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "New Content of  hashTableNotificationListener after calling the method removeNotificationListener(ObjectName name, NotificationListener listener,NotificationFilter filter, Object handback) : "+hashTableNotificationListener);
          }

        RemoveNotificationListener1 removeNotificationListener1 = new RemoveNotificationListener1(name,
            filter, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
            logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
          }
          requestor.doRequete(removeNotificationListener1);
          poolRequestors.freeRequestor(requestor);
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
          }
          break; // The MBean must have a listener that exactly matches the
                 // given listener, filter, and handback parameters. If there is
                 // more than one such listener, only one is removed.
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

  public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException,
      ReflectionException, IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : MBeanInfo getMBeanInfo(ObjectName name)");
      }
    GetMBeanInfo getMBeanInfo = new GetMBeanInfo(name);
    MBeanInfo mbeanInfoResult = null;
    try {
      // clientJms.doRequete(getMBeanInfo);
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(getMBeanInfo);
      mbeanInfoResult = (MBeanInfo) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return mbeanInfoResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return mbeanInfoResult;
  }

  public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException,
      IOException {
    Requestor requestor = null;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Call of the method : boolean isInstanceOf(ObjectName name, String className)");
      }
    IsInstanceOf isInstanceOf = new IsInstanceOf(name, className);
    Boolean booleanResults = false;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" was taken" );
        logger.log(BasicLevel.DEBUG, "The Connection is : "+requestor.connection.getClass().getName() );
      }
      requestor.doRequete(isInstanceOf);
      booleanResults = (Boolean) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "The Requestor : "+requestor.getClass().getName()+" has been released" );
      }
      return booleanResults;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return booleanResults;
  }

}

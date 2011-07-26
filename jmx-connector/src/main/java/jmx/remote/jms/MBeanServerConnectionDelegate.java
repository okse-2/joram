/**
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

import jmx.remote.jms.structures.*;
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
  protected Connection connection;
  MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
  FileWriter f;
  static HashMap hashTableNotificationListener;
  Object key;
  static int value = 0;
  PoolRequestor poolRequestors;

  public MBeanServerConnectionDelegate(Connection connection) throws IOException {
    this.connection = connection;
    String path = new File("").getAbsolutePath();
    System.out.println("***MBC*****" + path);
    f = new FileWriter(new File(path + "\\Ordre d'Appel des methodes.txt"), true);
    hashTableNotificationListener = new HashMap();
    poolRequestors = new PoolRequestor(connection);
    try {
      int SizePoolRequestor = Integer.parseInt(System.getProperty("SizePoolRequestor"));
      System.out.println("***************** taille du pool Requestor = " + SizePoolRequestor);
      poolRequestors.initPool(SizePoolRequestor);
    } catch (Exception e) {

      ShowMessageInformations showMessageInformations = new ShowMessageInformations(null,
          "Wrong input the size of the Pool Requestor, you Should choose an integer more than  0",
          "Wrong Size Pool Requestor", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
      IOException {
    Requestor requestor = null;
    f.write("Appel a la methode createMBean(String className,ObjectName name) \n ");
    System.out.println("Appel a la methode ObjectInstance createMBean(String className,ObjectName name)");
    CreateMBean createMbean = new CreateMBean(className, name);
    ObjectInstance objectInstanceResult = null;
    try {
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(createMbean);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());

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
    f.write("Appel a la methode createMBean(String className,ObjectName name,ObjectName loaderName) \n ");
    System.out
        .println("Appel a la methode ObjectInstance createMBean(String className,ObjectName name,ObjectName loaderName)");
    CreateMBean1 createMBean1 = new CreateMBean1(className, name, loaderName);
    ObjectInstance objectInstanceResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(createMBean1);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode createMBean(String className,ObjectName name,Object[] params,String[] signature) \n ");
    System.out
        .println("Appel a la methode createMBean(String className,ObjectName name,Object[] params,String[] signature)");
    CreateMBean2 createMBean2 = new CreateMBean2(className, name, params, signature);
    ObjectInstance objectInstanceResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(createMBean2);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode  createMBean(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature) \n ");
    System.out
        .println("Appel a la methode  createMBean(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature)");
    CreateMBean3 createMBean3 = new CreateMBean3(className, name, loaderName, params, signature);
    ObjectInstance objectInstanceResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(createMBean3);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;

  }

  public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException,
      IOException {
    f.write("Appel a la methode unregisterMBean(ObjectName name) \n ");
    System.out.println("Appel a la methode void unregisterMBean(ObjectName name)");
    UnregisterMbean unregisterMbean = new UnregisterMbean(name);
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(unregisterMbean);
      requestor.doReceive();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
    f.write("Appel a la methode getObjectInstance(ObjectName name) \n ");
    System.out.println("Appel a la methode getObjectInstance(ObjectName name)");
    GetObjectInstance getObjectInstance = new GetObjectInstance(name);
    ObjectInstance objectInstanceResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getObjectInstance);
      objectInstanceResult = (ObjectInstance) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return objectInstanceResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectInstanceResult;
  }

  public Set queryMBeans(ObjectName name, QueryExp query) throws IOException {
    f.write("Appel a la methode queryMBeans(ObjectName name,QueryExp query) \n ");
    System.out.println("Appel a la methode queryMBeans(ObjectName name,QueryExp query)");
    QueryMbeans queryMbeans = new QueryMbeans(name, query);
    Set setResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(queryMbeans);
      setResult = (Set) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return setResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return setResult;
  }

  public Set queryNames(ObjectName name, QueryExp query) throws IOException {
    System.out.println("******* * * * Connexion : ");
    f.write("Appel a la methode queryNames(ObjectName name,QueryExp query) \n ");
    System.out.println("Appel a la methode queryNames() \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    QueryName queryNames = new QueryName(name, query);
    Set setResult = null;
    try {
      Requestor requestor = null;
      requestor = poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(queryNames);
      setResult = (Set) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return setResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return setResult;

  }

  public boolean isRegistered(ObjectName name) throws IOException {
    Requestor requestor = null;
    f.write("Appel a la methode isRegistered(ObjectName name)\n ");
    f.write("name = " + name);
    System.out.println("Appel a la methode isRegistered(ObjectName name) \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    IsRegistered isRegistered = new IsRegistered(name);
    Boolean booleanResult = false;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * J'ai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(isRegistered);
      booleanResult = (Boolean) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return booleanResult;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return booleanResult;
  }

  public Integer getMBeanCount() throws IOException {
    Requestor requestor = null;
    f.write("Appel a la methode getMBeanCount() \n ");
    System.out.println("Appel a la methode getMBeanCount()");
    GetMBeanCount getMBeanCount = new GetMBeanCount();
    Integer integerResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getMBeanCount);
      integerResult = (Integer) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode getAttribute(ObjectName name,String attribute) \n ");
    System.out.println("Appel a la methode getAttribute() \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    GetAttribute getAttribute = new GetAttribute(name, attribute);
    Object objectResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getAttribute);
      objectResult = requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectResult;
  }

  public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException,
      ReflectionException, IOException {
    Requestor requestor = null;
    f.write("Appel a la methode getAttributes(ObjectName name,String[] attributes) \n ");
    System.out.println("Appel a la methode getAttributes() \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    GetAttributes getAttributes = new GetAttributes(name, attributes);
    AttributeList attributeListResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getAttributes);
      attributeListResult = (AttributeList) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());

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
    f.write("Appel a la methode setAttribute(ObjectName name,Attribute attribute) \n ");
    System.out.println("Appel a la methode setAttribute(ObjectName name,Attribute attribute)");
    SetAttribute setAttribute = new SetAttribute(name, attribute);
    System.out.println("Appel a la methode setAttribute() \n ");
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(setAttribute);
      requestor.doReceive();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode setAttributes(ObjectName name,AttributeList attributes) \n ");
    System.out.println("Appel a la methode setAttributes(ObjectName name,AttributeList attributes)");
    SetAttributes setAttributes = new SetAttributes(name, attributes);
    AttributeList attributeListResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(setAttributes);
      attributeListResult = (AttributeList) requestor.doReceive().getObject();
      System.out.println("Je suis lalallalalalalalallalaa ");
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return attributeListResult;
  }

  public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
    Requestor requestor = null;
    f.write("Appel a la methode invoke(ObjectName name,String operationName,Object[] params,String[] signature) \n ");
    System.out.println("Appel a la methode invoke()");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    Invoke invoke = new Invoke(name, operationName, params, signature);
    Object objectResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(invoke);
      objectResult = requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return objectResult;

  }

  public String getDefaultDomain() throws IOException {
    Requestor requestor = null;
    f.write("Appel a la methode getDefaultDomain() \n ");
    GetDefaultDomain getDefaultDomain = new GetDefaultDomain();
    System.out.println("Appel a la methode String getDefaultDomain() \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    String stringResult = "getDefaultDomain is not done yet";
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getDefaultDomain);
      stringResult = (String) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode getDomains() \n ");
    System.out.println("Appel a la methode:  String[] getDomains()");
    GetDomains getDomains = new GetDomains();
    String[] stringResult = null;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getDomains);
      stringResult = (String[]) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) \n ");
    System.out
        .println("--> Appel a la methode addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)");
    value++;
    try {
      key = new Integer(value);
      System.out.println(key.toString());
      AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
          name, listener, filter, handback);
      hashTableNotificationListener.put(key, objectAddNotificationListenerStored);
      System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
      System.out.println("hashhhhhhhhhhhhhhhhhhh");
      AddNotificationListener addNotificationListener = new AddNotificationListener(name, filter, key);
      try {
        Requestor requestor;
        requestor = (Requestor) poolRequestors.allocRequestor();
        System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
        System.out.println("****** * * * Connexion : " + requestor.connection.toString());
        requestor.subscribeToNotifications(addNotificationListener);
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
    f.write("Appel a la methode addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) \n ");
    // mbs.addNotificationListener(name, listener, filter, handback);
    System.out
        .println("Appel a la methode addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback)");
    value++;
    try {
      key = new Integer(value);
      System.out.println(key.toString());
      AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(
          name, listener, filter, handback);
      hashTableNotificationListener.put(key, objectAddNotificationListenerStored);
      System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
      System.out.println("hashhhhhhhhhhhhhhhhhhh");
      AddNotificationListener addNotificationListener = new AddNotificationListener(name, filter, key);
      try {
        Requestor requestor;
        requestor = (Requestor) poolRequestors.allocRequestor();
        System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
        System.out.println("****** * * * Connexion : " + requestor.connection.toString());
        requestor.subscribeToNotifications(addNotificationListener);
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
    f.write("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener) \n ");
    System.out.println("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener)");
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
        System.out.println("la clé a ete touvé !! key = " + keyRestored);
        System.out
            .println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "
                + keyRestored);
        it.remove();// remove(keyRestored);
        System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
        System.out.println("***----> l'objet objectAddNotificationListenerStored"
            + hashTableNotificationListener.get(keyRestored)
            + " a ete supprimé de la hashTableNotificationListener ");
        RemoveNotificationListener2 objectRemoveNotificationListener2 = new RemoveNotificationListener2(name,
            listener, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
          System.out.println("****** * * * Connexion : " + requestor.connection.toString());
          requestor.doRequete(objectRemoveNotificationListener2);
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
    f.write("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) \n ");
    System.out
        .println("**************************Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback)");
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
        System.out.println("la clé a ete touvé !! key = " + keyRestored);
        System.out
            .println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "
                + keyRestored);
        it.remove();// remove(keyRestored);
        System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
        System.out.println("***----> l'objet objectAddNotificationListenerStored"
            + hashTableNotificationListener.get(keyRestored)
            + " a ete supprimé de la hashTableNotificationListener ");
        RemoveNotificationListener3 objectRemoveNotificationListener3 = new RemoveNotificationListener3(name,
            listener, filter, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
          System.out.println("****** * * * Connexion : " + requestor.connection.toString());
          requestor.doRequete(objectRemoveNotificationListener3);
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
    f.write("Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener) \n ");
    System.out
        .println("**************************Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener)");
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
        System.out.println("la clé a ete touvé !! key = " + keyRestored);
        System.out
            .println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "
                + keyRestored);
        it.remove();// remove(keyRestored);
        System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
        System.out.println("***----> l'objet objectAddNotificationListenerStored"
            + hashTableNotificationListener.get(keyRestored)
            + " a ete supprimé de la hashTableNotificationListener ");

        RemoveNotificationListener objectRemoveNotificationListener = new RemoveNotificationListener(name,
            keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
          System.out.println("****** * * * Connexion : " + requestor.connection.toString());
          requestor.doRequete(objectRemoveNotificationListener);
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
    f.write("Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) \n ");
    System.out
        .println("********************Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)");
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
        System.out.println("la clé a ete touvé !! key = " + keyRestored);
        System.out
            .println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "
                + keyRestored);
        it.remove();// remove(keyRestored);
        System.out.println("Contenu de la hashTableNotificationListener : " + hashTableNotificationListener);
        System.out.println("***----> l'objet objectAddNotificationListenerStored"
            + hashTableNotificationListener.get(keyRestored)
            + " a ete supprimé de la hashTableNotificationListener ");
        System.out.println("------------->    keyRestored : " + keyRestored);
        RemoveNotificationListener1 removeNotificationListener1 = new RemoveNotificationListener1(name,
            filter, keyRestored);
        try {
          Requestor requestor;
          requestor = (Requestor) poolRequestors.allocRequestor();
          System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
          System.out.println("****** * * * Connexion : " + requestor.connection.toString());
          requestor.doRequete(removeNotificationListener1);
          System.out.println("Requete RemoveNotificationListener envoyéeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
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
    f.write("Appel a la methode getMBeanInfo(ObjectName name) \n ");
    System.out.println("Appel a la methode getMBeanInfo() \n ");
    System.out.println("thread appelant : " + Thread.currentThread().getName());
    GetMBeanInfo getMBeanInfo = new GetMBeanInfo(name);
    System.out.println("paramètre de getMBeanInfo name = " + name);
    MBeanInfo mbeanInfoResult = null;
    try {
      // clientJms.doRequete(getMBeanInfo);
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      requestor.doRequete(getMBeanInfo);
      mbeanInfoResult = (MBeanInfo) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
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
    f.write("Appel a la methode isInstanceOf(ObjectName name,String className) \n ");
    System.out.println("Appel a la methode isInstanceOf(ObjectName name,String className)");
    IsInstanceOf isInstanceOf = new IsInstanceOf(name, className);
    Boolean booleanResults = false;
    try {
      requestor = (Requestor) poolRequestors.allocRequestor();
      System.out.println("****** * * * Connexion : " + requestor.connection.toString());
      System.out.println("********* * * * Jai pris le requestor numero : " + requestor.toString());
      requestor.doRequete(isInstanceOf);
      booleanResults = (Boolean) requestor.doReceive().getObject();
      poolRequestors.freeRequestor(requestor);
      System.out.println("********* * * * J'ai libere le requestor numero : " + requestor.toString());
      return booleanResults;
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return booleanResults;
  }

}

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

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.jms.*;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jmx.remote.jms.structures.*;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;

/**
 * 
 * 
 * <b>JMSConnector which represents the comportement of the server connector</b>
 * <p>
 * <ul>
 * <li>Destination : The JMS connector builds his own destination with Queue
 * type from the Session object.</li>
 * </ul>
 * <ul>
 * <li>Message filtering : JMS Connector does a filtering of messages it
 * receives to be sure that the message in question him is intended, in this
 * case, it builds the response and retrieves the customer's destination from
 * message received with getJMSReplyTo, and then sends the response to this
 * destination.</li>
 * </ul>
 * <ul>
 * <li>Receiving Messages : <b><i>JMSConnecteur</b></i> JMSConnecteur implements
 * the MessageListener interface, so a listener will be implicitly associated to
 * consummer of the server connector, so when the client sends a message to the
 * destination of the connector in question it will be notified directly and the
 * message received will be processed in the method <b><i>onMessage</b></i>
 * which takes in parameter the received message.</li>
 * </ul>
 * </p>
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * @version 1.3
 */

public class JMSConnector implements MessageListener {
  private static ConnectionFactory connectionFactory;
  private static Topic topic;
  private static Queue QReponse, QRequete, queueNotification;
  private MBeanServer mbs = null;
  static Object reponse = null;
  MessageProducer producer;
  Session session2, session;
  Destination replyTo;
  Queue qToto;
  Object handback;
  static HashMap myNotificationListener;
  Object objetRecu;

  public JMSConnector() throws NamingException, JMSException, AdminException, IOException {

    // SimpleAgentMbs agent = new SimpleAgentMbs();

    // Get the platform MBeanServer
    mbs = ManagementFactory.getPlatformMBeanServer(); // On recupère le mbean
                                                      // Server qui est lancé
                                                      // par JMSConnecteur
    myNotificationListener = new HashMap();
    // Récupération du contexte JNDI
    Context jndiContext = new InitialContext();
    // Recherche des objets administrés
    ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
    /**
     * On crée la file de destination du JMSConnecteur et puis on l'enregistre
     * dans la jndi
     */
    // Queue qtoto = (Queue)jndiContext.lookup("Qtoto");
    // Queue QRequete = (Queue)jndiContext.lookup("QRequete");
    // Queue QReponse = (Queue)jndiContext.lookup("QReponse");
    // Topic topic = (Topic)jndiContext.lookup("Topic");
    jndiContext.close();
    // Création des artéfacts nécessaires pour se connecter à la file et au
    // sujet
    Connection connection = connectionFactory.createConnection();
    /* Session */session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // la
                                                                                      // session
                                                                                      // accuse
    Queue qtoto = (Queue) session.createQueue("Qtoto");
    // queueNotification = (Queue) session.createQueue("QNotification");
    session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    String selecteur = "Connecteur = 'toto'";
    MessageConsumer consumer = session.createConsumer(qtoto, selecteur);
    producer = session.createProducer(null);
    consumer.setMessageListener(this);
    connection.start();
  }

  public void onMessage(Message message) {
    try {
      replyTo = message.getJMSReplyTo();
      if (replyTo != null) {
        System.out.println("Mise a jour de la destination du JMSConnecteur");
        System.out.println("-------->>>>>>>>>>>" + replyTo.toString());
      }
      System.out.println("Message received: ");
      if (message instanceof ObjectMessage) {

        objetRecu = ((ObjectMessage) message).getObject();
        if (objetRecu instanceof GetAttribute) {
          GetAttribute o = (GetAttribute) objetRecu;
          reponse = mbs.getAttribute(o.name, o.attributes);
        } else if (objetRecu instanceof SetAttribute) {
          SetAttribute setAtt = (SetAttribute) objetRecu;
          mbs.setAttribute(setAtt.name, setAtt.attribute);
          reponse = new String("Le Set a ete fait");
        } else if (objetRecu instanceof Invoke) {
          Invoke ObjetInvoke = (Invoke) objetRecu;
          reponse = mbs.invoke(ObjetInvoke.name, ObjetInvoke.operationName, ObjetInvoke.parametres,
              ObjetInvoke.signature);
        } else if (objetRecu instanceof GetMBeanInfo) {
          GetMBeanInfo mbeanInfo = (GetMBeanInfo) objetRecu;
          reponse = mbs.getMBeanInfo(mbeanInfo.name);
        } else if (objetRecu instanceof IsRegistered) {
          IsRegistered isRegistered = (IsRegistered) objetRecu;
          reponse = mbs.isRegistered(isRegistered.name);
        } else if (objetRecu instanceof IsInstanceOf) {
          IsInstanceOf isInstanceOf = (IsInstanceOf) objetRecu;
          reponse = mbs.isInstanceOf(isInstanceOf.name, isInstanceOf.className);
        }

        else if (objetRecu instanceof QueryName) {
          QueryName objectQueryName = (QueryName) objetRecu;
          reponse = mbs.queryNames(objectQueryName.name, objectQueryName.query);
        } else if (objetRecu instanceof GetAttributes) {
          GetAttributes objectGetAttributes = (GetAttributes) objetRecu;
          reponse = mbs.getAttributes(objectGetAttributes.name, objectGetAttributes.attributes);
        } else if (objetRecu instanceof GetDefaultDomain) {
          GetDefaultDomain objectGetDefaultDomain = (GetDefaultDomain) objetRecu;
          reponse = mbs.getDefaultDomain();
        } else if (objetRecu instanceof CreateMBean) {
          CreateMBean objectCreateMbean = (CreateMBean) objetRecu;
          reponse = mbs.createMBean(objectCreateMbean.className, objectCreateMbean.name);
        } else if (objetRecu instanceof CreateMBean1) {
          CreateMBean1 objectCreateMBean1 = (CreateMBean1) objetRecu;
          reponse = mbs.createMBean(objectCreateMBean1.className, objectCreateMBean1.name,
              objectCreateMBean1.loaderName);
        } else if (objetRecu instanceof CreateMBean2) {
          CreateMBean2 objectCreateMBean2 = (CreateMBean2) objetRecu;
          reponse = mbs.createMBean(objectCreateMBean2.className, objectCreateMBean2.name,
              objectCreateMBean2.parametres, objectCreateMBean2.signature);
        } else if (objetRecu instanceof CreateMBean3) {
          CreateMBean3 objectCreateMBean3 = (CreateMBean3) objetRecu;
          reponse = mbs.createMBean(objectCreateMBean3.className, objectCreateMBean3.name,
              objectCreateMBean3.loaderName, objectCreateMBean3.parametres, objectCreateMBean3.signature);
        } else if (objetRecu instanceof UnregisterMbean) {
          UnregisterMbean objectUnregisterMbean = (UnregisterMbean) objetRecu;
          mbs.unregisterMBean(objectUnregisterMbean.name);
          reponse = "Unregistering of the MBean : " + objectUnregisterMbean.name + "is done";
        } else if (objetRecu instanceof GetObjectInstance) {
          GetObjectInstance objectGetObjectInstance = (GetObjectInstance) objetRecu;
          reponse = mbs.getObjectInstance(objectGetObjectInstance.name);
        } else if (objetRecu instanceof QueryMbeans) {
          QueryMbeans objectQueryMbeans = (QueryMbeans) objetRecu;
          reponse = mbs.queryMBeans(objectQueryMbeans.name, objectQueryMbeans.query);
        } else if (objetRecu instanceof GetMBeanCount) {
          GetMBeanCount objectGetMBeanCount = (GetMBeanCount) objetRecu;
          reponse = mbs.getMBeanCount();
        } else if (objetRecu instanceof SetAttributes) {
          SetAttributes objectSetAttributes = (SetAttributes) objetRecu;
          reponse = mbs.setAttributes(objectSetAttributes.name, objectSetAttributes.attributes);
        } else if (objetRecu instanceof GetDomains) {
          GetDomains objectGetDomains = (GetDomains) objetRecu;
          reponse = mbs.getDomains();
        } else if (objetRecu instanceof AddNotificationListener) {
          AddNotificationListener objectAddNotificationListener = (AddNotificationListener) objetRecu;
          ObjectNotificationListener myObjectNotificationListener = new ObjectNotificationListener(session2,
              producer, replyTo, objectAddNotificationListener.filter, objectAddNotificationListener.key);
          System.out.println("***************valeur de la key dans AddNotificationListener : "
              + objectAddNotificationListener.key);
          myNotificationListener.put(objectAddNotificationListener.key, myObjectNotificationListener);

          System.out.println("****avant l'appel de mbs.addNotificationListener");
          System.out.println("key = " + objectAddNotificationListener.key);
          mbs.addNotificationListener(objectAddNotificationListener.name, myObjectNotificationListener,
              objectAddNotificationListener.filter, objectAddNotificationListener.key);
          System.out.println("****après l'appel de mbs.addNotificationListener");
          reponse = new String("Le Listener a ete enregistré!");

        } else if (objetRecu instanceof RemoveNotificationListener1) {
          RemoveNotificationListener1 objectRemoveNotificationListener1 = (RemoveNotificationListener1) objetRecu;
          // ObjectNotificationListenerToRemoved
          // objectNotificationListenerToRemoved = new
          // ObjectNotificationListenerToRemoved();

          System.out.println("le handback du RemoveNotificationListener : "
              + objectRemoveNotificationListener1.handback);
          ObjectNotificationListener objectNotificationListenerRecovered = (ObjectNotificationListener) myNotificationListener
              .get(objectRemoveNotificationListener1.handback);
          System.out.println("objectNotificationListenerRecovered : "
              + objectNotificationListenerRecovered.toString());
          mbs.removeNotificationListener(objectRemoveNotificationListener1.name,
              objectNotificationListenerRecovered, objectNotificationListenerRecovered.filter,
              objectNotificationListenerRecovered.handback);
          System.out.println("je suis dans le Remoooooooooooooooooveeeeeeeeeeeeeeeeeeeeeeeeeeeee");
          reponse = new String("Remove de la notificationListener4 avec le handback :");// +objectRemoveNotificationListener4.handback.toString());
        } else if (objetRecu instanceof RemoveNotificationListener) {
          RemoveNotificationListener objectRemoveNotificationListener = (RemoveNotificationListener) objetRecu;
          // ObjectNotificationListenerToRemoved
          // objectNotificationListenerToRemoved = new
          // ObjectNotificationListenerToRemoved();

          System.out.println("le handback du RemoveNotificationListener : "
              + objectRemoveNotificationListener.handback);
          ObjectNotificationListener objectNotificationListenerRecovered = (ObjectNotificationListener) myNotificationListener
              .get(objectRemoveNotificationListener.handback);
          System.out.println("objectNotificationListenerRecovered : "
              + objectNotificationListenerRecovered.toString());
          mbs.removeNotificationListener(objectRemoveNotificationListener.name,
              objectNotificationListenerRecovered);
          reponse = new String("Remove de la notificationListener3 avec le handback :");// +objectRemoveNotificationListener4.handback.toString());
        } else if (objetRecu instanceof RemoveNotificationListener2) {
          RemoveNotificationListener2 objectRemoveNotificationListener2 = (RemoveNotificationListener2) objetRecu;
          // ObjectNotificationListenerToRemoved
          // objectNotificationListenerToRemoved = new
          // ObjectNotificationListenerToRemoved();

          System.out.println("le handback du RemoveNotificationListener : "
              + objectRemoveNotificationListener2.handback);
          ObjectNotificationListener objectNotificationListenerRecovered = (ObjectNotificationListener) myNotificationListener
              .get(objectRemoveNotificationListener2.handback);
          System.out.println("objectNotificationListenerRecovered : "
              + objectNotificationListenerRecovered.toString());
          mbs.removeNotificationListener(objectRemoveNotificationListener2.name,
              objectNotificationListenerRecovered);
          reponse = new String("Remove de la notificationListener3 avec le handback :");// +objectRemoveNotificationListener4.handback.toString());
        } else if (objetRecu instanceof RemoveNotificationListener3) {
          RemoveNotificationListener3 objectRemoveNotificationListener3 = (RemoveNotificationListener3) objetRecu;
          // ObjectNotificationListenerToRemoved
          // objectNotificationListenerToRemoved = new
          // ObjectNotificationListenerToRemoved();

          System.out.println("le handback du RemoveNotificationListener : "
              + objectRemoveNotificationListener3.handback);
          ObjectNotificationListener objectNotificationListenerRecovered = (ObjectNotificationListener) myNotificationListener
              .get(objectRemoveNotificationListener3.handback);
          System.out.println("objectNotificationListenerRecovered : "
              + objectNotificationListenerRecovered.toString());
          mbs.removeNotificationListener(objectRemoveNotificationListener3.name,
              objectNotificationListenerRecovered, objectRemoveNotificationListener3.filter,
              objectRemoveNotificationListener3.handback);
          reponse = new String("Remove de la notificationListener3 avec le handback :");// +objectRemoveNotificationListener4.handback.toString());
        }

      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AttributeNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstanceNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MBeanException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ReflectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NullPointerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvalidAttributeValueException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IntrospectionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstanceAlreadyExistsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NotCompliantMBeanException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ListenerNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ObjectMessage messageReponse = null;
    if (!(objetRecu instanceof AddNotificationListener)) {
      try {
        messageReponse = session.createObjectMessage();
      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      try {
        messageReponse.setObject((Serializable) reponse);
      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      try {
        producer.send(replyTo, messageReponse);
      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
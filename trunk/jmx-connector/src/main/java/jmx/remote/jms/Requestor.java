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

import java.awt.List;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.jms.*;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.NotificationListener;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.admin.AdminException;

/**
 * A <b>Requestor </b> a requestor allows to do one or many JMS requetes to the
 * server connector , through the server JORAM.
 * 
 * @author Djamel-Eddine Boumchedda
 * @version 1.3
 * 
 */

public class Requestor implements MessageListener {
  private static Topic topic;
  private static Queue QReponse, QRequete;
  static String[] signatureMethode;
  static Object[] paramMethode;
  static String operationName, objectName;
  Session session;
  Session session2;
  ObjectMessage message, messageRecu;
  ObjectMessage messageNotificationRecu = null;
  MessageProducer producer, producerNotification;
  MessageConsumer consumer, consumerNotification;
  Destination dest;
  Queue queue;
  TemporaryQueue queueTemporaire;
  AttributeList attributesList;
  Queue queueNotification;
  Boolean notificationRecu = false;
  Connection connection;

  public Requestor(Connection connection) {
    this.connection = connection;
    try {
      createEntitiesOfAdministration(connection);
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * <ul>
   * the <b>createEntitiesOfAdministration(Connection connection)</b> is called
   * during initialization of a requestor, she allows to create the logical
   * entities of directors from a common connection to all the requestor
   * creates.
   * </ul>
   * <ul>
   * The logical entities are :
   * </ul>
   * <ul>
   * <li>Two Sessions.
   * </ul>
   * </li>
   * <ul>
   * <li>Two Destination-type "Queue" (a for request-response and the other for
   * notifications).
   * </ul>
   * </li>
   * <ul>
   * <li>A producer, consumer.
   * </ul>
   * </li>
   * 
   * @param Connection
   *          <ul>
   *          <li>The connection which is common for all requestor.
   *          </ul>
   *          </li>
   * @throws JMSException
   */

  public void createEntitiesOfAdministration(Connection connection) throws JMSException {
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    // Le client crée sa destination temporaire
    queueTemporaire = (TemporaryQueue) session.createTemporaryQueue();
    Queue qtoto = (Queue) session.createQueue("Qtoto");
    queueNotification = (TemporaryQueue) session.createTemporaryQueue();
    consumer = session.createConsumer(queueTemporaire);
    producer = session.createProducer(qtoto);
    consumerNotification = session2.createConsumer(queueNotification);
    producerNotification = session.createProducer(queueNotification);
    consumerNotification.setMessageListener(this);

  }

  /**
   * <b>onMessage</b> in this method is implemented the listener of the
   * QNotification destination for receiving the notifications issued by the
   * registered MBean in the MBeanServer.
   * 
   * @param Message
   *          <ul>
   *          the message parameter to the method represents the message
   *          received .
   *          </ul>
   * @throws JMSException
   */

  public void onMessage(Message message) {
    messageNotificationRecu = (ObjectMessage) message;
    // HashMap hashTableNotificationContext =
    // MBeanServerConnectionDelegate.hashTableNotificationContext;
    HashMap hashTableNotificationListener = MBeanServerConnectionDelegate.hashTableNotificationListener;
    // HashMap myNotificationListener = JMSConnector.myNotificationListener;
    NotificationAndKey notificationAndKey = null;
    try {
      notificationAndKey = (NotificationAndKey) ((ObjectMessage) message).getObject();
      System.out.println("NotificationAndKey = " + notificationAndKey.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("--> L'objet NotificationAndKey contenant la notification et le handback a ete recu!");
    AddNotificationListenerStored objectAddNotificationListenerStored = (AddNotificationListenerStored) hashTableNotificationListener
        .get(notificationAndKey.handback);
    System.out.println("--> L'objet objectAddNotificationListenerStored a été recupere");
    // System.out.println("--> On recupere le handback");
    // Object handBackRecovered =
    // hashTableNotificationContext.get(notificationAndKey.handback);
    // System.out.println("--> On recupere le listener");
    // NotificationListener notificationListenerRecovered =
    // (NotificationListener)
    // hashTableNotificationListener.get(notificationAndKey.handback);
    // notificationListenerRecovered.handleNotification(notificationAndKey.notification,
    // handBackRecovered);
    ((NotificationListener) objectAddNotificationListenerStored.listener).handleNotification(
        notificationAndKey.notification, objectAddNotificationListenerStored.handback);
    System.out.println("La Notification a été faite");
    System.out.println("NotiffffffffffffEndddddddddddddddddddddd!!!");
    notificationRecu = true;

  }

  /**
   * <b>doRequete</b> is the method that is called by a requestor for send a
   * message of type ObjectMesage.
   * 
   * @param Object
   *          <ul>
   *          The object that is passed as a parameter is the object which will
   *          be send to the destination of server connector .
   *          </ul>
   * @throws JMSException
   */

  public void doRequete(Object o) throws JMSException {

    /*** Envoi d'un message Object dans la QueueRequete ***/
    try {
      ObjectMessage message = session.createObjectMessage();
      message.setObject((Serializable) o);
      message = initializationPropertiesOfMessageSending(message, "Connecteur", "toto", queueTemporaire);
      // message.setStringProperty("Connecteur", "toto");
      // message.setJMSReplyTo(queueTemporaire);
      System.out.println("QueueTemporaire : " + queueTemporaire.toString());
      producer.send(message);
      System.out.println("Requete envoye");

    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }

  }

  /**
   * <b>initializationPropertiesOfMessageSending</b> this method is invoked by a
   * requester before sending a message to initialize the properties of the
   * message to send.
   * 
   * @param ObjectMessage
   *          <ul>
   *          represents the message that will be modified by changing his
   *          properties.
   *          </ul>
   * @param String
   *          <ul>
   *          represents the name of the key of the property of the message.
   *          </ul>
   * @param String
   *          <ul>
   *          represents the value of the key in the properties of the message
   *          which is the name of the server connector.
   *          </ul>
   * @param Destination
   *          <ul>
   *          represents the destination of the sender (requestor).
   *          </ul>
   * @return ObjectMessage
   * @throws JMSException
   */

  public ObjectMessage initializationPropertiesOfMessageSending(ObjectMessage message, String key,
      String connectorName, Destination ReplyTo) {
    try {
      message.setStringProperty(key, connectorName);
      message.setJMSReplyTo(ReplyTo);
      System.out.println("QueueTemporaire : " + ReplyTo.toString());
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return message;

  }

  /**
   * <b>doReceive</b> is the method that is called by a requestor to retrieve
   * the response of the request sent by the doRequete method.
   * 
   * @param void
   * @return ObjectMessage
   *         <ul>
   *         <li>This method returns the received message (Object Message).
   *         </ul>
   *         </li>
   * @throws JMSException
   */

  public ObjectMessage doReceive() {
    ObjectMessage messageRecu = null;
    try {
      messageRecu = (ObjectMessage) consumer.receive();
      if (messageRecu.getObject() != null && !(messageRecu.getObject() instanceof MBeanInfo)
          && !(messageRecu.getObject() instanceof NotificationAndKey)) {
        System.out.println("MessageRecu");
        System.out.println(messageRecu.getObject().toString());
      }
      if (messageRecu.getObject() instanceof AttributeList) {
        attributesList = (AttributeList) messageRecu.getObject();
        System.out.println(attributesList.toString());
      }
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("Thread retour : " + Thread.currentThread().getName().toString());

    return messageRecu;

  }

  /**
   * <b>subscribeToNotifications</b> is the method that is called by a requestor
   * to subscribe to notifications obligations issued by the registered MBean in
   * the MBeanServer
   * 
   * @param Object
   *          <ul>
   *          The object that is passed as a parameter is the object which will
   *          be send to the destination of server connector .
   *          </ul>
   * @throws JMSException
   */

  public void subscribeToNotifications(Object objectAddNotificationListener) throws JMSException {
    try {

      ObjectMessage message = session.createObjectMessage();
      message.setObject((Serializable) objectAddNotificationListener);
      initializationPropertiesOfMessageSending(message, "Connecteur", "toto", queueNotification);
      System.out.println("Queue Notification : " + queueNotification.toString());
      System.out
          .println("***********************************************************Requete de notification envoye");
      producer.send(message);

    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}

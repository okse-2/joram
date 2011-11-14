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
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * The AdminRequestor class allows making administration service requests.
 * The AdminRequestor constructor is given a Connection, it creates a session and a
 * TemporaryTopic for the responses. It provides a request method that sends the request
 * message to the administration topic of the connected server and waits for its reply.
 * 
 * @see AdminWrapper
 */
public final class AdminRequestor {
  // Session used to send requests and receive replies.
  private javax.jms.Session session;
  // The administration topic of the connected server.
  private javax.jms.Topic topic;
  // The temporary topic needed to receive replies.
  private TemporaryTopic tmpTopic;
  // The message producer to send requests.
  private MessageProducer producer;
  // The message consumer to receive replies.
  private MessageConsumer consumer;
  
  /**
   * Property allowing to set the timeout before aborting a request.
   *
   * @see requestTimeout
   */
  public static final String REQUEST_TIMEOUT_PROP = "org.objectweb.joram.client.jms.admin.requestTimeout";

  /**
   * Defines the default value for timeout before aborting a request.
   * <p>
   * Default value is 60.000 ms.
   *
   * @see requestTimeout
   */
  public final static long DEFAULT_REQUEST_TIMEOUT = 60000;

  /**
   * Defines the maximum time in milliseconds before aborting a request.
   * <p>
   * Default value is 60.000 ms.
   * <p>
   *  This value can be adjusted by setting
   * <code>org.objectweb.joram.client.jms.admin.requestTimeout</code> property.
   * 
   * @see DEFAULT_REQUEST_TIMEOUT
   * @see REQUEST_TIMEOUT_PROP
   * @since 5.2.2
   */
  private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;
  
  /**
   * Set the maximum time in ms before aborting arequest.
   * 
   * @param requestTimeout the maximum time in ms before aborting request.
   * 
   * @since 5.2.2
   */
  public void setRequestTimeout(long requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  /**
   * Returns the maximum time in ms before aborting a request.
   * 
   * @return the maximum time in ms before aborting request.
   * 
   * @since 5.2.2
   */
  public long getRequestTimeout() {
    return requestTimeout;
  }

  public static Logger logger = Debug.getLogger(AdminRequestor.class.getName());
  
  /**
   * Constructor for the AdminRequestor class.
   * This implementation assumes an unified connection and uses a non-transacted session
   * with an AUTO_ACKNOWLEDGE delivery mode. The Connection needs to be started.
   * 
   * @param cnx           A Joram connection.
   * @throws JMSException if Joram fails to create the AdminRequestor due to some internal error.
   */
  public AdminRequestor(javax.jms.Connection cnx) throws JMSException {
    requestTimeout = Long.getLong(REQUEST_TIMEOUT_PROP, requestTimeout).longValue();
    
    try {
      // Creates the needed session.
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // Gets the administration topic of the connected server.
      topic = session.createTopic("#AdminTopic");
      // Creates the message producer needed to send requests.
      producer = session.createProducer(topic);
      // Creates a temporary topic and a consumer needed to receive replies.
      tmpTopic = session.createTemporaryTopic();
      consumer = session.createConsumer(tmpTopic);
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR,
                 "AdminRequestor.<init> Cannot open session.", exc);
      
      if (session != null) session.close();
      throw exc;
    }
  }

  /**
   * Sends an administration request and waits for a reply.
   * A temporary topic is used for the <code>JMSReplyTo</code> destination and the request
   * <code>JMSMessageID</code> as <code>JMSCorrelationID</code> to select the corresponding
   * reply; any other replies are discarded.
   * 
   * @param request the administration request to send
   * @return  the reply message
   * 
   * @throws JMSException if Joram fails to complete the request due to some internal error.
   */
  public synchronized AdminReply request(AdminRequest request) throws AdminException, ConnectException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminRequestor.request(" + request + ')');

    AdminMessage requestMsg = new AdminMessage();
    javax.jms.Message replyMsg = null;
    AdminReply reply = null;
    
    try {
      // Sends the an AdminMessage containing the request to the administration topic.
      requestMsg.setAdminMessage(request);
      requestMsg.setJMSReplyTo(tmpTopic);
      producer.send(requestMsg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, requestTimeout);

      // Selects the message containing the reply.
      String correlationId = requestMsg.getJMSMessageID();
      while (true) {
        replyMsg = consumer.receive(requestTimeout);
        if (replyMsg == null)
          throw new JMSException("Interrupted request");

        if (correlationId.equals(replyMsg.getJMSCorrelationID())) break;

        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "AdminRequestor.request() bad correlation identifier.");
      }
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "AdminRequestor.request() connection failed.", exc);
      throw new ConnectException("Connection failed: " + exc.getMessage());
    }
    
    try {
      reply = (AdminReply) ((AdminMessage) replyMsg).getAdminMessage();
    } catch (ClassCastException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "AdminRequestor.request() invalid server reply.", exc);
      throw new AdminException("Invalid server reply: " + exc.getMessage());
    } catch (MessageFormatException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "AdminRequestor.request() invalid server reply.", exc);
      throw new AdminException("Invalid server reply: " + exc.getMessage());
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminRequestor.request(" + request + ") -> " + reply);

    if (reply != null) throwException(reply);
    return reply;
  }
  
  /**
   * Throws an exception corresponding to the error code of the reply if needed. 
   * 
   * @param reply The reply to verify.
   * @exception AdminException The exception corresponding to the error code in the reply.
   */
  private final void throwException(AdminReply reply) throws AdminException {
    if (! reply.succeeded()) {
      switch (reply.getErrorCode()) {
      case AdminReply.NAME_ALREADY_USED:
        throw new NameAlreadyUsedException(reply.getInfo());
      case AdminReply.START_FAILURE:
        throw new StartFailureException(reply.getInfo());
      case AdminReply.SERVER_ID_ALREADY_USED:
        throw new ServerIdAlreadyUsedException(reply.getInfo());
      case AdminReply.UNKNOWN_SERVER:
        throw new UnknownServerException(reply.getInfo());
      default:
        throw new AdminException(reply.getInfo());
      }
    }
  }

  /**
   * Aborts the running request.
   * 
   * @throws ConnectConnection A problem occurs with the connection.
   */
  public void abort() throws ConnectException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminRequestor.abort()");

    try {
      consumer.close();
      consumer = session.createConsumer(tmpTopic);
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "AdminRequestor.abort() connection failed.", exc);
      throw new ConnectException("Connection failed: " + exc.getMessage());
    }
  }

  /**
   * Closes the AdminRequestor and its session.
   * All internals allocated resources are closed and freed. Note that this method does
   * not close the Connection object passed to the AdminRequestor constructor.
   */
  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AdminRequestor.close()");

    try {
      consumer.close();
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "AdminRequestor.close()", exc);
    }
    try {
      producer.close();
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "AdminRequestor.close()", exc);
    }
    try {
      tmpTopic.delete();
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "AdminRequestor.close()", exc);
    }
    try {
      session.close();
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "AdminRequestor.close()", exc);
    }
  }
}

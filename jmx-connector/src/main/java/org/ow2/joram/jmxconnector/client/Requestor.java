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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.ow2.joram.jmxconnector.client;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <b>Requestor </b> a requestor allows to do one or many JMS requetes to the
 * server connector , through the server JORAM.
 */
public class Requestor {
  private static final Logger logger = Debug.getLogger(Requestor.class.getName());
  
  Connection cnx;
  Session session;
  MessageProducer prod;
  MessageConsumer cons;
  Queue requestQ, replyQ;

  /**
   * Initializes the requestor creating a session, a messages producer for
   * requests and a messages consumer for replies.
   * 
   * @param cnx   the common connection shared by all requestors.
   * @param qname the name of the JMX connector server queue.
   * 
   * @throws JMSException
   */
  public Requestor(Connection cnx, String qname) throws JMSException {
    this.cnx = cnx;
    
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    requestQ = session.createQueue(qname);
    prod = session.createProducer(null);
    replyQ = session.createTemporaryQueue();
    cons = session.createConsumer(replyQ);
  }
  
  /**
   * Makes a synchronous request to JMX connector server.
   * 
   * @param Object  The object that is passed as a parameter is the object which will
   *                be send to the destination of server connector .
   * 
   * @throws JMSException
   */
  Object request(Serializable request) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request(" + request + ')');
    
    Object reply = null;
    try {
      ObjectMessage msg = session.createObjectMessage();
      msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      msg.setObject(request);
      
      msg = (ObjectMessage) request(requestQ, msg);
      reply = msg.getObject();
    } catch (JMSException exc) {
      logger.log(BasicLevel.DEBUG, "Requestor.request: failure.", exc);
      throw exc;
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request: " + reply + ')');
    
    return reply;
  }

  Message request(Destination dest, Message msg) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request()");

    msg.setJMSReplyTo(replyQ);
    prod.send(dest, msg);
    String msgid = msg.getJMSMessageID();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request: " + msgid + " sent.");

    do {
      msg = cons.receive();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Requestor.request: receives " + msg);          
    } while ((msg != null) && !msgid.equals(msg.getJMSCorrelationID()));

    return msg;
  }

  Message request(Destination dest, Message request, long timeout) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request(" + timeout +')');
    
    if (dest == null)
        throw new IllegalStateException("Cannot request \"null\" destination");
    
    if (timeout < 0)
      throw new IllegalStateException("Invalid timeout:" + timeout);

    Message reply;
    request.setJMSReplyTo(replyQ);
    prod.send(dest, request);
    String msgid = request.getJMSMessageID();
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request: " + msgid + " sent.");
    
    if (timeout == 0) {
      // There is no reply.
      return null;
    }

    long end = System.currentTimeMillis() + timeout;
    do {
      reply = null;
      reply = cons.receive(timeout);
      if (reply == null)
        break;
      if (msgid.equals(reply.getJMSCorrelationID()))
        break;
      timeout = end - System.currentTimeMillis();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Requestor.request: receives unexpected " + reply);          
    } while (timeout >= 0);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Requestor.request: receives " + reply);          

    return reply;
  }
}

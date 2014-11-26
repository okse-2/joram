/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package joram.ctrlgreen;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ActionRequestor {
  Destination queue = null;
  Session session = null;
  MessageConsumer cons = null;
  MessageProducer prod = null;

  /**
   * Initialize a Requestor.
   * @throws Exception
   */
  ActionRequestor(Connection cnx) throws JMSException {
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    queue = session.createTemporaryQueue();
    prod = session.createProducer(null);
    cons = session.createConsumer(queue);
  }

  /**
   * Execute a synchronous request.
   * 
   * @param dest    The destination to send requests.
   * @param callee  The name of called component.
   * @param action  the action to execute.
   * @return  The result of action.
   * @throws JMSException
   */
  synchronized ActionReturn request(Destination dest, String callee, Action action) throws JMSException {
    if (dest == null)
      throw new IllegalStateException("Cannot request \"null\" destination");

    Message request = session.createMessage();
    request.setJMSReplyTo(queue);
    request.setStringProperty("callee", callee);
    request.setIntProperty("type", action.getType());
    request.setStringProperty("parameters", action.getParameters());
    prod.send(dest, request);
    String msgid = request.getJMSMessageID();
    
    Message reply = null;
    do {
      reply = cons.receive();
    } while ((reply != null) && !msgid.equals(reply.getJMSCorrelationID()));
    
    return new ActionReturn(reply.getIntProperty("replyCode"),
                            reply.getStringProperty("replyMsg"));
  }

  /**
   * Execute a request with timeout, if timeout is 0 the request is asynchronous.
   * 
   * @param dest    The destination to send requests.
   * @param callee  The name of called component.
   * @param action  the action to execute.
   * @param timeout The maximum time to wait the reply.
   * @return  The result of action.
   * @throws JMSException
   */
  synchronized ActionReturn request(Destination dest, String callee, Action action, long timeout) throws JMSException {
    if (dest == null)
        throw new IllegalStateException("Cannot request \"null\" destination");
    
    if (timeout < 0)
      throw new IllegalStateException("Invalid timeout:" + timeout);

    ObjectMessage request = session.createObjectMessage();
    if (timeout != 0) request.setJMSReplyTo(queue);
    request.setStringProperty("callee", callee);
    request.setObject(action);
    prod.send(dest, request);
    if (timeout == 0) return null;
    String msgid = request.getJMSMessageID();
    long end = System.currentTimeMillis() + timeout;
    
    Message reply = null;
    do {
      reply = null;
      reply = cons.receive(timeout);
      if (reply == null) break;
      if (msgid.equals(reply.getJMSCorrelationID()))
        break;
      timeout = end - System.currentTimeMillis();
    } while (timeout >= 0);
    if (reply == null) return null;
    
    return new ActionReturn(reply.getIntProperty("replyCode"),
                            reply.getStringProperty("replyMsg"));
  }

  synchronized void close() {
    try {
      if (session != null)
        session.close();
    } catch (JMSException exc) {
      Trace.error("ActionRequestor: Cannot close session", exc);
    }
  }
}

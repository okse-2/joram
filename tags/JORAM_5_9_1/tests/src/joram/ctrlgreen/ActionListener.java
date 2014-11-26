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
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ActionListener implements MessageListener {
  ActionHandler handler = null;
  Session session = null;
  MessageConsumer consumer = null;
  MessageProducer producer = null;
  
  ActionListener(ActionHandler handler, Connection cnx, Destination dest, String name) throws JMSException {
    Trace.debug("Initialize ActionListener");
    this.handler = handler;
    try {
      session = cnx.createSession(true, 0);
      consumer = session.createConsumer(dest, "callee='" + name + "'");
      producer = session.createProducer(null);
      consumer.setMessageListener(this);
    } catch (JMSException exc) {
      close();
      throw exc;
    }
  }
  
  @Override
  public void onMessage(Message msg) {
    try {
    Trace.debug("ActionListener: get " + msg);
    Destination replyTo = null;
    String correlationId = null;
    ActionReturn reply = null;
    try {
      replyTo = msg.getJMSReplyTo();
      correlationId = msg.getJMSMessageID();
    } catch (JMSException exc) {
      Trace.error("ActionListener: Cannot get replyTo.");
    }
    
    try {
      int type = msg.getIntProperty("type");
      String parameters = msg.getStringProperty("parameters");
      Action action = new Action(type, parameters);
      Trace.debug("ActionListener: call handler");
      reply = handler.onAction(action);
    } catch (Exception exc) {
      Trace.error("ActionListener: Bad action.", exc);
      reply = new ActionReturn(ActionReturn.ERROR, "Bad action definition");
    }

    Trace.debug("ActionListener: reply to " + replyTo);
    if (replyTo != null) {
      Message replyMsg;
      try {
        replyMsg = session.createMessage();
        replyMsg.setJMSCorrelationID(correlationId);
        replyMsg.setIntProperty("replyCode", reply.error);
        if (reply.msg != null)
          replyMsg.setStringProperty("replyMsg", reply.msg);
        producer.send(replyTo, replyMsg);
      } catch (JMSException exc) {
        Trace.error("ActionListener: Cannot send reply.", exc);
      }
    }
    try {
      session.commit();
    } catch (JMSException exc) {
      Trace.error("ActionListener: Cannot commit.", exc);
    }
    } catch (Throwable t) {
      Trace.fatal("ActionListener.onMessage", t);
    } finally {
      Trace.debug("ActionListener: end ");
    }
  }
  
  
  void close() {
    try {
      if (producer != null)
        producer.close();
    } catch (JMSException exc) {
      Trace.error("InventoryListener: Cannot close producer.", exc);
    } finally {
      producer = null;
    }
    try {
      if (consumer != null)
        consumer.close();
    } catch (JMSException exc) {
      Trace.error("InventoryListener: Cannot close consumer.", exc);
    } finally {
      consumer = null;
    }
    try {
      if (session != null)
        session.close();
    } catch (JMSException exc) {
      Trace.error("InventoryListener: Cannot close session.");
    } finally {
      session = null;
    }
    handler = null;
  }
}

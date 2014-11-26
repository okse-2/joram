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
package com.scalagent.ctrlgreen;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ParameterListener implements MessageListener {
  ParameterHandler handler = null;
  Session session = null;
  MessageConsumer consumer = null;
  
  ParameterListener(ParameterHandler handler, Connection cnx, Destination dest) throws JMSException {
    this.handler = handler;
    try {
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    consumer = session.createConsumer(dest);
    consumer.setMessageListener(this);
    } catch (JMSException exc) {
      close();
      throw exc;
    }
  }
  
  @Override
  public void onMessage(Message msg) {
    if (msg instanceof ObjectMessage) {
      try {
        Parameters parameters = (Parameters) ((ObjectMessage) msg).getObject();
        handler.onParameter(parameters);
      } catch (Exception exc) {
        Trace.error("ParameterListener: Bad parameters.", exc);
      }
    } else {
      Trace.error("ParameterListener: Bad message type.");
    }
  }
  
  void close() {
    try {
      if (consumer != null)
        consumer.close();
    } catch (JMSException exc) {
      Trace.error("ParameterListener: Cannot close consumer.", exc);
    } finally {
      consumer = null;
    }
    try {
      if (session != null)
        session.close();
    } catch (JMSException exc) {
      Trace.error("ParameterListener: Cannot close session.");
    } finally {
      session = null;
    }
    handler = null;
  }

}

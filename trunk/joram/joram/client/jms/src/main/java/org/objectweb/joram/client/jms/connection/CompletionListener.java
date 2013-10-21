/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.connection;

import java.util.ArrayList;

import org.objectweb.joram.client.jms.MessageProducer;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class CompletionListener {
  public static Logger logger = Debug.getLogger(CompletionListener.class.getName());
  
  Session session = null;
  MessageProducer messageProducer = null;
  ArrayList<javax.jms.CompletionListener> listeners = null;
  ArrayList<javax.jms.Message> messages = null;
  
  public CompletionListener(Session session, MessageProducer messageProducer) {
    this.session = session;
    this.messageProducer = messageProducer;
    listeners = new ArrayList<javax.jms.CompletionListener>();
    messages = new ArrayList<javax.jms.Message>();
  }

  public void addCompletionListener(javax.jms.CompletionListener listener, javax.jms.Message message) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "addCompletionListener(" + listener + ", " + message + ')');
    if (listener != null) {
      listeners.add(listener);
      messages.add(message);
    }
  }
  
  /**
   * Notifies the application that the message has been successfully sent
   */
  void onCompletion() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "onCompletion()");
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onCompletion(messages.get(i));
//      new Thread() {
//        public void run() {
//          listener.onCompletion(message);
//        }
//      }.start();
    }
  }

  /**
   * Notifies user that the specified exception was thrown while attempting to
   * send the specified message. If an exception occurs it is undefined
   * whether or not the message was successfully sent.
   * 
   * @param exception the exception
   * 
   */
  void onException(final Exception exception) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "onException(" + exception + ')');
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onException(messages.get(i), exception);
//      new Thread() {
//        public void run() {
//          listener.onException(message, exception);
//        }
//      }.start();
    }
  }
  
  public String toString() {
    return "CompletionListener (" + session + ", " + messageProducer + ", " + listeners + ", " + messages +')';
  }
}

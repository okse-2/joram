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

public class CompletionListener {
  javax.jms.CompletionListener listener = null;
  javax.jms.Message message = null;
  
  public CompletionListener(javax.jms.CompletionListener listener, javax.jms.Message message) {
    this.listener = listener;
    this.message = message;
  }
  
  /**
   * Notifies the application that the message has been successfully sent
   */
  void onCompletion() {
    if (listener != null) {
      new Thread() {
        public void run() {
          listener.onCompletion(message);
        }
      }.start();
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
    if (listener != null) {
      new Thread() {
        public void run() {
          listener.onException(message, exception);
        }
      }.start();
    }
  }
}

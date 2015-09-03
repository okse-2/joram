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
package org.objectweb.joram.mom.proxies;

/**
 * A <code>ProxyMessageSender</code> sends messages (<code>ProxyMessage</code>)
 * to a client. Messages can be sent synchronously by directly calling
 * <code>send</code> or asynchronously by executing a task (if allowed by the
 * <code>ProxyMessageSender</code>).
 */
public interface ProxyMessageSender {
  
  /**
   * Sends a message to the client.
   * @param msg the message to be sent
   * @throws Exception if an error occurs
   */
  void send(ProxyMessage msg) throws Exception;
  
  /**
   * States whether executing a task is allowed.
   * @return <code>true</code> if executing a task is allowed;
   *         <code>false</code> otherwise.
   */
  boolean isExecutor();
  
  /**
   * Executes a task.
   * @param task task to be executed
   */
  void execute(Runnable task);
  
  /**
   * Closes the connection used by this
   * <code>ProxyMessageSender</code> to
   * send messages to the client.
   */
  void close();

}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import org.objectweb.joram.shared.messages.Message;

/**
 * {@link DistributionHandler} interface is made to work with a distribution
 * queue or topic via a {@link DistributionModule}. Its purpose is to easily
 * extend Joram destinations in order to deliver JMS messages outside of the JMS
 * world, for example using e-mail or ftp.
 */
public interface DistributionHandler {

  /**
   * Distributes the given message outside of the JORAM server.
   * 
   * @param message
   *          the message to distribute
   * @throws Exception if the message could not be distributed. The message will
   *           be forwarded to a DMQ, if any.
   */
  public void distribute(Message message) throws Exception;

  /**
   * Configures the handler with the given properties.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void init(Properties properties);

  /**
   * Closes this handler and releases any system resources associated to it.
   * There will be no subsequent call to this handler after it has been closed.
   */
  public void close();

}

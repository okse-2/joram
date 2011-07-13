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

/**
 * {@link AcquisitionDaemon} interface is made to work with an acquisition queue
 * or topic via an {@link AcquisitionModule}. Its purpose is to collect messages
 * from non-JMS sources in order to inject them into the JMS world. When the
 * daemon is started, it starts listening to incoming messages. Once received,
 * these messages are transmitted to the MOM using the given transmitter.
 * <p>
 * This interface is made for implicit acquisition. For on-demand or periodic
 * acquisition, you need to extend {@link AcquisitionHandler} instead.
 */
public interface AcquisitionDaemon {

  /**
   * Tells the daemon to start with the given properties.
   * 
   * @param properties
   *          The initial set of properties.
   * @param transmitter
   *          a transmitter used to transmit retrieved messages to the MOM
   *          reliably.
   */
  public void start(Properties properties, ReliableTransmitter transmitter);

  /**
   * Tells the daemon to stop. Any system resources previously allocated must be
   * released.
   */
  public void stop();

}

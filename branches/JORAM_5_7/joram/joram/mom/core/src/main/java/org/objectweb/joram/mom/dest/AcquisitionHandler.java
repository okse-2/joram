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
 * {@link AcquisitionHandler} interface is made to work with an acquisition
 * queue or topic via an {@link AcquisitionModule}. Its purpose is to retrieve
 * messages from non-JMS sources (e-mail, ftp, JMX, ...) in order to inject them
 * into the JMS world. The {@link #retrieve(ReliableTransmitter)} method is
 * called regularly depending on how the {@link AcquisitionModule} is
 * configured.
 * <p>
 * This interface is made for explicit acquisition. For implicit acquisition
 * such as message listeners, you need to extend {@link AcquisitionDaemon}
 * instead.
 */
public interface AcquisitionHandler {

  /**
   * Retrieves one or more message from an external source (e-mail, ftp, ...).
   * Message properties such as priority, expiration or persistence will be set
   * afterwards by the {@link AcquisitionModule}.<br>
   * <br>
   * If the external source is reliable, acknowledgment can be done safely after
   * transmitting the message using the transmitter.
   * 
   * @param transmitter
   *          a transmitter used to transmit retrieved messages to the MOM
   *          reliably.
   * @throws Exception
   */
  public void retrieve(ReliableTransmitter transmitter) throws Exception;

  /**
   * Configures the handler with the given properties. This method is called one
   * time before the first call to retrieve and then when the acquisition
   * destination receives a configuration message.
   * 
   * @param properties
   *          The new set of properties.
   */
  public void setProperties(Properties properties);

  /**
   * Closes this handler and releases any system resources associated to it.
   * There will be no subsequent call to this handler after it has been closed.
   */
  public void close();

}

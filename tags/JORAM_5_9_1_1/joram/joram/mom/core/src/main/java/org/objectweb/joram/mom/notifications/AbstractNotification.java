/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * The <code>AbstractNotification</code> class is the superclass of the
 * notifications exchanged by a client agent and a MOM destination agent.
 */
public abstract class AbstractNotification extends Notification {
  /**
   * The <code>clientContext</code> field allows a client to identify a context
   * within which a notification is exchanged with a destination.
   * <p>
   * This field is for the client use only and might not be set.
   */
  private int clientContext = -1;

  /**
   * Constructs an <code>AbstractNotification</code>.
   *
   * @param clientContext  Identifies a client context.
   */
  public AbstractNotification(int clientContext) {
    this.clientContext = clientContext;
  }

  /**
   * Constructs an <code>AbstractNotification</code>.
   */
  public AbstractNotification() {}


  /** Returns the client context identifier. */
  public int getClientContext() {
    return clientContext;
  }

  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(", clientContext=").append(clientContext);
    output.append(')');

    return output;
  }
  
  public int getEncodedSize() throws Exception {
    return super.getEncodedSize() + INT_ENCODED_SIZE;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encode32(clientContext);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    clientContext = decoder.decode32();
  }
  
}

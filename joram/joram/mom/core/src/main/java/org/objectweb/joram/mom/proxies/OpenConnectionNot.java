/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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

import org.objectweb.joram.mom.proxies.ConnectionContext.Type;

import fr.dyade.aaa.agent.SyncNotification;

public class OpenConnectionNot extends SyncNotification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private ConnectionContext.Type type;

  private int heartBeat;

  private boolean noAckedQueue;

  public OpenConnectionNot(ConnectionContext.Type type, int heartBeat, boolean noAckedQueue) {
    this.type = type;
    this.heartBeat = heartBeat;
    this.noAckedQueue = noAckedQueue;
  }

  public void Return(ConnectionContext ctx) {
    Return(new Object[] { ctx });
  }
  
  public Type getType() {
    return type;
  }

  public final int getHeartBeat() {
    return heartBeat;
  }

  public final boolean isNoAckedQueue() {
    return noAckedQueue;
  }

  public final Object getConnectionContext() {
    return getValue(0);
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   * 
   * @param output buffer to fill in
   * @return <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",type=").append(type.toString());
    output.append(",heartBeat=").append(heartBeat);
    output.append(",noAckedQueue=").append(noAckedQueue);
    output.append(')');

    return output;
  }
}

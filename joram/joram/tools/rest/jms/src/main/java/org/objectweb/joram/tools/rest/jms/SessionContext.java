/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import javax.jms.Destination;
import javax.jms.JMSContext;

public abstract class SessionContext {

  private RestClientContext clientCtx;
  private JMSContext jmsContext;
  private Destination dest;
  protected long lastId = 0;

  public SessionContext(RestClientContext clientCtx) {
    this.clientCtx = clientCtx;
  }

  public void setDest(Destination dest) {
    this.dest = dest;
  }

  public Destination getDest() {
    return dest;
  }

  public synchronized long getLastId() {
    return lastId;
  }

  public synchronized long incLastId() {
    lastId++;
    return lastId;
  }
  
  public synchronized void setLastId(long lastId) {
    this.lastId = lastId;
  }

  /**
   * @return the jmsContext
   */
  public JMSContext getJmsContext() {
    return jmsContext;
  }

  /**
   * @param jmsContext
   *          the jmsContext to set
   */
  public void setJmsContext(JMSContext jmsContext) {
    this.jmsContext = jmsContext;
  }

  /**
   * @return the clientCtx
   */
  public RestClientContext getClientCtx() {
    return clientCtx;
  }
}

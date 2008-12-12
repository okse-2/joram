/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp.proxy.request;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class AccessRequestNot extends SyncNotification {
  
  private int channelId;
  private String realm;
  private boolean exclusive; 
  private boolean passive;
  private boolean active;
  private boolean write;
  private boolean read;
  
  /**
   * @param channelId
   * @param realm
   * @param exclusive
   * @param passive
   * @param active
   * @param write
   * @param read
   */
  public AccessRequestNot(int channelId, String realm, boolean exclusive,
      boolean passive, boolean active, boolean write, boolean read) {
    super();
    this.channelId = channelId;
    this.realm = realm;
    this.exclusive = exclusive;
    this.passive = passive;
    this.active = active;
    this.write = write;
    this.read = read;
  }
  
  public boolean isActive() {
    return active;
  }
  public int getChannelId() {
    return channelId;
  }
  public boolean isExclusive() {
    return exclusive;
  }
  public boolean isPassive() {
    return passive;
  }
  public boolean isRead() {
    return read;
  }
  public String getRealm() {
    return realm;
  }
  public boolean isWrite() {
    return write;
  }
  
  public AMQP.Access.RequestOk accessRequest(AgentId proxyId) throws Exception {
    Object[] res = invoke(proxyId);
    return (AMQP.Access.RequestOk) res[0];
  }
  
  public void Return(AMQP.Access.RequestOk res) {
    Return(new Object[]{res});
  }

}

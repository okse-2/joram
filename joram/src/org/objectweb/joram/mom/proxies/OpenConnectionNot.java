/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.*;

public class OpenConnectionNot extends SyncNotification {

  private boolean reliable;
  
  private int heartBeat;

  public OpenConnectionNot(boolean reliable,
                           int heartBeat) {
    this.reliable = reliable;
    this.heartBeat = heartBeat;
  }
  
  public void Return(int key, Object queue) {
    Return(new Object[]{
      new Integer(key),
      queue});
  }

  public final boolean getReliable() {
    return reliable;
  }

  public final int getHeartBeat() {
    return heartBeat;
  }

  public final int getKey() {
    return ((Integer)getValue(0)).intValue();
  }

  public final Object getReplyQueue() {
    return getValue(1);
  }

  public String toString() {
    return '(' + super.toString() +
      ",reliable=" + reliable + 
      ",heartBeat=" + heartBeat + ')';
  }
}

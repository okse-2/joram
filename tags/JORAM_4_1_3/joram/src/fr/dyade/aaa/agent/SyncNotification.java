/*
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

/**
 * This notification is used to synchronously call a
 * local agent from a collocated thread.
 */
public class SyncNotification extends Notification {
  private transient Object lock;

  private transient Result res;

  protected SyncNotification() {
    persistent = false;
    lock = new Object();
    res = new Result();
  }

  public Object[] invoke(AgentId to) 
    throws InterruptedException, Exception {
    synchronized (lock) {
      Channel.sendTo(to, this);
      lock.wait();
    }
    if (res.exc != null) {
      throw res.exc;
    } else {
      return res.values;
    }
  }

  public void Throw(Exception exc) {
    if (lock != null) {
      synchronized (lock) {
        res.exc = exc;
        lock.notify();
      }
    }
  }

  public void Return(Object[] values) {
    if (lock != null) {
      synchronized (lock) {
        res.values = values;
        lock.notify();
      }
    }
  }

  public Object getValue(int index) {
    if (res.values != null) {
      return res.values[index];
    } else return null;
  }

  public final Exception getException() {
    return res.exc;
  }

  static class Result {
    Object[] values;
    Exception exc;
  }
}

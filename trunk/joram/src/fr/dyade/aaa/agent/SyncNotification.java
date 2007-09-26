/*
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private transient Context ctx;

  protected SyncNotification() {
    persistent = false;
    ctx = new Context(this);
  }

  public Object[] invoke(AgentId to) 
    throws InterruptedException, Exception {
    return ctx.invoke(to);
  }

  public void Throw(Exception exc) {
    if (ctx != null) {
      ctx.Throw(exc);
    }
  }

  public void Return(Object[] values) {
    if (ctx != null) {
      ctx.Return(values);
    }
  }

  public Object getValue(int index) {
    if (ctx != null) {
      return ctx.getValue(index);
    } else return null;
  }

  public final Exception getException() {
    if (ctx != null) {
      return ctx.getException();
    } else return null;
  }

  static class Result {
    Object[] values;
    Exception exc;
  }

  public static class Context {
    private Notification syncRequest;

    private Result res;

    public Context(Notification syncRequest) {
      this.syncRequest = syncRequest;
      res = new Result();
    }

    public synchronized Object[] invoke(AgentId to) 
      throws InterruptedException, Exception {
      Channel.sendTo(to, syncRequest);
      wait();
      if (res.exc != null) {
        throw res.exc;
      } else {
        return res.values;
      }
    }

    public synchronized void Throw(Exception exc) {
      res.exc = exc;
      notify();
    }

    public synchronized void Return(Object[] values) {
      res.values = values;
      notify();
    }

    public Object getValue(int index) {
      if (res.values != null) {
        return res.values[index];
      } else return null;
    }

    public final Exception getException() {
      return res.exc;
    }
  }
}

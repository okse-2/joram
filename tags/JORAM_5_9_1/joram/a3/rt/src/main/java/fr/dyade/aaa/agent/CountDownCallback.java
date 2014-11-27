/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Callback to be invoked once a countdown has reached the value zero.
 * The countdown is incremented when the <code>CountDownCallback</code> is
 * assigned to a <code>CallbackNotification</code> and decremented after the
 * <code>CallbackNotification</code> has been successfully processed, or after
 * an error has been raised.
 */
public class CountDownCallback {
  
  private AtomicInteger countDown;
  
  private List<Throwable> errors;
  
  private Callback callback;

  /**
   * Creates a callback by specifying the
   * <code>Callback</code> to invoke.
   * Not permitted in an <code>Agent</code>.
   * @param callback the callback to invoke
   * @throws RuntimeException if called by an <code>Agent</code>
   */
  public CountDownCallback(Callback callback) {
    super();
    if (AgentServer.isEngineThread()) {
      throw new RuntimeException(
          "Not permitted: the current thread belongs to the Engine");
    }
    this.callback = callback;
    countDown = new AtomicInteger();
  }

  void incrementAndGet() {
    countDown.incrementAndGet();
  }
  
  void done() {
    int count = countDown.decrementAndGet();
    if (count == 0) {
      if (errors != null) {
        callback.failed(errors);
      } else {
        callback.done();
      }
    }
  }
  
  void failed(Throwable error) {
    if (errors == null) {
      errors = new ArrayList<Throwable>(countDown.get());
    }
    errors.add(error);
    int count = countDown.decrementAndGet();
    if (count == 0) {
      callback.failed(errors);
    }
  }

}

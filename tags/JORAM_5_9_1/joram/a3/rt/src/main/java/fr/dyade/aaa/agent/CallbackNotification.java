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

/**
 * Notification locally transmitting a <code>CountDownCallback</code>. The
 * <code>CountDownCallback</code> is not persistent and is not transmitted to a
 * remote destination. If sent to a remote destination, the
 * <code>CountDownCallback</code> should be decremented either after the
 * notification has been successfully transmitted, or if a transmission error
 * has been raised. A <code>CountDownCallback</code> can be transmitted in
 * several <code>CallbackNotification</code> instances. In that case it is
 * incremented and decremented once for every <code>CallbackNotification</code>.
 */
public class CallbackNotification extends Notification {
  
  private transient CountDownCallback countDownCallback;

  /**
   * Returns <code>true</code> if a <code>CountDownCallback</code> is
   * transmitted, <code>false</code> otherwise.
   * 
   * @return <code>true</code> if a <code>CountDownCallback</code> is
   *         transmitted, <code>false</code> otherwise
   */
  public boolean hasCallback() {
    return (countDownCallback != null);
  }
  
  /**
   * Passes the <code>CountDownCallback</code> to the specified
   * <code>CallbackNotification</code>.
   * 
   * @param not
   *          the <code>CallbackNotification</code> to be assigned with the
   *          <code>CountDownCallback</code> owned by this
   *          <code>CallbackNotification</code>
   */
  public void passCallback(CallbackNotification not) {
    not.setCountDownCallback(countDownCallback);
  }

  /**
   * Sets the <code>CountDownCallback</code> to be transmitted by this
   * <code>CallbackNotification</code>.
   * 
   * @param the
   *          <code>CountDownCallback</code> to transmit
   */
  public void setCountDownCallback(CountDownCallback countDownCallback) {
    if (countDownCallback != null) {
      countDownCallback.incrementAndGet();
      this.countDownCallback = countDownCallback;
    }
  }
  
  /**
   * Called if this <code>CallbackNotification</code> is successfully processed.
   */
  public void done() {
    if (countDownCallback != null) {
      countDownCallback.done();
      countDownCallback = null;
    }
  }
  
  /**
   * Called if this <code>CallbackNotification</code> raises an error.
   * @param error the error that has been raised
   */
  public void failed(Throwable error) {
    if (countDownCallback != null) {
      countDownCallback.failed(error);
      countDownCallback = null;
    }
  }

}

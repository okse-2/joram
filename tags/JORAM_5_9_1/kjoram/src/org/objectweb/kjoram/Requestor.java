/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

public class Requestor implements ReplyListener {

  private static class Status {
    /**
     * The requestor is free: it can be called by a client thread.
     */
    public static final int INIT = 0;

    /**
     * The requestor is busy: the client thread is waiting.
     * Two threads can make a call:
     *   1- the demultiplexer thread can call replyReceived and replyAborted.
     *   2- another client thread can abort the request.
     */
    public static final int RUN = 1;

    /**
     * The requestor is either completed (by the demultiplxer thread) or
     * aborted (by another client thread  or a timeout).
     * This state is transitional. It enables the requesting client thread to
     * finalize its request.
     */
    public static final int DONE = 2;

    public static final int CLOSE = 3;

    private static final String[] names = {
      "INIT", "RUN", "DONE", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  private int status;

  private RequestMultiplexer mtpx;

  AbstractRequest request;
  AbstractReply reply;

  public Requestor(RequestMultiplexer mtpx) {
    this.mtpx = mtpx;
    if (status == Status.DONE) {
      setStatus(Status.INIT);
      request = null;
      reply = null;
    }
  }

  private void setStatus(int status) {
    this.status = status;
  }

  public final synchronized int getRequestId() {
    return request.getRequestId();
  }

  public synchronized AbstractReply request(AbstractRequest request) throws JoramException {
    return request(request, 0);
  }

  /**
   * Method sending a synchronous request to the server and waiting for an
   * answer.
   *
   * @exception IllegalStateException  If the connection is closed or broken,
   *                                   if the server state does not allow to
   *                                   process the request.
   * @exception SecurityException  When sending a request to a destination
   *              not accessible because of security.
   * @exception InvalidDestinationException  When sending a request to a
   *              destination that no longer exists.
   * @exception JoramException  If the request failed for any other reason.
   */
  public synchronized AbstractReply request(
    AbstractRequest request,
    long timeout) 
    throws JoramException {

    if (status == Status.CLOSE) {
      return null;
    } else if (status == Status.RUN) {
      throw new IllegalStateException("Requestor already used");
    }

    this.request = request;
    this.reply = null;
    mtpx.sendRequest(request, this);
    setStatus(Status.RUN);
        
    try {
      wait(timeout);
    } catch (InterruptedException exc) {
      setStatus(Status.DONE);
    }
    
    if (status == Status.RUN) {
      // Means that the wait ended with a timeout.
      // Abort the request.
      mtpx.abortRequest(getRequestId());
      this.request = null;
      return null;
    } else if (status == Status.CLOSE) {
      if (reply instanceof ConsumerMessages) {
        mtpx.deny((ConsumerMessages) reply);
      }
      this.request = null;
      return null;
    } else if (status == Status.DONE) {
      this.request = null;
      return reply;
    }
    return reply;
  }

  public synchronized boolean replyReceived(AbstractReply reply) 
    throws AbortedRequestException {

    if (status == Status.RUN &&
        reply.getCorrelationId() == request.getRequestId()) {      
      this.reply = reply;
      setStatus(Status.DONE);
      notify();
      return true;
    } else {
      // The request has been aborted.
      throw new AbortedRequestException();
    }
  }
  
  public synchronized void replyAborted() {
    if (status == Status.RUN) {
      this.request = null;
      this.reply = null;
      setStatus(Status.DONE);
      notify();
    }
    // Else the request has been aborted.
    // Do nothing
  }

  public synchronized void abortRequest() {
    if (status == Status.RUN && request != null) {
      mtpx.abortRequest(request.getRequestId());
      this.request = null;
      setStatus(Status.DONE);
      notify();
    }
    // Else the request has been completed.
    // Do nothing
  }

  public synchronized void close() {
    if (status != Status.CLOSE) {
      abortRequest();
      setStatus(Status.CLOSE);
    }
    // Else idempotent.
  }
}

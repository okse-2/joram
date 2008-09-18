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

public class MessageConsumerListener implements ReplyListener {

  /**
   * Status of the message consumer listener.
   */
  protected static class Status {
    public static final int INIT = 0;
    public static final int RUN = 1;
    public static final int DONE = 2;
    public static final int CLOSE = 3;

    private static final String[] names = {
      "INIT", "RUN", "DONE", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }
  
  public int status;

  public RequestMultiplexer mtpx;

  public AbstractRequest req;
  public AbstractReply reply;

  public MessageListener listener;
  
  public MessageConsumerListener(RequestMultiplexer mtpx, MessageListener listener) {
    this.mtpx = mtpx;
    this.listener = listener;
    status = Status.INIT;
  }

  /**
   * Called by Session.
   */
  public synchronized void request(AbstractRequest req) throws JoramException {
    if (status != Status.INIT) {
      throw new IllegalStateException();
    }
    this.req = req;
    reply = null;
    status = Status.RUN;
    mtpx.sendRequest(req, this);
  }

  public AbstractReply getReply() throws JoramException {
    if (status == Status.RUN) return null;

    if (status == Status.DONE) return reply;

    throw new AbortedRequestException();
  }

  public synchronized void abortRequest() {
    if (status == Status.RUN) {
      mtpx.abortRequest(getRequestId());
      status = Status.INIT;
    }
  }

  public synchronized void close() throws JoramException {
    if (status != Status.CLOSE) {
      status = Status.CLOSE;
      abortRequest();
    }
  }

  public MessageListener getMessageListener() {
    return listener;
  }


  /**
   * Called by RequestMultiplexer.
   */
  public int getRequestId() {
    return req.getRequestId();
  }

  public synchronized boolean replyReceived(AbstractReply reply) throws AbortedRequestException {
    if (status == Status.RUN) {
      this.reply = reply;
      status = Status.DONE;
      return true;
    }

//  The request has been aborted.
    throw new AbortedRequestException();
  }

  public void replyAborted() {
    if (status == Status.RUN) {
      reply = null;
      status = Status.DONE;
    }
  }
}

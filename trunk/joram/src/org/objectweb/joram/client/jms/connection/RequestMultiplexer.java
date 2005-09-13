/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
package org.objectweb.joram.client.jms.connection;

import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.PingRequest;
import org.objectweb.joram.shared.client.SessDenyRequest;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.DestinationException;
import org.objectweb.joram.shared.excepts.MomException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSException;

import fr.dyade.aaa.util.Timer;

import org.objectweb.util.monolog.api.BasicLevel;

public class RequestMultiplexer {

  private static class Status {
    public static final int OPEN = 0;
    public static final int CLOSE = 1;

    private static final String[] names = {
      "OPEN", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  private String cnxId;

  private int status;

  private RequestChannel channel;

  public Hashtable requestsTable;

  private int requestCounter;

  private DemultiplexerDaemon demtpx;

  private Timer timer;

  /**
   * The task responsible for keeping
   * the connection alive.
   */
  private HeartBeatTask heartBeatTask;

  private javax.jms.ExceptionListener exceptionListener;

  /**
   * The date of the last request
   */
  private long lastRequestDate;

  public RequestMultiplexer(
    RequestChannel channel,
    long heartBeat,
    String cnxId) throws JMSException {
    this.channel = channel;
    this.cnxId = cnxId;
    requestsTable = new Hashtable();
    requestCounter = 0;
    demtpx = new DemultiplexerDaemon();
    demtpx.start();
    setStatus(Status.OPEN);
    
    timer = new Timer();
    if (heartBeat > 0) {
      heartBeatTask = new HeartBeatTask(heartBeat);
      lastRequestDate = System.currentTimeMillis();
      try {
        heartBeatTask.start();
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
        throw new JMSException(exc.toString());
      }
    }
  }

  private void setStatus(int status) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "RequestMultiplexer.setStatus(" + 
        Status.toString(status) + ')');
    this.status = status;
  }
  
  public boolean isClosed() {
    return status == Status.CLOSE;
  }

  public void setExceptionListener(
    javax.jms.ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
  }

  public javax.jms.ExceptionListener getExceptionListener() {
    return exceptionListener;
  }

  public void sendRequest(
    AbstractJmsRequest request)
    throws JMSException {
    sendRequest(request, null);
  }

  public synchronized void sendRequest(
    AbstractJmsRequest request,
    ReplyListener listener) 
    throws JMSException {
    if (status == Status.CLOSE) 
      throw new IllegalStateException("Connection closed");

    if (requestCounter == Integer.MAX_VALUE) {
      requestCounter = 0;
    }

   request.setRequestId(requestCounter++);

    if (listener != null) {
      requestsTable.put(
        new Integer(request.getRequestId()), 
        listener);
    }

    try {
      channel.send(request);
    } catch (Exception exc) {
      JMSException jmsExc = new JMSException(exc.toString());
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }

    if (heartBeatTask != null) {
      lastRequestDate = System.currentTimeMillis();
    }
  }

  /**
   * Not synchronized because it would possibly
   * deadlock with some reply listeners 
   * (actually requestors).
   */
  public void close() {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "RequestMultiplexer.close()");
    
    synchronized (this) {
      if (status == Status.CLOSE)
        return;
      // Immediately set the status as no error
      // can be thrown. This enables to release
      // the lock and avoid any dead lock
      // with the demultiplexer thread that
      // calls close() when interrupted.
      setStatus(Status.CLOSE);
    }

    if (heartBeatTask != null)
      heartBeatTask.cancel();

    if (timer != null)
      timer.cancel();
    
    channel.close();

    demtpx.stop();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, " -> requestsTable=" + requestsTable);
    
    // The requests table can't be accessed
    // either by an external thread (status CLOSE)
    // or by the internal demultiplexer thread (stopped).
    Enumeration requestIds = requestsTable.keys();
    Enumeration listeners = requestsTable.elements();
    while (listeners.hasMoreElements()) {
      Integer requestId = (Integer)requestIds.nextElement();
      ReplyListener rl = (ReplyListener)listeners.nextElement();
      rl.replyAborted(requestId.intValue());
    }
    requestsTable.clear();
  }

  /**
   * Used by Connection clean up.
   * It's a very specific usage linked to
   * the connector layer.
   */
  public void cleanup() {
    Enumeration requestIds = requestsTable.keys();
    Enumeration listeners = requestsTable.elements();
    while (listeners.hasMoreElements()) {
      Integer requestId = (Integer)requestIds.nextElement();
      ReplyListener rl = (ReplyListener)listeners.nextElement();
      rl.replyAborted(requestId.intValue());
    }
    requestsTable.clear();
  }

  /**
   * Not synchronized because it would possibly
   * deadlock with some reply listeners 
   * (actually requestors).
   */
  public void abortRequest(int requestId) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "RequestMultiplexer.abortRequest(" + requestId + ')');
    ReplyListener rl = doAbortRequest(requestId);    
    if (rl != null) {
      rl.replyAborted(requestId);
    }
  }
  
  private synchronized ReplyListener doAbortRequest(int requestId) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "RequestMultiplexer.doAbortRequest(" + 
        requestId + ')');
    if (status == Status.CLOSE) return null;
    return (ReplyListener)requestsTable.remove(
      new Integer(requestId));
  }

  /**
   * Not synchronized because it may be called by the
   * demultiplexer during a concurrent close. It would deadlock
   * as the close waits for the demultiplexer to stop.
   */
  private void route(AbstractJmsReply reply) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "RequestMultiplexer.route(" + reply + ')');
    int requestId = reply.getCorrelationId();
    Integer requestKey = new Integer(requestId);
    ReplyListener rl = (ReplyListener)requestsTable.get(requestKey);
    if (reply instanceof MomExceptionReply) {
      MomException momExc = ((MomExceptionReply)reply).getException();
      JMSException jmsExc = null;
      if (momExc instanceof AccessException) {
        jmsExc = new JMSSecurityException(momExc.getMessage());
      } else if (momExc instanceof DestinationException) {
        jmsExc = new InvalidDestinationException(momExc.getMessage());
      } else {
        jmsExc = new JMSException(momExc.getMessage());
      }
      if (rl instanceof ErrorListener) {
        ((ErrorListener)rl).errorReceived(requestId, jmsExc);
      } else {
        // The listener is null or doesn't implement ErrorListener
        onException(jmsExc);
      }
    } else {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, " -> rl = " + rl + ')');
      if (rl != null) {
        try {
          if (rl.replyReceived(reply)) {
            requestsTable.remove(requestKey);
          }
        } catch (AbortedRequestException exc) {
          JoramTracing.dbgClient.log(
            BasicLevel.WARN, 
            " -> Request aborted: " + requestId);
          abortReply(reply);
        }
      } else {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
          JoramTracing.dbgClient.log(
            BasicLevel.WARN, 
            " -> Listener not found for the reply: " + requestId);
        abortReply(reply);
      }
    }
  }

  private void abortReply(AbstractJmsReply reply) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "RequestMultiplexer.abortReply(" + 
        reply + ')');
    if (reply instanceof ConsumerMessages) {
      deny((ConsumerMessages)reply);
    }
    // Else nothing to do.
  }

  public void deny(ConsumerMessages messages) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "RequestMultiplexer.deny(" + 
        messages + ')');
    Vector msgList = messages.getMessages();
    Vector ids = new Vector();
    for (int i = 0; i < msgList.size(); i++) {
      org.objectweb.joram.shared.messages.Message msg =
        (org.objectweb.joram.shared.messages.Message) msgList.elementAt(i);
      ids.addElement(msg.getIdentifier());
    }
    SessDenyRequest deny = new SessDenyRequest(
      messages.comesFrom(),
      ids,
      messages.getQueueMode());
    try {
      sendRequest(deny);
    } catch (JMSException exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      // Connection failure
      // Nothing to do
    }
  }

  private void onException(JMSException jmsExc) {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "RequestMultiplexer.onException(" + 
        jmsExc + ')');
    if (exceptionListener != null) {
      exceptionListener.onException(jmsExc);
    }
  }

  public void schedule(fr.dyade.aaa.util.TimerTask task,
                       long period) {
    if (timer != null) {
      try {
        timer.schedule(task, period);
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
      }
    }
  }

  private class DemultiplexerDaemon extends fr.dyade.aaa.util.Daemon {
    DemultiplexerDaemon() {
      super("Connection#" + cnxId);
    }

    public void run() {
      loop:
      while (running) {
        canStop = true;
        AbstractJmsReply reply;
        try {
          reply = channel.receive();
          if (reply instanceof ConsumerMessages) {
            java.util.Vector msgs = ((ConsumerMessages)reply).getMessages();
            // set momMessage read-only
            for (int i = 0; i < msgs.size(); i++ )
              ((org.objectweb.joram.shared.messages.Message)
               msgs.elementAt(i)).setReadOnly();
          }
        } catch (IOException ioe) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", ioe);
            RequestMultiplexer.this.close();
            exceptionListener.onException(new JMSException("Could not connect to JMS server!"));
            break loop;
        } catch (Exception exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "", exc);
          RequestMultiplexer.this.close();
          break loop;
        }
        canStop = false; 
        route(reply);
      }
    }

    /**
     * Enables the daemon to stop itself.
     */
    public void stop() {
      if (isCurrentThread()) {
        finish();
      } else {
        super.stop();
      }
    }

    protected void shutdown() {}

    protected void close() {}
  }

  /**
   * Timer task responsible for sending a ping message
   * to the server if no request has been sent during 
   * the specified timeout ('cnxPendingTimer' from the
   * factory parameters).
   */
  private class HeartBeatTask extends fr.dyade.aaa.util.TimerTask {    

    private long heartBeat;

    HeartBeatTask(long heartBeat) {
      this.heartBeat = heartBeat;
    }

    public void run() {
      try {
        long date = System.currentTimeMillis();        
        if ((date - lastRequestDate) > heartBeat) {
          sendRequest(new PingRequest());
        }
        start();
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
      }
    }

    public void start() throws Exception {
      timer.schedule(this, heartBeat);
    }
  }
}



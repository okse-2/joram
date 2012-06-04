/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.PingRequest;
import org.objectweb.joram.shared.client.SessDenyRequest;
import org.objectweb.joram.shared.excepts.MomException;

import fr.dyade.aaa.common.Debug;

public class RequestMultiplexer {

  private static class Status {
    public static final int OPEN = 0;
    public static final int CLOSE = 1;
    
    private static final String[] names = {"OPEN", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }
  
  private static Logger logger = Debug.getLogger(RequestMultiplexer.class.getName());

  /**
   * Converts a {@link MomExceptionReply} to the corresponding
   * {@link JMSException}.
   * 
   * @param excReply the MOM reply to convert
   * @return the corresponding Exception
   */
  public static JMSException buildJmsException(MomExceptionReply excReply) {
    JMSException jmsExc = null;
    int excType = excReply.getType();
    if (excType == MomExceptionReply.AccessException) {
      jmsExc = new JMSSecurityException(excReply.getMessage());
    } else if (excType == MomExceptionReply.DestinationException) {
      jmsExc = new InvalidDestinationException(excReply.getMessage());
    } else {
      jmsExc = new JMSException(excReply.getMessage());
    }
    return jmsExc;
  }

  private Connection cnx;

  private volatile int status;

  private RequestChannel channel;

  private Map requestsTable;

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
  private volatile long lastRequestDate;
  
  public RequestMultiplexer(Connection cnx,
                            RequestChannel channel,
                            long heartBeat) throws JMSException {
    this.channel = channel;
    this.cnx = cnx; 
    requestsTable = new Hashtable();
    requestCounter = 0;
    timer = new Timer();
    channel.setTimer(timer);
    try {
      channel.connect();
    } catch (JMSException exc) {
      throw exc;
    } catch (Exception exc) {
      // Wraps the incoming exception
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      if (timer != null) timer.cancel();
      throw new JMSException(exc.toString());
    }
    
    demtpx = new DemultiplexerDaemon();
    demtpx.start();
    setStatus(Status.OPEN);
    
    if (heartBeat > 0) {
      heartBeatTask = new HeartBeatTask(heartBeat);
      lastRequestDate = System.currentTimeMillis();
      try {
        heartBeatTask.start();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        throw new JMSException(exc.toString());
      }
    }
  }

  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }
  
  public boolean isClosed() {
    return status == Status.CLOSE;
  }
  
  public void closing() {
    channel.closing();
  }

  public void setExceptionListener(javax.jms.ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
  }

  public javax.jms.ExceptionListener getExceptionListener() {
    return exceptionListener;
  }

  public void sendRequest(AbstractJmsRequest request) throws JMSException {
    sendRequest(request, null);
  }
  
  public void sendRequest(AbstractJmsRequest request, ReplyListener listener) throws JMSException {

    synchronized (this) {
      if (status == Status.CLOSE)
        throw new IllegalStateException("Connection closed");

      if (requestCounter == Integer.MAX_VALUE) {
        requestCounter = 0;
      }

      request.setRequestId(requestCounter++);

      if (listener != null) {
        requestsTable.put(new Integer(request.getRequestId()), listener);
      }

      if (heartBeatTask != null) {
        lastRequestDate = System.currentTimeMillis();
      }
    }

    try {
      channel.send(request);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      JMSException jmsExc = new JMSException(exc.toString());
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }
  }
  
  public void setMultiThreadSync(int delay, int threshold) {
    channel = new MultiThreadSyncChannel(channel, delay, threshold);
  }

  /**
   * Not synchronized because it would possibly
   * deadlock with some reply listeners 
   * (actually requestors).
   */
  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.close()");
    
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

    if (heartBeatTask != null) heartBeatTask.cancel();
    if (timer != null) timer.cancel();
    channel.close();
    demtpx.stop();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> requestsTable=" + requestsTable);
    
    // The requests table can't be accessed
    // either by an external thread (status CLOSE)
    // or by the internal demultiplexer thread (stopped).
    
    cleanup();
  }

  /**
   * Used by:
   * 1- close()
   * 2- the connector layer (OutboundConnection.cleanup())
   */
  public void cleanup() {
    // Create first a copy of the current keys
    // registered into the requests table.
    Integer[] requestIds;
    synchronized (requestsTable) {
      Set keySet = requestsTable.keySet();
      requestIds = new Integer[keySet.size()];
      keySet.toArray(requestIds);
    }
    for (int i = 0; i < requestIds.length; i++) {
      ReplyListener rl = (ReplyListener) requestsTable.get(requestIds[i]);
      // The listener may be null because the table
      // may have been modified meanwhile.
      if (rl != null) {
        rl.replyAborted(requestIds[i].intValue());
      }
    }
    requestsTable.clear();
  }
  
  public void replyAllError(MomExceptionReply exc) {
    // Create first a copy of the current keys
    // registered into the requests table.
    Integer[] requestIds;
    synchronized (requestsTable) {
      Set keySet = requestsTable.keySet();
      requestIds = new Integer[keySet.size()];
      keySet.toArray(requestIds);
    }
    for (int i = 0; i < requestIds.length; i++) {
      ReplyListener rl = (ReplyListener) requestsTable.get(requestIds[i]);
      // The listener may be null because the table
      // may have been modified meanwhile.
      if (rl != null) {
      	rl.errorReceived(requestIds[i].intValue(), exc);
      }
    }
    requestsTable.clear();
  }

  /**
   * Not synchronized because it would possibly
   * deadlock with some reply listeners 
   * (actually requestors).
   */
  public void abortRequest(int requestId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.abortRequest(" + requestId + ')');
    
    ReplyListener rl = doAbortRequest(requestId);    
    if (rl != null) {
      rl.replyAborted(requestId);
    }
  }
  
  private synchronized ReplyListener doAbortRequest(int requestId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.doAbortRequest(" + requestId + ')');
    
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.route(" + reply + ')');
    
    int requestId = reply.getCorrelationId();
    Integer requestKey = new Integer(requestId);
    ReplyListener rl = (ReplyListener)requestsTable.get(requestKey);
    if (reply instanceof MomExceptionReply) {
      MomExceptionReply excReply = (MomExceptionReply) reply;
      if (rl instanceof ErrorListener) {
        ((ErrorListener) rl).errorReceived(requestId, excReply);
      } else {
        // The listener is null or doesn't implement ErrorListener
        if (exceptionListener != null) {
          exceptionListener.onException(buildJmsException(excReply));
        }
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, " -> rl = " + rl + ')');
      if (rl != null) {
        try {
          if (rl.replyReceived(reply)) {
            requestsTable.remove(requestKey);
          }
        } catch (AbortedRequestException exc) {
          logger.log(BasicLevel.WARN, " -> Request aborted: " + requestId);
          abortReply(reply);
        }
      } else {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, " -> Listener not found for the reply: " + requestId);
        abortReply(reply);
      }
    }
  }

  private void abortReply(AbstractJmsReply reply) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.abortReply(" + reply + ')');
    
    if (reply instanceof ConsumerMessages) {
      deny((ConsumerMessages)reply);
    }
    // Else nothing to do.
  }

  public void deny(ConsumerMessages messages) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.deny(" + messages + ')');

    Vector msgList = messages.getMessages();
    Vector ids = new Vector();
    for (int i = 0; i < msgList.size(); i++) {
      ids.addElement(((org.objectweb.joram.shared.messages.Message) msgList.elementAt(i)).id);
    }
    SessDenyRequest deny = new SessDenyRequest(messages.comesFrom(),
                                               ids,
                                               messages.getQueueMode());
    try {
      sendRequest(deny);
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      // Connection failure
      // Nothing to do
    }
  }

  class onExceptionRunner implements Runnable {
    Exception exc;

    onExceptionRunner(Exception exc) {
      this.exc = exc;
    }

    public void run() {
      onException(exc);
    }
  }

  private void onException(Exception exc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "RequestMultiplexer.onException(" + exc + ')');
    JMSException jmsExc;
    if (exc instanceof JMSException) {
      jmsExc = (JMSException) exc;
    } else {
      jmsExc = new IllegalStateException(exc.getMessage());
    }
    if (exceptionListener != null)
      exceptionListener.onException(jmsExc);
  }

  public void schedule(TimerTask task, long period) {
    if (timer != null) {
      try {
        timer.schedule(task, period);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "", exc);
      }
    }
  }

  public void setDemultiplexerDaemonName(String name) {
    demtpx.setName(name);
  }
  
  public String getDemultiplexerDaemonName() {
    return demtpx.getName();
  }

  private class DemultiplexerDaemon extends fr.dyade.aaa.common.Daemon {
    DemultiplexerDaemon() {
      // The real name is set later when
      // the proxy id and connection id are known
      // see setDemultiplexerDaemonName()
      super("Connection#?", logger);
    }

    public void run() {
      try {
        loop:
        while (running) {
          canStop = true;
          AbstractJmsReply reply;
          try {
            reply = channel.receive();
          } catch (Exception exc) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "Exception during receive", exc);
            // Check if the connection is not already
            // closed (the exception may occur as a consequence
            // of a closure or at the same time as an independant
            // close call).
            if (! isClosed()) {
            	replyAllError(new MomExceptionReply(new MomException(exc.getMessage())));
            	
            	RequestMultiplexer.this.close();
            	
              // The connection close() must be
              // called by another thread. Calling it with
              // this thread (demultiplexer daemon) could
              // lead to a deadlock if another thread called
              // close() just before.
              Closer closer = new Closer(exc);
              new Thread(closer).start();
            } else {
              // Else it means that the connection is already closed
              // Runs the onException in a separate thread in order to avoid
              // deadlock in connector onException (synchronized).
              onExceptionRunner oer = new onExceptionRunner(exc);
              new Thread(oer).start();
            }
            
            break loop;
          }
          canStop = false; 
          route(reply);
          if (!running && isClosed()) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "DemultiplexerDaemon ended and Socket closed.");
            onExceptionRunner oer = new onExceptionRunner(new Exception("DemultiplexerDaemon ended and Socket closed."));
            new Thread(oer).start();
          }
        }
      } finally {
        finish();
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
  
  private class Closer implements Runnable {
    private Exception exc;
    
    Closer(Exception e) {
      exc = e;
    }
    
    public void run() {
      try {
        RequestMultiplexer.this.cnx.close();
      } catch (JMSException exc2) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Error during close", exc2);
      }
      onException(exc);
    }
  }

  /**
   * Timer task responsible for sending a ping message
   * to the server if no request has been sent during 
   * the specified timeout ('cnxPendingTimer' from the
   * factory parameters).
   */
  private class HeartBeatTask extends TimerTask {    

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
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }

    public void start() throws Exception {
      if (timer != null)
        timer.schedule(this, heartBeat, heartBeat);
    }
  }

}
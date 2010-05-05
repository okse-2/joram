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
 */
package org.objectweb.kjoram;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class RequestMultiplexer extends Daemon {

  private static final Logger logger = Debug.getLogger(RequestMultiplexer.class.getName());
  
  private static class Status {
    public static final int OPEN = 0;
    public static final int CLOSE = 1;
    
    private static final String[] names = {"OPEN", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  private Connection cnx;

  private volatile int status;

  private Channel channel;

  public Hashtable requestsTable;

  private int requestCounter;

 // private DemultiplexerDaemon demtpx;
 
  private ExceptionListener exceptionListener;

//  /**
//   * The date of the last request
//   */
//  private volatile long lastRequestDate;
  
  
  public void run() {
    try {
      AbstractReply reply = null;

      while (running) {
        canStop = true;
        try {
          reply = channel.receive();
        } catch (IOException exc) {
          logger.log(BasicLevel.ERROR, "EXCPTION:: run ", exc);
          // Check if the connection is not already
          // closed (the exception may occur as a consequence
          // of a closure or at the same time as an independent
          // close call).
          if (! isClosed()) {
            close();
          } else {
            // Else it means that the connection is already closed
            if (exceptionListener != null)
              exceptionListener.onException(new ConnectException());
          }
          break;
        }
        canStop = false; 
        route(reply);
      }
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,"RequestMultiplexer run - Exit on Exception :", e);
    }
    finish();
  }

  public RequestMultiplexer(Connection cnx, Channel channel) 
  throws JoramException {
    super("RequestMultiplexer_" + cnx.toString());
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,"RequestMultiplexer()"); 
    this.channel = channel;
    this.cnx = cnx;
    exceptionListener = null;
    requestsTable = new Hashtable();
    requestCounter = 0;
    try {
      channel.connect();
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: RequestMultiplexer()", e);
      throw new JoramException(e.getMessage());
    }

    status = Status.OPEN;

    start();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "RequestMultiplexer(): requestsTable = " + requestsTable);
  }

  public boolean isClosed() {
    return (status == Status.CLOSE);
  }

  public void setExceptionListener(ExceptionListener exceptionListener) {
    this.exceptionListener = exceptionListener;
  }

  public ExceptionListener getExceptionListener() {
    return exceptionListener;
  }

  public void sendRequest(AbstractRequest request) throws JoramException {
    sendRequest(request, null);
  }
    
  public synchronized void sendRequest(AbstractRequest request, ReplyListener listener) 
  throws JoramException {
    if (status == Status.CLOSE) {
      throw new IllegalStateException();
    }

    if (requestCounter == Integer.MAX_VALUE)
      requestCounter = 0;
    else
      requestCounter += 1;
    request.setRequestId(requestCounter);
    
    if (listener != null)
      requestsTable.put(new Integer(requestCounter), listener);

    try {
    channel.send(request);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: sendRequest request = " + request, e);
      throw new JoramException(e.getMessage());
    }
  }

  public synchronized void close() {
    if (status == Status.CLOSE) return;
    status = Status.CLOSE;

    Enumeration e = requestsTable.keys();
    while (e.hasMoreElements()) {
      ReplyListener rl = (ReplyListener) requestsTable.get(e.nextElement());
      rl.replyAborted();
    }
    requestsTable.clear();

    try {
      channel.close();
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: RequestMultiplexer.close() channel.close()", exc);
    }
    stop();

  /*   void stop() { */
  /*     if (isCurrentThread()) { */
  /*       finish(); */
  /*     } else { */
  /*       super.stop(); */
  /*     } */
  /*   } */
  }

  /**
   * Not synchronized because it may be called by the
   * demultiplexer during a concurrent close. It would deadlock
   * as the close waits for the demultiplexer to stop.
   */
  public synchronized void route(AbstractReply reply) {
    int requestId = reply.getCorrelationId();
    
    if (reply instanceof MomExceptionReply) {
      if (exceptionListener != null) {
        MomExceptionReply excReply = (MomExceptionReply) reply;
        int type = excReply.getType();
        String msg = excReply.getMessage();
        if (type == MomExceptionReply.AccessException) {
          exceptionListener.onException(new SecurityException(msg));
        } else if (type == MomExceptionReply.DestinationException) {
          exceptionListener.onException(new InvalidDestinationException(msg));
        } else {
          exceptionListener.onException(new MOMException(msg));
        }
      }
    } else {
      ReplyListener rl = (ReplyListener) requestsTable.remove(new Integer(requestId));
      if (rl != null) {
        try {
          rl.replyReceived(reply);
        } catch (AbortedRequestException e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "route AbortedRequestException", e);
        }
      } else {
        if (reply instanceof ConsumerMessages) {
          deny((ConsumerMessages) reply);
        }
      }
    }
  }

  public synchronized void abortRequest(int requestId) {
    if (status == Status.CLOSE) return;

    ReplyListener rl = (ReplyListener) requestsTable.remove(new Integer(requestId));

    if (rl != null) 
      rl.replyAborted();
  }

  public void deny(ConsumerMessages messages) {
    Vector msgList = messages.getMessages();
    Vector ids = new Vector();
    for (int i=0; i < msgList.size(); i++) {
      ids.addElement(((Message) msgList.elementAt(i)).getMessageID());
    }
    SessDenyRequest deny = new SessDenyRequest(messages.comesFrom(),
                                               ids,
                                               messages.getQueueMode());
    try {
      sendRequest(deny);
    } catch (Exception exc) {
      // Connection failure, nothing to do
    }

    if (messages != null) {
      messages = null;
    }
  }

  protected void shutdown() {
    // TODO Auto-generated method stub
    
  }
}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): David Feliot (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.MomTracing;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * This class enables a thread to communicate with a
 * user proxy.
 *
 * @see ConnectionManager
 * @see UserAgent
 */
public class UserConnection {

  /**
   * Connection status before it is started.
   */
  public static final int INIT = 0;

  /**
   * Connection status once it is started.
   */
  public static final int RUN = 1;

  /**
   * Connection status once it is closed.
   */
  public static final int CLOSE = 2;

  public static final String[] statusNames = {
    "INIT", "RUN", "CLOSE"};

  /**
   * Name of the user
   */
  private String userName;

  /**
   * User's password
   */
  private String userPassword;

  /**
   * Key of the connection
   * (unique for a user)
   */
  private int key;

  /**
   * Reference to the user's proxy
   */
  private UserAgent user;

  /**
   * Status of the connection
   */
  private int status;

  /**
   * The queue where the replies are posted
   */
  private fr.dyade.aaa.util.Queue replyQueue;

  /**
   * The date of the last request
   */
  private long lastRequestDate;

  /**
   * Disconnection timeout
   */
  private int timeout;

  private GarbageTask garbageTask;

  private Object context;

  /**
   * Creates a new user connection from the
   * name of the user, its password and the
   * disconnection timeout.
   *
   * @param userName name of the user
   * @param userPassword user's password
   * @param timeout disconnection timeout
   */
  public UserConnection(String userName,
                        String userPassword,
                        int timeout,
                        Object context) {
    this.userName = userName;
    this.userPassword = userPassword;
    this.timeout = timeout * 2;
    this.context = context;
    replyQueue =
      new fr.dyade.aaa.util.Queue();
    key = -1;
    setStatus(INIT);
    lastRequestDate = System.currentTimeMillis();    
  }

  public final Object getContext() {
    return context;
  }

  /**
   * Returns the name of the user
   */
  public final String getUserName() {
    return userName;
  }

  /**
   * Returns the password of the user
   */
  public final String getUserPassword() {
    return userPassword;
  }

  /**
   * Returns the key of the connection
   */
  public final int getKey() {
    return key;
  }

  /**
   * Returns the reference to the user's proxy
   */
  public final UserAgent getUser() {
    return user;
  }

  public final int getTimeout() {
    return timeout;
  }

  /**
   * Sends a request to the user
   *
   * @param request the request to send to the user
   *
   * @exception IllegalStateException if the connection status
   * is not <code>RUN</code>.
   */
  public synchronized void send(AbstractJmsRequest request) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + userName + ',' + key + "].send(" + request + ')');
    if (status == RUN) {
      lastRequestDate = System.currentTimeMillis();
      if (request instanceof PingRequest) {
        // Do nothing
      } else if (request instanceof CnxCloseRequest) {
        doReact((CnxCloseRequest)request);        
      } else {
        user.invoke(key, request);
      }
    } else throw new Exception(
      "Connection " + userName + ':' + key + " closed");
  }

  /**
   * Receives a reply from the user. Blocks until
   * a reply arrives.
   *
   * @return the reply from the user
   */
  public AbstractJmsReply receive()
    throws InterruptedException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + userName + ',' + key + "].receive()");
    AbstractJmsReply reply = 
      (AbstractJmsReply)replyQueue.get();
    replyQueue.pop();
    return reply;
  }

  private void doReact(CnxCloseRequest request) {
    close();
    CnxCloseReply reply = new CnxCloseReply();
    reply.setCorrelationId(request.getRequestId());
    replyQueue.push(reply);
  }

  /**
   * Closes the connection (idempotent).
   */
  public synchronized void close() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + userName + ',' + key + "].close()");
    if (status == RUN) {
      user.invoke(key, new CnxCloseRequest());
      user.closeConnection(key);
      if (garbageTask != null)
        garbageTask.cancel();
      setStatus(CLOSE);
      ConnectionManager.removeConnection(this);
    } else if (MomTracing.dbgProxy.isLoggable(
      BasicLevel.DEBUG)) {
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        " -> already closed");
    }
  }

  /**
   * Push a reply into the replies queue.
   *
   * @param reply the reply to push
   */
  void pushReply(AbstractJmsReply reply) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + userName + ',' + key + "].pushReply(" + reply + ')');
    if (status == RUN) {      
      replyQueue.push(reply);
    } else throw new IllegalStateException();
  }

  /**
   * Starts the connection
   */
  void start(UserAgent user, int key) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + this.key + "].start(" + 
        user + ',' + key + ')');
    this.user = user;
    this.key = key;
    setStatus(RUN);
    if (timeout > 0) {
      garbageTask = new GarbageTask();
      garbageTask.start();      
    }
  }

  private void setStatus(int status) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "UserConnection[" + userName + ',' + key + "].setStatus(" + 
        statusNames[status] + ')');
    this.status = status;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",userName=" + userName + 
      ",userPassword=" + userPassword + 
      ",key=" + key +
      ",status=" + statusNames[status] + 
      ",timeout=" + timeout + ')';
  }

  /**
   * Timer task responsible for garbaging the connection if 
   * it has not sent any requests for the duration 'timeout'.
   */
  class GarbageTask extends fr.dyade.aaa.util.TimerTask {    
    public void run() {
      long date = System.currentTimeMillis();
      if ((date - lastRequestDate) > timeout) {
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(
            BasicLevel.DEBUG,
            " -> garbage connection " + UserConnection.this);
        close();
      } else {
        start();
      }
    }

    public void start() {
      try {
        ConnectionManager.getTimer().schedule(
          this, timeout);
      } catch (Exception exc) {
        throw new Error(exc.toString());
      }
    }
  }
}

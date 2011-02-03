/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.jms.*;
import fr.dyade.aaa.util.*;

import java.util.*;

import javax.jms.InvalidDestinationException;
import javax.jms.IllegalStateException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * Implements the <code>javax.jms.Connection</code> interface.
 */
public class Connection implements javax.jms.Connection
{ 
  /** Actual connection linking the client and the JORAM platform. */
  private ConnectionItf connectionImpl;
  
  /** Client's agent proxy identifier. */
  private String proxyId;
  /** Connection key. */
  private int key;

  /** Connection meta data. */
  private ConnectionMetaData metaData = null;
  /** The connection's exception listener, if any. */
  private javax.jms.ExceptionListener excListener = null;

  /** Requests counter. */
  private long requestsC = 0;
  /** Sessions counter. */
  private long sessionsC = 0;
  /** Messages counter. */
  private long messagesC = 0;
  /** Subscriptions counter. */
  private long subsC = 0;

  /** Timer for closing pending sessions. */
  private fr.dyade.aaa.util.Timer sessionsTimer = null;

  /** The factory's parameters. */
  FactoryParameters factoryParameters;

  /** Driver listening to asynchronous deliveries. */
  Driver driver;

  /** <code>true</code> if the connection is started. */
  boolean started = false;
  /** <code>true</code> if the connection is closing. */
  boolean closing = false;
  /** <code>true</code> if the connection is closed. */
  boolean closed = false;
  /** Vector of the connection's sessions. */
  Vector sessions;
  /** Vector of the connection's consumers. */
  Vector cconsumers;
  /** 
   * Table holding requests related objects, either locks of synchronous
   * requests, or asynchronous consumers.
   * <p>
   * <b>Key:</b> request's identifier<br>
   * <b>Object:</b> request related object
   */
  Hashtable requestsTable;
  /**
   * Table holding the server replies to synchronous requests.
   * <p>
   * <b>Key:</b> reply's identifier<br>
   * <b>Object:</b> reply object
   */
  Hashtable repliesTable;


  /**
   * Creates a <code>Connection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param connectionImpl  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection(FactoryParameters factoryParameters,
                    ConnectionItf connectionImpl) throws JMSException
  {
    try {
      this.factoryParameters = factoryParameters;

      sessions = new Vector();
      requestsTable = new Hashtable();
      repliesTable = new Hashtable();
    
      this.connectionImpl = connectionImpl;

      // Creating and starting the connection's driver:
      driver = connectionImpl.createDriver(this);
      driver.start();
  
      // Requesting the connection key and proxy identifier:
      CnxConnectRequest req = new CnxConnectRequest();
      CnxConnectReply rep = (CnxConnectReply) syncRequest(req);
      proxyId = rep.getProxyId();
      key = rep.getKey();

      // Transactions will be scheduled; creating a timer.
      if (factoryParameters.txPendingTimer != 0)
        sessionsTimer = new fr.dyade.aaa.util.Timer();

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": opened."); 
    }
    // Connection could not be established:
    catch (JMSException jE) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, jE);
      throw jE;
    }
  }

  /** String image of the connection. */
  public String toString()
  {
    return "Cnx:" + proxyId + "-" + key;
  }

  /**
   * Specializes this Object method; returns <code>true</code> if the
   * parameter is a <code>Connection</code> instance sharing the same
   * proxy identifier and connection key.
   */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof Connection))
      return false;

    Connection cnx = (Connection) obj;

    return toString().equals(cnx.toString());
  }

  /** Returns the connection's key. */
  public int getKey()
  {
    return key;
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
         createConnectionConsumer(javax.jms.Destination dest, String selector,
                                  javax.jms.ServerSessionPool sessionPool,
                                  int maxMessages) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new ConnectionConsumer(this, (Destination) dest, selector,
                                  sessionPool, maxMessages);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
         createDurableConnectionConsumer(javax.jms.Topic topic, String subName,
                                         String selector,
                                         javax.jms.ServerSessionPool sessPool,
                                         int maxMessages) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new ConnectionConsumer(this, (Topic) topic, subName, selector,
                                  sessPool, maxMessages);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.Session
         createSession(boolean transacted, int acknowledgeMode)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new Session(this, transacted, acknowledgeMode);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public void setExceptionListener(javax.jms.ExceptionListener listener)
              throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    this.excListener = listener;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.ExceptionListener getExceptionListener() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    return excListener;
  }

  /**
   * Passes an asynchronous exception to the exception listener, if any.
   *
   * @param jE  The asynchronous JMSException.
   */
  synchronized void onException(JMSException jE)
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
      JoramTracing.dbgClient.log(BasicLevel.WARN, this + ": " + jE);

    if (excListener != null)
      excListener.onException(jE);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void setClientID(String clientID) throws JMSException
  {
    throw new IllegalStateException("ClientID is already set by the"
                                    + " provider.");
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public String getClientID() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    return proxyId;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.ConnectionMetaData getMetaData() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    if (metaData == null)
      metaData = new ConnectionMetaData();
    return metaData;
  }

  /**
   * API method for starting the connection.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public void start() throws JMSException
  {
    // If closed, throwing an exception:
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    // Ignoring the call if the connection is started:
    if (started)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": starting..."); 

    // Starting the sessions:
    Session session;
    for (int i = 0; i < sessions.size(); i++) {
      session = (Session) sessions.get(i);
      session.repliesIn.start();
      session.start();
    }
    // Sending a start request to the server:
    asyncRequest(new CnxStartRequest());

    started = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": started."); 
  }

  /**
   * API method for stopping the connection; even if the connection appears
   * to be broken, stops the sessions.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public void stop() throws JMSException
  {
    IllegalStateException isE = null;

    // If closed, throwing an exception:
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    // Ignoring the call if the connection is already stopped:
    if (! started)
      return;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": stopping..."); 

    // Sending a synchronous "stop" request to the server:
    try {
      syncRequest(new CnxStopRequest());
    }
    // Catching an IllegalStateException if the connection is broken:
    catch (IllegalStateException caughtISE) {
      isE = caughtISE;
    }

    // At this point, the server won't deliver messages anymore,
    // the connection just waits for the sessions to have finished their
    // processings.
    Session session;
    for (int i = 0; i < sessions.size(); i++) {
      session = (Session) sessions.get(i);
      try {
        session.repliesIn.stop();
      }
      catch (InterruptedException iE) {}
      session.stop();
    }

    started = false;

    if (isE != null) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, isE);
      throw isE;
    }

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": is stopped."); 
  }


  /**
   * API method for closing the connection; even if the connection appears
   * to be broken, closes the sessions.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void close() throws JMSException
  {
    // Ignoring the call if the connection is closed:
    if (closed)
      return;

    closing = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this 
                                 + ": closing...");

    // Finishing the timer, if any:
    if (sessionsTimer != null)
      sessionsTimer.cancel();

    // Stopping the connection:
    try {
      stop();
    }
    // Catching a JMSException if the connection is broken:
    catch (JMSException jE) {}

    // Closing the sessions:
    Session session;
    while (! sessions.isEmpty()) {
      session = (Session) sessions.elementAt(0);
      try {
        session.close();
      }
      // Catching a JMSException if the connection is broken:
      catch (JMSException jE) {}
    }

    // Closing the connection consumers:
    if (cconsumers != null) {
      ConnectionConsumer cc;
      while (! cconsumers.isEmpty()) {
        cc = (ConnectionConsumer) cconsumers.elementAt(0);
        cc.close();
      }
    }
    
    // Closing the connection:
    connectionImpl.close();

    // Shutting down the driver, if needed:
    if (! driver.stopping)
      driver.stop();

    requestsTable.clear();
    requestsTable = null;
    repliesTable.clear();
    repliesTable = null;

    closed = true;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed.");
  }

  /** Returns a new request identifier. */
  synchronized String nextRequestId()
  {
    if (requestsC == Long.MAX_VALUE)
      requestsC = 0;
    requestsC++;
    return "c" + key + "r" + requestsC;
  }

  /** Returns a new session identifier. */
  synchronized String nextSessionId()
  {
    if (sessionsC == Long.MAX_VALUE)
      sessionsC = 0;
    sessionsC++;
    return "c" + key + "s" + sessionsC;
  }
 
  /** Returns a new message identifier. */
  synchronized String nextMessageId()
  {
    if (messagesC == Long.MAX_VALUE)
      messagesC = 0;
    messagesC++;
    return "ID:" + proxyId + "c" + key + "m" + messagesC;
  }

  /** Returns a new subscription name. */
  synchronized String nextSubName()
  {
    if (subsC == Long.MAX_VALUE)
      subsC = 0;
    subsC++;
    return "c"  + key + "sub" + subsC;
  }

  /** Schedules a session task to the connection's timer. */
  synchronized void schedule(fr.dyade.aaa.util.TimerTask task)
  {
    if (sessionsTimer == null)
      return;

    try {
      sessionsTimer.schedule(task, factoryParameters.txPendingTimer * 1000);
    }
    catch (Exception exc) {}
  }
  
  /**
   * Method sending a synchronous request to the server and waiting for an
   * answer.
   *
   * @return The server reply.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   * @exception JMSSecurityException  When sending a request to a destination
   *              not accessible because of security.
   * @exception InvalidDestinationException  When sending a request to a
   *              destination that no longer exists.
   * @exception JMSException  If the request failed for any other reason.
   */
  AbstractJmsReply syncRequest(AbstractJmsRequest request) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    if (request.getRequestId() == null)
      request.setRequestId(nextRequestId());

    String requestId = request.getRequestId();

    try {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": sends request: "
                                   + request.getClass().getName()
                                   + " with id: " + requestId);

      Lock lock = new Lock();  
      requestsTable.put(requestId, lock);
      synchronized(lock) {
        connectionImpl.send(request);
        while (true) {
          try {
            lock.wait();
            break;
          }
          catch (InterruptedException iE) {
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
              JoramTracing.dbgClient.log(BasicLevel.WARN,
                                         this
                                         + ": caught InterruptedException");
            continue;
          }
        }
        requestsTable.remove(requestId);
      }
    }
    // Catching an exception because of...
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof JMSException)
        throw (JMSException) e;
      else
        jE = new JMSException("Exception while getting a reply.");

      jE.setLinkedException(e);

      // Unregistering the request:
      if (requestsTable != null && requestId != null)
        requestsTable.remove(requestId);

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, jE);
      throw jE;
    }

    // Finally, returning the reply:
    AbstractJmsReply reply = (AbstractJmsReply) repliesTable.remove(requestId);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": got reply.");

    // If the reply is null, it means that the requester has been unlocked
    // by the driver, because it detected a connection failure:
    if (reply == null)
      throw new IllegalStateException("Connection is broken.");
    // Else, if the reply notifies of an error: throwing the appropriate exc: 
    else if (reply instanceof MomExceptionReply) {
      MomException mE = ((MomExceptionReply) reply).getException();

      if (mE instanceof AccessException)
        throw new JMSSecurityException(mE.getMessage());
      else if (mE instanceof DestinationException)
        throw new InvalidDestinationException(mE.getMessage());
      else
        throw new JMSException(mE.getMessage());
    }
    // Else: returning the reply:
    else
      return reply;
  }

  /**
   * Actually sends an asynchronous request to the server.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  void asyncRequest(AbstractJmsRequest request) throws IllegalStateException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    if (request.getRequestId() == null)
      request.setRequestId(nextRequestId());

    try {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": sends request: "
                                   + request.getClass().getName()
                                   + " with id: " + request.getRequestId());
      connectionImpl.send(request);
    }
    // In the case of a broken connection:
    catch (IllegalStateException exc) {
      // Removes the potentially stored requester:
      if (request.getRequestId() != null)
        requestsTable.remove(request.getRequestId());

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, exc);
      throw exc;
    }
  }

  /**
   * Method called by the driver for distributing the server replies
   * it gets on the connection.
   * <p>
   * Server replies are either synchronous replies to client requests,
   * or asynchronous message deliveries, or asynchronous exceptions
   * notifications.
   */
  void distribute(AbstractJmsReply reply)
  {
    // Getting the correlation identifier:
    String correlationId = reply.getCorrelationId();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": got reply: "
                                   + correlationId);

    Object obj = null;
    if (correlationId != null)
      obj = requestsTable.get(correlationId);

    // If the request is a synchronous request, putting the reply in the
    // replies table and unlocking the requester:
    if (obj instanceof Lock) {
      repliesTable.put(correlationId, reply);

      synchronized(obj) {
        obj.notify();
      }
    }
    // If the reply is an asynchronous exception, passing it:
    else if (reply instanceof MomExceptionReply) {
      // Removing the potential consumer object from the table:
      if (correlationId != null)
        requestsTable.remove(correlationId);

      MomException mE = ((MomExceptionReply) reply).getException();
      JMSException jE = null;

      if (mE instanceof AccessException)
        jE = new JMSSecurityException(mE.getMessage());
      else if (mE instanceof DestinationException)
        jE = new InvalidDestinationException(mE.getMessage());
      else
        jE = new JMSException(mE.getMessage());

      onException(jE);
    }
    // Else, if the reply is an asynchronous delivery:
    else if (obj != null) {
      try {
        // Passing the reply to its consumer:
        if (obj instanceof ConnectionConsumer)
          ((ConnectionConsumer) obj).repliesIn.push(reply);
        else if (obj instanceof MessageConsumer)
          ((MessageConsumer) obj).sess.repliesIn.push(reply);
      }
      catch (StoppedQueueException sqE) {
        denyDelivery((ConsumerMessages) reply);
      }
    }
    // Finally, if the requester disappeared, denying the delivery:
    else if (reply instanceof ConsumerMessages)
      denyDelivery((ConsumerMessages) reply);
  }

  /** Actually denies a non deliverable delivery. */
  private void denyDelivery(ConsumerMessages delivery)
  {
    Vector msgs = delivery.getMessages();
    fr.dyade.aaa.mom.messages.Message msg;

    if (msgs.isEmpty())
      return;

    Vector ids = new Vector();
    while (! msgs.isEmpty()) {
      msg = (fr.dyade.aaa.mom.messages.Message) msgs.remove(0);
      ids.addElement(msg.getIdentifier());
    }

    try {
      // Sending the denying as an asynchronous request, as no synchronous
      // behaviour is expected here:
      asyncRequest(new SessDenyRequest(delivery.comesFrom(), ids,
                                       delivery.getQueueMode(), true));
    }
    // If sthg goes wrong while denying, nothing more can be done!
    catch (JMSException jE) {}
  }
}
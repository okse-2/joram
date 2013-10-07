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

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;


/**
 * Implements the <code>javax.jms.Connection</code> interface.
 */
public class Connection {
  public static Logger logger = Debug.getLogger(Connection.class.getName());
  
  /**
   * Status of the connection.
   */
  private static class Status {
    /**
     * Status of the connection when it is stopped.
     * This is the initial status.
     */
    public static final int STOP = 0;

    /**
     * Status of the connection when it is started.
     */
    public static final int START = 1;

    /**
     * Status of the connection when it is closed.
     */
    public static final int CLOSE = 2;

    private static final String[] names = {
      "STOP", "START", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }

  /**
   * The request multiplexer used to communicate
   * with the user proxy.
   */
  private RequestMultiplexer mtpx;

  /**
   * The requestor used to communicate
   * with the user proxy.
   */
  private Requestor requestor;

  
  class AtomicCounter {
    long value;
    StringBuffer strbuf;
    int initial;

    AtomicCounter(String prefix) {
      value = 0;
      strbuf = new StringBuffer(prefix.length() + 20);
      strbuf.append(prefix);
      initial = strbuf.length();
    }

    synchronized String nextValue() {
      strbuf.setLength(initial);
      strbuf.append(value++);
      return strbuf.toString();
    }
  }

  /** Sessions counter. */
  private AtomicCounter sessionsC;

  /** Messages counter. */
  private AtomicCounter messagesC;

  /** Subscriptions counter. */
  private AtomicCounter subsC;

  /** Client's agent proxy identifier. */
  String proxyId;

  /** Connection key. */
  private int key;

  /**
   * Status of the connection.
   * STOP, START, CLOSE
   */
  private int status;

  /** Vector of the connection's sessions. */
  private Vector sessions;

//  /** Vector of the connection's consumers. */
//  private Vector consumers;

  /**
   * Used to synchronize the method close()
   */
  private Closer closer;

  private String stringImage = null;
  private int hashCode;
  
  boolean isStopped() {
    return (status == Status.STOP);
  }

  /**
   * API method for starting the connection.
   *
   * @exception JoramStateException  If the connection is closed or broken.
   */
  public synchronized void start() throws JoramException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
          BasicLevel.DEBUG, 
          newTrace(".start()")); 
    checkClosed();

    // Ignoring the call if the connection is started:
    if (status == Status.START)
      return;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this
          + ": starting..."); 

    // Starting the sessions:

    for (int i = 0; i < sessions.size(); i++) {
      Session session = (Session) sessions.elementAt(i);
      session.start();
    }

    // Sending a start request to the server:
    mtpx.sendRequest(new CnxStartRequest());

    setStatus(Status.START);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public synchronized Session
      createSession(boolean transacted, 
                    int acknowledgeMode)
    throws JoramException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 newTrace(".createSession(" + transacted + ',' +  acknowledgeMode + ')'));
    checkClosed();
    Session session = new Session(
      this,
      transacted, 
      acknowledgeMode, 
      mtpx);
    addSession(session);
    return session;
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public synchronized void
      setExceptionListener(ExceptionListener listener) throws JoramException {
    checkClosed();
    mtpx.setExceptionListener(listener);
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public ExceptionListener getExceptionListener() throws JoramException {
    checkClosed();
    return mtpx.getExceptionListener();
  }
  

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public String getClientID() throws JoramException {
    checkClosed();
    return proxyId;
  }
  


  /**
   * API method for stopping the connection; even if the connection appears
   * to be broken, stops the sessions.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public void stop() throws JoramException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".stop()"));
    checkClosed();

    synchronized (this) {
      if (status == Status.STOP)
        return;
    }
    
    // At this point, the server won't deliver messages anymore,
    // the connection just waits for the sessions to have finished their
    // processings.
    // Must go out of the synchronized block in order to enable
    // the message listeners to use the connection.
    // As a csq, the connection stop is reentrant. Several 
    // threads can enter this method during the stopping stage.
    for (int i = 0; i < sessions.size(); i++) {
      Session session = (Session) sessions.elementAt(i);
      session.stop();
    }
    
    synchronized (this) {
      if (status == Status.STOP)
        return;

      // Sending a synchronous "stop" request to the server:
      requestor.request(new CnxStopRequest());

      // Set the status as STOP as the following operations
      // (Session.stop) can't fail.
      setStatus(Status.STOP);
    }
  }

  /**
   * API method for closing the connection; even if the connection appears
   * to be broken, closes the sessions.
   *
   * @exception JoramException  Actually never thrown.
   */
  public void close() throws JoramException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(
        BasicLevel.DEBUG, 
        newTrace(".close()"));

    closer.close();
  }
  
  /** Returns a new session identifier. */
  String nextSessionId() {
    return sessionsC.nextValue();
  }
  
  /** Returns a new message identifier. */
  String nextMessageId() {
    return messagesC.nextValue();
  }

  /** Returns a new subscription name. */
  String nextSubName() {
    return subsC.nextValue();
  }
  
  /**
   * Creates a <code>Connection</code> instance.
   *
   * @param connectionImpl  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection(Channel requestChannel) 
    throws JoramException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Connection.<init>(" + requestChannel + ')');
    mtpx = new RequestMultiplexer(this, requestChannel);
    
    requestor = new Requestor(mtpx);
    sessions = new Vector();
//    consumers = new Vector();
    
    closer = new Closer();
    
    setStatus(Status.STOP);

    // Requesting the connection key and proxy identifier:
    CnxConnectRequest req = new CnxConnectRequest();
    CnxConnectReply rep = (CnxConnectReply) requestor.request(req);

    proxyId = rep.getProxyId();
    key = rep.getCnxKey();

    sessionsC = new AtomicCounter("c" + key + 's');
    messagesC =
      new AtomicCounter("ID:" + proxyId.substring(1) + 'c' + key + 'm');
    subsC = new AtomicCounter("c"  + key + "sub");

    stringImage = "Cnx:" + proxyId + ':' + key;
    hashCode = stringImage.hashCode();

   // mtpx.setDemultiplexerDaemonName(toString());
  }

  public int hashCode() {
    return hashCode;
  }
  
  /**
   * Specializes this Object method; returns <code>true</code> if the
   * parameter is a <code>Connection</code> instance sharing the same
   * proxy identifier and connection key.
   */
  public boolean equals(Object obj) {
    return (obj instanceof Connection) && (hashCode() == obj.hashCode()) && toString().equals(obj.toString());
  }
 
  private final String newTrace(String trace) {
    return "Connection[" + proxyId + ':' + key + ']' + trace;
  }
  
  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 newTrace(".setStatus(" + Status.toString(status) + ')'));
    this.status = status;
  }
  
  /**
   * Checks if the connection is closed. If true raises an
   * IllegalStateException.
   */
  final protected synchronized void checkClosed() throws IllegalStateException {
    if (status == Status.CLOSE ||  mtpx.isClosed()) 
      throw new IllegalStateException("Forbidden call on a closed connection.");
  }
  

  /**
   * Called here and by sub-classes.
   */
  protected synchronized void addSession(Session session) {
    sessions.addElement(session);
    if (status == Status.START) {
      session.start();
    }
  }
  
  
  /**
   * This class synchronizes the close.
   * Close can't be synchronized with 'this' 
   * because the connection must be accessed
   * concurrently during its closure. So
   * we need a second lock.
   */
  class Closer {
    synchronized void close() {
      doClose();
    }
  }

  void doClose() {
    synchronized (this) {
      if (status == Status.CLOSE) {
        return;
      }
    }

    for (int i = 0; i < sessions.size(); i++) {
      Session session = (Session) sessions.elementAt(i);
      session.close();
    }
    
//    for (int i = 0; i < consumers.size(); i++) {
//      MultiSessionConsumer consumer = 
//        (MultiSessionConsumer) consumers.elementAt(i);
//      try {
//        consumer.close();
//      } catch (JoramException exc) {
//        if (logger.isLoggable(BasicLevel.DEBUG))
//          logger.log(
//            BasicLevel.DEBUG, "", exc);
//      }
//    }

    
    try {
      CnxCloseRequest closeReq = new CnxCloseRequest();
      requestor.request(closeReq);
    } catch (JoramException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(
          BasicLevel.DEBUG, "", exc);
    }
    
    mtpx.close();
    
    synchronized (this) {
      setStatus(Status.CLOSE);
    }
  }

}

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
 * Contributor(s): Abdenbi Benammour
 */
package org.objectweb.joram.client.jms;

import java.util.List;
import java.util.Vector;

import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.client.jms.connection.RequestChannel;
import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.client.jms.connection.Requestor;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseRequest;
import org.objectweb.joram.shared.client.CnxConnectReply;
import org.objectweb.joram.shared.client.CnxConnectRequest;
import org.objectweb.joram.shared.client.CnxStartRequest;
import org.objectweb.joram.shared.client.CnxStopRequest;
import org.objectweb.joram.shared.client.ConsumerSubRequest;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Implements the <code>javax.jms.Connection</code> interface.
 * <p>
 * A Connection object allows the client's active connection to the Joram
 * server. Connections support concurrent use, it serves several purposes:
 * <ul>
 * <li>It encapsulates the real connection with the Joram server (for example
 * an open TCP/IP socket between the client and the server).
 * <li>It needs the client authentication.
 * <li>It specifies a unique client identifier.
 * <li>It supports the ExceptionListener object. 
 * </ul>
 * A Joram client typically creates a connection, one or more sessions, and a
 * number of message producers and consumers. 
 * <br>
 * When a connection is created, it is in stopped mode that means that no
 * messages are being delivered. In order to minimize any client confusion
 * that may result from asynchronous message delivery during setup, it is
 * typical to leave the connection in stopped mode until setup is complete.
 * A message producer can send messages while a connection is stopped.
 */
public class Connection implements javax.jms.Connection, ConnectionMBean {
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

  /** Connection meta data. */
  private ConnectionMetaData metaData = null;

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
  private String proxyId;

  /** Connection key. */
  private int key;

  /** The factory's parameters. */
  private FactoryParameters factoryParameters;

  /**
   * Status of the connection.
   * STOP, START, CLOSE
   */
  private int status;

  /** Vector of the connection's sessions. */
  private Vector sessions;

  /** Vector of the connection's consumers. */
  private Vector cconsumers;

  /**
   * Used to synchronize the method close()
   */
  private Closer closer;

  private String stringImage = null;
  private int hashCode;
  
  private Identity identity = null;

  /**
   * Creates a <code>Connection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param requestChannel  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection() {
    // AF: This method shouldn't be public but it is actually used by AbstractFactory
    // in admin package (merge in future in ConnectionFactory class).
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Connection.<init>");
  }
  
  /**
   * Open the <code>Connection</code>.
   *
   * @param factoryParameters  The factory parameters.
   * @param requestChannel  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public void open(FactoryParameters factoryParameters,
                    RequestChannel requestChannel) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "Connection.open(" + factoryParameters + ',' + requestChannel + ')');
    // We need to clone the FactoryParameter Object to avoid side-effect with
    // external modifications.
    this.factoryParameters = (FactoryParameters) factoryParameters.clone();
    mtpx = new RequestMultiplexer(this,
                                  requestChannel,
                                  factoryParameters.cnxPendingTimer);
    if (factoryParameters.multiThreadSync) {
      mtpx.setMultiThreadSync(factoryParameters.multiThreadSyncDelay,
                              factoryParameters.multiThreadSyncThreshold);
    }
    
    requestor = new Requestor(mtpx);
    sessions = new Vector();
    cconsumers = new Vector();
    
    closer = new Closer();
    
    setStatus(Status.STOP);

    // Requesting the connection key and proxy identifier:
    CnxConnectRequest req = new CnxConnectRequest();
    CnxConnectReply rep = (CnxConnectReply) requestor.request(req);
    proxyId = rep.getProxyId();
    key = rep.getCnxKey();

    sessionsC = new AtomicCounter("c" + key + 's');
    messagesC = new AtomicCounter("ID:" + proxyId.substring(1) + 'c' + key + 'm');
    subsC = new AtomicCounter("c"  + key + "sub");

    stringImage = "Connection[" + proxyId + ':' + key + ']';
    hashCode = (proxyId.hashCode() & 0xFFFF0000) + key;

    mtpx.setDemultiplexerDaemonName(toString());
    
    identity = requestChannel.getIdentity();
    registerMBean(JMXBeanBaseName);
  }

 // base name of the MBean.
 protected String JMXBeanBaseName = null;
 
 public void setJMXBeanBaseName(String JMXBeanBaseName) {
   this.JMXBeanBaseName = JMXBeanBaseName;
 }

 public String getJMXBeanName() {
   StringBuffer buf = new StringBuffer();
   buf.append(JMXBeanBaseName).append(":type=Connections,");
   buf.append("name=").append((identity != null)?identity.getUserName():"null");
   buf.append(" c" + key);
   if (factoryParameters.getPort() < 0) {
     buf.append(" [localhost]");
   } else {
     buf.append(" [" + factoryParameters.getHost()).append(" _ ").append(factoryParameters.getPort()).append("]");
   }
   return buf.toString();
 }
 
 public String registerMBean(String base) {
   if (base == null) return null;
   String JMXBeanName = getJMXBeanName();
   try {
      MXWrapper.registerMBean(this, JMXBeanName);
   } catch (Exception e) {
     if (logger.isLoggable(BasicLevel.DEBUG))
       logger.log(BasicLevel.DEBUG, "Connection.registerMBean: " + JMXBeanName, e);
   }
   
   return JMXBeanName;
 }

 public void unregisterMBean() {
   if (JMXBeanBaseName == null)
     return;

   try {
     MXWrapper.unregisterMBean(getJMXBeanName());
   } catch (Exception e) {
     if (logger.isLoggable(BasicLevel.DEBUG))
       logger.log(BasicLevel.DEBUG, "Connection.unregisterMBean: " + JMXBeanBaseName + ":" + getJMXBeanName(), e);
   }
 }
  
  private void setStatus(int status) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".setStatus(" + Status.toString(status) + ')');
    this.status = status;
  }

  public boolean isStopped() {
    return (status == Status.STOP);
  }

  /** String image of the connection. */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append('(').append(super.toString());
    strbuf.append(",proxyId=").append(proxyId);
    strbuf.append(",key=").append(key);
    strbuf.append(')');
    return strbuf.toString();
  }

  public int hashCode() {
    return hashCode;
  }
  
  /**
   * Returns <code>true</code> if the parameter is a <code>Connection</code> instance
   * sharing the same proxy identifier and connection key.
   */
  public boolean equals(Object obj) {
    if (obj == this) return true;

    if (obj instanceof Connection) {
      Connection cnx = (Connection) obj;
      return (proxyId.equals(cnx.proxyId) && (key == cnx.key));
    }
    return false;
  }

  /**
   *  Returns the duration in seconds during which a JMS transacted (non XA)
   * session might be pending; above that duration the session is rolled back
   * and closed; the 0 value means "no timer".
   *
   * @return the duration in seconds during which a JMS transacted (non XA)
   * session might be pending.
   *
   * @see FactoryParameters#txPendingTimer
   */
  public final long getTxPendingTimer() {
    return factoryParameters.txPendingTimer;
  }
  
  /** 
   *  Indicates whether the messages consumed are implicitly acknowledged
   * or not. If true messages are immediately removed from queue when
   * delivered and there is none acknowledge message from client to server.
   * <p>
   *  This attribute is inherited from FactoryParameters, by default false.
   *
   * @return true if messages produced are implicitly acknowledged.
   *
   * @see FactoryParameters#implicitAck
   * @see Session#isImplicitAck()
   */
  public final boolean getImplicitAck() {
    return factoryParameters.implicitAck;
  }

  /** 
   * Indicates whether the persistent produced messages are asynchronously sent
   * (without acknowledge) or not. Messages sent asynchronously may be lost if a
   * failure occurs before the message is persisted on the server.
   * <p>
   * Non persistent messages are always sent without acknowledgment.
   * <p>
   * This attribute is inherited from FactoryParameters, by default false, persistent
   * messages are sent with acknowledge.
   *
   * @return true if messages produced are asynchronously sent.
   *
   * @see FactoryParameters#asyncSend
   * @see Session#isAsyncSend()
   */
  public final boolean getAsyncSend() {
    return factoryParameters.asyncSend;
  }
  
  /**
   *  Get the maximum number of messages that can be read at once from a queue
   * for this Connection.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 1.
   * 
   * @return    The maximum number of messages that can be read at once from
   *            a queue.
   *
   * @see FactoryParameters#queueMessageReadMax
   * @see Session#getQueueMessageReadMax()
   */
  public final int getQueueMessageReadMax() {
    return factoryParameters.queueMessageReadMax;
  }
  
  /**
   *  Get the maximum number of acknowledgements that can be buffered when
   * using Session.DUPS_OK_ACKNOWLEDGE mode for this Connection.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 0.
   *
   * @return The Maximum number of acknowledgements that can be buffered when
   *         using Session.DUPS_OK_ACKNOWLEDGE mode.
   *
   * @see FactoryParameters#topicAckBufferMax
   * @see Session#getTopicAckBufferMax()
   */
  public final int getTopicAckBufferMax() {
    return factoryParameters.topicAckBufferMax;
  }
  
  /**
   * Get the threshold of passivation for this Connection.
   * <p>
   * This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is
   * Integer.MAX_VALUE.
   *
   * @return The maximum messages number over which the subscription
   *         is passivated.
   *
   * @see FactoryParameters#topicPassivationThreshold
   * @see Session#getTopicPassivationThreshold()
   */
  public final int getTopicPassivationThreshold() {
    return factoryParameters.topicPassivationThreshold;
  }
  
  /**
   * Get the threshold of activation for this Connection.
   * <p>
   * This threshold is the minimum messages number below which
   * the subscription is activated.
   * <p>
   *  This attribute is inherited from FactoryParameters, default value is 0.
   *
   * @return The minimum messages number below which the subscription
   *         is activated.
   *
   * @see FactoryParameters#topicActivationThreshold
   * @see Session#getTopicActivationThreshold()
   */
  public final int getTopicActivationThreshold() {
    return factoryParameters.topicActivationThreshold;
  }
  
  /**
   * Returns the local IP address on which the TCP connection is activated. 
   * <p>
   * This attribute is inherited from FactoryParameters.
   *  
   * @return the local IP address on which the TCP connection is activated.
   *
   * @see FactoryParameters#outLocalAddress
   */
  public final String getOutLocalAddress() {
    return factoryParameters.outLocalAddress;
  }

  /**
   * Returns the local IP address port on which the TCP connection is activated
   * <p>
   * This attribute is inherited from FactoryParameters.
   *  
   * @return the local IP address port on which the TCP connection is activated.
   *
   * @see FactoryParameters#outLocalPort
   */
  public final int getOutLocalPort() {
    return factoryParameters.outLocalPort;
  }
  
  /**
   * returns the list of IN message interceptors.
   * <br>Each IN message interceptor is {@link MessageInterceptor#handle(javax.jms.Message) called}
   * when {@link Session#receive() receiving} a message.
   * <br>The execution follows the order of the elements within the list.
   * @return the list of the IN message interceptors.
   */
  final List getInInterceptors() {
	  return factoryParameters.inInterceptors;
  }
  /**
   * returns the list of OUT message interceptors.
   * <br>Each OUT message interceptor is {@link MessageInterceptor#handle(javax.jms.Message) called}
   * when {@link Session#send() sending} a message.
   * <br>The execution follows the order of the elements within the list.
   * @return the list of the OUT message interceptors.
   */
  final List getOutInterceptors() {
	  return factoryParameters.outInterceptors;
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
   * API method.
   * Creates a connection consumer for this connection, this is an expert facility needed for
   * applications servers.
   * 
   * @param dest        the destination to access.
   * @param selector    only messages with properties matching the message selector expression
   *                    are delivered. A value of null or an empty string indicates that there
   *                    is no message selector for this message consumer.
   * @param sessionPool the server session pool to associate with this connection consumer.
   * @param maxMessages the maximum number of messages that can be assigned to a server session
   *                    at one time.
   * @return    The connection consumer.
   *                    
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public synchronized javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Destination dest, 
                                                                            String selector,
                                                                            javax.jms.ServerSessionPool sessionPool,
                                                                            int maxMessages) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 stringImage + ".createConnectionConsumer(" + dest + ',' + selector + ',' +
                 sessionPool + ',' + maxMessages + ')');
    checkClosed();
    return createConnectionConsumer(dest, null, selector, sessionPool, maxMessages);
  }

  /**
   * API method.
   * Create a durable connection consumer for this connection, this is an expert facility
   * needed for applications servers.
   * 
   * @param topic       the topic to access.
   * @param subName     durable subscription name.
   * @param selector    only messages with properties matching the message selector expression
   *                    are delivered. A value of null or an empty string indicates that there
   *                    is no message selector for this message consumer.
   * @param sessionPool the server session pool to associate with this connection consumer.
   * @param maxMessages the maximum number of messages that can be assigned to a server session
   *                    at one time.
   * @return    The durable connection consumer.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
      createDurableConnectionConsumer(javax.jms.Topic topic, 
                                      String subName,
                                      String selector,
                                      javax.jms.ServerSessionPool sessPool,
                                      int maxMessages) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 stringImage + ".createDurableConnectionConsumer(" + topic + ',' + subName + ',' + selector + ',' +
                 sessPool + ',' + maxMessages + ')');
    checkClosed();
    if (subName == null) 
      throw new JMSException("Invalid subscription name: " + subName);
    return createConnectionConsumer(topic, subName, selector, sessPool, maxMessages);
  }
  
  private synchronized javax.jms.ConnectionConsumer
    createConnectionConsumer(
        javax.jms.Destination dest, 
        String subName,
        String selector,
        javax.jms.ServerSessionPool sessionPool,
        int maxMessages)  throws JMSException {
    checkClosed();
    
    try {
      org.objectweb.joram.shared.selectors.Selector.checks(selector);
    } catch (org.objectweb.joram.shared.excepts.SelectorException sE) {
      throw new InvalidSelectorException("Invalid selector syntax: " + sE);
    }

    if (sessionPool == null)
      throw new JMSException("Invalid ServerSessionPool parameter: "
                             + sessionPool);
    if (maxMessages <= 0)
      throw new JMSException("Invalid maxMessages parameter: " + maxMessages);
    
    boolean queueMode;
    String targetName;
    boolean durable;
    
    if (dest instanceof javax.jms.Queue) {
      queueMode = true;
      targetName = ((Destination) dest).getName();
      durable = false;
    } else {
      queueMode = false;
      if (subName == null) {
        targetName = nextSubName();
        durable = false;
      } else {
        targetName = subName;
        durable = true;
      }
      requestor.request(new ConsumerSubRequest(((Destination) dest).getName(),
          targetName, selector, false, durable, false));
    }
    
    MultiSessionConsumer msc =
      new MultiSessionConsumer(
          queueMode,
          durable,
          selector,
          ((Destination) dest).getAdminName(),
          targetName,
          sessionPool,
          factoryParameters.queueMessageReadMax,
          factoryParameters.topicActivationThreshold,
          factoryParameters.topicPassivationThreshold,
          factoryParameters.topicAckBufferMax,
          mtpx,
          this,
          maxMessages);
    
    msc.start();
    
    cconsumers.addElement(msc);
    
    return msc;
  }  

  /** 
   * API method.
   * Creates a Session object.
   * 
   * @param transacted  indicates whether the session is transacted.
   * @param acknowledgeMode indicates whether the consumer or the client will acknowledge any messages
   *                        it receives; ignored if the session is transacted. Legal values are
   *                        Session.AUTO_ACKNOWLEDGE, Session.CLIENT_ACKNOWLEDGE, and Session.DUPS_OK_ACKNOWLEDGE.
   * @return A newly created session.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public synchronized javax.jms.Session createSession(boolean transacted, 
                                                      int acknowledgeMode) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 stringImage + ".createSession(" + transacted + ',' +  acknowledgeMode + ')');
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
   * Called here and by sub-classes.
   */
  protected synchronized void addSession(Session session) {
    sessions.addElement(session);
    if (status == Status.START) {
      session.start();
    }
  }

  /**
   * API method.
   * Sets an exception listener for this connection. When Joram detects a serious problem with
   * a connection, it informs the connection's ExceptionListener, if one has been registered.
   * It does this by calling the listener's onException method, passing it a JMSException object
   * describing the problem.
   * <p>
   * The exception listener allows a client to be notified of a problem asynchronously. Some
   * connections only consume messages, so they would have no other way to learn their connection
   * has failed. A connection serializes execution of its ExceptionListener.
   * 
   * @param listener the exception listener.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public synchronized void setExceptionListener(javax.jms.ExceptionListener listener) throws JMSException {
    checkClosed();
    mtpx.setExceptionListener(listener);
  }

  /**
   * API method.
   * Gets the ExceptionListener object for this connection if it is configured.
   * 
   * @return the ExceptionListener for this connection, or null. if no ExceptionListener
   * is associated with this connection.
   * 
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.ExceptionListener getExceptionListener() throws JMSException {
    checkClosed();
    return mtpx.getExceptionListener();
  }

  /**
   * API method.
   * Sets the client identifier for this connection. Joram automatically allocates a
   * client identifier when the connection is created, so this method always throws 
   * an IllegalStateException.
   * 
   * @param clientID  the unique client identifier.
   *
   * @exception IllegalStateException  Systematically thrown.
   */
  public void setClientID(String clientID) throws JMSException {
    throw new IllegalStateException("ClientID is already set by the provider.");
  }

  /**
   * API method.
   * Gets the client identifier for this connection. This value is specific to the Joram, it
   * is automatically assigned by the server.
   * 
   * @return the unique client identifier.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public String getClientID() throws JMSException {
    checkClosed();
    return proxyId;
  }

  /**
   * API method.
   * Gets the metadata for this connection.
   * 
   * @return the  connection metadata.
   *
   * @exception IllegalStateException  If the connection is closed.
   * 
   * @see ConnectionMetadata
   */
  public javax.jms.ConnectionMetaData getMetaData() throws JMSException {
    checkClosed();
    if (metaData == null)
      metaData = new ConnectionMetaData();
    return metaData;
  }

  /**
   * API method for starting the connection.
   * <p>
   * Starts (or restarts) the connection's delivery of incoming messages. A call to start
   * on a connection that has already been started is ignored.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public synchronized void start() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".start()"); 
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
   * API method for stopping the connection; even if the connection appears
   * to be broken, stops the sessions.
   * <p>
   * Temporarily stops the connection's delivery of incoming messages. Delivery can
   * be restarted using the connection's start method. When the connection is stopped,
   * delivery to all the connection's message consumers is inhibited.
   * <p>
   * Stopping a connection has no effect on its ability to send messages. A call to stop
   * on a connection that has already been stopped is ignored. This call blocks until receives
   * and/or message listeners in progress have completed, it must not return until delivery
   * of messages has paused. 
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   */
  public void stop() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".stop()");
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
      Session session = (Session) sessions.get(i);
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
   * <p>
   * In order to free significant resources allocated on behalf of a connection,
   * clients should close connections when they are not needed. Closing a connection
   * automatically close all related sessions, producers, and consumers and causes
   * all temporary destinations to be deleted. 
   *
   * @exception JMSException  Actually never thrown.
   * 
   * @see javax.jms.Connection.close()
   */
  public void close() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".close()");

    unregisterMBean();
    
    closer.close();
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
      
    Vector sessionsToClose = (Vector)sessions.clone();
    sessions.clear();
    mtpx.closing();
    
    for (int i = 0; i < sessionsToClose.size(); i++) {
      Session session = (Session) sessionsToClose.elementAt(i);
      try {
        session.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
    
    Vector consumersToClose = (Vector)cconsumers.clone();
    cconsumers.clear();
    
    for (int i = 0; i < consumersToClose.size(); i++) {
      MultiSessionConsumer consumer = (MultiSessionConsumer) consumersToClose.elementAt(i);
      try {
        consumer.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log( BasicLevel.DEBUG, "", exc);
      }
    }

    try {
      CnxCloseRequest closeReq = new CnxCloseRequest();
      requestor.request(closeReq);
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
    }
    
    mtpx.close();
    
    synchronized (this) {
      setStatus(Status.CLOSE);
    }
  }


  /**
   * Used by OutboundConnection in the connector layer.
   * When a connection is put back in a pool, 
   * it must be cleaned up.
   */
  public void cleanup() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage +".cleanup()");
    
    // Closing the sessions:
    // Session session;
    Vector sessionsToClose = (Vector)sessions.clone();
    sessions.clear();

    for (int i = 0; i < sessionsToClose.size(); i++) {
      Session session = (Session) sessionsToClose.elementAt(i);
      try {
        session.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
    
    mtpx.cleanup();
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
   * Called by Session.
   */
  synchronized void closeSession(Session session) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".closeSession(" + session + ')');
    sessions.removeElement(session);
  }

  /**
   * Called by MultiSessionConsumer.
   * Synchronized with run().
   */
  synchronized void closeConnectionConsumer(MultiSessionConsumer cc) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".closeConnectionConsumer(" + cc + ')');
    cconsumers.removeElement(cc);
  }

  synchronized AbstractJmsReply syncRequest(AbstractJmsRequest request) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, stringImage + ".syncRequest(" + request + ')');
    return requestor.request(request);
  }

  /**
   * Called by temporary destinations deletion.
   */
  synchronized void checkConsumers(String agentId) throws JMSException {
    for (int i = 0; i < sessions.size(); i++) {
      Session sess = (Session) sessions.elementAt(i);
      sess.checkConsumers(agentId);
    }
  }

  protected final RequestMultiplexer getRequestMultiplexer() {
    return mtpx;
  }
}

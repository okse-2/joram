/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s):
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.InvalidClientIDException;
import javax.jms.InvalidClientIDRuntimeException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidDestinationRuntimeException;
import javax.jms.InvalidSelectorException;
import javax.jms.InvalidSelectorRuntimeException;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.management.JMRuntimeException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A {@code JMSContext} is the main interface in the simplified JMS API
 * introduced for JMS 2.0. This combines in a single object the functionality of
 * two separate objects from the JMS 1.1 API: a {@code Connection} and a
 * {@code Session}.
 * <p>
 * When an application needs to send messages it use the
 * {@code createProducer} method to create a {@code JMSProducer} which
 * provides methods to configure and send messages. Messages may be sent either
 * synchronously or asynchronously.
 * <p>
 * When an application needs to receive messages it uses one of several
 * {@code createConsumer} or {@code createDurableConsumer} methods to
 * create a {@code JMSConsumer} . A {@code JMSConsumer} provides
 * methods to receive messages either synchronously or asynchronously.
 * <p>
 * In terms of the JMS 1.1 API a {@code JMSContext} should be thought of as
 * representing both a {@code Connection} and a {@code Session}.
 * Although the simplified API removes the need for applications to use those
 * objects, the concepts of connection and session remain important. A
 * connection represents a physical link to the JMS server and a session
 * represents a single-threaded context for sending and receiving messages.
 * <p>
 * A {@code JMSContext} may be created by calling one of several
 * {@code createContext} methods on a {@code ConnectionFactory}. A
 * {@code JMSContext} that is created in this way is described as being
 * <i>application-managed</i>. An application-managed {@code JMSContext}
 * must be closed when no longer needed by calling its {@code close}
 * method.
 * <p>
 * Applications running in the Java EE web and EJB containers may alternatively
 * inject a {@code JMSContext} into their application using the
 * {@code @Inject} annotation. A {@code JMSContext} that is created in
 * this way is described as being <i>container-managed</i>. A 
 * container-managed {@code JMSContext} will be closed automatically by
 * the container. 
 * <p>
 * Applications running in the Java EE web and EJB containers are not permitted
 * to create more than one active session on a connection so combining them in a
 * single object takes advantage of this restriction to offer a simpler API.
 * <p>
 * However applications running in a Java SE environment or in the Java EE
 * application client container are permitted to create multiple active sessions
 * on the same connection. This allows the same physical connection to be used
 * in multiple threads simultaneously. Such applications which require multiple
 * sessions to be created on the same connection should use one of the
 * {@code createContext} methods on the {@code ConnectionFactory} to
 * create the first {@code JMSContext} and then use the
 * {@code createContext} method on {@code JMSContext} to create
 * additional {@code JMSContext} objects that use the same connection. All
 * these {@code JMSContext} objects are application-managed and must be
 * closed when no longer needed by calling their {@code close} method.
 * 
 * @since Joram 5.9
 */
public class JMSContext implements javax.jms.JMSContext {
  public static Logger logger = Debug.getLogger(JMSContext.class.getName());
  
  // Internal shared connection 
  private ContextConnection connection = null;
  // Internal session for private use
  private Session session = null;

//  Session getSession() {
//    return session;
//  }
  
  /**
   * API method.
   * 
   * Creates a new {@code JMSContext} with the specified session mode
   * using the same connection as this {@code JMSContext} and creating a
   * new session.
   * <p>
   * This method does not start the connection. If the connection has not
   * already been started then it will be automatically started when a
   * {@code JMSConsumer} is created on any of the {@code JMSContext}
   * objects for that connection.
   * <p>
   * <ul>
   * <li>If {@code sessionMode} is set to
   * {@code JMSContext.SESSION_TRANSACTED} then the session will use a
   * local transaction which may subsequently be committed or rolled back by
   * calling the {@code JMSContext}'s {@code commit} or
   * {@code rollback} methods.
   * <li>If {@code sessionMode} is set to any of
   * {@code JMSContext.CLIENT_ACKNOWLEDGE},
   * {@code JMSContext.AUTO_ACKNOWLEDGE} or
   * {@code JMSContext.DUPS_OK_ACKNOWLEDGE}. then the session will be
   * non-transacted and messages received by this session will be acknowledged
   * according to the value of {@code sessionMode}. For a definition of
   * the meaning of these acknowledgement modes see the links below.
   * </ul>
   * <p>
   * This method must not be used by applications running in the Java EE web
   * or EJB containers because doing so would violate the restriction that
   * such an application must not attempt to create more than one active (not
   * closed) {@code Session} object per connection. If this method is
   * called in a Java EE web or EJB container then a
   * {@code JMSRuntimeException} will be thrown.
   * 
   * @param sessionMode
   *            indicates which of four possible session modes will be used.
   *            The permitted values are
   *            {@code JMSContext.SESSION_TRANSACTED},
   *            {@code JMSContext.CLIENT_ACKNOWLEDGE},
   *            {@code JMSContext.AUTO_ACKNOWLEDGE} and
   *            {@code JMSContext.DUPS_OK_ACKNOWLEDGE}.
   * 
   * @return a newly created JMSContext
   * 
   * @exception JMSRuntimeException
   *                if the JMS provider fails to create the JMSContext due to
   *                <ul>
   *                <li>some internal error or <li>because this method is
   *                being called in a Java EE web or EJB application.
   *                </ul>
   * @since JMS 2.0
   * 
   * @see JMSContext#SESSION_TRANSACTED
   * @see JMSContext#CLIENT_ACKNOWLEDGE
   * @see JMSContext#AUTO_ACKNOWLEDGE
   * @see JMSContext#DUPS_OK_ACKNOWLEDGE
   * 
   * @see javax.jms.ConnectionFactory#createContext()
   * @see javax.jms.ConnectionFactory#createContext(int)
   * @see javax.jms.ConnectionFactory#createContext(java.lang.String,
   *      java.lang.String)
   * @see javax.jms.ConnectionFactory#createContext(java.lang.String,
   *      java.lang.String, int)
   * @see javax.jms.JMSContext#createContext(int)
   */
  public javax.jms.JMSContext createContext(int sessionMode) {
    return new JMSContext(connection, sessionMode);
  }

  /**
   * Creates a new Context sharing the connection of the calling context.
   * 
   * @param connection  the connection of the calling context.
   * @param sessionMode indicates which of four possible session modes will be used.
   */
  public JMSContext(ContextConnection connection, int sessionMode) {
    this.connection = connection;
    try {
      session = (Session) connection.createSession(sessionMode);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JMSContext created ");
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JMSContext not created ", e);
      throw new JMSRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    }
  }
  
  /**
   * Creates a new Context using a newly created JMS connection.
   * 
   * @param cnx  the created JMS connection.
   */
  public JMSContext(Connection cnx) {
    this.connection = new ContextConnection(cnx);
    try {
      session = (Session) connection.createSession();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JMSContext created parameters[ Connection = " + connection
                + " | session = " + session + ", " + session.transacted + ", " + session.getSessionMode() + " ] ");
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JMSContext not created ", e);
      throw new JMSRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    }
  }

  /**
   * Creates a new Context using a newly created JMS connection.
   * 
   * @param cnx  the created JMS connection.
   * @param sessionMode indicates which of four possible session modes will be used.
   */
  public JMSContext(Connection cnx, int sessionMode) {
    this.connection = new ContextConnection(cnx);
    try {
      session = (Session) connection.createSession(sessionMode);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JMSContext created ");
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JMSContext not created ", e);
      throw new JMSRuntimeException("Unable to create JMSContext", e.getMessage(), e);
    }
  }

  /**
   * API method.
   * 
   * Closes the JMSContext
   * <p>
   * This closes the underlying session and any underlying producers and
   * consumers. If there are no other active (not closed) JMSContext objects
   * using the underlying connection then this method also closes the
   * underlying connection.
   * 
   * <P>
   * Since a provider typically allocates significant resources outside the
   * JVM on behalf of a connection, clients should close these resources when
   * they are not needed. Relying on garbage collection to eventually reclaim
   * these resources may not be timely enough.
   * 
   * <P>
   * Closing a connection causes all temporary destinations to be deleted.
   * 
   * <P>
   * When this method is invoked, it should not return until message
   * processing has been shut down in an orderly fashion. This means that all
   * message listeners that may have been running have returned, and that all
   * pending receives have returned. A close terminates all pending message
   * receives on the connection's sessions' consumers. The receives may return
   * with a message or with null, depending on whether there was a message
   * available at the time of the close. If one or more of the connection's
   * sessions' message listeners is processing a message at the time when
   * connection {@code close} is invoked, all the facilities of the
   * connection and its sessions must remain available to those listeners
   * until they return control to the JMS provider.
   * <p>
   * This method must not return until any incomplete asynchronous send
   * operations for this <tt>JMSContext</tt> have been completed and any
   * <tt>CompletionListener</tt> callbacks have returned. Incomplete sends
   * should be allowed to complete normally unless an error occurs.
   * <p>
   * For the avoidance of doubt, if an exception listener for the JMSContext's
   * connection is running when {@code close} is invoked, there is no
   * requirement for the {@code close} call to wait until the exception
   * listener has returned before it may return.
   * <P>
   * Closing a connection causes any of its sessions' transactions in progress
   * to be rolled back. In the case where a session's work is coordinated by
   * an external transaction manager, a session's {@code commit} and
   * {@code rollback} methods are not used and the result of a closed
   * session's work is determined later by the transaction manager.
   * <p>
   * Closing a connection does NOT force an acknowledgment of
   * client-acknowledged sessions.
   * <P>
   * Invoking the {@code acknowledge} method of a received message from a
   * closed connection's session must throw an
   * {@code IllegalStateRuntimeException}. Closing a closed connection must NOT
   * throw an exception.
   * <p>
   * A <tt>MessageListener</tt> must not attempt to close its own
   * <tt>JMSContext</tt> as this would lead to deadlock. The JMS provider must
   * detect this and throw a <tt>IllegalStateRuntimeException</tt>.
   * <p>
   * A <tt>CompletionListener</tt> callback method must not call
   * <tt>close</tt> on its own <tt>JMSContext</tt>. Doing so will cause an
   * <tt>IllegalStateRuntimeException</tt> to be thrown.
   * <p>
   * This method must not be used if the {@code JMSContext} is
   * container-managed (injected). Doing so will cause a
   * {@code IllegalStateRuntimeException} to be thrown.
   * 
   * @exception IllegalStateRuntimeException
   *                <ul>
   *                <li>if this method has been called by a <tt>MessageListener
   *                </tt> on its own <tt>JMSContext</tt></li> <li>if this method
   *                has been called by a <tt>CompletionListener</tt> callback
   *                method on its own <tt>JMSContext</tt></li>
   *                <li>if the {@code JMSContext} is container-managed (injected)</li>
   *                </ul>
   * @exception JMSRuntimeException
   *                if the JMS provider fails to close the
   *                {@code JMSContext} due to some internal error. For example, a
   *                failure to release resources or to close a socket
   *                connection can cause this exception to be thrown. 
   */
  public synchronized void close() {
    connection.lockClientID();
    if (session.checkThread())
      throw new IllegalStateRuntimeException("Cannot stop context from listener");

    try {
      session.close();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException("Unable to close context", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to close context", e.getMessage(), e);
    }
    try {
      connection.close();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException("Unable to close context", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to close context", e.getMessage(), e);
    }
  }

  public void acknowledge() {
    connection.lockClientID();
    try {
      session.acknowledge();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException("Unable to acknowledge", e.getMessage(), e);
    } catch (JMSException e) {
      logger.log(BasicLevel.ERROR, "Unable to acknowledge", e);
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  public void commit() {
    connection.lockClientID();
    try {
      session.commit();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  public void recover() {
    connection.lockClientID();
    try {
      session.recover();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  public void rollback() {
    connection.lockClientID();
    try {
      session.rollback();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  // Message creation

  public Message createMessage() {
    connection.lockClientID();
    try {
      return session.createMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }
  
  public BytesMessage createBytesMessage() {
    connection.lockClientID();
    try {
      return  session.createBytesMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create byte message " + e.getMessage());
    }
  }
  
  public MapMessage createMapMessage() {
    connection.lockClientID();
    try {
      return session.createMapMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  public ObjectMessage createObjectMessage() {
    connection.lockClientID();
    try {
      return session.createObjectMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  public ObjectMessage createObjectMessage(Serializable object) {
    connection.lockClientID();
    try {
      return session.createObjectMessage(object);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  public TextMessage createTextMessage() {
    connection.lockClientID();
    try {
      return session.createTextMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  public TextMessage createTextMessage(String text) {
    connection.lockClientID();
    try {
      return session.createTextMessage(text);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  public StreamMessage createStreamMessage() {
    connection.lockClientID();
    try {
      return session.createStreamMessage();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create message", e.getMessage(), e);
    }
  }

  // Destination creation
  
  public TemporaryQueue createTemporaryQueue() {
    connection.lockClientID();
    try {
      return session.createTemporaryQueue();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create temporary queue", e.getMessage(), e);
    }
  }

  public TemporaryTopic createTemporaryTopic() {
    connection.lockClientID();
    try {
      return session.createTemporaryTopic();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create temporary topic", e.getMessage(), e);
    }
  }

  public Queue createQueue(String name) {
    connection.lockClientID();
    try {
      return session.createQueue(name);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create queue: " + name, e.getMessage(), e);
    }
  }

  public Topic createTopic(String name) {
    connection.lockClientID();
    try {
      return session.createTopic(name);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create topic: " + name, e.getMessage(), e);
    }
  }

  // 

  public boolean getAutoStart() {
    connection.lockClientID();
    return connection.getAutoStart();
  }

  public void setAutoStart(boolean autoStart) {
    connection.lockClientID();
    connection.setAutoStart(autoStart);
  }

  //
  
  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination) {
    return createConsumer(destination, null);
  }

  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination, String selector) {
    return createConsumer(destination, selector, false);
  }

  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination, String selector, boolean noLocal) {
    connection.lockClientID();
    try {
      MessageConsumer consumer = (MessageConsumer) session.createConsumer(destination, selector, noLocal);
      return new JMSConsumer(consumer);
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage());
    } catch (InvalidSelectorException e) {
      throw new InvalidSelectorRuntimeException(e.getMessage());
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Unable to instantiate a JMSConsumer" + e.getMessage());
      throw new JMSRuntimeException("Unable to instantiate a JMSConsumer");
    } finally {
      if (connection.getAutoStart())
        connection.start();
    }
  }

  public QueueBrowser createBrowser(Queue queue) {
    return createBrowser(queue, null);
  }

  public QueueBrowser createBrowser(Queue queue, String messageSelector) {
    connection.lockClientID();
    try {
      return session.createBrowser(queue, messageSelector);
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage());
    } catch (InvalidSelectorException e) {
      throw new InvalidSelectorRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  public javax.jms.JMSConsumer createDurableConsumer(Topic topic, String name) {
    return createDurableConsumer(topic, name, null, false);
  }

  public JMSConsumer createDurableConsumer(Topic topic, String name, String selector, boolean noLocal) {
    connection.lockClientID();
    try {
      MessageConsumer consumer = (MessageConsumer) session.createDurableConsumer(topic, name, selector, noLocal);
      return new JMSConsumer(consumer);
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage());
    } catch (InvalidSelectorException e) {
      throw new InvalidSelectorRuntimeException(e.getMessage());
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage());
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Unable to instantiate a JMSConsumer" + e.getMessage());
      throw new JMSRuntimeException("Unable to instantiate a JMSConsumer");
    } finally {
      if (connection.getAutoStart())
        connection.start();
    }
  }

  public JMSProducer createProducer() {
    connection.lockClientID();
    try {
      return new org.objectweb.joram.client.jms.JMSProducer(session);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to create producer "
          + e.getMessage());
    }
  }

  public javax.jms.JMSConsumer createSharedConsumer(Topic topic,
      String sharedSubscriptionName) {
    return createSharedConsumer(topic, sharedSubscriptionName, null);
  }

  public javax.jms.JMSConsumer createSharedConsumer(Topic topic,
      String sharedSubscriptionName, String messageSelector) {
    connection.lockClientID();
    try {
      return new JMSConsumer(session.createSharedConsumer(topic, sharedSubscriptionName, messageSelector));
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (InvalidSelectorException e) {
      throw new InvalidSelectorRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Unable to crate shared consumer " + e.getMessage());
      throw new JMSRuntimeException("Unable to create shared consumer " + e.getMessage(), e.getErrorCode(), e);
    } finally {
      if (connection.getAutoStart())
        connection.start();
    }
  }

  public javax.jms.JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
    return createSharedDurableConsumer(topic, name, null);
  }

  public javax.jms.JMSConsumer createSharedDurableConsumer(Topic topic,
      String name, String messageSelector) {
    connection.lockClientID();
    try {
      return new JMSConsumer(session.createSharedDurableConsumer(topic, name, messageSelector));
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (InvalidSelectorException e) {
      throw new InvalidSelectorRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Unable to crate shared durable consumer " + e.getMessage());
      throw new JMSRuntimeException("Unable to create shared durable consumer " + e.getMessage(), e.getErrorCode(), e);
    } finally {
      if (connection.getAutoStart())
        connection.start();
    }
  }

  private Session getCopyOfSession() {
    try {
      return (Session) connection.createSession(getSessionMode());
    } catch (JMSException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
            "Unable to create a session " + e.getMessage());
      throw new JMRuntimeException("Unable to create a session " + e.getMessage());
    }
  }
  

  /**
   * JMS 2.0 API method.
   * Returns the session mode of the JMSContext's session. This can be set at
   * the time that the JMSContext is created. Possible values are
   * JMSContext.SESSION_TRANSACTED, JMSContext.AUTO_ACKNOWLEDGE,
   * JMSContext.CLIENT_ACKNOWLEDGE and JMSContext.DUPS_OK_ACKNOWLEDGE
   * <p>
   * If a session mode was not specified when the JMSContext was created a
   * value of JMSContext.AUTO_ACKNOWLEDGE will be returned.
   * 
   * @return the session mode of the JMSContext's session
   * 
   * @exception JMSRuntimeException
   *                if the JMS provider fails to return the acknowledgment
   *                mode due to some internal error.
   * 
   * @see Connection#createSession
   * @since JMS 2.0
   */
  public int getSessionMode() {
    connection.lockClientID();
    try {
      return session.getAcknowledgeMode();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get session mode", e.getMessage(), e);
    }
  }

  public boolean getTransacted() {
    connection.lockClientID();
    return session.transacted;
  }

  public ConnectionMetaData getMetaData() {
    connection.lockClientID();
    return connection.getMetaData();
  }
  
  public String getClientID() {
    return connection.getClientID();
  }

  public void setClientID(String clientID) {
    connection.setClientID(clientID);
  }

  public ExceptionListener getExceptionListener() {
    connection.lockClientID();
    return connection.getExceptionListener();
  }

  public void setExceptionListener(ExceptionListener listener) {
    connection.lockClientID();
    connection.setExceptionListener(listener);
  }

  public void start() {
    connection.lockClientID();
    connection.start();
  }
  
  /**
   * Temporarily stops the delivery of incoming messages by the JMSContext's
   * connection. Delivery can be restarted using the {@code start}
   * method. When the connection is stopped, delivery to all the connection's
   * message consumers is inhibited: synchronous receives block, and messages
   * are not delivered to message listeners.
   * 
   * <P>
   * This call blocks until receives and/or message listeners in progress have
   * completed.
   * 
   * <P>
   * Stopping a connection has no effect on its ability to send messages. A
   * call to {@code stop} on a connection that has already been stopped
   * is ignored.
   * 
   * <P>
   * A call to {@code stop} must not return until delivery of messages
   * has paused. This means that a client can rely on the fact that none of
   * its message listeners will be called and that all threads of control
   * waiting for {@code receive} calls to return will not return with a
   * message until the connection is restarted. The receive timers for a
   * stopped connection continue to advance, so receives may time out while
   * the connection is stopped.
   * 
   * <P>
   * If message listeners are running when {@code stop} is invoked, the
   * {@code stop} call must wait until all of them have returned before
   * it may return. While these message listeners are completing, they must
   * have the full services of the connection available to them.
   * <p>
   * A message listener must not attempt to stop its own JMSContext as this
   * would lead to deadlock. The JMS provider must detect this and throw a
   * <tt>IllegalStateRuntimeException</tt>
   * <p>
   * For the avoidance of doubt, if an exception listener for the JMSContext's
   * connection is running when {@code stop} is invoked, there is no
   * requirement for the {@code stop} call to wait until the exception
   * listener has returned before it may return.
   * <p>
   * This method must not be used in a Java EE web or EJB application. Doing
   * so may cause a {@code JMSRuntimeException} to be thrown though this
   * is not guaranteed.
   * <p>
   * This method must not be used if the {@code JMSContext} is
   * container-managed (injected). Doing so will cause a
   * {@code IllegalStateRuntimeException} to be thrown.
   * 
   * @exception IllegalStateRuntimeException
   *                <ul>
   *                <li>if this method has been called by a <tt>MessageListener</tt>
   *                on its own <tt>JMSContext</tt>
   *                <li>if the {@code JMSContext} is container-managed (injected).
   *                </ul>
   * @exception JMSRuntimeException
   *                if the JMS provider fails to stop message delivery for one
   *                of the following reasons:
   *                <ul>
   *                <li>an internal error has occurred or <li>this method has
   *                been called in a Java EE web or EJB application (though it
   *                is not guaranteed that an exception is thrown in this
   *                case) 
   *                </ul>
   *                
   * @see javax.jms.JMSContext#start
   */
  public void stop() {
    connection.lockClientID();
    if (session.checkThread())
      throw new IllegalStateRuntimeException("Cannot stop context from listener");
    
    connection.stop();
  }

  public void unsubscribe(String name) {
    connection.lockClientID();
    try {
      session.unsubscribe(name);
    } catch (InvalidDestinationException exc) {
      throw new InvalidDestinationRuntimeException("Unable to unsubcribe to: " + name, exc.getMessage(), exc);
    } catch (JMSException exc) {
      throw new JMSRuntimeException("Unable to unsubcribe to: " + name, exc.getMessage(), exc);
    }
  }

  Session getSession() {
    return session;
  }
}

class ContextConnection {
  private int counter = 1;
  private boolean autoStart = true;
  private org.objectweb.joram.client.jms.Connection connection = null;
  private boolean lockClientID = false;
  
  void lockClientID() {
    lockClientID = true;
  }
  
  ContextConnection(Connection connection) {
    this.connection = (org.objectweb.joram.client.jms.Connection) connection;
  }

  public synchronized Session createSession() throws JMSException {
    try {
      return (Session) connection.createSession();
    } finally {
      counter += 1;      
    }
  }

  synchronized Session createSession(int mode) throws JMSException {
    try {
      return (Session) connection.createSession(mode);
    } finally {
      counter += 1;      
    }
  }
  
  synchronized void close() throws JMSException {
    if (--counter <= 0)
      connection.close();
  }
  
  public void start() {
    try {
      connection.start();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Cannot start connection", e.getMessage(), e);
    }
  }

  public void stop() {
    try {
      connection.stop();
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException("Unable to close context", e.getMessage(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Cannot stop connection", e.getMessage(), e);
    }
  }

  public ConnectionMetaData getMetaData() {
    try {
      return connection.getMetaData();
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }

  public ExceptionListener getExceptionListener() {
    try {
      return connection.getExceptionListener();
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }

  public void setExceptionListener(ExceptionListener listener) {
    try {
      connection.setExceptionListener(listener);
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }

  public String getClientID() {
    try {
      return connection.getClientID();
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }

  public void setClientID(String clientID) {
    try {
      if (lockClientID)
        throw new IllegalStateException("ClientID is already set by the provider.");
      connection.setClientID(clientID);
      lockClientID = true;
    } catch (InvalidClientIDException e) {
      throw new InvalidClientIDRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage(), e.getErrorCode(), e);
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }
  
  synchronized void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }
  
  synchronized boolean getAutoStart() {
    return autoStart;
  }
}
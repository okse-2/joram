/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#ifndef XORAM_H
#define XORAM_H

#include "Types.H"
#include "Vector.H"
#include "XStream.H"
#include "AbstractMessage.H"
#include "XoramException.H"
#include "XQueue.H"
#include "Daemon.H"
#include "Destination.H"

#include <errno.h>

class Channel;
class Session;

/**
 *  An exception listener allows a client to be notified of a problem
 * asynchronously. If the Joram server detects a serious problem during
 * a connection, it informs the client through the ExceptionListener if it
 * has been registered. It does this by calling the listener's onException
 * method, passing it an exception describing the problem.
 */
class ExceptionListener {
 public:
  /**
   * Notifies the user of an asynchronous error during connection.
   *
   * @param exc	An exception describing the issue.
   */
  virtual void onException(XoramException exc) =0;
};

class Requestor;
class RequestMultiplexer;

/**
 *  A Connection object allows the client's active connection to the Joram
 * server. Connections support concurrent use, it serves several purposes:
 * <ul>
 * <li>It encapsulates the real connection with the Joram server (for example
 * an open TCP/IP socket between the client and the server).
 * <li>It needs the client authentication.
 * <li>It specifies a unique client identifier.
 * <li>It supports the ExceptionListener object. 
 * </ul>
 * A Xoram client typically creates a connection, one or more sessions, and a
 * number of message producers and consumers. 
 * <br>
 * When a connection is created, it is in stopped mode that means that no
 * messages are being delivered. In order to minimize any client confusion
 * that may result from asynchronous message delivery during setup, it is
 * typical to leave the connection in stopped mode until setup is complete.
 * A message producer can send messages while a connection is stopped. 
 */
class Connection {
 private:
  ExceptionListener* exclist;
  char* clientid;
  Channel* channel;

  enum {STOP, START, CLOSE};

  int status;

  long cnxPendingTimer;

  /**  The request multiplexer used to communicate with the user proxy. */
  RequestMultiplexer* mtpx;

  /** The requestor used to communicate with the user proxy. */
  Requestor* requestor;

  /** Sessions counter. */
  int sessionsC;

  /** Messages counter. */
  int messagesC;

  /** Subscriptions counter. */
  int subsC;

  /** Client's agent proxy identifier. */
  char* proxyId;

  /** Connection key. */
  int key;

  /** Vector of the connection's sessions. */
  Vector<Session>* sessions;

 public:
  /**
   * Non API method, should be hidden.
   */
  Connection(Channel* channel);

  ~Connection();

  /**
   *
   */
  boolean isStopped();

  /**
   * Starts the connection's delivery of incoming messages. A call to start
   * on an already started connection is ignored.
   *
   * @see stop
   */
  void start();

  /**
   *  Creates a Session object. Currently all sessions are in AUTO_ACKNOWLEDGE
   * mode.
   *
   * @see Session.
   */
  Session* createSession();

  /**
   *  Sets an exception listener for this connection. If the Joram server
   * detects a serious problem during a connection, it informs the client
   * through the ExceptionListener if it has been registered. It does this
   * by calling the listener's onException method, passing it an exception
   * describing the problem.
   *
   * @param exclist	the exception listener.
   */
  void setExceptionListener(ExceptionListener* exclist);
  
  /**
   * Gets the ExceptionListener object for this connection.
   *
   * @return the ExceptionListener for this connection, or null if none is
   * defined for this connection.
   */
  ExceptionListener* getExceptionListener();

  /**
   * Returns the unique client identifier for this connection.
   *
   * @return the unique client identifier.
   */
  char* getClientID();

  /**
   *  Temporarily stops the connection's delivery of incoming messages.
   * Delivery can be restarted using the start  method. When the connection
   * is stopped, messages are no longer delivered to any connection's message
   * consumers.
   * <br>
   *  This call blocks until receives and/or message listeners in progress have
   * completed.
   * <br>
   * Stopping a connection has no effect on its ability to send messages. A
   * call to stop on a connection that has already been stopped is ignored. 
   *
   * @see start
   */
  void stop();

  /**
   * Closes the connection. Since a connection typically allocates significant
   * resources, clients should close the connections when they are not needed.
   */
  void close();

  /** Non API method, should be hidden. Returns a new session identifier. */
  char* nextSessionId();
 
  /** Non API method, should be hidden. Returns a new message identifier. */
  char* nextMessageId();

  /** Non API method, should be hidden. Returns a new subscription name. */
  char* nextSubName();
};

/**
 *  A ConnectionFactory object encapsulates the set of connection configuration
 * parameters needed to create a connection with the Joram server.
 */
class ConnectionFactory {
 protected:
  /**
   * Default login name for connection, default value is "anonymous".
   */
  char* dfltLogin;
  /**
   * Default login password for connection, default value is "anonymous".
   */
  char* dfltPassword;
 
  ConnectionFactory();

 public:
  /**
   *  Creates a connection with the default user identity. The connection
   * is created in stopped mode.
   */
  Connection* createConnection();

  /**
   *  Creates a connection with the specified user identity. The connection
   * is created in stopped mode.
   */
  virtual Connection* createConnection(char *user, char *pass) = 0;
};

/**
 *  A TCPConnectionFactory object encapsulates the set of connection
 * configuration parameters needed to create a TCP based connection with
 * the Joram server.
 */
class TCPConnectionFactory : public ConnectionFactory {
 private:
  /**
   * Default hostname for connection, default value is "localhost".
   */
  char* dfltHost;
  /**
   * Default listen port for connection, default value is 16010.
   */
  int dfltPort;

 public:
   ~TCPConnectionFactory();
   
  /**
   *  Creates a TCP connection factory with default server parameters
   * ("localhost", 16010) and user identity ("anonymous", "anonymous").
   */
  TCPConnectionFactory();

  /**
   *  Creates a TCP connection factory with specified server parameters and
   * default user identity ("anonymous", "anonymous").
   */
  TCPConnectionFactory(char *host, int port);

  /**
   *  Creates a TCP connection with default server parameters and the specified
   * user identity. The connection is created in stopped mode.
   *
   * @param user	the client's user name.
   * @param pass	the client's password.
   * @return		The newly created connection.
   */
  Connection* createConnection(char *user, char *pass);

  /**
   *  Creates a TCP connection with specified server parameters and default
   * user identity. The connection is created in stopped mode.
   *
   * @param host	the server's hostname.
   * @param port	the server's listening port.
   * @return		The newly created connection.
   */
  Connection* createConnection(char *host, int port);

  /**
   *  Creates a TCP connection specifying server parameters and user identity.
   * The connection is created in stopped mode.
   *
   * @param user	the client's user name.
   * @param pass	the client's password.
   * @param host	the server's hostname.
   * @param port	the server's listening port.
   * @return		The newly created connection.
   */
  Connection* createConnection(char *user, char *pass, char *host, int port);
};

/**
 * Internal class.
 */
class ReplyListener {
 public:
  /**
   * @return whether the request must be aborted or not.
   *
   * @exception if the request has been aborted.
   */
   virtual int getRequestId() = 0;

   virtual boolean replyReceived(AbstractReply* reply) throw (AbortedRequestException) = 0;

   virtual void replyAborted() = 0;
};

/**
 * Internal class.
 */
class Requestor : public virtual ReplyListener, Synchronized {
 private:
  enum {INIT, RUN, DONE, CLOSE};
  int status;

  RequestMultiplexer* mtpx;

  AbstractRequest* req;
  AbstractReply* reply;

 public:
  Requestor(RequestMultiplexer* mtpx);
  ~Requestor();

  AbstractReply* request(AbstractRequest* request);

  AbstractReply* request(AbstractRequest* request, long timeout);

  int getRequestId();

  void abortRequest();

  boolean replyReceived(AbstractReply* reply) throw (AbortedRequestException);

/*   void errorReceived(int replyId, XoramException exc; */
  void replyAborted();

  void close();
};

/**
 * Internal class.
 */
class RequestMultiplexer : Synchronized, Daemon {
 private:
  enum {OPEN, CLOSE};

  Connection* cnx;

  volatile int status;

  Channel* channel;

  Vector<ReplyListener>* requestsTable;

  int requestCounter;

  /*    DemultiplexerDaemon* demtpx; */

  ExceptionListener* exceptionListener;

  /**
   * Not synchronized because it may be called by the
   * demultiplexer during a concurrent close. It would deadlock
   * as the close waits for the demultiplexer to stop.
   */
  void route(AbstractReply* reply);

  void abortReply(AbstractReply reply);

 protected:
  virtual void run();
 
 public:
  RequestMultiplexer(Connection* cnx,
                     Channel* channel) throw (XoramException);
  
  ~RequestMultiplexer();

  boolean isClosed();

  void setExceptionListener(ExceptionListener* exceptionListener);

  ExceptionListener* getExceptionListener();

  void sendRequest(AbstractRequest* request) throw (XoramException);
  
  void sendRequest(AbstractRequest* request, ReplyListener* listener)
    throw (XoramException);

  void close();

  void abortRequest(int requestId);

  void deny(ConsumerMessages* messages);
};

class Message;
class Destination;
class Queue;
class Topic;
class TemporaryTopic;
class MessageProducer;
class MessageConsumer;
class MessageListener;

class MessageConsumerListener;

/* class MessageListenerContext { */
/*  public: */
/*   MessageConsumerListener* listener; */
/*   ConsumerMessages* cm; */

/*   MessageListenerContext(MessageConsumerListener* listener,  */
/*                          ConsumerMessages* cm) { */
/*     this->listener = listener; */
/*     this->cm = cm; */
/*   } */
/* }; */

/**
 * Internal class. This class listens to replies asynchronously returned by
 * the user proxy for a message consumer.
 */
class MessageConsumerListener : public virtual ReplyListener, Synchronized {
 private:
  enum {INIT, RUN, DONE, CLOSE};
  int status;

  RequestMultiplexer* mtpx;

  AbstractRequest* req;
  AbstractReply* reply;

  MessageListener* listener;

 public:
  MessageConsumerListener(RequestMultiplexer* mtpx, MessageListener* listener);

 /**
  * Called by Session.
  */
  void request(AbstractRequest* req);
  AbstractReply* getReply();
  void abortRequest();
  void close() throw(XoramException);

  MessageListener* getMessageListener();

 /**
  * Called by RequestMultiplexer (ReplyListener interface).
  */
  int getRequestId();
  boolean replyReceived(AbstractReply* reply) throw (AbortedRequestException);
  void replyAborted();
};

/**
 *  A Session object is a single-threaded context for producing and consuming
 * messages. A session serves several purposes:
 * <ul>
 * <li>It is a factory for its message producers and consumers.
 * <li>It is a factory for Joram specific message.
 * <li>It defines a serial order for the messages it consumes and the messages
 * it produces.
 * <li>It retains messages it consumes until they have been acknowledged.
 * <li>It serializes execution of message listeners registered with its message
 * consumers.
 * </ul>
 *  A session can create and service multiple message producers and consumers.
 */
class Session : public Daemon {
 private:
  /** The connection the session belongs to. */
  Connection* cnx;

  enum {NONE, STOP, START, CLOSE};
  int status;

  /** The identifier of the session. */
  char* ident;

  /** <code>true</code> if the session is transacted. */
  boolean transacted;

  /** The acknowledgement mode of the session. */
  int acknowledgeMode;

  const static boolean receiveAck = FALSE;

  /** <code>true</code> if the session's acknowledgements are automatic. */
  const static boolean autoAck = FALSE;

  /** Vector of message consumers. */
  Vector<MessageConsumer>* consumers;

  /** Vector of message producers. */
  Vector<MessageProducer>* producers;

  /** The message consumer currently making a request (null if none). */
  MessageConsumer* pendingMessageConsumer;

  /** FIFO queue holding the asynchronous server deliveries. */
/*   XQueue<MessageListenerContext>* repliesIn; */

  /** Counter of message listeners. */
  int listenerCount;

  /** 
   * Table holding the <code>ProducerMessages</code> holding producers'
   * messages and destinated to be sent at commit.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> <code>ProducerMessages</code>
   */
/*   Hashtable sendings; */

  /** 
   * Table holding the identifiers of the messages delivered per
   * destination or subscription, and not acknowledged.
   * <p>
   * <b>Key:</b> destination or subscription name<br>
   * <b>Object:</b> <code>MessageAcks</code> instance
   */
/*   Hashtable deliveries; */

  /**
   * The request multiplexer used to communicate with the user proxy.
   */
  RequestMultiplexer* mtpx;

  /**
   * The requestor used by the session to communicate with the user proxy.
   */
  Requestor* requestor;

  /**
   * The requestor used by the session to make 'receive' with the user
   * proxy. This second requestor is necessary because it must be closed
   * during the session close (see method close).
   */
  Requestor* receiveRequestor;

  /**
   * The message consumer currently making a request (null if none).
   */
/*   MessageConsumer* pendingMessageConsumer; */

  /**
   * The current active control thread.
   */
/*   Thread singleThreadOfControl; */

/*   friend void MessageConsumer::onMessage(); */

 public:
  /**
   *  With this acknowledgment mode, the session automatically acknowledges a
   * client's receipt of a message either when the session has successfully
   * returned from a call to receive or when the message listener the session
   * has called to process the message successfully returns.
   */
  static const byte AUTO_ACKNOWLEDGE = 0;
  /**
   *  With this acknowledgment mode, the client needs to acknowledge
   * explicitly any consumed message by calling the message's acknowledge
   * method.
   */
  static const byte CLIENT_ACKNOWLEDGE = 1;
  /**
   *  This acknowledgment mode instructs the session to lazily acknowledge
   * the delivery of messages. This is likely to result in the delivery of
   * some duplicate messages if the Joram server fails
   */
  static const byte DUPS_OK_ACKNOWLEDGE = 2;
  /**
   *  This value is returned from the method getAcknowledgeMode if the session
   * is transacted.
   */
  static const byte SESSION_TRANSACTED = 3;

  /**
   * Initializes a session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  Session(Connection* cnx, 
          boolean transacted,
          int acknowledgeMode,
          RequestMultiplexer* mtpx) throw (XoramException);

  ~Session();

  /**
   * Returns the connection the session belongs to.
   *
   * @return	the connection's object.
   */
  Connection* getConnection();

  /**
   *  Non API method, should be hidden. Returns the request multiplexer
   * allowing the communication with the Joram server.
   *
   * @return	the connection's request multiplexer.
   */
  RequestMultiplexer* getRequestMultiplexer();

  /**
   * Creates a MessageProducer to send messages to the specified destination.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception XoramException  If the creation fails for any other reason.
   */
  MessageProducer* createProducer(Destination *dest);

  /**
   *  Creates a MessageConsumer for the specified destination using a
   * message selector. A client uses a MessageConsumer object to receive
   * messages that have been published to a destination.
   *
   * @param dest	the Destination to access.
   * @param selector	only messages with properties matching the message
   *			selector expression are delivered. A value of null or
   *			an empty string indicates that there is no message
   *			selector for the message consumer.
   * @param nolocal	if true, and the destination is a topic, inhibits the
   * 			delivery of messages published by its own connection.
   * @return		the message consumer
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception XoramException  If the creation fails for any other reason.
   */
  MessageConsumer* createConsumer(Destination* dest,
                                  char* selector,
                                  boolean nolocal);

  /**
   *  Creates a MessageConsumer for the specified destination. A client uses a
   * MessageConsumer object to receive messages that have been published to a
   * destination.
   *
   * @param dest	the Destination to access.
   * @return		the message consumer
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception XoramException  If the creation fails for any other reason.
   */
  MessageConsumer* createConsumer(Destination* dest);

  /**
   * Creates a durable subscriber to the specified topic.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception XoramException  If the creation fails for any other reason.
   */
  MessageConsumer* createDurableSubscriber(Topic* topic, char* subname);

  /**
   * Creates a durable subscriber to the specified topic, using a
   * message selector and specifying whether messages published by its
   * own connection should be delivered to it.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception XoramException  If the creation fails for any other reason.
   */
  MessageConsumer* createDurableSubscriber(Topic* topic, char* subname,
                                           char* selector,
                                           boolean noLocal);
      
  Topic* createTopic(char* topicName);

  TemporaryTopic* createTemporaryTopic();

  /**
   * Unsubscribes a durable subscription that has been created by a client.
   */
  void unsubscribe(char* name);

  /**
   * Creates a Message object.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  Message* createMessage();

  /**
   * Returns the acknowledgement mode of the session.
   */
  int getAcknowledgeMode();

  /**
   * Indicates whether the session is in transacted mode.
   */
  boolean getTransacted();

  /**
   * Commits all messages done in this transaction.
   * Always throw a NotYetImplementedException.
   */
  void commit();

  /**
   * Rolls back any messages done in this transaction.
   * Always throw a NotYetImplementedException.
   */
  void rollback();

  /**
   * Starts the asynchronous deliveries in the session.
   */
  void start();

  /**
   * Stops the asynchronous deliveries processing in the session.
   * <p>
   * This method must be carefully used. When the session is stopped, the
   * connection might very well going on pushing deliveries in the
   * session's queue. If the session is never re-started, these deliveries
   * will never be poped out, and this may lead to a situation of consumed
   * but never acknowledged messages.
   * <p>
   * This fatal situation never occurs as the <code>stop()</code> method is
   * either called by he <code>Session.close()</code>
   * and <code>Connection.stop()</code> methods, which first empties the
   * session's deliveries and forbid any further push.
   */
  void stop();

  /**
   * Closes the session.
   *
   * @exception XoramException
   */
  void close();

  /* -- Internals methods -- */

  /**
   * Non API method, should be hidden.
   */
  void send(Message* msg, Destination* dest,
            int deliveryMode,
            int priority,
            long timeToLive) throw (XoramException);

  /**
   * Non API method, should be hidden.
   */
  void addProducer(MessageProducer* prod);

   /**
   * Non API method, should be hidden.
   */
  void closeProducer(MessageProducer* prod);

  /**
   * Non API method, should be hidden.
   */
  Message* receive(long timeOut1, long timeOut2,
                   MessageConsumer* cons,
                   char* targetName,
                   char* selector, boolean queueMode) throw (XoramException);

  /**
   * Non API method, should be hidden.
   */
  void addConsumer(MessageConsumer* cons);

  /**
   * Non API method, should be hidden.
   */
  void closeConsumer(MessageConsumer* cons) throw (XoramException);

  /**
   * Non API method, should be hidden.
   */
  AbstractReply* syncRequest(AbstractRequest* request) throw (XoramException);

  /**
   * Non API method, should be hidden. Called by MessageConsumer.
   */
  void addMessageListener(MessageConsumerListener* mcl);

  /**
   * Non API method, should be hidden. Called by MessageConsumer.
   * The thread of control and the status should be checked if the call results
   * from a setMessageListener but not from a close.
   */
  void removeMessageListener(MessageConsumerListener* mcl);

  /**
   * Non API method, should be hidden.
   */
  void run();

  /**
   * Non API method, should be hidden.
   */
  void distribute(AbstractReply* asyncReply);
};

/**
 *  A client uses a MessageProducer object to send messages to a destination.
 * A MessageProducer object is created by calling the createProducer method on
 * the session object. A message producer is normally dedicated to a unique
 * destination.
 * <br>
 *  A client also has the option of creating a message producer without
 * supplying a unique destination. In this case, a destination must be
 * provided with every send operation. 
 * <br>
 *  A client can specify a default delivery mode, priority, and time to live
 * for messages sent by a message producer. It can also specify the delivery
 * mode, priority, and time to live for each individual message.
 *
 * @see Session.createProducer
 */
class MessageProducer {
 private:
  /** Default delivery mode: PERSISTENT. */
  int deliveryMode;

  /** Default priority: 4. */
  int priority;

  /** Default time to live: 0, messages never expire. */
  long timeToLive;

  /** <code>true</code> if the producer is closed. */
  boolean closed;

  /** The session the producer belongs to. */
  Session* session;

  /** The destination the producer sends messages to. */
  Destination* dest;

 public:
  /**
   * Non API method, should be hidden. Constructs a producer.
   *
   * @param session  The session the producer belongs to.
   * @param dest     The destination the producer sends messages to.
   *
   * @exception IllegalStateException
   *	If the connection is broken.
   * @exception XoramException
   *	If the creation fails for any other reason.
   */
  MessageProducer(Session* session, 
                  Destination* dest) throw (IllegalStateException);

  ~MessageProducer();

  /**
   * Sets the producer's default delivery mode.
   * Delivery mode is set to PERSISTENT by default.
   *
   * @param deliveryMode	the message delivery mode for this producer.
   *
   * @exception IllegalStateException
   *  	If the producer is closed.
   * @exception IllegalArgumentException
   *	When setting an invalid delivery mode.
   */
  void setDeliveryMode(int deliveryMode) throw (IllegalStateException, IllegalArgumentException);

  /**
   *  Sets the producer's default priority.
   *  There is ten levels of priority value, with 0 as the lowest priority and
   * 9 as the highest.
   *  Priority is set to 4 by default.
   *
   * @param priority	the message priority for this producer.
   *
   * @exception IllegalStateException  
   *	If the producer is closed.
   * @exception IllegalArgumentException
   *	When setting an invalid priority.
   */
  void setPriority(int priority) throw (IllegalStateException, IllegalArgumentException);

  /**
   *  Sets the default duration of time in milliseconds that a produced
   * message should be retained by the provider. Time to live is set to
   * zero by default.
   *
   * @param timeToLive	the message time to live in milliseconds; zero is 
   *			unlimited.
   *
   * @exception IllegalStateException  
   *	If the producer is closed.
   */
  void setTimeToLive(long timeToLive) throw (IllegalStateException);

  /**
   * Gets the destination associated with this MessageProducer.
   *
   * @return this producer's destination.
   *
   * @exception IllegalStateException
   *	If the producer is closed.
   */
  Destination* getDestination() throw (IllegalStateException);

  /**
   * Gets the producer's default delivery mode.
   *
   * @return this producer's default delivery mode.
   *
   * @exception IllegalStateException  
   *	If the producer is closed.
   */
  int getDeliveryMode() throw (IllegalStateException);

  /**
   * Gets the producer's default priority.
   *
   * @return this producer's default priority.
   * @exception IllegalStateException  
   *	If the producer is closed.
   */
  int getPriority() throw (IllegalStateException);

  /**
   * Gets the default duration in milliseconds that a produced message
   * should be retained by the provider.
   *
   * @return the message time to live in milliseconds.
   *
   * @exception IllegalStateException  
   *	If the producer is closed.
   */
  long getTimeToLive() throw (IllegalStateException);

  /**
   * Sends a message with the default delivery parameters.
   *
   * @param msg the message to send.
   *
   * @exception InvalidDestinationException
   *	If the destinationInvalidDestinationException is unidentified.
   * @exception IllegalStateException
   *	If the producer is closed, or if the connection is broken.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  void send(Message* msg) throw (XoramException);

  /**
   * Sends a message to a destination for an unidentified message producer.
   * Uses the default delivery parameters.
   *
   * @param msg		the message to send.
   * @param dest 	the destination to send this message to.
   *
   * @exception InvalidDestinationException
   *	If the destinationInvalidDestinationException is unidentified.
   * @exception IllegalStateException
   *	If the producer is closed, or if the connection is broken.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  void send(Message* msg, Destination* dest) throw (XoramException);

  /**
   * Sends a message with the given delivery parameters.
   *
   * @param msg 	 the message to send.
   * @param deliveryMode the delivery mode to use
   * @param priority	 the priority for this message
   * @param timeToLive	 the message's lifetime (in milliseconds)
   *
   * @exception InvalidDestinationException
   *	If the destinationInvalidDestinationException is unidentified.
   * @exception IllegalStateException
   *	If the producer is closed, or if the connection is broken.
   * @exception SecurityException
   *	If the user if not a WRITER for the specified destination.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  void send(Message* msg, Destination* dest,
            int deliveryMode,
            int priority,
            long timeToLive) throw (XoramException);

  /**
   * Closes the message producer.
   */
  void close();
};

/**
 *  A MessageListener object is used to asynchronously receive delivered
 * messages.
 * <br>
 * Each session must insure that it passes messages serially to the listener.
 * This means that a listener assigned to one or more consumers of the same
 * session can assume that the onMessage method is not called with the next
 * message until the session has completed the last call.
 */
class MessageListener {
 public:
  /**
   * Passes a message to the listener.
   *
   * @param msg	the message passed to the listener.
   */
  virtual void onMessage(Message* msg) =0;
};

/**
 *  A client uses a MessageConsumer object to receive messages from a
 * destination. A MessageConsumer object is created by calling the
 * createConsumer method on a session object. A message consumer is
 * normally dedicated to a unique destination.
 * <br>
 * A message consumer can be created with a message selector. A message
 * selector allows the client to restrict the messages delivered to the
 * message consumer to those that match the selector.
 * <br>
 * A client may either synchronously receive a message consumer's messages or
 * have the consumer asynchronously deliver them as they arrive:<ul>
 * <li>For synchronous receipt, a client can request the next message from
 * the message consumer using one of its receive methods. There are several
 * variations of receive that allow a client to poll or wait for the next
 * message.
 * <li>For asynchronous delivery, a client must register a MessageListener
 * object with a message consumer. As messages arrive at the message consumer,
 * it delivers them by calling the MessageListener's onMessage method.
 * </ul>
 *It is a client programming error for a MessageListener to throw an exception.
 */
class MessageConsumer {
 private:
  /** <code>true</code> if the producer is closed. */
  boolean closed;

  /** The session the producer belongs to. */
  Session* session;

  /** The destination the producer sends messages to. */
  Destination* dest;

  /** <code>true</code> for a durable subscriber. */
  boolean durableSubscriber;

  /** The selector for filtering messages. */
  char* selector;

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  boolean noLocal;

  /** 
   * The consumer server side target is either a queue or a subscription on
   * its proxy.
   */
  char* targetName;

  /** <code>true</code> if the consumer is a queue consumer. */
  boolean queueMode;

  /**
   * Message listener context (null if no message listener).
   */
  MessageConsumerListener* mcl;

  void onMessage();

 public:
  /**
   * Non API method, should be hidden.
   * Constructs a MessageConsumer for the specified destination using a
   * message selector.
   *
   * @param session   The session the producer belongs to.
   * @param dest      The destination the producer sends messages to.
   * @param selector  The selector to filter incoming messages.
   *
   * @exception IllegalStateException
   *	If the connection is broken.
   * @exception XoramException
   *	If the creation fails for any other reason.
   */
  MessageConsumer(Session* session, 
                  Destination* dest,
                  char* selector) throw (XoramException);

  /**
   * Non API method, should be hidden.
   * Constructs a MessageConsumer for the specified destination.
   *
   * @param session   The session the producer belongs to.
   * @param dest      The destination the producer sends messages to.
   * @param selector  The selector to filter incoming messages.
   *
   * @exception IllegalStateException
   *	If the connection is broken.
   * @exception XoramException
   *	If the creation fails for any other reason.
   */
  MessageConsumer(Session* session, 
                  Destination* dest,
                  char* selector,
                  char* subName, 
                  boolean nolocal) throw (XoramException);

  ~MessageConsumer();

  /**
   * Non API method, should be hidden.
   */
  char* getTargetName();

  /**
   * Non API method, should be hidden.
   */
  boolean getQueueMode();

  /**
   * Sets the message consumer's MessageListener.
   * It is forbidden to call this method while messages are being consumed
   * by an existing listener or the consumer is being used to consume messages
   * synchronously is undefined.
   * <br>
   * This mode is currently not supported.
   *
   * @param listener the message consumer's MessageListener.
   *
   * @exception IllegalStateException  If the consumer is closed, or if the
   *              connection is broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  void setMessageListener(MessageListener* listener) throw (XoramException);

  /**
   * Gets the message consumer's MessageListener.
   *
   * @return the listener for the message consumer, or null if none is set.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  MessageListener* getMessageListener() throw (XoramException);

  /**
   * Gets this message consumer's message selector expression.
   *
   * @return this message consumer's message selector, or null if no message
   * 	     selector exists for the message consumer.
   *
   * @exception IllegalStateException  If the consumer is closed.
   */
  char* getMessageSelector() throw (XoramException);

  /** 
   * Receives the next message produced for this message consumer.
   * This call blocks indefinitely until a message is produced or until
   * this message consumer is closed.
   *
   * @return the next message produced for this message consumer, or null
   *	     if this message consumer is concurrently closed.
   *
   * @exception IllegalStateException
   *	If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *	If the requester is not a READER on the destination.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  Message* receive() throw (XoramException);

  /** 
   * Receives the next message that arrives before the specified timeout.
   * This call blocks until a message arrives, the timeout expires, or this
   * message consumer is closed. A timeout of zero never expires, and the call
   * blocks indefinitely.
   *
   * @param timeOut	the timeout value in milliseconds.
   *
   * @return the next message produced for this message consumer, or null
   *	     if the timeout expires or this message consumer is concurrently
   *	     closed.
   *
   * @exception IllegalStateException
   *	If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *	If the requester is not a READER on the destination.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  Message* receive(long timeOut) throw (XoramException);

  /** 
   * Receives the next message if one is immediately available.
   *
   * @return the next message produced for this message consumer, or null
   *	     if none is available.
   * 
   * @exception IllegalStateException
   *	If the consumer is closed, or if the connection is broken.
   * @exception SecurityException
   *	If the requester is not a READER on the destination.
   * @exception XoramException
   *	If the request fails for any other reason.
   */
  Message* receiveNoWait() throw (XoramException);

  /**
   * Closes the message consumer.
   */
  void close();
};


class TemporaryTopic : public Topic {
 private:
  Requestor* requestor;

 public:
  /**
   * Creates a TemporaryTopic object.
   */
  TemporaryTopic(char* agentId,  Requestor* requestor);

  ~TemporaryTopic();

  void destroy();

  Requestor* getRequestor();

  boolean isTemporaryTopic();
};
#endif /* XORAM_H */

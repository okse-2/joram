/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
#ifndef ABSTRACT_MESSAGE_H
#define ABSTRACT_MESSAGE_H

#include "Types.H"
#include "Vector.H"
#include "XStream.H"

class AbstractRequest;
class AbstractReply;

//class UnknownClass {};

/**
 * An <code>AbstractMessage</code> is a message exchanged between a
 * Xoram client and its (Joram) proxy.
 */
class AbstractMessage : Streamable {
 protected:
  int classid;

 public:
  AbstractMessage();

  enum {
    NULL_CLASS_ID = -1,
    CNX_CONNECT_REQUEST=0,
    CNX_CONNECT_REPLY=1,
    CNX_START_REQUEST=2,
    CNX_STOP_REQUEST=3,
    CNX_CLOSE_REQUEST=4,
    CNX_CLOSE_REPLY=5,
    PRODUCER_MESSAGES=6,
    CONSUMER_RECEIVE_REQUEST=7,
    CONSUMER_MESSAGES=8,
    CONSUMER_SUB_REQUEST=9,
    CONSUMER_UNSUB_REQUEST=10,
    CONSUMER_ACK_REQUEST=11,
    CONSUMER_DENY_REQUEST=12,
    SESS_ACK_REQUEST=13,
    SESS_DENY_REQUEST=14,
    MOM_EXCEPTION_REPLY=15,
    SERVER_REPLY=16,
    ACTIVATE_CONSUMER_REQUEST = 17,
//    COMMIT_REQUEST = 18,
    CONSUMER_CLOSE_SUB_REQUEST = 19,
//    CONSUMER_SET_LIST_REQUEST = 20,
//    CONSUMER_UNSET_LIST_REQUEST = 21,
    GET_ADMIN_TOPIC_REPLY = 22,
    GET_ADMIN_TOPIC_REQUEST = 23,
//    JMS_REQUEST_GROUP = 24,
//    PING_REQUEST = 25,
//    QBROWSE_REPLY = 26,
//    QBROWSE_REQUEST = 27,
    //SESS_CREATE_TDREPLY = 28,
    SESS_CREATE_DEST_REPLY = 28,
    SESS_CREATE_DEST_REQUEST = 29,
//    SESS_CREATE_TQREQUEST = 29,
    XXX_CREATE_TTREQUEST = 30,
    TEMP_DEST_DELETE_REQUEST = 31
//    XA_CNX_COMMIT = 32,
//    XA_CNX_PREPARE = 33,
//    XA_CNX_RECOVER_REPLY = 34,
//    XA_CNX_RECOVER_REQUEST = 35,
//    XA_CNX_ROLLBACK = 36
  };

  boolean instanceof(int classid);
  int getClassId();

  // ==================================================
  // Streamable interface
  // ==================================================

  static void write(AbstractMessage* msg,
                    OutputStream* os) throw (IOException);
  static AbstractMessage* read(InputStream *is) throw (IOException);
};

/**
 * An <code>AbstractRequest</code> is a request sent by a Xoram client
 * to its (Joram) proxy.
 */
class AbstractRequest : public AbstractMessage {
 protected:
  /**
   * Identifier of the request.
   */
  int requestId;

  /**
   * The request target is either a destination agent name, or a subscription
   * name.
   */
  char* target;

 public:
  AbstractRequest();
  /**
   * Constructs an <code>AbstractRequest</code>.
   *
   * @param target  String identifier of the request target, either a queue
   *          name, or a subscription name.
   */
  AbstractRequest(char* target);
  ~AbstractRequest();

  /**
   * Sets the request identifier.
   */
  void setRequestId(int requestId);

  /** Returns the request identifier. */
  int getRequestId();

  /** Sets the request target name. */
  void setTarget(char* target);

  /** Returns the request target name.  */
  char* getTarget();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * An <code>AbstractReply</code> is sent by a Joram proxy to a Xoram client as a
 * reply to an <code>AbstractRequest</code>.
 */
class AbstractReply : public AbstractMessage {
 protected:
  /** Identifier of the replied request. */
  int correlationId;

 public:
  AbstractReply();

  /**
   * Constructs an <code>AbstractReply</code>.
   *
   * @param correlationId  Identifier of the replied request.
   */
  AbstractReply(int correlationId);

  /** Sets the replied request identifier. */
  void setCorrelationId(int correlationId);

  /** Returns the replied request identifier. */
  int getCorrelationId();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>CnxConnectRequest</code> is sent by an opening
 * <code>Connection</code>.
 */
class CnxConnectRequest : public AbstractRequest {
 public:
  /**
   * Constructs a <code>CnxConnectRequest</code>.
   */
  CnxConnectRequest();
  ~CnxConnectRequest();
};

/**
 * A <code>CnxConnectReply</code> is sent by a Joram proxy as a reply to a
 * connection <code>CnxConnectRequest</code> and holds the connection's key
 * and the proxy identifier.
 */
class CnxConnectReply : public AbstractReply {
 private:
  /** The connection's key. */
  int cnxKey;

  /** The proxy's identifier. */
  char* proxyId;

 public:
  CnxConnectReply();
  /**
   * Constructs a <code>CnxConnectReply</code>.
   *
   * @param req  The replied request.
   * @param cnxKey  The connection's key.
   * @param proxyId  The proxy's identifier.
   */
  CnxConnectReply(CnxConnectRequest req, int cnxKey, char*  proxyId);

  ~CnxConnectReply();

  /** Sets the connection key. */
  void setCnxKey(int cnxKey);

  /** Returns the connection's key. */
  int getCnxKey();

  /** Sets the proxy's identifier */
  void setProxyId(char* proxyId);

  /** Returns the proxy's identifier */
  char* getProxyId();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>CnxStartRequest</code> request is sent by a starting
 * <code>Connection</code>.
 */
class CnxStartRequest : public AbstractRequest {
 public:
  /** Constructs a <code>CnxStartRequest</code> instance. */
  CnxStartRequest();
  ~CnxStartRequest();
};

/**
 * A <code>CnxStopRequest</code> is sent by a stopping <code>Connection</code>.
 */
class CnxStopRequest : public AbstractRequest {
 public:
  /** Constructs a <code>CnxStopRequest</code> instance. */
  CnxStopRequest();
  ~CnxStopRequest();
};

/**
 * A <code>CnxCloseRequest</code> is sent by a closing connection.
 */
class CnxCloseRequest : public AbstractRequest {
 public:
  /** Constructs a <code>CnxCloseRequest</code> instance. */
  CnxCloseRequest();
  ~CnxCloseRequest();
};

/**
 * A <code>CnxCloseReply</code> replies to a <code>CnxCloseRequest</code>.
 */
class CnxCloseReply : public AbstractReply {
 public:
  /** Constructs a <code>CnxCloseReply</code> instance. */
  CnxCloseReply();
  ~CnxCloseReply();
};

class Message;

/**
 * A <code>ProducerMessages</code> instance is sent by a
 * <code>MessageProducer</code> when sending messages.
 */
class ProducerMessages : public AbstractRequest {
 private:
  /** The wrapped messages. */
  Vector<Message>* messages;

  /**
   * Indicates whether the produced messages are asynchronously send or not.
   */
  boolean asyncSend;

 public:
  /**
   * Constructs a <code>ProducerMessages</code> instance.
   */
  ProducerMessages();

  /**
   * Constructs a <code>ProducerMessages</code> instance.
   *
   * @param dest  Name of the destination the messages are sent to.
   */
  ProducerMessages(char* dest);

  /**
   * Constructs a <code>ProducerMessages</code> instance carrying a single
   * message.
   *
   * @param dest  Name of the destination the messages are sent to.
   * @param msg  Message to carry.
   */
  ProducerMessages(char* dest, Message* msg);

  ~ProducerMessages();

  /** Returns the produced messages. */
  Vector<Message>* getMessages();

  /** Adds a message to deliver. */
  void addMessage(Message* msg);

  /** Adds messages to deliver. */
  void addMessages(Vector<Message>* msgs);

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  virtual void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  virtual void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerReceiveRequest</code> is sent by a
 * <code>MessageConsumer</code> when requesting a message.
 */
class ConsumerReceiveRequest : public AbstractRequest {
 private:
  /** The selector for filtering messages. */
  char* selector;

  /** The time to live value of the request (negative for infinite). */
  long long timeToLive;

  /** <code>true</code> if the messages come from a queue. */
  boolean queueMode;

  boolean receiveAck;

 public:
  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   */
  ConsumerReceiveRequest();

  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  The selector for filtering messages, if any.
   * @param timeToLive  Time to live value in milliseconds, negative for
   *          infinite.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  ConsumerReceiveRequest(char* targetName, char* selector,
                         long long timeToLive, boolean queueMode);

  ~ConsumerReceiveRequest();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  virtual void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  virtual void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerMessages</code> is used by a Joram proxy for sending messages
 * to a consumer.
 */
class ConsumerMessages : public AbstractReply {
 private:
  /** Wrapped messages. */
  Vector<Message>* messages;

  /** Name of the subscription or the queue the messages come from. */
  char* comingFrom;

  /** <code>true</code> if the messages come from a queue. */
  boolean queueMode;

 public:
  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   */
  ConsumerMessages();

  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param message  Message to wrap.
   * @param comingFrom  Name of the queue or the subscription the message
   *          come from.
   * @param queueMode  <code>true</code> if the message come from a queue.
   */
  ConsumerMessages(int correlationId, Message* message,
                   char* comingFrom, boolean queueMode);

  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param messages  Messages to wrap.
   * @param comingFrom  Name of the queue or the subscription the messages
   *          comes from.
   * @param queueMode  <code>true</code> if the messages come from a queue.
   */
  ConsumerMessages(int correlationId, Vector<Message>* messages,
                   char* comingFrom, boolean queueMode);

  /**
   * Constructs an empty <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  Reply identifier.
   * @param comingFrom  Name of the queue or the subscription the reply
   *          comes from.
   * @param queueMode  <code>true</code> if it replies to a queue consumer.
   */
   ConsumerMessages(int correlationId, char* comingFrom, boolean queueMode);

   ~ConsumerMessages();

  /** Returns the messages to deliver. */
   Vector<Message>* getMessages() ;

   void addMessage(Message* msg);

   int getMessageCount();

  /**
   * Returns the name of the queue or the subscription the messages come
   * from.
   */
    char* comesFrom();

    void setComesFrom(char* comingFrom);

  /** Returns <code>true</code> if the messages come from a queue. */
    boolean getQueueMode();

    void setQueueMode(boolean queueMode);

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  virtual void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  virtual void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerSubRequest</code> is sent by a constructing
 * <code>MessageConsumer</code> destinated to consume messages on a topic.
 */
class ConsumerSubRequest : public AbstractRequest {
 private:
  /** The subscription's name. */
  char* subName;

  /** The selector for filtering messages. */
  char* selector;

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  boolean noLocal;

  /** <code>true</code> if the subscription is durable. */
  boolean durable;

  /** <code>true</code> if the subscription is asynchrone. */
  boolean asyncSub;

 public:
  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   */
  ConsumerSubRequest();

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   */
  ConsumerSubRequest(char* topic, char* subName,
                     char* selector,
                     boolean noLocal,
                     boolean durable);

                       /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   * @param asyncSub <code>true</code> if the subscription is asynchrone.
   */
  ConsumerSubRequest(char* topic, char* subName,
                     char* selector,
                     boolean noLocal,
                     boolean durable,
                     boolean asyncSub);

  ~ConsumerSubRequest();

  /** Sets the subscription name. */
  void setSubName(char* subName);

  /** Returns the name of the subscription. */
  char* getSubName();

  /** Sets the selector. */
  void setSelector(char* selector);

  /** Returns the selector for filtering the messages. */
  char* getSelector();

  /** Sets the noLocal attribute. */
  void setNoLocal(boolean noLocal);

  /** Returns <code>true</code> for not consuming the local messages. */
  boolean getNoLocal() ;

  /** Sets the durable attribute. */
  void setDurable(boolean durable);

  /** Returns <code>true</code> for a durable subscription. */
  boolean getDurable();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  virtual void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  virtual void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerUnsubRequest</code> is sent by a closing temporary
 * <code>MessageConsumer</code> on a topic, or by a <code>Session</code>
 * unsubscribing a durable subscriber.
 */
class ConsumerUnsubRequest : public AbstractRequest {
 public:
  /**
   * Constructs a <code>ConsumerUnsubRequest</code>.
   */
  ConsumerUnsubRequest();

  /**
   * Constructs a <code>ConsumerUnsubRequest</code>.
   *
   * @param subName  The name of the subscription to delete.
   */
  ConsumerUnsubRequest(char* subName);

  ~ConsumerUnsubRequest();
};

/**
 * A <code>ConsumerAckRequest</code> instance is used by a
 * <code>MessageConsumer</code> for acknowledging a received message.
 */
class ConsumerAckRequest : public AbstractRequest {
 private:
  /** Vector of message identifiers. */
  Vector<char>* ids;

  /** <code>true</code> if the request is destinated to a queue. */
  boolean queueMode;

 public:
  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   */
  ConsumerAckRequest();

  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   */
  ConsumerAckRequest(char* targetName, boolean queueMode);

  ~ConsumerAckRequest();

  /** Sets the vector of identifiers. */
  void setIds(Vector<char>* ids);

  void addId(char* id);

  /** Returns the vector of acknowledged messages identifiers. */
  Vector<char>* getIds();

  /** Sets the target destination type. */
  void setQueueMode(boolean queueMode);

  /** Returns <code>true</code> if the request is destinated to a queue. */
  boolean getQueueMode();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerDenyRequest</code> instance is used by a
 * <code>MessageConsumer</code> for denying a received message.
 */
class ConsumerDenyRequest : public AbstractRequest {
 private:
  /** Vector of message identifiers. */
  char* id;

  /** <code>true</code> if the request is destinated to a queue. */
  boolean queueMode;

  /** <code>true</code> if the request must not be acked by the server. */
  boolean doNotAck;

 public:
  /**
   * Public no-arg constructor needed by Externalizable.
   */
  ConsumerDenyRequest();

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  ConsumerDenyRequest(char* targetName, char* id, boolean queueMode);

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  ConsumerDenyRequest(char* targetName, char* id, boolean queueMode, boolean doNotAck);

  ~ConsumerDenyRequest();

  /** Sets the identifier. */
  void setId(char* id);

  /** Returns the vector of denyed messages identifiers. */
  char* getId();

  /** Sets the target destination type. */
  void setQueueMode(boolean queueMode);

  /** Returns <code>true</code> if the request is destinated to a queue. */
  boolean getQueueMode();

  /** Sets the server ack policy. */
  void setDoNotAck(boolean doNotAck);

  /**
   * Returns <code>true</code> if the request must not be acked by the
   * server.
   */
  boolean getDoNotAck();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>SessAckRequest</code> instance is used by a <code>Session</code>
 * for acknowledging the messages it consumed.
 */
class SessAckRequest : public AbstractRequest {
 private:
  /** Vector of message identifiers. */
  Vector<char>* ids;

  /** <code>true</code> if the request is destinated to a queue. */
  boolean queueMode;

 public:
  /**
   * Constructs a <code>SessAckRequest</code> instance.
   */
  SessAckRequest();

  /**
   * Constructs a <code>SessAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of acknowledged message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  SessAckRequest(char* targetName,
                 Vector<char>* ids,
                 boolean queueMode);

  ~SessAckRequest();

  /** Sets the vector of identifiers. */
  void setIds(Vector<char>* ids);

  void addId(char* id);

  /** Returns the vector of acknowledged messages identifiers. */
  Vector<char>* getIds();

  /** Sets the target destination type. */
  void setQueueMode(boolean queueMode);

  /** Returns <code>true</code> if the request is destinated to a queue. */
  boolean getQueueMode();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>SessDenyRequest</code> instance is used by a <code>Session</code>
 * for denying the messages it consumed.
 */
class SessDenyRequest : public AbstractRequest {
 private:
  /** Vector of message identifiers. */
  Vector<char>* ids;

  /** <code>true</code> if the request is destinated to a queue. */
  boolean queueMode;

  /** <code>true</code> if the request must not be acked by the server. */
  boolean doNotAck;

 public:
  /**
   * Public no-arg constructor needed by Externalizable.
   */
  SessDenyRequest();

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  SessDenyRequest(char* targetName, Vector<char>* ids, boolean queueMode);

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  SessDenyRequest(char* targetName, Vector<char>* ids, boolean queueMode, boolean doNotAck);

  ~SessDenyRequest();

  /** Sets the vector of identifiers. */
  void setIds(Vector<char>* ids);

  void addId(char* id);

  /** Returns the vector of denyed messages identifiers. */
  Vector<char>* getIds();

  /** Sets the target destination type. */
  void setQueueMode(boolean queueMode);

  /** Returns <code>true</code> if the request is destinated to a queue. */
  boolean getQueueMode();

  /** Sets the server ack policy. */
  void setDoNotAck(boolean doNotAck);

  /**
   * Returns <code>true</code> if the request must not be acked by the
   * server.
   */
  boolean getDoNotAck();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>MomExceptionReply</code> instance is used by a JMS client proxy
 * to send a <code>MomException</code> back to a JMS client.
 */
class MomExceptionReply : public AbstractReply {
 private:
  /** The wrapped exception type. */
  int type;

  /** The wrapped exception message. */
  char* message;

 public:
  enum {
    MomException = 1,
    AccessException = 2,
    DestinationException = 3,
    MessageException = 4,
    MessageROException = 5,
    MessageValueException = 6,
    RequestException = 7,
    SelectorException = 8,
    StateException = 9
  };

  /**
   * Constructs an empty <code>MomExceptionReply</code> instance.
   */
  MomExceptionReply();

  ~MomExceptionReply();

  /** Returns thewrapped exception type. */
  int getType();

  /** Returns the wrapped exception message. */
  char* getMessage();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  virtual void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  virtual void readFrom(InputStream* is) throw(IOException);
};

class ServerReply : public AbstractReply {
 public:
  ServerReply();
  ~ServerReply();
};

/**
 * A <code>SessAckRequest</code> instance is used by a <code>Session</code>
 * for acknowledging the messages it consumed.
 */
class ActivateConsumerRequest : public AbstractRequest {
 private:
  /** <code>true</code> if the request is destinated to a queue. */
  boolean activate;

 public:
  /**
   * Constructs a <code>ActivateConsumerRequest</code> instance.
   */
  ActivateConsumerRequest();

  /**
   * Constructs a <code>ActivateConsumerRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of acknowledged message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  ActivateConsumerRequest(char* targetName,
                          boolean queueMode);

  ~ActivateConsumerRequest();

  boolean getActivate();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>ConsumerCloseSubRequest</code> is sent by a closing durable
 * <code>TopicSubscriber</code>.
 */
class ConsumerCloseSubRequest : public AbstractRequest {
 public:
  /**
   * Constructs a <code>ConsumerCloseSubRequest</code>.
   */
  ConsumerCloseSubRequest();

  /**
   * Constructs a <code>ConsumerCloseSubRequest</code>.
   *
   * @param subName  The name of the closing durable subscription.
   */
  ConsumerCloseSubRequest(char* subName);

  ~ConsumerCloseSubRequest();
};

/**
 * A <code>GetAdminTopicRequest</code>
 */
class GetAdminTopicRequest : public AbstractRequest {
 public:
  /**
   * Constructs a <code>GetAdminTopicRequest</code>.
   */
  GetAdminTopicRequest();

  ~GetAdminTopicRequest();
};

/**
 * A <code>GetAdminTopicReply</code>
 */
class GetAdminTopicReply : public AbstractReply {
 private:
  /** message identifiers. */
  char* id;

 public:
  /**
   * Constructs a <code>GetAdminTopicReply</code>.
   */
  GetAdminTopicReply();

  /**
   * Constructs a <code>GetAdminTopicReply</code>.
   */
  GetAdminTopicReply(GetAdminTopicRequest* req, char* id);

  ~GetAdminTopicReply();

  /** Sets the identifier. */
  void setId(char* id);

  /** Returns the vector of denyed messages identifiers. */
  char* getId();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>TempDestDeleteRequest</code>
 */
class TempDestDeleteRequest : public AbstractRequest {
 public:
  /**
   * Constructs a <code>TempDestDeleteRequest</code>.
   */
  TempDestDeleteRequest();

  /**
   * Constructs a <code>TempDestDeleteRequest</code>.
   */
  TempDestDeleteRequest(char* uid);

  ~TempDestDeleteRequest();
};

/**
 * A <code>SessCreateDestReply</code> is used by a JMS proxy for replying
 * to a <code>SessCreate&lt;TQ/TT&gt;Request</code>.
 */
class SessCreateDestReply : public AbstractReply {
 private:
  char* agentId;

 public:
  /**
   * Constructs a <code>SessCreateTDReply</code> instance.
   */
  SessCreateDestReply();

  /**
   * Constructs a <code>SessCreateDestReply</code> instance.
   *
   * @param request  The replied request.
   * @param agentId  String identifier of the destination agent.
   */
  SessCreateDestReply(AbstractRequest* req, char* agentId);

  ~SessCreateDestReply();

  /** Sets the destination identifier. */
  void setAgentId(char* agentId);

  /** Returns the temporary destination's agent identifier. */
  char* getAgentId();

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

/**
 * A <code>SessCreateDestRequest</code> is sent by a <code>Session</code>
 * for creating a destination.
 */
class SessCreateDestRequest : public AbstractRequest {
 private:
	/** Destination type, Queue or Topic, temporary or not. */
	byte type;
	/** Name of the destination if any */
	char* name;

 public:
	/**
	   * Constructs a <code>SessCreateDestRequest</code> instance.
	   */
	  SessCreateDestRequest();

  /**
   * Constructs a <code>SessCreateDestRequest</code> instance.
   */
  SessCreateDestRequest(byte type);

  /** Constructs a <code>SessCreateTQRequest</code> instance. */
  SessCreateDestRequest(byte type, char* name);

  ~SessCreateDestRequest();

  /** Returns destination's type. */
  byte getType();

  /**
   * Returns the destination name if any.
   *
   * @return  the destination name if any, null if not set.
   */
  char* getName() {
    return name;
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  void writeTo(OutputStream* os) throw(IOException);

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  void readFrom(InputStream* is) throw(IOException);
};

#endif /* ABSTRACT_MESSAGE_H */

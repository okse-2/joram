/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
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
#include "AbstractMessage.H"
#include "Message.H"

// ######################################################################
// AbstractMessage Class
// ######################################################################

AbstractMessage::AbstractMessage() {
  classid = -1;
}

int AbstractMessage::getClassId() {
  return classid;
}

boolean AbstractMessage::instanceof(int classid) {
  return (this->classid == classid);
}

void AbstractMessage::write(AbstractMessage* msg,
                            OutputStream* os) throw (IOException) {
  if (msg == (AbstractMessage*) NULL) {
    if (os->writeInt(NULL_CLASS_ID) == -1) throw IOException();
  } else {
    if (os->writeInt(msg->getClassId()) == -1) throw IOException();
    msg->writeTo(os);
  }
}

AbstractMessage* AbstractMessage::read(InputStream *is) throw (IOException) {
  int classid;
  AbstractMessage* reply = NULL;

  if (is->readInt(&classid) == -1) throw IOException();
  switch(classid) {
  case NULL_CLASS_ID:
    return (AbstractMessage*) NULL;
  case CNX_CONNECT_REQUEST:
    reply = new CnxConnectRequest();
    break;
  case CNX_CONNECT_REPLY:
    reply = new CnxConnectReply();
    break;
  case CNX_START_REQUEST:
    reply = new CnxStartRequest();
    break;
  case CNX_STOP_REQUEST:
    reply = new CnxStopRequest();
    break;
  case CNX_CLOSE_REQUEST:
    reply = new CnxCloseRequest();
    break;
  case CNX_CLOSE_REPLY:
    reply = new CnxCloseReply();
    break;
  case PRODUCER_MESSAGES:
    reply = new ProducerMessages();
    break;
  case CONSUMER_RECEIVE_REQUEST:
    reply = new ConsumerReceiveRequest();
    break;
  case CONSUMER_MESSAGES:
    reply = new ConsumerMessages();
    break;
  case CONSUMER_SUB_REQUEST:
    reply = new ConsumerSubRequest();
    break;
  case CONSUMER_UNSUB_REQUEST:
    reply = new ConsumerUnsubRequest();
    break;
  case CONSUMER_ACK_REQUEST:
    reply = new ConsumerAckRequest();
    break;
  case CONSUMER_DENY_REQUEST:
    reply = new ConsumerDenyRequest();
    break;
  case SESS_ACK_REQUEST:
    reply = new SessAckRequest();
    break;
  case SESS_DENY_REQUEST:
    reply = new SessDenyRequest();
    break;
  case MOM_EXCEPTION_REPLY:
    reply = new MomExceptionReply();
    break;
  case SERVER_REPLY:
    reply = new ServerReply();
    break;
  case ACTIVATE_CONSUMER_REQUEST:
    reply = new ActivateConsumerRequest();
    break;
  case CONSUMER_CLOSE_SUB_REQUEST:
    reply = new ConsumerCloseSubRequest();
    break;
  case GET_ADMIN_TOPIC_REPLY:
    reply = new GetAdminTopicReply();
    break;
  case GET_ADMIN_TOPIC_REQUEST:
    reply = new GetAdminTopicRequest();
    break;
  case TEMP_DEST_DELETE_REQUEST:
    reply = new TempDestDeleteRequest();
    break;
  case SESS_CREATE_DEST_REPLY:
    reply = new SessCreateDestReply();
    break;
  case SESS_CREATE_DEST_REQUEST:
    reply = new SessCreateDestRequest();
    break;

  default:
    throw UnknownClass();
  }
  reply->readFrom(is);

  return reply;
}

// ######################################################################
// AbstractRequest Class
// ######################################################################

AbstractRequest::AbstractRequest() {}

AbstractRequest::AbstractRequest(char* target) : AbstractMessage() {
  if(DEBUG)
    printf("=> AbstractRequest: target = 0x%x, target = %s\n", target, target);
  if (target != (char*) NULL) {
    char* newTarget = new char[strlen(target)+1];
    strcpy(newTarget, target);
    newTarget[strlen(target)] = '\0';
    this->target = newTarget;
  }
  else
    this->target = target;
  if(DEBUG)
    printf("<= AbstractRequest: target = 0x%x, target = %s\n", target, target);
}

AbstractRequest::~AbstractRequest() {
  if(DEBUG)
    printf("~AbstractRequest: target = 0x%x, target = %s\n", target, target);
  if (target != (char*) NULL) {
    delete[] target;
    target = (char*) NULL;
  }
}


/** 
 * Sets the request identifier. 
 */
void AbstractRequest::setRequestId(int requestId) {
  this->requestId = requestId;
}
  
/** Returns the request identifier. */
int AbstractRequest::getRequestId() {
  return requestId;
}

/** Sets the request target name. */
void AbstractRequest::setTarget(char* target) {
  this->target = target;
}

/** Returns the request target name.  */
char* AbstractRequest::getTarget() {
  return target;
}

// ====================
// Streamable interface
// ====================

void AbstractRequest::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeInt(requestId) == -1) throw IOException();
  if (os->writeString(target) == -1) throw IOException();
}

void AbstractRequest::readFrom(InputStream *is) throw (IOException) {
  if (is->readInt(&requestId) == -1) throw IOException();
  if (is->readString(&target) == -1) throw IOException();
}

// ######################################################################
// AbstractReply Class
// ######################################################################

AbstractReply::AbstractReply() : AbstractMessage() {
  this->correlationId = -1;
}

AbstractReply::AbstractReply(int correlationId) : AbstractMessage() {
  this->correlationId = correlationId;
}

/** Sets the replied request identifier. */
void AbstractReply::setCorrelationId(int correlationId) {
  this->correlationId = correlationId;
}

/** Returns the replied request identifier. */
int AbstractReply::getCorrelationId() {
  return correlationId;
}

// ====================
// Streamable interface
// ====================

void AbstractReply::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeInt(correlationId) == -1) throw IOException();
}

void AbstractReply::readFrom(InputStream *is) throw (IOException) {
  if (is->readInt(&correlationId) == -1) throw IOException();
}

// ######################################################################
// CnxConnectRequest Class
// ######################################################################

CnxConnectRequest::CnxConnectRequest() : AbstractRequest((char *) NULL) {
  classid = CNX_CONNECT_REQUEST;
}

CnxConnectRequest::~CnxConnectRequest() {
  if(DEBUG)
    printf("~CnxConnectRequest()\n");
}

// ######################################################################
// CnxConnectReply Class
// ######################################################################

CnxConnectReply::CnxConnectReply() : AbstractReply() {
  classid = CNX_CONNECT_REPLY;
}

CnxConnectReply::CnxConnectReply(CnxConnectRequest req, int cnxKey, char*  proxyId) : AbstractReply(req.getRequestId()) {
  classid = CNX_CONNECT_REPLY;
  this->cnxKey = cnxKey;
  this->proxyId = proxyId;
}

CnxConnectReply::~CnxConnectReply() {
  if(DEBUG)
    printf("~CnxConnectReply(): proxyId = 0x%x\n", proxyId);
  /*
  if (proxyId != (char*) NULL) {
    delete[] proxyId;
    proxyId = (char*) NULL;
  }
  */
}

/** Sets the connection key. */
void CnxConnectReply::setCnxKey(int cnxKey) {
  this->cnxKey = cnxKey;
}
 
/** Returns the connection's key. */
int CnxConnectReply::getCnxKey() {
  return cnxKey;
}

/** Sets the proxy's identifier */
void CnxConnectReply::setProxyId(char* proxyId) {
  this->proxyId = proxyId;
} 

/** Returns the proxy's identifier */
char* CnxConnectReply::getProxyId() {
  return proxyId;
} 

// ====================
// Streamable interface
// ====================

void CnxConnectReply::writeTo(OutputStream *os) throw (IOException) {
  AbstractReply::writeTo(os);
  if (os->writeInt(cnxKey) == -1)  throw IOException();
  if (os->writeString(proxyId) == -1)  throw IOException();
}

void CnxConnectReply::readFrom(InputStream *is) throw (IOException) {
  AbstractReply::readFrom(is);
  if (is->readInt(&cnxKey) == -1)  throw IOException();
  if (is->readString(&proxyId) == -1)  throw IOException();
}

// ######################################################################
// CnxStartRequest Class
// ######################################################################

CnxStartRequest::CnxStartRequest() : AbstractRequest((char*) NULL) {
  classid = CNX_START_REQUEST;
}

CnxStartRequest::~CnxStartRequest() {
  if(DEBUG)
    printf("~CnxStartRequest()\n");
}

// ######################################################################
// CnxStopRequest Class
// ######################################################################

CnxStopRequest::CnxStopRequest() : AbstractRequest((char*) NULL) {
  classid = CNX_STOP_REQUEST;
}

CnxStopRequest::~CnxStopRequest() {
  if(DEBUG)
    printf("~CnxStopRequest()\n");
}

// ######################################################################
// CnxCloseRequest Class
// ######################################################################

CnxCloseRequest::CnxCloseRequest() : AbstractRequest((char*) NULL) {
  classid = CNX_CLOSE_REQUEST;
}

CnxCloseRequest::~CnxCloseRequest() {
  if(DEBUG)
    printf("~CnxCloseRequest()\n");
}

// ######################################################################
// CnxCloseReply Class
// ######################################################################

CnxCloseReply::CnxCloseReply() : AbstractReply() {
  classid = CNX_CLOSE_REPLY;
}

CnxCloseReply::~CnxCloseReply() {
  if(DEBUG)
    printf("~CnxCloseReply()\n");
}

// ######################################################################
// ProducerMessages Class
// ######################################################################

/**
 * Constructs a <code>ProducerMessages</code> instance.
 */
ProducerMessages::ProducerMessages() : AbstractRequest((char*) NULL) {
  classid = PRODUCER_MESSAGES;
  messages = (Vector<Message>*) NULL;
  asyncSend = FALSE;
}

/**
 * Constructs a <code>ProducerMessages</code> instance.
 *
 * @param dest  Name of the destination the messages are sent to.
 */
ProducerMessages::ProducerMessages(char* dest) : AbstractRequest(dest) {
  classid = PRODUCER_MESSAGES;
  messages = (Vector<Message>*) NULL;
  asyncSend = FALSE;
}

/**
 * Constructs a <code>ProducerMessages</code> instance carrying a single
 * message.
 *
 * @param dest  Name of the destination the messages are sent to.
 * @param msg  Message to carry.
 */
ProducerMessages::ProducerMessages(char* dest, Message* msg) : AbstractRequest(dest) {
  classid = PRODUCER_MESSAGES;
  messages = new Vector<Message>();
  messages->addElement(msg);
  asyncSend = FALSE;
}

ProducerMessages::~ProducerMessages() {
  if(DEBUG)
    printf("~ProducerMessages(): messages = Ox%x\n", messages);
  if (messages != (Vector<Message>*) NULL) {
    delete messages;
    messages = (Vector<Message>*) NULL;
  }
}

/** Returns the produced messages. */
Vector<Message>* ProducerMessages::getMessages() {
  if (messages == (Vector<Message>*) NULL) messages = new Vector<Message>();
  return messages;
}

/** Adds a message to deliver. */
void ProducerMessages::addMessage(Message* msg) {
  if (messages == (Vector<Message>*) NULL) messages = new Vector<Message>();
  messages->addElement(msg);
}

/** Adds messages to deliver. */
void ProducerMessages::addMessages(Vector<Message>* msgs) {
  if (messages == (Vector<Message>*) NULL) messages = new Vector<Message>();
  for (int i=0; i<msgs->size(); i++)
    messages->addElement(msgs->elementAt(i));
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
void ProducerMessages::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  Message::writeVectorTo(messages, os);
  if (os->writeBoolean(asyncSend) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ProducerMessages::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  messages = Message::readVectorFrom(is);
  if (is->readBoolean(&asyncSend) == -1) throw IOException();
}

// ######################################################################
// ConsumerReceiveRequest Class
// ######################################################################

/**
 * Constructs a <code>ConsumerReceiveRequest</code> instance.
 */
ConsumerReceiveRequest::ConsumerReceiveRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_RECEIVE_REQUEST;
  selector = (char*) NULL;
  timeToLive = 0;
  queueMode = FALSE;
  receiveAck = TRUE;
}

ConsumerReceiveRequest::~ConsumerReceiveRequest() {
  if(DEBUG)
    printf("~ConsumerReceiveRequest(): selector = 0x%x\n", selector);
  if (selector != (char*) NULL) {
    delete[] selector;
    selector = (char*) NULL;
  }
}

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
ConsumerReceiveRequest::ConsumerReceiveRequest(char* targetName, char* selector, long long timeToLive, boolean queueMode) : AbstractRequest(targetName) {
  classid = CONSUMER_RECEIVE_REQUEST;
  this->selector = selector;
  this->timeToLive = timeToLive;
  this->queueMode = queueMode;
  this->receiveAck = FALSE;
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
void ConsumerReceiveRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  if (os->writeString(selector) == -1)  throw IOException();
  if (os->writeLong(timeToLive) == -1)  throw IOException();
  if (os->writeBoolean(queueMode) == -1) throw IOException();
  if (os->writeBoolean(receiveAck) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ConsumerReceiveRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  if (is->readString(&selector) == -1)  throw IOException();
  if (is->readLong(&timeToLive) == -1)  throw IOException();
  if (is->readBoolean(&queueMode) == -1) throw IOException();
  if (is->readBoolean(&receiveAck) == -1) throw IOException();
}

// ######################################################################
// ConsumerMessage Class
// ######################################################################

/**
 * Constructs an empty <code>ConsumerMessages</code> instance.
 */
ConsumerMessages::ConsumerMessages() : AbstractReply() {
  classid = CONSUMER_MESSAGES;
  messages = (Vector<Message>*) NULL;
  comingFrom = (char*) NULL;
  queueMode = FALSE;
}

/**
 * Constructs a <code>ConsumerMessages</code> instance.
 *
 * @param correlationId  Reply identifier.
 * @param message  Message to wrap.
 * @param comingFrom  Name of the queue or the subscription the message
 *          come from.
 * @param queueMode  <code>true</code> if the message come from a queue.
 */
ConsumerMessages::ConsumerMessages(int correlationId,
                 Message* msg,
                 char* comingFrom,
                 boolean queueMode) : AbstractReply(correlationId) {
  classid = CONSUMER_MESSAGES;
  messages = new Vector<Message>();
  messages->addElement(msg);
  this->comingFrom = comingFrom;
  this->queueMode = queueMode;
}

/**
 * Constructs a <code>ConsumerMessages</code> instance.
 *
 * @param correlationId  Reply identifier.
 * @param messages  Messages to wrap.
 * @param comingFrom  Name of the queue or the subscription the messages
 *          comes from.
 * @param queueMode  <code>true</code> if the messages come from a queue.
 */
ConsumerMessages::ConsumerMessages(int correlationId,
                   Vector<Message>* messages,
                   char* comingFrom,
                   boolean queueMode) : AbstractReply(correlationId) {
  classid = CONSUMER_MESSAGES;
  this->messages = messages;
  this->comingFrom = comingFrom;
  this->queueMode = queueMode;
}

/**
 * Constructs an empty <code>ConsumerMessages</code> instance.
 *
 * @param correlationId  Reply identifier.
 * @param comingFrom  Name of the queue or the subscription the reply
 *          comes from.
 * @param queueMode  <code>true</code> if it replies to a queue consumer.
 */
ConsumerMessages::ConsumerMessages(int correlationId,
                 char* comingFrom,
                 boolean queueMode) : AbstractReply(correlationId) {
  classid = CONSUMER_MESSAGES;
   messages = (Vector<Message>*) NULL;
  this->comingFrom = comingFrom;
  this->queueMode = queueMode;
}

ConsumerMessages::~ConsumerMessages() {
  if(DEBUG)
    printf("~ConsumerMessages(): messages = 0x%x, comingFrom = 0x%x\n", messages, comingFrom);
  if (messages != (Vector<Message>*) NULL) {
    delete messages;
    messages = (Vector<Message>*) NULL;
  }
  if (comingFrom != (char*) NULL) {
    delete[] comingFrom;
    comingFrom = (char*) NULL;
  }
}

/** Returns the messages to deliver. */
Vector<Message>* ConsumerMessages::getMessages() {
  return messages;
}

void ConsumerMessages::addMessage(Message* msg) {
  if (messages == (Vector<Message>*) NULL)
    messages = new Vector<Message>();
  messages->addElement(msg);
}

int ConsumerMessages::getMessageCount() {
  return messages->size();
}

/**
 * Returns the name of the queue or the subscription the messages come
 * from.
 */
char* ConsumerMessages::comesFrom() {
  return comingFrom;
}

void ConsumerMessages::setComesFrom(char* comingFrom) {
  this->comingFrom = comingFrom;
}


/** Returns <code>true</code> if the messages come from a queue. */
boolean ConsumerMessages::getQueueMode() {
  return queueMode;
}

void ConsumerMessages::setQueueMode(boolean queueMode) {
  this->queueMode = queueMode;
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
void ConsumerMessages::writeTo(OutputStream* os) throw(IOException) {
  AbstractReply::writeTo(os);
  Message::writeVectorTo(messages, os);
  if (os->writeString(comingFrom) == -1)  throw IOException();
  if (os->writeBoolean(queueMode) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ConsumerMessages::readFrom(InputStream* is) throw(IOException) {
  AbstractReply::readFrom(is);
  messages = Message::readVectorFrom(is);
  if (is->readString(&comingFrom) == -1)  throw IOException();
  if (is->readBoolean(&queueMode) == -1) throw IOException();
}

// ######################################################################
// ConsumerSubRequest Class
// ######################################################################

/**
 * Constructs a <code>ConsumerSubRequest</code>.
 */
ConsumerSubRequest::ConsumerSubRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_SUB_REQUEST;
}

/**
 * Constructs a <code>ConsumerSubRequest</code>.
 *
 * @param topic  The topic identifier the client wishes to subscribe to.
 * @param subName  The subscription's name.
 * @param selector  The selector for filtering messages, if any.
 * @param noLocal  <code>true</code> for not consuming the local messages.
 * @param durable  <code>true</code> for a durable subscription.
 */
ConsumerSubRequest::ConsumerSubRequest(char* topic, char* subName,
                                       char* selector,
                                       boolean noLocal,
                                       boolean durable) : AbstractRequest(topic) {
  classid = CONSUMER_SUB_REQUEST;
  this->subName = subName;
  this->selector = selector;
  this->noLocal = noLocal;
  this->durable = durable;
  this->asyncSub = false;                                 
}
                                       
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
ConsumerSubRequest::ConsumerSubRequest(char* topic, char* subName,
                                       char* selector,
                                       boolean noLocal,
                                       boolean durable,
                                       boolean asyncSub) : AbstractRequest(topic) {
  classid = CONSUMER_SUB_REQUEST;
  this->subName = subName;
  this->selector = selector;
  this->noLocal = noLocal;
  this->durable = durable;
  this->asyncSub = asyncSub;
}

ConsumerSubRequest::~ConsumerSubRequest() {
  if(DEBUG)
    printf("~ConsumerSubRequest(): selector = 0x%x, subName = 0x%x\n", selector, subName);
  if (subName != (char*) NULL) {
    delete[] subName;
    subName = (char*) NULL;
  }
  if (selector != (char*) NULL) {
    delete[] selector;
    selector = (char*) NULL;
  }
  if (clientID != (char*) NULL) {
    delete[] clientID;
    clientID = (char*) NULL;
  }
}

/** Sets the subscription name. */
void ConsumerSubRequest::setSubName(char* subName) {
  this->subName = subName;
}

/** Returns the name of the subscription. */
char* ConsumerSubRequest::getSubName() {
  return subName;
}

/** Sets the selector. */
void ConsumerSubRequest::setSelector(char* selector) {
  this->selector = selector;
}

/** Returns the selector for filtering the messages. */
char* ConsumerSubRequest::getSelector() {
  return selector;
}

/** Sets the noLocal attribute. */
void ConsumerSubRequest::setNoLocal(boolean noLocal) {
  this->noLocal = noLocal;
}

/** Returns <code>true</code> for not consuming the local messages. */
boolean ConsumerSubRequest::getNoLocal() {
  return noLocal;
}

/** Sets the durable attribute. */
void ConsumerSubRequest::setDurable(boolean durable) {
  this->durable = durable;
}

/** Returns <code>true</code> for a durable subscription. */
boolean ConsumerSubRequest::getDurable() {
  return durable;
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
void ConsumerSubRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  if (os->writeString(subName) == -1)  throw IOException();
  if (os->writeString(selector) == -1)  throw IOException();
  if (os->writeBoolean(noLocal) == -1)  throw IOException();
  if (os->writeBoolean(durable) == -1)  throw IOException();
  if (os->writeBoolean(asyncSub) == -1)  throw IOException();
  if (os->writeString(clientID) == -1)  throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ConsumerSubRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  if (is->readString(&subName) == -1)  throw IOException();
  if (is->readString(&selector) == -1)  throw IOException();
  if (is->readBoolean(&noLocal) == -1)  throw IOException();
  if (is->readBoolean(&durable) == -1)  throw IOException();
  if (is->readBoolean(&asyncSub) == -1)  throw IOException();
  if (is->readString(&clientID) == -1)  throw IOException();
}

// ######################################################################
// ConsumerUnsubRequest Class
// ######################################################################

/**
 * Constructs a <code>ConsumerUnsubRequest</code>.
 */
ConsumerUnsubRequest::ConsumerUnsubRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_UNSUB_REQUEST;
}

/**
 * Constructs a <code>ConsumerUnsubRequest</code>.
 *
 * @param subName  The name of the subscription to delete.
 */
ConsumerUnsubRequest::ConsumerUnsubRequest(char* subName) : AbstractRequest(subName) {
  classid = CONSUMER_UNSUB_REQUEST;
}

ConsumerUnsubRequest::~ConsumerUnsubRequest() {
  if(DEBUG)
    printf("~ConsumerUnsubRequest()\n");
}

// ######################################################################
// MomExceptionReply Class
// ######################################################################

/**
 * Public no-arg constructor. 
 */
MomExceptionReply::MomExceptionReply() : AbstractReply() {
  classid = MOM_EXCEPTION_REPLY;
}

MomExceptionReply::~MomExceptionReply() {
  if(DEBUG)
    printf("~MomExceptionReply(): message = 0x%x\n", message);
  if (message != (char*) NULL) {
    delete[] message;
    message = (char*) NULL;
  }
}

/** Returns thewrapped exception type. */
int MomExceptionReply::getType() {
  return type;
}

/** Returns the wrapped exception message. */
char* MomExceptionReply::getMessage() {
  return message;
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
void MomExceptionReply::writeTo(OutputStream *os) throw (IOException) {
  AbstractReply::writeTo(os);
  if (os->writeInt(type) == -1) throw IOException();
  if (os->writeString(message) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void MomExceptionReply::readFrom(InputStream *is) throw (IOException) {
  AbstractReply::readFrom(is);
  if (is->readInt(&type) == -1)  throw IOException();
  if (is->readString(&message) == -1)  throw IOException();
}

// ######################################################################
// ConsumerAckRequest Class
// ######################################################################

/**
 * Constructs a <code>SessAckRequest</code> instance.
 */
ConsumerAckRequest::ConsumerAckRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_ACK_REQUEST;
}

ConsumerAckRequest::~ConsumerAckRequest() {
  if(DEBUG)
    printf("~ConsumerAckRequest(): ids = 0x%x\n", ids);
  if (ids != (Vector<char>*) NULL) {
    delete ids;
    ids = (Vector<char>*) NULL;
  }
}

/**
 * Constructs a <code>SessAckRequest</code> instance.
 *
 * @param targetName  Name of the target queue or subscription.
 * @param ids  Vector of acknowledged message identifiers.
 * @param queueMode  <code>true</code> if this request is destinated to a
 *          queue.
 */
ConsumerAckRequest::ConsumerAckRequest(char* targetName,
                                       boolean queueMode) : AbstractRequest(targetName) {
  classid = CONSUMER_ACK_REQUEST;
  this->ids = (Vector<char>*) NULL;
  this->queueMode = queueMode;
}

/** Sets the vector of identifiers. */
void ConsumerAckRequest::setIds(Vector<char>* ids) {
  this->ids = ids;
}

void ConsumerAckRequest::addId(char* id) {
  if (ids == (Vector<char>*) NULL) ids = new Vector<char>();
  ids->addElement(id);
}

/** Returns the vector of acknowledged messages identifiers. */
Vector<char>* ConsumerAckRequest::getIds() {
  return ids;
}

/** Sets the target destination type. */
void ConsumerAckRequest::setQueueMode(boolean queueMode) {
  this->queueMode = queueMode;
}

/** Returns <code>true</code> if the request is destinated to a queue. */
boolean ConsumerAckRequest::getQueueMode() {
  return queueMode;
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
void ConsumerAckRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  os->writeVectorOfString(ids);
  if (os->writeBoolean(queueMode) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ConsumerAckRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  ids = is->readVectorOfString();
  if (is->readBoolean(&queueMode) == -1) throw IOException();
}

// ######################################################################
// ConsumerDenyRequest Class
// ######################################################################

/**
 * Public no-arg constructor needed by Externalizable.
 */
ConsumerDenyRequest::ConsumerDenyRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_DENY_REQUEST;
}

/**
 * Constructs a <code>SessDenyRequest</code> instance.
 *
 * @param targetName  Name of the target queue or subscription.
 * @param ids  Vector of denied message identifiers.
 * @param queueMode  <code>true</code> if this request is destinated to a
 *          queue.
 */
ConsumerDenyRequest::ConsumerDenyRequest(char* targetName,
                                         char* ids,
                                         boolean queueMode) : AbstractRequest(targetName) {
  classid = CONSUMER_DENY_REQUEST;
  this->id = id;
  this->queueMode = queueMode;
  doNotAck = FALSE;
}

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
ConsumerDenyRequest::ConsumerDenyRequest(char* targetName,
                                         char* ids,
                                         boolean queueMode,
                                         boolean doNotAck) : AbstractRequest(targetName) {
  classid = CONSUMER_DENY_REQUEST;
  this->id = id;
  this->queueMode = queueMode;
  this->doNotAck = doNotAck;
}

ConsumerDenyRequest::~ConsumerDenyRequest() {
  if(DEBUG)
    printf("~ConsumerDenyRequest(): id = 0x%x\n", id);
  if (id != (char*) NULL) {
    delete[] id;
    id = (char*) NULL;
  }
}

/** Sets the identifier. */
void ConsumerDenyRequest::setId(char* id) {
  this->id = id;
}

/** Returns the vector of denyed messages identifiers. */
char* ConsumerDenyRequest::getId() {
  return id;
}

/** Sets the target destination type. */
void ConsumerDenyRequest::setQueueMode(boolean queueMode) {
  this->queueMode = queueMode;
}

/** Returns <code>true</code> if the request is destinated to a queue. */
boolean ConsumerDenyRequest::getQueueMode() {
  return queueMode;
}

/** Sets the server ack policy. */
void ConsumerDenyRequest::setDoNotAck(boolean doNotAck) {
  this->doNotAck = doNotAck;
}

/**
 * Returns <code>true</code> if the request must not be acked by the 
 * server.
 */
boolean ConsumerDenyRequest::getDoNotAck() {
  return doNotAck;
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
void ConsumerDenyRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  if (os->writeString(id) == -1) throw IOException();
  if (os->writeBoolean(queueMode) == -1)  throw IOException();
  if (os->writeBoolean(doNotAck) == -1)  throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ConsumerDenyRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  if (is->readString(&id) == -1) throw IOException();
  if (is->readBoolean(&queueMode) == -1)  throw IOException();
  if (is->readBoolean(&doNotAck) == -1)  throw IOException();
}

// ######################################################################
// SessAckRequest Class
// ######################################################################

/**
 * Constructs a <code>SessAckRequest</code> instance.
 */
SessAckRequest::SessAckRequest() : AbstractRequest((char*) NULL) {
  classid = SESS_ACK_REQUEST;
}

/**
 * Constructs a <code>SessAckRequest</code> instance.
 *
 * @param targetName  Name of the target queue or subscription.
 * @param ids  Vector of acknowledged message identifiers.
 * @param queueMode  <code>true</code> if this request is destinated to a
 *          queue.
 */
SessAckRequest::SessAckRequest(char* targetName,
                               Vector<char>* ids,
                               boolean queueMode) : AbstractRequest(targetName) {
  classid = SESS_ACK_REQUEST;
  this->ids = ids;
  this->queueMode = queueMode;
}

SessAckRequest::~SessAckRequest() {
  if(DEBUG)
    printf("~SessAckRequest(): ids = 0x%x\n", ids);
  if (ids != (Vector<char>*) NULL) {
    delete ids;
    ids = (Vector<char>*) NULL;
  }
}

/** Sets the vector of identifiers. */
void SessAckRequest::setIds(Vector<char>* ids) {
  this->ids = ids;
}

void SessAckRequest::addId(char* id) {
  if (ids == (Vector<char>*) NULL) ids = new Vector<char>();
  ids->addElement(id);
}

/** Returns the vector of acknowledged messages identifiers. */
Vector<char>* SessAckRequest::getIds() {
  return ids;
}

/** Sets the target destination type. */
void SessAckRequest::setQueueMode(boolean queueMode) {
  this->queueMode = queueMode;
}

/** Returns <code>true</code> if the request is destinated to a queue. */
boolean SessAckRequest::getQueueMode() {
  return queueMode;
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
void SessAckRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  os->writeVectorOfString(ids);
  if (os->writeBoolean(queueMode) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void SessAckRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  ids = is->readVectorOfString();
  if (is->readBoolean(&queueMode) == -1) throw IOException();
}

// ######################################################################
// SessDenyRequest Class
// ######################################################################

/**
 * Public no-arg constructor needed by Externalizable.
 */
SessDenyRequest::SessDenyRequest() : AbstractRequest((char*) NULL) {
  classid = SESS_DENY_REQUEST;
}

/**
 * Constructs a <code>SessDenyRequest</code> instance.
 *
 * @param targetName  Name of the target queue or subscription.
 * @param ids  Vector of denied message identifiers.
 * @param queueMode  <code>true</code> if this request is destinated to a
 *          queue.
 */
SessDenyRequest::SessDenyRequest(char* targetName,
                                 Vector<char>* ids,
                                 boolean queueMode) : AbstractRequest(targetName) {
  classid = SESS_DENY_REQUEST;
  this->ids = ids;
  this->queueMode = queueMode;
  doNotAck = false;
}

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
SessDenyRequest::SessDenyRequest(char* targetName,
                                 Vector<char>* ids,
                                 boolean queueMode,
                                 boolean doNotAck) : AbstractRequest(targetName) {
  classid = SESS_DENY_REQUEST;
  this->ids = ids;
  this->queueMode = queueMode;
  this->doNotAck = doNotAck;
}

SessDenyRequest::~SessDenyRequest() {
  if(DEBUG)
    printf("~SessDenyRequest(): ids = 0x%x\n", ids);
  if (ids != (Vector<char>*) NULL) {
    delete ids;
    ids = (Vector<char>*) NULL;
  }
}

/** Sets the vector of identifiers. */
void SessDenyRequest::setIds(Vector<char>* ids) {
  this->ids = ids;
}

void SessDenyRequest::addId(char* id) {
  if (ids == (Vector<char>*) NULL) ids = new Vector<char>();
  ids->addElement(id);
}

/** Returns the vector of denyed messages identifiers. */
Vector<char>* SessDenyRequest::getIds() {
  return ids;
}

/** Sets the target destination type. */
void SessDenyRequest::setQueueMode(boolean queueMode) {
  this->queueMode = queueMode;
}

/** Returns <code>true</code> if the request is destinated to a queue. */
boolean SessDenyRequest::getQueueMode() {
  return queueMode;
}

/** Sets the server ack policy. */
void SessDenyRequest::setDoNotAck(boolean doNotAck) {
  this->doNotAck = doNotAck;
}

/**
 * Returns <code>true</code> if the request must not be acked by the 
 * server.
 */
boolean SessDenyRequest::getDoNotAck() {
  return doNotAck;
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
void SessDenyRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  os->writeVectorOfString(ids);
  if (os->writeBoolean(queueMode) == -1)  throw IOException();
  if (os->writeBoolean(doNotAck) == -1)  throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void SessDenyRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  ids = is->readVectorOfString();
  if (is->readBoolean(&queueMode) == -1)  throw IOException();
  if (is->readBoolean(&doNotAck) == -1)  throw IOException();
}

// ######################################################################
// ServerReply Class
// ######################################################################

ServerReply::ServerReply() : AbstractReply() {
  classid = SERVER_REPLY;
}

ServerReply::~ServerReply() {
  if(DEBUG)
    printf("~ServerReply()\n");
}

// ######################################################################
// ActivateConsumerRequest Class
// ######################################################################

/**
 * Public no-arg constructor needed by Externalizable.
 */
ActivateConsumerRequest::ActivateConsumerRequest() : AbstractRequest((char*) NULL) {
  classid = ACTIVATE_CONSUMER_REQUEST;
}

/**
 * Constructs a <code>ActivateConsumerRequest</code> instance.
 *
 * @param targetName  Name of the target queue or subscription.
 * @param activate  
 */
ActivateConsumerRequest::ActivateConsumerRequest(char* targetName,
                                                 boolean activate) : AbstractRequest(targetName) {
  classid = ACTIVATE_CONSUMER_REQUEST;
  this->activate = activate;
}

ActivateConsumerRequest::~ActivateConsumerRequest() {
  if(DEBUG)
    printf("~ActivateConsumerRequest()\n");
}

boolean ActivateConsumerRequest::getActivate() {
  return activate;
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
void ActivateConsumerRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  if (os->writeBoolean(activate) == -1)  throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void ActivateConsumerRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  if (is->readBoolean(&activate) == -1)  throw IOException();
}

// ######################################################################
// ConsumerCloseSubRequest Class
// ######################################################################

/**
 * Constructs a <code>ConsumerCloseSubRequest</code>.
 */
ConsumerCloseSubRequest::ConsumerCloseSubRequest() : AbstractRequest((char*) NULL) {
  classid = CONSUMER_CLOSE_SUB_REQUEST;
}

/**
 * Constructs a <code>ConsumerCloseSubRequest</code>.
 *
 * @param subName  The name of the closing durable subscription.
 */
ConsumerCloseSubRequest::ConsumerCloseSubRequest(char* subName) : AbstractRequest(subName) {
  classid = CONSUMER_CLOSE_SUB_REQUEST;
}

ConsumerCloseSubRequest::~ConsumerCloseSubRequest() {
  if(DEBUG)
    printf("~ConsumerCloseSubRequest()\n");
}

GetAdminTopicRequest::GetAdminTopicRequest() : AbstractRequest((char*) NULL) {
  classid = GET_ADMIN_TOPIC_REQUEST;
}

GetAdminTopicRequest::~GetAdminTopicRequest() {
  if(DEBUG)
    printf("~GetAdminTopicRequest()\n");
}

GetAdminTopicReply::GetAdminTopicReply() : AbstractReply() {
  classid = GET_ADMIN_TOPIC_REPLY;
}

GetAdminTopicReply::~GetAdminTopicReply() {
  if(DEBUG)
    printf("~GetAdminTopicReply()\n");
}

GetAdminTopicReply::GetAdminTopicReply(GetAdminTopicRequest* req, char* id) : AbstractReply(req->getRequestId()) {
  classid = GET_ADMIN_TOPIC_REPLY;
  this->id = id;
}

/** Sets the identifier. */
void GetAdminTopicReply::setId(char* id) {
  this->id = id;
}

/** Returns the vector of denyed messages identifiers. */
char* GetAdminTopicReply::getId() {
  return id;
}

/* ***** ***** ***** ***** *****
 * Streamable interface
 * ***** ***** ***** ***** ***** */

/**
 *  The object implements the writeTo method to write its contents to
 *  the output stream.
 *
 * @param os the stream to write the object to
 */
void GetAdminTopicReply::writeTo(OutputStream* os) throw(IOException) {
  AbstractReply::writeTo(os);
  if (os->writeString(id) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void GetAdminTopicReply::readFrom(InputStream* is) throw(IOException) {
  AbstractReply::readFrom(is);
  if (is->readString(&id) == -1) throw IOException();
}


TempDestDeleteRequest::TempDestDeleteRequest() : AbstractRequest((char*) NULL) {
  classid = TEMP_DEST_DELETE_REQUEST;
}

TempDestDeleteRequest::TempDestDeleteRequest(char* uid) : AbstractRequest(uid) {
  classid = TEMP_DEST_DELETE_REQUEST;
}

TempDestDeleteRequest::~TempDestDeleteRequest() {
}


/**
 * Constructs a <code>SessCreateDestReply</code> instance.
 */
SessCreateDestReply::SessCreateDestReply() : AbstractReply() {
  classid = SESS_CREATE_DEST_REPLY;
}

/**
 * Constructs a <code>SessCreateDestReply</code> instance.
 *
 * @param request  The replied request.
 * @param agentId  String identifier of the destination agent.
 */
SessCreateDestReply::SessCreateDestReply(AbstractRequest* req, char* agentId)  : AbstractReply(req->getRequestId()) {
  classid = SESS_CREATE_DEST_REPLY;
  this->agentId = agentId;
}

SessCreateDestReply::~SessCreateDestReply() {
}

/** Sets the destination identifier. */
void SessCreateDestReply::setAgentId(char* agentId) {
  this->agentId = agentId;
}

/** Returns the temporary destination's agent identifier. */
char* SessCreateDestReply::getAgentId() {
  return agentId;
}

/* ***** ***** ***** ***** *****
 * Streamable interface
 * ***** ***** ***** ***** ***** */

/**
 *  The object implements the writeTo method to write its contents to
 *  the output stream.
 *
 * @param os the stream to write the object to
 */
void SessCreateDestReply::writeTo(OutputStream* os) throw(IOException) {
  AbstractReply::writeTo(os);
  if (os->writeString(agentId) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void SessCreateDestReply::readFrom(InputStream* is) throw(IOException) {
  AbstractReply::readFrom(is);
  if (is->readString(&agentId) == -1) throw IOException();
}

SessCreateDestRequest::SessCreateDestRequest() : AbstractRequest((char*) NULL) {
  classid = SESS_CREATE_DEST_REQUEST;
}

SessCreateDestRequest::SessCreateDestRequest(byte type) : AbstractRequest((char*) NULL) {
	classid = SESS_CREATE_DEST_REQUEST;
	this->type = type;
	this->name = (char*) NULL;
}

SessCreateDestRequest::SessCreateDestRequest(byte type, char* name) : AbstractRequest((char*) NULL) {
	classid = SESS_CREATE_DEST_REQUEST;
	this->type = type;
	this->name = name;
}

SessCreateDestRequest::~SessCreateDestRequest() {

}

/* ***** ***** ***** ***** *****
 * Streamable interface
 * ***** ***** ***** ***** ***** */

/**
 *  The object implements the writeTo method to write its contents to
 *  the output stream.
 *
 * @param os the stream to write the object to
 */
void SessCreateDestRequest::writeTo(OutputStream* os) throw(IOException) {
  AbstractRequest::writeTo(os);
  if (os->writeByte(type) == -1) throw IOException();
  if (os->writeString(name) == -1) throw IOException();
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void SessCreateDestRequest::readFrom(InputStream* is) throw(IOException) {
  AbstractRequest::readFrom(is);
  if (is->readByte(&type) == -1) throw IOException();
  if (is->readString(&name) == -1) throw IOException();
}

/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2006 CNES
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
#include <stdio.h>
#include <unistd.h>

#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h>

#include "Synchronized.H"
#include "Daemon.H"
#include "XQueue.H"

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

// ######################################################################
// Channel Class
// ######################################################################

class Channel {
 protected:
  enum {INIT, CONNECT, CONNECTING, CLOSE};

  int status;
  int key;

 public:
  virtual void connect() = 0;

  virtual void send(AbstractRequest* obj) throw (IOException) = 0;

  virtual AbstractReply* receive() throw (IOException) = 0;

  virtual void close() = 0;
};

// ######################################################################
// TcpChannel Class
// ######################################################################

class TcpMessage {
 public:
  long long id;
  AbstractRequest* msg;

  TcpMessage(long long id, AbstractRequest* msg) {
    this->id = id;
    this->msg = msg;
  }

  ~TcpMessage() {
  }
};

class TcpChannel : public Channel {
 private:
  char* user;
  char* pass;
  char* host;
  int port;

  int sock;
  OutputStream* out;
  InputStream* in;

  // definition from ReliableTcpConnection
  int windowSize;
  long long inputCounter;
  long long outputCounter;
  int unackCounter;
  Vector<TcpMessage>* pendingMessages;

/*   Object inputLock; */
/*   Object outputLock; */

/*   Timer timer; */

 public:
  TcpChannel(char *user, char *pass, char *host, int port) {
    if(DEBUG)
      printf("=> TcpChannel():\n");
    this->user = user;
    this->pass = pass;
    this->host = host;
    this->port = port;

    windowSize = 10;
    inputCounter = -1;
    outputCounter = 0;
    unackCounter = 0;
    pendingMessages = new Vector<TcpMessage>();

/*     inputLock = new Object(); */
/*     outputLock = new Object(); */

/*     timer = timer2; */

    key = -1;
    status = INIT;

    sock = -1;
    out = NULL;
    in = NULL;
    if(DEBUG)
      printf("<= TcpChannel(): pendingMessages = 0x%x\n", pendingMessages);
  }

  ~TcpChannel() {
    if(DEBUG)
      printf("~TcpChannel(): pendingMessages = 0x%x, out = 0x%x, in = 0x%x\n", pendingMessages, out, in);
    if (pendingMessages != (Vector<TcpMessage>*) NULL) {
      delete pendingMessages;
      pendingMessages = (Vector<TcpMessage>*) NULL;
    }
    if (out != (OutputStream*) NULL) {
      delete out;
      out = (OutputStream*) NULL;
    }
    if (in != (InputStream*) NULL) {
      delete in;
      in = (InputStream*) NULL;
    }
  }

  long long currentTimeMillis() {
    long long time;
    struct timespec ts;

    clock_gettime(CLOCK_REALTIME, &ts);
    time = ((long long)ts.tv_sec) *1000;
    time += ts.tv_nsec /1000000;

    return time;
  }

  virtual void connect()  {
    if (status != INIT) throw IllegalStateException();
    status = CONNECTING;

    struct sockaddr_in addr;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == -1) throw ConnectException("Cannot create socket");

    addr.sin_family = AF_INET;
    addr.sin_port   = htons(port);

    int opt = 1;
    setsockopt(sock,SOL_TCP , TCP_NODELAY, &opt, sizeof(opt));

    struct hostent *server = gethostbyname(host);
    if (server == NULL) throw ConnectException("Host unknown");
    bcopy((char *)server->h_addr,
          (char *)&addr.sin_addr.s_addr,
          server->h_length);
    if (::connect(sock, (sockaddr*) &addr, sizeof(addr)) == -1)
      throw ConnectException("Cannot connect");

    out = new OutputStream();
    in = new InputStream();

    // Joram magic number: should be defined in another way..
    byte magic[] = {'J', 'O', 'R', 'A', 'M', 5, 8, 58};

    // Writes the Joram magic number
    if (out->writeBuffer(magic, 8) ==-1) throw IOException();
    // Writes the current date
    long long date = currentTimeMillis();
    if (out->writeLong(date) == -1) throw IOException();
    if(DEBUG)
      printf("write date OK = %lld\n", date);

    // Writes the identity using SimpleIdentity convention
    if (out->writeString("") ==-1) throw IOException();
    if (out->writeString(user) == -1) throw IOException();
    if (out->writeString(pass) == -1) throw IOException();

    if (out->writeInt(key) == -1) throw IOException();

    if (key == -1) {
      // Open new connection
      if (out->writeInt(/* reconnectTimeout */ 0) == -1) throw IOException();

      if (out->writeDataTo(sock) == -1) throw IOException();
      if (in->readFrom(sock) == -1) throw IOException();

    // read dt
    long long dt;
    if (in->readLong(&dt) == -1) throw IOException();
    if(DEBUG)
      printf("read dt = %lld\n", dt);

      int res;
      if (in->readInt(&res) == -1) throw IOException();
      if (res > 0) {
        char* info;
        if (in->readString(&info) == -1) throw ConnectException();
        throw ConnectException(info);
      }
      if (in->readInt(&key) == -1) {
        throw IOException();
      }
    } else {
      // Reopen the connection
      if (out->writeDataTo(sock) == -1) throw IOException();
      if (in->readFrom(sock) == -1) throw IOException();

      int res;
      if (in->readInt(&res) == -1) throw IOException();
      if (res > 0) {
        char* info;
        if (in->readString(&info) == -1) throw ConnectException();
        throw ConnectException(info);
      }
    }

    if(DEBUG)
      printf("connected -> %d\n", key);

    TcpMessage* pendingMsg = (TcpMessage*) NULL;
    for (int i = 0; i < pendingMessages->size(); i++) {
      pendingMsg = pendingMessages->elementAt(i);
      doSend(pendingMsg->id, inputCounter, pendingMsg->msg);
    }

    status = CONNECT;
  }

  virtual void send(AbstractRequest* request) throw (IOException) {
    if (status != CONNECT) throw IOException("Connection closed");

    try {
      doSend(outputCounter, inputCounter, request);
      pendingMessages->addElement(new TcpMessage(outputCounter, request));
      outputCounter++;
    } catch (IOException exc) {
      close();
      throw exc;
    }
  }

  void doSend(long long id, long long counter,
              AbstractRequest* request) throw (IOException) {
    if (status != CONNECT) throw ConnectException();

    if (out->writeLong(id) == -1) throw IOException();
    if (out->writeLong(counter) == -1) throw IOException();
    AbstractMessage::write(request, out);
    if (out->writeTo(sock) == -1) throw IOException();
  }

  virtual AbstractReply* receive() throw (IOException) {
    if (status != CONNECT) throw ConnectException();

    while (true) {
      if (in->readFrom(sock) == -1) {
        if (status != CLOSE) {
          printf("receive in->readFrom(sock) ERROR=%s\n", strerror(errno));
          throw IOException();
        }
      }

      long long msgid, ackid;

      if (in->readLong(&msgid) == -1) throw IOException();
      if (in->readLong(&ackid) == -1) throw IOException();
      AbstractReply* reply = (AbstractReply*) AbstractMessage::read(in);

      while (pendingMessages->size() > 0) {
        TcpMessage* pendingMsg = pendingMessages->elementAt(0);
        if (ackid < pendingMsg->id) {
          // It's an old acknowledge
          break;
        } else {
          pendingMessages->removeElementAt(0);
          delete pendingMsg;
        }
      }

      if (reply != (AbstractReply*) NULL) {
        if (unackCounter < windowSize) {
          unackCounter++;
        } else {
          // TODO
/*           AckTimerTask ackTimertask = new AckTimerTask(); */
/*           timer.schedule(ackTimertask, 0); */
        }
        if (msgid > inputCounter) {
          inputCounter = msgid;
          return reply;
        }
      }
    }
  }

  virtual void close()  {
    ::close(sock);
    status = CLOSE;
  }
};

// ######################################################################
// RequestMultiplexer Class
// ######################################################################

void RequestMultiplexer::run() {
  try {
    AbstractReply* reply = (AbstractReply*) NULL;

    while (running) {
      canStop = true;
      try {
        reply = channel->receive();
      } catch (IOException exc) {
        // Check if the connection is not already
        // closed (the exception may occur as a consequence
        // of a closure or at the same time as an independant
        // close call).
        if (! isClosed()) {
          close();
        } else {
          // Else it means that the connection is already closed
          if (exceptionListener != (ExceptionListener*) NULL)
            exceptionListener->onException(ConnectException());
        }
        break;
      }
      canStop = false;
      route(reply);
    }
  } catch (...) {
    printf("RequestMultiplexer::run - Exit on Exception\n");
  }
  finish();
}

RequestMultiplexer::RequestMultiplexer(Connection* cnx,
                                       Channel* channel) throw (XoramException) : Synchronized(), Daemon() {
  if(DEBUG)
    printf("=> RequestMultiplexer():\n");
  this->channel = channel;
  this->cnx = cnx;
  exceptionListener = (ExceptionListener*) NULL;
  requestsTable = new Vector<ReplyListener>();
  requestCounter = 0;
  channel->connect();

  status = OPEN;

  Daemon::start();
  if(DEBUG)
    printf("<= RequestMultiplexer(): requestsTable = 0x%x\n", requestsTable);
}

RequestMultiplexer::~RequestMultiplexer() {
  if(DEBUG)
    printf("~RequestMultiplexer(): requestsTable = 0x%x, exceptionListener = 0x%x\n", requestsTable, exceptionListener);
  cnx = (Connection*) NULL;
  channel = (Channel*) NULL;
  if (requestsTable != (Vector<ReplyListener>*) NULL) {
    delete requestsTable;
    requestsTable = (Vector<ReplyListener>*) NULL;
  }
  if (exceptionListener != (ExceptionListener*) NULL) {
    delete exceptionListener;
    exceptionListener = (ExceptionListener*) NULL;
  }
}

boolean RequestMultiplexer::isClosed() {
  return (status == CLOSE);
}

void RequestMultiplexer::setExceptionListener(ExceptionListener* exceptionListener) {
  this->exceptionListener = exceptionListener;
}

ExceptionListener* RequestMultiplexer::getExceptionListener() {
  return exceptionListener;
}

void RequestMultiplexer::sendRequest(AbstractRequest* request) throw (XoramException) {
  sendRequest(request, (ReplyListener*) NULL);
}

void RequestMultiplexer::sendRequest(AbstractRequest* request,
                                     ReplyListener* listener) throw (XoramException) {
  sync_begin();
  if (status == CLOSE) {
    sync_end();
    throw IllegalStateException();
  }

  if (requestCounter == INTEGER_MAX_VALUE)
    requestCounter = 0;
  else
    requestCounter += 1;
  request->setRequestId(requestCounter);

  if (listener != (ReplyListener*) NULL)
    requestsTable->addElement(listener);
  sync_end();

  channel->send(request);
}

void RequestMultiplexer::close() {
  sync_begin();
  if (status == CLOSE) return;
  status = CLOSE;

  for (int i=0; i<requestsTable->size(); i++) {
    requestsTable->elementAt(i)->replyAborted();
  }
  requestsTable->clear();
  sync_end();

  channel->close();
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
void RequestMultiplexer::route(AbstractReply* reply) {
  int requestId = reply->getCorrelationId();

  if (reply->instanceof(AbstractMessage::MOM_EXCEPTION_REPLY)) {
    if (exceptionListener != (ExceptionListener*) NULL) {
      MomExceptionReply* excReply = (MomExceptionReply*) reply;
      int type = excReply->getType();
      char* msg = excReply->getMessage();
      if (type == MomExceptionReply::AccessException) {
        exceptionListener->onException(SecurityException(msg));
      } else if (type == MomExceptionReply::DestinationException) {
        exceptionListener->onException(InvalidDestinationException(msg));
      } else {
        exceptionListener->onException(MOMException(msg));
      }
    }
  } else {
    ReplyListener* rl = (ReplyListener*) NULL;
    sync_begin();
    for (int i=0; i < requestsTable->size(); i++) {
      if (requestsTable->elementAt(i)->getRequestId() == requestId) {
        rl = requestsTable->removeElementAt(i);
        break;
      }
    }
    sync_end();

    if (rl != (ReplyListener*) NULL) {
      rl->replyReceived(reply);
    } else {
      if (reply->instanceof(AbstractMessage::CONSUMER_MESSAGES)) {
        deny((ConsumerMessages*) reply);
      }
    }
  }
}

void RequestMultiplexer::abortRequest(int requestId) {
  sync_begin();
  if (status == CLOSE) return;

  ReplyListener* rl = (ReplyListener*) NULL;
  for (int i=0; i < requestsTable->size(); i++) {
    if (requestsTable->elementAt(i)->getRequestId() == requestId) {
      rl = requestsTable->removeElementAt(i);
      break;
    }
  }
  sync_end();

  if (rl != (ReplyListener*) NULL) rl->replyAborted();
}

void RequestMultiplexer::deny(ConsumerMessages* messages) {
  Vector<Message>* msgList = messages->getMessages();
  Vector<char>* ids = new Vector<char>();
  for (int i=0; i < msgList->size(); i++) {
    ids->addElement(msgList->elementAt(i)->getMessageID());
  }
  SessDenyRequest* deny = new SessDenyRequest(messages->comesFrom(),
                                             ids,
                                             messages->getQueueMode());
  try {
    sendRequest(deny);
  } catch (Exception exc) {
    // Connection failure, nothing to do
  }

  if (messages != (ConsumerMessages*) NULL) {
    delete messages;
    messages = (ConsumerMessages*) NULL;
  }
}

// ######################################################################
// ReplyListener Class
// ######################################################################

// ######################################################################
// Requestor Class
// ######################################################################

Requestor::Requestor(RequestMultiplexer* mtpx) : Synchronized() {
  if(DEBUG)
    printf("=> Requestor()\n");
  this->mtpx = mtpx;
  status = INIT;
  req = (AbstractRequest*) NULL;
  reply = (AbstractReply*) NULL;
  if(DEBUG)
    printf("<= Requestor()\n");
}

Requestor::~Requestor() {
  if(DEBUG)
    printf("~Requestor(): req = 0x%x, reply = 0x%x, mtpx = 0x%x\n", req, reply, mtpx);
  if (req != (AbstractRequest*) NULL) {
    delete req;
    req = (AbstractRequest*) NULL;
  }
  if (reply != (AbstractReply*) NULL) {
    delete reply;
    reply = (AbstractReply*) NULL;
  }
  // Be careful the RequestMultiplexer is part of the Connection !!
  mtpx = (RequestMultiplexer*) NULL;
}

AbstractReply* Requestor::request(AbstractRequest* req) {
  return request(req, 0);
}

AbstractReply* Requestor::request(AbstractRequest* req, long timeout) {
  sync_begin();
  if (status == CLOSE) {
    sync_end();
    return (AbstractReply*) NULL;
  } else if (status == RUN) {
    sync_end();
    throw IllegalStateException();
  }

  this->req = req;
  reply = (AbstractReply*) NULL;

  mtpx->sendRequest(req, this);
  status = RUN;
  wait(timeout);

  if (status == RUN) {
    // Means that the wait ended with a timeout.
    // Abort the request.
    mtpx->abortRequest(getRequestId());
    this->req = (AbstractRequest*) NULL;
    sync_end();
    return (AbstractReply*) NULL;
  } else if (status == CLOSE) {
    if (reply->instanceof(AbstractMessage::CONSUMER_MESSAGES)) {
      mtpx->deny((ConsumerMessages*) reply);
    }
    this->req = (AbstractRequest*) NULL;
    sync_end();
    return (AbstractReply*) NULL;
  } else if (status == DONE) {
    sync_end();
    this->req = (AbstractRequest*) NULL;
    return reply;
  }
}

int Requestor::getRequestId() {
  return req->getRequestId();
}

void Requestor::abortRequest() {
  sync_begin();
  if (status == RUN) {
    mtpx->abortRequest(getRequestId());
    status = DONE;
    notify();
  }
  sync_end();
}

boolean Requestor::replyReceived(AbstractReply* reply) throw (AbortedRequestException) {
  sync_begin();
  if (status == RUN) {
    this->reply = reply;
    status = DONE;
    notify();
    sync_end();
    return true;
  }
  sync_end();
  // The request has been aborted.
  throw AbortedRequestException();
}

void Requestor::replyAborted() {
  if (status == RUN) {
    reply = (AbstractReply*) NULL;
    status = DONE;
    notify();
  }
}

void Requestor::close() {
  if (status != CLOSE) {
    abortRequest();
    status = CLOSE;
  }
}

// ######################################################################
// Connection Class
// ######################################################################

Connection::Connection(Channel* channel) {
  if(DEBUG)
    printf("=> Connection()\n");
  exclist = (ExceptionListener*) NULL;
  clientid = (char*) NULL;
  this->channel =  channel;
  sessionsC = 0;
  messagesC = 0;
  subsC = 0;

  sessions = new Vector<Session>();

  mtpx = new RequestMultiplexer(this, channel);
  requestor = new Requestor(mtpx);

  status = STOP;

  // Requesting the connection key and proxy identifier:
  CnxConnectRequest* request = new CnxConnectRequest();
  CnxConnectReply* reply =  (CnxConnectReply*) requestor->request(request);
  delete request;
  proxyId = reply->getProxyId();
  key = reply->getCnxKey();

  delete reply;
  reply = (CnxConnectReply*) NULL;

  if(DEBUG)
    printf("<= Connection(): clientid = 0x%x, channel = 0x%x, mtpx = 0x%x, requestor = 0x%x, sessions = 0x%x, proxyId = 0x%x\n", clientid, channel, mtpx, requestor, sessions, proxyId);
}

Connection::~Connection() {
  if(DEBUG)
    printf("~Connection(): exclist = 0x%x, clientid = 0x%x, channel = 0x%x, mtpx = 0x%x, requestor = 0x%x, sessions = 0x%x, proxyId = 0x%x\n", exclist, clientid, channel, mtpx, requestor, sessions, proxyId);
  if (exclist != (ExceptionListener*) NULL) {
    delete exclist;
    exclist = (ExceptionListener*) NULL;
  }
  if (clientid != (char*) NULL) {
    delete[] clientid;
    clientid = (char*) NULL;
  }
  if (channel != (TcpChannel*) NULL) {
    delete (TcpChannel*) channel;
    channel = (TcpChannel*) NULL;
  }
  if (mtpx != (RequestMultiplexer*) NULL) {
    delete mtpx;
    mtpx = (RequestMultiplexer*) NULL;
  }
  if (requestor != (Requestor*) NULL) {
    delete requestor;
    requestor = (Requestor*) NULL;
  }
  if (sessions != (Vector<Session>*) NULL) {
    delete sessions;
    sessions = (Vector<Session>*) NULL;
  }
  if (proxyId != (char*) NULL) {
    delete[] proxyId;
    proxyId = (char*) NULL;
  }

}

boolean Connection::isStopped() {
  return (status == STOP);
}

void Connection::start() {
  if (status == CLOSE) throw IllegalStateException();

  // Ignoring the call if the connection is started:
  if (status == START) return;

  // Starting the sessions:
  for (int i=0; i<sessions->size(); i++) {
    Session* session = sessions->elementAt(i);
    session->start();
  }

  // Sending a start request to the server:
  CnxStartRequest* request = new CnxStartRequest();
  mtpx->sendRequest(request);
  delete request;

   status = START;
}

Session* Connection::createSession() {
  if (status == CLOSE) throw IllegalStateException();

  Session* session = new Session(this, false, Session::AUTO_ACKNOWLEDGE, mtpx);
  sessions->addElement(session);
  if (status == START) session->start();

  return session;
}

void Connection::setExceptionListener(ExceptionListener* exclist) {
  if (status == CLOSE) throw IllegalStateException();
  this->exclist = exclist;
  mtpx->setExceptionListener(exclist);
}

ExceptionListener* Connection::getExceptionListener() {
  if (status == CLOSE) throw IllegalStateException();
  return exclist;
}

char* Connection::getClientID() {
  if (status == CLOSE) throw IllegalStateException();
  return clientid;
}

void Connection::stop() {
  if (status == CLOSE) throw IllegalStateException();

  if (status == STOP) return;

  for (int i=0; i<sessions->size(); i++) {
    Session* session = sessions->elementAt(i);
    session->stop();
  }

  if (status != STOP) {
    // Sending a synchronous "stop" request to the server:
    CnxStopRequest* request = new CnxStopRequest();
    AbstractReply* reply = requestor->request(request);
    delete request;
    request = (CnxStopRequest*) NULL;
    if (reply != (AbstractReply*) NULL) {
      delete reply;
      reply = (AbstractReply*) NULL;
    }

    // Set the status as STOP as the following operations
    // (Session.stop) can't fail.
    status = STOP;
  }
}

void Connection::close() {
  if (status == CLOSE) return;

  for (int i=0; i<sessions->size(); i++) {
    Session* session = sessions->elementAt(i);
    try {
      session->close();
    } catch (Exception exc) { }
  }

  CnxCloseRequest* request = new CnxCloseRequest();
  AbstractReply* reply = requestor->request(request);
  delete request;
  request = (CnxCloseRequest*) NULL;
  if (reply != (AbstractReply*) NULL) {
    delete reply;
    reply = (AbstractReply*) NULL;
  }

  mtpx->close();

  status = CLOSE;
}

/** Returns a new session identifier. */
char* Connection::nextSessionId() {
  char buf[25];
  // TODO
/*   if (sessionsC == Integer.MAX_VALUE) */
/*     sessionsC = 0; */
  sessionsC++;
  sprintf(buf, "c%ds%d", key, sessionsC);
  char* ret = new char[strlen(buf)+1];
  strcpy(ret, buf);
  ret[strlen(buf)] = '\0';
  return ret;
  //  return strdup(buf);
}

/** Returns a new message identifier. */
char* Connection::nextMessageId() {
  char buf[40];
  // TODO
/*   if (messagesC == Integer.MAX_VALUE) */
/*     messagesC = 0; */
  messagesC++;
  sprintf(buf, "ID:%sc%dm%d\0", proxyId+1, key, messagesC);
  char* ret = new char[strlen(buf)+1];
  strcpy(ret, buf);
  ret[strlen(buf)] = '\0';
  return ret;
  //return strdup(buf);
}

/** Returns a new subscription name. */
char* Connection::nextSubName() {
  char buf[30];
  // TODO
/*   if (subsC == Integer.MAX_VALUE) */
/*     subsC = 0; */
  subsC++;
  sprintf(buf, "c%dsub%d", key, subsC);
  char* ret = new char[strlen(buf)+1];
  strcpy(ret, buf);
  ret[strlen(buf)] = '\0';
  return ret;
  //return strdup(buf);
}

// ######################################################################
// ConnectionFactory Class
// ######################################################################

ConnectionFactory::ConnectionFactory() {
  this->dfltLogin = "anonymous";
  this->dfltPassword = "anonymous";
}

// Creates a connection with the default parameters.
// The connection is created in stopped mode, no messages will be
// delivered until the Connection.start method is explicitly called.
Connection* ConnectionFactory::createConnection() {
  return createConnection(dfltLogin, dfltPassword);
};

// ######################################################################
// TCPConnectionFactory Class
// ######################################################################

TCPConnectionFactory::TCPConnectionFactory() {
  this->dfltHost = "localhost";
  this->dfltPort = 16010;
}

TCPConnectionFactory::~TCPConnectionFactory() {
  if(DEBUG)
    printf("~TCPConnectionFactory()\n");
  delete[] dfltHost;
}

TCPConnectionFactory::TCPConnectionFactory(char *host, int port) {
  this->dfltHost = host;
  this->dfltPort = port;
}

// Creates a connection with the specified user identity.
Connection* TCPConnectionFactory::createConnection(char *user, char *pass) {
  return createConnection(user, pass, dfltHost, dfltPort);
}

// Creates a connection to the specified server.
Connection* TCPConnectionFactory::createConnection(char *host, int port) {
  return createConnection(dfltLogin, dfltPassword, host, port);
}

// Creates a connection to the specified server with the specified
// user identity.
Connection* TCPConnectionFactory::createConnection(char *user, char *pass, char *host, int port) {
  return new Connection(new TcpChannel(user, pass, host, port));
}

// ######################################################################
// Destination Class
// ######################################################################

Destination::Destination(char* uid, byte type, char* name) {
  if (uid != (char*) NULL) {
    char* newUid = new char[strlen(uid)+1];
    strcpy(newUid, uid);
    newUid[strlen(uid)] = '\0';
    this->uid = newUid;
  }
  if (name != (char*) NULL) {
    char* newName = new char[strlen(name)+1];
    strcpy(newName, name);
    newName[strlen(name)] = '\0';
    this->name = newName;
  }

 // this->uid = uid;
 // this->name = name;
  this->type = type;
}

char* Destination::getUID() {
  return uid;
}

char* Destination::getName() {
  return name;
}

boolean Destination::isQueue() {
  return (type == QUEUE_TYPE);
}

boolean Destination::isTopic() {
  return (type == TOPIC_TYPE);
}

byte Destination::getType() {
  return type;
}

Destination* Destination::newInstance(char* uid, byte type, char* name) {
  if (type == QUEUE_TYPE) {
    return new Queue(uid, name);
  } else if (type == TOPIC_TYPE) {
    return new Topic(uid, name);
  } else {
    throw XoramException();
  }
}

TemporaryTopic::TemporaryTopic(char* agentId, Requestor* requestor) : Topic(agentId, Destination::typeToString((byte)TOPIC_TEMP_TYPE)) {
  this->requestor = requestor;
}

TemporaryTopic::~TemporaryTopic() {
}

void TemporaryTopic::destroy() {
  if (requestor != (Requestor*) NULL) {
    requestor->request(new TempDestDeleteRequest(getUID()));
  }
}

Requestor* TemporaryTopic::getRequestor() {
  return requestor;
}

boolean TemporaryTopic::isTemporaryTopic() {
  return (getType() == TOPIC_TEMP_TYPE);
}

// ######################################################################
// Session Class
// ######################################################################

/**
 * Opens a session.
 *
 * @param cnx  The connection the session belongs to.
 * @param transacted  <code>true</code> for a transacted session.
 * @param acknowledgeMode  auto, client or dups.
 *
 * @exception JMSException  In case of an invalid acknowledge mode.
 */
Session::Session(Connection* cnx,
                 boolean transacted,
                 int acknowledgeMode,
                 RequestMultiplexer* mtpx) throw (XoramException) {
  if(DEBUG)
    printf("=> Session()\n");
  this->status = NONE;
  this->listenerCount = 0;
  this->cnx = cnx;
  this->ident = cnx->nextSessionId();
  this->transacted = transacted;
  if (transacted)
    this->acknowledgeMode = SESSION_TRANSACTED;
  else
    this->acknowledgeMode = acknowledgeMode;
  this->mtpx = mtpx;

  requestor = new Requestor(mtpx);
  receiveRequestor = new Requestor(mtpx);

  consumers = new Vector<MessageConsumer>();
  producers = new Vector<MessageProducer>();

  pendingMessageConsumer = (MessageConsumer*) NULL;

  if(DEBUG)
    printf("<= Session(): cnx = 0x%x, ident = 0x%x, consumers = 0x%x, producers = 0x%x, mtpx= 0x%x, requestor = 0x%x, receiveRequestor = 0x%x\n", cnx, ident, consumers, producers, mtpx, requestor, receiveRequestor);

/*   repliesIn = new XQueue<MessageListenerContext>(); */
/*   sendings = new Hashtable(); */
/*   deliveries = new Hashtable(); */

/*     setStatus(Status.STOP); */
/*     setSessionMode(SessionMode.NONE); */
/*     setRequestStatus(RequestStatus.NONE); */
}

Session::~Session() {
  if(DEBUG)
    printf("~Session(): cnx = 0x%x, ident = 0x%x, consumers = 0x%x, producers = 0x%x, pendingMessageConsumer = 0x%x, mtpx= 0x%x, requestor = 0x%x, receiveRequestor = 0x%x\n", cnx, ident, consumers, producers, pendingMessageConsumer, mtpx, requestor, receiveRequestor);

  if (ident != (char*) NULL) {
    delete[] ident;
    ident = (char*) NULL;
  }
  if (consumers != (Vector<MessageConsumer>*) NULL) {
    delete consumers;
    consumers = (Vector<MessageConsumer>*) NULL;
  }
  if (producers != (Vector<MessageProducer>*) NULL) {
    delete producers;
    producers = (Vector<MessageProducer>*) NULL;
  }
  // pendingMessageConsumer if exist is already closed and freed as part of consumers vector.
  pendingMessageConsumer = (MessageConsumer*) NULL;
  if (receiveRequestor != (Requestor*) NULL) {
    delete receiveRequestor;
    receiveRequestor = (Requestor*) NULL;
  }
  if (requestor != (Requestor*) NULL) {
    delete requestor;
    requestor = (Requestor*) NULL;
  }
}

Connection* Session::getConnection() {
  return cnx;
}

RequestMultiplexer* Session::getRequestMultiplexer() {
    return mtpx;
}


Topic* Session::createTopic(char* topicName) {
  // Checks if the topic to retrieve is the administration topic:
  if (strcmp(topicName, "#AdminTopic") == 0) {
    GetAdminTopicReply* reply =
      (GetAdminTopicReply*) requestor->request(new GetAdminTopicRequest());
    if (reply->getId() != (char*) NULL)
      return new Topic(reply->getId(), topicName);
    else
      return (Topic*) NULL;
  }
  return new Topic(topicName, topicName);
}

TemporaryTopic* Session::createTemporaryTopic() {
  SessCreateDestReply* reply =
    (SessCreateDestReply*) requestor->request(new SessCreateDestRequest((byte)TOPIC_TEMP_TYPE));

  char* tempDest = reply->getAgentId();
  return new TemporaryTopic(tempDest, requestor);
}

/*
SessCreateDestReply* Session::createDestination(byte type, String name) {
  SessCreateDestReply* reply = (SessCreateDestReply) requestor->request(new SessCreateDestRequest(type, name));
  return reply;
}

TemporaryQueue* Session::createTemporaryQueue() {
  SessCreateDestReply* reply = (SessCreateDestReply) requestor->request(new SessCreateDestRequest((byte)QUEUE_TEMP_TYPE));
  String tempDest = reply->getAgentId();
  return new TemporaryQueue(tempDest, cnx);
}
*/

/**
 * Creates a MessageProducer to send messages to the specified destination.
 *
 * @exception IllegalStateException  If the session is closed or if the
 *              connection is broken.
 * @exception XoramException  If the creation fails for any other reason.
 */
MessageProducer* Session::createProducer(Destination *dest) {
  if (status == CLOSE) throw IllegalStateException();
  /*     checkThreadOfControl(); */
  MessageProducer* prod = new MessageProducer(this, dest);
  addProducer(prod);
  return prod;
}

/**
 * Creates a MessageConsumer for the specified destination using a
 * message selector.
 *
 * @exception IllegalStateException  If the session is closed or if the
 *              connection is broken.
 * @exception XoramException  If the creation fails for any other reason.
 */
MessageConsumer* Session::createConsumer(Destination* dest,
                                         char* selector,
                                         boolean nolocal) {
  if (status == CLOSE) throw IllegalStateException();
  /*     checkThreadOfControl(); */
  MessageConsumer* cons = new MessageConsumer(this, dest,
                                              selector, (char*) NULL,
                                              nolocal);
  addConsumer(cons);
  return cons;
}

/**
 * Creates a MessageConsumer for the specified destination.
 *
 * @exception IllegalStateException  If the session is closed or if the
 *              connection is broken.
 * @exception XoramException  If the creation fails for any other reason.
 */
MessageConsumer* Session::createConsumer(Destination* dest) {
  return createConsumer(dest, (char*) NULL, FALSE);
}

/**
 * Creates a durable subscriber to the specified topic.
 *
 * @exception IllegalStateException  If the session is closed or if the
 *              connection is broken.
 * @exception XoramException  If the creation fails for any other reason.
 */
MessageConsumer* Session::createDurableSubscriber(Topic* topic,
                                                  char* subname) {
  return createDurableSubscriber(topic, subname, (char*) NULL, FALSE);
}

/**
 * Creates a durable subscriber to the specified topic, using a
 * message selector and specifying whether messages published by its
 * own connection should be delivered to it.
 *
 * @exception IllegalStateException  If the session is closed or if the
 *              connection is broken.
 * @exception XoramException  If the creation fails for any other reason.
 */
MessageConsumer* Session::createDurableSubscriber(Topic* topic, char* subname,
                                                  char* selector,
                                                  boolean nolocal) {
  if (status == CLOSE) throw IllegalStateException();
  /*     checkThreadOfControl(); */
  MessageConsumer* cons = new MessageConsumer(this, topic,
                                              selector, subname,
                                              nolocal);
  addConsumer(cons);
  return cons;
}

/**
 * Unsubscribes a durable subscription that has been created by a client.
 */
void Session::unsubscribe(char* name) {
  if (status == CLOSE) throw IllegalStateException();
  /*     checkThreadOfControl(); */
  MessageConsumer* cons;
  // TODO: Durable
/*   if (consumers != null) { */
/*     for (int i = 0; i < consumers.size(); i++) { */
/*       cons = (MessageConsumer) consumers.get(i); */
/*       if (! cons.queueMode && cons.targetName.equals(name)) */
/*         throw XoramException(); */
/*     } */
/*   } */
  AbstractReply* reply = syncRequest(new ConsumerUnsubRequest(name));
  if (reply != (AbstractReply*) NULL) {
    delete reply;
    reply = (AbstractReply*) NULL;
  }
}

/**
 * Creates a Message object.
 *
 * @exception IllegalStateException  If the session is closed.
 */
Message* Session::createMessage() {
  if (status == CLOSE) throw IllegalStateException();
  return new Message();
}

/**
 * Returns the acknowledgement mode of the session.
 */
int Session::getAcknowledgeMode() {
  return acknowledgeMode;
}

/**
 * Indicates whether the session is in transacted mode.
 */
boolean Session::getTransacted() {
  return transacted;
}

/**
 * Commits all messages done in this transaction.
 */
void Session::commit() {
  if (status == CLOSE) throw IllegalStateException();
/*   checkThreadOfControl(); */

  if (! transacted) throw IllegalStateException();

  // TODO: transacted
  throw NotYetImplementedException();

/*   // Sending client messages: */
/*   try { */
/*     CommitRequest commitReq= new CommitRequest(); */

/*     Enumeration producerMessages = sendings.elements(); */
/*     while (producerMessages.hasMoreElements()) { */
/*       ProducerMessages pM =  */
/*         (ProducerMessages) producerMessages.nextElement(); */
/*       commitReq.addProducerMessages(pM); */
/*     } */
/*     sendings.clear(); */

/*     // Acknowledging the received messages: */
/*     Enumeration targets = deliveries.keys(); */
/*     while (targets.hasMoreElements()) { */
/*       String target = (String) targets.nextElement(); */
/*       MessageAcks acks = (MessageAcks) deliveries.get(target); */
/*       commitReq.addAckRequest( */
/*                               new SessAckRequest( */
/*                                                  target,  */
/*                                                  acks.getIds(), */
/*                                                  acks.getQueueMode())); */
/*     } */
/*     deliveries.clear(); */

/*     if (asyncSend) { */
/*       // Asynchronous sending */
/*       commitReq.setAsyncSend(true); */
/*       mtpx.sendRequest(commitReq); */
/*     } else { */
/*       requestor.request(commitReq); */
/*     } */
/*   } catch (JMSException jE) { */
/*   // Catching an exception if the sendings or acknowledgement went wrong: */
/*     TransactionRolledBackException tE =  */
/*       new TransactionRolledBackException("A JMSException was thrown during" */
/*                                          + " the commit."); */
/*     tE.setLinkedException(jE); */

/*     rollback(); */
/*     throw tE; */
/*   } */
}

/**
 * Rolls back any messages done in this transaction.
 */
void Session::rollback() {
  if (status == CLOSE) throw IllegalStateException();
/*   checkThreadOfControl(); */

  if (! transacted) throw IllegalStateException();

  // TODO: transacted
  throw NotYetImplementedException();

/*   // Denying the received messages: */
/*   deny(); */
/*   // Deleting the produced messages: */
/*   sendings.clear(); */
}

/**
 * Starts the asynchronous deliveries in the session.
 */
void Session::start() {
  if ((status == CLOSE) || (status == START)) return;

  if (listenerCount > 0) {
/*     repliesIn->start(); */
    Daemon::start();
/*     singleThreadOfControl = daemon.getThread(); */
  }

  status = START;
}

void Session::stop() {
  if ((status == STOP) || (status == CLOSE)) return;
  if (isRunning() == TRUE) Daemon::stop();

/*     singleThreadOfControl = null; */
  status = STOP;
}

/**
 * Closes the session.
 *
 * @exception XoramException
 */
void Session::close() {
  // TODO
/*     Vector consumersToClose = (Vector)consumers.clone(); */
/*     consumers.clear(); */
/*     for (int i = 0; i < consumersToClose.size(); i++) { */
/*       MessageConsumer mc =  */
/*         (MessageConsumer)consumersToClose.elementAt(i); */
/*       try { */
/*         mc.close(); */
/*       } catch (JMSException exc) { */
/*         if (logger.isLoggable(BasicLevel.DEBUG)) */
/*           logger.log( */
/*             BasicLevel.DEBUG, "", exc); */
/*       } */
/*     } */

/*     Vector producersToClose = (Vector)producers.clone(); */
/*     producers.clear(); */
/*     for (int i = 0; i < producersToClose.size(); i++) { */
/*       MessageProducer mp =  */
/*         (MessageProducer)producersToClose.elementAt(i); */
/*       try { */
/*         mp.close(); */
/*       } catch (JMSException exc) { */
/*         if (logger.isLoggable(BasicLevel.DEBUG)) */
/*           logger.log( */
/*             BasicLevel.DEBUG, "", exc); */
/*       } */
/*     } */

/*     // This is now in removeMessageListener */
/*     // called by MessageConsumer.close() */
/*     // (see above) */
/* //     try { */
/* //       repliesIn.stop(); */
/* //     } catch (InterruptedException iE) {} */

  //stop();

/*     // The requestor must be closed because */
/*     // it could be used by a concurrent receive */
/*     // as it is not synchronized (see receive()). */
/*     receiveRequestor.close(); */

/*     cnx.closeSession(this); */

  status = CLOSE;
}

void Session::run() {
  try {
    while (running) {
      canStop = true;
/*       try { */
/*         reply = (AbstractReply*) repliesIn->get(); */
/*       } catch (Exception exc) { */
/*         continue; */
/*       } */
/*       canStop = false; */

/*       // Processing it through the session: */
/*       distribute(reply); */
/*       repliesIn->pop(); */
    }
  } catch (Exception exc) {
  }

  finish();
}

void Session::distribute(AbstractReply* asyncReply) {
/*   if (asyncReply->getClassId() != AbstractMessage::CONSUMER_MESSAGES) { */
/*     // TODO: write an error message. */
/*     return; */
/*   } */

/*   // Getting the message: */
/*   ConsumerMessages* reply = (ConsumerMessages*) asyncReply; */

/*   // Getting the consumer: */
/*   MessageConsumer* cons = (MessageConsumer*) NULL; */
/*   if (reply->getQueueMode()) { */
/*     cons = (MessageConsumer*) cnx->requestsTable->remove(reply->getKey()); */
/*   } else { */
/*     cons = (MessageConsumer*) cnx->requestsTable->get(reply.getKey()); */
/*   } */

/*   // Passing the message(s) to the consumer: */
/*   if (cons != (MessageConsumer*) NULL) { */
/*     Vector<Message>* msgs = reply->getMessages(); */
/*     for (int i=0; i<msgs->size(); i++) */
/*       cons.onMessage((Message*) msgs->get(i)); */
/*   }  else { */
/*     // The target consumer of the received message may be null if it has */
/*     // been closed without having stopped the connection: denying the */
/*     // deliveries. */
/*     Vector<Message>* msgs = reply->getMessages(); */
/*     Message* msg; */
/*     Vector<char*> ids = new Vector<char*>(); */
/*     for (int i = 0; i < msgs->size(); i++) { */
/*       msg = (Message*) msgs->get(i); */
/*       ids->addElement(msg->getMessageID()); */
/*     } */

/*     if (ids.isEmpty()) */
/*       return; */

/*     try {  */
/*       cnx.asyncRequest(new SessDenyRequest(reply.comesFrom(), ids, */
/*                                            reply.getQueueMode(), true)); */
/*     } */
/*     catch (JMSException jE) {} */
/*   } */
}

long long currentTimeMillis() {
  long long time;
  struct timespec ts;

  clock_gettime(CLOCK_REALTIME, &ts);
  time = ts.tv_sec *1000;
  time += ts.tv_nsec /1000000;

  return time;
}

void Session::send(Message* msg, Destination* dest,
                   int deliveryMode,
                   int priority,
                   long timeToLive) throw (XoramException) {
  if (status == CLOSE) throw IllegalStateException();
  /*     checkThreadOfControl(); */

  // Updating the message property fields:
  msg->setMessageID(cnx->nextMessageId());
  msg->setDeliveryMode(deliveryMode);
  msg->setDestination(dest);
  msg->setTimestamp(currentTimeMillis());
  if (timeToLive == 0) {
    msg->setExpiration(0);
  } else {
    msg->setExpiration(msg->getTimestamp() + timeToLive);
  }
  msg->setPriority(priority);

  if (transacted) {
    // TODO: transacted
    throw NotYetImplementedException();
    // If the session is transacted, keeping the request for later delivery:
/*     prepareSend(dest, msg->clone()); */
  } else {
     ProducerMessages* pM = new ProducerMessages(dest->getUID(), msg->clone());
    AbstractReply* reply = requestor->request(pM);
    if (reply != (AbstractReply*) NULL) {
      delete reply;
      reply = (AbstractReply*) NULL;
    }
    if (pM != (ProducerMessages*) NULL) {
      delete pM;
      pM = (ProducerMessages*) NULL;
    }
  }
}

void Session::addProducer(MessageProducer* prod) {
  producers->addElement(prod);
}

void Session::closeProducer(MessageProducer* prod) {
  producers->removeElement(prod);
}

Message* Session::receive(long timeOut1, long timeOut2,
                          MessageConsumer* cons,
                          char* targetName,
                          char* selector, boolean queueMode) throw (XoramException) {
    if (status == CLOSE) throw IllegalStateException();
/*   checkThreadOfControl(); */

    // Don't call checkSessionMode because
    // we also check that the session mode is not
    // already set to RECEIVE.
/*     switch (sessionMode) { */
/*     case SessionMode.NONE: */
/*       setSessionMode(SessionMode.RECEIVE); */
/*       break; */
/*     default: */
/*       throw new IllegalStateException("Illegal session mode"); */
/*     } */

/*     singleThreadOfControl = pthread_self(); */
    pendingMessageConsumer = cons;

    try {
      ConsumerMessages* reply = (ConsumerMessages*) NULL;
      ConsumerReceiveRequest* request = new ConsumerReceiveRequest(targetName, selector, timeOut1, queueMode);
/*       if (receiveAck) request.setReceiveAck(true); */
      reply = (ConsumerMessages*) receiveRequestor->request(request, timeOut2);

      delete request;
      request = (ConsumerReceiveRequest*) NULL;

/*       synchronized (this) { */
/*         // The session may have been  */
/*         // closed in between. */
/*         if (status == Status.CLOSE) { */
/*           if (reply != null) { */
/*             mtpx.deny(reply); */
/*           } */
/*           return null; */
/*         } */

        if (reply != (ConsumerMessages*) NULL) {
          Vector<Message>* msgs = reply->getMessages();
          if ((msgs != (Vector<Message>*) NULL) && (msgs->size() != 0)) {
            Message* msg = msgs->elementAt(0)->clone();
            char* msgId = msg->getMessageID();

/*             // Auto ack: acknowledging the message: */
/*             if (autoAck && ! receiveAck) { */
              ConsumerAckRequest* req = new ConsumerAckRequest(targetName, queueMode);
              if (msgId != (char*) NULL) {
                char* newMsgID = new char[strlen(msgId)+1];
                strcpy(newMsgID, msgId);
                newMsgID[strlen(msgId)] = '\0';
                msgId = newMsgID;
              }
              req->addId(msgId);
              mtpx->sendRequest(req);
              delete req;
/*             } else { */
/*               prepareAck(targetName, msgId, queueMode); */
/*             } */
            msg->session = this;

            delete reply;
            reply = (ConsumerMessages*) NULL;

            pendingMessageConsumer = (MessageConsumer*) NULL;
            return msg;
          } else {
            pendingMessageConsumer = (MessageConsumer*) NULL;
            return (Message*) NULL;
          }
        } else {
          pendingMessageConsumer = (MessageConsumer*) NULL;
          return (Message*) NULL;
        }
/*       } */
    } catch (...) {
/*       singleThreadOfControl = -1; */
      pendingMessageConsumer = (MessageConsumer*) NULL;
    }
    pendingMessageConsumer = (MessageConsumer*) NULL;
}

void Session::addConsumer(MessageConsumer* cons) {
  consumers->addElement(cons);
}

void Session::closeConsumer(MessageConsumer* cons) throw (XoramException) {
  consumers->removeElement(cons);

  // TODO
/*   if (pendingMessageConsumer == cons) { */
/*     if (requestStatus == RequestStatus.RUN) { */
/*       // Close the requestor. A call to abortRequest()  */
/*       // is not enough because the receiving thread  */
/*       // may call request() just after this thread  */
/*       // calls abort(). */
/*       receiveRequestor.close(); */

/*       // Wait for the end of the request */
/*       try { */
/*         while (requestStatus != RequestStatus.NONE) { */
/*           wait(); */
/*         } */
/*       } catch (InterruptedException exc) {} */

/*       // Create a new requestor. */
/*       receiveRequestor = new Requestor(mtpx); */
/*     } */
/*   } */
}

/**
 * Called by MessageConsumer
 */
void Session::addMessageListener(MessageConsumerListener* mcl) {
/*   checkClosed(); */
/*   checkThreadOfControl(); */

/*   checkSessionMode(SessionMode.LISTENER); */

  listenerCount++;

  if (status == START && listenerCount == 1) {
    // It's the first message listener, starts the session's thread.
    Daemon::start();
  }
}

/**
 * Called by MessageConsumer. The thread of control and the status
 * must be checked if the call results from a setMessageListener
 * but not from a close.
 */
void Session::removeMessageListener(MessageConsumerListener* mcl) {
/*   if (check) { */
/*     checkClosed(); */
/*     checkThreadOfControl(); */
/*   } */

/*   synchronized (this) { */
    listenerCount--;
    if (status == START && listenerCount == 0) {
      // All the message listeners have been closed
      // so we can call doStop() in a synchronized
      // block. No deadlock possible.
      Daemon::stop();
/*       singleThreadOfControl = null; */
    }
/*   } */
}

/**
 * The requestor raises an exception if it is called during another request.
 * This cannot happen as a session is monothreaded.
 */
AbstractReply* Session::syncRequest(AbstractRequest* request) throw (XoramException) {
  return requestor->request(request);
}

// ######################################################################
// MessageProducer Class
// ######################################################################

/**
 * Constructs a message producer.
 *
 * @param session  The session the producer belongs to.
 * @param dest     The destination the producer sends messages to.
 *
 * @exception IllegalStateException
 *	If the connection is broken.
 * @exception XoramException
 *	If the creation fails for any other reason.
 */
MessageProducer::MessageProducer(Session* session,
                                 Destination* dest) throw (IllegalStateException) {
  this->deliveryMode = DeliveryMode::PERSISTENT;
  this->priority = 4;
  this->timeToLive = 0;
  this->closed = false;
  this->session = session;
  this->dest = dest;
}

MessageProducer::~MessageProducer() {
  if(DEBUG)
    printf("~MessageProducer()\n");
}

/**
 * Sets the producer's default delivery mode.
 *
 * @exception IllegalStateException
 *  	If the producer is closed.
 * @exception IllegalArgumentException
 *	When setting an invalid delivery mode.
 */
void MessageProducer::setDeliveryMode(int deliveryMode) throw (IllegalStateException, IllegalArgumentException) {
  if (closed) throw IllegalStateException();

  if ((deliveryMode != DeliveryMode::PERSISTENT) &&
      (deliveryMode != DeliveryMode::NON_PERSISTENT))
    throw IllegalArgumentException();

  this->deliveryMode = deliveryMode;
}

/**
 * Sets the producer's default priority.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 * @exception IllegalArgumentException
 *	When setting an invalid priority.
 */
void MessageProducer::setPriority(int priority) throw (IllegalStateException, IllegalArgumentException) {
  if (closed) throw IllegalStateException();
  if (priority < 0 || priority > 9) throw IllegalArgumentException();

  this->priority = priority;
}

/**
 * Sets the default duration of time in milliseconds that a produced
 * message should be retained by the provider.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 */
void MessageProducer::setTimeToLive(long timeToLive) throw (IllegalStateException) {
  if (closed) throw IllegalStateException();

  this->timeToLive = timeToLive;
}

/**
 * Gets the destination associated with this MessageProducer.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 */
Destination* MessageProducer::getDestination() throw (IllegalStateException) {
  if (closed) throw IllegalStateException();

  return dest;
}

/**
 * Gets the producer's default delivery mode.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 */
int MessageProducer::getDeliveryMode() throw (IllegalStateException) {
  if (closed) throw IllegalStateException();

  return deliveryMode;
}

/**
 * Gets the producer's default priority.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 */
int MessageProducer::getPriority() throw (IllegalStateException) {
  if (closed) throw IllegalStateException();

  return priority;
}

/**
 * Gets the default duration in milliseconds that a produced message
 * should be retained by the provider.
 *
 * @exception IllegalStateException
 *	If the producer is closed.
 */
long MessageProducer::getTimeToLive() throw (IllegalStateException) {
  if (closed) throw IllegalStateException();

  return timeToLive;
}

/**
 * Sends a message with the default delivery parameters.
 *
 * @exception InvalidDestinationException
 *	If the destinationInvalidDestinationException is unidentified.
 * @exception IllegalStateException
 *	If the producer is closed, or if the connection is broken.
 * @exception XoramException
 *	If the request fails for any other reason.
 */
void MessageProducer::send(Message* msg) throw (XoramException) {
  send(msg, dest);
}

void MessageProducer::send(Message* msg, Destination* dest) throw (XoramException) {
  send(msg, dest, deliveryMode, priority, timeToLive);
}

/**
 * Sends a message with given delivery parameters.
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
void MessageProducer::send(Message* msg, Destination* dest,
                           int deliveryMode,
                           int priority,
                           long timeToLive) throw (XoramException) {
  if (dest == NULL)
    throw InvalidDestinationException();

  if (msg  == NULL)
    throw MessageFormatException();

  if (closed) throw IllegalStateException();

  session->send(msg, dest, deliveryMode, priority, timeToLive);
}

/**
 * Closes the message producer.
 * API method.
 */
void MessageProducer::close() {
  // Ignoring call if producer is already closed:
  if (closed) return;

  session->closeProducer(this);
  closed = true;
}

// ######################################################################
// MessageConsumer Class
// ######################################################################

/**
 * Constructs a MessageConsumer for the specified destination.
 *
 * @param session  The session the producer belongs to.
 * @param dest     The destination the producer sends messages to.
 *
 * @exception IllegalStateException
 *	If the connection is broken.
 * @exception XoramException
 *	If the creation fails for any other reason.
 */
MessageConsumer::MessageConsumer(Session* session,
                                 Destination* dest,
                                 char* selector) throw (XoramException) {
  MessageConsumer(session, dest, selector, (char*) NULL, FALSE);
}

/**
 * Constructs a MessageConsumer for the specified destination using a
 * message selector.
 *
 * @param session  The session the producer belongs to.
 * @param dest     The destination the producer sends messages to.
 *
 * @exception IllegalStateException
 *	If the connection is broken.
 * @exception XoramException
 *	If the creation fails for any other reason.
 */
MessageConsumer::MessageConsumer(Session* session,
                                 Destination* dest,
                                 char* selector,
                                 char* subName,
                                 boolean noLocal) throw (XoramException) {
  if (dest == (Destination*) NULL) throw InvalidDestinationException();

  // TODO: temporary destination are not yet implemented.
  // If the destination is temporary, verify that the connection is
  // the right one.
  /*     if (dest instanceof TemporaryQueue) { */
  /*       Connection tempQCnx = ((TemporaryQueue) dest).getCnx(); */

  /*       if (tempQCnx == null || ! tempQCnx.equals(sess.getConnection())) */
  /*         throw new SecurityException(); */
  /*     } else if (dest instanceof TemporaryTopic) { */
  /*       Connection tempTCnx = ((TemporaryTopic) dest).getCnx(); */

  /*       if (tempTCnx == null || ! tempTCnx.equals(sess.getConnection())) */
  /*         throw new SecurityException(); */
  /*     } */

  // TODO: selectors are not yet implemented.
  // Checks the selector validity.
  /*     try { */
  /*       ClientSelector.checks(selector); */
  /*     } catch (org.objectweb.joram.shared.excepts.SelectorException sE) { */
  /*       throw new InvalidSelectorException("Invalid selector syntax: " + sE); */
  /*     } */


  if(DEBUG)
    printf("=> MessageConsumer():\n");
  // If the destination is a topic, the consumer is a subscriber:
  if (dest->isQueue()) {
    targetName = dest->getUID();
    queueMode = true;
  } else {
    if (subName == (char*) NULL) {
      subName = session->getConnection()->nextSubName();
      durableSubscriber = false;
    } else {
      durableSubscriber = true;
    }
    AbstractReply* reply = session->syncRequest(new ConsumerSubRequest(dest->getUID(), subName,
                                                selector,
                                                noLocal,
                                                durableSubscriber));
    if (reply != (AbstractReply*) NULL) {
      delete reply;
      reply = (AbstractReply*) NULL;
    }
    targetName = subName;
    this->noLocal = noLocal;
    queueMode = false;
  }

  this->session = session;
  this->dest = dest;
  this->selector = (char*) NULL;
  this->mcl = (MessageConsumerListener*) NULL;

  this->closed = false;
  if(DEBUG)
    printf("<= MessageConsumer(): mcl = 0x%x\n", mcl);
}

MessageConsumer::~MessageConsumer() {
  if(DEBUG)
    printf("~MessageConsumer(): mcl = 0x%x\n", mcl);
  if (mcl != (MessageConsumerListener*) NULL) {
    delete mcl;
    mcl = (MessageConsumerListener*) NULL;
  }
}

char* MessageConsumer::getTargetName() {
  return targetName;
}

boolean MessageConsumer::getQueueMode() {
  return queueMode;
}

/**
 * Sets the message consumer's MessageListener.
 * <p>
 * This method must not be called if the connection the consumer belongs to
 * is started, because the session would then be accessed by the thread
 * calling this method and by the thread controlling asynchronous deliveries.
 * This situation is clearly forbidden by the single threaded nature of
 * sessions. Moreover, unsetting a message listener without stopping the
 * connection may lead to the situation where asynchronous deliveries would
 * arrive on the connection, the session or the consumer without being
 * able to reach their target listener!
 *
 * @exception IllegalStateException  If the consumer is closed, or if the
 *              connection is broken.
 * @exception JMSException  If the request fails for any other reason.
 */
void MessageConsumer::setMessageListener(MessageListener* listener) throw (XoramException) {
  if (closed) throw IllegalStateException();

  throw NotYetImplementedException();

  if (mcl != (MessageConsumerListener*) NULL) {
    if (listener != (MessageListener*) NULL) throw IllegalStateException();

    session->removeMessageListener(mcl);
    mcl = (MessageConsumerListener*) NULL;
  } else {
    if (listener != (MessageListener*) NULL) {
      mcl = new MessageConsumerListener(session->getRequestMultiplexer(),
                                        listener);
      session->addMessageListener(mcl);
      // TODO: listener
/*       mcl->start(); */
    }
  }
}

/**
 * Gets the message consumer's MessageListener.
 *
 * @exception IllegalStateException  If the consumer is closed.
 */
MessageListener* MessageConsumer::getMessageListener() throw (XoramException) {
  if (closed) throw IllegalStateException();

  if (mcl == (MessageConsumerListener*) NULL)
    return (MessageListener*) NULL;
  return mcl->getMessageListener();
}

/**
 * Gets this message consumer's message selector expression.
 * API method.
 *
 * @exception IllegalStateException  If the consumer is closed.
 */
char* MessageConsumer::getMessageSelector() throw (XoramException) {
  if (closed) throw IllegalStateException();
  return selector;
}

/**
 * Receives the next message produced for this message consumer.
 *
 * @exception IllegalStateException
 *	If the consumer is closed, or if the connection is broken.
 * @exception SecurityException
 *	If the requester is not a READER on the destination.
 * @exception XoramException
 *	If the request fails for any other reason.
 */
Message* MessageConsumer::receive() throw (XoramException) {
  return receive(0);
}

/**
 * Receives the next message that arrives before the specified timeout.
 *
 * @exception IllegalStateException
 *	If the consumer is closed, or if the connection is broken.
 * @exception SecurityException
 *	If the requester is not a READER on the destination.
 * @exception XoramException
 *	If the request fails for any other reason.
 */
Message* MessageConsumer::receive(long timeOut) throw (XoramException) {
  if (closed) throw IllegalStateException();

  return session->receive(timeOut, timeOut, this,
                          targetName, selector, queueMode);
}

/**
 * Receives the next message if one is immediately available.
 *
 * @exception IllegalStateException
 *	If the consumer is closed, or if the connection is broken.
 * @exception SecurityException
 *	If the requester is not a READER on the destination.
 * @exception XoramException
 *	If the request fails for any other reason.
 */
Message* MessageConsumer::receiveNoWait() throw (XoramException) {
  if (closed) throw IllegalStateException();

  if (session->getConnection()->isStopped()) {
    return (Message*) NULL;
  } else {
    return session->receive(-1, 0, this,
                            targetName, selector, queueMode);
  }
}

/**
 * Closes the message consumer.
 * API method.
 */
void MessageConsumer::close() {
  // Ignoring call if producer is already closed:
  if (closed) return;
  closed = true;

  if (!queueMode) {
    // For a topic, remove the subscription.
    if (durableSubscriber) {
      try {
        AbstractReply* reply = session->syncRequest(new ConsumerCloseSubRequest(targetName));
        if (reply != (AbstractReply*) NULL) {
          delete reply;
          reply = (AbstractReply*) NULL;
        }
      } catch (XoramException exc) {
        // TODO
      }
    } else {
      try {
        AbstractReply* reply = session->syncRequest(new ConsumerUnsubRequest(targetName));
        if (reply != (AbstractReply*) NULL) {
          delete reply;
          reply = (AbstractReply*) NULL;
        }
      } catch (XoramException exc) {
        // TODO
      }
    }
  }
  session->closeConsumer(this);

  if (mcl != (MessageConsumerListener*) NULL) {
    // This may block if a message listener
    // is currently receiving a message (onMessage is called)
    // so we have to be out of the synchronized block.
    mcl->close();

    // Stop the listener.
    session->removeMessageListener(mcl);
  }
}

// ######################################################################
// MessageConsumerListener Class
// ######################################################################
MessageConsumerListener::MessageConsumerListener(RequestMultiplexer* mtpx,
                                                 MessageListener* listener) {
  this->mtpx = mtpx;
  this->listener = listener;

  status = INIT;
}

/**
 * Called by Session.
 */
void MessageConsumerListener::request(AbstractRequest* req) {
  sync_begin();
  if (status != INIT) {
    sync_end();
    throw IllegalStateException();
  }

  this->req = req;
  reply =(AbstractReply*) NULL;

  status = RUN;
  sync_end();

  mtpx->sendRequest(req, this);
}

AbstractReply* MessageConsumerListener::getReply() {
  if (status == RUN) return (AbstractReply*) NULL;

  if (status == DONE) return reply;

  throw AbortedRequestException();
}

void MessageConsumerListener::abortRequest() {
  sync_begin();
  if (status == RUN) {
    mtpx->abortRequest(getRequestId());
    status = INIT;
  }
  sync_end();
}

void MessageConsumerListener::close() throw(XoramException) {
  if (status != CLOSE) {
    sync_begin();
    status = CLOSE;
    sync_end();
    abortRequest();
  }
}

MessageListener* MessageConsumerListener::getMessageListener() {
  return listener;
}


/**
 * Called by RequestMultiplexer.
 */
int MessageConsumerListener::getRequestId() {
  return req->getRequestId();
}

boolean MessageConsumerListener::replyReceived(AbstractReply* reply) throw(AbortedRequestException) {
  sync_begin();
  if (status == RUN) {
    this->reply = reply;
    status = DONE;
    sync_end();
    return true;
  }
  sync_end();

  // The request has been aborted.
  throw AbortedRequestException();
}

/*   if (queueMode) { */
/*     // 1- Change the status before pushing the  */
/*     // messages into the session queue. */
/*     setReceiveStatus(ReceiveStatus.CONSUMING_REPLY); */
/*   } */

/*   try { */
/*     ConsumerMessages* cm = (ConsumerMessages*) NULL; */
/*     if (reply->getClassId() != AbstractMessage::CONSUMER_MESSAGES) */
/*       cm = (ConsumerMessages*) reply; */
/*     // 2- increment messageCount (synchronized) */
/*     messageCount += cm->getMessageCount(); */

/*     session->repliesIn->push(new MessageListenerContext(this, cm)); */
/*   } catch (StoppedQueueException exc) { */
/*     throw AbortedRequestException(); */
/*   } catch (XoramException exc) { */
/*     throw AbortedRequestException(); */
/*   } */
/*   if (queueMode) { */
/*     return true; */
/*   } else { */
/*     return false; */
/*   } */


void MessageConsumerListener::replyAborted() {
  if (status == RUN) {
    reply = (AbstractReply*) NULL;
    status = DONE;
  }
}

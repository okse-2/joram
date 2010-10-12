/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
/*
#include <unistd.h>

#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h> 
#include <netinet/in.h>
#include <netdb.h>

#include "Synchronized.H"
#include "Daemon.H"
#include "XQueue.H"
*/

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

#include "XoramAdmin.H"
#include "AbstractAdminMessage.H"

AdminRequestor::AdminRequestor(Connection* cnx) {
  if(DEBUG)
    printf("=> XoramAdmin: AdminRequestor(0x%x)\n", cnx);

  this->cnx = cnx;
  this->sess = cnx->createSession();
  topic = sess->createTopic("#AdminTopic");
  producer = sess->createProducer(topic);
  tmpTopic = sess->createTemporaryTopic();
  consumer = sess->createConsumer(tmpTopic);
}

AdminRequestor::~AdminRequestor() {
  if(DEBUG)
    printf("=> XoramAdmin: ~AdminRequestor()\n");
}

Message* AdminRequestor::request(AdminRequest* request, long timeout) {
  if(DEBUG)
    printf("=> XoramAdmin: request(0x%x,%d)\n", request, timeout);
  AdminMessage* requestMsg = new AdminMessage();
  requestMsg->setAdminMessage(request);
  requestMsg->setReplyTo(tmpTopic);
  producer->send(requestMsg);
  char* correlationId = requestMsg->getMessageID();
  while (true) {
    Message* reply = consumer->receive(timeout);
    if (reply == (Message*) NULL) {
      // TODO throw new JMSException("Interrupted request");
      printf("**** EXCEPTION : reply = NULL.\n");
    } else {
      if (strcmp(correlationId,reply->getCorrelationID()) == 0) {
        return reply;
      } else {
        printf("reply id (%s) != request id (%s)\n",reply->getCorrelationID(),  correlationId);
        continue;
      }
    }
  }
}

void AdminRequestor::close() {
  if(DEBUG)
    printf("=> XoramAdmin: close()\n");
  consumer->close();
  producer->close();
  tmpTopic->destroy();
  sess->close();
}

XoramAdmin::XoramAdmin() {
  if(DEBUG)
    printf("<=> XoramAdmin::XoramAdmin()\n");
   cnx =  (Connection*) NULL;
}
XoramAdmin::~XoramAdmin() {
}


/**
 * Opens a connection dedicated to administering with the Joram server
 * which parameters are wrapped by a given
 * <code>TopicConnectionFactory</code>.
 *
 * @param cnxFact  The TopicConnectionFactory to use for connecting.
 * @param name  Administrator's name.
 * @param password  Administrator's password.
 *
 * @exception ConnectException  If connecting fails.
 * @exception AdminException  If the administrator identification is
 *              incorrect.
 */
void XoramAdmin::connect(TCPConnectionFactory* cnxFact,
                         char* name,
                         char* password) {
  if (cnx != (Connection*) NULL)
    return;

  cnx = cnxFact->createConnection(name, password);
  requestor = new AdminRequestor(cnx);
  cnx->start();

  /*
    org.objectweb.joram.client.jms.FactoryParameters params = null;
    params = ((org.objectweb.joram.client.jms.ConnectionFactory)
    cnxFact).getParameters();
    
    localHost = params.getHost();
    localPort = params.getPort();

  // Getting the id of the local server:
  localServer = requestor.getLocalServerId();
  */
}

/**
 * Opens a TCP connection with the Joram server running on a given host and
 * listening to a given port.
 *
 * @param host  The name or IP address of the host the server is running on.
 * @param port  The number of the port the server is listening to.
 * @param name  Administrator's name.
 * @param password  Administrator's password.
 * @param cnxTimer  Timer in seconds during which connecting to the server
 *          is attempted.
 * @param reliableClass  Reliable class name.
 *
 * @exception UnknownHostException  If the host is invalid.
 * @exception ConnectException  If connecting fails.
 * @exception AdminException  If the administrator identification is
 *              incorrect.
 */
void XoramAdmin::connect(char* hostName,
                         int port,
                         char* name,
                         char* password,
                         int cnxTimer,
                         char* reliableClass) {
    TCPConnectionFactory* cnxFact = new TCPConnectionFactory(hostName, port);
    XoramAdmin::connect(cnxFact, name, password);
}
     
void XoramAdmin::connect(char* hostName,
                         int port,
                         char* name,
                         char* password,
                         int cnxTimer) {
 XoramAdmin::connect(hostName, port, name, password, cnxTimer, NULL);
}

void XoramAdmin::connect(char* name, char* password, int cnxTimer) {
  XoramAdmin::connect("localhost", 16010, name, password, cnxTimer);
}

void XoramAdmin::disconnect() {
  requestor->close();
  cnx->close();
}

  //void setDefaultDMQ(int serverId, DeadMQueue* dmq);
  //void setDefaultDMQ(DeadMQueue* dmq);

/**
 * Method actually sending an <code>AdminRequest</code> instance to
 * the platform and getting an <code>AdminReply</code> instance.
 *
 * @exception ConnectException  If the connection to the platform fails.
 * @exception AdminException  If the platform's reply is invalid, or if
 *              the request failed.
 */
AdminReply* XoramAdmin::doRequest(AdminRequest* request, long timeout) {
  if (cnx == (Connection*) NULL)
    //TODO throw new ConnectException("Admin connection not established.");
    return (AdminReply*) NULL;
  
  if (timeout < 1)
    timeout = 120000;
  
  AdminMessage* replyMsg = (AdminMessage*) requestor->request(request, timeout);
  AdminReply* reply = (AdminReply*) replyMsg->getAdminMessage();

  /*
    if (! reply.succeeded()) {
    switch (reply.getErrorCode()) {
    case AdminReply.NAME_ALREADY_USED:
    throw new NameAlreadyUsedException(reply.getInfo());
    case AdminReply.START_FAILURE:
    throw new StartFailureException(reply.getInfo());
    case AdminReply.SERVER_ID_ALREADY_USED:
    throw new ServerIdAlreadyUsedException(reply.getInfo());
    case AdminReply.UNKNOWN_SERVER:
    throw new UnknownServerException(reply.getInfo());
    default:
    throw new AdminException(reply.getInfo());
    }
    } else {
  */
  return reply;
}

AdminReply* XoramAdmin::doRequest(AdminRequest* request) {
  return doRequest(request, 120000);
}

/**
 * Admin method setting free reading access to this destination.
 * <p>
 * The request fails if this destination is deleted server side.
 *
 * @exception ConnectException  If the admin connection is closed or broken.
 * @exception AdminException  If the request fails.
 */
void XoramAdmin::setFreeReading(Destination* dest) {
  XoramAdmin::doRequest(new SetReader((char*) NULL, dest->getUID()));
}

/**
 * Admin method setting free writing access to this destination.
 * <p>
 * The request fails if this destination is deleted server side.
 *
 * @exception ConnectException  If the admin connection is closed or broken.
 * @exception AdminException  If the request fails.
 */
void XoramAdmin::setFreeWriting(Destination* dest) {
  XoramAdmin::doRequest(new SetWriter((char*) NULL, dest->getUID()));
}

/**
 * Admin method unsetting free reading access to this destination.
 * <p>
 * The request fails if this destination is deleted server side.
 *
 * @exception ConnectException  If the admin connection is closed or broken.
 * @exception AdminException  If the request fails.
 */
void XoramAdmin::unsetFreeReading(Destination* dest) { 
  XoramAdmin::doRequest(new UnsetReader((char*) NULL, dest->getUID()));
}

/**
 * Admin method unsetting free writing access to this destination.
 * <p>
 * The request fails if this destination is deleted server side.
 *
 * @exception ConnectException  If the admin connection is closed or broken.
 * @exception AdminException  If the request fails.
 */
void XoramAdmin::unsetFreeWriting(Destination* dest) {
  doRequest(new UnsetWriter((char*) NULL, dest->getUID()));
}


/**
 * Admin method creating a user for a given server and instanciating the
 * corresponding <code>User</code> object.
 * <p>
 * If the user has already been set on this server, the method simply
 * returns the corresponding <code>CreateUserReply</code> object. Its fails if the
 * target server does not belong to the platform, or if a proxy could not
 * be deployed server side for a new user. 
 *
 * @param name  Name of the user.
 * @param password  Password of the user.
 * @param serverId  The identifier of the user's server.
 */
CreateUserReply* XoramAdmin::createUser(char* userName, char* passwd, int serverId) {
  AdminReply* reply = XoramAdmin::doRequest(new CreateUserRequest(userName, passwd, serverId));
  return (CreateUserReply*) reply;
}


/**
 * Admin method creating a user for server 0 and instanciating the
 * corresponding <code>User</code> object.
 * <p>
 * If the user has already been set on this server, the method simply
 * returns the corresponding <code>CreateUserReply</code> object. Its fails if the
 * target server does not belong to the platform, or if a proxy could not
 * be deployed server side for a new user. 
 *
 * @param name  Name of the user.
 * @param password  Password of the user.
 */
CreateUserReply* XoramAdmin::createUser(char* userName, char* passwd) {
  return XoramAdmin::createUser(userName, passwd, 0);
}

/**
 * Removes this user.
 */
void XoramAdmin::deleteUser(char* userName, char* proxyId) {
  XoramAdmin::doRequest(new DeleteUser(userName, proxyId));
} 

CreateDestinationReply* XoramAdmin::doCreate(int serverId, 
                                             char* name,
                                             char* className, 
                                             Properties* prop, 
                                             byte type) {
  CreateDestinationRequest* cdr =
    new CreateDestinationRequest(serverId,
                                 name,
                                 className,
                                 prop,
                                 type);
  AdminReply* reply = XoramAdmin::doRequest(cdr);
  return (CreateDestinationReply*) reply;
}

Queue* XoramAdmin::createQueue(int serverId,
                               char* name,
                               char* className,
                               Properties* prop) {
  CreateDestinationReply* reply = doCreate(serverId, name, className, prop, QUEUE_TYPE);
  Queue* queue = new Queue(reply->getId(), reply->getName());
  return queue;
}

Queue* XoramAdmin::createQueue(int serverId,
                               char* className,
                               Properties* prop) {
  return XoramAdmin::createQueue(serverId, (char*) NULL, className, prop);
}

Queue* XoramAdmin::createQueue(int serverId, Properties* prop) { 
  return XoramAdmin::createQueue(serverId, "org.objectweb.joram.mom.dest.Queue", prop);
}

Queue* XoramAdmin::createQueue(int serverId, char* name) {
  return XoramAdmin::createQueue(serverId, 
                                 name, 
                                 "org.objectweb.joram.mom.dest.Queue", 
                                 (Properties*) NULL);
}

Queue* XoramAdmin::createQueue(char* name) {
  return XoramAdmin::createQueue(0, name);
}

Queue* XoramAdmin::createQueue(int serverId) {
  return XoramAdmin::createQueue(0, (char*) NULL);
}

Queue* XoramAdmin::createQueue() {
  return XoramAdmin::createQueue(0);
}

Topic* XoramAdmin::createTopic(int serverId,
                               char* name,
                               char* className,
                               Properties* prop) {
  CreateDestinationReply* reply = doCreate(serverId, name, className, prop, TOPIC_TYPE);
  Topic* topic = new Topic(reply->getId(), reply->getName());
  return topic;
}

Topic* XoramAdmin::createTopic(int serverId,
                               char* className,
                               Properties* prop) {
  return XoramAdmin::createTopic(serverId, (char*) NULL, className, prop);
}

Topic* XoramAdmin::createTopic(int serverId, Properties* prop) { 
  return XoramAdmin::createTopic(serverId, "org.objectweb.joram.mom.dest.Topic", prop);
}

Topic* XoramAdmin::createTopic(int serverId, char* name) {
  return XoramAdmin::createTopic(serverId, 
                                 name, 
                                 "org.objectweb.joram.mom.dest.Topic", 
                                 (Properties*) NULL);
}

Topic* XoramAdmin::createTopic(char* name) {
  return XoramAdmin::createTopic(0, name);
}

Topic* XoramAdmin::createTopic(int serverId) {
  return XoramAdmin::createTopic(0, (char*) NULL);
}

Topic* XoramAdmin::createTopic() {
  return XoramAdmin::createTopic(0);
}

/**
 * Admin method removing this destination from the platform.
 *
 */
void XoramAdmin::deleteDestination(char* id) {
  XoramAdmin::doRequest(new DeleteDestination(id));
}

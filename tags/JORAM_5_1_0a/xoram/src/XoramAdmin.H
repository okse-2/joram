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
#ifndef XORAM_ADMIN_H
#define XORAM_ADMIN_H

#include "Types.H"
#include "Properties.H"
//#include "Vector.H"
//#include "XStream.H"
//#include "AbstractMessage.H"
#include "AbstractAdminMessage.H"
//#include "XoramException.H"
#include "Xoram.H"

class AdminRequestor {
 private:
  Connection* cnx;// = (Connection*) NULL;
  Session* sess;// = (Session*) NULL;
  Topic* topic;// = (Topic*) NULL;
  TemporaryTopic* tmpTopic;// = (TemporaryTopic*) NULL;
  MessageProducer* producer;// = (MessageProducer*) NULL;
  MessageConsumer* consumer;// = (MessageConsumer*) NULL;

 public:
    AdminRequestor(Connection* cnx);
    ~AdminRequestor();

    Message* request(AdminRequest* request, long timeout);

    void close();
};

class XoramAdmin {
 public:
  //const static long DEFAULT_REQUEST_TIMEOUT = 120000;
  Connection* cnx;// = (Connection*) NULL;
  AdminRequestor* requestor;

  XoramAdmin();
  ~XoramAdmin();

  void connect(TCPConnectionFactory* cnxFact,
               char* name,
               char* password);

  void connect(char* hostName,
               int port,
               char* name,
               char* password,
               int cnxTimer,
               char* reliableClass);
     
  void connect(char* hostName,
               int port,
               char* name,
               char* password,
               int cnxTimer);

  void connect(char* name, char* password, int cnxTimer);

  void disconnect();

  //void setDefaultDMQ(int serverId, DeadMQueue* dmq);
  //void setDefaultDMQ(DeadMQueue* dmq);

  AdminReply* doRequest(AdminRequest* request, long timeout);
  AdminReply* doRequest(AdminRequest* request);


  void setFreeReading(Destination* dest);
  void setFreeWriting(Destination* dest);
  void unsetFreeReading(Destination* dest);
  void unsetFreeWriting(Destination* dest);

  CreateUserReply* createUser(char* userName, char* passwd, int serverId);
  CreateUserReply* XoramAdmin::createUser(char* userName, char* passwd);
  void deleteUser(char* userName, char* proxyId);

  Queue* createQueue(int serverId,
                     char* name,
                     char* className,
                     Properties* prop);
  Queue* createQueue(int serverId,
                     char* className,
                     Properties* prop);
  Queue* createQueue(int serverId, Properties* prop);
  Queue* createQueue(int serverId, char* name);
  Queue* createQueue(char* name);
  Queue* createQueue(int serverId);
  Queue* createQueue();
   
  Topic* createTopic(int serverId,
                     char* name,
                     char* className,
                     Properties* prop);
  Topic* createTopic(int serverId,
                     char* className,
                     Properties* prop);
  Topic* createTopic(int serverId, Properties* prop);
  Topic* createTopic(int serverId, char* name);
  Topic* createTopic(char* name);
  Topic* createTopic(int serverId);
  Topic* createTopic();

  CreateDestinationReply* doCreate(int serverId, 
                                   char* name,
                                   char* className, 
                                   Properties* prop, 
                                   char type);

  void deleteDestination(char* id);
};

#endif /* XORAM_ADMIN_H */

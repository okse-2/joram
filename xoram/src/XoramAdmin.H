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
#include "AbstractAdminMessage.H"
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

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  CreateUserReply* createUser(char* userName, char* passwd, int serverId);
  CreateUserReply* XoramAdmin::createUser(char* userName, char* passwd);
  void deleteUser(char* userName, char* proxyId);

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception AdminException   If the creation fails.
   */
  Queue* createQueue(int serverId,
                     char* name,
                     char* className,
                     Properties* prop);
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception AdminException   If the creation fails.
   */
  Queue* createQueue(int serverId,
                     char* className,
                     Properties* prop);
  Queue* createQueue(int serverId, Properties* prop);
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  Queue* createQueue(int serverId, char* name);
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  Queue* createQueue(char* name);
  Queue* createQueue(int serverId);
  Queue* createQueue();

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception AdminException   If the creation fails.
   */
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

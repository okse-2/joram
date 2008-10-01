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
#ifndef ABSTRACT_ADMIN_MESSAGE_H
#define ABSTRACT_ADMIN_MESSAGE_H

#include "Types.H"
#include "Vector.H"
#include "XStream.H"
#include "Message.H"

class AdminRequest;
class AdminReply;

//class UnknownClass {};

class AbstractAdminMessage : Streamable {
  // protected:
 public:
  int classid;
  AbstractAdminMessage();
  ~AbstractAdminMessage();

  enum {
    NULL_CLASS_ID = -1,
    ABSTRACT_ADMIN_MESSAGE = 0,
    ADD_DOMAIN_REQUEST = 1,
    ADD_QUEUE_CLUSTER = 2,
    ADD_SERVER_REQUEST = 3,
    ADD_SERVICE_REQUEST = 4,
    ADMIN_REPLY = 5,
    ADMIN_REQUEST = 6,
    CLEAR_QUEUE = 7,
    CLEAR_SUBSCRIPTION = 8,
    CREATE_DESTINATION_REPLY = 9,
    CREATE_DESTINATION_REQUEST = 10,
    CREATE_USER_REPLY = 11,
    CREATE_USER_REQUEST = 12,
    DELETE_DESTINATION = 13,
    DELETE_QUEUE_MESSAGE = 14,
    DELETE_SUBSCRIPTION_MESSAGE = 15,
    DELETE_USER = 16,
    GET_CONFIG_REQUEST = 17,
    GET_DOMAIN_NAMES = 18,
    GET_DOMAIN_NAMES_REP = 19,
    GET_LOCAL_SERVER = 20,
    GET_LOCAL_SERVER_REP = 21,
    GET_QUEUE_MESSAGE = 22,
    GET_QUEUE_MESSAGE_IDS = 23,
    GET_QUEUE_MESSAGE_IDS_REP = 24,
    GET_QUEUE_MESSAGE_REP = 25,
    GET_SUBSCRIBER_IDS = 26,
    GET_SUBSCRIBER_IDS_REP = 27,
    GET_SUBSCRIPTION = 28,
    GET_SUBSCRIPTION_MESSAGE = 29,
    GET_SUBSCRIPTION_MESSAGE_IDS = 30,
    GET_SUBSCRIPTION_MESSAGE_IDS_REP = 31,
    GET_SUBSCRIPTION_MESSAGE_REP = 32,
    GET_SUBSCRIPTION_REP = 33,
    GET_SUBSCRIPTIONS = 34,
    GET_SUBSCRIPTIONS_REP = 35,
    LIST_CLUSTER_QUEUE = 36,
    /*    MONITOR_GET_CLUSTER = 37,
    MONITOR_GET_CLUSTER_REP = 38,
    MONITOR_GET_DMQ_SETTINGS = 39,
    MONITOR_GET_DMQ_SETTINGS_REP = 40,
    MONITOR_GET_DESTINATIONS = 41,
    MONITOR_GET_DESTINATIONS_REP = 42,
    MONITOR_GET_FATHER = 43,
    MONITOR_GET_FATHER_REP = 44,
    MONITOR_GET_FREE_ACCESS = 45,
    MONITOR_GET_FREE_ACCESS_REP = 46,
    MONITOR_GET_NB_MAX_MSG = 47,
    MONITOR_GET_NB_MAX_MSG_REP = 48,
    MONITOR_GET_NUMBER_REP = 49,
    MONITOR_GET_PENDING_MESSAGES = 50,
    MONITOR_GET_PENDING_REQUESTS = 51,
    MONITOR_GET_READERS = 52,
    MONITOR_GET_SERVERS_IDS = 53,
    MONITOR_GET_SERVERS_IDS_REP = 54,
    MONITOR_GET_STAT = 55,
    MONITOR_GET_STAT_REP = 56,
    MONITOR_GET_SUBSCRIPTIONS = 57,
    MONITOR_GET_USERS = 58,
    MONITOR_GET_USERS_REP = 59,
    MONITOR_GET_WRITERS = 60,
    MONITOR_REPLY = 61,
    MONITOR_REQUEST = 62,*/
    QUEUE_ADMIN_REQUEST = 63,
    REMOVE_DOMAIN_REQUEST = 64,
    REMOVE_QUEUE_CLUSTER = 65,
    REMOVE_SERVER_REQUEST = 66,
    REMOVE_SERVICE_REQUEST = 67,
    SET_CLUSTER = 68,
    SET_DEFAULT_DMQ = 69,
    SET_DEFAULT_THRESHOLD = 70,
    SET_DESTINATION_DMQ = 71,
    SET_FATHER = 72,
    SET_NB_MAX_MSG = 73,
    SET_QUEUE_THRESHOLD = 74,
    SET_READER = 75,
    SET_RIGHT = 76,
    SET_USER_DMQ = 77,
    SET_USER_THRESHOLD = 78,
    SET_WRITER = 79,
    SPECIAL_ADMIN = 80,
    STOP_SERVER_REQUEST = 81,
    SUBSCRIPTION_ADMIN_REQUEST = 82,
    UNSET_CLUSTER = 83,
    UNSET_DEFAULT_DMQ = 84,
    UNSET_DEFAULT_THRESHOLD = 85,
    UNSET_DESTINATION_DMQ = 86,
    UNSET_FATHER = 87,
    UNSET_QUEUE_THRESHOLD = 88,
    UNSET_READER = 89,
    UNSET_USER_DMQ = 90,
    UNSET_USER_THRESHOLD = 91,
    UNSET_WRITER = 92,
    UPDATE_USER = 93,
    USER_ADMIN_REQUEST = 94
  };

  boolean instanceof(int classid);
  int getClassId();

  // ==================================================
  // Streamable interface
  // ==================================================

  static void write(AbstractAdminMessage* msg,
                    OutputStream* os) throw (IOException);
  static AbstractAdminMessage* read(InputStream *is) throw (IOException);
};

class AdminRequest : public AbstractAdminMessage {
 public:
  AdminRequest();
  ~AdminRequest();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class AdminReply : public AbstractAdminMessage {
 protected:
  /** true if this reply replies to a successful request. */
  boolean success;

  /** Information. */
  char* info;

  /** Object. */
  //Object replyObj;

  int errorCode;

 public:
  const static int NAME_ALREADY_USED = 0;
  const static int START_FAILURE = 1;
  const static int SERVER_ID_ALREADY_USED = 2;
  const static int UNKNOWN_SERVER = 3;

  AdminReply();

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a
   *                 successful request.
   * @param info  Information to carry.
   */
  AdminReply(boolean success, 
             char* info);

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param errorCode error code defining the type of the error
   * @param info  Information to carry.
   */
  AdminReply(boolean success, 
             int errorCode,
             char* info);

  ~AdminReply();

  /**
   * Returns <code>true</code> if this reply replies to a successful request.
   */
  boolean succeeded() {
    return success;
  }

  /** Returns the carried info. */
  char* getInfo() {
    return info;
  }

  int getErrorCode() {
    return errorCode;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class CreateDestinationRequest : public AdminRequest {
 private:
  /** Id of the server where deploying the destination. */
  int serverId;

  /** Name attributed to the destination. */
  char* name;

  /** Name of the class to be instanciated. */
  char* className;
 
  /** Properties needed to create destination object. */
  Properties* props;

  char* expectedType;

 public:
  CreateDestinationRequest();

  CreateDestinationRequest(int serverId,
                           char* name,
                           char* className,
                           Properties* props,
                           char* expectedType);

  ~CreateDestinationRequest();

  /** Returns the id of the server where deploying the destination. */
  int getServerId() {
    return serverId;
  }

  /** Returns the name attributed to the destination. */
  char* getDestinationName() {
    return name;
  }

  /** Returns the class name of destination (queue, topic, ...). */
  char* getClassName() {
    return className;
  }

  /** Returns the destination properties. */
  Properties* getProperties() {
    return props;
  }

  char* getExpectedType() {
    return expectedType;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class CreateDestinationReply : public AdminReply {
 private:
  /** Identifier of the created destination. */
  char* id;
  char* name;
  char* type;

 public:
  CreateDestinationReply();
  CreateDestinationReply(char* id, 
                         char* name,
                         char* type,
                         char* info);
  ~CreateDestinationReply();

  /** Returns the id of the created queue. */
  char* getId() {
    return id;
  }

  char* getName() {
    return name;
  }

  char* getType() {
    return type;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};


class DeleteDestination : public AdminRequest {
 private:
  char* id;

 public:
  DeleteDestination();
  DeleteDestination(char* id);
  ~DeleteDestination();

  char* getId() {
    return id;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class CreateUserRequest : public AdminRequest {
 private:
  /** Id of the server where deploying the proxy. */
  int serverId;

  /** Name of the user. */
  char* userName;

  /** Password of the user. */
  char* userPass;

 public:
  CreateUserRequest();
  
  CreateUserRequest(char* userName,
                    char* userPass,
                    int serverId);
  
  ~CreateUserRequest();

  /** Returns the id of the server where deploying its proxy. */
  int getServerId() {
    return serverId;
  }

  /** Returns the name of the user to create. */
  char* getUserName() {
    return userName;
  }

  /** Returns the password of the user. */
  char* getUserPass() {
    return userPass;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class CreateUserReply : public AdminReply {
 private:
  /** Identifier of the user's proxy. */
  char* id;

 public:
  CreateUserReply();
  CreateUserReply(char* id, 
                  char* info);
  ~CreateUserReply();

  /** Returns the id of the user's proxy. */
  char* getProxId() {
    return id;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class DeleteUser : public AdminRequest {
 private:
  char* userName;
  char* proxyId;

 public:
  DeleteUser();
  DeleteUser(char* userName, char* proxyId);
  ~DeleteUser();

  char* getUserName() {
    return userName;
  }

  char* getProxyId() {
    return proxyId;
  }
  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class SetDefaultDMQ : public AdminRequest {
 private:
  /** Identifier of the server the DMQ is set for. */
  int serverId;
  /** Identifier of the DMQ. */
  char* dmqId;

 public:
  SetDefaultDMQ();
  SetDefaultDMQ(int serverId, char* dmqId);
  ~SetDefaultDMQ();

  /** Returns the identifier of the server the DMQ is set for. */
  int getServerId() {
    return serverId;
  }

  /** Returns the identifier of the DMQ. */
  char* getDmqId() {
    return dmqId;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class SetDestinationDMQ : public AdminRequest {
  private:
  /** Identifier of the destination the DMQ is set for. */
  char* destId;
  /** Identifier of the DMQ. */
  char* dmqId;

 public:
  /**
   * Constructs a <code>SetDestinationDMQ</code> instance.
   *
   * @param destId  Identifier of the destination the DMQ is set for.
   * @param dmqId  Identifier of the DMQ.
   */
  SetDestinationDMQ(char* destId, char* dmqId);
  SetDestinationDMQ();
  ~SetDestinationDMQ();
  
  /** Returns the identifier of the destination the DMQ is set for. */
  char* getDestId() {
    return destId;
  }

  /** Returns the identifier of the DMQ. */
  char* getDmqId() {
    return dmqId;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class SetRight : public AdminRequest {
 private:
  /** Identifier of the user's proxy, <code>null</code> for all users. */
  char* userProxId;
  /** Identifier of the destination. */
  char* destId;

 public:
  /**
   * Constructs a <code>SetRight</code> instance.
   *
   * @param userProxId  The identifier of the user's proxy, <code>null</code>
   *          for all users.
   * @param destId  The identifier of the destination.
   */
  SetRight(char* userProxId, char* destId);
  SetRight();
  ~SetRight();
  
  /** Returns the identifier of the future reader's proxy. */
 char* getUserProxId() {
    return userProxId;
  }
  
  /** Returns the identifier of the destination. */
  char* getDestId() {
    return destId;
  }

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class SetReader : public SetRight {
  public:
  /**
   * Constructs a <code>SetReader</code> instance.
   *
   * @param userProxId  See superclass.
   * @param destId  See superclass.
   */
  SetReader(char* userProxId, char* destId);
  SetReader();
  ~SetReader();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class SetWriter : public SetRight {
  public:
  /**
   * Constructs a <code>SetWriter</code> instance.
   *
   * @param userProxId  See superclass.
   * @param destId  See superclass.
   */
  SetWriter(char* userProxId, char* destId);
  SetWriter();
  ~SetWriter();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class UnsetReader : public SetRight {
  public:
  /**
   * Constructs a <code>UnsetReader</code> instance.
   *
   * @param userProxId  See superclass.
   * @param destId  See superclass.
   */
  UnsetReader(char* userProxId, char* destId);
  UnsetReader();
  ~UnsetReader();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class UnsetWriter : public SetRight {
  public:
  /**
   * Constructs a <code>UnsetWriter</code> instance.
   *
   * @param userProxId  See superclass.
   * @param destId  See superclass.
   */
  UnsetWriter(char* userProxId, char* destId);
  UnsetWriter();
  ~UnsetWriter();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream *os) throw (IOException);
  virtual void readFrom(InputStream *is) throw (IOException);
};

class AdminMessage : public Message {
 public:
  AdminMessage();
  ~AdminMessage();

  void setAdminMessage(AbstractAdminMessage* adminMsg);
  AbstractAdminMessage* getAdminMessage();
};

#endif /* ABSTRACT_ADMIN_MESSAGE_H */

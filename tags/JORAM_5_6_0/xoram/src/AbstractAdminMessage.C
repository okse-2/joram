/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2009 ScalAgent Distributed Technologies
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
#include "AbstractAdminMessage.H"
#include "Message.H"

// ######################################################################
// AbstractAdminMessage Class
// ######################################################################

AbstractAdminMessage::AbstractAdminMessage() {
  classid = ABSTRACT_ADMIN_MESSAGE;
}

AbstractAdminMessage::~AbstractAdminMessage() {
}

int AbstractAdminMessage::getClassId() {
  return classid;
}

boolean AbstractAdminMessage::instanceof(int classid) {
  return (this->classid == classid);
}

void AbstractAdminMessage::write(AbstractAdminMessage* msg,
                                   OutputStream* os) throw (IOException) {
  if (msg == (AbstractAdminMessage*) NULL) {
    if (os->writeInt(NULL_CLASS_ID) == -1) throw IOException();
  } else {
    if (os->writeInt(msg->getClassId()) == -1) throw IOException();
    msg->writeTo(os);
  }
}

AbstractAdminMessage* AbstractAdminMessage::read(InputStream *is) throw (IOException) {
  int classid;
  AbstractAdminMessage* reply = NULL;

  if (is->readInt(&classid) == -1) throw IOException();
  switch(classid) {
  case NULL_CLASS_ID:
    return (AbstractAdminMessage*) NULL;
    /*
   case ABSTRACT_ADMIN_MESSAGE:
     reply = new AbstractAdminMessage();
    break;
    */
    //ADD_DOMAIN_REQUEST = 1,
    //ADD_QUEUE_CLUSTER = 2,
    //ADD_SERVER_REQUEST = 3,
    //ADD_SERVICE_REQUEST = 4,
  case ADMIN_REPLY:
    reply = new AdminReply();
    break;
  case ADMIN_REQUEST:
    reply = new AdminRequest();
    break;
      //CLEAR_QUEUE = 7,
      //CLEAR_SUBSCRIPTION = 8,
  case CREATE_DESTINATION_REPLY:
    reply = new CreateDestinationReply();
    break;
  case CREATE_DESTINATION_REQUEST:
    reply = new CreateDestinationRequest();
    break;
  case CREATE_USER_REPLY:
    reply = new CreateUserReply();
    break;
  case CREATE_USER_REQUEST:
    reply = new CreateUserRequest();
    break;
  case DELETE_DESTINATION:
    reply = new DeleteDestination();
    break;
      //DELETE_QUEUE_MESSAGE = 14,
      //DELETE_SUBSCRIPTION_MESSAGE = 15,
   case DELETE_USER:
    reply = new DeleteUser();
    break;

/*      GET_CONFIG_REQUEST = 17, */
/*      GET_DOMAIN_NAMES = 18, */
/*      GET_DOMAIN_NAMES_REP = 19, */
/*      GET_LOCAL_SERVER = 20, */
/*      GET_LOCAL_SERVER_REP = 21, */
/*      GET_QUEUE_MESSAGE = 22, */
/*      GET_QUEUE_MESSAGE_IDS = 23, */
/*      GET_QUEUE_MESSAGE_IDS_REP = 24, */
/*      GET_QUEUE_MESSAGE_REP = 25, */
/*      GET_SUBSCRIBER_IDS = 26, */
/*      GET_SUBSCRIBER_IDS_REP = 27, */
/*      GET_SUBSCRIPTION = 28, */
/*      GET_SUBSCRIPTION_MESSAGE = 29, */
/*      GET_SUBSCRIPTION_MESSAGE_IDS = 30, */
/*      GET_SUBSCRIPTION_MESSAGE_IDS_REP = 31, */
/*      GET_SUBSCRIPTION_MESSAGE_REP = 32, */
/*      GET_SUBSCRIPTION_REP = 33, */
/*      GET_SUBSCRIPTIONS = 34, */
/*      GET_SUBSCRIPTIONS_REP = 35, */
/*      LIST_CLUSTER_QUEUE = 36, */
/*      MONITOR_GET_CLUSTER = 37, */
/*      MONITOR_GET_CLUSTER_REP = 38, */
/*      MONITOR_GET_DMQ_SETTINGS = 39, */
/*      MONITOR_GET_DMQ_SETTINGS_REP = 40, */
/*      MONITOR_GET_DESTINATIONS = 41, */
/*      MONITOR_GET_DESTINATIONS_REP = 42, */
/*      MONITOR_GET_FATHER = 43, */
/*      MONITOR_GET_FATHER_REP = 44, */
/*      MONITOR_GET_FREE_ACCESS = 45, */
/*      MONITOR_GET_FREE_ACCESS_REP = 46, */
/*      MONITOR_GET_NB_MAX_MSG = 47, */
/*      MONITOR_GET_NB_MAX_MSG_REP = 48, */
/*      MONITOR_GET_NUMBER_REP = 49, */
/*      MONITOR_GET_PENDING_MESSAGES = 50, */
/*      MONITOR_GET_PENDING_REQUESTS = 51, */
/*      MONITOR_GET_READERS = 52, */
/*      MONITOR_GET_SERVERS_IDS = 53, */
/*      MONITOR_GET_SERVERS_IDS_REP = 54, */
/*      MONITOR_GET_STAT = 55, */
/*      MONITOR_GET_STAT_REP = 56, */
/*      MONITOR_GET_SUBSCRIPTIONS = 57, */
/*      MONITOR_GET_USERS = 58, */
/*      MONITOR_GET_USERS_REP = 59, */
/*      MONITOR_GET_WRITERS = 60, */
/*      MONITOR_REPLY = 61, */
/*      MONITOR_REQUEST = 62, */
/*      QUEUE_ADMIN_REQUEST = 63, */
/*      REMOVE_DOMAIN_REQUEST = 64, */
/*      REMOVE_QUEUE_CLUSTER = 65, */
/*      REMOVE_SERVER_REQUEST = 66, */
/*      REMOVE_SERVICE_REQUEST = 67, */
/*      SET_CLUSTER = 68, */
  case SET_DEFAULT_DMQ:
    reply = new SetDefaultDMQ();
    break;
      //SET_DEFAULT_THRESHOLD = 70,
  case SET_DESTINATION_DMQ:
    reply = new SetDestinationDMQ();
    break;
      //SET_FATHER = 72,
      //SET_NB_MAX_MSG = 73,
      //SET_QUEUE_THRESHOLD = 74,
  case SET_READER:
    reply = new SetReader();
    break;
  case SET_RIGHT:
    reply = new SetRight();
    break;
      //SET_USER_DMQ = 77,
      //SET_USER_THRESHOLD = 78,
  case SET_WRITER:
    reply = new SetWriter();
    break;
      //SPECIAL_ADMIN = 80,
      //STOP_SERVER_REQUEST = 81,
      //SUBSCRIPTION_ADMIN_REQUEST = 82,
/*      UNSET_CLUSTER = 83, */
/*      UNSET_DEFAULT_DMQ = 84, */
/*      UNSET_DEFAULT_THRESHOLD = 85, */
/*      UNSET_DESTINATION_DMQ = 86, */
/*      UNSET_FATHER = 87, */
/*      UNSET_QUEUE_THRESHOLD = 88, */
  case UNSET_READER:
    reply = new UnsetReader();
    break;
/*      UNSET_USER_DMQ = 90, */
/*      UNSET_USER_THRESHOLD = 91, */
  case UNSET_WRITER:
    reply = new UnsetWriter();
    break;
/*      UPDATE_USER = 93, */
/*      USER_ADMIN_REQUEST = 94 */

  default:
    throw UnknownClass();
  }
  reply->readFrom(is);

  return reply;
}

// ######################################################################
// AdminRequest Class
// ######################################################################

AdminRequest::AdminRequest() : AbstractAdminMessage() {
  classid = ADMIN_REQUEST;
  if(DEBUG)
    printf("<=> AdminRequest:\n");
}

AdminRequest::~AdminRequest() {
  if(DEBUG)
    printf("~AdminRequest:\n");
}

// ====================
// Streamable interface
// ====================
void AdminRequest::writeTo(OutputStream *os) throw (IOException) {
}

void AdminRequest::readFrom(InputStream *is) throw (IOException) {
}

// ######################################################################
// AdminReply Class
// ######################################################################

AdminReply::AdminReply() : AbstractAdminMessage() {
  classid = ADMIN_REPLY;
  success = TRUE;
  info = '\0';
  errorCode = -1;
}

AdminReply::AdminReply(boolean success, char* info) : AbstractAdminMessage() {
  classid = ADMIN_REPLY;
  this->success = success;
  this->info = info;
  errorCode = -1;
}

AdminReply::AdminReply(boolean success, int errorCode, char* info) : AbstractAdminMessage() {
  classid = ADMIN_REPLY;
  this->success = success;
  this->info = info;
  this->errorCode = errorCode;
}

AdminReply::~AdminReply() {
  //TODO
}

// ====================
// Streamable interface
// ====================
void AdminReply::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeBoolean(success) == -1) throw IOException();
  if (os->writeString(info) == -1) throw IOException();
  if (os->writeInt(errorCode) == -1) throw IOException();
}

void AdminReply::readFrom(InputStream *is) throw (IOException) {
  if (is->readBoolean(&success) == -1) throw IOException();
  if (is->readString(&info) == -1) throw IOException();
  if (is->readInt(&errorCode) == -1) throw IOException();
}

// ######################################################################
// CreateDestinationRequest Class
// ######################################################################
CreateDestinationRequest::CreateDestinationRequest() : AdminRequest() {
  classid = CREATE_DESTINATION_REQUEST;
}

CreateDestinationRequest::CreateDestinationRequest(int serverId,
                                                   char* name,
                                                   char* className,
                                                   Properties* props,
                                                   byte expectedType) : AdminRequest() {
  classid = CREATE_DESTINATION_REQUEST;
  this->serverId = serverId;
  this->name = name;
  this->className = className;
  this->props = props;
  this->expectedType = expectedType;
}

CreateDestinationRequest::~CreateDestinationRequest() {
  //TODO
}

// ====================
// Streamable interface
// ====================
void CreateDestinationRequest::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeInt(serverId) == -1)  throw IOException();
  if (os->writeString(name) == -1)  throw IOException();
  if (os->writeString(className) == -1)  throw IOException();
  os->writeProperties(props);
  if (os->writeByte(expectedType) == -1)  throw IOException();
}

void CreateDestinationRequest::readFrom(InputStream *is) throw (IOException) {
  if (is->readInt(&serverId) == -1)  throw IOException();
  if (is->readString(&name) == -1)  throw IOException();
  if (is->readString(&className) == -1)  throw IOException();
  props = is->readProperties();
  if (is->readByte(&expectedType) == -1)  throw IOException();
}

// ######################################################################
// CreateDestinationReply Class
// ######################################################################

CreateDestinationReply::CreateDestinationReply() : AdminReply() {
  classid = CREATE_DESTINATION_REPLY;
}

CreateDestinationReply::CreateDestinationReply(char* id,
                                               char* name,
                                               byte type,
                                               char* info) : AdminReply(TRUE, info) {
  classid = CREATE_DESTINATION_REPLY;
  this->id = id;
  this->name = name;
  this->type = type;
}

CreateDestinationReply::~CreateDestinationReply() {
  if(DEBUG)
    printf("~CreateDestinationReply()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void CreateDestinationReply::writeTo(OutputStream *os) throw (IOException) {
  AdminReply::writeTo(os);
  if (os->writeString(id) == -1)  throw IOException();
  if (os->writeString(name) == -1)  throw IOException();
  //if (os->writeByte(type) == -1)  throw IOException();
}

void CreateDestinationReply::readFrom(InputStream *is) throw (IOException) {
  AdminReply::readFrom(is);
  if (is->readString(&id) == -1)  throw IOException();
  if (is->readString(&name) == -1)  throw IOException();
  //if (is->readByte(&type) == -1)  throw IOException();
}

// ######################################################################
// DeleteDestination Class
// ######################################################################

DeleteDestination::DeleteDestination() : AdminRequest() {
  classid = DELETE_DESTINATION;
  id = '\0';
}

DeleteDestination::DeleteDestination(char* id) : AdminRequest() {
  classid = DELETE_DESTINATION;
  this->id = id;
}

DeleteDestination::~DeleteDestination() {
  if(DEBUG)
    printf("~DeleteDestination()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void DeleteDestination::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeString(id) == -1)  throw IOException();
}

void DeleteDestination::readFrom(InputStream *is) throw (IOException) {
  if (is->readString(&id) == -1)  throw IOException();
}

// ######################################################################
// CreateUserRequest Class
// ######################################################################
CreateUserRequest::CreateUserRequest() : AdminRequest() {
  classid = CREATE_USER_REQUEST;
}

CreateUserRequest::CreateUserRequest(char* userName,
                                     char* userPass,
                                     int serverId) : AdminRequest() {
  classid = CREATE_USER_REQUEST;
  this->serverId = serverId;
  this->userName = userName;
  this->userPass = userPass;
}

CreateUserRequest::~CreateUserRequest() {
  //TODO
}

// ====================
// Streamable interface
// ====================
void CreateUserRequest::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeInt(serverId) == -1) throw IOException();
  // Always default identity class: "org.objectweb.joram.shared.security.SimpleIdentity".
  if (os->writeString("") == -1) throw IOException();
  if (os->writeString(userName) == -1) throw IOException();
  if (os->writeString(userPass) == -1) throw IOException();
}

void CreateUserRequest::readFrom(InputStream *is) throw (IOException) {
  char* identityClass;
  if (is->readInt(&serverId) == -1) throw IOException();
  if (is->readString(&identityClass) == -1) throw IOException();
  if ((strlen(identityClass) != 0) &&
	  (strcmp(identityClass, "org.objectweb.joram.shared.security.SimpleIdentity") != 0)) throw IOException();
  if (is->readString(&userName) == -1) throw IOException();
  if (is->readString(&userPass) == -1) throw IOException();
}

// ######################################################################
// CreateUserReply Class
// ######################################################################

CreateUserReply::CreateUserReply() : AdminReply() {
  classid = CREATE_USER_REPLY;
}

CreateUserReply::CreateUserReply(char* id,
                                 char* info) : AdminReply(TRUE, info) {
  classid = CREATE_USER_REPLY;
  this->id = id;
}

CreateUserReply::~CreateUserReply() {
  if(DEBUG)
    printf("~CreateUserReply()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void CreateUserReply::writeTo(OutputStream *os) throw (IOException) {
  AdminReply::writeTo(os);
  if (os->writeString(id) == -1)  throw IOException();
}

void CreateUserReply::readFrom(InputStream *is) throw (IOException) {
  AdminReply::readFrom(is);
  if (is->readString(&id) == -1)  throw IOException();
}

// ######################################################################
// DeleteUser Class
// ######################################################################

DeleteUser::DeleteUser() : AdminRequest() {
  classid = DELETE_USER;
  userName = '\0';
  proxyId = '\0';
}

DeleteUser::DeleteUser(char* userName, char* proxyId) : AdminRequest() {
  classid = DELETE_USER;
  this->userName = userName;
  this->proxyId = proxyId;
}

DeleteUser::~DeleteUser() {
  if(DEBUG)
    printf("~DeleteUser()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void DeleteUser::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeString(userName) == -1)  throw IOException();
  if (os->writeString(proxyId) == -1)  throw IOException();
}

void DeleteUser::readFrom(InputStream *is) throw (IOException) {
  if (is->readString(&userName) == -1)  throw IOException();
  if (is->readString(&proxyId) == -1)  throw IOException();
}

// ######################################################################
// SetDefaultDMQ Class
// ######################################################################

SetDefaultDMQ::SetDefaultDMQ() : AdminRequest() {
  classid = SET_DEFAULT_DMQ;
}

SetDefaultDMQ::SetDefaultDMQ(int serverId, char* dmqId) : AdminRequest() {
  classid = SET_DEFAULT_DMQ;
  this->serverId = serverId;
  this->dmqId = dmqId;
}

SetDefaultDMQ::~SetDefaultDMQ() {
  if(DEBUG)
    printf("~SetDefaultDMQ()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void SetDefaultDMQ::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeInt(serverId) == -1)  throw IOException();
  if (os->writeString(dmqId) == -1)  throw IOException();
}

void SetDefaultDMQ::readFrom(InputStream *is) throw (IOException) {
  if (is->readInt(&serverId) == -1)  throw IOException();
  if (is->readString(&dmqId) == -1)  throw IOException();
}

// ######################################################################
// SetDestinationDMQ Class
// ######################################################################

SetDestinationDMQ::SetDestinationDMQ() : AdminRequest() {
  classid = SET_DESTINATION_DMQ;
}

SetDestinationDMQ::SetDestinationDMQ(char* destId, char* dmqId) : AdminRequest() {
  classid = SET_DESTINATION_DMQ;
  this->destId = destId;
  this->dmqId = dmqId;
}

SetDestinationDMQ::~SetDestinationDMQ() {
  if(DEBUG)
    printf("~SetDestinationDMQ()\n");
  //TODO
}

// ====================
// Streamable interface
// ====================

void SetDestinationDMQ::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeString(destId) == -1)  throw IOException();
  if (os->writeString(dmqId) == -1)  throw IOException();
}

void SetDestinationDMQ::readFrom(InputStream *is) throw (IOException) {
  if (is->readString(&destId) == -1)  throw IOException();
  if (is->readString(&dmqId) == -1)  throw IOException();
}

// ######################################################################
// SetRight Class
// ######################################################################

/**
 * Constructs a <code>AdminRequest</code> instance.
 */
SetRight::SetRight() : AdminRequest() {
  classid = SET_RIGHT;
}

SetRight::SetRight(char* userProxId, char* destId) : AdminRequest() {
  classid = SET_RIGHT;
  this->userProxId = userProxId;
  this->destId = destId;
}

SetRight::~SetRight() {
  if(DEBUG)
    printf("~SetRight()\n");
  //TODO
}


// ====================
// Streamable interface
// ====================

void SetRight::writeTo(OutputStream *os) throw (IOException) {
  if (os->writeString(userProxId) == -1)  throw IOException();
  if (os->writeString(destId) == -1)  throw IOException();
}

void SetRight::readFrom(InputStream *is) throw (IOException) {
  if (is->readString(&userProxId) == -1)  throw IOException();
  if (is->readString(&destId) == -1)  throw IOException();
}

// ######################################################################
// SetReader Class
// ######################################################################

/**
 * Constructs a <code>SetReader</code> instance.
 */
SetReader::SetReader() : SetRight() {
  classid = SET_READER;
}

/**
 * Constructs a <code>SetReader</code> instance.
 */
SetReader::SetReader(char* userProxId, char* destId) : SetRight(userProxId, destId) {
  classid = SET_READER;
}

SetReader::~SetReader() {
  //TODO
}

// ====================
// Streamable interface
// ====================

void SetReader::writeTo(OutputStream *os) throw (IOException) {
  SetRight::writeTo(os);
}

void SetReader::readFrom(InputStream *is) throw (IOException) {
  SetRight::readFrom(is);
}

// ######################################################################
// SetWriter Class
// ######################################################################

/**
 * Constructs a <code>SetWriter</code> instance.
 */
SetWriter::SetWriter() : SetRight() {
  classid = SET_WRITER;
}

/**
 * Constructs a <code>SetWriter</code> instance.
 */
SetWriter::SetWriter(char* userProxId, char* destId) : SetRight(userProxId, destId) {
  classid = SET_WRITER;
}

SetWriter::~SetWriter() {
  //TODO
}

// ====================
// Streamable interface
// ====================

void SetWriter::writeTo(OutputStream *os) throw (IOException) {
  SetRight::writeTo(os);
}

void SetWriter::readFrom(InputStream *is) throw (IOException) {
  SetRight::readFrom(is);
}

// ######################################################################
// UnsetReader Class
// ######################################################################

/**
 * Constructs a <code>UnsetReader</code> instance.
 */
UnsetReader::UnsetReader() : SetRight() {
  classid = UNSET_READER;
}

/**
 * Constructs a <code>UnsetReader</code> instance.
 */
UnsetReader::UnsetReader(char* userProxId, char* destId) : SetRight(userProxId, destId) {
  classid = UNSET_READER;
}

UnsetReader::~UnsetReader() {
  //TODO
}

// ====================
// Streamable interface
// ====================

void UnsetReader::writeTo(OutputStream *os) throw (IOException) {
  SetRight::writeTo(os);
}

void UnsetReader::readFrom(InputStream *is) throw (IOException) {
  SetRight::readFrom(is);
}

// ######################################################################
// UnsetWriter Class
// ######################################################################

/**
 * Constructs a <code>UnsetWriter</code> instance.
 */
UnsetWriter::UnsetWriter() : SetRight() {
  classid = UNSET_WRITER;
}

/**
 * Constructs a <code>UnsetWriter</code> instance.
 */
UnsetWriter::UnsetWriter(char* userProxId, char* destId) : SetRight(userProxId, destId) {
  classid = UNSET_WRITER;
}

UnsetWriter::~UnsetWriter() {
  //TODO
}

// ====================
// Streamable interface
// ====================

void UnsetWriter::writeTo(OutputStream *os) throw (IOException) {
  SetRight::writeTo(os);
}

void UnsetWriter::readFrom(InputStream *is) throw (IOException) {
  SetRight::readFrom(is);
}

// ######################################################################
// AdminMessage Class
// ######################################################################
AdminMessage::AdminMessage() : Message() {
  type = Message::ADMIN;
}

AdminMessage::~AdminMessage() {
}

void AdminMessage::setAdminMessage(AbstractAdminMessage* adminMsg) {
  type = Message::ADMIN;
  OutputStream* out = new OutputStream();
  AbstractAdminMessage::write(adminMsg, out);
  length = out->size();
  body = new byte[length+4];
  out->toBuffer(body);
  delete out;
}

AbstractAdminMessage* AdminMessage::getAdminMessage() {
  InputStream* in = new InputStream(body, length, length);
  AbstractAdminMessage* adminMsg = AbstractAdminMessage::read(in);
  delete in;
  return adminMsg;
}

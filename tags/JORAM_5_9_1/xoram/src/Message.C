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
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "XoramException.H"
#include "Message.H"
#include "Destination.H"

// ######################################################################
// Message Class
// ######################################################################

/**
 * Constructs a bright new <code>Message</code>.
 */
Message::Message() {
  if(DEBUG)
    printf("=> Message():\n");
  session = (Session*) NULL;

  type = SIMPLE;

  id = (char*) NULL;
  persistent = true;
  priority = 4;
  expiration = 0;
  timestamp = -1;
  redelivered = false;
  deliveryCount = 0;
  toId = (char*) NULL;
  toName = (char*) NULL;
  replyToId = (char*) NULL;
  replyToName = (char*) NULL;
  correlationId = (char*) NULL;
  jmsType = (char*) NULL;
  properties = (Properties*) NULL;
  body = (byte*) NULL;
  compressed = false;
  deliveryTime = 0;
  clientID = (char*) NULL;
  //replyToType = Destination::NOTYPE;
  if(DEBUG)
    printf("<= Message():\n");
}

Message::~Message() {
  if(DEBUG)
    printf("~Message(): properties = 0x%x", properties);
  if (properties != NULL) {
    delete properties;
    properties = (Properties*) NULL;
  }
  if (id != (char*) NULL) {
    delete id;
    id = (char*) NULL;
  }
  return;
}

void Message::setBody(int length, byte* body) {
	this->length = length;
	this->body = body;
	type = BYTES;
}

void Message::getBody(int* length, byte** body) {
	*length = this->length;
	*body = this->body;
}

void Message::acknowledge() {
  if (session == NULL) throw IllegalStateException();
}

/**
 * Returns the message correlation identifier.
 */
char* Message::getCorrelationID() {
  return correlationId;
}

/**
 * Sets the correlation ID for the message.
 */
void Message::setCorrelationID(char* correlationId) {
  this->correlationId = correlationId;
}

/**
 * Returns <code>true</code> if the message is persistent.
 */
int Message::getDeliveryMode() {
  if (persistent)
    return DeliveryMode::PERSISTENT;
  else
    return DeliveryMode::NON_PERSISTENT;
}

/**
 * Sets the DeliveryMode value for this message.
 */
void Message::setDeliveryMode(int deliveryMode) {
  if (deliveryMode == DeliveryMode::PERSISTENT)
    persistent = TRUE;
  else
    persistent = FALSE;
}

/**
 * Returns the message destination.
 */
Destination* Message::getDestination() {
  return new Destination(toId, toType, (char*) NULL);
}

/**
 * Sets the Destination object for this message.
 */
void Message::setDestination(Destination* destination) {
  toId = destination->getUID();
  toType = destination->getType();
}

/**
 * Returns the message expiration time in seconds.
 */
long long Message::getExpiration() {
  return expiration;
}

/**
 * Sets the message's expiration value in seconds.
 */
void Message::setExpiration(long long expiration) {
  this->expiration = expiration;
}

/**
 * Returns the message identifier.
 */
char* Message::getMessageID() {
  return id;
}

/**
 * Sets the message ID.
 */
void Message::setMessageID(char* id) {
  this->id = id;
}

/**
 * Returns the message priority.
 */
int Message::getPriority() {
  return priority;
}

/**
 * Sets the priority level for this message.
 */
void Message::setPriority(int priority) {
  this->priority = priority;
}

/**
 * Gets an indication of whether this message is being redelivered.
 */
boolean Message::getRedelivered() {
  return redelivered;
}

void Message::setReplyTo(Destination* replyTo) {
  replyToId = replyTo->getUID();
  replyToName = replyTo->getName();
  replyToType = replyTo->getType();
}

/**
 * Gets the Destination object to which a reply to this message should
 * be sent.
 */
Destination* Message::getReplyTo() {
  if (replyToId != (char*) NULL) {
    return new Destination(replyToId, replyToType, (char*) NULL);
  }
  return (Destination*) NULL;
}

/**
 * Returns the message time stamp.
 */
long long Message::getTimestamp() {
  return timestamp;
}

/**
 * Sets the message timestamp.
 */
void Message::setTimestamp(long long timestamp) {
  this->timestamp = timestamp;
}

// ==================================================
// Methods about properties
// ==================================================

void Message::clearProperties() {
  if (properties != (Properties*) NULL) {
    delete properties;
    properties = (Properties*) NULL;
  }
  return;
}

void Message::checkPropertyName(char* name) throw (MessageFormatException) {
  if ((name == (char*) NULL) || (*name == '\0'))
    throw  MessageFormatException("Invalid property name");

  if (strncmp(name, "JMS", 3) == 0)
    throw  MessageFormatException("Invalid property names with prefix 'JMS'");

  if ((strcasecmp(name, "NULL") == 0) ||
      (strcasecmp(name, "TRUE") == 0) || (strcasecmp(name, "FALSE") == 0) ||
      (strcasecmp(name, "NOT") == 0) ||
      (strcasecmp(name, "AND") == 0) ||
      (strcasecmp(name, "OR") == 0) ||
      (strcasecmp(name, "BETWEEN") == 0) ||
      (strcasecmp(name, "LIKE") == 0) ||
      (strcasecmp(name, "IN") == 0) ||
      (strcasecmp(name, "IS") == 0) ||
      (strcasecmp(name, "ESCAPE") == 0))
    throw  MessageFormatException("Invalid property names using SQL terminal");
}

boolean Message::getBooleanProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) return FALSE;

  return properties->getBooleanProperty(name);
}

byte Message::getByteProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getByteProperty(name);
}

double Message::getDoubleProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getDoubleProperty(name);
}

float Message::getFloatProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getFloatProperty(name);
}

int Message::getIntProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getIntProperty(name);
}

long Message::getLongProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getLongProperty(name);
}

short Message::getShortProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) throw MessageFormatException();

  return properties->getShortProperty(name);
}

char* Message::getStringProperty(char* name) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL) return (char*) NULL;

  return properties->getStringProperty(name);
}

void Message::setBooleanProperty(char* name, boolean value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setBooleanProperty(name, value);
}

void Message::setByteProperty(char* name, byte value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setByteProperty(name, value);
}

void Message::setDoubleProperty(char* name, double value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setDoubleProperty(name, value);
}

void Message::setFloatProperty(char* name, float value) {
  throw NotYetImplementedException();
}

void Message::setIntProperty(char* name, int value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setIntProperty(name, value);
}

void Message::setLongProperty(char* name, long value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setLongProperty(name, value);
}

void Message::setShortProperty(char* name, short value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setShortProperty(name, value);
}

void Message::setStringProperty(char* name, char* value) {
  checkPropertyName(name);
  if (properties == (Properties*) NULL)
    properties = new Properties();

  properties->setStringProperty(name, value);
}

// ==================================================
// Cloneable interface
// ==================================================

Message* Message::clone() {
  Message* clone = new Message();

  clone->session = session;
  clone->type = type;
  if (id != (char*) NULL) {
    char* cloneId = new char[strlen(id)+1];
    strcpy(cloneId, id);
    cloneId[strlen(id)] = '\0';
    clone->id = cloneId;
  } else {
    clone->id = id;
  }
  clone->persistent = persistent;
  clone->priority = priority;
  clone->expiration = expiration;
  clone->timestamp = timestamp;
  clone->redelivered = redelivered;
  clone->toId = toId;
  clone->toType = toType;
  clone->replyToId = replyToId;
  clone->replyToName = replyToName;
  clone->replyToType = replyToType;
  clone->correlationId = correlationId;
  clone->jmsType = jmsType;
  clone->compressed = compressed;
  clone->deliveryTime = deliveryTime;
  clone->clientID = clientID;

  if (body != (byte*) NULL) {
    //    throw NotYetImplementedException();
    // AF: May be we can share the body as it should be RO.
    //clone->body = new byte[strlen(body)+1];
    //strcpy(clone->body,body);
    clone->length = length;
    clone->body = body;
    //    System.arraycopy(body, 0, clone.body, 0, body.length);
  }
  if (properties != (Properties*) NULL) {
    clone->properties = (Properties*) properties->clone();
  }

  return clone;
}

// ==================================================
// Streamable interface
// ==================================================

/**
 *  The object implements the writeTo method to write its contents to
 * the output stream.
 *
 * @param os the stream to write the object to
 */
void Message::writeTo(OutputStream* os) throw (IOException) {
  os->writeString(id);
  os->writeString(toId);
  os->writeString(toName);
  os->writeByte(toType);
  os->writeLong(timestamp);
  os->writeBoolean(compressed);
  os->writeLong(deliveryTime);
  os->writeString(clientID);

  short b = 0;
  b = b | (type != SIMPLE ? typeFlag : 0);
  b = b | (replyToId != NULL ? replyToIdFlag : 0);
  b = b | (properties != NULL ? propertiesFlag : 0);
  b = b | (priority != 4 ? priorityFlag : 0);
  b = b | (expiration != 0 ? expirationFlag : 0);
  b = b | (correlationId != NULL ? correlationIdFlag : 0);
  b = b | (deliveryCount != 0 ? deliveryCountFlag : 0);
  b = b | (jmsType != NULL ? jmsTypeFlag : 0);
  b = b | (redelivered ? redeliveredFlag : 0);
  b = b | (persistent ? persistentFlag : 0);
  
  os->writeShort(b);
  
  if (type != SIMPLE) { os->writeInt(type); }					// AF: Should be a byte
  if (replyToId != NULL) { 
    os->writeString(replyToId); 
    os->writeString(replyToName);
    os->writeByte(replyToType);
  }
  if (properties != NULL) { os->writeProperties(properties); }			// Should be null !!
  if (priority != 4) { os->writeInt(priority); }				// AF: Should be a byte
  if (expiration != 0) { os->writeLong(expiration); }
  if (correlationId != NULL) { os->writeString(correlationId); }
  if (deliveryCount != 0) { os->writeInt(deliveryCount); }
  if (jmsType != NULL) { os->writeString(jmsType); }

  os->writeByteArray(body, length);
}

/**
 *  The object implements the readFrom method to restore its contents from
 * the input stream.
 *
 * @param is the stream to read data from in order to restore the object
 */
void Message::readFrom(InputStream* is) throw (IOException) {
  is->readString(&id);
  is->readString(&toId);
  is->readString(&toName);
  is->readByte(&toType);
  is->readLong(&timestamp);
  is->readBoolean(&compressed);
  is->readLong(&deliveryTime);
  is->readString(&clientID);
  
  short b;
  is->readShort(&b);

  if (b & typeFlag) { is->readInt(&type); }
  if (b & replyToIdFlag) { 
    is->readString(&replyToId); 
    is->readString(&replyToName); 
    is->readByte(&replyToType); 
  }
  if (b & propertiesFlag) { properties = is->readProperties(); }
  if (b & priorityFlag) { is->readInt(&priority); }
  if (b & expirationFlag) { is->readLong(&expiration); }
  if (b & correlationIdFlag) { is->readString(&correlationId); }
  if (b & deliveryCountFlag) { is->readInt(&deliveryCount); }
  if (b & jmsTypeFlag) { is->readString(&jmsType); }
  redelivered = ((b & redeliveredFlag) != 0);
  persistent = ((b & persistentFlag) != 0);

  length = is->readByteArray(&body);
  
}

/**
 *  The object allows to write to the output stream a vector of message.
 *
 * @param messages 	the vector of messages
 * @param os 		the stream to write the vector to
 */
void Message::writeVectorTo(Vector<Message>* messages,
                                   OutputStream* os) throw (IOException) {
  if (messages == (Vector<Message>*) NULL) {
    os->writeInt(-1);
  } else {
    int size = messages->size();
    os->writeInt(size);
    for (int i=0; i<size; i++) {
      messages->elementAt(i)->writeTo(os);
    }
  }
}

/**
 *  this method allows to read from the input stream a vector of messages.
 *
 * @param is 	the stream to read data from in order to restore the vector
 * @return	the vector of messages
 */
Vector<Message>* Message::readVectorFrom(InputStream* is) throw (IOException) {
  int size;
  is->readInt(&size);
  if (size == -1) {
    return (Vector<Message>*) NULL;
  } else {
    Vector<Message>* messages = new Vector<Message>(size);
    for (int i=0; i<size; i++) {
      Message* msg = new Message();
      msg->readFrom(is);
      messages->addElement(msg);
    }
    return messages;
  }

}

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
#include <string.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"
#include "XoramWrapper.H"

//##############################################################
// TCPConnectionFactory
//##############################################################
char* create_tcp_connection_factory(char* host, int port) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_tcp_connection_factory(%s,%i)\n", host, port);
  ConnectionFactory* cf = new TCPConnectionFactory(host, port);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_tcp_connection_factory cf = 0x%x\n", cf);
  return (char*) cf;
}

char* _create_tcp_connection_factory() {
  return (char*) create_tcp_connection_factory(DFLT_HOST, DFLT_PORT);
}

void delete_tcp_connection_factory(char* connectionFactory) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: delete_tcp_connection_factory(0x%x)\n", connectionFactory);
  TCPConnectionFactory* cf = (TCPConnectionFactory*)connectionFactory;
  delete cf;
}

//##############################################################
// Connection
//##############################################################
char* create_connection_host(char* connectionFactory, char* user, char* passwd, char* host, int port) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_connection_host(0x%x,%s,%s,%s,%i)\n", connectionFactory, user, passwd, host, port);
  TCPConnectionFactory* cf = (TCPConnectionFactory*)connectionFactory;
  Connection* cnx = cf->createConnection(user, passwd);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_connection_host cnx = 0x%x\n", cnx);
  return (char*) cnx;
}

char* create_connection(char* connectionFactory, char* user, char* passwd) {
  return (char*) create_connection_host(connectionFactory, user, passwd, DFLT_HOST, DFLT_PORT);
}

char* _create_connection(char* connectionFactory) {
  return (char*) create_connection(connectionFactory, DFLT_LOGIN, DFLT_PASSWORD);
}

void start_connection(char* connection) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: start_connection(0x%x)\n", connection);
  Connection* cnx = (Connection*) connection;
  cnx->start();
}

void close_connection(char* connection) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: close_connection(0x%x)\n", connection);
  Connection* cnx = (Connection*) connection;
  cnx->close();
  delete cnx;
}

boolean is_stopped_connection(char* connection) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: is_stopped_connection(0x%x)\n", connection);
  Connection* cnx = (Connection*) connection;
  return cnx->isStopped();
}

//##############################################################
// Destination
//##############################################################
char* create_queue(char* id, char* name) {
  // id = "#0.0.1026", name = "queue"
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_queue(%s,%s)\n", id, name);
  Queue* queue = new Queue(id, name);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_queue queue = 0x%x\n", queue);
  return (char*) queue;
}

char* create_topic(char* id, char* name) {
  // id = "#0.0.1026", name = "topic"
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_topic(%s,%s)\n", id, name);
  Topic* topic = new Topic(id, name);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_topic topic = 0x%x\n", topic);
  return (char*) topic;
}

boolean is_queue(char* destination) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: is_queue(0x%x)\n", destination);
  Destination* dest = (Destination*) destination;
  return dest->isQueue();
}

boolean is_topic(char* destination) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: isTopic(0x%x)\n", destination);
  Destination* dest = (Destination*) destination;
  return dest->isTopic();
}

void delete_destination(char* destination) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: delete_destination(0x%x)\n", destination);
  Destination* dest = (Destination*) destination;
  delete dest;
}

//##############################################################
// Session
//##############################################################
char* create_session(char* connection) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_session(0x%x)\n", connection);
  Connection* cnx = (Connection*) connection;
  Session* session = cnx->createSession();
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_session session = 0x%x\n", session);
  return (char*) session;
}

char* get_connection(char* session) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_connection(0x%x)\n", session);
  Session* sess = (Session*) session;
  return (char*) sess->getConnection();
}

void close_session(char* session) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: close_session(0x%x)\n", session);
  Session* sess = (Session*) session;
  sess->close();
  delete sess;
}

//##############################################################
// Producer
//##############################################################
/* return MessageProducer */
char* create_producer(char* session, char* destination) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_producer(0x%x,0x%x)\n", session, destination);
  Session* sess = (Session*) session;
  Destination* dest = (Destination*) destination;
  MessageProducer* prod = sess->createProducer(dest);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_producer prod = 0x%x\n", prod);
  return (char*) prod;
}

char* get_destination_message_producer(char* producer) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_destination_message_producer(0x%x)\n", producer);
  MessageProducer* prod = (MessageProducer*) producer;
  return (char*) prod->getDestination();
}

void send_message_ttl(char* producer, 
                     char* message, 
                     char* destination,
                     int deliveryMode,
                     int priority,
                     long timeToLive) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: send_message(0x%x,0x%x,0x%x,%d,%d,%d)\n", 
           producer, message, destination, deliveryMode, priority, timeToLive);
  Message* msg = (Message*) message;
  MessageProducer* prod = (MessageProducer*) producer;
  Destination* dest = (Destination*) destination;
  prod->send(msg, dest, deliveryMode, priority, timeToLive);
}

void send_message_dest(char* producer, char* message, char* destination) {
  MessageProducer* prod = (MessageProducer*) producer;
  send_message_ttl(producer, message, destination, prod->getDeliveryMode(), prod->getPriority(), prod->getTimeToLive());
}

void send_message(char* producer, char* message) {
  MessageProducer* prod = (MessageProducer*) producer;
  send_message_dest(producer, message, (char*) prod->getDestination());
}

//##############################################################
// Consumer
//##############################################################
/* return MessageConsumer */
char* create_consumer_selector(char* session, char* destination, char* selector, boolean nolocal) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: createConsumer(0x%x,0x%x,%s,%b)\n", session, destination, selector, nolocal);
  Session* sess = (Session*) session;
  Destination* dest = (Destination*) destination;
  MessageConsumer* cons = sess->createConsumer(dest, selector, nolocal);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: createConsumer cons = 0x%x\n", cons);
  return (char*) cons;
}

/* return MessageConsumer */
char* create_consumer(char* session, char* destination) {
  return create_consumer_selector(session, destination, (char*) NULL, FALSE);
}

/* return MessageConsumer */
char* create_durable_subscriber_selector(char* session, char* topic, char* subname, char* selector, boolean nolocal) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_durable_subscriber(0x%x,0x%x,%s,%s,%b)\n", session, topic, subname, selector, nolocal);
  Session* sess = (Session*) session;
  Topic* dest = (Topic*) topic;
  MessageConsumer* cons = sess->createDurableSubscriber(dest, subname, selector, nolocal);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_durable_subscriber cons = 0x%x\n", cons);
  return (char*) cons;
}

char* create_durable_subscriber(char* session, char* topic, char* subname) {
  return create_durable_subscriber_selector(session, topic, subname, (char*) NULL, FALSE);
}

void unsubscribe(char* session, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: unsubscribe(0x%x,%s)\n", session, name);
  Session* sess = (Session*) session;
  sess->unsubscribe(name);
}

/* Message */
char* receive_message_timeout(char* consumer, long timeOut) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: receive_message_timeout(0x%x,%d)\n", consumer, timeOut);
  MessageConsumer* cons = (MessageConsumer*) consumer;
  Message* msg = cons->receive(timeOut);
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: receive_message_timeout msg = 0x%x\n", msg);
  return (char*) msg;
}

char* receive_message(char* consumer) {
  return receive_message_timeout(consumer, 0);
}

/* Message */
char* receive_no_wait_message(char* consumer) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: receive_no_wait_message(0x%x)\n", consumer);
  MessageConsumer* cons = (MessageConsumer*) consumer;
  Message* msg = cons->receiveNoWait();
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: receive_no_wait_message msg = 0x%x\n", msg);
  return (char*) msg;
}

//##############################################################
// Message
//##############################################################
char* create_message(char* session) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_message(0x%x)\n", session);
  Session* sess = (Session*) session;
  Message* msg = sess->createMessage();
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: create_message msg = 0x%x\n", msg);
  return (char*) msg;
}

void delete_message(char* message) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: delete_message(0x%x)\n", message);
  Message* msg = (Message*) message;
  delete msg;
}

void clear_properties(char* message) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: clear_properties(0x%x)\n", message);
  Message* msg = (Message*) message;
  msg->clearProperties();
}

boolean get_boolean_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_boolean_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getBooleanProperty(name);
}

byte get_byte_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_byte_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getByteProperty(name);
}

double get_double_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_double_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getDoubleProperty(name);
}

float get_float_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_float_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getFloatProperty(name);
}

int get_int_property(char* message, char* name) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_int_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getIntProperty(name);
}

long get_long_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_long_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getLongProperty(name);
}

short get_short_property(char* message, char* name) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_short_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getShortProperty(name);
}

char* get_string_property(char* message, char* name) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_string_property(0x%x,%s)\n", message, name);
  Message* msg = (Message*) message;
  return msg->getStringProperty(name);
}

void set_boolean_property(char* message, char* name, boolean value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_boolean_property(0x%x,%s,%b)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setBooleanProperty(name, value);
}

void set_byte_property(char* message, char* name, byte value) {
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_byte_property(0x%x,%s,%b)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setByteProperty(name, value);
}

void set_double_property(char* message, char* name, double value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_double_property(0x%x,%s,%d)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setDoubleProperty(name, value);
}

void set_float_property(char* message, char* name, float value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_float_property(0x%x,%s,%f)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setFloatProperty(name, value);
}

void set_int_property(char* message, char* name, int value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_int_property(0x%x,%s,%i)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setIntProperty(name, value);
}

void set_long_property(char* message, char* name, long value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_long_property(0x%x,%s,%d)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setLongProperty(name, value);
}

void set_short_property(char* message, char* name, short value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_short_property(0x%x,%s,%d)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setShortProperty(name, value);
}

void set_string_property(char* message, char* name, char* value) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_string_property(0x%x,%s,%s)\n", message, name, value);
  Message* msg = (Message*) message;
  msg->setStringProperty(name, value);
}

char* clone_message(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: clone_message(0x%x)\n", message);
  Message* msg = (Message*) message;
  return (char*) msg->clone();
}

long long get_timestamp(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_timestamp(0x%x)\n", message);
  Message* msg = (Message*) message;
  return msg->getTimestamp();
}

// Destination
char* get_replyto(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_replyto(0x%x)\n", message);
  Message* msg = (Message*) message;
  return (char*) msg->getReplyTo();
}

char* get_destination(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_destination(0x%x)\n", message);
  Message* msg = (Message*) message;
  return (char*) msg->getDestination();
}

void set_destination(char* message, char* destination) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: set_destination(0x%x,0x%x)\n", message, destination);
  Message* msg = (Message*) message;
  msg->setDestination((Destination*) destination);
}

char* get_message_id(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_message_id(0x%x)\n", message);
  Message* msg = (Message*) message;
  return msg->getMessageID();
}

boolean get_redelivered(char* message) { 
  if (WRAPPER_DEBUG)
    printf("XoramWrapper: get_redelivered(0x%x)\n", message);
  Message* msg = (Message*) message;
  return msg->getRedelivered();
}

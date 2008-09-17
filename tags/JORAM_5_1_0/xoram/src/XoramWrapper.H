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
#ifndef XORAM_WRAPPER_H
#define XORAM_WRAPPER_H

#ifndef NULL
#define NULL    0
#endif /* NULL */

#ifndef TRUE
#define TRUE	(0==0)
#define FALSE	(!TRUE)
#endif /* TRUE */

#ifndef INTEGER_MAX_VALUE
#define INTEGER_MAX_VALUE 0x7FFFFFFF
#endif /* INTEGER_MAX_VALUE */

#ifndef DEBUG
#define DEBUG    (FALSE)
#endif /* DEBUG */

#ifndef WRAPPER_DEBUG
#define WRAPPER_DEBUG    (FALSE)
#endif /* WRAPPER_DEBUG */

typedef char byte;
typedef byte boolean;

static char* DFLT_LOGIN = "anonymous";
static char* DFLT_PASSWORD = "anonymous";
static char* DFLT_HOST = "localhost";
static int DFLT_PORT = 16010;

/*
typedef struct {

} cnx;
*/

#ifdef __cplusplus
extern "C" {
#endif
  char* create_tcp_connection_factory(char* host, int port);
  char* _create_tcp_connection_factory();
  void delete_tcp_connection_factory(char* connectionFactory);
  char* create_connection_host(char* connectionFactory, char* user, char* passwd, char* host, int port);
  char* create_connection(char* connectionFactory, char* user, char* passwd);
  char* _create_connection(char* connectionFactory);
  void start_connection(char* connection);
  void close_connection(char* connection);
  boolean is_stopped_connection(char* connection);
  char* create_queue(char* id, char* name);
  char* create_topic(char* id, char* name);
  boolean is_queue(char* destination);
  boolean is_topic(char* destination);
  void delete_destination(char* destination);
  char* create_session(char* connection);
  char* get_connection(char* session);
  void close_session(char* session);
  char* create_producer(char* session, char* destination);
  char* get_destination_message_producer(char* producer);
  void send_message_ttl(char* producer, char* message, char* destination, int deliveryMode, int priority, long timeToLive);
  void send_message_dest(char* producer, char* message, char* destination);
  void send_message(char* producer, char* message);
  char* create_consumer_selector(char* session, char* destination, char* selector, boolean nolocal);
  char* create_consumer(char* session, char* destination);
  char* create_durable_subscriber_selector(char* session, char* topic, char* subname, char* selector, boolean nolocal);
  char* create_durable_subscriber(char* session, char* topic, char* subname);
  void unsubscribe(char* session, char* name);
  char* receive_message_timeout(char* consumer, long timeOut);
  char* receive_message(char* consumer);
  char* receive_no_wait_message(char* consumer);
  char* create_message(char* session);
  void delete_message(char* message);
  void clear_properties(char* message);
  boolean get_boolean_property(char* message, char* name);
  byte get_byte_property(char* message, char* name);
  double get_double_property(char* message, char* name);
  float get_float_property(char* message, char* name);
  int get_int_property(char* message, char* name); 
  long get_long_property(char* message, char* name);
  short get_short_property(char* message, char* name);
  char* get_string_property(char* message, char* name); 
  void set_boolean_property(char* message, char* name, boolean value); 
  void set_byte_property(char* message, char* name, byte value);
  void set_double_property(char* message, char* name, double value); 
  void set_float_property(char* message, char* name, float value); 
  void set_int_property(char* message, char* name, int value); 
  void set_long_property(char* message, char* name, long value); 
  void set_short_property(char* message, char* name, short value); 
  void set_string_property(char* message, char* name, char* value); 
  char* clone_message(char* message); 
  long long get_timestamp(char* message); 
  char* get_reply_to(char* message); 
  char* get_destination(char* message); 
  void set_destination(char* message, char* destination); 
  char* get_message_id(char* message); 
  boolean get_redelivered(char* message); 
#ifdef __cplusplus
}
#endif

#endif /* XORAM_WRAPPER_H */

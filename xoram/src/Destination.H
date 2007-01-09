/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
#ifndef DESTINATION_H
#define DESTINATION_H

#include <string.h>

/**
 * A Destination object encapsulates a Joram's specific address.
 */
class Destination {
 private:
  char* uid;
  char type;
  char* name;

 public:
  enum {QUEUE, TOPIC};

  Destination(char* uid, char type, char* name);

  /**
   * Returns the unique internal name of the destination.
   */
  char* getUID();

  /**
   * Returns the administrative name of the destination.
   */
  char* getName();

  /**
   * Returns true if the destinatination is a queue.
   */
  boolean isQueue();

  /**
   * Returns true if the destinatination is a topic.
   */
  boolean isTopic();

  /**
   * Non API method, should be hidden.
   */
  char getType();

  /**
   * Non API method, should be hidden.
   */
  static char* typeToString(char type) {
    if (type == QUEUE)
      return "queue";
    else if (type == TOPIC)
      return "topic";
    return (char*) NULL;
  }

  /**
   * Non API method, should be hidden.
   */
  static char stringToType(char *type) {
    if (strcasecmp(type, "queue"))
      return QUEUE;
    else if (strcasecmp(type, "topic"))
      return TOPIC;
    return 0;
  }

  /**
   * Non API method, should be hidden.
   */
  Destination* newInstance(char* uid, char type, char* name);
};

/**
 * A Queue object encapsulates information to handle a Joram's queue.
 */
class Queue : public Destination {
 public:
  /**
   * Creates a Queue object.
   */
  Queue(char* uid, char* name) : Destination(uid, QUEUE, name) { }
};

/**
 * A Topic object encapsulates information to handle a Joram's topic.
 */
class Topic : public Destination {
 public:
  /**
   * Creates a Topic object.
   */
  Topic(char* uid, char* name) : Destination(uid, TOPIC, name) { }
};

#endif /* DESTINATION_H */

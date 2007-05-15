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
#ifndef PROPERTIES_H
#define PROPERTIES_H

#include "Types.H"

class OutputStream;
class InputStream;

// ######################################################################
// Properties Class
// ######################################################################
/**
 * Built-in facility for supporting application-defined property values.<br>
 * Property names must not be null, and must not be empty strings. If a
 * property name is set and it is either null or an empty string, an
 * IllegalArgumentException must be thrown.<br>
 * Property values can be boolean, byte, short, int, long, float, double,
 * and String. At this time, properties does nor support conversion between
 * types.
 */
class Properties {
 private:
  class Entry {
  public:
    int hash;
    char* key;

    byte type;
    union {
      boolean b;
      byte c;
      double d;
      float f;
      int i;
      long long l;
      short s;
      char* str;
    };
    Entry* next;

    Entry(int hash, char* key, Entry* next) {
      this->hash = hash;
      this->key = key;
      this->next = next;
    }
  };

  int count;
  int length;
  Entry** table;
  float loadFactor;
  int threshold;

  void rehash();

  int hashCode(char* key);

  Entry* get(char* key);
  Entry* put(char* key);

 public:
  Properties(int capacity, float loadFactor);

  Properties(int capacity);

  Properties();

  void init(int capacity, float loadFactor);

  ~Properties();

  int size();

  boolean isEmpty();

  void clear();

  boolean getBooleanProperty(char* name);
  byte getByteProperty(char* name);
  double getDoubleProperty(char* name);
  float getFloatProperty(char* name);
  int getIntProperty(char* name);
  long long getLongProperty(char* name);
  short getShortProperty(char* name);
  char* getStringProperty(char* name);

  void setBooleanProperty(char* name, boolean value);
  void setByteProperty(char* name, byte value);
  void setDoubleProperty(char* name, double value);
  void setFloatProperty(char* name, float value);
  void setIntProperty(char* name, int value);
  void setLongProperty(char* name, long long value);
  void setShortProperty(char* name, short value);
  void setStringProperty(char* name, char* value);

  // ==================================================
  // Cloneable interface
  // ==================================================

  Properties* clone();

  // ==================================================
  // Streamable interface
  // ==================================================

  void writePropertiesTo(OutputStream* os) throw (IOException);
  void readPropertiesFrom(InputStream* is, int count) throw (IOException);
};

#endif /* PROPERTIES_H */

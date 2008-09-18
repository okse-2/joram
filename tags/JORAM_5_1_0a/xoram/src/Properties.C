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
#include <string.h>

#include "Properties.H"
#include "XStream.H"
#include "XoramException.H"

// ######################################################################
// Properties Class
// ######################################################################

void Properties::init(int capacity, float loadFactor) {
  if(DEBUG)
    printf("=> Properties::init()\n");
  if (capacity < 0)
    throw IllegalArgumentException();
  if (loadFactor <= 0)
    throw IllegalArgumentException();

  count = 0;
  length = capacity;
  if (length == 0) length = 1;
  table = new Entry* [length];
  for (int i=0; i<length; i++) {
    table[i] = (Entry*) NULL;
  } 
  this->loadFactor = loadFactor;
  threshold = (int) (length * loadFactor);
  if(DEBUG)
    printf("<= Properties::init() table = 0x%x\n", table);
}

Properties::Properties(int capacity, float loadFactor) {
  init(capacity, loadFactor);
}

Properties::Properties(int capacity) {
  init(capacity, 0.75f);
}

Properties::Properties() {
  init(11, 0.75f);
}

Properties::~Properties() {
  if(DEBUG)
    printf("~Properties(): table = 0x%x\n", table);
  clear();
  if (table != (Entry**) NULL) {
    //delete[] table;
    table = (Entry**) NULL;
  }
  count = 0;
  length = 0;
}

int Properties::size() {
  return count;
}

boolean Properties::isEmpty() {
  return (count == 0);
}

void Properties::clear() {
  for (int index = length; --index >= 0; ) {
    if (table[index] != (Entry*) NULL) {
      delete table[index];
    }
     table[index] = (Entry*) NULL;
  }
  count = 0;
}

int Properties::hashCode(char* key) {
  int hash = 0;
  int len = strlen(key);
  for (int i=0; i<len; i++) {
    hash = 31*hash + key[i];
  }
  return hash;
}

void Properties::rehash() {
  Entry** old = table;
  int oldLength = length;

  length = length*2 +1;
  table = new Entry* [length];
  threshold = (int) (length * loadFactor);

  // Initialisation des tableaux ?

  for (int i=0; i<oldLength ; i++) {
    while (old[i] != (Entry*) NULL) {
      Entry* entry = old[i];
      old[i] = entry->next;

      int index = (entry->hash & 0x7FFFFFFF) % length;
      entry->next = table[index];
      table[index] = entry;
    }
  }
}

Properties::Entry* Properties::put(char* key) {
  Entry* entry = get(key);

  if (entry == (Entry*) NULL) {
    if (count >= threshold) rehash();
    int hash = hashCode(key);
    int index = (hash & 0x7FFFFFFF) % length;
    // Creates the new entry.
    entry = new Entry(hash, key, table[index]);
    table[index] = entry;
    count++;
  }

  return entry;
}

Properties::Entry* Properties::get(char* key) {
  if ((key == (char*) NULL) || (*key == '\0'))
    throw IllegalArgumentException("Bad property name");

  if  (length > 0) {
    int hash = hashCode(key);
    int index = (hash & 0x7FFFFFFF) % length;
    //printf("index = %i, table[%i]=0x%x\n",index,index,table[index]);//NTA tmp
    for (Entry* e=table[index]; e!=(Entry*) NULL ; e=e->next) {
      if ((e->hash == hash) && (strcmp(e->key, key) == 0)) {
        return e;
      }
    }
  }

  return (Entry*) NULL;
}

boolean Properties::getBooleanProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    return FALSE;
  } else if (entry->type == XStream::S_BOOLEAN) {
    return entry->b;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to Boolean");
  } else {
    throw MessageFormatException("Can't converted to Boolean");
  }
}

byte Properties::getByteProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to byte");
  } else if (entry->type == XStream::S_BYTE) {
    return entry->c;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to byte");
  } else {
    throw MessageFormatException("Can't converted to byte");
  }
}

double Properties::getDoubleProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to double 1");
  } else if (entry->type == XStream::S_DOUBLE) {
    return entry->d;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to double 2");
  } else {
    throw MessageFormatException("Can't converted to double 3");
  }
}

float Properties::getFloatProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to float");
  } else if (entry->type == XStream::S_FLOAT) {
    return entry->f;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to float");
  } else {
    throw MessageFormatException("Can't converted to float");
  }
}

int Properties::getIntProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to integer");
  } else if (entry->type == XStream::S_INT) {
    return entry->i;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to integer");
  } else {
    throw MessageFormatException("Can't converted to integer");
  }
}

long long Properties::getLongProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to long");
  } else if (entry->type == XStream::S_LONG) {
    return entry->l;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to long");
  } else {
    throw MessageFormatException("Can't converted to long");
  }
}

short Properties::getShortProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    throw MessageFormatException("Can't converted to short");
  } else if (entry->type == XStream::S_SHORT) {
    return entry->s;
  } else if (entry->type == XStream::S_STRING) {
    // TODO
    throw MessageFormatException("Can't converted to short");
  } else {
    throw MessageFormatException("Can't converted to short");
  }
}

// AF: Be careful to string allocation !!
char* Properties::getStringProperty(char* name) {
  Entry* entry = get(name);
  if (entry == (Entry*) NULL) {
    return (char*) NULL;
  } else if (entry->type == XStream::S_STRING) {
    return entry->str;
  } else if (entry->type == XStream::S_BOOLEAN) {
    if (entry->b == TRUE)
      return "true";
    return "false";
  } else if (entry->type == XStream::S_BYTE) {
    // TODO
    throw MessageFormatException("");
  } else if (entry->type == XStream::S_DOUBLE) {
    // TODO
    throw MessageFormatException("");
  } else if (entry->type == XStream::S_FLOAT) {
    // TODO
    throw MessageFormatException("");
  } else if (entry->type == XStream::S_INT) {
    // TODO
    throw MessageFormatException("");
  } else if (entry->type == XStream::S_LONG) {
    // TODO
    throw MessageFormatException("");
  } else if (entry->type == XStream::S_SHORT) {
    // TODO
    throw MessageFormatException("");
  } else {
    throw MessageFormatException("Unknown type");
  }
}

void Properties::setBooleanProperty(char* name, boolean value) {
  Entry* entry = put(name);
  entry->type = XStream::S_BOOLEAN;
  entry->b = value;
}

void Properties::setByteProperty(char* name, byte value) {
  Entry* entry = put(name);
  entry->type = XStream::S_BYTE;
  entry->c = value;
}

void Properties::setDoubleProperty(char* name, double value) {
  Entry* entry = put(name);
  entry->type = XStream::S_DOUBLE;
  entry->d = value;
}

void Properties::setFloatProperty(char* name, float value) {
  Entry* entry = put(name);
  entry->type = XStream::S_FLOAT;
  entry->f = value;
}

void Properties::setIntProperty(char* name, int value) {
  Entry* entry = put(name);
  entry->type = XStream::S_INT;
  entry->i = value;
}

void Properties::setLongProperty(char* name, long long value) {
  Entry* entry = put(name);
  entry->type = XStream::S_LONG;
  entry->l = value;
}

void Properties::setShortProperty(char* name, short value) {
  Entry* entry = put(name);
  entry->type = XStream::S_SHORT;
  entry->s = value;
}

void Properties::setStringProperty(char* name, char* value) {
  Entry* entry = put(name);
  entry->type = XStream::S_STRING;
  entry->str = value;
}

Properties* Properties::clone() {
  Properties* clone = new Properties(length, loadFactor);

  for (int index = length-1; index >= 0; index--) {
    Entry* entry = table[index];

    while (entry != (Entry*) NULL) {
      entry->key;
      if (entry->type == XStream::S_BOOLEAN) {
        clone->setBooleanProperty(entry->key, entry->b);
      } else if (entry->type == XStream::S_BYTE) {
        clone->setByteProperty(entry->key, entry->c);
      } else if (entry->type == XStream::S_SHORT) {
        clone->setShortProperty(entry->key, entry->s);
      } else if (entry->type == XStream::S_INT) {
        clone->setIntProperty(entry->key, entry->i);
      } else if (entry->type == XStream::S_LONG) {
        clone->setLongProperty(entry->key, entry->l);
      } else if (entry->type == XStream::S_FLOAT) {
        clone->setFloatProperty(entry->key, entry->f);
      } else if (entry->type == XStream::S_DOUBLE) {
        clone->setDoubleProperty(entry->key, entry->d);
      } else if (entry->type == XStream::S_STRING) {
        clone->setStringProperty(entry->key, entry->str);
      } else if (entry->type == XStream::S_BYTEARRAY) {
        throw NotYetImplementedException();
      } 
      entry = entry->next;
    }
  }

  return clone;
}

void Properties::writePropertiesTo(OutputStream* os) throw (IOException) {
  for (int index = length-1; index >= 0; index--) {
    Entry* entry = table[index];
      
    while (entry != (Entry*) NULL) {
      os->writeString(entry->key);
      os->writeByte(entry->type);
      if (entry->type == XStream::S_BOOLEAN) {
        os->writeBoolean(entry->b);
      } else if (entry->type == XStream::S_BYTE) {
        os->writeByte(entry->c);
      } else if (entry->type == XStream::S_SHORT) {
        os->writeShort(entry->s);
      } else if (entry->type == XStream::S_INT) {
        os->writeInt(entry->i);
      } else if (entry->type == XStream::S_LONG) {
        os->writeLong(entry->l);
      } else if (entry->type == XStream::S_FLOAT) {
        os->writeFloat(entry->f);
      } else if (entry->type == XStream::S_DOUBLE) {
        os->writeDouble(entry->d);
      } else if (entry->type == XStream::S_STRING) {
        os->writeString(entry->str);
      } else if (entry->type == XStream::S_BYTEARRAY) {
        throw NotYetImplementedException();
      } 
      entry = entry->next;
    }
  }
}

void Properties::readPropertiesFrom(InputStream* is,
                                    int count) throw (IOException) {
  char* key;
  byte type;

  for (int i=0; i<count; i++) {
    is->readString(&key);
    is->readByte(&type);
    switch (type) {
    case XStream::S_BOOLEAN: {
      boolean b;
      if (is->readBoolean(&b) == -1) throw IOException();
      setBooleanProperty(key, b);
      break;
    }
    case XStream::S_BYTE: {
      byte b;
      if (is->readByte(&b) == -1) throw IOException();
      setByteProperty(key, b);
      break;
    }
    case XStream::S_SHORT: {
      short s;
      if (is->readShort(&s) == -1) throw IOException();
      setShortProperty(key, s);
      break;
    }
    case XStream::S_INT: {
      int i;
      if (is->readInt(&i) == -1) throw IOException();
      setIntProperty(key, i);
      break;
    }
    case XStream::S_LONG: {
      long long l;
      if (is->readLong(&l) == -1) throw IOException();
      setLongProperty(key, l);
      break;
    }
    case XStream::S_FLOAT: {
      float f;
      if (is->readFloat(&f) == -1) throw IOException();
      setFloatProperty(key, f);
      break;
    }
    case XStream::S_DOUBLE: {
      double d;
      if (is->readDouble(&d) == -1) throw IOException();
      setDoubleProperty(key, d);
      break;
    }
    case XStream::S_STRING: {
      char* str;
      if (is->readString(&str) == -1) throw IOException();
      setStringProperty(key, str);
      break;
    }
    case XStream::S_BYTEARRAY: {
      throw IOException("Not yet implemented");
    }
    default:
      throw IOException("Invalid type"); 
    }
  }
}


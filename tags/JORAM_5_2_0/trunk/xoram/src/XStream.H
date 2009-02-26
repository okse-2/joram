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
#ifndef XSTREAM_H
#define XSTREAM_H

#include <stdio.h>

#include "Types.H"
#include "Vector.H"
#include "Properties.H"

class XStream {
 public:
  static const byte S_TRUE = 0;
  static const byte S_FALSE = 1;

  static const byte S_NULL = -1;
  static const byte S_BOOLEAN = 1;
  static const byte S_BYTE = 2;
  static const byte S_SHORT = 3;
  static const byte S_INT = 4;
  static const byte S_LONG = 5;
  static const byte S_FLOAT = 6;
  static const byte S_DOUBLE = 7;
  static const byte S_STRING = 8;
  static const byte S_BYTEARRAY = 9;
};

class OutputStream : XStream {
 private:
  // The number of valid bytes in the buffer. 
  unsigned int count;

  // The buffer where data is stored. 
  byte* buffer;

  // The length of buffer. 
  unsigned int length;

  int writeBuffer(byte* buf, int len);

 public:
  OutputStream();
  ~OutputStream();

  void reset();
  int size();

  int writeTo(int fd);
  void toBuffer(byte* buf);

  int writeLong(long long l);
  int writeInt(int i);
  int writeShort(short s);
  int writeBoolean(boolean b);
  int writeByte(byte b);
  int writeString(char *str);
  int writeByteArray(byte* tab, int len);

  int writeFloat(float f);
  int writeDouble(double d);

  void writeVectorOfString(Vector<char>* vector) throw(IOException);
  void writeProperties(Properties* properties) throw(IOException);
};

class InputStream : XStream {
 private:
  // The buffer where data is stored. 
  byte* buffer;

  // The length of buffer. 
  unsigned int length;

  // The number of byte in buffer. 
  unsigned int count;

  // The index of the next character to read from the buffer.
  unsigned int pos;

 public:
  InputStream();
  InputStream(byte* buffer, int length, int count);
  ~InputStream();


  int readBuffer(byte* buf, int len);
  int readFrom(int fd);

  void reset();
  int size();
  int skip(int nb);

  int readLong(long long *l);
  int readInt(int *i);
  int readShort(short *s);
  int readBoolean(boolean *b);
  int readByte(byte *b);
  int readString(char **str);
  int readByteArray(byte** tab);

  int readFloat(float *f);
  int readDouble(double *d);

  Vector<char>* readVectorOfString() throw(IOException);
  Properties* readProperties() throw(IOException);
};

class Streamable {
 public:
  virtual void writeTo(OutputStream *os) throw (IOException) =0;
  virtual void readFrom(InputStream *is) throw (IOException) =0;
};

#endif /* XSTREAM_H */

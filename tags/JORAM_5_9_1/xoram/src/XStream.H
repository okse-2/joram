/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2006 CNES
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

/**
 * This class implements an output stream in which the data is written into an array of byte.
 * The internal buffer automatically grows as data is written to it.
 */
class OutputStream : XStream {
 private:
  // The number of valid bytes in the buffer including 4 bytes to transfer the count value.
  unsigned int count;

  // The internal buffer where data is stored.
  byte* buffer;

  // The size of the internal buffer.
  unsigned int length;


 public:
  OutputStream();
  ~OutputStream();

  /**
   * Writes len bytes from array buf to the internal buffer of the stream.
   */
  int writeBuffer(byte* buf, int len);

  /**
   * Resets the count field of this stream to zero, so that all currently accumulated output in the stream is discarded.
   */
  void reset();
  /**
   * Return the current number of bytes stored in the stream.
   */
  int size();

  /**
   * Writes the content of this stream to the specified file descriptor.
   * This method first writes the size of the stream (4 bytes) then the content of the internal buffer.
   * The stream is automatically reseted at the end of the method.
   */
  int writeTo(int fd);
  /**
   * Writes the content of the internal buffer to the specified file descriptor.
   * The stream is automatically reseted at the end of the method.
   */
  int writeDataTo(int fd);
  /**
   * Copy the content of the internal buffer in the specified buffer.
   */
  void toBuffer(byte* buf);

  /**
   * Writes a long to the stream as eight bytes, high byte first.
   */
  int writeLong(long long l);
  /**
   * Writes an int to the stream as four bytes, high byte first.
   */
  int writeInt(int i);
  /**
   * Writes a short to output stream as two bytes, high byte first.
   */
  int writeShort(short s);
  /**
   * Writes a boolean to the stream as a 1-byte value.
   */
  int writeBoolean(boolean b);
  /**
   * Writes out a byte to the stream as a 1-byte value.
   */
  int writeByte(byte b);
  /**
   * Writes a string to the stream.
   */
  int writeString(char *str);
  /**
   * Writes an array of len bytes to the stream.
   */
  int writeByteArray(byte* tab, int len);

  /**
   *
   */
  int writeFloat(float f);
  /**
   *
   */
  int writeDouble(double d);

  /**
   * Writes a vector of string to the stream.
   */
  void writeVectorOfString(Vector<char>* vector) throw(IOException);
  /**
   * Writes a Properties object to the stream.
   */
  void writeProperties(Properties* properties) throw(IOException);
};

/**
 * This class implements an input stream in which the data is read from an array of byte.
 */
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


  /**
   * Reads up to len bytes from the internal buffer to the array of bytes buf.
   * The number of read bytes is returned.
   */
  int readBuffer(byte* buf, int len);
  /**
   * Fill the buffer from the specified file descriptor.
   * The number of available bytes are first read then the buffer is filled.
   */
  int readFrom(int fd);

  /**
   * Resets the stream as empty.
   */
  void reset();
  /**
   * Returns the number of bytes in the stream.
   */
  int size();
  /**
   * Skips nb bytes.
   */
  int skip(int nb);

  /**
   * Reads eight input bytes and returns a long value.
   */
  int readLong(long long *l);
  /**
   * Reads four input bytes and returns an int value.
   */
  int readInt(int *i);
  /**
   * Reads two input bytes and returns a short value.
   */
  int readShort(short *s);
  /**
   * Reads one input byte and returns true if that byte is nonzero, false if that byte is zero.
   */
  int readBoolean(boolean *b);
  /**
   * Reads and returns one input byte.
   */
  int readByte(byte *b);
  /**
   * Reads in a string.
   */
  int readString(char **str);
  /**
   * Reads the length of the array then reads the corresponding bytes and stores them into the byte array tab.
   */
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

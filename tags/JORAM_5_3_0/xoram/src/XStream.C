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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
/* pour ntoh, hton fonctions */
#include <netinet/in.h>

#include "XStream.H"

/**
 * Writes len bytes from array buf to the internal buffer of the stream.
 */
int OutputStream::writeBuffer(byte* buf, int len) {
  int newcount = count + len;
  if (newcount > length) {
    byte* newbuf;
    int newlength = length*2;
    if (newcount > newlength)
      newlength = newcount;

    newbuf = new byte[newlength];
    if (newbuf == NULL) {
      perror("writeBuffer - new byte[]");
      return -1;
    }
    memcpy(newbuf, buffer, count);
    delete[] buffer;
    buffer = newbuf;
    length = newlength;
  }
  memcpy(buffer+count, buf, len);
  count += len;

  return len;
}

OutputStream::OutputStream() {
  if(DEBUG)
    printf("=> OutputStream():\n");
  count = 4;
  length = 256;
  buffer = new byte[length]; //(byte*) malloc(length);
  if(DEBUG)
    printf("<= OutputStream(): buffer = 0x%x\n", buffer);
}

OutputStream::~OutputStream() {
  if(DEBUG)
    printf("~OutputStream(): buffer = 0x%x\n", buffer);
  //free(buffer);
  if (buffer != (byte*) NULL) {
    delete[] buffer;
    buffer = (byte*) NULL;
    length = 0;
  }
}

void OutputStream::reset() {
  count = 4;
}

int OutputStream::size() {
  return count -4;
}

/**
 * Writes the content of this stream to the specified file descriptor.
 * This method first writes the size of the stream (4 bytes) then the content of the internal buffer.
 * The stream is automatically reseted at the end of the method.
 */
int OutputStream::writeTo(int fd) {
  *((int*) buffer) = htonl(size());
  if (write(fd, buffer, count) != count) {
    perror("writeTo");
    return -1;
  }
  reset();
  return 0;
}

/**
 * Writes the content of the internal buffer to the specified file descriptor.
 * The stream is automatically reseted at the end of the method.
 */
int OutputStream::writeDataTo(int fd) {
  if (write(fd, buffer+4, count-4) != count-4) {
    perror("writeDataTo");
    return -1;
  }
  reset();
  return 0;
}

/**
 * Copy the content of the internal buffer in the specified buffer.
 */
void OutputStream::toBuffer(byte* buf) {
  memcpy(buf, buffer+4, count-4);
}

/**
 * Writes a long to the stream as eight bytes, high byte first.
 */
int OutputStream::writeLong(long long l) {
  int x1 = htonl((int) ((l >> 32) & 0xFFFFFFFFL));
  int x2 = htonl((int) (l & 0xFFFFFFFFL));
  if ((writeBuffer((byte*) &x1, 4) != 4) || (writeBuffer((byte*) &x2, 4) != 4)) {
    perror("writeLong");
    return -1;
  }
  return 0;
}

/**
 * Writes an int to the stream as four bytes, high byte first.
 */
int OutputStream::writeInt(int i) {
  int x = htonl(i);
  if (writeBuffer((byte*) &x, 4) != 4) {
    perror("writeInt");
    return -1;
  }
  return 0;
}

/**
 * Writes a short to output stream as two bytes, high byte first.
 */
int OutputStream::writeShort(short s) {
  short x = htons(s);
  if (writeBuffer((byte*) &x, 2) != 2) {
    perror("writeShort");
    return -1;
  }
  return 0;
}

/**
 * Writes a boolean to the stream as a 1-byte value.
 */
int OutputStream::writeBoolean(boolean b) {
  if (b)
    return writeByte((byte) S_TRUE);
  else
    return writeByte((byte) S_FALSE);
}

/**
 * Writes out a byte to the stream as a 1-byte value.
 */
int OutputStream::writeByte(byte b) {
  if ((count + 1) > length) {
    byte* newbuf = new byte[length*2];
    if (newbuf == NULL) {
      perror("writeByte - new byte[]");
      return -1;
    }
    memcpy(newbuf, buffer, count);
    delete[] buffer;
    buffer = newbuf;
    length = length *2;
  }
  buffer[count] = b;
  count += 1;

  return 1;
}

/**
 * Writes a string to the stream.
 */
int OutputStream::writeString(char *str) {
  int len;

  if (str == NULL)
    return writeInt(-1);
  len = strlen(str);
  if (writeInt(len) == -1)
    return -1;
  return writeBuffer((byte*) str, len);
}

/**
 * Writes an array of len bytes to the stream.
 */
int OutputStream::writeByteArray(byte* tab, int len) {
  if (tab == NULL) {
    return writeInt(-1);
  } else if (len <= 0) {
    return writeInt(0);
  } else {
    if (writeInt(len) == -1)
      return -1;
    return writeBuffer(tab, len);
  }
}

int OutputStream::writeFloat(float f) {
  return writeInt(* ((int *) &f));
}

int OutputStream::writeDouble(double d) {
  return writeLong(* ((long long *) &d));
}

/**
 * Writes a vector of string to the stream.
 */
void OutputStream::writeVectorOfString(Vector<char>* vector) throw(IOException) {
  if (vector == (Vector<char>*) NULL) {
    if (writeInt(-1) == -1) throw IOException();
  } else {
/*     if (writeInt(vector->capacity()) == -1) throw IOException(); */
    int count = vector->size();
    if (writeInt(count) == -1) throw IOException();
    for (int i=0; i<count; i++) {
      if (writeString(vector->elementAt(i)) == -1) throw IOException();
    }
  }
}

/**
 * Writes a Properties object to the stream.
 */
void OutputStream::writeProperties(Properties* properties) throw(IOException) {
  if (properties == (Properties*) NULL) {
    if (writeInt(-1) == -1) throw IOException();
  } else {
    if (writeInt(properties->size()) == -1) throw IOException();
    properties->writePropertiesTo(this);
  }
}

/**
 * Reads up to len bytes from the internal buffer to the array of bytes buf.
 * The number of read bytes is returned.
 */
int InputStream::readBuffer(byte* buf, int len) {
  if (buf == NULL) return -1;

  if (pos >= count) return -1;

  if (pos + len > count)
    len = count - pos;

  if (len <= 0) return 0;

  memcpy(buf, buffer+pos, len);
  pos += len;
  return len;
}

// Attention le buffer doit avoir ete alloue avec new (pas malloc).
InputStream::InputStream(byte* buffer, int length, int count) {
   this->buffer = buffer;
   this->length = length;
   this->count = count;
   pos = 0;
}

InputStream::InputStream() {
  if(DEBUG)
    printf("=> InputStream():\n");
  buffer = new byte[256];//(byte*) malloc(256);
  length = 256;
  count = 0;
  pos = 0;
  if(DEBUG)
    printf("<= InputStream(): buffer = 0x%x\n", buffer);
}

InputStream::~InputStream() {
  if(DEBUG)
    printf("~InputStream(): buffer = 0x%x\n", buffer);
  if (length > 0) {
    //free(buffer);
    delete[] buffer;
    buffer = (byte*) NULL;
    length = 0;
  }
}

/**
 * Resets the stream as empty.
 */
void InputStream::reset() {
  pos = 0;
}

/**
 * Returns the number of bytes in the stream.
 */
int InputStream::size() {
  return count;
}

/**
 * Fill the buffer from the specified file descriptor.
 * The number of available bytes are first read then the buffer is filled.
 */
int InputStream::readFrom(int fd) {
  int buf, len;
  if (read(fd, &buf, 4) != 4)  {
    if(DEBUG)
      printf("readFrom");
    //perror("readFrom");
    return -1;
  }
  len = ntohl(buf);
  if (len > length) {
    delete[] buffer;
    buffer = new byte[len];
    if (buffer == NULL) {
      if(DEBUG)
        printf("readFrom - new byte[]");
      //perror("readFrom - new byte[]");
      return -1;
    }
    length = len;
  }
  if (read(fd, buffer, len) != len)  {
    if(DEBUG)
      printf("readFrom: len");
    //perror("readFrom: len");
    count = 0;
    return -1;
  }
  pos = 0;
  count = len;

  return 0;
}

/**
 * Reads eight input bytes and returns a long value.
 */
int InputStream::readLong(long long *l) {
  long long x1, x2;

  if (pos +8 > count) return -1;

  x1 = (((long long) ntohl(*(int*)(buffer+pos))) & 0xFFFFFFFFL);
  x2 = (((long long) ntohl(*(int*)(buffer+pos+4))) & 0xFFFFFFFFL);
  *l = (x1 << 32) | x2;
  pos += 8;

  return 0;
}

/**
 * Reads four input bytes and returns an int value.
 */
int InputStream::readInt(int *i) {
  if (pos +4 > count) return -1;

  *i = ntohl(*(int*)(buffer+pos));
  pos += 4;

  return 0;
}

/**
 * Reads two input bytes and returns a short value.
 */
int InputStream::readShort(short *s) {
  if (pos +2 > count) return -1;

  *s = ntohs(*(short*)(buffer+pos));
  pos += 2;

  return 0;
}

/**
 * Reads one input byte and returns true if that byte is nonzero, false if that byte is zero.
 */
int InputStream::readBoolean(boolean *b) {
  if (pos +1 > count) return -1;

  *b = (byte) (buffer[pos] == ((byte) S_TRUE));
  pos += 1;

  return 0;
}

/**
 * Reads and returns one input byte.
 */
int InputStream::readByte(byte *b) {
  if (pos +1 > count) return -1;

  *b = (byte) (buffer[pos] & ((byte) 0xff));
  pos += 1;

  return 0;
}

/**
 * Reads in a string.
 */
int InputStream::readString(char **str) {
  int len;
  char* buf = (char*) NULL;

  if (readInt(&len) == -1)
    return -1;

  if (len == -1) {
    *str = NULL;
    return 0;
  }

  buf = new char[len+1];//(char*) malloc(len+1);
  if (readBuffer((byte*) buf, len) == -1) {
    delete[] buf;
    return -1;
  }
  buf[len] = '\0';
  if(DEBUG)
    printf("XStream: buf = 0x%x, size = %i\n", buf, strlen(buf));

  *str = buf;
  return 0;
}

/**
 * Reads the length of the array then reads the corresponding bytes and stores them into the byte array tab.
 */
int InputStream::readByteArray(byte** tab) {
  int len;

  if (readInt(&len) == -1) return -1;

  if (len <= 0) {
    *tab = NULL;
    return 0;
  }

  *tab = new byte[len];//(byte*) malloc(len);
  if (readBuffer((byte*) *tab, len) == -1) {
    //free(tab);
    delete[] *tab;
    return -1;
  }

  return len;
}

int InputStream::readFloat(float *f) {
  return readInt((int *) f);
}

int InputStream::readDouble(double *d) {
  return readLong((long long *) d);
}

Vector<char>* InputStream::readVectorOfString() throw(IOException) {
  int capacity;
  int count;
  char* str;

/*   if (readInt(&capacity) == -1) throw IOException(); */
/*   if (capacity <= 0) return (Vector<char>*) NULL; */

  if (readInt(&count) == -1) throw IOException();
  if (count <= 0) return (Vector<char>*) NULL;

  Vector<char>* vector = new Vector<char>(count);
  for (int i=0; i<count; i++) {
    if (readString(&str) == -1) {
      delete vector;
      throw IOException();
    }
    vector->addElement(str);
  }

  return vector;
}

Properties* InputStream::readProperties() throw(IOException) {
  int count;

  if (readInt(&count) == -1) throw IOException();
  if (count == -1) return (Properties*) NULL;

//Properties* properties = new Properties(((4*count)/3) +1); //NTA comment
  Properties* properties = new Properties();
  properties->readPropertiesFrom(this, count);

  return properties;
}

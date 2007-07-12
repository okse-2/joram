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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
/* pour ntoh, hton fonctions */
#include <netinet/in.h>

#include "XStream.H"

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

int OutputStream::writeTo(int fd) {
  *((int*) buffer) = htonl(size());
  if (write(fd, buffer, count) != count) {
    perror("writeTo");
    return -1;
  }
  reset();
  return 0;
}

void OutputStream::toBuffer(byte* buf) {
  memcpy(buf, buffer+4, count-4);
}

int OutputStream::writeLong(long long l) {
  int x1 = htonl((int) ((l >> 32) & 0xFFFFFFFFL));
  int x2 = htonl((int) (l & 0xFFFFFFFFL));
  if ((writeBuffer((byte*) &x1, 4) != 4) || (writeBuffer((byte*) &x2, 4) != 4)) {
    perror("writeLong");
    return -1;
  }
  return 0;
}

int OutputStream::writeInt(int i) {
  int x = htonl(i);
  if (writeBuffer((byte*) &x, 4) != 4) {
    perror("writeInt");
    return -1;
  }
  return 0;
}

int OutputStream::writeShort(short s) {
  short x = htons(s);
  if (writeBuffer((byte*) &x, 2) != 2) {
    perror("writeShort");
    return -1;
  }
  return 0;
}

int OutputStream::writeBoolean(boolean b) {
  if (b)
    return writeByte((byte) S_TRUE);
  else
    return writeByte((byte) S_FALSE);
}

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

int OutputStream::writeString(char *str) {
  int len;

  if (str == NULL)
    return writeInt(-1);
  len = strlen(str);
  if (writeInt(len) == -1)
    return -1;
  return writeBuffer((byte*) str, len);
}

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

void OutputStream::writeProperties(Properties* properties) throw(IOException) {
  if (properties == (Properties*) NULL) {
    if (writeInt(-1) == -1) throw IOException();
  } else {
    if (writeInt(properties->size()) == -1) throw IOException();
    properties->writePropertiesTo(this);
  }
}

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

void InputStream::reset() {
  pos = 0;
}

int InputStream::size() {
  return count;
}

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

int InputStream::readLong(long long *l) {
  long long x1, x2;
  
  if (pos +8 > count) return -1;

  x1 = (((long long) ntohl(*(int*)(buffer+pos))) & 0xFFFFFFFFL);
  x2 = (((long long) ntohl(*(int*)(buffer+pos+4))) & 0xFFFFFFFFL);
  *l = (x1 << 32) | x2;
  pos += 8;

  return 0;
}

int InputStream::readInt(int *i) {
  if (pos +4 > count) return -1;

  *i = ntohl(*(int*)(buffer+pos));
  pos += 4;

  return 0;
}

int InputStream::readShort(short *s) {
  if (pos +2 > count) return -1;

  *s = ntohs(*(short*)(buffer+pos));
  pos += 2;

  return 0;
}

int InputStream::readBoolean(boolean *b) {
  if (pos +1 > count) return -1;

  *b = (byte) (buffer[pos] == ((byte) S_TRUE));
  pos += 1;

  return 0;
}

int InputStream::readByte(byte *b) {
  if (pos +1 > count) return -1;

  *b = (byte) (buffer[pos] & ((byte) 0xff));
  pos += 1;

  return 0;
}

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

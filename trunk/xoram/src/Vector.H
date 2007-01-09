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
#ifndef VECTOR_H
#define VECTOR_H

#include "Types.H"
#include "XoramException.H"

class ArrayIndexOutOfBoundsException : public Exception {
 public:
  ArrayIndexOutOfBoundsException() : Exception() {}
  ArrayIndexOutOfBoundsException(char* msg) : Exception(msg) {}
};

template<class T> class Vector {
 private:
  T** data;
  int length;
  int count;

  void init(int capacity) {
    if (capacity < 0) throw IllegalArgumentException();

    data = (T**) NULL;
    if (capacity > 0) {
      data = new T* [capacity];
    }
    length = capacity;
    count = 0;
  }

 public:
  Vector() {
    init(10);
  }

  Vector(int capacity) {
    init(capacity);
  }

  ~Vector() {
    if (length > 0) delete data;
  }

  int capacity() {
    return length;
  }

  int size() {
    return count;
  }

  void clear() {
    for (int i=0; i<count; i++)
      data[i] = (T*) NULL;
    count = 0;
  }

  void addElement(T* element) {
    if (length == 0) {
      length = 10;
      this->data = new T* [length]; 
    } else if (count == length) {
      T** newData = new T* [2* length];
      for (int i=0; i<count; i++)
        newData[i] = data[i];
      delete data;
      length = 2* length;
      data = newData;
    }
    data[count++] = element;
  }

  int indexOf(T* element){
    for (int i=0; i<count; i++) {
      if (data[i] == element) return i;
    }
    return -1;
  }

  T* elementAt(int index) {
    if (index >= count) throw ArrayIndexOutOfBoundsException();
    return data[index];
  }

  T* removeElementAt(int index) {
    if (index >= count) throw ArrayIndexOutOfBoundsException();
    T* obj = data[index];

    for (int i=index; i<(count -1); i++) 
      data[i] = data[i+1];
    count--;
    data[count] = (T*) NULL;

    return obj;
  }

  boolean removeElement(T* element) {
    int i = indexOf(element);
    if (i >= 0) {
      removeElementAt(i);
      return true;
    }
    return false;
  }
};

#endif /* VECTOR_H */

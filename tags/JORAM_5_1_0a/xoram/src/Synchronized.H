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
#ifndef SYNCHRONIZED_H
#define SYNCHRONIZED_H

#include <pthread.h>
#include "Types.H"

class InterruptedException : public Exception {
 public:
  InterruptedException() : Exception() {}
  InterruptedException(char* msg) : Exception(msg) {}
};

class Synchronized {
 private:
  pthread_mutex_t mutex;
  pthread_cond_t cond;

 public:
  Synchronized();
  ~Synchronized();

  void sync_begin();

  void wait();
  void wait(long timeout);
  void notify();
  void notifyAll();

  void sync_end();
};

#endif /* SYNCHRONIZED_H */

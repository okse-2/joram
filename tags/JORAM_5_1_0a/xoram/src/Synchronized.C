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
#include <pthread.h>
#include "Synchronized.H"

Synchronized::Synchronized() {
  /* Initialize mutex and condition variable objects */
  pthread_mutex_init(&mutex, NULL);
  pthread_cond_init (&cond, NULL);
}

Synchronized::~Synchronized() {
  pthread_mutex_destroy(&mutex);
  pthread_cond_destroy(&cond);
}

void Synchronized::sync_begin() {
  pthread_mutex_lock(&mutex);
}

void Synchronized::wait() {
  pthread_cond_wait(&cond, &mutex);
}

void Synchronized::wait(long timeout) {
  struct timespec ts;

  if (timeout == 0) {
    wait();
  } else {
    clock_gettime(CLOCK_REALTIME, &ts);
    ts.tv_sec += (timeout /1000);
    ts.tv_nsec += ((timeout %1000) *1000000);
    /*     if (pthread_cond_timedwait(&cond, &mutex, &ts) == ETIMEDOUT) { */
    /*       printf("timeout\n"); */
    /*     }; */
    pthread_cond_timedwait(&cond, &mutex, &ts);
  }
}

void Synchronized::notify() {
  pthread_cond_signal(&cond);
}

void Synchronized::notifyAll() {
  pthread_cond_broadcast(&cond);
}

void Synchronized::sync_end() {
  pthread_mutex_unlock(&mutex);
}

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
#include <pthread.h>

#include "Daemon.H"

Daemon::Daemon() {
  running = FALSE;
}

Daemon::~Daemon() {}

boolean Daemon::isRunning() {
  return running;
}

void Daemon::finish() {
  running = false;
  close();
  pthread_exit(0);
}

void* Daemon::main(void* par) {
  Daemon* daemon = (Daemon*) par;
  daemon->run();
  pthread_exit(0);
}

void Daemon::start() {
  pthread_attr_t attr;

  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
  running = TRUE;
  pthread_create(&thread, &attr, Daemon::main, (void *)this);
  pthread_attr_destroy(&attr);
}

void Daemon::stop() {
  running = false;
  close();
}

void Daemon::join() {
  pthread_join(thread, (void **)&status);
}

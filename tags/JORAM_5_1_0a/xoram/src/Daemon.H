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
#ifndef DAEMON_H
#define DAEMON_H

#include "Types.H"

class Daemon {
 private:
  pthread_t thread;

  static void* main(void* par);

 protected:
  volatile boolean running;
  volatile boolean canStop;
  volatile int status;

  virtual void run() =0;
  virtual void close() =0;
  void finish();

 public:
  Daemon();
  ~Daemon();

  boolean isRunning();
  
  void start();
  void stop();
  void join();
};

#endif /* DAEMON_H */

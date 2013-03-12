/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

public class FlowControl {
  /**  Flow control. */
  private static Object lock = new Object();
  private static int inFlow = 
      org.objectweb.joram.mom.proxies.ConnectionManager.inFlow;
  private static long flowControl = 0;
  private static long start = 0L;
  private static long end = 0L;
  private static int nbmsg = 0;

  public static void flowControl() {
    // Flow control: gives the opportunity to other threads to consume waiting messages.
    synchronized (lock) {
      if (start == 0L) start = System.currentTimeMillis();
      nbmsg += 1;
      if (nbmsg == inFlow) {
        end = System.currentTimeMillis();
        flowControl = 1000L - (end - start);
        if (flowControl > 0) {
          try {
            Thread.sleep(flowControl);
          } catch (InterruptedException exc) {}
          start = System.currentTimeMillis();
        } else {
          start = end;
        }
        nbmsg = 0;
      }
    }
  }
}

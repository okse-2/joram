/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001-2003 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */

package a3.base;

import java.io.*;
import java.net.*;

import fr.dyade.aaa.agent.AdminProxy;

public class askiller {
  public static void main(String args[]) throws Exception {
    int port = -1;

    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception exc) {
      System.err.println("usage: AdminProxy port as integer.");
    }

    Socket socket = null;

    try {
      socket = new Socket("localhost", port);
      socket.getOutputStream().write(AdminProxy.CRASH_SERVER.getBytes());
      socket.getOutputStream().write('\n');
      socket.getOutputStream().flush();
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      System.err.println("Can't crash server.");
      exc.printStackTrace();
    } finally {
      try {
        socket.getInputStream().close();
      } catch (Exception exc) {}
      try {
        socket.getOutputStream().close();
      } catch (Exception exc) {}
      try {
        socket.close();
      } catch (Exception exc) {}
    }
    socket = null;
  }
}

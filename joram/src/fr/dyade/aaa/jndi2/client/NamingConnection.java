/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.client;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.jndi2.msg.*;

public class NamingConnection {

  private String hostName;

  private int port;

  private Socket socket;

  private SerialOutputStream sender;

  private ObjectInputStream receiver;

  public NamingConnection(String hostName, int port) {
    this.hostName = hostName;
    this.port = port;
    this.socket = null;
    this.sender = null;
    this.receiver = null;
  }

  public final String getHostName() {
    return hostName;
  }

  public final int getPort() {
    return port;
  }

  /**
   * An invoke opens a connection and closes it 
   * when the result has been returned. The overhead
   * of the connection opening could be avoided
   * if the server could close connections. Such a
   * protocol would change the client as well.
   */
  public synchronized JndiReply invoke(JndiRequest request) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "NamingConnection.invoke(" + request + ')');
    open();
    try {
      sender.writeObject(request);
      return (JndiReply)receiver.readObject();
    } catch (IOException ioe) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR, "NamingConnection.receive()", ioe);
      NamingException ne = new NamingException(ioe.getMessage());
      ne.setRootCause(ioe);
      throw ne;
    } catch (ClassNotFoundException cnfe) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR, "NamingConnection.receive()", cnfe);
      NamingException ne = new NamingException(cnfe.getMessage());
      ne.setRootCause(cnfe);
      throw ne;
    } finally {
      close();
    }
  }

  private void open() throws NamingException {
    try {
      socket = new Socket(hostName, port);
      sender = new SerialOutputStream(socket.getOutputStream());
      receiver = new ObjectInputStream(socket.getInputStream());
    } catch (IOException exc) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR, "NamingConnection.open()", exc);
      NamingException exc2 = new NamingException(exc.getMessage());
      exc2.setRootCause(exc);
      throw exc2;
    }
  }

  private void close() throws NamingException {
    try {
      if (sender != null)
        sender.close();
      if (receiver != null)
        receiver.close();
      if (socket != null)
        socket.close();
      socket = null;
      sender = null;
      receiver = null;
    } catch (IOException exc) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR, "NamingConnection.close()", exc);
      NamingException exc2 = new NamingException(exc.getMessage());
      exc2.setRootCause(exc);
      throw exc2;
    }
  }

  Hashtable getEnvironment() {
    Hashtable env = new Hashtable();
    env.put("java.naming.provider.host", hostName);
    env.put("java.naming.provider.port", "" + port);
    return env;
  }

  public String toString() {
    return '(' + super.toString() +
      ",hostname=" + hostName +
      ",port=" + port + ')';
  }

}

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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.client;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.jndi2.msg.*;

public class SimpleNamingConnection 
    implements NamingConnection {

  private String hostName;

  private int port;

  private Hashtable env;

  private IOControl ioCtrl;

  public SimpleNamingConnection(String hostName, 
                                int port,
                                Hashtable env) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "SimpleNamingConnection.<init>(" + 
                       hostName + ',' + 
                       port + ',' + 
                       env + ')');
    this.hostName = hostName;
    this.port = port;
    this.env = env;
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
      ioCtrl.writeObject(request);
      return (JndiReply)ioCtrl.readObject();
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
      ioCtrl.close();
    }
  }

  private static String TIMEOUT_PROPERTY =
      "fr.dyade.aaa.jndi2.client.SimpleNamingConnection.timeout";

  private void open() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "SimpleNamingConnection.open()");
    try {
      int timeout = Integer.getInteger(TIMEOUT_PROPERTY, 0).intValue();
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(hostName, port), timeout);
      ioCtrl = new IOControl(socket);
    } catch (IOException exc) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "NamingConnection.open()", exc);
      NamingException exc2 = new NamingException(exc.getMessage());
      exc2.setRootCause(exc);
      throw exc2;
    }
  }

  public Hashtable getEnvironment() {
    return env;
  }

  public NamingConnection cloneConnection() {
    return new SimpleNamingConnection(hostName, port, env);
  }

  public String toString() {
    return '(' + super.toString() +
      ",hostname=" + hostName +
      ",port=" + port + 
      ",env=" + env + ')';
  }
}

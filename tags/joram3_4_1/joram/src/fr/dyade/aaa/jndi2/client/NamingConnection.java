/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

class NamingConnection {

  private String hostName;

  private int port;

  private Socket socket;

  private SerialOutputStream sender;

  private ObjectInputStream receiver;

  NamingConnection(String hostName, int port) {
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

}

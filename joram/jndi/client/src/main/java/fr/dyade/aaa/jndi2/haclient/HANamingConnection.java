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
package fr.dyade.aaa.jndi2.haclient;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.client.NamingConnection;
import fr.dyade.aaa.jndi2.client.Trace;
import fr.dyade.aaa.jndi2.msg.BindRequest;
import fr.dyade.aaa.jndi2.msg.IOControl;
import fr.dyade.aaa.jndi2.msg.JndiReadRequest;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;

public class HANamingConnection implements NamingConnection {

  public static final int IDEMPOTENT = -2;
  public static final int NOT_IDEMPOTENT = -1;

  public static boolean isIdempotent(JndiRequest request) {
    if (request instanceof JndiReadRequest) return true;
    if (request instanceof BindRequest) {
      BindRequest br = (BindRequest)request;
      return br.isRebind();
    }
    return false;
  }

  private Vector addresses;

  private IOControl ioCtrl;

  private int id;

  public HANamingConnection() {
    addresses = new Vector();
    id = -1;
  }

  public void addServerAddress(String host, int port) {
    addresses.addElement(new ServerAddress(host, port));
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
                       "HANamingConnection.invoke(" + request + ')');
    while (true) {
      open();      
      try {
        if (id < 0) {
          if (isIdempotent(request)) {
            if (Trace.logger.isLoggable(BasicLevel.DEBUG))
              Trace.logger.log(BasicLevel.DEBUG, 
                               " -> write idempotent");
            ioCtrl.writeInt(IDEMPOTENT);
          } else {
            if (Trace.logger.isLoggable(BasicLevel.DEBUG))
              Trace.logger.log(BasicLevel.DEBUG, 
                               " -> write not idempotent");
            ioCtrl.writeInt(NOT_IDEMPOTENT);            
            id = ioCtrl.readInt();
            if (Trace.logger.isLoggable(BasicLevel.DEBUG))
              Trace.logger.log(BasicLevel.DEBUG, 
                               " -> receive new request id = " + id);
          }
        } else {
          ioCtrl.writeInt(id);
        }
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
          Trace.logger.log(BasicLevel.DEBUG, 
                           " -> send request");
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
        close();
      }
    }
  }

  private void open() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "HANamingConnection.open()");
    int i = 0;
    while (i < addresses.size()) {
      ServerAddress sa = (ServerAddress)addresses.elementAt(0);
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, 
                         " -> try connection " + sa);
      try {
        Socket socket = new Socket(sa.hostName, sa.port);
        ioCtrl = new IOControl(socket);
        return;
      } catch (IOException exc) {
        if (Trace.logger.isLoggable(BasicLevel.WARN))
          Trace.logger.log(BasicLevel.WARN, "NamingConnection.open()", exc);
        // Put the faulty address at the end of the list
        addresses.removeElementAt(0);
        addresses.addElement(sa);
      }
      i++;
    }    
    NamingException exc2 = 
      new NamingException("Connection failed with all replicas: " + 
                          addresses);
    throw exc2;
  }

  private void close() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "HANamingConnection.close()");
    ioCtrl.close();
  }

  public NamingConnection cloneConnection() {
    HANamingConnection clone = new HANamingConnection();
    clone.addresses = (Vector)addresses.clone();
    return clone;
  }

  public String toString() {
    return '(' + super.toString() +
      ",addresses=" + addresses + ')';
  }

  public Hashtable getEnvironment() {
    Hashtable env = new Hashtable();
    return env;
  }

  static class ServerAddress {
    String hostName;
    int port;

    public ServerAddress(String hostName, int port) {
      this.hostName = hostName;
      this.port = port;
    }

    public String toString() {
      return '(' + super.toString() +
        ",hostName=" + hostName + 
        ",port=" + port + ')';
    }
  }
}

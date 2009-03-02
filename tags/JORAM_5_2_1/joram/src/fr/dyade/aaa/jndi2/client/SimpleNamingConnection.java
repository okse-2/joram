/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;

import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.msg.IOControl;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;
import fr.dyade.aaa.util.SocketFactory;

public class SimpleNamingConnection implements NamingConnection {
  /**
   *  Allows to define a specific factory for socket in order to by-pass
   * compatibility problem between JDK version.
   *  Currently there is two factories, The default factory one for JDK
   * since 1.4, and "fr.dyade.aaa.util.SocketFactory13" for JDK prior to 1.4.
   *  This value can be adjusted by setting
   * <code>fr.dyade.aaa.jndi2.client.SocketFactory</code> property.
   */
  SocketFactory socketFactory = null;

  /**
   * Name of the property that allow to define the SocketFactory class used for
   * NamingConnection.
   */
  final static String SOCKET_FACTORY_PROPERTY = "fr.dyade.aaa.jndi2.client.SocketFactory";

  /**
   *  Defines in milliseconds the timeout used during socket connection.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 0.
   *  This value can be adjusted by setting
   * <code>fr.dyade.aaa.jndi2.client.ConnectTimeout</code> property.
   * <p>
   */
  int connectTimeout = 0;

  /**
   * Name of the property that allow the configuration of the timeout
   * during connect (by default 0, infinite timeout).
   */ 
  final static String TIMEOUT_PROPERTY = "fr.dyade.aaa.jndi2.client.ConnectTimeout";

  protected String hostName;

  protected int port;

  protected Hashtable env;

  protected IOControl ioCtrl;

  public SimpleNamingConnection() {}

  public SimpleNamingConnection(String hostName, int port, Hashtable env) {
    init(hostName, port, env);
  }

  public void init(String hostName, int port, Hashtable env) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "SimpleNamingConnection.init(" + hostName + ',' + port + ',' + env + ')');
    
    this.hostName = hostName;
    this.port = port;
    this.env = env;

    connectTimeout = Integer.getInteger(TIMEOUT_PROPERTY, connectTimeout).intValue();

    String sfcn = System.getProperty(SOCKET_FACTORY_PROPERTY, SocketFactory.DefaultFactory);
    socketFactory = SocketFactory.getFactory(sfcn);
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
                       "SimpleNamingConnection.invoke(" + request + ')');
    
    open();
    try {
      ioCtrl.writeObject(request);
      return (JndiReply)ioCtrl.readObject();
    } catch (IOException ioe) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR,
                         "SimpleNamingConnection.receive()", ioe);
      NamingException ne = new NamingException(ioe.getMessage());
      ne.setRootCause(ioe);
      throw ne;
    } catch (ClassNotFoundException cnfe) {
      if (Trace.logger.isLoggable(BasicLevel.ERROR))
        Trace.logger.log(BasicLevel.ERROR,
                         "SimpleNamingConnection.receive()", cnfe);
      NamingException ne = new NamingException(cnfe.getMessage());
      ne.setRootCause(cnfe);
      throw ne;
    } finally {
      ioCtrl.close();
    }
  }

  private void open() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG,
                       "SimpleNamingConnection.open()");
    try {
      InetAddress addr = InetAddress.getByName(hostName);
      Socket socket = socketFactory.createSocket(addr, port, connectTimeout);
      ioCtrl = new IOControl(socket);
    } catch (IOException exc) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG,
                         "SimpleNamingConnection.open()", exc);
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

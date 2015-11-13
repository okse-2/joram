/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.kjoram;


public class TcpConnectionFactory {
  public String dfltHost;
  public int dfltPort;
  public String dfltLogin = "anonymous";
  public String dfltPassword = "anonymous";
  
  /**
   * default connection factory with localhost and 16010.
   */
  public TcpConnectionFactory() {
    dfltHost = "localhost";
    dfltPort = 16010;
  }

  /**
   * @param host
   * @param port
   */
  public TcpConnectionFactory(String host, int port) {
    dfltHost = host;
    dfltPort = port;
  }

  /**
   * Creates a connection with the specified user identity.
   * 
   * @return Connection.
   * @throws JoramException
   */
  public Connection createConnection() throws JoramException {
    return createConnection(dfltLogin, dfltPassword, dfltHost, dfltPort);
  }
   
  /**
   * Creates a connection with the specified user identity.
   * 
   * @param user
   * @param pass
   * @return Connection.
   * @throws JoramException
   */
  public Connection createConnection(String user, String pass) throws JoramException {
    return createConnection(user, pass, dfltHost, dfltPort);
  }
 
  /**
   * Creates a connection to the specified server.
   * 
   * @param host
   * @param port
   * @return Connection.
   * @throws JoramException
   */
  public Connection createConnection(String host, int port) throws JoramException {
    return createConnection(dfltLogin, dfltPassword, host, port);
  }
 
  /**
   *  Creates a connection to the specified server with the specified
   *  user identity.
   *  
   * @param user
   * @param pass
   * @param host
   * @param port
   * @return Connection.
   * @throws JoramException
   */
  public Connection createConnection(String user, String pass, String host, int port) throws JoramException {
    return new Connection(new TcpChannel(user, pass, host, port));
  }

}

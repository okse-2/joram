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
 * Initial developer(s): Andre Freyssinet (ScalAgent)
 * Contributor(s): Frederic Maistre (INRIA)
 */
package fr.dyade.aaa.mom.proxies.tcp;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.NotificationInputStream;
import fr.dyade.aaa.agent.NotificationOutputStream;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.dest.AdminTopicImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * A <code>ConnectionFactory</code> proxy is started as a service in each
 * MOM agent server for allowing connections with external clients; this
 * class is also the super class of the MOM TCP proxy agents directly
 * connected to their specific external clients and acting as their MOM
 * representants. 
 */
public class ConnectionFactory extends fr.dyade.aaa.ip.TcpMultiServerProxy
{
  /**
   * The listen socket of the proxy is statically
   * created (@see init).
   */
  private static ServerSocket serverSocket;
  
  /**
   * Overrides the default behavior that creates a
   * server socket. The socket is statically created
   * (@see init).
   */
  protected ServerSocket getServerSocket() {
    return serverSocket;
  }

  /** Default port the <code>ConnectionFactory</code> service listens to. */
  static final int defaultPort = 16010;

  /** Initial JMS administrator's name of the local server. */
  private String initialAdminName;
  /** Initial JMS administrator's password of the local server. */
  private String initialAdminPass;
  /** JMS administrator's proxy identifier. */
  private AgentId adminProxyId;

  /** Incoming messages flow (msgs/s) requested, if any (-1 if none). */
  public static int inFlow = -1;

  /**
   * Constructs a <code>ConnectionFactory</code> listening to a given port.
   * <p>
   * This constructor is the one called for instanciating the
   * <code>ConnectionFactory</code> service.
   *
   * @param port  Port to listen to.
   */
  public ConnectionFactory(int port) throws Exception
  {
    super(port);
    super.newClient = false;
   
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a <code>ConnectionFactory</code> not listening to a port.
   * <p>
   * This constructor is the one called by sub classes for constructing
   * a <code>ConnectionFactory</code> proxy waiting to be connected by its
   * server's <code>ConnectionFactory</code> service.
   */ 
  protected ConnectionFactory() {
    super();
    super.newClient = false;
  }

  /**
   * Constructs a <code>ConnectionFactory</code> listening to a given port
   * and giving an administrator access.
   *
   * @param port  Port to listen to.
   * @param name  Initial name of the administrator.
   * @param pass  Initial password of the administrator.
   * @param proxyId  Identifier of the administrator's proxy.
   */
  private ConnectionFactory(int port, String name, String pass,
                            AgentId proxyId) throws Exception
  {
    this(port);
    initialAdminName = name;
    initialAdminPass = pass;
    adminProxyId = proxyId;
  }

  /** Returns a String images of a ConnectionFactory. */
  public String toString()
  {
    return "ConnectionFactory:" + this.getId();
  }

  
  /**
   * Initializes the <code>ConnectionFactory</code> as a service, and
   * if requested, creates and deploys a <code>JmsProxy</code> proxy for
   * an administrator client.
   *
   * @param args  Port parameter from the configuration file.
   * @param firstTime  <code>true</code> when the agent server starts.
   * @exception Exception  Thrown when processing the String argument
   *              or in case of a problem when deploying the ConnectionFactory.
   */
  public static void init(String args, boolean firstTime) throws Exception
  {
    try {
      int port;
      String initialAdminName = null;
      String initialAdminPass = null;

      if (args != null) {
        StringTokenizer st = new StringTokenizer(args);

        port = Integer.parseInt(st.nextToken());
      
        if (st.hasMoreTokens()) {
          initialAdminName = st.nextToken();
          initialAdminPass = st.nextToken();
        }
        if (st.hasMoreTokens())
          inFlow = Integer.parseInt(st.nextToken());
      }
      else
        port = defaultPort;

      // Create the socket here in order to throw an exception
      // if the socket can't be created (even if firstTime is false).
      serverSocket = new ServerSocket(port);

      if (! firstTime)
        return;

      ConnectionFactory cF;

      // This service must give an access to an administrator:
      if (initialAdminName != null) {
        JmsProxy adminProxy = new JmsProxy(initialAdminName, initialAdminPass);
        adminProxy.deploy();
        cF = new ConnectionFactory(port, initialAdminName, initialAdminPass,
                                   adminProxy.getId());
      }
      // No administrator access is given:
      else
        cF = new ConnectionFactory(port);

      cF.deploy();
    }
    catch (NoSuchElementException exc) {
      throw new Exception("Could not parse arguments");
    }
    catch (IOException exc) {
      throw new Exception("ConnectionFactory deployment error: " + exc);
    }
  }

  /**
   * Stops the <code>ConnectionFactory</code> service.
   */ 
  public static void stopService() {
    // Do nothing
  }

  /**
   * Retrieves a proxy identifier given a String request.
   *
   * @exception Exception  In case of an invalid request or identification.
   */
  protected AgentId idFromString(String header) throws Exception
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": got request: "
                              + header);
    try {
      if (header.startsWith("USER:")) {
        StringTokenizer st = new StringTokenizer(header);
        String tmp = st.nextToken();
        String readName = st.nextToken();
        String readPass = st.nextToken();

        // If the requester is the administrator, returning the admin proxy id:
        if (initialAdminName != null && initialAdminPass != null
            && readName.equals(initialAdminName)
            && readPass.equals(initialAdminPass))
          return adminProxyId;

        return AdminTopicImpl.ref.getProxyId(readName, readPass);
      }
      else
        throw new Exception("Incorrect request read on stream.");
    }
    catch (NoSuchElementException nE) {
      throw new Exception("Incorrect request read on stream: " + nE);
    }
    catch (NullPointerException exc) {
      throw new Exception("Administration topic on server "
                          + AgentServer.getServerId()
                          + " not deployed.");
    }
  }

  /**
   * Acknowledges an outside request with a given status code and an
   * exception description if needed.
   *
   * @param status  Integer value coding a status (as 0 for success).
   * @param info  String information.
   * @exception IOException  In case of an output stream problem.
   */
  protected void acknowledgeOutsideRequest(int status, String info)
               throws IOException
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": acknowledges"
                              + " request with status " + status 
                              + " and info: " + info);
    if (info == null)
      dos.writeUTF((new Integer(status)).toString());
    else
      dos.writeUTF((new Integer(status)).toString() + " INFO: " + info);
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified InputStream
   * into a NotificationInputStream.
   * <p>
   * Not used at this level but at the sub classes'.
   */
  protected NotificationInputStream setInputFilters(InputStream in)
    throws StreamCorruptedException, IOException
  {
    throw new IOException("Filters must be set by subclasses."); 
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified OutputStream
   * into a NotificationOutputStream.
   * <p>
   * Not used at this level but at the sub classes'.
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out)
    throws IOException
  {
    throw new IOException("Filters must be set by subclasses."); 
  }
}

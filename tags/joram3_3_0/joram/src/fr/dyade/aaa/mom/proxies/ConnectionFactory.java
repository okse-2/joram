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
 * Initial developer(s): Andre Freyssinet (ScalAgent)
 * Contributor(s): Frederic Maistre (INRIA)
 */
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.dest.AdminTopic;
import fr.dyade.aaa.mom.dest.AdminTopicImpl;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>ConnectionFactory</code> proxy is started as a service in each
 * MOM agent server for allowing connections with external clients; this
 * class is also the super class of the MOM proxy agents directly connected
 * to their specific external clients and acting as their MOM representatives. 
 * <p>
 * As stated above, each agent server holds a <code>ConnectionFactory</code>
 * service listening to a given port, on which connection requests from
 * the outside are read. Those requests contain a user identification 
 * the ConnectionFactory uses to retrieve the right proxy agent to pass
 * the connection to. This latest agent is a proxy agent inheriting
 * from this class but no actively listening on a port. It is just waiting for
 * the server's ConnectionFactory service to pass a connection. This sub
 * proxy is the <code>JmsProxy</code> agent, representative of a JMS client,
 * or of a JMS administrator.
 * <p>
 * To pass the connections to the right proxies, the
 * <code>ConnectionFactory</code> accesses data contained in the local
 * <code>fr.dyade.aaa.mom.dest.AdminTopicImpl</code>.
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

  /** Returns a String images of a ConnectionFactory. */
  public String toString()
  {
    return "ConnectionFactory:" + this.getId();
  }

  /**
   * Initializes the <code>ConnectionFactory</code> as a service, creates
   * and deploys a <code>fr.dyade.aaa.dest.AdminTopic</code> topic, and
   * if requested, creates and deploys a <code>JmsProxy</code> proxy for
   * an administrator client.
   *
   * @param args  Port parameter from the configuration file.
   * @param firstTime  <code>true</code> when the agent server starts.
   * @exception Exception  Thrown when processing the String argument
   *              or in case of a problem when deploying the ConnectionFactory
   *              or the admin topic.
   */
  public static void init(String args, boolean firstTime) throws Exception
  {
    try {
      int port;
      String name = null;
      String pass = null;

      if (args != null) {
        StringTokenizer st = new StringTokenizer(args);

        port = Integer.parseInt(st.nextToken());
      
        if (st.hasMoreTokens()) {
          name = st.nextToken();
          pass = st.nextToken();
        }
      }
      else
        port = defaultPort;

      // Create the socket here in order to throw an exception
      // if the socket can't be created (even if firstTime is false).
      serverSocket = new ServerSocket(port);

      if (! firstTime)
        return;

      ConnectionFactory cF = new ConnectionFactory(port);
      cF.deploy();
      AdminTopic adminTopic = new AdminTopic();
      adminTopic.deploy();

      if (name != null) {
        JmsProxy adminProxy = new JmsProxy(name, pass, adminTopic.getId());
        adminProxy.deploy();
      }
    }
    catch (NoSuchElementException exc) {
      throw new Exception("Could not parse arguments");
    }
    catch (IOException exc) {
      throw new Exception("Could not deploy an agent");
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

        return AdminTopicImpl.ref.getProxyId(readName, readPass);
      }
      else
        throw new Exception("Incorrect request read on stream.");
    }
    catch (NoSuchElementException nE) {
      throw new Exception("Incorrect request read on stream: " + nE);
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

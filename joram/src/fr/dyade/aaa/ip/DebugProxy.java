/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;
import fr.dyade.aaa.agent.*;

/**
 * A <code>DebugProxy</code> agent provides a command line interface to
 * access to debugging functions in a running agent server.
 * <p>
 * The <code>DebugProxy</code> agent exports its connecting parameters in
 * a file named server<serverId>DebugProxy.cnx. It may be accessed from the
 * outside using a <code>telnet</code> client.
 * <p>
 * The <code>DebugProxy</code> input and output streams filters are a unique
 * <code>DebugDriver</code> object. There is only one thread running, as
 * input driver, which reads and analyses commands from the input flow,
 * and writes results synchronously onto the output flow.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		DebugDriver
 */
public class DebugProxy extends TcpProxy {
public static final String RCS_VERSION="@(#)$Id: DebugProxy.java,v 1.6 2002-01-16 12:46:47 joram Exp $";

  transient DebugDriver driver = null;

  /**
    * Creates a local agent with unknown port.
    */
  public DebugProxy() {
    super(0);
  }

  /**
    * Creates an agent to be configured.
    *
    * @param to		target agent server
    * @param name	symbolic name of this agent
    */
  public DebugProxy(short to, String name) {
    super(to, name);
    localPort = 0;
  }


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() + ")";
  }

  /**
    * Initializes the transient members of this agent.
    * This function is first called by the factory agent,
    * then by the system each time the agent server is restarted.
    *
    * @param firstTime		true when first called by the factory
    */
  protected void initialize(boolean firstTime) throws Exception {
    // initializes the command line socket
    super.initialize(firstTime);

    // registers this agent so that the client may connect
    File file = new File("server" + AgentServer.getServerId() + "DebugProxy.cnx");
    if (file.exists()) {
      // remove previous registration
      if (! file.delete())
	throw new IllegalArgumentException("cannot delete " + file.getName());
    }
    FileWriter fw = new FileWriter(file);
    fw.write(InetAddress.getLocalHost().getHostAddress());
    fw.write(':');
    fw.write(String.valueOf(listenPort));
    fw.close();

    // creates the driver object
    driver = new DebugDriver();
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * <code>InputStream</code> into a <code>NotificationInputStream</code>.
   *
   * @param in		the underlying input stream
   */
  protected NotificationInputStream setInputFilters(InputStream in) throws StreamCorruptedException, IOException {
    driver.setInputStream(in);
    return driver;
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
   *
   * @param out		the underlying output stream
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out) throws IOException {
    driver.setOutputStream(out);
    return null;
  }
}

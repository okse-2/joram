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
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;

/**
 * A <code>TransientNetworkServer</code> component resides in a transient
 * agent server and ensures the notification delivery from and to the other
 * agent servers.
 * <p>
 * Each transient agent server is subordinated to a single persistent agent
 * server.
 * The persistent agent server, declared in the configuration file, hosts
 * a <code>TransientNetworkProxy</code> component connected which the
 * <code>TransientNetworkServer</code> component connects to when it
 * initializes.
 *
 * @author	Freyssinet Andre
 *
 * @see		TransientNetworkProxy
 */
final class TransientNetworkServer implements MessageConsumer {
  /** The <code>MessageQueue</code> associated with this network component */
  MessageQueue qout;
  /**  */
  NetServerIn netServerIn = null;
  /**  */
  NetServerOut netServerOut = null;

  /**
   * Returns this <code>MessageConsumer</code>'s name.
   *
   * @return this <code>MessageConsumer</code>'s name.
   */
  public final String getName() {
    return "transient";
  }

  /**
   * Creates and initializes a new <code>TransientNetworkProxy</code>
   * component.
   */
  TransientNetworkServer() {
    qout = new MessageQueue();
    netServerIn = new NetServerIn(getName());
    netServerOut = new NetServerOut(getName());
  }

  /**
   * Insert a message in the <code>MessageQueue</code>. This method is unused
   * as this component is not persistent.
   *
   * @param msg		the message
   */
  public void insert(Message msg) {
    qout.insert(msg);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {}

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws IOException {}

  /**
   *  Adds a message in "ready to deliver" list. There is no need of stamp
   * allocation: the network link is FIFO and there is no persistancy.
   * 
   * @param msg		the message
   */
  public void post(Message msg) {
    qout.push(msg);
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    qout.validate();
  }

  /**
   * Invalidates all messages pushed in queue during transaction session.
   */
  public void invalidate() {
    qout.invalidate();
  }

  /** communication socket */
  Socket sock = null;
  /** input stream from transient agent server */
  ObjectInputStream ois = null;
  /** output stream to transient agent server */
  ObjectOutputStream oos = null;

  /**
   * Causes this component to begin execution.
   */
  public void start() throws IOException {
    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " starting.", false);

    try {
      if (isRunning())
	throw new IOException("Consumer already running.");

      // connects to the proxy server
      ServerDesc proxy = AgentServer.getServerDesc(AgentServer.getServerDesc().gateway);
      for (int i=0; ; i++) {
	try {
	  sock = new Socket(proxy.getAddr(), proxy.port);
	  break;
	} catch (IOException exc) {
	  if (i > 5) throw exc;
	  try {
	    Thread.currentThread().sleep(i * 500);
	  } catch (InterruptedException e) {}
	}
      }
      sock.setSoTimeout(0);
      sock.setTcpNoDelay(true);
      
      // sets the input and output flows
      oos = new ObjectOutputStream(sock.getOutputStream());
      ois = new ObjectInputStream(sock.getInputStream());
      // sends local configuration
      oos.writeObject(new Boot());
      oos.flush();
      oos.reset();

      netServerIn.start();
      netServerOut.start();
    } catch (IOException exc) {
      Debug.trace(getName(), exc);
      throw exc;
    }

    if (Debug.debug && Debug.network)
      Debug.trace(getName() + " started.", false);
  }

  /**
   * Forces the component to stop executing.
   */
  public void stop() {
    if (netServerIn != null) netServerIn.stop();
    if (netServerOut != null) netServerOut.stop();
  }

  /**
   * Tests if the network component is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    if ((netServerIn != null) && netServerIn.isRunning &&
	(netServerOut != null) && netServerOut.isRunning)
      return true;
    else
      return false;
  }

  public MessageQueue getQueue() {
    return qout;
  }

  /**
   * Close the connection.
   */
  public void close() {
    try {
      ois.close();
    } catch (Exception exc) {}
    ois = null;
    try {
      oos.close();
    } catch (Exception exc) {}
    oos = null;
    try {
      sock.close();
    } catch (Exception exc) {}
    sock = null;
  }

  final class NetServerIn extends Daemon {
    NetServerIn(String name) {
      super(name + ".netServerIn");
    }

    void shutdown() {
      close();
    }

    public void run() {
      Message msg = null;

      try {
	while (isRunning) {
	  canStop = true;

	  try {
	    if (Debug.debug && Debug.message)
	      Debug.trace(this.getName() + " waiting message", false);

	    msg = (Message) ois.readObject();
	    // reset the error's count.
	  } catch (ClassNotFoundException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    Debug.trace(this.getName(), exc);
	    continue;
	  } catch (InvalidClassException exc) {
	    // TODO: In order to process it we have to return an error,
	    // but in that case me must identify the bad message...
	    Debug.trace(this.getName(), exc);
	    continue;
	  } catch (StreamCorruptedException exc) {
	    Debug.trace(this.getName(), exc);
	    break;
	  } catch (OptionalDataException exc) {
	    Debug.trace(this.getName(), exc);
	    break;
	  } catch (IOException exc) {
	    Debug.trace(this.getName(), exc);
	    // May be we have to handle differently EOFException and others
	    // IOException.
	    break;
	  }
	  canStop = false;

	  if (Debug.debug && Debug.message)
	    Debug.trace(this.getName() + " receives message " +
			msg, false);

	  Channel.post( msg);
	  Channel.validate();
	}
      } catch (Exception exc) {
	// TODO:
	Debug.trace(this.getName() + " exited", exc);
      } finally {
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(this.getName() + " stopped", false);

	close();
	isRunning = false;
	thread = null;

	AgentServer.stop();
      }
    }
  }

  final class NetServerOut extends Daemon {
    NetServerOut(String name) {
      super(name + ".netServerOut");
    }

    void shutdown() {
      close();
    }

    public void run() {
      Message msg = null;
      
      try {
	while (isRunning) {
	  canStop = true;

	  if (Debug.debug && Debug.message)
	    Debug.trace(this.getName() + " waiting message", false);

	  try {
	    msg = qout.get();
	  } catch (InterruptedException exc) {
	    continue;
	  }
	  canStop = false;

	  if (Debug.debug && Debug.message)
	    Debug.trace(this.getName() + " try to send message -> " + msg, false);

	  // Send the message
	  try {
	    oos.writeObject(msg);
	    oos.flush();
	    oos.reset();
	  } catch (IOException exc) {
	    if (Debug.debug && Debug.network)
	      Debug.trace(this.getName() + " error during sending", exc);
	    break;
	  }
	  qout.pop();
	}
      } finally {
	if (Debug.debug && Debug.A3Server)
	  Debug.trace(this.getName() + " stopped", false);

	close();
	isRunning = false;
	thread = null;

	AgentServer.stop();
      }
    }
  }
}

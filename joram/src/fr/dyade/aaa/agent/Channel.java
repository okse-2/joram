/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * Class <code>Channel</code> realizes the interface for sending messages.
 * It defines function member SendTo to send a notification to an identified
 * agent.<p>
 * Notifications are then routed to a message queue where they are
 * stored in chronological order. The Channel object is responsible for
 * localizing the target agent.
 */
abstract public class Channel {
  static Channel channel = null;

  /**
   * Creates a new instance of channel (result depends of server type).
   *
   * @return	the corresponding <code>Channel</code>'s instance.
   */
  static Channel newInstance() throws Exception {
    String cname = System.getProperty("Channel",
                                      "fr.dyade.aaa.agent.TransactionChannel");
    Class cclass = Class.forName(cname);
    channel = (Channel) cclass.newInstance();
    return channel;
  }

  protected Logger logmon = null;

  /**
   * Constructs a new <code>Channel</code> object (can only be used by
   * subclasses).
   */
  protected Channel() {
    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Engine +
                             ".#" + AgentServer.getServerId());
    logmon.log(BasicLevel.DEBUG, toString() + " created.");
  }

  /**
   * Sends a notification to an agent. It may be used anywhere,
   * from any object and any thread. However it is best practice to call
   * the <a href="Agent.html#sendTo(AgentId, Notification)"><code>sendTo
   * </code></a> function defined in class <code>Agent</code> from an agent
   * code executed during a reaction.<p>
   * The destination agent receives the notification with a declared null
   * source agent id, which may be recognized using the <code>isNullId</code>
   * function of class <code>AgentId</code>.
   * This is not true when this call is performed during an standard agent
   * reaction. In that case the current reacting agent, known by the engine,
   * is provided as source agent.<p>
   * The notification is immediately validated, that is made persistent,
   * if it is not sent from an agent reaction.
   * Be careful, does not use this method in the engine thread, sometime
   * engine.agent is null and it throws a NullPointerException.
   *
   * @param   to     destination agent.
   * @param   not    notification.
   *
   * @exception IOException
   *	error when accessing the local persistent storage
   */
  public final static void sendTo(AgentId to,
				  Notification not) {
//     try {
//       EngineThread thread = (EngineThread) Thread.currentThread();
//       // Use the engine's sendTo method that push message in temporary queue
//       // until the end of current reaction.
//       thread.engine.push(AgentServer.engine.agent.getId(), to, not);
//     } catch (ClassCastException exc) {
//       //  Be careful, the destination node use the from.to field to
//       // get the from node id.
//       channel.directSendTo(AgentId.localId, to, not);
//     }

//  if (Class.EngineThread.isAssignable(Thread.currentThread())) {
    if (Thread.currentThread() == AgentServer.engine.thread) {
      AgentServer.engine.push(AgentServer.engine.agent.getId(), to, not);
    } else {
      channel.directSendTo(AgentId.localId, to, not);
    }
  }

  /**
   * Adds a message in "ready to deliver" list of right consumer. This method
   * set the logical date of message, push it in the corresponding queue, and
   * save it.
   *
   * @param msg		The message to deliver.
   */
  static final void post(Message msg) throws Exception {
    try {
      AgentServer.getConsumer(msg.to.getTo()).post(msg);
    } catch (UnknownServerException exc) {
      channel.logmon.log(BasicLevel.ERROR,
                         channel.toString() + ", can't post message: " + msg,
                         exc);
      // TODO: Post an ErrorNotification
    }
  }

  /**
   * Save state of all modified consumer.
   */
  static final void save() throws IOException {
    for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
      ((MessageConsumer) c.nextElement()).save();
    }
  }

  /**
   *  Validates all messages previously dispatched. There is two separate
   * methods for dispatch and validate messages because of use of transactions
   * in <code>Engine</code>. The messages are only validated in queues after
   * the commit of transaction.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   *
   * @see TransactionEngine#commit()
   */
  static final void validate() {
    for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
      ((MessageConsumer) c.nextElement()).validate();
    }
  }

  /**
   * Sends an immediately validated notification to an agent. Post and
   * directly dispatches the notification to the <code>MessageConsumer</code>.
   * Does not queue the notification in the local message queue.<p>
   * Normally used uniquely in <a href="#sendTo(AgentId, Notification)"><code>
   * sendTo</code></a> method.
   *
   * This function is designed to be indirectly used by secondary threads,
   * such as <code>Driver</code>s.
   *
   * @param   from   source agent.
   * @param   to     destination agent.
   * @param   not    notification.
   *
   * @exception IOException
   *	error when accessing the local persistent storage
   */
  abstract void
  directSendTo(AgentId from,
	       AgentId to,
	       Notification not);
}

final class TransactionChannel extends Channel {
  /**
   * Constructs a new <code>TransactionChannel</code> object. this method
   * must only be used by <a href="Channel.html#newInstance()">static channel
   * allocator</a>.
   */
  TransactionChannel() {
    super();
  }

  /**
   *  Sends an immediately validated notification to an agent. Normally
   * used uniquely in <a href="#sendTo(AgentId, Notification)"><code>
   * sendTo</code></a> method and in TransientProxy to forward messages.
   *
   * @param   from   source agent.
   * @param   to     destination agent.
   * @param   not    notification.
   */
  void directSendTo(AgentId from,
		    AgentId to,
		    Notification not) {
    MessageConsumer consumer = null;
    Message msg = null;

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 toString() + ".directSendTo(" + from + ", " + to + ", " + not + ")");

    if ((to == null) || to.isNullId())
      return;

    msg = Message.alloc(from, to, not);
    try {
      consumer = AgentServer.getConsumer(to.to);
    } catch (UnknownServerException exc) {
      channel.logmon.log(BasicLevel.ERROR,
                         toString() + ", can't post message: " + msg,
                         exc);
      // TODO: Post an ErrorNotification ?
      return;
    }

    try {
      AgentServer.transaction.begin();
      consumer.post(msg);
      consumer.save();
      AgentServer.transaction.commit();
      // then commit and validate the message.
      consumer.validate();
      AgentServer.transaction.release();
    } catch (Exception exc) {
      // Should never happened (IOException or ClassNotFoundException).
      logmon.log(BasicLevel.FATAL,
                 toString() + ", Transaction problem.", exc);
      throw new TransactionError(toString() + ", " + exc.getMessage());
    }
  }

  /**
   * Returns a string representation of this <code>TransactionChannel</code>
   * object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("TransactionChannel#").append(AgentServer.getServerId());
    return strbuf.toString();
  }
}

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

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

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
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: Channel.java,v 1.9 2002-03-06 16:50:00 joram Exp $";

  static Channel channel = null;

  protected Queue mq;

  /**
   * Creates a new instance of channel (result depends of server type).
   */
  static Channel newInstance() {
    if (AgentServer.isTransient())
      channel = new TransientChannel();
    else // if (type == AgentServer.TRANSACTION)
      channel = new TransactionChannel();
    return channel;
  }

  protected Monitor logmon = null;

  /**
   * Constructs a new <code>Channel</code> object (can only be used by
   * subclasses).
   */
  protected Channel() {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getMonitor(Debug.A3Engine);
    logmon.log(BasicLevel.DEBUG, toString() + " created.");

    this.mq = new Queue();
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
   *
   * @param   to     destination agent.
   * @param   not    notification.
   *
   * @exception IOException
   *	error when accessing the local persistent storage
   */
  public final static void sendTo(AgentId to,
				  Notification not) {
    if (Thread.currentThread() == AgentServer.engine.thread) {
      // Be careful, does not use this method in the engine thread, sometime
      // engine.agent is null and it throws a NullPointerException.
      channel.push(AgentServer.engine.agent.getId(), to, not);
    } else {
      //  Be careful, the destination node use the from.to field
      // to get the from node id.
      channel.directSendTo(AgentId.localId, to, not);
    }
  }

  /**
   * Sends a notification to an agent. Normally used uniquely in
   * <a href="Agent.html#sendTo(AgentId, Notification)">sendTo</a> method.
   * 
   * @param   from   source agent.
   * @param   to     destination agent.
   * @param   not    notification.
   */
  synchronized final void sendTo(AgentId from,
				 AgentId to,
				 Notification not) {
    if (Thread.currentThread() == AgentServer.engine.thread) {
      // Be careful, does not use this method in the engine thread, sometime
      // engine.agent is null and it throws a NullPointerException.
      push(from, to, not);
    } else {
      //  Be careful, the destination node use the from.to field
      // to get the from node id.
      directSendTo(from, to, not);
    }

  }

  synchronized final void push(AgentId from,
                               AgentId to,
                               Notification not) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 toString() + ".SendTo(" + from + ", " + to + ", " + not + ")");
    if ((to == null) || to.isNullId())
      return;
    
    mq.push(new Message(from, to, not));
  }

  /**
   * Dispatch messages between the <a href="MessageConsumer.html">
   * <code>MessageConsumer</code></a>: <a href="Engine.html">
   * <code>Engine</code></a> component and <a href="Network.html">
   * <code>Network</code></a> components.<p>
   * Handle persistent information in respect with engine transaction.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   *
   * @exception IOException	error when accessing the local persistent
   *				storage.
   */
  static final void dispatch() throws IOException {
    Message msg = null;

    while (! channel.mq.isEmpty()) {
      try {
	msg = (Message) channel.mq.get();
      } catch (InterruptedException exc) {
	continue;
      }

      if (msg.from == null) msg.from = AgentId.localId;
      post(msg);
      channel.mq.pop();
    }
    save();
  }

  /**
   * Adds a message in "ready to deliver" list of right consumer. This method
   * set the logical date of message, push it in the corresponding queue, and
   * save it.
   *
   * @param msg		The message to deliver.
   */
  static final void post(Message msg) throws IOException {
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
    for (int i=0; i<AgentServer.consumers.length; i++) {
      AgentServer.consumers[i].save();
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
    for (int i=0; i<AgentServer.consumers.length; i++) {
      AgentServer.consumers[i].validate();
    }
  }

  /**
   *  Invalidates all messages previously dispatched. This method is used
   * during abortion of reaction in <code>Engine</code>.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   *
   * @see TransactionEngine#abort()
   */
  static final void invalidate() {
    for (int i=0; i<AgentServer.consumers.length; i++) {
      AgentServer.consumers[i].invalidate();
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

  /**
   * Cleans the Channel queue of all pushed notifications.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   */
  static final void clean() {
    channel.mq.removeAllElements();
  }
}

final class TransactionChannel extends Channel {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: Channel.java,v 1.9 2002-03-06 16:50:00 joram Exp $";

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

    msg = new Message(from, to, not);
    try {
      consumer = AgentServer.getConsumer(to.to);
    } catch (UnknownServerException exc) {
      channel.logmon.log(BasicLevel.ERROR,
                         channel.toString() + ", can't post message: " + msg,
                         exc);
      // TODO: Post an ErrorNotification ?
      return;
    }

    try {
      AgentServer.transaction.begin();
      try {
        consumer.post(msg);
        consumer.save();
      } catch (IOException exc2) {
        logmon.log(BasicLevel.FATAL,
                   toString() + ", can't post message: " + msg,
                   exc2);
        consumer.invalidate();
        AgentServer.transaction.rollback();
        // Restore the matrix clock state from disk.
	consumer.restore();
        AgentServer.transaction.release();
        throw exc2;
      }
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

final class TransientChannel extends Channel {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: Channel.java,v 1.9 2002-03-06 16:50:00 joram Exp $";

  /**
   * Constructs a new <code>TransientChannel</code> object. this method
   * must only be used by <a href="Channel.html#newInstance()">static channel
   * allocator</a>.
   */
  TransientChannel() {
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
 
    try {
      msg = new Message(from, to, not);
      consumer = AgentServer.getConsumer(to.to);
    } catch (UnknownServerException exc) {
      channel.logmon.log(BasicLevel.ERROR,
                         channel.toString() + ", can't post message: " + msg,
                         exc);
      // TODO: Post an ErrorNotification
    }

    try {
      consumer.post(msg);
      consumer.save();
    } catch (IOException exc) {
      logmon.log(BasicLevel.FATAL,
                 "Channel: Can't post message to #" + to.getTo(), exc);
      consumer.invalidate();
      throw new TransactionError(toString() + ", " + exc.getMessage());
    }
    consumer.validate();
  }


  /**
   * Returns a string representation of this <code>TransientChannel</code>
   * object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("TransientChannel#").append(AgentServer.getServerId());
    return strbuf.toString();
  }
}

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
import fr.dyade.aaa.util.*;

/**
 * Class <code>Channel</code> realizes the interface for sending messages.
 * It defines function member SendTo to send a notification to an identified
 * agent.<p>
 * Notifications are then routed to a message queue where they are
 * stored in chronological order. The Channel object is responsible for
 * localizing the target agent.
 *
 * @author  Andre Freyssinet
 */
abstract public class Channel {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: Channel.java,v 1.3 2000-10-05 15:15:20 tachkeni Exp $";

  public static Channel channel = null;

  /**
   * give the number of SendTo realized into a period
   */
  static protected int nbMessageSend = 0;

  protected Queue mq;
  protected MessageQueue qin;
  protected MessageQueue qout;
  protected MatrixClock mclock;

  static Channel newInstance(Queue mq,
			     MessageQueue qin,
			     MessageQueue qout,
			     MatrixClock mclock) {
    if (Server.isTransient(Server.getServerId()))
      channel = new TransientChannel(mq, qin, qout, mclock);
    else // if (type == Server.TRANSACTION)
      channel = new TransactionChannel(mq, qin, qout, mclock);
    return channel;
  }

  Channel(Queue mq,
	  MessageQueue qin,
	  MessageQueue qout,
	  MatrixClock mclock) {
    this.mq = mq;
    this.qin = qin;
    this.qout =qout ;
    this.mclock =mclock ;
  }

  /**
   * Sends a notification to an agent. This function should be the only one
   * exported by this class to send a notification. It may be used anywhere,
   * from any object and any thread. However it is best practice to call
   * the <a href="Agent.html#sendTo(AgentId, Notification)"><code>sendTo
   * </code></a> function defined in class <code>Agent</code> from an agent
   * code executed during a reaction.<p>
   * The destination agent receives the notification with a declared null
   * source agent id, which may be recognized using the <code>isNullId</code>
   * function of class <code>AgentId</code>. The <code>from</code> and
   * <code>to</code> variables of this agent id are set to this server id.
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
  public void
      sendTo(AgentId to, Notification not) throws IOException {
    if ((Thread.currentThread() == Server.engine.thread) &&
	(Server.engine.agent != null)) {
      sendTo(Server.engine.agent.getId(), to, not);
    } else {
      //  Be careful, the destination node use the from.to field
      // to get the from node id.
      directSendTo(AgentId.localId, to, not);
    }
  }

  /**
   *
   */
  abstract void
      sendTo(AgentId from,
	     AgentId to,
	     Notification not);

  /**
   *
   */
  abstract void dispatch() throws IOException;

  /**
   * Sends an immediately validated notification to an agent.
   *
   * @deprecated     Use <a href="#sendTo(AgentId, Notification)"><code>
   *		     sendTo(AgentId, Notification)</code></a>method instead.
   *
   * @param   to     destination agent.
   * @param   not    notification.
   *
   * @exception IOException
   *	error when accessing the local persistent storage
   */
  abstract public void
      directSendTo(AgentId to,
		   Notification not) throws IOException;

  /**
   * Sends an immediately validated notification to an agent. Normally
   * used uniquely in <a href="#sendTo(AgentId, Notification)">sendTo</a>
   * method and in TransientProxy to forward messages.
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
		   Notification not) throws IOException;

  /**
   *
   */
  abstract void clean();
}

final class TransactionChannel extends Channel {
  TransactionChannel(Queue mq,
		     MessageQueue qin,
		     MessageQueue qout,
		     MatrixClock mclock) {
    super(mq, qin, qout, mclock);
  }

  /**
   * Should only be used by Agent class in sendTo method.
   *
   * @param   from   .
   * @param   to     .
   * @param   not    .
   */
  synchronized void
      sendTo(AgentId from,
	     AgentId to,
	     Notification not) {
    Message msg;

    if ((to == null) || to.isNullId())
      return;

    if (Debug.channelSend)
      Debug.trace("Channel: from " + from +
		  " to " + to +
		  " send " + not,
		  false);

    if (Server.isTransient(to.to))
      msg = new Message(AgentId.localId,
			Server.transientProxyId(to.to),
			new TransientMessage(from, to, not));
    else
      msg = new Message(from, to, not);
    
    mq.push(msg);
  }

  synchronized void dispatch() throws IOException {
    while (! mq.isEmpty()) {
      Message msg = (Message) mq.get();
      if (msg.from == null) msg.from = AgentId.localId;
      msg.update = mclock.getSendUpdate(msg.to.to);
      msg.save();
      if (msg.to.to == Server.serverId) {
	qin.push(msg);
      } else {
	qout.push(msg);
      }
      mq.pop();
    }
    mclock.save();
  }

  /**
   * Sends and directly dispatches a notification into the server queues.
   * Does not queue the notification in the local message queue.
   *
   * This function is designed to be used by secondary threads, such as
   * <code>Driver</code>s.
   *
   * @param   to     .
   * @param   not    .
   */
  public void
      directSendTo(AgentId to,
		   Notification not) throws IOException {
    //  Be careful, the destination node use the from.to field
    // to get the from node id.
    directSendTo(AgentId.localId, to, not);
  }

  /**
   * This function should only be used by particular proxy use to
   * interconnect agent's server.
   *
   * @param   from   .
   * @param   to     .
   * @param   not    .
   */
  void directSendTo(AgentId from,
		    AgentId to,
		    Notification not) throws IOException {
    Message msg;

    if ((to == null) || to.isNullId())
      return;

    if (Debug.channelSend)
      Debug.trace("Channel: from " + from +
		  " to " + to +
		  " directSendTo " + not,
		  false);

    if (Server.isTransient(to.to))
      msg = new Message(AgentId.localId,
			Server.transientProxyId(to.to),
			new TransientMessage(from, to, not));
    else
      msg = new Message(from, to, not);

    Server.transaction.begin();
    try {
      msg.update = mclock.getSendUpdate(msg.to.to);
      msg.save();
      if (msg.to.to == Server.serverId) {
        qin.push(msg);
      } else {
	qout.push(msg);
      }
      mclock.save();
    } catch (IOException exc) {
      exc.printStackTrace(System.err);
      if (msg.to.to == Server.serverId) {
	qin.invalidate();
      } else {
        qout.invalidate();
      };
      Server.transaction.rollback();
      // Restore the matrix clock state from disk.
      try {
	mclock = MatrixClock.load();
      } catch (Exception exc2) {
	// Should never happened (IOException or ClassNotFoundException).
	Debug.trace("Channel: Can't rollback from " + exc, exc2);
	throw new RuntimeException("Channel: Can't rollback.");
      }
      Server.transaction.release();
      throw exc;
    }
    Server.transaction.commit();
    // then commit and validate the message.
    if (msg.to.to == Server.serverId) {
      qin.validate();
    } else {
      qout.validate();
    };
    Server.transaction.release();
  }

  synchronized void clean() {
    mq.removeAllElements();
  }

  /**
   * Returns a string representation of this <code>TransactionChannel</code>
   * object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("TransactionChannel#");
    strbuf.append(Server.serverId);
    strbuf.append("\tMatrixClock:\n");
    strbuf.append(mclock.toString());
    strbuf.append("\tMessageQueue qin = [");
    strbuf.append(qin.toString());
    strbuf.append("]\n\tMessageQueue qout = [");
    strbuf.append(qout.toString());
    strbuf.append("]\n");

    return strbuf.toString();
  }
}

final class TransientChannel extends Channel {
  TransientChannel(Queue mq,
		   MessageQueue qin,
		   MessageQueue qout,
		   MatrixClock mclock) {
    super(mq, qin, qout, mclock);
  }

  /**
   * Use by not Agent... if there is no valid agent destination...
   *
   * @param   to     .
   * @param   not    .
   */
  public void
      sendTo(AgentId to,
	     Notification not) {
    //  Be careful, the destination node use the from.to field
    // to get the from node id.
    sendTo(AgentId.localId, to, not);
  }
 
  /**
   * Should only be used by Agent class in sendTo method.
   *
   * @param   from   .
   * @param   to     .
   * @param   not    .
   */
  synchronized void
      sendTo(AgentId from,
	     AgentId to,
	     Notification not) {
    if ((to != null) && (! to.isNullId())) {
      if (Debug.channelSend)
	Debug.trace("Channel: from " + from +
		    " to " + to +
		    " send " + not,
		    false);

      if (to.to == Server.serverId) {
	mq.push(new Message(from, to, not));
      } else {
	mq.push(new Message(AgentId.localId,
			    AgentId.transientProxyId,
			    new TransientMessage(from, to, not)));
      }
    }
  }

  void dispatch() throws IOException {}

  /**
   * Sends and directly dispatches a notification into the server queue.
   * Does not queue the notification in the local message queue.
   *
   * This function is designed to be used by secondary threads, such as
   * <code>Driver</code>s.
   *
   * @param   to     .
   * @param   not    .
   */
  public void
      directSendTo(AgentId to,
		   Notification not) throws IOException {
    //  Be careful, the destination node use the from.to field
    // to get the from node id.
    directSendTo(AgentId.localId, to, not);
  }

  /**
   * This function should only be used by particular proxy use to
   * interconnect agent's server.
   *
   * @param   from   .
   * @param   to     .
   * @param   not    .
   */
  void
      directSendTo(AgentId from,
		   AgentId to,
		   Notification not) throws IOException {
    if (Debug.channelSend)
      Debug.trace("Channel: from " + from +
		  " to " + to +
		  " direct send " + not,
		  false);

    if (to.to == Server.serverId) {
      mq.push(new Message(from, to, not));
    } else {
      mq.push(new Message(AgentId.localId,
			  AgentId.transientProxyId,
			  new TransientMessage(from, to, not)));
    }
  }

  void clean() {}

  /**
   * Returns a string representation of this <code>TransientChannel</code>
   * object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("TransientChannel#");
    strbuf.append(Server.serverId);
    strbuf.append(":\n");
    strbuf.append("Queue = [");
    strbuf.append(mq.toString());
    strbuf.append("]\n");

    return strbuf.toString();
  }
}

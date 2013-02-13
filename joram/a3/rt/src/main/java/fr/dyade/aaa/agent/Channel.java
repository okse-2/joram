/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Class <code>Channel</code> realizes the interface for sending messages.
 * It defines function member SendTo to send a notification to an identified
 * agent.<p>
 * Notifications are then routed to a message queue where they are
 * stored in chronological order. The Channel object is responsible for
 * localizing the target agent.
 */
public class Channel {
  static Channel channel = null;

  /**
   * Creates a new instance of channel (result depends of server type).
   *
   * @return	the corresponding <code>Channel</code>'s instance.
   */
  static Channel newInstance() throws Exception {
    String cname = AgentServer.getProperty("Channel", "fr.dyade.aaa.agent.Channel");
    Class<?> cclass = Class.forName(cname);
    channel = (Channel) cclass.newInstance();
    return channel;
  }

  protected Logger logmon = null;

  /**
   * Constructs a new <code>Channel</code> object (can only be used by
   * subclasses).
   */
  protected Channel() {
    consumers = new Vector<MessageConsumer>();

    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Engine + ".#" + AgentServer.getServerId());
    logmon.log(BasicLevel.DEBUG, toString() + " created.");
  }

  static Vector<MessageConsumer> consumers = null;

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
  public final static void sendTo(AgentId to, Notification not) {
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
      MessageConsumer cons = AgentServer.getConsumer(msg.to.getTo());
      if (! consumers.contains(cons)) {
        consumers.add(cons);
      }
      cons.post(msg);
    } catch (UnknownServerException exc) {
      channel.logmon.log(BasicLevel.ERROR,
                         channel.toString() + ", can't post message: " + msg, exc);
      if ((msg.from != null) && (msg.from.stamp != AgentId.NullIdStamp))
        post(Message.alloc(AgentId.localId, msg.from, new UnknownAgent(msg.to, msg.not)));
    }
  }

  /**
   * Save state of all modified consumer.
   */
  static final void save() throws IOException {
    for (int i=0; i<consumers.size(); i++) {
      ((MessageConsumer) consumers.elementAt(i)).save();
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
   * @see Engine#commit()
   */
  static final void validate() {
    for (int i=0; i<consumers.size(); i++) {
      ((MessageConsumer) consumers.elementAt(i)).validate();
    }
    consumers.clear();
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
      // JORAM_PERF_BRANCH:
      if (msg.not != null && msg.not.persistent == false) {
        consumer.postAndValidate(msg);
      } else {
      AgentServer.getTransaction().begin();      
      consumer.post(msg);
      
//      if (AgentServer.sdf != null) {
//        // SDF generation
//        StringBuffer strbuf = new StringBuffer();
//        strbuf.append("<sendto agent=\"").append(msg.to);
//        strbuf.append("\" notification=\"").append(StringId.toStringId('N', '_', msg.getSource(), msg.getDest(), msg.getStamp()));
//        strbuf.append("\" info=\"").append(msg.not.getClass().getSimpleName());
//        strbuf.append("\" flowid=\"0\">\n");
//        strbuf.append("<comment>").append(msg.not).append("</comment>\n" + "</sendto>\n");
//
//        AgentServer.sdf.println(strbuf.toString());
//      }
//      
//      if (AgentServer.logsdf.isLoggable(BasicLevel.INFO))
//        AgentServer.logsdf.log(BasicLevel.INFO,
//                             "  sendto " + msg.to + ' ' + StringId.toStringId('N', '_', msg.getSource(), msg.getDest(), msg.getStamp()));
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, toString() + ".directSendTo() -> " + msg.getStamp());

      consumer.save();
      AgentServer.getTransaction().commit(false);
      // then commit and validate the message.
      consumer.validate();
      AgentServer.getTransaction().release();
      }
    } catch (Exception exc) {
      // Should never happened (IOException or ClassNotFoundException).
      logmon.log(BasicLevel.FATAL,
                 toString() + ", Transaction problem.", exc);
      throw new TransactionError(toString() + ", " + exc.getMessage());
    }
  }

  /**
   * Returns a string representation of this <code>Channel</code> object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("Channel#").append(AgentServer.getServerId());
    return strbuf.toString();
  }
}

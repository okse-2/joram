/*
 * Copyright (C) 2004 - 2011 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.lang.ref.SoftReference;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Transaction;

public final class MessageSoftRef {

  private static final Logger logmon = Debug.getLogger(MessageSoftRef.class.getName());

  /**
   * Name for persistent message, used to retrieve garbaged message from
   * persistent storage.
   */
  private String name = null;

  /**
   * Reference for transient message, used to pin non persistent in memory.
   */
  private Message ref = null;

  /**
   * The SoftReference to the message, which permits to the message to be
   * garbaged in response to memory demand.
   */
  private SoftReference<Message> softRef = null;

  /**
   * The stamp of the referenced message. It is useful to avoid reloading
   * messages from disk when looking for a particular message.
   */
  private int stamp;

  /**
   * If the notification is stored independently of its containing message,
   * messageId contains the persistent name of this notification.
   */
  private String messageId;

  /**
   * The expiration date of the notification hold by the message, if any.
   */
  private long expiration;

  /**
   * The agent responsible of treating the notification when it expires. It is
   * useful to avoid reloading messages from disk before deleting them if the
   * field is empty.
   */
  private AgentId deadNotAgentId;

  MessageSoftRef(Message msg) {
    this.softRef = new SoftReference<Message>(msg);
    this.stamp = msg.stamp;
    if (msg.not != null) {
      this.expiration = msg.not.expiration;
      this.deadNotAgentId = msg.not.deadNotificationAgentId;
    }
    if (msg.isPersistent()) {
      name = msg.toStringId();
      if (msg.not.detachable && !msg.not.detached) {
        messageId = msg.not.getMessageId();
      }
    } else {
      ref = msg;
    }
  }

  /**
   * Tests whether the message has expired.
   * 
   * @param time the current time.
   * @return true if the message has expired.
   */
  public boolean isExpired(long time) {
    return expiration > 0 && expiration < time;
  }

  /**
   * Returns the agent responsible of treating the notification when it expires.
   */
  public AgentId getDeadNotAgentId() {
    return deadNotAgentId;
  }

  /**
   * Returns this reference message's referent. If the message has been swap out
   * it returns null.
   * 
   * @return The message to which this reference refers.
   */
  public Message getMessage() {
    return ref == null ? (Message) softRef.get() : ref;
  }

  /**
   * Returns the stamp of the message backed by this MessageSoftRef, without
   * reloading it if it has been swapped.
   * 
   * @return the stamp of the message.
   */
  public int getStamp() {
    return stamp;
  }

  /**
   * Returns the message to which this reference refers, loading it from disk if
   * the message has been swapped out. If loading from disk is done, the
   * SoftReference is renewed to avoid reloading the message each time this
   * method is called.
   * 
   * @return The message to which this reference refers.
   */
  public Message loadMessage() throws TransactionError {
    Message msg = getMessage();
    if (msg == null) {
      try {
        msg = Message.load(name);
        softRef = new SoftReference<Message>(msg);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, "SoftReference: reload from disk " + msg);
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR, "SoftReference: Can't load message " + name, exc);
        throw new TransactionError(exc);
      }
    }
    return msg;
  }

  /**
   * Deletes the message in persistent storage. This method must be called
   * during a {@link Transaction}.
   */
  public void delete() {
    if (name != null) {
      Message msg = (Message) softRef.get();
      if (msg != null) {
        /* This is the main case, messages are in main memory when handled by
         * the engine. */
        msg.delete();
        msg.free();
      } else {
        /* This case can only happen in networks, when messages waiting for an
         * acknowledgment have been swapped out. */
        AgentServer.getTransaction().delete(name);
        if (messageId != null) {
          AgentServer.getTransaction().delete(messageId);
        }
      }
    }
  }

  /**
   * Returns a string representation of this <code>MessageSoftRef</code> object.
   * 
   * @return A string representation of this object.
   */
  public String toString() {
    return "msgSoft#" + stamp;
  }

  final static class TransactionError extends Error {

    private static final long serialVersionUID = 1L;

    TransactionError(Throwable cause) {
      super(cause.getMessage());
    }
  }
}

/*
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * This class holds a list of {@link Message}, waiting to be sent. After sending
 * one message it is marked as sent, which means it is kept until its
 * acknowledgment. Once acknowledged, the message is deleted.<br>
 * This list can hold an acknowledgment message, which is always the first one
 * to be sent if present.<br>
 * If the list is marked as persistent, messages are encapsulated by
 * {@link MessageSoftRef} in order to be garbaged from memory if necessary.
 */
public class MessageSoftList {

  /**
   * A comparator to keep list sorted by message stamp.
   */
  static class MessageComparator implements Comparator<Message> {
    public int compare(Message o1, Message o2) {
      return o1.getStamp() - o2.getStamp();
    }
  }

  /**
   * A comparator to keep list sorted by message stamp when using soft
   * references.
   */
  static class MessageSoftComparator implements Comparator<MessageSoftRef> {
    public int compare(MessageSoftRef o1, MessageSoftRef o2) {
      return o1.getStamp() - o2.getStamp();
    }
  }

  /**
   * An iterator returning the messages when going through a collection of
   * {@link MessageSoftRef}.
   */
  private static class MessageSoftIterator implements Iterator {

    private Iterator iterator;

    public MessageSoftIterator(Iterator iterator) {
      this.iterator = iterator;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Object next() {
      MessageSoftRef msgRef = (MessageSoftRef) iterator.next();
      return msgRef.loadMessage();
    }

    public void remove() {
      iterator.remove();
    }
  }

  /** Logger for this class. */
  private Logger logmon;
  
  /** Current ack message which will be sent in priority. */
  private Message ack;

  /** The list of messages waiting to be sent. */
  private SortedSet msgToSendlist;

  /** The list of sent messages waiting to be acknowledged. */
  private SortedSet msgSentlist;

  /** Tells if messages are persisted on disk and can be garbaged from memory. */
  private final boolean persistent;

  public MessageSoftList(String name, boolean persistent) {
    logmon = Debug.getLogger(getClass().getName() + '.' + name);
    ack = null;
    if (persistent) {
      msgToSendlist = new TreeSet(new MessageSoftComparator());
      msgSentlist = new TreeSet(new MessageSoftComparator());
    } else {
      msgToSendlist = new TreeSet(new MessageComparator());
      msgSentlist = new TreeSet(new MessageComparator());
    }
    this.persistent = persistent;
  }

  /**
   * Adds a message at the end of the list. Use {@link #setAck(Message)} if you
   * want to add an ack to the list.
   */
  public synchronized void addMessage(Message msg) {
    if (persistent) {
      msgToSendlist.add(new MessageSoftRef(msg));
    } else {
      msgToSendlist.add(msg);
    }
  }

  /**
   * Returns the acknowledge waiting to be sent if present.
   * 
   * @return current acknowledge waiting to be sent, or null if there is none.
   */
  public synchronized Message getAck() {
    return ack;
  }

  /**
   * Returns the first non expired message waiting to be sent. It can be an
   * acknowledge.
   * 
   * @return the next message to send or null if there is none.
   */
  public synchronized Message getFirst() {
    Message msg;
    do {
      msg = selectFirst();
    } while (msg == null && msgToSendlist.size() > 0);
    return msg;
  }

  private Message selectFirst() {
    Message msg = null;
    if (persistent) {
      if (ack != null) {
        msg = ack;
        ack = null;
      } else if (msgToSendlist.size() > 0) {
        MessageSoftRef msgRef = (MessageSoftRef) msgToSendlist.first();

        if (msgRef.isExpired(System.currentTimeMillis())) {
          msgToSendlist.remove(msgRef);
          removeExpired(msgRef);
          return null;
        } else {
          msg = msgRef.loadMessage();
        }
      }
    } else {
      if (ack != null) {
        msg = ack;
        ack = null;
      } else if (msgToSendlist.size() > 0) {
        msg = (Message) msgToSendlist.first();
        if (msg.not.expiration > 0 && msg.not.expiration < System.currentTimeMillis()) {
          msgToSendlist.remove(msg);
          removeExpired(msg);
          return null;
        }
      }
    }
    return msg;
  }

  /**
   * Removes the expired message, if needed an ExpiredNot is sent to the
   * deadNotificationAgentId specified.
   * 
   * @param msg
   *          The message to remove.
   */
  private void removeExpired(MessageSoftRef msgRef) {
    if (msgRef.getDeadNotAgentId() != null) {
      Message msg = msgRef.loadMessage();
      removeExpired(msg);
    } else {
      try {
        AgentServer.getTransaction().begin();
        msgRef.delete();
        AgentServer.getTransaction().commit(true);
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR, "exception when deleting expired msg#" + msgRef.getStamp(), exc);
      }
    }
  }

  /**
   * Removes the expired message, if needed an ExpiredNot is sent to the
   * deadNotificationAgentId specified.
   * 
   * @param msg
   *          The message to remove.
   */
  private void removeExpired(Message msg) {

    try {
      AgentServer.getTransaction().begin();

      ExpiredNot expiredNot = null;
      if (msg.not.deadNotificationAgentId != null) {
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "forward expired notification " + msg.from + ", " + msg.not + " to "
              + msg.not.deadNotificationAgentId);
        }
        expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
      } else {
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "removes expired notification " + msg.from + ", " + msg.not);
        }
      }

      if (expiredNot != null) {
        Channel.post(Message.alloc(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot));
        Channel.validate();
      }

      msg.delete();
      msg.free();

      AgentServer.getTransaction().commit(true);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, "exception in removeExpired msg#" + msg.getStamp(), exc);
    }
  }

  /**
   * Iterates over messages waiting for an acknowledge to delete the ones which
   * have a stamp inferior or equal to the given one.
   * 
   * @param stamp
   *          the stamp of the last message acked.
   * @throws IOException
   */
  public synchronized void deleteMessagesUpTo(int stamp) throws IOException {
    if (persistent) {
      MessageSoftRef msgRef = null;
      Iterator iter = msgSentlist.iterator();

      int removedCount = 0;
      AgentServer.getTransaction().begin();
      while (iter.hasNext()) {
        msgRef = (MessageSoftRef) iter.next();
        if (msgRef.getStamp() <= stamp) {
          iter.remove();
          msgRef.delete();
          removedCount++;
        } else {
          break;
        }
      }
      AgentServer.getTransaction().commit(true);

      if (removedCount == 0) {
        throw new NoSuchElementException();
      }
    } else {
      Iterator iter = msgSentlist.iterator();
      while (iter.hasNext()) {
        Message msg = (Message) iter.next();
        if (msg.getStamp() <= stamp) {
          iter.remove();
        } else {
          break;
        }
      }
    }
  }

  /**
   * Sets the next ack to send.
   */
  public synchronized void setAck(Message ack) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, ", set ack (" + ack + ')');
    this.ack = ack;
  }

  /**
   * Returns an iterator over the messages waiting to be sent.
   * 
   * @return an iterator over the messages waiting to be sent.
   */
  public Iterator toSendIterator() {
    if (persistent) {
      return new MessageSoftIterator(msgToSendlist.iterator());
    }
    return msgToSendlist.iterator();
  }

  public synchronized String toString() {
    if (persistent) {
      return "ack#" + (ack == null ? "undef" : String.valueOf(ack.stamp)) + " + "
          + Arrays.toString(msgSentlist.toArray()) + Arrays.toString(msgToSendlist.toArray());
    }
    return "ack#" + (ack == null ? "undef" : String.valueOf(ack.stamp)) + " + [size=" + msgSentlist.size()
        + "][size=" + msgToSendlist.size() + "]";
  }

  /**
   * Resets all messages waiting for an acknowledge: they are placed back at the
   * beginning of the list respecting their stamp order.<br>
   * Furthermore, expired messages are removed from the list.
   */
  public synchronized void reset() {
    msgToSendlist.addAll(msgSentlist);
    msgSentlist.clear();
    
    long currentTime = System.currentTimeMillis();
    if (persistent) {
      for (Iterator iterator = msgToSendlist.iterator(); iterator.hasNext();) {
        MessageSoftRef msgRef = (MessageSoftRef) iterator.next();
        if (msgRef.isExpired(currentTime)) {
          iterator.remove();
          if (msgRef.getDeadNotAgentId() != null) {
            Message msg = msgRef.loadMessage();
            removeExpired(msg);
          } else {
            msgRef.delete();
          }
        }
      }
    } else {
      for (Iterator iterator = msgToSendlist.iterator(); iterator.hasNext();) {
        Message msg = (Message) iterator.next();
        if (msg.not.expiration > 0 && msg.not.expiration < currentTime) {
          iterator.remove();
          removeExpired(msg);
        }
      }
    }

  }

  /**
   * Returns the number of messages waiting to be sent.
   * 
   * @return the number of messages waiting to be sent.
   */
  public synchronized int toSendSize() {
    return msgToSendlist.size() + (ack == null ? 0 : 1);
  }

  /**
   * Mark specified message as sent (ie waiting for an ack). The given message
   * must be the next one to be sent, returned by {@link #getFirst()}.
   * 
   * @param msg
   *          the message which have been sent.
   */
  public synchronized void setSent(Message msg) {
    if (persistent) {
      MessageSoftRef msgRef = (MessageSoftRef) msgToSendlist.first();
      if (msgRef.getStamp() != msg.getStamp()) {
        throw new IllegalArgumentException("Send order not respected !");
      }
      msgToSendlist.remove(msgRef);
      msgSentlist.add(msgRef);
    } else {
      Message first = (Message) msgToSendlist.first();
      if (first.stamp != msg.stamp) {
        throw new IllegalArgumentException("Send order not respected !");
      }
      msgToSendlist.remove(first);
      msgSentlist.add(first);
    }
  }

  /**
   * Returns the number of messages waiting for an ack.
   * 
   * @return the number of messages waiting for an ack.
   */
  public synchronized int sentSize() {
    return msgSentlist.size();
  }

}

/*
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Class <code>MessageVector</code> represents a persistent vector of Message
 * (source and target agent identifier, notification). 
 * As messages  have a relatively short life span, then the messages are
 * kept in main memory. The list is backed by a persistent image on the
 * disk for reliability needs.<p><hr>
 */
final class MessageVector {
  private Logger logmon = null;
  private String logmsg = null;

  /**
   * The array buffer into which the <code>Message</code> objects are stored
   * in memory. The capacity of this array buffer is at least large enough to
   * contain all the messages of the <code>MessageVector</code>.<p>
   * Messages are stored in a circular way, first one in <tt>data[first]</tt>
   * through <tt>data[(first+count-1)%length]</tt>.
   * Any other array elements  are null.
   */
  private Object data[];
  /**
   * The index of the first message in the circular buffer.
   */
  private int first;
  /**
   * The number of messages in this <tt>MessageQueue</tt> object. Components
   * <tt>data[first]</tt> through <tt>data[(first+count)%length]</tt> are the
   * actual items.
   */
  private int count;

  private boolean persistent;

  MessageVector(String name, boolean persistent) {
    logmon = Debug.getLogger(getClass().getName());
    logmsg = name + ".MessageVector: ";

    this.persistent = persistent;
    data = new Object[50];
    first = 0;
    count = 0;
  }

  /**
   * Inserts the specified message to the internal <code>Vector</code>.
   */
  void insertMessageAt(Message item, int index) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "insertMessageAt(" + item.getStamp() + ", " + index + ")");

    if (count == data.length) {
      Object newData[] = new Object[data.length *2];
      if ((first + count) < data.length) {
        System.arraycopy(data, first, newData, 0, count);
      } else {
        int j = data.length - first;
        System.arraycopy(data, first, newData, 0, j);
        System.arraycopy(data, 0, newData, j, count - j);
      }
      first = 0;
      data = newData;
    }
    if (index != count)
      System.arraycopy(data, index, data, index + 1, count - index);
    if (persistent)
      data[(first + index)%data.length] = new MessageSoftRef(item);
    else
      data[(first + index)%data.length] = item;
    count += 1;

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "insertMessageAt() -> " + this);
  }

  /**
   * Adds the specified message to the end of internal <code>Vector</code>.
   */
  void addMessage(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "addMessage(" + item.getStamp() + ")");

    insertMessageAt(item, count);
  }

  /**
   * Returns the message at the specified index.
   */
  Message getMessageAt(int index) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "getMessageAt(" + index + ")");

    if (persistent)
      return ((MessageSoftRef) data[(first + index)%data.length]).getMessage();
    else
      return (Message) data[(first + index)%data.length];
  }

  /**
   * Deletes the message at the specified index. 
   */
  void removeMessageAt(int index) {
    if ((first + index) < data.length) {
      // Moves the start of the vector +1 to the empty 'box'
      System.arraycopy(data, first,
                       data, first +1, index);
      // Erase the old first 'box'
      data[first] = null; /* to let gc do its work */
      // Move the first ptr +1, and decrease counter
      first = (first +1)%data.length;
      count -= 1;
    } else {
      // Moves the end of the vector -1 to the empty 'box'
      System.arraycopy(data, (first + index)%data.length +1,
                       data, (first + index)%data.length, count - index -1);
      // Erase the old last 'box'
      data[(first + count -1)%data.length] = null; /* to let gc do its work */
      // Decrease counter
      count -= 1;
    }
    if (count == 0) first = 0;

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "removeMessageAt(" + index + ") -> " + this);
  }

  /**
   * Returns the number of messages in this vector.
   *
   * @return  the number of messages in this vector.
   */
  public int size() {
    return count;
  }

  /**
   * Returns a string representation of this <code>MessageVector</code>
   * object. Be careful we scan the vector without synchronization, so the
   * result can be incoherent.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    
    strbuf.append('(').append(super.toString()).append(',');
    strbuf.append(first).append(',').append(count).append(",(");
    for (int i=0; i<data.length; i++) {
      strbuf.append(data[i]).append(',');
    }
    strbuf.append("))");
    
    return strbuf.toString();
  }
  
  final class MessageSoftRef
    extends java.lang.ref.SoftReference {
    /**
     *  Name for persistent message, used to retrieve garbaged message
     * from persistent storage.
     */
    String name = null;
    /**
     *  Reference for transient message, used to pin non persistent
     * in memory.
     */
    Message ref = null;
    
    MessageSoftRef(Message msg) {
      super(msg);
      if (msg.isPersistent())
        name = msg.toStringId();
      else
        ref = msg;
    }

    /**
     * Returns this reference message's referent. If the message has been
     * swap out, it is loaded from disk.
     *
     * @return The message to which this reference refers.
     */
    public Message getMessage() throws TransactionError {
      if (ref != null) return ref;

      Message msg = (Message) get();
      if (msg == null) {
        try {
          msg = (Message) AgentServer.getTransaction().load(name);

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, logmsg + "reload from disk " + msg);
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     logmsg + "Can't load message " + name, exc);
          throw new TransactionError(exc);
        }
      }
      return msg;
    }
  }

  final class TransactionError extends Error {
    TransactionError(Throwable cause) {
      super(cause);
    }
  }
}

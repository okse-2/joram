/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies 
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

/**
 * Class Notification is the root of the notifications hierarchy. Every
 * notification's class has Notification as a superclass.
 */
public class Notification implements Serializable, Cloneable {
  static final long serialVersionUID = 3007264908616389613L;

  /**
   * True if the notification is persistent, false otherwise. By default, this
   * field is set to true during object creation and disk loading. This field
   * is carry by network protocol.
   */
  protected transient boolean persistent = true;

  /**
   * True if the notification is detachable, false otherwise. A detachable
   * notification is saved in a different object that its containing
   * message.
   */
  protected transient boolean detachable = false;

  /**
   * True if the notification is detached, false otherwise. A detached
   * notification is not destroyed in the same way that its containing
   * message.
   */
  protected transient boolean detached = false;

  /**
   * If the notification is stored independently that its containing message
   * messageId contains the persistent name of this notification.
   */
  transient String messageId = null;

  public String getMessageId() {
    return messageId;
  }

  /** Context of the notification. */
  private Object context;

  /**
   * Sets the context of the notification.
   *
   * @param context the context of the notification.
   */
  public final void setContext(Object context) {
    this.context = context;
  }

  /**
   * Returns the context of the notification.
   *
   * @return the context of the notification.
   */
  public final Object getContext() {
    return context;
  }

  /**
   * Returns a clone of this notification.
   *
   * @return  a clone of this notification.
   */
  public synchronized Object clone() {
    try {
      Notification dup = (Notification) super.clone();
      dup.detached = false;
      dup.messageId = null;
      return dup;
    } catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  public String toString() {
    StringBuffer output = new StringBuffer();

    output.append("(");
    output.append(super.toString());
    output.append(",messageId=").append(messageId);
    output.append(",persistent=").append(persistent);
    output.append(",detachable=").append(detachable);
    output.append(",detached=").append(detached);
    output.append(",context=").append(context);
    output.append(")");

    return output.toString();
  }

}

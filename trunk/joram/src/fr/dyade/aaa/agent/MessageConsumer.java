/*
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
/**
 * The parent interface for all messages consumers.
 * 
 * @author	Freyssinet Andre
 * @see		Engine, Network.
 */
public interface MessageConsumer {
  public static final String RCS_VERSION="@(#)$Id: MessageConsumer.java,v 1.10 2003-06-23 13:37:51 fmaistre Exp $";

  /**
   * Returns this <code>MessageConsumer</code>'s name.
   *
   * @return this <code>MessageConsumer</code>'s name.
   */
  String getName();
  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  void insert(Message msg);

  /**
   * Saves logical clock information to persistent storage.
   */
  void save() throws IOException;

  /**
   * Restores logical clock information from persistent storage.
   */
  void restore() throws Exception;

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  void post(Message msg) throws Exception;

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  void validate();

  /**
   * Invalidates all messages pushed in queue during transaction session.
   */
  void invalidate();

  /**
   * Causes this component to begin execution.
   *
   * @see stop
   */
  void start() throws Exception;

  /**
   * Forces the component to stop executing.
   *
   * @see start
   */
  void stop();

  /**
   *  Get this consumer's <code>MessageQueue</code>. Use in administration and
   * debug tasks, should be replaced by a common attribute.
   *
   * @return this <code>MessageConsumer</code>'s queue.
   */
  MessageQueue getQueue();

  /**
   *  Tests if the component is alive. A <code>MessageConsumer</code> is alive
   * if it has been started and has not yet stopped.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  boolean isRunning();
}

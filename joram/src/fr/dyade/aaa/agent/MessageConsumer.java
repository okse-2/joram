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

import java.io.IOException;
/**
 * The parent interface for all messages consumers.
 * 
 * @author	Freyssinet Andre
 * @see		Engine, Network.
 */
public interface MessageConsumer {
  public static final String RCS_VERSION="@(#)$Id: MessageConsumer.java,v 1.9 2003-03-19 15:16:06 fmaistre Exp $";

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
  void post(Message msg) throws IOException;

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

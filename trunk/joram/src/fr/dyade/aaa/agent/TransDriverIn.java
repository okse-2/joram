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


/**
 * Derived <code>DriverIn</code> class specializing the reaction to incoming
 * notifications, used by <code>TransientManager</code> class and
 * <code>TransientServer</code> class.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 * @see		TransientMonitor
 * @see		TransientServer
 */
class TransDriverIn extends DriverIn {

  /** RCS version number of this file: $Revision: 1.1.1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: TransDriverIn.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param manager	id of manager or server agent
   * @param in		stream to read notifications from
   * @param maxNotSent	max number of notifications between <code>FlowControl</code>s
   */
  TransDriverIn(int id, AgentId manager, ObjectInputStream in, int maxNotSent) {
    super(id, manager, new ObjectToNotifInputStream(in), maxNotSent);
  }

  /**
   * Reacts to a notification from the input stream.
   * <p>
   * Received notifications are of the <code>TransientMessage</code> class.
   * Overloads the base class realization to send the notification contained
   * in the message directly to the target agent contained in the message,
   * with a declared source agent also contained in the message.
   *
   * @param not		<code>TransientMessage</code> notification to react to
   */
  void react(Notification not) throws IOException {
    if (Debug.driversData)
      Debug.trace(toString() + ".react(" + not + ")", false);
    TransientMessage msg = (TransientMessage) not;
    Channel.channel.directSendTo(msg.from, msg.to, msg.not);
  }
}


/**
 * This class provides the <code>NotificationInputStream</code> interface
 * on top of an <code>ObjectInputStream</code>.
 * <p>
 * This class is dedicated to the input filter of a <code>TransDriverIn</code>
 * object.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransDriverIn
 */
class ObjectToNotifInputStream implements NotificationInputStream {

  /** underlying input stream */
  ObjectInputStream ois;

  /**
   * Constructor.
   *
   * @param ois		underlying input stream
   */
  ObjectToNotifInputStream(ObjectInputStream ois) {
    this.ois = ois;
  }

  /**
   * Gets a <code>Notification</code> from the stream.
   */
  public Notification readNotification()
    throws ClassNotFoundException, IOException {
    return (Notification) ois.readObject();
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    ois.close();
  }
}

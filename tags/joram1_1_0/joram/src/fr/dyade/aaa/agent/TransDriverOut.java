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

import fr.dyade.aaa.util.Queue;
import java.io.*;


/**
 * Derived <code>DriverOut</code> class providing a specialized
 * <code>sendTo</code> function, used by <code>TransientManager</code> class
 * and <code>TransientServer</code> class.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 * @see		TransientMonitor
 * @see		TransientServer
 */
class TransDriverOut extends DriverOut {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: TransDriverOut.java,v 1.3 2000-10-05 15:15:24 tachkeni Exp $";

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param manager	id of manager or server agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  TransDriverOut(int id, AgentId manager, Queue mq, ObjectOutputStream out) {
    super(id, manager, mq, new ObjectToNotifOutputStream(out));
  }

  /**
   * Sends a notification on the output stream.
   *
   * @param from	actual source agent
   * @param to		actual target agent
   * @param not		notification to send
   */
  void sendTo(AgentId from, AgentId to, Notification not) {
    sendTo(new TransientMessage(from, to, not));
  }
}


/**
 * This class provides the <code>NotificationOutputStream</code> interface
 * on top of an <code>ObjectOutputStream</code>.
 * <p>
 * This class is dedicated to the output filter of a <code>TransDriverOut</code>
 * object.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransDriverOut
 */
class ObjectToNotifOutputStream implements NotificationOutputStream {

  /** underlying output stream */
  ObjectOutputStream oos;

  /**
   * Constructor.
   *
   * @param ois		underlying output stream
   */
  ObjectToNotifOutputStream(ObjectOutputStream oos) {
    this.oos = oos;
  }

  /**
   * Writes a <code>Notification</code> to the stream.
   */
  public void writeNotification(Notification msg) throws IOException {
    oos.writeObject(msg);
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    oos.close();
  }
}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.AbstractJmsReply;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>SessionDaemon</code> daemon is attached to a session for
 * serializing the delivery of asynchronous replies to its consumers.
 */
class SessionDaemon extends fr.dyade.aaa.util.Daemon
{
  /** The session the daemon is attached to. */
  private Session sess;

  /**
   * Constructs a session daemon.
   *
   * @param sess  The session the daemon belongs to.
   */
  public SessionDaemon(Session sess)
  {
    super(sess.toString());
    this.sess = sess;
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "SessionDaemon: " + sess
                                 + ": created.");
  }

  /** The daemon's loop. */
  public void run()
  {
    AbstractJmsReply reply;

    try {
      while (running) {
        canStop = true; 

        // Expecting a reply:
        try {
          reply = (AbstractJmsReply) sess.repliesIn.get();
        }
        catch (Exception iE) {
          continue;
        }
        canStop = false;

        // Processing it through the session:
        sess.distribute(reply);
        sess.repliesIn.pop();
      }
    }
    finally {
      finish();
    }
  }

  /** Shuts the daemon down. */
  public void shutdown()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "SessionDaemon shut down.");
  }

  /** Releases the daemon's resources. */
  public void close()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "SessionDaemon: finished."); 
  }
}

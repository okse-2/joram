/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.jms.AbstractJmsReply;


/**
 * A <code>SessionDaemon</code> daemon is attached to a session for
 * serializing the delivery of asynchronous replies to its consumers.
 */
class SessionDaemon extends com.scalagent.kjoram.util.Daemon
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
    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, "SessionDaemon: " + sess
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
    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, "SessionDaemon shut down.");
  }

  /** Releases the daemon's resources. */
  public void close()
  {
    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, "SessionDaemon: finished."); 
  }
}

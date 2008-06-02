/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package com.scalagent.joram.mom.dest.ftp;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>FtpQueue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>FtpQueueImpl</code> instance.
 *
 * @see FtpQueueImpl
 */
public class FtpQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(FtpQueue.class.getName());

  public static final String FTP_QUEUE_TYPE = "queue.ftp";

  public static String getDestinationType() {
    return FTP_QUEUE_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public FtpQueue() {}

  /**
   * Creates the <tt>FtpQueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new FtpQueueImpl(adminId, prop);
  }
  
  public void react(AgentId from, Notification not)
  throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "FtpQueue.react(" + from + ',' + not + ')');
    if (not instanceof FtpNot) {
      ((FtpQueueImpl) destImpl).ftpNot((FtpNot) not);
    } else
      super.react(from, not);
  }
}

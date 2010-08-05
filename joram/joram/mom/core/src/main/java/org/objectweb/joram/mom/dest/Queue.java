/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.AbortReceiveRequest;
import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.Notification;

/**
 * A <code>Queue</code> agent is an agent hosting a MOM queue, and which
 * behavior is provided by a <code>QueueImpl</code> instance.
 *
 * @see QueueImpl
 */
public class Queue extends Destination implements BagSerializer {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public final byte getType() {
    return DestinationConstants.QUEUE_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public Queue() {}

  /**
   * Creates the <tt>QueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   * @throws RequestException 
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) throws RequestException {
    return new QueueImpl(adminId, prop);
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.react(" + from + ',' + not + ')');

    try {     
      if (not instanceof ReceiveRequest)
        ((QueueImpl)destImpl).receiveRequest(from, (ReceiveRequest) not);
      else if (not instanceof BrowseRequest)
        ((QueueImpl)destImpl).browseRequest(from, (BrowseRequest) not);
      else if (not instanceof AcknowledgeRequest)
        ((QueueImpl)destImpl).acknowledgeRequest((AcknowledgeRequest) not);
      else if (not instanceof DenyRequest)
        ((QueueImpl)destImpl).denyRequest(from, (DenyRequest) not);
      else if (not instanceof AbortReceiveRequest)
        ((QueueImpl)destImpl).abortReceiveRequest(from, (AbortReceiveRequest) not);
      else if (not instanceof ExpiredNot)
        ((QueueImpl) destImpl).handleExpiredNot(from, (ExpiredNot) not);
      else
        super.react(from, not);

    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      if (not instanceof AbstractRequestNot) {
        AbstractRequestNot req = (AbstractRequestNot) not;
        Channel.sendTo(from, new ExceptionReply(req, exc));
      }
    }
  }

  public void readBag(ObjectInputStream in) throws IOException, ClassNotFoundException {
    destImpl.setAgent(this);
    ((QueueImpl) destImpl).readBag(in);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    ((QueueImpl) destImpl).writeBag(out);
  }
}

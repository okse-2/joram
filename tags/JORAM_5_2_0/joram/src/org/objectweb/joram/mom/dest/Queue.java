/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
import org.objectweb.joram.mom.notifications.AbstractRequest;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.BrowseRequest;
import org.objectweb.joram.mom.notifications.DenyRequest;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_GetNbMaxMsg;
import org.objectweb.joram.mom.notifications.Monit_GetPendingMessages;
import org.objectweb.joram.mom.notifications.Monit_GetPendingRequests;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.notifications.SetNbMaxMsgRequest;
import org.objectweb.joram.mom.notifications.SetThreshRequest;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.WakeUpTask;

/**
 * A <code>Queue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>QueueImpl</code> instance.
 *
 * @see QueueImpl
 */
public class Queue extends Destination implements BagSerializer {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static final String QUEUE_TYPE = "queue";

  public static String getDestinationType() {
    return QUEUE_TYPE;
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
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new QueueImpl(adminId, prop);
  }

  private transient WakeUpTask task;

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    task = new WakeUpTask(getId(), WakeUpNot.class);
    task.schedule(((QueueImpl) destImpl).getPeriod());
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Queue.react(" + from + ',' + not + ')');

    try {     
      if (not instanceof SetThreshRequest)
        ((QueueImpl)destImpl).setThreshRequest(from, (SetThreshRequest) not);
      else if (not instanceof SetNbMaxMsgRequest)
        ((QueueImpl)destImpl).setNbMaxMsgRequest(from, (SetNbMaxMsgRequest) not);
      else if (not instanceof Monit_GetPendingMessages)
        ((QueueImpl)destImpl).monitGetPendingMessages(from, (Monit_GetPendingMessages) not);
      else if (not instanceof Monit_GetPendingRequests)
        ((QueueImpl)destImpl).monitGetPendingRequests(from, (Monit_GetPendingRequests) not);
      else if (not instanceof Monit_GetNbMaxMsg)
        ((QueueImpl)destImpl).monitGetNbMaxMsg(from, (Monit_GetNbMaxMsg) not);
      else if (not instanceof ReceiveRequest)
        ((QueueImpl)destImpl).receiveRequest(from, (ReceiveRequest) not);
      else if (not instanceof BrowseRequest)
        ((QueueImpl)destImpl).browseRequest(from, (BrowseRequest) not);
      else if (not instanceof AcknowledgeRequest)
        ((QueueImpl)destImpl).acknowledgeRequest(from, (AcknowledgeRequest) not);
      else if (not instanceof DenyRequest)
        ((QueueImpl)destImpl).denyRequest(from, (DenyRequest) not);
      else if (not instanceof AbortReceiveRequest)
        ((QueueImpl)destImpl).abortReceiveRequest(from, (AbortReceiveRequest) not);
      else if (not instanceof ExpiredNot)
        ((QueueImpl) destImpl).handleExpiredNot(from, (ExpiredNot) not);
//      else if (not instanceof DestinationAdminRequestNot)
//        ((QueueImpl)destImpl).destinationAdminRequestNot(from, (DestinationAdminRequestNot) not);
      else if (not instanceof WakeUpNot) {
        if (task == null)
          task = new WakeUpTask(getId(), WakeUpNot.class);
        task.schedule(((QueueImpl) destImpl).getPeriod());
        ((QueueImpl)destImpl).wakeUpNot((WakeUpNot) not);
      }else
        super.react(from, not);

    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, exc);

      if (not instanceof AbstractRequest) {
        AbstractRequest req = (AbstractRequest) not;
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

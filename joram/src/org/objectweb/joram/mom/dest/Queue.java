/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.notifications.WakeUpNot;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.BagSerializer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;

import fr.dyade.aaa.util.Timer;
import fr.dyade.aaa.util.TimerTask;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;

/**
 * A <code>Queue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>QueueImpl</code> instance.
 *
 * @see QueueImpl
 */
public class Queue extends Destination implements BagSerializer {
  
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
    return new QueueImpl(getId(), adminId, prop);
  }

  private transient Task task;

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
    task = new Task(getId());
    task.schedule();
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof WakeUpNot) {
      if (task == null)
        task = new Task(getId());
      task.schedule();
      ((QueueImpl) destImpl).react(from, not);
    } else {
      super.react(from, not);
    }
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    ((QueueImpl) destImpl).readBag(in);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    ((QueueImpl) destImpl).writeBag(out);
  }

  private class Task extends TimerTask {
    private AgentId to;

    private Task(AgentId to) {
      this.to = to;
    }
    
    /** Method called when the timer expires. */
    public void run() {
      try {
        Channel.sendTo(to, new WakeUpNot());
      } catch (Exception e) {}
    }

    public void schedule() {
      long period = ((QueueImpl) destImpl).getPeriod();

      if (period != -1) {
        try {
          Timer timer = ConnectionManager.getTimer();
          timer.schedule(this, period);
        } catch (Exception exc) {
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
            MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                          "--- " + this + " Queue(...)", exc);
        }
      }
    }
  }
}

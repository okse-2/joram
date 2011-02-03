/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.mail;

import java.util.Properties;

import org.objectweb.joram.mom.dest.DestinationImpl;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.util.Timer;
import fr.dyade.aaa.util.TimerTask;

/**
 * A <code>JavaMailQueue</code> agent is an agent hosting a MOM queue, and
 * which behaviour is provided by a <code>JavaMailQueueImpl</code> instance.
 *
 * @see JavaMailQueueImpl
 */
public class JavaMailQueue extends Queue {

  public static Logger logger =
    Debug.getLogger("com.scalagent.joram.mom.dest.mail.JavaMailQueue");
  
  public static final String MAIL_QUEUE_TYPE = "queue.mail";

  public static String getDestinationType() {
    return MAIL_QUEUE_TYPE;
  }

  /**
   * Empty constructor for newInstance(). 
   */ 
  public JavaMailQueue() {}

  /**
   * Creates the <tt>JavaMailQueueImpl</tt>.
   *
   * @param adminId  Identifier of the queue administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new JavaMailQueueImpl(getId(), adminId, prop);
  }

  private transient PopTask poptask;

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
    poptask = new PopTask(getId());
    poptask.schedule();
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof WakeUpPopNot) {
      if (poptask == null)
        poptask = new PopTask(getId());
      poptask.schedule();
      ((JavaMailQueueImpl) destImpl).doPop();
    } else {
      super.react(from, not);
    }
  }

  /**
   * Timer task responsible for doing a pop.
   */
  private class PopTask extends TimerTask {    
    private AgentId to;
  
    public PopTask(AgentId to) {
      this.to = to;
    }
  
    /** Method called when the timer expires. */
    public void run() {
      try {
        Channel.sendTo(to, new WakeUpPopNot());
      } catch (Exception e) {}
    }

    public void schedule() {
      long period = ((JavaMailQueueImpl) destImpl).getPopPeriod();

      if (period > 0) {
        try {
          Timer timer = ConnectionManager.getTimer();
          timer.schedule(this, period);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "--- " + this + " Queue(...)", exc);
        }
      }
    }
  }
}
/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.util.management.MXWrapper;

import org.objectweb.joram.mom.MomTracing;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>Destination</code> agent is an agent hosting a MOM destination,
 * for example a <tt>Queue</tt> or a <tt>Topic</tt>.
 * Its behaviour is provided by a <code>DestinationImpl</code> instance.
 *
 * @see DestinationImpl
 */
public abstract class Destination extends Agent implements AdminDestinationItf {

  /**
   * The reference of the <code>DestinationImpl</code> instance providing this
   * this agent with its <tt>Destination</tt> behaviour.
   */
  protected DestinationImpl destImpl;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public Destination() {}

  /**
   *  Constructor with parameters for fixing the destination and specifying its
   * identifier.
   *  It is uniquely used by the AdminTopic agent.
   */
  protected Destination(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /**
   * Initializes the destination by creating the <tt>DestinationImpl</tt>
   * object.
   *
   * @param adminId  Identifier of the destination administrator.
   * @param prop     The initial set of properties.
   */
  public final void init(AgentId adminId, Properties properties) {
    destImpl = createsImpl(adminId, properties);
    destImpl.setAgent(this);
  }

  /**
   * Creates the specific implementation.
   *
   * @param adminId  Identifier of the topic administrator.
   * @param prop     The initial set of properties.
   */
  public abstract DestinationImpl createsImpl(AgentId adminId, Properties prop);

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
    MXWrapper.registerMBean(destImpl, "Joram", getMBeanName());
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
    try {
      MXWrapper.unregisterMBean("Joram", getMBeanName());
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
    }
    super.agentFinalize(lastTime);
  }

  private String getMBeanName() {
    return new StringBuffer()
      .append("type=Destination")
      .append(",name=").append((name==nullName)?getId().toString():name)
      .toString();
  }

  /**
   * Reactions to notifications are implemented by the
   * <tt>DestinationImpl</tt> class.
   */
  public void react(AgentId from, Notification not) throws Exception {

    // set agent no save (this is the default).
    setNoSave();

    try {
      destImpl.react(from, not);

      // A DeleteNot notification is finally processed at the
      // Agent level when its processing went successful in
      // the DestinationImpl instance.
      if (not instanceof DeleteNot && destImpl.canBeDeleted()) 
        super.react(from, not);
    } catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }

  protected void setNoSave() {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    this + ": setNoSave().");
    super.setNoSave();
  }

  protected void setSave() {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    this + ": setSave().");
    super.setSave();
  }
}

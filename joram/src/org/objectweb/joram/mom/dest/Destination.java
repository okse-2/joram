/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.notifications.AbstractRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.DestinationAdminRequestNot;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.Monit_FreeAccess;
import org.objectweb.joram.mom.notifications.Monit_GetDMQSettings;
import org.objectweb.joram.mom.notifications.Monit_GetReaders;
import org.objectweb.joram.mom.notifications.Monit_GetStat;
import org.objectweb.joram.mom.notifications.Monit_GetWriters;
import org.objectweb.joram.mom.notifications.RequestGroupNot;
import org.objectweb.joram.mom.notifications.SetDMQRequest;
import org.objectweb.joram.mom.notifications.SetRightRequest;
import org.objectweb.joram.mom.notifications.SpecialAdminRequest;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.agent.UnknownNotificationException;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>Destination</code> agent is an agent hosting a MOM destination,
 * for example a <tt>Queue</tt> or a <tt>Topic</tt>.
 * Its behaviour is provided by a <code>DestinationImpl</code> instance.
 *
 * @see DestinationItf
 */
public abstract class Destination extends Agent implements AdminDestinationItf {
  /** logger */
  public static Logger logger = Debug.getLogger(Destination.class.getName());
  
  /**
   * The reference of the <code>DestinationItf</code> instance providing this
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
   * Initializes the destination by creating the <tt>DestinationItf</tt>
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "agentInitialize(" + firstTime + ')');

    super.agentInitialize(firstTime);
    destImpl.setAgent(this);
    destImpl.initialize(firstTime);
    
    try {
      MXWrapper.registerMBean(destImpl, "Joram#"+AgentServer.getServerId(), getMBeanName());
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, this + " jmx failed", exc);
    }
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize(boolean lastTime) {
    try {
      MXWrapper.unregisterMBean("Joram#"+AgentServer.getServerId(), getMBeanName());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Destination.agentFinalize", exc);
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
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Destination.react(" + from + ',' + not + ')');

    // set agent no save (this is the default).
    setNoSave();
    
    try {
      if (not instanceof SetRightRequest)
        destImpl.setRightRequest(from, (SetRightRequest) not);
      else if (not instanceof SetDMQRequest)
        destImpl.setDMQRequest(from, (SetDMQRequest) not);
      else if (not instanceof Monit_GetReaders)
        destImpl.monitGetReaders(from, (Monit_GetReaders) not);
      else if (not instanceof Monit_GetWriters)
        destImpl.monitGetWriters(from, (Monit_GetWriters) not);
      else if (not instanceof Monit_FreeAccess)
        destImpl.monitFreeAccess(from, (Monit_FreeAccess) not);
      else if (not instanceof Monit_GetDMQSettings)
        destImpl.monitGetDMQSettings(from, (Monit_GetDMQSettings) not);
      else if (not instanceof Monit_GetStat)
        destImpl.monitGetStat(from, (Monit_GetStat) not);
      else if (not instanceof SpecialAdminRequest)
        destImpl.specialAdminRequest(from, (SpecialAdminRequest) not);
      else if (not instanceof ClientMessages)
        destImpl.clientMessages(from, (ClientMessages) not);
      else if (not instanceof UnknownAgent)
        destImpl.unknownAgent(from, (UnknownAgent) not);
      else if (not instanceof RequestGroupNot)
        destImpl.requestGroupNot(from, (RequestGroupNot)not);
      else if (not instanceof DeleteNot) {
        destImpl.deleteNot(from, (DeleteNot) not); 
        if (destImpl.canBeDeleted()) {
          // A DeleteNot notification is finally processed at the
          // Agent level when its processing went successful in
          // the DestinationItf instance.
          super.react(from, not);
        }
      } else if (not instanceof DestinationAdminRequestNot)
        destImpl.destinationAdminRequestNot(from, (DestinationAdminRequestNot) not);
      else
        throw new UnknownNotificationException(not.getClass().getName());
    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, this + ".react()", exc);

      AbstractRequest req = (AbstractRequest) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    } catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }
  
  
  protected void setNoSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": setNoSave().");
    super.setNoSave();
  }

  protected void setSave() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": setSave().");
    super.setSave();
  }
}

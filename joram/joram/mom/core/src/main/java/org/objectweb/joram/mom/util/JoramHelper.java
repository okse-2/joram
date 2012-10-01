/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.util;

import java.util.Properties;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.dest.AdminTopic.DestinationDesc;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * Administration stuff for internal use.
 * <p>
 * The functions use the AdminTopic to perform the operations.
 * The AdminTopic agent is called directly from the calling agent,
 * so special care should be taken to ensure consistency.
 * JoramHelper functions should be called from an agent reaction,
 * and they should save the state of the AdminTopic agent so that
 * the new state is committed with the current reaction.
 */
public class JoramHelper {

  /** class specific logger */
  public static Logger logger = Debug.getLogger(JoramHelper.class.getName());
  
  public static final String JNDI_INITIAL = "java.naming.factory.initial";
  public static final String JNDI_HOST = "scn.naming.factory.host";
  public static final String JNDI_PORT = "scn.naming.factory.port";
  
  /**
   * Create user.
   * 
   * @param userName user name
   * @param userPass user password
   */
  public final static void createUser(String userName, String userPass) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramHelper.createUser(" + userName + ')');
    try {
      SimpleIdentity identity = new SimpleIdentity();
      identity.setIdentity(userName, userPass);
      AdminTopic.CreateUserAndSave(new CreateUserRequest(identity, AgentServer.getServerId(), null), null, "-1");
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Exception:: JoramHelper.createUser", exc);
    }
  }

  /**
   * Instantiating the destination class or retrieving the destination.
   * 
   * @param destNane      destination name
   * @param adminId       other admin (null for TopicAdmin)
   * @param destClassName destination class name
   * @param type          destination type
   * @param properties    destination properties
   * @param fromClassName 
   * @param jndiLookup    true lookup in jndi
   * @param jndiRebind    true re-bind destination in jndi
   * @return destination AgentId
   * @throws Exception
   */
  public final static AgentId createDestination(
      String destName,
      AgentId adminId,
      String destClassName,
      byte type,
      Properties properties,
      boolean freerw) throws Exception {
    AgentId destId = null;
    StringBuffer strbuf = new StringBuffer();
    DestinationDesc destDesc = null;

    try {
      destDesc = AdminTopic.createDestinationAndSave(destName, adminId, properties,
                                                     type, destClassName,
                                                     "JoramHelper", strbuf);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JoramHelper.createDestination, Cannot create destination " + destName, exc);
      throw exc;
    }
    
    destId = destDesc.getId();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramHelper.createDestination info = " + strbuf.toString());
    strbuf.setLength(0);     

    if (freerw) {
      try {
        AdminTopic.setRightAndSave(new SetReader(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "JoramHelper.createDestination, Cannot set FreeReader", exc);
      }
      try {
        AdminTopic.setRightAndSave(new SetWriter(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "JoramHelper.createDestination, Cannot set FreeWriter", exc);
      }
    }

    return destId;
  }
}

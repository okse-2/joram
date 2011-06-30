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

import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.Reference;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.ObjectFactory;
import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.dest.AdminTopic.DestinationDesc;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.UnknownServerException;
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
      logger.log(BasicLevel.DEBUG,
          "createUser(" + userName + ')');
    try {
      SimpleIdentity identity = new SimpleIdentity();
      identity.setIdentity(userName, userPass);
      AdminTopic.CreateUserAndSave(new CreateUserRequest(identity, AgentServer.getServerId(), null), null, "-1");
    } 
    catch (UnknownServerException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
            "Exception:: createUser", e);
    } catch (RequestException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
            "Exception:: createUser", e);
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
            "Exception:: createUser", e);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR,
            "Exception:: createUser", e);
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
      String fromClassName,
      boolean jndiLookup,
      boolean jndiRebind,
      boolean freerw) throws Exception {
    AgentId destId = null;
    Destination dest = null;
    StringBuffer buff = new StringBuffer();
    DestinationDesc destDesc = null;

    // remove jndi reference if rebind is required
    if (jndiRebind) {
      try {

        Properties props = new Properties();
        props.setProperty(JoramHelper.JNDI_INITIAL, AgentServer.getProperty(JoramHelper.JNDI_INITIAL));
        props.setProperty(JoramHelper.JNDI_HOST, AgentServer.getProperty(JoramHelper.JNDI_HOST));
        props.setProperty(JoramHelper.JNDI_PORT, AgentServer.getProperty(JoramHelper.JNDI_PORT));

        //InitialContext ctx = JndiHelper.newInitialContext(props);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "JoramHelper.createDestination ::props = " + props);
        InitialContext jndiCtx = new InitialContext(props);
        jndiCtx.unbind(destName);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "JoramHelper.createDestination :: Exception ",e);
      }
    }

    Exception typeExc = null;
    
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
            "createDestination(" + destName +')');

      if (AdminTopic.isDestinationTableContain(destName)) {
        destDesc =
          AdminTopic.createDestinationAndSave(destName, adminId, properties, type, destClassName, fromClassName, buff);
        destId = destDesc.getId();
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
              "createDestination info = " + buff.toString());
        buff.setLength(0);
      } else if (jndiLookup) {
        Properties props = new Properties();
        props.setProperty(JNDI_INITIAL, AgentServer.getProperty(JNDI_INITIAL));
        props.setProperty(JNDI_HOST, AgentServer.getProperty(JNDI_HOST));
        props.setProperty(JNDI_PORT, AgentServer.getProperty(JNDI_PORT));
        
        //InitialContext ctx = JndiHelper.newInitialContext(props);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "props = " + props);
        InitialContext ctx = new InitialContext(props);
        Object obj =  ctx.lookup(destName);

        if (obj instanceof Reference) {
          ObjectFactory objFactory = new ObjectFactory();
          dest = (Destination) objFactory.getObjectInstance(obj, null, null, null);
        } else if (obj instanceof Destination) {
          dest = (Destination) obj;
        }
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG,
              "createDestination: dest = " + dest);
        if (dest != null) {
          destId = AgentId.fromString(dest.getName());
          // check the destination type
          if (!DestinationConstants.compatible(dest.getType(), type)) {
            typeExc = new Exception("JoramHelper, retrieve wrong destination type: " + destName);
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, typeExc);
          }
        }
      }
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createDestination(" + destName +')', e);
    }

    if (typeExc != null) throw typeExc;
    
    if (destId == null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createDestination id = null");
      try {
        destDesc =         
          AdminTopic.createDestinationAndSave(destName, adminId, properties, type, destClassName, fromClassName, buff);
        destId = destDesc.getId();
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "createDestination info = " + buff.toString());
        buff.setLength(0);     
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "createDestination :: Exception " + buff.toString(), exc);
        buff.setLength(0);
        throw exc;
      }
    }

    if (freerw) {
      try {
        AdminTopic.setRightAndSave(new SetReader(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "Cannot set FreeReader", exc);
      }
      try {
        AdminTopic.setRightAndSave(new SetWriter(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "Cannot set FreeWriter", exc);
      }
    }
    
    if (jndiRebind && dest == null) {
      if (DestinationConstants.compatible(DestinationConstants.QUEUE_TYPE, destDesc.getType()))
        dest = new org.objectweb.joram.client.jms.Queue(""+destDesc.getId());
      else
        dest = new org.objectweb.joram.client.jms.Topic(""+destDesc.getId());
      if (dest != null) {
        Properties props = new Properties();
        props.setProperty(JNDI_INITIAL, AgentServer.getProperty(JNDI_INITIAL));
        props.setProperty(JNDI_HOST, AgentServer.getProperty(JNDI_HOST));
        props.setProperty(JNDI_PORT, AgentServer.getProperty(JNDI_PORT));
        //InitialContext ctx = JndiHelper.newInitialContext(props);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "props = " + props);
        InitialContext ctx = new InitialContext(props);
        ctx.rebind(destName, dest);
      }
    }
    
    return destId;
  }

//  /**
//   * set free writing and reading.
//   * @param destId
//   */
//  public final static void setFreeRW(String destId) {
//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG, "setFreeRW(" + destId + ')');
//    try {
//      AdminTopic.setRightAndSave(new SetReader(null,destId), null, "-1");
//      AdminTopic.setRightAndSave(new SetWriter(null,destId), null, "-1");
//    } catch (Exception e) {
//      if (logger.isLoggable(BasicLevel.ERROR))
//        logger.log(BasicLevel.ERROR, "Exception:: setFreeRW", e);
//    }
//  }
}

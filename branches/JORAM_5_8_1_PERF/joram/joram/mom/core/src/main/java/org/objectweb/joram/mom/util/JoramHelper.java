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
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.ClearQueue;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteQueueMessage;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
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
  
  // JORAM_PERF_BRANCH
  public static final int CLASS_ID_AREA = 0x20000;
  public static final int QUEUE_CLASS_ID = CLASS_ID_AREA + 0;
  public static final int USERAGENT_CLASS_ID = CLASS_ID_AREA + 1;
  public static final int CLIENTCONTEXT_CLASS_ID = CLASS_ID_AREA + 2;
  public static final int RECEIVEREQUEST_CLASS_ID = CLASS_ID_AREA + 3;
  public static final int CLIENTSUBSCRIPTION_CLASS_ID = CLASS_ID_AREA + 4;
  public static final int MESSAGE_CLASS_ID = CLASS_ID_AREA + 5;
  public static final int MESSAGETXID_CLASS_ID = CLASS_ID_AREA + 6;
  public static final int MESSAGEBODYTXID_CLASS_ID = CLASS_ID_AREA + 7;
  public static final int CLIENTCONTEXTTXID_CLASS_ID = CLASS_ID_AREA + 8;
  public static final int CLIENTSUBSCRIPTIONTXID_CLASS_ID = CLASS_ID_AREA + 9;
  
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
   * @param destName      destination name
   * @param adminId       Agent Id. of the administrator (null for TopicAdmin)
   * @param destClassName destination class name
   * @param type          destination type
   * @param properties    destination properties
   * @param freerw        if true rights all users can read and write the destination.
   * @return destination AgentId
   * @throws Exception
   */
  public final static AgentId createDestination(String destName, AgentId adminId, String destClassName,
      byte type, Properties properties, boolean freerw) throws Exception {
    return createDestination(destName, adminId, destClassName, type, properties, freerw, freerw);
  }
  
  /**
   * Instantiating the destination class or retrieving the destination.
   * 
   * @param destNane      destination name
   * @param adminId       Agent Id. of the administrator (null for TopicAdmin)
   * @param destClassName destination class name
   * @param type          destination type
   * @param properties    destination properties
   * @param freerw        if true rights all users can read and write the destination.
   * @param freerw        if true rights all users can read and write the destination.
   * @return destination AgentId
   * @throws Exception
   */
  public final static AgentId createDestination(
      String destName,
      AgentId adminId,
      String destClassName,
      byte type,
      Properties properties,
      boolean freeReading,
      boolean freeWriting) throws Exception {
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

    if (freeReading) {
     try {
        AdminTopic.setRightAndSave(new SetReader(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "JoramHelper.createDestination, Cannot set FreeReader", exc);
      }
    }
    if (freeWriting) {
      try {
        AdminTopic.setRightAndSave(new SetWriter(null, destId.toString()), null, "-1");
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "JoramHelper.createDestination, Cannot set FreeWriter", exc);
      }
    }

    return destId;
  }

  /**
   * Delete a message in a queue
   * @param queueName Name of the queue
   * @param msgId ID of the message to be deleted
   * @return
   */
  public final static boolean deleteQueueMessage(String queueName, String msgId) {
    DestinationDesc queueDesc;
    try {
      queueDesc = AdminTopic.lookupDest(queueName, DestinationConstants.QUEUE_TYPE);
    } catch (RequestException e) {
      return false;
    }
    if(queueDesc==null) return false;
    DeleteQueueMessage req = new DeleteQueueMessage(queueDesc.getId().toString(), msgId);
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, null, null);
    Channel.sendTo(queueDesc.getId(), not);
    return true;
  }
  
  /**
   * Deletes a message in a subscription
   * @param userName Subscriber's name
   * @param subName Subscription name
   * @param msgId ID of the message to be deleted
   * @return
   */
  public final static boolean deleteSubMessage(String userName, String subName, String msgId) {
    AgentId userId = AdminTopic.lookupUser(userName);
    if(userId==null)
      return false;
    DeleteSubscriptionMessage req =
        new DeleteSubscriptionMessage(userId.toString(), subName, msgId);
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, null, null);
    Channel.sendTo(userId, not);
    return true;
  }
  
  /**
   * Clears all pending message of a queue
   * @param queueName Name of the queue
   * @return
   */
  public final static boolean clearQueue(String queueName) {
    DestinationDesc queueDesc;
    try {
      queueDesc = AdminTopic.lookupDest(queueName, DestinationConstants.QUEUE_TYPE);
      if(queueDesc==null) return false;
    } catch (RequestException e) {
      return false;
    }
    ClearQueue req = new ClearQueue(queueDesc.getId().toString());
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, null, null);
    Channel.sendTo(queueDesc.getId(), not);
    return true;
  }
  
  /**
   * Clears all pending message of a subscription
   * @param userName Subscriber's name
   * @param subName Subscription name
   * @return
   */
  public final static boolean clearSubscription(String userName, String subName) {
    AgentId userId = AdminTopic.lookupUser(userName);
    ClearSubscription req =
        new ClearSubscription(userId.toString(), subName);
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, null, null);
    Channel.sendTo(userId, not);
    return true;
  }

}

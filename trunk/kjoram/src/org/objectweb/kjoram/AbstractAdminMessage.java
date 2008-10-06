/*
 * JORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */

package org.objectweb.kjoram;

import java.io.IOException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * An <code>AbstractAdminMessage</code> is a message exchanged between a
 * Admin kjoram client and its proxy.
 */
public abstract class AbstractAdminMessage implements Streamable {
  public static Logger logger = Debug.getLogger(AbstractAdminMessage.class.getName());
  
  protected final static int NULL_CLASS_ID = -1;

  protected final static int ABSTRACT_ADMIN_MESSAGE = 0;
  protected final static int ADD_DOMAIN_REQUEST = 1;
  protected final static int ADD_QUEUE_CLUSTER = 2;
  protected final static int ADD_SERVER_REQUEST = 3;
  protected final static int ADD_SERVICE_REQUEST = 4;
  protected final static int ADMIN_REPLY = 5;
  protected final static int ADMIN_REQUEST = 6;
  protected final static int CLEAR_QUEUE = 7;
  protected final static int CLEAR_SUBSCRIPTION = 8;
  protected final static int CREATE_DESTINATION_REPLY = 9;
  protected final static int CREATE_DESTINATION_REQUEST = 10;
  protected final static int CREATE_USER_REPLY = 11;
  protected final static int CREATE_USER_REQUEST = 12;
  protected final static int DELETE_DESTINATION = 13;
  protected final static int DELETE_QUEUE_MESSAGE = 14;
  protected final static int DELETE_SUBSCRIPTION_MESSAGE = 15;
  protected final static int DELETE_USER = 16;
  protected final static int GET_CONFIG_REQUEST = 17;
  protected final static int GET_DOMAIN_NAMES = 18;
  protected final static int GET_DOMAIN_NAMES_REP = 19;
  protected final static int GET_LOCAL_SERVER = 20;
  protected final static int GET_LOCAL_SERVER_REP = 21;
  protected final static int GET_QUEUE_MESSAGE = 22;
  protected final static int GET_QUEUE_MESSAGE_IDS = 23;
  protected final static int GET_QUEUE_MESSAGE_IDS_REP = 24;
  protected final static int GET_QUEUE_MESSAGE_REP = 25;
  protected final static int GET_SUBSCRIBER_IDS = 26;
  protected final static int GET_SUBSCRIBER_IDS_REP = 27;
  protected final static int GET_SUBSCRIPTION = 28;
  protected final static int GET_SUBSCRIPTION_MESSAGE = 29;
  protected final static int GET_SUBSCRIPTION_MESSAGE_IDS = 30;
  protected final static int GET_SUBSCRIPTION_MESSAGE_IDS_REP = 31;
  protected final static int GET_SUBSCRIPTION_MESSAGE_REP = 32;
  protected final static int GET_SUBSCRIPTION_REP = 33;
  protected final static int GET_SUBSCRIPTIONS = 34;
  protected final static int GET_SUBSCRIPTIONS_REP = 35;
  protected final static int LIST_CLUSTER_QUEUE = 36;
  protected final static int MONITOR_GET_CLUSTER = 37;
  protected final static int MONITOR_GET_CLUSTER_REP = 38;
  protected final static int MONITOR_GET_DMQ_SETTINGS = 39;
  protected final static int MONITOR_GET_DMQ_SETTINGS_REP = 40;
  protected final static int MONITOR_GET_DESTINATIONS = 41;
  protected final static int MONITOR_GET_DESTINATIONS_REP = 42;
  protected final static int MONITOR_GET_FATHER = 43;
  protected final static int MONITOR_GET_FATHER_REP = 44;
  protected final static int MONITOR_GET_FREE_ACCESS = 45;
  protected final static int MONITOR_GET_FREE_ACCESS_REP = 46;
  protected final static int MONITOR_GET_NB_MAX_MSG = 47;
  protected final static int MONITOR_GET_NB_MAX_MSG_REP = 48;
  protected final static int MONITOR_GET_NUMBER_REP = 49;
  protected final static int MONITOR_GET_PENDING_MESSAGES = 50;
  protected final static int MONITOR_GET_PENDING_REQUESTS = 51;
  protected final static int MONITOR_GET_READERS = 52;
  protected final static int MONITOR_GET_SERVERS_IDS = 53;
  protected final static int MONITOR_GET_SERVERS_IDS_REP = 54;
  protected final static int MONITOR_GET_STAT = 55;
  protected final static int MONITOR_GET_STAT_REP = 56;
  protected final static int MONITOR_GET_SUBSCRIPTIONS = 57;
  protected final static int MONITOR_GET_USERS = 58;
  protected final static int MONITOR_GET_USERS_REP = 59;
  protected final static int MONITOR_GET_WRITERS = 60;
  protected final static int MONITOR_REPLY = 61;
  protected final static int MONITOR_REQUEST = 62;
  protected final static int QUEUE_ADMIN_REQUEST = 63;
  protected final static int REMOVE_DOMAIN_REQUEST = 64;
  protected final static int REMOVE_QUEUE_CLUSTER = 65;
  protected final static int REMOVE_SERVER_REQUEST = 66;
  protected final static int REMOVE_SERVICE_REQUEST = 67;
  protected final static int SET_CLUSTER = 68;
  protected final static int SET_DEFAULT_DMQ = 69;
  protected final static int SET_DEFAULT_THRESHOLD = 70;
  protected final static int SET_DESTINATION_DMQ = 71;
  protected final static int SET_FATHER = 72;
  protected final static int SET_NB_MAX_MSG = 73;
  protected final static int SET_QUEUE_THRESHOLD = 74;
  protected final static int SET_READER = 75;
  protected final static int SET_RIGHT = 76;
  protected final static int SET_USER_DMQ = 77;
  protected final static int SET_USER_THRESHOLD = 78;
  protected final static int SET_WRITER = 79;
  protected final static int SPECIAL_ADMIN = 80;
  protected final static int STOP_SERVER_REQUEST = 81;
  protected final static int SUBSCRIPTION_ADMIN_REQUEST = 82;
  protected final static int UNSET_CLUSTER = 83;
  protected final static int UNSET_DEFAULT_DMQ = 84;
  protected final static int UNSET_DEFAULT_THRESHOLD = 85;
  protected final static int UNSET_DESTINATION_DMQ = 86;
  protected final static int UNSET_FATHER = 87;
  protected final static int UNSET_QUEUE_THRESHOLD = 88;
  protected final static int UNSET_READER = 89;
  protected final static int UNSET_USER_DMQ = 90;
  protected final static int UNSET_USER_THRESHOLD = 91;
  protected final static int UNSET_WRITER = 92;
  protected final static int UPDATE_USER = 93;
  protected final static int USER_ADMIN_REQUEST = 94;

  protected int classid;

  protected static final String[] classnames = {
    "org.objectweb.kjoram.AbstractAdminMessage",
    "org.objectweb.kjoram.AddDomainRequest",
    "org.objectweb.kjoram.AddQueueCluster",
    "org.objectweb.kjoram.AddServerRequest",
    "org.objectweb.kjoram.AddServiceRequest",
    "org.objectweb.kjoram.AdminReply",
    "org.objectweb.kjoram.AdminRequest",
    "org.objectweb.kjoram.ClearQueue",
    "org.objectweb.kjoram.ClearSubscription",
    "org.objectweb.kjoram.CreateDestinationReply",
    "org.objectweb.kjoram.CreateDestinationRequest",
    "org.objectweb.kjoram.CreateUserReply",
    "org.objectweb.kjoram.CreateUserRequest",
    "org.objectweb.kjoram.DeleteDestination",
    "org.objectweb.kjoram.DeleteQueueMessage",
    "org.objectweb.kjoram.DeleteSubscriptionMessage",
    "org.objectweb.kjoram.DeleteUser",
    "org.objectweb.kjoram.GetConfigRequest",
    "org.objectweb.kjoram.GetDomainNames",
    "org.objectweb.kjoram.GetDomainNamesRep",
    "org.objectweb.kjoram.GetLocalServer",
    "org.objectweb.kjoram.GetLocalServerRep",
    "org.objectweb.kjoram.GetQueueMessage",
    "org.objectweb.kjoram.GetQueueMessageIds",
    "org.objectweb.kjoram.GetQueueMessageIdsRep",
    "org.objectweb.kjoram.GetQueueMessageRep",
    "org.objectweb.kjoram.GetSubscriberIds",
    "org.objectweb.kjoram.GetSubscriberIdsRep",
    "org.objectweb.kjoram.GetSubscription",
    "org.objectweb.kjoram.GetSubscriptionMessage",
    "org.objectweb.kjoram.GetSubscriptionMessageIds",
    "org.objectweb.kjoram.GetSubscriptionMessageIdsRep",
    "org.objectweb.kjoram.GetSubscriptionMessageRep",
    "org.objectweb.kjoram.GetSubscriptionRep",
    "org.objectweb.kjoram.GetSubscriptions",
    "org.objectweb.kjoram.GetSubscriptionsRep",
    "org.objectweb.kjoram.ListClusterQueue",
    "org.objectweb.kjoram.Monitor_GetCluster",
    "org.objectweb.kjoram.Monitor_GetClusterRep",
    "org.objectweb.kjoram.Monitor_GetDMQSettings",
    "org.objectweb.kjoram.Monitor_GetDMQSettingsRep",
    "org.objectweb.kjoram.Monitor_GetDestinations",
    "org.objectweb.kjoram.Monitor_GetDestinationsRep",
    "org.objectweb.kjoram.Monitor_GetFather",
    "org.objectweb.kjoram.Monitor_GetFatherRep",
    "org.objectweb.kjoram.Monitor_GetFreeAccess",
    "org.objectweb.kjoram.Monitor_GetFreeAccessRep",
    "org.objectweb.kjoram.Monitor_GetNbMaxMsg",
    "org.objectweb.kjoram.Monitor_GetNbMaxMsgRep",
    "org.objectweb.kjoram.Monitor_GetNumberRep",
    "org.objectweb.kjoram.Monitor_GetPendingMessages",
    "org.objectweb.kjoram.Monitor_GetPendingRequests",
    "org.objectweb.kjoram.Monitor_GetReaders",
    "org.objectweb.kjoram.Monitor_GetServersIds",
    "org.objectweb.kjoram.Monitor_GetServersIdsRep",
    "org.objectweb.kjoram.Monitor_GetStat",
    "org.objectweb.kjoram.Monitor_GetStatRep",
    "org.objectweb.kjoram.Monitor_GetSubscriptions",
    "org.objectweb.kjoram.Monitor_GetUsers",
    "org.objectweb.kjoram.Monitor_GetUsersRep",
    "org.objectweb.kjoram.Monitor_GetWriters",
    "org.objectweb.kjoram.Monitor_Reply",
    "org.objectweb.kjoram.Monitor_Request",
    "org.objectweb.kjoram.QueueAdminRequest",
    "org.objectweb.kjoram.RemoveDomainRequest",
    "org.objectweb.kjoram.RemoveQueueCluster",
    "org.objectweb.kjoram.RemoveServerRequest",
    "org.objectweb.kjoram.RemoveServiceRequest",
    "org.objectweb.kjoram.SetCluster",
    "org.objectweb.kjoram.SetDefaultDMQ",
    "org.objectweb.kjoram.SetDefaultThreshold",
    "org.objectweb.kjoram.SetDestinationDMQ",
    "org.objectweb.kjoram.SetFather",
    "org.objectweb.kjoram.SetNbMaxMsg",
    "org.objectweb.kjoram.SetQueueThreshold",
    "org.objectweb.kjoram.SetReader",
    "org.objectweb.kjoram.SetRight",
    "org.objectweb.kjoram.SetUserDMQ",
    "org.objectweb.kjoram.SetUserThreshold",
    "org.objectweb.kjoram.SetWriter",
    "org.objectweb.kjoram.SpecialAdmin",
    "org.objectweb.kjoram.StopServerRequest",
    "org.objectweb.kjoram.SubscriptionAdminRequest",
    "org.objectweb.kjoram.UnsetCluster",
    "org.objectweb.kjoram.UnsetDefaultDMQ",
    "org.objectweb.kjoram.UnsetDefaultThreshold",
    "org.objectweb.kjoram.UnsetDestinationDMQ",
    "org.objectweb.kjoram.UnsetFather",
    "org.objectweb.kjoram.UnsetQueueThreshold",
    "org.objectweb.kjoram.UnsetReader",
    "org.objectweb.kjoram.UnsetUserDMQ",
    "org.objectweb.kjoram.UnsetUserThreshold",
    "org.objectweb.kjoram.UnsetWriter",
    "org.objectweb.kjoram.UpdateUser",
    "org.objectweb.kjoram.UserAdminRequest"
  };

  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractAdminMessage</code>.
   */
  public AbstractAdminMessage() {
    classid = getClassId();
  }

  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(classnames[getClassId()]);
    toString(strbuf);
    return strbuf.toString();
  }
  
  public abstract void toString(StringBuffer strbuf);
  
  
  // ==================================================
  // Streamable interface
  // ==================================================
    static public void write(AbstractAdminMessage msg,
                             OutputXStream os) throws IOException {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "AbstractAdminMessage write: classid = " + msg.getClassId() + 
            ", " + classnames[msg.getClassId()]);
      if (msg == null) {
        os.writeInt(NULL_CLASS_ID);
      } else {
        os.writeInt(msg.getClassId());
        msg.writeTo(os);
      }
    }

    static public AbstractAdminMessage read(InputXStream is) 
    throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
      int classid = -1;
      AbstractAdminMessage msg = null;

      classid = is.readInt();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "AbstractAdminMessage read: classid = " + classid + ", " + classnames[classid]);
      if (classid != NULL_CLASS_ID) {
        msg = (AbstractAdminMessage) Class.forName(classnames[classid]).newInstance();
        msg.readFrom(is);
      }
      return msg;
    }
}

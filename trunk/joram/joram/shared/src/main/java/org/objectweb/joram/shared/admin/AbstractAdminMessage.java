/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.Hashtable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;
import fr.dyade.aaa.common.stream.Streamable;

/**
 * An <code>AbstractAdminMessage</code> is a message exchanged between a
 * Admin Joram client and its proxy.
 */
public abstract class AbstractAdminMessage implements Externalizable, Streamable {
  public static Logger logger = Debug.getLogger(AbstractAdminMessage.class.getName());

  protected final static int NULL_CLASS_ID = -1;

  protected final static int ABSTRACT_ADMIN_MESSAGE = 0;
  protected final static int ADD_DOMAIN_REQUEST = 1;
  protected final static int ADD_DESTINATION_CLUSTER = 2;
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
  protected final static int LIST_CLUSTER_DEST = 36;
  protected final static int XXX_MONITOR_GET_CLUSTER = 37;
  protected final static int LIST_CLUSTER_DEST_REP = 38;
  protected final static int MONITOR_GET_DMQ_SETTINGS = 39;
  protected final static int MONITOR_GET_DMQ_SETTINGS_REP = 40;
  protected final static int MONITOR_GET_DESTINATIONS = 41;
  protected final static int MONITOR_GET_DESTINATIONS_REP = 42;
  protected final static int MONITOR_GET_FATHER = 43;
  protected final static int MONITOR_GET_FATHER_REP = 44;
  protected final static int GET_RIGHTS_REQUEST = 45;
  protected final static int GET_RIGHTS_REPLY = 46;
  protected final static int MONITOR_GET_NB_MAX_MSG = 47;
  protected final static int XXX_MONITOR_GET_NB_MAX_MSG_REP = 48;
  protected final static int MONITOR_GET_NUMBER_REP = 49;
  protected final static int MONITOR_GET_PENDING_MESSAGES = 50;
  protected final static int MONITOR_GET_PENDING_REQUESTS = 51;
  protected final static int XXX_MONITOR_GET_READERS = 52;
  protected final static int MONITOR_GET_SERVERS_IDS = 53;
  protected final static int MONITOR_GET_SERVERS_IDS_REP = 54;
  protected final static int MONITOR_GET_STAT = 55;
  protected final static int MONITOR_GET_STAT_REP = 56;
  protected final static int MONITOR_GET_SUBSCRIPTIONS = 57;
  protected final static int GET_USERS_REQUEST = 58;
  protected final static int GET_USERS_REPLY = 59;
  protected final static int XXX_MONITOR_GET_WRITERS = 60;
  protected final static int XXX_MONITOR_REPLY = 61;
  protected final static int XXX_MONITOR_REQUEST = 62;
  protected final static int XXX_QUEUE_ADMIN_REQUEST = 63;
  protected final static int REMOVE_DOMAIN_REQUEST = 64;
  protected final static int REMOVE_DESTINATION_CLUSTER = 65;
  protected final static int REMOVE_SERVER_REQUEST = 66;
  protected final static int REMOVE_SERVICE_REQUEST = 67;
  protected final static int XXX_SET_CLUSTER = 68;
  protected final static int XXX_SET_DEFAULT_DMQ = 69;
  protected final static int SET_THRESHOLD = 70;
  protected final static int SET_DMQ = 71;
  protected final static int SET_FATHER = 72;
  protected final static int SET_NB_MAX_MSG = 73;
  protected final static int XXX_SET_QUEUE_THRESHOLD = 74;
  protected final static int SET_READER = 75;
  protected final static int SET_RIGHT = 76;
  protected final static int XXX_SET_USER_DMQ = 77;
  protected final static int XXX_SET_USER_THRESHOLD = 78;
  protected final static int SET_WRITER = 79;
  protected final static int XXX_SPECIAL_ADMIN = 80;
  protected final static int STOP_SERVER_REQUEST = 81;
  protected final static int SUBSCRIPTION_ADMIN_REQUEST = 82;
  protected final static int XXX_UNSET_CLUSTER = 83;
  protected final static int XXX_UNSET_DEFAULT_DMQ = 84;
  protected final static int XXX_UNSET_DEFAULT_THRESHOLD = 85;
  protected final static int XXX_UNSET_DESTINATION_DMQ = 86;
  protected final static int XXX_UNSET_FATHER = 87;
  protected final static int XXX_UNSET_QUEUE_THRESHOLD = 88;
  protected final static int UNSET_READER = 89;
  protected final static int XXX_UNSET_USER_DMQ = 90;
  protected final static int XXX_UNSET_USER_THRESHOLD = 91;
  protected final static int UNSET_WRITER = 92;
  protected final static int UPDATE_USER = 93;
  protected final static int USER_ADMIN_REQUEST = 94;
  protected final static int CMD_ADMIN_REQUEST = 95;
  protected final static int CMD_ADMIN_REPLY = 96;
  protected final static int ADD_REMOTE_DEST = 97;
  protected final static int DEL_REMOTE_DEST = 98;
  protected final static int MONITOR_GET_DELIVERED_MESSAGES = 99;
  protected final static int SND_DEST_WEIGHTS = 100;
  protected final static int SET_SYNC_EXCEPTION_ON_FULL_DEST = 101;
  protected final static int SCALE_REQUEST = 102;

  protected int classid;

  protected static final String[] classnames = {
    AbstractAdminMessage.class.getName(),
    AddDomainRequest.class.getName(),
    ClusterAdd.class.getName(),
    AddServerRequest.class.getName(),
    AddServiceRequest.class.getName(),
    AdminReply.class.getName(),
    AdminRequest.class.getName(),
    ClearQueue.class.getName(),
    ClearSubscription.class.getName(),
    CreateDestinationReply.class.getName(),
    CreateDestinationRequest.class.getName(),
    CreateUserReply.class.getName(),
    CreateUserRequest.class.getName(),
    DeleteDestination.class.getName(),
    DeleteQueueMessage.class.getName(),
    DeleteSubscriptionMessage.class.getName(),
    DeleteUser.class.getName(),
    GetConfigRequest.class.getName(),
    GetDomainNames.class.getName(),
    GetDomainNamesRep.class.getName(),
    GetLocalServer.class.getName(),
    GetLocalServerRep.class.getName(),
    GetQueueMessage.class.getName(),
    GetQueueMessageIds.class.getName(),
    GetQueueMessageIdsRep.class.getName(),
    GetQueueMessageRep.class.getName(),
    GetSubscriberIds.class.getName(),
    GetSubscriberIdsRep.class.getName(),
    GetSubscription.class.getName(),
    GetSubscriptionMessage.class.getName(),
    GetSubscriptionMessageIds.class.getName(),
    GetSubscriptionMessageIdsRep.class.getName(),
    GetSubscriptionMessageRep.class.getName(),
    GetSubscriptionRep.class.getName(),
    GetSubscriptions.class.getName(),
    GetSubscriptionsRep.class.getName(),
    ClusterList.class.getName(),
    null,
    ClusterListReply.class.getName(),
    GetDMQSettingsRequest.class.getName(),
    GetDMQSettingsReply.class.getName(),
    GetDestinationsRequest.class.getName(),
    GetDestinationsReply.class.getName(),
    GetFatherRequest.class.getName(),
    GetFatherReply.class.getName(),
    GetRightsRequest.class.getName(),
    GetRightsReply.class.getName(),
    GetNbMaxMsgRequest.class.getName(),
    null,
    GetNumberReply.class.getName(),
    GetPendingMessages.class.getName(),
    GetPendingRequests.class.getName(),
    null,
    GetServersIdsRequest.class.getName(),
    GetServersIdsReply.class.getName(),
    GetStatsRequest.class.getName(),
    GetStatsReply.class.getName(),
    GetSubscriptionsRequest.class.getName(),
    GetUsersRequest.class.getName(),
    GetUsersReply.class.getName(),
    null,
    null,
    null,
    null,
    RemoveDomainRequest.class.getName(),
    ClusterLeave.class.getName(),
    RemoveServerRequest.class.getName(),
    RemoveServiceRequest.class.getName(),
    null,
    null,
    SetThresholdRequest.class.getName(),
    SetDMQRequest.class.getName(),
    SetFather.class.getName(),
    SetNbMaxMsgRequest.class.getName(),
    null,
    SetReader.class.getName(),
    SetRight.class.getName(),
    null,
    null,
    SetWriter.class.getName(),
    null,
    StopServerRequest.class.getName(),
    SubscriptionAdminRequest.class.getName(),
    null,
    null,
    null,
    null,
    null,
    null,
    UnsetReader.class.getName(),
    null,
    null,
    UnsetWriter.class.getName(),
    UpdateUser.class.getName(),
    UserAdminRequest.class.getName(),
    AdminCommandRequest.class.getName(),
    AdminCommandReply.class.getName(),
    AddRemoteDestination.class.getName(),
    DelRemoteDestination.class.getName(),
    GetDeliveredMessages.class.getName(),
    SendDestinationsWeights.class.getName(),
    SetSyncExceptionOnFullDestRequest.class.getName(),
    ScaleRequest.class.getName()
  };
  
  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractAdminMessage</code>.
   */
  public AbstractAdminMessage() {
    classid = getClassId();
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Externalizable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  public final void writeExternal(ObjectOutput out) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractAdminMessage.writeExternal: " + out);
    writeTo((OutputStream) out);
  }

  public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractAdminMessage.readExternal: " + in);
    readFrom((InputStream) in);
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  static public void write(AbstractAdminMessage msg,
                           OutputStream os) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractAdminMessage.write: " + msg);

    if (msg == null) {
      StreamUtil.writeTo(NULL_CLASS_ID, os);
    } else {
      StreamUtil.writeTo(msg.getClassId(), os);
      msg.writeTo(os);
    }
  }

  static public AbstractAdminMessage read(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    int classid = -1;
    AbstractAdminMessage msg = null;

    classid = StreamUtil.readIntFrom(is);
    if (classid != NULL_CLASS_ID) {
      msg = (AbstractAdminMessage) Class.forName(classnames[classid]).newInstance();
      msg.readFrom(is);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractAdminMessage.read: " + msg);

    return msg;
  }
}

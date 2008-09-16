/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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

import java.io.Externalizable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Hashtable;

import org.objectweb.joram.shared.stream.Streamable;
import org.objectweb.joram.shared.stream.StreamUtil;

import fr.dyade.aaa.util.Debug;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * An <code>AbstractAdminMessage</code> is a message exchanged between a
 * Admin Joram client and its proxy.
 */
public abstract class AbstractAdminMessage implements Externalizable, Streamable {
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
    "org.objectweb.joram.shared.admin.AbstractAdminMessage",
    "org.objectweb.joram.shared.admin.AddDomainRequest",
    "org.objectweb.joram.shared.admin.AddQueueCluster",
    "org.objectweb.joram.shared.admin.AddServerRequest",
    "org.objectweb.joram.shared.admin.AddServiceRequest",
    "org.objectweb.joram.shared.admin.AdminReply",
    "org.objectweb.joram.shared.admin.AdminRequest",
    "org.objectweb.joram.shared.admin.ClearQueue",
    "org.objectweb.joram.shared.admin.ClearSubscription",
    "org.objectweb.joram.shared.admin.CreateDestinationReply",
    "org.objectweb.joram.shared.admin.CreateDestinationRequest",
    "org.objectweb.joram.shared.admin.CreateUserReply",
    "org.objectweb.joram.shared.admin.CreateUserRequest",
    "org.objectweb.joram.shared.admin.DeleteDestination",
    "org.objectweb.joram.shared.admin.DeleteQueueMessage",
    "org.objectweb.joram.shared.admin.DeleteSubscriptionMessage",
    "org.objectweb.joram.shared.admin.DeleteUser",
    "org.objectweb.joram.shared.admin.GetConfigRequest",
    "org.objectweb.joram.shared.admin.GetDomainNames",
    "org.objectweb.joram.shared.admin.GetDomainNamesRep",
    "org.objectweb.joram.shared.admin.GetLocalServer",
    "org.objectweb.joram.shared.admin.GetLocalServerRep",
    "org.objectweb.joram.shared.admin.GetQueueMessage",
    "org.objectweb.joram.shared.admin.GetQueueMessageIds",
    "org.objectweb.joram.shared.admin.GetQueueMessageIdsRep",
    "org.objectweb.joram.shared.admin.GetQueueMessageRep",
    "org.objectweb.joram.shared.admin.GetSubscriberIds",
    "org.objectweb.joram.shared.admin.GetSubscriberIdsRep",
    "org.objectweb.joram.shared.admin.GetSubscription",
    "org.objectweb.joram.shared.admin.GetSubscriptionMessage",
    "org.objectweb.joram.shared.admin.GetSubscriptionMessageIds",
    "org.objectweb.joram.shared.admin.GetSubscriptionMessageIdsRep",
    "org.objectweb.joram.shared.admin.GetSubscriptionMessageRep",
    "org.objectweb.joram.shared.admin.GetSubscriptionRep",
    "org.objectweb.joram.shared.admin.GetSubscriptions",
    "org.objectweb.joram.shared.admin.GetSubscriptionsRep",
    "org.objectweb.joram.shared.admin.ListClusterQueue",
    "org.objectweb.joram.shared.admin.Monitor_GetCluster",
    "org.objectweb.joram.shared.admin.Monitor_GetClusterRep",
    "org.objectweb.joram.shared.admin.Monitor_GetDMQSettings",
    "org.objectweb.joram.shared.admin.Monitor_GetDMQSettingsRep",
    "org.objectweb.joram.shared.admin.Monitor_GetDestinations",
    "org.objectweb.joram.shared.admin.Monitor_GetDestinationsRep",
    "org.objectweb.joram.shared.admin.Monitor_GetFather",
    "org.objectweb.joram.shared.admin.Monitor_GetFatherRep",
    "org.objectweb.joram.shared.admin.Monitor_GetFreeAccess",
    "org.objectweb.joram.shared.admin.Monitor_GetFreeAccessRep",
    "org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsg",
    "org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsgRep",
    "org.objectweb.joram.shared.admin.Monitor_GetNumberRep",
    "org.objectweb.joram.shared.admin.Monitor_GetPendingMessages",
    "org.objectweb.joram.shared.admin.Monitor_GetPendingRequests",
    "org.objectweb.joram.shared.admin.Monitor_GetReaders",
    "org.objectweb.joram.shared.admin.Monitor_GetServersIds",
    "org.objectweb.joram.shared.admin.Monitor_GetServersIdsRep",
    "org.objectweb.joram.shared.admin.Monitor_GetStat",
    "org.objectweb.joram.shared.admin.Monitor_GetStatRep",
    "org.objectweb.joram.shared.admin.Monitor_GetSubscriptions",
    "org.objectweb.joram.shared.admin.Monitor_GetUsers",
    "org.objectweb.joram.shared.admin.Monitor_GetUsersRep",
    "org.objectweb.joram.shared.admin.Monitor_GetWriters",
    "org.objectweb.joram.shared.admin.Monitor_Reply",
    "org.objectweb.joram.shared.admin.Monitor_Request",
    "org.objectweb.joram.shared.admin.QueueAdminRequest",
    "org.objectweb.joram.shared.admin.RemoveDomainRequest",
    "org.objectweb.joram.shared.admin.RemoveQueueCluster",
    "org.objectweb.joram.shared.admin.RemoveServerRequest",
    "org.objectweb.joram.shared.admin.RemoveServiceRequest",
    "org.objectweb.joram.shared.admin.SetCluster",
    "org.objectweb.joram.shared.admin.SetDefaultDMQ",
    "org.objectweb.joram.shared.admin.SetDefaultThreshold",
    "org.objectweb.joram.shared.admin.SetDestinationDMQ",
    "org.objectweb.joram.shared.admin.SetFather",
    "org.objectweb.joram.shared.admin.SetNbMaxMsg",
    "org.objectweb.joram.shared.admin.SetQueueThreshold",
    "org.objectweb.joram.shared.admin.SetReader",
    "org.objectweb.joram.shared.admin.SetRight",
    "org.objectweb.joram.shared.admin.SetUserDMQ",
    "org.objectweb.joram.shared.admin.SetUserThreshold",
    "org.objectweb.joram.shared.admin.SetWriter",
    "org.objectweb.joram.shared.admin.SpecialAdmin",
    "org.objectweb.joram.shared.admin.StopServerRequest",
    "org.objectweb.joram.shared.admin.SubscriptionAdminRequest",
    "org.objectweb.joram.shared.admin.UnsetCluster",
    "org.objectweb.joram.shared.admin.UnsetDefaultDMQ",
    "org.objectweb.joram.shared.admin.UnsetDefaultThreshold",
    "org.objectweb.joram.shared.admin.UnsetDestinationDMQ",
    "org.objectweb.joram.shared.admin.UnsetFather",
    "org.objectweb.joram.shared.admin.UnsetQueueThreshold",
    "org.objectweb.joram.shared.admin.UnsetReader",
    "org.objectweb.joram.shared.admin.UnsetUserDMQ",
    "org.objectweb.joram.shared.admin.UnsetUserThreshold",
    "org.objectweb.joram.shared.admin.UnsetWriter",
    "org.objectweb.joram.shared.admin.UpdateUser",
    "org.objectweb.joram.shared.admin.UserAdminRequest"
  };

  
  
  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractAdminMessage</code>.
   */
  public AbstractAdminMessage() {
    classid = getClassId();
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Interface needed for soap serialization
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  /**
   *
   * @exception	IOException
   */
  public Hashtable soapCode() throws IOException {
    Hashtable h = new Hashtable();
    h.put("classname", getClass().getName());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writeTo(baos);
    baos.flush();
    h.put("bytecontent", baos.toByteArray());
    baos.close();

    return h;
  }

  /**
   *
   * @exception	ClassNotFound
   * @exception	InstantiationException
   * @exception	IllegalAccessException
   * @exception	IOException
   */
  public static Object soapDecode(Hashtable h) throws Exception {
    AbstractAdminMessage msg = null;
    ByteArrayInputStream bais = null;

    try {
      String classname = (String) h.get("classname");
      msg = (AbstractAdminMessage) Class.forName(classname).newInstance();
      byte[] content = (byte[]) h.get("bytecontent");
      bais = new ByteArrayInputStream(content);
      msg.readFrom(bais);
    } finally {
      bais.close();
    }

    return msg;
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

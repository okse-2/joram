/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.notifications.AbstractRequestNot;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.ExceptionReply;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.GetRightsReplyNot;
import org.objectweb.joram.mom.notifications.GetRightsRequestNot;
import org.objectweb.joram.mom.notifications.PingNot;
import org.objectweb.joram.mom.notifications.PongNot;
import org.objectweb.joram.mom.notifications.RequestGroupNot;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.proxies.SendRepliesNot;
import org.objectweb.joram.mom.proxies.SendReplyNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.mom.util.InterceptorsHelper;
import org.objectweb.joram.mom.util.MessageInterceptor;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminCommandRequest;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.GetStatsReply;
import org.objectweb.joram.shared.admin.GetStatsRequest;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetRight;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.admin.UnsetReader;
import org.objectweb.joram.shared.admin.UnsetWriter;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.messages.MessageHelper;
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
import fr.dyade.aaa.agent.WakeUpTask;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>Destination</code> class implements the common behavior of
 * MOM destinations.
 */
public abstract class Destination extends Agent implements DestinationMBean, TxDestination {

  public static Logger logger = Debug.getLogger(Destination.class.getName());
  
  public static final String WAKEUP_PERIOD = "period";
  
  /**
   * <code>true</code> if the destination successfully processed a deletion
   * request.
   */
  private boolean deletable = false;

  /** period to run task at regular interval: cleaning, load-balancing, etc. */
  private long period = -1L;
  
  /**
   * Identifier of the destination's administrator.
   * In any case the administration topics are authorized to handle the
   * destination, this mechanism allows an other agent to get the same rights.
   * In particular it is needed to allow user's proxy to handle temporary
   * destinations.
   */
  private AgentId adminId;
  
  protected transient WakeUpTask task;

  /** the interceptors list. */
  private String interceptorsStr = null;
  private transient List interceptors = null;
  
  /**
   * Empty constructor for newInstance().
   */
  public Destination() {
  }

  /**
   * Constructor with parameters for fixing the destination and specifying its
   * identifier.
   * It is uniquely used by the AdminTopic agent.
   */
  protected Destination(String name, boolean fixed, int stamp) {
    super(name, fixed, stamp);
  }

  /**
   * Returns the type of this destination: Queue or Topic.
   * 
   * @return the type of this destination.
   * @see org.objectweb.joram.shared.DestinationConstants#TOPIC_TYPE
   * @see org.objectweb.joram.shared.DestinationConstants#QUEUE_TYPE
   */
  public abstract byte getType();

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   * 
   * @param firstTime true when first called by the factory
   * @exception Exception
   *              unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "agentInitialize(" + firstTime + ')');

    super.agentInitialize(firstTime);
    
    // interceptors
    if (interceptorsStr != null) {
      interceptors = new ArrayList();
      InterceptorsHelper.addInterceptors(interceptorsStr, interceptors);
    }
    
    initialize(firstTime);

    if (getPeriod() > -1)
      task = new WakeUpTask(getId(), WakeUpNot.class, getPeriod());

    try {
      MXWrapper.registerMBean(this, getMBeanName());
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, this + " jmx failed", exc);
    }
  }

  /**
   * Initializes the destination.
   * 
   * @param firstTime true when first called by the factory
   */
  protected abstract void initialize(boolean firstTime);

  /**
   * Finalizes the agent before it is garbaged.
   * 
   * @param lastime true if the destination is deleted
   */
  public void agentFinalize(boolean lastTime) {
    if (task != null) task.cancel();
    
    finalize(lastTime);
    
    try {
      MXWrapper.unregisterMBean(getMBeanName());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Destination.agentFinalize", exc);
    }
    super.agentFinalize(lastTime);
  }

  /**
   * Finalizes the destination before it is garbaged.
   * 
   * @param lastime true if the destination is deleted
   */
  protected abstract void finalize(boolean lastTime);

  public String getMBeanName() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("Joram#").append(AgentServer.getServerId());
    strbuf.append(':');
    strbuf.append("type=Destination,name=").append(getName());

    return strbuf.toString();
  }

  abstract fr.dyade.aaa.common.stream.Properties getStats();
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * 
   * @throws Exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Destination.react(" + from + ',' + not + ')');

    // set agent no save (this is the default).
    setNoSave();

    try {
      if (not instanceof GetRightsRequestNot)
        getRights(from, (GetRightsRequestNot) not);
      else if (not instanceof ClientMessages)
        clientMessages(from, (ClientMessages) not);
      else if (not instanceof UnknownAgent)
        unknownAgent(from, (UnknownAgent) not);
      else if (not instanceof RequestGroupNot)
        requestGroupNot(from, (RequestGroupNot) not);
      else if (not instanceof DeleteNot) {
        deleteNot(from, (DeleteNot) not);
        if (canBeDeleted()) {
          // A DeleteNot notification is finally processed at the
          // Agent level when its processing went successful in
          // the DestinationItf instance.
          super.react(from, not);
        }
      } else if (not instanceof WakeUpNot) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG,
                     "wakeupnot received: current task=" + task + " update=" + ((WakeUpNot) not).update);
        }
        setNoSave();
        if (task == null || ((WakeUpNot) not).update) {
          doSetPeriod(getPeriod());
        }
        if (getPeriod() > 0) {
          wakeUpNot((WakeUpNot) not);
        }
      } else if (not instanceof FwdAdminRequestNot) {
        handleAdminRequestNot(from, (FwdAdminRequestNot) not);
      } else if (not instanceof PingNot) {
        Channel.sendTo(from, new PongNot(getStats()));
      } else {
        throw new UnknownNotificationException(not.getClass().getName());
      }
    } catch (MomException exc) {
      // MOM Exceptions are sent to the requester.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, this + ".react()", exc);

      AbstractRequestNot req = (AbstractRequestNot) not;
      Channel.sendTo(from, new ExceptionReply(req, exc));
    } catch (UnknownNotificationException exc) {
      super.react(from, not);
    }
  }

  private void doSetPeriod(long period) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": setPeriod(" + period + ")." + " -> task " + task);
    if (task == null) {
      task = new WakeUpTask(getId(), WakeUpNot.class, period);
    } else {
      // cancel task
      task.cancel();
      // Schedules the wake up task period.
      if (period > 0)
        task = new WakeUpTask(getId(), WakeUpNot.class, period);
    }
  }
  
  /** <code>true</code> if the READ access is granted to everybody. */
  protected boolean freeReading = false;
  /** <code>true</code> if the WRITE access is granted to everybody. */
  protected boolean freeWriting = false;
  /** Table of the destination readers and writers. */
  protected Hashtable clients = new Hashtable();

  /** READ access value. */
  public static int READ = 1;
  /** WRITE access value. */
  public static int WRITE = 2;
  /** READ and WRITE access value. */
  public static int READWRITE = 3;

  /**
   * Identifier of the dead message queue this destination must send its
   * dead messages to, if any.
   */
  protected AgentId dmqId = null;

  /**
   * Transient <code>StringBuffer</code> used to build message, this buffer
   * is created during agent initialization, then reused during the destination
   * life.
   */
  transient StringBuffer strbuf = new StringBuffer();

  /**
   * date of creation.
   */
  public long creationDate = System.currentTimeMillis();

  protected long nbMsgsReceiveSinceCreation = 0;
  protected long nbMsgsDeliverSinceCreation = 0;
  protected long nbMsgsSentToDMQSinceCreation = 0;

  /**
   * Sets the administrator of the destination.
   * 
   * @param adminId Identifier of the administrator of the destination.
   */
  public final void setAdminId(AgentId adminId) {
    this.adminId = adminId;
  }

  /**
   * Sets the configuration of a <code>Destination</code>. Be careful, this is
   * done a first time before {@link #deploy()}, so the agent is serialized and
   * initialized afterwards.<br>
   * After deployment, firstTime argument is set to false.
   * 
   * @param prop The initial set of properties.
   */ 
  public void setProperties(Properties prop, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, this + ", setProperties.");
    }

    long newPeriod = -1;
    if (prop != null && prop.containsKey(WAKEUP_PERIOD)) {
      try {
        newPeriod = ConversionHelper.toLong(prop.get(WAKEUP_PERIOD));
      } catch (Exception e) {
        logger.log(BasicLevel.ERROR, this + ": error setting destination period", e);
      }
    }
    if (firstTime) {
      period = newPeriod;
    } else {
      setPeriod(newPeriod);
    }
    
    interceptorsStr = null;
    interceptors = null;
    if (prop != null && prop.containsKey(AdminCommandConstant.INTERCEPTORS)) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this + ": setProperties interceptors = " + prop.get(AdminCommandConstant.INTERCEPTORS));
      interceptorsStr = (String) prop.get(AdminCommandConstant.INTERCEPTORS);
    }
    // Interceptors are set the first time in agent initialization
    if (!firstTime) {
      if (interceptorsStr != null) {
        interceptors = new ArrayList();
        InterceptorsHelper.addInterceptors(interceptorsStr, interceptors);
      } else {
        interceptors = null;
      }
    }
  }

  protected boolean isLocal(AgentId id) {
    return (getId().getTo() == id.getTo());
  }

  /** Returns <code>true</code> if the destination might be deleted. */
  private final boolean canBeDeleted() {
    return deletable;
  }

  /**
   * Returns  the period value of this destination, -1 if not set.
   *
   * @return the period value of this destination; -1 if not set.
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets or unsets the period for this destination.
   *
   * @param period The period value to be set or -1 for unsetting previous
   *               value (ignore 0).
   */
  public void setPeriod(long period) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "setPeriod " + period + ", old was " + this.period);
    }
    if (this.period != period) {
      WakeUpNot not = new WakeUpNot();
      not.update = true;
      forward(getId(), not);
      this.period = period;
    }
  }
  
  public abstract void wakeUpNot(WakeUpNot not);
    
  /**
   * Method implementing the reaction to a <code>SetRightRequest</code>
   * notification requesting rights to be set for a user.
   *
   * @param not The notification describing the security modifications.
   */
  protected void setRight(SetRight request,
                       AgentId replyTo,
                       String requestMsgId,
                       String replyMsgId) {
    AgentId user = null;
    if (request.getUserProxId() != null)
      user = AgentId.fromString(request.getUserProxId());

    int right = 0;
    if (request instanceof SetReader)
      right = READ;
    else if (request instanceof SetWriter)
      right = WRITE;
    else if (request instanceof UnsetReader)
      right = - READ;
    else if (request instanceof UnsetWriter)
      right = - WRITE;

    try {
      processSetRight(user, right);
      doRightRequest(user, right);

      replyToTopic(new AdminReply(true, null), replyTo, requestMsgId, replyMsgId);
    } catch (RequestException exc) {
      strbuf.append("Request [").append(request.getClass().getName());
      strbuf.append("], sent to Destination [").append(getId());
      strbuf.append("], successful [false]: ").append(exc.getMessage());
      replyToTopic(new AdminReply(false, strbuf.toString()), replyTo, requestMsgId, replyMsgId);
      logger.log(BasicLevel.ERROR, strbuf.toString());
      strbuf.setLength(0);
    }
  }
 
  /**
   * This method is needed for right revocation.
   * It allows to remove request or subscription from users no longer authorized.
   * 
   * @param user  The user about right modification.
   * @param right The right modification.
   */
  abstract protected void doRightRequest(AgentId user, int right);

  /** set user right. */
  protected void processSetRight(AgentId user, int right) 
    throws RequestException {
    // state change, so save.
    setSave();

    // Setting "all" users rights:
    if (user == null) {
      if (right == READ)
        freeReading = true;
      else if (right == WRITE)
        freeWriting = true;
      else if (right == -READ)
        freeReading = false;
      else if (right == -WRITE)
        freeWriting = false;
      else
        throw new RequestException("Invalid right value: " + right);
    } else {
      // Setting a specific user right:
      Integer currentRight = (Integer) clients.get(user);
      if (right == READ) {
        if (currentRight != null && currentRight.intValue() == WRITE)
          clients.put(user, new Integer(READWRITE));
        else
          clients.put(user, new Integer(READ));
      } else if (right == WRITE) {
        if (currentRight != null && currentRight.intValue() == READ)
          clients.put(user, new Integer(READWRITE));
        else
          clients.put(user, new Integer(WRITE));
      } else if (right == -READ) {
        if (currentRight != null && currentRight.intValue() == READWRITE)
          clients.put(user, new Integer(WRITE));
        else if (currentRight != null && currentRight.intValue() == READ)
          clients.remove(user);
      } else if (right == -WRITE) {
        if (currentRight != null && currentRight.intValue() == READWRITE)
          clients.put(user, new Integer(READ));
        else if (currentRight != null && currentRight.intValue() == WRITE)
          clients.remove(user);
      } else
        throw new RequestException("Invalid right value: " + right);
    }
  }

  /**
   * Method implementing the reaction to a <code>GetRightsRequest</code>
   * notification requesting the rights about this destination.
   *
   * @exception AccessException  If the requester is not the administrator.
   */
  protected void getRights(AgentId from, GetRightsRequestNot not) throws AccessException {
    if (! isAdministrator(from))
      throw new AccessException("ADMIN right not granted");

    AgentId key;
    int right;
    Vector readers = new Vector();
    Vector writers = new Vector();
    for (Enumeration keys = clients.keys(); keys.hasMoreElements();) {
      key = (AgentId) keys.nextElement();
      right = ((Integer) clients.get(key)).intValue();

      if (right == READ || right == READWRITE) readers.add(key);
      if (right == WRITE || right == READWRITE) writers.add(key);
    }
    forward(from, new GetRightsReplyNot(not, freeReading, freeWriting, readers, writers));
  }


  public static String[] _rights = {":R;", ";W;", ":RW;"};

  /**
   * Returns a string representation of the rights set on this destination.
   *
   * @return the rights set on this destination.
   */
  public String[] getRights() {
    String rigths[] = new String[clients.size()];

    AgentId key;
    int right;

    int i=0;
    for (Enumeration keys = clients.keys(); keys.hasMoreElements();) {
      key = (AgentId) keys.nextElement();
      right = ((Integer) clients.get(key)).intValue();
      rigths[i++] = key.toString() + _rights[right -1];
    }

    return rigths;
  }

  /**
   * Returns a string representation of rights set on this destination for a
   * particular user. The user is pointed out by its unique identifier.
   *
   * @param userid The user's unique identifier.
   * @return the rights set on this destination.
   */
  public String getRight(String userid) {
    AgentId key = AgentId.fromString(userid);
    if (key == null) return userid + ":bad user;";
    Integer right = (Integer) clients.get(key);
    if (right == null) return userid + ":unknown;";

    return userid + _rights[right.intValue() -1];
  }

  /**
   * This method allows to exclude some JMX attribute of getJMXStatistics method.
   * It must be overloaded in subclass.
   * 
   * @param attrName name of attribute to test.
   * @return true if the attribute is a valid one.
   */
  protected boolean isValidJMXAttribute(String attrName) {
    if (attrName == null)
      return false;
    return true;
  }
  
  /**
   * Returns values of all valid JMX attributes about the destination.
   * 
   * @return a Hashtable containing the values of all valid JMX attributes about the destination.
   *         The keys are the name of corresponding attributes.
   */
  protected final Hashtable getJMXStatistics() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Destination.getJMXStatistics()");

    Hashtable stats = null;

    try {
      List attributes = MXWrapper.getAttributeNames(getMBeanName());
      if (attributes != null) {
        stats = new Hashtable(attributes.size());
        for (int k = 0; k < attributes.size(); k++) {
          String name = (String) attributes.get(k);
          if (isValidJMXAttribute(name)) {
            Object value = MXWrapper.getAttribute(getMBeanName(), name);
            if ((value != null) && ((value instanceof String) || (value instanceof Number)))
              stats.put(name, value);
          }
        }
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, " getAttributes  on " + getMBeanName() + " error.", exc);
    }

    return stats;
  }

  /**
   * Method implementing the reaction to a <code>ClientMessages</code>
   * notification holding messages sent by a client.
   * <p>
   * If the sender is not a writer on the destination the messages are
   * sent to the DMQ and an exception is thrown. Otherwise, the processing of
   * the received messages is performed in subclasses.
   *
   * @exception AccessException  If the sender is not a WRITER on the
   *              destination.
   */
  protected void clientMessages(AgentId from, ClientMessages not) throws AccessException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Destination.clientMessages(" + from + ',' + not + ')');
    
    // JORAM_PERF_BRANCH
    if (AgentId.nullId.equals(from)) {
      from = not.getProxyId();
    }
    
    // If sender is not a writer, sending the messages to the DMQ, and
    // throwing an exception:
    if (!isWriter(from)) {
      DMQManager dmqManager = new DMQManager(not.getDMQId(), dmqId, getId());
      Message msg;
      for (Iterator msgs = not.getMessages().iterator(); msgs.hasNext();) {
        msg = (Message) msgs.next();
        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(msg, MessageErrorConstants.NOT_WRITEABLE);
        handleDeniedMessage(msg.id, AgentId.fromString(msg.replyToId));
      }
      dmqManager.sendToDMQ();
      throw new AccessException("WRITE right not granted");
    }
    doClientMessages(from, not, true);

    // For topic performance we must send reply after process ClientMessage. It results
    // in a best flow-control of sender allowing the handling of forwarded messages before
    // sender freeing.
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SendReplyNot: " + not.isPersistent() + " " + not.getAsyncSend());
    if (!not.isPersistent() && !not.getAsyncSend()) {
      SendReplyNot replyNot = new SendReplyNot(not.getClientContext(), not.getRequestId());
      forward(from, replyNot);
    }
  }

  /**
   * Method used to do specific actions when a message is denied because of a
   * lack of rights.
   */
  protected void handleDeniedMessage(String msgId, AgentId replyTo) {
    // Nothing to do, useful in admin topic
  }

  /**
   * Method implementing the reaction to an <code>UnknownAgent</code>
   * notification.
   * <p>
   * If the unknown agent is the DMQ, its identifier is set to null. If it
   * is a client of the destination, it is removed. Specific processing is
   * also done in subclasses.
   */
  protected void unknownAgent(AgentId from, UnknownAgent not) {
    if (isAdministrator(not.agent)) {
      if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR,
                       "Admin of dest " + getId() + " does not exist anymore.");
    } else if (not.agent.equals(dmqId)) {
      // state change, so save.
      setSave();
      dmqId = null;
    } else {
      // state change, so save.
      setSave();
      clients.remove(from);
      doUnknownAgent(not);
    }
  }

  /**
   * Method implementing the reaction to a <code>DeleteNot</code>
   * notification requesting the deletion of the destination.
   * <p>
   * The processing is done in subclasses if the sender is an administrator.
   */
  protected void deleteNot(AgentId from, DeleteNot not) {
    if (! isAdministrator(from)) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN,
                   "Unauthorized deletion request from " + from);
    } else {
      doDeleteNot(not);
      setSave(); // state change, so save.
      deletable = true;
    }
  }
  
  protected void requestGroupNot(AgentId from, RequestGroupNot not) {
    Enumeration en = not.getClientMessages();
    ClientMessages theCM = (ClientMessages) en.nextElement();
    Vector replies = new Vector();
    replies.addElement(new SendReplyNot(
        theCM.getClientContext(), 
        theCM.getRequestId()));
    while (en.hasMoreElements()) {
      ClientMessages cm = (ClientMessages) en.nextElement();
      List msgs = cm.getMessages();
      for (int i = 0; i < msgs.size(); i++) {
        theCM.addMessage((Message) msgs.get(i));
      }
      if (! cm.getAsyncSend()) {
        replies.addElement(new SendReplyNot(
            cm.getClientContext(), 
            cm.getRequestId()));
      }
    }
    
    
    try {
      doClientMessages(from, theCM, false);
    } catch (AccessException e) {/* never happens */}

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // for topic performance : must send reply after process ClientMessage
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    if (!not.isPersistent() && replies.size() > 0) {
      forward(from, new SendRepliesNot(replies));
    }
  }

  /**
   * Checks the reading permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a reading permission.
   */
  protected boolean isReader(AgentId client) {
    if (isAdministrator(client) || freeReading)
      return true;

    Integer clientRight = (Integer) clients.get(client);
    if (clientRight == null) return false;
    
    return ((clientRight.intValue() == READ) ||
            (clientRight.intValue() == READWRITE));
  }

  /**
   * Checks the writing permission of a given client agent.
   *
   * @param client  AgentId of the client requesting a writing permission.
   */
  protected boolean isWriter(AgentId client) {
    if (isAdministrator(client) || freeWriting)
      return true;

    Integer clientRight = (Integer) clients.get(client);
    if (clientRight == null) return false;
    
    return ((clientRight.intValue() == WRITE) ||
            (clientRight.intValue() == READWRITE));
  }

  /**
   * Checks the administering permission of a given client agent.
   *
   * @param client  AgentId of the client requesting an admin permission.
   */
  protected boolean isAdministrator(AgentId client) {
    return AdminTopic.isAdminTopicId(client) || client.equals(adminId);
  }

  abstract protected void doClientMessages(AgentId from, ClientMessages not, boolean throwsExceptionOnFullDest) throws AccessException;
  abstract protected void doUnknownAgent(UnknownAgent not);
  abstract protected void doDeleteNot(DeleteNot not);
  
  public void delete() {
    DeleteDestination request = new DeleteDestination(getDestinationId());
    FwdAdminRequestNot deleteNot = new FwdAdminRequestNot(request, null, null);
    Channel.sendTo(AdminTopic.getDefault(), deleteNot);
  }

  // AF (TODO): We have to define an interface that allow subclass to declare
  // a processing through delegation.

  /**
   * This method is needed to add processing before the standard handling. It
   * is used in subclass of {@link Queue} and {@link Topic}.
   * The incoming messages can be modified or deleted during the processing.
   * 
   * @param from The sender of the message
   * @param msgs The incoming messages.
   * @return The incoming messages after processing.
   */
  protected ClientMessages preProcess(AgentId from, ClientMessages msgs) {
    // nothing to do.
    return msgs;
  }

  /**
   * This method is needed to add processing after the standard handling. It
   * is used in subclass of {@link Queue} and {@link Topic}.
   * The incoming messages can be modified or deleted during the processing.
   * 
   * @param from The sender of the message
   * @param msgs The incoming messages.
   * @return The incoming messages after processing.
   */
  protected void postProcess(ClientMessages msgs) {
    // nothing to do.
  }
  
  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    out.writeBoolean(deletable);
    out.writeObject(adminId);
    out.writeBoolean(freeReading);
    out.writeBoolean(freeWriting);
    out.writeObject(clients);    
    out.writeObject(dmqId);
    out.writeLong(creationDate);
    out.writeLong(nbMsgsReceiveSinceCreation);
    out.writeLong(nbMsgsDeliverSinceCreation);
    out.writeLong(nbMsgsSentToDMQSinceCreation);
    out.writeLong(period);
    out.writeObject(interceptorsStr);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    deletable = in.readBoolean();
    adminId = (AgentId)in.readObject();
    freeReading = in.readBoolean();
    freeWriting = in.readBoolean();
    clients = (Hashtable)in.readObject();
    dmqId = (AgentId)in.readObject();
    strbuf = new StringBuffer();
    creationDate = in.readLong();
    nbMsgsReceiveSinceCreation = in.readLong();
    nbMsgsDeliverSinceCreation = in.readLong();
    nbMsgsSentToDMQSinceCreation = in.readLong();
    period = in.readLong();
    interceptorsStr = (String) in.readObject();
  }

  // DestinationMBean interface

  /**
   * Returns the unique identifier of the destination.
   *
   * @return the unique identifier of the destination.
   */
  public final String getDestinationId() {
    return getId().toString();
  }

  /**
   * Tests if this destination is free for reading.
   *
   * @return true if anyone can receive messages from this destination;
   * 	     false otherwise.
   */
  public boolean isFreeReading() {
    return freeReading;
  }

  /**
   * Sets the <code>FreeReading</code> attribute for this destination.
   *
   * @param on	if true anyone can receive message from this destination.
   */
  public void setFreeReading(boolean on) {
    // state change, so save.
    setSave();
    freeReading = on;
  }

  /**
   * Tests if this destination is free for writing.
   *
   * @return true if anyone can send messages to this destination;
   * 	     false otherwise.
   */
  public boolean isFreeWriting() {
    return freeWriting;
  }

  /**
   * Sets the <code>FreeWriting</code> attribute for this destination.
   *
   * @param on	if true anyone can send message to this destination.
   */
  public void setFreeWriting(boolean on) {
    // state change, so save.
    setSave();
    freeWriting = on;
  }

  /**
   * Return the unique identifier of DMQ set for this destination if any.
   *
   * @return the unique identifier of DMQ set for this destination if any;
   *	     null otherwise.
   */
  public String getDMQId() {
    if (dmqId != null) 
      return dmqId.toString();
    return null;
  }

  public AgentId getDMQAgentId() {
    return dmqId;
  }

  /**
   * Returns this destination creation time as a long.
   *
   * @return the destination creation time as UTC milliseconds from the epoch.
   */
  public long getCreationTimeInMillis() {
    return creationDate;
  }

  /**
   * Returns this destination creation time through a <code>String</code> of
   * the form: <code>dow mon dd hh:mm:ss zzz yyyy</code>.
   *
   * @return the destination creation time.
   */
  public String getCreationDate() {
    return new Date(creationDate).toString();
  }

  /**
   * Returns the number of messages received since creation time of this
   * destination.
   *
   * @return the number of messages received since creation time.
   */
  abstract public long getNbMsgsReceiveSinceCreation();
  
  /**
   * Returns the number of messages delivered since creation time of this
   * destination. It includes messages all delivered messages to a consumer,
   * already acknowledged or not.
   *
   * @return the number of messages delivered since creation time.
   */
  public long getNbMsgsDeliverSinceCreation() {
    return nbMsgsDeliverSinceCreation;
  }

  /**
   * Returns the number of erroneous messages forwarded to the DMQ since
   * creation time of this destination..
   *
   * @return the number of erroneous messages forwarded to the DMQ.
   */
  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }

  protected void replyToTopic(AdminReply reply,
                              AgentId replyTo,
                              String requestMsgId,
                              String replyMsgId) {
    Message message = MessageHelper.createMessage(replyMsgId, requestMsgId, getAgentId(), getType());
    try {
      message.setAdminMessage(reply);
      ClientMessages clientMessages = new ClientMessages(-1, -1, message);
      forward(replyTo, clientMessages);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
      throw new Error(exc.getMessage());
    }
  }
  
  protected final void forward(AgentId to, Notification not) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Destination.forward(" + to + ',' + not + ')');
    Channel.sendTo(to, not);
  }
  
  /**
   * 
   * @param from
   * @param not
   */
  protected void handleAdminRequestNot(AgentId from, FwdAdminRequestNot not) {
    AdminRequest adminRequest = not.getRequest();

    if (adminRequest instanceof SetRight) {
      setRight((SetRight) adminRequest,
               not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof GetStatsRequest) {
      replyToTopic(new GetStatsReply(getJMXStatistics()),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof SetDMQRequest) {
      // state change, so save.
      setSave();

      if (((SetDMQRequest)adminRequest).getDmqId() != null)
        dmqId = AgentId.fromString(((SetDMQRequest)adminRequest).getDmqId());
      else
        dmqId = null;

      replyToTopic(new AdminReply(true, null),
                   not.getReplyTo(), not.getRequestMsgId(), not.getReplyMsgId());
    } else if (adminRequest instanceof AdminCommandRequest) {
      processAdminCommand((AdminCommandRequest) adminRequest, not.getReplyTo(), not.getRequestMsgId());
    } else {
      logger.log(BasicLevel.ERROR, "Unknown administration request for destination " + getId());
      replyToTopic(new AdminReply(AdminReply.UNKNOWN_REQUEST, null),
                   not.getReplyTo(),
                   not.getRequestMsgId(),
                   not.getReplyMsgId());
      
    }
  }
  
  /**
   * Proccess an admin command.
   * 
   * @param request The administration request.
   * @param replyTo The destination to reply.
   * @param requestMsgId The JMS message id needed to reply.
   */
  protected void processAdminCommand(AdminCommandRequest request,	AgentId replyTo, String requestMsgId) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "processAdminCommand(" + request + ", " + replyTo + ", " + requestMsgId + ')');
  	Properties prop = null;
  	Properties replyProp = null;
  	try {
			switch (request.getCommand()) {
			case AdminCommandConstant.CMD_ADD_INTERCEPTORS:
				prop = request.getProp();
				if (interceptors == null)
					interceptors = new ArrayList();
				InterceptorsHelper.addInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS), interceptors);
				interceptorsStr = InterceptorsHelper.getListInterceptors(interceptors);
				// state change
				setSave();
				break;
			case AdminCommandConstant.CMD_REMOVE_INTERCEPTORS:
				prop = request.getProp();
				InterceptorsHelper.removeInterceptors((String) prop.get(AdminCommandConstant.INTERCEPTORS), interceptors);
				interceptorsStr = InterceptorsHelper.getListInterceptors(interceptors);
				if (interceptors.isEmpty())
					interceptors = null;
				// state change
				setSave();
				break;
			case AdminCommandConstant.CMD_GET_INTERCEPTORS:
				replyProp = new Properties();
				if (interceptors == null) {
                    replyProp.put(AdminCommandConstant.INTERCEPTORS, "");
				} else {
	                replyProp.put(AdminCommandConstant.INTERCEPTORS, InterceptorsHelper.getListInterceptors(interceptors));
				}
				break;
			case AdminCommandConstant.CMD_REPLACE_INTERCEPTORS:
				prop = request.getProp();
				if (interceptors == null)
					throw new Exception("interceptors == null.");
				InterceptorsHelper.replaceInterceptor(
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_NEW)), 
						((String) prop.get(AdminCommandConstant.INTERCEPTORS_OLD)), 
						interceptors);
						interceptorsStr = InterceptorsHelper.getListInterceptors(interceptors);
				// state change
				setSave();
				break;
			case AdminCommandConstant.CMD_SET_PROPERTIES:
				setProperties(request.getProp(), false);
				setSave();
				break;
			case AdminCommandConstant.CMD_START_HANDLER:
				replyProp = processStartHandler(request.getProp());
				break;
			case AdminCommandConstant.CMD_STOP_HANDLER:
				replyProp = processStopHandler(request.getProp());
				break;

			default:
				throw new Exception("Bad command : \"" + request.getCommand() + "\"");
			}
			// reply
			replyToTopic(new AdminCommandReply(true, AdminCommandConstant.commandNames[request.getCommand()] + " done.", replyProp), replyTo, requestMsgId, requestMsgId);
		} catch (Exception exc) {
			if (logger.isLoggable(BasicLevel.WARN))
				logger.log(BasicLevel.WARN, "", exc);
			replyToTopic(new AdminReply(-1, exc.getMessage()), replyTo, requestMsgId, requestMsgId);
		}
  }
  
  /**
   * Start the acquisition queue/topic handler.
   * 
   * @param prop properties for start if needed (can be null)
   * @return properties for the reply.
   * @throws Exception
   */
  protected Properties processStartHandler(Properties prop) throws Exception { 
  	if (this instanceof AcquisitionQueue) {
  		return ((AcquisitionQueue) this).startHandler(prop);
  	} else if (this instanceof AcquisitionTopic) {
  		return ((AcquisitionTopic) this).startHandler(prop);
  	} else {
  		throw new Exception("processStartHandler :: bad destination.");
  	}
  }
  
  /**
   * Stop the acquisition queue/topic handler.
   * 
   * @param prop properties for start if needed (can be null)
   * @return properties for the reply.
   * @throws Exception
   */
  protected Properties processStopHandler(Properties prop) throws Exception { 
  	if (this instanceof AcquisitionQueue) {
  		return ((AcquisitionQueue) this).stopHandler(prop);
  	} else if (this instanceof AcquisitionTopic) {
  		return ((AcquisitionTopic) this).stopHandler(prop);
  	} else {
  		throw new Exception("processStopHandler :: bad destination.");
  	}
  }
  
  /**
   * Interceptors process
   * 
   * @param msg the message
   * @return message potentially modified by the interceptors (message can be null)
   */
  protected Message processInterceptors(Message msg) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "processInterceptors(" + msg + ')');
  	
  	if (interceptors != null && !interceptors.isEmpty()) {
  		Iterator it = interceptors.iterator();
  		while (it.hasNext()) {
  			MessageInterceptor interceptor = (MessageInterceptor) it.next();
  			if (!interceptor.handle(msg))
  				return null;
  		}
  	}
  	return msg;
  }
  
  /**
   * @return true if interceptors set
   */
  protected boolean interceptorsAvailable() {
  	return interceptors != null && !interceptors.isEmpty();
  }
}

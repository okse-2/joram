/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.dest.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.jms.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.task.*;

/**
 * A <code>JmsClientProxy</code> agent is a proxy for a JMS client.
 */ 
public class JmsClientProxy extends ConnectionFactory
{
  /** Identifier of this proxy administrator. */
  private AgentId adminId;
  /** Id of Scheduler for checking the subscriptions "receive" requests. */
  private AgentId scheduler;

  /** Vector of the proxy's <code>CnxContext</code> instances. */
  private Vector connections;
  /** Key of the active connection. */
  private int currKey = 0;
  /** Reference to the active connection's <code>CnxContext</code> instance. */
  private CnxContext cnx = null;
  /**
   * Table holding the client subscriptions.
   * <p>
   * <b>Key:</b> subscription name<br>
   * <b>Object:</b> subscription
   */
  private Hashtable subsTable;
  /**
   * Table holding the messages destinated to the client subscribers.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Object:</b> message
   */
  private Hashtable messagesTable;

  /**
   * Identifier of this proxy dead message queue, <code>null</code> for DMQ
   * not set.
   */
  private AgentId dmqId = null;
  /**
   * Threshold value, 0 or negative for no threshold, <code>null</code> for
   * value not set.
   */
  private Integer threshold = null;


  /**
   * Constructs a <code>JmsClientProxy</code> agent.
   *
   * @param adminId  Identifier of this proxy administrator.
   */
  public JmsClientProxy(AgentId adminId)
  {
    super();
    super.multiConn = true;
  
    this.adminId = adminId;
    scheduler = Scheduler.getDefault();

    connections = new Vector();
    subsTable = new Hashtable();
    messagesTable = new Hashtable();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this JMS proxy. */
  public String toString()
  {
    return "JmsProxy:" + this.getId();
  }

  /**
   * Specializes this <code>Agent</code> method called when (re)deploying 
   * the proxy.
   * <p>
   * When re-initializing a proxy after a crash, cleans its pre-crash
   * connections state.
   */
  public void initialize(boolean firstTime) throws Exception
  {
    super.initialize(firstTime);

    if (firstTime)
      return;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " re-initializing...");

    // Browsing the pre-crash connections:
    int key;
    while (! connections.isEmpty()) {
      key = ((CnxContext) connections.remove(0)).key;
      try {
        setCnx(key);

        // Denying the non acknowledged messages:
        cnx.deny();

        // Removing or desactivating the subscriptions:
        String subName;
        ClientSubscription sub;
        while (! cnx.activeSubs.isEmpty()) {
          subName = (String) cnx.activeSubs.remove(0);
          sub = (ClientSubscription) subsTable.get(subName);
  
          if (sub.durable) {
            sub.active = false;
            sub.requestId = null;
            sub.denyAll();
  
            if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Durable subscription"
                                      + subName + " de-activated.");
          }
          else {
            subsTable.remove(subName);
            sub.delete();
            sendTo(sub.topicId, new UnsubscribeRequest(null, subName));
  
            if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Temporary"
                                      + " subscription " + name + " deleted.");
          }
        }

        // Deleting the temporary destinations:
        while (! cnx.tempDestinations.isEmpty()) {
          AgentId destId = (AgentId) cnx.tempDestinations.remove(0);
          sendTo(destId, new DeleteNot());
    
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
            MomTracing.dbgProxy.log(BasicLevel.WARN, "Deletes temporary"
                                    + " destination " + destId.toString());
        }
      
        // Clearing the transactions table:
        if (cnx.transactionsTable != null) {
          cnx.transactionsTable.clear();
          cnx.transactionsTable = null;
        }

        cnx = null;
        currKey = 0;
      }
      catch (ProxyException pE) {}
    }
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified InputStream
   * into a <code>JmsInputStream</code>.
   *
   * @param in   An InputStream for this proxy.
   * @return  A NotificationInputStream for this proxy.
   */
  protected NotificationInputStream setInputFilters(InputStream in)
    throws StreamCorruptedException, IOException
  {
    return (new JmsInputStream(in));
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * OutputStream into a <code>JmsOutputStream</code>.
   *
   * @param out  An OutputStream for this proxy.
   * @return  A NotificationOutputStream for this proxy.
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out)
    throws IOException
  {
    return (new JmsOutputStream(out));
  }

  /**
   * This method overrides the <code>ProxyAgent</code> class
   * <code>driverReact</code> method called by the drivers "in" when filtering
   * a notification out of the input stream.
   * <p>
   * Some of the client requests are directly forwarded by the driver to their
   * target destinations. Those requests are:
   * <ul>
   * <li><code>ProducerMessages</code></li>
   * <li><code>ConsumerReceiveRequest</code></li>
   * <li><code>ConsumerSetListRequest</code></li>
   * <li><code>QBrowseRequest</code></li>
   * </ul>
   * <p>
   * A <code>MomExceptionReply</code> wrapping a <code>RequestException</code>
   * might be sent back if the target destination of those requests can't be
   * identified.
   * <p>
   * The other requests are sent to the proxy so that their handling occurs
   * in a reaction.
   */
  protected void driverReact(int key, Notification not)
  {
    if (not instanceof InputNotification) {
      InputNotification iNot = (InputNotification) not;

      if (iNot.getObj() instanceof AbstractJmsRequest) {
        AbstractJmsRequest req = (AbstractJmsRequest) iNot.getObj();
        try {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG)) {
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                                    + " got " + req.getClass().getName()
                                    + " with id: " + req.getRequestId()
                                    + " through cnx: " + key);
          }

          // Requests directly processed by the DriverIn:
          if (req instanceof ProducerMessages)
            driverDoFwd(key, (ProducerMessages) req);
          else if (req instanceof ConsumerReceiveRequest)
            driverDoFwd(key, (ConsumerReceiveRequest) req);
          else if (req instanceof ConsumerSetListRequest)
            driverDoFwd(key, (ConsumerSetListRequest) req);
          else if (req instanceof QBrowseRequest)
            driverDoFwd(key, (QBrowseRequest) req);
          // Other requests are forwarded to the proxy:
          else
            sendTo(this.getId(), new DriverNotification(key, not));
        }
        // Catching an exception due to an invalid agent identifier to
        // forward the request to:
        catch (IllegalArgumentException iE) {
          DestinationException dE =
            new DestinationException("Proxy could not forward the request to"
                                     + " incorrectly identified destination: "
                                     + iE);

          doReply(key, new MomExceptionReply(req.getRequestId(), dE));
        }
      }
    }
    // This case can't happen as a proxy necessarily wraps the data read
    // on the stream in an InputNotification!!
    else {}
  }

  /**
   * Actually forwards the messages sent by the client in a
   * <code>ProducerMessages</code> request as a <code>ClientMessages</code>
   * MOM request directly to a destination, and acknowledges them by sending
   * a <code>ServerReply</code> back.
   */
  private void driverDoFwd(int key, ProducerMessages req)
  {
    ClientMessages not = new ClientMessages(key, req.getRequestId(),
                                            req.getMessages());

    // Setting the producer's DMQ identifier field: 
    if (dmqId != null)
      not.setDMQId(dmqId);
    else
      not.setDMQId(DeadMQueueImpl.id);

    sendTo(AgentId.fromString(req.getTarget()), not);
    doReply(key, new ServerReply(req));
    // Flow control: gives the opportunity to other threads to consume the
    // messages.
    Thread.yield();
  }

  /**
   * Either forwards the <code>ConsumerReceiveRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   */
  private void driverDoFwd(int key, ConsumerReceiveRequest req)
  {
    if (req.getQueueMode()) {
      AgentId to = AgentId.fromString(req.getTarget());
      sendTo(to, new ReceiveRequest(key, req.getRequestId(),
                                    req.getSelector(), req.getTimeToLive(),
                                    false));
    }
    else
      sendTo(this.getId(),
             new DriverNotification(key, new InputNotification(req)));
  }

  /**
   * Either forwards the <code>ConsumerSetListRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue, or wraps it
   * and sends it to the proxy if destinated to a subscription.
   */
  private void driverDoFwd(int key, ConsumerSetListRequest req)
  {
    if (req.getQueueMode()) {
      AgentId to = AgentId.fromString(req.getTarget());
      ReceiveRequest rr = new ReceiveRequest(key, req.getRequestId(),
                                             req.getSelector(), 0, false);
      sendTo(to, rr);
    }
    else
      sendTo(this.getId(),
             new DriverNotification(key, new InputNotification(req)));
  }

  /**
   * Actually forwards the client's <code>QBrowseRequest</code> request as
   * a <code>BrowseRequest</code> MOM request directly to a destination.
   */
  private void driverDoFwd(int key, QBrowseRequest req)
  {
    sendTo(AgentId.fromString(req.getTarget()),
           new BrowseRequest(key, req.getRequestId(), req.getSelector()));
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the JMS client proxy with its specific behaviour.
   * <p>
   * A JMS proxy reacts to:
   * <ul>
   * <li><code>fr.dyade.aaa.agent.DriverNotification</code> notifications,</li>
   * <li><code>SyncReply</code> proxy synchronizing notifications,</li>
   * <li><code>SetDMQRequest</code> admin notifications,</li>
   * <li><code>SetThreshRequest</code> admin notifications,</li>
   * <li><code>AbstractReply</code> destination replies,</li>
   * <li><code>fr.dyade.aaa.task.ConditionNot</code>,</li>
   * <li><code>fr.dyade.aaa.agent.UnknownAgent</code>,</li>
   * <li><code>fr.dyade.aaa.agent.DriverDone</code>,</li>
   * <li><code>fr.dyade.aaa.agent.DeleteNot</code>.</li>
   * </ul>
   * @exception Exception  Thrown at superclass level.
   */ 
  public void react(AgentId from, Notification not) throws Exception
  {
    // Notification forwarded by a DriverIn or sent by the proxy to itself:
    if (not instanceof DriverNotification) {
      DriverNotification dNot = (DriverNotification) not;

      // Notification coming from a external client...
      if (dNot.getNotification() instanceof InputNotification) {
        InputNotification iNot = (InputNotification) dNot.getNotification();

        // ...and containing a client request object:
        if (iNot.getObj() instanceof AbstractJmsRequest)
          doReact(dNot.getDriverKey(), (AbstractJmsRequest) iNot.getObj());

        // As an input stream filter does not accept other objects than
        // AbstractJmsRequest requests, this case can't happen!
        else {}
      }
      // As a driver in necessarily wraps the incoming data into an
      // InputNotification, this case can't happen!
      else {}
    }
    // Notifications setting the DMQ and threshold parameters:
    else if (not instanceof SetDMQRequest)
      doReact((SetDMQRequest) not);
    else if (not instanceof SetThreshRequest)
      doReact((SetThreshRequest) not);
    // Notification sent by the proxy to itself for causal reasons:
    else if (not instanceof SyncReply)
      doReact((SyncReply) not);
    // Notifications sent by a destination:
    else if (not instanceof AbstractReply) 
      doFwd(from, (AbstractReply) not);
    // Platform notifications:
    else if (not instanceof Condition)
      doReact((Condition) not);
    else if (not instanceof UnknownAgent)
      doReact((UnknownAgent) not);
    else if (not instanceof DriverDone)
      doReact(from, (DriverDone) not);
    else if (not instanceof DeleteNot)
      doReact(from, (DeleteNot) not);
    // Notification possibly destinated to this proxy super-classes:
    else
      super.react(from, not);
  }

  
  /**
   * Distributes the client requests to the appropriate reactions.
   * <p>
   * The proxy accepts the following requests:
   * <ul>
   * <li><code>CnxConnectRequest</code></li>
   * <li><code>CnxStartRequest</code></li>
   * <li><code>CnxStopRequest</code></li>
   * <li><code>SessCreateTQRequest</code></li>
   * <li><code>SessCreateTTRequest</code></li>
   * <li><code>ConsumerSubRequest</code></li>
   * <li><code>ConsumerUnsubRequest</code></li>
   * <li><code>ConsumerCloseSubRequest</code></li>
   * <li><code>ConsumerSetListRequest</code></li>
   * <li><code>ConsumerUnsetListRequest</code></li>
   * <li><code>ConsumerReceiveRequest</code></li>
   * <li><code>ConsumerAckRequest</code></li>
   * <li><code>ConsumerDenyRequest</code></li>
   * <li><code>SessAckRequest</code></li>
   * <li><code>SessDenyRequest</code></li>
   * <li><code>TempDestDeleteRequest</code></li>
   * <li><code>XASessPrepare</code></li>
   * <li><code>XASessCommit</code></li>
   * <li><code>XASessRollback</code></li>
   * </ul>
   * <p>
   * A <code>JmsExceptReply</code> is sent back to the client when an
   * exception is thrown by the reaction.
   */ 
  private void doReact(int key, AbstractJmsRequest request)
  {
    try {
      // Updating the active connection if the request is not a new connection
      // request!
      if (! (request instanceof CnxConnectRequest))
        setCnx(key);

      if (request instanceof CnxConnectRequest)
        doReact(key, (CnxConnectRequest) request);
      else if (request instanceof CnxStartRequest)
        doReact((CnxStartRequest) request);
      else if (request instanceof CnxStopRequest)
        doReact((CnxStopRequest) request);
      else if (request instanceof SessCreateTQRequest)
        doReact((SessCreateTQRequest) request);
      else if (request instanceof SessCreateTTRequest)
        doReact((SessCreateTTRequest) request);
      else if (request instanceof ConsumerSubRequest)
        doReact((ConsumerSubRequest) request);
      else if (request instanceof ConsumerUnsubRequest)
        doReact((ConsumerUnsubRequest) request);
      else if (request instanceof ConsumerCloseSubRequest)
        doReact((ConsumerCloseSubRequest) request);
      else if (request instanceof ConsumerSetListRequest)
        doReact((ConsumerSetListRequest) request);
      else if (request instanceof ConsumerUnsetListRequest)
        doReact((ConsumerUnsetListRequest) request);
      else if (request instanceof ConsumerReceiveRequest)
        doReact((ConsumerReceiveRequest) request);
      else if (request instanceof ConsumerAckRequest)
        doReact((ConsumerAckRequest) request);
      else if (request instanceof ConsumerDenyRequest)
        doReact((ConsumerDenyRequest) request);
      else if (request instanceof SessAckRequest)
        doReact((SessAckRequest) request);
      else if (request instanceof SessDenyRequest)
        doReact((SessDenyRequest) request);
      else if (request instanceof TempDestDeleteRequest)
        doReact((TempDestDeleteRequest) request);
      else if (request instanceof XASessPrepare)
        doReact((XASessPrepare) request);
      else if (request instanceof XASessCommit)
        doReact((XASessCommit) request);
      else if (request instanceof XASessRollback)
        doReact((XASessRollback) request);
    }
    catch (MomException mE) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, mE);

      // If the connection has not been lost, sending the exception to
      // the client:
      if (! (mE instanceof ProxyException))
        doReply(new MomExceptionReply(request.getRequestId(), mE));
    }
  }


  /**
   * Method implementing the reaction to a <code>CnxConnectRequest</code>
   * requesting the key of the active connection.
   * <p>
   * It simply sends back a <code>ConnectReply</code> holding the active
   * connection's key.
   */
  private void doReact(int key, CnxConnectRequest req)
  {
    currKey = key;
    cnx = new CnxContext(key);
    connections.add(cnx);
    
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Connection " + key
                              + " opened.");

    doReply(new CnxConnectReply(req, key));
  }

  /**
   * Method implementing the proxy reaction to a <code>CnxStartRequest</code>
   * requesting to start a connection.
   * <p>
   * This method sends the pending <code>ConsumerMessages</code> replies,
   * if any.
   */
  private void doReact(CnxStartRequest req)
  {
    cnx.started = true;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Connection " + currKey
                              + " started.");


    // Delivering the pending deliveries, if any:
    ConsumerMessages pending;
    while (! cnx.repliesBuffer.isEmpty()) {
      pending = (ConsumerMessages) cnx.repliesBuffer.remove(0);

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Sending pending"
                                + " reply " + pending.getCorrelationId());

      doReply(pending);
    }

    // Launching deliveries for the active subscriptions:
    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    for (int i = 0; i < cnx.activeSubs.size(); i++) {
      subName = (String) cnx.activeSubs.get(i);
      sub = (ClientSubscription) subsTable.get(subName);
      consM = sub.deliver();
      if (consM != null)
        doReply(consM);
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>CnxStopRequest</code> requesting to stop a connection.
   * <p>
   * This method sends a <code>ServerReply</code> back.
   */
  private void doReact(CnxStopRequest req)
  {
    cnx.started = false;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Connection " + currKey
                              + "stopped.");

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SessCreateTQRequest</code> requesting the creation of a temporary
   * queue.
   * <p>
   * Creates the queue, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   *
   * @exception RequestException  If the queue could not be deployed.
   */
  private void doReact(SessCreateTQRequest req) throws RequestException
  {
    Queue queue = new Queue(this.getId());
    AgentId qId = queue.getId();

    try {
      queue.deploy();

      // Setting free WRITE right on the queue:
      sendTo(qId, new SetRightRequest(currKey, req.getRequestId(), null, 2));

      // Adding the queue to the table of temporary destinations: 
      cnx.tempDestinations.add(qId);

      sendTo(this.getId(),
             new SyncReply(currKey,
                           new SessCreateTDReply(req, qId.toString())));

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Temporary queue "
                                + qId + " created.");
    }
    catch (IOException iE) {
      queue = null;
      throw new RequestException("Could not deploy temporary queue "
                                 + qId + ": " + iE);
    } 
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SessCreateTTRequest</code> requesting the creation of a temporary
   * topic.
   * <p>
   * Creates the topic, sends it a <code>SetRightRequest</code> for granting
   * WRITE access to all, and wraps a <code>SessCreateTDReply</code> in a
   * <code>SyncReply</code> notification it sends to itself. This latest
   * action's purpose is to preserve causality.
   *
   * @exception RequestException  If the topic could not be deployed.
   */
  private void doReact(SessCreateTTRequest req) throws RequestException
  {
    Topic topic = new Topic(this.getId());
    AgentId tId = topic.getId();

    try {
      topic.deploy();

      // Setting free WRITE right on the topic:
      sendTo(tId, new SetRightRequest(currKey, req.getRequestId(), null, 2));

      // Adding the topic to the table of temporary destinations: 
      cnx.tempDestinations.add(tId);

      sendTo(this.getId(),
             new SyncReply(currKey,
                           new SessCreateTDReply(req, tId.toString())));

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Temporary topic"
                                + tId + " created.");
    }
    catch (IOException iE) {
      topic = null;
      throw new RequestException("Could not deploy temporary topic "
                                 + tId + ": " + iE);
    } 
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerSubRequest</code> requesting to subscribe to a topic.
   * <p>
   * Sends a <code>SubscribeRequest</code> to the target proxy, registers
   * the subscription and acknowledges it.
   */
  private void doReact(ConsumerSubRequest req) throws RequestException
  {
    // Checking the name of the subscription:
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    // In the case of an existing durable subscription, activating it if
    // possible:
    if (sub != null) {
      if (sub.active)
        throw new RequestException("The durable subscription " + subName 
                                   + " has already been activated.");

      // If the subscribed topic does not change:
      if (req.getTarget().equals(sub.topicId.toString())) {
        // If the selector changes, updating the subscription:
        if (sub.selector == null) {
          if (req.getSelector() != null)
            sendTo(AgentId.fromString(req.getTarget()),
                   new SubscribeRequest(req.getRequestId(), subName,
                                        req.getSelector()));
        }
        else if (! sub.selector.equals(req.getSelector()))
          sendTo(AgentId.fromString(req.getTarget()),
                 new SubscribeRequest(req.getRequestId(), subName,
                                      req.getSelector()));
      }
      // Else, unsubscribing to the previous topic and updating the
      // subscription:
      else {
        sendTo(sub.topicId, new UnsubscribeRequest(req.getRequestId(),
                                                   subName));
        sendTo(AgentId.fromString(req.getTarget()),
               new SubscribeRequest(req.getRequestId(), subName,
                                    req.getSelector()));
        sub.topicId = AgentId.fromString(req.getTarget());
      }
      sub.connectionKey = currKey;
      sub.selector = req.getSelector();
      sub.noLocal = req.getNoLocal();
      sub.active = true;
      cnx.activeSubs.add(subName);
    }
    // Else, in the case of a new subscription:
    else {
      // Subscribing to the topic.
      sendTo(AgentId.fromString(req.getTarget()),
             new SubscribeRequest(req.getRequestId(), subName,
                                  req.getSelector()));

      // Registering the subscription:
      subsTable.put(subName, new ClientSubscription(currKey, req));
      cnx.activeSubs.add(subName);

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Subscription " + subName
                                + " created.");
    }
    // Acknowledging the request:
    sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerSetListRequest</code> notifying the creation of a client
   * listener.
   * <p>
   * Sets the listener for the subscription, and if the connection is started,
   * launches a delivery sequence.
   *
   * @exception RequestException  If the subscription can't be retrieved.
   */
  private void doReact(ConsumerSetListRequest req) throws RequestException
  {
    // Getting the subscription:
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new RequestException("Can't set a listener on the non existing"
                                 + " subscription: " + subName);

    sub.requestId = req.getRequestId();
    sub.toListener = true;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Listener has been set on"
                              + " subscription: " + subName);

    // If the connection is started, launching a delivery sequence:
    if (cnx.started) {
      ConsumerMessages consM = sub.deliver();
      if (consM != null)
        doReply(consM);
    }
  }
   
  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerUnsetListRequest</code> notifying that a subscriber listener
   * is unset.
   */
  private void doReact(ConsumerUnsetListRequest req) throws RequestException
  {
    // If the listener was listening to a queue, cancelling any pending reply:
    if (req.queueMode())
      cnx.cancelledRequestId = req.getId();
    // If the listener was listening to a topic, de-activating the
    // subscription:
    else {
      String subName = req.getId();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null)
        throw new RequestException("Can't unset a listener on the non existing"
                                   + " subscription: " + subName);

      sub.requestId = null;
      sub.toListener = false;
    
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Listener has been unset on"
                                + " subscription: " + subName);
    }

    // Acknowledging the request:
    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerCloseSubRequest</code> requesting to desactivate a durable
   * subscription.
   */
  private void doReact(ConsumerCloseSubRequest req) throws RequestException
  {
    // Getting the name of the subscription:
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new RequestException("Can't desactivate non existing"
                                 + " subscription: " + subName);

    // Denying the sub's non acknowledged messages. */
    sub.denyAll();

    // Desactivating the subscription:
    cnx.activeSubs.remove(subName);
    sub.requestId = null;
    sub.toListener = false;
    sub.active = false;
    
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this
                              + " desactivated subscription " + subName);

    // Acknowledging the request:
    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerUnsubRequest</code> requesting to unsubscribe to a topic.
   * <p>
   * Sends an <code>UnsubscribeRequest</code> to the target proxy, removes
   * the subscription and acknowledges the request.
   */
  private void doReact(ConsumerUnsubRequest req) throws RequestException
  {
    // Getting the subscription:
    String subName = req.getTarget();

    ClientSubscription sub = (ClientSubscription) subsTable.remove(subName);

    if (sub == null)
      throw new RequestException("Can't unsubscribe non existing"
                                 + " subscription: " + subName);

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Removing subscription "
                              + subName);

    // Unsubscribing to the topic.
    sendTo(sub.topicId, new UnsubscribeRequest(req.getRequestId(), subName));

    // Removing the subscription:
    cnx.activeSubs.remove(subName);
    sub.delete();

    // Acknowledging the request:
    sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the proxy reaction to a
   * <code>ConsumerReceiveRequest</code> instance, requesting a message from a
   * subscription.
   * <p>
   * This method registers the request and launches a delivery sequence if
   * the connection is started and the request not immediate. If immediate,
   * may bufferize the reply if the connection is stopped. If with a positive
   * timer, may register the request to the Scheduler if not answered.
   */
  private void doReact(ConsumerReceiveRequest req) throws RequestException
  {
    String subName = req.getTarget();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new RequestException("Can't request a message from the unknown"
                                 + " subscription: " + subName);

    String reqId = req.getRequestId();
    sub.toListener = false;

    ConsumerMessages consM = null;
    sub.requestId = reqId;
    boolean replied = false;

    // In the case of an immediate delivery request:
    if (req.getTimeToLive() == -1) {
      // Getting something to deliver, or delivering an empty reply:
      consM = sub.deliver();
      if (consM == null) {
        sub.requestId = null;
        consM = new ConsumerMessages(reqId, subName, false);
      }
      // Replying if the connection is started:
      if (cnx.started)
        doReply(consM);
      // Or buffering:
      else
        cnx.repliesBuffer.add(consM);
    }
    // Else, if the connection is started, trying to deliver messages:
    else if (cnx.started) {
      consM = sub.deliver();
      if (consM != null) {
        doReply(consM);
        replied = true;
      }
    }
    // If no reply was sent, registering the request to the Scheduler if
    // needed: 
    if (! replied && req.getTimeToLive() > 0) {
      AddConditionListener addL = new AddConditionListener(subName);
      sendTo(scheduler, addL);
      ScheduleEvent sched =
        new ScheduleEvent(subName,
                          new java.util.Date(System.currentTimeMillis()
                                             + req.getTimeToLive()));
      sendTo(scheduler, sched);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request registered"
                                      + " to the Scheduler with timer: "
                                      + req.getTimeToLive());
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>SessAckRequest</code> acknowledging messages either on a queue
   * or on a subscription.
   */
  private void doReact(SessAckRequest req)
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      Vector ids = req.getIds();
      sendTo(qId, new AcknowledgeRequest(currKey, req.getRequestId(), ids));
      cnx.ackedIds(qId, ids);
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null)
        sub.acknowledge(req.getIds());
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>SessDenyRequest</code> denying messages either on a queue or on
   * a subscription.
   */
  private void doReact(SessDenyRequest req)
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      Vector ids = req.getIds();
      cnx.ackedIds(qId, ids);
      sendTo(qId, new DenyRequest(currKey, req.getRequestId(), ids));

      // Acknowledging the request unless forbidden:
      if (! req.doNotAck())
        sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null) 
        return;

      sub.deny(req.getIds());

      try {
        setCnx(sub.connectionKey);
   
        // Launching a delivery sequence:
        if (cnx.started) {
          ConsumerMessages consM = sub.deliver();
          if (consM != null)
            doReply(consM);
        } 
      }
      catch (ProxyException pE) {}
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerAckRequest</code> acknowledging a message either on a queue
   * or on a subscription.
   */
  private void doReact(ConsumerAckRequest req)
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      String id = req.getId();
      Vector ids = new Vector();
      ids.add(id);
      sendTo(qId, new AcknowledgeRequest(currKey, req.getRequestId(), ids));
      cnx.ackedId(qId, id);
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        Vector ids = new Vector();
        ids.add(req.getId());
        sub.acknowledge(ids);
      }
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>ConsumerDenyRequest</code> denying a message either on a queue
   * or on a subscription.
   * <p>
   * This request is acknowledged when destinated to a queue.
   */
  private void doReact(ConsumerDenyRequest req)
  {
    if (req.getQueueMode()) {
      AgentId qId = AgentId.fromString(req.getTarget());
      String id = req.getId();
      Vector ids = new Vector();
      ids.add(id);
      cnx.ackedId(qId, id);
      sendTo(qId, new DenyRequest(currKey, req.getRequestId(), ids));

      // Acknowledging the request, unless forbidden:
      if (! req.doNotAck())
        sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
    }
    else {
      String subName = req.getTarget();
      ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

      if (sub == null) 
        return;

      Vector ids = new Vector();
      ids.add(req.getId());
      sub.deny(ids);

      try {
        setCnx(sub.connectionKey);
   
        // Launching a delivery sequence:
        if (cnx.started) {
          ConsumerMessages cM = sub.deliver();
          if (cM != null)
            doReply(cM);
        } 
      }
      catch (ProxyException pE) {}
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a 
   * <code>TempDestDeleteRequest</code> request for deleting a temporary
   * destination.
   * <p>
   * This method sends a <code>fr.dyade.aaa.agent.DeleteNot</code> to the
   * destination and acknowledges the request.
   */
  private void doReact(TempDestDeleteRequest req)
  {
    // Removing the destination from the proxy's list:
    AgentId tempId = AgentId.fromString(req.getTarget());
    cnx.tempDestinations.remove(tempId);

    // Sending the request to the destination:
    sendTo(tempId, new DeleteNot());

    // Acknowledging the request:
    sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XASessPrepare</code> request holding messages and acknowledgements
   * produced in an XA transaction.
   * <p>
   * This method stores the various objects for later commit and acknowledges
   * the request.
   */
  private void doReact(XASessPrepare req)
  {
    if (cnx.transactionsTable == null)
      cnx.transactionsTable = new Hashtable();

    String id = req.getId();
    cnx.transactionsTable.put(id, req);

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XASessCommit</code> request commiting the operations performed
   * in a given transaction.
   * <p>
   * This method actually processes the objects sent at the prepare phase,
   * and acknowledges the request.
   */
  private void doReact(XASessCommit req)
  {
    String id = req.getId();

    XASessPrepare prepare = (XASessPrepare) cnx.transactionsTable.remove(id);
    Vector sendings = prepare.getSendings();
    Vector acks = prepare.getAcks();

    ProducerMessages pM;
    while (! sendings.isEmpty()) {
      pM = (ProducerMessages) sendings.remove(0);
      sendTo(AgentId.fromString(pM.getTarget()),
             new ClientMessages(currKey, pM.getRequestId(), pM.getMessages()));
    }

    while (! acks.isEmpty())
      doReact((SessAckRequest) acks.remove(0));

    if (cnx.transactionsTable.isEmpty())
      cnx.transactionsTable = null;

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XASessRollback</code> request rolling back the operations performed
   * in a given transaction.
   */
  private void doReact(XASessRollback req)
  {
    String id = req.getId();

    String queueName;
    AgentId qId;
    Vector ids;
    while (req.hasMoreQueues()) {
      queueName = req.nextQueue();
      qId = AgentId.fromString(queueName);
      ids = req.getQueueIds(queueName);
      cnx.ackedIds(qId, ids);
      sendTo(qId, new DenyRequest(currKey, req.getRequestId(), ids));
    }

    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    while (req.hasMoreSubs()) {
      subName = req.nextSub();
      sub = (ClientSubscription) subsTable.get(subName);
      if (sub != null) {
        sub.deny(req.getSubIds(subName));

        try {
          setCnx(sub.connectionKey);
 
          // Launching a delivery sequence:
          if (cnx.started) {
            consM = sub.deliver();
            if (consM != null)
              doReply(consM);
          }
        }
        catch (ProxyException pE) {}
      }
    }

    if (cnx.transactionsTable != null) {
      XASessPrepare prepare = (XASessPrepare) cnx.transactionsTable.remove(id);
      Vector acks = prepare.getAcks();

      SessAckRequest ack;
      while (! acks.isEmpty()) {
        ack = (SessAckRequest) acks.remove(0);
        doReact(new SessDenyRequest(ack.getTarget(), ack.getIds(),
                                    ack.getQueueMode(), true));
      }

      if (cnx.transactionsTable.isEmpty())
        cnx.transactionsTable = null;
    }

    sendTo(this.getId(), new SyncReply(currKey, new ServerReply(req)));
  }

  
  /**
   * Method implementing the reaction to a <code>SetDMQRequest</code>
   * instance setting the dead message queue identifier for this proxy.
   */
  private void doReact(SetDMQRequest not)
  {
    dmqId = not.getDmqId();
  }

  /**
   * Method implementing the reaction to a <code>SetThreshRequest</code>
   * instance setting the threshold value for this proxy.
   */
  private void doReact(SetThreshRequest not)
  {
    threshold = not.getThreshold();
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>SyncReply</code> notification sent by itself, wrapping a reply
   * to be sent to a client.
   */
  private void doReact(SyncReply not)
  {
    doReply(not.key, not.reply);
  }

  /**
   * Distributes the JMS replies to the appropriate reactions.
   * <p>
   * JMS proxies react the following replies:
   * <ul>
   * <li><code>QueueMsgReply</code></li>
   * <li><code>BrowseReply</code></li>
   * <li><code>TopicMsgsReply</code></li>
   * <li><code>ExceptionReply</code></li>
   * </ul>
   */
  private void doFwd(AgentId from, AbstractReply rep)
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " got " + rep.getClass().getName()
                              + " with id: " + rep.getCorrelationId()
                              + " from: " + from);

    if (rep instanceof QueueMsgReply)
      doFwd(from, (QueueMsgReply) rep);
    else if (rep instanceof BrowseReply)
      doFwd((BrowseReply) rep);
    else if (rep instanceof TopicMsgsReply)
      doFwd((TopicMsgsReply) rep);
    else if (rep instanceof ExceptionReply)
      doFwd((ExceptionReply) rep);
    else {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Unexpected reply!");
    }
  }


  /**
   * Actually forwards a <code>QueueMsgReply</code> coming from a destination
   * as a <code>ConsumerMessages</code> destinated to the requesting client.
   * <p>
   * If the corresponding connection is stopped, stores the
   * <code>ConsumerMessages</code> for later delivery.
   */
  private void doFwd(AgentId from, QueueMsgReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());

      // If the receive request being replied has been cancelled, denying
      // the message.
      if (rep.getCorrelationId().equals(cnx.cancelledRequestId)) {
        if (rep.getMessage() != null) {
          String msgId = rep.getMessage().getIdentifier();

          if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
            MomTracing.dbgProxy.log(BasicLevel.WARN, "Denying message: "
                                    + msgId);

          Vector ids = new Vector();
          ids.add(msgId);
          sendTo(from, new DenyRequest(rep.getCorrelationId(), ids));
        }
        return;
      }

      // Building the reply and storing the wrapped message id for later
      // denying in the case of a failure:
      ConsumerMessages jRep = new ConsumerMessages(rep.getCorrelationId(),
                                                   rep.getMessage(),
                                                   from.toString(), true);
      if (jRep.getMessage() != null)
        cnx.addId(from, jRep.getMessage().getIdentifier());

      // If the connection is started, delivering the message, or buffering
      // it:
      if (cnx.started)
        doReply(jRep);
      else
        cnx.repliesBuffer.add(jRep);
    }
    // The connection is lost: denying the message:
    catch (ProxyException pE) {
      if (rep.getMessage() != null) {
        String msgId = rep.getMessage().getIdentifier();

        if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
          MomTracing.dbgProxy.log(BasicLevel.WARN, "Denying message: "
                                  + msgId);

        Vector ids = new Vector();
        ids.add(msgId);
        sendTo(from, new DenyRequest(rep.getCorrelationId(), ids));
      }
    }
  }


  /**
   * Actually forwards a <code>BrowseReply</code> coming from a
   * destination as a <code>QBrowseReply</code> destinated to the
   * requesting client.
   */
  private void doFwd(BrowseReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());
      doReply(new QBrowseReply(rep));
    }
    // The connection is lost; nothing to do.
    catch (ProxyException pE) {}
  }


  /**
   * Method implementing the proxy reaction to a <code>TopicMsgsReply</code>
   * holding messages published by a topic.
   */
  private void doFwd(TopicMsgsReply rep)
  {
    // Storing the received messages:
    messagesTable.putAll(rep.getAllMessages());
    // Keeping the received messages identifiers:
    Enumeration receivedKeys = rep.getAllMessages().keys();

    // Browsing the target subscriptions:
    String subName;
    ClientSubscription sub;
    ConsumerMessages consM;
    while (rep.hasMoreSubs()) {
      subName = rep.nextSub();
      sub = (ClientSubscription) subsTable.get(subName);
 
      // The subscription still exists: 
      if (sub != null) {
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Adding delivered"
                                  + " messages on sub " + subName);
        sub.addIds(rep.getIds(subName));

        // If it is active, and its connection started, launching a delivery
        // sequence:
        if (sub.active) {
          try {
            // Updating the active connection:
            setCnx(sub.connectionKey);

            if (cnx.started) {
              consM = sub.deliver();
              if (consM != null) 
                doReply(consM);
            }
          }
          // The connection is lost: nothing to do.
          catch (ProxyException pE) {}
        }
      }
    }
    // Checking among the newly stored messages if some won't be delivered:
    String id;
    Message msg;
    while (receivedKeys.hasMoreElements()) {
      id = (String) receivedKeys.nextElement();
      msg = (Message) messagesTable.get(id);
      // No acknowledgement expected for this message: removing it.
      if (msg.acksCounter == 0)
        messagesTable.remove(id);
    }
  }


  /**
   * Actually forwards an <code>ExceptionReply</code> coming from a destination
   * as a <code>MomExceptionReply</code> destinated to the requesting client.
   */
  private void doFwd(ExceptionReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());
      doReply(new MomExceptionReply(rep.getCorrelationId(),
                                    rep.getException()));
    }
    // The connection is lost; nothing to do.
    catch (ProxyException pE) {}
  }


  /**
   * Method implementing the proxy reaction to a
   * <code>fr.dyade.aaa.task.Condition</code> instance sent by the Scheduler
   * service, notifying the expiry of a request.
   * <p>
   * The method usually sends a <code>ConsumerMessages</code> back to the
   * expired request's requester, and a
   * <code>fr.dyade.aaa.task.RemoveConditionListener</code> to the
   * scheduler.
   */
  private void doReact(Condition not)
  {
    String subName = not.name;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " notified of request expiry for sub "
                              + subName);

    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    // The target subscription still exists: delivering a null to the
    // subscriber if the connection is started, or buffering it.
    if (sub != null && sub.requestId != null) {
      try {
        setCnx(sub.connectionKey);

        ConsumerMessages consM = new ConsumerMessages(sub.requestId, subName,
                                                      false);
        sub.requestId = null;

        if (cnx.started)
          doReply(consM);
        else
          cnx.repliesBuffer.add(consM);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Request on sub "
                                        + subName + " answered by"
                                        + " null message because of expiry.");
      }
      catch (ProxyException pE) {}
    }
    // Removing the condition listener corresponding to this request.
    RemoveConditionListener remL = new RemoveConditionListener(not.name);
    sendTo(scheduler, remL);
  }


  /**
   * Method implementing the JMS proxy reaction to an <code>UnknownAgent</code>
   * notification notifying that a destination does not exist.
   * <p>
   * If the request was a subscribe request, the method removes the 
   * corresponding subscriptions. If the request was messages sending, the
   * messages are sent to the DMQ. For all requests, sends also an
   * <code>JmsExceptReply</code> to the requester.
   * <p>
   * This case might also happen when sending a <code>ClientMessages</code>
   * to a dead message queue. In that case, the invalid DMQ identifier is set
   * to null.
   */
  private void doReact(UnknownAgent uA)
  {
    Notification not = uA.not;
    AgentId agId = uA.agent;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
      MomTracing.dbgProxy.log(BasicLevel.WARN, "--- " + this
                              + " notified of invalid destination: "
                              + agId.toString());

    if (not instanceof AbstractRequest) {
      AbstractRequest req = (AbstractRequest) not;

      // If the sent request was messages destinated to a queue, forwarding
      // them to the DMQ:
      if (req instanceof ClientMessages) {
        // If the queue actually was a dead message queue, updating its
        // identifier:
        if (dmqId != null && agId.equals(dmqId))
          dmqId = null;
        if (DeadMQueueImpl.id != null && agId.equals(DeadMQueueImpl.id))
          DeadMQueueImpl.id = null;
        sendToDMQ(((ClientMessages) req).getMessages());
      }
      // If the sent request was a subscribe request, removing the sub:
      else if (req instanceof SubscribeRequest) {
        String name = ((SubscribeRequest) req).getName();
        ClientSubscription sub = (ClientSubscription) subsTable.remove(name);
        cnx.activeSubs.remove(name);
      }
      doReply(new MomExceptionReply(req.getRequestId(),
                                    new DestinationException("Destination "
                                                             + agId + " does"
                                                             + " not"
                                                             + " exist.")));

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Connection "
                                + req.getConnectionKey() + " notified of"
                                + " the deletion of destination " + agId);
    }
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>fr.dyade.aaa.agent.DriverDone</code> notification notifying a
   * closed or broken connection.
   * <p>
   * The method denies the non acknowledged messages delivered to this
   * connection, and deletes its temporary subscriptions and destinations.
   *
   * @exception Exception  Thrown at super class level.
   */
  private void doReact(AgentId from, DriverDone not) throws Exception
  {
    int cKey = not.getDriverKey();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " notified of the closing connection: "
                              + cKey);
    try {
      setCnx(cKey);

      // Denying the non acknowledged messages:
      cnx.deny();

      // Removing or desactivating the subscriptions:
      String subName = null;
      ClientSubscription sub;
      while (! cnx.activeSubs.isEmpty()) {
        subName = (String) cnx.activeSubs.remove(0);
        sub = (ClientSubscription) subsTable.get(subName);
  
        if (sub.durable) {
          sub.active = false;
          sub.requestId = null;
          sub.denyAll();
  
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Durable subscription"
                                    + subName + " de-activated.");
        }
        else {
          subsTable.remove(subName);
          sub.delete();
          sendTo(sub.topicId, new UnsubscribeRequest(null, subName));
  
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Temporary subscription"
                                    + name + " deleted.");
        }
      }

      // Deleting the temporary destinations:
      while (! cnx.tempDestinations.isEmpty()) {
        AgentId destId = (AgentId) cnx.tempDestinations.remove(0);
        sendTo(destId, new DeleteNot());
    
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
          MomTracing.dbgProxy.log(BasicLevel.WARN, "Deletes temporary"
                                  + " destination " + destId.toString());
      }
      
      // Clearing the transactions table:
      if (cnx.transactionsTable != null)
        cnx.transactionsTable.clear();

      connections.remove(cnx);
      cnx = null;
      currKey = 0;
    }
    catch (ProxyException pE) {}

    super.react(from, not);
  }


  /**
   * Method implementing the JMS proxy reaction to a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> notification notifying the
   * proxy to be deleted.
   *
   * @exception Exception  Thrown at super class level.
   */
  private void doReact(AgentId from, DeleteNot not) throws Exception
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " notified to be deleted.");

    // If sender is not the administrator, ignoring the notification:
    if (! from.equals(adminId)) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Deletion request received"
                                + " from invalid agent: " + from);
      return;
    }

    // Notifying the connected clients:
    int key;
    while (! connections.isEmpty()) {
      key = ((CnxContext) connections.remove(0)).key;
      try {
        setCnx(key);

        doReply(new MomExceptionReply(null,
                                      new ProxyException("Client proxy is "
                                                         + "deleted.")));

        // Denying the non acknowledged messages:
        cnx.deny();

        // Deleting the temporary destinations:
        while (! cnx.tempDestinations.isEmpty()) {
          AgentId destId = (AgentId) cnx.tempDestinations.remove(0);
          sendTo(destId, new DeleteNot());
  
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Sending DeleteNot to"
                                    + " temporary destination "
                                    + destId.toString());
        }

        // Removing all proxy's subscriptions:
        Enumeration subNames = subsTable.keys();
        String subName;
        ClientSubscription sub;
        Vector tIds = new Vector();
        while (subNames.hasMoreElements()) {
          subName = (String) subNames.nextElement();
          sub = (ClientSubscription) subsTable.remove(subName);
          if (! tIds.contains(sub.topicId)) {
            tIds.add(sub.topicId);
            sendTo(sub.topicId, new UnsubscribeRequest(null, null));
          }
        }
        tIds.removeAllElements();
        tIds = null;
        messagesTable.clear();
      }
      catch (ProxyException pE) {}
    }
    super.react(from, not);
  }
   
  
  /**
   * Updates the reference to the active connection.
   *
   * @param key  Key of the activated connection.
   *
   * @exception ProxyException  If the connection has actually been closed or
   *              lost.
   */
  private void setCnx(int key) throws ProxyException
  {
    // If the required connection is the last used, no need to update the
    // references:
    if (key == currKey)
      return;

    // Else, updating the cnx reference:
    int i = 0;
    while (i < connections.size()) {
      cnx = (CnxContext) connections.get(i);
      if (cnx.key == key) {
        currKey = key;
        return;
      }
      i++;
    }
    // If connection not found, throwing an exception:
    currKey = 0;
    cnx = null;
    throw new ProxyException("Connection " + key + " is closed or broken.");
  }

 
  /**
   * Method used for sending an <code>AbstractJmsReply</code> back to an
   * external client through the active connection.
   *
   * @param rep  The reply to send.
   */
  private void doReply(AbstractJmsReply reply)
  {
    doReply(currKey, reply);
  }

  /**
   * Method used for sending an <code>AbstractJmsReply</code> back to an
   * external client through a given connection.
   *
   * @param key  The connection through witch replying.
   * @param rep  The reply to send.
   */
  private void doReply(int key, AbstractJmsReply reply)
  {
    OutputNotification oN = new OutputNotification(reply);

    try { 
      super.sendOut(key, oN);
    }
    // Closed or broken connection:
    catch (Exception e) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Connection " + key
                                + " broken or closed: could not send reply: "
                                + reply.getClass().getName() + " with id: "
                                + reply.getCorrelationId());
    }
  }

  /**
   * Method used for sending a vector of messages to the appropriate dead
   * message queue.
   *
   * @param messages  Vector of dead messages.
   */
  private void sendToDMQ(Vector messages)
  {
    if (dmqId != null)
      sendTo(dmqId, new ClientMessages(null, messages));
    else if (DeadMQueueImpl.id != null)
      sendTo(DeadMQueueImpl.id, new ClientMessages(null, messages));
  }


  /** 
   * The <code>CnxContext</code> class is used for managing objects related
   * to a given client connection.
   */
  private class CnxContext implements java.io.Serializable
  {
    /** The key of the connection. */
    private int key;
    /** <code>true</code> if the connection is started. */
    private boolean started = false;
    /** Vector of the connection's temporary destinations. */
    private Vector tempDestinations;
    /** Vector of active subscriptions. */
    private Vector activeSubs;
    /** Buffer of pending replies waiting for the connection to be started. */
    private Vector repliesBuffer;
    /**
     * Table holding the objects sent by a preparing transaction and waiting
     * to be commited.
     * <p>
     * <b>Key:</b> transaction identifier<br>
     * <b>Object:</b> <code>XA&lt;Q/T&gt;SessPrepare</code> object
     */
    private Hashtable transactionsTable;
    /**
     * Table holding the non acknowledged messages identifiers delivered by
     * queues.
     * <p>
     * <b>Key:</b> queue identifier<br>
     * <b>Object:</b> vector of message identifiers
     */
    private Hashtable qDeliveries;
    /** Identifier of the currently "used" queue. */
    private AgentId qId = null;
    /** Reference to the vector of msg ids of the currently "used" queue. */
    private Vector ids = null;
    /**
     * Identifier of a cancelled "receive" request, set when a listener has
     * been unset.
     */
    String cancelledRequestId = null;

    /**
     * Constructs a <code>CnxContext</code> instance.
     *
     * @param key  Key of the connection.
     */
    private CnxContext(int key)
    {
      this.key = key;
      tempDestinations = new Vector();
      activeSubs = new Vector();
      repliesBuffer = new Vector();
      qDeliveries = new Hashtable();
    }
    

    /** Adds a delivered and not yet acknowledged message identifier. */
    private void addId(AgentId qId, String msgId)
    {
      updateCurrentQueue(qId);
      ids.add(msgId);
    }

    /**
     * Notifies the <code>CnxContext</code> of a message acknowledgement
     * or denying.
     */
    private void ackedId(AgentId qId, String msgId)
    {
      updateCurrentQueue(qId);
      ids.remove(msgId);
      if (ids.isEmpty())
        qDeliveries.remove(qId);
    }

    /**
     * Notifies the <code>CnxContext</code> of messages acknowledgement
     * or denying.
     */
    private void ackedIds(AgentId qId, Vector msgIds)
    {
      updateCurrentQueue(qId);
      ids.removeAll(msgIds);
      if (ids.isEmpty())
        qDeliveries.remove(qId);
    }

    /** Denies all non acknowledged messages. */
    private void deny()
    {
      Enumeration queues = qDeliveries.keys();
      while (queues.hasMoreElements()) {
        updateCurrentQueue((AgentId) queues.nextElement());
        sendTo(qId, new DenyRequest(null, ids));
      }
      qDeliveries.clear();
    }
 
    /**
     * Updates the references to the current queue and its not acknowledged
     * message identifiers vector.
     */ 
    private void updateCurrentQueue(AgentId qId)
    {
      if (qId.equals(this.qId)) {
        if (qDeliveries.isEmpty())
          qDeliveries.put(qId, ids);
        return;
      }

      ids = (Vector) qDeliveries.get(qId);
      if (ids == null) {
        ids = new Vector();
        qDeliveries.put(qId, ids);
      }
      this.qId = qId;
    }
  }


  /**
   * The <code>ClientSubscription</code> class is used by the JMS proxy
   * for storing and managing its clients subscriptions.
   */
  private class ClientSubscription implements java.io.Serializable
  {
    /** Identifier of the client connection. */
    private int connectionKey;
    /** The subscription name. */
    private String name;
    /** The subscribed topic identifier. */
    private AgentId topicId;
    /** The selector for filtering messages. */
    private String selector;
    /**
     * <code>true</code> if the subscriber does not wish to consume 
     * messages published also by its connection.
     */
    private boolean noLocal;
    /** <code>true</code> if the subscription is durable. */
    private boolean durable;

    /** <code>true</code> if the subscription is active. */
    private boolean active;
    /** Vector of identifiers of the messages to deliver to the client. */
    private Vector ids;
    /** Vector of identifiers of the messages delivered to the client. */
    private Vector deliveredIds;
    /**
     * Table for keeping a record of the denied messages.
     * <p>
     * <b>Key:</b> message identifier<br>
     * <b>Object:</b> number of delivery attempts
     */
    private Hashtable deniedMsgs;
    /**
     * Identifier of the request requesting messages, either the listener's
     * request, or a "receive" request.
     */
    private String requestId = null;
    /** <code>true</code> if the messages are destinated to a listener. */
    private boolean toListener;


    /**
     * Constructs a <code>ClientSubscription</code> instance.
     */
    private ClientSubscription(int key, ConsumerSubRequest req)
    {
      connectionKey = key;
      name = req.getSubName();
      topicId = AgentId.fromString(req.getTarget());
      selector = req.getSelector();
      noLocal = req.getNoLocal();
      durable = req.getDurable();

      active = true;
      ids = new Vector();
      deliveredIds = new Vector();
      deniedMsgs = new Hashtable();
    }


    /** Adds identifiers of messages to deliver. */
    private void addIds(Vector newIds)
    {
      // Browsing the delivered message identifiers:
      String newId;
      Message msg;
      while (! newIds.isEmpty()) {
        newId = (String) newIds.remove(0);
        msg = (Message) messagesTable.get(newId);
 
        // Adding it if noLocal selection matches (messages published
        // by the same connection as the subscriber's are recognized by the
        // presence of the string "c<connectionKey>m" in their identifiers).
        if (! noLocal
            || (newId.indexOf("c" + connectionKey + "m")) == -1) {
          msg.acksCounter++;
          ids.add(newId);

          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + newId
                                    + " added in sub " + name);
        }
      }
    }

    /**
     * Returns a <code>ConsumerMessages</code> reply if there are messages to
     * deliver to the client subscriber, <code>null</code> otherwise.
     */
    private ConsumerMessages deliver()
    {
      // Returning null if no request exists:
      if (requestId == null)
        return null;

      int i = 0;
      String id = null;
      Message message;
      Vector messages = new Vector();
      int prior;
      int j;
      Integer deliveryAttempts = null;
      Vector deadM = null;

      // When delivering to a listener, it is a vector of messages that may
      // be sent:
      if (toListener) {
        // Browsing the subscription's non delivered messages:
        while (i < ids.size()) {
          id = (String) ids.get(i);
          if (! deliveredIds.contains(id)) {
            message = (Message) messagesTable.get(id);
            // If the message has not already been removed:
            if (message != null) {
              // If the current message is valid, inserting it according
              // to priorities:
              if (message.isValid()) {
                j = 0;
                while (j < messages.size()) {
                  prior = ((Message) messages.get(j)).getPriority();
                  if (prior >= message.getPriority())
                    j++;
                  else
                    break;
                }
                // Setting message's deliveryCount field:
                deliveryAttempts = (Integer) deniedMsgs.get(id);
                if (deliveryAttempts == null)
                  message.deliveryCount = 1;
                else {
                  message.deliveryCount = deliveryAttempts.intValue() + 1;
                  message.denied = true;
                }

                messages.insertElementAt(message, j);
                deliveredIds.add(id);
                i++;

                if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
                  MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + id
                                          + " added for delivery in sub "
                                          + name);
              }
              // Removing the invalid message, and adding it to the vector
              // of dead messages:
              else {
                ids.remove(id);
                messagesTable.remove(id);
                deliveryAttempts = (Integer) deniedMsgs.remove(id);
                if (deliveryAttempts != null) {
                  message.deliveryCount = deliveryAttempts.intValue();
                  message.denied = true;
                }
                if (deadM == null)
                  deadM = new Vector();
                message.expired = true;
                deadM.add(message);
              }
            }
            // If the message has been removed, clearing the resources:
            else {
              ids.remove(id);
              deniedMsgs.remove(id);
            }
          }
          else
            i++;
        }
      }
      // When delivering to a receiver, getting the highest priority message
      // and putting it in the vector of delivery:
      else {
        int highestP = -1;
        Message keptMsg = null;
        while (i < ids.size()) {
          id = (String) ids.get(i);
          if (! deliveredIds.contains(id)) {
            message = (Message) messagesTable.get(id);
            // If the message hasn't already been removed:
            if (message != null) {
              // If the current message is valid, comparing its priority with
              // the hightest encountered so far, and if superior, keeping it:
              if (message.isValid()) {
                if (message.getPriority() > highestP) {
                  highestP = message.getPriority();
                  keptMsg = message;
                }
                i++;
              }
              // Removing the invalid message, and adding it to the vector
              // of dead messages:
              else {
                ids.remove(id);
                messagesTable.remove(id);
                deliveryAttempts = (Integer) deniedMsgs.remove(id);
                if (deliveryAttempts != null) {
                  message.deliveryCount = deliveryAttempts.intValue();
                  message.denied = true;
                }
                deadM = new Vector();
                message.expired = true;
                deadM.add(message);
              }
            }
            // If the message has been removed, clearing the resources:
            else {
              ids.remove(id);
              deniedMsgs.remove(id);
            }
          }
          else 
            i++;
        }
        // Putting the kept message in the vector:
        if (keptMsg != null) {
          deliveryAttempts = (Integer) deniedMsgs.get(keptMsg.getIdentifier());
          if (deliveryAttempts == null)
            keptMsg.deliveryCount = 1;
          else {
            keptMsg.deliveryCount = deliveryAttempts.intValue() + 1;
            keptMsg.denied = true;
          }

          messages.add(keptMsg);
          deliveredIds.add(keptMsg.getIdentifier());

          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message "
                                    + keptMsg.getIdentifier()
                                    + " added for delivery in sub " + name);
        }
      }

      // Sending the dead messages to the DMQ, if any:
      if (deadM != null)
        sendToDMQ(deadM);

      // Finally, returning the reply or null:
      if (! messages.isEmpty()) {
        ConsumerMessages consM = new ConsumerMessages(requestId, messages,
                                                      name, false);
        if (! toListener)
          requestId = null;

        return consM;
      }
      return null;
    }


    /**
     * Acknowledges messages.
     *
     * @param ackIds  Vector of acknowledged messages identifiers
     */
    private void acknowledge(Vector ackIds)
    {
      // Browsing the acknowledged messages:
      String ackId;
      Message msg;
      while (! ackIds.isEmpty()) {
        ackId = (String) ackIds.remove(0);
        ids.remove(ackId);
        deliveredIds.remove(ackId);
        deniedMsgs.remove(ackId);
        msg = (Message) messagesTable.get(ackId);
        // If message still exists, and if no more ack is expected,
        // removing it:
        if (msg != null) {
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + ackId
                                    + " acknowledged in sub " + name);

          msg.acksCounter--;
          if (msg.acksCounter == 0)
            messagesTable.remove(ackId);
        }
      }  
    }


    /**
     * Denies messages.
     *
     * @param deniedIds  Vector of denied messages identifiers
     */
    private void deny(Vector denIds)
    {
      String denyId;
      Message msg;
      Vector deadM = new Vector();
      int deliveryAttempts = 1;

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Messages denied in sub "
                                + name + ": " + denIds);

      // Browsing the denied messages:
      while (! denIds.isEmpty()) {
        denyId = (String) denIds.remove(0);
        deliveredIds.remove(denyId);
        msg = (Message) messagesTable.get(denyId);
        if (msg != null) {
          // If message invalid, removing it, and adding it to the vector
          // of dead messages:
          if (! msg.isValid()) {
            ids.remove(denyId);
            messagesTable.remove(denyId);
            Integer value = (Integer) deniedMsgs.remove(denyId);
            if (value != null)
              deliveryAttempts = value.intValue() + 1;
            msg.deliveryCount = deliveryAttempts;
            msg.expired = true;
            deadM.add(msg);
          }
          // Message is valid:
          else {
            // Getting its number of delivery attempts:
            Integer value = (Integer) deniedMsgs.get(denyId);
            if (value != null)
              deliveryAttempts = value.intValue() + 1;

            // If maximum delivery attempts reached, removing the message:
            if (isUndeliverable(deliveryAttempts)) {
              ids.remove(denyId);
              deniedMsgs.remove(denyId);
              msg.deliveryCount = deliveryAttempts;
              msg.undeliverable = true;
              deadM.add(msg);
              msg.acksCounter--;
            }
            // Else, adding a new entry for it:
            else {
              deniedMsgs.put(denyId, new Integer(deliveryAttempts));

              if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
                MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + denyId
                                        + " denied in sub " + name);
            }
          }
        }
        // If message already removed:
        else if (msg == null) {
          ids.remove(denyId);
          deniedMsgs.remove(denyId);
        }
      }  

      // Sending dead messages to the DMQ, if needed:
      if (! deadM.isEmpty())
        sendToDMQ(deadM);
    }


    /** Denies all delivered messages. */
    private void denyAll()
    {
      deny(deliveredIds);
    }


    /** Deletes the subscription and its messages, if possible. */
    private void delete()
    {
      String id;
      Message msg;

      while (! ids.isEmpty()) {
        id = (String) ids.remove(0);
        msg = (Message) messagesTable.get(id);

        // If message still exists, decreasing its acknowledgement counter:
        if (msg != null) {
          msg.acksCounter--;
          // If this subscription acknowledgement was the last expected for it,
          // removing it:
          if (msg.acksCounter == 0)
            messagesTable.remove(id);
        }
      }
    }

    /**
     * Returns <code>true</code> if a given value matches the threshold value
     * for this user.
     */
    private boolean isUndeliverable(int deliveryAttempts)
    {
      if (threshold != null)
        return deliveryAttempts == threshold.intValue();
      else if (DeadMQueueImpl.threshold != null)
        return deliveryAttempts == DeadMQueueImpl.threshold.intValue();
      return false;
    }
  }
}

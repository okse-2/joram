/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.comm.*;
import fr.dyade.aaa.mom.dest.*;
import fr.dyade.aaa.mom.excepts.*;
import fr.dyade.aaa.mom.jms.*;
import fr.dyade.aaa.mom.messages.Message;
import fr.dyade.aaa.task.*;

import java.io.*;
import java.util.*;

import org.objectweb.monolog.api.BasicLevel;

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

        if (subsTable != null && subsTable.isEmpty()) {
          subsTable = null;
          messagesTable = null;
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
   * <li><code>SessCreateDestRequest</code></li>
   * <li><code>ProducedMessages</code></li>
   * <li><code>QRecReceiveRequest</code></li>
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
          if (req instanceof SessCreateDestRequest)
            driverDoFwd(key, (SessCreateDestRequest) req);
          else if (req instanceof ProducerMessages)
            driverDoFwd(key, (ProducerMessages) req);
          else if (req instanceof QRecReceiveRequest)
            driverDoFwd(key, (QRecReceiveRequest) req);
          else if (req instanceof QBrowseRequest)
            driverDoFwd(key, (QBrowseRequest) req);
          // Other requests are forwarded to the proxy:
          else
            sendTo(this.getId(), new DriverNotification(key, not));
        }
        // Catching an exception due to an invalid agent identifier to
        // forward the request to:
        catch (IllegalArgumentException iE) {
          RequestException mE = new RequestException("Proxy could not forward"
                                                     + " the request to"
                                                     + " incorrectly"
                                                     + " identified"
                                                     + " destination: "
                                                     + iE);

          doReply(key, new MomExceptionReply(req.getRequestId(), mE));
        }
      }
    }
    // This case can't happen as a proxy necessarily wraps the data read
    // on the stream in an InputNotification!!
    else {}
  }

  /**
   * Actually forwards a <code>SessCreateDestRequest</code> as a
   * <code>PingRequest</code> to a destination.
   */
  private void driverDoFwd(int key, SessCreateDestRequest req)
  {
    sendTo(AgentId.fromString(req.getTo()), 
           new PingRequest(key, req.getRequestId()));
  }

  /**
   * Actually forwards the messages sent by the client in a
   * <code>ProducerMessages</code> request as a <code>ClientMessages</code>
   * MOM request directly to a destination, and acknowledges them by sending
   * a <code>ServerReply</code> back.
   */
  private void driverDoFwd(int key, ProducerMessages req)
  {
    sendTo(AgentId.fromString(req.getTo()),
           new ClientMessages(key, req.getRequestId(), req.getMessages()));

    doReply(key, new ServerReply(req));
  }

  /**
   * Actually forwards a <code>QRecReceiveRequest</code> request as a
   * <code>ReceiveRequest</code> directly to the target queue.
   */
  private void driverDoFwd(int key, QRecReceiveRequest req)
  {
    AgentId to = AgentId.fromString(req.getTo());
    sendTo(to, new ReceiveRequest(key, req.getRequestId(),
                                  req.getSelector(), req.getTimeToLive(),
                                  false));
  }

  /**
   * Actually forwards the client's <code>QBrowseRequest</code> request as
   * a <code>BrowseRequest</code> MOM request directly to a destination.
   */
  private void driverDoFwd(int key, QBrowseRequest req)
  {
    sendTo(AgentId.fromString(req.getTo()),
           new BrowseRequest(key, req.getRequestId(), req.getSelector()));
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the JMS client proxy with its specific behaviour.
   * <p>
   * A JMS proxy reacts to:
   * <ul>
   * <li><code>fr.dyade.aaa.agent.DriverNotification</code> notifications,</li>
   * <li><code>ProxySyncAck</code> proxy acknowledgements,</li>
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
    // Notification sent by the proxy to itself for causal reasons:
    else if (not instanceof ProxySyncAck)
      doReact((ProxySyncAck) not);
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
   * <li><code>CnxAccessRequest</code></li>
   * <li><code>CnxStartRequest</code></li>
   * <li><code>CnxStopRequest</code></li>
   * <li><code>SessCreateTQRequest</code></li>
   * <li><code>SessCreateTTRequest</code></li>
   * <li><code>TSessSubRequest</code></li>
   * <li><code>TSessUnsubRequest</code></li>
   * <li><code>TSessCloseRequest</code></li>
   * <li><code>TSubSetListRequest</code></li>
   * <li><code>TSubUnsetListRequest</code></li>
   * <li><code>TSubReceiveRequest</code></li>
   * <li><code>QRecAckRequest</code></li>
   * <li><code>QRecDenyRequest</code></li>
   * <li><code>TSubAckRequest</code></li>
   * <li><code>TSubDenyRequest</code></li>
   * <li><code>QSessAckRequest</code></li>
   * <li><code>QSessDenyRequest</code></li>
   * <li><code>TSessAckRequest</code></li>
   * <li><code>TSessDenyRequest</code></li>
   * <li><code>TempDestDeleteRequest</code></li>
   * <li><code>XAQSessPrepare</code></li>
   * <li><code>XATSessPrepare</code></li>
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
      else if (request instanceof CnxAccessRequest)
        doReact((CnxAccessRequest) request);
      else if (request instanceof CnxStartRequest)
        doReact((CnxStartRequest) request);
      else if (request instanceof CnxStopRequest)
        doReact((CnxStopRequest) request);
      else if (request instanceof SessCreateTQRequest)
        doReact((SessCreateTQRequest) request);
      else if (request instanceof SessCreateTTRequest)
        doReact((SessCreateTTRequest) request);
      else if (request instanceof TSessSubRequest)
        doReact((TSessSubRequest) request);
      else if (request instanceof TSessUnsubRequest)
        doReact((TSessUnsubRequest) request);
      else if (request instanceof TSubCloseRequest)
        doReact((TSubCloseRequest) request);
      else if (request instanceof TSubSetListRequest)
        doReact((TSubSetListRequest) request);
      else if (request instanceof TSubUnsetListRequest)
        doReact((TSubUnsetListRequest) request);
      else if (request instanceof TSubReceiveRequest)
        doReact((TSubReceiveRequest) request);
      else if (request instanceof QRecAckRequest)
        doReact((QRecAckRequest) request);
      else if (request instanceof QRecDenyRequest)
        doReact((QRecDenyRequest) request);
      else if (request instanceof TSubAckRequest)
        doReact((TSubAckRequest) request);
      else if (request instanceof TSubDenyRequest)
        doReact((TSubDenyRequest) request);
      else if (request instanceof QSessAckRequest)
        doReact((QSessAckRequest) request);
      else if (request instanceof QSessDenyRequest)
        doReact((QSessDenyRequest) request);
      else if (request instanceof TSessAckRequest)
        doReact((TSessAckRequest) request);
      else if (request instanceof TSessDenyRequest)
        doReact((TSessDenyRequest) request);
      else if (request instanceof TempDestDeleteRequest)
        doReact((TempDestDeleteRequest) request);
      else if (request instanceof XAQSessPrepare)
        doReact((XAQSessPrepare) request);
      else if (request instanceof XATSessPrepare)
        doReact((XATSessPrepare) request);
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
   * Method implementing the reaction to a <code>CnxAccessRequest</code>
   * checking a user's right on a destination.
   * <p>
   * It simply forwards it as an <code>AccessRequest</code> to the
   * destination. The reason why this simple forward does not take place
   * directly in the driver is causal.
   */
  private void doReact(CnxAccessRequest req)
  {
    sendTo(AgentId.fromString(req.getTo()), 
           new AccessRequest(currKey, req.getRequestId(), req.getRight()));
  }

  /**
   * Method implementing the proxy reaction to a <code>CnxStartRequest</code>
   * requesting to start a connection.
   * <p>
   * This method sends the pending <code>QueueMessage</code> and
   * <code>SubMessages</code> replies, if any.
   */
  private void doReact(CnxStartRequest req)
  {
    cnx.started = true;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Connection " + currKey
                              + " started.");


    // Delivering the pending deliveries, if any:
    AbstractJmsReply pending;
    while (! cnx.repliesBuffer.isEmpty()) {
      pending = (AbstractJmsReply) cnx.repliesBuffer.remove(0);

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Sending pending"
                                + " reply " + pending.getCorrelationId());

      doReply(pending);
    }

    // Launching deliveries for the active subscriptions:
    String subName;
    ClientSubscription sub;
    SubMessages subM;
    for (int i = 0; i < cnx.activeSubs.size(); i++) {
      subName = (String) cnx.activeSubs.get(i);
      sub = (ClientSubscription) subsTable.get(subName);
      subM = sub.deliver();
      if (subM != null)
        doReply(subM);
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
   * <code>ProxySyncAck</code> notification it sends to itself.
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
             new ProxySyncAck(currKey, new SessCreateTDReply(req,
                                                             qId.toString())));

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
   * <code>ProxySyncAck</code> notification it sends to itself.
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
             new ProxySyncAck(currKey, new SessCreateTDReply(req,
                                                             tId.toString())));

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
   * <code>TSessSubRequest</code> requesting to subscribe to a topic.
   * <p>
   * Sends a <code>SubscribeRequest</code> to the target proxy, registers
   * the subscription and acknowledges it.
   */
  private void doReact(TSessSubRequest req) throws RequestException
  {
    if (subsTable == null) {
      subsTable = new Hashtable();
      messagesTable = new Hashtable();
    }

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
      if (req.getTo().equals(sub.topicId.toString())) {
        // If the selector changes, updating the subscription:
        if (sub.selector == null) {
          if (req.getSelector() != null)
            sendTo(AgentId.fromString(req.getTo()),
                   new SubscribeRequest(req.getRequestId(), subName,
                                        req.getSelector()));
        }
        else if (! sub.selector.equals(req.getSelector()))
          sendTo(AgentId.fromString(req.getTo()),
                 new SubscribeRequest(req.getRequestId(), subName,
                                      req.getSelector()));
      }
      // Else, unsubscribing to the previous topic and updating the
      // subscription:
      else {
        sendTo(sub.topicId, new UnsubscribeRequest(req.getRequestId(),
                                                   subName));
        sendTo(AgentId.fromString(req.getTo()),
               new SubscribeRequest(req.getRequestId(), subName,
                                    req.getSelector()));
        sub.topicId = AgentId.fromString(req.getTo());
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
      sendTo(AgentId.fromString(req.getTo()),
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
    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>TSubSetListRequest</code> notifying the creation of a client
   * listener.
   * <p>
   * Sets the listener for the subscription, and if the connection is started,
   * launches a delivery sequence.
   *
   * @exception RequestException  If the subscription can't be retrieved.
   */
  private void doReact(TSubSetListRequest req) throws RequestException
  {
    // Getting the subscription:
    String subName = req.getSubName();
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
      SubMessages subM = sub.deliver();
      if (subM != null)
        doReply(subM);
    }
  }
   
  /**
   * Method implementing the JMS proxy reaction to a
   * <code>TSubUnsetListRequest</code> notifying that a subscriber listener
   * is unset.
   */
  private void doReact(TSubUnsetListRequest req) throws RequestException
  {
    // Desactivating the subscription:
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new RequestException("Can't unset a listener on the non existing"
                                 + " subscription: " + subName);

    sub.requestId = null;
    sub.toListener = false;
    
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Listener has been unset on"
                              + " subscription: " + subName);

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>TSubCloseRequest</code> requesting to desactivate a durable
   * subscription.
   */
  private void doReact(TSubCloseRequest req) throws RequestException
  {
    // Getting the name of the subscription:
    String subName = req.getSubName();
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
   * <code>TSessUnsubRequest</code> requesting to unsubscribe to a topic.
   * <p>
   * Sends an <code>UnsubscribeRequest</code> to the target proxy, removes
   * the subscription and acknowledges the request.
   */
  private void doReact(TSessUnsubRequest req) throws RequestException
  {
    // Getting the subscription:
    String subName = req.getSubName();

    if (subsTable == null)
      throw new RequestException("Can't unsubscribe non existing"
                                 + " subscription: " + subName);

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

    if (subsTable.isEmpty()) {
      subsTable = null;
      messagesTable = null;
    }

    // Acknowledging the request:
    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the proxy reaction to a
   * <code>TSubReceiveRequest</code> instance, requesting a message from a
   * subscription.
   * <p>
   * This method registers the request and launches a delivery sequence if
   * the connection is started and the request not immediate. If immediate,
   * may bufferize the reply if the connection is stopped. If with a positive
   * timer, may register the request to the Scheduler if not answered.
   */
  private void doReact(TSubReceiveRequest req) throws RequestException
  {
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null)
      throw new RequestException("Can't request a message from the unknown"
                                 + " subscription: " + subName);

    String reqId = req.getRequestId();
    sub.requestId = reqId;
    sub.toListener = false;

    SubMessages subM = null;
    boolean replied = false;

    // In the case of an immediate delivery request:
    if (req.getTimeToLive() == 0) {
      // Getting something to deliver, or delivering an empty reply:
      subM = sub.deliver();
      if (subM == null)
        subM = new SubMessages(reqId, subName, null);
      // Replying if the connection is started:
      if (cnx.started)
        doReply(subM);
      // Or buffering:
      else
        cnx.repliesBuffer.add(subM);
    }
    // Else, if the connection is started, trying to deliver messages:
    else if (cnx.started) {
      subM = sub.deliver();
      if (subM != null) {
        doReply(subM);
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
   * <code>QSessAckRequest</code> acknowledging messages on a queue.
   */
  private void doReact(QSessAckRequest req)
  {
    AgentId qId = AgentId.fromString(req.getTo());
    Vector ids = req.getIds();
    sendTo(qId, new AcknowledgeRequest(currKey, req.getRequestId(), ids));
    cnx.ackedIds(qId, ids);
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>TSessAckRequest</code> acknowledging messages on a subscription.
   */
  private void doReact(TSessAckRequest req)
  {
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    if (sub != null)
      sub.acknowledge(req.getIds());
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>QSessDenyRequest</code> denying messages on a queue.
   * <p>
   * Actually sends a deny request to the queue and acknowledges the request.
   */
  private void doReact(QSessDenyRequest req)
  {
    AgentId qId = AgentId.fromString(req.getTo());
    Vector ids = req.getIds();
    cnx.ackedIds(qId, ids);
    sendTo(qId, new DenyRequest(currKey, req.getRequestId(), ids));

    // Acknowledging the request:
    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>TSessDenyRequest</code> denying messages on a subscription.
   * <p>
   * This method denies the messages and launches a new delivery sequence.
   */
  private void doReact(TSessDenyRequest req)
  {
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);

    if (sub == null) 
      return;

    sub.deny(req.getIds());

    try {
      setCnx(sub.connectionKey);
   
      // Launching a delivery sequence:
      if (cnx.started) {
        SubMessages sM = sub.deliver();
        if (sM != null)
          doReply(sM);
      } 
    }
    catch (ProxyException pE) {}
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>QRecAckRequest</code> acknowledging a message on a queue.
   */
  private void doReact(QRecAckRequest req)
  {
    AgentId qId = AgentId.fromString(req.getTo());
    String id = req.getId();
    Vector ids = new Vector();
    ids.add(id);
    sendTo(qId, new AcknowledgeRequest(currKey, req.getRequestId(), ids));
    cnx.ackedId(qId, id);
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>TSubAckRequest</code> acknowledging a message on a subscription.
   */
  private void doReact(TSubAckRequest req)
  {
    String subName = req.getSubName();
    ClientSubscription sub = (ClientSubscription) subsTable.get(subName);
    if (sub != null) {
      Vector ids = new Vector();
      ids.add(req.getId());
      sub.acknowledge(ids);
    }
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>QRecDenyRequest</code> denying a message on a queue.
   * <p>
   * Actually sends a deny request to the queue and acknowledges the request.
   */
  private void doReact(QRecDenyRequest req)
  {
    AgentId qId = AgentId.fromString(req.getTo());
    String id = req.getId();
    Vector ids = new Vector();
    ids.add(id);
    cnx.ackedId(qId, id);
    sendTo(qId, new DenyRequest(currKey, req.getRequestId(), ids));

    // Acknowledging the request:
    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /** 
   * Method implementing the JMS proxy reaction to a
   * <code>TSubDenyRequest</code> denying a message on a subscription.
   * <p>
   * This method simply denies the target message and launches a new
   * delivery sequence.
   */
  private void doReact(TSubDenyRequest req)
  {
    String subName = req.getSubName();
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
        SubMessages sM = sub.deliver();
        if (sM != null)
          doReply(sM);
      } 
    }
    catch (ProxyException pE) {}
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
    AgentId tempId = AgentId.fromString(req.getTo());
    cnx.tempDestinations.remove(tempId);

    // Sending the request to the destination:
    sendTo(tempId, new DeleteNot());

    // Acknowledging the request:
    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XAQSessPrepare</code> request holding messages and acknowledgements
   * produced in an XA transaction.
   * <p>
   * This method stores the various objects for later commit and acknowledges
   * the request.
   */
  private void doReact(XAQSessPrepare req)
  {
    if (cnx.transactionsTable == null)
      cnx.transactionsTable = new Hashtable();

    String id = req.getId();
    cnx.transactionsTable.put(id, req);

    doReply(new ServerReply(req));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XATSessPrepare</code> request holding messages and acknowledgements
   * produced in an XA transaction.
   * <p>
   * This method stores the various objects for later commit and acknowledges
   * the request.
   */
  private void doReact(XATSessPrepare req)
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

    Vector sendings;
    Vector acks;
    ProducerMessages pM;

    Object obj = cnx.transactionsTable.remove(id);
    if (obj instanceof XAQSessPrepare) {
      XAQSessPrepare qPrep = (XAQSessPrepare) obj;
      sendings = qPrep.getSendings();
      acks = qPrep.getAcks();

      while (! sendings.isEmpty()) {
        pM = (ProducerMessages) sendings.remove(0);
        sendTo(AgentId.fromString(pM.getTo()),
               new ClientMessages(currKey, pM.getRequestId(),
                                  pM.getMessages()));
      }
      while (! acks.isEmpty())
        doReact((QSessAckRequest) acks.remove(0));
    }
    else if (obj instanceof XATSessPrepare) {
      XATSessPrepare tPrep = (XATSessPrepare) obj;
      sendings = tPrep.getSendings();
      acks = tPrep.getAcks();

      while (! sendings.isEmpty()) {
        pM = (ProducerMessages) sendings.remove(0);
        sendTo(AgentId.fromString(pM.getTo()),
               new ClientMessages(currKey, pM.getRequestId(),
                                  pM.getMessages()));
      }
      while (! acks.isEmpty())
        doReact((TSessAckRequest) acks.remove(0));
    }

    if (cnx.transactionsTable.isEmpty())
      cnx.transactionsTable = null;

    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to an
   * <code>XASessRollback</code> request rolling the operations performed
   * in a given transaction back.
   * <p>
   * This method actually processes the objects sent at the prepare phase,
   * and acknowledges the request.
   */
  private void doReact(XASessRollback req)
  {
    String id = req.getId();

    if (cnx.transactionsTable == null) {
      sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
      return;
    }
    
    Vector acks;

    Object obj = cnx.transactionsTable.remove(id);
    if (obj instanceof XAQSessPrepare) {
      XAQSessPrepare qPrep = (XAQSessPrepare) obj;
      acks = qPrep.getAcks();
      QSessAckRequest qAck;

      while (! acks.isEmpty()) {
        qAck = (QSessAckRequest) acks.remove(0);
        doReact(new QSessDenyRequest(qAck.getTo(), qAck.getIds()));
      }
    }
    else if (obj instanceof XATSessPrepare) {
      XATSessPrepare tPrep = (XATSessPrepare) obj;
      acks = tPrep.getAcks();
      TSessAckRequest tAck;

      while (! acks.isEmpty()) {
        tAck = (TSessAckRequest) acks.remove(0);
        doReact(new TSessDenyRequest(tAck.getSubName(), tAck.getIds()));
      }
    }

    if (cnx.transactionsTable.isEmpty())
      cnx.transactionsTable = null;

    sendTo(this.getId(), new ProxySyncAck(currKey, new ServerReply(req)));
  }

  /**
   * Method implementing the JMS proxy reaction to a <code>ProxySyncAck</code>
   * notification holding a JMS reply wrapped and sent by itself.
   * <p>
   * This method allows to actually acknowledge a client request after the
   * reaction to this request has been commited. All this to preserve 
   * causality. The considered replies are:
   * <ul>
   * <li><code>SessCreateTDReply</code></li>
   * <li><code>ServerReply</code></li>
   * </ul>
   * <p>
   * The method simply sends the wrapped reply to the external client.
   */
  private void doReact(ProxySyncAck not)
  {
    doReply(not.key, not.reply);
  }


  /**
   * Distributes the JMS replies to the appropriate reactions.
   * <p>
   * JMS proxies react the following replies:
   * <ul>
   * <li><code>PongReply</code></li>
   * <li><code>AccessReply</code></li>
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

    if (rep instanceof PongReply)
      doFwd((PongReply) rep);
    else if (rep instanceof AccessReply)
      doFwd((AccessReply) rep);
    else if (rep instanceof QueueMsgReply)
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
   * Actually forwards a <code>PongReply</code> coming from a destination
   * as a <code>SessCreateDestReply</code> destinated to the requesting client.
   */
  private void doFwd(PongReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());
      doReply(new SessCreateDestReply(rep));
    }
    // The connection is lost; nothing to do.
    catch (ProxyException pE) {}
  }

  /**
   * Actually forwards an <code>AccessReply</code> coming from a destination
   * as a <code>CnxAccessReply</code> destinated to the requesting client.
   */
  private void doFwd(AccessReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());
      doReply(new CnxAccessReply(rep));
    }
    // The connection is lost; nothing to do.
    catch (ProxyException pE) {}
  }

  /**
   * Actually forwards a <code>QueueMsgReply</code> coming from a destination
   * as a <code>QueueMessage</code> destinated to the requesting client.
   * <p>
   * If the corresponding connection is stopped, stores the
   * <code>QueueMessage</code> for later delivery.
   */
  private void doFwd(AgentId from, QueueMsgReply rep)
  {
    try {
      // Updating the active connection:
      setCnx(rep.getConnectionKey());

      // Building the reply and storing the wrapped message id for later
      // denying in the case of a failure:
      QueueMessage jRep = new QueueMessage(rep);
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
   * <p>
   */
  private void doFwd(TopicMsgsReply rep)
  {
    // Storing the received messages:
    messagesTable.putAll(rep.getAllMessages());

    String subName;
    ClientSubscription sub;
    SubMessages subM;

    // Browsing the target subscriptions:
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
              subM = sub.deliver();
              if (subM != null)
                doReply(subM);
            }
          }
          // The connection is lost: nothing to do.
          catch (ProxyException pE) {}
        }
      }
    }
    // Checking among the stored messages if some won't be delivered:
    Enumeration keys = messagesTable.keys();
    String id;
    Message msg;
    while (keys.hasMoreElements()) {
      id = (String) keys.nextElement();
      msg = (Message) messagesTable.get(id);
      // No acknowledgement expected for this message: removing it...
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
   * The method usually sends a <code>SubMessages</code> back to the
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

        SubMessages subM = new SubMessages(sub.requestId, subName, null);
        sub.requestId = null;

        if (cnx.started)
          doReply(subM);
        else
          cnx.repliesBuffer.add(subM);

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
   * notification notifying that a destination has been deleted.
   * <p>
   * If the request was a subscribe request, the method removes the 
   * corresponding subscriptions. For all requests, sends also an
   * <code>JmsExceptReply</code> to the requester.
   */
  private void doReact(UnknownAgent uA)
  {
    Notification not = uA.not;
    AgentId agId = uA.agent;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
      MomTracing.dbgProxy.log(BasicLevel.WARN, "--- " + this
                              + " notified of dead destination: "
                              + agId.toString());

    if (not instanceof AbstractRequest) {
      AbstractRequest req = (AbstractRequest) not;

      // If the sent request was a subscribe request, removing the sub:
      if (req instanceof SubscribeRequest) {
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

        doReply(new MomExceptionReply("null",
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
        if (subsTable != null) {
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
     * <b>Object:</b> <code>XAQSessPrepare</code> object
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
    /** Vector of identifiers of the messages denied by the client. */
    private Vector deniedIds;
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
    private ClientSubscription(int key, TSessSubRequest req)
    {
      connectionKey = key;
      name = req.getSubName();
      topicId = AgentId.fromString(req.getTo());
      selector = req.getSelector();
      noLocal = req.getNoLocal();
      durable = req.getDurable();

      active = true;
      ids = new Vector();
      deliveredIds = new Vector();
      deniedIds = new Vector();
    }


    /** Adds identifiers of messages to deliver. */
    private void addIds(Vector newIds)
    {
      String newId;
      Message msg;

      // Browsing the delivered message identifiers:
      while (! newIds.isEmpty()) {
        newId = (String) newIds.remove(0);
        msg = (Message) messagesTable.get(newId);
 
        // If the message hasn't already been removed by an other
        // subscription:
        if (msg != null) {  
          // If the message is no more valid, removing it: 
          if (! msg.isValid())
            messagesTable.remove(newId);
          // Else, adding it if noLocal selection matches (messages published
          // by the same connection as the subscriber's are recognized by the
          // presence of the string "c<connectionKey>m" in their identifiers).
          else if (! noLocal
                   || (newId.indexOf("c" + connectionKey + "m")) == -1) {
            msg.acksCounter++;
            ids.add(newId);

             if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
               MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + newId
                                       + " added in sub " + name);
          }
        }
      }
    }


    /**
     * Returns a <code>SubMessages</code> reply if there are messages to
     * deliver to the client subscriber, <code>null</code> otherwise.
     */
    private SubMessages deliver()
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
                // Setting the message denied field:
                if (deniedIds.contains(id)) {
                  message.denied = true;
                  deniedIds.remove(id);
                }
                else
                  message.denied = false;
                messages.insertElementAt(message, j);
                deliveredIds.add(id);
                i++;

                if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
                  MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + id
                                          + " added for delivery in sub "
                                          + name);
              }
              // Removing the invalid message:
              else {
                ids.remove(id);
                deniedIds.remove(id);
                messagesTable.remove(id);
              }
            }
            // If the message has been removed, clearing the vector:
            else {
              ids.remove(id);
              deniedIds.remove(id);
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
              // Removing the invalid message:
              else {
                ids.remove(id);
                deniedIds.remove(id);
                messagesTable.remove(id);
              }
            }
            // If the message has been removed, clearing the vectors:
            else {
              ids.remove(id);
              deniedIds.remove(id);
            }
          }
          else 
            i++;
        }
        // Putting the kept message in the vector:
        if (keptMsg != null) {
          if (deniedIds.contains(keptMsg.getIdentifier())) {
            keptMsg.denied = true;
            deniedIds.remove(keptMsg.getIdentifier());
          }
          else
            keptMsg.denied = false;
          messages.add(keptMsg);
          deliveredIds.add(keptMsg.getIdentifier());

          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message "
                                    + keptMsg.getIdentifier()
                                    + " added for delivery in sub " + name);
        }
      }

      // Finally, returning the reply or null:
      if (! messages.isEmpty()) {
        SubMessages subM = new SubMessages(requestId, name, messages);
        if (! toListener)
          requestId = null;
        return subM;
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
      String ackId;
      Message msg;

      // Browsing the acknowledged messages:
      while (! ackIds.isEmpty()) {
        ackId = (String) ackIds.remove(0);
        ids.remove(ackId);
        deliveredIds.remove(ackId);
        msg = (Message) messagesTable.get(ackId);
        // If message still exists, checking its validity:
        if (msg != null) {
          // If still valid, decreasing its acknowledgements counter,
          // and if no more ack is expected, removing it:
          if (msg.isValid()) {
            msg.acksCounter--;

            if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + ackId
                                      + " acknowledged in sub " + name);

            if (msg.acksCounter == 0)
              messagesTable.remove(ackId);
          }
          // If no more valid, removing it:
          else
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

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Messages denied in sub "
                                + name + ": " + denIds);

      // Browsing the denied messages:
      while (! denIds.isEmpty()) {
        denyId = (String) denIds.remove(0);
        deliveredIds.remove(denyId);
        msg = (Message) messagesTable.get(denyId);
        if (msg != null) {
          // If message invalid, removing it:
          if (! msg.isValid()) {
            ids.remove(denyId);
            messagesTable.remove(denyId);
          }
          // Else, adding it to the vector of denied message identifiers:
          else {
            deniedIds.add(denyId);

            if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
              MomTracing.dbgProxy.log(BasicLevel.DEBUG, "Message " + denyId
                                      + " denied in sub " + name);
          }
        }
        // If message already removed:
        else if (msg == null)
          ids.remove(denyId);
      }  
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

        // If message still exists:
        if (msg != null) {
          // If this subscription acknowledgement was the last expected for it,
          // removing it:
          if (msg.acksCounter == 1 || ! msg.isValid())
            messagesTable.remove(id);
        }
      }
    }
  }
}

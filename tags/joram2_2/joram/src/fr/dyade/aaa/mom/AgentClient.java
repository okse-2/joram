/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.mom;

import fr.dyade.aaa.agent.*;

/**
 * An <code>AgentClient</code> is an agent inheriting from
 * <code>mom.ConnectionFactory</code>, <code>ip.TcpMultiServerProxy</code>
 * and <code>agent.ProxyAgent</code>.
 * <br><br>
 * It is a proxy accepting multiple connections from the client side, and
 * reacting to two types of notifications: the <code>DriverNotification</code>
 * is of the first type, wrapping clients messages (messages, acknowledgements,
 * requests), and coming from a <code>agent.DriverIn</code>. All the other
 * notifications are of the second type, and come from inside the agents world.
 * <br><br>
 * Its reactions are in <code>CommonClientAAA</code>.
 *
 * @author Frederic Maistre
 *
 * @see fr.dyade.aaa.mom.ConnectionFactory
 * @see fr.dyade.aaa.ip.TcpMultiServerProxy
 * @see fr.dyade.aaa.agent.ProxyAgent
 * @see fr.dyade.aaa.mom.CommonClientAAA	
 * @see fr.dyade.aaa.mom.Queue
 * @see fr.dyade.aaa.mom.Topic
 */


public class AgentClient extends fr.dyade.aaa.mom.ConnectionFactory
  implements AgentClientItf
{
  /**
   * This class holds most methods for reacting to the
   * notifications received.
   */
  CommonClientAAA commonClient;

  /** Constructor. */
  public AgentClient()
  {
    super();
    // Setting the parent ProxyAgent's multi-connections mode.
    super.setMultiConn();
    commonClient = new CommonClientAAA(this);
  }

  /**
   * react() method all agents must implement for defining their
   * behaviour when receiving notifications.
   */  
  public void react(AgentId from, Notification not) throws Exception
  { 
    try {
      if(Debug.debug)
        if(Debug.clientTest)
          System.out.println("AgentClient.react(): not " + not.getClass().getName());	 

      if (not instanceof DriverNotification) {
        //////////////////////////////////////////////////////////////////////
        // The following notification comes from outside the agents server. //
        // It wraps a client request, message or acknowledgement.           //
        //////////////////////////////////////////////////////////////////////
        commonClient.reactToProxyNotification((DriverNotification) not);

        ///////////////////////////////////////////////////////////////////////
        // The following notifications come from inside the agents server in //
        // the context of MOM services.                                      //
        ///////////////////////////////////////////////////////////////////////
      } else if (not instanceof NotifAckFromDestination) { 
        // Receiving an wrapped agreement to a client request from a destination.
        commonClient.reactToDestinationAcknowledgement(from,
          (NotifAckFromDestination) not);
      } else if (not instanceof NotifMessageFromQueue) { 
        // Receiving a wrapped message from a Queue.
        commonClient.reactToQueueMsgSending(from, (NotifMessageFromQueue) not); 
      } else if (not instanceof NotifMessageEnumFromQueue) { 
        // Receiving a wrapped enumeration of the messages in a Queue.
        commonClient.reactToQueueEnumSending(from, (NotifMessageEnumFromQueue) not);
      } else if (not instanceof NotifMessageFromTopic) { 
        // Receiving a wrapped message from a Topic.
        commonClient.reactToTopicMsgSending(from, (NotifMessageFromTopic) not);

        ////////////////////////////////////////////////////////////////////
        // The following notifications come from inside the agents server //
        // when exceptions occur.                                         //
        ////////////////////////////////////////////////////////////////////
      } else if (not instanceof NotificationMOMException) { 
        // Receiving an exception from a Destination agent.
        commonClient.reactToMOMException(from, (NotificationMOMException) not); 
      } else if (not instanceof fr.dyade.aaa.agent.ExceptionNotification) { 
        // Receiving an agents server exception notification.
        commonClient.reactToException(from, (ExceptionNotification) not);
      } else if (not instanceof fr.dyade.aaa.agent.UnknownAgent) {
        // Receiving an agents server "unknown agent" exception notification.
        commonClient.reactToUnknownAgentExcept((fr.dyade.aaa.agent.UnknownAgent) not);

        ////////////////////////////////////////////////////////////////////
        // The following notifications come from inside the agents server //
        // for managing connections.                                      //
        ////////////////////////////////////////////////////////////////////
      } else  if (not instanceof fr.dyade.aaa.ip.ConnectNot) {
        // Receiving a ConnectNot notification from the parent ConnectionFactory.
        commonClient.reactToOpeningConnection(super.getProxyDriversKey());
        super.react(from, not);
      } else if (not instanceof DriverDone) { 
        // Receiving a DriverDone notification from a closing Driver.
        commonClient.reactToClosingConnection(((DriverDone) not).getDriverKey());
        super.react(from, not);

        //////////////////////////
        // Other notifications. //
        //////////////////////////
      } else {
        super.react(from, not); 
      } 

    } catch (Exception exc) {
      if (Debug.debug)
        if (Debug.clientSub)
          System.err.println(exc);
    
      throw(exc);
    }
  }


  /** Method sending a <code>MessageMOMExtern</code> to an external client. */
  public void sendMessageMOMExtern(fr.dyade.aaa.mom.MessageMOMExtern msgMOMExtern)
  {
    // The drvKey given by the MessageMOMExtern tells where to
    // find the qout in which pushing the message.
    int drvKey = msgMOMExtern.getDriverKey();
    DriverMonitor dMonitor = (DriverMonitor) driversTable.get(new Integer(drvKey));
    if (dMonitor != null) {
      fr.dyade.aaa.mom.NotificationOutputMessage not = 
        new fr.dyade.aaa.mom.NotificationOutputMessage(msgMOMExtern);

      (dMonitor.getQout()).push(not);
    }
  }

    
  /** 
   * Method sending a notification to a Destination agent
   * (<code>Queue</code> or <code>Topic</code>).
   */
  public void sendNotification(fr.dyade.aaa.agent.AgentId to, 
    fr.dyade.aaa.agent.Notification not)
  {
    sendTo(to, not);
  }

}

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
import java.util.*; 

/**
 * A <code>ClientSubscription</code> contains a given
 * subscription's information, and holds the messages
 * coming from this subscription's <code>Topic</code>.
 * <br>
 * It is uniquely identified by a <code>ClientSubscriptionKey</code>
 * and stored in an <code>AgentClient</code>'s subscriptionTable.
 *	
 * @see  fr.dyade.aaa.mom.Topic
 * @see  fr.dyade.aaa.mom.Theme
 * @see  fr.dyade.aaa.mom.ClientSubscriptionKey
 */
public class ClientSubscription implements java.io.Serializable
{ 
  /** The subscribed <code>Topic</code> id. */
  private AgentId topicID;

  /** The subscribed <code>Theme</code> name. */
  private String theme;

  /** If true, the client won't receive its own published messages. */
  private boolean noLocal;  

  /** The request's selector. */ 
  private String selector;  

  /** If true, a messageListener is active for this subscripition. */
  private boolean messageListener;

  /** The subscriber's connection key. */
  private int driverKey;

  /** The vector of messages received from the <code>Theme</code>. */
  private Vector receivedMessages;

  /** Index of the last delivered message. */
  private int lastDeliveredMsg;

  /** If true, this subscription is a ConnectionConsumer's. */
  private boolean connectionConsumer;

  /** Synchronous request. */
  private SynchronousReceptionRequestMsg requestMsg = null;

  /** Boolean telling if the client has disconnected(durable only). */
  private boolean closedSession;

  /** Constructor. */
  public ClientSubscription(boolean noLocal, String selector,
    AgentId topicID, String theme, int drvKey,
    boolean connectionConsumer)
  {
    this.noLocal = noLocal; 
    this.selector = selector ;
    this.topicID = topicID;
    this.theme = theme;
    this.driverKey = drvKey;
    this.connectionConsumer = connectionConsumer;
    this.messageListener = false ;
    this.lastDeliveredMsg = -1;
    this.receivedMessages = new Vector();
  }


  /**
   * Method updating the subscription. Messages in the vector are 
   * destroyed according to the new parameters.
   */
  public void updateSubscription(AgentId agentClient,
    boolean noLocal, String selector) throws Exception
  {
    this.noLocal = noLocal; 
    this.selector = selector ;

    fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();
    String messageID;
    String sender;

    int i = 0;
    while (i < receivedMessages.size()) {
      fr.dyade.aaa.mom.Message msg =
        (fr.dyade.aaa.mom.Message) receivedMessages.elementAt(i);

      if (Destination.checkMessage(msg)) {
        messageID = msg.getJMSMessageID();
        sender =  messageID.substring(0, messageID.indexOf('_'));

        if ((noLocal && agentClient.toString().equals(sender)) ||
          (selecObj.isAvailable(msg,selector)))

          receivedMessages.removeElementAt(i);
        else
          i++;
      }
      else {
        // Destroying the message because timeOut expired.
        receivedMessages.removeElementAt(i);
      }
    }
  }


  /** Method adding a message to the vector of messages. */
  public void putMsgInClientSub(fr.dyade.aaa.mom.Message msg)
  {
    receivedMessages.addElement(msg);
  }


  /** Method removing a series a messages in the vector of messages. */
  public void removeMessage(String messageID) throws Exception
  {
    boolean messageNotFound = true;

    while (!receivedMessages.isEmpty()) {
      fr.dyade.aaa.mom.Message msg =
        (fr.dyade.aaa.mom.Message) receivedMessages.firstElement();

      if (msg.getJMSMessageID().equals(messageID)) {
        receivedMessages.removeElementAt(0);
        lastDeliveredMsg--;
        messageNotFound = false;
        break ;
      }
      else {
        receivedMessages.removeElementAt(0);
        lastDeliveredMsg--;
      }
    }

    // This case can happen when the client closes a durable Sub and then
    // acknowledges a message ????
    if (lastDeliveredMsg < -1)
      lastDeliveredMsg= -1;

    if (messageNotFound) {
      throw (new MOMException("Can't find message to acknowledge in ClientSubscription",
        MOMException.MESSAGEID_NO_EXIST));
    }
  }


  /** Method returning the first undelivered message. */
  public fr.dyade.aaa.mom.Message deliveryMessage() throws Exception
  {
    fr.dyade.aaa.mom.Message msg;

    while (lastDeliveredMsg < (receivedMessages.size() - 1)) {
      msg = (fr.dyade.aaa.mom.Message) receivedMessages.elementAt(lastDeliveredMsg + 1);

      if (Destination.checkMessage(msg)) {
        //if (messageListener) {
          lastDeliveredMsg++;
          return msg;
        //}
        //return null;
      }
      else 
        // Removing expired message.
        receivedMessages.removeElementAt(lastDeliveredMsg + 1);
    } 
    return null;
  }


  /**
   * Method putting back the delivered but non acknowledged
   * messages in the vector.
   */
  public void putBackNonAckMessages() throws Exception
  {
    int i;
    for (i = 0; i <= lastDeliveredMsg; i++) {
      fr.dyade.aaa.mom.Message msg =
        (fr.dyade.aaa.mom.Message) receivedMessages.elementAt(i);
      msg.setJMSRedelivered(true);
    }
    lastDeliveredMsg = -1;
  }


  /**
   * Method for putting back a delivered but non acknowledged message
   * in the vector, keeping the original order.
   */
  protected void putBackNonAckMessage(String messageID) throws Exception
  {

    if (lastDeliveredMsg > -1) {

      while (!receivedMessages.isEmpty()) {
        fr.dyade.aaa.mom.Message msg =
          (fr.dyade.aaa.mom.Message) receivedMessages.firstElement();

        if (msg.getJMSMessageID().equals(messageID)) {
          receivedMessages.removeElementAt(0);
          this.putMsgInClientSub(msg);	
          lastDeliveredMsg--;
          break ;
        }
        else {
          receivedMessages.removeElementAt(0);
          this.putMsgInClientSub(msg);	
          lastDeliveredMsg--;
        }
      }
    }
  }

  
  public AgentId getTopicID() {
    return this.topicID;
  }


  public String getTheme() {
    return this.theme;
  }

  public void setMessageListener(boolean messageListenerNew) {
    messageListener = messageListenerNew;
  }


  public boolean getMessageListener() {
    return this.messageListener;
  }


  public void setDriverKey(int drvKey) {
    this.driverKey = drvKey;
  }


  public int getDriverKey() {
    return driverKey;
  }

  public boolean isConnectionConsumer() 
  {
    return connectionConsumer;
  }

  public void addRequest(SynchronousReceptionRequestMsg requestMsg)
  {
    this.requestMsg = requestMsg;
  }

  public SynchronousReceptionRequestMsg getRequest()
  {
    SynchronousReceptionRequestMsg req = requestMsg;
    requestMsg = null;
    return req;
  }

  public void setClosedSession(boolean closedSession)
  {
    this.closedSession = closedSession;
  }

  public boolean getClosedSession()
  {
    return closedSession;
  }

}

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
package fr.dyade.aaa.joram;

/**
 * A <code>SessionListener</code> is a listener for
 * asynchronously consuming messages destinated to
 * its Session <code>MessageConsumer</code>s.
 *
 * @author Frederic Maistre
 */
class SessionListener extends fr.dyade.aaa.util.Daemon
{
  private Connection connection;
  private Session session;
  private boolean topic = false;

  /**
   * Constructor. 
   */
  public SessionListener(Long sessionID, Connection connection, Session session)
  {
    super(sessionID.toString());
    this.connection = connection;
    this.session = session;
  } 
  public SessionListener(Long sessionID, Connection connection, Session session,
    boolean topic)
  {
    this(sessionID, connection, session);
    this.topic = topic;
  } 

  public void run()
  {
    while (isRunning) {
      try {
        if (!topic) {
        Object key = session.listenersRequests.get();  

        fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern receivedMsg = 
          (fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern)
          connection.messageJMSMOMTable.get(key);

        String consumerID = (String) connection.waitThreadTable.get(key);
        javax.jms.MessageListener listener = (javax.jms.MessageListener)
          session.listenersTable.get(consumerID);

        if (receivedMsg.message != null) {	
          if (!session.transacted) {
            if(session.acknowledgeMode == fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
              fr.dyade.aaa.mom.AckQueueMessageMOMExtern msgAck =
                new fr.dyade.aaa.mom.AckQueueMessageMOMExtern(connection.getMessageMOMID(),
                receivedMsg.queue, receivedMsg.message.getJMSMessageID(),
                fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE,
                new Long(session.sessionID).toString());

              //session.sendToConnection(msgAck);
              connection.sendMsgToAgentClient(msgAck);
            }
            else {
              session.lastNotAckVector.addElement(receivedMsg);
              receivedMsg.message.setRefSessionItf(session);
            }
          }
          else {
            synchronized(session.transactedSynchroObject) {
              session.transactedMessageToAckVector.addElement(receivedMsg);
            }
          }
        }

        session.resetMessage(receivedMsg.message);
        listener.onMessage(receivedMsg.message);

        key = session.listenersRequests.pop();

        long requestID = connection.getMessageMOMID();
        Long requestKey = new Long(requestID);

        fr.dyade.aaa.mom.ReceptionMessageMOMExtern reqMsg =
          new fr.dyade.aaa.mom.ReceptionMessageMOMExtern(requestID, receivedMsg.queue,
          receivedMsg.selector, -1, 0, new Long(session.sessionID).toString());
        reqMsg.toListener = true;

        connection.waitThreadTable.put(requestKey, consumerID);
        connection.sessionsTable.put(requestKey, session);
        connection.sendMsgToAgentClient(reqMsg);
        }
        else {
          fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern topicMsg =
            (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) session.messagesToDeliver.get();

          fr.dyade.aaa.mom.TopicNaming topic = new fr.dyade.aaa.mom.TopicNaming
            (((fr.dyade.aaa.mom.TopicNaming) topicMsg.message.getJMSDestination()).getTopicName(),
            topicMsg.theme);

          fr.dyade.aaa.mom.AckTopicMessageMOMExtern msgAck =
            new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(connection.getMessageMOMID(),
            topic, topicMsg.nameSubscription, topicMsg.message.getJMSMessageID(),
            session.acknowledgeMode);

          synchronized (session.messageConsumerTable) {
            java.util.Vector v = (java.util.Vector) (session.messageConsumerTable).get(topic);
            fr.dyade.aaa.joram.MessageConsumer consumer = 
              (fr.dyade.aaa.joram.MessageConsumer) v.firstElement();

            if (consumer.getDeliveryMessage()) {
              if (consumer.messageListener != null) {
                if (session.transacted) 
                  (session.transactedMessageToAckVector).addElement(topicMsg);
            
                else if (session.acknowledgeMode ==
                  fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE)
                  connection.sendMsgToAgentClient(msgAck);

                else {
                  topicMsg.message.setRefSessionItf(session);
                  session.resetMessage(topicMsg.message);
                  (session.lastNotAckVector).addElement(topicMsg);
                }

                consumer.messageListener.onMessage(topicMsg.message);
                session.messagesToDeliver.pop();
              }
            }
          }
        }
      } catch (Exception e) {
        if (e instanceof InterruptedException)
          break;
        else
          System.out.println("Exception " + e);
      }
    }
  }


  /** Daemon class abstract method. */
  public void shutdown() {}

}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class Session extends Daemon {
  
  public static Logger logger = Debug.getLogger(Session.class.getName());
  
  public static final int AUTO_ACKNOWLEDGE = 1;
  public static final int CLIENT_ACKNOWLEDGE = 2;
  public static final int DUPS_OK_ACKNOWLEDGE = 3;
  
  public static final int SESSION_TRANSACTED = 0;
  
  /** The connection the session belongs to. */
  Connection cnx;

  int status;

  /** The identifier of the session. */
  String ident;

  /** <code>true</code> if the session is transacted. */
  boolean transacted;

  /** The acknowledgment mode of the session. */
  int acknowledgeMode;

  static boolean receiveAck = false;

  /** <code>true</code> if the session's acknowledgments are automatic. */
  static boolean autoAck = false;

  /** Vector of message consumers. */
  Vector consumers;

  /** Vector of message producers. */
  Vector producers;

  /** The message consumer currently making a request (null if none). */
  MessageConsumer pendingMessageConsumer;

  /** Counter of message listeners. */
  int listenerCount;

  /**
   * The request multiplexer used to communicate with the user proxy.
   */
  RequestMultiplexer mtpx;

  /**
   * The requestor used by the session to communicate with the user proxy.
   */
  Requestor requestor;

  /**
   * The requestor used by the session to make 'receive' with the user
   * proxy. This second requestor is necessary because it must be closed
   * during the session close (see method close).
   */
  Requestor receiveRequestor;
  
  /**
   * Status of the session
   */
  private static class Status {
    public static final int NONE = 0;
    
    /**
     * Status of the session when the connection is stopped.
     * This is the initial status.
     */
    public static final int STOP = 1;

    /**
     * Status of the session when the connection is started.
     */
    public static final int START = 2;

    /**
     * Status of the connection when it is closed.
     */
    public static final int CLOSE = 3;

    private static final String[] names = {
      "NONE", "STOP", "START", "CLOSE"};

    public static String toString(int status) {
      return names[status];
    }
  }
  
  /**
   * Opens a session.
   *
   * @param cnx  The connection the session belongs to.
   * @param transacted  <code>true</code> for a transacted session.
   * @param acknowledgeMode  1 (auto), 2 (client) or 3 (dups ok).
   * @param mtpx request multiplexer.
   *
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public Session(Connection cnx, 
      boolean transacted,
      int acknowledgeMode,
      RequestMultiplexer mtpx) throws JoramException {
    super("Session");
    status = Status.NONE;
    listenerCount = 0;
    this.cnx = cnx;
    this.ident = cnx.nextSessionId();
    this.transacted = transacted;
    if (transacted)
      this.acknowledgeMode = SESSION_TRANSACTED;
    else
      this.acknowledgeMode = acknowledgeMode;
    this.mtpx = mtpx;

    requestor = new Requestor(mtpx);
    receiveRequestor = new Requestor(mtpx);

    consumers = new Vector();
    producers = new Vector();
  }
  

  public Connection getConnection() {
    return cnx;
  }

  public RequestMultiplexer getRequestMultiplexer() {
      return mtpx;
  }

  /**
   * Creates a topic identity given a Topic name. 
   * @param   topicName the name of this Topic
   * @return  a Topic with the given name
   * @exception JoramException  If the session is closed.
   *                            If the topic creation failed.
   */
  public Topic createTopic(String topicName) throws JoramException {
    // Checks if the topic to retrieve is the administration topic:
    if (topicName.equals("#AdminTopic")) {
      GetAdminTopicReply reply =  
        (GetAdminTopicReply) requestor.request(new GetAdminTopicRequest());
      if (reply.getId() != null)
        return new Topic(reply.getId(), topicName);
      else
        return null;
    }
    return new Topic(topicName, topicName);
  }

  /**
   * Creates a TemporaryTopic object. 
   * Its lifetime will be that of the Connection unless it is deleted earlier.
   * 
   * @return a temporary topic identity 
   * @exception JoramException  If the session is closed or if the connection is broken.
   *                            If the request fails for any other reason.
   */
  public TemporaryTopic createTemporaryTopic() throws JoramException {
    SessCreateTDReply reply =
      (SessCreateTDReply) requestor.request(new SessCreateTTRequest());
    String tempDest = reply.getAgentId();
    return new TemporaryTopic(tempDest, requestor);
  }

  /**
   * Creates a MessageProducer to send messages to the specified destination.
   *
   * @exception IllegalStateException  If the session is closed or if the 
   *              connection is broken.
   * @exception JoramException  If the creation fails for any other reason.
   */
  public MessageProducer createProducer(Destination dest) throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    MessageProducer prod = new MessageProducer(this, dest);
    addProducer(prod);
    return prod;
  }

  /**
   * Creates a MessageConsumer for the specified destination using a
   * message selector.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JoramException  If the creation fails for any other reason.
   */
  public MessageConsumer createConsumer(Destination dest,
                                           String selector,
                                           boolean nolocal) throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    MessageConsumer cons = new MessageConsumer(this, dest, selector, null, nolocal);
    addConsumer(cons);
    return cons;
  }

  /**
   * Creates a MessageConsumer for the specified destination.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JoramException  If the creation fails for any other reason.
   */
  public MessageConsumer createConsumer(Destination dest) throws JoramException {
    return createConsumer(dest, null, false);
  }

  /**
   * Creates a durable subscriber to the specified topic.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JoramException  If the creation fails for any other reason.
   */
  public MessageConsumer createDurableSubscriber(Topic topic, String subname) throws JoramException {
    return createDurableSubscriber(topic, subname, null, false);
  }

  /**
   * Creates a durable subscriber to the specified topic, using a
   * message selector and specifying whether messages published by its
   * own connection should be delivered to it.
   *
   * @exception IllegalStateException  If the session is closed or if the
   *              connection is broken.
   * @exception JoramException  If the creation fails for any other reason.
   */
  public MessageConsumer createDurableSubscriber(Topic topic, 
      String subname,
      String selector,
      boolean nolocal) throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    MessageConsumer cons = new MessageConsumer(this, topic, selector, subname, nolocal);
    addConsumer(cons);
    return cons;
  }
            
  /**
   * Unsubscribes a durable subscription that has been created by a client.
   */
  public void unsubscribe(String name) throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    /*     checkThreadOfControl(); */
   // MessageConsumer cons;
    // TODO: Durable
  /*   if (consumers != null) { */
  /*     for (int i = 0; i < consumers.size(); i++) { */
  /*       cons = (MessageConsumer) consumers.get(i); */
  /*       if (! cons.queueMode && cons.targetName.equals(name)) */
  /*         throw JoramException(); */
  /*     } */
  /*   } */
    AbstractReply reply = syncRequest(new ConsumerUnsubRequest(name));
    if (reply != null) {
      reply = null;
    }
  }

  /**
   * Creates a Message object.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public Message createMessage() throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    return new Message();
  }
  
  /**
   * Creates a TextMessage object.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public TextMessage createTextMessage() throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    return new TextMessage();
  }

  /**
   * Creates a StreamMessage object.
   *
   * @exception IllegalStateException  If the session is closed.
   */
  public StreamMessage createStreamMessage() throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
    return new StreamMessage();
  }
  
  /**
   * Returns the acknowledgment mode of the session.
   */
  public int getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * Indicates whether the session is in transacted mode.
   */
  public boolean getTransacted() {
    return transacted;
  }

  /**
   * Commits all messages done in this transaction.
   */
  public void commit() throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();

    if (! transacted) throw new IllegalStateException();

    throw new NotYetImplementedException();
  }

  /**
   * Rolls back any messages done in this transaction.
   */
  public void rollback() throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();
  /*   checkThreadOfControl(); */

    if (! transacted) throw new IllegalStateException();

    // TODO: transacted
    throw new NotYetImplementedException();

  /*   // Denying the received messages: */
  /*   deny(); */
  /*   // Deleting the produced messages: */
  /*   sendings.clear(); */
  }

  /**
   * Starts the asynchronous deliveries in the session.
   */
  public void start() {
    if ((status == Status.CLOSE) || (status == Status.START)) return;

    if (listenerCount > 0) {
  /*     repliesIn.start(); */
      start();
  /*     singleThreadOfControl = daemon.getThread(); */
    }

    status = Status.START;
  }

  public void stop() {
    if ((status == Status.STOP) || (status == Status.CLOSE)) return;
    //if (isRunning()) super.stop();
    super.stop();
    
  /*     singleThreadOfControl = null; */
    status = Status.STOP;
  }

  /**
   * Closes the session.
   */
  public void close() {
    for (int i = 0; i < consumers.size(); i++) { 
      MessageConsumer mc = 
        (MessageConsumer)consumers.elementAt(i); 
      try { 
        mc.close(); 
      } catch (JoramException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
      }
    }
    consumers.removeAllElements();

    for (int i = 0; i < producers.size(); i++) { 
      MessageProducer mp = (MessageProducer)producers.elementAt(i); 
      mp.close(); 
    } 
    producers.removeAllElements();
      
    stop();

    // The requestor must be closed because 
    // it could be used by a concurrent receive 
    // as it is not synchronized (see receive()). 
    receiveRequestor.close(); 

   // cnx.closeSession(this); 

    status = Status.CLOSE;
  }

  public void run() {
    try {
      while (running) {
        canStop = true;
  /*       try { */
  /*         reply = (AbstractReply*) repliesIn.get(); */
  /*       } catch (Exception exc) { */
  /*         continue; */
  /*       } */
  /*       canStop = false; */

  /*       // Processing it through the session: */
  /*       distribute(reply); */
  /*       repliesIn.pop(); */
      }
    } catch (Exception exc) {
    }
    finish();
  }

  public void distribute(AbstractReply asyncReply) {
  /*   if (asyncReply.getClassId() != AbstractMessage::CONSUMER_MESSAGES) { */
  /*     // TODO: write an error message. */
  /*     return; */
  /*   } */

  /*   // Getting the message: */
  /*   ConsumerMessages* reply = (ConsumerMessages*) asyncReply; */

  /*   // Getting the consumer: */
  /*   MessageConsumer* cons = (MessageConsumer*) NULL; */
  /*   if (reply.getQueueMode()) { */
  /*     cons = (MessageConsumer*) cnx.requestsTable.remove(reply.getKey()); */
  /*   } else { */
  /*     cons = (MessageConsumer*) cnx.requestsTable.get(reply.getKey()); */
  /*   } */

  /*   // Passing the message(s) to the consumer: */
  /*   if (cons != (MessageConsumer*) NULL) { */
  /*     Vector<Message>* msgs = reply.getMessages(); */
  /*     for (int i=0; i<msgs.size(); i++) */
  /*       cons.onMessage((Message*) msgs.get(i)); */
  /*   }  else { */
  /*     // The target consumer of the received message may be null if it has */
  /*     // been closed without having stopped the connection: denying the */
  /*     // deliveries. */
  /*     Vector<Message>* msgs = reply.getMessages(); */
  /*     Message* msg; */
  /*     Vector<String> ids = new Vector<String>(); */
  /*     for (int i = 0; i < msgs.size(); i++) { */
  /*       msg = (Message*) msgs.get(i); */
  /*       ids.addElement(msg.getMessageID()); */
  /*     } */

  /*     if (ids.isEmpty()) */
  /*       return; */
    
  /*     try {  */
  /*       cnx.asyncRequest(new SessDenyRequest(reply.comesFrom(), ids, */
  /*                                            reply.getQueueMode(), true)); */
  /*     } */
  /*     catch (JMSException jE) {} */
  /*   } */
  }

  /**
   * Called by MessageProducer.
   * 
   * @param msg
   * @param dest
   * @param deliveryMode
   * @param priority
   * @param timeToLive
   * @throws JoramException
   */
  public void send(Message msg, Destination dest,
                     int deliveryMode,
                     int priority,
                     long timeToLive) throws JoramException {
    if (status == Status.CLOSE) throw new IllegalStateException();

    // Updating the message property fields:
    msg.setMessageID(cnx.nextMessageId());
    msg.setDeliveryMode(deliveryMode);
    msg.setDestination(dest);
    msg.setTimestamp(System.currentTimeMillis());
    if (timeToLive == 0) {
      msg.setExpiration(0);
    } else {
      msg.setExpiration(msg.getTimestamp() + timeToLive);
    } 
    msg.setPriority(priority);

    if (transacted) {
      // TODO: transacted
      throw new NotYetImplementedException();
      // If the session is transacted, keeping the request for later delivery:
  /*     prepareSend(dest, msg.clone()); */
    } else {
       ProducerMessages pM = new ProducerMessages(dest.getUID(), msg.clone());
      AbstractReply reply = requestor.request(pM);
      if (reply != null) {
        reply = null;
      }
      if (pM != null) {
        pM = null;
      }
    }
  }

  /**
   * Called here and by sub-classes.
   */
  public void addProducer(MessageProducer prod) {
    producers.addElement(prod);
  }

  /**
   * Called by MessageProducer.
   */
  public void closeProducer(MessageProducer prod) {
    producers.removeElement(prod);
  }

  /**
   * Called by MessageConsumer.
   *
   * @param timeOut1
   * @param timeOut2
   * @param cons
   * @param targetName
   * @param selector
   * @param queueMode
   * @return Message
   * @throws JoramException
   */
  public Message receive(long timeOut1, long timeOut2,
                            MessageConsumer cons,
                            String targetName,
                            String selector, boolean queueMode) throws JoramException {
      if (status == Status.CLOSE) throw new IllegalStateException();
      
      // Don't call checkSessionMode because
      // we also check that the session mode is not 
      // already set to RECEIVE.
  /*     switch (sessionMode) { */
  /*     case SessionMode.NONE: */
  /*       setSessionMode(SessionMode.RECEIVE); */
  /*       break; */
  /*     default: */
  /*       throw new IllegalStateException("Illegal session mode"); */
  /*     } */

  /*     singleThreadOfControl = pthread_self(); */
      pendingMessageConsumer = cons;

      try {
        ConsumerMessages reply = null;
        ConsumerReceiveRequest request = new ConsumerReceiveRequest(targetName, selector, timeOut1, queueMode);
  /*       if (receiveAck) request.setReceiveAck(true); */
        reply = (ConsumerMessages) receiveRequestor.request(request, timeOut2);

        request = null;
          
  /*       synchronized (this) { */
  /*         // The session may have been  */
  /*         // closed in between. */
  /*         if (status == Status.CLOSE) { */
  /*           if (reply != null) { */
  /*             mtpx.deny(reply); */
  /*           } */
  /*           return null; */
  /*         } */
          
          if (reply != null) {
            Vector msgs = reply.getMessages();
            if ((msgs != null) && (msgs.size() != 0)) {
              Message msg = ((Message) msgs.elementAt(0)).clone();
              String msgId = msg.getMessageID();
              
  /*             // Auto ack: acknowledging the message: */
  /*             if (autoAck && ! receiveAck) { */
                ConsumerAckRequest req = new ConsumerAckRequest(targetName, queueMode);
                req.addId(msgId);
                mtpx.sendRequest(req);

  /*             } else { */
  /*               prepareAck(targetName, msgId, queueMode); */
  /*             } */
              msg.session = this;
              reply = null;

              return msg;
            } else {
              return null;
            }
          } else {
            return null;
          }
  /*       } */
      } catch (Exception e) {
        pendingMessageConsumer = null;
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR,"EXCEPTION",e); 
        return null;
      }
  }

  /**
   * Called here and by sub-classes.
   */
  public void addConsumer(MessageConsumer cons) {
    consumers.addElement(cons);
  }

  /**
   * Called by MessageConsumer.
   */
  public void closeConsumer(MessageConsumer cons) throws JoramException {
    consumers.removeElement(cons);

    // TODO
  /*   if (pendingMessageConsumer == cons) { */
  /*     if (requestStatus == RequestStatus.RUN) { */
  /*       // Close the requestor. A call to abortRequest()  */
  /*       // is not enough because the receiving thread  */
  /*       // may call request() just after this thread  */
  /*       // calls abort(). */
  /*       receiveRequestor.close(); */

  /*       // Wait for the end of the request */
  /*       try { */
  /*         while (requestStatus != RequestStatus.NONE) { */
  /*           wait(); */
  /*         } */
  /*       } catch (InterruptedException exc) {} */

  /*       // Create a new requestor. */
  /*       receiveRequestor = new Requestor(mtpx); */
  /*     } */
  /*   } */
  }

  /**
   * Called by MessageConsumer
   */
  public void addMessageListener(MessageConsumerListener mcl) {
  /*   checkClosed(); */
  /*   checkThreadOfControl(); */

  /*   checkSessionMode(SessionMode.LISTENER); */

    listenerCount++;
      
    if (status == Status.START && listenerCount == 1) {
      // It's the first message listener, starts the session's thread.
      start();
    }
  }

  /**
   * Called by MessageConsumer. The thread of control and the status
   * must be checked if the call results from a setMessageListener
   * but not from a close.
   */
  public void removeMessageListener(MessageConsumerListener mcl) {
  /*   if (check) { */
  /*     checkClosed(); */
  /*     checkThreadOfControl(); */
  /*   } */
    
  /*   synchronized (this) { */
      listenerCount--;
      if (status == Status.START && listenerCount == 0) {
        // All the message listeners have been closed
        // so we can call doStop() in a synchronized
        // block. No deadlock possible.
        stop();
  /*       singleThreadOfControl = null; */
      }
  /*   } */
  }

  /**
   * The requestor raises an exception if it is called during another request.
   * This cannot happen as a session is monothreaded.
   */
  public AbstractReply syncRequest(AbstractRequest request) throws JoramException {
    return requestor.request(request);
  }
  
  protected void shutdown() {
    //TODO
  }

}


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
package dotcom;

import javax.jms.*;

/**
 * Listener getting messages from a topic.
 *
 * @author	Maistre Frederic
 * 
 * @see		CustomerServer
 * @see		InventoryServer
 * @see		BillingServer
 */
class TopicListener implements javax.jms.MessageListener {
  /** TopicSession getting messages from a topic. */
  TopicSession session ;
  /** FifoQueue used to hold incoming messages. */
  fr.dyade.aaa.util.Queue queue ;
  
  /**
   * Creates a TopicListener.
   *
   * @param session		current TopicSession
   * @param queue		FifoQueue used to hold incoming messages
   */
  TopicListener(TopicSession session, fr.dyade.aaa.util.Queue queue) {
    this.session = session ;
    this.queue = queue ;
  }

  /**
   * Method called when receiving a message.
   *
   * @param msg			message received from the topic
   */
  public void onMessage(javax.jms.Message msg) {
    try {
      // push incoming message into the queue
      queue.push((ObjectMessage) msg);
      // commiting the reception
      session.commit() ;
      
    } catch (Exception exc) {
      System.out.println(" Exception caught in TopicListener: " + exc);
      exc.printStackTrace();
    }	
  } 
}

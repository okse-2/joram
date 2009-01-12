/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package dotcom;

import javax.jms.TopicSession;

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
      queue.push(msg);
      // Committing the reception
      session.commit() ;
      
    } catch (Exception exc) {
      System.out.println(" Exception caught in TopicListener: " + exc);
      exc.printStackTrace();
    }	
  } 
}

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.EmptyQueueException;
import fr.dyade.aaa.common.Queue;

public class DistributionDaemon extends Daemon {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(DistributionDaemon.class.getName());

  /** Holds the distribution logic. */
  private DistributionHandler distributionHandler;
  
  private Queue distributeQueue;
  private Queue ackQueue;
  
  private TxDestination txDest;

  public DistributionDaemon(DistributionHandler distributionHandler, String destinationName, TxDestination txDest) {
  	super("DistributionDaemon_" + destinationName, logger);
  	this.distributionHandler = distributionHandler;
  	distributeQueue = new Queue();
  	ackQueue = new Queue();
  	this.txDest = txDest;
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon<> distributionHandler = " + distributionHandler + ", txDest = " + txDest);
  }
  
	public void run() {
		if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon run()");
		
		while (running) {
      canStop = true;
      Message msg = null;
      try {
      	if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "DistributionDaemon run: distributeQueue.size() = " + distributeQueue.size());
      	// get the first message
      	msg = (Message) distributeQueue.get();
      	
      	if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "DistributionDaemon run: distributeQueue.get() = " + msg.id);
      	
      	// test if this message is deliverable
      	if (isUndeliverable(msg)) {
      		if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "DistributionDaemon run: the message " +  msg.id + " is undeliverable.");
      		// delete the message from the distributeQueue
      		distributeQueue.pop();
      		continue;
      	}
      	
      } catch (InterruptedException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "", exc);
        return;
      }

      canStop = false;
      // process
      try {
      	// distribute the message
      	distributionHandler.distribute(msg);
      	
      	if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "DistributionDaemon run: distributeQueue.pop = " + msg.id);
      	// delete the message from the distributeQueue
      	distributeQueue.pop();

      	// add message id to the ackQueue
      	ackQueue.push(msg.id);
      	if (logger.isLoggable(BasicLevel.DEBUG))
      		logger.log(BasicLevel.DEBUG, "DistributionDaemon run: ackQueue.push : " + msg.id);

      	// transaction delete the message
      	String txName = txDest.getTxName(msg.id);
      	if (logger.isLoggable(BasicLevel.DEBUG))
      		logger.log(BasicLevel.DEBUG, "DistributionDaemon run: txName(" + msg.id + ")=" + txName);
      	if (txName != null) {
      		org.objectweb.joram.mom.messages.Message momMsg = new org.objectweb.joram.mom.messages.Message(msg);
      		momMsg.setTxName(txName);
      		momMsg.delete();
      		AgentServer.getTransaction().begin();
      		AgentServer.getTransaction().commit(true);
      		if (logger.isLoggable(BasicLevel.DEBUG))
        		logger.log(BasicLevel.DEBUG, "DistributionDaemon run: " + msg.id + " deleted.");
      	} else {
      		if (logger.isLoggable(BasicLevel.ERROR))
            logger.log(BasicLevel.ERROR, "DistributionDaemon run: txName == null for msg " + msg.id + " can't be delete.");
      	}
      	
      } catch (Exception e) {
      	if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "DistributionDaemon run()", e);
      	
      	if (e instanceof EmptyQueueException) {
      		continue;
      	}
      	
      	// Increment the delivery count
      	incDeliveryCount(msg);
      	
      	canStop = true;
      	// the connection is down, wait a wakeup from DistributionQueue or DistributionTopic.
      	synchronized (this) {
	        try {
	        	if (logger.isLoggable(BasicLevel.DEBUG))
	        		logger.log(BasicLevel.DEBUG, "DistributionDaemon run: wait.");
	          wait();
	          if (logger.isLoggable(BasicLevel.DEBUG))
	        		logger.log(BasicLevel.DEBUG, "DistributionDaemon run: wakeup.");
          } catch (InterruptedException e1) {
          	if (logger.isLoggable(BasicLevel.DEBUG))
          		logger.log(BasicLevel.DEBUG, "DistributionDaemon run wait InterruptedException.");
          }
        }
			}
    }
  }

	@Override
  protected void shutdown() {
		if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon shutdown()");
  }

	@Override
  protected void close() {
		if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon close()");
	  distributeQueue.clear();
	  ackQueue.clear();
	  distributeQueue.close();
	  ackQueue.close();
  }

	public void push(Message msg) {
		if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon distributeQueue.push(" + msg.id + ')');
		distributeQueue.push(msg);
  }

	public List getAckList() {
	  List ackList = new ArrayList();
		while (!ackQueue.isEmpty()) {
			try {
	      ackList.add(ackQueue.getAndPop());
      } catch (InterruptedException e) { }
		}
	  return ackList;
  }

	public boolean isEmpty() {
	  return distributeQueue.isEmpty();
  }

  private boolean isUndeliverable(Message message) {
  	if (! isValid(message))
  		return true;
  	
  	int threshold = 0;
  	if (txDest instanceof DistributionQueue) {
      threshold =((DistributionQueue)txDest).getThreshold();
  	}
  	
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionDaemon isUndeliverable: deliveryCount = " + message.deliveryCount + ", threshold = " + threshold);
  	
    if (threshold == 0) return false;
    if (threshold > 0)
      return (message.deliveryCount >= threshold);
    else if (org.objectweb.joram.mom.dest.Queue.getDefaultThreshold() > 0)
      return (message.deliveryCount >= org.objectweb.joram.mom.dest.Queue.getDefaultThreshold());
    return false;
  }
	
  /**
   * Returns <code>true</code> if the message is valid. 
   * The message is valid if not expired.
   */
  private boolean isValid(Message message) {
  	return (message.expiration <= 0) || (message.expiration > System.currentTimeMillis());
  }
  
  /** Increments the message delivery count. */
  private void incDeliveryCount(Message message) {
    message.deliveryCount += 1;
  }
}

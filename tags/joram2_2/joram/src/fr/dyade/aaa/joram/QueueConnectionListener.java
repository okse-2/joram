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
 * A <code>QueueConnectionListener</code> is a listener for
 * receiving messages from a <code>Queue</code> and passing
 * them to a <code>ConnectionConsumer</code>.
 *
 * @author Frederic Maistre
 */
class QueueConnectionListener extends fr.dyade.aaa.util.Daemon
{
  /** The connection this listener is listening to. */
  private Connection connection;
  /** The connectionConsumer this listener is listening for. */
  private ConnectionConsumer connectionConsumer = null;
  /** The Queue this listener will get the messages from. */
  private fr.dyade.aaa.mom.QueueNaming queue = null;
  /** The selector for filtering the messages. */
  private String selector;

  /**
   * Constructor used by <code>QueueConnection</code>s when creating
   * a <code>ConnectionConsumer</code>.
   *
   * @param connection  The Connection this listener is attached to.
   * @param connectionConsumer  The ConnectionConsumer constructing this listener.
   * @param queue  The Queue this listener is listening to.
   * @param selector  The selector parameter.
   */
  public QueueConnectionListener(Connection connection,
    ConnectionConsumer connectionConsumer, javax.jms.Queue queue,
    String selector)
  {
    super(connection.toString());
    this.connection = connection;
    this.connectionConsumer = connectionConsumer;
    this.queue = (fr.dyade.aaa.mom.QueueNaming) queue; 
    this.selector = selector;
  }


  public void run()
  {
    try {
      while (running) {
        canStop = true; 
        fr.dyade.aaa.mom.MessageMOMExtern momMsg;

        try {
          long requestID = connection.getMessageMOMID();
          Long longRequestID = new Long(requestID);

          fr.dyade.aaa.mom.ReceptionMessageMOMExtern reqMsg = 
            new fr.dyade.aaa.mom.ReceptionMessageMOMExtern(requestID,
            queue, selector, (long) -1, 0, "");

          Object lock = new Object();
          synchronized (lock) {
            connection.waitThreadTable.put(longRequestID, lock);
            connection.sendMsgToAgentClient(reqMsg);
            lock.wait();
          }

          momMsg = (fr.dyade.aaa.mom.MessageMOMExtern)
            connection.messageJMSMOMTable.remove(longRequestID);
        } catch (InterruptedException iE) {
          continue;
        }
        canStop = false;
        if (momMsg instanceof fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) {
          connectionConsumer.getMessage(momMsg);
        }
      }
    } catch (Exception e) {
    } finally {
      running = false;
    }
  } 

  public void shutdown()
  {
    while (thread.isAlive()) {
      try {
        thread.sleep(1);
      } catch (InterruptedException iE) {}
    }
    thread = null;
  }
}

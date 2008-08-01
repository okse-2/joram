/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.util;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.TransactionError;
import fr.dyade.aaa.util.Transaction;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.messages.MessageBody;
import org.objectweb.joram.shared.messages.MessagePersistent;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

import java.util.Vector;
import java.io.IOException;


/**
 * The <code>MessagePersistenceModule</code> class is a utility class used
 * by queues and proxies for persisting, retrieving and deleting messages.
 */
public class MessagePersistenceModule {
 
  static public String getSaveName(String agentId, 
                                   MessagePersistent message) {
    String id = message.getIdentifier();
    return "msg" + agentId + id.substring(3);
  }

  /**
   * save the message (header and body).
   *
   * @param agentId  id of agent.
   * @param message  Message to save.
   */
  static public String save(String agentId, 
                            MessagePersistent message) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.save(" + agentId +
                                    "," + message + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.save(" + agentId +
                              "," + message + ')');

    String id = message.getIdentifier();
    String name = "msg" + agentId + id.substring(3);
    Transaction tx = AgentServer.getTransaction();

    if (!tx.isPersistent())
      return null;
    
    try {
      // save header part of message.
      tx.save(message,name);

      // save body part of message.
      MessageBody body = message.getMessageBody();
      if (! body.saved) {
        body.saved = true;
        tx.save(body,name+".body");
      }
    } catch (Exception exc) {
      throw new TransactionError(exc.toString());
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "msg " + id + " header save to " + name +
                                    " body save to " + name + ".body");
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "msg " + id + " header save to " + name +
                              " body save to " + name + ".body");
    return name;
  }

  /**
   * save the message header.
   *
   * @param agentId  id of agent.
   * @param message  Message to save header.
   */
  static public String saveHeader(String agentId, 
                                  MessagePersistent message) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.saveHeader(" + agentId +
                                    "," + message + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.saveHeader(" + agentId +
                              "," + message + ')');

    String id = message.getIdentifier();
    String name = "msg" + agentId + id.substring(3);
    Transaction tx = AgentServer.getTransaction();

    if (!tx.isPersistent())
      return null;
    
    try {
      // save header part of message.
      tx.save(message,name);
    } catch (Exception exc) {
      throw new TransactionError(exc.toString());
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "msg " + id + " header save to " + name);
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "msg " + id + " header save to " + name);
    return name;
  }

  /**
   * save the message body.
   *
   * @param agentId  id of agent.
   * @param message  Message to save body.
   */
  static public String saveBody(String agentId, 
                                MessagePersistent message) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.saveBody(" + agentId +
                                    "," + message + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.saveBody(" + agentId +
                              "," + message + ')');

    String id = message.getIdentifier();
    String name = "msg" + agentId + id.substring(3);
    Transaction tx = AgentServer.getTransaction();
    
    if (!tx.isPersistent())
      return null;

    try {
      // save body part of message.
      MessageBody body = message.getMessageBody();
      if (! body.saved) {
        body.saved = true;
        tx.save(body,name+".body");
      }
    } catch (Exception exc) {
      throw new TransactionError(exc.toString());
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "msg " + id + 
                                    " body save to " + name + ".body");
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "msg " + id + 
                              " body save to " + name + ".body");
    return name+".body";
  }

  /** Load persisted message header. */
  static public Message loadHeader(String name) 
    throws ClassNotFoundException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.loadHeader(" + name + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.loadHeader(" + name + ')');

    Transaction tx = AgentServer.getTransaction();
    try {
      return ((MessagePersistent) tx.load(name)).getMessage();
    } catch (IOException exc) {
      throw new TransactionError(exc.toString());
    }
  }

  /** Load persisted message body. */
  static public MessageBody loadBody(String name)
    throws ClassNotFoundException {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.loadBody(" + name + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.loadBody(" + name + ')');

    Transaction tx = AgentServer.getTransaction();
    try {
      return (MessageBody) tx.load(name+".body");
    } catch (IOException exc) {
      throw new TransactionError(exc.toString());
    }
  }

  /**
   * delete the message.
   *
   * @param message  Message to delete.
   */
  static public void delete(MessagePersistent message) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.delete(" + message + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.delete(" + message + ')');

    String id = message.getIdentifier();
    String name = message.getSaveName();
    Transaction tx = AgentServer.getTransaction();

    if (!tx.isPersistent()) return;

    if (name != null) {
      tx.delete(name);
      tx.delete(name+".body");

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                      "msg " + id + " delete " + name +
                                      " and " + name + ".body");
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                "msg " + id + " delete " + name +
                                " and " + name + ".body");
    }
  }

  /** Loads all persisted messages. */
  static public Vector loadAll(String agentId) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.loadAll(" + agentId + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.loadAll(" + agentId + ')');

    Vector messages = new Vector();

    // Retrieving the names of the persistence message previously saved. 
    Transaction tx = AgentServer.getTransaction();
    String[] messageNames = tx.getList("msg" + agentId);

    // Retrieving the messages individually persisted.
    for (int i = 0; i < messageNames.length; i++) {
      if (messageNames[i].endsWith("body")) continue;
      try {
        MessagePersistent mp = (MessagePersistent) tx.load(messageNames[i]);
        Message msg = mp.getMessage();
        msg.setPin(mp.getPin());
        if (msg.isPin() && 
            ! msg.noBody) {
          msg.setMessageBody((MessageBody) tx.load(messageNames[i]+".body"));
        }

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "loadAll: messageNames[" + i +
                                        "] msg = " + msg);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  "loadAll: messageNames[" + i +
                                  "] msg = " + msg);
        messages.add(msg);
      } catch (Exception exc) {
        MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                      "Message named ["
                                      + messageNames[i]
                                      + "] could not be loaded for queue ["
                                      + agentId
                                      + "]",
                                      exc);
        MomTracing.dbgProxy.log(BasicLevel.ERROR,
                                "Message named ["
                                + messageNames[i]
                                + "] could not be loaded for queue ["
                                + agentId
                                + "]",
                                exc);
      }
    }
    return messages;
  }

  /** Deletes all persisted objects. */
  static public void deleteAll(String agentId) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "MessagePersistenceModule.deleteAll(" + agentId + ')');
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "MessagePersistenceModule.deleteAll(" + agentId + ')');

    Transaction tx = AgentServer.getTransaction();

    // Retrieving the names of the persistence message previously saved. 
    String[] messageNames = tx.getList("msg" + agentId);

    // Deleting the message.
    for (int i = 0; i < messageNames.length; i++) {
      tx.delete(messageNames[i]);
      tx.delete(messageNames[i]+".body");
    }
  }
}

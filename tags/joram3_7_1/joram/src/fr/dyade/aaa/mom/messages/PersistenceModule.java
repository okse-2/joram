/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.mom.messages;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.util.Transaction;

import org.objectweb.util.monolog.api.BasicLevel;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;


/**
 * The <code>PersistenceModule</code> class provides methods for persisting,
 * retrieving and deleting messages.
 * <p>
 * Messages are either persisted individually, or grouped into vectors.
 */
public class PersistenceModule implements java.io.Serializable
{
  /** Identifier of the agent using the module. */
  private AgentId agentId;
  /**
   * Table of the identifiers of the messages persisted in vectors.
   * <p>
   * <b>Key:</b> name of the persistence vector<br>
   * <b>Value:</b> table of identifiers of the persisted messages
   */
  private Hashtable vectorsTable;
  /** Counter of persistence objects. */
  private long counter = 0;

  /** Table of the messages to save for the first time. */
  private transient Hashtable toBeSaved;
  /** Table of the messages to update. */
  private transient Hashtable toBeUpdated;
  /** Table of the identifiers of the messages to delete. */
  private transient Hashtable idsToBeDeleted;
  /** 
   * Table of the persisted messages' identifiers.
   * <p>
   * <b>Key:</b> identifier of the persisted message<br>
   * <b>Value:</b> name of the vector in which the message is persisted,
   *               <code>null</code> if message persisted individually.
   */
  private transient HashMap idsTable;


  /**
   * Constructs a <code>PersistenceModule</code> instance.
   *
   * @param agentId  Identifier of the agent building the module.
   */
  public PersistenceModule(AgentId agentId)
  {
    this.agentId = agentId;
    vectorsTable = new Hashtable();

    toBeSaved = new Hashtable();
    toBeUpdated = new Hashtable();
    idsToBeDeleted = new Hashtable();
    idsTable = new HashMap();
  }

 
  /**
   * Registers a message for future saving.  
   *
   * @param message  Message to persist.
   */
  public void save(Message message)
  {
    if (! message.getPersistent())
      return;

    String id = message.getIdentifier();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "Registering msg " + id + " for saving");
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "Registering msg " + id + " for saving");

    // If message already persisted, its persistent state will have
    // to be upated.
    if (idsTable.containsKey(id))
      toBeUpdated.put(id, message);
    // Else, the message will have to be saved.
    else
      toBeSaved.put(id, message);

    idsToBeDeleted.remove(id);
  }

  /**
   * Registers a message for future deletion.
   *
   * @param message  Message to delete.
   */
  public void delete(Message message)
  {
    if (! message.getPersistent())
      return;

    String id = message.getIdentifier();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                    "Registering msg " + id + " for deletion");
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "Registering msg " + id + " for deletion");

    idsToBeDeleted.put(id, id);

    toBeSaved.remove(id);
    toBeUpdated.remove(id);
  }
    
  /** Commits the registered savings and deletions. */
  public void commit()
  {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "Commiting...");

    // Nothing to commit.
    if (toBeUpdated.isEmpty()
        && toBeSaved.isEmpty()
        && idsToBeDeleted.isEmpty())
      return;

    Enumeration enum;
    String id;
    String name;
    Hashtable ids;
    Vector msgs = new Vector();
    Hashtable msgIds = new Hashtable();
    
    Transaction tx = AgentServer.getTransaction();

    // Deletion: browsing the identifiers of the messages to delete.
    for (enum = idsToBeDeleted.keys(); enum.hasMoreElements();) {
      id = (String) enum.nextElement();
      name = (String) idsTable.remove(id);

      // No vector name: message was persisted individually.
      if (name == null) {
        tx.delete("msg" + agentId + id.substring(3));

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "Message deleted: " + id);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                        "Message deleted: " + id);
      }
      // Vector name retrieved: message was persisted in a vector.
      else {
        ids = (Hashtable) vectorsTable.get(name);
        ids.remove(id);

        // No more messages to be kept: removing the persistence object.
        if (ids.isEmpty()) {
          vectorsTable.remove(name);
          tx.delete(name);

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                          "Vector deleted: " + name);
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                    "Vector deleted: " + name);
        }
      }
    }

    // Update: deleting the previous message's state.
    for (enum = toBeUpdated.keys(); enum.hasMoreElements();) {
      id = (String) enum.nextElement();
      name = (String) idsTable.get(id);

      // No vector name: message was persisted individually.
      if (name == null) {
        tx.delete("msg" + agentId + id.substring(3));

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "Message deleted: " + id);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  "Message deleted: " + id);
      }
      // Vector name retrieved: message was persisted in a vector.
      else {
        ids = (Hashtable) vectorsTable.get(name);
        ids.remove(id);

        // No more messages to be kept: removing the persistence object.
        if (ids.isEmpty()) {
          vectorsTable.remove(name);
          tx.delete(name);

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                          "Vector deleted: " + name);
          if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                    "Vector deleted: " + name);
        }
      }
      // Adding the message to the vector of the messages to persist.
      msgs.add(toBeUpdated.get(id));
      msgIds.put(id, id);
    }

    // Adding the messages to be saved to the vector.
    for (enum = toBeSaved.keys(); enum.hasMoreElements();) {
      id = (String) enum.nextElement();
      msgs.add(toBeSaved.get(id));
      msgIds.put(id, id);
    }

    // Saving the messages. 
    try {
      // Single message: saving it individually.
      if (msgs.size() == 1) {
        Message msg = (Message) msgs.get(0);
        name = "msg" + agentId + msg.getIdentifier().substring(3);
        idsTable.put(msg.getIdentifier(), null);
        tx.save(msg, name);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "Message saved: "
                                        + msg.getIdentifier());
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  "Message saved: "
                                  + msg.getIdentifier());
      }
      // Many messages: saving the vector.
      else if (! msgs.isEmpty()) {
        if (counter == Long.MAX_VALUE)
          counter = 0;
        name = "msgs" + agentId + "-" + counter++;
        vectorsTable.put(name, msgIds);

        for (enum = msgIds.keys(); enum.hasMoreElements();)
          idsTable.put(enum.nextElement(), name);
  
        tx.save(msgs, name);

        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG,
                                        "Vector saved: " + name);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                                  "Vector saved: " + name);
      }
    }
    catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgDestination.log(BasicLevel.ERROR, 
                                      "Messages with ids ["
                                      + toBeSaved.toString()
                                      + "] could not be saved by queue ["
                                      + agentId.toString()
                                      + "]: "
                                      + exc);
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
        MomTracing.dbgProxy.log(BasicLevel.ERROR, 
                                "Messages with ids ["
                                + toBeSaved.toString()
                                + "] could not be saved by queue ["
                                + agentId.toString()
                                + "]: "
                                + exc);
    }
    toBeSaved = new Hashtable();
    toBeUpdated = new Hashtable();
    idsToBeDeleted = new Hashtable();
  }

  /** Rolls back all the registered saving and deletion requests. */
  public void rollback()
  {
    toBeSaved.clear();
    toBeUpdated.clear();
    idsToBeDeleted.clear();
  }

  /** Loads all persisted objects. */
  public Vector loadAll()
  {
    Enumeration enum;
    Vector messages = new Vector();
    Vector persistedVec;
    Message msg;
    String msgId;

    // Getting the identifiers of the messages persisted in vectors, and to
    // be kept.
    Hashtable idsToPersist = new Hashtable();
    for (enum = vectorsTable.keys(); enum.hasMoreElements();)
      idsToPersist.putAll((Hashtable) vectorsTable.remove(enum.nextElement()));
     
    // Retrieving the names of the persistence objects previously saved. 
    Transaction tx = AgentServer.getTransaction();
    String[] messageNames = tx.getList("msg" + agentId);
    String[] vectorNames = tx.getList("msgs" + agentId);

    // Retrieving the messages individually persisted.
    for (int i = 0; i < messageNames.length; i++) {
      try {
        messages.add(tx.load(messageNames[i]));
      }
      catch (Exception exc) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                        "Message named ["
                                        + messageNames[i]
                                        + "] could not be loaded for queue ["
                                        + agentId.toString()
                                        + "]: "
                                        + exc);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgProxy.log(BasicLevel.ERROR,
                                  "Message named ["
                                  + messageNames[i]
                                  + "] could not be loaded for queue ["
                                  + agentId.toString()
                                  + "]: "
                                  + exc);
      }
    }

    // Retrieving the messages persisted in vectors.
    for (int i = 0; i < vectorNames.length; i++) {
      try {
        persistedVec = (Vector) tx.load(vectorNames[i]);
        // Browsing the messages to check if they are still to be persisted.
        for (enum = persistedVec.elements(); enum.hasMoreElements();) {
          msg = (Message) enum.nextElement();
          msgId = msg.getIdentifier();
          if (idsToPersist.contains(msgId))
            messages.add(msg);
        }
      }
      catch (Exception exc) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                        "Vector named ["
                                        + vectorNames[i]
                                        + "] could not be loaded for queue ["
                                        + agentId.toString()
                                        + "]: "
                                        + exc);
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
          MomTracing.dbgProxy.log(BasicLevel.ERROR,
                                  "Vector named ["
                                  + vectorNames[i]
                                  + "] could not be loaded for queue ["
                                  + agentId.toString()
                                  + "]: "
                                  + exc);
      }
    }
    return messages;
  }

  /** Deletes all persisted objects. */
  public void deleteAll()
  {
    Transaction tx = AgentServer.getTransaction();

    // Retrieving the names of the persistence objects previously saved. 
    String[] messageNames = tx.getList("msg" + agentId);
    String[] vectorNames = tx.getList("msgs" + agentId);

    // Deleting the objects.
    for (int i = 0; i < messageNames.length; i++)
      tx.delete(messageNames[i]);
    for (int i = 0; i < vectorNames.length; i++)
      tx.delete(vectorNames[i]);
  }


  /** Deserializes a <code>PersistenceModule</code>. */
  private void readObject(java.io.ObjectInputStream in)
               throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    toBeSaved = new Hashtable();
    toBeUpdated = new Hashtable();
    idsToBeDeleted = new Hashtable();
    idsTable = new HashMap();
  }
}

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
package fr.dyade.aaa.mom.comm;

import fr.dyade.aaa.mom.messages.Message;

import java.util.*;

/**
 * A <code>TopicMsgsReply</code> instance is used by a topic for sending
 * messages to an agent client which subscribed at least one time to it.
 * <p>
 * A <code>TopicMsgsReply</code> is a particular reply not replying to a
 * single subscription request but potentially to many. This is for 
 * optimizing the case where an agent client would have many subscriptions
 * to a same topic, by not sending many times a same message.
 */
public class TopicMsgsReply extends AbstractReply
{
  /**
   * Table of messages carried by this reply.
   * <p>
   * <b>Key:</b> message identifier<br>
   * <b>Object:</b> message
   */
  private Hashtable messagesTable;
  /**
   * Table of message identifiers per target subscription.
   * <p>
   * <b>Key:</b> subscription name<br>
   * <b>Object:</b> vector of message identifiers
   */
  private Hashtable subsTable;
  /** Enumeration of the subscription names. */
  private Enumeration subNames = null;


  /**
   * Constructs a <code>TopicMsgsReply</code>.
   */
  public TopicMsgsReply()
  {
    super(0, null);
    messagesTable = new Hashtable();
    subsTable = new Hashtable();
  }


  /** Adds a message and the vector of subscriptions it replies to. */
  public void addMessage(Message msg, Vector subNames)
  {
    // Putting the message in the table:
    messagesTable.put(msg.getIdentifier(), msg);

    String subName;
    Vector ids;
      
    // Browsing the subscriptions the message is destinated to:
    for (int i = 0; i < subNames.size(); i++) {
      subName = (String) subNames.get(i);
     
      // Adding the message identifier to the vector of identifiers: 
      ids = (Vector) subsTable.get(subName);
      if (ids == null) {
        ids = new Vector();
        subsTable.put(subName, ids);
      }
      ids.add(msg.getIdentifier());
    }
  }

  /** Returns <code>true</code> if the reply is empty. */
  public boolean isEmpty()
  {
    return messagesTable.isEmpty();
  }

  /** Returns the table of messages hold by this reply. */
  public Hashtable getAllMessages()
  {
    return messagesTable;
  }

  /**
   * Returns <code>true</code> if this reply carries more subscription
   * names.
   */
  public boolean hasMoreSubs()
  {
    if (subNames == null)
      subNames = subsTable.keys();

    return subNames.hasMoreElements();
  }

  /** Returns the next subscription name. */
  public String nextSub()
  {
    if (subNames == null)
      subNames = subsTable.keys();

    try {
      return (String) subNames.nextElement();
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns a vector of message identifiers destinated to a given
   * subscription.
   */
  public Vector getIds(String subName)
  {
    return (Vector) subsTable.remove(subName);
  }
} 

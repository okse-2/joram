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
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.ProducerMessages;

import java.util.*;
  
/**
 * An <code>XAContext</code> instance is used by an XA session for holding
 * the state of its wrapped session after it has been delisted from a
 * transaction.
 * <p>
 * "State" actually means the messages produced and the acknowledgements to
 * perform.
 */
class XAContext
{
  /** The transaction status. */
  int status;
  /**
   * Table holding the <code>ProducerMessages</code> produced in the
   * transaction.
   * <p>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> <code>ProducerMessages</code>
   */
  Hashtable sendings;
  /** 
   * Table holding the identifiers of the messages delivered per
   * destination or subscription, in the transaction.
   * <p>
   * <b>Key:</b> destination or subscription name<br>
   * <b>Object:</b> corresponding <code>MessageAcks</code> instance
   */
  Hashtable deliveries;


  /**
   * Constructs an <code>XAContext</code> instance.
   */
  XAContext()
  {
    sendings = new Hashtable();
    deliveries = new Hashtable();
  }


  /**
   * Adds new sendings performed by the resumed transaction.
   */
  void addSendings(Hashtable newSendings)
  {
    String newDest;
    ProducerMessages newPM;
    ProducerMessages storedPM;

    // Browsing the destinations for which messages have been produced:
    Enumeration newDests = newSendings.keys();
    while (newDests.hasMoreElements()) {
      newDest = (String) newDests.nextElement();
      newPM = (ProducerMessages) newSendings.remove(newDest);
      storedPM = (ProducerMessages) sendings.get(newDest);
      // If messages haven't already been produced for this destination,
      // storing the new ProducerMessages object:
      if (storedPM == null)
        sendings.put(newDest, newPM);
      // Else, adding the newly produced messages to the existing
      // ProducerMessages:
      else
        storedPM.addMessages(newPM.getMessages());
    }
  }

  /**
   * Adds new deliveries occured within the resumed transaction.
   */
  void addDeliveries(Hashtable newDeliveries)
  {
    String newName;
    MessageAcks newAcks;
    MessageAcks storedAcks;

    // Browsing the destinations or subscriptions to which messages will have
    // to be acknowledged:
    Enumeration newNames = newDeliveries.keys();
    while (newNames.hasMoreElements()) {
      newName = (String) newNames.nextElement();
      newAcks = (MessageAcks) newDeliveries.remove(newName);
      storedAcks = (MessageAcks) deliveries.get(newName);
      // If there are no messages to acknowledge for this destination or 
      // subscription, storing the new vector:
      if (storedAcks == null)
        deliveries.put(newName, newAcks);
      // Else, adding the new ids to the stored ones:
      else
        storedAcks.addIds(newAcks.getIds());
    }
  }
}
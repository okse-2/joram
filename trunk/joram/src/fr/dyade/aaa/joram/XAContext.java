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
    Vector msgs;

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
      else {
        msgs = newPM.getMessages();
        for (int i = 0; i < msgs.size(); i++)
          storedPM.addMessage((fr.dyade.aaa.mom.messages.Message) msgs.get(i));
      }
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

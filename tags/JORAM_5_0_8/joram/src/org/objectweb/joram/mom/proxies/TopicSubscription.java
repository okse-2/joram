/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2005 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * The <code>TopicSubscription</code> class holds the parameters of a proxy's
 * subscription to a topic.
 */
class TopicSubscription {
  /** Table of subscriptions selectors. */
  private Hashtable subs;
  /** Last built selector. */
  private String lastSelector = null;


  /** 
   * Creates a <code>TopicSubscription</code> instance.
   */
  TopicSubscription() {
    this.subs = new Hashtable();
  }

 
  /**
   * Adds a new subscription or updates an existing one.
   *
   * @param name  Subscription name.
   * @param selector  Selector.
   */
  void putSubscription(String name, String selector) {
    if (selector == null)
      selector = "";
    subs.put(name, selector);
  }

  /**
   * Removes a subscription.
   *
   * @param name  Subscription name.
   */
  void removeSubscription(String name)  {
    subs.remove(name);
  }

  /** Returns <code>true</code> if the subscriptions table is empty. */
  boolean isEmpty() {
    return subs.isEmpty();
  }

  /** Returns a selector built from the subscriptions' selectors. */
  String buildSelector() {
    String currentSel;
    String builtSelector = null;
    for (Enumeration names = subs.keys(); names.hasMoreElements();) {
      currentSel = (String) subs.get(names.nextElement());

      if (currentSel.equals("")) return "";
      
      if (builtSelector == null)
        builtSelector = "(" + currentSel + ")";
      else
        builtSelector = builtSelector + " OR (" + currentSel + ")";
       
    }
    return builtSelector;
  }

  /** Sets the last selector value. */
  void setLastSelector(String selector) {
    this.lastSelector = selector;
  }

  /** Returns the last selector value. */
  String getLastSelector() {
    return lastSelector;
  }

  /** Returns the names of the subscriptions. */
  Enumeration getNames() {
    return subs.keys();
  }
}

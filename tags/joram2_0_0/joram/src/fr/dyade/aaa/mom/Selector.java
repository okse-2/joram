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
package fr.dyade.aaa.mom; 
 
import java.lang.*; 
import java.util.*; 
import fr.dyade.aaa.agent.*; 
 
/** 
 *	A <code>Selector</code> allows to filter messages. 
 * 
 * @see  fr.dyade.aaa.mom.Topic 
 * @see  fr.dyade.aaa.mom.AgentClient
 */ 
public class Selector implements java.io.Serializable { 
  /** The message hold in the Queue. */ 
  fr.dyade.aaa.mom.Message msg; 
  /** The selector for filtering. */
  String selector;

  public Selector() {
    selector = null;
  }


  public boolean isAvailable(fr.dyade.aaa.mom.Message msg, 
    String selector) throws Exception {

    if(selector==null)
      return true;

    if(selector.length() != 0) {
      String messageID = msg.getJMSMessageID();
      int last = messageID.indexOf('_');

      // DEBUG
      if(Debug.debug) {
        if(Debug.JmsSelector)
          System.out.println("selector : "+selector+"messagID : "+messageID);
      }

      String agent = messageID.substring(0,last);

      if(selector.equals(agent))
        return true;
      else
        return false;
    } else
      return true;
  }
}

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
 * A <code>Destination</code> holds methods used by 
 * both Queue and Topic agents.
 * 
 * @see  fr.dyade.aaa.mom.Topic 
 * @see  fr.dyade.aaa.mom.AgentClient 
 */ 
public class Destination extends fr.dyade.aaa.agent.Agent { 

  public void react(AgentId from, Notification not) throws Exception { 
    if (not instanceof NotificationCloseDestination) { 
      notificationCloseDestination(from, (NotificationCloseDestination) not); 
    } else { 
      super.react(from, not); 
    } 
  }


  /** allows to notify to a Queue or a Topic to delete itself */
  protected void notificationCloseDestination(AgentId from, NotificationCloseDestination not) {
    super.delete();
  }	
	
	
  /** Method checking if message is OK. */
  public static boolean checkMessage(fr.dyade.aaa.mom.Message msg) throws Exception {
    if(msg.getJMSExpiration() != 0) {
      if((System.currentTimeMillis() - msg.getJMSExpiration()) >= 0) 
        return false;
      else
        return true;
    } else {
      return true;
    }
  }

	
	/** send an exception to an agentClient */
	protected void deliveryException (AgentId to, NotificationMOMRequest not, MOMException exc) {
		/* construction of the exception notification except in auto-acknowledge */ 
		fr.dyade.aaa.mom.NotificationMOMException notException = new fr.dyade.aaa.mom.NotificationMOMException(not, exc, not.driverKey); 
		sendTo(to, notException);
	}


  /** Method sending an acknowledgement to an AgentClient's request. */
  protected void deliveryAgreement (AgentId to, NotificationMOMRequest not) {
    fr.dyade.aaa.mom.NotifAckFromDestination notAgree = 
      new fr.dyade.aaa.mom.NotifAckFromDestination(not, not.driverKey); 

    sendTo(to, notAgree);
  }
}

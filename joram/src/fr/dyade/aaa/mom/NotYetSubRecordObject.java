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
/* 
 *	a NotYetSubRecordObject is the object used in the notYetSubscriptionRecordTable
 *	of the CommonClientAAA to wait the response of the Topic, ie if the
 *	subscription is accepted or not.
 *
 *	@see	fr.dyade.aaa.mom.CommonClientAAA
 */


public class NotYetSubRecordObject implements java.io.Serializable { 
 
 	/** the identifier of the session */
	public String sessionID;
	
	/** tests if the subscription is durable or not */
	public boolean subDurable;
	
	/** the mode of acknowledgment of the session */
	public int ackMode;
	
	/** Constructor */
	public NotYetSubRecordObject(String sessionID, boolean subDurable, int ackMode) {
		this.sessionID = sessionID;
		this.subDurable = subDurable;
		this.ackMode = ackMode;
	}

}

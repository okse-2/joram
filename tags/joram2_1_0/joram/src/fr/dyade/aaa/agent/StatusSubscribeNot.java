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


package fr.dyade.aaa.agent;

/**
 * Subscribe notification to status events.
 */
public class StatusSubscribeNot extends SubscribeNot {
public static final String RCS_VERSION="@(#)$Id: StatusSubscribeNot.java,v 1.6 2001-08-31 08:14:00 tachkeni Exp $";
    /**
     * The name of the status to listen to.
     */
    public String statusName;
    
    public StatusSubscribeNot() { }

    /**
     * Creates a subscribe notification to status events.
     * @param action the action (ADD or REMOVE).
     * @param statusName the name of the status attribute to listen to.
     */
    public StatusSubscribeNot(int action,String statusName) {
	super(action);
	this.statusName = statusName;
    }

    /**
     * Provides a string image for this object.
     * @return string image of this object.
     */
    public String toString() {
	return "(" + super.toString() +
	    ",statusName=" + statusName + ")";
    }
}

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
 * Subscribe notification to output events.
 */
public class OutputSubscribeNot extends SubscribeNot {
public static final String RCS_VERSION="@(#)$Id: OutputSubscribeNot.java,v 1.5 2001-05-14 16:26:41 tachkeni Exp $";
    
    /**
     * The name of the role to listen to.
     */
    public String roleName;
    
    /**
     * The type of output notification to listen to.
     */
    public String notifType;

    public OutputSubscribeNot() { }
    
    /*
     * Creates a subscribe notification to output events.
     * @param action the action (ADD or REMOVE).
     * @param role the name of the role to listen to.
     * @param notifType the type of output notification to listen to.
     */
    public OutputSubscribeNot(int action,String roleName,String notifType) {
	super(action);
	this.notifType = notifType;
	this.roleName = roleName;
    }

    /**
     * Provides a string image for this object.
     * @return string image of this object.
     */
    public String toString() {
	return "(" + super.toString() +
	    ",roleName=" + roleName +
	    ",notifType=" + notifType + ")";
    }
}

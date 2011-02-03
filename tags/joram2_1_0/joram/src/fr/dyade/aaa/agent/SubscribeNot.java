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
 * This is the super class of all subscribe notification.
 */
public class SubscribeNot extends Notification {
public static final String RCS_VERSION="@(#)$Id: SubscribeNot.java,v 1.6 2001-08-31 08:14:00 tachkeni Exp $";
    /**
     * Add listener action.
     */
    public static final int ADD = 0;

    /**
     * Remove listener action.
     */
    public static final int REMOVE = 1;

    public static final String[] actionName = {"ADD","REMOVE"};

    /**
     * The action (ADD or REMOVE).
     */
    public int action;

    public SubscribeNot() { }

    /**
     * Creates a subscribe notification.
     * @param action the action (ADD or REMOVE).
     */
    public SubscribeNot(int action) {
	this.action = action;
    }

    /**
     * Provides a string image for this object.
     * @return string image of this object.
     */
    public String toString() {
	return "(" + super.toString() +
	    ",action=" + actionName[action] + ")";
    }
}
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

import java.io.*;

import fr.dyade.aaa.util.*;

/**
 * MsgItem
 * Be careful: this class must be public, otherwise it cannot be included
 * in a message (Class.newInstance)
 * @version	v1.3, 23 Jan 97
 * @author	Freyssinet Andr*
 * @see		msgQueue
 */
public class MsgQItem implements SerializAAA {
    private static final boolean DEBUG = true;

    AgentId ag;
    Notification not;

    public void Encode(DataOutputStream s) throws IOException {
	ag.Encode(s);
	//System.out.println("Test OLAN. Message code : Notification code :");

	String cn = not.getClass().getName();
	//System.out.println("Test OLAN. Message code : Notification code : cn.lengt" + cn.length());
	s.writeInt(cn.length());
	//System.out.println("Test OLAN. Message code : Notification code : cn" + cn);
	s.writeChars(cn);
	//System.out.println("Test OLAN. Message code : Notification state code ");
	not.Encode(s);	
    }

    public void Decode(DataInputStream s) throws IOException {
	ag = new AgentId(s);
	char tmp[] = new char[s.readInt()];
	for (int i=0; i<tmp.length; i++)
	    tmp[i] = s.readChar();
	String cn = new String(tmp);
	try {
	    not = (Notification) Class.forName(cn).newInstance();
	} catch (Exception exc) {
	    // ClassNotFoundException, InstantiationException and IllegalAccessException
	    if (DEBUG) System.err.println("MsgQItem.Decode[" + Thread.currentThread() + "]: " + exc);
	}
	not.Decode(s);
    }

    public MsgQItem() {}
    
    public MsgQItem(DataInputStream s) throws IOException {
	Decode(s);
    }
    
    public MsgQItem(AgentId a, Notification n) {
	ag = a;
	not = n;
    }
}

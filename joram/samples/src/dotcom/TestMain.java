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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package dotcom;

import javax.naming.*;
import java.util.*;

/**
 *
 */
public class TestMain {

    static Context ictx = null; 

    public static void main(String[] arg) {
	try {
	    // Get InitialContext
	    ictx = new InitialContext();
	    System.out.println("InitialContext = " + ictx);

	    String nameClassPair = null;
	    Binding b = null;
	    try {
		NamingEnumeration ne = ictx.list("");
		System.out.print("list =(");
		for (Enumeration e = ne; e.hasMoreElements() ;) {
		    nameClassPair = ((NameClassPair) e.nextElement()).toString();
		    System.out.println("\t" + nameClassPair + ",");
		}
		System.out.println(")");
	    } catch (Exception ex) { 
		System.out.println(" exception list");
	    }

	    ictx.close();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(2);
	}
    }
}

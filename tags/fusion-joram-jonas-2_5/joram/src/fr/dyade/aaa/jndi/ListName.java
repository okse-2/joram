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

package fr.dyade.aaa.jndi;

import java.io.*;
import java.util.*;
import javax.naming.*;

public class ListName implements NamingEnumeration {
    protected Enumeration names;
    protected Hashtable table;
    
    public ListName(Hashtable table ) {
	this.table = table;
	this.names = table.keys();
    }    
    public boolean hasMore() throws NamingException {
	return names.hasMoreElements();
    }
    public Object next() throws NamingException {
	String name = (String) names.nextElement();
	String className;
	Object obj = table.get(name);
	if (obj instanceof Reference) {
	    className = ((Reference) obj).getClassName();
	} else {
	    className = obj.getClass().getName();
	}
	return new NameClassPair(name, className);
    }
    public void close() {
    }
    public Object nextElement() {
	try {
	    return next();
	} catch (NamingException e) {
	    throw new NoSuchElementException(e.toString());
	}
    }
    public boolean hasMoreElements() {
	try {
	    return hasMore();
	} catch (NamingException e) {
	    return false;
	}
    }
}


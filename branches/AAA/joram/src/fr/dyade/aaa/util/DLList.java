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

package fr.dyade.aaa.util;

/**
 * A class that implements a double linked list.
 * For example:
 * <pre>
 * 	class intList extends DLList {
 *	    int value;
 *	    public intList(int v) {
 *	        super();
 *	        value = v;
 *	    }
 *	    public intList(int v, DLList list) {
 *	        super(list);
 *	        value = v;
 *	    }
 *	}
 * </pre>
 * @version	v1.0, 18 Dec 1996
 * @author	Freyssinet Andr*
 */
public class DLList {
    /**
     * Pointer to next and previous element.
     */
    DLList next, prev;

    /**
     * Creates an element linked on itself.
     */
    public DLList() {
	prev = next = this;
    }

    /**
     * Creates an element and links it at the end of the list.
     * @param list     The head list.
     */
    public DLList(DLList list) {
	if (list == null) {
	    next = prev = this;
	} else {
	    prev = list.prev;
	    next = list;
	    next.prev = this;
	    prev.next = this;
	}
    }

    /**
     * Get the next element of the list.
     */
    public DLList getNext() {
	return next;
    }
    
    /**
     * Deletes the current element from the list.
     */
    public void delete() {
	next.prev = prev;
	prev.next = next;
	next = prev = null;
    }
}

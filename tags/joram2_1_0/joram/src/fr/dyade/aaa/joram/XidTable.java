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

package fr.dyade.aaa.joram;

import java.io.*;
import java.util.*;
import javax.transaction.xa.*;

/**
 * XidTable is used to store and manage several transactions in a XA session.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class XidTable implements Serializable {
    
    /* The list of Xid status */

    /** The transaction does not exist */
    public final static int UNKNOWN = -1;
    /** The transaction is being processed */
    public final static int ACTIVE = 0;
    /** The transaction has been temporarily stopped */
    public final static int SUSPENDED = 1;
    /** The transaction has been ended and wait for a commit or a rollback */
    public final static int SUCCESS = 2;
    /** The transaction must be rollbacked */
    public final static int RB_ONLY = 3;
    /** The transaction has been prepared */
    public final static int PREPARED = 4;
    /** The transaction has been committed */
    public final static int COMMITTED = 5;
    /** The transaction has been rollbacked */
    public final static int ROLLBACKED = 6;

    private Hashtable messageToSendTable;
    private Hashtable messageToAckTable;
    private Hashtable statusTable;


    public XidTable() {
	super();
	messageToSendTable = new Hashtable();
	messageToAckTable = new Hashtable();
	statusTable = new Hashtable();
    }
    

    /**
     * Remove a xid from the list.
     */
    public void removeXid(javax.transaction.xa.Xid xid) throws Exception {
	if (statusTable.containsKey(xid)) {
	    messageToSendTable.remove(xid);
	    messageToAckTable.remove(xid);
	    statusTable.remove(xid);
	} else {
	    throw new Exception("Unknown Xid");
	}
    }


    /**
     * Get a xid vector from the 'message to send' list.
     */
    public Vector getMessageToSendXid(javax.transaction.xa.Xid xid) throws Exception {
	if (statusTable.containsKey(xid)) {
	    return ((Vector) messageToSendTable.get(xid));
	} else {
	    throw new Exception("Unknown Xid");
	}
    }


    /**
     * Get a xid vector from the 'message to ack' list.
     */
    public Vector getMessageToAckXid(javax.transaction.xa.Xid xid) throws Exception {
	if (statusTable.containsKey(xid)) {
	    return ((Vector) messageToAckTable.get(xid));
	} else {
	    throw new Exception("Unknown Xid");
	}
    }


    /**
     * Set a xid in the lists.
     */
    public void setXid(javax.transaction.xa.Xid xid, Vector xidSendVector, Vector xidAckVector) throws Exception {
	if (statusTable.containsKey(xid)) {
	    messageToSendTable.put(xid, new Vector(xidSendVector));
	    messageToAckTable.put(xid, new Vector(xidAckVector));
	} else {
	    throw new Exception("Unknown Xid");
	}
    }


    /**
     * Set a xid in the 'message to send' list.
     */
    public void setMessageToSendXid(javax.transaction.xa.Xid xid, Vector xidSendVector) {
	if (statusTable.containsKey(xid)) {
      Vector newVec = (Vector) messageToSendTable.get(xid);
      Enumeration newElements = xidSendVector.elements();
      while (newElements.hasMoreElements())
        newVec.add(newElements.nextElement());
      messageToSendTable.put(xid, newVec);
	}
    else
	  messageToSendTable.put(xid, new Vector(xidSendVector));
    }


    /**
     * Set a xid in the 'ack to send' list.
     */
    public void setAckToSendXid(javax.transaction.xa.Xid xid, Vector xidAckVector) {
	if (statusTable.containsKey(xid)) {
      Vector newVec = (Vector) messageToAckTable.get(xid);
      Enumeration newElements = xidAckVector.elements();
      while (newElements.hasMoreElements())
        newVec.add(newElements.nextElement());
      messageToAckTable.put(xid, newVec);
	}
    else
	messageToAckTable.put(xid, new Vector(xidAckVector));
    }
    
    /**
     * Return true if the xid is already registered.
     */
    public boolean registeredXid(javax.transaction.xa.Xid xid) {
	return statusTable.containsKey(xid);
    }


    /**
     * Get the status of the transaction.
     */
    public int getXidStatus(javax.transaction.xa.Xid xid) {
	if (!statusTable.containsKey(xid)) {
	    return UNKNOWN;
	} else {
	    return ((Integer) statusTable.get(xid)).intValue();
	}
    }


    /**
     * Set the status of the transaction.
     */
    public void setXidStatus(javax.transaction.xa.Xid xid, int status) throws Exception {
	statusTable.put(xid, new Integer(status));
    }


    /**
     * Get the list of all Xid.
     */
    public javax.transaction.xa.Xid[] getXidList() throws Exception {
  	Vector v = new Vector(statusTable.keySet());
  	int size = v.size();


  	Xid[] xidArray = new Xid[size];
  	for (int i = 0; i < size; i++) {
  	    xidArray[i] = (javax.transaction.xa.Xid) v.elementAt(i);
  	}
	return xidArray;
	//return ((javax.transaction.xa.Xid[]) statusTable.keySet().toArray());
    }

    
    /**
     * Returns a string representation of this Xid.
     */
    public String toString() {
	return "Acks : " + messageToAckTable + "\nMsgs : " + messageToSendTable;
    }

} // XidTable

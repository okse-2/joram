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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.ip.*;
import fr.dyade.aaa.ns.*;
import java.io.*;
import java.net.*;

public class SerialOutputStream implements NotificationOutputStream {
    ObjectOutputStream out;
    
    /**
     * Creates a filter built on top of the specified <code>OutputStream</code>.
     */
    public SerialOutputStream(OutputStream out) throws IOException {
	this.out = new ObjectOutputStream(out);
    }
    
    /**
     * Writes a <code>Notification</code> to the stream.
     */
    public void writeNotification(Notification not) throws IOException {
	Object obj = null;
	if (not instanceof LookupReportObject) {
	    obj =  ((LookupReportObject) not).getObject();
	} else if (not instanceof ListReportObject) {
	    obj =  ((ListReportObject) not).getObject();
	} else if (not instanceof NotificationNamingException) {
	    obj = ((NotificationNamingException) not).getObject();
	}
	out.writeObject(obj);
	out.reset();
	out.flush();
    }
    
    /**
     * Closes the stream.
     */
    public void close() throws IOException {
	out.close();
    }
}


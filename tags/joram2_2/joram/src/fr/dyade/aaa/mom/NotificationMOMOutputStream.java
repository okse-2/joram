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

import java.io.*; 

/**	the outputStream for the MOM
 *
 *	@see  fr.dyade.aaa.agent.NotificationOutputStream     
 */


public class NotificationMOMOutputStream implements fr.dyade.aaa.agent.NotificationOutputStream {

    /** the inputStream which contains the ObjectInputStream,
     *	ie the MessageJMSMOM
     */
    private ObjectOutputStream out;
    
    /** Constructor of the NotificationMOMInputStream */
    public NotificationMOMOutputStream(OutputStream outNew) throws IOException{
	out = new ObjectOutputStream(outNew);
	out.flush();
    }
    
    /**
     * Writes a <code>Notification</code> to the stream.
     */
	    public void writeNotification(fr.dyade.aaa.agent.Notification not) throws IOException {
		fr.dyade.aaa.mom.MessageMOMExtern msgRequest = ((fr.dyade.aaa.mom.NotificationOutputMessage) not).msgJMSMOM; 
		out.writeObject(msgRequest);
		out.reset();
		out.flush();
	    }
    
    /**
     * Closes the stream.
     */
    public void close() throws IOException {
	//out.flush(); // flush is not obligatory but it costs nothing to do
	out.close();
    }
    
}		

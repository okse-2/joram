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
 
/** 
 *	a TextMessage is as JMS specifications 
 * 
 *	@see         subclasses
 *	javax.jms.Message 
 */ 
 
public class TextMessage extends fr.dyade.aaa.mom.Message implements javax.jms.TextMessage{ 
 
	/** the text of the message */ 
	private java.lang.String txt;
	
	public TextMessage() {
		modeReadWrite = READ_WRITE_MODE;
	}
	
	/** @see jms specifications */ 
	public java.lang.String getText() throws javax.jms.JMSException {
		try {
			return txt;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see jms specifications */ 
	public void setText(java.lang.String txt) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			this.txt = txt;
		} catch (javax.jms.JMSException exc) {
			throw(exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}	
	}
	
	/** overwrite the methode as jms specified */ 
	public void clearBody() throws javax.jms.JMSException {
		try {
			modeReadWrite = READ_WRITE_MODE;
			txt = null;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** put the mode in readOnly */
	public void reset() throws javax.jms.JMSException {
		try {
			modeReadWrite = READ_MODE;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
}

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
 
/** 
 *	a mother class for all Exception thrown by JMS part
 *	number 0-999 are reserved for fr.dyade.aaa.mom.MOMException
 *	this class intercepts the MOMException
 */ 
 
public class JMSAAAException extends javax.jms.JMSException { 

	/** the errors of the JMS part */
	/** the default error code of a MOM Exception*/
	public static final int DEFAULT_JMSAAA_ERROR = 1000;
	
	/** the method is not available */
	public static final int NOT_YET_AVAILABLE = 1001;
	
	/** the method is not available */
	public static final int MOM_INTERNAL_ERROR = 1002;
	
	/** no request corresponds to the messageJMSMOMID */
	public static final int NO_SUCH_REQUEST_SENT = 1003;
	
	/** Internal Error, Session not created */
	public static final int ERROR_CREATION_SESSION = 1004;
	
	/** Internal Error, Session not created */
	public static final int ERROR_CREATION_MESSAGECONSUMER = 1005;
	
	/** Internal Error, No Message available */
	public static final int ERROR_NO_MESSAGE_AVAILABLE = 1006;
	
	/** Internal Error, No Message available */
	public static final int ERROR_CONNECTION_MOM = 1007;
	
	/** Internal Error, No session is listening on this subscription */
	public static final int NO_SESSION_LISTENING = 1008;
	
	/** Name of the subscription already taken */
	public static final int NAME_SUBSCRIPTION_ALREADY_TAKEN = 1009;
	
	/** Incorrect Syntax of the unsubscription */
	public static final int INCORRECT_SYNTAX_UNSUBSCRIBE = 1010;
	
	/** the messageID to acknowledge is null */
	public static final int MESSAGEID_NULL = 1011;
	
	/** the session is in transacted mode */
	public static final int SESSION_NOT_TRANSACTED = 1012;
	
	/** the session is not in transacted mode */
	public static final int SESSION_TRANSACTED = 1013;
	
	
	/** the error Code of the exception */
	private int errorCode;
	
	/** Construct a MOMException with reason and errorCode for exception */
	public JMSAAAException(String reason, int errorCodeNew) {
		super(reason, java.lang.String.valueOf(errorCodeNew));
		this.errorCode = errorCodeNew;
	}
	
	/** Construct a MOMException with reason and default errorCode for exception */
	public JMSAAAException(String reason) {
		super(reason, java.lang.String.valueOf(DEFAULT_JMSAAA_ERROR));
		this.errorCode = DEFAULT_JMSAAA_ERROR;
	}
	
	/** get the error Code */
	public int getJMSAAAErrorCode() {
		return this.errorCode;
	}
	
}

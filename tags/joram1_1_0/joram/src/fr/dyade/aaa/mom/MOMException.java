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
 *	a mother class for all Exception thown in the MOM
 * 
 *	@see		fr.dyade.aaa.mom.Topic 
 *	@see		fr.dyade.aaa.mom.AgentClient
 *	@see		fr.dyade.aaa.mom.Destination
 */ 
 
public class MOMException extends Exception { 
	
	/** the errors of the MOM */
	/** the default error code of a MOM Exception*/
	public static final int DEFAULT_MOM_ERROR = 0;
	
	/** Fields of the message incomplete */
	public static final int MESSAGE_INCOMPLETE = 1;
	
	/** The messageID for an ack doesn't exist in the Queue 
	 *	canceling of all previous actions to come back in stable state
	 *	allows to do only 1 pass
	 */
	public static final int MESSAGEID_NO_EXIST = 2;
	
	/** a subscription of a client already exist for a particular theme  */
	public static final int SUBSCRIPTION_ALREADY_EXIST = 3;
	
	/** a theme doesn't exist in a particular Topic */
	public static final int THEME_NO_EXIST = 4;
	
	/** the subscription of the client he wants to destroy doesn't exist */
	public static final int SUBSCRIPTION_NO_EXIST = 5;
	
	/** the acknowledge message doesn't exist in the Topic
	 *	canceling of all previous actions to come back in stable state
	 *	allows to do only 1 pass
	 */
	public static final int TOPIC_MESSAGEID_NO_EXIST = 6;
	
	/** no corresponding request Id of the client is present in the hastbales */
	public static final int NO_SUCH_REQUESTID_EXIST = 7;
	
	/** message received on agentClient with no subscription exists */
	public static final int MSG_RECEIVED_WITHOUT_SUBSCRIPTION = 8;
	
	/** the destruction requested by the client is impossible because, no such
	 *	object exists. AgentId is unknown
	 */
	public static final int NO_SUCH_TEMPORARY_DESTINATION_EXIST = 9;
	
	/** Error arised during the deployement of a temporary Queue or Topic */
	public static final int ERROR_DURING_DEPLOYEMENT = 10;
	
	/** incorrect name of agent Queue or Topic given by the Client */
	public static final int INCORRECT_NAME_OF_AGENT = 11;
	
	/** no session exists for the subscription */
	public static final int NO_SUCH_SESSION_EXIST = 12;
	
	
	/** the error Code of the exception */
	private int errorCode;
	
	/** Construct a MOMException with reason and errorCode for exception */
	public MOMException(String reason, int errorCodeNew) {
		super(reason);
		errorCode = errorCodeNew;
	}
	
	/** Construct a MOMException with reason and default errorCode for exception */
	public MOMException(String reason) {
		super(reason);
		errorCode = DEFAULT_MOM_ERROR;
	}
	
	/** Construct a MOMException with default reason and default errorCode for exception */
	public MOMException() {
		super("Error MOM ");
		errorCode = DEFAULT_MOM_ERROR;
	}
	
	/** get the error Code */
	public int getErrorCode() {
		return errorCode;
	}
}

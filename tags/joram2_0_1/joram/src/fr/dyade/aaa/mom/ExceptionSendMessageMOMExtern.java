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
 * An <code>ExceptionSendMessageMOMExtern</code> contains an exception 
 * that occured after a client sending.<br>
 * It is sent by an <code>AgentClient</code> when it receives a
 * <code>NotificationMOMException</code>.
 * 
 * @see  fr.dyade.aaa.mom.MessageMOMExtern 
 * @see  fr.dyade.aaa.mom.CommonClientAAA
 */ 
public class ExceptionSendMessageMOMExtern
  extends ExceptionMessageMOMExtern
{ 
  /** The message that produced the exception. */
  public fr.dyade.aaa.mom.Message message;
	
  /** Constructor. */
  public ExceptionSendMessageMOMExtern(long requestID, 
    java.lang.Exception exception,
    fr.dyade.aaa.mom.Message message, int driverOutKey)
  {
    super(requestID, exception, driverOutKey);
    this.message = message;
  }
	
}

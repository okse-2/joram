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
package fr.dyade.aaa.agent;

/**
 * DeleteAck - Acknowledge for deleting an agent
 */
public class DeleteAck extends Notification { 
  public static final String RCS_VERSION="@(#)$Id: DeleteAck.java,v 1.10 2002-10-21 08:41:13 maistrfr Exp $"; 

  public AgentId agent = null;

  public static final int OK = 0;
  public static final int DENIED = 1;
  public static final int EXCEPTION = 2;

  public int status = OK;

  public Throwable exc;

  public DeleteAck(AgentId agent) {
    this.status = OK;
    this.agent = agent;
  }

  public DeleteAck(AgentId agent, Throwable exc) {
    this.status = EXCEPTION;
    this.agent = agent;
    this.exc = exc;
  }

  public DeleteAck(AgentId agent, int status) {
    this.status = status;
    this.agent = agent;
  }
}

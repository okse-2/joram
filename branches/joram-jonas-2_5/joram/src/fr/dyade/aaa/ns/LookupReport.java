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

package fr.dyade.aaa.ns;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification reporting the agent associated with a name, as result
 * from a <code>LookupCommand</code> request.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	LookupCommand
 */
public class LookupReport extends SimpleReport {

public static final String RCS_VERSION="@(#)$Id: LookupReport.java,v 1.6 2002-03-26 16:09:47 joram Exp $";

  /** agent associated with name */
  private AgentId agent;

  /**
   * Creates a notification to be sent.
   *
   * @param command		executed command
   * @param status		completion status
   * @param message		optional message
   * @param agent		agent associated with name
   */
  public LookupReport(SimpleCommand command, int status, String message,
		      AgentId agent)
  {
    super(command, status, message);
    this.agent = agent;
  }

  /**
   * Accesses read only property.
   *
   * @return		agent associated with name
   */
  public AgentId getAgent() { return agent; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",agent=" + agent + ")";
  }
}

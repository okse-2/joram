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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.Strings;


/**
 * Notification describing a basic name service command.
 * Only used by <code>NameService</code> agents.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		NameService
 */
public class SimpleCommand extends Notification {

public static final String RCS_VERSION="@(#)$Id: SimpleCommand.java,v 1.4 2002-01-16 12:46:47 joram Exp $";

  /** agent to report completion status to */
  private AgentId report;

  /** name of target entry of the command */
  private String name;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   */
  public SimpleCommand(AgentId report, String name) {
    this.report = report;
    this.name = name;
  }

  /**
   * Accesses property.
   * Allows differed setting of report variable.
   * Does not allow changing the variable.
   *
   * @param report	agent to report status to
   */
  public void setReport(AgentId report) throws Exception {
    if (this.report != null) {
      throw new IllegalArgumentException("cannot change report: " + this);
    }
    this.report = report;
  }

  /**
   * Accesses property.
   *
   * @return		agent to report status to
   */
  public final AgentId getReport() {
    if (report == null)
      return AgentId.nullId;
    else
      return report;
  }

  /**
   * Accesses read only property.
   *
   * @return		name of target entry of the command
   */
  public String getName() { return name; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",report=" + report + 
      ",name=" + Strings.toString(name) + ")";
  }
}

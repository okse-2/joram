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
 * Notification requesting a name-object registration.
 * The <code>NameService</code> agent replies with a <code>SimpleReport</code>
 * notification sent to the agent referenced in <code>report</code>.
 *
 * @author	Nicolas Tachker
 * @version	v1.0
 *
 */
public class RegisterObject extends SimpleCommand {

public static final String RCS_VERSION="@(#)$Id: RegisterObject.java,v 1.4 2002-01-16 12:46:47 joram Exp $";

  /** object associated with name */
  private Object obj;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   * @param agent		object associated with name
   */
  public RegisterObject(AgentId report, String name, Object obj) {
    super(report, name);
    this.obj = obj;
  }

  /**
   * Accesses read only property.
   *
   * @return  object associated with name
   */
  public Object getObject() { return obj; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",object=" + obj + ")";
  }
}

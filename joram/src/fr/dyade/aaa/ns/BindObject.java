/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 */

package fr.dyade.aaa.ns;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification requesting a name-object registration.
 * The <code>NameService</code> agent replies with a <code>BindReportObject</code>
 * notification sent to the agent referenced in <code>report</code>.
 *
 * @author	Nicolas Tachker
 * @version	v1.0
 * @see         <code>BindReportObject</code>
 */
public class BindObject extends SimpleCommand {

public static final String RCS_VERSION="@(#)$Id: BindObject.java,v 1.12 2004-02-13 08:14:35 fmaistre Exp $";

  /** object associated with name */
  private Object obj;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   * @param agent		object associated with name
   */
  public BindObject(AgentId report, String name, Object obj) {
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

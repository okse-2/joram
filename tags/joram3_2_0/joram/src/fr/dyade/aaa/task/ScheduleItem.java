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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.task;

import java.io.*;
import java.util.*;
import fr.dyade.aaa.agent.*;


/**
 * Implements a double linked list of <code>ScheduleEvent</code> objects.
 * Both ends are marked with a <code>null</code> value. Events are ordered in
 * increasing order of dates.
 * <p>
 * The <code>status</code> field associated with the <code>ScheduleEvent</code>
 * is used by the <code>Scheduler</code> agent, and is set to <code>true</code>
 * when the <code>true</code> <code>Condition</code> notification has been sent
 * and the <code>Scheduler</code> agent waits for the duration to expire before
 * sending the <code>false</code> condition and resetting the field.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		ScheduleEvent
 */
public class ScheduleItem implements Serializable {

public static final String RCS_VERSION="@(#)$Id: ScheduleItem.java,v 1.3 2002-10-21 08:41:14 maistrfr Exp $"; 


  /** may be of a derived class */
  ScheduleEvent event;
  /** next schedule date */
  Date date;
  /** last sent condition status */
  boolean status;
  /** previous item, null terminated */
  ScheduleItem prev;
  /** next item, null terminated */
  ScheduleItem next;

  /**
   * Creates an item.
   *
   * @param event	event to schedule
   */
  public ScheduleItem(ScheduleEvent event) {
    this.event = event;
    date = null;
    status = false;
    prev = null;
    next = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    for (ScheduleItem item = this; item != null; item = item.next) {
      output.append("(event=");
      output.append(item.event.toString());
      output.append(",date=");
      output.append(item.date);
      output.append(",status=");
      output.append(item.status);
      output.append("),");
    }
    output.append("null)");
    return output.toString();
  }
}

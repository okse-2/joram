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

import java.util.*;
import fr.dyade.aaa.util.*;

/**
 * Contains the events fired by an agent during a reaction.
 * Each event is described by a report.
 */
public class EventNot extends Notification {
public static final String RCS_VERSION="@(#)$Id: EventNot.java,v 1.7 2002-01-16 12:46:47 joram Exp $";
    /**
     * The name of the agent that fires this event.
     */
    public String name;
    
    /**
     * Contains the monitoring reports.
     * @see InputReport
     * @see OutputReport
     * @see StatusReport
     */
    public Vector events = new Vector();

    /**
     * @param name The name of the agent that fires this event.
     */
    public EventNot(String name) {
	this.name = name;
    }

    /**
     * Provides a string image for this object.
     * @return string image of this object.
     */
    public String toString() {
      StringBuffer output = new StringBuffer();
      output.append("(");
      output.append(super.toString());
      output.append(",name=");
      output.append(name);
      output.append(",events=");
      Strings.toString(output,events);
      output.append(")");
      return output.toString();
    }
}

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

import java.io.*;

/**
 * A role wraps an AgentId. It is identified by a name.
 */
public class Role implements Serializable {
public static final String RCS_VERSION="@(#)$Id: Role.java,v 1.2 2000-08-01 09:13:29 tachkeni Exp $";
    /**
     * The wrapped <code>AgentId</code>.
     */
    private AgentId listener;

    /**
     * The role name.
     */
    private String name;

    /**
     * Creates a new role with the specified name.
     * @param name the role name.
     */
    public Role(String name) {
	this.name= name;
    }

    /**
     * Creates a new role with the specified name and AgentId.
     * @param name the role name.
     * @param listener the wrapped <code>AgentId</code>.
     */
    public Role(String name, AgentId listener) {
	this(name);
	this.listener = listener;
    }

    /**
     * Sets the wrapped <code>AgentId</code>.
     * @param listener the wrapped <code>AgentId</code>.
     */
    public void setListener(AgentId listener) {
	this.listener = listener;
    }

    /**
     * Returns the wrapped <code>AgentId</code>.
     */
    public AgentId getListener() {
	return listener;
    }

    /**
     * Returns the role name.
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the role name.
     * @param name the role name.
     */
    public void setName(String name) {
	this.name = name;
    }

    public String toString() {
      StringBuffer output = new StringBuffer();
      output.append("(");
      output.append(super.toString());
      output.append(",name=" + name);
      output.append(",listener=" + listener);
      output.append(")");
      return output.toString();
    }
}

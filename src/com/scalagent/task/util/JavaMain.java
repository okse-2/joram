/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.task.util;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;

/**
 * <code>Program</code> defined as a java class with a main.
 *
 * @see		ProcessManager
 * @see		ProcessEnd
 */
public class JavaMain extends Program {
  /**
   * Default constructor
   */
  public JavaMain() {
    super();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param command	java class name plus <code>main</code> parameters
   * @param classPath	java class path to execute program
   */
  public JavaMain(short to,
                  AgentId parent,
                  String command,
                  String classPath) {
    super(to, parent, (String[]) null);

    String cmd[] = parseCommand(command);

    this.command = new String[3 + cmd.length];

    this.command[0] = "java";
    this.command[1] = "-classpath";
    this.command[2] = classPath;
    for (int i=0; i<cmd.length; i++) {
      this.command[i+3] = cmd[i];
    }
  }
}

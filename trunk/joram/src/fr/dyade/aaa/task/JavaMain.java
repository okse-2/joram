/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
*/
package fr.dyade.aaa.task;

import org.objectweb.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;


/**
 * <code>Program</code> defined as a java class with a main.
 *
 * @see		ProcessManager
 * @see		ProcessEnd
 */
public class JavaMain extends Program {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: JavaMain.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


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
  public JavaMain(short to, AgentId parent, String command, String classPath) {
    super(to, parent, (String[]) null);
    StringBuffer commandLine = new StringBuffer("java");
    if (classPath != null) {
      commandLine.append(" -classpath ");
      commandLine.append(classPath);
    }
    commandLine.append(' ');
    commandLine.append(command);
    this.command = parseCommand(commandLine.toString());
  }
}

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
import java.text.*;
import java.util.*;


/**
  * Class which realizes the <code>DebugProxy</code> functions in an input
  * driver thread.
  * <p>
  * Commands syntax is:
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see	fr.dyade.aaa.ip.DebugProxy
  */
public class DebugDriver implements NotificationInputStream {
public static final String RCS_VERSION="@(#)$Id: DebugDriver.java,v 1.7 2002-01-16 12:46:47 joram Exp $";

  /** token marking end of command in stream */
  static final char commandTerminator = ';';

  /** quote character for strings */
  static final char commandQuote = '"';

  /** help command */		static final int CMD_HELP = 0;
  /** exit command */		static final int CMD_EXIT = 1;
  /** echo command */		static final int CMD_ECHO = 2;
  /** list command */		static final int CMD_LIST = 3;
  /** dump command */		static final int CMD_DUMP = 4;
  /** commands following CMD_* indexes */
  static final String[] commands = {		
    "help",
    "exit",
    "echo",
    "list",
    "dump"
  };

  /** analyzed input */
  protected StreamTokenizer input;

  /** keep stream to be able to close it */
  private InputStream in;

  /** output stream */
  protected PrintWriter out;

  /**
   * Creates a driver. The driver input and output streams are set afterwards
   * by calls to <code>setInputStream</code> and <code>setOutputStream</code>.
   */
  public DebugDriver() {
    input = null;
    in = null;
    out = null;
  }

  /**
   * Sets the driver input stream.
   *
   * @param in		the underlying input stream
   */
  public void setInputStream(InputStream in) {
    this.in = in;
    input = new StreamTokenizer(new BufferedReader(new InputStreamReader(in)));
    input.resetSyntax();
    input.eolIsSignificant(false);
    input.quoteChar(commandQuote);
    input.whitespaceChars(' ', ' ');
    input.whitespaceChars('\n', '\n');
    input.whitespaceChars('\r', '\r');
    input.whitespaceChars('\t', '\t');
    input.wordChars('a', 'z');
    input.wordChars('A', 'Z');
    input.wordChars('_', '_');
    // In order to read AgentId #x.y.z
    input.wordChars('#', '#');
    input.wordChars('.', '.');
    input.wordChars('0', '9');
    /*    input.parseNumbers(); */
    input.slashSlashComments(true);
  }

  /**
   * Sets the driver output stream.
   *
   * @param out		the underlying output stream
   */
  public void setOutputStream(OutputStream out) {
    this.out = new PrintWriter(out, true);
    this.out.println("connected");
    this.out.println("commands end with ;");
    this.out.println("type help; for more help");
  }

  /**
    * Gets a <code>Notification</code> from the stream.
    * <p>
    * This function usually returns notifications to be handled be the
    * associated proxy agent. Here it is used as a kind of main loop, never
    * returning a notification. Instead commands are executed synchronously
    * and results are written onto the output stream.
    */
  public Notification readNotification() throws IOException {
    Notification notification = null;
  parseLoop:
    while (true) {
      StringBuffer modifiers = null;

      try {
	// finds command identifier
	switch (input.nextToken()) {
	case StreamTokenizer.TT_EOF:
	  throw new EOFException();
	case commandTerminator:
	  throw new ParseException("unexpected command terminator", 0);
	case StreamTokenizer.TT_WORD:
	  break;
	default:
	  throw new ParseException("unexpected token", 0);
	}

	int command;
	for (command = commands.length; command-- > 0;) {
	  if (input.sval.equals(commands[command]))
	    break;
	}
	switch (command) {
	case CMD_HELP:
	case CMD_EXIT:
	case CMD_LIST:
	  break;
	case CMD_ECHO:
	case CMD_DUMP:
	  modifiers = new StringBuffer();
	  // gets modifiers
	  break;
	default:
	  throw new ParseException("unexpected command", 0);
	}

	// gets modifiers
	input.nextToken();

	while (true) {
	  switch (input.ttype) {
	  case commandTerminator:
	    // checks command is complete and executes it
	    try {
	      switch (command) {
	      case CMD_HELP:
		help();
		break;
	      case CMD_EXIT:
		break parseLoop;
	      case CMD_LIST:
		list();
		break;
	      case CMD_ECHO:
		echo(modifiers.toString());
		break;
	      case CMD_DUMP:
		dump(modifiers.toString());
		break;
	      }
	    } catch (Exception exc) {
	      out.println("error while executing command " +
			  commands[command] + ":");
	      out.println(exc.toString());
	    }
	    continue parseLoop;
	  default:
	    modifiers.append(" ");
	    modifiers.append(input.ttype);
	    break;
	  case StreamTokenizer.TT_WORD:
	    modifiers.append(" ");
	    modifiers.append(input.sval);
	    break;
	  case StreamTokenizer.TT_NUMBER:
	    modifiers.append(" ");
	    modifiers.append(input.nval);
	    break;
	  }
	  input.nextToken();
	}
      } catch (ParseException exc) {
	parseException(exc);
	// skips input until next terminator
	out.println("skipping up to next ;");
	while (input.ttype != commandTerminator) {
	  if (input.nextToken() == StreamTokenizer.TT_EOF)
	    throw new EOFException();
	}
      }
    }
    // return notification;
    throw new EOFException();
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    if (in != null) {
      in.close();
      in = null;
    }
    if (out != null) {
      out.close();
      out = null;
    }
  }

  /**
   * Provides some help comments.
   *
   * @exception Exception
   *	unspecialized exception
   */
  void help() throws Exception {
    out.println("commands may span over multiple lines and terminate with ;");
    out.println("available commands are :");
    out.println("  help: this message");
    out.println("  exit: closes the connection");
    out.println("  echo [words]: test command");
    out.println("  list: list all agents loaded in memory");
    out.println("  dump [id]: dump agents designed in parameters");
    out.println("    id: #x.y.z");
  }

  /**
   * Executes an echo command.
   *
   * @param s	string to echo on output stream
   *
   * @exception Exception
   *	unspecialized exception
   */
  void echo(String s) throws Exception {
    out.println(s);
  }

  /**
   * Executes a list command.
   */
  void list() {
    AgentId list[] = Agent.getLoadedAgentIdlist();
    for (int i=list.length; --i>=0; )
      out.print(list[i].toString()+ " ");
    out.println();
  }

  /**
   * Executes a list command.
   */
  void dump(String s) {
    try {
      int start = 0;
      int end = 0;
      while ((start = s.indexOf('#', end)) != -1) {
	if ((end = s.indexOf(' ', start)) == -1)
	  end = s.length();
	AgentId id = AgentId.fromString(s.substring(start, end));
	out.println(Agent.dumpAgent(id));
      }
    } catch (Exception exc) {
      exc.printStackTrace(out);
    }
  }

  /**
   * Reacts to an exception while parsing an command.
   *
   * @param e	exception to react to
   */
  void parseException(ParseException e) {
    out.println("parse error at " + e.getErrorOffset() + ": " +
		e.getMessage());
  }
}

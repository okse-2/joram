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
package com.scalagent.scheduler.proxy;

import java.io.*;
import java.text.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.scheduler.event.*;

/**
  * Class which translates ascii scheduling commands into
  * <code>ScheduleEvent</code> notifications.
  *
  * Commands syntax is:
  *	schedule "event name" start="start date" duration=<number>;
  *	cron "event name" repeat="<minutes> <hours> <days of month> <months> <days of week>";
  *
  * An example of date syntax is "21 sep 98 14:29:18".
  * An example of cron syntax is "0 1 * * *", for every day at 1h.
  *
  * @see	SchedulerProxy
  */
public class SchedulerInputStream implements NotificationInputStream {
  /** token marking end of command in stream */
  static final char commandTerminator = ';';

  /** quote character for strings */
  static final char commandQuote = '"';

  /** ScheduleEvent command */	static final int CMD_SCHEDULE = 0;
  /** CronEvent command */	static final int CMD_CRON = 1;
  /** commands following CMD_* indexes */
  static final String[] commands = {		
    "schedule",
    "cron"
  };

  /** analyzed input */
  protected StreamTokenizer input;

  /** keep stream to be able to close it */
  private InputStream in;

  /**
   * Creates a filter built on top of the specified <code>InputStream</code>.
   *
   * @param in		the underlying input stream
   */
  protected SchedulerInputStream(InputStream in) throws StreamCorruptedException, IOException {
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
    input.parseNumbers();
    input.slashSlashComments(true);
  }

  /**
   * Gets a <code>Notification</code> from the stream.
   *
   * @return	a <code>ScheduleEvent</code> derived class notification
   */
  public Notification readNotification() throws IOException {
    Notification notification = null;
  parseLoop:
    while (true) {
      String eventName = null;
      Date startDate = null;
      long duration = -1;
      String repeat = null;

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
	case CMD_SCHEDULE:
	case CMD_CRON:
	  // gets event name as a string
	  if (input.nextToken() != commandQuote)
	    throw new ParseException("missing event name", 0);
	  eventName = input.sval;

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
	    // checks command is complete and builds notification
	    switch (command) {
	    case CMD_SCHEDULE:
	      if (startDate == null)
		throw new ParseException("missing start date", 0);
	      if (duration == -1) {
		notification = new ScheduleEvent(eventName, startDate);
	      } else {
		notification = new ScheduleEvent(eventName, startDate, duration);
	      }
	      break parseLoop;
	    case CMD_CRON:
	      if (repeat == null)
		throw new ParseException("missing repeat definition", 0);
	      notification = new CronEvent(eventName, repeat);
	      break parseLoop;
	    }
	    throw new ParseException("incomplete command", 0);
	  default:
	    throw new ParseException("unexpected token", 0);
	  case StreamTokenizer.TT_WORD:
	    String modifier = input.sval;
	    if (input.nextToken() != '=')
	      throw new ParseException("missing =", 0);
	    input.nextToken();
	    if (modifier.equals("start")) {
	      if (input.ttype != commandQuote)
		throw new ParseException("unexpected start token", 0);
	      // gets start date as a string, throws ParseException
	      startDate = DateFormat.getDateTimeInstance().parse(input.sval);
	    } else if (modifier.equals("duration")) {
	      if (input.ttype != StreamTokenizer.TT_NUMBER)
		throw new ParseException("unexpected duration token", 0);
	      // be sure of the integral result
	      duration = (long) (input.nval + 0.1);
	    } else if (modifier.equals("repeat")) {
	      if (input.ttype != commandQuote)
		throw new ParseException("unexpected repeat token", 0);
	      // gets repeat definition as a string
	      // actual parsing is performed in CronEvent constructor
	      repeat = input.sval;
	    } else {
	      throw new ParseException("unexpected modifier", 0);
	    }
	    break;
	  }
	  input.nextToken();
	}
      } catch (ParseException exc) {
        Debug.getLogger("fr.dyade.aaa.task").log(BasicLevel.ERROR,
                                                 "SchedulerInputStream, error reading command", exc);
	// skips input until next terminator
	while (input.ttype != commandTerminator) {
	  if (input.nextToken() == StreamTokenizer.TT_EOF)
	    throw new EOFException();
	}
      }
    }
    return notification;
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    in.close();
  }
}

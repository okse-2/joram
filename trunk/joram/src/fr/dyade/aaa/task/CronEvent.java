/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.util.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification requesting a recurrent scheduling to a <code>Scheduler</code> agent.
 * The recurring occurence time is described in a cron like syntax, that is :
 *	<minutes> <hours> <days of month> <months> <days of week>
 *	<minutes> = [ 0-59 | * ]
 *	<hours> = [ 0-23 | * ]
 *	<days of month> = [ 1-31 | * ]
 *	<months> = [ 0-11 | * ]
 *	<days of week> = [ 0-6 | * ]
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		ScheduleEvent
 */
public class CronEvent extends ScheduleEvent {

  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: CronEvent.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** minutes of hour */
  private static final int CRON_MN = 0;
  /** hours of day */
  private static final int CRON_H = 1;
  /** days of month */
  private static final int CRON_DOM = 2;
  /** months of year */
  private static final int CRON_MOY = 3;
  /** days of week */
  private static final int CRON_DOW = 4;
  /** max value (+1) for <code>CRON_*</code> constants */
  private static final int CRON_MAX = 5;

  /** string image for <code>CRON_*</code> constants, by index in the table */
  private static final String[] values = {
    "minutes", "hours", "days of month", "months of year", "days of week"
  };
  /** minimum for values designed by <code>CRON_*</code> constants */
  private static final int[] min = { 0, 0, 1, 0, 0 };
  /** maximum for values designed by <code>CRON_*</code> constants */
  private static final int[] max = { 59, 23, 31, 11, 6 };

  /** cron dates for this event */
  private BitSet ranges[] = new BitSet[CRON_MAX];


  /**
   * Creates an item.
   *
   * @param name		event and condition name
   * @param date		event scheduling date as a cron like string
   * @exception IllegalArgumentException	when date is misformed
   */
  public CronEvent(String name, String date) throws IllegalArgumentException {
    super(name, new Date());

    StringTokenizer st = new StringTokenizer(date, " ", false);
    if (st.countTokens() != CRON_MAX)
      throw new IllegalArgumentException("Bad number of tokens in cron date");

    String sched[] = new String[CRON_MAX];
    for (int i = 0; i < CRON_MAX; i ++) {
      sched[i] = st.nextToken();
    }

    for (int i = 0; i < CRON_MAX; i ++) {
      String tok = null;
      try {
	ranges[i] = new BitSet(max[i] + 1);
	st = new StringTokenizer(sched[i], ",", false);
	while (st.countTokens() > 0) {
	  // range syntax is not allowed in first version
	  tok = st.nextToken();
	  if (tok.compareTo("*") == 0) {
	    for (int j = max[i]+1; j-- > 0;)
	      ranges[i].set(j);
	  } else {
	    int j = Integer.parseInt(tok);
	    if (j < min[i] || j > max[i])
	      throw new IllegalArgumentException("Bad " + values[i] + " (" + tok + ") in cron date");
	    ranges[i].set(j);
	  }
	}
      } catch (NumberFormatException exc) {
	throw new IllegalArgumentException("Bad " + values[i] + " (" + tok + ") in cron date");
      }
    }
  }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    for (int i = 0; i < CRON_MAX; i ++) {
      output.append(",");
      output.append(values[i]);
      output.append("=");
      output.append(ranges[i]);
    }
    output.append(")");
    return output.toString();
  }

  /**
   * Returns the next scheduling date after current date given as parameter.
   * The new date must be strictly greater than the current date.
   * A <code>null</code> date leads to the scheduler deleting the event.
   *
   * @param now		current date
   * @return		next scheduling date after now
   */
  Date nextDate(Date now) {
    // avoids multiple scheduling in a minute
    long start = now.getTime() + 60000;
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(new Date(start));

    int idx_h = calendar.get(Calendar.HOUR_OF_DAY);
    int idx_mn = calendar.get(Calendar.MINUTE);

    date_loop:
    while (true) {
      while (! ranges[CRON_MOY].get(calendar.get(Calendar.MONTH))) {
	calendar.set(Calendar.DAY_OF_MONTH, 1);
	calendar.add(Calendar.MONTH, 1);
	idx_h = 0;
	idx_mn = 0;
      }
      month_block:
      // authorized month, finds next day
      while (true) {
	day_block:
	if (ranges[CRON_DOM].get(calendar.get(Calendar.DAY_OF_MONTH)) &&
	    ranges[CRON_DOW].get(calendar.get(Calendar.DAY_OF_WEEK))) {
	  // authorized day, finds next time in the day
	  while (true){
	    hour_block:
	    if (ranges[CRON_H].get(idx_h)) {
	      // finds minute in current hour
	      while (true) {
		if (ranges[CRON_MN].get(idx_mn)) {
		  // next date found
		  calendar.set(Calendar.HOUR_OF_DAY, idx_h);
		  calendar.set(Calendar.MINUTE, idx_mn);
		  break date_loop;
		}
		idx_mn ++;
		if (idx_mn > max[CRON_MN])
		  break hour_block;
	      }
	    }
	    idx_h ++;
	    idx_mn = 0;
	    if (idx_h > max[CRON_H])
	      break day_block;
	  }
	}
	calendar.add(Calendar.DAY_OF_MONTH, 1);
	idx_h = 0;
	if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
	  break month_block;
      }
    }

    // updates date variable so as to be able to add the minute
    date = calendar.getTime();
    return date;
  }
}

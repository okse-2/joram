/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.util.*;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.scheduler.event.*;

/**
 * Main to test class <code>PeriodicEvent</code>.
 *
 * @see		PeriodicEvent
 */
public class test13 extends TestCase {
  /** marker for DST offset in the string representation of a date */
  private static final String DST_MARKER = "DST";

  /**
   * Gets a Java date from a date in local string format.
   *
   * @param str
   *	the date in local string format
   *
   * @return
   *	the corresponding Java date
   * @exception IllegalArgumentException
   *	if the string is badly formed.
   */
  static public long parseDate(String str) throws IllegalArgumentException {
    int year = Integer.parseInt(str.substring(0, 4));
    int month = Integer.parseInt(str.substring(4, 6));
    int day = Integer.parseInt(str.substring(6, 8));
    int hour = Integer.parseInt(str.substring(8, 10));
    int minute = Integer.parseInt(str.substring(10, 12));
    int seconds = Integer.parseInt(str.substring(12, 14));
    boolean dst = false;
    int dstoff = 0;
    if (str.regionMatches(14, DST_MARKER, 0, DST_MARKER.length())) {
      dst = true;
      if (str.length() > (14 + DST_MARKER.length())) {
	dstoff = Integer.parseInt(str.substring((14 + DST_MARKER.length())));
      }
    }

    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month -1, day, hour, minute, seconds);
    calendar.set(Calendar.ERA, GregorianCalendar.AD);
    if (dst) {
      if (dstoff == 0) {
	TimeZone tz = calendar.getTimeZone();
	if (! (tz instanceof SimpleTimeZone))
	  throw new IllegalArgumentException("missing DST offset");
	dstoff = ((SimpleTimeZone) tz).getDSTSavings();
      }
      calendar.set(Calendar.DST_OFFSET, dstoff);
    }

    long date = calendar.getTime().getTime();
    if (dst) {
      if (calendar.get(Calendar.DST_OFFSET) != dstoff) {
	// there is a bug in jdk 1.3, when time switches from summer to winter
	// time. If the date in winter time exists, the DST offset is ignored
	date -= dstoff;
      }
    }

    return date;
  }

  /**
   * Gets a date in local string format.
   *
   * @param date
   *	the Java date to format
   * @param dst
   *	if <code>true</code> takes the <code>DST</code> field into account
   *
   * @return
   *	the date in local string format
   */
  static public String fmtDate(long date, boolean dst) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(new Date(date));
    StringBuffer buf = new StringBuffer(14);

    int year = calendar.get(Calendar.YEAR);
    if ((year >= 10000) || (year < 0))
      throw new IllegalStateException("invalid year (" + year + ")");
    else if (year < 1000) {
      if (year >= 100)
	buf.append("0");
      else if (year >= 10)
	buf.append("00");
      else
	buf.append("000");
    }
    buf.append(year);

    int month = calendar.get(Calendar.MONTH) + 1;
    if (month < 10)
      buf.append("0");
    buf.append(month);

    int day = calendar.get(Calendar.DAY_OF_MONTH);
    if (day < 10)
      buf.append("0");
    buf.append(day);

    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    if (hour < 10)
      buf.append("0");
    buf.append(hour);

    int min = calendar.get(Calendar.MINUTE);
    if (min < 10)
      buf.append("0");
    buf.append(min);

    int sec = calendar.get(Calendar.SECOND);
    if (sec < 10)
      buf.append("0");
    buf.append(sec);

    if (dst) {
      int dstoff = calendar.get(Calendar.DST_OFFSET);
      if (dstoff != 0) {
        buf.append(DST_MARKER);
        if (dstoff != 3600000) buf.append(dstoff);
      }
    }

    return buf.toString();
  }

  public static void check(int step,
                           PeriodicEvent diary, long testDate,
                           String[] ref)
    throws Exception {

    if (! diary.validateMask())
      throw new IllegalArgumentException("illegal mask");

    long date = diary.getNextDate(testDate, true);
    TestCase.assertEquals("step#" + step,
                          ref[0], fmtDate(date, true));

    for (int i=1; i<ref.length; i++) {
      date = diary.getNextDate(date, false);
      TestCase.assertEquals("step#" + step,
                            ref[i], fmtDate(date, true));
    }
  }

  /**
   * Checks the behaviour of the periodic diary with varying values for the
   * reference date, the period, and the lookup date. Plays with the DST
   * changing date, that is 3am, on October 29th 2000, in France.
   */
  public void runTest(String args[]) {
    try {
      timeout = 5000L;

      int[] maskField = null;
      int[] maskValue = null;

      String ref1[] = {
        "20001029010000DST",
        "20001029013000DST",
        "20001029020000DST",
        "20001029023000DST",
        "20001029020000",
        "20001029023000",
        "20001029030000"};

      check(1,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.MINUTE, 30)),
            parseDate("20001029010000"),
            ref1);

      String ref2[] = {
        "20001029010000DST",
        "20001029020000DST",
        "20001029020000",
        "20001029030000"};

      check(2,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.MINUTE, 60)),
            parseDate("20001029010000"),
            ref2);

      String ref3[] = {
        "20001029010000DST",
        "20001029020000DST",
        "20001029020000",
        "20001029030000"};

      check(3,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.HOUR_OF_DAY, 1)),
            parseDate("20001029010000"),
            ref3);

      String ref4[] = {
        "20001029010000DST",
        "20001029020000",
        "20001029040000"};

      check(4,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.HOUR_OF_DAY, 2)),
            parseDate("20001029010000"),
            ref4);

      String ref5[] = {
        "20001028000000DST",
        "20001029000000DST",
        "20001030000000"};

      check(5,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.DAY_OF_MONTH, 1)),
            parseDate("20001028000000"),
            ref5);

      String ref6[] = {
        "20001028010000DST",
        "20001029010000DST",
        "20001030000000"};

      check(6,
            new PeriodicEvent(
              parseDate("20000101000000"),
              new DiaryPeriod(Calendar.HOUR_OF_DAY, 24)),
            parseDate("20001028000000"),
            ref6);

      String ref7[] = {
        "20001028023000DST",
        "20001029023000DST",
        "20001030023000"};

      check(7,
            new PeriodicEvent(
              parseDate("20000101023000"),
              new DiaryPeriod(Calendar.DAY_OF_MONTH, 1)),
            parseDate("20001028000000"),
            ref7);

      String ref8[] = {
        "20000929023000DST",
        "20001029023000",
        "20001129023000"};

      check(8,
            new PeriodicEvent(
              parseDate("20000129023000"),
              new DiaryPeriod(Calendar.MONTH, 1)),
            parseDate("20000901000000"),
            ref8);

      String ref9[] = {
        "20000130000000",
        "20000229000000",
        "20000329000000DST",
        "20000429000000DST",
        "20000529000000DST",
        "20000629000000DST",
        "20000729000000DST",
        "20000829000000DST",
        "20000929000000DST",
        "20001029000000DST",
        "20001129000000",
        "20001229000000"};

      check(9,
            new PeriodicEvent(
              parseDate("20000130000000"),
              new DiaryPeriod(Calendar.MONTH, 1)),
            parseDate("20000101000000"),
            ref9);

      String ref10[] = {
        "20000130000000",
        "20000330000000DST",
        "20000430000000DST",
        "20000530000000DST",
        "20000630000000DST",
        "20000730000000DST",
        "20000830000000DST",
        "20000930000000DST",
        "20001030000000",
        "20001130000000",
        "20001230000000",
        "20010130000000"};

      maskField = new int[1];
      maskField[0] = Calendar.DAY_OF_MONTH;
      check(10,
            new PeriodicEvent(
              null,
              parseDate("20000130000000"),
              new DiaryPeriod(Calendar.MONTH, 1),
              maskField,
              null),
            parseDate("20000101000000"),
            ref10);

      String ref11[] = {
        "20000131000000",
        "20000229000000",
        "20000331000000DST",
        "20000430000000DST",
        "20000531000000DST",
        "20000630000000DST",
        "20000731000000DST",
        "20000831000000DST",
        "20000930000000DST",
        "20001031000000",
        "20001130000000",
        "20001231000000"};

      maskField = new int[1];
      maskField[0] = Calendar.DAY_OF_MONTH;
      maskValue = new int[1];
      maskValue[0] = -1;
      check(11,
            new PeriodicEvent(
              null,
              parseDate("20000131000000"),
              new DiaryPeriod(Calendar.MONTH, 1),
              maskField,
              maskValue),
            parseDate("20000101000000"),
            ref11);

      String ref12[] = {
        "19960229000000",
        "20000229000000",
        "20040229000000"};

      maskField = new int[2];
      maskField[0] = Calendar.MONTH;
      maskField[1] = Calendar.DAY_OF_MONTH;
      check(12,
            new PeriodicEvent(
              null,
              parseDate("20000229000000"),
              new DiaryPeriod(Calendar.YEAR, 1),
              maskField,
              null),
            parseDate("19960101000000"),
            ref12);

      endTest();
    } catch (Throwable exc) {
      addError(exc);
      endTest();
    }
  }

  public static void main(String args[]) {
    new test13().runTest(args);
  }
}

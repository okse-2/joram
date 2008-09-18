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
package com.scalagent.scheduler.event;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.DateFormat;

import fr.dyade.aaa.util.Strings;

/**
 * A <code>PeriodicEvent</code> object implements a repetitive event defined
 * by a reference date and a time period. The time period is qualified with
 * a time unit, so that user meaningful periods may be defined, even though
 * they are variable in length (such as a day, a month, and so on).
 * <p>
 * The reference date is used internally to keep track of the last computed
 * event occurrence.
 */
public class PeriodicEvent implements DiaryEvent {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Provides an internal computation facility to others, unrelated to
   * any specific <code>PeriodicEvent</code> object.
   *
   * @param refDate	reference date from which the result date is computed
   * @param period	period definition to add to <code>refDate</code>
   * @return		<code>refDate + period</code>
   */
  public static long getDate(long refDate, DiaryPeriod period) {
    if ((refDate == -1) ||
	(period.value == 0))
      return -1;
    PeriodicEvent pevt = new PeriodicEvent(refDate, period);
    return pevt.getNextDate(refDate);
  }

  /**
   * Checks a field value of a calendar against a reference value.
   * Handles some special -1 cases.
   *
   * @param calendar	reference date to check
   * @param field	field identifier, as a <code>Calendar</code> constant
   * @param value	reference value, may be negative
   * @return		<code>true</code> if values match
   */
  public static boolean checkField(
    Calendar calendar, int field, int value) {

    if (value >= 0) {
      if (calendar.get(field) != value)
	return false;
      return true;
    }

    // keeps the parameter calendar as it is
    Calendar clone = (Calendar) calendar.clone();
    switch (field) {
    case Calendar.DAY_OF_MONTH:
      clone.add(Calendar.DAY_OF_MONTH, -value);
      if (clone.get(Calendar.DAY_OF_MONTH) != 1)
	return false;
      return true;
    case Calendar.DAY_OF_WEEK_IN_MONTH:
      clone.add(Calendar.DAY_OF_MONTH, -7 * value);
      if (clone.get(Calendar.DAY_OF_MONTH) >= 7)
	return false;
      return true;
    default:
      // not accepted
      break;
    }

    return false;
  }

  /**
   * ID of the time zone to be used when computing dates.
   * A <code>null</code> value stands for the local time zone.
   * <p>
   * Defaults to <code>null</code>.
   */
  protected String timeZoneId = null;

  /**
   * Reference date from which event dates are computed by adding a multiple
   * of the period length. A <code>-1</code> value means that no reference date
   * is set.
   * <p>
   * Defaults to <code>-1</code>.
   */
  protected long refDate = -1;
  
  /**
   * Period definition. A <code>null</code> value stands for a no repetitive
   * event.
   * <p>
   * Defaults to <code>null</code>.
   */
  protected DiaryPeriod period = null;

  /**
   * Mask of fields to match event dates, as <code>Calendar</code> constants.
   * <p>
   * Some values for the <code>period</code> field accept a logical definition
   * of the event date, in addition to the reference date. There are only a
   * small set of <code>(period/maskField)</code> legal combinations that may
   * be validated using {@link validateMask}.
   * <p>
   * The fields are ordered according to the following:
   * <code>YEAR, MONTH, WEEK_OF_YEAR, WEEK_OF_MONTH,
   * DAY_OF_YEAR, DAY_OF_MONTH, DAY_OF_WEEK_IN_MONTH, DAY_OF_WEEK</code>.
   */
  protected int[] maskField = null;

  /**
   * Values of corresponding fields in <code>maskField</code>.
   * May be set from <code>refDate</code> using {@link validateMask}.
   * <p>
   * Positive values require an exact match, while negative values are
   * interpreted either by the <code>Calendar</code> class or directly by
   * this class. Negative values are accepted for the <code>DAY_OF_MONTH</code>
   * and <code>DAY_OF_WEEK_IN_MONTH</code> fields.
   */
  protected int[] maskValue = null;

  /**
   * Object used internally to compute dates. It is created from the
   * <code>timeZoneId</code> field when this object is created, and initialized
   * with the reference date.
   * <p>
   * A <code>Calendar</code> object holds two descriptions of a date, as a
   * <code>long</code> value, and as a number of fields. Those two descriptions
   * are always synced in <code>calendar</code>.
   */
  protected Calendar calendar;

  /**
   * Constructor setting all fields. May need a call to {@link validateMask}
   * if <code>maskField</code> is not <code>null</code>.
   *
   * @param timeZoneId
   *	id of the time zone to be used when computing dates
   * @param refDate
   *	reference date from which event dates are computed
   * @param period
   *	period definition
   * @param maskField
   *	mask of fields to match event dates, as <code>Calendar</code> constants
   * @param maskValue
   *	values of corresponding fields in <code>maskField</code>
   */
  public PeriodicEvent(String timeZoneId, long refDate, DiaryPeriod period,
		       int[] maskField, int[] maskValue) {
    this.timeZoneId = timeZoneId;
    this.refDate = refDate;
    this.period = period;
    this.maskField = maskField;
    this.maskValue = maskValue;

    if (timeZoneId == null) {
      calendar = Calendar.getInstance();
    } else {
      TimeZone tz = TimeZone.getTimeZone(timeZoneId);
      if (tz == null)
	calendar = Calendar.getInstance();
      else
	calendar = Calendar.getInstance(tz);
    }

    if (refDate != -1)
      calendar.setTime(new Date(refDate));
  }

  /**
   * Constructor with no mask.
   *
   * @param timeZoneId	id of the time zone to be used when computing dates
   * @param refDate	reference date from which event dates are computed
   * @param period	period definition
   */
  public PeriodicEvent(String timeZoneId, long refDate, DiaryPeriod period) {
    this(timeZoneId, refDate, period, null, null);
  }

  /**
   * Constructor with default time zone.
   *
   * @param refDate	reference date from which event dates are computed
   * @param period	period definition
   */
  public PeriodicEvent(long refDate, DiaryPeriod period) {
    this(null, refDate, period);
  }

  /**
   * Default constructor.
   * Creates an event with a null date and no repeat period.
   */
  public PeriodicEvent() {
    this(-1, null);
  }

  /**
   * Field accessor.
   *
   * @param timeZoneId	id of the time zone to be used when computing dates
   */
  public void setTimeZoneId(String timeZoneId) {
    this.timeZoneId = timeZoneId;
  }

  /**
   * Field accessor.
   *
   * @return	id of the time zone to be used when computing dates
   */
  public String getTimeZoneId() {
    return timeZoneId;
  }

  /**
   * Field accessor. May need an additional call to {@link validateMask}.
   *
   * @param refDate	reference date from which event dates are computed
   */
  public void setRefDate(long refDate) {
    this.refDate = refDate;
    if (refDate != -1)
      calendar.setTime(new Date(refDate));
  }

  /**
   * Field accessor.
   *
   * @return	reference date from which event dates are computed
   */
  public long getRefDate() {
    return refDate;
  }

  /**
   * Field accessor.
   *
   * @param period	period definition
   */
  public void setPeriod(DiaryPeriod period) {
    this.period = period;
  }

  /**
   * Field accessor.
   *
   * @return	period definition
   */
  public DiaryPeriod getPeriod() {
    return period;
  }

  /**
   * Field accessor.
   *
   * @param maskField
   *	mask of fields to match event dates, as <code>Calendar</code> constants
   */
  public void setMaskField(int[] maskField) {
    if ((maskField != null) && (maskField.length == 0))
      this.maskField = null;
    else
      this.maskField = maskField;
  }

  /**
   * Field accessor.
   *
   * @return	mask of fields to match event dates
   */
  public int[] getMaskField() {
    return maskField;
  }

  /**
   * Field accessor.
   *
   * @param maskValue
   *	values of corresponding fields in <code>maskField</code>
   */
  public void setMaskValue(int[] maskValue) {
    if ((maskValue != null) && (maskValue.length == 0))
      this.maskValue = null;
    else
      this.maskValue = maskValue;
  }

  /**
   * Field accessor.
   *
   * @return	values of corresponding fields in <code>maskField</code>
   */
  public int[] getMaskValue() {
    return maskValue;
  }

  /**
   * Checks the consistency of the four fields <code>refDate</code>,
   * <code>period</code>, <code>maskField</code>, and <code>maskValue</code>.
   * Updates <code>maskValue</code> when <code>null</code>.
   * <p>
   * Legal <code>(period/maskField)</code> combinations are
   * <code>(YEAR/DAY_OF_YEAR)</code>,
   * <code>(YEAR/WEEK_OF_YEAR(&DAY_OF_WEEK))</code>,
   * <code>(YEAR/MONTH&DAY_OF_MONTH)</code>,
   * <code>(YEAR/MONTH&WEEK_OF_MONTH(&DAY_OF_WEEK))</code>,
   * <code>(YEAR/MONTH&DAY_OF_WEEK_IN_MONTH(&DAY_OF_WEEK))</code>,
   * <code>(MONTH/DAY_OF_MONTH)</code>,
   * <code>(MONTH/WEEK_OF_MONTH(&DAY_OF_WEEK))</code>,
   * <code>(MONTH/DAY_OF_WEEK_IN_MONTH(&DAY_OF_WEEK))</code>.
   * The optional <code>DAY_OF_WEEK</code> mask value may be retrieved from
   * the reference date.
   *
   * @return
   *	<code>true</code> when all three values are consistent,
   *	<code>false</code> otherwise.
   */
  public boolean validateMask() {
    if ((maskField == null) ||
	(maskField.length == 0))
      return true;

    if (period == null) {
      // a sensible configuration should also set maskField to null
      return false;
    }

    boolean fillValue = false;
    if (maskValue == null) {
      fillValue = true;
      maskValue = new int[maskField.length];
    }

    // checks the reference date
    if (refDate != -1)
      calendar.setTime(new Date(refDate));

    if (maskField[0] == Calendar.YEAR)
      return false;
    int index = 0;
    switch (period.unit) {
    case Calendar.YEAR:
      switch (maskField[index]) {
      case Calendar.MONTH:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.MONTH);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.MONTH, maskValue[index]))
	    return false;
	}
	index ++;
	if (index >= maskField.length)
	  return false;
	switch (maskField[index]) {
	case Calendar.WEEK_OF_MONTH:
	  if (fillValue) {
	    if (refDate == -1)
	      return false;
	    maskValue[index] = calendar.get(Calendar.WEEK_OF_MONTH);
	  } else if (refDate != -1) {
	    if (! checkField(calendar,
			     Calendar.WEEK_OF_MONTH,
			     maskValue[index]))
	      return false;
	  }
	  index ++;
	  if (index < maskField.length) {
	    switch (maskField[index]) {
	    case Calendar.DAY_OF_WEEK:
	      if (fillValue) {
		if (refDate == -1)
		  return false;
		maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	      } else if (refDate != -1) {
		if (! checkField(calendar, Calendar.DAY_OF_WEEK, maskValue[index]))
		  return false;
	      }
	      index ++;
	      if (index >= maskField.length)
		return true;
	      return false;
	    default:
	      return false;
	    }
	  } else {
	    // gets the DAY_OF_WEEK value from the reference date
	    if (refDate == -1)
	      return false;
	    int mask[] = new int[index];
	    System.arraycopy(maskField, 0, mask, 0, maskField.length);
	    maskField = mask;
	    maskField[index] = Calendar.DAY_OF_WEEK;
	    mask = new int[index];
	    System.arraycopy(maskValue, 0, mask, 0, maskValue.length);
	    maskValue = mask;
	    maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	    return true;
	  }
	case Calendar.DAY_OF_MONTH:
	  if (fillValue) {
	    if (refDate == -1)
	      return false;
	    maskValue[index] = calendar.get(Calendar.DAY_OF_MONTH);
	  } else if (refDate != -1) {
	    if (! checkField(calendar, Calendar.DAY_OF_MONTH, maskValue[index]))
	      return false;
	  }
	  index ++;
	  if (index >= maskField.length)
	    return true;
	  return false;
	case Calendar.DAY_OF_WEEK_IN_MONTH:
	  if (fillValue) {
	    if (refDate == -1)
	      return false;
	    maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
	  } else if (refDate != -1) {
	    if (! checkField(calendar, Calendar.DAY_OF_WEEK_IN_MONTH, maskValue[index]))
	      return false;
	  }
	  index ++;
	  if (index < maskField.length) {
	    switch (maskField[index]) {
	    case Calendar.DAY_OF_WEEK:
	      if (fillValue) {
		if (refDate == -1)
		  return false;
		maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	      } else if (refDate != -1) {
		if (! checkField(calendar, Calendar.DAY_OF_WEEK, maskValue[index]))
		  return false;
	      }
	      index ++;
	      if (index >= maskField.length)
		return true;
	      return false;
	    default:
	      return false;
	    }
	  } else {
	    // gets the DAY_OF_WEEK value from the reference date
	    if (refDate == -1)
	      return false;
	    int mask[] = new int[index];
	    System.arraycopy(maskField, 0, mask, 0, maskField.length);
	    maskField = mask;
	    maskField[index] = Calendar.DAY_OF_WEEK;
	    mask = new int[index];
	    System.arraycopy(maskValue, 0, mask, 0, maskValue.length);
	    maskValue = mask;
	    maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	    return true;
	  }
	default:
	  return false;
	}
      case Calendar.WEEK_OF_YEAR:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.WEEK_OF_YEAR);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.WEEK_OF_YEAR, maskValue[index]))
	    return false;
	}
	index ++;
	if (index < maskField.length) {
	  switch (maskField[index]) {
	  case Calendar.DAY_OF_WEEK:
	    if (fillValue) {
	      if (refDate == -1)
		return false;
	      maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	    } else if (refDate != -1) {
	      if (! checkField(calendar, Calendar.DAY_OF_WEEK, maskValue[index]))
		return false;
	    }
	    index ++;
	    if (index >= maskField.length)
	      return true;
	    return false;
	  default:
	    return false;
	  }
	} else {
	  // gets the DAY_OF_WEEK value from the reference date
	  if (refDate == -1)
	    return false;
	  int mask[] = new int[index];
	  System.arraycopy(maskField, 0, mask, 0, maskField.length);
	  maskField = mask;
	  maskField[index] = Calendar.DAY_OF_WEEK;
	  mask = new int[index];
	  System.arraycopy(maskValue, 0, mask, 0, maskValue.length);
	  maskValue = mask;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	  return true;
	}
      case Calendar.DAY_OF_YEAR:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_YEAR);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.DAY_OF_YEAR, maskValue[index]))
	    return false;
	}
	index ++;
	if (index >= maskField.length)
	  return true;
	return false;
      }
      break;
    case Calendar.MONTH:
      switch (maskField[index]) {
      case Calendar.WEEK_OF_MONTH:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.WEEK_OF_MONTH);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.WEEK_OF_MONTH, maskValue[index]))
	    return false;
	}
	index ++;
	if (index < maskField.length) {
	  switch (maskField[index]) {
	  case Calendar.DAY_OF_WEEK:
	    if (fillValue) {
	      if (refDate == -1)
		return false;
	      maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	    } else if (refDate != -1) {
	      if (! checkField(calendar, Calendar.DAY_OF_WEEK, maskValue[index]))
		return false;
	    }
	    index ++;
	    if (index >= maskField.length)
	      return true;
	    return false;
	  default:
	    return false;
	  }
	} else {
	  // gets the DAY_OF_WEEK value from the reference date
	  if (refDate == -1)
	    return false;
	  int mask[] = new int[index];
	  System.arraycopy(maskField, 0, mask, 0, maskField.length);
	  maskField = mask;
	  maskField[index] = Calendar.DAY_OF_WEEK;
	  mask = new int[index];
	  System.arraycopy(maskValue, 0, mask, 0, maskValue.length);
	  maskValue = mask;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	  return true;
	}
      case Calendar.DAY_OF_MONTH:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_MONTH);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.DAY_OF_MONTH, maskValue[index]))
	    return false;
	}
	index ++;
	if (index >= maskField.length)
	  return true;
	return false;
      case Calendar.DAY_OF_WEEK_IN_MONTH:
	if (fillValue) {
	  if (refDate == -1)
	    return false;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
	} else if (refDate != -1) {
	  if (! checkField(calendar, Calendar.DAY_OF_WEEK_IN_MONTH, maskValue[index]))
	    return false;
	}
	index ++;
	if (index < maskField.length) {
	  switch (maskField[index]) {
	  case Calendar.DAY_OF_WEEK:
	    if (fillValue) {
	      if (refDate == -1)
		return false;
	      maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	    } else if (refDate != -1) {
	      if (! checkField(calendar, Calendar.DAY_OF_WEEK, maskValue[index]))
		return false;
	    }
	    index ++;
	    if (index >= maskField.length)
	      return true;
	    return false;
	  default:
	    return false;
	  }
	} else {
	  // gets the DAY_OF_WEEK value from the reference date
	  if (refDate == -1)
	    return false;
	  int mask[] = new int[index];
	  System.arraycopy(maskField, 0, mask, 0, maskField.length);
	  maskField = mask;
	  maskField[index] = Calendar.DAY_OF_WEEK;
	  mask = new int[index];
	  System.arraycopy(maskValue, 0, mask, 0, maskValue.length);
	  maskValue = mask;
	  maskValue[index] = calendar.get(Calendar.DAY_OF_WEEK);
	  return true;
	}
      default:
	return false;
      }
    default:
      // when period is the day or lower, no mask is allowed
      return false;
    }

    // not reached
    return false;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",timeZoneId=");
    Strings.toString(output, timeZoneId);
    output.append(",refDate=");
    output.append(refDate);
    if (refDate > 0) {
      output.append("(");
      output.append(DateFormat.getDateTimeInstance().format(
	new Date(refDate)));
      output.append(")");
    }
    output.append(",period=");
    output.append(period);
    output.append(",maskField=");
    Strings.toStringArray(output, maskField);
    output.append(",maskValue=");
    Strings.toStringArray(output, maskValue);
    output.append(")");
    return output.toString();
  }

  /**
   * Applies the mask on the calendar.
   */
  protected void applyMask() {
    if (maskField == null)
      return;

    int lastDayInMonth = 0;

    for (int i = 0; i < maskField.length; i ++) {
      // negative values for DAY_OF_MONTH are not handled by the Calendar
      if ((maskField[i] == Calendar.DAY_OF_MONTH) &&
	  (maskValue[i] < 0)) {
	lastDayInMonth = maskValue[i];
	calendar.set(Calendar.DAY_OF_MONTH, 33);
      } else {
	calendar.set(maskField[i], maskValue[i]);
      }
    }

    if (lastDayInMonth != 0) {
      // resolve the calendar date.
//    calendar.getTime();
      // Work-around calendar jdk1.4 bug (Id: 4685354).
      calendar.get(Calendar.MONTH);
      calendar.set(Calendar.DAY_OF_MONTH, lastDayInMonth + 1);
    }
  }

  /**
   * Checks that the calendar conforms to the mask.
   */
  protected boolean checkMask() {
    if (maskField == null)
      return true;

    for (int i = 0; i < maskField.length; i ++) {
      if ((maskValue[i] > 0) &&
	  (calendar.get(maskField[i]) != maskValue[i]))
	return false;
    }

    return true;
  }

  /**
   * Gets the next event date after the parameter date. Returns
   * <code>-1</code> if there is no such date.
   *
   * @param now
   *	starting date for the lookup
   * @param inclusive
   *	if <code>true</code> checks <code>now</code> as an answer
   * @return
   *	the next event date after now,
   *	<code>-1</code> if there is no such date
   */
  public long getNextDate(long now, boolean inclusive) {
    return getNextDate(now, 1, inclusive);
  }

  /**
   * Gets the next event date after the parameter date. Returns
   * <code>-1</code> if there is no such date.
   * Calls {@link getNextDate(long, boolean) getNextDate} with the
   * <code>inclusive</code> parameter set to <code>false</code>.
   *
   * @param now
   *	starting date for the lookup
   * @return
   *	the next event date after now,
   *	<code>-1</code> if there is no such date
   */
  public long getNextDate(long now) {
    return getNextDate(now, false);
  }

  /**
   * Gets the last event date before the parameter date. Returns
   * <code>-1</code> if there is no such date.
   *
   * @param now
   *	starting date for the backward lookup
   * @param inclusive
   *	if <code>true</code> checks <code>now</code> as an answer
   * @return
   *	the last event date before now,
   *	<code>-1</code> if there is no such date
   */
  public long getLastDate(long now, boolean inclusive) {
    return getNextDate(now, -1, inclusive);
  }

  /**
   * Gets the last event date before the parameter date. Returns
   * <code>-1</code> if there is no such date.
   * Calls {@link getLastDate(long, boolean) getLastDate} with the
   * <code>inclusive</code> parameter set to <code>false</code>.
   *
   * @param now
   *	starting date for the backward lookup
   * @return
   *	the last event date before now,
   *	<code>-1</code> if there is no such date
   */
  public long getLastDate(long now) {
    return getLastDate(now, false);
  }

  /**
   * Gets the next event date before or after the parameter date. Returns
   * <code>-1</code> if there is no such date.
   *
   * @param now
   *	starting date for the lookup
   * @param dir
   *	direction of lookup, forward when positive, backward when negative
   * @param inclusive
   *	if <code>true</code> checks <code>now</code> as an answer
   * @return
   *	the next event date before/after now,
   *	<code>-1</code> if there is no such date
   */
  private long getNextDate(long now, int dir, boolean inclusive) {
    if (dir == 0)
      return -1;
    if (dir > 0)
      dir = 1;
    else
      dir = -1;

    if (refDate == -1)
      return -1;

    // the calendar date is always set to the reference date, which is
    // a valid event date, or to the last valid event date returned
    long date = calendar.getTime().getTime();
    if (inclusive && (date == now))
      return date;

    if (period.value == 0)
      return -1;

    Calendar clone = (Calendar) calendar.clone();

    // gets a rough estimate of the number of periods between date and now
    long pnum = now - date;
    int sign = 1;
    if (pnum > 0) {
      sign = dir;
    } else {
      pnum = -pnum;
      sign = -dir;
    }
    switch (period.unit) {
    case Calendar.YEAR:
      pnum /= 12;
    case Calendar.MONTH:
      pnum /= 30;
    case Calendar.DAY_OF_MONTH:
      pnum /= 24;
    case Calendar.HOUR_OF_DAY:
      pnum /= 60;
    case Calendar.MINUTE:
      pnum /= 60;
    case Calendar.SECOND:
      pnum /= 1000;
    }
    pnum /= period.value;

    // tries to skip a large period
    pnum_loop:
    while (pnum > 3) {
      calendar.add(period.unit, (int) (pnum * period.value * dir * sign));

      // sets all fields before evaluating the calendar
      applyMask();
      if (! checkMask()) {
	// this is not a valid event date
	calendar = clone;
	clone = (Calendar) calendar.clone();
	pnum --;
	continue pnum_loop;
      }

      // this is a valid event date
      clone = (Calendar) calendar.clone();
      break pnum_loop;
    }

    date = calendar.getTime().getTime();
    if (now == date) {
      if (inclusive)
	return date;
      sign = 1;
    } else if (now > date) {
      sign = dir;
    } else {
      sign = -dir;
    }
    pnum = 1;

    main_loop:
    while (true) {
      calendar.add(period.unit, (int) (pnum * period.value * dir * sign));

      applyMask();

      date = calendar.getTime().getTime();
      if ((sign == -1) &&
	  ((((date - now) * dir) < 0) ||
	   (!inclusive && (date == now)))) {
	calendar = clone;
	return calendar.getTime().getTime();
      }

      if (! checkMask()) {
	// this is not a valid event date
	calendar = clone;
	clone = (Calendar) calendar.clone();
	pnum ++;
	continue main_loop;
      }

      // this is a valid event date
      if ((sign == 1) &&
	  ((((date - now) * dir) > 0) ||
	   (inclusive && (date == now)))) {
	return date;
      }

      clone = (Calendar) calendar.clone();
      pnum = 1;
    }

    // never reached
  }
}

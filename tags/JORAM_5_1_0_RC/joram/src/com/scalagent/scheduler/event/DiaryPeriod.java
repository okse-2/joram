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

import java.io.Serializable;
import java.util.Calendar;

/**
 * A <code>DiaryPeriod</code> object defines a time period with a choice of
 * user meaningful time units. The available time units are the relevant
 * constants in class <code>Calendar</code>.
 */
public class DiaryPeriod implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Time unit for this period, as a <code>Calendar</code> constant.
   * Available constants are <code>YEAR</code>, <code>MONTH</code>,
   * <code>DAY_OF_MONTH</code>, <code>HOUR_OF_DAY</code>, <code>MINUTE</code>,
   * <code>SECOND</code>, and <code>MILLISECOND</code>.
   * <p>
   * Defaults to <code>MILLISECOND</code>.
   */
  public int unit = Calendar.MILLISECOND;

  /**
   * Period length as a number of <code>unit</code>s.
   * <p>
   * Defaults to <code>0</code>.
   */
  public int value = 0;

  /**
   * Default constructor.
   * Creates a null length period.
   */
  public DiaryPeriod() {}

  /**
   * Constructor setting the period unit and length.
   *
   * @param unit	period time unit as a <code>Calendar</code> constant
   * @param value	period length as a number of <code>unit</code>s
   */
  public DiaryPeriod(int unit, int value) {
    switch (unit) {
    case Calendar.YEAR:
    case Calendar.MONTH:
    case Calendar.DAY_OF_MONTH:
    case Calendar.HOUR_OF_DAY:
    case Calendar.MINUTE:
    case Calendar.SECOND:
    case Calendar.MILLISECOND:
      break;
    default:
      throw new IllegalArgumentException("unknown diary period unit");
    }
    this.unit = unit;
    this.value = value;
  }

  /**
   * Constructor inferring the period unit and length from a number of
   * milliseconds. Recognized units are the second (1000), the minute (60000),
   * the hour (3600000), the day (86400000), the month (2592000000), and the
   * year (31536000000).
   *
   * @param value	period length as a number of milliseconds
   */
  public DiaryPeriod(long value) {
    final long MS_SECOND = 1000;
    final long MS_MINUTE = 60 * MS_SECOND;
    final long MS_HOUR = 60 * MS_MINUTE;
    final long MS_DAY = 24 * MS_HOUR;
    final long MS_MONTH = 30 * MS_DAY;
    final long MS_YEAR = 365 * MS_DAY;

    if (value == 0) {
      // uses default values
    } else if ((value % MS_YEAR) == 0) {
      this.unit = Calendar.YEAR;
      this.value = (int) (value / MS_YEAR);
    } else if ((value % MS_MONTH) == 0) {
      this.unit = Calendar.MONTH;
      this.value = (int) (value / MS_MONTH);
    } else if ((value % MS_DAY) == 0) {
      this.unit = Calendar.DAY_OF_MONTH;
      this.value = (int) (value / MS_DAY);
    } else if ((value % MS_HOUR) == 0) {
      this.unit = Calendar.HOUR_OF_DAY;
      this.value = (int) (value / MS_HOUR);
    } else if ((value % MS_MINUTE) == 0) {
      this.unit = Calendar.MINUTE;
      this.value = (int) (value / MS_MINUTE);
    } else if ((value % MS_SECOND) == 0) {
      this.unit = Calendar.SECOND;
      this.value = (int) (value / MS_SECOND);
    } else {
      this.unit = Calendar.MILLISECOND;
      this.value = (int) (value);
    }
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
    output.append(",unit=");
    output.append(unit);
    output.append(",value=");
    output.append(value);
    output.append(")");
    return output.toString();
  }
}

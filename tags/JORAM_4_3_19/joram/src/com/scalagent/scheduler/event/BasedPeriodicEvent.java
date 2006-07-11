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

/**
 * The <code>PeriodicEvent</code> class extends <code>PeriodicEvent</code>
 * to allow for a dynamic setting of the reference date. This extension does
 * not allow field masking.
 */
public class BasedPeriodicEvent extends PeriodicEvent
  implements BasedDiaryEvent {
  /**
   * Default constructor.
   * Creates an event to be initialized by the field accessors.
   */
  public BasedPeriodicEvent() {}

  /**
   * Constructor setting all fields.
   *
   * @param timeZoneId	id of the time zone to be used when computing dates
   * @param period	period definition
   */
  public BasedPeriodicEvent(String timeZoneId, DiaryPeriod period) {
    super(timeZoneId, -1, period);
  }

  /**
   * Constructor with default time zone.
   *
   * @param period	period definition
   */
  public BasedPeriodicEvent(DiaryPeriod period) {
    this(null, period);
  }

  /**
   * Sets the base date from which the event dates are computed.
   * Actually sets {@link refDate}.
   *
   * @param baseDate
   *	base date from which the event dates are computed
   */
  public void setBaseDate(long baseDate) {
    setRefDate(baseDate);
  }
}

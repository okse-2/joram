/*
 * Copyright (C) 2001 - SCALAGENT
 */
package fr.dyade.aaa.task;

/**
 * The <code>PeriodicEvent</code> class extends <code>PeriodicEvent</code>
 * to allow for a dynamic setting of the reference date. This extension does
 * not allow field masking.
 */
public class BasedPeriodicEvent extends PeriodicEvent
  implements BasedDiaryEvent {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: BasedPeriodicEvent.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

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

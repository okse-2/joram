/*
 * Copyright (C) 2001 - SCALAGENT
 */
package fr.dyade.aaa.task;

/**
 * The <code>BasedDiaryEvent</code> specializes <code>DiaryEvent</code> to
 * allow the dynamic setting of a base date from which the event dates are
 * computed.
 */
public interface BasedDiaryEvent extends DiaryEvent {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: BasedDiaryEvent.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

  /**
   * Sets the base date from which the event dates are computed.
   *
   * @param baseDate
   *	base date from which the event dates are computed
   */
  public void setBaseDate(long baseDate);
}

/*
 * Copyright (C) 2001 - SCALAGENT
 */
package fr.dyade.aaa.task;

import java.io.Serializable;

/**
 * The <code>DiaryEvent</code> interface allows access to the occurrence dates
 * of an event that could be stored in a diary.
 */
public interface DiaryEvent extends Serializable {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: DiaryEvent.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

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
  public long getNextDate(long now, boolean inclusive);

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
  public long getNextDate(long now);

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
  public long getLastDate(long now, boolean inclusive);

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
  public long getLastDate(long now);
}

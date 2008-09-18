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
 * The <code>DiaryEvent</code> interface allows access to the occurrence dates
 * of an event that could be stored in a diary.
 */
public interface DiaryEvent extends java.io.Serializable {
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

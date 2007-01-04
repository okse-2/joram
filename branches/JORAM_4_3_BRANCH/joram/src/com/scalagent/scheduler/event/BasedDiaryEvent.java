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
 * The <code>BasedDiaryEvent</code> specializes <code>DiaryEvent</code> to
 * allow the dynamic setting of a base date from which the event dates are
 * computed.
 */
public interface BasedDiaryEvent extends DiaryEvent {
  /**
   * Sets the base date from which the event dates are computed.
   *
   * @param baseDate
   *	base date from which the event dates are computed
   */
  public void setBaseDate(long baseDate);
}

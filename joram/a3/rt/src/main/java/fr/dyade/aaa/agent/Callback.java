/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.util.List;

/**
 * Callback interface notified either when a <code>CountDownCallback</code>
 * transmitted by a <code>CallbackNotification</code> reaches zero. If at least
 * one error has been raised, the method <code>failed</code> is called.
 * Otherwise the method <code>done</code> is called.
 */
public interface Callback {
  
  /**
   * Called if no error has been raised.
   */
  void done();

  /**
   * Called if at least one error has been raised.
   * 
   * @param errors
   *          the errors raised by the <code>CountDownCallback</code>
   *          transmitted by a <code>CallbackNotification</code>
   */
  void failed(List<Throwable> errors);

}

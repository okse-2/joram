/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.selectors;

import org.objectweb.joram.shared.selectors.Checker;
import org.objectweb.joram.shared.selectors.Lexer;
import org.objectweb.joram.shared.excepts.SelectorException;

/**
 * The <code>ClientSelector</code> class is used for checking the correctness
 * of a selector client side.
 */
public class ClientSelector {
  /**
   * Clients call this method to check a selector syntax.
   *
   * @return  <code>true</code> when the syntax is ok.
   * @exception SelectorException  When the selector syntax is incorrect.
   */
  public static boolean checks(String selector) throws SelectorException
  {
    if (selector == null || selector.equals(""))
      return true;

    try {
      Checker checker = new Checker(new Lexer(selector));
      return ((Boolean) checker.parse().value).booleanValue();
    } catch (SelectorException sE) {
      throw sE;
    } catch (Throwable t) {
      throw new SelectorException("Invalid selector: " + t.getMessage());
    }
  }
}
  

/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):  ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.selectors;

import org.objectweb.joram.shared.excepts.SelectorException;
import org.objectweb.joram.shared.messages.Message;

/**
 * The <code>Selector</code> class is used for filtering messages according
 * to their header fields and properties.
 */
public class Selector
{
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
    }
    catch (SelectorException sE) {
      throw sE;
    }
    catch (Throwable t) {
      throw new SelectorException("Invalid selector: " + t.getMessage());
    }
  }

  /**
   * Destinations call this method to filter a message according to a selector.
   *
   * @return  <code>true</code> when the selection matches, <code>false</code>
   *          otherwise.
   */
  public static boolean matches(Message message, String selector)
  {
    if (selector == null || selector.equals(""))
      return true;

    try {
      Filter filter = new Filter(new Lexer(selector), message, "JMS");
      Boolean result = (Boolean) filter.parse().value;

      if (result == null)
        return false;
    
      return result.booleanValue();
    }
    catch (Throwable t) {
      return false;
    }
  }
}
  

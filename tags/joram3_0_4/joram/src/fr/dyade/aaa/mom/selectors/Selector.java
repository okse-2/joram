/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.selectors;

import fr.dyade.aaa.mom.excepts.SelectorException;
import fr.dyade.aaa.mom.messages.Message;

/**
 * The <code>Selector</code> class is used for filtering messages according
 * to their header fields and properties.
 */
public class Selector
{
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
      Filter filter = new Filter(new Lexer(selector), message);
      Boolean result = (Boolean) filter.parse().value;

      if (result == null)
        return false;
    
      return result.booleanValue();
    }
    catch (Throwable t) {
      return false;
    }
  }

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
}
  

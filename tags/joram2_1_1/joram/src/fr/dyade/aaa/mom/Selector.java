/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.mom; 
 
/** 
 * <code>fr.dyade.aaa.mom.Selector</code> provides the method
 * for comparing a given <code>fr.dyade.aaa.mom.Message</code>
 * to a String selector.
 */ 
public class Selector implements java.io.Serializable
{ 
  /** Constructor. */
  public Selector()
  {}

  /**
   * Method checking if the <code>fr.dyade.aaa.mom.Message</code>
   * matches with the selector.
   */
  public boolean matches(Message msg, String selector)
    throws Exception
  {
    if (selector == null || selector.equals(""))
      // No selector => returning true.
      return true;

    fr.dyade.aaa.mom.selectors.parser selectorParser =
      new fr.dyade.aaa.mom.selectors.parser(
      new fr.dyade.aaa.mom.selectors.Lexer(selector), msg);

    Object result = selectorParser.parse().value;
   
    // If the parsing result is unknown, selector does not match. 
    if (result == null)
      return false;

    else
      return ((Boolean) result).booleanValue();
  }

}

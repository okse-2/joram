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
package fr.dyade.aaa.agent;

/**
 * Thrown to indicate that the specified agent serverdoes not exist. 
 *
 * @author  Andre Freyssinet
 */
public class UnknownServerException extends Exception {

public static final String RCS_VERSION="@(#)$Id: UnknownServerException.java,v 1.1 2001-05-04 15:04:05 tachkeni Exp $"; 

  /**
   *  Constructs a new <code>UnknownServerException</code> with no
   * detail message.
   */
  public UnknownServerException() {
    super();
  }

  /**
   *  Constructs a new <code>UnknownServerException</code> with the
   * specified  detail message. 
   *
   * @param   s   the detail message.
   */
  public UnknownServerException(String s) {
    super(s);
  }
}

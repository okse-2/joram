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


package fr.dyade.aaa.util;

/**
 * Thrown by methods in the <code>Queue</code> class to indicate 
 * that the queue is empty. 
 *
 * @author  Andr* Freyssinet
 * @version 1.0, 10/22/97
 * @see     fr.dyade.aaa.util.Queue
 */
public class EmptyQueueException extends RuntimeException {

public static final String RCS_VERSION="@(#)$Id: EmptyQueueException.java,v 1.1.1.1 2000-05-30 11:45:21 tachkeni Exp $"; 

  /**
   * Constructs a new <code>EmptyQueueException</code> with no detail message.
   */
  public EmptyQueueException() {}
}

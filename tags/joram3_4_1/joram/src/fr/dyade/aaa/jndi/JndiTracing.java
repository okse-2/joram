/*
 * Copyright (C) 2001 - 2002 SCALAGENT
 * Copyright (C) 1996 - 2001 BULL
 * Copyright (C) 1996 - 2001 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.jndi 
 * and fr.dyade.aaa.joram, released October, 2001. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.jndi;

import org.objectweb.util.monolog.api.Logger;

/**
 * The <code>JndiTracing</code> class centralizes the log tracing for jndi.
 */
public class JndiTracing
{
  public static Logger dbg = null;
  private static boolean initialized = false;


  /**
   * Initializes the package by setting the various loggers.
   */
  static
  {
    dbg = fr.dyade.aaa.agent.Debug.getLogger("fr.dyade.aaa.jndi");
  }
}

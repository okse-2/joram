/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.mom;

import fr.dyade.aaa.agent.Debug;
import org.objectweb.monolog.api.Monitor;

/**
 * The <code>MomTracing</code> class centralizes the log tracing for the MOM.
 */
public class MomTracing
{
  /**
   * Logger used to trace destinations activity, to be set using topic
   * fr.dyade.aaa.mom.Destination
   */
  public static Monitor dbgDestination = null;

  /**
   * Logger used to trace proxies activity, to be set using topic
   * fr.dyade.aaa.mom.Proxy
   */
  public static Monitor dbgProxy = null;

  /**
   * <code>true</code> when <code>init</code> has been called.
   */
  private static boolean initialized = false;


  /**
   * Initializes the package by setting the various loggers.
   */
  static
  {
    dbgDestination = Debug.getMonitor("fr.dyade.aaa.mom.Destination");
    dbgProxy = Debug.getMonitor("fr.dyade.aaa.mom.Proxy");
  }
}
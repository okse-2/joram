/*
 * Copyright (C) 2002 SCALAGENT
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
 * XML Wrapper interface for A3 configuration file.
 */
public interface A3CMLWrapper {
  /**
   * Parses the xml file named <code>cfgFileName</code>.
   *
   * @param cfgFileName    the name of the xml file
   * @param name    	   the name of the configuration
   * @param serverId       the id of the local server
   *
   * @exception Exception  unspecialized error
   */
  public A3CMLHandler parse(java.io.Reader reader,
                            String name,
                            short serverId) throws Exception;
}

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

import java.io.*;

/**
 * Description of a service.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 */
public final class ServiceDesc implements Serializable {
public static final String RCS_VERSION="@(#)$Id: ServiceDesc.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";

  /** service class name */
  public String className;

  /** starting parameters, may be null */
  public String parameters;

  /**
   * Constructor.
   *
   * @param	className	service class name
   * @param	parameters	starting parameters, may be null
   */
  public ServiceDesc(String className,
		     String parameters) {
    this.className = className;
    this.parameters = parameters;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",className=" + className +
      ",parameters=" + parameters + ")";
  }
}

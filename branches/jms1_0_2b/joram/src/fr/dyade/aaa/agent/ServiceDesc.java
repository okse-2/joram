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
public static final String RCS_VERSION="@(#)$Id: ServiceDesc.java,v 1.9 2002-03-26 16:08:39 joram Exp $";

  /** service class name */
  String scname;

  /** starting arguments, may be null */
  String args;

  /** */
  boolean initialized;

  /** */
  boolean running;

  /**
   * Constructor.
   *
   * @param	scname	service class name
   * @param	args	starting parameters, may be null
   */
  public ServiceDesc(String scname,
		     String args) {
    this.scname = scname;
    this.args = args;
    this.initialized = false;
    this.running = false;
  }

  /**
   * Gets the class name for service.
   *
   * @return the classname.
   */
  public String getClassName() {
    return scname;
  }

  /**
   * Gets the starting arguments for service.
   *
   * @return the arguments.
   */
  public String getArguments() {
    return args;
  }

  /**
   * Tests if this <code>Service</code> is initialized.
   *
   * @return true if the <code>Service</code> is initialized.
   */
  public boolean isInitialized()  {
    return initialized;
  }

  /**
   * Set the initialized property.
   */
  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  /**
   * Tests if this <code>Service</code> is running.
   *
   * @return true if the <code>Service</code> is running.
   */
  public boolean isRunning()  {
    return running;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",scname=" + scname +
      ",args=" + args +
      ",initialized=" + initialized +
      ",running=" + running + ")";
  }
}

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
 * Thrown by the <code>Agentfactory</code> when an error is
 * raised during the initialization.
 */
public class AgentException extends Exception {

  /**
   * This class does not use the <code>initCause</code>
   * because this class handles the deserialization
   * of the cause that may fail if its class is 
   * unknown.
   */
  private Throwable cause;

  public AgentException(Throwable cause) {
    super(cause.toString());
    this.cause = cause;
  }

  /**
   * Overrides the <code>Throwable</code> behavior 
   * in order to handle the deserialization
   * of the cause that may fail if its class
   * is unknown.
   *
   * @return the cause or <code>null</code> if
   * its class is unknown.
   */
  public final Throwable getCause() {
    return cause;
  }

  private void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException {
    out.writeObject(cause);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
    try {
      this.cause = (Throwable)in.readObject();
    } catch (ClassNotFoundException exc) {
      // Do nothing
    }
  }

}

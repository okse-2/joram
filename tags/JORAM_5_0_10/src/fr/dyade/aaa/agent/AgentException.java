/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 */
package fr.dyade.aaa.agent;

/**
 * Thrown by the <code>Agentfactory</code> when an error is
 * raised during the initialization.
 */
public class AgentException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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

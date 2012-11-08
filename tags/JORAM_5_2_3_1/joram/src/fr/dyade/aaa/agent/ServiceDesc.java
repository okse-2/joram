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

import java.io.IOException;
import java.io.Serializable;

/**
 * Description of a service.
 */
public final class ServiceDesc implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** service class name */
  transient String scname;

  /** starting arguments, may be null */
  transient String args;

  /** */
  transient boolean initialized;

  /** */
  transient boolean running;

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

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    out.writeUTF(scname);
    if (args != null)
      out.writeUTF(args);
    else
      out.writeUTF("");
    out.writeBoolean(initialized);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    scname = in.readUTF();
    args = in.readUTF();
    if (args.length() == 0)
      args = null;
    initialized = in.readBoolean();
    running = false;
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
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(").append(super.toString());
    strBuf.append(",scname=").append(scname);
    strBuf.append(",args=").append(args);
    strBuf.append(",initialized=").append(initialized);
    strBuf.append(",running=").append(running);
    strBuf.append(")");
    return strBuf.toString();
  }
}

/*
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 2004 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package fr.dyade.aaa.agent;

import java.io.*;

/**
 *  This interface is used by object that need to carry additionnal data
 * between HA nodes.
 */
public interface BagSerializer {
  /**
   * The readBag method is responsible for reading from the stream and
   * restoring the agent's transient state.
   */
  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException;

  /**
   * The writeBag method is responsible for writing the extra data
   * of this particular agent so that the corresponding readBag method
   * can restore it.
   */
  public void writeBag(ObjectOutputStream out)
    throws IOException;
}

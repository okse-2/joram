/*
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import fr.dyade.aaa.agent.AgentServer;

/**
 * The <code>ResolverObjectInputStream</code> is an {@link ObjectInputStream}
 * which tries to resolve classes using the {@link ResolverRepository} in case
 * of a {@link ClassNotFoundException}. This is necessary when using OSGi, as
 * the agent server can handle objects unknown to its own classloader.
 */
public class ResolverObjectInputStream extends ObjectInputStream {

  public ResolverObjectInputStream(InputStream in) throws IOException {
    super(in);
  }

  protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    try {
      return super.resolveClass(desc);
    } catch (ClassNotFoundException cnfe) {
      return AgentServer.getResolverRepository().resolveClass(desc.getName());
    }
  }

}

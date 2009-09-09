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
package org.objectweb.joram.mom.osgi;

import fr.dyade.aaa.util.Resolver;

public class MOMResolver implements Resolver {
  
  private static final String[] PACKAGES = new String[] {
      "org.objectweb.joram.mom.notifications",
      "org.objectweb.joram.mom.dest",
      "org.objectweb.joram.mom.dest.jmsbridge",
      "org.objectweb.joram.mom.proxies",
      "org.objectweb.joram.mom.proxies.tcp",
      "org.objectweb.joram.mom.messages",
      "org.objectweb.joram.shared.security",
      "org.objectweb.joram.shared.security.jaas",
      "com.scalagent.joram.mom.dest.collector",
      "com.scalagent.joram.mom.dest.ftp",
      "com.scalagent.joram.mom.dest.scheduler" };

  public Class resolveClass(String objName) throws ClassNotFoundException {
    return Class.forName(objName);
  }

  public String[] getResolvedPackages() {
    return PACKAGES;
  }

}

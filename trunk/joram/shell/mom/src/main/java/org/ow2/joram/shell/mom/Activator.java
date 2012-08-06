/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.mom;

import java.util.Hashtable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ow2.joram.shell.mom.commands.MOMCommands;
import org.ow2.joram.shell.mom.commands.MOMCommandsImpl;

import fr.dyade.aaa.agent.Debug;

public class Activator implements BundleActivator {
  public static Logger logger = Debug.getLogger(Activator.class.getName());

  private BundleContext bundleContext;

  public void start(BundleContext context) throws Exception {
    Hashtable<String, Object> prop = new Hashtable<String, Object>();
      //CommandProcessor.COMMAND_SCOPE="osgi.command.scope"
      prop.put("osgi.command.scope",
          MOMCommandsImpl.NAMESPACE);
      //CommandProcessor.COMMAND_FUNCTION="osgi.command.function"
      prop.put("osgi.command.function",
          MOMCommandsImpl.COMMANDS);
    this.bundleContext = context;
    MOMCommandsImpl.init(bundleContext);
    try {
      bundleContext.registerService(MOMCommands.class.getCanonicalName(),
          MOMCommandsImpl.getInstance(), prop);
    } catch(Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, e);
    }
  }

  public void stop(BundleContext context) throws Exception {
  }

}

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
package org.ow2.shelbie.command.joram;

import org.ow2.shelbie.ICommandHandler;
import org.ow2.shelbie.IExecutionContext;

import fr.dyade.aaa.agent.AgentServer;

public class JoramStopCommandHandler implements ICommandHandler {

  /**
   * Execution of the command. Stops the agent server.
   * 
   * @param executionContext
   *          Context which stores all command options and a reference on
   *          {@link JoramStartCommand}.
   * @return The command execution result.
   */
  public Object execute(final IExecutionContext executionContext) {
    AgentServer.stop();
    return "Agent server stopped";
  }
}
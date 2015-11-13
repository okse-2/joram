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

import javax.jms.Connection;

import org.objectweb.joram.client.jms.admin.AdminWrapper;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.ow2.shelbie.CommandException;
import org.ow2.shelbie.ICommandHandler;
import org.ow2.shelbie.IExecutionContext;

public class JoramListCommandHandler implements ICommandHandler {

  /**
   * Execution of the command. Lists all queues / topics / users on the local
   * server.
   * 
   * @param executionContext
   *          Context which stores all command options and a reference on
   *          {@link JoramStartCommand}.
   * @return The command execution result.
   */
  public Object execute(final IExecutionContext executionContext) throws CommandException {

    JoramListCommand.JoramCreateOption option = (JoramListCommand.JoramCreateOption) executionContext
        .getOptions();

    AdminWrapper wrapper;
    try {
      Connection connection;
      if (option.authUser != null && option.authPass != null) {
        connection = new LocalConnectionFactory().createConnection(option.authUser, option.authPass);
      } else if (option.authUser != null || option.authPass != null) {
        throw new CommandException(
            "You must specify user and password options (or none of them to use defaults).");
      } else {
        connection = new LocalConnectionFactory().createConnection("root", "root");
      }
      connection.start();
      wrapper = new AdminWrapper(connection);
    } catch (Exception exc) {
      throw new CommandException("Error when connecting to Joram server: " + exc.getMessage(), exc);
    }
    
    JoramListCommandResult result = new JoramListCommandResult();
    try {
      result.destinations = wrapper.getDestinations();
    } catch (Exception exc) {
      throw new CommandException("Error when retrieving destinations: " + exc.getMessage(), exc);
    }
    
    try {
      result.users = wrapper.getUsers();
    } catch (Exception exc) {
      throw new CommandException("Error when retrieving users: " + exc.getMessage(), exc);
    }
    wrapper.close();

    return result;
  }

}
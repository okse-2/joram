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

import java.util.Iterator;

import javax.jms.Connection;

import org.objectweb.joram.client.jms.admin.AdminWrapper;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.ow2.shelbie.CommandException;
import org.ow2.shelbie.ICommandHandler;
import org.ow2.shelbie.IExecutionContext;

public class JoramCreateCommandHandler implements ICommandHandler {

  /**
   * Execution of the command. Creates the requested queues / topic / users on
   * the local server.
   * 
   * @param executionContext
   *          Context which stores all command options and a reference on
   *          {@link JoramCreateCommand}.
   * @return The command execution result.
   */
  public Object execute(final IExecutionContext executionContext) throws CommandException {

    JoramCreateCommand.JoramCreateOption option = (JoramCreateCommand.JoramCreateOption) executionContext
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
    
    if (option.queues != null) {
      if (option.sid == -1) {
        for (Iterator<String> iterator = option.queues.iterator(); iterator.hasNext();) {
          String queueName = iterator.next();
          try {
            wrapper.createQueue(queueName);
          } catch (Exception exc) {
            throw new CommandException(
                "Error when creating queue " + queueName + ": " + exc.getMessage(), exc);
          }
        }
      } else {
        for (Iterator<String> iterator = option.queues.iterator(); iterator.hasNext();) {
          String queueName = iterator.next();
          try {
            wrapper.createQueue(option.sid, queueName);
          } catch (Exception exc) {
            throw new CommandException(
                "Error when creating queue " + queueName + ": " + exc.getMessage(), exc);
          }
        }
      }
    }
    
    if (option.topics != null) {
      if (option.sid == -1) {
        for (Iterator<String> iterator = option.topics.iterator(); iterator.hasNext();) {
          String topicName = iterator.next();
          try {
            wrapper.createTopic(topicName);
          } catch (Exception exc) {
            throw new CommandException(
                "Error when creating topic " + topicName + ": " + exc.getMessage(), exc);
          }
        }
      } else {
        for (Iterator<String> iterator = option.topics.iterator(); iterator.hasNext();) {
          String topicName = iterator.next();
          try {
            wrapper.createTopic(option.sid, topicName);
          } catch (Exception exc) {
            throw new CommandException(
                "Error when creating topic " + topicName + ": " + exc.getMessage(), exc);
          }
        }
      }
    }
    
    if (option.user != null) {
      if (option.sid == -1) {
        for (Iterator<String> iterator = option.user.iterator(); iterator.hasNext();) {
          String user = iterator.next();
          String name;
          String password;
          int index = user.indexOf(':');
          if (index != -1) {
            name = user.substring(0, index);
            password = user.substring(index + 1, user.length());
          } else {
            name = user;
            password = user;
          }
          try {
            wrapper.createUser(name, password);
          } catch (Exception exc) {
            throw new CommandException("Error when creating user " + user + ": " + exc.getMessage(), exc);
          }
        }
      } else {
        for (Iterator<String> iterator = option.user.iterator(); iterator.hasNext();) {
          String user = iterator.next();
          String name;
          String password;
          int index = user.indexOf(':');
          if (index != -1) {
            name = user.substring(0, index);
            password = user.substring(index + 1, user.length());
          } else {
            name = user;
            password = user;
          }
          try {
            wrapper.createUser(name, password, option.sid);
          } catch (Exception exc) {
            throw new CommandException("Error when creating user " + user + ": " + exc.getMessage(), exc);
          }
        }
      }
    }
    
    wrapper.close();

    return "Creation done.";
  }

}
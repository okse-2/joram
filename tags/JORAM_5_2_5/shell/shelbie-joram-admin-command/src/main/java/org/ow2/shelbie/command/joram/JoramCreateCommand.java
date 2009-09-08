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

import java.util.List;

import org.kohsuke.args4j.Option;
import org.ow2.shelbie.DefaultCommandSupport;
import org.ow2.shelbie.ICommandHandler;

public class JoramCreateCommand extends DefaultCommandSupport {

  /**
   * Returns the class that has to be called to execute the command.
   * 
   * @return A class which implements ICommandHandler interface.
   */
  public Class<? extends ICommandHandler> getCommandHandler() {
    return JoramCreateCommandHandler.class;
  }

  /**
   * @return A textual description of the command.
   */
  public String getDescription() {
    return "Create queues, topics and users.";
  }

  /**
   * @return The name of the command.
   */
  public String getName() {
    return "joram_create";
  }

  /**
   * Specifies the class that contains the options.
   * 
   * @see org.ow2.shelbie.ICommand#getOption()
   * @return The class of {@link JoramCreateOption}
   */
  public Class<?> getOption() {
    return JoramCreateOption.class;
  }

  /**
   * An inner class which stores the command line option in order to be
   * available both for the handler and the renderer.
   */
  public static class JoramCreateOption {

    // TODO replace this option by an authentication command
    @Option(required = false, name = "-au", aliases = "-auth-user", usage = "User name used to connect to JORAM server.")
    public String authUser;

    // TODO replace this option by an authentication command
    @Option(required = false, name = "-ap", aliases = "-auth-pass", usage = "User password used to connect to JORAM server.")
    public String authPass;
    
    @Option(required = false, name = "-s", aliases = "-sid", usage = "Id of the server where the destination will be created.")
    public int sid = -1;

    @Option(required = false, name = "-q", aliases = "-queue", usage = "Create a queue with the specified name.")
    public List<String> queues;

    @Option(required = false, name = "-t", aliases = "-topic", usage = "Create a topic with the specified name.")
    public List<String> topics;
    
    @Option(required = false, name = "-u", aliases = "-user", usage = "Create a user as specified -> login:password.")
    public List<String> user;

  }

}

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

import org.kohsuke.args4j.Option;
import org.ow2.shelbie.DefaultCommandSupport;
import org.ow2.shelbie.ICommandHandler;

public class JoramStartCommand extends DefaultCommandSupport {

  /**
   * Returns the class that has to be called to execute the command.
   * 
   * @return A class which implements ICommandHandler interface.
   */
  public Class<? extends ICommandHandler> getCommandHandler() {
    return JoramStartCommandHandler.class;
  }

  /**
   * @return A textual description of the command.
   */
  public String getDescription() {
    return "Start the collocated JORAM server";
  }

  /**
   * @return The name of the command.
   */
  public String getName() {
    return "joram_start";
  }

  /**
   * Specifies the class that contains the options.
   * 
   * @see org.ow2.shelbie.ICommand#getOption()
   * @return The class of {@link JoramStartOption}
   */
  public Class<?> getOption() {
    return JoramStartOption.class;
  }

  /**
   * An inner class which stores the command line option in order to be
   * available both for the handler and the renderer.
   */
  public static class JoramStartOption {

    @Option(required = true, name = "-sid", usage = "server id")
    public int sid;

    @Option(required = true, name = "-dir", usage = "persisting directory name")
    public String dir;

  }

}

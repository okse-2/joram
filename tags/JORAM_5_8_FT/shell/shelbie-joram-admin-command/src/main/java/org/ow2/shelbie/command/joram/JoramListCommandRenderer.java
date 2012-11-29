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

import org.ow2.shelbie.ICommandRenderer;
import org.ow2.shelbie.IConsoleRenderer;
import org.ow2.shelbie.IExecutionContext;

public class JoramListCommandRenderer implements ICommandRenderer {

  /**
   * Display JORAM destinations and users present in
   * {@link JoramListCommandResult}.
   * 
   * @see org.ow2.shelbie.ICommandRenderer#displayResult(java.io.OutputStream,
   *      java.lang.Object)
   * @param out
   *          The outputstream to write result.
   * @param commandExecutionResult
   *          The execution result of command, a {@link JoramListCommandResult}
   *          object.
   * @param executionContext
   *          The context that stores command options.
   */
  public void displayResult(final IConsoleRenderer out, final Object commandExecutionResult,
      final IExecutionContext executionContext) {
    JoramListCommandResult result = (JoramListCommandResult) commandExecutionResult;
    if (result.destinations != null) {
      out.println("JORAM destinations:");
      for (int i = 0; i < result.destinations.length; i++) {
        out.println("\t" + result.destinations[i]);
      }
      out.println("");
    }
    
    if (result.destinations != null) {
      out.println("JORAM users:");
      for (int i = 0; i < result.users.length; i++) {
        out.println("\t" + result.users[i]);
      }
      out.println("");
    }
  }
}

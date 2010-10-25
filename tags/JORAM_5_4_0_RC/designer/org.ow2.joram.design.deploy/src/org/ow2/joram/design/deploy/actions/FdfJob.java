/*
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.ow2.joram.design.deploy.actions;

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.objectweb.fdf.components.fdf.lib.runnable.Launcher;
import org.objectweb.fdf.util.printer.lib.io.PrintStreamPrinterFactory;
import org.ow2.joram.design.deploy.JoramDeployPlugin;

public class FdfJob extends Job {

  public static final String INSTALL_ACTION = "install";

  public static final String START_ACTION = "start";

  public static final String STOP_ACTION = "stop";

  public static final String UNINSTALL_ACTION = "uninstall";

  private String action;

  private String path;

  public FdfJob(String path, String action) {
    super("FDF deployment.");
    this.path = path;
    this.action = action;
  }

  protected IStatus run(IProgressMonitor monitor) {
    System.setProperty("fractal.provider", "org.objectweb.fractal.julia.Julia");
    System.setProperty("julia.config", "fdf-julia.cfg");

    monitor.beginTask("Deploying configuration", IProgressMonitor.UNKNOWN);
    MessageConsole console = findConsole("FDF");
    console.activate();
    PrintStreamPrinterFactory.printStream = new PrintStream(console.newOutputStream());

    try {
      String[] args = { "-fractal", Launcher.class.getName() + "(" + path + "," + action + ",,,,,)", "r" };
      org.objectweb.fractal.adl.Launcher.main(args);
    } catch (Exception exc) {
      return new Status(IStatus.ERROR, JoramDeployPlugin.ID, exc.getMessage(), exc);
    }
    monitor.done();

    return new Status(IStatus.OK, JoramDeployPlugin.ID, "");
  }

  private MessageConsole findConsole(String name) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++)
      if (name.equals(existing[i].getName()))
        return (MessageConsole) existing[i];
    //no console found, so create a new one
    MessageConsole myConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[] { myConsole });
    return myConsole;
  }

}

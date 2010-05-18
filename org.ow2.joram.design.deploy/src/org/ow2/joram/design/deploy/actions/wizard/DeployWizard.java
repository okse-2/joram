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
package org.ow2.joram.design.deploy.actions.wizard;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.ow2.joram.design.deploy.JoramDeployPlugin;
import org.ow2.joram.design.deploy.actions.FdfJob;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.Host;
import org.ow2.joram.design.model.joram.ScalAgentServer;

public class DeployWizard extends Wizard {

  private SelectA3ConfWizardPage page1;

  private DeployPreferencesWizardPage page2;

  private WizardNewFileCreationPage page3;

  private IStructuredSelection myselection;

  protected Config joramConf;

  public DeployWizard(Config conf, IStructuredSelection myselection) {
    this.joramConf = conf;
    this.myselection = myselection;
    setDialogSettings(JoramDeployPlugin.getDefault().getDialogSettings());
  }

  public boolean performFinish() {
    try {
      IFile outputFile = page3.createNewFile();
      String confName = outputFile.getName().substring(0,
          outputFile.getName().length() - outputFile.getFileExtension().length() - 1);
      String generated = generateDeploywareConf(confName, joramConf);
      outputFile.setContents(new ByteArrayInputStream(generated.getBytes()), true, true, null);

      new FdfJob(outputFile.getLocation().toOSString(), FdfJob.INSTALL_ACTION).schedule();

    } catch (Exception exc) {
      MessageDialog.openError(getShell(), "Deploy Plug-in", "Deployware file creation failed: "
          + exc.getMessage());
      return false;
    }
    return true;
  }

  public void addPages() {
    page1 = new SelectA3ConfWizardPage(myselection);
    addPage(page1);

    page2 = new DeployPreferencesWizardPage();
    addPage(page2);

    page3 = new WizardNewFileCreationPage("DeploywareNewFileCreationPage", myselection);
    page3.setFileName("MyJoram.fdf");
    page3.setFileExtension("fdf");
    page3.setAllowExistingResources(true);
    page3.setTitle("Save deployware configuration and deploy.");
    page3.setDescription("Create a new deployment file.");

    addPage(page3);
  }

  public boolean canFinish() {
    if (this.getContainer().getCurrentPage() == page3) {
      return true;
    }
    return false;
  }

  /**
   * Generates the contents of the configuration file using the selected
   * elements.
   */
  private String generateDeploywareConf(String confName, Config rootElement) {

    StringBuilder sb = new StringBuilder();
    
    sb.append(confName + " = FDF.RUNNABLE(Joram-Servers)\n");
    sb.append("{\n");

    sb.append("  # Description of hosts\n");
    sb.append("  Hosts = INTERNET.NETWORK {\n");
    sb.append("\n");

    for (Host host : rootElement.getHosts()) {
      sb.append("    " + host.getHostName() + " = INTERNET.HOST {\n");
      sb.append("      hostname = INTERNET.HOSTNAME(" + host.getHostName() + ");\n");
      sb.append("      user     = INTERNET.USER(" + host.getLogin() + "," + host.getPassword() + ","
          + (host.getPrivateKeyPath() == null ? "" : host.getPrivateKeyPath()) + ");\n");
      sb.append("      transfer = TRANSFER." + host.getTransfert() + ";\n");
      sb.append("      protocol = PROTOCOL." + host.getProtocol() + ";\n");
      sb.append("      shell    = SHELL." + host.getShell() + ";\n");
      sb.append("    }\n");
    }
    sb.append("  }\n");

    sb.append("\n");
    sb.append("\n");
    sb.append("  Joram-Servers = FDF.SEQUENTIAL-COLLECTION(Joram Deployment) {\n");
    for (ScalAgentServer server : rootElement.getServers()) {
      sb.append("\n");
      sb.append("    joram-" + server.getHost().getHostName() + '-' + server.getSid()
              + " = JORAM.SERVER {\n");
      sb.append("      archive    = JORAM.ARCHIVE(" + page2.getArchivePath() + ");\n");
      sb.append("      a3servers  = JORAM.A3SERVERS(" + page1.getSelectedA3Conf().getLocation().toOSString() + ");\n");
      sb.append("      home       = JORAM.HOME(" + page2.getDeployDirectory() + "/s"
          + server.getSid() + ");\n");
      sb.append("      host       = Hosts/" + server.getHost().getHostName() + ";\n");
      sb.append("      serverid   = JORAM.ID(" + server.getSid() + ");\n");
      sb.append("    }\n");
    }
    sb.append("  }\n");
    sb.append("}\n");
    sb.append("\n");
    
    return sb.toString();
  }

}

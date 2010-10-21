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
package org.ow2.joram.design.model.export.wizard;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.ow2.joram.design.model.export.wizard.SelectEntryPointWizardPage.EntryPoint;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionFactory;
import org.ow2.joram.design.model.joram.ConnectionFactoryClass;
import org.ow2.joram.design.model.joram.Destination;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.JNDIServer;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.Queue;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.Topic;
import org.ow2.joram.design.model.joram.User;

public class ExportAdminWizard extends Wizard {

  private SelectEntryPointWizardPage page1;

  private SelectJNDIWizardPage page2;

  private SelectElementsWizardPage page3;

  private WizardNewFileCreationPage page4;

  private IStructuredSelection myselection;

  protected Config joramConf;

  public ExportAdminWizard(Config conf, IStructuredSelection myselection) {
    this.joramConf = conf;
    this.myselection = myselection;
  }

  public boolean performFinish() {
    try {
      String generated = generateAdminConf(page1.getEntryPoint(), page2.getJNDI(), page3.getElements());
      IFile outputFile = page4.createNewFile();
      outputFile.setContents(new ByteArrayInputStream(generated.getBytes()), true, true, null);
    } catch (Exception exc) {
      MessageDialog.openError(getShell(), "Export Plug-in", "Export to JoramAdmin.xml failed: "
          + exc.getMessage());
      return false;
    }
    return true;
  }

  public void addPages() {
    page1 = new SelectEntryPointWizardPage();
    addPage(page1);

    page2 = new SelectJNDIWizardPage();
    addPage(page2);

    page3 = new SelectElementsWizardPage();
    addPage(page3);

    page4 = new WizardNewFileCreationPage("JoramAdminNewFileCreationPage", myselection);
    page4.setFileName("JoramAdmin.xml");
    page4.setFileExtension("xml");
    page4.setAllowExistingResources(true);
    page4.setTitle("Save admin script.");
    page4.setDescription("Create a new script.");

    addPage(page4);
  }

  public boolean canFinish() {
    if (this.getContainer().getCurrentPage() == page4 && page4.isPageComplete()) {
      return true;
    }
    return false;
  }

  /**
   * Generates the contents of the configuration file using the selected
   * elements.
   */
  private String generateAdminConf(EntryPoint entrypoint, JoramService jndi, Object[] exportedElements) {

    StringBuilder sb = new StringBuilder();
    sb.append("<JoramAdmin>");
    sb.append("\n");
    if (entrypoint.tcp == null) {
      sb.append("  <AdminModule>\n");
      sb.append("    <collocatedConnect name='" + entrypoint.cm.getUser() + "'\n");
      sb.append("                       password='" + entrypoint.cm.getPassword() + "'/>\n");
      sb.append("  </AdminModule>\n");
      sb.append("\n");
    } else {
      sb.append("  <AdminModule>\n");
      sb.append("    <connect host='" + entrypoint.hostname + "'\n");
      sb.append("             port='" + entrypoint.tcp.getPort() + "'\n");
      sb.append("             name='" + entrypoint.cm.getUser() + "'\n");
      sb.append("             password='" + entrypoint.cm.getPassword() + "'/>\n");
      sb.append("  </AdminModule>\n");
      sb.append("\n");
    }

    if (jndi != null) {
      int jndiport = -1;
      if (jndi instanceof JNDIServer) {
        jndiport = ((JNDIServer) jndi).getPort();
      } else {
        jndiport = ((DistributedJNDIServer) jndi).getPort();
      }
      sb.append("  <InitialContext>\n");
      sb.append("    <property name='java.naming.factory.initial' value='fr.dyade.aaa.jndi2.client.NamingContextFactory'/>\n");
      sb.append("    <property name='java.naming.factory.host' value='"
          + ((ScalAgentServer) jndi.eContainer()).getHost().getHostName() + "'/>\n");
      sb.append("    <property name='java.naming.factory.port' value='" + jndiport + "'/>\n");
      sb.append("  </InitialContext>\n");
      sb.append("\n");
    }

    short currentSid = -1;
    for (Object element : exportedElements) {

      if (element instanceof ScalAgentServer) {
        ScalAgentServer server = (ScalAgentServer) element;
        currentSid = server.getSid();

      } else if (element instanceof Destination) {
        sb.append("\n");
        Destination destination = (Destination) element;
        if (destination instanceof Topic) {
          Topic topic = (Topic) destination;
          sb.append("  <Topic name='" + topic.getName() + "' serverId='" + currentSid + "' className='"
              + topic.getClassName() + "'>\n");
        } else {
          Queue queue = (Queue) destination;
          sb.append("  <Queue name='" + queue.getName() + "' serverId='" + currentSid + "' className='"
              + queue.getClassName() + "'");
          if (queue.getDeadMessageQueue() != null) {
            sb.append(" dmq='" + queue.getDeadMessageQueue().getName() + "'");
          }
          sb.append(">\n");
        }
        if (destination.isFreeReader()) {
          sb.append("    <freeReader/>\n");
        }
        if (destination.isFreeWriter()) {
          sb.append("    <freeWriter/>\n");
        }
        EList<User> users = destination.getReaders();
        for (User user : users) {
          sb.append("    <reader user='" + user.getName() + "'/>\n");
        }
        users = destination.getWriters();
        for (User user : users) {
          sb.append("    <writer user='" + user.getName() + "'/>\n");
        }
        if (destination.getJndiName() != null) {
          sb.append("    <jndi name='" + destination.getJndiName() + "'/>\n");
        }
        if (destination instanceof Topic) {
          sb.append("  </Topic>\n");
        } else {
          sb.append("  </Queue>\n");
        }

      } else if (element instanceof User) {
        sb.append("\n");
        User user = (User) element;
        sb.append("  <User name='" + user.getName() + "' password='" + user.getPassword() + "' serverId='"
            + currentSid + "'/>\n");

      } else if (element instanceof ConnectionFactory) {
        sb.append("\n");
        ConnectionFactory cf = (ConnectionFactory) element;
        sb.append("  <ConnectionFactory className='" + cf.getClassName() + "'>\n");
        if (cf.getClassName().equals(ConnectionFactoryClass.TCP_CONNECTION_FACTORY)) {
          sb.append("    <tcp>\n");
        } else if (cf.getClassName().equals(ConnectionFactoryClass.SOAP_CONNECTION_FACTORY)) {
          sb.append("    <soap>\n");
        } else if (cf.getClassName().equals(ConnectionFactoryClass.LOCAL_CONNECTION_FACTORY)) {
          sb.append("    <local>\n");
        }
        if (cf.getJndiName() != null) {
          sb.append("    <jndi name='" + cf.getJndiName() + "'/>\n");
        }
        sb.append("  </ConnectionFactory>\n");
      }
    }
    sb.append("</JoramAdmin>\n");

    return sb.toString();
  }

}

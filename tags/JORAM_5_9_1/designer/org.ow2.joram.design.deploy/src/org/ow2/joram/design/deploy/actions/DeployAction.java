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

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ow2.joram.design.deploy.actions.wizard.DeployWizard;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.ScalAgentServer;

public class DeployAction implements IObjectActionDelegate {

  private Shell shell;

  private ISelection selection;

  public DeployAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    shell = targetPart.getSite().getShell();
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    if (selection instanceof TreeSelection) {

      final TreeSelection tree = (TreeSelection) selection;
      final Object o = tree.getFirstElement();

      if (o instanceof IFile) {

        try {

          final IFile joramModelFile = (IFile) o;

          final JoramPackage ePackage = JoramPackage.eINSTANCE;
          // Create resource set and register your generated resource factory.
          final ResourceSet resourceSet = new ResourceSetImpl();
          resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("joram",
              new XMIResourceFactoryImpl());

          final Resource resource = resourceSet.getResource(URI.createFileURI(joramModelFile.getLocationURI()
              .getPath()), true);
          final Config rootElement = (Config) resource.getContents().get(0);
          
          for (ScalAgentServer server : rootElement.getServers()) {
            if (server.getHost() == null) {
              throw new Exception("Can't deploy when a server has no host defined.");
            }
          }

          WizardDialog wizard = new WizardDialog(shell, new DeployWizard(rootElement, tree));
          wizard.setHelpAvailable(false);
          wizard.setPageSize(300, 250);
          wizard.open();

        } catch (Exception e) {
          MessageDialog.openError(shell, "Deploy Plug-in", "Deployment failed: "
              + e.getMessage());
        }
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

}

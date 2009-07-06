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
package org.ow2.joram.design.model.export;

import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ExportAction implements IObjectActionDelegate {

  /**
   * 
   */
  private ISelection selection;

  /**
   * 
   */
  private Shell shell;

  /**
   * Constructor for Action1.
   */
  public ExportAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    shell = targetPart.getSite().getShell();
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(final IAction action) {

    if (selection instanceof TreeSelection) {

      final TreeSelection tree = (TreeSelection) selection;
      final Object o = tree.getFirstElement();

      if (o instanceof IFile) {

        try {

          final IFile joramModelFile = (IFile) o;

          /* create the correct URI for topology.xml based on *.joram */
          URI outputFileURi = URI.createFileURI(joramModelFile.getLocation().toOSString());
          outputFileURi = outputFileURi.trimSegments(1);
          outputFileURi = outputFileURi.appendSegment("a3servers.xml");

          TransformerFactory tFactory = TransformerFactory.newInstance();
          URL url = Platform.getBundle("org.ow2.joram.design.diagram").getResource(
              "org/ow2/joram/design/model/export/xslt/joram.xsl");
          Transformer transformer = tFactory.newTransformer(new StreamSource(url.toURI().toString()));
          transformer.transform(new StreamSource(joramModelFile.getContents()), new StreamResult(
              outputFileURi.devicePath()));

          joramModelFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
          MessageDialog.openInformation(shell, "Export Plug-in", "Export to a3servers.xml completed.");

        } catch (Exception e) {
          MessageDialog.openError(shell, "Export Plug-in", "Export to a3servers.xml failed: "
              + e.getMessage());
          e.printStackTrace();
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

  public static void main(String[] args) throws Exception {
    TransformerFactory tFactory = TransformerFactory.newInstance();
    URL url = ClassLoader
        .getSystemResource("org/ow2/jasmine/design/diagram/export/popup/actions/xslt/joram.xsl");
    Transformer transformer = tFactory.newTransformer(new StreamSource(url.toURI().toString()));
    transformer.transform(new StreamSource("default.joram"), new StreamResult("a3servers.xml"));
  }

}

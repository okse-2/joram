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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class SelectA3ConfWizardPage extends WizardPage {

  TreeViewer treeViewer;

  IStructuredSelection myselection;

  protected SelectA3ConfWizardPage(IStructuredSelection myselection) {
    super(SelectA3ConfWizardPage.class.getName(), "Select A3 configuration file.", null);
    setDescription("a3servers.xml");
    this.myselection = myselection;
  }

  public void createControl(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout gl = new GridLayout();
    composite.setLayout(gl);

    treeViewer = new TreeViewer(composite, SWT.BORDER);
    treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    treeViewer.setLabelProvider(new WorkbenchLabelProvider());
    WorkbenchContentProvider wbcp = new WorkbenchContentProvider();
    treeViewer.setContentProvider(wbcp);
    treeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
    treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event) {
        setMessage(null);
        IResource obj = (IResource) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
        if (obj instanceof IFile) {
          if (!obj.getName().equals("a3servers.xml")) {
            setMessage("By convention, this file is usually named 'a3servers.xml'", WARNING);
          }
          setPageComplete(true);
        } else {
          setPageComplete(false);
        }
      }

    });

    // Select a3servers.xml if found or model file container
    IContainer container = ((IResource) myselection.getFirstElement()).getParent();
    treeViewer.setSelection(new StructuredSelection(container));
    Object[] childrens = wbcp.getChildren(container);
    for (int i = 0; i < childrens.length; i++) {
      Object element = childrens[i];
      if (element instanceof IFile) {
        IFile file = (IFile) element;
        if (file.getName().equals("a3servers.xml")) {
          treeViewer.setSelection(new StructuredSelection(file));
          break;
        }
      }
    }

    GridData data = new GridData(GridData.FILL_BOTH);
    Tree treeWidget = treeViewer.getTree();
    treeWidget.setLayoutData(data);
    treeWidget.setFont(parent.getFont());

    setControl(composite);
  }

  IFile getSelectedA3Conf() {
    if (isPageComplete()) {
      return (IFile) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
    }
    return null;
  }

}

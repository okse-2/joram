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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.ow2.joram.design.model.export.wizard.SelectEntryPointWizardPage.EntryPoint;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionFactory;
import org.ow2.joram.design.model.joram.Destination;
import org.ow2.joram.design.model.joram.JORAM;
import org.ow2.joram.design.model.joram.Queue;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.Topic;
import org.ow2.joram.design.model.joram.User;

public class SelectElementsWizardPage extends WizardPage {
  
  ITreeContentProvider fContentProvider;

  CheckboxTreeViewer treeViewer;

  Config fInput;

  private boolean firstTime;

  protected SelectElementsWizardPage() {
    super("SelectElementsWizardPage", "Select elements to create.", null);
    setDescription("Select the elements which will be in the script.");
    firstTime = true;
  }

  Object[] getElements() {
    return treeViewer.getCheckedElements();
  }

  public void setVisible(boolean visible) {
    // Select objects on the server where we are connected when the
    // page is shown for the first time.
    if (visible && firstTime) {
      EntryPoint entryPoint = ((SelectEntryPointWizardPage) getPreviousPage().getPreviousPage())
          .getEntryPoint();
      if (entryPoint != null) {
        treeViewer.setChecked(entryPoint.cm.eContainer(), true);
        validCheckedElements();
      }
      firstTime = false;
    }
    super.setVisible(visible);
  }

  public void createControl(Composite parent) {

    fInput = ((ExportAdminWizard) getWizard()).joramConf;
    fContentProvider = new JoramServerTreeContentProvider();
    
    Composite composite = new Composite(parent, SWT.NONE);
    
    GridLayout gl = new GridLayout();
    composite.setLayout(gl);
    
    treeViewer = new ContainerCheckedTreeViewer(composite, SWT.BORDER);
    treeViewer.setContentProvider(fContentProvider);
    treeViewer.setInput(fInput);
    treeViewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        validCheckedElements();
      }
    });
    treeViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        if (element instanceof ScalAgentServer) {
          ScalAgentServer server = (ScalAgentServer) element;
          return "Server " + server.getSid() + " (" + server.getName() + ')';
        } else if (element instanceof Queue) {
          Queue queue = (Queue) element;
          return "Queue " + queue.getName();
        } else if (element instanceof Topic) {
          Topic topic = (Topic) element;
          return "Topic " + topic.getName();
        } else if (element instanceof User) {
          User user = (User) element;
          return "User " + user.getName();
        } else if (element instanceof ConnectionFactory) {
          ConnectionFactory cf = (ConnectionFactory) element;
          return "ConnectionFactory " + cf.getJndiName();
        } else {
          return element.toString();
        }
      }
    });
    
    createSelectionButtons(composite);
    GridData data = new GridData(GridData.FILL_BOTH);
    Tree treeWidget = treeViewer.getTree();
    treeWidget.setLayoutData(data);
    treeWidget.setFont(parent.getFont());
    
    treeViewer.setExpandedElements(fInput.getServers().toArray());
    
    setControl(composite);
  }
  
  protected void validCheckedElements() {
    Object[] checkedElements = treeViewer.getCheckedElements();
    for (int i = 0; i < checkedElements.length; i++) {
      if (checkedElements[i] instanceof Destination) {
        Destination destination = (Destination) checkedElements[i];
        if (destination.getJndiName() == null) {
          setMessage("Some elements have no JNDI name, they will not be registered.", WARNING);
          return;
        }
      } else if (checkedElements[i] instanceof ConnectionFactory) {
        ConnectionFactory cf = (ConnectionFactory) checkedElements[i];
        if (cf.getJndiName() == null) {
          setMessage("Some elements have no JNDI name, they will not be registered.", WARNING);
          return;
        }
      }
    }
    setMessage(null);
  }

  protected void createSelectionButtons(Composite composite) {
    Composite buttonComposite = new Composite(composite, SWT.RIGHT);
    GridLayout layout = new GridLayout();
    layout.numColumns = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    buttonComposite.setLayout(layout);
    buttonComposite.setFont(composite.getFont());
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
    data.grabExcessHorizontalSpace = true;
    buttonComposite.setLayoutData(data);
    Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, "Select &All");
    SelectionListener listener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Object[] viewerElements = fContentProvider.getElements(fInput);
        treeViewer.setCheckedElements(viewerElements);
        validCheckedElements();
      }
    };
    selectButton.addSelectionListener(listener);
    Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, "&Deselect All");
    listener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        treeViewer.setCheckedElements(new Object[0]);
        validCheckedElements();
      }
    };
    deselectButton.addSelectionListener(listener);
  }

  protected Button createButton(Composite parent, int id, String label) {
    // increment the number of columns in the button bar
    ((GridLayout) parent.getLayout()).numColumns++;
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    button.setFont(JFaceResources.getDialogFont());
    button.setData(new Integer(id));
    setButtonLayoutData(button);
    return button;
  }

  static class JoramServerTreeContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof JORAM) {
        return ((JORAM) parentElement).getJmsObjects().toArray();
      }
      return null;
    }

    public Object getParent(Object element) {
      return ((EObject) element).eContainer();
    }

    public boolean hasChildren(Object element) {
      if (element instanceof JORAM) {
        return ((JORAM) element).getJmsObjects().size() > 0;
      }
      return false;
    }

    public Object[] getElements(Object inputElement) {
      return ((Config) inputElement).getServers().toArray();
    }

    public void dispose() {
      // do nothing
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
    }

  }
}

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.JNDIServer;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.ScalAgentServer;

public class SelectJNDIWizardPage extends WizardPage {

  private TableViewer fTableViewer;

  protected SelectJNDIWizardPage() {
    super("SelectJNDIWizardPage", "Select JNDI server.", null);
    setDescription("Select the jndi used to register objects. A JNDIServer service is needed.");
  }

  JNDIServer getJNDI() {
    return (JNDIServer) ((IStructuredSelection) fTableViewer.getSelection()).getFirstElement();
  }

  public void createControl(Composite parent) {
    Config joramConf = ((ExportAdminWizard) getWizard()).joramConf;

    EList<ScalAgentServer> servers = joramConf.getServers();
    List<JoramService> jndis = new ArrayList<JoramService>();
    for (ScalAgentServer server : servers) {
      List<JoramService> services = server.getServices();
      for (JoramService service : services) {
        if (service instanceof JNDIServer || service instanceof DistributedJNDIServer) {
          jndis.add(service);
        }
      }
    }

    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout gl = new GridLayout();
    composite.setLayout(gl);

    fTableViewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    fTableViewer.setContentProvider(new ArrayContentProvider());
    fTableViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        if (element instanceof JNDIServer) {
          JNDIServer jndi = (JNDIServer) element;
          ScalAgentServer server = (ScalAgentServer) jndi.eContainer();
          return "JNDI on server " + server.getSid() + " (" + server.getHost().getHostName() + ':'
              + jndi.getPort() + ')';
        } else if (element instanceof DistributedJNDIServer) {
          DistributedJNDIServer jndi = (DistributedJNDIServer) element;
          ScalAgentServer server = (ScalAgentServer) jndi.eContainer();
          return "Distributed JNDI on server " + server.getSid() + " (" + server.getHost().getHostName()
              + ':' + jndi.getPort() + ')';
        } else {
          return element.toString();
        }
      }
    });
    fTableViewer.setInput(jndis);

    GridData gd = new GridData(GridData.FILL_BOTH);
    Table table = fTableViewer.getTable();
    table.setLayoutData(gd);
    table.setFont(parent.getFont());
    setControl(composite);
  }

}

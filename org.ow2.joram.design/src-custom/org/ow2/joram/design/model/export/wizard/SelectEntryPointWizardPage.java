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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionManager;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.TCPProxyService;

public class SelectEntryPointWizardPage extends WizardPage {

  private static final int TCP_MODE = 0;

  private static final int COLLOCATED_MODE = 1;

  private TableViewer fTcpTableViewer;

  private TableViewer fCollocatedTableViewer;

  private Button collocatedButton;

  private Button tcpButton;

  private List<EntryPoint> tcpentrypoints;

  private List<EntryPoint> collocatedentrypoints;

  protected SelectEntryPointWizardPage() {
    super(SelectEntryPointWizardPage.class.getName(), "Select entry point.", null);
    setDescription("Select the server where the connection will be done.");
  }

  EntryPoint getEntryPoint() {
    if (collocatedButton.getSelection()) {
      return (EntryPoint) ((IStructuredSelection) fCollocatedTableViewer.getSelection()).getFirstElement();
    }
    return (EntryPoint) ((IStructuredSelection) fTcpTableViewer.getSelection()).getFirstElement();
  }

  public void createControl(Composite parent) {

    Config joramConf = ((ExportAdminWizard) getWizard()).joramConf;

    EList<ScalAgentServer> servers = joramConf.getServers();
    tcpentrypoints = new ArrayList<EntryPoint>();
    collocatedentrypoints = new ArrayList<EntryPoint>();
    for (ScalAgentServer server : servers) {
      List<JoramService> services = server.getServices();
      EntryPoint entryPoint = new EntryPoint();
      for (JoramService service : services) {
        if (service instanceof TCPProxyService) {
          entryPoint.tcp = (TCPProxyService) service;
        } else if ((service instanceof ConnectionManager)) {
          entryPoint.cm = (ConnectionManager) service;
          EntryPoint ep = new EntryPoint();
          ep.sid = server.getSid();
          ep.hostname = server.getHost().getHostName();
          ep.cm = (ConnectionManager) service;
          collocatedentrypoints.add(ep);
        }
      }
      if (entryPoint.cm != null && entryPoint.tcp != null) {
        entryPoint.sid = server.getSid();
        entryPoint.hostname = server.getHost().getHostName();
        tcpentrypoints.add(entryPoint);
      }
    }

    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout gl = new GridLayout();
    composite.setLayout(gl);

    Label titleLabel = new Label(composite, SWT.NONE);
    titleLabel.setText("Choose entry point used for script execution:");

    // TCP table
    tcpButton = new Button(composite, SWT.RADIO);
    tcpButton.setText("TCP connection");

    fTcpTableViewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    fTcpTableViewer.setContentProvider(new ArrayContentProvider());
    fTcpTableViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        EntryPoint entry = (EntryPoint) element;
        return "Server " + entry.sid + " (" + entry.cm.getUser() + '@' + entry.hostname + ':'
            + entry.tcp.getPort() + ')';
      }
    });
    fTcpTableViewer.setInput(tcpentrypoints);
    if (!tcpentrypoints.isEmpty()) {
      fTcpTableViewer.setSelection(new StructuredSelection(tcpentrypoints.get(0)));
    }

    GridData gd = new GridData(GridData.FILL_BOTH);
    Table table = fTcpTableViewer.getTable();
    table.setLayoutData(gd);
    table.setFont(parent.getFont());

    // Collocated table
    collocatedButton = new Button(composite, SWT.RADIO);
    collocatedButton.setText("Collocated connection");

    fCollocatedTableViewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    fCollocatedTableViewer.setContentProvider(new ArrayContentProvider());
    fCollocatedTableViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        EntryPoint entry = (EntryPoint) element;
        return "Server " + entry.sid + " (" + entry.cm.getUser() + '@' + entry.hostname + ')';
      }
    });
    fCollocatedTableViewer.setInput(collocatedentrypoints);
    if (!collocatedentrypoints.isEmpty()) {
      fCollocatedTableViewer.setSelection(new StructuredSelection(collocatedentrypoints.get(0)));
    }

    gd = new GridData(GridData.FILL_BOTH);
    table = fCollocatedTableViewer.getTable();
    table.setLayoutData(gd);
    table.setFont(parent.getFont());

    setControl(composite);

    // Select tcp as default
    tcpButton.setSelection(true);
    setMode(TCP_MODE);

    // Add listeners
    collocatedButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (!collocatedButton.getSelection())
          return;
        setMode(COLLOCATED_MODE);
      }
    });

    tcpButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (!tcpButton.getSelection())
          return;
        setMode(TCP_MODE);
      }
    });
  }
  
  void setMode(int mode) {
    setErrorMessage(null);
    setPageComplete(true);
    if (mode == COLLOCATED_MODE) {
      fTcpTableViewer.getTable().setEnabled(false);
      fCollocatedTableViewer.getTable().setEnabled(true);
      if (collocatedentrypoints.isEmpty()) {
        setErrorMessage("No server with ConnectionManager found: no entry point to connect to.");
        setPageComplete(false);
      }
    } else if (mode == TCP_MODE) {
      fTcpTableViewer.getTable().setEnabled(true);
      fCollocatedTableViewer.getTable().setEnabled(false);
      if (tcpentrypoints.isEmpty()) {
        setErrorMessage("No server with TCPProxyService found: no entry point to connect to.");
        setPageComplete(false);
      }
    }
  }

  static class EntryPoint {

    public short sid;

    public String hostname;

    public ConnectionManager cm;

    public TCPProxyService tcp;

  }

}

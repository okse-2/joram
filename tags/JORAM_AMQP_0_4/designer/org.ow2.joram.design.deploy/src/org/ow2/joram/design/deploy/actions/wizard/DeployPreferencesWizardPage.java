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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DeployPreferencesWizardPage extends WizardPage {

  private static final int SIZING_TEXT_FIELD_WIDTH = 250;

  private static final char SEPARATOR = System.getProperty("file.separator").charAt(0);

  private static final String LAST_SELECTED_RELEASE_PATH = "ReleasePath";

  private static final String LAST_SELECTED_DEPLOY_PATH = "DeployPath";

  private Text archivePathField;

  private Button archiveBrowseButton;

  private Text deployDirField;

  protected DeployPreferencesWizardPage() {
    super(DeployPreferencesWizardPage.class.getName(), "Deployment customization.", null);
    setDescription("Select deployment preferences.");
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout gl = new GridLayout();
    gl.numColumns = 2;
    composite.setLayout(gl);

    Label label = new Label(composite, SWT.NONE);
    label.setText("Select joram archive release file:");
    GridData data = new GridData(GridData.BEGINNING);
    data.horizontalSpan = 2;
    label.setLayoutData(data);

    archivePathField = new Text(composite, SWT.BORDER);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    archivePathField.setLayoutData(data);

    archiveBrowseButton = new Button(composite, SWT.PUSH);
    archiveBrowseButton.setText("Browse");
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    archiveBrowseButton.setLayoutData(data);

    createLine(composite, 2);

    label = new Label(composite, SWT.NONE);
    label.setText("Choose existing remote installation path:");
    data = new GridData(GridData.BEGINNING);
    data.horizontalSpan = 2;
    label.setLayoutData(data);

    deployDirField = new Text(composite, SWT.BORDER);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    data.horizontalSpan = 2;
    deployDirField.setLayoutData(data);
    
    // Add listeners
    archivePathField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        getDialogSettings().put(LAST_SELECTED_RELEASE_PATH, archivePathField.getText());
        validate();
      }
    });

    archiveBrowseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handlePatchFileBrowseButtonPressed();
      }
    });

    deployDirField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        getDialogSettings().put(LAST_SELECTED_DEPLOY_PATH, deployDirField.getText());
        validate();
      }
    });

    // Put last known values in fields
    if (getDialogSettings().get(LAST_SELECTED_RELEASE_PATH) != null) {
      archivePathField.setText(getDialogSettings().get(LAST_SELECTED_RELEASE_PATH));
    }
    if (getDialogSettings().get(LAST_SELECTED_DEPLOY_PATH) != null) {
      deployDirField.setText(getDialogSettings().get(LAST_SELECTED_DEPLOY_PATH));
    }

    setControl(composite);
  }

  public String getDeployDirectory() {
    return deployDirField.getText();
  }

  public String getArchivePath() {
    return archivePathField.getText();
  }

  protected void handlePatchFileBrowseButtonPressed() {
    FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
    dialog.setText("Select joram.tgz");
    String patchFilePath = getPatchFilePath();
    if (patchFilePath != null) {
      int lastSegment = patchFilePath.lastIndexOf(SEPARATOR);
      if (lastSegment > 0) {
        patchFilePath = patchFilePath.substring(0, lastSegment);
      }
    }
    dialog.setFilterPath(patchFilePath);
    String res = dialog.open();
    if (res == null)
      return;

    patchFilePath = dialog.getFileName();
    IPath filterPath = new Path(dialog.getFilterPath());
    IPath path = filterPath.append(patchFilePath).makeAbsolute();
    patchFilePath = path.toOSString();

    archivePathField.setText(patchFilePath);
  }

  protected void validate() {
    if (!new File(archivePathField.getText()).exists()) {
      setMessage("Joram release archive file not found", ERROR);
      setPageComplete(false);
      return;
    } else if (deployDirField.getText() == null || deployDirField.getText().isEmpty()) {
      setMessage("Deploy directory path is empty.", ERROR);
      setPageComplete(false);
      return;
    }
    setMessage(null);
    setPageComplete(true);
  }


  private String getPatchFilePath() {
    if (archivePathField != null)
      return archivePathField.getText();
    return ""; //$NON-NLS-1$
  }

  private void createLine(Composite parent, int ncol) {
    Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.BOLD);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = ncol;
    line.setLayoutData(gridData);
  }

}
